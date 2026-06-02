"""Code investigation utilities for Modify mode.

Provides helpers to read files, extract methods, and build rich context
strings that are injected into the agent's conversation for code investigation.
"""

import os
import re


# Directories to skip during project file search
_SKIP_DIRS = {'.git', '.svn', 'node_modules', '__pycache__', '.venv', 'venv',
              'target', 'build', 'dist', 'out', '.idea', '.vscode', '.settings',
              'bin', '.gradle', '.mvn'}


def find_file_in_project(filename, search_roots):
    """Search for a file by name within project directory trees.

    Walks the directory trees starting from each search root, skipping
    common non-project directories. Returns the first match found.

    Args:
        filename: The basename of the file to find (e.g., 'DataHelper.java').
        search_roots: List of directory paths to search from.

    Returns:
        str or None: Absolute path to the found file, or None if not found.
    """
    target = filename.strip().replace('\\', '/').replace('/', os.sep)
    for root_dir in search_roots:
        if not os.path.isdir(root_dir):
            continue
        candidate = os.path.abspath(os.path.join(root_dir, target))
        if os.path.isfile(candidate):
            return candidate

    basename = os.path.basename(target)
    for root_dir in search_roots:
        if not os.path.isdir(root_dir):
            continue
        for dirpath, dirnames, filenames in os.walk(root_dir):
            # Skip non-project directories in-place
            dirnames[:] = [d for d in dirnames if d not in _SKIP_DIRS]
            if basename in filenames:
                full_path = os.path.abspath(os.path.join(dirpath, basename))
                norm_full = os.path.normpath(full_path)
                norm_target = os.path.normpath(target)
                
                full_parts = [p for p in norm_full.split(os.sep) if p]
                target_parts = [p for p in norm_target.split(os.sep) if p]
                
                if len(full_parts) >= len(target_parts):
                    if full_parts[-len(target_parts):] == target_parts:
                        return norm_full
    return None


def find_all_files_in_project(filename, search_roots):
    """Search for all matching files by name within project directory trees.

    Walks the directory trees starting from each search root, skipping
    common non-project directories. Returns a list of absolute paths of all matches.

    Args:
        filename: The basename of the file to find (e.g., 'DataHelper.java').
        search_roots: List of directory paths to search from.

    Returns:
        list[str]: Absolute paths to all found files.
    """
    target = filename.strip().replace('\\', '/').replace('/', os.sep)
    matches = []
    seen = set()
    for root_dir in search_roots:
        if not os.path.isdir(root_dir):
            continue
        candidate = os.path.abspath(os.path.join(root_dir, target))
        if os.path.isfile(candidate) and candidate not in seen:
            matches.append(candidate)
            seen.add(candidate)

    basename = os.path.basename(target)
    for root_dir in search_roots:
        if not os.path.isdir(root_dir):
            continue
        for dirpath, dirnames, filenames in os.walk(root_dir):
            # Skip non-project directories in-place
            dirnames[:] = [d for d in dirnames if d not in _SKIP_DIRS]
            if basename in filenames:
                full_path = os.path.abspath(os.path.join(dirpath, basename))
                norm_full = os.path.normpath(full_path)
                norm_target = os.path.normpath(target)
                
                full_parts = [p for p in norm_full.split(os.sep) if p]
                target_parts = [p for p in norm_target.split(os.sep) if p]
                
                if len(full_parts) >= len(target_parts):
                    if full_parts[-len(target_parts):] == target_parts:
                        if norm_full not in seen:
                            matches.append(norm_full)
                            seen.add(norm_full)
    return matches


def read_file_content(filepath):
    """Read a file and return its content with line numbers for investigation.

    Args:
        filepath: Path to the file to read.

    Returns:
        tuple: (raw_content: str, numbered_content: str) or (None, None) if unreadable.
    """
    abs_path = os.path.abspath(os.path.expanduser(filepath))
    if not os.path.isfile(abs_path):
        return None, None

    try:
        with open(abs_path, 'r', encoding='utf-8') as f:
            raw = f.read()
    except Exception:
        return None, None

    lines = raw.splitlines()
    numbered = "\n".join(f"{i + 1:>4} | {line}" for i, line in enumerate(lines))
    return raw, numbered


def extract_method_java(content, method_name):
    """Extract a Java method body from file content by name.

    Uses brace-depth tracking starting from the method signature line.

    Args:
        content: Raw file content string.
        method_name: Name of the method to extract.

    Returns:
        str or None: The extracted method text (including signature), or None if not found.
    """
    lines = content.splitlines()
    # Pattern matches common Java method signatures containing the method name
    sig_pattern = re.compile(
        r'(public|protected|private|static|\s)+'
        + re.escape(method_name)
        + r'\s*\('
    )

    start_idx = None
    for i, line in enumerate(lines):
        if sig_pattern.search(line):
            start_idx = i
            break

    if start_idx is None:
        return None

    # Track braces to find the end of the method
    depth = 0
    found_open = False
    end_idx = start_idx

    for i in range(start_idx, len(lines)):
        for ch in lines[i]:
            if ch == '{':
                depth += 1
                found_open = True
            elif ch == '}':
                depth -= 1
        if found_open and depth == 0:
            end_idx = i
            break
    else:
        # Reached end of file without closing — return from start to EOF
        end_idx = len(lines) - 1

    return "\n".join(lines[start_idx:end_idx + 1])


