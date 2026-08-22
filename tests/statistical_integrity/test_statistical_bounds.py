"""
PARSA STATISTICAL INTEGRITY TEST SUITE (PHASE 8 & PHASE 10)
============================================================
Validates multiple-testing penalties (Bonferroni scaling), small sample protections,
and statistical edge cases in JudgeEngine.
"""

import unittest
from parsa_layers.judges.judge_engine import JudgeEngine
from parsa_layers.contracts.models import TestResult


class TestStatisticalBounds(unittest.TestCase):

    def setUp(self):
        self.judge = JudgeEngine(experiment_id="EXP-STAT-TEST")

    def _generate_test_results(self, n: int, win_rate: float) -> list:
        correct_count = int(round(n * win_rate))
        results = []
        for i in range(n):
            status = "CORRECT" if i < correct_count else "WRONG"
            results.append(
                TestResult(
                    experiment_id="EXP-STAT-TEST",
                    prediction_id=f"PRED-{i:04d}",
                    evaluated_timestamp=1700000000.0 + i,
                    source="TEST",
                    version="3.0.0",
                    status=status,
                    net_return_pct=1.5 if status == "CORRECT" else -1.5,
                    gross_return_pct=1.5 if status == "CORRECT" else -1.5,
                    friction_bps=15.0,
                    mfe_pct=2.0,
                    mae_pct=0.5
                )
            )
        return results

    def test_sample_size_below_30_is_insufficient_sample(self):
        """Proves that any sample size N < 30 is classified strictly as INSUFFICIENT_SAMPLE."""
        results = self._generate_test_results(n=25, win_rate=0.80)
        verdict = self.judge.evaluate_test_population(results, num_hypotheses_tested=1)
        self.assertEqual(verdict.law_classification, "INSUFFICIENT_SAMPLE")
        self.assertFalse(verdict.real_money_authorized)

    def test_bonferroni_correction_rejects_spurious_significance(self):
        """
        Proves that when 1,000 hypotheses are tested, an uncorrected p-value of 0.01
        is correctly rejected because 0.01 > (0.05 / 1000 = 0.00005).
        """
        # N=100 with 62% win rate: p-value is approx 0.016
        results = self._generate_test_results(n=100, win_rate=0.62)
        
        # 1 hypothesis: is significant at alpha=0.05
        verdict_single = self.judge.evaluate_test_population(results, num_hypotheses_tested=1)
        self.assertTrue(verdict_single.is_statistically_significant)
        self.assertEqual(verdict_single.law_classification, "CANDIDATE_EXPLORATORY")

        # 1,000 hypotheses: Bonferroni threshold = 0.00005 -> REJECTED
        verdict_multi = self.judge.evaluate_test_population(results, num_hypotheses_tested=1000)
        self.assertFalse(verdict_multi.is_statistically_significant)
        self.assertEqual(verdict_multi.law_classification, "NOT_SIGNIFICANT")
        self.assertAlmostEqual(verdict_multi.bonferroni_threshold, 0.00005, places=6)

    def test_real_money_authorized_is_always_false(self):
        """Proves real_money_authorized is strictly False across all parameter combinations."""
        for n in [30, 100, 500]:
            for wr in [0.50, 0.70, 0.95]:
                results = self._generate_test_results(n=n, win_rate=wr)
                verdict = self.judge.evaluate_test_population(results, num_hypotheses_tested=1)
                self.assertFalse(verdict.real_money_authorized)


if __name__ == "__main__":
    unittest.main()
