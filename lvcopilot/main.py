import sys
import os
import re
# pyrefly: ignore [missing-import]
from dotenv import load_dotenv
from rich.console import Console
from rich.markdown import Markdown
from rich.panel import Panel
from rich.prompt import Prompt, Confirm
from .agent import LVDeveloperAgent
from . import file_merger

console = Console()

def configure_llm():
    # Load from current directory .lvcopilotenv
    env_path = os.path.join(os.getcwd(), '.lvcopilotenv')
    if os.path.exists(env_path):
        load_dotenv(dotenv_path=env_path)
        
    # Check for old GEMINI_API_KEY migration
    old_api_key = os.environ.get("GEMINI_API_KEY")
    if old_api_key and old_api_key != "your_api_key_here" and not os.environ.get("LLM_MODEL"):
        console.print("[yellow]Migrating existing GEMINI_API_KEY configuration...[/yellow]")
        os.environ["LLM_MODEL"] = "gemini/gemini-2.5-flash"
        os.environ["LLM_API_KEY"] = old_api_key
        try:
            with open(env_path, 'a') as f:
                f.write(f"\nLLM_MODEL=gemini/gemini-2.5-flash\nLLM_API_KEY={old_api_key}\n")
        except Exception:
            pass

    llm_model = os.environ.get("LLM_MODEL")
    llm_api_key = os.environ.get("LLM_API_KEY")
    llm_api_base = os.environ.get("LLM_API_BASE")
    
    if not llm_model:
        console.print("[yellow]LLM Configuration not found in the current project.[/yellow]")
        console.print("Supported formats:")
        console.print("  - OpenAI: [cyan]gpt-4o, gpt-4-turbo[/cyan]")
        console.print("  - Gemini: [cyan]gemini/gemini-2.5-flash, gemini/gemini-pro[/cyan]")
        console.print("  - Anthropic: [cyan]anthropic/claude-3-opus-20240229[/cyan]")
        console.print("  - Ollama: [cyan]ollama/llama3[/cyan]")
        try:
            llm_model = Prompt.ask("Please enter the LLM Model you want to use", default="gemini/gemini-2.5-flash").strip()
            if not llm_model:
                llm_model = "gemini/gemini-2.5-flash"
                
            needs_key = not llm_model.startswith("ollama/")
            if needs_key:
                llm_api_key = Prompt.ask("Please enter your API Key", password=True).strip()
                if not llm_api_key:
                    console.print("[bold red]API Key is required for this model. Exiting.[/bold red]")
                    sys.exit(1)
                    
            llm_api_base = Prompt.ask("Please enter your API Base URL (optional, press Enter to skip)").strip()
            
        except EOFError:
            console.print("\n[bold red]Configuration aborted. Exiting.[/bold red]")
            sys.exit(1)
            
        # Save to .lvcopilotenv in current directory
        try:
            with open(env_path, 'a') as f:
                f.write(f"\nLLM_MODEL={llm_model}\n")
                if llm_api_key:
                    f.write(f"LLM_API_KEY={llm_api_key}\n")
                if llm_api_base:
                    f.write(f"LLM_API_BASE={llm_api_base}\n")
            console.print(f"[green]LLM Configuration saved to {env_path}[/green]")
        except Exception as e:
            console.print(f"[bold yellow]Warning: Could not save LLM Configuration to {env_path}: {e}[/bold yellow]")
            
        # Update environ for the current run
        os.environ["LLM_MODEL"] = llm_model
        if llm_api_key:
            os.environ["LLM_API_KEY"] = llm_api_key
        if llm_api_base:
            os.environ["LLM_API_BASE"] = llm_api_base

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
                    console.print(f"[bold yellow]Warning: Could not read file {filepath}: {e}[/bold yellow]")
            elif os.path.isdir(filepath):
                try:
                    files = os.listdir(filepath)
                    context += f"\n\n--- Contents of directory {filepath} ---\n"
                    for file in files:
                        context += f"{file}\n"
                except Exception as e:
                    console.print(f"[bold yellow]Warning: Could not list directory {filepath}: {e}[/bold yellow]")
    
    if context:
        user_input += "\n\n[System Context injected based on user @ references]:" + context
        
    return user_input

