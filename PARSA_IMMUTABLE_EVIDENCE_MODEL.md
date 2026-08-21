# ⛓️ PARSA IMMUTABLE EVIDENCE MODEL
## CRYPTOGRAPHIC PROVENANCE & CHAIN-OF-EVIDENCE ARCHITECTURE (PHASE 4)

**Objective:** Establish a mathematically verifiable, cryptographically sealed chain of custody for every market data point, algorithm execution, prediction lock, outcome observation, and judicial verdict within the PARSA ecosystem.

---

### 1. The 9-Stage Immutable Evidence Chain

```
[1. RAW DATA]
   │  • Binance Public REST API v3 / WebSocket JSON
   │  • SHA-256 Digest of Raw Ingest Payloads
   ▼
[2. EXECUTION EVENT]
   │  • Triggered at timestamp T
   │  • Algorithm input features calculated using only t <= T
   ▼
[3. LOCKED PREDICTION]
   │  • Immutably written & cryptographically sealed at timestamp T
   │  • Forecast: Asset, Direction, Target, Maturity (T + Δt)
   ▼
[4. MATURITY EVENT]
   │  • Wall-clock time reaches T + Δt
   │  • Forward evaluation window unlocked
   ▼
[5. OBSERVED OUTCOME]
   │  • Real Binance price tick data ingested for [T, T + Δt]
   │  • High, Low, Close, Volume captured
   ▼
[6. SCORE & METRIC EVENT]
   │  • MFE (Max Favorable Excursion), MAE (Max Adverse Excursion)
   │  • Realized Return, Directional Correctness (Correct/Wrong/Neutral)
   ▼
[7. JUDGE VERDICT]
   │  • Out-of-Sample aggregation across N >= 30 events
   │  • p-value, t-statistic, Bonferroni false-discovery correction
   ▼
[8. GUARDIAN AUDIT]
   │  • Automated forensic scan of all 9 stages
   │  • Validates timestamps, hash integrity, zero-leakage, zero-fabrication
   ▼
[9. VALIDATED SCIENTIFIC REPORT]
   │  • Human-readable and machine-auditable synthesis
   │  • Cryptographically signed report manifest
```

---

### 2. Schema Specification for Every Chain Node

Every record in the chain MUST implement the standard `EvidenceNode` contract:

```typescript
interface EvidenceNode {
  node_id: string;              // Globally unique UUID / Canonical ID
  node_type: string;            // E.g., "LOCKED_PREDICTION", "OBSERVED_OUTCOME"
  parent_node_id: string;       // Reference to preceding node in chain
  timestamp_utc: number;        // Unix epoch timestamp (seconds or milliseconds)
  timestamp_iso: string;        // ISO 8601 string: YYYY-MM-DDTHH:MM:SSZ
  source_layer: string;         // EXECUTOR, TEST, JUDGES, GUARDIAN, etc.
  provenance_uri: string;       // Binance endpoint or internal store URI
  payload_sha256: string;       // SHA-256 hash of the node's core data payload
  signature?: string;           // Optional cryptographic seal
}
```

---

### 3. Step-by-Step Node Definitions

#### Stage 1: Raw Data Node (`RAW_DATA_INGEST`)
* **ID Format:** `RAW-<SYMBOL>-<TIMEFRAME>-<OPEN_TIME>`
* **Fields:** Open, High, Low, Close, Volume, CloseTime, NumberOfTrades, TakerBuyBaseVolume.
* **Hash:** `SHA-256(Raw JSON Response from Binance)`.

#### Stage 2: Execution Event Node (`EXECUTION_EVENT`)
* **ID Format:** `EXEC-<SYMBOL>-<STRATEGY_ID>-<TIMESTAMP>`
* **Parent:** `RAW_DATA_INGEST`
* **Invariant:** Ingested raw data timestamps must all satisfy $t \le \text{timestamp\_utc}$.

#### Stage 3: Locked Prediction Node (`LOCKED_PREDICTION`)
* **ID Format:** `PRED-<SYMBOL>-<TIMESTAMP>-<HORIZON>`
* **Parent:** `EXECUTION_EVENT`
* **Fields:** `symbol`, `direction` (LONG/SHORT/NEUTRAL), `trigger_price`, `horizon_seconds`, `maturity_timestamp`, `strategy_version_hash`.
* **Immutability Rule:** Written to append-only JSONL log file. File system permissions enforce read-only post-write.

#### Stage 4: Maturity Event Node (`MATURITY_EVENT`)
* **ID Format:** `MATURE-<PREDICTION_ID>`
* **Parent:** `LOCKED_PREDICTION`
* **Invariant:** $\text{timestamp\_utc} \ge \text{maturity\_timestamp}$. Scoring is physically blocked before this node is emitted.

#### Stage 5: Observed Outcome Node (`OBSERVED_OUTCOME`)
* **ID Format:** `OUTCOME-<PREDICTION_ID>`
* **Parent:** `MATURITY_EVENT`
* **Fields:** `exit_price`, `realized_high`, `realized_low`, `realized_volume`, `raw_matured_candle_id`.

#### Stage 6: Score Node (`SCORE_EVENT`)
* **ID Format:** `SCORE-<PREDICTION_ID>`
* **Parent:** `OBSERVED_OUTCOME`
* **Fields:** `gross_return_pct`, `fee_deduction_pct`, `net_return_pct`, `mfe_pct`, `mae_pct`, `is_correct` (true/false/null).

#### Stage 7: Judge Verdict Node (`JUDGE_VERDICT`)
* **ID Format:** `VERD-<STRATEGY_ID>-<EVALUATION_BATCH_ID>`
* **Parent:** Array of `SCORE_EVENT` IDs
* **Fields:** `sample_size`, `win_rate`, `mean_net_return`, `sharpe_ratio`, `p_value`, `is_significant`, `law_tier`.

#### Stage 8: Guardian Audit Node (`GUARDIAN_AUDIT_CERTIFICATE`)
* **ID Format:** `AUDIT-<BATCH_ID>-<TIMESTAMP>`
* **Parent:** `JUDGE_VERDICT`
* **Fields:** `audit_status` (PASS/FAIL/INVALID), `findings_count`, `violations_detected`, `leakage_detected` (false).

#### Stage 9: Validated Report Node (`REPORT_NODE`)
* **ID Format:** `REP-<MISSION_OR_TRIAL_ID>`
* **Parent:** `GUARDIAN_AUDIT_CERTIFICATE`
* **Fields:** `report_hash`, `summary_metrics`, `status_seal`.

---

### 4. Integrity Invariants & Verification Rules

1. **Temporal Monotonicity Invariant:**  
   $$\text{Timestamp}(Node_k) \ge \text{Timestamp}(Node_{k-1})$$
   *Any node having a timestamp earlier than its parent is flagged as an immediate fatal temporal violation.*
2. **Hash-Chain Continuity:**  
   $$Node_k.\text{parent\_hash} == \text{SHA-256}(Node_{k-1}.\text{payload})$$
3. **Maturity Precedence Rule:**  
   $$\text{Timestamp}(ScoreEvent) \ge PredictionEvent.\text{maturity\_timestamp}$$
   *Scoring an event prior to its maturity timestamp invalidates the entire trial batch.*

---
*End of Immutable Evidence Model (Phase 4).*
