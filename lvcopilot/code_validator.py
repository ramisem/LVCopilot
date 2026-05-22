"""Local code validator for LVCopilot-generated LabVantage Java code.

Performs regex-based static analysis against LabVantage best practices
**before** the generated code is shown to the user.  All checks are local
(no LLM call) — an LLM correction round is triggered only when issues are
found.

Toggle via the ``LV_VALIDATE`` environment variable (default: ``true``).
"""

import re
from dataclasses import dataclass, field


@dataclass
class ValidationIssue:
    """A single code-quality issue detected by the validator."""
    severity: str       # "error" | "warning"
    rule: str           # e.g., "SQL_INJECTION"
    line_hint: str      # approximate location / context
    message: str        # human-readable explanation
    fix_hint: str       # how to fix it


# ── Individual check functions ────────────────────────────────────────────

def _check_sql_injection(code, lines):
    """Detect string concatenation inside SQL statements.

    Flags patterns like:
        ``"SELECT ... " + variable``
        ``"WHERE col = '" + value + "'"``
    """
    issues = []
    # Pattern: SQL keyword in a string literal followed by string concat
    sql_concat = re.compile(
        r'(?:\"[^\"]*(?:SELECT|INSERT|UPDATE|DELETE|WHERE|FROM|SET|VALUES)'
        r'[^\"]*\"\s*\+)|'
        r'(?:\+\s*\"[^\"]*(?:SELECT|INSERT|UPDATE|DELETE|WHERE|FROM|SET|VALUES))',
        re.IGNORECASE,
    )

    for i, line in enumerate(lines, 1):
        stripped = line.strip()
        # Skip comment lines
        if stripped.startswith('//') or stripped.startswith('*') or stripped.startswith('/*'):
            continue
        # Skip lines that are part of SafeSQL / Object[] usage
        if 'SafeSQL' in line or 'safeSQL' in line or 'Object[]' in line or 'addVar' in line:
            continue
        # Skip lines that are inside the WRONG example block (anti-pattern demos)
        if '// WRONG' in line or '// INCORRECT' in line or 'Anti-Pattern' in line:
            continue

        if sql_concat.search(line):
            issues.append(ValidationIssue(
                severity="error",
                rule="SQL_INJECTION",
                line_hint=f"Line ~{i}: {stripped[:80]}",
                message="SQL string concatenation detected. This is a SQL injection risk.",
                fix_hint="Use SafeSQL with addVar() or QueryProcessor with Object[] params.",
            ))

    return issues


def _check_dataset_null_safety(code, lines):
    """Detect DataSet.getValue() without prior getRowCount() check."""
    issues = []
    # Find lines with ds.getValue(0, ...) or similar
    get_value_pattern = re.compile(r'\.getValue\s*\(\s*0\s*,')

    for i, line in enumerate(lines, 1):
        if not get_value_pattern.search(line):
            continue

        # Check if there's a getRowCount() check within 8 lines before
        start = max(0, i - 9)  # i is 1-indexed, lines is 0-indexed
        preceding = '\n'.join(lines[start:i - 1])
        has_check = (
            'getRowCount()' in preceding
            or '.getRowCount() >' in preceding
            or '.getRowCount() !=' in preceding
            or 'getRowCount() ==' in preceding
        )

        if not has_check:
            issues.append(ValidationIssue(
                severity="warning",
                rule="DATASET_NULL_SAFETY",
                line_hint=f"Line ~{i}: {line.strip()[:80]}",
                message="DataSet.getValue(0, ...) called without a prior getRowCount() check.",
                fix_hint="Add 'if (ds.getRowCount() > 0)' before accessing row 0.",
            ))

    return issues


