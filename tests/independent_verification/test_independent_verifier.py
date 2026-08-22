"""
PARSA INDEPENDENT VERIFIER & METAMORPHIC TEST SUITE (PHASE 14)
==============================================================
Validates production layer results against an independent mathematical reference model.
Uses metamorphic testing and invariant checks to prevent circular validation.
"""

import unittest
import math
import time
from parsa_layers.contracts.independent_verifier import IndependentForensicVerifier
from parsa_layers.judges.judge_engine import JudgeEngine, compute_wilson_confidence_interval_95, compute_cohens_h
from parsa_layers.contracts.models import (
    TestResult,
    compute_sha256,
    PredictionRecord,
    OutcomeRecord,
    FutureDataAccessViolation
)
from parsa_layers.test.outcome_policy import OutcomePolicy


class TestIndependentVerifier(unittest.TestCase):

    def test_independent_wilson_ci_agreement(self):
        """Cross-verifies Wilson CI between production JudgeEngine and IndependentForensicVerifier."""
        test_cases = [
            (50, 100),
            (15, 30),
            (80, 100),
            (1, 100),
            (99, 100),
            (0, 50),
            (50, 50)
        ]
        for k, n in test_cases:
            prod_low, prod_high = compute_wilson_confidence_interval_95(k, n)
            ind_low, ind_high = IndependentForensicVerifier.independent_wilson_ci(k, n)
            self.assertAlmostEqual(prod_low, ind_low, places=1, msg=f"Wilson lower mismatch for k={k}, n={n}")
            self.assertAlmostEqual(prod_high, ind_high, places=1, msg=f"Wilson upper mismatch for k={k}, n={n}")

    def test_independent_cohens_h_agreement(self):
        """Cross-verifies Cohen's h between production and independent implementation."""
        proportions = [0.50, 0.55, 0.60, 0.70, 0.85, 0.30]
        for p in proportions:
            prod_h = compute_cohens_h(p, 0.50)
            ind_h = IndependentForensicVerifier.independent_cohens_h(p, 0.50)
            self.assertAlmostEqual(prod_h, ind_h, places=3, msg=f"Cohen's h mismatch for p={p}")

    def test_independent_z_and_pvalue_agreement(self):
        """Cross-verifies z-statistic and p-value against independent implementation."""
        test_cases = [(65, 100), (55, 100), (80, 100), (25, 50)]
        for k, n in test_cases:
            ind_z, ind_p = IndependentForensicVerifier.independent_z_and_pvalue(k, n)
            
            # Construct mock test results to run production JudgeEngine
            results = [
                TestResult(
                    experiment_id="EXP-IND",
                    prediction_id=f"PRED-{i}",
                    evaluated_timestamp=1000.0 + i,
                    source="TEST",
                    version="3.0.0",
                    status="CORRECT" if i < k else "WRONG",
                    net_return_pct=1.0 if i < k else -1.0,
                    gross_return_pct=1.0 if i < k else -1.0,
                    friction_bps=15.0,
                    mfe_pct=2.0,
                    mae_pct=0.5
                )
                for i in range(n)
            ]
            judge = JudgeEngine("EXP-IND")
            verdict = judge.evaluate_test_population(results, num_hypotheses_tested=1)
            
            self.assertAlmostEqual(verdict.t_statistic, ind_z, places=1)
            self.assertAlmostEqual(verdict.p_value, ind_p, places=2)

    def test_metamorphic_directional_invariance(self):
        """
        Metamorphic Relation: Inverting price movement and direction simultaneously
        must yield identical return magnitudes and excursion metrics.
        """
        policy = OutcomePolicy(movement_threshold_pct=0.10)
        
        pred_long = PredictionRecord(
            experiment_id="EXP-META",
            prediction_id="P-LONG",
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
            input_data_hash="HASH_INPUT"
        )
        
        pred_short = PredictionRecord(
            experiment_id="EXP-META",
            prediction_id="P-SHORT",
            prediction_timestamp=1000.0,
            source="TEST",
            version="3.0.0",
            asset="BTCUSDT",
            timeframe="15m",
            horizon_seconds=900,
            maturity_timestamp=1900.0,
            direction="SHORT",
            predicted_range={"upper": 61000.0, "lower": 59000.0},
            model_identifiers=["M1"],
            input_data_hash="HASH_INPUT"
        )
        
        # Bullish candles: 60000 -> 61200 (+2%)
        candles_bull = [
            {"open_time": 1000.0, "close_time": 1900.0, "open": 60000.0, "high": 61500.0, "low": 59800.0, "close": 61200.0, "volume": 10.0}
        ]
        
        # Bearish candles: 60000 -> 58800 (-2%)
        candles_bear = [
            {"open_time": 1000.0, "close_time": 1900.0, "open": 60000.0, "high": 60200.0, "low": 58500.0, "close": 58800.0, "volume": 10.0}
        ]
        
        out_long, res_long = policy.evaluate("EXP-META", "3.0.0", pred_long, candles_bull, friction_bps=15.0, current_time=1900.0)
        out_short, res_short = policy.evaluate("EXP-META", "3.0.0", pred_short, candles_bear, friction_bps=15.0, current_time=1900.0)
        
        self.assertEqual(res_long.status, "CORRECT")
        self.assertEqual(res_short.status, "CORRECT")
        self.assertAlmostEqual(res_long.gross_return_pct, res_short.gross_return_pct, places=2)
        self.assertAlmostEqual(res_long.net_return_pct, res_short.net_return_pct, places=2)

    def test_avalanche_effect_on_hash(self):
        """Verifies that changing any single character in a payload completely alters the SHA-256 hash."""
        payload1 = {"experiment_id": "EXP-1", "timestamp": 1000.0, "asset": "BTCUSDT"}
        payload2 = {"experiment_id": "EXP-2", "timestamp": 1000.0, "asset": "BTCUSDT"}
        
        h1 = compute_sha256(payload1)
        h2 = compute_sha256(payload2)
        self.assertNotEqual(h1, h2)
        
        # Hamming distance on hex string should be substantial
        diff_chars = sum(1 for c1, c2 in zip(h1, h2) if c1 != c2)
        self.assertGreater(diff_chars, 30)


if __name__ == "__main__":
    unittest.main()
