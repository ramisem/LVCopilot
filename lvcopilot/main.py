import sys
import os
import re
# pyrefly: ignore [missing-import]
from dotenv import load_dotenv
try:
    from .agent import LVDeveloperAgent
except ImportError:
    from agent import LVDeveloperAgent

def ensure_api_key():
    # Load from current directory .env
    env_path = os.path.join(os.getcwd(), '.env')
    if os.path.exists(env_path):
        load_dotenv(dotenv_path=env_path)
        
    api_key = os.environ.get("GEMINI_API_KEY")
    if not api_key or api_key == "your_api_key_here":
        print("GEMINI_API_KEY not found in the current project.")
        try:
            api_key = input("Please enter your Gemini API Key: ").strip()
        except EOFError:
            api_key = ""
            
        if not api_key:
            print("API Key is required. Exiting.")
            sys.exit(1)
            
        # Save to .env in current directory
        try:
            with open(env_path, 'a') as f:
                f.write(f"\nGEMINI_API_KEY={api_key}\n")
            print(f"API Key saved to {env_path}")
        except Exception as e:
            print(f"Warning: Could not save API Key to {env_path}: {e}")
            
        # Update environ for the current run
        os.environ["GEMINI_API_KEY"] = api_key

def process_at_references(user_input):
    # Match @ followed by non-space characters
    matches = set(re.findall(r'@([^\s]+)', user_input))
    context = ""
    for match in matches:
        filepath = match
        if os.path.exists(filepath):
            if os.path.isfile(filepath):
                try:
                    with open(filepath, 'r', encoding='utf-8') as f:
                        content = f.read()
                    context += f"\n\n--- Content of {filepath} ---\n{content}\n"
                except Exception as e:
                    print(f"Warning: Could not read file {filepath}: {e}")
            elif os.path.isdir(filepath):
                try:
                    files = os.listdir(filepath)
                    context += f"\n\n--- Contents of directory {filepath} ---\n"
                    for file in files:
                        context += f"{file}\n"
                except Exception as e:
                    print(f"Warning: Could not list directory {filepath}: {e}")
    
    if context:
        user_input += "\n\n[System Context injected based on user @ references]:" + context
        
    return user_input

def process_and_save_files(response):
    # Only prompt for file saving if we are in Phase 4
    if not re.search(r'\[Phase:\s*4\]', response, re.IGNORECASE):
        return

    # Find all code blocks
    pattern = r'```([a-zA-Z]*)\n(.*?)```'
    matches = list(re.finditer(pattern, response, re.DOTALL))
    
    if not matches:
        return

    print("\n--- Code Generation Detected ---")
    
    for match in matches:
        lang = match.group(1)
        code = match.group(2)
        
        # We can look at the text before this match to find a filename
        start_idx = match.start()
        text_before = response[:start_idx]
        
        # Get the last non-empty line before the code block
        lines_before = [line for line in text_before.split('\n') if line.strip()]
        last_line = lines_before[-1] if lines_before else ""
        
        filename = None
        
        # Regex to extract a file name with an extension from the last line
        path_pattern = r'([a-zA-Z0-9_\-\./\\]+\.[a-zA-Z0-9]{2,})'
        path_match = re.search(path_pattern, last_line)
        if path_match:
            # We extract just the basename so the agent only decides the file name
            filename = os.path.basename(path_match.group(1).strip())
            # Clean up common markdown artifacts just in case
            filename = filename.strip('*_`\"\'')
            
        if filename:
            print(f"\n[LVCopilot] Found code for: {filename}")
            choice = input(f"Do you want to save this file? (y/n): ").strip().lower()
        else:
            print(f"\n[LVCopilot] Found a {lang if lang else 'code'} block without a clear file name.")
            choice = input("Do you want to save this code block? (y/n): ").strip().lower()
            
        if choice == 'y':
            while True:
                if filename:
                    location = input(f"Enter the directory path to save '{filename}' to (e.g., ./src/actions/): ").strip()
                    if not location:
                        print("Validation Error: Directory path cannot be empty.")
                        continue
                    
                    expanded_location = os.path.expanduser(location)
                    final_path = os.path.join(expanded_location, filename)
                else:
                    filepath = input("Enter the full file path (including file name) to save to: ").strip()
                    
                    if not filepath:
                        print("Validation Error: File path cannot be empty. Please provide the file name along with the file path.")
                        continue
                        
                    if filepath.endswith('/') or filepath.endswith('\\'):
                        print("Validation Error: You provided a directory path. Please provide the file name along with the file path.")
                        continue
                        
                    expanded_path = os.path.expanduser(filepath)
                    if os.path.isdir(expanded_path):
                        print(f"Validation Error: '{filepath}' is an existing directory. Please provide the file name along with the file path.")
                        continue
                        
                    final_path = expanded_path
                    
                break

            try:
                # Expand ~ to user home directory
                abs_path = os.path.abspath(final_path)
                os.makedirs(os.path.dirname(abs_path) or '.', exist_ok=True)
                with open(abs_path, 'w', encoding='utf-8') as f:
                    f.write(code)
                print(f"✅ Saved to: {abs_path}")
            except Exception as e:
                print(f"❌ Error saving to {abs_path if 'abs_path' in locals() else final_path}: {e}")
        else:
            print(f"⏭️  Skipped saving.")


def main():
    print("=" * 60)
    print("  Initializing Autonomous LV Developer Agent CLI...")
    print("=" * 60 + "\n")
    
    ensure_api_key()
    
    print("  Loading Knowledge Base and configuring Gemini...")
    try:
        agent = LVDeveloperAgent()
        initial_greeting = agent.start()
    except ValueError as ve:
        print(f"Configuration Error: {ve}")
        sys.exit(1)
    except Exception as e:
        print(f"Failed to start agent. Error: {e}")
        sys.exit(1)
        
    print(f"🤖 LV Agent:\n{initial_greeting}\n")
    
    while True:
        try:
            user_input = input("👤 Architect (type 'exit' to quit): ")
            if user_input.lower() in ['exit', 'quit']:
                print("Goodbye!")
                break
                
            if not user_input.strip():
                continue
                
            print("\n🤖 LV Agent is thinking...\n")
            processed_input = process_at_references(user_input)
            response = agent.send_message(processed_input)
            print(f"🤖 LV Agent:\n{response}\n")
            process_and_save_files(response)
            
        except KeyboardInterrupt:
            print("\nExiting...")
            break
        except Exception as e:
            print(f"\nError: {e}\n")

if __name__ == "__main__":
    main()
