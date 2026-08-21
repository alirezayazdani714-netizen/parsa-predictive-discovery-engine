# 🔒 PARSA LAYER ACCESS POLICY & ISOLATION CONTRACT
## ACCESS CONTROL MATRIX & INTER-LAYER COMMUNICATION PROTOCOLS (PHASE 3)

**Objective:** Enforce strict architectural isolation across all 7 logical PARSA layers, ensuring zero look-ahead bias, zero synthetic fallback, immutable evidence chains, and completely independent forensic auditing.

---

### 1. Master Layer Access Control Matrix

| Layer | Can Read | Can Write | Strictly Forbidden Access / Operations |
| :--- | :--- | :--- | :--- |
| **1. LABORATORY** | • Scenario Specifications<br>• Permitted In-Sample Historical Data ($t \le T_{train}$) | • Hypothesis Definitions<br>• Discovery Algorithms<br>• Feature Transformers | • Out-Of-Sample Data ($t > T_{train}$)<br>• Live Future Market Data ($t > T$)<br>• Test Outcomes & Scores<br>• Judge Verdicts |
| **2. EXECUTOR** | • Approved Discovery Protocol<br>• Live/Historical Market Data up to current $T$ ($t \le T$) | • Raw Ingestion Events<br>• Immutable Locked Predictions (at time $T$) | • Future Market Data ($t > T$)<br>• Prediction Outcomes ($T + \Delta t$)<br>• Self-Scoring / Evaluation<br>• Fallback to Mock Data |
| **3. TEST** | • Immutable Locked Predictions<br>• Market Data post-maturity ($t \ge T + \Delta t$) | • Evaluated Outcomes<br>• MFE, MAE, Realized Returns<br>• Friction-Adjusted Trade Logs | • Modifying Locked Predictions<br>• Evaluating Before Maturity ($t < T + \Delta t$)<br>• Passing Un-evaluated Forecasts |
| **4. JUDGES** | • Test Evaluation Records<br>• Historical Rejection Memory<br>• Sample Size & Multi-testing Matrices | • Statistical Verdicts (p-values, t-stats)<br>• Discovery Classifications (A/B/Cand/Rej)<br>• Refutation Records | • Modifying Raw Execution Logs<br>• Modifying Test Scoring Metrics<br>• Overriding Guardian Invalidation Flags |
| **5. GUARDIAN** | • Read-Only Access to ALL Layers<br>• Raw Logs, Manifests, Hashes<br>• Source Code & Ast Trees | • Forensic Finding Records<br>• Invalidation Flags<br>• Audit Reports & Issue Certificates | • Mutating Any Scientific Evidence<br>• Mutating Predictions or Test Scores<br>• Altering Execution Pipelines Silently |
| **6. REPORT** | • Validated Judge Verdicts<br>• Validated Test Outcomes<br>• Guardian Audit Certificates | • Human-Readable Markdown Summaries<br>• UI Dashboards & Presentation Models | • Manufacturing / Altering Evidence<br>• Displaying Unverified Data as Verified<br>• Suppressing Negative or Failed Results |
| **7. SCENARIO** | • System Configuration Guidelines<br>• Market Metadata Specs | • Experiment Protocols<br>• Universe & Horizon Definitions | • Modifying Active Execution Streams<br>• Declaring Experiment Success / Alpha |

---

### 2. Formal Layer Contracts & Input/Output Specifications

#### Contract 1: Laboratory -> Scenario Interface
* **Input to Lab:** `ExperimentSpecification` (Assets, feature constraints, in-sample time window $[T_0, T_{split}]$).
* **Output from Lab:** `HypothesisProtocol` (Mathematical signal formula, entry/exit criteria, expected holding horizon $\Delta t$, cryptographic SHA-256 digest of algorithm code).
* **Constraint:** Lab logic MUST compile deterministically without non-deterministic side effects or external runtime network fetches during model transformation.

#### Contract 2: Executor -> Test Interface
* **Input to Executor:** `HypothesisProtocol` + Live/Historical Candle Stream ($t \le T$).
* **Output from Executor:** `LockedPredictionEvent`
  ```json
  {
    "prediction_id": "PRED-20260821-BTC-001",
    "timestamp_created": 1787341200,
    "asset": "BTCUSDT",
    "trigger_price": 60500.25,
    "direction": "LONG",
    "horizon_delta_seconds": 900,
    "maturity_timestamp": 1787342100,
    "feature_vector_hash": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
    "status": "LOCKED"
  }
  ```
* **Constraint:** Once written to the immutable log, the Executor cannot modify, delete, or overwrite the record.

#### Contract 3: Test -> Judges Interface
* **Input to Test:** `LockedPredictionEvent` + Market Data strictly at or after $T + \Delta t$.
* **Output from Test:** `EvaluatedOutcomeEvent`
  ```json
  {
    "evaluation_id": "EVAL-20260821-BTC-001-15M",
    "prediction_id": "PRED-20260821-BTC-001",
    "evaluation_timestamp": 1787342105,
    "entry_price": 60500.25,
    "exit_price": 60720.50,
    "realized_return_pct": 0.364,
    "max_favorable_excursion_pct": 0.450,
    "max_adverse_excursion_pct": 0.110,
    "friction_deducted_bps": 15.0,
    "net_return_pct": 0.214,
    "outcome": "CORRECT"
  }
  ```
* **Constraint:** Test evaluation MUST verify that `evaluation_timestamp >= maturity_timestamp`. Any scoring before maturity is a fatal look-ahead violation.

#### Contract 4: Judges -> Report Interface
* **Input to Judges:** Stream of `EvaluatedOutcomeEvent` records ($N \ge N_{min}$).
* **Output from Judges:** `ScientificVerdict`
  ```json
  {
    "verdict_id": "VERD-20260821-DISC-01",
    "discovery_id": "PARSA-DISC-01",
    "sample_size": 88,
    "win_rate_pct": 32.95,
    "t_statistic": 1.42,
    "p_value": 0.158,
    "bonferroni_p_threshold": 0.0026,
    "statistically_significant": false,
    "classification": "UNPROVEN_CANDIDATE",
    "real_money_authorized": false
  }
  ```
* **Constraint:** If sample size $N < 30$, classification MUST be set to `INSUFFICIENT_SAMPLE`. Real-money trading authorization is strictly false.

---

### 3. Core Isolation Policies

1. **Zero Look-Ahead Enforcement:**  
   * Any function calculating a feature at index $i$ is strictly forbidden from referencing array indices $j > i$.
   * Live execution environments must receive candle data strictly one tick/candle at a time via forward-only iterators.
2. **Anti-Fabrication & Anti-Fallback Policy:**  
   * If a market data feed fails or drops, the system MUST emit `DATA_UNAVAILABLE` and halt execution.
   * Creating synthetic candles, random drift, or interpolated mock data to bypass network interruptions is strictly prohibited.
3. **Guardian Independence:**  
   * Guardian audit scripts execute in dedicated, read-only inspection processes.
   * Guardian code must not import execution logic with mutable side-effects.

---
*End of Access Policy Specification (Phase 3).*
