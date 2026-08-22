"""
PARSA GUARDIAN PERSISTENT EVIDENCE SINK FORENSIC TESTS (BUG-01)
==============================================================
Verifies:
1. Append-only persistence to JSONL backend.
2. State recovery and chain verification after process reset.
3. Strict tamper detection (GUARDIAN_EVIDENCE_TAMPERED) upon:
   - Line deletion
   - Finding modification
   - Severity modification
   - Status modification
   - Timestamp modification
   - Parent hash modification
"""

import unittest
import os
import json
import tempfile
import shutil
from parsa_layers.guardian.guardian_engine import GuardianEngine
from parsa_layers.contracts.models import DataUnavailableError


class TestGuardianEvidenceSinkForensics(unittest.TestCase):

    def setUp(self):
        self.test_dir = tempfile.mkdtemp(prefix="parsa_sink_test_")
        self.sink_file = os.path.join(self.test_dir, "guardian_findings.jsonl")
        self.guardian = GuardianEngine(inspector_id="GUARDIAN_TEST", sink_path=self.sink_file)

    def tearDown(self):
        shutil.rmtree(self.test_dir, ignore_errors=True)

    def test_01_persistence_and_recovery_after_process_reset(self):
        """Creates findings, resets process state, reloads from disk and verifies integrity."""
        # 1. Emit 3 findings
        f1 = self.guardian.add_finding("CHK-01", "LOW", "feed.py", "func1", "None", "evidence1", "exp1", "act1", "PASS")
        f2 = self.guardian.add_finding("CHK-02", "HIGH", "feed.py", "func2", "Violation2", "evidence2", "exp2", "act2", "WARNING")
        f3 = self.guardian.add_finding("CHK-03", "CRITICAL", "feed.py", "func3", "Violation3", "evidence3", "exp3", "act3", "FAIL")

        # Verify disk file exists and has 3 lines
        self.assertTrue(os.path.exists(self.sink_file))
        with open(self.sink_file, "r") as f:
            lines = [l.strip() for l in f if l.strip()]
        self.assertEqual(len(lines), 3)

        # 2. Simulate complete process restart
        guardian_recovered = GuardianEngine(inspector_id="GUARDIAN_RECOVERED", sink_path=self.sink_file)
        self.assertEqual(len(guardian_recovered.findings), 3)

        # 3. Verify disk chain
        audit_res = guardian_recovered.verify_sink_file()
        self.assertEqual(audit_res.status, "PASS")

    def test_02_tamper_line_deletion(self):
        """Deleting a finding line from disk must cause verify to return GUARDIAN_EVIDENCE_TAMPERED."""
        for i in range(4):
            self.guardian.add_finding(f"CHK-{i:02d}", "LOW", "file", "func", "None", f"ev_{i}", "exp", "act", "PASS")

        # Read lines, delete line at index 1
        with open(self.sink_file, "r") as f:
            lines = [l for l in f if l.strip()]
        
        tampered_lines = [lines[0], lines[2], lines[3]]
        with open(self.sink_file, "w") as f:
            f.writelines(tampered_lines)

        guardian_checker = GuardianEngine(inspector_id="AUDITOR", sink_path=self.sink_file)
        audit_res = guardian_checker.verify_sink_file()
        self.assertEqual(audit_res.status, "INVALID")
        self.assertEqual(audit_res.severity, "CRITICAL")
        self.assertEqual(audit_res.violation, "GUARDIAN_EVIDENCE_TAMPERED")

    def test_03_tamper_finding_modification(self):
        """Modifying finding evidence or payload must cause verify to fail."""
        self.guardian.add_finding("CHK-01", "LOW", "file", "func", "None", "original_evidence", "exp", "act", "PASS")
        self.guardian.add_finding("CHK-02", "LOW", "file", "func", "None", "evidence_2", "exp", "act", "PASS")

        with open(self.sink_file, "r") as f:
            lines = [json.loads(l) for l in f if l.strip()]

        # Tamper evidence text in entry 0
        lines[0]["evidence"] = "tampered_fake_evidence"
        with open(self.sink_file, "w") as f:
            for item in lines:
                f.write(json.dumps(item) + "\n")

        guardian_checker = GuardianEngine(inspector_id="AUDITOR", sink_path=self.sink_file)
        audit_res = guardian_checker.verify_sink_file()
        self.assertEqual(audit_res.status, "INVALID")
        self.assertEqual(audit_res.violation, "GUARDIAN_EVIDENCE_TAMPERED")

    def test_04_tamper_severity_modification(self):
        """Modifying severity from CRITICAL to LOW in disk file must fail."""
        self.guardian.add_finding("CHK-01", "CRITICAL", "file", "func", "Violation", "ev", "exp", "act", "INVALID")
        self.guardian.add_finding("CHK-02", "LOW", "file", "func", "None", "ev", "exp", "act", "PASS")

        with open(self.sink_file, "r") as f:
            lines = [json.loads(l) for l in f if l.strip()]

        lines[0]["severity"] = "LOW"
        with open(self.sink_file, "w") as f:
            for item in lines:
                f.write(json.dumps(item) + "\n")

        guardian_checker = GuardianEngine(inspector_id="AUDITOR", sink_path=self.sink_file)
        audit_res = guardian_checker.verify_sink_file()
        self.assertEqual(audit_res.status, "INVALID")
        self.assertEqual(audit_res.violation, "GUARDIAN_EVIDENCE_TAMPERED")

    def test_05_tamper_status_modification(self):
        """Modifying status from FAIL to PASS in disk file must fail."""
        self.guardian.add_finding("CHK-01", "CRITICAL", "file", "func", "Violation", "ev", "exp", "act", "FAIL")

        with open(self.sink_file, "r") as f:
            lines = [json.loads(l) for l in f if l.strip()]

        lines[0]["status"] = "PASS"
        with open(self.sink_file, "w") as f:
            for item in lines:
                f.write(json.dumps(item) + "\n")

        guardian_checker = GuardianEngine(inspector_id="AUDITOR", sink_path=self.sink_file)
        audit_res = guardian_checker.verify_sink_file()
        self.assertEqual(audit_res.status, "INVALID")
        self.assertEqual(audit_res.violation, "GUARDIAN_EVIDENCE_TAMPERED")

    def test_06_tamper_timestamp_modification(self):
        """Modifying timestamp in disk file must fail."""
        self.guardian.add_finding("CHK-01", "LOW", "file", "func", "None", "ev", "exp", "act", "PASS")

        with open(self.sink_file, "r") as f:
            lines = [json.loads(l) for l in f if l.strip()]

        lines[0]["timestamp"] = lines[0]["timestamp"] + 100.0
        with open(self.sink_file, "w") as f:
            for item in lines:
                f.write(json.dumps(item) + "\n")

        guardian_checker = GuardianEngine(inspector_id="AUDITOR", sink_path=self.sink_file)
        audit_res = guardian_checker.verify_sink_file()
        self.assertEqual(audit_res.status, "INVALID")
        self.assertEqual(audit_res.violation, "GUARDIAN_EVIDENCE_TAMPERED")

    def test_07_tamper_parent_hash_modification(self):
        """Modifying parent_hash in disk file must fail."""
        self.guardian.add_finding("CHK-01", "LOW", "file", "func", "None", "ev", "exp", "act", "PASS")
        self.guardian.add_finding("CHK-02", "LOW", "file", "func", "None", "ev", "exp", "act", "PASS")

        with open(self.sink_file, "r") as f:
            lines = [json.loads(l) for l in f if l.strip()]

        lines[1]["parent_hash"] = "0" * 64
        with open(self.sink_file, "w") as f:
            for item in lines:
                f.write(json.dumps(item) + "\n")

        guardian_checker = GuardianEngine(inspector_id="AUDITOR", sink_path=self.sink_file)
        audit_res = guardian_checker.verify_sink_file()
        self.assertEqual(audit_res.status, "INVALID")
        self.assertEqual(audit_res.violation, "GUARDIAN_EVIDENCE_TAMPERED")

    def test_08_clear_findings_does_not_destroy_persistent_sink(self):
        """clear_findings() only clears in-memory buffer, persistent file remains intact."""
        self.guardian.add_finding("CHK-01", "LOW", "file", "func", "None", "ev", "exp", "act", "PASS")
        self.assertEqual(len(self.guardian.findings), 1)

        # Clear in-memory
        self.guardian.clear_findings()
        self.assertEqual(len(self.guardian.findings), 0)

        # Verify disk file still contains the record
        with open(self.sink_file, "r") as f:
            lines = [l for l in f if l.strip()]
        self.assertEqual(len(lines), 1)


if __name__ == "__main__":
    unittest.main()
