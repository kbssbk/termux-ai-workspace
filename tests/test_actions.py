import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

from workspace.actions import (
    ARTIFACTS,
    artifact_fingerprints,
    execute_action,
    invalidate_verification,
    verify_artifacts,
    write_verification,
)


class ActionTests(unittest.TestCase):
    def setUp(self):
        self.p={"id":"x","name":"X","type":"x","repo_path":"~","actions":["pull"]}

    def test_reject(self):
        self.assertEqual(execute_action(self.p,"deploy")["status"],"rejected")

    @patch("workspace.actions.run_command")
    def test_pull_fixed(self,r):
        r.return_value={"ok":True}
        execute_action(self.p,"pull")
        self.assertEqual(r.call_args.args[0],["git","pull","--ff-only"])

    def test_artifact_allowlist_is_exact(self):
        self.assertEqual(ARTIFACTS,("main.js","manifest.json","styles.css"))

    def test_verify_artifacts_rejects_missing_or_empty(self):
        with tempfile.TemporaryDirectory() as d:
            root=Path(d)
            for name in ARTIFACTS: (root/name).write_text("ok")
            (root/"styles.css").write_text("")
            result=verify_artifacts(root)
            self.assertFalse(result["ok"])
            self.assertEqual(result["status"],"not_ready")

    def test_fingerprints_change_when_artifact_changes(self):
        with tempfile.TemporaryDirectory() as d:
            root=Path(d)
            for name in ARTIFACTS: (root/name).write_text(name)
            before=artifact_fingerprints(root)
            (root/"main.js").write_text("changed")
            after=artifact_fingerprints(root)
            self.assertNotEqual(before,after)

    def test_write_and_invalidate_verification(self):
        with tempfile.TemporaryDirectory() as d:
            root=Path(d)
            for name in ARTIFACTS: (root/name).write_text(name)
            marker=write_verification(root)
            self.assertTrue(marker.exists())
            invalidate_verification(root)
            self.assertFalse(marker.exists())

    @patch("workspace.actions.shutil.which", return_value="/bin/npm")
    @patch("workspace.actions.run_command")
    def test_build_runs_npm_build_and_requires_artifacts(self, runner, _which):
        with tempfile.TemporaryDirectory() as d:
            root=Path(d)
            (root/"package.json").write_text("{}")
            project={"id":"x","repo_path":d,"actions":["build"]}
            runner.return_value={"ok":True,"code":0}
            result=execute_action(project,"build")
            self.assertEqual(runner.call_args.args[0],["npm","run","build"])
            self.assertFalse(result["ok"])
            self.assertEqual(result["status"],"not_ready")

    @patch("workspace.actions.shutil.which", return_value="/bin/npm")
    @patch("workspace.actions.run_command")
    def test_test_success_writes_verified_marker(self, runner, _which):
        with tempfile.TemporaryDirectory() as d:
            root=Path(d)
            (root/"package.json").write_text("{}")
            for name in ARTIFACTS: (root/name).write_text(name)
            project={"id":"x","repo_path":d,"actions":["test"]}
            runner.return_value={"ok":True,"code":0}
            result=execute_action(project,"test")
            self.assertTrue(result["ok"])
            self.assertEqual(result["verification"],"ready")
            self.assertTrue((root/".ai-workspace-verified.json").exists())

    def test_deploy_requires_verification_marker(self):
        with tempfile.TemporaryDirectory() as repo, tempfile.TemporaryDirectory() as vault:
            root=Path(repo)
            for name in ARTIFACTS: (root/name).write_text(name)
            project={"id":"x","repo_path":repo,"deploy_path":vault,"actions":["deploy"]}
            result=execute_action(project,"deploy")
            self.assertFalse(result["ok"])
            self.assertEqual(result["status"],"not_ready")
            self.assertEqual(list(Path(vault).iterdir()),[])

    def test_deploy_rejects_changed_artifact_after_verification(self):
        with tempfile.TemporaryDirectory() as repo, tempfile.TemporaryDirectory() as vault:
            root=Path(repo)
            for name in ARTIFACTS: (root/name).write_text(name)
            write_verification(root)
            (root/"main.js").write_text("changed-after-test")
            project={"id":"x","repo_path":repo,"deploy_path":vault,"actions":["deploy"]}
            result=execute_action(project,"deploy")
            self.assertFalse(result["ok"])
            self.assertEqual(result["status"],"not_ready")
            self.assertEqual(list(Path(vault).iterdir()),[])

    def test_deploy_copies_only_verified_allowlisted_artifacts(self):
        with tempfile.TemporaryDirectory() as repo, tempfile.TemporaryDirectory() as vault:
            root=Path(repo)
            for name in ARTIFACTS: (root/name).write_text(name)
            (root/"secret.txt").write_text("never deploy")
            write_verification(root)
            project={"id":"x","repo_path":repo,"deploy_path":vault,"actions":["deploy"]}
            result=execute_action(project,"deploy")
            self.assertTrue(result["ok"])
            self.assertEqual(result["deployed"],list(ARTIFACTS))
            self.assertEqual(sorted(p.name for p in Path(vault).iterdir()),sorted(ARTIFACTS))


if __name__ == "__main__":
    unittest.main()
