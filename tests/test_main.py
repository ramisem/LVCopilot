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


if __name__ == '__main__':
    unittest.main()
