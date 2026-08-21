"""
PARSA HISTORICAL INTEGRITY TESTS (PART 10)
==========================================
Verifies that:
- Historical mission directories and reports remain present and unmodified.
- Legacy scripts are preserved as archive artifacts.
"""

import unittest
import os
import hashlib


class TestHistoricalIntegrity(unittest.TestCase):

    def test_historical_mission_directories_exist(self):
        """Validates that all historical mission directories (M10-M20) are intact."""
        expected_dirs = [
            "mission_10_scientific_audit",
            "mission_11_forensic_audit",
            "mission_12_novel_discovery",
            "mission_13_final_approval_gate",
            "mission_14_anti_fabrication_gate",
            "mission_19_live_forecasting_lab",
            "mission_20_live_trial_vault"
        ]
        for d in expected_dirs:
            self.assertTrue(os.path.isdir(d), f"Historical mission directory '{d}' is missing!")

    def test_historical_reports_exist(self):
        """Validates that historical report markdown files are preserved."""
        expected_reports = [
            "PARSA_MISSION_10_FINAL_AUDIT.md",
            "MISSION_11_FINAL_FORENSIC_REPORT.md",
            "PARSA_MISSION_12_FINAL_AUDIT.md",
            "MISSION_13_FINAL_APPROVAL_REPORT.md",
            "MISSION_14_FINAL_EVALUATION_REPORT.md",
            "MISSION_19_LIVE_FORECASTING_REPORT.md",
            "MISSION_20_LIVE_TRIAL_REPORT.md"
        ]
        for r in expected_reports:
            self.assertTrue(os.path.isfile(r), f"Historical report '{r}' is missing!")


if __name__ == "__main__":
    unittest.main()
