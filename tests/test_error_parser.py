"""Unit tests for lvcopilot.error_parser module.

Tests stack trace parsing, SQL query detection, action call parameter
extraction, and the top-level parse_error_log context builder.
"""

import os
import sys
import tempfile
import unittest

# Add project root to path so we can import lvcopilot
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))

from lvcopilot import error_parser


# ──────────────────────────────────────────────
# Sample log fixtures
# ──────────────────────────────────────────────

SAMPLE_STACK_TRACE = """\
2026-05-27 10:15:23 ERROR [sapphire.action.ActionProcessor] - Action execution failed
java.lang.NullPointerException: Cannot invoke method on null object
    at com.client.actions.SampleAction.execute(SampleAction.java:42)
    at sapphire.action.ActionProcessor.runAction(ActionProcessor.java:185)
    at sapphire.servlet.ActionServlet.doPost(ActionServlet.java:97)
    at javax.servlet.http.HttpServlet.service(HttpServlet.java:681)
"""

SAMPLE_SQL_LOG = """\
2026-05-27 10:15:22 DEBUG [sapphire.util.SafeSQL] - Executing query:
SELECT s_sampleid, s_status FROM s_sample WHERE s_sampleid = ? AND s_status = ?
Bind variable 1 = 'SAMP-001'
Bind variable 2 = 'Active'
2026-05-27 10:15:23 ERROR - java.sql.SQLException: ORA-00942: table or view does not exist
    at oracle.jdbc.driver.OracleStatement.execute(OracleStatement.java:312)
"""

SAMPLE_ACTION_LOG = """\
2026-05-27 10:15:20 INFO - Calling action com.client.actions.SampleAction with parameters: {sampleId=SAMP-001, status=null, userId=admin}
2026-05-27 10:15:23 ERROR - Action execution failed
java.lang.NullPointerException
    at com.client.actions.SampleAction.execute(SampleAction.java:42)
"""

SAMPLE_COMBINED_LOG = """\
2026-05-27 10:15:20 INFO - Calling action com.client.actions.SampleAction with parameters: {sampleId=SAMP-001, status=null}
2026-05-27 10:15:22 DEBUG - Executing query:
SELECT s_sampleid, s_status FROM s_sample WHERE s_sampleid = ?
Bind variable 1 = 'SAMP-001'
2026-05-27 10:15:23 ERROR - Action execution failed
java.lang.NullPointerException: Cannot invoke "String.toString()" because "status" is null
    at com.client.actions.SampleAction.execute(SampleAction.java:42)
    at sapphire.action.ActionProcessor.runAction(ActionProcessor.java:185)
Caused by: java.lang.IllegalStateException: Invalid state
    at com.client.helpers.StatusHelper.validate(StatusHelper.java:18)
"""


class TestStackTraceParser(unittest.TestCase):
    """Tests for parse_stack_trace()."""

    def test_basic_stack_trace(self):
        frames, exceptions = error_parser.parse_stack_trace(SAMPLE_STACK_TRACE)
        self.assertGreater(len(frames), 0)
        # First frame should be the client code
        self.assertEqual(frames[0]['fqcn'], 'com.client.actions.SampleAction')
        self.assertEqual(frames[0]['method'], 'execute')
        self.assertEqual(frames[0]['filename'], 'SampleAction.java')
        self.assertEqual(frames[0]['lineno'], 42)

    def test_extracts_exceptions(self):
        frames, exceptions = error_parser.parse_stack_trace(SAMPLE_STACK_TRACE)
        self.assertGreater(len(exceptions), 0)
        self.assertEqual(exceptions[0]['type'], 'java.lang.NullPointerException')

    def test_multiple_frames(self):
        frames, _ = error_parser.parse_stack_trace(SAMPLE_STACK_TRACE)
        filenames = [f['filename'] for f in frames]
        self.assertIn('SampleAction.java', filenames)
        self.assertIn('ActionProcessor.java', filenames)

    def test_no_stack_trace(self):
        frames, exceptions = error_parser.parse_stack_trace("Hello, no errors here.")
        self.assertEqual(len(frames), 0)
        self.assertEqual(len(exceptions), 0)

    def test_caused_by_chain(self):
        frames, exceptions = error_parser.parse_stack_trace(SAMPLE_COMBINED_LOG)
        exc_types = [e['type'] for e in exceptions]
        self.assertIn('java.lang.NullPointerException', exc_types)
        self.assertIn('java.lang.IllegalStateException', exc_types)

    def test_deduplication(self):
        """Duplicate stack frames should be deduplicated."""
        doubled = SAMPLE_STACK_TRACE + "\n" + SAMPLE_STACK_TRACE
        frames, _ = error_parser.parse_stack_trace(doubled)
        # Each unique (fqcn, method, file, line) should appear only once
        seen = set()
        for f in frames:
            key = (f['fqcn'], f['method'], f['filename'], f['lineno'])
            self.assertNotIn(key, seen, f"Duplicate frame: {key}")
            seen.add(key)


