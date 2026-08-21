"""
PARSA ADVERSARIAL GUARDIAN TESTS (PART 9)
=========================================
Deliberately attempts 15 adversarial attacks against the Guardian Inspector:
1. Inject future candles
2. Inject future outcomes
3. Modify a locked prediction
4. Modify an old outcome
5. Replace real candles with synthetic candles (inverted OHLC / negative volume)
6. Insert duplicate candles
7. Remove losing predictions (cherry-picking / suppression)
8. Change a timestamp (non-monotonic)
9. Change a win rate (manipulated summary)
10. Change a final report (mismatch)
11. Bypass the Executor (unauthorized layer read)
12. Bypass the Test layer (premature scoring before maturity)
13. Bypass the Guardian (tampered evidence hash)
14. Introduce a hardcoded prediction (source pattern)
15. Introduce a hardcoded score / profit

Guardian MUST detect and flag every attack with FAIL or INVALID.
"""

import unittest
import time
from parsa_layers.contracts.models import (
    MarketDataSnapshot,
    PredictionRecord,
    OutcomeRecord,
    TestResult,
    JudgeResult,
    ReportRecord,
    compute_sha256
)
from parsa_layers.guardian.guardian_engine import GuardianEngine


class TestAdversarialGuardian(unittest.TestCase):

    def setUp(self):
        self.guardian = GuardianEngine()

    def test_attack_01_inject_future_candles(self):
        """Attack 1: Inject future candles into execution snapshot."""
        pred_time = 1000.0
        snapshot = MarketDataSnapshot(
            experiment_id="EXP-ATK-1",
            timestamp=pred_time,
            source="TEST_FEED",
            asset="BTCUSDT",
            timeframe="15m",
            candles=[
                {"open_time": 900.0, "open": 100, "high": 105, "low": 95, "close": 102, "volume": 10},
                {"open_time": 950.0, "open": 102, "high": 108, "low": 100, "close": 106, "volume": 12}
            ]
        )
        # Verify clean pass
        res_pass = self.guardian.audit_chk01_future_data_leakage(snapshot, pred_time)
        self.assertEqual(res_pass.status, "PASS")

        # Now attack with future candle
        bad_snapshot = MarketDataSnapshot.__new__(MarketDataSnapshot)
        object.__setattr__(bad_snapshot, "experiment_id", "EXP-ATK-1")
        object.__setattr__(bad_snapshot, "timestamp", pred_time)
        object.__setattr__(bad_snapshot, "source", "TEST_FEED")
        object.__setattr__(bad_snapshot, "asset", "BTCUSDT")
        object.__setattr__(bad_snapshot, "timeframe", "15m")
        object.__setattr__(bad_snapshot, "candles", [
            {"open_time": 900.0, "open": 100, "high": 105, "low": 95, "close": 102, "volume": 10},
            {"open_time": 1200.0, "open": 102, "high": 108, "low": 100, "close": 106, "volume": 12}  # Future!
        ])
        object.__setattr__(bad_snapshot, "schema_version", "2.0.0")
        object.__setattr__(bad_snapshot, "parent_hash", "")
        object.__setattr__(bad_snapshot, "hash", "dummy")

        res_fail = self.guardian.audit_chk01_future_data_leakage(bad_snapshot, pred_time)
        self.assertEqual(res_fail.status, "INVALID")
        self.assertEqual(res_fail.severity, "CRITICAL")

    def test_attack_02_inject_future_outcomes(self):
        """Attack 2: Prediction created after or at maturity."""
        pred = PredictionRecord(
            experiment_id="EXP-ATK-2",
            prediction_id="P1",
            prediction_timestamp=1000.0,
            source="EXEC",
            version="2.0.0",
            asset="BTCUSDT",
            timeframe="15m",
            horizon_seconds=900,
            maturity_timestamp=1900.0,
            direction="LONG",
            predicted_range={"u": 100, "l": 90},
            model_identifiers=["M1"],
            input_data_hash="h1"
        )
        res_pass = self.guardian.audit_chk02_future_outcome_leakage(pred, 950.0)
        self.assertEqual(res_pass.status, "PASS")

    def test_attack_03_modify_locked_prediction(self):
        """Attack 3: Modify a prediction record post-locking."""
        pred = PredictionRecord(
            experiment_id="EXP-ATK-3",
            prediction_id="P1",
            prediction_timestamp=1000.0,
            source="EXEC",
            version="2.0.0",
            asset="BTCUSDT",
            timeframe="15m",
            horizon_seconds=900,
            maturity_timestamp=1900.0,
            direction="LONG",
            predicted_range={"u": 100, "l": 90},
            model_identifiers=["M1"],
            input_data_hash="h1"
        )
        # Clean check
        res_pass = self.guardian.audit_chk07_prediction_modified_after_locking(pred)
        self.assertEqual(res_pass.status, "PASS")

        # Tampered dictionary
        tampered = pred.to_dict()
        tampered["direction"] = "SHORT"  # Tamper!
        res_fail = self.guardian.audit_chk07_prediction_modified_after_locking(pred, tampered)
        self.assertEqual(res_fail.status, "INVALID")
        self.assertEqual(res_fail.severity, "CRITICAL")

    def test_attack_04_modify_old_outcome(self):
        """Attack 4: Hash mismatch in outcome node."""
        outcome = OutcomeRecord(
            experiment_id="EXP-ATK-4",
            prediction_id="P1",
            observed_timestamp=1905.0,
            source="TEST",
            version="2.0.0",
            entry_price=100.0,
            exit_price=105.0,
            high_price=106.0,
            low_price=99.0,
            volume=500.0,
            actual_return_pct=5.0,
            max_favorable_excursion_pct=6.0,
            max_adverse_excursion_pct=1.0
        )
        res_pass = self.guardian.audit_chk15_evidence_hash_mismatch(outcome.hash, outcome.to_dict(), "OUTCOME-NODE")
        self.assertEqual(res_pass.status, "PASS")

        # Tampered payload
        tampered_dict = outcome.to_dict()
        tampered_dict["actual_return_pct"] = 99.0
        res_fail = self.guardian.audit_chk15_evidence_hash_mismatch(outcome.hash, tampered_dict, "OUTCOME-NODE")
        self.assertEqual(res_fail.status, "INVALID")

    def test_attack_05_synthetic_inverted_candles(self):
        """Attack 5: Inverted Low > High or negative volume."""
        bad_snapshot = MarketDataSnapshot.__new__(MarketDataSnapshot)
        object.__setattr__(bad_snapshot, "experiment_id", "EXP-ATK-5")
        object.__setattr__(bad_snapshot, "timestamp", 1000.0)
        object.__setattr__(bad_snapshot, "source", "TEST_FEED")
        object.__setattr__(bad_snapshot, "asset", "BTCUSDT")
        object.__setattr__(bad_snapshot, "timeframe", "15m")
        object.__setattr__(bad_snapshot, "candles", [
            {"open_time": 900.0, "open": 100, "high": 90, "low": 110, "close": 95, "volume": -5}  # Bad!
        ])
        res_fail = self.guardian.audit_chk03_fake_synthetic_market_data(bad_snapshot)
        self.assertEqual(res_fail.status, "INVALID")
        self.assertEqual(res_fail.severity, "CRITICAL")

    def test_attack_06_duplicate_candles(self):
        """Attack 6: Duplicate candle timestamps."""
        bad_snapshot = MarketDataSnapshot.__new__(MarketDataSnapshot)
        object.__setattr__(bad_snapshot, "experiment_id", "EXP-ATK-6")
        object.__setattr__(bad_snapshot, "timestamp", 1000.0)
        object.__setattr__(bad_snapshot, "source", "TEST_FEED")
        object.__setattr__(bad_snapshot, "asset", "BTCUSDT")
        object.__setattr__(bad_snapshot, "timeframe", "15m")
        object.__setattr__(bad_snapshot, "candles", [
            {"open_time": 900.0, "open": 100, "high": 105, "low": 95, "close": 102, "volume": 10},
            {"open_time": 900.0, "open": 102, "high": 108, "low": 100, "close": 106, "volume": 12}  # Duplicate!
        ])
        res_fail = self.guardian.audit_chk05_duplicate_candles(bad_snapshot)
        self.assertEqual(res_fail.status, "FAIL")
        self.assertEqual(res_fail.severity, "HIGH")

    def test_attack_07_remove_losing_predictions(self):
        """Attack 7: Suppress losing predictions from evaluation list."""
        p1 = PredictionRecord("EXP", "P1", 1000, "EXEC", "2.0.0", "BTC", "15m", 900, 1900, "LONG", {}, [], "h1")
        p2 = PredictionRecord("EXP", "P2", 1000, "EXEC", "2.0.0", "BTC", "15m", 900, 1900, "SHORT", {}, [], "h2")

        t1 = TestResult("EXP", "P1", 1905, "TEST", "2.0.0", "CORRECT", 0.5, 0.6, 15, 0.7, 0.1)
        # P2 omitted intentionally!
        res_fail = self.guardian.audit_chk13_missing_failed_predictions([p1, p2], [t1])
        self.assertEqual(res_fail.status, "INVALID")
        self.assertEqual(res_fail.severity, "CRITICAL")

    def test_attack_08_non_monotonic_timestamp(self):
        """Attack 8: Time going backwards in series."""
        bad_snapshot = MarketDataSnapshot.__new__(MarketDataSnapshot)
        object.__setattr__(bad_snapshot, "experiment_id", "EXP-ATK-8")
        object.__setattr__(bad_snapshot, "timestamp", 1000.0)
        object.__setattr__(bad_snapshot, "source", "TEST_FEED")
        object.__setattr__(bad_snapshot, "asset", "BTCUSDT")
        object.__setattr__(bad_snapshot, "timeframe", "15m")
        object.__setattr__(bad_snapshot, "candles", [
            {"open_time": 900.0, "open": 100, "high": 105, "low": 95, "close": 102, "volume": 10},
            {"open_time": 850.0, "open": 102, "high": 108, "low": 100, "close": 106, "volume": 12}  # Regression!
        ])
        res_fail = self.guardian.audit_chk06_timestamp_inconsistency(bad_snapshot)
        self.assertEqual(res_fail.status, "FAIL")

    def test_attack_09_manipulated_win_rate(self):
        """Attack 9: Claiming 80% win rate when raw results show 50%."""
        t1 = TestResult("EXP", "P1", 1905, "TEST", "2.0.0", "CORRECT", 0.5, 0.6, 15, 0.7, 0.1)
        t2 = TestResult("EXP", "P2", 1905, "TEST", "2.0.0", "WRONG", -0.5, -0.4, 15, 0.1, 0.7)

        # True win rate = 50.0%
        fake_judge = JudgeResult(
            experiment_id="EXP",
            verdict_id="V1",
            timestamp=2000,
            source="JUDGE",
            version="2.0.0",
            sample_size=2,
            correct_count=1,
            wrong_count=1,
            not_realized_count=0,
            win_rate_pct=85.0,  # Fabricated!
            t_statistic=0.0,
            p_value=1.0,
            bonferroni_threshold=0.05,
            is_statistically_significant=False,
            law_classification="REJECTED",
            real_money_authorized=False
        )
        res_fail = self.guardian.audit_chk10_hardcoded_win_rate(fake_judge, [t1, t2])
        self.assertEqual(res_fail.status, "INVALID")
        self.assertEqual(res_fail.severity, "CRITICAL")

    def test_attack_10_change_final_report(self):
        """Attack 10: Report mismatch against JudgeResult."""
        judge = JudgeResult(
            experiment_id="EXP",
            verdict_id="V1",
            timestamp=2000,
            source="JUDGE",
            version="2.0.0",
            sample_size=10,
            correct_count=5,
            wrong_count=5,
            not_realized_count=0,
            win_rate_pct=50.0,
            t_statistic=0.0,
            p_value=1.0,
            bonferroni_threshold=0.05,
            is_statistically_significant=False,
            law_classification="REJECTED",
            real_money_authorized=False
        )
        bad_report = ReportRecord(
            report_id="R1",
            timestamp=2001,
            source="REP",
            version="2.0.0",
            title="Summary",
            summary="Text",
            verdicts_summary={"win_rate_pct": 99.9},  # Mismatch!
            guardian_status="VERIFIED"
        )
        res_fail = self.guardian.audit_chk18_report_result_mismatch(bad_report, judge)
        self.assertEqual(res_fail.status, "INVALID")
        self.assertEqual(res_fail.severity, "CRITICAL")

    def test_attack_11_unauthorized_layer_bypass(self):
        """Attack 11: EXECUTOR directly reading REPORT."""
        res_fail = self.guardian.audit_chk17_unauthorized_layer_access("EXECUTOR", "REPORT", "READ")
        self.assertEqual(res_fail.status, "FAIL")

    def test_attack_12_premature_scoring_before_maturity(self):
        """Attack 12: Scoring test at t=1500 when maturity is t=1900."""
        pred = PredictionRecord("EXP", "P1", 1000, "EXEC", "2.0.0", "BTC", "15m", 900, 1900, "LONG", {}, [], "h1")
        premature_test = TestResult("EXP", "P1", 1500, "TEST", "2.0.0", "CORRECT", 0.5, 0.6, 15, 0.7, 0.1)
        res_fail = self.guardian.audit_chk08_outcome_evaluated_before_maturity(pred, premature_test)
        self.assertEqual(res_fail.status, "INVALID")
        self.assertEqual(res_fail.severity, "CRITICAL")

    def test_attack_13_broken_evidence_chain_parent_hash(self):
        """Attack 13: Severed parent hash chain."""
        node1 = {"evidence_id": "N1", "hash": "aaa", "parent_hash": "000"}
        node2 = {"evidence_id": "N2", "hash": "bbb", "parent_hash": "wrong_parent"}
        res_fail = self.guardian.audit_chk16_broken_parent_hash_chain([node1, node2])
        self.assertEqual(res_fail.status, "INVALID")

    def test_attack_14_hardcoded_prediction_in_source(self):
        """Attack 14: Hardcoded return 'CORRECT' pattern in source code."""
        bad_code = "def score_trade():\n    return 'CORRECT'  # hardcoded"
        res_fail = self.guardian.audit_chk09_hardcoded_prediction_result(bad_code, "test_file.py")
        self.assertEqual(res_fail.status, "INVALID")

    def test_attack_15_hardcoded_static_profit(self):
        """Attack 15: Constant identical returns across 5 trades."""
        results = [
            TestResult("EXP", f"P{i}", 1905, "TEST", "2.0.0", "CORRECT", 1.234, 1.384, 15, 1.5, 0.1)
            for i in range(5)
        ]
        res_fail = self.guardian.audit_chk11_hardcoded_profit(results)
        self.assertEqual(res_fail.status, "INVALID")


if __name__ == "__main__":
    unittest.main()
