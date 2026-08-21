"""
PARSA LAYER CONTRACTS & IMMUTABLE DATA MODELS
============================================
Immutable data structures, schemas, and runtime violations across the 7 PARSA layers.
"""

from dataclasses import dataclass, field, asdict
from typing import List, Dict, Any, Optional
import json
import hashlib
import time


class ParsaArchitectureViolation(Exception):
    """Base exception for all architectural violations."""
    pass


class FutureDataAccessViolation(ParsaArchitectureViolation):
    """Raised when an attempt is made to access data past the allowed timestamp (Look-ahead/Leakage)."""
    pass


class UnauthorizedLayerAccessViolation(ParsaArchitectureViolation):
    """Raised when a layer attempts an illegal read/write from/to a non-permitted layer."""
    pass


class DataUnavailableError(ParsaArchitectureViolation):
    """Raised when real market data is unavailable. Absolute NO-MOCK enforcement."""
    pass


class ImmutableRecordViolation(ParsaArchitectureViolation):
    """Raised when an attempt is made to alter a sealed/locked record."""
    pass


class EvidenceChainBrokenError(ParsaArchitectureViolation):
    """Raised when parent hash or cryptographic signature does not link cleanly."""
    pass


def compute_sha256(data: Any) -> str:
    """Deterministic SHA-256 computation over JSON-serialized payload."""
    if isinstance(data, str):
        payload = data.encode('utf-8')
    elif isinstance(data, (dict, list)):
        payload = json.dumps(data, sort_keys=True, separators=(',', ':')).encode('utf-8')
    elif hasattr(data, "to_dict"):
        payload = json.dumps(data.to_dict(), sort_keys=True, separators=(',', ':')).encode('utf-8')
    else:
        payload = str(data).encode('utf-8')
    return hashlib.sha256(payload).hexdigest()


@dataclass(frozen=True)
class ExperimentScenario:
    """Specifies the scope, universe, timeframes, and friction constraints."""
    experiment_id: str
    timestamp: float
    source: str
    version: str
    universe: List[str]
    timeframes: List[str]
    horizons_seconds: List[int]
    friction_bps: float
    out_of_sample_split_timestamp: float
    schema_version: str = "2.0.0"
    parent_hash: str = "GENESIS"
    hash: str = field(init=False)

    def __post_init__(self):
        payload = {
            "experiment_id": self.experiment_id,
            "timestamp": self.timestamp,
            "source": self.source,
            "version": self.version,
            "universe": self.universe,
            "timeframes": self.timeframes,
            "horizons_seconds": self.horizons_seconds,
            "friction_bps": self.friction_bps,
            "out_of_sample_split_timestamp": self.out_of_sample_split_timestamp,
            "schema_version": self.schema_version,
            "parent_hash": self.parent_hash
        }
        object.__setattr__(self, "hash", compute_sha256(payload))

    def to_dict(self) -> Dict[str, Any]:
        return {
            "experiment_id": self.experiment_id,
            "timestamp": self.timestamp,
            "source": self.source,
            "version": self.version,
            "universe": list(self.universe),
            "timeframes": list(self.timeframes),
            "horizons_seconds": list(self.horizons_seconds),
            "friction_bps": self.friction_bps,
            "out_of_sample_split_timestamp": self.out_of_sample_split_timestamp,
            "schema_version": self.schema_version,
            "parent_hash": self.parent_hash,
            "hash": self.hash
        }


@dataclass(frozen=True)
class MarketDataSnapshot:
    """Historical/Live market data strictly bounded at timestamp <= current_t."""
    experiment_id: str
    timestamp: float
    source: str
    asset: str
    timeframe: str
    candles: List[Dict[str, Any]]
    schema_version: str = "2.0.0"
    parent_hash: str = ""
    hash: str = field(init=False)

    def __post_init__(self):
        # Invariant check: candles must all have open_time <= timestamp
        for c in self.candles:
            candle_time = c.get("open_time", c.get("timestamp", 0))
            if candle_time > self.timestamp * 1000 and candle_time > self.timestamp:
                raise FutureDataAccessViolation(
                    f"MarketDataSnapshot contains future candle timestamp {candle_time} > snapshot allowed {self.timestamp}"
                )
        payload = {
            "experiment_id": self.experiment_id,
            "timestamp": self.timestamp,
            "source": self.source,
            "asset": self.asset,
            "timeframe": self.timeframe,
            "candles_count": len(self.candles),
            "first_candle": self.candles[0] if self.candles else None,
            "last_candle": self.candles[-1] if self.candles else None,
            "schema_version": self.schema_version,
            "parent_hash": self.parent_hash
        }
        object.__setattr__(self, "hash", compute_sha256(payload))

    def to_dict(self) -> Dict[str, Any]:
        return {
            "experiment_id": self.experiment_id,
            "timestamp": self.timestamp,
            "source": self.source,
            "asset": self.asset,
            "timeframe": self.timeframe,
            "candles": self.candles,
            "schema_version": self.schema_version,
            "parent_hash": self.parent_hash,
            "hash": self.hash
        }