class TestSQLDetector(unittest.TestCase):
    """Tests for detect_sql_queries()."""

    def test_detect_select(self):
        queries = error_parser.detect_sql_queries(SAMPLE_SQL_LOG)
        self.assertGreater(len(queries), 0)
        self.assertIn('SELECT', queries[0]['query'].upper())

    def test_detect_bind_params(self):
        queries = error_parser.detect_sql_queries(SAMPLE_SQL_LOG)
        self.assertGreater(len(queries), 0)
        self.assertGreater(len(queries[-1]['params']), 0)

    def test_no_sql(self):
        queries = error_parser.detect_sql_queries("No SQL here, just a regular log.")
        self.assertEqual(len(queries), 0)

    def test_extract_table_names(self):
        sql_queries = [
            {"query": "SELECT * FROM s_sample WHERE s_sampleid = :1"},
            {"query": "INSERT INTO APP.USERS (id, name) VALUES (1, 'Rami')"},
            {"query": "UPDATE s_sample SET s_status = 'Active' WHERE s_sampleid = 1"},
            {"query": "DELETE FROM s_history WHERE id = 1"},
            {"query": "MERGE INTO s_history h USING s_sample s ON (h.id = s.id) WHEN MATCHED THEN UPDATE SET h.status = s.status"},
        ]
        tables = error_parser._extract_table_names(sql_queries)
        self.assertIn("S_SAMPLE", tables)
        self.assertIn("APP.USERS", tables)
        self.assertIn("S_HISTORY", tables)

    def test_parse_error_log_includes_sql_tables(self):
        result = error_parser.parse_error_log(
            SAMPLE_COMBINED_LOG,
            search_roots=[],
            product_src_dir=None,
        )
        self.assertTrue(result['detected'])
        self.assertIn('sql_tables', result)
        self.assertIn('S_SAMPLE', result['sql_tables'])


class TestActionCallParser(unittest.TestCase):
    """Tests for detect_action_calls()."""

    def test_detect_action_with_params(self):
        calls = error_parser.detect_action_calls(SAMPLE_ACTION_LOG)
        self.assertGreater(len(calls), 0)
        self.assertEqual(calls[0]['action_class'],
                         'com.client.actions.SampleAction')
        self.assertIn('sampleId', calls[0]['parameters_raw'])

    def test_no_action(self):
        calls = error_parser.detect_action_calls("No action calls here.")
        self.assertEqual(len(calls), 0)

    def test_fallback_action_eq(self):
        text = "action=com.client.actions.FallbackAction"
        calls = error_parser.detect_action_calls(text)
        self.assertGreater(len(calls), 0)
        self.assertEqual(calls[0]['action_class'],
                         'com.client.actions.FallbackAction')


