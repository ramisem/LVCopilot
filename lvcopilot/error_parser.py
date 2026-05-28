"""Error log parsing engine for Debug Mode.

Parses Java stack traces, SQL queries, and LabVantage action call logs
from user input.  Locates the referenced source files in the client
workspace first, then falls back to the embedded product source
(sapphire_jar_decompiled).  Extracts surrounding code context (±30
lines) for each matched frame and builds a rich system-context block
that is injected into the agent's conversation.
"""

import os
import re

from . import code_investigator


# ──────────────────────────────────────────────
# Constants
# ──────────────────────────────────────────────
CONTEXT_RADIUS = 30  # ±30 lines around the error line
MAX_PRODUCT_FRAMES = 3  # Cap on product-source frames to inject context


# ──────────────────────────────────────────────
# Data classes (plain dicts for simplicity)
# ──────────────────────────────────────────────

def _frame(fqcn, method, filename, lineno):
    """Create a stack-trace frame dict."""
    return {
        "fqcn": fqcn,          # e.g. "com.client.actions.MyAction"
        "method": method,      # e.g. "execute"
        "filename": filename,  # e.g. "MyAction.java"
        "lineno": lineno,      # int or None
    }


def _action_call(action_class, parameters_raw):
    """Create an action-call metadata dict."""
    return {
        "action_class": action_class,
        "parameters_raw": parameters_raw,
    }


# ──────────────────────────────────────────────
# 1. Stack Trace Parser
# ──────────────────────────────────────────────

# Matches lines like:
#   at com.client.actions.MyAction.execute(MyAction.java:42)
#   at sapphire.util.ActionProcessor.run(ActionProcessor.java:123)
_STACK_FRAME_RE = re.compile(
    r'at\s+'
    r'([\w\.\$]+)'            # FQCN  (group 1)
    r'\.'
    r'([\w<>\$]+)'            # method (group 2)
    r'\('
    r'([\w\.\-]+)'            # filename (group 3)
    r'(?::(\d+))?'            # optional line number (group 4)
    r'\)'
)

# Matches the exception header line, e.g.:
#   java.lang.NullPointerException: some message
#   Caused by: java.sql.SQLException: ORA-00942
_EXCEPTION_HEADER_RE = re.compile(
    r'(?:^|\n)\s*(?:Caused by:\s*)?'
    r'([\w\.\$]+(?:Exception|Error|Throwable))'
    r'(?::\s*(.*))?',
    re.MULTILINE,
)


def parse_stack_trace(text):
    """Parse Java stack trace frames from text.

    Returns:
        tuple: (frames: list[dict], exceptions: list[dict])
            frames — list of _frame dicts (deduplicated, ordered as found).
            exceptions — list of {"type": str, "message": str} dicts.
    """
    frames = []
    seen = set()
    for m in _STACK_FRAME_RE.finditer(text):
        fqcn = m.group(1)
        method = m.group(2)
        filename = m.group(3)
        lineno = int(m.group(4)) if m.group(4) else None
        key = (fqcn, method, filename, lineno)
        if key not in seen:
            seen.add(key)
            frames.append(_frame(fqcn, method, filename, lineno))

    exceptions = []
    for m in _EXCEPTION_HEADER_RE.finditer(text):
        exceptions.append({
            "type": m.group(1),
            "message": (m.group(2) or "").strip(),
        })

    return frames, exceptions


# ──────────────────────────────────────────────
# 2. SQL Query Detector
# ──────────────────────────────────────────────

# Matches common SQL statement starts.  We look for the keyword at the
# start of a line or after whitespace, optionally inside quotes.
_SQL_KEYWORDS = (
    "SELECT", "INSERT", "UPDATE", "DELETE", "MERGE",
    "WITH",   # CTE
)

_SQL_RE = re.compile(
    r'(?:^|\n)\s*'
    r'(?:[\"\']?\s*)?'
    r'(' + '|'.join(_SQL_KEYWORDS) + r')\b'
    r'(.+?)(?:;|\n\n|\Z)',
    re.IGNORECASE | re.DOTALL,
)

# Heuristic: also catch parameter-binding lines that look like
#   Bind variable 1 = 'value'   or   :1 = value
_SQL_PARAM_RE = re.compile(
    r'(?:bind\s*(?:variable|param(?:eter)?)\s*\d*\s*[:=]\s*(.+))|'
    r'(?::\d+\s*=\s*(.+))',
    re.IGNORECASE,
)


