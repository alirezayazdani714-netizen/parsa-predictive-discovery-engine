"""
PARSA TEST LAYER (LAYER 3)
==========================
Responsible for post-maturity outcome observation, forward excursion measurement (MFE/MAE),
friction-adjusted return computation, and objective scoring against locked predictions.

CRITICAL INVARIANTS:
1. Cannot evaluate before maturity_timestamp. Attempting to score before maturity raises FutureDataAccessViolation.
2. Cannot modify the locked PredictionRecord.
3. Operates exclusively on verified post-maturity market outcomes.
"""

from typing import List, Dict, Any, Optional
import time
from parsa_layers.contracts.models import (
    PredictionRecord,
    OutcomeRecord,
    TestResult,
    FutureDataAccessViolation,
    DataUnavailableError,
    compute_sha256
)


class TestEngine:
    """Post-Maturity Outcome Evaluation Engine."""

    def __init__(self, experiment_id: str, friction_bps: float = 15.0, version: str = "2.0.0"):
        self.experiment_id = experiment_id
        self.friction_bps = friction_bps
        self.version = version

    def evaluate_prediction_outcome(
        self,
        prediction: PredictionRecord,
        forward_candles: List[Dict[str, Any]],
        current_time: Optional[float] = None
    ) -> tuple[OutcomeRecord, TestResult]:
        """
        Evaluates a matured prediction against forward market candles.
        Enforces: current_time >= prediction.maturity_timestamp.
        """
        eval_time = current_time if current_time is not None else time.time()

        # RUNTIME GUARD: Maturity check
        if eval_time < prediction.maturity_timestamp:
            raise FutureDataAccessViolation(
                f"CRITICAL VIOLATION: Attempted to score prediction {prediction.prediction_id} at {eval_time} before maturity {prediction.maturity_timestamp}"
            )

        if not forward_candles:
            raise DataUnavailableError(
                f"STATUS = DATA_UNAVAILABLE: No forward candles available to evaluate prediction {prediction.prediction_id}"
            )

        entry_price = float(forward_candles[0]["open"])
        exit_price = float(forward_candles[-1]["close"])
        high_price = max(float(c["high"]) for c in forward_candles)
        low_price = min(float(c["low"]) for c in forward_candles)
        total_vol = sum(float(c["volume"]) for c in forward_candles)

        # Calculate Excursions
        if prediction.direction == "LONG":
            gross_return = (exit_price - entry_price) / entry_price * 100.0
            mfe = (high_price - entry_price) / entry_price * 100.0
            mae = (entry_price - low_price) / entry_price * 100.0
        elif prediction.direction == "SHORT":
            gross_return = (entry_price - exit_price) / entry_price * 100.0
            mfe = (entry_price - low_price) / entry_price * 100.0
            mae = (high_price - entry_price) / entry_price * 100.0
        else:  # NEUTRAL
            gross_return = -abs(exit_price - entry_price) / entry_price * 100.0
            mfe = 0.0
            mae = max((high_price - entry_price), (entry_price - low_price)) / entry_price * 100.0

        friction_pct = self.friction_bps / 100.0
        net_return = gross_return - friction_pct

        # Determine outcome classification
        # Movement threshold: 0.10% min movement required for definitive realization
        threshold = 0.10
        actual_price_movement = (exit_price - entry_price) / entry_price * 100.0

        if abs(actual_price_movement) < threshold and prediction.direction != "NEUTRAL":
            status = "PREDICTION_NOT_REALIZED"
        elif prediction.direction == "LONG":
            status = "CORRECT" if actual_price_movement > threshold else "WRONG"
        elif prediction.direction == "SHORT":
            status = "CORRECT" if actual_price_movement < -threshold else "WRONG"
        else:  # NEUTRAL
            status = "CORRECT" if abs(actual_price_movement) <= threshold else "WRONG"

        outcome_record = OutcomeRecord(
            experiment_id=self.experiment_id,
            prediction_id=prediction.prediction_id,
            observed_timestamp=eval_time,
            source="TEST_ENGINE_V2",
            version=self.version,
            entry_price=entry_price,
            exit_price=exit_price,
            high_price=high_price,
            low_price=low_price,
            volume=total_vol,
            actual_return_pct=round(gross_return, 4),
            max_favorable_excursion_pct=round(mfe, 4),
            max_adverse_excursion_pct=round(mae, 4),
            parent_hash=prediction.prediction_hash
        )

        test_result = TestResult(
            experiment_id=self.experiment_id,
            prediction_id=prediction.prediction_id,
            evaluated_timestamp=eval_time,
            source="TEST_ENGINE_V2",
            version=self.version,
            status=status,
            net_return_pct=round(net_return, 4),
            gross_return_pct=round(gross_return, 4),
            friction_bps=self.friction_bps,
            mfe_pct=round(mfe, 4),
            mae_pct=round(mae, 4),
            parent_hash=outcome_record.hash
        )

        return outcome_record, test_result
