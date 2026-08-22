"""
PARSA JUDGES LAYER STATISTICAL INTEGRITY TESTS (GATES 13, 14)
=============================================================
Verifies:
- GATE 13: Judge metrics, sample size N < 30 -> INSUFFICIENT_SAMPLE, real_money_authorized=False.
- GATE 14: Dynamic Bonferroni penalty based on number of hypotheses.
"""

import unittest
from parsa_layers.contracts.models import TestResult
from parsa_layers.judges.judge_engine import JudgeEngine


class TestJudgeEngineStatisticalIntegrity(unittest.TestCase):

    def setUp(self):
        self.judge = JudgeEngine("EXP-JUDGE-TEST")

    def test_gate13_insufficient_sample_size_below_30(self):
        """GATE 13: Sample size < 30 is strictly classified as INSUFFICIENT_SAMPLE."""
        # 20 correct results
        results = [
            TestResult("EXP", f"P{i}", 1905.0, "TEST", "3.0.0", "CORRECT", 1.0, 1.1, 10.0, 1.2, 0.1)
            for i in range(20)
        ]
        verdict = self.judge.evaluate_test_population(results, num_hypotheses_tested=1)
        self.assertEqual(verdict.law_classification, "INSUFFICIENT_SAMPLE")
        self.assertFalse(verdict.real_money_authorized)
        self.assertEqual(verdict.sample_size, 20)
        self.assertEqual(verdict.correct_count, 20)

    def test_gate14_dynamic_bonferroni_penalty(self):
        """GATE 14: Bonferroni threshold strictly equals 0.05 / num_hypotheses."""
        results = [
            TestResult("EXP", f"P{i}", 1905.0, "TEST", "3.0.0", "CORRECT", 1.0, 1.1, 10.0, 1.2, 0.1)
            for i in range(35)
        ]
        verdict_1 = self.judge.evaluate_test_population(results, num_hypotheses_tested=1)
        self.assertAlmostEqual(verdict_1.bonferroni_threshold, 0.05, places=5)

        verdict_10 = self.judge.evaluate_test_population(results, num_hypotheses_tested=10)
        self.assertAlmostEqual(verdict_10.bonferroni_threshold, 0.005, places=5)

        verdict_50 = self.judge.evaluate_test_population(results, num_hypotheses_tested=50)
        self.assertAlmostEqual(verdict_50.bonferroni_threshold, 0.001, places=5)


if __name__ == "__main__":
    unittest.main()
