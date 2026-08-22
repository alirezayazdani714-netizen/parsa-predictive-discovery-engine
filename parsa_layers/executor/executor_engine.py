"""
PARSA EXECUTOR LAYER (LAYER 2 - PHASE 3 HARDENED)
=================================================
Handles live & bounded historical market data ingestion, runtime boundary enforcement,
and immutable cryptographic prediction locking at time T.

CRITICAL INVARIANTS:
1. Runtime Guard: If any ingested market data timestamp > current allowed timestamp T, raise FutureDataAccessViolation.
2. Absolute NO-MOCK Rule: If data source is unavailable, raise DataUnavailableError. No synthetic/mock fallback.
3. Cannot score its own predictions or read Test/Judge/Report outcomes.
4. Exception Handling: Never swallow or mask FutureDataAccessViolation or DataUnavailableError.
"""

from typing import List, Dict, Any, Optional
import time
import urllib.request
import json
import ssl
from parsa_layers.contracts.models import (
    MarketDataSnapshot,
    PredictionRecord,
    FutureDataAccessViolation,
    DataUnavailableError,
    UnauthorizedLayerAccessViolation,
    compute_sha256
)
from parsa_layers.contracts.access_control import LayerAccessController, LayerContext, enforce_layer


class ExecutorEngine:
    """Bounded Execution and Prediction Locking Engine."""

    def __init__(self, experiment_id: str, version: str = "3.0.0"):
        self.experiment_id = experiment_id
        self.version = version

    @enforce_layer("EXECUTOR")
    def ingest_live_binance_candles(
        self,
        symbol: str,
        interval: str = "15m",
        limit: int = 100,
        allowed_timestamp: Optional[float] = None
    ) -> MarketDataSnapshot:
        """
        Fetches authentic spot market data from Binance Public REST API.
        Enforces Absolute NO-MOCK Rule: Fails immediately with DataUnavailableError on any network or API issue.
        Enforces Runtime Guard: Raises FutureDataAccessViolation if any candle time > allowed_timestamp.
        """
        now = time.time()
        max_allowed_time = allowed_timestamp if allowed_timestamp is not None else now
        url = f"https://api.binance.com/api/v3/klines?symbol={symbol}&interval={interval}&limit={limit}"

        try:
            req = urllib.request.Request(
                url,
                headers={"User-Agent": "PARSA-Execution-Engine/3.0"}
            )
            ctx = ssl.create_default_context()
            with urllib.request.urlopen(req, timeout=10, context=ctx) as response:
                if response.status != 200:
                    raise DataUnavailableError(f"Binance API returned HTTP status {response.status}")
                raw_data = response.read().decode('utf-8')
                raw_candles = json.loads(raw_data)

            if not raw_candles or not isinstance(raw_candles, list):
                raise DataUnavailableError(f"STATUS = DATA_UNAVAILABLE: Empty or malformed candle array for {symbol}")

            parsed_candles: List[Dict[str, Any]] = []
            for row in raw_candles:
                open_time_ms = int(row[0])
                open_time_sec = open_time_ms / 1000.0

                # HARD RUNTIME GUARD: Exact Future Leakage Check (Zero Tolerance)
                if open_time_sec > max_allowed_time:
                    raise FutureDataAccessViolation(
                        f"CRITICAL VIOLATION: Ingested candle open_time ({open_time_sec}) exceeds allowed execution timestamp ({max_allowed_time})"
                    )

                # Candle sanity checks (CHK-03: Authentic OHLCV)
                open_p = float(row[1])
                high_p = float(row[2])
                low_p = float(row[3])
                close_p = float(row[4])
                volume = float(row[5])

                if not (low_p <= open_p <= high_p and low_p <= close_p <= high_p and volume >= 0):
                    raise DataUnavailableError(f"Corrupted or non-authentic candle structure detected for {symbol} at {open_time_ms}")

                parsed_candles.append({
                    "open_time": open_time_ms,
                    "open": open_p,
                    "high": high_p,
                    "low": low_p,
                    "close": close_p,
                    "volume": volume,
                    "close_time": int(row[6]),
                    "trades": int(row[8])
                })

            return MarketDataSnapshot(
                experiment_id=self.experiment_id,
                timestamp=max_allowed_time,
                source="BINANCE_PUBLIC_REST_API_V3",
                asset=symbol,
                timeframe=interval,
                candles=parsed_candles
            )
        except FutureDataAccessViolation:
            # Critical architecture exceptions must propagate directly
            raise
        except DataUnavailableError:
            raise
        except Exception as e:
            # ABSOLUTE NO-MOCK: Never fall back to mock data
            raise DataUnavailableError(
                f"STATUS = DATA_UNAVAILABLE: Failed to retrieve authentic Binance data for {symbol}: {str(e)}"
            ) from e

    @enforce_layer("EXECUTOR")
    def ingest_bounded_candles(
        self,
        symbol: str,
        raw_candles: List[Dict[str, Any]],
        interval: str = "15m",
        allowed_timestamp: Optional[float] = None
    ) -> MarketDataSnapshot:
        """
        Ingests bounded authentic market candles, enforcing runtime look-ahead bounds.
        """
        now = time.time()
        max_allowed_time = allowed_timestamp if allowed_timestamp is not None else now

        try:
            if not raw_candles or not isinstance(raw_candles, list):
                raise DataUnavailableError(f"STATUS = DATA_UNAVAILABLE: Empty or malformed candle array for {symbol}")

            parsed_candles: List[Dict[str, Any]] = []
            for idx, c in enumerate(raw_candles):
                raw_t = c.get("open_time", c.get("timestamp", c.get("time", None)))
                if raw_t is None:
                    raise DataUnavailableError(f"STATUS = DATA_UNAVAILABLE: Candle {idx} lacks timestamp")
                open_time_sec = raw_t / 1000.0 if raw_t > 1e11 else float(raw_t)

                if open_time_sec > max_allowed_time:
                    raise FutureDataAccessViolation(
                        f"CRITICAL VIOLATION: Ingested candle open_time ({open_time_sec}) exceeds allowed execution timestamp ({max_allowed_time})"
                    )

                open_p = float(c["open"])
                high_p = float(c["high"])
                low_p = float(c["low"])
                close_p = float(c["close"])
                volume = float(c.get("volume", 0.0))

                if not (low_p <= open_p <= high_p and low_p <= close_p <= high_p and volume >= 0):
                    raise DataUnavailableError(f"Corrupted candle structure detected for {symbol} at index {idx}")

                parsed_candles.append({
                    "open_time": raw_t,
                    "open": open_p,
                    "high": high_p,
                    "low": low_p,
                    "close": close_p,
                    "volume": volume
                })

            return MarketDataSnapshot(
                experiment_id=self.experiment_id,
                timestamp=max_allowed_time,
                source="BOUNDED_AUTHENTIC_FEED",
                asset=symbol,
                timeframe=interval,
                candles=parsed_candles
            )
        except FutureDataAccessViolation:
            raise
        except DataUnavailableError:
            raise
        except Exception as e:
            raise DataUnavailableError(f"STATUS = DATA_UNAVAILABLE: {str(e)}") from e

    @enforce_layer("EXECUTOR")
    def generate_and_lock_prediction(
        self,
        snapshot: MarketDataSnapshot,
        horizon_seconds: int,
        direction: str,
        predicted_range: Dict[str, float],
        model_identifiers: List[str],
        execution_timestamp: Optional[float] = None
    ) -> PredictionRecord:
        """
        Creates an immutable locked prediction record at execution_timestamp T.
        Enforces maturity_timestamp = T + horizon_seconds.
        """
        now = execution_timestamp if execution_timestamp is not None else snapshot.timestamp
        maturity_t = now + horizon_seconds
        prediction_id = f"PRED-{self.experiment_id}-{snapshot.asset}-{int(now)}-{horizon_seconds}S"

        record = PredictionRecord(
            experiment_id=self.experiment_id,
            prediction_id=prediction_id,
            prediction_timestamp=now,
            source="EXECUTOR_ENGINE_V3",
            version=self.version,
            asset=snapshot.asset,
            timeframe=snapshot.timeframe,
            horizon_seconds=horizon_seconds,
            maturity_timestamp=maturity_t,
            direction=direction,
            predicted_range=predicted_range,
            model_identifiers=model_identifiers,
            input_data_hash=snapshot.hash,
            parent_hash=snapshot.hash
        )

        return record