def _check_ajax_error_handling(code, lines):
    """Detect AJAX handlers missing try/catch/finally skeleton."""
    issues = []
    has_handle_request = False
    has_try = False
    has_catch = False
    has_finally = False

    for line in lines:
        stripped = line.strip()
        if 'handleRequest' in stripped or 'handleAjax' in stripped:
            has_handle_request = True
        if stripped.startswith('try'):
            has_try = True
        if stripped.startswith('catch') or stripped.startswith('} catch'):
            has_catch = True
        if stripped.startswith('finally') or stripped.startswith('} finally'):
            has_finally = True

    if has_handle_request and not (has_try and has_catch and has_finally):
        missing = []
        if not has_try:
            missing.append("try")
        if not has_catch:
            missing.append("catch")
        if not has_finally:
            missing.append("finally")

        issues.append(ValidationIssue(
            severity="error",
            rule="AJAX_ERROR_HANDLING",
            line_hint="AJAX handler method",
            message=f"AJAX handler is missing {'/'.join(missing)} block(s). "
                    "AJAX handlers MUST have try/catch/finally.",
            fix_hint="Wrap the handler body in try { ... } catch (Exception e) "
                     "{ ... } finally { ... }",
        ))

    return issues


def _check_sdc_requires_flags(code, lines):
    """Flag SDC rules where ALL requires* flags are set to true."""
    issues = []
    requires_true_count = 0
    requires_total_count = 0

    for line in lines:
        if re.search(r'requires\w+\s*[=:]\s*true', line, re.IGNORECASE):
            requires_true_count += 1
            requires_total_count += 1
        elif re.search(r'requires\w+\s*[=:]\s*false', line, re.IGNORECASE):
            requires_total_count += 1

    # Flag if there are 3+ requires flags and ALL are true
    if requires_total_count >= 3 and requires_true_count == requires_total_count:
        issues.append(ValidationIssue(
            severity="warning",
            rule="SDC_REQUIRES_FLAGS",
            line_hint="SDC Rule configuration",
            message=f"All {requires_true_count} requires* flags are set to true. "
                    "This causes unnecessary DB overhead.",
            fix_hint="Set requires* flags to true ONLY for hooks your rule actually "
                     "implements. Set unused hooks to false.",
        ))

    return issues


def _check_boolean_convention(code, lines):
    """Detect using 'true'/'false' instead of 'Yes'/'No' in output properties."""
    issues = []
    # Pattern: setProperty(..., "true") or setProperty(..., "false")
    bool_pattern = re.compile(
        r'\.setProperty\s*\([^)]*,\s*\"(true|false)\"\s*\)',
        re.IGNORECASE,
    )

    for i, line in enumerate(lines, 1):
        match = bool_pattern.search(line)
        if match:
            bad_value = match.group(1)
            issues.append(ValidationIssue(
                severity="warning",
                rule="BOOLEAN_CONVENTION",
                line_hint=f"Line ~{i}: {line.strip()[:80]}",
                message=f"Boolean output uses \"{bad_value}\" — LabVantage convention is "
                        "\"Yes\"/\"No\".",
                fix_hint=f"Replace \"{bad_value}\" with "
                         f"\"{'Yes' if bad_value.lower() == 'true' else 'No'}\".",
            ))

    return issues


def _check_is_database_required(code, lines):
    """Detect actions with no DB calls but missing isDatabaseRequired override."""
    issues = []
    has_db_call = False
    has_override = False
    is_action = False

    for line in lines:
        if 'extends BaseAction' in line:
            is_action = True
        if ('this.database' in line or 'getQueryProcessor()' in line
                or 'this.dbUtil' in line):
            has_db_call = True
        if 'isDatabaseRequired' in line:
            has_override = True

    if is_action and not has_db_call and not has_override:
        issues.append(ValidationIssue(
            severity="warning",
            rule="MISSING_IS_DATABASE_REQUIRED",
            line_hint="Action class declaration",
            message="Action has no database calls but does not override "
                    "isDatabaseRequired() to return false.",
            fix_hint="Add: @Override public boolean isDatabaseRequired() { return false; }",
        ))

    return issues