def process_and_save_files(response, agent):
    # Only prompt for file saving if we are in Phase 4
    if not re.search(r'\[Phase:\s*4\]', response, re.IGNORECASE):
        return

    # Find all code blocks
    pattern = r'```([a-zA-Z]*)\n(.*?)```'
    matches = list(re.finditer(pattern, response, re.DOTALL))
    
    if not matches:
        return

    console.print("\n[bold cyan]--- Code Generation Detected ---[/bold cyan]")
    
    # ── Phase 1: Pre-scan — collect all file entries and resolve paths ──
    file_entries = []
    
    for match in matches:
        lang = match.group(1)
        code = match.group(2)
        
        # Look at text before this match to find a filename
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
            # Extract just the basename so the agent only decides the file name
            filename = os.path.basename(path_match.group(1).strip())
            # Clean up common markdown artifacts
            filename = filename.strip('*_`\"\' ')
        
        file_entries.append({
            'filename': filename,
            'lang': lang,
            'code': code,
            'abs_path': None,
            'exists': False,
            'existing_content': None
        })
    
    # ── Phase 2: Resolve paths — prompt user for directory/path per file ──
    resolved_entries = []
    
    for entry in file_entries:
        filename = entry['filename']
        lang = entry['lang']
        
        if filename:
            console.print(f"\n[bold blue][LVCopilot][/bold blue] Found code for: [cyan]{filename}[/cyan]")
            choice = Confirm.ask("Do you want to save this file?")
        else:
            console.print(f"\n[bold blue][LVCopilot][/bold blue] Found a [cyan]{lang if lang else 'code'}[/cyan] block without a clear file name.")
            choice = Confirm.ask("Do you want to save this code block?")
        
        if not choice:
            console.print("⏭️  [dim]Skipped.[/dim]")
            continue
        
        # Get the target path from user
        while True:
            if filename:
                location = Prompt.ask(f"Enter the directory path to save '{filename}' to (e.g., ./src/actions/)").strip()
                if not location:
                    console.print("[bold red]Validation Error:[/bold red] Directory path cannot be empty.")
                    continue
                
                expanded_location = os.path.expanduser(location)
                final_path = os.path.join(expanded_location, filename)
            else:
                filepath_input = Prompt.ask("Enter the full file path (including file name) to save to").strip()
                
                if not filepath_input:
                    console.print("[bold red]Validation Error:[/bold red] File path cannot be empty. Please provide the file name along with the file path.")
                    continue
                    
                if filepath_input.endswith('/') or filepath_input.endswith('\\'):
                    console.print("[bold red]Validation Error:[/bold red] You provided a directory path. Please provide the file name along with the file path.")
                    continue
                    
                expanded_path = os.path.expanduser(filepath_input)
                if os.path.isdir(expanded_path):
                    console.print(f"[bold red]Validation Error:[/bold red] '{filepath_input}' is an existing directory. Please provide the file name along with the file path.")
                    continue
                    
                final_path = expanded_path
                filename = os.path.basename(final_path)
                
            break
        
        abs_path = os.path.abspath(final_path)
        exists, existing_content = file_merger.detect_existing_file(abs_path)
        
        entry['filename'] = filename
        entry['abs_path'] = abs_path
        entry['exists'] = exists
        entry['existing_content'] = existing_content
        resolved_entries.append(entry)
    
    if not resolved_entries:
        return
    
    # ── Phase 3: Summary display ──
    existing_entries = [e for e in resolved_entries if e['exists']]
    new_entries = [e for e in resolved_entries if not e['exists']]
    
    console.print("\n[bold cyan]--- File Summary ---[/bold cyan]")
    for entry in resolved_entries:
        status = "[yellow][UPDATE][/yellow]" if entry['exists'] else "[green][NEW]   [/green]"
        console.print(f"  {status} {entry['filename']:<30} → {entry['abs_path']}")
    console.print("")
    
    # ── Phase 4: Determine batch action for existing files ──
    batch_action = None  # None means new-only, 'm', 'o', 's', or 'p'
    
    if existing_entries:
        console.print(f"⚠️  [bold yellow]{len(existing_entries)} of {len(resolved_entries)} file(s) already exist.[/bold yellow]")
        
        if len(existing_entries) == 1:
            # Single existing file — go directly to per-file mode
            batch_action = 'p'
        else:
            # Multiple existing files — offer batch options
            console.print("Choose action for existing files:")
            console.print("  (m) Merge all   — smart LLM merge for all existing files")
            console.print("  (o) Overwrite all — replace all existing files with new content")
            console.print("  (s) Skip all    — skip all existing files, only save new ones")
            console.print("  (p) Per-file    — decide individually for each file")
            batch_choice = Prompt.ask("Your choice", choices=['m', 'o', 's', 'p'])
            batch_action = batch_choice
    
    # ── Phase 5: Process each file ──
    merge_max_lines = file_merger.get_merge_max_lines()
    
    for entry in resolved_entries:
        filename = entry['filename']
        code = entry['code']
        abs_path = entry['abs_path']
        exists = entry['exists']
        existing_content = entry['existing_content']
        
        if not exists:
            # New file — write directly
            _write_new_file(abs_path, code, filename)
            continue
        
        # Existing file — determine action
        action = batch_action
        
        if action == 'p':
            # Per-file prompt
            action = Prompt.ask(f"  {filename}: (m)erge, (o)verwrite, or (s)kip?", choices=['m', 'o', 's'])
        
        if action == 's':
            console.print(f"  ⏭️  [dim]Skipped: {filename}[/dim]")
            continue
        
        if action == 'o':
            # Overwrite — backup first, then write
            file_merger.create_backup(abs_path)
            _write_new_file(abs_path, code, filename)
            continue
        
        if action == 'm':
            # Merge — check file size, call LLM, show diff, confirm
            _perform_merge(agent, entry, merge_max_lines)
            continue


