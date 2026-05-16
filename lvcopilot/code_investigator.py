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
    target = filename.strip()
    for root_dir in search_roots:
        if not os.path.isdir(root_dir):
            continue
        for dirpath, dirnames, filenames in os.walk(root_dir):
            # Skip non-project directories in-place
            dirnames[:] = [d for d in dirnames if d not in _SKIP_DIRS]
            if target in filenames:
                return os.path.abspath(os.path.join(dirpath, target))
    return None


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


def build_investigation_context(file_path, method_hint=None):
    """Build a rich context string for the agent from a file and optional method hint.

    This is the main entry point used by main.py to construct the investigation
    context that gets injected into the agent's conversation.

    Args:
        file_path: Path to the entry-point file.
        method_hint: Optional method name or code snippet to focus on.

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

    context_parts = [
        f"\n[System Context — Investigation Entry Point]",
        f"File: {abs_path}",
        f"Language: {language}",
        f"Total lines: {len(raw_content.splitlines())}",
    ]

    # If a method hint is provided, try to extract the specific method
    if method_hint:
        method_hint = method_hint.strip()
        context_parts.append(f"Method/Code hint: {method_hint}")

        extracted = None
        if language == 'java':
            extracted = extract_method_java(raw_content, method_hint)
        if extracted is None:
            extracted = extract_method_generic(raw_content, method_hint)

        if extracted:
            context_parts.append(f"\n--- Focused Extraction (around '{method_hint}') ---")
            context_parts.append(extracted)
            context_parts.append("--- End Focused Extraction ---")
        else:
            context_parts.append(
                f"[Note: Could not locate '{method_hint}' in the file. "
                f"Providing full file content instead.]"
            )

    # Always include the full file content for complete context
    context_parts.append(f"\n--- Full Content of {filename} ---")
    context_parts.append(numbered_content)
    context_parts.append(f"--- End of {filename} ---")

    return "\n".join(context_parts)
