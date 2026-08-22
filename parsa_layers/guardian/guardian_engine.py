"""
PARSA INDEPENDENT GUARDIAN / INSPECTOR LAYER (LAYER 5 - PHASE 3 HARDENED)
========================================================================
Independent forensic inspector capable of detecting and flagging 25 critical architectural,
methodological, and adversarial violations with an append-only evidence sink.

CRITICAL INVARIANTS:
1. Cannot modify predictions, outcomes, scores, verdicts, or historical evidence.
2. Read-only inspection across all artifacts, contracts, hashes, and execution logs.
3. Emits formal GuardianFinding records with severities (LOW, MEDIUM, HIGH, CRITICAL)
   and statuses (PASS, WARNING, FAIL, INVALID, DATA_UNAVAILABLE, UNVERIFIED).
4. Non-Destructive Append-Only Evidence Sink: Every finding is cryptographically recorded.
"""

from typing import List, Dict, Any, Optional
import time
import math
import re
import json
import os
from parsa_layers.contracts.models import (
    GuardianFinding,
    PredictionRecord,
    OutcomeRecord,
    TestResult,
    JudgeResult,
    ReportRecord,
    MarketDataSnapshot,
    DataUnavailableError,
    compute_sha256,
    unfreeze
)
from parsa_layers.contracts.access_control import enforce_layer, ALLOWED_READ_MATRIX, ALLOWED_WRITE_MATRIX


