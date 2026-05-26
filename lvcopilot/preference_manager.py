"""Architect Preference Learning for LVCopilot.

Captures the architect's design and coding style preferences from their
Phase 2/3 feedback (rejections and modification requests), persists them
to a project-local JSON file, and injects them into the system prompt so
the agent's future solutions align with the architect's thought process.

Extraction runs in a background thread using a separately configurable
LLM model so it never blocks the main conversation.

Configure via environment variables:
    ``LV_PREF_MODEL``          — LLM model for preference extraction (optional, falls back to LLM_MODEL)
    ``LV_PREF_API_KEY``        — API key for the preference model (optional, falls back to LLM_API_KEY)
    ``LV_PREF_API_BASE``       — API base URL for the preference model (optional, falls back to LLM_API_BASE)
    ``LV_MAX_PREFERENCES``     — Maximum stored preferences (default: 30)
"""

import json
import os
import re
import threading
import uuid
from datetime import datetime

# pyrefly: ignore [missing-import]
import litellm


# ── Extraction prompt ─────────────────────────────────────────────────────

_EXTRACTION_PROMPT = """\
You are analyzing a conversation between a Software Architect and an AI Developer Agent. \
The Architect has just rejected or requested modifications to the Agent's proposal.

Your task: Decide how to handle this feedback as a learned preference for the Architect's \
design and coding style. You must think smartly — only store preferences that will \
meaningfully guide the Agent in future conversations.

AGENT'S PROPOSAL (what was rejected/modified):
{agent_proposal}

ARCHITECT'S FEEDBACK (the rejection/modification request):
{architect_feedback}

EXISTING PREFERENCES (already learned from this Architect):
{existing_preferences}

INSTRUCTIONS:
1. First, review ALL existing preferences carefully.
2. Decide one of three actions:
   a) **SKIP** — if the feedback does not express a reusable preference, OR if an existing \
preference already fully covers this feedback. Do not store redundant or trivial preferences.
   b) **MERGE** — if the feedback refines, extends, or is closely related to an existing \
preference. Merge them into a single, stronger combined rule that captures both.
   c) **NEW** — if the feedback expresses a genuinely new preference not covered by any existing one.
3. Be smart about storage: prefer MERGE over NEW when possible to keep the preference list \
compact and powerful. Only use NEW for truly distinct preferences.
4. Classify into: design, coding_style, api_usage, naming, architecture, error_handling, performance

OUTPUT FORMAT (JSON only, no markdown fences):

For SKIP:
{{"action": "SKIP", "reasoning": "<why this was skipped>"}}

For NEW:
{{"action": "NEW", "category": "<category>", "rule": "<concise rule statement>", "reasoning": "<why this is a new preference>"}}

For MERGE:
{{"action": "MERGE", "merge_with_id": "<id of existing preference to merge with>", "category": "<category>", "merged_rule": "<new combined rule that covers both the existing and new preference>", "reasoning": "<why these were merged>"}}

Rules:
- Rules must be clear, actionable directives (e.g., "Always use X instead of Y when Z")
- Max 60 words for any rule
- Do NOT include project-specific details (file names, variable names) — keep it general
- Think like the Architect: what rule would ensure the Agent never makes this kind of mistake again?
"""

_CONSOLIDATION_PROMPT = """\
You are a preference optimization assistant. The Architect's preference store has reached \
its maximum capacity. You must merge two related preferences into one stronger, combined rule \
that preserves ALL the knowledge from both preferences without losing any guidance.

PREFERENCE A:
  ID: {pref_a_id}
  Category: {pref_a_category}
  Rule: {pref_a_rule}

PREFERENCE B:
  ID: {pref_b_id}
  Category: {pref_b_category}
  Rule: {pref_b_rule}

INSTRUCTIONS:
1. Merge these two preferences into a single, comprehensive rule.
2. The merged rule MUST preserve ALL guidance from both originals — do NOT lose any information.
3. Keep it concise but complete (max 80 words).
4. Choose the most appropriate category for the merged rule.

OUTPUT FORMAT (JSON only, no markdown fences):
{{"category": "<category>", "merged_rule": "<combined rule preserving both preferences>", "reasoning": "<how these were combined>"}}
"""


