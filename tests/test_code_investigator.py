"""Unit tests for lvcopilot.code_investigator module.
"""

import os
import sys
import tempfile
import shutil
import unittest

# Add project root to path so we can import lvcopilot
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))

from lvcopilot import code_investigator


class TestCodeInvestigatorFileSearch(unittest.TestCase):
    """Tests for code_investigator.py file search logic."""

    def setUp(self):
        # Create a temporary directory structure to simulate a project
        self.test_dir = tempfile.mkdtemp()
        
        # Create a mock nested directory structure
        self.src_dir = os.path.join(self.test_dir, "src", "com", "labvantage", "actions")
        os.makedirs(self.src_dir, exist_ok=True)
        
        self.file_path = os.path.join(self.src_dir, "ReagentExpiryNotification.java")
        with open(self.file_path, "w", encoding="utf-8") as f:
            f.write("public class ReagentExpiryNotification {}")
            
        # Create another file with the same name in a different path to test multiple matches
        self.backup_dir = os.path.join(self.test_dir, "backup", "actions")
        os.makedirs(self.backup_dir, exist_ok=True)
        self.backup_file_path = os.path.join(self.backup_dir, "ReagentExpiryNotification.java")
        with open(self.backup_file_path, "w", encoding="utf-8") as f:
            f.write("public class ReagentExpiryNotificationBackup {}")

    def tearDown(self):
        # Remove temporary directory
        shutil.rmtree(self.test_dir)

    def test_find_file_by_basename(self):
        # Searching by exact basename
        found = code_investigator.find_file_in_project("ReagentExpiryNotification.java", [self.test_dir])
        self.assertIsNotNone(found)
        self.assertIn(os.path.normpath(found), [os.path.normpath(self.file_path), os.path.normpath(self.backup_file_path)])

    def test_find_file_by_relative_path_forward_slash(self):
        # Searching by a relative path with forward slashes
        query = "src/com/labvantage/actions/ReagentExpiryNotification.java"
        found = code_investigator.find_file_in_project(query, [self.test_dir])
        self.assertIsNotNone(found)
        self.assertEqual(os.path.normpath(found), os.path.normpath(self.file_path))

    def test_find_file_by_relative_path_backward_slash(self):
        # Searching by a relative path with backward slashes (common on Windows / from some configs)
        query = "src\\com\\labvantage\\actions\\ReagentExpiryNotification.java"
        found = code_investigator.find_file_in_project(query, [self.test_dir])
        self.assertIsNotNone(found)
        self.assertEqual(os.path.normpath(found), os.path.normpath(self.file_path))

    def test_find_file_partial_relative_path(self):
        # Searching by a partial relative path
        query = "labvantage/actions/ReagentExpiryNotification.java"
        found = code_investigator.find_file_in_project(query, [self.test_dir])
        self.assertIsNotNone(found)
        self.assertEqual(os.path.normpath(found), os.path.normpath(self.file_path))

    def test_find_file_not_found(self):
        # Non-existent file
        found = code_investigator.find_file_in_project("NonExistentFile.java", [self.test_dir])
        self.assertIsNone(found)

    def test_find_all_files_project(self):
        # Find all should return both files when matching basename
        matches = code_investigator.find_all_files_in_project("ReagentExpiryNotification.java", [self.test_dir])
        self.assertEqual(len(matches), 2)
        norm_matches = [os.path.normpath(m) for m in matches]
        self.assertIn(os.path.normpath(self.file_path), norm_matches)
        self.assertIn(os.path.normpath(self.backup_file_path), norm_matches)

    def test_find_all_files_by_relative_path(self):
        # Find all with relative path should return only the specific one matching the path structure
        query = "src/com/labvantage/actions/ReagentExpiryNotification.java"
        matches = code_investigator.find_all_files_in_project(query, [self.test_dir])
        self.assertEqual(len(matches), 1)
        self.assertEqual(os.path.normpath(matches[0]), os.path.normpath(self.file_path))


if __name__ == '__main__':
    unittest.main()
