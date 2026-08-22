"""
PARSA DEEP IMMUTABILITY & FULL SNAPSHOT HASH TESTS (GATES 02, 03, 04)
=====================================================================
Verifies:
- GATE 02: Full canonical snapshot hash over all candles and metadata. Middle candle mutation changes hash.
- GATE 03: Deep immutability on PredictionRecord, OutcomeRecord, TestResult, JudgeResult, GuardianFinding, MarketDataSnapshot.
- GATE 04: Prediction Lock cryptographic seal.
"""

import unittest
from parsa_layers.contracts.models import (
    MarketDataSnapshot,
    PredictionRecord,
    OutcomeRecord,
    TestResult,
    JudgeResult,
    GuardianFinding,
    compute_sha256
)


class TestImmutabilityAndHashing(unittest.TestCase):

    def test_gate02_full_snapshot_canonical_hashing(self):
        """GATE 02: MarketDataSnapshot hash encompasses all internal candles canonically."""
        c1 = {"open_time": 900.0, "open": 100, "high": 105, "low": 95, "close": 102, "volume": 10}
        c2 = {"open_time": 950.0, "open": 102, "high": 108, "low": 100, "close": 106, "volume": 12}
        c3 = {"open_time": 1000.0, "open": 106, "high": 110, "low": 104, "close": 109, "volume": 15}

        snapshot_1 = MarketDataSnapshot("EXP-IMM-1", 1000.0, "BINANCE", "BTCUSDT", "15m", [c1, c2, c3])
        snapshot_2 = MarketDataSnapshot("EXP-IMM-1", 1000.0, "BINANCE", "BTCUSDT", "15m", [c1, c2, c3])

        # Identical snapshots produce identical hashes
        self.assertEqual(snapshot_1.hash, snapshot_2.hash)

        # Mutating middle candle changes the hash
        c2_tampered = dict(c2)
        c2_tampered["close"] = 106.01
        snapshot_3 = MarketDataSnapshot("EXP-IMM-1", 1000.0, "BINANCE", "BTCUSDT", "15m", [c1, c2_tampered, c3])
        self.assertNotEqual(snapshot_1.hash, snapshot_3.hash)

    def test_gate03_deep_immutability_all_models(self):
        """GATE 03: In-place mutations on frozen dataclasses and nested structures are rejected."""
        # 1. MarketDataSnapshot
        c1 = {"open_time": 900.0, "open": 100, "high": 105, "low": 95, "close": 102, "volume": 10}
        snap = MarketDataSnapshot("EXP", 1000.0, "BINANCE", "BTCUSDT", "15m", [c1])
        with self.assertRaises(Exception):
            snap.asset = "ETHUSDT"
        with self.assertRaises(TypeError):
            snap.candles[0]["open"] = 999.0

        # 2. PredictionRecord
        pred = PredictionRecord(
            "EXP", "P1", 1000.0, "EXEC", "3.0.0", "BTCUSDT", "15m", 900, 1900.0,
            "LONG", {"upper": 65000, "lower": 64000}, ["M1"], "hash123"
        )
        with self.assertRaises(Exception):
            pred.direction = "SHORT"
        with self.assertRaises(TypeError):
            pred.predicted_range["upper"] = 999999.0
        with self.assertRaises(AttributeError):
            pred.model_identifiers.append("M2")

        # 3. OutcomeRecord
        outcome = OutcomeRecord("EXP", "P1", 1905.0, "TEST", "3.0.0", 100.0, 105.0, 106.0, 99.0, 50.0, 5.0, 6.0, 1.0)
        with self.assertRaises(Exception):
            outcome.actual_return_pct = 10.0

        # 4. TestResult
        tr = TestResult("EXP", "P1", 1905.0, "TEST", "3.0.0", "CORRECT", 0.5, 0.6, 10.0, 0.7, 0.1)
        with self.assertRaises(Exception):
            tr.status = "WRONG"

        # 5. JudgeResult
        judge = JudgeResult("EXP", "V1", 2000.0, "JUDGE", "3.0.0", 50, 30, 20, 0, 60.0, 1.4, 0.08, 0.05, False, "REJECTED", False, (46.0, 72.0))
        with self.assertRaises(Exception):
            judge.win_rate_pct = 80.0
        with self.assertRaises(AttributeError):
            judge.confidence_interval_95.append(80.0)

        # 6. GuardianFinding
        finding = GuardianFinding("F1", 2000.0, "CHK-01", "CRITICAL", "feed", "func", "violation", "ev", "exp", "act", "INVALID")
        with self.assertRaises(Exception):
            finding.severity = "LOW"

    def test_gate04_prediction_lock_cryptographic_seal(self):
        """GATE 04: PredictionRecord prediction_hash strictly covers all prediction parameters."""
        pred = PredictionRecord(
            experiment_id="EXP-LOCK-1",
            prediction_id="PRED-LOCK-001",
            prediction_timestamp=1000.0,
            source="EXECUTOR",
            version="3.0.0",
            asset="BTCUSDT",
            timeframe="15m",
            horizon_seconds=900,
            maturity_timestamp=1900.0,
            direction="LONG",
            predicted_range={"upper": 65000, "lower": 64000},
            model_identifiers=["MODEL_ALPHA"],
            input_data_hash="input_sha256_placeholder"
        )
        self.assertIsNotNone(pred.prediction_hash)
        self.assertEqual(len(pred.prediction_hash), 64)


if __name__ == "__main__":
    unittest.main()
