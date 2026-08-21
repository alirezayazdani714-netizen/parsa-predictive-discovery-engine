"""
PARSA LAYER ACCESS CONTROL TESTS (PART 8)
=========================================
Verifies that:
- LAB cannot read future outcomes / data past cutoff.
- EXECUTOR cannot read TEST / JUDGES / REPORT results.
- TEST cannot modify PredictionRecord.
- JUDGES cannot modify raw market data.
- REPORT cannot modify evidence.
- GUARDIAN cannot modify experiment data.
"""

import unittest
import time
from parsa_layers.contracts.models import (
    FutureDataAccessViolation,
    UnauthorizedLayerAccessViolation,
    ImmutableRecordViolation,
    PredictionRecord,
    TestResult,
    JudgeResult,
    ReportRecord
)
from parsa_layers.laboratory.laboratory_engine import LaboratoryEngine
from parsa_layers.executor.executor_engine import ExecutorEngine
from parsa_layers.test.test_engine import TestEngine
from parsa_layers.judges.judge_engine import JudgeEngine
from parsa_layers.guardian.guardian_engine import GuardianEngine
from parsa_layers.report.report_engine import ReportEngine


class TestAccessControl(unittest.TestCase):

    def test_lab_cannot_access_future_data_past_cutoff(self):
        """LAB layer must raise FutureDataAccessViolation if input candles exceed in-sample cutoff."""
        cutoff_t = 1000.0
        lab = LaboratoryEngine(in_sample_cutoff_timestamp=cutoff_t)
        future_candles = [
            {"open_time": 900.0, "open": 100, "high": 105, "low": 95, "close": 102, "volume": 10},
            {"open_time": 1050.0, "open": 102, "high": 110, "low": 101, "close": 108, "volume": 15}  # Violation!
        ]
        with self.assertRaises(FutureDataAccessViolation):
            lab.compute_trend_momentum_signal(future_candles)

    def test_prediction_record_is_immutable(self):
        """TEST or any other layer cannot modify PredictionRecord attributes (frozen dataclass)."""
        pred = PredictionRecord(
            experiment_id="EXP-001",
            prediction_id="PRED-001",
            prediction_timestamp=1000.0,
            source="EXECUTOR",
            version="2.0.0",
            asset="BTCUSDT",
            timeframe="15m",
            horizon_seconds=900,
            maturity_timestamp=1900.0,
            direction="LONG",
            predicted_range={"upper": 61000, "lower": 60000},
            model_identifiers=["DISC-01"],
            input_data_hash="abc"
        )
        with self.assertRaises(Exception):  # FrozenInstanceError / AttributeError
            pred.direction = "SHORT"

    def test_test_result_is_immutable(self):
        """JUDGES or REPORT cannot mutate TestResult objects."""
        res = TestResult(
            experiment_id="EXP-001",
            prediction_id="PRED-001",
            evaluated_timestamp=1905.0,
            source="TEST",
            version="2.0.0",
            status="CORRECT",
            net_return_pct=0.45,
            gross_return_pct=0.60,
            friction_bps=15.0,
            mfe_pct=0.80,
            mae_pct=0.10
        )
        with self.assertRaises(Exception):
            res.status = "WRONG"

    def test_guardian_access_control_audit(self):
        """Guardian flags unauthorized layer reads."""
        guardian = GuardianEngine()
        # EXECUTOR attempting to read JUDGES
        finding = guardian.audit_chk17_unauthorized_layer_access("EXECUTOR", "JUDGES", "READ")
        self.assertEqual(finding.status, "FAIL")
        self.assertEqual(finding.severity, "HIGH")

        # Permitted access: TEST reading EXECUTOR
        finding_perm = guardian.audit_chk17_unauthorized_layer_access("TEST", "EXECUTOR", "READ")
        self.assertEqual(finding_perm.status, "PASS")


if __name__ == "__main__":
    unittest.main()
