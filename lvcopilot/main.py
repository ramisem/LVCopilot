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
from . import error_parser
from .db_connector import DatabaseConnector

console = Console()

# Track investigated file paths from Modify mode Phase 1.5
# Maps basename -> absolute path so Phase 4 can auto-resolve without re-asking
_investigated_files = {}

# Track extra search directories provided during interactive phase
_extra_search_roots = set()


def _resolve_investigated_file(filename):
    """Smart path resolution for Phase 4 file saving.

    Attempts to match the given filename against _investigated_files using:
      1. Exact basename match
      2. Case-insensitive basename match
      3. Dot-separated FQCN extraction (e.g. 'com.client.actions.MyAction' → 'MyAction.java')

    If no match is found in _investigated_files, performs a project-wide search
    using code_investigator.find_file_in_project and registers the result.

    Args:
        filename: The filename (or FQCN-style name) to resolve.

    Returns:
        str or None: Absolute path to the file, or None if not found.
    """
    if not filename:
        return None

    # 1. Exact match
    if filename in _investigated_files:
        return _investigated_files[filename]

    # 2. Case-insensitive match
    filename_lower = filename.lower()
    for tracked_name, tracked_path in _investigated_files.items():
        if tracked_name.lower() == filename_lower:
            return tracked_path

    # 3. Dot-separated FQCN extraction
    #    e.g. "com.client.actions.MyAction" → try "MyAction.java"
    if '.' in filename:
        parts = filename.rsplit('.', 1)
        # If the last part is an extension (e.g. "java", "py"), extract basename
        # e.g. "com.client.actions.MyAction.java" → basename = "MyAction", ext = "java"
        name_part = parts[0]
        ext_part = parts[1] if len(parts) > 1 else ''

        if ext_part and len(ext_part) <= 5:
            # Likely a real extension — extract basename from FQCN portion
            basename_from_fqcn = name_part.rsplit('.', 1)[-1]
            candidate = f"{basename_from_fqcn}.{ext_part}"
        else:
            # No extension — assume FQCN without ext, try common Java extension
            basename_from_fqcn = filename.rsplit('.', 1)[-1]
            candidate = f"{basename_from_fqcn}.java"

        if candidate in _investigated_files:
            return _investigated_files[candidate]
        # Case-insensitive fallback for the FQCN candidate
        candidate_lower = candidate.lower()
        for tracked_name, tracked_path in _investigated_files.items():
            if tracked_name.lower() == candidate_lower:
                return tracked_path

    # 4. Project-wide search fallback
    search_roots = [os.getcwd()]
    product_src_dir = os.environ.get("PRODUCT_SRC_DIR", "").strip()
    if product_src_dir and os.path.isdir(product_src_dir):
        search_roots.append(product_src_dir)
    for root in _extra_search_roots:
        if root not in search_roots:
            search_roots.append(root)

    for abs_path in _investigated_files.values():
        parent = os.path.dirname(abs_path)
        if parent not in search_roots:
            search_roots.append(parent)
        grandparent = os.path.dirname(parent)
        if grandparent and grandparent not in search_roots:
            search_roots.append(grandparent)

    found = code_investigator.find_file_in_project(filename, search_roots)
    if found:
        _investigated_files[filename] = found
        return found

    # Try the FQCN-derived candidate in the project as well
    if '.' in filename:
        if ext_part and len(ext_part) <= 5:
            basename_from_fqcn = name_part.rsplit('.', 1)[-1]
            candidate = f"{basename_from_fqcn}.{ext_part}"
        else:
            basename_from_fqcn = filename.rsplit('.', 1)[-1]
            candidate = f"{basename_from_fqcn}.java"

        found = code_investigator.find_file_in_project(candidate, search_roots)
        if found:
            _investigated_files[candidate] = found
            return found

    return None



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
        console.print("  - OpenCode Zen: [cyan]openai/minimax-m2.5-free[/cyan] + API base [cyan]https://opencode.ai/zen/v1[/cyan]")
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
            
            # Ask for optional LV_SUMMARY_INTERVAL
            while True:
                lv_summary_interval = Prompt.ask(
                    "Please enter the summarization interval (number of user turns between summaries, optional, press Enter to skip)"
                ).strip()
                if not lv_summary_interval:
                    lv_summary_interval = None
                    break
                try:
                    int(lv_summary_interval)
                    break
                except ValueError:
                    console.print("[bold red]Validation Error:[/bold red] Please enter a valid integer or press Enter to skip.")
            
            # Ask for optional Preference Learning LLM config
            console.print("\n[bold cyan]Preference Learning Configuration (optional)[/bold cyan]")
            console.print("[dim]The agent can learn your design/coding preferences from your feedback.[/dim]")
            console.print("[dim]You can use a separate (cheaper/faster) model for preference extraction.[/dim]")
            
            lv_pref_model = Prompt.ask(
                "Preference extraction model (optional, press Enter to use the main model)"
            ).strip() or None
            
            lv_pref_api_key = None
            lv_pref_api_base = None
            if lv_pref_model:
                lv_pref_api_key = Prompt.ask(
                    "Preference model API Key (press Enter to use the main API key)",
                    password=True
                ).strip() or None
                lv_pref_api_base = Prompt.ask(
                    "Preference model API Base URL (press Enter to use the main API base)"
                ).strip() or None
            
            # Ask for optional LV_MAX_PREFERENCES
            lv_max_preferences = None
            while True:
                lv_max_prefs_input = Prompt.ask(
                    "Max preferences to store (default: 30, press Enter to skip)"
                ).strip()
                if not lv_max_prefs_input:
                    break
                try:
                    int(lv_max_prefs_input)
                    lv_max_preferences = lv_max_prefs_input
                    break
                except ValueError:
                    console.print("[bold red]Validation Error:[/bold red] Please enter a valid integer or press Enter to skip.")
            
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
                if lv_summary_interval:
                    f.write(f"LV_SUMMARY_INTERVAL={lv_summary_interval}\n")
                if lv_pref_model:
                    f.write(f"LV_PREF_MODEL={lv_pref_model}\n")
                if lv_pref_api_key:
                    f.write(f"LV_PREF_API_KEY={lv_pref_api_key}\n")
                if lv_pref_api_base:
                    f.write(f"LV_PREF_API_BASE={lv_pref_api_base}\n")
                if lv_max_preferences:
                    f.write(f"LV_MAX_PREFERENCES={lv_max_preferences}\n")
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
        if lv_summary_interval:
            os.environ["LV_SUMMARY_INTERVAL"] = lv_summary_interval
        if lv_pref_model:
            os.environ["LV_PREF_MODEL"] = lv_pref_model
        if lv_pref_api_key:
            os.environ["LV_PREF_API_KEY"] = lv_pref_api_key
        if lv_pref_api_base:
            os.environ["LV_PREF_API_BASE"] = lv_pref_api_base
        if lv_max_preferences:
            os.environ["LV_MAX_PREFERENCES"] = lv_max_preferences

    # ── Database Configuration (optional) ──
    # Only prompt if no DB is already configured
    db1_type = os.environ.get("DB1_TYPE", "").strip()
    if not db1_type:
        console.print("\n[bold cyan]Database Configuration (optional)[/bold cyan]")
        console.print("[dim]Configure database access so the agent can query tables during development.[/dim]")
        configure_db = Confirm.ask("Do you want to configure database access?", default=False)
        
        if configure_db:
            _configure_database(env_path, "DB1", "Primary")
            
            add_second = Confirm.ask("\nDo you want to configure a second database?", default=False)
            if add_second:
                _configure_database(env_path, "DB2", "Secondary")
            
            # Shared settings
            db_max_rows = Prompt.ask(
                "Max rows per query result (default: 50)"
            ).strip()
            if db_max_rows:
                try:
                    int(db_max_rows)
                    os.environ["DB_MAX_ROWS"] = db_max_rows
                    try:
                        with open(env_path, 'a') as f:
                            f.write(f"DB_MAX_ROWS={db_max_rows}\n")
                    except Exception:
                        pass
                except ValueError:
                    console.print("[bold yellow]Invalid number — using default (50).[/bold yellow]")
    else:
        console.print(f"[dim]Database DB1 configured: {db1_type}[/dim]")
        db2_type = os.environ.get("DB2_TYPE", "").strip()
        if db2_type:
            console.print(f"[dim]Database DB2 configured: {db2_type}[/dim]")

    # ── Product Source Configuration (optional) ──
    # Only prompt if no PRODUCT_SRC_DIR is already configured
    product_src_dir = os.environ.get("PRODUCT_SRC_DIR", "").strip()
    if not product_src_dir:
        console.print("\n[bold cyan]Product Source Configuration (optional)[/bold cyan]")
        console.print("[dim]Provide the path to sapphire_jar_decompiled to enable automated product-level log parsing.[/dim]")
        
        # Check if a folder named sapphire_jar_decompiled exists in the current directory
        local_dir = os.path.join(os.getcwd(), 'sapphire_jar_decompiled')
        default_prompt_val = local_dir if os.path.isdir(local_dir) else ""
        
        while True:
            prompt_str = "Please enter the absolute path to 'sapphire_jar_decompiled' (optional, press Enter to skip)"
            user_path = Prompt.ask(prompt_str, default=default_prompt_val).strip()
            if not user_path:
                break
            
            expanded_path = os.path.abspath(os.path.expanduser(user_path))
            if os.path.isdir(expanded_path):
                os.environ["PRODUCT_SRC_DIR"] = expanded_path
                try:
                    with open(env_path, 'a') as f:
                        f.write(f"\nPRODUCT_SRC_DIR={expanded_path}\n")
                    console.print(f"[green]Product Source directory configured successfully: {expanded_path}[/green]")
                except Exception as e:
                    console.print(f"[bold yellow]Warning: Could not save configuration to {env_path}: {e}[/bold yellow]")
                break
            else:
                console.print(f"[bold red]Validation Error:[/bold red] '{user_path}' is not a valid directory on disk. Please enter a valid directory path or press Enter to skip.")
    else:
        console.print(f"[dim]Product Source directory configured: {product_src_dir}[/dim]")


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
    """Detect if agent is in Modify, Debug, or New mode based on response markers.
    
    Returns:
        str: 'modify', 'debug', or 'new'
    """
    if re.search(r'\[Mode:\s*Modify\]', response, re.IGNORECASE):
        return 'modify'
    if re.search(r'\[Mode:\s*Debug\]', response, re.IGNORECASE):
        return 'debug'
    return 'new'


