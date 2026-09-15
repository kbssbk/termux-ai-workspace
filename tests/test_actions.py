import unittest
from unittest.mock import patch
from workspace.actions import execute_action
class ActionTests(unittest.TestCase):
 def setUp(self):self.p={"id":"x","name":"X","type":"x","repo_path":"~","actions":["pull"]}
 def test_reject(self):self.assertEqual(execute_action(self.p,"deploy")["status"],"rejected")
 @patch("workspace.actions.run_command")
 def test_pull_fixed(self,r):
  r.return_value={"ok":True};execute_action(self.p,"pull");self.assertEqual(r.call_args.args[0],["git","pull","--ff-only"])