class TestCodeContextExtractor(unittest.TestCase):
    """Tests for extract_code_context()."""

    def test_extract_around_line(self):
        # Create a temporary file with 100 lines
        with tempfile.NamedTemporaryFile(
            mode='w', suffix='.java', delete=False
        ) as f:
            for i in range(1, 101):
                f.write(f"// Line {i}\n")
            temp_path = f.name

        try:
            result = error_parser.extract_code_context(temp_path, 50, radius=5)
            self.assertIsNotNone(result)
            # Should contain the error marker
            self.assertIn('>>>', result)
            # Should contain line 50
            self.assertIn('Line 50', result)
            # Should be roughly 11 lines (50-5 to 50+5)
            lines = result.strip().split('\n')
            self.assertEqual(len(lines), 11)
        finally:
            os.unlink(temp_path)

    def test_file_not_found(self):
        result = error_parser.extract_code_context('/nonexistent/file.java', 10)
        self.assertIsNone(result)

    def test_no_lineno(self):
        with tempfile.NamedTemporaryFile(
            mode='w', suffix='.java', delete=False
        ) as f:
            for i in range(1, 20):
                f.write(f"// Line {i}\n")
            temp_path = f.name

        try:
            result = error_parser.extract_code_context(temp_path, None, radius=5)
            self.assertIsNotNone(result)
            # Should NOT contain the error marker since lineno is None
            self.assertNotIn('>>>', result)
        finally:
            os.unlink(temp_path)


