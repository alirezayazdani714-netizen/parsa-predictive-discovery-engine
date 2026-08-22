"""
PARSA JUDGES LAYER (LAYER 4 - PHASE 3 HARDENED)
===============================================
Statistical arbitration and hypothesis adjudication layer.
Computes significance tests, Wilson confidence intervals, effect sizes,
Bonferroni false discovery controls, and rigorous scientific classifications.

CRITICAL INVARIANTS:
1. Operates strictly on populations of TestResult instances.
2. Cannot generate predictions or modify raw test outcomes.
3. If sample size N < 30, classification MUST be INSUFFICIENT_SAMPLE.
4. Real-money authorization is strictly FALSE (Phase 3 Hardened).
5. Bonferroni threshold strictly scaled by declared hypothesis count.
"""

from typing import List, Dict, Any, Optional, Tuple
import math
import time
from parsa_layers.contracts.models import (
    TestResult,
    JudgeResult,
    UnauthorizedLayerAccessViolation,
    compute_sha256
)
from parsa_layers.contracts.access_control import enforce_layer


def compute_wilson_confidence_interval_95(correct: int, total: int) -> Tuple[float, float]:
    """
    Computes 95% Wilson Score Interval for binomial proportion.
    Returns (ci_lower_pct, ci_upper_pct).
    """
    if total <= 0:
        return 0.0, 0.0

    p_hat = correct / total
    z = 1.95996  # 95% standard normal quantile
    z2 = z * z
    n = float(total)

    denominator = 1.0 + z2 / n
    center = (p_hat + z2 / (2.0 * n)) / denominator
    spread = (z * math.sqrt((p_hat * (1.0 - p_hat) / n) + (z2 / (4.0 * n * n)))) / denominator

    lower = max(0.0, center - spread) * 100.0
    upper = min(1.0, center + spread) * 100.0
    return round(lower, 2), round(upper, 2)


def compute_cohens_h(p_hat: float, p_null: float = 0.50) -> float:
    """
    Computes Cohen's h effect size: 2 * arcsin(sqrt(p1)) - 2 * arcsin(sqrt(p2)).
    """
    p_hat_clamped = max(0.0, min(1.0, p_hat))
    p_null_clamped = max(0.0, min(1.0, p_null))
    phi1 = 2.0 * math.asin(math.sqrt(p_hat_clamped))
    phi2 = 2.0 * math.asin(math.sqrt(p_null_clamped))
    return round(phi1 - phi2, 4)


class JudgeEngine:
    """Statistical Adjudication and Verdict Engine."""

    def __init__(self, experiment_id: str, version: str = "3.0.0"):
        self.experiment_id = experiment_id
        self.version = version

    @enforce_layer("JUDGES")
    def evaluate_test_population(
        self,
        test_results: List[TestResult],
        num_hypotheses_tested: int = 1,
        timestamp: Optional[float] = None
    ) -> JudgeResult:
        """
        Renders a mathematical verdict across a population of scored test results.
        Applies Bonferroni multi-testing correction, Wilson 95% CI, and Cohen's h.
        """
        eval_time = timestamp if timestamp is not None else time.time()
        sample_size = len(test_results)
        verdict_id = f"VERD-{self.experiment_id}-{int(eval_time)}"
        bonferroni_thresh = 0.05 / max(1, num_hypotheses_tested)

        if sample_size == 0:
            return JudgeResult(
                experiment_id=self.experiment_id,
                verdict_id=verdict_id,
                timestamp=eval_time,
                source="JUDGE_ENGINE_V3",
                version=self.version,
                sample_size=0,
                correct_count=0,
                wrong_count=0,
                not_realized_count=0,
                win_rate_pct=0.0,
                t_statistic=0.0,
                p_value=1.0,
                bonferroni_threshold=round(bonferroni_thresh, 6),
                is_statistically_significant=False,
                law_classification="INSUFFICIENT_SAMPLE",
                real_money_authorized=False,
                confidence_interval_95=(0.0, 0.0),
                effect_size=0.0,
                parent_hash="EMPTY"
            )

        correct = sum(1 for r in test_results if r.status == "CORRECT")
        wrong = sum(1 for r in test_results if r.status == "WRONG")
        not_realized = sum(1 for r in test_results if r.status == "PREDICTION_NOT_REALIZED")

        resolved = correct + wrong
        win_rate = (correct / resolved * 100.0) if resolved > 0 else 0.0
        p_hat = (correct / resolved) if resolved > 0 else 0.50

        # Binomial / z-statistic against 50% baseline
        p_null = 0.50
        if resolved >= 5:
            se = math.sqrt(p_null * (1.0 - p_null) / resolved)
            z_stat = (p_hat - p_null) / se if se > 0 else 0.0
            # 2-tailed p-value via normal approximation
            p_val = 2.0 * (1.0 - 0.5 * (1.0 + math.erf(abs(z_stat) / math.sqrt(2.0))))
        else:
            z_stat = 0.0
            p_val = 1.0

        is_sig = (p_val < bonferroni_thresh) and (z_stat > 0)
        ci_lower, ci_upper = compute_wilson_confidence_interval_95(correct, resolved)
        effect_sz = compute_cohens_h(p_hat, p_null)

        # Rigorous law classification logic (Phase 3 Hardened: No Class A/B commercial claims)
        if sample_size < 30 or resolved < 20:
            classification = "INSUFFICIENT_SAMPLE"
        elif is_sig and win_rate >= 55.0:
            classification = "CANDIDATE_EXPLORATORY"
        elif win_rate > 50.0:
            classification = "NOT_SIGNIFICANT"
        else:
            classification = "REJECTED"

        # Cryptographic Parent Hash linking all input test result hashes
        combined_parent_hash = compute_sha256([r.hash for r in test_results])

        return JudgeResult(
            experiment_id=self.experiment_id,
            verdict_id=verdict_id,
            timestamp=eval_time,
            source="JUDGE_ENGINE_V3",
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
            real_money_authorized=False,  # Strictly False
            confidence_interval_95=(ci_lower, ci_upper),
            effect_size=effect_sz,
            parent_hash=combined_parent_hash
        )
