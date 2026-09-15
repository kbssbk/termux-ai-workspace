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


if __name__ == "__main__":
    unittest.main()