@dataclass(frozen=True)
class PredictionRecord:
    """An immutable forecast generated at prediction_timestamp with zero future information."""
    experiment_id: str
    prediction_id: str
    prediction_timestamp: float
    source: str
    version: str
    asset: str
    timeframe: str
    horizon_seconds: int
    maturity_timestamp: float
    direction: str  # "LONG", "SHORT", "NEUTRAL"
    predicted_range: Dict[str, float]
    model_identifiers: List[str]
    input_data_hash: str
    schema_version: str = "2.0.0"
    parent_hash: str = ""
    prediction_hash: str = field(init=False)

    def __post_init__(self):
        if self.maturity_timestamp <= self.prediction_timestamp:
            raise ParsaArchitectureViolation(
                f"Maturity timestamp ({self.maturity_timestamp}) must be strictly greater than prediction timestamp ({self.prediction_timestamp})"
            )
        payload = {
            "experiment_id": self.experiment_id,
            "prediction_id": self.prediction_id,
            "prediction_timestamp": self.prediction_timestamp,
            "source": self.source,
            "version": self.version,
            "asset": self.asset,
            "timeframe": self.timeframe,
            "horizon_seconds": self.horizon_seconds,
            "maturity_timestamp": self.maturity_timestamp,
            "direction": self.direction,
            "predicted_range": self.predicted_range,
            "model_identifiers": self.model_identifiers,
            "input_data_hash": self.input_data_hash,
            "schema_version": self.schema_version,
            "parent_hash": self.parent_hash
        }
        object.__setattr__(self, "prediction_hash", compute_sha256(payload))

    def to_dict(self) -> Dict[str, Any]:
        return {
            "experiment_id": self.experiment_id,
            "prediction_id": self.prediction_id,
            "prediction_timestamp": self.prediction_timestamp,
            "source": self.source,
            "version": self.version,
            "asset": self.asset,
            "timeframe": self.timeframe,
            "horizon_seconds": self.horizon_seconds,
            "maturity_timestamp": self.maturity_timestamp,
            "direction": self.direction,
            "predicted_range": dict(self.predicted_range),
            "model_identifiers": list(self.model_identifiers),
            "input_data_hash": self.input_data_hash,
            "schema_version": self.schema_version,
            "parent_hash": self.parent_hash,
            "prediction_hash": self.prediction_hash
        }


@dataclass(frozen=True)
class OutcomeRecord:
    """The observed market reality between prediction_timestamp and maturity_timestamp."""
    experiment_id: str
    prediction_id: str
    observed_timestamp: float
    source: str
    version: str
    entry_price: float
    exit_price: float
    high_price: float
    low_price: float
    volume: float
    actual_return_pct: float
    max_favorable_excursion_pct: float
    max_adverse_excursion_pct: float
    schema_version: str = "2.0.0"
    parent_hash: str = ""
    hash: str = field(init=False)

    def __post_init__(self):
        payload = {
            "experiment_id": self.experiment_id,
            "prediction_id": self.prediction_id,
            "observed_timestamp": self.observed_timestamp,
            "source": self.source,
            "version": self.version,
            "entry_price": self.entry_price,
            "exit_price": self.exit_price,
            "high_price": self.high_price,
            "low_price": self.low_price,
            "volume": self.volume,
            "actual_return_pct": self.actual_return_pct,
            "max_favorable_excursion_pct": self.max_favorable_excursion_pct,
            "max_adverse_excursion_pct": self.max_adverse_excursion_pct,
            "schema_version": self.schema_version,
            "parent_hash": self.parent_hash
        }
        object.__setattr__(self, "hash", compute_sha256(payload))

    def to_dict(self) -> Dict[str, Any]:
        return {
            "experiment_id": self.experiment_id,
            "prediction_id": self.prediction_id,
            "observed_timestamp": self.observed_timestamp,
            "source": self.source,
            "version": self.version,
            "entry_price": self.entry_price,
            "exit_price": self.exit_price,
            "high_price": self.high_price,
            "low_price": self.low_price,
            "volume": self.volume,
            "actual_return_pct": self.actual_return_pct,
            "max_favorable_excursion_pct": self.max_favorable_excursion_pct,
            "max_adverse_excursion_pct": self.max_adverse_excursion_pct,
            "schema_version": self.schema_version,
            "parent_hash": self.parent_hash,
            "hash": self.hash
        }


