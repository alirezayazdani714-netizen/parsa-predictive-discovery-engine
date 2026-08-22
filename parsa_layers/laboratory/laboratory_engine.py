"""
PARSA LABORATORY LAYER (LAYER 1 - PHASE 3 HARDENED)
===================================================
Encapsulates hypothesis formulation, mathematical signal functions, and pattern extractors.
Must NOT access data past the permitted in-sample training cutoff.
"""

from typing import List, Dict, Any, Optional
import math
from parsa_layers.contracts.models import (
    FutureDataAccessViolation,
    UnauthorizedLayerAccessViolation
)
from parsa_layers.contracts.access_control import enforce_layer


class LaboratoryEngine:
    """Laboratory signal and hypothesis formulation engine."""

    def __init__(self, in_sample_cutoff_timestamp: float):
        self.cutoff_timestamp = in_sample_cutoff_timestamp

    def validate_in_sample_boundary(self, candles: List[Dict[str, Any]]) -> None:
        """Enforces that no candle provided to the laboratory is beyond the in-sample cutoff."""
        for c in candles:
            t = c.get("open_time", c.get("timestamp", 0))
            # If timestamp is in ms, normalize
            t_sec = t / 1000.0 if t > 1e11 else float(t)
            if t_sec > self.cutoff_timestamp:
                raise FutureDataAccessViolation(
                    f"Laboratory accessed data at timestamp {t_sec} > in-sample cutoff {self.cutoff_timestamp}"
                )

    @enforce_layer("LABORATORY")
    def compute_trend_momentum_signal(self, candles: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Calculates a directional signal using bounded historical candles."""
        self.validate_in_sample_boundary(candles)
        if len(candles) < 20:
            return {"signal": "NEUTRAL", "confidence": 0.0, "reason": "INSUFFICIENT_HISTORY"}

        closes = [float(c["close"]) for c in candles]
        volumes = [float(c["volume"]) for c in candles]

        # Fast EMA (9) and Slow EMA (21)
        ema_fast = sum(closes[-9:]) / 9.0
        ema_slow = sum(closes[-21:]) / 21.0
        last_close = closes[-1]

        # Volume trend
        avg_vol = sum(volumes[-20:]) / 20.0
        last_vol = volumes[-1]

        if ema_fast > ema_slow and last_close > ema_fast and last_vol > avg_vol * 1.05:
            direction = "LONG"
            confidence = min(0.95, (ema_fast - ema_slow) / ema_slow * 100.0 + 0.5)
        elif ema_fast < ema_slow and last_close < ema_fast and last_vol > avg_vol * 1.05:
            direction = "SHORT"
            confidence = min(0.95, (ema_slow - ema_fast) / ema_slow * 100.0 + 0.5)
        else:
            direction = "NEUTRAL"
            confidence = 0.0

        return {
            "signal": direction,
            "confidence": round(confidence, 4),
            "ema_fast": round(ema_fast, 4),
            "ema_slow": round(ema_slow, 4),
            "last_close": last_close
        }
