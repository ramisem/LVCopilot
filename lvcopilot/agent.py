import os
import sys
import json
import re
# pyrefly: ignore [missing-import]
import litellm
from dotenv import load_dotenv
from .conversation_manager import ConversationManager
from . import knowledge_index
from .code_validator import (
    LVCodeValidator, format_validation_feedback, extract_code_blocks,
)
from .preference_manager import PreferenceManager

# load_dotenv() is handled in main.py per project directory
SYSTEM_PROMPT = """\
Role: You are an autonomous LabVantage (LV) Senior Developer. You operate in a structured multi-phase loop. You support two modes: **New** (building from scratch) and **Modify** (changing existing functionality).

State Tracking:
Begin EVERY response with: `[Phase: X] [Mode: New|Modify]` where X is 1, 1.5, 2, 3, or 4.

Knowledge Base:
Rely on provided documents for all syntax, logic, and best practices. Use tools to fetch them when needed.

────────────────────────────────────────
PHASE 1: REQUIREMENT ELICITATION
────────────────────────────────────────
- Ask for the Business Requirement.
- Detect mode: **[Mode: New]** → Phase 2. **[Mode: Modify]** → Phase 1.5.

────────────────────────────────────────
PHASE 1.5: INVESTIGATION SETUP [Modify only]
────────────────────────────────────────
Ask the Architect:
1. "Which file to investigate? (use @filepath)"
2. "Starting method/code? (optional)"
Then transition to Phase 2.

────────────────────────────────────────
PHASE 2: SCOPING / CODE INVESTIGATION
────────────────────────────────────────
[New] Determine components needed. Use `fetch_architecture_guide` for component selection.
  After identifying which components are needed, call `fetch_lv_rules` for each component to load the correct API patterns.
  Propose a plan using pure logic/algorithmic flow. Do NOT mention, reference, or use any specific LabVantage APIs, classes, interfaces, or method signatures (e.g., PropertyList, DataSet, SafeSQL, QueryProcessor) during this phase, as you do not have full visibility of the LV APIs yet. Focus strictly on logical/algorithmic design and architectural steps. → WAIT for approval.
[Modify] Investigate code from entry-point file:
  1. Analyze provided file — quote specific lines from the ACTUAL code.
  2. To request related files: `[Investigate: filename.ext]` (system auto-locates).
  3. SCOPE RULE: Only request files DIRECTLY involved in the change.
  4. Present: **Files Involved** | **Execution Flow** | **Areas to Change**.
  5. Propose a modification plan using pure logic/algorithmic flow. Do NOT mention, reference, or use any specific LabVantage APIs, classes, interfaces, or method signatures during this phase. Focus strictly on logical/algorithmic design. → WAIT for approval.
  GROUND TRUTH: Investigated code is absolute truth. NEVER hallucinate functionality not present in the code.

  REJECTION HANDLING (applies to both New and Modify):
  If the Architect rejects your proposal, analyze their feedback:
  - **Design/approach/logic feedback** (e.g., "use a different component", "simplify the logic", "wrong hook", "incorrect algorithm flow"): Re-think your logic and algorithm using the knowledge you already have, modify your plan, and re-propose. No need to fetch API docs during this phase.

────────────────────────────────────────
PHASE 3: DEVELOPMENT & REVIEW
────────────────────────────────────────
Before writing code: If `fetch_lv_rules` was not already called for the relevant component in Phase 2, call it now. If you need detailed API docs beyond the rules (especially regarding core Java classes like PropertyList, DataSet, SDIData, SafeSQL, etc.), smartly identify your lack of information and call `fetch_lv_reference` with `java_public_api` or the specific component and the desired topic. Do not guess signatures.
CRITICAL API RULE: Use ONLY method signatures and patterns from the fetched skill rules / reference docs. NEVER use API methods from your own memory — they may not exist in LabVantage.
If the Architect rejects your code citing wrong APIs or missing patterns, you must smartly evaluate the API/knowledge gap:
  * If it is a generic component API or layout reference, use the respective component (action, ajax, sdc_rule, etc.).
  * If it concerns underlying Java utility classes (like PropertyList, PropertyListCollection, DataSet, SDIData, SafeSQL, ActionBlock, DBAccess, or QueryProcessor methods), use the `java_public_api` component.
  * Call `fetch_lv_reference` for that specific component and topic that you need to resolve the gap, then regenerate the code using ONLY the APIs from the reference.
For other feedback (logic, structure, approach), re-work using existing knowledge.
Do NOT call `fetch_lv_reference` for every rejection — only when you genuinely identify that your current knowledge is missing something.

[New] Generate:
  1. Java Source Code — fully qualified classes (e.g., `com.client.actions`).
  2. Enforce: SafeSQL/Object[] for queries (NEVER string concat), try/catch/finally for AJAX, selective requires* flags for SDC Rules.
  3. Configuration Guide — System Admin registration steps.
  → WAIT for approval.

[Modify] Show DIFF PREVIEW for each changed file:
  ```diff
  --- a/OriginalFile.java (existing)
  +++ b/OriginalFile.java (modified)
  @@ -line,count +line,count @@
   context (unchanged)
  -removed
  +added
  ```
  CRITICAL: You are MODIFYING existing code. Base output on investigated code — do NOT generate from scratch.
  → WAIT for approval.

────────────────────────────────────────
PHASE 4: FILE GENERATION
────────────────────────────────────────
[New] Output final code blocks. Prefix each with: `File: filename.ext`.
[Modify] Output COMPLETE modified file content (full file, not just changes). Prefix each with: `File: filename.ext`.
→ WAIT for save confirmation, then return to Phase 1.
"""

