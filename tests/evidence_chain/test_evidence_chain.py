"""
PARSA IMMUTABLE EVIDENCE CHAIN TESTS (PART 7)
=============================================
Verifies that:
- The 9-stage evidence chain preserves unbroken parent-hash continuity.
- Temporal monotonicity is strictly enforced.
- Any out-of-order insertion or tampering causes immediate failure.
"""

import unittest
import time
from parsa_layers.evidence.evidence_chain import ImmutableEvidenceChain
from parsa_layers.contracts.models import (
    EvidenceChainBrokenError,
    FutureDataAccessViolation
)


class TestEvidenceChain(unittest.TestCase):

    def test_complete_9_stage_evidence_lifecycle(self):
        """Constructs a full 9-stage evidence chain and verifies end-to-end cryptographic integrity."""
        t = 1000.0
        chain = ImmutableEvidenceChain("EXP-LIFECYCLE-001", genesis_timestamp=t)
        t += 5

        # Stage 1: Market Snapshot
        chain.append_stage("MARKET_SNAPSHOT", "EXECUTOR", {"candles_count": 50}, timestamp=t)
        t += 10

        # Stage 2: Prediction
        chain.append_stage("PREDICTION", "EXECUTOR", {"direction": "LONG", "target": 65000}, timestamp=t)
        t += 5

        # Stage 3: Lock
        chain.append_stage("LOCK", "EXECUTOR", {"status": "LOCKED", "sha": "abc"}, timestamp=t)
        t += 900  # 15 minutes later

        # Stage 4: Maturity
        chain.append_stage("MATURITY", "TEST", {"maturity_reached": True}, timestamp=t)
        t += 5

        # Stage 5: Outcome
        chain.append_stage("OUTCOME", "TEST", {"exit_price": 65200, "return": 0.30}, timestamp=t)
        t += 5

        # Stage 6: Test Result
        chain.append_stage("TEST_RESULT", "TEST", {"status": "CORRECT", "net_return": 0.15}, timestamp=t)
        t += 10

        # Stage 7: Judge Result
        chain.append_stage("JUDGE_RESULT", "JUDGES", {"win_rate": 60.0, "verdict": "CANDIDATE"}, timestamp=t)
        t += 5

        # Stage 8: Guardian Result
        chain.append_stage("GUARDIAN_RESULT", "GUARDIAN", {"status": "PASS", "violations": 0}, timestamp=t)
        t += 5

        # Stage 9: Report
        chain.append_stage("REPORT", "REPORT", {"title": "Final Summary", "rendered": True}, timestamp=t)

        self.assertEqual(chain.chain_length, 10)  # Genesis + 9 stages
        self.assertTrue(chain.verify_chain_integrity())

    def test_temporal_regression_rejected(self):
        """Chain must reject events inserted with past timestamps (time going backwards)."""
        chain = ImmutableEvidenceChain("EXP-TIME-001", genesis_timestamp=1000.0)
        chain.append_stage("MARKET_SNAPSHOT", "EXECUTOR", {"data": 1}, timestamp=1000.0)

        with self.assertRaises(FutureDataAccessViolation):
            # Attempt to insert event at t=950 (earlier than 1000.0)
            chain.append_stage("PREDICTION", "EXECUTOR", {"data": 2}, timestamp=950.0)


if __name__ == "__main__":
    unittest.main()