def detect_investigation_phase(response):
    """Check if the agent is in Phase 1.5 (investigation setup).
    
    Returns:
        bool: True if [Phase: 1.5] is found in the response.
    """
    return bool(re.search(r'\[Phase:\s*1\.5\]', response, re.IGNORECASE))


def handle_investigation_phase(session, agent_response=None):
    """Prompt the architect or auto-resolve investigation starting points (supports directories).
    
    Checks if agent requested files using [Investigate: filename], tries to auto-resolve them
    within project search roots + PRODUCT_SRC_DIR + extra search roots. Prompts with a numbered
    selection menu if multiple matches are found. If files are not resolved or manual inputs
    are wanted, asks for file/directory paths.
    
    Args:
        session: The PromptSession for input.
        agent_response: The agent's raw response to parse for requested files.
    
    Returns:
        str: The user's text combined with investigation context for all files,
             ready to send to the agent.
    """
    console.print("\n[bold cyan]📂 Investigation Mode[/bold cyan]")
    console.print("[dim]The agent needs to understand the existing code before proposing changes.[/dim]")
    
    from prompt_toolkit.formatted_text import HTML as _HTML
    
    all_contexts = []
    file_descriptions = []
    file_count = 0
    
    # ── Phase 1: Try auto-resolution of [Investigate: ...] markers from agent_response ──
    requested_files = []
    if agent_response:
        requested_files = _parse_investigate_markers(agent_response)
        
    if requested_files:
        console.print("\n[bold cyan]🔎 Auto-Resolving Agent's Investigation Requests...[/bold cyan]")
        
        # Build search roots
        search_roots = set()
        search_roots.add(os.getcwd())
        product_src_dir = os.environ.get("PRODUCT_SRC_DIR", "").strip()
        if product_src_dir and os.path.isdir(product_src_dir):
            search_roots.add(product_src_dir)
        for root in _extra_search_roots:
            search_roots.add(root)
            
        for fname in requested_files:
            fname = fname.strip()
            # Search using find_all_files_in_project
            matches = code_investigator.find_all_files_in_project(fname, list(search_roots))
            
            if not matches:
                # If fname has directories in it, or is a relative path, see if it is a real file directly on disk
                candidate_path = os.path.abspath(os.path.expanduser(fname))
                if os.path.isfile(candidate_path):
                    matches = [candidate_path]
            
            if not matches:
                console.print(f"  ❌ [yellow]Could not find '{fname}' in project/product search roots.[/yellow]")
                continue
                
            selected_path = None
            if len(matches) == 1:
                selected_path = matches[0]
                console.print(f"  ✅ [green]Found exactly one match for '{fname}':[/green] [dim]{selected_path}[/dim]")
            else:
                # Multiple matches! Present interactive selection menu
                console.print(f"\n  [bold yellow]⚠️ Multiple matches found for '{fname}':[/bold yellow]")
                for idx, path in enumerate(matches, 1):
                    console.print(f"    [{idx}] {path}")
                choices = [str(i) for i in range(1, len(matches) + 1)]
                choice = Prompt.ask("  Select which file to investigate", choices=choices, default="1")
                selected_path = matches[int(choice) - 1]
                
            if selected_path:
                console.print(f"  📖 [dim]Reading: {selected_path}[/dim]")
                context = code_investigator.build_investigation_context(selected_path)
                all_contexts.append(context)
                
                basename = os.path.basename(selected_path)
                _investigated_files[basename] = selected_path
                _investigated_files[fname] = selected_path
                
                file_descriptions.append(f"{fname} (auto-resolved → {selected_path})")
                file_count += 1
                console.print(f"  ✅ [green]File '{fname}' loaded successfully[/green]")

    # ── Phase 2: Ask if user wants to provide more files or manual inputs ──
    prompt_manual = True
    if file_count > 0:
        prompt_manual = Confirm.ask("\nDo you want to provide any additional file/directory paths to investigate?", default=False)
        
    if prompt_manual:
        console.print("[dim]Provide file/directory path(s) to investigate (you can use @path for autocomplete).[/dim]\n")
        
        while True:
            label = "Primary" if file_count == 0 else "Additional"
            file_input = session.prompt(_HTML(f"\n<ansicyan><b>📄 {label} file/directory path to investigate: </b></ansicyan>"))
            file_input = file_input.strip()
            
            if not file_input:
                if file_count == 0:
                    console.print("[bold yellow]No file/directory provided. The agent will continue without investigation context.[/bold yellow]")
                    return ""
                else:
                    break
            
            # Extract the file path — handle @prefix if used
            file_path = file_input.lstrip('@').strip()
            abs_path = os.path.abspath(os.path.expanduser(file_path))
            
            if os.path.isdir(abs_path):
                console.print(f"  📁 [green]Added directory '{file_path}' to investigation search roots.[/green]")
                _extra_search_roots.add(abs_path)
                
                # Check if we can now find any pending requested files in this new directory
                if requested_files:
                    for fname in requested_files:
                        if fname in _investigated_files:
                            continue
                        # Search inside this new directory
                        matches = code_investigator.find_all_files_in_project(fname, [abs_path])
                        if matches:
                            selected_path = None
                            if len(matches) == 1:
                                selected_path = matches[0]
                                console.print(f"  ✅ [green]Found match for '{fname}' in new directory:[/green] [dim]{selected_path}[/dim]")
                            else:
                                console.print(f"\n  [bold yellow]⚠️ Multiple matches found for '{fname}' in new directory:[/bold yellow]")
                                for idx, path in enumerate(matches, 1):
                                    console.print(f"        [{idx}] {path}")
                                choices = [str(i) for i in range(1, len(matches) + 1)]
                                choice = Prompt.ask("      Select which file to investigate", choices=choices, default="1")
                                selected_path = matches[int(choice) - 1]
                                
                            if selected_path:
                                console.print(f"  📖 [dim]Reading: {selected_path}[/dim]")
                                context = code_investigator.build_investigation_context(selected_path)
                                all_contexts.append(context)
                                
                                basename = os.path.basename(selected_path)
                                _investigated_files[basename] = selected_path
                                _investigated_files[fname] = selected_path
                                
                                file_descriptions.append(f"{fname} (found in {file_path} → {selected_path})")
                                file_count += 1
                                console.print(f"  ✅ [green]File '{fname}' loaded successfully[/green]")
            elif not os.path.isfile(abs_path):
                console.print(f"[bold yellow]Warning: File or Directory '{file_path}' not found. Skipping.[/bold yellow]")
            else:
                # Get optional method/code hint
                method_hint = session.prompt(_HTML("\n<ansicyan><b>🔍 Starting method/code (optional, press Enter to skip): </b></ansicyan>"))
                method_hint = method_hint.strip() if method_hint else None
                
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
            add_more = Confirm.ask("\nDo you have another file or directory to investigate?", default=False)
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
    product_src_dir = os.environ.get("PRODUCT_SRC_DIR", "").strip()
    if product_src_dir and os.path.isdir(product_src_dir):
        search_roots.add(product_src_dir)
    for root in _extra_search_roots:
        search_roots.add(root)

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
            return last_response
        
        # Deduplicate and filter out already-investigated files
        unique_requests = []
        seen = set()
        for fname in requested_files:
            fname = fname.strip()
            if fname not in seen and fname not in _investigated_files:
                unique_requests.append(fname)
                seen.add(fname)
        
        if not unique_requests:
            return last_response
        
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
                last_response, stats = agent.send_message(not_found_msg)
            console.print(Panel(Markdown(last_response), title=f"🤖 LV Agent — Investigation (Round {round_num})", border_style="yellow"))
            _display_token_stats(agent, stats)
            continue
        
        # Build the auto-investigation message
        auto_msg = f"[System: Auto-Investigation Round {round_num}]\n"
        auto_msg += f"Found {len(found_contexts)} of {len(unique_requests)} requested file(s).\n"
        if not_found:
            auto_msg += f"Could NOT find: {', '.join(not_found)}\n"
        auto_msg += "\n" + "\n\n".join(found_contexts)
        
        with console.status("[bold green]🤖 LV Agent continuing investigation...[/bold green]", spinner="dots"):
            last_response, stats = agent.send_message(auto_msg)
        console.print(Panel(Markdown(last_response), title=f"🤖 LV Agent — Investigation (Round {round_num})", border_style="yellow"))
        _display_token_stats(agent, stats)
    
    if _parse_investigate_markers(last_response):
        console.print(f"[bold yellow]⚠️  Investigation round limit ({MAX_AUTO_INVESTIGATION_ROUNDS}) reached. The agent may continue with partial context.[/bold yellow]")
        
    return last_response


