"""
PARSA SCENARIO / EXPERIMENT DESIGN LAYER (LAYER 7 - PHASE 3 HARDENED)
=====================================================================
Defines experiment protocols, asset universe, timeframe resolutions, and horizon grids.
Cannot declare experiment success or modify active execution streams.
"""

from typing import List, Dict, Any, Optional
import time
from parsa_layers.contracts.models import (
    ExperimentScenario,
    UnauthorizedLayerAccessViolation
)
from parsa_layers.contracts.access_control import enforce_layer


class ScenarioEngine:
    """Manages the creation and formal definition of experiment protocols."""

    def __init__(self, experiment_id: str, version: str = "3.0.0"):
        self.experiment_id = experiment_id
        self.version = version

    @enforce_layer("SCENARIO")
    def create_protocol(
        self,
        universe: List[str],
        timeframes: List[str],
        horizons_seconds: List[int],
        friction_bps: float = 15.0,
        out_of_sample_split_timestamp: Optional[float] = None
    ) -> ExperimentScenario:
        """Constructs an immutable experiment scenario protocol."""
        now = time.time()
        oos_split = out_of_sample_split_timestamp if out_of_sample_split_timestamp is not None else now

        return ExperimentScenario(
            experiment_id=self.experiment_id,
            timestamp=now,
            source="SCENARIO_ENGINE_V3",
            version=self.version,
            universe=universe,
            timeframes=timeframes,
            horizons_seconds=horizons_seconds,
            friction_bps=friction_bps,
            out_of_sample_split_timestamp=oos_split
        )
