import sys
import os
import re
# pyrefly: ignore [missing-import]
from dotenv import load_dotenv
from rich.console import Console
from rich.markdown import Markdown
from rich.panel import Panel
from rich.prompt import Prompt, Confirm
# pyrefly: ignore [missing-import]
from prompt_toolkit import PromptSession
# pyrefly: ignore [missing-import]
from prompt_toolkit.formatted_text import HTML
# pyrefly: ignore [missing-import]
from prompt_toolkit.key_binding import KeyBindings
from .agent import LVDeveloperAgent
from . import file_merger
from . import code_investigator

console = Console()

# Track investigated file paths from Modify mode Phase 1.5
# Maps basename -> absolute path so Phase 4 can auto-resolve without re-asking
_investigated_files = {}


def _find_at_token(text):
    """Find the @path token at the end of text. Returns (start_index, path_prefix) or None."""
    # Walk backwards to find the last @ that starts a token
    i = len(text) - 1
    while i >= 0:
        if text[i] == '@' and (i == 0 or text[i - 1].isspace()):
            path_prefix = text[i + 1:]
            if ' ' not in path_prefix:
                return i, path_prefix
        if text[i].isspace():
            break
        i -= 1
    return None


def _list_matches(path_prefix):
    """List filesystem matches for a partial path. Returns list of (name, is_dir) tuples."""
    expanded = os.path.expanduser(path_prefix)
    if os.path.isdir(expanded):
        dirname, basename = expanded, ''
    else:
        dirname, basename = os.path.dirname(expanded), os.path.basename(expanded)
    if not dirname:
        dirname = '.'
    if not os.path.isdir(dirname):
        return []
    try:
        items = sorted(os.listdir(dirname))
    except OSError:
        return []
    matches = []
    for item in items:
        if item.startswith(basename):
            full = os.path.join(dirname, item)
            matches.append((item, os.path.isdir(full)))
    return matches


def _longest_common_prefix(names):
    """Return the longest common prefix of a list of strings."""
    if not names:
        return ''
    prefix = names[0]
    for name in names[1:]:
        while not name.startswith(prefix):
            prefix = prefix[:-1]
            if not prefix:
                return ''
    return prefix


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
        console.print("  - NVIDIA (OpenAI-compat): [cyan]openai/deepseek-ai/deepseek-v4-flash[/cyan] + API base [cyan]https://integrate.api.nvidia.com/v1[/cyan]")
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
            
            enable_thinking = Confirm.ask(
                "Enable reasoning/thinking mode? (Only supported by NVIDIA DeepSeek — say No for all other models)",
                default=False
            )
            
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
                if enable_thinking:
                    f.write("LLM_THINKING=true\n")
            console.print(f"[green]LLM Configuration saved to {env_path}[/green]")
        except Exception as e:
            console.print(f"[bold yellow]Warning: Could not save LLM Configuration to {env_path}: {e}[/bold yellow]")
            
        # Update environ for the current run
        os.environ["LLM_MODEL"] = llm_model
        if llm_api_key:
            os.environ["LLM_API_KEY"] = llm_api_key
        if llm_api_base:
            os.environ["LLM_API_BASE"] = llm_api_base
        if enable_thinking:
            os.environ["LLM_THINKING"] = "true"

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
        # Remove the '@' symbol from valid paths in the prompt so the agent sees normal file paths
        for match in matches:
            if os.path.exists(match):
                user_input = user_input.replace(f"@{match}", match)
        
        user_input += "\n\n[System Context injected based on user @ references]:" + context
        
    return user_input


def detect_agent_mode(response):
    """Detect if agent is in Modify mode based on response markers.
    
    Returns:
        str: 'modify' or 'new'
    """
    if re.search(r'\[Mode:\s*Modify\]', response, re.IGNORECASE):
        return 'modify'
    return 'new'


def detect_investigation_phase(response):
    """Check if the agent is in Phase 1.5 (investigation setup).
    
    Returns:
        bool: True if [Phase: 1.5] is found in the response.
    """
    return bool(re.search(r'\[Phase:\s*1\.5\]', response, re.IGNORECASE))