def process_and_save_files(response, agent, session):
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
        
        # Check if this file was previously investigated (Modify/Debug mode)
        # Use smart resolution: exact, case-insensitive, FQCN, and project search
        resolved_path = _resolve_investigated_file(filename) if filename else None
        
        if resolved_path:
            abs_path = resolved_path
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
            from prompt_toolkit.formatted_text import HTML as _HTML
            if filename:
                location = session.prompt(_HTML(f"<ansicyan><b>Enter the directory path to save '{filename}' to (e.g., ./src/actions/): </b></ansicyan>")).strip()
                if location:
                    location = location.lstrip('@').strip()
                
                if not location:
                    console.print("[bold red]Validation Error:[/bold red] Directory path cannot be empty.")
                    continue
                
                expanded_location = os.path.expanduser(location)
                final_path = os.path.join(expanded_location, filename)
            else:
                filepath_input = session.prompt(_HTML("<ansicyan><b>Enter the full file path (including file name) to save to: </b></ansicyan>")).strip()
                if filepath_input:
                    filepath_input = filepath_input.lstrip('@').strip()
                
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


def _display_token_stats(agent, turn_stats):
    """Display token usage stats after an agent response.
    
    Args:
        agent: The LVDeveloperAgent instance.
        turn_stats: Token stats dict from send_message(), or None.
    """
    if not turn_stats:
        return
    prompt_k = turn_stats['prompt_tokens'] / 1000
    comp_k = turn_stats['completion_tokens'] / 1000
    total_session = (agent.session_stats['total_prompt']
                     + agent.session_stats['total_completion'])
    total_k = total_session / 1000
    turns = agent.session_stats['turns']
    console.print(
        f"[dim]  📊 Turn: ↑{prompt_k:.1f}K ↓{comp_k:.1f}K tokens | "
        f"Session: {total_k:.1f}K total ({turns} turns)[/dim]"
    )