def detect_sql_queries(text):
    """Detect SQL queries and bind parameters in the log text.

    Returns:
        list[dict]: Each dict has keys "query" (str) and
                    "params" (list[str]).
    """
    results = []
    for m in _SQL_RE.finditer(text):
        keyword = m.group(1)
        body = m.group(2).strip()
        full_query = f"{keyword} {body}"
        # Clean trailing quote artifacts
        full_query = full_query.rstrip("'\"")
        results.append({
            "query": full_query,
            "params": [],
        })

    # Attach bind parameters (best-effort: attach to last query)
    params = []
    for m in _SQL_PARAM_RE.finditer(text):
        val = (m.group(1) or m.group(2) or "").strip()
        params.append(val)
    if params and results:
        results[-1]["params"] = params

    return results


def _extract_table_names(sql_queries):
    """Extract table names from SQL queries for schema auto-injection."""
    tables = set()
    from_re = re.compile(r'\bFROM\b\s+([\w\.]+)', re.IGNORECASE)
    join_re = re.compile(r'\bJOIN\b\s+([\w\.]+)', re.IGNORECASE)
    update_re = re.compile(r'\bUPDATE\b\s+([\w\.]+)', re.IGNORECASE)
    insert_re = re.compile(r'\bINSERT\s+INTO\s+([\w\.]+)', re.IGNORECASE)
    merge_re = re.compile(r'\bMERGE\s+INTO\s+([\w\.]+)', re.IGNORECASE)
    for sq in sql_queries:
        query_text = sq.get('query', '')
        for pat in (from_re, join_re, update_re, insert_re, merge_re):
            for m in pat.finditer(query_text):
                table_name = m.group(1).strip().upper()
                if table_name:
                    tables.add(table_name)
    return tables


# ──────────────────────────────────────────────
# 3. Action Call & Parameter Parser
# ──────────────────────────────────────────────

# Matches patterns like:
#   Calling action com.client.actions.MyAction with parameters: {id=123, name=test}
#   Executing action: com.client.actions.MyAction  Parameters: {...}
#   action=com.client.actions.MyAction
#   ActionClass=com.client.actions.MyAction
_ACTION_CALL_RE = re.compile(
    r'(?:(?:calling|executing|running|invoking)\s+action\s*[:=]?\s*)'
    r'([\w\.\$]+)'
    r'(?:\s+(?:with\s+)?(?:parameters?|params?|args?)\s*[:=]?\s*)?'
    r'(\{[^}]*\})?',
    re.IGNORECASE,
)

# Fallback: match "action=FQCN" style
_ACTION_EQ_RE = re.compile(
    r'(?:action(?:class|name)?)\s*=\s*([\w\.\$]+)',
    re.IGNORECASE,
)

# Matches key=value pairs inside a parameter block like {id=123, name=test}
_PARAM_KV_RE = re.compile(r'([\w]+)\s*=\s*([^,}]+)')


def detect_action_calls(text):
    """Detect LabVantage action calls and their parameters.

    Returns:
        list[dict]: Each dict has "action_class" (str) and
                    "parameters_raw" (str).
    """
    results = []
    seen = set()

    for m in _ACTION_CALL_RE.finditer(text):
        cls = m.group(1)
        params_raw = (m.group(2) or "").strip()
        if cls not in seen:
            seen.add(cls)
            results.append(_action_call(cls, params_raw))

    # Fallback patterns
    for m in _ACTION_EQ_RE.finditer(text):
        cls = m.group(1)
        if cls not in seen:
            seen.add(cls)
            results.append(_action_call(cls, ""))

    return results


# ──────────────────────────────────────────────
# 4. Hierarchical File Matcher
# ──────────────────────────────────────────────

def _find_file_by_fqcn(fqcn, filename, search_roots):
    """Search for a file matching the FQCN suffix in the project."""
    if not fqcn:
        return None
    # Strip nested class suffix
    base_fqcn = fqcn.split('$')[0]
    # Replace dots with path separators
    rel_path_suffix = base_fqcn.replace('.', os.sep)
    # Get extension
    ext = os.path.splitext(filename)[1]
    suffix = rel_path_suffix + ext
    
    # Normalize path separator for matching
    suffix_norm = suffix.replace('\\', '/').lower()
    
    for root_dir in search_roots:
        if not os.path.isdir(root_dir):
            continue
        for dirpath, dirnames, filenames in os.walk(root_dir):
            # Skip non-project directories in-place
            dirnames[:] = [d for d in dirnames if d not in code_investigator._SKIP_DIRS]
            if filename in filenames:
                candidate = os.path.abspath(os.path.join(dirpath, filename))
                candidate_norm = candidate.replace('\\', '/').lower()
                if candidate_norm.endswith(suffix_norm):
                    return candidate
    return None


