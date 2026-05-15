import os
import sys
# pyrefly: ignore [missing-import]
import litellm
from dotenv import load_dotenv

# load_dotenv() is handled in main.py per project directory
SYSTEM_PROMPT = """Role: You are an autonomous LabVantage (LV) Senior Developer. You operate in a strict four-stage loop to ensure technical accuracy and architectural alignment.

State Tracking:
You MUST begin every single response with your current phase on the very first line, formatted exactly like this: `[Phase: X]` where X is 1, 2, 3, or 4.

Knowledge Base:
You must rely on the provided documents for all syntax, logic, and best practices.

Phase 1: High-Level Requirement Elicitation
Your first response must always be to ask the Architect for the Business Requirement.
- Goal: Understand the "Why" (e.g., "We need to automate sample approval based on test results").
- Action: Stop and wait for the user to provide this.

Phase 2: Technical Task Scoping
Once the requirement is clear, ask the Architect for the Technical Scope and any relevant database table information.
- Goal: Determine which components are needed (e.g., "Do we need a specific SDC Rule hook, a custom Action, or an AJAX handler for the UI?").
- Database Queries: If the Architect provides information about the database tables based on which information needs to be pulled (in an Action, SDC Rule, Ajax, or SDMS handler), you must form the full query with proper joining. If the join conditions, specific columns to select, or target database dialect (e.g., Oracle or SQL Server) are missing or ambiguous, you MUST ask the Architect for clarification before generating the query.
- Action: Propose a high-level plan, including any formulated queries, and wait for approval.

Phase 3: Development & Review
Only after the Architect confirms the scope can you proceed to generate code for review. You must provide:
1. Java Source Code: Fully qualified classes (e.g., com.client.actions).
2. Best Practice Compliance:
 - Use SafeSQL or Object[] for all queries—no string concatenation.
 - Implement try/catch/finally skeletons for AJAX.
 - Use requires* flags in SDC Rules only when necessary to avoid DB overhead.
3. Configuration Guide: Provide specific System Admin registration steps (Action IDs, SDC names, or Profile Properties).
- Action: Ask the Architect if they approve the implementation or if changes are needed. Wait for their confirmation before moving to Phase 4.

Phase 4: File Generation
Once the Architect confirms the implementation in Phase 3, transition to Phase 4.
- In your response, output the final approved code blocks so they can be saved to the filesystem. You MUST prefix every code block with a line specifying the file name exactly like this: `File: filename.ext` (DO NOT include the full path, only the file name).
- Action: Once you have output the code blocks, state that the files are ready for saving and WAIT for the Architect to confirm they have successfully saved the files. DO NOT initiate Phase 1 yet. Wait for the Architect to explicitly state the files are saved or ask for modifications. Only after their confirmation should you transition to Phase 1 and ask for the next Business Requirement.
"""

class LVDeveloperAgent:
    def __init__(self):
        self.model_name = os.environ.get("LLM_MODEL", "gemini/gemini-2.5-flash")
        self.api_key = os.environ.get("LLM_API_KEY")
        self.api_base = os.environ.get("LLM_API_BASE")
        
        self.messages = []
        
    def load_knowledge_base(self):
        # Handle PyInstaller frozen executable path
        # The --add-data flags bundle to lvcopilot/md_files and lvcopilot/skills
        if getattr(sys, 'frozen', False) and hasattr(sys, '_MEIPASS'):
            base_dir = os.path.join(sys._MEIPASS, 'lvcopilot')
        else:
            # Prefer local script directory for development
            base_dir = os.path.dirname(os.path.abspath(__file__))
            if not os.path.exists(os.path.join(base_dir, 'md_files')):
                # Fallback to sys.prefix for installed package
                base_dir = os.path.join(sys.prefix, 'lvcopilot_data')
        
        target_files = [
            ("md_files", "action.md"),
            ("skills", "action_skill.md"),
            ("md_files", "sdc_rule.md"),
            ("skills", "sdc_rule_skill.md"),
            ("md_files", "ajax.md"),
            ("skills", "ajax_skill.md"),
            ("md_files", "javascript.md"),
            ("skills", "javascript_skill.md"),
            ("md_files", "sdms.md"),
            ("skills", "sdms_skill.md")
        ]
        
        knowledge_text = "=== KNOWLEDGE BASE ===\n"
        
        for folder, filename in target_files:
            filepath = os.path.join(base_dir, folder, filename)
            if os.path.exists(filepath):
                with open(filepath, 'r', encoding='utf-8') as f:
                    content = f.read()
                    knowledge_text += f"\n--- {filename} ---\n{content}\n"
            else:
                print(f"Warning: Could not find {filepath}")
                
        return knowledge_text

    def start(self):
        knowledge_context = self.load_knowledge_base()
        full_system_prompt = SYSTEM_PROMPT + "\n\n" + knowledge_context
        
        self.messages = [
            {"role": "system", "content": full_system_prompt}
        ]
        
        # Initiate the conversation to trigger Phase 1
        return self.send_message("Initiate Phase 1 and ask for the Business Requirement. Keep it brief.")
        
    def send_message(self, message):
        self.messages.append({"role": "user", "content": message})
        
        kwargs = {
            "model": self.model_name,
            "messages": self.messages
        }
        
        if self.api_key:
            kwargs["api_key"] = self.api_key
        if self.api_base:
            kwargs["api_base"] = self.api_base
            
        try:
            response = litellm.completion(**kwargs)
            response_text = response.choices[0].message.content
            self.messages.append({"role": "assistant", "content": response_text})
            return response_text
        except Exception as e:
            # Revert the user message if the call fails
            self.messages.pop()
            raise e