def _display_session_summary(agent):
    """Display a final session summary on exit.
    
    Args:
        agent: The LVDeveloperAgent instance.
    """
    total = (agent.session_stats['total_prompt']
             + agent.session_stats['total_completion'])
    if total > 0:
        turns = agent.session_stats['turns']
        console.print(
            f"\n[bold cyan]📊 Session Summary[/bold cyan]: "
            f"{total/1000:.1f}K tokens across {turns} turns "
            f"(↑{agent.session_stats['total_prompt']/1000:.1f}K prompt, "
            f"↓{agent.session_stats['total_completion']/1000:.1f}K completion)"
        )


def _handle_preferences_command(agent, user_input):
    """Handle the /preferences command for managing learned preferences.

    Subcommands:
        /preferences         — List all stored preferences
        /preferences delete N — Delete preference at index N
        /preferences clear   — Clear all preferences

    Args:
        agent: The LVDeveloperAgent instance.
        user_input: The full user input string.
    """
    parts = user_input.split()
    pref_mgr = agent.preference_manager

    if len(parts) == 1:
        # /preferences — list all
        prefs = pref_mgr.list_preferences()
        if not prefs:
            console.print("\n[dim]No architect preferences stored yet. Preferences are learned automatically from your feedback during Phase 2 and Phase 3.[/dim]\n")
            return

        console.print(f"\n[bold cyan]🧠 Architect Preferences ({len(prefs)}/{pref_mgr.max_preferences})[/bold cyan]\n")
        for i, pref in enumerate(prefs, 1):
            category = pref.get("category", "general").upper().replace("_", " ")
            rule = pref.get("rule", "")
            phase = pref.get("source_phase", "?")
            created = pref.get("created_at", "")[:10]
            console.print(f"  [bold]{i}.[/bold] [{category}] {rule}")
            console.print(f"     [dim]Phase {phase} • {created}[/dim]")
        console.print(f"\n[dim]Use '/preferences delete N' to remove or '/preferences clear' to clear all.[/dim]\n")
        return

    subcommand = parts[1].lower()

    if subcommand == "delete" and len(parts) >= 3:
        try:
            index = int(parts[2])
            removed = pref_mgr.remove_preference(index)
            if removed:
                console.print(f"\n[green]✅ Removed preference {index}: {removed.get('rule', '')}[/green]\n")
            else:
                console.print(f"\n[bold red]Invalid index: {index}[/bold red]\n")
        except ValueError:
            console.print("\n[bold red]Usage: /preferences delete <number>[/bold red]\n")
        return

    if subcommand == "clear":
        count = len(pref_mgr.list_preferences())
        if count == 0:
            console.print("\n[dim]No preferences to clear.[/dim]\n")
            return
        confirm = Confirm.ask(f"Clear all {count} preferences?")
        if confirm:
            pref_mgr.clear_all()
            console.print("\n[green]✅ All preferences cleared.[/green]\n")
        else:
            console.print("\n[dim]Cancelled.[/dim]\n")
        return

    console.print("\n[bold yellow]Usage:[/bold yellow]")
    console.print("  /preferences          — List all stored preferences")
    console.print("  /preferences delete N — Delete preference at index N")
    console.print("  /preferences clear    — Clear all preferences\n")


