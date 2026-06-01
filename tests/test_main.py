"""Unit tests for lvcopilot.main module.
"""

import os
import sys
import unittest
from unittest.mock import MagicMock, patch

# Add project root to path so we can import lvcopilot
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))

from lvcopilot import main


class TestMainInvestigation(unittest.TestCase):
    """Tests for main.py investigation features."""

    def test_parse_investigate_markers_single(self):
        text = "Please look at [Investigate: ReagentExpiryNotification.java] and tell me more."
        markers = main._parse_investigate_markers(text)
        self.assertEqual(markers, ["ReagentExpiryNotification.java"])

    def test_parse_investigate_markers_multiple(self):
        text = "Check [Investigate: file1.java] and then [Investigate: file2.java]"
        markers = main._parse_investigate_markers(text)
        self.assertEqual(markers, ["file1.java", "file2.java"])

    def test_parse_investigate_markers_none(self):
        text = "No markers in this text."
        markers = main._parse_investigate_markers(text)
        self.assertEqual(markers, [])

    @patch('lvcopilot.main._investigated_files', {})
    def test_run_auto_investigation_returns_input_when_no_markers(self):
        agent_mock = MagicMock()
        response = "This is a clean response with no investigation markers."
        result = main._run_auto_investigation(agent_mock, response)
        self.assertEqual(result, response)
        agent_mock.send_message.assert_not_called()


class TestProcessAndSaveFiles(unittest.TestCase):
    """Tests for Phase 4 process_and_save_files path resolution."""

    @patch('lvcopilot.main.Prompt.ask')
    @patch('lvcopilot.main.Confirm.ask')
    @patch('lvcopilot.main.file_merger.detect_existing_file')
    @patch('lvcopilot.main._resolve_investigated_file')
    def test_process_and_save_files_agent_path_auto_resolve(self, mock_resolve, mock_detect, mock_confirm_ask, mock_prompt_ask):
        # Setup mocks
        mock_resolve.return_value = None  # Not previously investigated
        mock_detect.return_value = (False, None)  # File does not exist yet
        mock_confirm_ask.return_value = True  # User confirms save
        mock_prompt_ask.return_value = 's'  # Return skip if any prompt.ask is called
        
        # Mock agent and session
        agent_mock = MagicMock()
        session_mock = MagicMock()
        
        # Response with a path containing directory separators
        response = """
[Phase: 4] [Mode: New]
File: src/com/client/actions/MyAction.java
```java
public class MyAction {}
```
"""
        
        with patch('lvcopilot.main._write_new_file') as mock_write:
            main.process_and_save_files(response, agent_mock, session_mock)
            
            # Assert that the path was resolved correctly based on the directory separator
            expected_abs_path = os.path.abspath(os.path.expanduser("src/com/client/actions/MyAction.java"))
            mock_detect.assert_called_with(expected_abs_path)
            mock_write.assert_called_once_with(expected_abs_path, "public class MyAction {}\n", "MyAction.java")
            mock_confirm_ask.assert_called_once_with("Do you want to save this new file?")

    @patch('lvcopilot.main.Prompt.ask')
    @patch('lvcopilot.main.Confirm.ask')
    @patch('lvcopilot.main.file_merger.detect_existing_file')
    @patch('lvcopilot.main._resolve_investigated_file')
    def test_process_and_save_files_agent_path_auto_resolve_existing(self, mock_resolve, mock_detect, mock_confirm_ask, mock_prompt_ask):
        # Setup mocks
        mock_resolve.return_value = None  # Not previously investigated
        mock_detect.return_value = (True, "existing content")  # File exists
        mock_confirm_ask.return_value = True  # User confirms save
        mock_prompt_ask.return_value = 's'  # Return skip if any prompt.ask is called
        
        # Mock agent and session
        agent_mock = MagicMock()
        session_mock = MagicMock()
        
        # Response with a path containing directory separators
        response = """
[Phase: 4] [Mode: New]
File: src/com/client/actions/MyAction.java
```java
public class MyAction {}
```
"""
        
        with patch('lvcopilot.main._write_new_file') as mock_write:
            main.process_and_save_files(response, agent_mock, session_mock)
            
            # Assert that the path was resolved correctly based on the directory separator
            expected_abs_path = os.path.abspath(os.path.expanduser("src/com/client/actions/MyAction.java"))
            mock_detect.assert_called_with(expected_abs_path)
            mock_confirm_ask.assert_called_once_with("Do you want to save the modified file?")


if __name__ == '__main__':
    unittest.main()