def extract_method_generic(content, method_hint):
    """Search for a code snippet or function name in file content.

    For non-Java files or when the language is unknown, does a simple
    text search and returns surrounding context.

    Args:
        content: Raw file content string.
        method_hint: A method name or code snippet to search for.

    Returns:
        str or None: The matching region with context, or None if not found.
    """
    lines = content.splitlines()
    hint_lower = method_hint.lower().strip()

    for i, line in enumerate(lines):
        if hint_lower in line.lower():
            # Return ±20 lines of context around the match
            start = max(0, i - 20)
            end = min(len(lines), i + 21)
            numbered = "\n".join(
                f"{j + 1:>4} | {lines[j]}" for j in range(start, end)
            )
            return numbered

    return None


def detect_language(filepath):
    """Detect language from file extension.

    Args:
        filepath: Path to the file.

    Returns:
        str: Language identifier (e.g. 'java', 'javascript', 'python', 'unknown').
    """
    ext = os.path.splitext(filepath)[1].lower()
    mapping = {
        '.java': 'java',
        '.js': 'javascript',
        '.jsx': 'javascript',
        '.ts': 'typescript',
        '.tsx': 'typescript',
        '.py': 'python',
        '.sql': 'sql',
        '.xml': 'xml',
        '.json': 'json',
        '.properties': 'properties',
        '.jsp': 'jsp',
    }
    return mapping.get(ext, 'unknown')


def build_structural_summary(content, language):
    """Extract a file's structural skeleton — imports, class declarations,
    and method signatures (without bodies).

    Gives the agent the "big picture" of a file without the full content,
    helping it understand the overall structure and available methods.

    Args:
        content: Raw file content string.
        language: Language identifier (e.g., 'java', 'python').

    Returns:
        str: A compact structural summary of the file.
    """
    lines = content.splitlines()
    summary_lines = []

    if language == 'java':
        for line in lines:
            stripped = line.strip()
            if not stripped or stripped.startswith('*') or stripped.startswith('/*'):
                continue
            # Keep: package, import, class/interface declarations, method signatures
            if (stripped.startswith('package ')
                    or stripped.startswith('import ')
                    or re.match(
                        r'(public|protected|private|static|abstract|final)\s+'
                        r'.*(class|interface|enum)\s', stripped)
                    or re.match(
                        r'\s*(public|protected|private|static)\s+.*\(.*\)', stripped)):
                summary_lines.append(line)
    elif language == 'python':
        for line in lines:
            stripped = line.strip()
            if (stripped.startswith('import ')
                    or stripped.startswith('from ')
                    or stripped.startswith('class ')
                    or stripped.startswith('def ')):
                summary_lines.append(line)
    else:
        # Generic: keep lines that look like declarations
        for line in lines:
            stripped = line.strip()
            if (stripped.startswith('import ')
                    or stripped.startswith('from ')
                    or 'class ' in stripped
                    or 'function ' in stripped
                    or stripped.startswith('def ')
                    or stripped.startswith('export ')):
                summary_lines.append(line)

    if not summary_lines:
        return "--- File Structure ---\n(Could not extract structural summary)\n"

    return "--- File Structure (imports, classes, method signatures) ---\n" + \
           "\n".join(summary_lines) + "\n--- End Structure ---"


def build_investigation_context(file_path, method_hint=None, full=False):
    """Build a rich context string for the agent from a file and optional method hint.

    This is the main entry point used by main.py to construct the investigation
    context that gets injected into the agent's conversation.

    When a method hint is provided and successfully matched, sends only the
    structural summary (imports + class + method signatures) plus the extracted
    method — NOT the full file. This typically reduces context by 70-90%.

    The agent can request the full file via ``[Investigate: filename]`` if the
    focused extraction is insufficient.

    Args:
        file_path: Path to the entry-point file.
        method_hint: Optional method name or code snippet to focus on.
        full: If True, always include the full file content regardless of
              whether a method hint was matched.

    Returns:
        str: Formatted context string ready to inject into the conversation,
             or an error message if the file cannot be read.
    """
    abs_path = os.path.abspath(os.path.expanduser(file_path))
    raw_content, numbered_content = read_file_content(abs_path)

    if raw_content is None:
        return f"[Error: Could not read file '{file_path}'. Please check the path and try again.]"

    language = detect_language(abs_path)
    filename = os.path.basename(abs_path)
    total_lines = len(raw_content.splitlines())

    context_parts = [
        f"\n[System Context — Investigation Entry Point]",
        f"File: {abs_path}",
        f"Language: {language}",
        f"Total lines: {total_lines}",
    ]

    # If a method hint is provided, try to extract the specific method
    if method_hint and not full:
        method_hint = method_hint.strip()
        context_parts.append(f"Method/Code hint: {method_hint}")

        extracted = None
        if language == 'java':
            extracted = extract_method_java(raw_content, method_hint)
        if extracted is None:
            extracted = extract_method_generic(raw_content, method_hint)

        if extracted:
            # Smart pruning: structural summary + focused extraction only
            structural = build_structural_summary(raw_content, language)
            context_parts.append(f"\n{structural}")
            context_parts.append(f"\n--- Focused Extraction: {method_hint} ---")
            context_parts.append(extracted)
            context_parts.append("--- End Focused Extraction ---")
            context_parts.append(
                f"\n[Note: Showing focused extraction ({len(extracted.splitlines())} lines) "
                f"from a {total_lines}-line file. Full file content available via "
                f"[Investigate: {filename}] if you need more context.]"
            )
            return "\n".join(context_parts)
        else:
            context_parts.append(
                f"[Note: Could not locate '{method_hint}' in the file. "
                f"Providing full file content instead.]"
            )

    # Full file content — either no hint, hint didn't match, or full=True
    context_parts.append(f"\n--- Full Content of {filename} ---")
    context_parts.append(numbered_content)
    context_parts.append(f"--- End of {filename} ---")

    return "\n".join(context_parts)
