import unittest
from unittest.mock import patch

from workspace.gemma import analyze_failure


class GemmaTests(unittest.TestCase):
    @patch("workspace.gemma.urlopen")
    def test_analyze_failure_uses_exact_local_model_and_structured_prompt(self, urlopen):
        response = urlopen.return_value.__enter__.return_value
        response.read.return_value = b'{"choices":[{"message":{"content":"{\\"cause\\":\\"test failed\\",\\"action\\":\\"inspect log\\",\\"retry\\":false}"}}]}'

        result = analyze_failure("test", {"stdout": "FAILED", "stderr": "boom"})

        request = urlopen.call_args.args[0]
        body = request.data.decode("utf-8")
        self.assertIn('"model": "gemma-4-E4B-it.litertlm"', body)
        self.assertIn("cause", body)
        self.assertIn("action", body)
        self.assertIn("retry", body)
        self.assertTrue(result["ok"])
        self.assertEqual(result["analysis"]["cause"], "test failed")

    @patch("workspace.gemma.urlopen", side_effect=OSError("offline"))
    def test_gemma_failure_never_changes_pipeline_result(self, _urlopen):
        result = analyze_failure("build", {"stdout": "", "stderr": "compile error"})
        self.assertFalse(result["ok"])
        self.assertEqual(result["status"], "unavailable")


if __name__ == "__main__":
    unittest.main()
