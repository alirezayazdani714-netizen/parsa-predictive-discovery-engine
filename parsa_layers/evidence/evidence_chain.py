"""
PARSA IMMUTABLE EVIDENCE CHAIN ENGINE (PART 7)
==============================================
Manages and verifies the strict 9-stage cryptographic evidence chain:
GENESIS → MARKET_SNAPSHOT → PREDICTION → LOCK → MATURITY → OUTCOME → TEST_RESULT → JUDGE_RESULT → GUARDIAN_RESULT → REPORT
"""

from typing import List, Dict, Any, Optional
import json
import os
import time
from parsa_layers.contracts.models import (
    EvidenceRecord,
    compute_sha256,
    EvidenceChainBrokenError,
    FutureDataAccessViolation,
    ImmutableRecordViolation
)

STAGES_ORDER = [
    "GENESIS",
    "MARKET_SNAPSHOT",
    "PREDICTION",
    "LOCK",
    "MATURITY",
    "OUTCOME",
    "TEST_RESULT",
    "JUDGE_RESULT",
    "GUARDIAN_RESULT",
    "REPORT"
]


class ImmutableEvidenceChain:
    """Cryptographic ledger enforcing monotonic timestamps and parent hash linkage."""

    def __init__(self, experiment_id: str, genesis_timestamp: Optional[float] = None):
        self.experiment_id = experiment_id
        self._chain: List[EvidenceRecord] = []
        # Initialize Genesis Block
        init_time = genesis_timestamp if genesis_timestamp is not None else time.time()
        genesis_payload = {
            "experiment_id": experiment_id,
            "stage": "GENESIS",
            "timestamp": init_time,
            "source_layer": "SCENARIO"
        }
        genesis_hash = compute_sha256(genesis_payload)
        genesis_node = EvidenceRecord(
            evidence_id=f"EVID-{experiment_id}-GENESIS",
            stage="GENESIS",
            timestamp=init_time,
            source_layer="SCENARIO",
            payload_hash=genesis_hash,
            parent_hash="0" * 64
        )
        self._chain.append(genesis_node)

    @property
    def latest_hash(self) -> str:
        return self._chain[-1].evidence_hash

    @property
    def chain_length(self) -> int:
        return len(self._chain)

    def get_records(self) -> List[EvidenceRecord]:
        return list(self._chain)

    def append_stage(
        self,
        stage: str,
        source_layer: str,
        payload_data: Any,
        timestamp: Optional[float] = None
    ) -> EvidenceRecord:
        """Appends a new evidence stage, validating sequence and temporal monotonicity."""
        if stage not in STAGES_ORDER:
            raise EvidenceChainBrokenError(f"Invalid stage name '{stage}'. Must be one of {STAGES_ORDER}")

        last_node = self._chain[-1]
        current_time = timestamp if timestamp is not None else time.time()

        # Monotonicity check
        if current_time < last_node.timestamp:
            raise FutureDataAccessViolation(
                f"Temporal violation in Evidence Chain: current timestamp {current_time} < parent timestamp {last_node.timestamp}"
            )

        payload_hash = compute_sha256(payload_data)
        evidence_id = f"EVID-{self.experiment_id}-{stage}-{len(self._chain):04d}"

        new_record = EvidenceRecord(
            evidence_id=evidence_id,
            stage=stage,
            timestamp=current_time,
            source_layer=source_layer,
            payload_hash=payload_hash,
            parent_hash=last_node.evidence_hash
        )

        self._chain.append(new_record)
        return new_record

    def verify_chain_integrity(self) -> bool:
        """Verifies that all nodes in the chain maintain unbroken parent hash links and monotonic timestamps."""
        if not self._chain:
            raise EvidenceChainBrokenError("Evidence chain is empty.")

        for i in range(1, len(self._chain)):
            prev_node = self._chain[i - 1]
            curr_node = self._chain[i]

            # Verify parent hash match
            if curr_node.parent_hash != prev_node.evidence_hash:
                raise EvidenceChainBrokenError(
                    f"Hash link broken at node {curr_node.evidence_id}: parent_hash ({curr_node.parent_hash}) != prev_node.evidence_hash ({prev_node.evidence_hash})"
                )

            # Verify temporal monotonicity
            if curr_node.timestamp < prev_node.timestamp:
                raise FutureDataAccessViolation(
                    f"Temporal regression at node {curr_node.evidence_id}: {curr_node.timestamp} < {prev_node.timestamp}"
                )

            # Recompute current node hash
            expected_payload = {
                "evidence_id": curr_node.evidence_id,
                "stage": curr_node.stage,
                "timestamp": curr_node.timestamp,
                "source_layer": curr_node.source_layer,
                "payload_hash": curr_node.payload_hash,
                "parent_hash": curr_node.parent_hash,
                "schema_version": curr_node.schema_version
            }
            expected_hash = compute_sha256(expected_payload)
            if curr_node.evidence_hash != expected_hash:
                raise EvidenceChainBrokenError(
                    f"Evidence node corruption at {curr_node.evidence_id}: evidence_hash mismatch"
                )

        return True

    def export_jsonl(self, filepath: str) -> None:
        """Exports evidence chain to JSONL file."""
        os.makedirs(os.path.dirname(os.path.abspath(filepath)), exist_ok=True)
        with open(filepath, "w") as f:
            for rec in self._chain:
                f.write(json.dumps(rec.to_dict()) + "\n")
