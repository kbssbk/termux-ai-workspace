import unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
class FrontendTests(unittest.TestCase):
 def test_mobile_and_i18n(self):
  h=(ROOT/"web/index.html").read_text();i=(ROOT/"web/i18n.js").read_text();self.assertIn('name="viewport"',h);self.assertIn("ko:{",i);self.assertIn("en:{",i)
 def test_touch_target(self):self.assertIn("min-height:44px",(ROOT/"web/styles.css").read_text())
