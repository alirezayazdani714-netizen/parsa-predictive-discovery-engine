"""
PARSA JUDGES LAYER (LAYER 4)
============================
Statistical arbitration and hypothesis adjudication layer.
Computes significance tests, false discovery controls, and law classifications.

CRITICAL INVARIANTS:
1. Operates strictly on populations of TestResult instances.
2. Cannot generate predictions or modify raw test outcomes.
3. If sample size N < 30, classification MUST be INSUFFICIENT_SAMPLE. Real-money authorization is strictly False.
"""

from typing import List, Dict, Any, Optional
import math
import time
from parsa_layers.contracts.models import (
    TestResult,
    JudgeResult,
    UnauthorizedLayerAccessViolation,
    compute_sha256
)


class JudgeEngine:
    """Statistical Adjudication and Verdict Engine."""

    def __init__(self, experiment_id: str, version: str = "2.0.0"):
        self.experiment_id = experiment_id
        self.version = version

    def evaluate_test_population(
        self,
        test_results: List[TestResult],
        num_hypotheses_tested: int = 1,
        timestamp: Optional[float] = None
    ) -> JudgeResult:
        """
        Renders a mathematical verdict across a population of scored test results.
        Applies Bonferroni multi-testing correction.
        """
        eval_time = timestamp if timestamp is not None else time.time()
        sample_size = len(test_results)
        verdict_id = f"VERD-{self.experiment_id}-{int(eval_time)}"

        if sample_size == 0:
            return JudgeResult(
                experiment_id=self.experiment_id,
                verdict_id=verdict_id,
                timestamp=eval_time,
                source="JUDGE_ENGINE_V2",
                version=self.version,
                sample_size=0,
                correct_count=0,
                wrong_count=0,
                not_realized_count=0,
                win_rate_pct=0.0,
                t_statistic=0.0,
                p_value=1.0,
                bonferroni_threshold=0.05 / max(1, num_hypotheses_tested),
                is_statistically_significant=False,
                law_classification="INSUFFICIENT_SAMPLE",
                real_money_authorized=False,
                parent_hash="EMPTY"
            )

        correct = sum(1 for r in test_results if r.status == "CORRECT")
        wrong = sum(1 for r in test_results if r.status == "WRONG")
        not_realized = sum(1 for r in test_results if r.status == "PREDICTION_NOT_REALIZED")

        resolved = correct + wrong
        win_rate = (correct / resolved * 100.0) if resolved > 0 else 0.0

        # Binomial / t-statistic against 50% baseline
        p_null = 0.50
        if resolved >= 5:
            p_hat = correct / resolved
            se = math.sqrt(p_null * (1.0 - p_null) / resolved)
            z_stat = (p_hat - p_null) / se if se > 0 else 0.0
            # Approx 2-tailed p-value via normal approximation
            p_val = 2.0 * (1.0 - 0.5 * (1.0 + math.erf(abs(z_stat) / math.sqrt(2.0))))
        else:
            z_stat = 0.0
            p_val = 1.0

        bonferroni_thresh = 0.05 / max(1, num_hypotheses_tested)
        is_sig = (p_val < bonferroni_thresh) and (z_stat > 0)

        # Law classification logic
        if sample_size < 30 or resolved < 20:
            classification = "INSUFFICIENT_SAMPLE"
        elif is_sig and win_rate >= 60.0:
            classification = "CLASS_B"  # Candidate validated in sample
        elif win_rate > 52.0:
            classification = "CANDIDATE"
        else:
            classification = "REJECTED"

        # Parent hash from hashes of all input test results
        combined_parent_hash = compute_sha256([r.hash for r in test_results])

        return JudgeResult(
            experiment_id=self.experiment_id,
            verdict_id=verdict_id,
            timestamp=eval_time,
            source="JUDGE_ENGINE_V2",
            version=self.version,
            sample_size=sample_size,
            correct_count=correct,
            wrong_count=wrong,
            not_realized_count=not_realized,
            win_rate_pct=round(win_rate, 2),
            t_statistic=round(z_stat, 3),
            p_value=round(p_val, 6),
            bonferroni_threshold=round(bonferroni_thresh, 6),
            is_statistically_significant=is_sig,
            law_classification=classification,
            real_money_authorized=False,  # Strictly false in Phase 2
            parent_hash=combined_parent_hash
        )