@dataclass(frozen=True)
class TestResult:
    """The formal scoring of a matured prediction against observed market reality."""
    experiment_id: str
    prediction_id: str
    evaluated_timestamp: float
    source: str
    version: str
    status: str  # "CORRECT", "WRONG", "PREDICTION_NOT_REALIZED", "DATA_UNAVAILABLE"
    net_return_pct: float
    gross_return_pct: float
    friction_bps: float
    mfe_pct: float
    mae_pct: float
    schema_version: str = "2.0.0"
    parent_hash: str = ""
    hash: str = field(init=False)

    def __post_init__(self):
        payload = {
            "experiment_id": self.experiment_id,
            "prediction_id": self.prediction_id,
            "evaluated_timestamp": self.evaluated_timestamp,
            "source": self.source,
            "version": self.version,
            "status": self.status,
            "net_return_pct": self.net_return_pct,
            "gross_return_pct": self.gross_return_pct,
            "friction_bps": self.friction_bps,
            "mfe_pct": self.mfe_pct,
            "mae_pct": self.mae_pct,
            "schema_version": self.schema_version,
            "parent_hash": self.parent_hash
        }
        object.__setattr__(self, "hash", compute_sha256(payload))

    def to_dict(self) -> Dict[str, Any]:
        return {
            "experiment_id": self.experiment_id,
            "prediction_id": self.prediction_id,
            "evaluated_timestamp": self.evaluated_timestamp,
            "source": self.source,
            "version": self.version,
            "status": self.status,
            "net_return_pct": self.net_return_pct,
            "gross_return_pct": self.gross_return_pct,
            "friction_bps": self.friction_bps,
            "mfe_pct": self.mfe_pct,
            "mae_pct": self.mae_pct,
            "schema_version": self.schema_version,
            "parent_hash": self.parent_hash,
            "hash": self.hash
        }


@dataclass(frozen=True)
class JudgeResult:
    """The statistical evaluation and classification rendered over a population of test results."""
    experiment_id: str
    verdict_id: str
    timestamp: float
    source: str
    version: str
    sample_size: int
    correct_count: int
    wrong_count: int
    not_realized_count: int
    win_rate_pct: float
    t_statistic: float
    p_value: float
    bonferroni_threshold: float
    is_statistically_significant: bool
    law_classification: str  # "CLASS_A", "CLASS_B", "CANDIDATE", "REJECTED", "INSUFFICIENT_SAMPLE"
    real_money_authorized: bool
    schema_version: str = "2.0.0"
    parent_hash: str = ""
    hash: str = field(init=False)

    def __post_init__(self):
        payload = {
            "experiment_id": self.experiment_id,
            "verdict_id": self.verdict_id,
            "timestamp": self.timestamp,
            "source": self.source,
            "version": self.version,
            "sample_size": self.sample_size,
            "correct_count": self.correct_count,
            "wrong_count": self.wrong_count,
            "not_realized_count": self.not_realized_count,
            "win_rate_pct": self.win_rate_pct,
            "t_statistic": self.t_statistic,
            "p_value": self.p_value,
            "bonferroni_threshold": self.bonferroni_threshold,
            "is_statistically_significant": self.is_statistically_significant,
            "law_classification": self.law_classification,
            "real_money_authorized": self.real_money_authorized,
            "schema_version": self.schema_version,
            "parent_hash": self.parent_hash
        }
        object.__setattr__(self, "hash", compute_sha256(payload))

    def to_dict(self) -> Dict[str, Any]:
        return {
            "experiment_id": self.experiment_id,
            "verdict_id": self.verdict_id,
            "timestamp": self.timestamp,
            "source": self.source,
            "version": self.version,
            "sample_size": self.sample_size,
            "correct_count": self.correct_count,
            "wrong_count": self.wrong_count,
            "not_realized_count": self.not_realized_count,
            "win_rate_pct": self.win_rate_pct,
            "t_statistic": self.t_statistic,
            "p_value": self.p_value,
            "bonferroni_threshold": self.bonferroni_threshold,
            "is_statistically_significant": self.is_statistically_significant,
            "law_classification": self.law_classification,
            "real_money_authorized": self.real_money_authorized,
            "schema_version": self.schema_version,
            "parent_hash": self.parent_hash,
            "hash": self.hash
        }


