"""Unit tests for agent preferences.
"""

import os
import sys
import unittest
from unittest.mock import MagicMock, patch

sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))

from lvcopilot.agent import LVDeveloperAgent
from lvcopilot.preference_manager import PreferenceManager


class TestAgentPreferences(unittest.TestCase):
    """Tests for architect preference tracking and instruction adherence."""

    @patch('lvcopilot.agent.PreferenceManager')
    @patch('lvcopilot.agent.litellm')
    def test_system_prompt_includes_preferences(self, mock_litellm, mock_pref_mgr_class):
        # Mock preference manager to return a formatted preference block
        mock_pref_mgr = MagicMock()
        mock_pref_mgr.format_for_prompt.return_value = "\n[CODING STYLE]\n  • Always use QueryProcessor instead of raw DBAccess.\n"
        mock_pref_mgr_class.return_value = mock_pref_mgr

        # Mock Litellm completion
        mock_response = MagicMock()
        mock_response.choices = [MagicMock()]
        mock_response.choices[0].message.content = "Test response"
        mock_response.choices[0].message.tool_calls = None
        mock_response.usage = MagicMock()
        mock_response.usage.prompt_tokens = 0
        mock_response.usage.completion_tokens = 0
        mock_litellm.completion.return_value = mock_response

        agent = LVDeveloperAgent()
        prompt = agent._get_system_prompt()

        # Check that the system prompt includes the preferences block
        self.assertIn("CRITICAL RULE FOR ARCHITECT PREFERENCES", prompt)
        self.assertIn("Always use QueryProcessor instead of raw DBAccess.", prompt)

    @patch('lvcopilot.agent.PreferenceManager')
    @patch('lvcopilot.agent.litellm')
    def test_send_message_refreshes_system_prompt(self, mock_litellm, mock_pref_mgr_class):
        mock_pref_mgr = MagicMock()
        mock_pref_mgr.format_for_prompt.return_value = "Initial Pref"
        mock_pref_mgr_class.return_value = mock_pref_mgr

        # Mock Litellm completion
        mock_response = MagicMock()
        mock_response.choices = [MagicMock()]
        mock_response.choices[0].message.content = "Test response"
        mock_response.choices[0].message.tool_calls = None
        mock_response.usage = MagicMock()
        mock_response.usage.prompt_tokens = 0
        mock_response.usage.completion_tokens = 0
        mock_litellm.completion.return_value = mock_response

        agent = LVDeveloperAgent()
        agent.start()

        # Check initial prompt contains Initial Pref
        system_msg = agent.conversation.messages[0]
        self.assertEqual(system_msg["role"], "system")
        self.assertIn("Initial Pref", system_msg["content"])

        # Change preference mock to return updated preference block
        mock_pref_mgr.format_for_prompt.return_value = "Updated Pref"

        # Send a message, which should trigger refresh of system prompt
        agent.send_message("Hello")

        system_msg = agent.conversation.messages[0]
        self.assertEqual(system_msg["role"], "system")
        self.assertNotIn("Initial Pref", system_msg["content"])
        self.assertIn("Updated Pref", system_msg["content"])


if __name__ == '__main__':
    unittest.main()