def _find_file(filename, search_roots, product_src_dir, fqcn=None):
    """Search for a file first in client workspace, then in product source.

    Args:
        filename: Basename of the file (e.g. "MyAction.java").
        search_roots: List of client workspace directories.
        product_src_dir: Path to sapphire_jar_decompiled or None.
        fqcn: Fully Qualified Class Name of the class (e.g. "com.client.actions.MyAction").

    Returns:
        tuple: (abs_path: str or None, is_product: bool)
    """
    # 1. First attempt to match by FQCN in client workspace (more precise)
    if fqcn:
        found = _find_file_by_fqcn(fqcn, filename, search_roots)
        if found:
            return found, False

    # Fallback to simple basename search in client workspace
    found = code_investigator.find_file_in_project(filename, search_roots)
    if found:
        return found, False

    # 2. Search product source by FQCN first
    if product_src_dir and os.path.isdir(product_src_dir):
        if fqcn:
            found = _find_file_by_fqcn(fqcn, filename, [product_src_dir])
            if found:
                return found, True
        
        # Fallback to simple basename search in product source
        found = code_investigator.find_file_in_project(
            filename, [product_src_dir]
        )
        if found:
            return found, True

    return None, False


# ──────────────────────────────────────────────
# 5. Offending Code Extractor
# ──────────────────────────────────────────────

def _find_execute_method_line(filepath):
    """Scan the file for the 'execute' method to use as a focus line.

    Returns:
        int or None: The 1-indexed line number of the execute method, or None.
    """
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            for idx, line in enumerate(f, 1):
                if 'public int execute' in line or 'int execute(' in line or 'void execute(' in line:
                    return idx
    except Exception:
        pass
    return None


def extract_code_context(filepath, lineno, radius=CONTEXT_RADIUS):
    """Extract lines around the error location with line numbers.

    Args:
        filepath: Absolute path to the source file.
        lineno: 1-indexed line number of the error, or None.
        radius: Number of lines before/after to include.

    Returns:
        str or None: Numbered code excerpt, or None if unreadable.
    """
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            lines = f.readlines()
    except Exception:
        return None

    total = len(lines)

    if lineno is None or lineno < 1:
        # No specific line — return the first 61 lines as a summary
        end = min(total, 2 * radius + 1)
        selected = lines[:end]
        start_idx = 0
    else:
        start_idx = max(0, lineno - 1 - radius)
        end_idx = min(total, lineno + radius)
        selected = lines[start_idx:end_idx]

    numbered = []
    for i, line in enumerate(selected, start=start_idx + 1):
        marker = " >>>" if (lineno and i == lineno) else "    "
        numbered.append(f"{i:>5}{marker} | {line.rstrip()}")

    return "\n".join(numbered)


# ──────────────────────────────────────────────
# 6. Top-Level Context Builder
# ──────────────────────────────────────────────

