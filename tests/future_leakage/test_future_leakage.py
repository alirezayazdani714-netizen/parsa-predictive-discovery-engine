"""
PARSA FUTURE LEAKAGE RUNTIME GUARD TESTS (PART 3)
=================================================
Verifies that:
- MarketDataSnapshot constructor rejects future candles with FutureDataAccessViolation.
- TestEngine.evaluate_prediction_outcome rejects evaluations before maturity.
- Hard runtime guards fail closed.
"""

import unittest
from parsa_layers.contracts.models import (
    FutureDataAccessViolation,
    MarketDataSnapshot,
    PredictionRecord
)
from parsa_layers.test.test_engine import TestEngine


class TestFutureLeakage(unittest.TestCase):

    def test_snapshot_rejects_future_candles(self):
        """MarketDataSnapshot must fail closed if any candle is in the future."""
        current_t = 1000.0  # seconds
        future_candle = {"open_time": 1050000, "open": 100, "high": 105, "low": 95, "close": 100, "volume": 10}  # 1050s in ms
        with self.assertRaises(FutureDataAccessViolation):
            MarketDataSnapshot(
                experiment_id="EXP-FL-1",
                timestamp=current_t,
                source="TEST",
                asset="BTCUSDT",
                timeframe="15m",
                candles=[future_candle]
            )

    def test_test_engine_rejects_premature_scoring(self):
        """Test engine rejects scoring when current_time < maturity_timestamp."""
        test_engine = TestEngine("EXP-FL-2")
        pred = PredictionRecord("EXP", "P1", 1000, "EXEC", "2.0.0", "BTC", "15m", 900, 1900, "LONG", {}, [], "h1")
        forward_candles = [{"open_time": 1000, "open": 100, "high": 105, "low": 95, "close": 102, "volume": 10}]
        # Attempt to score at t=1800 (100 seconds before maturity at t=1900)
        with self.assertRaises(FutureDataAccessViolation):
            test_engine.evaluate_prediction_outcome(pred, forward_candles, current_time=1800.0)

    def test_exact_horizon_enforcement_future_candle_rejected(self):
        """BUG-01: Reject forward candles whose timestamps exceed horizon maturity window."""
        test_engine = TestEngine("EXP-FL-3")
        pred = PredictionRecord("EXP", "P1", 1000, "EXEC", "2.0.0", "BTC", "15m", 60, 1060, "LONG", {}, [], "h1")
        # Candle is at t=1070 (past maturity t=1060)
        forward_candles_future = [
            {"open_time": 1010, "open": 100, "high": 105, "low": 95, "close": 102, "volume": 10},
            {"open_time": 1070, "open": 102, "high": 108, "low": 101, "close": 107, "volume": 15}
        ]
        with self.assertRaises(FutureDataAccessViolation):
            test_engine.evaluate_prediction_outcome(pred, forward_candles_future, current_time=2000.0)

    def test_exact_horizon_enforcement_past_candle_rejected(self):
        """BUG-01: Reject forward candles whose timestamps are prior to prediction start timestamp."""
        test_engine = TestEngine("EXP-FL-4")
        pred = PredictionRecord("EXP", "P1", 1000, "EXEC", "2.0.0", "BTC", "15m", 60, 1060, "LONG", {}, [], "h1")
        # Candle is at t=950 (before prediction timestamp t=1000)
        from parsa_layers.contracts.models import DataUnavailableError
        forward_candles_past = [
            {"open_time": 950, "open": 100, "high": 105, "low": 95, "close": 102, "volume": 10}
        ]
        with self.assertRaises(DataUnavailableError):
            test_engine.evaluate_prediction_outcome(pred, forward_candles_past, current_time=2000.0)

    def test_exact_horizon_enforcement_valid_window_succeeds(self):
        """BUG-01: Valid forward candles strictly within [prediction_timestamp, maturity_timestamp] evaluate correctly."""
        test_engine = TestEngine("EXP-FL-5", friction_bps=10.0)
        pred = PredictionRecord("EXP", "P1", 1000, "EXEC", "2.0.0", "BTC", "15m", 60, 1060, "LONG", {}, [], "h1")
        valid_candles = [
            {"open_time": 1000, "open": 100.0, "high": 105.0, "low": 98.0, "close": 103.0, "volume": 100.0},
            {"open_time": 1030, "close_time": 1060, "open": 103.0, "high": 106.0, "low": 102.0, "close": 105.0, "volume": 150.0}
        ]
        outcome, result = test_engine.evaluate_prediction_outcome(pred, valid_candles, current_time=1060.0)
        self.assertEqual(outcome.entry_price, 100.0)
        self.assertEqual(outcome.exit_price, 105.0)
        self.assertEqual(result.status, "CORRECT")
        self.assertEqual(result.gross_return_pct, 5.0)
        self.assertEqual(result.friction_bps, 10.0)
        self.assertEqual(result.net_return_pct, 4.90)


if __name__ == "__main__":
    unittest.main()
