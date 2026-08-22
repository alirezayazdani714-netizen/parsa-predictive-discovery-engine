"""
PARSA EXACT TEMPORAL BOUNDS TEST SUITE (PHASE 4 & PHASE 8)
==========================================================
Tests sub-millisecond precision temporal boundaries, proving that even a 0.001s
look-ahead is strictly rejected and cannot leak into predictions or evaluations.
"""

import unittest
import time
from parsa_layers.contracts.models import (
    MarketDataSnapshot,
    PredictionRecord,
    FutureDataAccessViolation,
    DataUnavailableError
)
from parsa_layers.executor.executor_engine import ExecutorEngine
from parsa_layers.test.outcome_policy import OutcomePolicy
from parsa_layers.guardian.guardian_engine import GuardianEngine


class TestExactTemporalBounds(unittest.TestCase):

    def test_snapshot_rejects_submillisecond_future_candle(self):
        """Proves that a candle at T + 0.001s is strictly rejected upon snapshot creation."""
        base_t = 1700000000.0
        future_candle = {
            "open_time": base_t + 0.001,
            "open": 60000.0,
            "high": 60100.0,
            "low": 59900.0,
            "close": 60050.0,
            "volume": 10.0
        }
        with self.assertRaises(FutureDataAccessViolation):
            MarketDataSnapshot(
                experiment_id="EXP-TIME",
                timestamp=base_t,
                source="TEST",
                asset="BTCUSDT",
                timeframe="15m",
                candles=[future_candle]
            )

    def test_executor_ingest_rejects_future_candle_zero_tolerance(self):
        """Proves ExecutorEngine rejects forward candles past allowed timestamp."""
        executor = ExecutorEngine(experiment_id="EXP-TIME")
        base_t = 1700000000.0
        future_candles = [
            {"open_time": base_t + 0.01, "open": 60000.0, "high": 60100.0, "low": 59900.0, "close": 60050.0, "volume": 10.0}
        ]
        with self.assertRaises(FutureDataAccessViolation):
            executor.ingest_bounded_candles("BTCUSDT", future_candles, interval="15m", allowed_timestamp=base_t)

    def test_outcome_policy_rejects_post_horizon_candle_zero_tolerance(self):
        """Proves OutcomePolicy rejects forward candles with open_time > window_end."""
        policy = OutcomePolicy(movement_threshold_pct=0.10)
        prediction = PredictionRecord(
            experiment_id="EXP-TIME",
            prediction_id="PRED-T1",
            prediction_timestamp=1000.0,
            source="TEST",
            version="3.0.0",
            asset="BTCUSDT",
            timeframe="15m",
            horizon_seconds=900,
            maturity_timestamp=1900.0,
            direction="LONG",
            predicted_range={"upper": 61000.0, "lower": 59000.0},
            model_identifiers=["M1"],
            input_data_hash="HASH"
        )
        post_horizon_candles = [
            {"open_time": 1900.001, "close_time": 2800.0, "open": 60000.0, "high": 60100.0, "low": 59900.0, "close": 60050.0, "volume": 10.0}
        ]
        with self.assertRaises(FutureDataAccessViolation):
            policy.evaluate("EXP-TIME", "3.0.0", prediction, post_horizon_candles, friction_bps=15.0, current_time=2800.0)

    def test_guardian_chk01_detects_future_candle_zero_tolerance(self):
        """Proves Guardian CHK-01 flags any candle with timestamp > prediction_timestamp."""
        guardian = GuardianEngine(inspector_id="TEST_GUARDIAN", sink_path=None)
        pred_t = 1000.0
        
        # Valid snapshot at T=1000.0
        snap_valid = MarketDataSnapshot(
            experiment_id="EXP-G",
            timestamp=pred_t,
            source="TEST",
            asset="BTCUSDT",
            timeframe="15m",
            candles=[{"open_time": 1000.0, "open": 60000.0, "high": 60100.0, "low": 59900.0, "close": 60050.0, "volume": 10.0}]
        )
        finding_valid = guardian.audit_chk01_future_data_leakage(snap_valid, prediction_timestamp=pred_t)
        self.assertEqual(finding_valid.status, "PASS")

        # Snapshot at T=1005.0 checked against an earlier prediction timestamp T=1000.0
        snap_future = MarketDataSnapshot(
            experiment_id="EXP-G",
            timestamp=1005.0,
            source="TEST",
            asset="BTCUSDT",
            timeframe="15m",
            candles=[{"open_time": 1005.0, "open": 60000.0, "high": 60100.0, "low": 59900.0, "close": 60050.0, "volume": 10.0}]
        )
        finding_future = guardian.audit_chk01_future_data_leakage(snap_future, prediction_timestamp=pred_t)
        self.assertEqual(finding_future.status, "INVALID")
        self.assertEqual(finding_future.severity, "CRITICAL")


if __name__ == "__main__":
    unittest.main()
