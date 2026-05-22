"""Conversation history manager for LVCopilot.

Wraps the raw message list with intelligent lifecycle management:
- Turn counting and configurable summarization thresholds
- Structured conversation compression (preserves phase, mode, decisions)
- Tool result pruning with semantic stubs
- Prevents unbounded context window growth

Configure via environment variables:
    ``LV_SUMMARY_INTERVAL``  — turns between summaries (default: 10)
"""

import os
import re
import json


# ── Semantic stubs for pruned tool results ────────────────────────────────

_COMPONENT_HINTS = {
    "action": "LV Action patterns: BaseAction lifecycle, PropertyList I/O, SafeSQL, "
              "QueryProcessor, DataSet access, error handling, ActionBlock.",
    "ajax": "LV AJAX patterns: handleRequest, JSON response, try/catch/finally, "
            "PropertyList, server-side data fetch.",
    "javascript": "LV JavaScript patterns: client-side event handling, AJAX calls, "
                  "form manipulation, page load hooks.",
    "sdc_rule": "LV SDC Rule patterns: pre/post hooks, validation, side effects, "
                "requires* flags, SapphireException.",
    "sdms": "LV SDMS patterns: file parsing, instrument integration, CSV/XML handling, "
            "data mapping.",
}

_SUMMARIZATION_PROMPT = """\
Extract a structured summary from the following conversation between an Architect and an LV Developer Agent.

CONVERSATION:
{conversation_text}

OUTPUT FORMAT (use exactly this structure):
PHASE: [current phase number]
MODE: [New or Modify]
DECISIONS:
- [bullet list of approved decisions and design choices]
FILES:
- [list of files discussed with their purpose/role]
PLAN:
- [the approved technical plan, if any — summarize key points]
PENDING:
- [what the architect is currently waiting for or what comes next]
COMPONENT:
- [which LV component(s) are being built: Action, SDC Rule, AJAX, SDMS, etc.]

Rules:
- Max 400 words total
- Preserve specific names, IDs, and technical details
- Do NOT add information that wasn't in the conversation
"""


