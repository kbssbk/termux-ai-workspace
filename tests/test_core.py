import json,tempfile,unittest
from pathlib import Path
from workspace.config import load_projects,public_project
from workspace.runner import run_command
class CoreTests(unittest.TestCase):
 def test_load_and_hide_path(self):
  with tempfile.TemporaryDirectory() as d:
   p=Path(d)/"p.json";p.write_text(json.dumps([{"id":"x","name":"X","type":"x","repo_path":"~/secret","actions":["pull"]}]))
   x=load_projects(p)[0];self.assertNotIn("repo_path",public_project(x))
 def test_reject_unknown_action(self):
  with tempfile.TemporaryDirectory() as d:
   p=Path(d)/"p.json";p.write_text(json.dumps([{"id":"x","name":"X","type":"x","repo_path":"~","actions":["rm"]}]))
   with self.assertRaises(ValueError):load_projects(p)
 def test_runner(self):
  r=run_command(["python","-c","print('OK')"]);self.assertTrue(r["ok"]);self.assertIn("OK",r["stdout"])