def _display_preference_notifications(agent):
    """Display any pending background preference extraction notifications.

    Called after each agent response to show the architect what preferences
    were learned from their feedback.

    Args:
        agent: The LVDeveloperAgent instance.
    """
    notifications = agent.preference_manager.get_pending_notifications()
    for notification in notifications:
        console.print(f"[bold magenta]{notification}[/bold magenta]")


def _configure_database(env_path, prefix, label):
    """Prompt the user for database configuration and save to .lvcopilotenv.

    Args:
        env_path: Path to the .lvcopilotenv file.
        prefix: Environment variable prefix (e.g., 'DB1', 'DB2').
        label: Human-readable label (e.g., 'Primary', 'Secondary').
    """
    console.print(f"\n[bold cyan]📦 {label} Database ({prefix})[/bold cyan]")

    db_type = Prompt.ask(
        f"  Database type",
        choices=["oracle", "sqlserver"]
    ).strip().lower()

    db_host = Prompt.ask(f"  Host").strip()

    default_port = "1521" if db_type == "oracle" else "1433"
    db_port = Prompt.ask(f"  Port", default=default_port).strip()

    name_label = "Service name/SID" if db_type == "oracle" else "Database name"
    db_name = Prompt.ask(f"  {name_label}").strip()

    db_user = Prompt.ask(f"  Username").strip()
    db_password = Prompt.ask(f"  Password", password=True).strip()

    # Set environment variables for the current run
    os.environ[f"{prefix}_TYPE"] = db_type
    os.environ[f"{prefix}_HOST"] = db_host
    os.environ[f"{prefix}_PORT"] = db_port
    os.environ[f"{prefix}_NAME"] = db_name
    os.environ[f"{prefix}_USER"] = db_user
    os.environ[f"{prefix}_PASSWORD"] = db_password

    # Save to .lvcopilotenv
    try:
        with open(env_path, 'a') as f:
            f.write(f"\n{prefix}_TYPE={db_type}\n")
            f.write(f"{prefix}_HOST={db_host}\n")
            f.write(f"{prefix}_PORT={db_port}\n")
            f.write(f"{prefix}_NAME={db_name}\n")
            f.write(f"{prefix}_USER={db_user}\n")
            f.write(f"{prefix}_PASSWORD={db_password}\n")
        console.print(f"  [green]✅ {label} database configuration saved.[/green]")
    except Exception as e:
        console.print(f"  [bold yellow]Warning: Could not save to {env_path}: {e}[/bold yellow]")

    # Test connection
    console.print(f"  🔄 Testing connection...")
    connector = DatabaseConnector(prefix)
    result = connector.test_connection()
    console.print(f"  {result}")


