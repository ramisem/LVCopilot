import os
import shutil
import difflib
from datetime import datetime


def detect_existing_file(filepath):
    """Check if a file exists and return its content if so.
    
    Returns:
        tuple: (exists: bool, content: str | None)
    """
    abs_path = os.path.abspath(filepath)
    if os.path.exists(abs_path) and os.path.isfile(abs_path):
        try:
            with open(abs_path, 'r', encoding='utf-8') as f:
                content = f.read()
            return True, content
        except Exception:
            return True, None
    return False, None


def get_merge_max_lines():
    """Read MERGE_MAX_LINES from environment (.lvcopilotenv file). Default: 500."""
    try:
        return int(os.environ.get("MERGE_MAX_LINES", "500"))
    except ValueError:
        return 500


def build_merge_prompt(existing_content, proposed_content, filename):
    """Construct the LLM prompt for intelligent file merging.
    
    Args:
        existing_content: The current content of the file on disk.
        proposed_content: The new content proposed by the agent.
        filename: The name of the file being merged.
    
    Returns:
        str: The merge prompt to send to the LLM.
    """
    return f"""You are performing a code merge for the file: {filename}

You have two versions of this file:

=== EXISTING FILE (currently on disk) ===
{existing_content}
=== END EXISTING FILE ===

=== PROPOSED CHANGES (new functionality to incorporate) ===
{proposed_content}
=== END PROPOSED CHANGES ===

MERGE RULES:
1. **Understand the Proposed Changes**: The proposed changes might represent a complete file or just a partial snippet of modifications to integrate into the existing production file.
2. **Preserve Existing Unchanged Code**: Preserve ALL existing methods, classes, variables, imports, annotations, and comments that are not explicitly and intentionally modified to implement the new feature. Do NOT remove or modify any stable, existing code on disk unless it is a functional change.
3. **Handle Placeholders/Omissions in Proposed Changes**:
   - If a method, class, or section in the proposed changes is represented by a placeholder or has a comment indicating existing code (e.g., `// ... existing code ...`, `// existing logic`, `/* rest of code */`, `// todo: rest of class`), you MUST PRESERVE the entire, original existing body of that method/class/section. Do NOT replace it with the placeholder or a truncated version.
   - If a method present in the existing file is missing or omitted entirely in the proposed changes, preserve the existing method in full.
4. **Intelligent Method Body Replacement**:
   - If a method/function in the proposed changes has the same signature as one in the existing code, compare their bodies carefully.
   - Replace the existing method body ONLY if the proposed version contains a fully implemented, functional, and intentional change to that method's logic.
   - If the proposed method body is simplified, lazy, missing error handling, missing logging, missing variables, or appears to be a degradation/hallucination compared to the existing code on disk, DO NOT replace it. Keep the existing robust implementation.
5. **Incorporate New Elements**:
   - If the proposed changes add NEW methods, variables, imports, fields, annotations, or constants, insert them into the appropriate locations of the existing file.
   - For imports: merge the imports from both files, keeping all unique imports.
6. **Structure and Style**:
   - Maintain the original package declaration, class declarations, annotations, and overall file structure.
   - Retain all original code style, indentation, spacing, comments, and formatting.
7. **Strict Safety Principle**: If in doubt, or if a proposed change to an existing method seems incomplete or potentially degrading, PRESERVE the existing method from the disk file in its entirety. Under no circumstances should stable, existing logic on disk be deleted, truncated, or broken.

OUTPUT RULES:
- Output ONLY the final merged file content.
- Do NOT include any markdown code fences (no ``` markers).
- Do NOT include any explanations, comments about the merge, or preamble.
- The output must be the raw file content, ready to write directly to disk."""


def merge_files(agent, existing_content, proposed_content, filename):
    """Send existing and proposed content to the LLM for intelligent merging.
    
    Uses a one-shot LLM call (does not pollute conversation history).
    
    Args:
        agent: The LVDeveloperAgent instance.
        existing_content: Current file content on disk.
        proposed_content: New content proposed by the agent.
        filename: Name of the file being merged.
    
    Returns:
        str: The merged file content from the LLM.
    
    Raises:
        Exception: If the LLM call fails.
    """
    prompt = build_merge_prompt(existing_content, proposed_content, filename)
    return agent.merge_content(prompt)


def show_diff(original, merged, filename="file"):
    """Display a unified diff between original and merged content in the terminal.
    
    Args:
        original: The original file content.
        merged: The merged file content.
        filename: The filename for diff headers.
    """
    original_lines = original.splitlines(keepends=True)
    merged_lines = merged.splitlines(keepends=True)
    
    diff = difflib.unified_diff(
        original_lines,
        merged_lines,
        fromfile=f"a/{filename} (original)",
        tofile=f"b/{filename} (merged)",
        lineterm=""
    )
    
    diff_output = list(diff)
    
    if not diff_output:
        print("  (no differences detected)")
        return
    
    print(f"\n--- Diff Preview for {filename} ---")
    for line in diff_output:
        line_stripped = line.rstrip('\n')
        if line_stripped.startswith('+') and not line_stripped.startswith('+++'):
            # Addition
            print(f"\033[92m{line_stripped}\033[0m")
        elif line_stripped.startswith('-') and not line_stripped.startswith('---'):
            # Deletion
            print(f"\033[91m{line_stripped}\033[0m")
        elif line_stripped.startswith('@@'):
            # Hunk header
            print(f"\033[96m{line_stripped}\033[0m")
        else:
            print(line_stripped)
    print("--- End Diff ---\n")


def create_backup(filepath):
    """Create a timestamped backup of the file.
    
    Format: filepath.bak.YYYYMMDD_HHMMSS
    
    Args:
        filepath: The absolute path to the file to back up.
    
    Returns:
        str: The path to the backup file, or None if backup failed.
    """
    if not os.path.exists(filepath):
        return None
    
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    backup_path = f"{filepath}.bak.{timestamp}"
    
    try:
        shutil.copy2(filepath, backup_path)
        print(f"  📋 Backup created: {backup_path}")
        return backup_path
    except Exception as e:
        print(f"  ⚠️  Warning: Could not create backup: {e}")
        return None