def _perform_merge(agent, entry, merge_max_lines):
    """Handle the LLM-based merge workflow for a single file.
    
    Args:
        agent: The LVDeveloperAgent instance.
        entry: File entry dict with filename, code, abs_path, existing_content.
        merge_max_lines: Max line threshold from MERGE_MAX_LINES env var.
    """
    filename = entry['filename']
    abs_path = entry['abs_path']
    existing_content = entry['existing_content']
    proposed_content = entry['code']
    
    if existing_content is None:
        console.print(f"  ⚠️  [bold yellow]Could not read existing file: {filename}. Falling back to overwrite.[/bold yellow]")
        file_merger.create_backup(abs_path)
        _write_new_file(abs_path, proposed_content, filename)
        return
    
    # Check file size against threshold
    line_count = len(existing_content.splitlines())
    if line_count > merge_max_lines:
        console.print(f"  ⚠️  [bold yellow]File has {line_count} lines (limit: {merge_max_lines}). LLM merge may consume significant tokens.[/bold yellow]")
        proceed = Confirm.ask("  Proceed with merge?")
        if not proceed:
            console.print(f"  ⏭️  [dim]Skipped merge for: {filename}[/dim]")
            return
    
    # Call LLM for merge
    console.print(f"  🔄 Merging {filename}...")
    try:
        merged_content = file_merger.merge_files(agent, existing_content, proposed_content, filename)
    except Exception as e:
        console.print(f"  ❌ [bold red]Merge failed: {e}[/bold red]")
        fallback = Prompt.ask("  Do you want to (o)verwrite or (s)kip?", choices=['o', 's'])
        if fallback == 'o':
            file_merger.create_backup(abs_path)
            _write_new_file(abs_path, proposed_content, filename)
        else:
            console.print(f"  ⏭️  [dim]Skipped: {filename}[/dim]")
        return
    
    # Show diff
    file_merger.show_diff(existing_content, merged_content, filename)
    
    # Confirm before writing
    confirm = Confirm.ask(f"  Write merged content to {filename}?")
    if confirm:
        file_merger.create_backup(abs_path)
        _write_new_file(abs_path, merged_content, filename)
    else:
        console.print(f"  ⏭️  [dim]Merge discarded for: {filename}[/dim]")


def _write_new_file(abs_path, content, filename):
    """Write content to a file, creating directories as needed.
    
    Args:
        abs_path: Absolute path to write to.
        content: File content string.
        filename: Filename for display purposes.
    """
    try:
        os.makedirs(os.path.dirname(abs_path) or '.', exist_ok=True)
        with open(abs_path, 'w', encoding='utf-8') as f:
            f.write(content)
        console.print(f"  ✅ [green]Saved to: {abs_path}[/green]")
    except Exception as e:
        console.print(f"  ❌ [bold red]Error saving {filename}: {e}[/bold red]")


def main():
    console.print(Panel.fit("[bold blue]Initializing Autonomous LV Developer Agent CLI...[/bold blue]"))
    
    configure_llm()
    
    console.print("[dim]Loading Knowledge Base and configuring LLM...[/dim]")
    try:
        agent = LVDeveloperAgent()
        initial_greeting = agent.start()
    except ValueError as ve:
        console.print(f"[bold red]Configuration Error:[/bold red] {ve}")
        sys.exit(1)
    except Exception as e:
        console.print(f"[bold red]Failed to start agent. Error:[/bold red] {e}")
        sys.exit(1)
        
    console.print(Panel(Markdown(initial_greeting), title="🤖 LV Agent", border_style="blue"))
    
    while True:
        try:
            user_input = console.input("\n[bold cyan]👤 Architect (type 'exit' to quit): [/bold cyan]")
            if user_input.lower() in ['exit', 'quit']:
                console.print("[bold green]Goodbye![/bold green]")
                break
                
            if not user_input.strip():
                continue
                
            with console.status("[bold green]🤖 LV Agent is thinking...[/bold green]", spinner="dots"):
                processed_input = process_at_references(user_input)
                response = agent.send_message(processed_input)
                
            console.print(Panel(Markdown(response), title="🤖 LV Agent", border_style="blue"))
            process_and_save_files(response, agent)
            
        except KeyboardInterrupt:
            console.print("\n[dim]Exiting...[/dim]")
            break
        except Exception as e:
            console.print(f"\n[bold red]Error:[/bold red] {e}\n")

if __name__ == "__main__":
    main()