def _handle_db_command(agent, user_input):
    """Handle the /db command for database management.

    Subcommands:
        /db status     — Show connection status for all configured databases
        /db test       — Test all configured database connections
        /db disconnect — Close all active database connections
        /db config     — Reconfigure database settings

    Args:
        agent: The LVDeveloperAgent instance.
        user_input: The full user input string.
    """
    parts = user_input.split()
    db_mgr = agent.db_manager

    if len(parts) == 1:
        # /db — show help
        console.print("\n[bold cyan]🗄️  Database Commands[/bold cyan]")
        console.print("  /db status     — Show connection status")
        console.print("  /db test       — Test database connections")
        console.print("  /db disconnect — Close all connections")
        console.print("  /db config     — Reconfigure database settings\n")
        return

    subcommand = parts[1].lower()

    if subcommand == "status":
        status = db_mgr.get_status()
        console.print(f"\n{status}\n")
        return

    if subcommand == "test":
        console.print("\n[bold cyan]🔄 Testing database connections...[/bold cyan]")
        results = db_mgr.test_connections()
        console.print(f"\n{results}\n")
        return

    if subcommand == "disconnect":
        result = db_mgr.close_all()
        console.print(f"\n{result}\n")
        return

    if subcommand == "config":
        env_path = os.path.join(os.getcwd(), '.lvcopilotenv')
        console.print("\n[bold cyan]📦 Database Reconfiguration[/bold cyan]")
        _configure_database(env_path, "DB1", "Primary")
        add_second = Confirm.ask("\nConfigure a second database?", default=False)
        if add_second:
            _configure_database(env_path, "DB2", "Secondary")
        # Reinitialize the db_manager with new env vars
        agent.db_manager.close_all()
        from .db_connector import DatabaseManager
        agent.db_manager = DatabaseManager()
        console.print("\n[green]✅ Database configuration updated.[/green]\n")
        return

    console.print(f"\n[bold yellow]Unknown /db subcommand: {subcommand}[/bold yellow]")
    console.print("  Use: /db status | /db test | /db disconnect | /db config\n")