def _check_import_completeness(code, lines):
    """Detect usage of common classes without matching import statements."""
    issues = []
    # Map of class usage pattern → expected import
    checks = {
        'SafeSQL': 'sapphire.util.SafeSQL',
        'DataSet': 'sapphire.util.DataSet',
        'QueryProcessor': 'sapphire.accessor.QueryProcessor',
        'SapphireException': 'sapphire.SapphireException',
        'PropertyList': 'sapphire.xml.PropertyList',
        'ActionProcessor': 'sapphire.accessor.ActionProcessor',
        'SDCProcessor': 'sapphire.accessor.SDCProcessor',
        'ActionBlock': 'sapphire.util.ActionBlock',
    }

    # Gather all import lines
    import_text = ' '.join(line for line in lines if line.strip().startswith('import '))

    for class_name, expected_import in checks.items():
        # Check if the class is used in non-import, non-comment lines
        used = False
        for line in lines:
            stripped = line.strip()
            if stripped.startswith('import ') or stripped.startswith('//'):
                continue
            if class_name in stripped:
                used = True
                break

        if used and expected_import not in import_text and class_name not in import_text:
            issues.append(ValidationIssue(
                severity="warning",
                rule="MISSING_IMPORT",
                line_hint=f"Class usage: {class_name}",
                message=f"'{class_name}' is used but not imported.",
                fix_hint=f"Add: import {expected_import};",
            ))

    return issues


# ── Main validator class ──────────────────────────────────────────────────

class LVCodeValidator:
    """Validates generated LabVantage Java code against best practices.

    Usage::

        validator = LVCodeValidator()
        issues = validator.validate(java_code, component="action")
        for issue in issues:
            print(f"[{issue.severity}] {issue.rule}: {issue.message}")
    """

    def validate(self, code, component="action"):
        """Run all applicable checks and return a list of issues.

        Args:
            code: The generated Java source code as a string.
            component: The LV component type ('action', 'ajax', 'sdc_rule',
                       'javascript', 'sdms').

        Returns:
            list[ValidationIssue]: All detected issues.
        """
        lines = code.splitlines()
        issues = []

        # Universal checks (apply to all components)
        issues.extend(_check_sql_injection(code, lines))
        issues.extend(_check_dataset_null_safety(code, lines))
        issues.extend(_check_boolean_convention(code, lines))
        issues.extend(_check_import_completeness(code, lines))

        # Component-specific checks
        if component == 'action':
            issues.extend(_check_is_database_required(code, lines))

        if component == 'ajax':
            issues.extend(_check_ajax_error_handling(code, lines))

        if component == 'sdc_rule':
            issues.extend(_check_sdc_requires_flags(code, lines))

        return issues


def format_validation_feedback(issues):
    """Format validation issues into a feedback string for the agent.

    Args:
        issues: List of ValidationIssue objects.

    Returns:
        str: Formatted feedback string ready to inject into the conversation.
    """
    if not issues:
        return ""

    errors = [i for i in issues if i.severity == "error"]
    warnings = [i for i in issues if i.severity == "warning"]

    parts = [f"Found {len(issues)} issue(s) in the generated code:\n"]

    if errors:
        parts.append(f"🔴 ERRORS ({len(errors)}):")
        for i, e in enumerate(errors, 1):
            parts.append(f"  {i}. [{e.rule}] {e.message}")
            parts.append(f"     Location: {e.line_hint}")
            parts.append(f"     Fix: {e.fix_hint}")
        parts.append("")

    if warnings:
        parts.append(f"🟡 WARNINGS ({len(warnings)}):")
        for i, w in enumerate(warnings, 1):
            parts.append(f"  {i}. [{w.rule}] {w.message}")
            parts.append(f"     Location: {w.line_hint}")
            parts.append(f"     Fix: {w.fix_hint}")

    return "\n".join(parts)


def extract_code_blocks(response_text):
    """Extract Java code blocks from an agent response.

    Args:
        response_text: The agent's full response string.

    Returns:
        list[str]: List of code block contents (Java/no-language blocks only).
    """
    pattern = r'```(?:java)?\n(.*?)```'
    matches = re.findall(pattern, response_text, re.DOTALL)
    return matches