# ── Rejection detection ───────────────────────────────────────────────────

# Phase 2 rejection patterns (plan/design feedback)
_PHASE2_REJECTION_PATTERNS = [
    r"\bno\b[,.]?\s+(?:use|try|do|go|instead|rather|change)",
    r"\bwrong\s+(?:approach|way|pattern|design|hook|flow)",
    r"\binstead\s+(?:of|use)",
    r"\bdon'?t\s+(?:use|do|go|need)",
    r"\bchange\s+(?:the|this|your|to)",
    r"\bnot\s+what\s+I\s+(?:want|need|meant|expected)",
    r"\bshouldn'?t\s+(?:use|be|do)",
    r"\bwhy\s+(?:not|aren'?t|don'?t)\s+(?:you|we)\s+use",
    r"\bprefer\s+(?:to|using|if)",
    r"\balways\s+use\b",
    r"\bnever\s+use\b",
    r"\buse\s+\w+\s+instead\b",
    r"\bsimplif",
    r"\btoo\s+(?:complex|complicated|heavy|verbose)",
    r"\bno\s*,?\s+(?:that'?s|this\s+is)\s+(?:not|wrong|incorrect)",
    r"\breject",
    r"\bnot\s+(?:correct|right|good|ideal|the\s+right)",
]

# Phase 3 rejection patterns (code/implementation feedback)
_PHASE3_REJECTION_PATTERNS = [
    r"\bwrong\s+(?:api|method|class|signature|import|pattern)",
    r"\bfix\s+(?:the|this|your)",
    r"\bshould\s+(?:be|use|have|return)",
    r"\buse\s+\w+\s+instead\s+of",
    r"\bdon'?t\s+(?:use|call|import|concat)",
    r"\bmissing\s+(?:import|null|check|error|try|catch|finally)",
    r"\bno\s+(?:need|reason)\s+(?:to|for)",
    r"\bremove\s+(?:the|this|these)",
    r"\bincorrect\b",
    r"\bnot\s+(?:how|the\s+way|correct|right)",
    r"\balways\s+(?:wrap|check|validate|handle|use)",
    r"\bnever\s+(?:concat|hardcode|use|call)",
    r"\bstyle\b.*\b(?:should|must|always|prefer)",
    r"\bnaming\b.*\b(?:should|convention|pattern)",
    r"\btoo\s+(?:many|much|long|short|complex|verbose)",
]


def detect_rejection(user_message, phase):
    """Detect whether a user message is a rejection/modification request.

    Uses phase-specific regex patterns to identify feedback that expresses
    a reusable design or coding preference.

    Args:
        user_message: The architect's message text.
        phase: Current phase number (2 or 3).

    Returns:
        bool: True if the message likely contains a rejection or style feedback.
    """
    if not user_message or len(user_message.strip()) < 5:
        return False

    message_lower = user_message.lower()

    # Skip pure approvals
    approval_patterns = [
        r"^\s*(?:yes|ok|okay|approved?|lgtm|looks?\s+good|go\s+ahead|proceed|perfect|great|nice|correct|right)\s*[.!]?\s*$",
        r"^(?:yes|ok|okay|approved?|lgtm)\s*[,.]",
    ]
    for pat in approval_patterns:
        if re.match(pat, message_lower):
            return False

    patterns = _PHASE2_REJECTION_PATTERNS if phase == 2 else _PHASE3_REJECTION_PATTERNS

    for pattern in patterns:
        if re.search(pattern, message_lower):
            return True

    return False