def main():
    console.print(Panel.fit("[bold blue]Initializing Autonomous LV Developer Agent CLI...[/bold blue]"))
    
    configure_llm()
    
    console.print("[dim]Loading Knowledge Base and configuring LLM...[/dim]")
    try:
        agent = LVDeveloperAgent()
        initial_greeting, initial_stats = agent.start()
    except ValueError as ve:
        console.print(f"[bold red]Configuration Error:[/bold red] {ve}")
        sys.exit(1)
    except Exception as e:
        console.print(f"[bold red]Failed to start agent. Error:[/bold red] {e}")
        sys.exit(1)
        
    console.print(Panel(Markdown(initial_greeting), title="🤖 LV Agent", border_style="blue"))
    _display_token_stats(agent, initial_stats)
    
    # Show loaded architect preferences count
    pref_count = len(agent.preference_manager.list_preferences())
    if pref_count > 0:
        console.print(f"[dim]🧠 {pref_count} architect preference(s) loaded from previous sessions[/dim]")
    
    # Show database status
    if agent.db_manager.has_any_configured():
        console.print(f"[dim]🗄️  Database access available (use /db status to check)[/dim]")    
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
                _display_session_summary(agent)
                console.print("[bold green]Goodbye![/bold green]")
                break
                
            if user_input.strip().lower() in ['clear', '/clear']:
                console.clear()
                console.print("\n[bold green]🔄 Session cleared successfully! Starting a fresh session...[/bold green]")
                pref_count = len(agent.preference_manager.list_preferences())
                if pref_count > 0:
                    console.print(f"[dim]💡 {pref_count} architect preference(s) retained (use /preferences to manage)[/dim]")
                console.print()
                _investigated_files.clear()
                current_mode = 'new'
                agent.reset()
                
                initial_greeting, initial_stats = agent.start()
                console.print(Panel(Markdown(initial_greeting), title="🤖 LV Agent", border_style="blue"))
                _display_token_stats(agent, initial_stats)
                continue
            
            if user_input.strip().lower().startswith('/preferences'):
                _handle_preferences_command(agent, user_input.strip())
                continue
            
            if user_input.strip().lower().startswith('/db'):
                _handle_db_command(agent, user_input.strip())
                continue
                
            if not user_input.strip():
                continue
                
            with console.status("[bold green]🤖 LV Agent is thinking...[/bold green]", spinner="dots"):
                processed_input = process_at_references(user_input)
                
                # In Modify/Debug mode, track any @referenced files so Phase 4
                # can auto-resolve their paths without re-asking
                if current_mode in ('modify', 'debug'):
                    at_paths = set(re.findall(r'@([^\s]+)', user_input))
                    for at_path in at_paths:
                        expanded = os.path.expanduser(at_path)
                        if os.path.isfile(expanded):
                            abs_ref = os.path.abspath(expanded)
                            _investigated_files[os.path.basename(abs_ref)] = abs_ref
                
                # ── Auto-Injection of Full Client Files on Transition to Phase 3 ──
                if current_mode == 'debug' and _investigated_files:
                    # Check if the previous assistant message was Phase 2
                    last_msg = ""
                    for msg in reversed(agent.conversation.messages):
                        role = msg.get("role") if isinstance(msg, dict) else getattr(msg, "role", "")
                        if role == "assistant":
                            last_msg = msg.get("content") if isinstance(msg, dict) else getattr(msg, "content", "")
                            break
                    
                    if "[Phase: 2]" in last_msg:
                        # Check if user input contains an approval/proceed keyword
                        user_input_lower = user_input.lower().strip()
                        approval_keywords = {
                            "approve", "approved", "looks good", "proceed", "go ahead", "yes", 
                            "ok", "okay", "fix it", "make the changes", "write the code", "do it", 
                            "looks fine", "looks correct", "apply", "y", "sure", "go on"
                        }
                        if any(re.search(r'\b' + re.escape(kw) + r'\b', user_input_lower) for kw in approval_keywords):
                            # Find all files whose full content is not yet in active history
                            history_text = ""
                            for msg in agent.conversation.messages:
                                content = msg.get("content") if isinstance(msg, dict) else getattr(msg, "content", "")
                                history_text += content or ""
                            
                            full_file_contexts = []
                            for basename, abs_path in _investigated_files.items():
                                # Only pull the full file if it was identified for potential modification in the Phase 2 plan
                                name_without_ext, _ = os.path.splitext(basename)
                                if basename.lower() not in last_msg.lower() and name_without_ext.lower() not in last_msg.lower():
                                    continue

                                marker = f"--- Full Content of {basename} (for modification) ---"
                                if marker not in history_text:
                                    _, numbered = code_investigator.read_file_content(abs_path)
                                    if numbered:
                                        full_file_contexts.append(
                                            f"\n\n--- Full Content of {basename} (for modification) ---\n"
                                            f"{numbered}\n"
                                            f"--- End of {basename} ---"
                                        )
                            
                            if full_file_contexts:
                                console.print("\n[bold magenta]📦 Plan approved — injecting full client file content for Phase 3 code generation...[/bold magenta]")
                                processed_input += "\n\n[System Context — Full Client Source Files for Phase 3 Code Generation]" + "".join(full_file_contexts)
                
                # ── Error Log Detection & Auto-Investigation ──
                # Scan user input for stack traces, SQL queries, and action calls.
                # If detected, automatically locate source files and inject
                # surrounding code context into the agent's prompt.
                search_roots = [os.getcwd()]
                for abs_path in _investigated_files.values():
                    search_roots.append(os.path.dirname(abs_path))
                
                # Resolve product source directory:
                # 1. Check if configured in environment variable PRODUCT_SRC_DIR
                # 2. Check if a local 'sapphire_jar_decompiled' folder exists in the current working directory
                # 3. Fall back to the packaged/base directory location
                product_src_dir = os.environ.get('PRODUCT_SRC_DIR')
                if not product_src_dir or not os.path.isdir(product_src_dir):
                    local_path = os.path.join(os.getcwd(), 'sapphire_jar_decompiled')
                    if os.path.isdir(local_path):
                        product_src_dir = local_path
                    else:
                        product_src_dir = os.path.join(
                            agent._get_base_dir(), 'sapphire_jar_decompiled'
                        )

                
                error_result = error_parser.parse_error_log(
                    processed_input, search_roots, product_src_dir
                )
                
                if error_result['detected']:
                    console.print("\n[bold magenta]🔍 Log pattern detected — auto-investigating source files...[/bold magenta]")
                    # Register client files for Phase 4 auto-resolution
                    for basename, abs_path in error_result['client_files'].items():
                        _investigated_files[basename] = abs_path
                        console.print(f"  📄 [green]Found client file: {abs_path}[/green]")
                    # Append the investigation context to the user input
                    processed_input += error_result['context']

                    # ── Auto Schema Injection for SQL Tables ──
                    # If table names are extracted from SQL, automatically describe them and inject schema
                    if error_result.get('sql_tables') and agent.db_manager.has_any_configured():
                        db_key = 'db1' if agent.db_manager.db1.is_configured() else 'db2'
                        for table in error_result['sql_tables']:
                            try:
                                schema = agent.db_manager.get_connector(db_key).describe_table(table)
                                if schema and 'Error' not in schema and 'not found or could not be described' not in schema:
                                    processed_input += f"\n[Auto-Injected Schema for {table}]\n{schema}\n"
                                    console.print(f"  🗃️  [green]Schema injected: {table}[/green]")
                            except Exception:
                                pass
                
                response, turn_stats = agent.send_message(processed_input)
                
            console.print(Panel(Markdown(response), title="🤖 LV Agent", border_style="blue"))
            _display_token_stats(agent, turn_stats)
            
            # Display any background preference extraction notifications
            _display_preference_notifications(agent)
            
            # Detect and track the operational mode
            detected_mode = detect_agent_mode(response)
            if detected_mode == 'modify':
                current_mode = 'modify'
                console.print("[dim]📋 Mode: Modify — The agent will investigate existing code before proposing changes.[/dim]")
            elif detected_mode == 'debug':
                current_mode = 'debug'
                console.print("[dim]🐛 Mode: Debug — The agent is diagnosing the error and will propose a fix.[/dim]")
            elif re.search(r'\[Phase:\s*1\]', response) and not re.search(r'\[Phase:\s*1\.5\]', response):
                # Reset mode when agent returns to Phase 1 (new cycle)
                current_mode = 'new'
                _investigated_files.clear()
                _extra_search_roots.clear()
            
            # Handle Phase 1.5 — Investigation Setup (Modify mode)
            if detect_investigation_phase(response):
                investigation_input = handle_investigation_phase(session, response)
                if investigation_input:
                    with console.status("[bold green]🤖 LV Agent is investigating...[/bold green]", spinner="dots"):
                        investigation_response, inv_stats = agent.send_message(investigation_input)
                    console.print(Panel(Markdown(investigation_response), title="🤖 LV Agent — Investigation", border_style="yellow"))
                    _display_token_stats(agent, inv_stats)
                    
                    # Auto-investigation loop: detect [Investigate: filename] requests
                    # and auto-feed files back to the agent
                    response = _run_auto_investigation(agent, investigation_response)
                    
                continue
            
            # Auto-investigation loop: if the response contains [Investigate: filename] markers, auto-feed files back
            if _parse_investigate_markers(response):
                response = _run_auto_investigation(agent, response)
            
            # Handle Phase 4 — File saving (both modes)
            # In Modify mode, the file merger will detect existing files
            # and offer merge/overwrite/skip options automatically
            process_and_save_files(response, agent, session)
            
        except KeyboardInterrupt:
            _display_session_summary(agent)
            console.print("\n[dim]Exiting...[/dim]")
            break
        except Exception as e:
            console.print(f"\n[bold red]Error:[/bold red] {e}\n")

if __name__ == "__main__":
    main()


