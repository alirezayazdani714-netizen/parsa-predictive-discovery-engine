"""
PARSA LEGACY ENGINE ADAPTERS & ARCHITECTURAL BRIDGE
===================================================
Wraps existing historical mission scripts and algorithms into the formal 7-layer interfaces.
Identifies historical scripts with legacy status.
"""

from typing import Dict, Any, List, Optional
from parsa_layers.contracts.models import (
    PredictionRecord,
    TestResult,
    JudgeResult,
    ReportRecord
)


LEGACY_SCRIPT_REGISTRY = {
    "scripts/binance_live_truth_runner.py": {
        "status": "LEGACY / NON-COMPLIANT",
        "reason": "Couples real-time execution with test scoring and markdown output in a single process."
    },
    "scripts/parsa_mission19_live_forecasting_lab.py": {
        "status": "LEGACY / NON-COMPLIANT",
        "reason": "Combines 19 discovery evaluation with live fetching, maturity scoring, and summary generation."
    },
    "scripts/parsa_mission20_live_trial_engine.py": {
        "status": "LEGACY / NON-COMPLIANT",
        "reason": "Combines 11-hour execution loop with 4-horizon test scoring and judicial metrics."
    },
    "scripts/data_ingestion_audit.py": {
        "status": "LEGACY / COMPLIANT",
        "reason": "Read-only market data integrity scanner."
    },
    "scripts/parsa_scientific_detective.py": {
        "status": "LEGACY / COMPLIANT",
        "reason": "Read-only statistical refutation and Bonferroni significance auditor."
    }
}


def get_legacy_script_status(script_path: str) -> Dict[str, str]:
    """Returns architectural compliance classification for historical scripts."""
    return LEGACY_SCRIPT_REGISTRY.get(
        script_path,
        {"status": "LEGACY / UNVERIFIED", "reason": "Not explicitly registered in Phase 2 adapter."}
    )