TOOLS = [
    {
        "type": "function",
        "function": {
            "name": "fetch_architecture_guide",
            "description": "Fetch the LabVantage Architecture Guide for component selection strategy. Call this during Phase 2 to determine which components (Action, SDC Rule, AJAX, SDMS) are appropriate for the requirement. Also covers SDC hook lifecycle, core framework processors, and design patterns.",
            "parameters": {
                "type": "object",
                "properties": {}
            }
        }
    },
    {
        "type": "function",
        "function": {
            "name": "fetch_lv_rules",
            "description": "Fetch the best-practice SKILL RULES for a LabVantage component. This is a compact reference (~300 lines) covering patterns, anti-patterns, and conventions. Always call this FIRST during Phase 3 before writing code.",
            "parameters": {
                "type": "object",
                "properties": {
                    "component": {
                        "type": "string",
                        "enum": ["action", "ajax", "javascript", "sdc_rule", "sdms"],
                        "description": "The LV component to fetch skill rules for."
                    }
                },
                "required": ["component"]
            }
        }
    },
    {
        "type": "function",
        "function": {
            "name": "fetch_lv_reference",
            "description": "Fetch SPECIFIC sections from the full API reference for a LabVantage component. Use this ONLY when the skill rules (from fetch_lv_rules) were insufficient and you need detailed API docs, method signatures, or example implementations for a specific topic.",
            "parameters": {
                "type": "object",
                "properties": {
                    "component": {
                        "type": "string",
                        "enum": ["action", "ajax", "javascript", "sdc_rule", "sdms", "java_public_api"],
                        "description": "The LV component. Use 'java_public_api' strictly for core Java public APIs and utility classes (such as PropertyList, PropertyListCollection, DataSet, SDIData, SafeSQL, ActionBlock, DBAccess, or QueryProcessor methods) when there is a clear lack of information or the architect signals API misuse."
                    },
                    "topic": {
                        "type": "string",
                        "description": "The specific topic you need details on (e.g., 'QueryProcessor', 'ActionBlock', 'error handling', 'SafeSQL', 'SDCProcessor', 'ConnectionInfo'). Used to retrieve only the relevant sections."
                    }
                },
                "required": ["component", "topic"]
            }
        }
    }
]


def parse_text_tool_call(text):
    """Scan response text for a JSON tool call block and return parsed dict and substring."""
    if not text:
        return None
    for match in re.finditer(r'\{', text):
        start = match.start()
        depth = 0
        for i in range(start, len(text)):
            if text[i] == '{':
                depth += 1
            elif text[i] == '}':
                depth -= 1
                if depth == 0:
                    substring = text[start:i+1]
                    try:
                        data = json.loads(substring)
                        if isinstance(data, dict) and "name" in data:
                            tool_name = data.get("name")
                            if tool_name in ("fetch_architecture_guide", "fetch_lv_rules", "fetch_lv_reference", "fetch_lv_syntax"):
                                return data, substring
                    except Exception:
                        pass
                    break
    return None


