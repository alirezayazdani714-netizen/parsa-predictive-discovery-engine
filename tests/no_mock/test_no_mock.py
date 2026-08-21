"""
PARSA NO-MOCK ENFORCEMENT TESTS (PART 5)
========================================
Verifies that:
- Real data failures raise DataUnavailableError immediately.
- No synthetic fallbacks, mock responses, or fake candles are accepted.
- Status is strictly DATA_UNAVAILABLE upon network/data failure.
"""

import unittest
from parsa_layers.contracts.models import (
    DataUnavailableError
)
from parsa_layers.executor.executor_engine import ExecutorEngine
from parsa_layers.test.test_engine import TestEngine
from parsa_layers.guardian.guardian_engine import GuardianEngine


class TestNoMockEnforcement(unittest.TestCase):

    def test_executor_fails_closed_on_bad_symbol(self):
        """Executor must raise DataUnavailableError on non-existent symbol without falling back to mock."""
        executor = ExecutorEngine("EXP-NOMOCK-001")
        with self.assertRaises(DataUnavailableError):
            # Invalid symbol will fail Binance HTTP call
            executor.ingest_live_binance_candles("NONEXISTENT_SYMBOL_XYZ_123")

    def test_test_engine_fails_closed_on_empty_forward_candles(self):
        """Test engine raises DataUnavailableError if no forward candles are supplied."""
        test_engine = TestEngine("EXP-NOMOCK-002")
        from parsa_layers.contracts.models import PredictionRecord
        pred = PredictionRecord("EXP", "P1", 1000, "EXEC", "2.0.0", "BTC", "15m", 900, 1900, "LONG", {}, [], "h1")
        with self.assertRaises(DataUnavailableError):
            test_engine.evaluate_prediction_outcome(pred, forward_candles=[], current_time=2000)

    def test_guardian_flags_mock_generators_in_source(self):
        """Guardian flags any script attempting to define mock generators."""
        guardian = GuardianEngine()
        code_with_mock = "def generate_mock_klines(symbol):\n    return [{'open': 100}]"
        finding = guardian.audit_chk20_synthetic_fallback_mock_data(code_with_mock, "legacy_mock_script.py")
        self.assertEqual(finding.status, "INVALID")
        self.assertEqual(finding.severity, "CRITICAL")


if __name__ == "__main__":
    unittest.main()
