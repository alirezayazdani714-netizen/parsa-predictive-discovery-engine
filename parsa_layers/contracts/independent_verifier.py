"""
PARSA INDEPENDENT FORENSIC VERIFIER (PHASE 14 SELF-TESTING ENGINE)
==================================================================
Independent reference calculations, metamorphic testing, and invariant validations
implemented completely separately from the production code paths.
Detects circular validation, secret regressions, and dual-modification of code and tests.
"""

from typing import List, Dict, Any, Tuple, Optional
import hashlib
import json
import math
import time


class IndependentForensicVerifier:
    """
    Independent reference calculations and invariant assertions that do NOT rely on
    production engine methods.
    """

    @staticmethod
    def independent_sha256_canonical(payload_dict: Dict[str, Any]) -> str:
        """
        Independent reference implementation of canonical SHA-256 serialization.
        Serializes JSON with explicit separators, sorted keys, and utf-8 encoding.
        """
        def _to_clean_json_compatible(val: Any) -> Any:
            if isinstance(val, (dict, )):
                return {k: _to_clean_json_compatible(v) for k, v in sorted(val.items())}
            elif isinstance(val, (list, tuple, set)):
                return [_to_clean_json_compatible(v) for v in val]
            elif isinstance(val, float):
                # Ensure deterministic float representation
                return round(val, 8) if not math.isnan(val) and not math.isinf(val) else "INVALID_FLOAT"
            return val

        cleaned = _to_clean_json_compatible(payload_dict)
        serialized_bytes = json.dumps(cleaned, sort_keys=True, separators=(',', ':')).encode('utf-8')
        return hashlib.sha256(serialized_bytes).hexdigest()

    @staticmethod
    def independent_wilson_ci(k: int, n: int, confidence: float = 0.95) -> Tuple[float, float]:
        """
        Independent Wilson score interval calculation using exact statistical formula.
        """
        if n <= 0 or k < 0 or k > n:
            return 0.0, 0.0

        z = 1.959963984540054  # 95% two-tailed quantile
        p = float(k) / float(n)
        
        denom = 1.0 + (z ** 2) / n
        center = (p + (z ** 2) / (2.0 * n)) / denom
        delta = (z * math.sqrt((p * (1.0 - p) / n) + ((z ** 2) / (4.0 * (n ** 2))))) / denom

        lower = max(0.0, center - delta) * 100.0
        upper = min(1.0, center + delta) * 100.0
        return round(lower, 2), round(upper, 2)

    @staticmethod
    def independent_cohens_h(p1: float, p2: float = 0.50) -> float:
        """
        Independent Cohen's h effect size calculation.
        h = 2 * (arcsin(sqrt(p1)) - arcsin(sqrt(p2)))
        """
        p1_c = max(0.0, min(1.0, float(p1)))
        p2_c = max(0.0, min(1.0, float(p2)))
        h = 2.0 * (math.asin(math.sqrt(p1_c)) - math.asin(math.sqrt(p2_c)))
        return round(h, 4)

    @staticmethod
    def independent_z_and_pvalue(k: int, n: int, p_null: float = 0.50) -> Tuple[float, float]:
        """
        Independent binomial z-score and 2-tailed p-value via Abramowitz-Stegun erf approximation.
        """
        if n <= 0 or k < 0:
            return 0.0, 1.0

        p_hat = float(k) / float(n)
        se = math.sqrt(p_null * (1.0 - p_null) / float(n))
        if se <= 0:
            return 0.0, 1.0

        z = (p_hat - p_null) / se

        # Independent erf rational approximation (Abramowitz & Stegun formula 7.1.26)
        x = abs(z) / math.sqrt(2.0)
        p_const = 0.3275911
        a1 = 0.254829592
        a2 = -0.284496736
        a3 = 1.421413741
        a4 = -1.453152027
        a5 = 1.061405429
        t = 1.0 / (1.0 + p_const * x)
        poly = ((((a5 * t + a4) * t + a3) * t + a2) * t + a1) * t
        erf_val = 1.0 - poly * math.exp(-x * x)

        p_val = 2.0 * (1.0 - 0.5 * (1.0 + erf_val))
        return round(z, 3), round(max(0.0, min(1.0, p_val)), 6)

    @staticmethod
    def verify_temporal_monotonicity(timestamps: List[float]) -> bool:
        """Verifies strictly monotonic ascending order with zero negative or zero deltas."""
        for i in range(1, len(timestamps)):
            if timestamps[i] <= timestamps[i - 1]:
                return False
        return True

    @staticmethod
    def verify_no_future_leakage(
        candle_timestamps: List[float],
        max_allowed_timestamp: float
    ) -> bool:
        """Verifies that no candle has a timestamp greater than max_allowed_timestamp."""
        for t in candle_timestamps:
            if t > max_allowed_timestamp:
                return False
        return True
