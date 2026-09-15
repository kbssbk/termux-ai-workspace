import tempfile
import unittest
from pathlib import Path

from workspace import actions, server

ROOT=Path(__file__).resolve().parents[1]


class StatusSemanticsTests(unittest.TestCase):
    def test_verification_ready_tracks_marker_and_current_artifacts(self):
        self.assertTrue(
            hasattr(actions, "verification_ready"),
            "verification_ready must expose real Build/Test verification state",
        )
        with tempfile.TemporaryDirectory() as d:
            root=Path(d)
            for name in actions.ARTIFACTS:
                (root/name).write_text(name)
            self.assertFalse(actions.verification_ready(root))
            actions.write_verification(root)
            self.assertTrue(actions.verification_ready(root))
            (root/"main.js").write_text("changed")
            self.assertFalse(actions.verification_ready(root))

    def test_project_payload_exposes_verification_without_repo_path(self):
        self.assertTrue(
            hasattr(server, "project_payload"),
            "project_payload must add verification state to the public project view",
        )
        with tempfile.TemporaryDirectory() as d:
            root=Path(d)
            for name in actions.ARTIFACTS:
                (root/name).write_text(name)
            project={
                "id":"x",
                "name":"X",
                "type":"obsidian-plugin",
                "repo_path":d,
                "actions":["build","test"],
            }
            before=server.project_payload(project)
            self.assertFalse(before["verified"])
            self.assertNotIn("repo_path", before)
            actions.write_verification(root)
            after=server.project_payload(project)
            self.assertTrue(after["verified"])
            self.assertNotIn("repo_path", after)

    def test_frontend_distinguishes_connectivity_from_project_verification(self):
        app=(ROOT/"web/app.js").read_text()
        i18n=(ROOT/"web/i18n.js").read_text()
        self.assertIn("p.verified", app)
        self.assertIn("systemConnected", app)
        self.assertIn("tr('connected')", app)
        self.assertIn('verified:"Build/Test 검증됨"', i18n)
        self.assertIn('needsVerification:"검증 필요"', i18n)


if __name__ == "__main__":
    unittest.main()