class TestParseErrorLog(unittest.TestCase):
    """Tests for the top-level parse_error_log() context builder."""

    def test_combined_log_detected(self):
        result = error_parser.parse_error_log(
            SAMPLE_COMBINED_LOG,
            search_roots=[],
            product_src_dir=None,
        )
        self.assertTrue(result['detected'])
        self.assertIn('Automated Error Log Investigation', result['context'])

    def test_action_in_context(self):
        result = error_parser.parse_error_log(
            SAMPLE_COMBINED_LOG,
            search_roots=[],
            product_src_dir=None,
        )
        self.assertIn('SampleAction', result['context'])

    def test_sql_in_context(self):
        result = error_parser.parse_error_log(
            SAMPLE_COMBINED_LOG,
            search_roots=[],
            product_src_dir=None,
        )
        self.assertIn('SELECT', result['context'])

    def test_exception_in_context(self):
        result = error_parser.parse_error_log(
            SAMPLE_COMBINED_LOG,
            search_roots=[],
            product_src_dir=None,
        )
        self.assertIn('NullPointerException', result['context'])

    def test_no_log(self):
        result = error_parser.parse_error_log(
            "Hello, just a normal message.",
            search_roots=[],
            product_src_dir=None,
        )
        self.assertFalse(result['detected'])
        self.assertEqual(result['context'], '')

    def test_client_file_found(self):
        """When a stack-trace file exists in the search roots, it should be
        found and registered as a client file."""
        with tempfile.TemporaryDirectory() as tmpdir:
            # Create a mock SampleAction.java
            filepath = os.path.join(tmpdir, 'SampleAction.java')
            with open(filepath, 'w') as f:
                for i in range(1, 60):
                    f.write(f"// Line {i}\n")

            result = error_parser.parse_error_log(
                SAMPLE_STACK_TRACE,
                search_roots=[tmpdir],
                product_src_dir=None,
            )
            self.assertTrue(result['detected'])
            self.assertIn('SampleAction.java', result['client_files'])
            self.assertEqual(
                result['client_files']['SampleAction.java'], filepath
            )
            # Context should contain the code excerpt
            self.assertIn('[CLIENT CODE]', result['context'])

    def test_product_file_fallback(self):
        """When a file is only found in product source, it should be tagged
        as READ ONLY."""
        with tempfile.TemporaryDirectory() as product_dir:
            # Create a mock ActionProcessor.java inside product source
            filepath = os.path.join(product_dir, 'ActionProcessor.java')
            with open(filepath, 'w') as f:
                for i in range(1, 200):
                    f.write(f"// Product Line {i}\n")

            result = error_parser.parse_error_log(
                SAMPLE_STACK_TRACE,
                search_roots=[],  # empty client workspace
                product_src_dir=product_dir,
            )
            self.assertTrue(result['detected'])
            # Should NOT be in client_files (it's product code)
            self.assertNotIn('ActionProcessor.java', result['client_files'])
            # Should be tagged as product code
            self.assertIn('PRODUCT CODE - READ ONLY REFERENCE',
                          result['context'])

    def test_action_file_mapping_without_frames(self):
        """When there are no stack frames but an action call is detected,
        the action class file should be resolved and included."""
        with tempfile.TemporaryDirectory() as tmpdir:
            # Create a mock SampleAction.java
            filepath = os.path.join(tmpdir, 'SampleAction.java')
            with open(filepath, 'w') as f:
                f.write("// line 1\n")
                f.write("// line 2\n")
                f.write("public int execute(PropertyList pl) {\n")
                f.write("    return 1;\n")
                f.write("}\n")

            action_log = "Calling action com.client.actions.SampleAction with parameters: {id=123}"
            result = error_parser.parse_error_log(
                action_log,
                search_roots=[tmpdir],
                product_src_dir=None,
            )
            self.assertTrue(result['detected'])
            self.assertIn('SampleAction.java', result['client_files'])
            self.assertEqual(result['client_files']['SampleAction.java'], filepath)
            # The label should indicate execution focused execute method and action class tag
            self.assertIn('[ACTION CLASS]', result['context'])
            self.assertIn('execute focused at line 3', result['context'])

    def test_find_execute_method_line(self):
        """Test private _find_execute_method_line helper directly."""
        with tempfile.NamedTemporaryFile(mode='w', suffix='.java', delete=False) as f:
            f.write("// Some comments\n")
            f.write("public class MyAction {\n")
            f.write("    public int execute(PropertyList pl) {\n")
            f.write("        return 0;\n")
            f.write("    }\n")
            f.write("}\n")
            temp_path = f.name

        try:
            # Test find
            lineno = error_parser._find_execute_method_line(temp_path)
            self.assertEqual(lineno, 3)
        finally:
            os.unlink(temp_path)

    def test_fqcn_precise_file_matching(self):
        """Test that files with duplicate basenames in different packages
        are resolved to their correct paths using FQCN matching."""
        with tempfile.TemporaryDirectory() as tmpdir:
            # Create two different directories for different packages
            pkg1_dir = os.path.join(tmpdir, 'sapphire', 'action')
            pkg2_dir = os.path.join(tmpdir, 'com', 'labvantage', 'actions')
            os.makedirs(pkg1_dir)
            os.makedirs(pkg2_dir)

            # Create duplicate ReagentExpiryNotification.java files in both directories
            file1 = os.path.join(pkg1_dir, 'ReagentExpiryNotification.java')
            file2 = os.path.join(pkg2_dir, 'ReagentExpiryNotification.java')
            
            with open(file1, 'w') as f:
                f.write("// Sapphire Action interface\n")
            with open(file2, 'w') as f:
                f.write("// Implementation class\n")

            # Case 1: Search for sapphire.action.ReagentExpiryNotification
            found1 = error_parser._find_file_by_fqcn(
                'sapphire.action.ReagentExpiryNotification',
                'ReagentExpiryNotification.java',
                [tmpdir]
            )
            self.assertEqual(found1, os.path.abspath(file1))

            # Case 2: Search for com.labvantage.actions.ReagentExpiryNotification
            found2 = error_parser._find_file_by_fqcn(
                'com.labvantage.actions.ReagentExpiryNotification',
                'ReagentExpiryNotification.java',
                [tmpdir]
            )
            self.assertEqual(found2, os.path.abspath(file2))


if __name__ == '__main__':
    unittest.main()