def _detect_current_phase(messages):
    """Detect the current conversation phase from message history.

    Scans assistant messages in reverse to find the most recent [Phase: X] marker.

    Args:
        messages: The conversation message list.

    Returns:
        int or float or None: The current phase (1, 1.5, 2, 3, 4) or None.
    """
    for msg in reversed(messages):
        content = None
        if isinstance(msg, dict):
            if msg.get("role") == "assistant":
                content = msg.get("content", "")
        elif hasattr(msg, "role") and msg.role == "assistant":
            content = getattr(msg, "content", "") or ""

        if content:
            match = re.search(r'\[Phase:\s*([\d.]+)\]', content)
            if match:
                try:
                    return float(match.group(1))
                except ValueError:
                    return None
    return None


def _get_last_assistant_message(messages):
    """Get the content of the most recent assistant message.

    Args:
        messages: The conversation message list.

    Returns:
        str or None: The assistant's last message content.
    """
    for msg in reversed(messages):
        if isinstance(msg, dict) and msg.get("role") == "assistant":
            return msg.get("content", "")
        elif hasattr(msg, "role") and msg.role == "assistant":
            return getattr(msg, "content", "") or ""
    return None


# ── Preference data structures ────────────────────────────────────────────

class PreferenceManager:
    """Manages architect preferences: extraction, storage, and prompt injection.

    Preferences are stored in a ``.lvcopilot_preferences.json`` file in the
    project directory (alongside ``.lvcopilotenv``).  Extraction uses a
    separately configurable LLM model and runs in a background thread.

    Args:
        project_dir: Path to the project directory (defaults to CWD).
        max_preferences: Maximum number of preferences to store.
    """

    def __init__(self, project_dir=None, max_preferences=None):
        self.project_dir = project_dir or os.getcwd()
        self._filepath = os.path.join(self.project_dir, ".lvcopilot_preferences.json")

        # Max preferences — from env or default 30
        if max_preferences is not None:
            self.max_preferences = max_preferences
        else:
            try:
                self.max_preferences = int(
                    os.environ.get("LV_MAX_PREFERENCES", "30")
                )
            except ValueError:
                self.max_preferences = 30

        # LLM config for preference extraction (separate from main model)
        self.pref_model = os.environ.get("LV_PREF_MODEL") or os.environ.get("LLM_MODEL", "gemini/gemini-2.5-flash")
        self.pref_api_key = os.environ.get("LV_PREF_API_KEY") or os.environ.get("LLM_API_KEY")
        self.pref_api_base = os.environ.get("LV_PREF_API_BASE") or os.environ.get("LLM_API_BASE")

        # In-memory preference store
        self.preferences = []
        self._lock = threading.Lock()

        # Notification queue for background extraction results
        self._pending_notifications = []

        # Load existing preferences
        self.load()

    def load(self):
        """Load preferences from the project JSON file."""
        if not os.path.isfile(self._filepath):
            return

        try:
            with open(self._filepath, 'r', encoding='utf-8') as f:
                data = json.load(f)
            self.preferences = data.get("preferences", [])
        except (json.JSONDecodeError, OSError):
            self.preferences = []

    def save(self):
        """Persist preferences to the project JSON file."""
        data = {
            "version": 1,
            "preferences": self.preferences,
        }
        try:
            with open(self._filepath, 'w', encoding='utf-8') as f:
                json.dump(data, f, indent=2, ensure_ascii=False)
        except OSError:
            pass  # Silently fail — preference persistence is best-effort

    def add_preference(self, category, rule, reasoning, source_phase):
        """Add a new preference, deduplicating and enforcing the cap.

        If the cap is reached, triggers smart consolidation (LLM-based
        merging of the two most related preferences) instead of dropping
        any knowledge.

        Args:
            category: Preference category (design, coding_style, etc.).
            rule: The preference rule text.
            reasoning: Why this preference was extracted.
            source_phase: Which phase it was extracted from (2 or 3).

        Returns:
            bool: True if the preference was added (not a duplicate).
        """
        # Deduplication: check if a very similar rule already exists
        rule_lower = rule.lower().strip()
        for existing in self.preferences:
            existing_lower = existing.get("rule", "").lower().strip()
            # Simple similarity: if >70% of words overlap, consider duplicate
            rule_words = set(rule_lower.split())
            existing_words = set(existing_lower.split())
            if rule_words and existing_words:
                overlap = len(rule_words & existing_words)
                similarity = overlap / max(len(rule_words), len(existing_words))
                if similarity > 0.7:
                    return False

        pref = {
            "id": f"pref_{uuid.uuid4().hex[:8]}",
            "category": category,
            "rule": rule,
            "reasoning": reasoning,
            "source_phase": source_phase,
            "created_at": datetime.now().isoformat(),
        }

        with self._lock:
            self.preferences.append(pref)

            # Smart cap enforcement: consolidate instead of evict
            if len(self.preferences) > self.max_preferences:
                self._consolidate_on_cap()

            self.save()

        return True

    def merge_preference(self, target_id, new_rule, new_category, reasoning):
        """Merge a new rule into an existing preference by ID.

        Replaces the existing preference's rule with the merged version,
        preserving its ID and updating the timestamp.

        Args:
            target_id: ID of the existing preference to merge into.
            new_rule: The new merged rule text.
            new_category: Category for the merged preference.
            reasoning: Why these were merged.

        Returns:
            bool: True if the target preference was found and updated.
        """
        with self._lock:
            for pref in self.preferences:
                if pref.get("id") == target_id:
                    pref["rule"] = new_rule
                    pref["category"] = new_category
                    pref["reasoning"] = f"[Merged] {reasoning}"
                    pref["updated_at"] = datetime.now().isoformat()
                    self.save()
                    return True
        return False

    def _consolidate_on_cap(self):
        """Smart consolidation: merge the two most related preferences.

        Called when the preference cap is exceeded.  Uses word-overlap
        scoring to find the most related pair, then calls the preference
        LLM to merge them into a single stronger rule.  This ensures no
        knowledge is lost when the cap is hit.

        Must be called while holding ``self._lock``.
        """
        if len(self.preferences) < 2:
            return

        # Find the two most similar preferences by word overlap
        best_score = -1
        best_pair = (0, 1)

        for i in range(len(self.preferences)):
            words_i = set(self.preferences[i].get("rule", "").lower().split())
            for j in range(i + 1, len(self.preferences)):
                words_j = set(self.preferences[j].get("rule", "").lower().split())
                if not words_i or not words_j:
                    continue
                overlap = len(words_i & words_j)
                # Also boost score if same category
                cat_bonus = 2 if self.preferences[i].get("category") == self.preferences[j].get("category") else 0
                score = overlap + cat_bonus
                if score > best_score:
                    best_score = score
                    best_pair = (i, j)

        idx_a, idx_b = best_pair
        pref_a = self.preferences[idx_a]
        pref_b = self.preferences[idx_b]

        # Try LLM-based merge
        try:
            prompt = _CONSOLIDATION_PROMPT.format(
                pref_a_id=pref_a.get("id", ""),
                pref_a_category=pref_a.get("category", "general"),
                pref_a_rule=pref_a.get("rule", ""),
                pref_b_id=pref_b.get("id", ""),
                pref_b_category=pref_b.get("category", "general"),
                pref_b_rule=pref_b.get("rule", ""),
            )

            result_text = self._llm_call(prompt)

            # Strip markdown fences
            result_text = re.sub(r'^```(?:json)?\s*', '', result_text.strip())
            result_text = re.sub(r'\s*```$', '', result_text)

            parsed = json.loads(result_text)
            merged_rule = parsed.get("merged_rule", "")
            merged_category = parsed.get("category", pref_a.get("category", "general"))

            if merged_rule and len(merged_rule.strip()) >= 10:
                # Replace pref_a with the merged version
                pref_a["rule"] = merged_rule
                pref_a["category"] = merged_category
                pref_a["reasoning"] = f"[Consolidated] Merged with {pref_b.get('id', '?')}: {parsed.get('reasoning', '')}"
                pref_a["updated_at"] = datetime.now().isoformat()

                # Remove pref_b (remove the higher index first to avoid shifting)
                self.preferences.pop(idx_b)

                self._pending_notifications.append(
                    f"🔀 Preferences consolidated (cap reached): \"{merged_rule[:80]}...\""
                    if len(merged_rule) > 80 else
                    f"🔀 Preferences consolidated (cap reached): \"{merged_rule}\""
                )
                return

        except Exception:
            pass

        # Fallback: if LLM merge fails, evict the oldest preference
        self.preferences.pop(0)
        self._pending_notifications.append(
            "⚠️ Preference cap reached — oldest preference removed (merge failed)"
        )

    def remove_preference(self, index):
        """Remove a preference by its display index (1-based).

        Args:
            index: 1-based index of the preference to remove.

        Returns:
            dict or None: The removed preference, or None if index invalid.
        """
        with self._lock:
            if 1 <= index <= len(self.preferences):
                removed = self.preferences.pop(index - 1)
                self.save()
                return removed
        return None

    def clear_all(self):
        """Remove all preferences."""
        with self._lock:
            self.preferences = []
            self.save()

    def list_preferences(self):
        """Return all stored preferences.

        Returns:
            list[dict]: List of preference dicts.
        """
        return list(self.preferences)

    def format_for_prompt(self):
        """Format preferences into a block suitable for system prompt injection.

        Returns:
            str: Formatted preferences block, or empty string if no preferences.
        """
        if not self.preferences:
            return ""

        lines = [
            "\n────────────────────────────────────────",
            "ARCHITECT PREFERENCES (Learned from previous interactions)",
            "────────────────────────────────────────",
            "The following preferences have been learned from this Architect's feedback.",
            "You MUST follow these when making design decisions and writing code.",
            "When these conflict with general best practices, ALWAYS prefer the Architect's stated preference.\n",
        ]

        # Group by category
        by_category = {}
        for pref in self.preferences:
            cat = pref.get("category", "general")
            by_category.setdefault(cat, []).append(pref)

        for category, prefs in by_category.items():
            lines.append(f"[{category.upper().replace('_', ' ')}]")
            for pref in prefs:
                lines.append(f"  • {pref['rule']}")
            lines.append("")

        return "\n".join(lines)

    def get_pending_notifications(self):
        """Retrieve and clear any pending background extraction notifications.

        Returns:
            list[str]: Notification messages from background extractions.
        """
        with self._lock:
            notifications = list(self._pending_notifications)
            self._pending_notifications.clear()
        return notifications

    # ── Background extraction ─────────────────────────────────────────────

    def trigger_extraction(self, agent_proposal, architect_feedback, source_phase):
        """Start preference extraction in a background thread.

        This method returns immediately. The extraction result is stored
        in ``_pending_notifications`` and can be polled via
        ``get_pending_notifications()``.

        Args:
            agent_proposal: The agent's proposal/code that was rejected.
            architect_feedback: The architect's rejection/modification feedback.
            source_phase: The conversation phase (2 or 3).
        """
        thread = threading.Thread(
            target=self._extract_and_save,
            args=(agent_proposal, architect_feedback, source_phase),
            daemon=True,
        )
        thread.start()

    def _llm_call(self, prompt):
        """Make a one-shot LLM call using the preference model config.

        Args:
            prompt: The user prompt string.

        Returns:
            str: The LLM response text.
        """
        kwargs = {
            "model": self.pref_model,
            "messages": [
                {"role": "system", "content": "You are a preference analysis assistant. Output only valid JSON."},
                {"role": "user", "content": prompt},
            ],
            "temperature": 0.1,
        }

        if self.pref_api_key:
            kwargs["api_key"] = self.pref_api_key
        if self.pref_api_base:
            kwargs["api_base"] = self.pref_api_base

        response = litellm.completion(**kwargs)
        return response.choices[0].message.content.strip()

    def _format_existing_for_prompt(self):
        """Format existing preferences for inclusion in the extraction prompt.

        Returns:
            str: Formatted preference list, or '(none)' if empty.
        """
        if not self.preferences:
            return "(none — this is the first preference being learned)"

        lines = []
        for pref in self.preferences:
            lines.append(
                f"  ID: {pref.get('id', '?')} | "
                f"Category: {pref.get('category', 'general')} | "
                f"Rule: {pref.get('rule', '')}"
            )
        return "\n".join(lines)

    def _extract_and_save(self, agent_proposal, architect_feedback, source_phase):
        """Background worker: extract preference via LLM and save it.

        The LLM sees all existing preferences and decides one of:
        - SKIP: feedback doesn't warrant a new preference, or already covered
        - NEW: genuinely new preference to add
        - MERGE: combine with an existing preference into a stronger rule

        Args:
            agent_proposal: The agent's proposal/code (truncated for prompt).
            architect_feedback: The architect's feedback text.
            source_phase: Phase number (2 or 3).
        """
        try:
            # Truncate proposal to avoid excessive token usage
            proposal_truncated = agent_proposal[:3000] if agent_proposal else "(no proposal)"
            feedback_truncated = architect_feedback[:2000] if architect_feedback else "(no feedback)"

            # Build context-aware prompt with existing preferences
            existing_prefs_text = self._format_existing_for_prompt()

            prompt = _EXTRACTION_PROMPT.format(
                agent_proposal=proposal_truncated,
                architect_feedback=feedback_truncated,
                existing_preferences=existing_prefs_text,
            )

            result_text = self._llm_call(prompt)

            # Strip markdown fences if present
            result_text = re.sub(r'^```(?:json)?\s*', '', result_text)
            result_text = re.sub(r'\s*```$', '', result_text)

            parsed = json.loads(result_text)
            action = parsed.get("action", "").upper()

            phase_label = f"Phase {source_phase}"

            if action == "SKIP":
                # LLM determined this is not a new preference or already covered
                return

            elif action == "MERGE":
                # Merge with an existing preference
                merge_with_id = parsed.get("merge_with_id", "")
                merged_rule = parsed.get("merged_rule", "")
                merged_category = parsed.get("category", "general")
                reasoning = parsed.get("reasoning", "")

                if not merged_rule or len(merged_rule.strip()) < 10:
                    return

                was_merged = self.merge_preference(
                    merge_with_id, merged_rule, merged_category, reasoning
                )

                if was_merged:
                    with self._lock:
                        self._pending_notifications.append(
                            f"🔀 Preference merged ({phase_label}): {merged_rule}"
                        )
                else:
                    # Merge target not found — fall back to adding as new
                    was_added = self.add_preference(
                        merged_category, merged_rule, reasoning, source_phase
                    )
                    if was_added:
                        with self._lock:
                            self._pending_notifications.append(
                                f"💡 Preference learned ({phase_label}): {merged_rule}"
                            )

            elif action == "NEW":
                category = parsed.get("category", "general")
                rule = parsed.get("rule", "")
                reasoning = parsed.get("reasoning", "")

                if not rule or len(rule.strip()) < 10:
                    return

                was_added = self.add_preference(
                    category, rule, reasoning, source_phase
                )

                if was_added:
                    with self._lock:
                        self._pending_notifications.append(
                            f"💡 Preference learned ({phase_label}): {rule}"
                        )

        except (json.JSONDecodeError, KeyError, Exception):
            # Silently fail — preference extraction is best-effort
            pass

    # ── Integration helpers ───────────────────────────────────────────────

    def check_and_trigger(self, user_message, messages):
        """Check if the user message is a rejection and trigger extraction.

        This is the main integration point — called from ``agent.send_message()``
        before the message is sent to the LLM.

        Args:
            user_message: The architect's message text.
            messages: The full conversation message list (for phase detection).

        Returns:
            bool: True if extraction was triggered.
        """
        phase = _detect_current_phase(messages)

        if phase not in (2, 2.0, 3, 3.0):
            return False

        phase_int = int(phase)

        if not detect_rejection(user_message, phase_int):
            return False

        # Get the agent's last proposal (what's being rejected)
        agent_proposal = _get_last_assistant_message(messages)
        if not agent_proposal:
            return False

        # Trigger background extraction
        self.trigger_extraction(agent_proposal, user_message, phase_int)
        return True
