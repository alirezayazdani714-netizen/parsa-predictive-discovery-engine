"""
PARSA ADVERSARIAL GUARDIAN TESTS (25 ATTACKS SUITE)
===================================================
Deliberately executes the full 25 adversarial red-team attacks against PARSA:
ATTACK-01 future candle injection
ATTACK-02 prediction mutation
ATTACK-03 nested prediction mutation
ATTACK-04 outcome mutation
ATTACK-05 test result mutation
ATTACK-06 judge result mutation
ATTACK-07 fake OHLC
ATTACK-08 duplicate candle
ATTACK-09 reversed timestamps
ATTACK-10 missing candle
ATTACK-11 hardcoded win rate
ATTACK-12 hardcoded correct result
ATTACK-13 early scoring
ATTACK-14 post-lock prediction modification
ATTACK-15 cherry-picked outcome
ATTACK-16 Guardian finding deletion
ATTACK-17 Guardian finding modification
ATTACK-18 unauthorized layer read
ATTACK-19 unauthorized layer write
ATTACK-20 mock/synthetic fallback
ATTACK-21 middle candle mutation after hash
ATTACK-22 outcome -> prediction contamination
ATTACK-23 future close -> signal
ATTACK-24 future high/low -> signal
ATTACK-25 report/result manipulation

Every attack is verified as BLOCKED or DETECTED (INVALID/FAIL).
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
    FutureDataAccessViolation,
    UnauthorizedLayerAccessViolation,
    compute_sha256
)
from parsa_layers.guardian.guardian_engine import GuardianEngine
from parsa_layers.contracts.access_control import LayerAccessController, LayerContext
from parsa_layers.laboratory.laboratory_engine import LaboratoryEngine
from parsa_layers.executor.executor_engine import ExecutorEngine
from parsa_layers.test.test_engine import TestEngine


class TestAdversarialGuardian(unittest.TestCase):

    def setUp(self):
        self.guardian = GuardianEngine()

    def test_attack_01_future_candle_injection(self):
        """ATTACK-01: Inject future candles past allowed snapshot timestamp."""
        pred_time = 1000.0
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
        object.__setattr__(bad_snapshot, "schema_version", "3.0.0")
        object.__setattr__(bad_snapshot, "parent_hash", "")
        object.__setattr__(bad_snapshot, "hash", "dummy")

        res_fail = self.guardian.audit_chk01_future_data_leakage(bad_snapshot, pred_time)
        self.assertEqual(res_fail.status, "INVALID")
        self.assertEqual(res_fail.severity, "CRITICAL")

    def test_attack_02_prediction_mutation(self):
        """ATTACK-02: Direct mutation attempt on PredictionRecord attributes."""
        pred = PredictionRecord(
            experiment_id="EXP-ATK-2",
            prediction_id="P1",
            prediction_timestamp=1000.0,
            source="EXEC",
            version="3.0.0",
            asset="BTCUSDT",
            timeframe="15m",
            horizon_seconds=900,
            maturity_timestamp=1900.0,
            direction="LONG",
            predicted_range={"upper": 65000, "lower": 64000},
            model_identifiers=["M1"],
            input_data_hash="h1"
        )
        # Direct mutation must be blocked by frozen dataclass
        with self.assertRaises(Exception):
            pred.direction = "SHORT"

        # Tampered dictionary check by Guardian
        tampered = pred.to_dict()
        tampered["direction"] = "SHORT"
        res_fail = self.guardian.audit_chk07_prediction_modified_after_locking(pred, tampered)
        self.assertEqual(res_fail.status, "INVALID")
        self.assertEqual(res_fail.severity, "CRITICAL")

    def test_attack_03_nested_prediction_mutation(self):
        """ATTACK-03: Nested dictionary and list mutation attempts on PredictionRecord."""
        pred = PredictionRecord(
            experiment_id="EXP-ATK-3",
            prediction_id="P1",
            prediction_timestamp=1000.0,
            source="EXEC",
            version="3.0.0",
            asset="BTCUSDT",
            timeframe="15m",
            horizon_seconds=900,
            maturity_timestamp=1900.0,
            direction="LONG",
            predicted_range={"upper": 65000, "lower": 64000},
            model_identifiers=["M1", "M2"],
            input_data_hash="h1"
        )
        # Dict mutation blocked by MappingProxyType
        with self.assertRaises(TypeError):
            pred.predicted_range["upper"] = 999999.0

        # List mutation blocked by tuple
        with self.assertRaises(AttributeError):
            pred.model_identifiers.append("M3_INJECTED")

        # Guardian deep immutability audit
        audit_res = self.guardian.audit_chk22_deep_immutability(pred)
        self.assertEqual(audit_res.status, "PASS")

    def test_attack_04_outcome_mutation(self):
        """ATTACK-04: Modify an outcome record or tamper with outcome hash."""
        outcome = OutcomeRecord(
            experiment_id="EXP-ATK-4",
            prediction_id="P1",
            observed_timestamp=1905.0,
            source="TEST",
            version="3.0.0",
            entry_price=100.0,
            exit_price=105.0,
            high_price=106.0,
            low_price=99.0,
            volume=500.0,
            actual_return_pct=5.0,
            max_favorable_excursion_pct=6.0,
            max_adverse_excursion_pct=1.0
        )
        with self.assertRaises(Exception):
            outcome.actual_return_pct = 99.0

        tampered_dict = outcome.to_dict()
        tampered_dict["actual_return_pct"] = 99.0
        res_fail = self.guardian.audit_chk15_evidence_hash_mismatch(outcome.hash, tampered_dict, "OUTCOME-NODE")
        self.assertEqual(res_fail.status, "INVALID")

    def test_attack_05_test_result_mutation(self):
        """ATTACK-05: Modify a test result or tamper with test hash."""
        tr = TestResult(
            experiment_id="EXP-ATK-5",
            prediction_id="P1",
            evaluated_timestamp=1905.0,
            source="TEST",
            version="3.0.0",
            status="WRONG",
            net_return_pct=-0.50,
            gross_return_pct=-0.40,
            friction_bps=10.0,
            mfe_pct=0.10,
            mae_pct=0.80
        )
        with self.assertRaises(Exception):
            tr.status = "CORRECT"

        tampered = tr.to_dict()
        tampered["status"] = "CORRECT"
        res_fail = self.guardian.audit_chk15_evidence_hash_mismatch(tr.hash, tampered, "TEST-RESULT-NODE")
        self.assertEqual(res_fail.status, "INVALID")

    def test_attack_06_judge_result_mutation(self):
        """ATTACK-06: Modify a judge result or tamper with confidence interval / classification."""
        judge = JudgeResult(
            experiment_id="EXP-ATK-6",
            verdict_id="V1",
            timestamp=2000.0,
            source="JUDGE",
            version="3.0.0",
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
            real_money_authorized=False,
            confidence_interval_95=(30.0, 70.0)
        )
        with self.assertRaises(Exception):
            judge.law_classification = "SCIENTIFIC_LAW_PROVEN"

        with self.assertRaises(AttributeError):
            judge.confidence_interval_95.append(90.0)

        tampered = judge.to_dict()
        tampered["win_rate_pct"] = 99.0
        res_fail = self.guardian.audit_chk15_evidence_hash_mismatch(judge.hash, tampered, "JUDGE-RESULT-NODE")
        self.assertEqual(res_fail.status, "INVALID")

    def test_attack_07_fake_ohlc(self):
        """ATTACK-07: Inverted Low > High or negative volume in market data."""
        bad_snapshot = MarketDataSnapshot.__new__(MarketDataSnapshot)
        object.__setattr__(bad_snapshot, "experiment_id", "EXP-ATK-7")
        object.__setattr__(bad_snapshot, "timestamp", 1000.0)
        object.__setattr__(bad_snapshot, "source", "TEST_FEED")
        object.__setattr__(bad_snapshot, "asset", "BTCUSDT")
        object.__setattr__(bad_snapshot, "timeframe", "15m")
        object.__setattr__(bad_snapshot, "candles", [
            {"open_time": 900.0, "open": 100, "high": 90, "low": 110, "close": 95, "volume": -5}  # Inverted!
        ])
        res_fail = self.guardian.audit_chk03_fake_synthetic_market_data(bad_snapshot)
        self.assertEqual(res_fail.status, "INVALID")
        self.assertEqual(res_fail.severity, "CRITICAL")

    def test_attack_08_duplicate_candle(self):
        """ATTACK-08: Duplicate candle timestamps in stream."""
        bad_snapshot = MarketDataSnapshot.__new__(MarketDataSnapshot)
        object.__setattr__(bad_snapshot, "experiment_id", "EXP-ATK-8")
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

    def test_attack_09_reversed_timestamps(self):
        """ATTACK-09: Time going backwards in market series."""
        bad_snapshot = MarketDataSnapshot.__new__(MarketDataSnapshot)
        object.__setattr__(bad_snapshot, "experiment_id", "EXP-ATK-9")
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

    def test_attack_10_missing_candle(self):
        """ATTACK-10: Significant gap between consecutive candles."""
        bad_snapshot = MarketDataSnapshot.__new__(MarketDataSnapshot)
        object.__setattr__(bad_snapshot, "experiment_id", "EXP-ATK-10")
        object.__setattr__(bad_snapshot, "timestamp", 5000.0)
        object.__setattr__(bad_snapshot, "source", "TEST_FEED")
        object.__setattr__(bad_snapshot, "asset", "BTCUSDT")
        object.__setattr__(bad_snapshot, "timeframe", "15m")
        object.__setattr__(bad_snapshot, "candles", [
            {"open_time": 1000.0, "open": 100, "high": 105, "low": 95, "close": 102, "volume": 10},
            {"open_time": 5000.0, "open": 102, "high": 108, "low": 100, "close": 106, "volume": 12}  # 4000s gap
        ])
        res_fail = self.guardian.audit_chk04_missing_market_data(bad_snapshot, expected_interval_seconds=900)
        self.assertEqual(res_fail.status, "WARNING")
        self.assertEqual(res_fail.severity, "HIGH")

    def test_attack_11_hardcoded_win_rate(self):
        """ATTACK-11: Fabricated win rate conflicting with true counts."""
        t1 = TestResult("EXP", "P1", 1905, "TEST", "3.0.0", "CORRECT", 0.5, 0.6, 15, 0.7, 0.1)
        t2 = TestResult("EXP", "P2", 1905, "TEST", "3.0.0", "WRONG", -0.5, -0.4, 15, 0.1, 0.7)
        fake_judge = JudgeResult(
            experiment_id="EXP",
            verdict_id="V1",
            timestamp=2000,
            source="JUDGE",
            version="3.0.0",
            sample_size=2,
            correct_count=1,
            wrong_count=1,
            not_realized_count=0,
            win_rate_pct=85.0,  # Fabricated 85% instead of 50%
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

    def test_attack_12_hardcoded_correct_result(self):
        """ATTACK-12: Hardcoded return 'CORRECT' pattern in source code."""
        bad_code = "def score_trade():\n    return 'CORRECT'  # hardcoded"
        res_fail = self.guardian.audit_chk09_hardcoded_prediction_result(bad_code, "test_file.py")
        self.assertEqual(res_fail.status, "INVALID")

    def test_attack_13_early_scoring(self):
        """ATTACK-13: Attempting to score a trade before maturity timestamp."""
        pred = PredictionRecord("EXP", "P1", 1000, "EXEC", "3.0.0", "BTC", "15m", 900, 1900, "LONG", {}, [], "h1")
        premature_test = TestResult("EXP", "P1", 1500, "TEST", "3.0.0", "CORRECT", 0.5, 0.6, 15, 0.7, 0.1)
        res_fail = self.guardian.audit_chk08_outcome_evaluated_before_maturity(pred, premature_test)
        self.assertEqual(res_fail.status, "INVALID")
        self.assertEqual(res_fail.severity, "CRITICAL")

    def test_attack_14_post_lock_prediction_modification(self):
        """ATTACK-14: Modifying prediction hash or direction post-creation."""
        pred = PredictionRecord("EXP", "P1", 1000, "EXEC", "3.0.0", "BTC", "15m", 900, 1900, "LONG", {}, [], "h1")
        tampered_dict = pred.to_dict()
        tampered_dict["horizon_seconds"] = 1800  # Tampered!
        res_fail = self.guardian.audit_chk07_prediction_modified_after_locking(pred, tampered_dict)
        self.assertEqual(res_fail.status, "INVALID")
        self.assertEqual(res_fail.severity, "CRITICAL")

    def test_attack_15_cherry_picked_outcome(self):
        """ATTACK-15: Cherry-picking outcomes by dropping losing trades."""
        p1 = PredictionRecord("EXP", "P1", 1000, "EXEC", "3.0.0", "BTC", "15m", 900, 1900, "LONG", {}, [], "h1")
        p2 = PredictionRecord("EXP", "P2", 1000, "EXEC", "3.0.0", "BTC", "15m", 900, 1900, "SHORT", {}, [], "h2")
        t1 = TestResult("EXP", "P1", 1905, "TEST", "3.0.0", "CORRECT", 0.5, 0.6, 15, 0.7, 0.1)
        res_fail = self.guardian.audit_chk13_missing_failed_predictions([p1, p2], [t1])
        self.assertEqual(res_fail.status, "INVALID")
        self.assertEqual(res_fail.severity, "CRITICAL")

    def test_attack_16_guardian_finding_deletion(self):
        """ATTACK-16: Deleting a finding from the Guardian evidence sink."""
        g = GuardianEngine()
        g.add_finding("CHK-01", "LOW", "f1", "fn1", "none", "ev1", "exp1", "act1", "PASS")
        g.add_finding("CHK-02", "LOW", "f2", "fn2", "none", "ev2", "exp2", "act2", "PASS")
        g.add_finding("CHK-03", "LOW", "f3", "fn3", "none", "ev3", "exp3", "act3", "PASS")
        # Attacker deletes entry 1
        del g._sink_chain[1]
        audit_res = g.audit_guardian_evidence_sink_integrity()
        self.assertEqual(audit_res.status, "INVALID")
        self.assertEqual(audit_res.severity, "CRITICAL")

    def test_attack_17_guardian_finding_modification(self):
        """ATTACK-17: Modifying severity or status in the Guardian evidence sink."""
        g = GuardianEngine()
        g.add_finding("CHK-01", "LOW", "f1", "fn1", "none", "ev1", "exp1", "act1", "PASS")
        g.add_finding("CHK-02", "CRITICAL", "f2", "fn2", "violation", "ev2", "exp2", "act2", "INVALID")
        # Attacker tampers with finding hash or parent hash
        g._sink_chain[1]["parent_hash"] = "FORGED_PARENT_HASH"
        audit_res = g.audit_guardian_evidence_sink_integrity()
        self.assertEqual(audit_res.status, "INVALID")

    def test_attack_18_unauthorized_layer_read(self):
        """ATTACK-18: EXECUTOR attempting to read from REPORT or TEST reading from JUDGES."""
        # Guardian detection
        res_fail = self.guardian.audit_chk17_unauthorized_layer_access("EXECUTOR", "REPORT", "READ")
        self.assertEqual(res_fail.status, "FAIL")
        # Runtime enforcement
        with self.assertRaises(UnauthorizedLayerAccessViolation):
            LayerAccessController.check_access("EXECUTOR", "REPORT", "READ")

    def test_attack_19_unauthorized_layer_write(self):
        """ATTACK-19: TEST attempting to write to EXECUTOR or EXECUTOR writing to OUTCOME."""
        # Guardian detection
        res_fail = self.guardian.audit_chk23_cross_layer_write_attempt("TEST", "EXECUTOR")
        self.assertEqual(res_fail.status, "FAIL")
        # Runtime enforcement
        with self.assertRaises(UnauthorizedLayerAccessViolation):
            LayerAccessController.check_access("TEST", "EXECUTOR", "WRITE")

    def test_attack_20_mock_synthetic_fallback(self):
        """ATTACK-20: Source code attempting to define synthetic market generator."""
        bad_code = "def generate_mock_klines(symbol):\n    return [{'open': 100}]"
        res_fail = self.guardian.audit_chk20_synthetic_fallback_mock_data(bad_code, "strategy.py")
        self.assertEqual(res_fail.status, "INVALID")
        self.assertEqual(res_fail.severity, "CRITICAL")

    def test_attack_21_middle_candle_mutation_after_hash(self):
        """ATTACK-21: Modifying a middle candle in a snapshot invalidates full snapshot hash."""
        c1 = {"open_time": 900.0, "open": 100, "high": 105, "low": 95, "close": 102, "volume": 10}
        c2 = {"open_time": 950.0, "open": 102, "high": 108, "low": 100, "close": 106, "volume": 12}
        c3 = {"open_time": 1000.0, "open": 106, "high": 110, "low": 104, "close": 109, "volume": 15}
        snapshot = MarketDataSnapshot("EXP-21", 1000.0, "FEED", "BTC", "15m", [c1, c2, c3])

        # Tamper middle candle
        tampered_c2 = dict(c2)
        tampered_c2["close"] = 107.5  # Mutation in middle candle!
        tampered_candles = [c1, tampered_c2, c3]

        res_fail = self.guardian.audit_chk25_snapshot_inner_candle_tamper(snapshot, tampered_candles)
        self.assertEqual(res_fail.status, "INVALID")
        self.assertEqual(res_fail.severity, "CRITICAL")

    def test_attack_22_outcome_to_prediction_contamination(self):
        """ATTACK-22: Prediction created at or after maturity timestamp is blocked by model and flagged by Guardian."""
        from parsa_layers.contracts.models import ParsaArchitectureViolation
        # 1. Blocked at contract model instantiation
        with self.assertRaises(ParsaArchitectureViolation):
            PredictionRecord("EXP", "P1", 1900, "EXEC", "3.0.0", "BTC", "15m", 900, 1900, "LONG", {}, [], "h1")

        # 2. Detected by Guardian CHK-02 if forged
        forged_pred = PredictionRecord.__new__(PredictionRecord)
        object.__setattr__(forged_pred, "experiment_id", "EXP")
        object.__setattr__(forged_pred, "prediction_id", "P1")
        object.__setattr__(forged_pred, "prediction_timestamp", 1900.0)
        object.__setattr__(forged_pred, "source", "EXEC")
        object.__setattr__(forged_pred, "version", "3.0.0")
        object.__setattr__(forged_pred, "asset", "BTC")
        object.__setattr__(forged_pred, "timeframe", "15m")
        object.__setattr__(forged_pred, "horizon_seconds", 900)
        object.__setattr__(forged_pred, "maturity_timestamp", 1900.0)
        object.__setattr__(forged_pred, "direction", "LONG")
        object.__setattr__(forged_pred, "predicted_range", {})
        object.__setattr__(forged_pred, "model_identifiers", ())
        object.__setattr__(forged_pred, "input_data_hash", "h1")
        object.__setattr__(forged_pred, "schema_version", "3.0.0")
        object.__setattr__(forged_pred, "parent_hash", "")
        object.__setattr__(forged_pred, "prediction_hash", "dummy")

        res_fail = self.guardian.audit_chk02_future_outcome_leakage(forged_pred, 1900.0)
        self.assertEqual(res_fail.status, "INVALID")

    def test_attack_23_future_close_to_signal(self):
        """ATTACK-23: Feeding future close into signal generator prior to cutoff."""
        lab = LaboratoryEngine(in_sample_cutoff_timestamp=1000.0)
        future_candles = [
            {"open_time": 900.0, "open": 100, "high": 105, "low": 95, "close": 102, "volume": 10},
            {"open_time": 1050.0, "open": 102, "high": 110, "low": 101, "close": 108, "volume": 15}  # Past cutoff
        ]
        with self.assertRaises(FutureDataAccessViolation):
            lab.compute_trend_momentum_signal(future_candles)

    def test_attack_24_future_high_low_to_signal(self):
        """ATTACK-24: Feeding future high/low into executor prior to allowed timestamp."""
        exec_engine = ExecutorEngine("EXP-24")
        future_candles = [
            {"open_time": 1200.0, "open": 100, "high": 115, "low": 95, "close": 110, "volume": 10}
        ]
        with self.assertRaises(FutureDataAccessViolation):
            exec_engine.ingest_bounded_candles("BTCUSDT", future_candles, allowed_timestamp=1000.0)

    def test_attack_25_report_result_manipulation(self):
        """ATTACK-25: Report metrics contradicting underlying JudgeResult."""
        judge = JudgeResult(
            experiment_id="EXP",
            verdict_id="V1",
            timestamp=2000,
            source="JUDGE",
            version="3.0.0",
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
            version="3.0.0",
            title="Summary",
            summary="Text",
            verdicts_summary={"win_rate_pct": 99.9},  # Contradiction!
            guardian_status="VERIFIED"
        )
        res_fail = self.guardian.audit_chk18_report_result_mismatch(bad_report, judge)
        self.assertEqual(res_fail.status, "INVALID")
        self.assertEqual(res_fail.severity, "CRITICAL")


if __name__ == "__main__":
    unittest.main()
