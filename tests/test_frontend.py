import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]

class FrontendTests(unittest.TestCase):
 def test_mobile_and_i18n(self):
  h=(ROOT/"web/index.html").read_text();i=(ROOT/"web/i18n.js").read_text()
  self.assertIn('name="viewport"',h);self.assertIn("ko:{",i);self.assertIn("en:{",i)

 def test_touch_target(self):
  self.assertIn("min-height:44px",(ROOT/"web/styles.css").read_text())

 def test_v2_has_human_result_and_progress_ui(self):
  h=(ROOT/"web/index.html").read_text();j=(ROOT/"web/app.js").read_text();i=(ROOT/"web/i18n.js").read_text()
  self.assertIn('id="toast"',h)
  self.assertIn('class="action-progress"',h)
  self.assertIn("humanResult",j)
  self.assertIn("alreadyCurrent",i)
  self.assertIn("running",i)

 def test_v2_exposes_details_only_as_secondary_ui(self):
  h=(ROOT/"web/index.html").read_text();i=(ROOT/"web/i18n.js").read_text()
  self.assertIn('id="details"',h)
  self.assertIn("showDetails",i)
  self.assertNotIn('<pre id="resultBody"',h)