class LVDeveloperAgent:
    def __init__(self):
        self.model_name = os.environ.get("LLM_MODEL", "gemini/gemini-2.5-flash")
        self.api_key = os.environ.get("LLM_API_KEY")
        self.api_base = os.environ.get("LLM_API_BASE")
        self.thinking = os.environ.get("LLM_THINKING", "").lower() == "true"
        self.validation_enabled = os.environ.get("LV_VALIDATE", "true").lower() == "true"

        self.conversation = ConversationManager()
        self.loaded_skills = set()
        self.loaded_full_refs = {}  # component -> set of section_ids
        self._knowledge_index = None
        self._md_files_dir = None

        # Token tracking
        self.session_stats = {
            'total_prompt': 0,
            'total_completion': 0,
            'turns': 0,
        }

        # Code validator
        self.validator = LVCodeValidator()
        self._current_component = None  # Tracks which component is being developed
        self.hallucination_detected = False

        # Architect preference learning
        self.preference_manager = PreferenceManager()

    def _is_in_phase_3(self):
        """Check if the conversation is currently in Phase 3.

        This is determined by checking if the last assistant message in history
        contains the '[Phase: 3]' marker.
        """
        for msg in reversed(self.conversation.messages):
            if isinstance(msg, dict) and msg.get("role") == "assistant":
                content = msg.get("content") or ""
                if "[Phase: 3]" in content:
                    return True
                # If we find another phase first, then we are not in Phase 3
                if any(f"[Phase: {p}]" in content for p in ("1", "1.5", "2", "4")):
                    return False
            elif hasattr(msg, "role") and msg.role == "assistant":
                content = getattr(msg, "content", "") or ""
                if "[Phase: 3]" in content:
                    return True
                if any(f"[Phase: {p}]" in content for p in ("1", "1.5", "2", "4")):
                    return False
        return False

    def _get_base_dir(self):
        if getattr(sys, 'frozen', False) and hasattr(sys, '_MEIPASS'):
            return os.path.join(sys._MEIPASS, 'lvcopilot')
        else:
            base_dir = os.path.dirname(os.path.abspath(__file__))
            if not os.path.exists(os.path.join(base_dir, 'md_files')):
                return os.path.join(sys.prefix, 'lvcopilot_data')
            return base_dir

    def _ensure_knowledge_index(self):
        """Lazily build/load the knowledge index."""
        if self._knowledge_index is None:
            base_dir = self._get_base_dir()
            self._knowledge_index, self._md_files_dir = \
                knowledge_index.ensure_index(base_dir)
        return self._knowledge_index, self._md_files_dir

    def _load_architecture_guide(self):
        """Load the architecture guide document."""
        base_dir = self._get_base_dir()
        filepath = os.path.join(base_dir, "architecture", "architecture_guide.md")

        if os.path.exists(filepath):
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
            return f"=== ARCHITECTURE GUIDE ===\n{content}\n"
        else:
            return "=== ARCHITECTURE GUIDE ===\n(No guide found.)\n"

    def _load_skill_rules(self, component_name):
        """Load only the compact skill rules for a component.

        Returns:
            str: The skill rules content, or an already-loaded message.
        """
        if component_name in self.loaded_skills and not getattr(self, 'hallucination_detected', False):
            return (f"Skill rules for {component_name} are already present in your "
                    f"conversation history. Refer to the previous messages.")

        base_dir = self._get_base_dir()
        skill_file = os.path.join(base_dir, "skills", f"{component_name}_skill.md")

        if os.path.exists(skill_file):
            with open(skill_file, 'r', encoding='utf-8') as f:
                content = f.read()
            self.loaded_skills.add(component_name)
            self._current_component = component_name
            return f"=== SKILL RULES: {component_name.upper()} ===\n{content}\n"
        else:
            return f"Error: Skill rules for '{component_name}' not found."

    def _load_reference_sections(self, component_name, topic):
        """Load only the relevant sections from the full reference doc.

        Uses the knowledge index to find sections matching the topic,
        rather than loading the entire 40-80KB document.

        Returns:
            str: The relevant section contents concatenated.
        """
        index, md_files_dir = self._ensure_knowledge_index()

        if not index.get(component_name):
            return f"Error: No reference documentation found for '{component_name}'."

        # Track loaded refs
        if component_name not in self.loaded_full_refs:
            self.loaded_full_refs[component_name] = set()

        # Retrieve matching sections
        sections = knowledge_index.retrieve_sections(
            index, component_name, topic, max_sections=3
        )

        if not sections:
            return (f"No sections matching '{topic}' found in {component_name} reference. "
                    f"Available sections: "
                    + ", ".join(s.title for s in index[component_name]))

        # Filter out already-loaded sections
        # Smart history check: if a section was previously loaded but has since been pruned
        # or summarized out of the active message history, we allow re-loading to prevent infinite loops.
        history_text = ""
        for msg in self.conversation.messages:
            if isinstance(msg, dict):
                history_text += msg.get("content") or ""
            elif hasattr(msg, "content") and msg.content:
                history_text += msg.content

        new_sections = []
        for s in sections:
            marker = f"--- {s.title} ---"
            if getattr(self, 'hallucination_detected', False):
                new_sections.append(s)
            elif marker not in history_text:
                new_sections.append(s)
            elif s.section_id not in self.loaded_full_refs[component_name]:
                new_sections.append(s)

        if not new_sections:
            return (f"The relevant sections for '{topic}' in {component_name} are "
                    f"already in your conversation history.")

        # Load content for new sections
        combined = f"=== REFERENCE: {component_name.upper()} — Topic: {topic} ===\n"
        for section in new_sections:
            content = knowledge_index.get_section_content(
                md_files_dir, component_name, section
            )
            combined += f"\n--- {section.title} ---\n{content}\n"
            self.loaded_full_refs[component_name].add(section.section_id)

        # Note available other sections
        all_titles = [s.title for s in index[component_name]
                      if s.section_id not in self.loaded_full_refs.get(component_name, set())]
        if all_titles:
            combined += (f"\n[Other available sections: {', '.join(all_titles)}. "
                         f"Call fetch_lv_reference again with a different topic if needed.]\n")

        return combined

    def reset(self):
        """Reset the conversation history and agent state to start a fresh session.

        Note: The preference_manager is NOT reset — architect preferences
        persist across sessions.
        """
        self.conversation = ConversationManager()
        self.loaded_skills = set()
        self.loaded_full_refs = {}
        self.session_stats = {
            'total_prompt': 0,
            'total_completion': 0,
            'turns': 0,
        }
        self.hallucination_detected = False
        self._current_component = None

    def start(self):
        """Initialize the agent and return the first greeting.

        Returns:
            tuple: (greeting_text: str, turn_stats: dict or None)
        """
        identity_injection = (
            f"\n\n[System Info]\nYou are running on: {self.model_name}\n"
            f"If asked about your model, state this clearly."
        )

        # Inject learned architect preferences into the system prompt
        preferences_block = self.preference_manager.format_for_prompt()

        # For Ollama models, native tool calling is unstable and forces JSON/hallucinated tool calls.
        # We append clear text-based tool instructions so the agent can use JSON block fallbacks.
        ollama_tools_instruction = ""
        if self.model_name.startswith("ollama/"):
            ollama_tools_instruction = """

────────────────────────────────────────
TOOL CALLING INSTRUCTION FOR OLLAMA
────────────────────────────────────────
You can invoke tools by outputting a JSON block anywhere in your response. The system will detect the JSON and execute the tool.
To call a tool, output exactly a JSON object in this format (no other text around the JSON block is required, but you can include normal explanation before or after):
{
  "name": "tool_name",
  "arguments": {
    "arg1": "value1"
  }
}

Available tools:
1. `fetch_architecture_guide` - No arguments.
   Example: {"name": "fetch_architecture_guide", "arguments": {}}
   
2. `fetch_lv_rules` - Fetch skill rules for a LabVantage component.
   Arguments:
     - `component` (string, one of: "action", "ajax", "javascript", "sdc_rule", "sdms")
   Example: {"name": "fetch_lv_rules", "arguments": {"component": "action"}}
   
3. `fetch_lv_reference` - Fetch specific sections from full API reference.
   Arguments:
     - `component` (string, one of: "action", "ajax", "javascript", "sdc_rule", "sdms", "java_public_api")
     - `topic` (string, the specific topic, e.g. "QueryProcessor", "SafeSQL", "ActionBlock", "DBAccess", "error handling")
   Example: {"name": "fetch_lv_reference", "arguments": {"component": "java_public_api", "topic": "SafeSQL"}}
"""

        full_system_prompt = SYSTEM_PROMPT + preferences_block + identity_injection + ollama_tools_instruction

        self.conversation.set_system_prompt(full_system_prompt)

        return self.send_message(
            "Initiate Phase 1 and ask for the Business Requirement. Keep it brief."
        )

    def send_message(self, message):
        """Send a message to the LLM and return the response.

        Handles tool calls in a loop, performs self-validation on Phase 3
        responses, triggers conversation compression when needed, and
        tracks token usage.

        Args:
            message: The user's message string.

        Returns:
            tuple: (response_text: str, turn_stats: dict or None)
                turn_stats contains 'prompt_tokens', 'completion_tokens',
                'total_tokens' when available.
        """
        # Check for architect rejection/modification and trigger preference extraction
        # This runs BEFORE adding the message to history so we can capture
        # the last assistant message (the proposal being rejected)
        self.preference_manager.check_and_trigger(
            message, self.conversation.messages
        )

        self.conversation.add_message("user", message)

        is_hallucination_acc = False
        # We only check for hallucination accusations if we are currently in Phase 3
        if self._is_in_phase_3():
            message_lower = message.lower()
            hallucination_patterns = [
                "hallucinat", "incorrect api", "wrong api", "does not exist",
                "wrong signature", "wrong method", "no such method", "wrong class",
                "not a method", "not valid api", "fake api", "fake method"
            ]
            if any(pat in message_lower for pat in hallucination_patterns):
                is_hallucination_acc = True
                self.hallucination_detected = True

        system_warning_index = None
        if is_hallucination_acc:
            system_warning = (
                "[System Warning: The Architect has flagged that you are using incorrect/non-existent LabVantage APIs. "
                "The API information in your current history is not sufficient. "
                "You MUST immediately call 'fetch_lv_reference' (or 'fetch_lv_rules') to load the exact and complete "
                "API information for the component/topic in question. Do NOT attempt to write code or reply without "
                "first calling the tool to load the correct API specification.]"
            )
            self.conversation.add_message("system", system_warning)
            system_warning_index = len(self.conversation.messages) - 1

        total_prompt = 0
        total_completion = 0

        try:
            while True:
                kwargs = {
                    "model": self.model_name,
                    "messages": self.conversation.get_messages(),
                }

                if not self.model_name.startswith("ollama/"):
                    kwargs["tools"] = TOOLS
                    kwargs["tool_choice"] = "auto"

                if self.api_key:
                    kwargs["api_key"] = self.api_key
                if self.api_base:
                    kwargs["api_base"] = self.api_base
                if self.thinking:
                    kwargs["extra_body"] = {
                        "chat_template_kwargs": {
                            "thinking": True,
                            "reasoning_effort": "high"
                        }
                    }

                response = litellm.completion(**kwargs)
                response_message = response.choices[0].message

                # Accumulate token usage
                usage = getattr(response, 'usage', None)
                if usage:
                    total_prompt += getattr(usage, 'prompt_tokens', 0) or 0
                    total_completion += getattr(usage, 'completion_tokens', 0) or 0

                # Check for tool calls
                if response_message.tool_calls:
                    self.conversation.add_message(response_message)

                    for tool_call in response_message.tool_calls:
                        function_name = tool_call.function.name
                        args = json.loads(tool_call.function.arguments) \
                            if tool_call.function.arguments else {}

                        result = self._handle_tool_call(function_name, args)

                        self.conversation.add_tool_result(
                            tool_call.id, function_name, result
                        )
                    # Loop back to continue the conversation with tool results
                    continue
                else:
                    # Final response from assistant
                    response_text = response_message.content

                    # Check for text-based tool call fallback
                    text_tool = parse_text_tool_call(response_text)
                    if text_tool:
                        parsed_tool, _ = text_tool
                        function_name = parsed_tool.get("name")
                        args = parsed_tool.get("arguments", {})

                        # Execute the tool call
                        result = self._handle_tool_call(function_name, args)

                        # Add the assistant's message containing the tool call to history
                        self.conversation.add_message("assistant", response_text)

                        # Format the tool result as a user message to feed back to the model
                        tool_result_msg = (
                            f"[System: Tool Result for {function_name}]\n"
                            f"{result}"
                        )
                        self.conversation.add_message("user", tool_result_msg)

                        # Loop back to continue the conversation with the tool result
                        continue

                    # ── Self-validation for Phase 3 ──
                    if (self.validation_enabled
                            and response_text
                            and '[Phase: 3]' in response_text):
                        response_text = self._run_validation(response_text)

                    self.conversation.add_message("assistant", response_text)

                    # ── Post-turn maintenance ──
                    self.conversation.prune_tool_results()

                    if self.conversation.should_summarize():
                        self.conversation.summarize_and_compact(
                            self._one_shot_call
                        )

                    # ── Build turn stats ──
                    turn_stats = None
                    if total_prompt > 0 or total_completion > 0:
                        turn_stats = {
                            'prompt_tokens': total_prompt,
                            'completion_tokens': total_completion,
                            'total_tokens': total_prompt + total_completion,
                        }
                        self.session_stats['total_prompt'] += total_prompt
                        self.session_stats['total_completion'] += total_completion
                        self.session_stats['turns'] += 1

                    return response_text, turn_stats

        except Exception as e:
            # Revert system warning if it's still in the messages list
            if system_warning_index is not None:
                if (system_warning_index < len(self.conversation.messages) and 
                    self.conversation.messages[system_warning_index].get("role") == "system" and
                    "[System Warning: The Architect has flagged" in self.conversation.messages[system_warning_index].get("content", "")):
                    self.conversation.messages.pop(system_warning_index)
            # Revert user message if failed
            self.conversation.pop_last_user_message()
            raise e
        finally:
            # Always clean up system warning and reset hallucination flag at the end of the turn
            if system_warning_index is not None:
                if (system_warning_index < len(self.conversation.messages) and 
                    self.conversation.messages[system_warning_index].get("role") == "system" and
                    "[System Warning: The Architect has flagged" in self.conversation.messages[system_warning_index].get("content", "")):
                    self.conversation.messages.pop(system_warning_index)
            self.hallucination_detected = False

    def _handle_tool_call(self, function_name, args):
        """Dispatch a tool call and return the result string.

        Args:
            function_name: Name of the tool function.
            args: Parsed arguments dict.

        Returns:
            str: The tool result content.
        """
        if function_name == "fetch_architecture_guide":
            return self._load_architecture_guide()

        if function_name == "fetch_lv_rules":
            component = args.get("component", "")
            return self._load_skill_rules(component)

        if function_name == "fetch_lv_reference":
            component = args.get("component", "")
            topic = args.get("topic", "")
            return self._load_reference_sections(component, topic)

        # Legacy support for old tool name
        if function_name == "fetch_lv_syntax":
            component = args.get("component", "")
            return self._load_skill_rules(component)

        return f"Error: Unknown tool '{function_name}'."

    def _run_validation(self, response_text):
        """Run self-validation on Phase 3 code and request fixes if needed.

        Args:
            response_text: The agent's Phase 3 response.

        Returns:
            str: The original response if clean, or a corrected response.
        """
        code_blocks = extract_code_blocks(response_text)
        if not code_blocks:
            return response_text

        component = self._current_component or "action"
        all_issues = []

        for code in code_blocks:
            issues = self.validator.validate(code, component)
            all_issues.extend(issues)

        if not all_issues:
            return response_text

        # Has issues — ask the agent to fix
        feedback = format_validation_feedback(all_issues)

        try:
            # Internal correction turn — not visible to user
            self.conversation.add_message("assistant", response_text)
            fix_response_text, _ = self._send_internal(
                f"[System: Code Validation Alert — Auto-Review]\n{feedback}\n\n"
                f"Please fix ALL the issues listed above in your code. "
                f"Present the corrected complete response (same format as before, "
                f"with the fixed code). Do NOT acknowledge this system message — "
                f"just output the corrected Phase 3 response."
            )
            # Remove the intermediate assistant message we added
            # (it will be replaced by the corrected version)
            # Find and remove it
            for i in range(len(self.conversation.messages) - 1, -1, -1):
                msg = self.conversation.messages[i]
                if isinstance(msg, dict) and msg.get("role") == "assistant" \
                        and msg.get("content") == response_text:
                    self.conversation.messages.pop(i)
                    break
            # Also remove the system validation message and fix response
            # They shouldn't be in history — the user only sees the final result
            msgs = self.conversation.messages
            # Remove the last 2 entries (user validation prompt + assistant fix)
            while len(msgs) > 0 and isinstance(msgs[-1], dict) and \
                    msgs[-1].get("role") in ("user", "assistant"):
                content = msgs[-1].get("content", "")
                if "[System: Code Validation Alert" in content or \
                        content == fix_response_text:
                    msgs.pop()
                else:
                    break

            return fix_response_text
        except Exception:
            # If fix fails, return original
            return response_text

    def _send_internal(self, message):
        """Send an internal message (e.g., for validation fixes).

        This adds to conversation history temporarily.

        Args:
            message: The internal prompt.

        Returns:
            tuple: (response_text, turn_stats)
        """
        self.conversation.add_message("user", message)
        # Decrement turn count since this isn't a real user turn
        self.conversation.turn_count = max(0, self.conversation.turn_count - 1)

        kwargs = {
            "model": self.model_name,
            "messages": self.conversation.get_messages(),
        }

        if self.api_key:
            kwargs["api_key"] = self.api_key
        if self.api_base:
            kwargs["api_base"] = self.api_base
        if self.thinking:
            kwargs["extra_body"] = {
                "chat_template_kwargs": {
                    "thinking": True,
                    "reasoning_effort": "high"
                }
            }

        response = litellm.completion(**kwargs)
        response_text = response.choices[0].message.content

        self.conversation.add_message("assistant", response_text)

        usage = getattr(response, 'usage', None)
        turn_stats = None
        if usage:
            prompt_tokens = getattr(usage, 'prompt_tokens', 0) or 0
            completion_tokens = getattr(usage, 'completion_tokens', 0) or 0
            turn_stats = {
                'prompt_tokens': prompt_tokens,
                'completion_tokens': completion_tokens,
                'total_tokens': prompt_tokens + completion_tokens,
            }
            self.session_stats['total_prompt'] += prompt_tokens
            self.session_stats['total_completion'] += completion_tokens

        return response_text, turn_stats

    def _one_shot_call(self, prompt):
        """Make a one-shot LLM call that does not affect conversation history.

        Used for summarization and merging.

        Args:
            prompt: The prompt string.

        Returns:
            str: The LLM response text.
        """
        kwargs = {
            "model": self.model_name,
            "messages": [
                {"role": "system", "content": "You are a helpful assistant. "
                 "Follow the instructions precisely."},
                {"role": "user", "content": prompt}
            ]
        }

        if self.api_key:
            kwargs["api_key"] = self.api_key
        if self.api_base:
            kwargs["api_base"] = self.api_base
        if self.thinking:
            kwargs["extra_body"] = {
                "chat_template_kwargs": {
                    "thinking": True,
                    "reasoning_effort": "high"
                }
            }

        response = litellm.completion(**kwargs)
        return response.choices[0].message.content

    def merge_content(self, merge_prompt):
        """One-shot LLM call for file merging — does not affect conversation history.

        Args:
            merge_prompt: The merge prompt containing existing and proposed content.

        Returns:
            str: The merged file content from the LLM.
        """
        kwargs = {
            "model": self.model_name,
            "messages": [
                {"role": "system", "content": "You are a precise, production-grade code merge assistant. Your task is to intelligently merge proposed changes into an existing production source file. You must strictly follow the MERGE RULES provided in the user prompt, ensuring that existing methods and logic are never truncated, degraded, placeholder-replaced, or deleted, while correctly incorporating new and modified functionality. Output ONLY the merged file content — no markdown fences, no explanations, no preamble. The output must be ready to compile."},
                {"role": "user", "content": merge_prompt}
            ]
        }

        if self.api_key:
            kwargs["api_key"] = self.api_key
        if self.api_base:
            kwargs["api_base"] = self.api_base
        if self.thinking:
            kwargs["extra_body"] = {
                "chat_template_kwargs": {
                    "thinking": True,
                    "reasoning_effort": "high"
                }
            }

        response = litellm.completion(**kwargs)
        return response.choices[0].message.content