def handle_investigation_phase(session):
    """Prompt the architect for investigation starting points (supports multiple files).
    
    Asks for one or more file paths and optionally a method/code hint per file,
    then builds a combined investigation context using code_investigator.
    
    Args:
        session: The PromptSession for input.
    
    Returns:
        str: The user's text combined with investigation context for all files,
             ready to send to the agent.
    """
    console.print("\n[bold cyan]📂 Investigation Mode[/bold cyan]")
    console.print("[dim]The agent needs to understand the existing code before proposing changes.[/dim]")
    console.print("[dim]Provide the file path(s) to investigate (you can use @path for autocomplete).[/dim]\n")
    
    from prompt_toolkit.formatted_text import HTML as _HTML
    
    all_contexts = []
    file_descriptions = []
    file_count = 0
    
    while True:
        label = "Primary" if file_count == 0 else "Additional"
        file_input = session.prompt(_HTML(f"\n<ansicyan><b>📄 {label} file path to investigate: </b></ansicyan>"))
        file_input = file_input.strip()
        
        if not file_input:
            if file_count == 0:
                console.print("[bold yellow]No file provided. The agent will continue without investigation context.[/bold yellow]")
                return ""
            else:
                # No more files — done
                break
        
        # Extract the file path — handle @prefix if used
        file_path = file_input.lstrip('@').strip()
        
        # Get optional method/code hint
        method_hint = session.prompt(_HTML("\n<ansicyan><b>🔍 Starting method/code (optional, press Enter to skip): </b></ansicyan>"))
        method_hint = method_hint.strip() if method_hint else None
        
        # Build the investigation context for this file
        abs_path = os.path.abspath(os.path.expanduser(file_path))
        if not os.path.isfile(abs_path):
            console.print(f"[bold yellow]Warning: File '{file_path}' not found. Skipping.[/bold yellow]")
        else:
            console.print(f"  📖 [dim]Reading: {abs_path}[/dim]")
            context = code_investigator.build_investigation_context(file_path, method_hint)
            all_contexts.append(context)
            
            # Remember this file path so Phase 4 can auto-resolve it
            basename = os.path.basename(abs_path)
            _investigated_files[basename] = abs_path
            
            desc = f"{file_path}"
            if method_hint:
                desc += f" (method: {method_hint})"
            file_descriptions.append(desc)
            file_count += 1
            console.print(f"  ✅ [green]File {file_count} loaded[/green]")
        
        # Ask if there are more files
        add_more = Confirm.ask("\nDo you have another file to investigate?", default=False)
        if not add_more:
            break
    
    if not all_contexts:
        return ""
    
    # Build the combined user message
    user_text = "Here are the files to investigate:\n"
    for desc in file_descriptions:
        user_text += f"  - {desc}\n"
    user_text += "\n" + "\n\n".join(all_contexts)
    
    total_chars = sum(len(c) for c in all_contexts)
    console.print(f"\n  📋 [green]Investigation context prepared: {file_count} file(s), {total_chars} chars total[/green]")
    
    return user_text


MAX_AUTO_INVESTIGATION_ROUNDS = 5


def _parse_investigate_markers(response):
    """Extract [Investigate: filename.ext] markers from an agent response.
    
    Args:
        response: The agent's response text.
    
    Returns:
        list[str]: List of filenames requested for investigation.
    """
    return re.findall(r'\[Investigate:\s*([^\]]+)\]', response, re.IGNORECASE)