def parse_error_log(text, search_roots, product_src_dir):
    """Parse an error log and build a rich investigation context.

    This is the main entry-point called from main.py.  It scans the
    user's input for stack traces, SQL queries, and action calls;
    locates the referenced source files; and constructs a formatted
    context block to inject into the agent's conversation.

    Args:
        text: The raw user input text (may contain a pasted log).
        search_roots: List of client workspace directory paths.
        product_src_dir: Path to the sapphire_jar_decompiled directory
                         (or None if unavailable).

    Returns:
        dict with keys:
            "context"        — str: The formatted context block to
                                append to the user input, or "" if
                                nothing was detected.
            "client_files"   — dict: basename → abs_path for client
                                files that were found (for registration
                                in _investigated_files).
            "detected"       — bool: True if any error-log artifacts
                                were detected.
            "sql_tables"     — set: The set of table names extracted from SQL.
    """
    frames, exceptions = parse_stack_trace(text)
    sql_queries = detect_sql_queries(text)
    action_calls = detect_action_calls(text)

    # Nothing detected → short-circuit
    if not frames and not sql_queries and not action_calls:
        return {"context": "", "client_files": {}, "detected": False, "sql_tables": set()}

    parts = []
    client_files = {}

    # ── Header ──
    parts.append("\n[System Context — Automated Error Log Investigation]")

    # ── Exceptions ──
    if exceptions:
        parts.append("\n--- Exceptions Detected ---")
        for exc in exceptions:
            msg = f": {exc['message']}" if exc['message'] else ""
            parts.append(f"  • {exc['type']}{msg}")

    # ── Action Calls ──
    if action_calls:
        parts.append("\n--- Action Call(s) from Log ---")
        for ac in action_calls:
            parts.append(f"  Action Class: {ac['action_class']}")
            if ac['parameters_raw']:
                parts.append(f"  Parameters:   {ac['parameters_raw']}")

    # ── SQL Queries ──
    if sql_queries:
        parts.append("\n--- SQL Query(ies) from Log ---")
        for i, sq in enumerate(sql_queries, 1):
            parts.append(f"  Query {i}: {sq['query']}")
            if sq['params']:
                parts.append(f"  Bind Params: {', '.join(sq['params'])}")
        parts.append(
            "\n[Instruction: If this query is relevant to the error, "
            "consider executing it via `query_database` or inspecting "
            "the schema via `describe_db_table` to understand the "
            "database state that led to the failure.]"
        )

    # ── Code Context Resolution ──
    files_to_resolve = []
    seen_files = set()

    # 1. First add stack frames (with their lineno)
    for frame in frames:
        fn = frame['filename']
        if fn not in seen_files:
            seen_files.add(fn)
            files_to_resolve.append({
                "filename": fn,
                "fqcn": frame['fqcn'],
                "method": frame['method'],
                "lineno": frame['lineno'],
                "is_action_only": False,
            })

    # 2. Add action calls (if not already added by frames)
    for ac in action_calls:
        fqcn = ac['action_class']
        parts_fqcn = fqcn.split('.')
        last_part = parts_fqcn[-1]
        class_name = last_part.split('$')[0]
        fn = f"{class_name}.java"

        if fn not in seen_files:
            seen_files.add(fn)
            files_to_resolve.append({
                "filename": fn,
                "fqcn": fqcn,
                "method": "execute",
                "lineno": None,
                "is_action_only": True,
            })

    if files_to_resolve:
        parts.append(f"\n--- Code Context: {len(files_to_resolve)} file(s) identified ---")

        # Resolve paths
        resolved = {}
        for item in files_to_resolve:
            fn = item['filename']
            fqcn = item['fqcn']
            key = (fn, fqcn)
            if key not in resolved:
                abs_path, is_product = _find_file(
                    fn, search_roots, product_src_dir, fqcn
                )
                resolved[key] = (abs_path, is_product)

        extracted_abs_paths = set()
        product_frame_count = 0

        for item in files_to_resolve:
            fn = item['filename']
            fqcn = item['fqcn']
            abs_path, is_product = resolved[(fn, fqcn)]

            label = f"{item['fqcn']}.{item['method']}({fn}"
            
            # Determine line to focus on
            focus_line = item['lineno']
            if focus_line is None and abs_path:
                focus_line = _find_execute_method_line(abs_path)

            if item['lineno']:
                label += f":{item['lineno']}"
            elif focus_line:
                label += f":execute focused at line {focus_line}"
            label += ")"

            if abs_path is None:
                parts.append(f"\n  ⚠ {label}  — file not found in workspace")
                continue

            tag = "[PRODUCT CODE - READ ONLY REFERENCE]" if is_product \
                else "[CLIENT CODE]"
            action_tag = " [ACTION CLASS]" if item['is_action_only'] else ""

            # Check if this product frame exceeds the MAX_PRODUCT_FRAMES cap
            if is_product:
                product_frame_count += 1
                if product_frame_count > MAX_PRODUCT_FRAMES:
                    parts.append(f"\n  ⏭ {label}  [PRODUCT CODE - SKIPPED] (deep framework frame)")
                    parts.append(f"     Source: {abs_path}")
                    continue

            # Check if we've already extracted this file by absolute path
            if abs_path in extracted_abs_paths:
                parts.append(f"\n  📄 {label}  [DUPLICATE - ALREADY EXTRACTED]")
                parts.append(f"     Source: {abs_path}")
                continue

            extracted_abs_paths.add(abs_path)

            parts.append(f"\n  📄 {label}  {tag}{action_tag}")
            parts.append(f"     Source: {abs_path}")

            if is_product:
                # Product code: focused ±30 line extraction (read-only reference)
                code = extract_code_context(abs_path, focus_line)
                if code:
                    parts.append(code)
            else:
                # Client code: inject structural summary and focused context for Phase 2 scoping.
                # Full content will be injected automatically in Phase 3/4.
                raw_content, _ = code_investigator.read_file_content(abs_path)
                if raw_content:
                    lang = code_investigator.detect_language(abs_path)
                    structural = code_investigator.build_structural_summary(raw_content, lang)
                    parts.append(structural)
                
                code = extract_code_context(abs_path, focus_line)
                if code:
                    parts.append(f"--- Focused Extraction of {fn} (around error line) ---")
                    parts.append(code)
                    parts.append(f"--- End Focused Extraction of {fn} ---")
                    parts.append(
                        f"\n[Note: Showing structural summary and focused extraction of {fn}. "
                        f"The full file content will be automatically loaded into your context "
                        f"when the Architect approves the plan and you transition to Phase 3, or you "
                        f"can request it immediately using the marker `[Investigate: {fn}]` if needed.]"
                    )

            # Track client files for _investigated_files registration
            if not is_product:
                client_files[fn] = abs_path

    parts.append("\n--- End of Automated Error Log Investigation ---")

    return {
        "context": "\n".join(parts),
        "client_files": client_files,
        "detected": True,
        "sql_tables": _extract_table_names(sql_queries),
    }
