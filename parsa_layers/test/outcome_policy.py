"""
PARSA OUTCOME POLICY & SCORING LOGIC (LAYER 3 - PHASE 3 HARDENED)
=================================================================
Centralized, deterministic scoring policy for evaluating matured predictions against
authentic market outcomes.

CRITICAL INVARIANTS:
1. Strict Horizon Enforcement: Cannot evaluate before maturity_timestamp.
2. Outcome Decoupling: Market reality is observed purely from independent forward candles.
3. Fail-Closed: Missing or corrupted candles immediately raise DataUnavailableError.
4. Non-Realized State: Subtle or noise-level movements (< threshold) are explicitly classified
   as PREDICTION_NOT_REALIZED rather than forced binary wins/losses.
"""

from typing import List, Dict, Any, Tuple, Optional
import time
from parsa_layers.contracts.models import (
    PredictionRecord,
    OutcomeRecord,
    TestResult,
    FutureDataAccessViolation,
    DataUnavailableError,
    unfreeze
)

DEFAULT_MOVEMENT_THRESHOLD_PCT = 0.10


class OutcomePolicy:
    """Standardized scoring and excursion measurement policy."""

    def __init__(self, movement_threshold_pct: float = DEFAULT_MOVEMENT_THRESHOLD_PCT):
        self.movement_threshold_pct = movement_threshold_pct

    def compute_excursions_and_return(
        self,
        direction: str,
        entry_price: float,
        exit_price: float,
        high_price: float,
        low_price: float,
        friction_bps: float
    ) -> Tuple[float, float, float, float]:
        """
        Computes (gross_return_pct, net_return_pct, mfe_pct, mae_pct).
        """
        if entry_price <= 0:
            raise DataUnavailableError("Invalid non-positive entry price encountered in outcome computation.")

        friction_pct = friction_bps / 100.0

        if direction == "LONG":
            gross_return = (exit_price - entry_price) / entry_price * 100.0
            mfe = max(0.0, (high_price - entry_price) / entry_price * 100.0)
            mae = max(0.0, (entry_price - low_price) / entry_price * 100.0)
        elif direction == "SHORT":
            gross_return = (entry_price - exit_price) / entry_price * 100.0
            mfe = max(0.0, (entry_price - low_price) / entry_price * 100.0)
            mae = max(0.0, (high_price - entry_price) / entry_price * 100.0)
        else:  # NEUTRAL
            gross_return = -abs(exit_price - entry_price) / entry_price * 100.0
            mfe = 0.0
            mae = max(
                max(0.0, (high_price - entry_price) / entry_price * 100.0),
                max(0.0, (entry_price - low_price) / entry_price * 100.0)
            )

        net_return = gross_return - friction_pct
        return gross_return, net_return, mfe, mae

    def classify_outcome_status(
        self,
        direction: str,
        entry_price: float,
        exit_price: float
    ) -> str:
        """
        Classifies status into CORRECT, WRONG, or PREDICTION_NOT_REALIZED.
        """
        raw_price_movement_pct = (exit_price - entry_price) / entry_price * 100.0
        thresh = self.movement_threshold_pct

        if direction != "NEUTRAL" and abs(raw_price_movement_pct) < thresh:
            return "PREDICTION_NOT_REALIZED"

        if direction == "LONG":
            return "CORRECT" if raw_price_movement_pct >= thresh else "WRONG"
        elif direction == "SHORT":
            return "CORRECT" if raw_price_movement_pct <= -thresh else "WRONG"
        elif direction == "NEUTRAL":
            return "CORRECT" if abs(raw_price_movement_pct) < thresh else "WRONG"
        else:
            return "WRONG"

    def evaluate(
        self,
        experiment_id: str,
        version: str,
        prediction: PredictionRecord,
        forward_candles: List[Dict[str, Any]],
        friction_bps: float,
        current_time: Optional[float] = None
    ) -> Tuple[OutcomeRecord, TestResult]:
        """
        Evaluates a matured prediction against forward market candles.
        Enforces: current_time >= prediction.maturity_timestamp.
        """
        eval_time = current_time if current_time is not None else time.time()

        # RUNTIME GUARD: Strict Maturity Check
        if eval_time < prediction.maturity_timestamp:
            raise FutureDataAccessViolation(
                f"CRITICAL VIOLATION: Attempted to evaluate prediction '{prediction.prediction_id}' at {eval_time} before maturity timestamp {prediction.maturity_timestamp}"
            )

        if not forward_candles:
            raise DataUnavailableError(
                f"STATUS = DATA_UNAVAILABLE: Forward candles empty for prediction '{prediction.prediction_id}'"
            )

        unfrozen_candles = [unfreeze(c) for c in forward_candles]
        entry_price = float(unfrozen_candles[0]["open"])
        exit_price = float(unfrozen_candles[-1]["close"])
        high_price = max(float(c["high"]) for c in unfrozen_candles)
        low_price = min(float(c["low"]) for c in unfrozen_candles)
        total_vol = sum(float(c["volume"]) for c in unfrozen_candles)

        gross_ret, net_ret, mfe, mae = self.compute_excursions_and_return(
            direction=prediction.direction,
            entry_price=entry_price,
            exit_price=exit_price,
            high_price=high_price,
            low_price=low_price,
            friction_bps=friction_bps
        )

        status = self.classify_outcome_status(
            direction=prediction.direction,
            entry_price=entry_price,
            exit_price=exit_price
        )

        outcome = OutcomeRecord(
            experiment_id=experiment_id,
            prediction_id=prediction.prediction_id,
            observed_timestamp=eval_time,
            source="TEST_ENGINE_V3",
            version=version,
            entry_price=entry_price,
            exit_price=exit_price,
            high_price=high_price,
            low_price=low_price,
            volume=total_vol,
            actual_return_pct=round(gross_ret, 4),
            max_favorable_excursion_pct=round(mfe, 4),
            max_adverse_excursion_pct=round(mae, 4),
            parent_hash=prediction.prediction_hash
        )

        test_result = TestResult(
            experiment_id=experiment_id,
            prediction_id=prediction.prediction_id,
            evaluated_timestamp=eval_time,
            source="TEST_ENGINE_V3",
            version=version,
            status=status,
            net_return_pct=round(net_ret, 4),
            gross_return_pct=round(gross_ret, 4),
            friction_bps=friction_bps,
            mfe_pct=round(mfe, 4),
            mae_pct=round(mae, 4),
            parent_hash=outcome.hash
        )

        return outcome, test_result
