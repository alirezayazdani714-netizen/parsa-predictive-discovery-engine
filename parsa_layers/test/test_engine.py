"""
PARSA TEST LAYER (LAYER 3 - PHASE 3 HARDENED)
=============================================
Responsible for post-maturity outcome observation, forward excursion measurement (MFE/MAE),
friction-adjusted return computation, and objective scoring against locked predictions.

CRITICAL INVARIANTS:
1. Cannot evaluate before maturity_timestamp. Attempting to score before maturity raises FutureDataAccessViolation.
2. Cannot modify the locked PredictionRecord.
3. Operates exclusively on verified post-maturity market outcomes via OutcomePolicy.
"""

from typing import List, Dict, Any, Optional, Tuple
import time
from parsa_layers.contracts.models import (
    PredictionRecord,
    OutcomeRecord,
    TestResult,
    FutureDataAccessViolation,
    DataUnavailableError
)
from parsa_layers.contracts.access_control import enforce_layer
from parsa_layers.test.outcome_policy import OutcomePolicy


class TestEngine:
    """Post-Maturity Outcome Evaluation Engine."""

    def __init__(
        self,
        experiment_id: str,
        friction_bps: float = 15.0,
        movement_threshold_pct: float = 0.10,
        version: str = "3.0.0"
    ):
        self.experiment_id = experiment_id
        self.friction_bps = friction_bps
        self.version = version
        self.policy = OutcomePolicy(movement_threshold_pct=movement_threshold_pct)

    @enforce_layer("TEST")
    def evaluate_prediction_outcome(
        self,
        prediction: PredictionRecord,
        forward_candles: List[Dict[str, Any]],
        current_time: Optional[float] = None
    ) -> Tuple[OutcomeRecord, TestResult]:
        """
        Evaluates a matured prediction against forward market candles via OutcomePolicy.
        Enforces: current_time >= prediction.maturity_timestamp.
        """
        return self.policy.evaluate(
            experiment_id=self.experiment_id,
            version=self.version,
            prediction=prediction,
            forward_candles=forward_candles,
            friction_bps=self.friction_bps,
            current_time=current_time
        )