@dataclass(frozen=True)
class GuardianFinding:
    """An independent audit finding emitted by the Guardian Inspector."""
    finding_id: str
    check_id: str
    severity: str  # "LOW", "MEDIUM", "HIGH", "CRITICAL"
    timestamp: float
    file: str
    line_or_function: str
    violation: str
    evidence: str
    expected_behavior: str
    actual_behavior: str
    status: str  # "PASS", "WARNING", "FAIL", "INVALID", "DATA_UNAVAILABLE", "UNVERIFIED"
    schema_version: str = "2.0.0"
    parent_hash: str = ""
    hash: str = field(init=False)

    def __post_init__(self):
        payload = {
            "finding_id": self.finding_id,
            "check_id": self.check_id,
            "severity": self.severity,
            "timestamp": self.timestamp,
            "file": self.file,
            "line_or_function": self.line_or_function,
            "violation": self.violation,
            "evidence": self.evidence,
            "expected_behavior": self.expected_behavior,
            "actual_behavior": self.actual_behavior,
            "status": self.status,
            "schema_version": self.schema_version,
            "parent_hash": self.parent_hash
        }
        object.__setattr__(self, "hash", compute_sha256(payload))

    def to_dict(self) -> Dict[str, Any]:
        return {
            "finding_id": self.finding_id,
            "check_id": self.check_id,
            "severity": self.severity,
            "timestamp": self.timestamp,
            "file": self.file,
            "line_or_function": self.line_or_function,
            "violation": self.violation,
            "evidence": self.evidence,
            "expected_behavior": self.expected_behavior,
            "actual_behavior": self.actual_behavior,
            "status": self.status,
            "schema_version": self.schema_version,
            "parent_hash": self.parent_hash,
            "hash": self.hash
        }


@dataclass(frozen=True)
class ReportRecord:
    """Presentation synthesis generated strictly from validated Judge and Guardian results."""
    report_id: str
    timestamp: float
    source: str
    version: str
    title: str
    summary: str
    verdicts_summary: Dict[str, Any]
    guardian_status: str
    schema_version: str = "2.0.0"
    parent_hash: str = ""
    hash: str = field(init=False)

    def __post_init__(self):
        payload = {
            "report_id": self.report_id,
            "timestamp": self.timestamp,
            "source": self.source,
            "version": self.version,
            "title": self.title,
            "summary": self.summary,
            "verdicts_summary": self.verdicts_summary,
            "guardian_status": self.guardian_status,
            "schema_version": self.schema_version,
            "parent_hash": self.parent_hash
        }
        object.__setattr__(self, "hash", compute_sha256(payload))

    def to_dict(self) -> Dict[str, Any]:
        return {
            "report_id": self.report_id,
            "timestamp": self.timestamp,
            "source": self.source,
            "version": self.version,
            "title": self.title,
            "summary": self.summary,
            "verdicts_summary": self.verdicts_summary,
            "guardian_status": self.guardian_status,
            "schema_version": self.schema_version,
            "parent_hash": self.parent_hash,
            "hash": self.hash
        }


@dataclass(frozen=True)
class EvidenceRecord:
    """Universal envelope for any node along the 9-stage evidence chain."""
    evidence_id: str
    stage: str  # "GENESIS", "MARKET_SNAPSHOT", "PREDICTION", "LOCK", "MATURITY", "OUTCOME", "TEST_RESULT", "JUDGE_RESULT", "GUARDIAN_RESULT", "REPORT"
    timestamp: float
    source_layer: str
    payload_hash: str
    parent_hash: str
    schema_version: str = "2.0.0"
    evidence_hash: str = field(init=False)

    def __post_init__(self):
        payload = {
            "evidence_id": self.evidence_id,
            "stage": self.stage,
            "timestamp": self.timestamp,
            "source_layer": self.source_layer,
            "payload_hash": self.payload_hash,
            "parent_hash": self.parent_hash,
            "schema_version": self.schema_version
        }
        object.__setattr__(self, "evidence_hash", compute_sha256(payload))

    def to_dict(self) -> Dict[str, Any]:
        return {
            "evidence_id": self.evidence_id,
            "stage": self.stage,
            "timestamp": self.timestamp,
            "source_layer": self.source_layer,
            "payload_hash": self.payload_hash,
            "parent_hash": self.parent_hash,
            "schema_version": self.schema_version,
            "evidence_hash": self.evidence_hash
        }