class ConversationManager:
    """Manages conversation history with intelligent compression.

    Args:
        summary_interval: Number of user turns between automatic summaries.
            Read from ``LV_SUMMARY_INTERVAL`` env var, default 10.
    """

    def __init__(self, summary_interval=None):
        if summary_interval is None:
            try:
                summary_interval = int(os.environ.get("LV_SUMMARY_INTERVAL", "10"))
            except ValueError:
                summary_interval = 10

        self.messages = []
        self.turn_count = 0
        self.summary_interval = summary_interval
        self._last_summary_turn = 0

    def set_system_prompt(self, content):
        """Set the system prompt as the first message.

        Args:
            content: The system prompt string.
        """
        if self.messages and self.messages[0].get("role") == "system":
            self.messages[0] = {"role": "system", "content": content}
        else:
            self.messages.insert(0, {"role": "system", "content": content})

    def add_message(self, role_or_message, content=None):
        """Add a message to the conversation history.

        Supports two calling conventions:
            - ``add_message("user", "Hello")``
            - ``add_message(response_message_object)`` — for tool-call messages

        Args:
            role_or_message: Either a role string ("user", "assistant", "tool")
                or a full message object (dict or API response message).
            content: The message content string (required when role_or_message
                is a role string).
        """
        if isinstance(role_or_message, str) and content is not None:
            msg = {"role": role_or_message, "content": content}
            self.messages.append(msg)
            if role_or_message == "user":
                self.turn_count += 1
        elif isinstance(role_or_message, dict):
            self.messages.append(role_or_message)
        else:
            # Assume it's a response message object (from litellm)
            self.messages.append(role_or_message)

    def add_tool_result(self, tool_call_id, function_name, content):
        """Add a tool result message.

        Args:
            tool_call_id: The ID of the tool call being responded to.
            function_name: Name of the function that was called.
            content: The tool result content string.
        """
        self.messages.append({
            "tool_call_id": tool_call_id,
            "role": "tool",
            "name": function_name,
            "content": content,
        })

    def get_messages(self):
        """Return the current message list for API calls.

        Returns:
            list[dict]: The compacted message list.
        """
        return self.messages

    def should_summarize(self):
        """Check if the conversation should be summarized.

        Returns:
            bool: True if the turn count has reached the summary threshold.
        """
        if self.summary_interval <= 0:
            return False
        return (
            self.turn_count > 0
            and self.turn_count >= self._last_summary_turn + self.summary_interval
            and len(self.messages) > 6  # Need enough messages to summarize
        )

    def summarize_and_compact(self, llm_call_fn):
        """Summarize older messages and replace them with a structured summary.

        Keeps: system prompt (index 0) + the last 4 messages.
        Summarizes: everything in between (the "stale middle").

        The summary is a one-shot LLM call that does NOT get appended
        to the main conversation history.

        Args:
            llm_call_fn: A callable that takes a prompt string and returns
                the LLM's response string.  This should be a one-shot call
                (e.g., ``agent.merge_content`` or a dedicated summary method).
        """
        if len(self.messages) <= 6:
            return  # Nothing worth summarizing

        # Keep system prompt (0) and last 4 messages
        system_msg = self.messages[0]
        recent_messages = self.messages[-4:]
        stale_middle = self.messages[1:-4]

        if not stale_middle:
            return

        # Build conversation text for summarization
        conversation_text = self._format_messages_for_summary(stale_middle)
        prompt = _SUMMARIZATION_PROMPT.format(conversation_text=conversation_text)

        try:
            summary = llm_call_fn(prompt)
        except Exception:
            # If summarization fails, don't crash — just skip
            return

        # Build the summary message
        summary_msg = {
            "role": "user",
            "content": (
                f"[System: Conversation Summary as of turn {self.turn_count}]\n"
                f"The following is a structured summary of the earlier conversation. "
                f"All details below are factual — treat them as ground truth.\n\n"
                f"{summary}"
            ),
        }

        # Replace the message list: system + summary + recent
        self.messages = [system_msg, summary_msg] + recent_messages
        self._last_summary_turn = self.turn_count

    def prune_tool_results(self):
        """Replace consumed tool results with compact semantic stubs.

        A tool result is considered "consumed" when the assistant has
        responded after it (i.e., there's an assistant message after
        the tool message in the history).

        The stub retains a hint of what the document contained so the
        agent remembers the gist without needing to re-read.
        """
        if len(self.messages) < 3:
            return

        i = 0
        while i < len(self.messages):
            msg = self.messages[i]

            # Only process tool messages
            if not isinstance(msg, dict) or msg.get("role") != "tool":
                i += 1
                continue

            # Check if there's an assistant message after this tool result
            has_following_assistant = any(
                isinstance(m, dict) and m.get("role") == "assistant"
                for m in self.messages[i + 1:]
            )

            if not has_following_assistant:
                i += 1
                continue

            # This tool result has been consumed — replace with stub
            tool_name = msg.get("name", "unknown")
            original_content = msg.get("content", "")

            # Generate semantic stub based on tool name and content
            stub = self._generate_stub(tool_name, original_content)

            if stub and len(stub) < len(original_content):
                self.messages[i] = {
                    "tool_call_id": msg.get("tool_call_id", ""),
                    "role": "tool",
                    "name": tool_name,
                    "content": stub,
                }

            i += 1

    def pop_last_user_message(self):
        """Remove the last user message (for error recovery).

        Returns:
            bool: True if a message was removed.
        """
        if self.messages and isinstance(self.messages[-1], dict) and \
                self.messages[-1].get("role") == "user":
            self.messages.pop()
            self.turn_count = max(0, self.turn_count - 1)
            return True
        return False

    # ── Private helpers ───────────────────────────────────────────────────

    def _format_messages_for_summary(self, messages):
        """Format a list of messages into a readable text block for summarization."""
        parts = []
        for msg in messages:
            if not isinstance(msg, dict):
                continue
            role = msg.get("role", "unknown")
            content = msg.get("content", "")

            if role == "tool":
                # Don't include full tool content in summary input —
                # just note which tool was called
                tool_name = msg.get("name", "unknown tool")
                content_preview = content[:200] + "..." if len(content) > 200 else content
                parts.append(f"[Tool: {tool_name}]: {content_preview}")
            elif role == "user":
                parts.append(f"Architect: {content[:500]}")
            elif role == "assistant":
                parts.append(f"Agent: {content[:500]}")

        return "\n\n".join(parts)

    def _generate_stub(self, tool_name, content):
        """Generate a semantic stub for a consumed tool result.

        Args:
            tool_name: Name of the tool function.
            content: The original tool result content.

        Returns:
            str: A compact stub with semantic hints, or None if no stub
                 should replace the original.
        """
        # Handle fetch_lv_rules / fetch_lv_syntax
        # NEVER prune skill rules — they are compact (~15KB) and contain the
        # authoritative API method signatures.  Replacing them with stubs
        # causes the agent to hallucinate non-existent APIs.
        if tool_name in ("fetch_lv_syntax", "fetch_lv_rules"):
            return None

        # Handle fetch_lv_reference
        if tool_name == "fetch_lv_reference":
            component = self._detect_component_from_content(content)
            # Extract section titles from the content
            sections = re.findall(r'^## .+', content, re.MULTILINE)
            section_list = ', '.join(s.lstrip('#').strip() for s in sections[:3])
            return (
                f"[Previously loaded: {component} reference sections — {section_list}. "
                f"Refer to the details already in your context.]"
            )

        # Handle fetch_architecture_guide
        if tool_name == "fetch_architecture_guide":
            return (
                "[Previously loaded: Architecture Guide — component selection matrix, "
                "SDC hook lifecycle, core framework processors, DB access patterns, "
                "design patterns (Fail-Fast, Silent Calculation, Safe Access, Atomic Action).]"
            )

        # For "already loaded" messages, keep as-is
        if content and "already present" in content.lower():
            return None

        # Default: don't stub unknown tools
        return None

    def _detect_component_from_content(self, content):
        """Try to detect which LV component a tool result is about."""
        content_lower = content[:200].lower()
        for comp in ("action", "ajax", "javascript", "sdc_rule", "sdms", "java_public_api"):
            if comp.replace('_', ' ') in content_lower or comp in content_lower:
                return comp
        return "unknown"
