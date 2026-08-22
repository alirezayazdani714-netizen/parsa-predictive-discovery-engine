"""
PARSA GUARDIAN SINK FAIL-CLOSED TEST SUITE (PHASE 3 & PHASE 7)
==============================================================
Proves that corrupted or tampered sink files on disk raise explicit DataUnavailableError
or Guardian INVALID findings instead of silently continuing or swallowing exceptions.
"""

import unittest
import tempfile
import os
import json
import time
from parsa_layers.guardian.guardian_engine import GuardianEngine
from parsa_layers.contracts.models import DataUnavailableError


class TestGuardianSinkFailClosed(unittest.TestCase):

    def test_corrupted_json_lines_fail_closed_on_startup(self):
        """Proves that unparseable JSON in persistent sink raises DataUnavailableError on initialization."""
        with tempfile.NamedTemporaryFile(mode="w", delete=False, suffix=".jsonl") as tf:
            tf.write('{"finding_id": "GF-01", "index": 0}\n')
            tf.write('CORRUPTED_NON_JSON_LINE\n')
            tf_path = tf.name

        try:
            with self.assertRaises(DataUnavailableError):
                GuardianEngine(inspector_id="TEST_CRASH", sink_path=tf_path)
        finally:
            if os.path.exists(tf_path):
                os.remove(tf_path)

    def test_verify_sink_file_catches_tampered_json(self):
        """Proves that verify_sink_file identifies tampered JSON lines."""
        with tempfile.NamedTemporaryFile(mode="w", delete=False, suffix=".jsonl") as tf:
            tf.write('{"finding_id": "GF-01", "index": 0, "finding_hash": "abc"}\n')
            tf.write('BROKEN_LINE\n')
            tf_path = tf.name

        try:
            guardian = GuardianEngine(inspector_id="TEST_VERIFY", sink_path=None)
            finding = guardian.verify_sink_file(tf_path)
            self.assertEqual(finding.status, "INVALID")
            self.assertEqual(finding.severity, "CRITICAL")
            self.assertEqual(finding.violation, "GUARDIAN_EVIDENCE_TAMPERED")
        finally:
            if os.path.exists(tf_path):
                os.remove(tf_path)

    def test_verify_sink_file_catches_deleted_record_gap(self):
        """Proves that deleting a line in the JSONL creates an index gap that Guardian flags."""
        guardian = GuardianEngine(inspector_id="TEST_GAP", sink_path=None)
        
        from parsa_layers.contracts.models import GuardianFinding
        
        f0 = GuardianFinding(
            finding_id="GF-00",
            check_id="CHK-01",
            severity="LOW",
            timestamp=1000.0,
            file="test",
            line_or_function="fn",
            violation="None",
            evidence="ok",
            expected_behavior="ok",
            actual_behavior="ok",
            status="PASS",
            parent_hash="GENESIS_GUARDIAN_ROOT"
        )
        
        entry0 = {
            "index": 0,
            "finding_id": f0.finding_id,
            "check_id": f0.check_id,
            "severity": f0.severity,
            "timestamp": f0.timestamp,
            "file": f0.file,
            "line_or_function": f0.line_or_function,
            "violation": f0.violation,
            "evidence": f0.evidence,
            "expected_behavior": f0.expected_behavior,
            "actual_behavior": f0.actual_behavior,
            "status": f0.status,
            "parent_hash": f0.parent_hash,
            "previous_finding_hash": "GENESIS_GUARDIAN_ROOT",
            "finding_hash": f0.hash
        }
        entry2 = {
            "index": 2,  # GAP: index 1 deleted
            "finding_id": "GF-02",
            "check_id": "CHK-02",
            "severity": "LOW",
            "timestamp": 1001.0,
            "file": "test",
            "line_or_function": "fn",
            "violation": "None",
            "evidence": "ok",
            "expected_behavior": "ok",
            "actual_behavior": "ok",
            "status": "PASS",
            "parent_hash": f0.hash,
            "previous_finding_hash": f0.hash,
            "finding_hash": "HASH_2"
        }
        
        with tempfile.NamedTemporaryFile(mode="w", delete=False, suffix=".jsonl") as tf:
            tf.write(json.dumps(entry0) + "\n")
            tf.write(json.dumps(entry2) + "\n")
            tf_path = tf.name

        try:
            finding = guardian.verify_sink_file(tf_path)
            self.assertEqual(finding.status, "INVALID")
            self.assertEqual(finding.violation, "GUARDIAN_EVIDENCE_TAMPERED")
            self.assertIn("index mismatch", finding.evidence)
        finally:
            if os.path.exists(tf_path):
                os.remove(tf_path)


if __name__ == "__main__":
    unittest.main()