def _run_auto_investigation(agent, last_response):
    """Auto-investigation loop: detect file requests and auto-feed them.
    
    Scans the agent's response for [Investigate: filename.ext] markers,
    searches the project for those files, reads them, and sends them
    back to the agent. Repeats until no more files are requested or
    the round limit is reached.
    
    Args:
        agent: The LVDeveloperAgent instance.
        last_response: The agent's most recent response text.
    """
    # Build search roots from investigated file directories + CWD
    search_roots = set()
    search_roots.add(os.getcwd())
    for abs_path in _investigated_files.values():
        parent = os.path.dirname(abs_path)
        search_roots.add(parent)
        # Also add the grandparent (project root is often one level up from src/)
        grandparent = os.path.dirname(parent)
        if grandparent:
            search_roots.add(grandparent)
    
    for round_num in range(1, MAX_AUTO_INVESTIGATION_ROUNDS + 1):
        requested_files = _parse_investigate_markers(last_response)
        
        if not requested_files:
            # No more files requested — investigation is done
            return
        
        # Deduplicate and filter out already-investigated files
        unique_requests = []
        seen = set()
        for fname in requested_files:
            fname = fname.strip()
            if fname not in seen and fname not in _investigated_files:
                unique_requests.append(fname)
                seen.add(fname)
        
        if not unique_requests:
            return
        
        console.print(f"\n[bold cyan]🔎 Auto-Investigation Round {round_num}[/bold cyan]")
        console.print(f"[dim]The agent needs {len(unique_requests)} more file(s):[/dim]")
        for fname in unique_requests:
            console.print(f"  [dim]• {fname}[/dim]")
        
        # Locate and read each requested file
        found_contexts = []
        not_found = []
        
        for fname in unique_requests:
            found_path = code_investigator.find_file_in_project(fname, list(search_roots))
            if found_path:
                console.print(f"  ✅ [green]Found: {found_path}[/green]")
                context = code_investigator.build_investigation_context(found_path)
                found_contexts.append(context)
                # Track for Phase 4 auto-resolution
                _investigated_files[fname] = found_path
                # Add this file's directory to search roots for future rounds
                search_roots.add(os.path.dirname(found_path))
            else:
                console.print(f"  ⚠️  [yellow]Not found: {fname}[/yellow]")
                not_found.append(fname)
        
        if not found_contexts:
            # None of the requested files were found
            not_found_msg = "The following files could not be located in the project:\n"
            for fname in not_found:
                not_found_msg += f"  - {fname}\n"
            not_found_msg += "Please continue your investigation with the files already provided, or ask the Architect for help locating these files."
            with console.status("[bold green]🤖 LV Agent continuing investigation...[/bold green]", spinner="dots"):
                last_response = agent.send_message(not_found_msg)
            console.print(Panel(Markdown(last_response), title=f"🤖 LV Agent — Investigation (Round {round_num})", border_style="yellow"))
            continue
        
        # Build the auto-investigation message
        auto_msg = f"[System: Auto-Investigation Round {round_num}]\n"
        auto_msg += f"Found {len(found_contexts)} of {len(unique_requests)} requested file(s).\n"
        if not_found:
            auto_msg += f"Could NOT find: {', '.join(not_found)}\n"
        auto_msg += "\n" + "\n\n".join(found_contexts)
        
        with console.status("[bold green]🤖 LV Agent continuing investigation...[/bold green]", spinner="dots"):
            last_response = agent.send_message(auto_msg)
        console.print(Panel(Markdown(last_response), title=f"🤖 LV Agent — Investigation (Round {round_num})", border_style="yellow"))
    
    if _parse_investigate_markers(last_response):
        console.print(f"[bold yellow]⚠️  Investigation round limit ({MAX_AUTO_INVESTIGATION_ROUNDS}) reached. The agent may continue with partial context.[/bold yellow]")


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
        
        # Check if this file was previously investigated (Modify mode)
        # If so, auto-resolve the path without asking
        if filename and filename in _investigated_files:
            abs_path = _investigated_files[filename]
            console.print(f"\n[bold blue][LVCopilot][/bold blue] Modified file: [cyan]{filename}[/cyan] → {abs_path}")
            choice = Confirm.ask("Do you want to save the modified file?")
            
            if not choice:
                console.print("⏭️  [dim]Skipped.[/dim]")
                continue
            
            exists, existing_content = file_merger.detect_existing_file(abs_path)
            entry['filename'] = filename
            entry['abs_path'] = abs_path
            entry['exists'] = exists
            entry['existing_content'] = existing_content
            resolved_entries.append(entry)
            continue
        
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
    
    # Shell-style Tab completion for @paths — inline cycling (no dropdown, no printing)
    # State for cycling through multiple matches
    _tab_state = {'cycling': False, 'matches': [], 'index': 0,
                  'basename_len': 0, 'insert_len': 0, 'cursor_after': 0}
    kb = KeyBindings()

    @kb.add('tab')
    def _(event):
        buf = event.current_buffer
        
        # If we are currently cycling and cursor hasn't moved, cycle to next match
        if _tab_state['cycling'] and buf.cursor_position == _tab_state['cursor_after']:
            buf.delete_before_cursor(_tab_state['insert_len'])
            _tab_state['index'] = (_tab_state['index'] + 1) % len(_tab_state['matches'])
            name, is_dir = _tab_state['matches'][_tab_state['index']]
            suffix = name[_tab_state['basename_len']:] + ('/' if is_dir else '')
            buf.insert_text(suffix)
            _tab_state['insert_len'] = len(suffix)
            _tab_state['cursor_after'] = buf.cursor_position
            return
        
        # Fresh Tab press — reset cycling state
        _tab_state['cycling'] = False
        
        text = buf.text[:buf.cursor_position]
        result = _find_at_token(text)
        if result is None:
            return
        at_index, path_prefix = result
        matches = _list_matches(path_prefix)
        if not matches:
            return

        expanded = os.path.expanduser(path_prefix)
        basename = '' if os.path.isdir(expanded) else os.path.basename(expanded)

        if len(matches) == 1:
            # Single match: complete it inline
            name, is_dir = matches[0]
            suffix = name[len(basename):] + ('/' if is_dir else '')
            buf.insert_text(suffix)
        else:
            # Multiple matches: try common prefix first
            names = [m[0] for m in matches]
            lcp = _longest_common_prefix(names)
            if len(lcp) > len(basename):
                buf.insert_text(lcp[len(basename):])
            else:
                # No common prefix to add — start cycling through matches
                _tab_state['cycling'] = True
                _tab_state['matches'] = matches
                _tab_state['index'] = 0
                _tab_state['basename_len'] = len(basename)
                name, is_dir = matches[0]
                suffix = name[len(basename):] + ('/' if is_dir else '')
                buf.insert_text(suffix)
                _tab_state['insert_len'] = len(suffix)
                _tab_state['cursor_after'] = buf.cursor_position

    session = PromptSession(key_bindings=kb)
    current_mode = 'new'  # Track the current operational mode
    
    while True:
        try:
            user_input = session.prompt(HTML("\n<ansicyan><b>👤 Architect (type 'exit' to quit): </b></ansicyan>"))
            if user_input.lower() in ['exit', 'quit']:
                console.print("[bold green]Goodbye![/bold green]")
                break
                
            if not user_input.strip():
                continue
                
            with console.status("[bold green]🤖 LV Agent is thinking...[/bold green]", spinner="dots"):
                processed_input = process_at_references(user_input)
                
                # In Modify mode, track any @referenced files so Phase 4
                # can auto-resolve their paths without re-asking
                if current_mode == 'modify':
                    at_paths = set(re.findall(r'@([^\s]+)', user_input))
                    for at_path in at_paths:
                        expanded = os.path.expanduser(at_path)
                        if os.path.isfile(expanded):
                            abs_ref = os.path.abspath(expanded)
                            _investigated_files[os.path.basename(abs_ref)] = abs_ref
                
                response = agent.send_message(processed_input)
                
            console.print(Panel(Markdown(response), title="🤖 LV Agent", border_style="blue"))
            
            # Detect and track the operational mode
            detected_mode = detect_agent_mode(response)
            if detected_mode == 'modify':
                current_mode = 'modify'
                console.print("[dim]📋 Mode: Modify — The agent will investigate existing code before proposing changes.[/dim]")
            elif re.search(r'\[Phase:\s*1\]', response) and not re.search(r'\[Phase:\s*1\.5\]', response):
                # Reset mode when agent returns to Phase 1 (new cycle)
                current_mode = 'new'
                _investigated_files.clear()
            
            # Handle Phase 1.5 — Investigation Setup (Modify mode)
            if detect_investigation_phase(response):
                investigation_input = handle_investigation_phase(session)
                if investigation_input:
                    with console.status("[bold green]🤖 LV Agent is investigating...[/bold green]", spinner="dots"):
                        investigation_response = agent.send_message(investigation_input)
                    console.print(Panel(Markdown(investigation_response), title="🤖 LV Agent — Investigation", border_style="yellow"))
                    
                    # Auto-investigation loop: detect [Investigate: filename] requests
                    # and auto-feed files back to the agent
                    _run_auto_investigation(agent, investigation_response)
                    
                continue
            
            # Handle Phase 4 — File saving (both modes)
            # In Modify mode, the file merger will detect existing files
            # and offer merge/overwrite/skip options automatically
            process_and_save_files(response, agent)
            
        except KeyboardInterrupt:
            console.print("\n[dim]Exiting...[/dim]")
            break
        except Exception as e:
            console.print(f"\n[bold red]Error:[/bold red] {e}\n")

if __name__ == "__main__":
    main()


