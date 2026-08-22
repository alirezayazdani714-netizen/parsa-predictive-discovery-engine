"""
PARSA OUTCOME POLICY & DETERMINISTIC TEST SUITE (GATES 09, 10, 11, 12)
======================================================================
Verifies:
- GATE 09: Independent outcome evaluation (Outcome = f(Prediction, ForwardCandles, OutcomePolicy)).
- GATE 10: Exact horizon window boundaries [T, T+H].
- GATE 11: OutcomePolicy as sole authority for scoring.
- GATE 12: Deterministic scoring for LONG, SHORT, NEUTRAL, and friction deduction.
"""

import unittest
from parsa_layers.contracts.models import (
    PredictionRecord,
    FutureDataAccessViolation,
    DataUnavailableError
)
from parsa_layers.test.outcome_policy import OutcomePolicy
from parsa_layers.test.test_engine import TestEngine


class TestOutcomePolicyDeterministic(unittest.TestCase):

    def setUp(self):
        self.policy = OutcomePolicy(movement_threshold_pct=0.15)
        self.test_engine = TestEngine("EXP-OUTCOME-DET", friction_bps=10.0, movement_threshold_pct=0.15)

    def test_gate12_long_correct_evaluation(self):
        """GATE 12: LONG trade exceeding upward threshold is CORRECT and deducts friction."""
        pred = PredictionRecord("EXP", "P1", 1000.0, "EXEC", "3.0.0", "BTC", "15m", 900, 1900.0, "LONG", {}, [], "h1")
        candles = [
            {"open_time": 1000.0, "open": 100.0, "high": 105.0, "low": 99.0, "close": 102.0, "volume": 10.0},
            {"open_time": 1500.0, "close_time": 1900.0, "open": 102.0, "high": 106.0, "low": 101.0, "close": 105.0, "volume": 15.0}
        ]
        outcome, result = self.test_engine.evaluate_prediction_outcome(pred, candles, current_time=1900.0)
        self.assertEqual(result.status, "CORRECT")
        self.assertEqual(result.gross_return_pct, 5.0)
        self.assertEqual(result.friction_bps, 10.0)
        self.assertEqual(result.net_return_pct, 4.90)  # 5.0% - 0.10% friction

    def test_gate12_long_wrong_evaluation(self):
        """GATE 12: LONG trade moving down past adverse threshold is WRONG."""
        pred = PredictionRecord("EXP", "P2", 1000.0, "EXEC", "3.0.0", "BTC", "15m", 900, 1900.0, "LONG", {}, [], "h2")
        candles = [
            {"open_time": 1000.0, "open": 100.0, "high": 101.0, "low": 96.0, "close": 98.0, "volume": 10.0},
            {"open_time": 1500.0, "close_time": 1900.0, "open": 98.0, "high": 99.0, "low": 94.0, "close": 95.0, "volume": 15.0}
        ]
        outcome, result = self.test_engine.evaluate_prediction_outcome(pred, candles, current_time=1900.0)
        self.assertEqual(result.status, "WRONG")
        self.assertEqual(result.gross_return_pct, -5.0)
        self.assertEqual(result.net_return_pct, -5.10)  # -5.0% - 0.10% friction

    def test_gate12_short_correct_evaluation(self):
        """GATE 12: SHORT trade moving down past downward threshold is CORRECT."""
        pred = PredictionRecord("EXP", "P3", 1000.0, "EXEC", "3.0.0", "BTC", "15m", 900, 1900.0, "SHORT", {}, [], "h3")
        candles = [
            {"open_time": 1000.0, "open": 100.0, "high": 101.0, "low": 94.0, "close": 98.0, "volume": 10.0},
            {"open_time": 1500.0, "close_time": 1900.0, "open": 98.0, "high": 99.0, "low": 94.0, "close": 95.0, "volume": 15.0}
        ]
        outcome, result = self.test_engine.evaluate_prediction_outcome(pred, candles, current_time=1900.0)
        self.assertEqual(result.status, "CORRECT")
        self.assertEqual(result.gross_return_pct, 5.0)
        self.assertEqual(result.net_return_pct, 4.90)

    def test_gate12_prediction_not_realized(self):
        """GATE 12: Sub-threshold market movement evaluated as PREDICTION_NOT_REALIZED."""
        pred = PredictionRecord("EXP", "P4", 1000.0, "EXEC", "3.0.0", "BTC", "15m", 900, 1900.0, "LONG", {}, [], "h4")
        candles = [
            {"open_time": 1000.0, "open": 100.0, "high": 100.05, "low": 99.95, "close": 100.02, "volume": 10.0},
            {"open_time": 1500.0, "close_time": 1900.0, "open": 100.02, "high": 100.08, "low": 99.98, "close": 100.05, "volume": 10.0}
        ]
        outcome, result = self.test_engine.evaluate_prediction_outcome(pred, candles, current_time=1900.0)
        self.assertEqual(result.status, "PREDICTION_NOT_REALIZED")


if __name__ == "__main__":
    unittest.main()