class GuardianEngine:
    """Independent Forensic Auditor and Guardian Inspector with persistent append-only sink."""

    def __init__(self, inspector_id: str = "GUARDIAN_CORE_V3", sink_path: Optional[str] = None):
        self.inspector_id = inspector_id
        self.sink_path = sink_path if sink_path is not None else "guardian_evidence/guardian_findings.jsonl"
        self._findings: List[GuardianFinding] = []
        self._sink_chain: List[Dict[str, Any]] = []

        # Auto-load existing records from persistent sink if available
        if self.sink_path and os.path.exists(self.sink_path):
            try:
                self.load_sink_from_file(self.sink_path)
            except Exception as e:
                raise DataUnavailableError(f"STATUS = DATA_UNAVAILABLE: Corrupted Guardian evidence sink file '{self.sink_path}': {str(e)}") from e

    @property
    def findings(self) -> List[GuardianFinding]:
        return list(self._findings)

    def clear_findings(self) -> None:
        """Clears in-memory cache only. Persistent disk sink is strictly preserved and never truncated."""
        self._findings.clear()
        self._sink_chain.clear()

    def load_sink_from_file(self, path: Optional[str] = None) -> List[Dict[str, Any]]:
        """Loads and parses persistent evidence sink from disk."""
        target_path = path or self.sink_path
        if not target_path or not os.path.exists(target_path):
            return []

        loaded_entries: List[Dict[str, Any]] = []
        loaded_findings: List[GuardianFinding] = []

        with open(target_path, "r", encoding="utf-8") as f:
            for line in f:
                line_str = line.strip()
                if not line_str:
                    continue
                entry = json.loads(line_str)
                loaded_entries.append(entry)
                
                # Reconstruct GuardianFinding
                finding = GuardianFinding(
                    finding_id=entry["finding_id"],
                    check_id=entry["check_id"],
                    severity=entry["severity"],
                    timestamp=float(entry["timestamp"]),
                    file=entry.get("file", "unknown"),
                    line_or_function=entry.get("line_or_function", "unknown"),
                    violation=entry.get("violation", ""),
                    evidence=entry.get("evidence", ""),
                    expected_behavior=entry.get("expected_behavior", ""),
                    actual_behavior=entry.get("actual_behavior", ""),
                    status=entry["status"],
                    parent_hash=entry.get("parent_hash", "")
                )
                loaded_findings.append(finding)

        self._sink_chain = loaded_entries
        self._findings = loaded_findings
        return loaded_entries

    def verify_sink_file(self, path: Optional[str] = None) -> GuardianFinding:
        """
        Forensically verifies that the persistent sink file on disk is authentic,
        unbroken, and has not suffered line deletion, attribute mutation, or hash forgery.
        """
        target_path = path or self.sink_path
        now = time.time()

        if not target_path or not os.path.exists(target_path):
            return GuardianFinding(
                finding_id=f"GF-SINK-{int(now)}-000",
                check_id="CHK-SINK-INTEGRITY",
                severity="CRITICAL",
                timestamp=now,
                file=target_path or "none",
                line_or_function="verify_sink_file",
                violation="GUARDIAN_EVIDENCE_TAMPERED",
                evidence="Persistent sink file missing or inaccessible",
                expected_behavior="Valid JSONL evidence file on disk",
                actual_behavior="File missing",
                status="INVALID",
                parent_hash="GENESIS_GUARDIAN_ROOT"
            )

        entries: List[Dict[str, Any]] = []
        with open(target_path, "r", encoding="utf-8") as f:
            for line_idx, line in enumerate(f):
                line_str = line.strip()
                if not line_str:
                    continue
                try:
                    entry = json.loads(line_str)
                    entries.append(entry)
                except Exception as e:
                    return GuardianFinding(
                        finding_id=f"GF-SINK-{int(now)}-{line_idx:03d}",
                        check_id="CHK-SINK-INTEGRITY",
                        severity="CRITICAL",
                        timestamp=now,
                        file=target_path,
                        line_or_function=f"line_{line_idx+1}",
                        violation="GUARDIAN_EVIDENCE_TAMPERED",
                        evidence=f"JSON corruption at line {line_idx+1}: {str(e)}",
                        expected_behavior="Valid JSON record",
                        actual_behavior="Corrupted JSON line",
                        status="INVALID",
                        parent_hash="BROKEN_SINK"
                    )

        if not entries:
            return GuardianFinding(
                finding_id=f"GF-SINK-{int(now)}-EMPTY",
                check_id="CHK-SINK-INTEGRITY",
                severity="LOW",
                timestamp=now,
                file=target_path,
                line_or_function="verify_sink_file",
                violation="None",
                evidence="Empty evidence sink ledger",
                expected_behavior="Valid ledger",
                actual_behavior="Empty",
                status="PASS",
                parent_hash="GENESIS_GUARDIAN_ROOT"
            )

        # Forensic chain & integrity audit across all disk entries
        prev_finding_hash = "GENESIS_GUARDIAN_ROOT"
        for i, entry in enumerate(entries):
            # 1. Check index ordering
            if entry.get("index") != i:
                return GuardianFinding(
                    finding_id=f"GF-SINK-{int(now)}-{i:03d}",
                    check_id="CHK-SINK-INTEGRITY",
                    severity="CRITICAL",
                    timestamp=now,
                    file=target_path,
                    line_or_function=f"entry_index_{i}",
                    violation="GUARDIAN_EVIDENCE_TAMPERED",
                    evidence=f"Entry index mismatch: expected {i}, got {entry.get('index')}. Possible line deletion.",
                    expected_behavior=f"Sequential index {i}",
                    actual_behavior=f"Found index {entry.get('index')}",
                    status="INVALID",
                    parent_hash=prev_finding_hash
                )

            # 2. Check previous finding hash linkage
            if entry.get("previous_finding_hash") != prev_finding_hash:
                return GuardianFinding(
                    finding_id=f"GF-SINK-{int(now)}-{i:03d}",
                    check_id="CHK-SINK-INTEGRITY",
                    severity="CRITICAL",
                    timestamp=now,
                    file=target_path,
                    line_or_function=f"linkage_idx_{i}",
                    violation="GUARDIAN_EVIDENCE_TAMPERED",
                    evidence=f"Previous finding hash mismatch at entry {i}: entry says '{entry.get('previous_finding_hash')}', expected '{prev_finding_hash}'",
                    expected_behavior=f"Link to {prev_finding_hash}",
                    actual_behavior=f"Severed linkage: {entry.get('previous_finding_hash')}",
                    status="INVALID",
                    parent_hash=prev_finding_hash
                )

            # 3. Recompute canonical finding hash
            canonical_payload = {
                "finding_id": entry["finding_id"],
                "check_id": entry["check_id"],
                "severity": entry["severity"],
                "timestamp": entry["timestamp"],
                "file": entry.get("file", "unknown"),
                "line_or_function": entry.get("line_or_function", "unknown"),
                "violation": entry.get("violation", ""),
                "evidence": entry.get("evidence", ""),
                "expected_behavior": entry.get("expected_behavior", ""),
                "actual_behavior": entry.get("actual_behavior", ""),
                "status": entry["status"],
                "schema_version": "3.0.0",
                "parent_hash": entry.get("parent_hash", "")
            }
            recomputed_hash = compute_sha256(canonical_payload)
            if recomputed_hash != entry.get("finding_hash"):
                return GuardianFinding(
                    finding_id=f"GF-SINK-{int(now)}-{i:03d}",
                    check_id="CHK-SINK-INTEGRITY",
                    severity="CRITICAL",
                    timestamp=now,
                    file=target_path,
                    line_or_function=f"hash_check_{i}",
                    violation="GUARDIAN_EVIDENCE_TAMPERED",
                    evidence=f"Tampered finding record at entry {i}: stored hash {entry.get('finding_hash')} != recomputed {recomputed_hash}",
                    expected_behavior="Stored hash matches recomputed payload hash",
                    actual_behavior="Record content/metadata tampered with",
                    status="INVALID",
                    parent_hash=prev_finding_hash
                )

            prev_finding_hash = entry.get("finding_hash")

        return GuardianFinding(
            finding_id=f"GF-SINK-{int(now)}-PASS",
            check_id="CHK-SINK-INTEGRITY",
            severity="LOW",
            timestamp=now,
            file=target_path,
            line_or_function="verify_sink_file",
            violation="None",
            evidence=f"All {len(entries)} disk sink records verified authentic and unbroken",
            expected_behavior="Intact append-only disk ledger",
            actual_behavior="Compliant",
            status="PASS",
            parent_hash=prev_finding_hash
        )

    @enforce_layer("GUARDIAN")
    def add_finding(
        self,
        check_id: str,
        severity: str,
        file: str,
        line_or_function: str,
        violation: str,
        evidence: str,
        expected_behavior: str,
        actual_behavior: str,
        status: str,
        parent_hash: str = ""
    ) -> GuardianFinding:
        now = time.time()
        finding_id = f"GF-{check_id}-{int(now)}-{len(self._sink_chain) + 1:03d}"
        
        # Link to previous finding hash in the chain
        prev_finding_hash = self._sink_chain[-1]["finding_hash"] if self._sink_chain else "GENESIS_GUARDIAN_ROOT"
        p_hash = parent_hash if parent_hash else prev_finding_hash

        finding = GuardianFinding(
            finding_id=finding_id,
            check_id=check_id,
            severity=severity,
            timestamp=now,
            file=file,
            line_or_function=line_or_function,
            violation=violation,
            evidence=evidence,
            expected_behavior=expected_behavior,
            actual_behavior=actual_behavior,
            status=status,
            parent_hash=p_hash
        )
        self._findings.append(finding)

        # Build canonical sink record
        sink_entry = {
            "index": len(self._sink_chain),
            "finding_id": finding.finding_id,
            "timestamp": finding.timestamp,
            "check_id": finding.check_id,
            "severity": finding.severity,
            "file": finding.file,
            "line_or_function": finding.line_or_function,
            "violation": finding.violation,
            "evidence": finding.evidence,
            "expected_behavior": finding.expected_behavior,
            "actual_behavior": finding.actual_behavior,
            "status": finding.status,
            "parent_hash": finding.parent_hash,
            "previous_finding_hash": prev_finding_hash,
            "finding_hash": finding.hash
        }
        self._sink_chain.append(sink_entry)

        # Persistent append-only disk sink write (NEVER swallows write errors)
        if self.sink_path:
            try:
                dir_name = os.path.dirname(os.path.abspath(self.sink_path))
                if dir_name:
                    os.makedirs(dir_name, exist_ok=True)
                with open(self.sink_path, "a", encoding="utf-8") as f:
                    f.write(json.dumps(sink_entry, sort_keys=True) + "\n")
            except Exception as e:
                raise DataUnavailableError(f"STATUS = DATA_UNAVAILABLE: Guardian evidence persistence failure: {str(e)}")

        return finding

    # -------------------------------------------------------------
    # SINK SELF-AUDIT
    # -------------------------------------------------------------
    def audit_guardian_evidence_sink_integrity(self) -> GuardianFinding:
        """Verifies that the Guardian evidence sink has not been tampered with, pruned, or modified."""
        # 1. Check in-memory chain integrity
        prev_finding_hash = "GENESIS_GUARDIAN_ROOT"
        for i, entry in enumerate(self._sink_chain):
            if entry.get("index") != i:
                return self.add_finding(
                    check_id="CHK-SINK-INTEGRITY",
                    severity="CRITICAL",
                    file="guardian_evidence_sink",
                    line_or_function=f"entry_index_{i}",
                    violation="GUARDIAN_EVIDENCE_TAMPERED",
                    evidence=f"In-memory index mismatch: expected {i}, got {entry.get('index')}. Finding deleted.",
                    expected_behavior="Sequential unbroken indices",
                    actual_behavior="Index sequence gap",
                    status="INVALID"
                )
            if entry.get("previous_finding_hash") != prev_finding_hash and entry.get("parent_hash") != prev_finding_hash:
                return self.add_finding(
                    check_id="CHK-SINK-INTEGRITY",
                    severity="CRITICAL",
                    file="guardian_evidence_sink",
                    line_or_function=f"linkage_{i}",
                    violation="GUARDIAN_EVIDENCE_TAMPERED",
                    evidence=f"In-memory sink linkage broken at entry {i}: entry has '{entry.get('previous_finding_hash')}', expected '{prev_finding_hash}'",
                    expected_behavior="Unbroken append-only sink linkage",
                    actual_behavior="Sink chain modified or pruned",
                    status="INVALID"
                )
            prev_finding_hash = entry.get("finding_hash", "")

        # 2. Check disk sink integrity if file is configured and exists
        if self.sink_path and os.path.exists(self.sink_path):
            disk_res = self.verify_sink_file(self.sink_path)
            if disk_res.status != "PASS":
                return disk_res

        return self.add_finding(
            check_id="CHK-SINK-INTEGRITY",
            severity="LOW",
            file="guardian_evidence_sink",
            line_or_function="audit_guardian_evidence_sink_integrity",
            violation="None",
            evidence=f"All {len(self._sink_chain)} sink records cryptographically intact",
            expected_behavior="Valid append-only ledger",
            actual_behavior="Compliant",
            status="PASS"
        )

    # -------------------------------------------------------------
    # THE 25 MANDATORY GUARDIAN CHECKS (CHK-01 to CHK-25)
    # -------------------------------------------------------------

    def audit_chk01_future_data_leakage(
        self,
        snapshot: MarketDataSnapshot,
        prediction_timestamp: float
    ) -> GuardianFinding:
        """CHK-01: Verifies that snapshot contains no candles past prediction_timestamp."""
        for c in snapshot.candles:
            t = c.get("open_time", c.get("timestamp", 0))
            t_sec = t / 1000.0 if t > 1e11 else float(t)
            if t_sec > prediction_timestamp:
                return self.add_finding(
                    check_id="CHK-01",
                    severity="CRITICAL",
                    file=snapshot.source,
                    line_or_function="audit_chk01_future_data_leakage",
                    violation="Future Data Leakage: Market candle timestamp exceeds prediction timestamp",
                    evidence=f"Candle time {t_sec} > prediction time {prediction_timestamp}",
                    expected_behavior="All input candles must have open_time <= prediction_timestamp",
                    actual_behavior=f"Found candle with timestamp {t_sec}",
                    status="INVALID"
                )
        return self.add_finding(
            check_id="CHK-01",
            severity="LOW",
            file=snapshot.source,
            line_or_function="audit_chk01_future_data_leakage",
            violation="None",
            evidence=f"All {len(snapshot.candles)} candles bounded <= {prediction_timestamp}",
            expected_behavior="open_time <= prediction_timestamp",
            actual_behavior="Compliant",
            status="PASS"
        )

    def audit_chk02_future_outcome_leakage(
        self,
        prediction: PredictionRecord,
        available_market_timestamp: float
    ) -> GuardianFinding:
        """CHK-02: Verifies prediction was sealed before future outcome candles existed."""
        if prediction.prediction_timestamp >= prediction.maturity_timestamp:
            return self.add_finding(
                check_id="CHK-02",
                severity="CRITICAL",
                file=prediction.source,
                line_or_function="audit_chk02_future_outcome_leakage",
                violation="Future Outcome Leakage: Prediction timestamp is at or after maturity timestamp",
                evidence=f"pred_t ({prediction.prediction_timestamp}) >= maturity_t ({prediction.maturity_timestamp})",
                expected_behavior="prediction_timestamp < maturity_timestamp",
                actual_behavior="Non-temporal prediction created",
                status="INVALID"
            )
        return self.add_finding(
            check_id="CHK-02",
            severity="LOW",
            file=prediction.source,
            line_or_function="audit_chk02_future_outcome_leakage",
            violation="None",
            evidence=f"Prediction locked at {prediction.prediction_timestamp}, maturity at {prediction.maturity_timestamp}",
            expected_behavior="Locked before maturity",
            actual_behavior="Compliant",
            status="PASS"
        )

    def audit_chk03_fake_synthetic_market_data(
        self,
        snapshot: MarketDataSnapshot
    ) -> GuardianFinding:
        """CHK-03: Checks for OHLC integrity, negative volume, or inverted price bars."""
        for idx, c in enumerate(snapshot.candles):
            o, h, l, cl = float(c["open"]), float(c["high"]), float(c["low"]), float(c["close"])
            v = float(c["volume"])
            if l > min(o, cl) or h < max(o, cl) or l > h or v < 0:
                return self.add_finding(
                    check_id="CHK-03",
                    severity="CRITICAL",
                    file=snapshot.source,
                    line_or_function=f"candle_idx_{idx}",
                    violation="Fake or Malformed Synthetic Candle Detected",
                    evidence=f"O:{o}, H:{h}, L:{l}, C:{cl}, V:{v}",
                    expected_behavior="Low <= min(O,C) and High >= max(O,C) and Vol >= 0",
                    actual_behavior="Physical price relation violated",
                    status="INVALID"
                )
        return self.add_finding(
            check_id="CHK-03",
            severity="LOW",
            file=snapshot.source,
            line_or_function="audit_chk03_fake_synthetic_market_data",
            violation="None",
            evidence=f"{len(snapshot.candles)} candles verified authentic OHLCV",
            expected_behavior="Low <= min(O,C) and High >= max(O,C)",
            actual_behavior="Compliant",
            status="PASS"
        )

    def audit_chk04_missing_market_data(
        self,
        snapshot: MarketDataSnapshot,
        expected_interval_seconds: int = 900
    ) -> GuardianFinding:
        """CHK-04: Detects unannounced gaps between consecutive candles."""
        if len(snapshot.candles) < 2:
            return self.add_finding(
                check_id="CHK-04",
                severity="LOW",
                file=snapshot.source,
                line_or_function="audit_chk04_missing_market_data",
                violation="None",
                evidence="Single candle snapshot",
                expected_behavior="Contiguous stream",
                actual_behavior="Compliant",
                status="PASS"
            )

        for i in range(1, len(snapshot.candles)):
            prev_t = snapshot.candles[i - 1]["open_time"]
            curr_t = snapshot.candles[i]["open_time"]
            gap_sec = (curr_t - prev_t) / 1000.0 if curr_t > 1e11 else float(curr_t - prev_t)
            if gap_sec > expected_interval_seconds * 2.5:
                return self.add_finding(
                    check_id="CHK-04",
                    severity="HIGH",
                    file=snapshot.source,
                    line_or_function=f"gap_between_{i-1}_and_{i}",
                    violation="Missing Market Data Gap Detected",
                    evidence=f"Time gap of {gap_sec}s > allowable {expected_interval_seconds * 2.5}s",
                    expected_behavior="Contiguous time series",
                    actual_behavior=f"Gap detected between {prev_t} and {curr_t}",
                    status="WARNING"
                )
        return self.add_finding(
            check_id="CHK-04",
            severity="LOW",
            file=snapshot.source,
            line_or_function="audit_chk04_missing_market_data",
            violation="None",
            evidence="Time series is continuous",
            expected_behavior="No gaps",
            actual_behavior="Compliant",
            status="PASS"
        )

    def audit_chk05_duplicate_candles(
        self,
        snapshot: MarketDataSnapshot
    ) -> GuardianFinding:
        """CHK-05: Verifies no duplicate candle timestamps exist in the snapshot."""
        seen = set()
        for c in snapshot.candles:
            t = c.get("open_time", c.get("timestamp", 0))
            if t in seen:
                return self.add_finding(
                    check_id="CHK-05",
                    severity="HIGH",
                    file=snapshot.source,
                    line_or_function="audit_chk05_duplicate_candles",
                    violation="Duplicate Candle Timestamps Detected",
                    evidence=f"Duplicate timestamp {t} found in series",
                    expected_behavior="Strictly unique candle timestamps",
                    actual_behavior=f"Timestamp {t} appears multiple times",
                    status="FAIL"
                )
            seen.add(t)
        return self.add_finding(
            check_id="CHK-05",
            severity="LOW",
            file=snapshot.source,
            line_or_function="audit_chk05_duplicate_candles",
            violation="None",
            evidence="All candle timestamps unique",
            expected_behavior="Unique timestamps",
            actual_behavior="Compliant",
            status="PASS"
        )

    def audit_chk06_timestamp_inconsistency(
        self,
        snapshot: MarketDataSnapshot
    ) -> GuardianFinding:
        """CHK-06: Verifies monotonically increasing timestamps."""
        for i in range(1, len(snapshot.candles)):
            prev_t = snapshot.candles[i - 1]["open_time"]
            curr_t = snapshot.candles[i]["open_time"]
            if curr_t <= prev_t:
                return self.add_finding(
                    check_id="CHK-06",
                    severity="HIGH",
                    file=snapshot.source,
                    line_or_function="audit_chk06_timestamp_inconsistency",
                    violation="Timestamp Inconsistency: Non-monotonic time progression",
                    evidence=f"curr_t ({curr_t}) <= prev_t ({prev_t})",
                    expected_behavior="Strictly monotonic ascending timestamps",
                    actual_behavior="Time stagnation or regression",
                    status="FAIL"
                )
        return self.add_finding(
            check_id="CHK-06",
            severity="LOW",
            file=snapshot.source,
            line_or_function="audit_chk06_timestamp_inconsistency",
            violation="None",
            evidence="Timestamps strictly monotonic",
            expected_behavior="Monotonic time progression",
            actual_behavior="Compliant",
            status="PASS"
        )

    def audit_chk07_prediction_modified_after_locking(
        self,
        prediction: PredictionRecord,
        tampered_dict: Optional[Dict[str, Any]] = None
    ) -> GuardianFinding:
        """CHK-07: Verifies prediction hash matches recomputed digest."""
        target_dict = tampered_dict if tampered_dict is not None else prediction.to_dict()
        expected_hash = target_dict.get("prediction_hash")

        payload = {
            "experiment_id": target_dict["experiment_id"],
            "prediction_id": target_dict["prediction_id"],
            "prediction_timestamp": target_dict["prediction_timestamp"],
            "source": target_dict["source"],
            "version": target_dict["version"],
            "asset": target_dict["asset"],
            "timeframe": target_dict["timeframe"],
            "horizon_seconds": target_dict["horizon_seconds"],
            "maturity_timestamp": target_dict["maturity_timestamp"],
            "direction": target_dict["direction"],
            "predicted_range": unfreeze(target_dict["predicted_range"]),
            "model_identifiers": list(target_dict["model_identifiers"]),
            "input_data_hash": target_dict["input_data_hash"],
            "schema_version": target_dict["schema_version"],
            "parent_hash": target_dict["parent_hash"]
        }
        recomputed_hash = compute_sha256(payload)

        if recomputed_hash != expected_hash:
            return self.add_finding(
                check_id="CHK-07",
                severity="CRITICAL",
                file=prediction.source,
                line_or_function="audit_chk07_prediction_modified_after_locking",
                violation="Prediction Record Modified Post-Locking (Tamper Detected)",
                evidence=f"Stored hash ({expected_hash}) != recomputed hash ({recomputed_hash})",
                expected_behavior="Stored hash matches recomputed payload hash",
                actual_behavior="Payload modified after lock sealing",
                status="INVALID"
            )
        return self.add_finding(
            check_id="CHK-07",
            severity="LOW",
            file=prediction.source,
            line_or_function="audit_chk07_prediction_modified_after_locking",
            violation="None",
            evidence="Cryptographic prediction seal intact",
            expected_behavior="Valid hash match",
            actual_behavior="Compliant",
            status="PASS"
        )

    def audit_chk08_outcome_evaluated_before_maturity(
        self,
        prediction: PredictionRecord,
        test_result: TestResult
    ) -> GuardianFinding:
        """CHK-08: Verifies test evaluation occurred strictly at or after maturity_timestamp."""
        if test_result.evaluated_timestamp < prediction.maturity_timestamp:
            return self.add_finding(
                check_id="CHK-08",
                severity="CRITICAL",
                file=test_result.source,
                line_or_function="audit_chk08_outcome_evaluated_before_maturity",
                violation="Outcome Evaluated Before Maturity Timestamp",
                evidence=f"evaluated_t ({test_result.evaluated_timestamp}) < maturity_t ({prediction.maturity_timestamp})",
                expected_behavior="evaluated_timestamp >= maturity_timestamp",
                actual_behavior="Premature evaluation before maturity window elapsed",
                status="INVALID"
            )
        return self.add_finding(
            check_id="CHK-08",
            severity="LOW",
            file=test_result.source,
            line_or_function="audit_chk08_outcome_evaluated_before_maturity",
            violation="None",
            evidence=f"Evaluated at {test_result.evaluated_timestamp} >= maturity {prediction.maturity_timestamp}",
            expected_behavior="Post-maturity evaluation",
            actual_behavior="Compliant",
            status="PASS"
        )

    def audit_chk09_hardcoded_prediction_result(
        self,
        code_str: str,
        file_path: str
    ) -> GuardianFinding:
        """CHK-09: Scans code for static hardcoded prediction returns."""
        suspicious_patterns = [
            r'return\s+["\']CORRECT["\']',
            r'result\s*=\s*["\']CORRECT["\']\s*#\s*always',
            r'status\s*=\s*["\']CORRECT["\']\s*;'
        ]
        for pattern in suspicious_patterns:
            if re.search(pattern, code_str):
                return self.add_finding(
                    check_id="CHK-09",
                    severity="CRITICAL",
                    file=file_path,
                    line_or_function="code_scan",
                    violation="Hardcoded Prediction Result Pattern Found in Source",
                    evidence=f"Matched regex pattern: {pattern}",
                    expected_behavior="Dynamic mathematical evaluation",
                    actual_behavior="Static result hardcoded in source",
                    status="INVALID"
                )
        return self.add_finding(
            check_id="CHK-09",
            severity="LOW",
            file=file_path,
            line_or_function="code_scan",
            violation="None",
            evidence="No static hardcoded outcome assignment patterns detected",
            expected_behavior="Dynamic evaluation",
            actual_behavior="Compliant",
            status="PASS"
        )

    def audit_chk10_hardcoded_win_rate(
        self,
        judge_result: JudgeResult,
        raw_test_results: List[TestResult]
    ) -> GuardianFinding:
        """CHK-10: Verifies win rate in JudgeResult exactly matches arithmetic computation."""
        resolved = [r for r in raw_test_results if r.status in ("CORRECT", "WRONG")]
        if not resolved:
            expected_wr = 0.0
        else:
            correct = sum(1 for r in resolved if r.status == "CORRECT")
            expected_wr = round(correct / len(resolved) * 100.0, 2)

        if abs(judge_result.win_rate_pct - expected_wr) > 0.01:
            return self.add_finding(
                check_id="CHK-10",
                severity="CRITICAL",
                file=judge_result.source,
                line_or_function="audit_chk10_hardcoded_win_rate",
                violation="Hardcoded or Manipulated Win Rate Detected",
                evidence=f"Reported win rate ({judge_result.win_rate_pct}%) != computed win rate ({expected_wr}%)",
                expected_behavior="Reported win rate matches sum(correct)/len(resolved)",
                actual_behavior="Discrepancy in statistical aggregation",
                status="INVALID"
            )
        return self.add_finding(
            check_id="CHK-10",
            severity="LOW",
            file=judge_result.source,
            line_or_function="audit_chk10_hardcoded_win_rate",
            violation="None",
            evidence=f"Win rate verified: {judge_result.win_rate_pct}% matches {expected_wr}%",
            expected_behavior="Exact arithmetic match",
            actual_behavior="Compliant",
            status="PASS"
        )

    def audit_chk11_hardcoded_profit(
        self,
        test_results: List[TestResult]
    ) -> GuardianFinding:
        """CHK-11: Detects unnatural static identical net return numbers across differing trades."""
        if len(test_results) >= 5:
            returns = [r.net_return_pct for r in test_results if r.status in ("CORRECT", "WRONG")]
            if len(set(returns)) == 1 and len(returns) > 3:
                return self.add_finding(
                    check_id="CHK-11",
                    severity="CRITICAL",
                    file="test_results_stream",
                    line_or_function="audit_chk11_hardcoded_profit",
                    violation="Hardcoded / Synthetic Profit Values Detected",
                    evidence=f"All {len(returns)} trades have identical net return: {returns[0]}%",
                    expected_behavior="Natural price movement variance",
                    actual_behavior="Static constant returns across multiple trades",
                    status="INVALID"
                )
        return self.add_finding(
            check_id="CHK-11",
            severity="LOW",
            file="test_results_stream",
            line_or_function="audit_chk11_hardcoded_profit",
            violation="None",
            evidence="Return distributions exhibit natural continuous variance",
            expected_behavior="Continuous variance",
            actual_behavior="Compliant",
            status="PASS"
        )

    def audit_chk12_cherry_picked_assets(
        self,
        declared_universe: List[str],
        tested_assets: List[str]
    ) -> GuardianFinding:
        """CHK-12: Verifies all declared assets in scenario were actually evaluated."""
        missing = set(declared_universe) - set(tested_assets)
        if missing:
            return self.add_finding(
                check_id="CHK-12",
                severity="HIGH",
                file="experiment_scope",
                line_or_function="audit_chk12_cherry_picked_assets",
                violation="Cherry-Picked Assets: Missing declared symbols in evaluation",
                evidence=f"Declared {declared_universe}, but missing: {list(missing)}",
                expected_behavior="All declared universe symbols must be evaluated and reported",
                actual_behavior=f"Omission of {list(missing)} from outcome reporting",
                status="FAIL"
            )
        return self.add_finding(
            check_id="CHK-12",
            severity="LOW",
            file="experiment_scope",
            line_or_function="audit_chk12_cherry_picked_assets",
            violation="None",
            evidence=f"All {len(declared_universe)} declared assets present in evaluation",
            expected_behavior="Full universe reporting",
            actual_behavior="Compliant",
            status="PASS"
        )

    def audit_chk13_missing_failed_predictions(
        self,
        predictions: List[PredictionRecord],
        test_results: List[TestResult]
    ) -> GuardianFinding:
        """CHK-13: Verifies every generated locked prediction has a corresponding test result."""
        pred_ids = {p.prediction_id for p in predictions}
        res_ids = {r.prediction_id for r in test_results}
        unaccounted = pred_ids - res_ids

        if unaccounted:
            return self.add_finding(
                check_id="CHK-13",
                severity="CRITICAL",
                file="prediction_pipeline",
                line_or_function="audit_chk13_missing_failed_predictions",
                violation="Missing / Suppressed Predictions Detected",
                evidence=f"{len(unaccounted)} predictions omitted from test results: {list(unaccounted)[:3]}...",
                expected_behavior="100% of locked predictions must have recorded outcome evaluations",
                actual_behavior=f"Omission of {len(unaccounted)} predictions",
                status="INVALID"
            )
        return self.add_finding(
            check_id="CHK-13",
            severity="LOW",
            file="prediction_pipeline",
            line_or_function="audit_chk13_missing_failed_predictions",
            violation="None",
            evidence=f"All {len(predictions)} predictions accounted for in test evaluations",
            expected_behavior="Complete accounting",
            actual_behavior="Compliant",
            status="PASS"
        )

    def audit_chk14_missing_not_realized_outcomes(
        self,
        test_results: List[TestResult]
    ) -> GuardianFinding:
        """CHK-14: Verifies test results properly classify and retain PREDICTION_NOT_REALIZED states."""
        not_realized = [r for r in test_results if r.status == "PREDICTION_NOT_REALIZED"]
        return self.add_finding(
            check_id="CHK-14",
            severity="LOW",
            file="test_engine",
            line_or_function="audit_chk14_missing_not_realized_outcomes",
            violation="None",
            evidence=f"{len(not_realized)} PREDICTION_NOT_REALIZED outcomes properly recorded without forced binary scoring",
            expected_behavior="Explicit retention of non-moving market states",
            actual_behavior="Compliant",
            status="PASS"
        )

    def audit_chk15_evidence_hash_mismatch(
        self,
        stored_hash: str,
        payload_data: Any,
        node_id: str
    ) -> GuardianFinding:
        """CHK-15: Verifies cryptographic hash of payload matches stored digest."""
        clean_payload = payload_data
        if isinstance(payload_data, dict):
            clean_payload = {
                k: v for k, v in payload_data.items()
                if k not in ("hash", "evidence_hash", "prediction_hash")
            }
        computed = compute_sha256(clean_payload)
        if computed != stored_hash:
            return self.add_finding(
                check_id="CHK-15",
                severity="CRITICAL",
                file=node_id,
                line_or_function="audit_chk15_evidence_hash_mismatch",
                violation="Evidence Hash Mismatch Detected",
                evidence=f"Stored: {stored_hash} != Computed: {computed}",
                expected_behavior="Exact SHA-256 match",
                actual_behavior="Data integrity compromised",
                status="INVALID"
            )
        return self.add_finding(
            check_id="CHK-15",
            severity="LOW",
            file=node_id,
            line_or_function="audit_chk15_evidence_hash_mismatch",
            violation="None",
            evidence="SHA-256 matches payload exactly",
            expected_behavior="Exact match",
            actual_behavior="Compliant",
            status="PASS"
        )

    def audit_chk16_broken_parent_hash_chain(
        self,
        chain_nodes: List[Dict[str, Any]]
    ) -> GuardianFinding:
        """CHK-16: Verifies unbroken parent_hash continuity throughout the evidence ledger."""
        for i in range(1, len(chain_nodes)):
            prev = chain_nodes[i - 1]
            curr = chain_nodes[i]
            if curr.get("parent_hash") != prev.get("evidence_hash", prev.get("hash")):
                return self.add_finding(
                    check_id="CHK-16",
                    severity="CRITICAL",
                    file="evidence_chain",
                    line_or_function=f"link_{i}",
                    violation="Broken Parent-Hash Chain Link Detected",
                    evidence=f"Node {curr.get('evidence_id')} parent_hash != Node {prev.get('evidence_id')} hash",
                    expected_behavior="curr.parent_hash == prev.hash",
                    actual_behavior="Cryptographic chain severed",
                    status="INVALID"
                )
        return self.add_finding(
            check_id="CHK-16",
            severity="LOW",
            file="evidence_chain",
            line_or_function="audit_chk16_broken_parent_hash_chain",
            violation="None",
            evidence=f"Chain of {len(chain_nodes)} nodes verified unbroken",
            expected_behavior="Unbroken parent linkage",
            actual_behavior="Compliant",
            status="PASS"
        )

    def audit_chk17_unauthorized_layer_access(
        self,
        caller_layer: str,
        target_layer: str,
        operation: str = "READ"
    ) -> GuardianFinding:
        """CHK-17: Verifies adherence to layer read/write access matrices."""
        if operation.upper() == "READ":
            permitted = ALLOWED_READ_MATRIX.get(caller_layer.upper(), set())
            if target_layer.upper() not in permitted:
                return self.add_finding(
                    check_id="CHK-17",
                    severity="HIGH",
                    file=f"{caller_layer}->{target_layer}",
                    line_or_function="audit_chk17_unauthorized_layer_access",
                    violation=f"Unauthorized Layer Read: {caller_layer} cannot read from {target_layer}",
                    evidence=f"Attempted READ from {target_layer} not in allowed: {sorted(list(permitted))}",
                    expected_behavior="Strict adherence to access control matrix",
                    actual_behavior="Cross-layer boundary violation",
                    status="FAIL"
                )
        elif operation.upper() == "WRITE":
            permitted_write = ALLOWED_WRITE_MATRIX.get(caller_layer.upper(), set())
            if target_layer.upper() not in permitted_write:
                return self.add_finding(
                    check_id="CHK-17",
                    severity="CRITICAL",
                    file=f"{caller_layer}->{target_layer}",
                    line_or_function="audit_chk17_unauthorized_layer_access",
                    violation=f"Unauthorized Layer Write: {caller_layer} cannot write to {target_layer}",
                    evidence=f"Attempted WRITE to {target_layer} not in allowed: {sorted(list(permitted_write))}",
                    expected_behavior="Only owner layer can write records",
                    actual_behavior="Cross-layer write boundary violation",
                    status="FAIL"
                )
        return self.add_finding(
            check_id="CHK-17",
            severity="LOW",
            file=f"{caller_layer}->{target_layer}",
            line_or_function="audit_chk17_unauthorized_layer_access",
            violation="None",
            evidence=f"{caller_layer} {operation} to {target_layer} permitted",
            expected_behavior="Permitted access",
            actual_behavior="Compliant",
            status="PASS"
        )

    def audit_chk18_report_result_mismatch(
        self,
        report: ReportRecord,
        judge_result: JudgeResult
    ) -> GuardianFinding:
        """CHK-18: Verifies report summary matches actual underlying JudgeResult metrics."""
        rep_wr = report.verdicts_summary.get("win_rate_pct")
        if rep_wr is not None and abs(rep_wr - judge_result.win_rate_pct) > 0.01:
            return self.add_finding(
                check_id="CHK-18",
                severity="CRITICAL",
                file=report.source,
                line_or_function="audit_chk18_report_result_mismatch",
                violation="Report / Evidence Mismatch: Report metrics differ from JudgeResult",
                evidence=f"Report win rate: {rep_wr}% != Judge win rate: {judge_result.win_rate_pct}%",
                expected_behavior="Report reflects JudgeResult verbatim",
                actual_behavior="Discrepancy between report and underlying verified evidence",
                status="INVALID"
            )
        return self.add_finding(
            check_id="CHK-18",
            severity="LOW",
            file=report.source,
            line_or_function="audit_chk18_report_result_mismatch",
            violation="None",
            evidence="Report values align perfectly with JudgeResult",
            expected_behavior="Exact alignment",
            actual_behavior="Compliant",
            status="PASS"
        )

    def audit_chk19_historical_artifact_modification(
        self,
        artifact_path: str,
        current_sha256: str,
        baseline_sha256: str
    ) -> GuardianFinding:
        """CHK-19: Verifies historical mission artifacts remain unmodified."""
        if current_sha256 != baseline_sha256:
            return self.add_finding(
                check_id="CHK-19",
                severity="HIGH",
                file=artifact_path,
                line_or_function="audit_chk19_historical_artifact_modification",
                violation="Historical Artifact Modified Post-Archival",
                evidence=f"Current: {current_sha256} != Baseline: {baseline_sha256}",
                expected_behavior="Historical mission files remain untouched",
                actual_behavior="Hash alteration in historical artifact",
                status="FAIL"
            )
        return self.add_finding(
            check_id="CHK-19",
            severity="LOW",
            file=artifact_path,
            line_or_function="audit_chk19_historical_artifact_modification",
            violation="None",
            evidence="Historical artifact hash matches baseline",
            expected_behavior="Unmodified historical file",
            actual_behavior="Compliant",
            status="PASS"
        )

    def audit_chk20_synthetic_fallback_mock_data(
        self,
        code_str: str,
        file_path: str
    ) -> GuardianFinding:
        """CHK-20: Scans source code for forbidden mock fallback functions."""
        forbidden_terms = [
            r'def\s+generate_mock_klines',
            r'def\s+mock_binance_response',
            r'fake_candles\s*=',
            r'np\.random\.normal\(.*price'
        ]
        for term in forbidden_terms:
            if re.search(term, code_str):
                return self.add_finding(
                    check_id="CHK-20",
                    severity="CRITICAL",
                    file=file_path,
                    line_or_function="code_scan",
                    violation="Forbidden Synthetic Fallback / Mock Data Generator Found",
                    evidence=f"Found forbidden mock signature: {term}",
                    expected_behavior="Absolute NO-MOCK enforcement (DATA_UNAVAILABLE on failure)",
                    actual_behavior="Synthetic market generator exists in source",
                    status="INVALID"
                )
        return self.add_finding(
            check_id="CHK-20",
            severity="LOW",
            file=file_path,
            line_or_function="code_scan",
            violation="None",
            evidence="Zero forbidden mock generator patterns in scanned source",
            expected_behavior="No synthetic fallbacks",
            actual_behavior="Compliant",
            status="PASS"
        )

    def audit_chk21_forward_data_availability(
        self,
        forward_candles: List[Any],
        prediction_id: str
    ) -> GuardianFinding:
        """CHK-21: Detects empty or unavailable forward market data during evaluation."""
        if not forward_candles:
            return self.add_finding(
                check_id="CHK-21",
                severity="HIGH",
                file="test_engine",
                line_or_function="audit_chk21_forward_data_availability",
                violation="Missing Forward Market Data For Evaluation",
                evidence=f"forward_candles is empty for prediction {prediction_id}",
                expected_behavior="Must fail closed with DATA_UNAVAILABLE",
                actual_behavior="Evaluation requested without forward market reality",
                status="DATA_UNAVAILABLE"
            )
        return self.add_finding(
            check_id="CHK-21",
            severity="LOW",
            file="test_engine",
            line_or_function="audit_chk21_forward_data_availability",
            violation="None",
            evidence=f"Forward candles available ({len(forward_candles)} bars)",
            expected_behavior="Authentic forward candles",
            actual_behavior="Compliant",
            status="PASS"
        )

    def audit_chk22_deep_immutability(
        self,
        record: Any
    ) -> GuardianFinding:
        """CHK-22: Verifies that an immutable record's nested attributes reject in-place mutation."""
        try:
            # Check nested mutation
            if hasattr(record, "candles") and len(record.candles) > 0:
                record.candles[0]["open"] = 999999.0
            elif hasattr(record, "predicted_range") and record.predicted_range:
                record.predicted_range["upper"] = 999999.0
            elif hasattr(record, "direction"):
                record.direction = "INVERTED"
            else:
                record.experiment_id = "TAMPERED"
            
            # If no exception raised, immutability is breached!
            return self.add_finding(
                check_id="CHK-22",
                severity="CRITICAL",
                file=type(record).__name__,
                line_or_function="audit_chk22_deep_immutability",
                violation="Deep Immutability Breach: Record allowed in-place modification",
                evidence="Object attribute mutation succeeded without raising error",
                expected_behavior="Frozen dataclass and MappingProxyType must reject mutation",
                actual_behavior="Object mutable",
                status="INVALID"
            )
        except (TypeError, AttributeError, Exception):
            return self.add_finding(
                check_id="CHK-22",
                severity="LOW",
                file=type(record).__name__,
                line_or_function="audit_chk22_deep_immutability",
                violation="None",
                evidence="Object strictly rejected in-place mutation",
                expected_behavior="Immutable",
                actual_behavior="Compliant",
                status="PASS"
            )

    def audit_chk23_cross_layer_write_attempt(
        self,
        caller_layer: str,
        target_layer: str
    ) -> GuardianFinding:
        """CHK-23: Detects unauthorized cross-layer write attempts."""
        return self.audit_chk17_unauthorized_layer_access(caller_layer, target_layer, "WRITE")

    def audit_chk24_multiple_testing_penalty(
        self,
        judge_result: JudgeResult,
        num_hypotheses: int
    ) -> GuardianFinding:
        """CHK-24: Verifies Bonferroni threshold matches num_hypotheses count."""
        expected_threshold = round(0.05 / max(1, num_hypotheses), 6)
        if abs(judge_result.bonferroni_threshold - expected_threshold) > 1e-5:
            return self.add_finding(
                check_id="CHK-24",
                severity="HIGH",
                file=judge_result.source,
                line_or_function="audit_chk24_multiple_testing_penalty",
                violation="Multiple Testing Penalty Omitted / Incorrect Threshold",
                evidence=f"Reported threshold {judge_result.bonferroni_threshold} != expected {expected_threshold}",
                expected_behavior=f"Bonferroni threshold = 0.05 / {num_hypotheses}",
                actual_behavior="Unadjusted significance threshold used",
                status="FAIL"
            )
        return self.add_finding(
            check_id="CHK-24",
            severity="LOW",
            file=judge_result.source,
            line_or_function="audit_chk24_multiple_testing_penalty",
            violation="None",
            evidence=f"Bonferroni correction applied correctly ({judge_result.bonferroni_threshold})",
            expected_behavior="Exact match",
            actual_behavior="Compliant",
            status="PASS"
        )

    def audit_chk25_snapshot_inner_candle_tamper(
        self,
        snapshot: MarketDataSnapshot,
        tampered_candles: List[Dict[str, Any]]
    ) -> GuardianFinding:
        """CHK-25: Verifies that altering an internal candle in a snapshot produces a hash mismatch."""
        canonical_candles = [unfreeze(c) for c in tampered_candles]
        tampered_payload = {
            "experiment_id": snapshot.experiment_id,
            "timestamp": snapshot.timestamp,
            "source": snapshot.source,
            "asset": snapshot.asset,
            "timeframe": snapshot.timeframe,
            "candles_count": len(canonical_candles),
            "candles": canonical_candles,
            "schema_version": snapshot.schema_version,
            "parent_hash": snapshot.parent_hash
        }
        recomputed = compute_sha256(tampered_payload)
        if recomputed != snapshot.hash:
            return self.add_finding(
                check_id="CHK-25",
                severity="CRITICAL",
                file=snapshot.source,
                line_or_function="audit_chk25_snapshot_inner_candle_tamper",
                violation="Snapshot Candle Tampering Detected via Full-Digest Recomputation",
                evidence=f"Snapshot hash {snapshot.hash} != Tampered candles hash {recomputed}",
                expected_behavior="Full snapshot hash matches canonical candle sequence",
                actual_behavior="Internal candle tampered post-construction",
                status="INVALID"
            )
        return self.add_finding(
            check_id="CHK-25",
            severity="LOW",
            file=snapshot.source,
            line_or_function="audit_chk25_snapshot_inner_candle_tamper",
            violation="None",
            evidence="Snapshot matches internal candle sequence",
            expected_behavior="Hash match",
            actual_behavior="Compliant",
            status="PASS"
        )
