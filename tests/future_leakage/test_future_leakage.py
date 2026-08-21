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
        forward_candles = [{"open": 100, "high": 105, "low": 95, "close": 102, "volume": 10}]
        # Attempt to score at t=1800 (100 seconds before maturity at t=1900)
        with self.assertRaises(FutureDataAccessViolation):
            test_engine.evaluate_prediction_outcome(pred, forward_candles, current_time=1800.0)


if __name__ == "__main__":
    unittest.main()
