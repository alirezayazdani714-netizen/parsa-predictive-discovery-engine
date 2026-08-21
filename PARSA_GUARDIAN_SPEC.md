# 🛡️ PARSA GUARDIAN SPECIFICATION
## AUTOMATED FORENSIC INSPECTOR & ANTI-FABRICATION PROTOCOL (PHASE 5)

**Role:** Independent Forensic Guardian & Scientific Auditor  
**Mandate:** Detect, Flag, Invalidate, and Report any methodological violations, data fabrications, look-ahead biases, or cross-layer unauthorized accesses without mutating experimental records.

---

### 1. The 20 Automated Forensic Audit Checks

| Check ID | Verification Name | Target Layer | Forensic Detection Logic | Severity |
| :---: | :--- | :---: | :--- | :---: |
| **CHK-01** | **Future Leakage Check** | EXECUTOR / LAB | Scans feature computation routines for forward array indexing ($[i+k]$ with $k > 0$) or future timestamp queries. | **CRITICAL** |
| **CHK-02** | **Look-Ahead Bias Check** | TEST | Asserts that prediction scoring occurs strictly at or after $T + \text{horizon}$. Validates that test datasets are strictly separated by cutoff time $T_{split}$. | **CRITICAL** |
| **CHK-03** | **Fake Candles Check** | EXECUTOR | Verifies candle prices against authentic Binance OHLCV relationships: $\text{Low} \le \min(\text{Open}, \text{Close}) \le \max(\text{Open}, \text{Close}) \le \text{High}$ and $\text{Volume} \ge 0$. | **CRITICAL** |
| **CHK-04** | **Mock Data Check** | EXECUTOR / DATA | AST scans for `random()`, `numpy.random`, `faker`, or hardcoded mock JSON arrays serving as market data streams. | **CRITICAL** |
| **CHK-05** | **Fallback Data Check** | EXECUTOR | Detects `try/except` blocks that fall back to synthetic or simulated data upon network failure instead of raising `DATA_UNAVAILABLE`. | **CRITICAL** |
| **CHK-06** | **Hardcoded Outcomes Check** | TEST / JUDGES | Scans test scoring routines for static return arrays, predetermined win/loss assignments, or bypassed calculation paths. | **CRITICAL** |
| **CHK-07** | **Hardcoded Win Rates Check** | JUDGES / REPORT | Scans for static percentage declarations (e.g., `win_rate = 0.85`) not computed directly from the outcome array `sum(correct)/len(total)`. | **CRITICAL** |
| **CHK-08** | **Fabricated Timestamps Check** | ALL | Verifies that all event timestamps are monotonically increasing and correspond to actual calendar time within allowable clock drift ($\le 2\text{s}$). | **HIGH** |
| **CHK-09** | **Missing Observations Check** | TEST / DATA | Verifies time-series continuity: checks for unannounced candle gaps, missing 1m bars, or dropped prediction cycles. | **HIGH** |
| **CHK-10** | **Duplicate Observations Check** | EXECUTOR / TEST | Validates that no prediction ID or execution event is ingested or scored multiple times to artificially inflate sample size $N$. | **HIGH** |
| **CHK-11** | **Duplicate Discoveries Check** | LAB / JUDGES | Computes mathematical and correlation similarity across registered discoveries to prevent repackaging identical signals as new discoveries. | **MEDIUM** |
| **CHK-12** | **Cherry-Picked Assets Check** | SCENARIO / JUDGES | Validates that all assets in the declared experimental universe are reported, preventing selective omission of losing trading pairs. | **HIGH** |
| **CHK-13** | **Cherry-Picked Time Periods Check** | SCENARIO / TEST | Ensures contiguous evaluation periods without post-hoc date filtering or exclusion of high-volatility drawdown windows. | **HIGH** |
| **CHK-14** | **Premature Scoring Check** | TEST | Asserts that scoring jobs are triggered only after horizon expiration: $T_{\text{eval}} \ge T_{\text{prediction}} + \Delta t_{\text{horizon}}$. | **CRITICAL** |
| **CHK-15** | **Premature Report Generation** | REPORT | Checks that reports are only synthesized after all active forecast horizons in a batch have concluded and been scored. | **HIGH** |
| **CHK-16** | **Unauthorized Layer Access** | ALL | Inspects file imports and directory accesses to verify compliance with `PARSA_LAYER_ACCESS_POLICY.md`. | **HIGH** |
| **CHK-17** | **Inconsistent Counts Check** | REPORT / JUDGES | Cross-verifies that $\text{Total Evaluated} == \text{Correct} + \text{Wrong} + \text{Neutral/Not Realized}$ across all report tables. | **HIGH** |
| **CHK-18** | **Inconsistent Hashes Check** | GUARDIAN / DATA | Re-computes SHA-256 digests of all raw data and prediction logs and verifies against the cryptographic manifests. | **CRITICAL** |
| **CHK-19** | **Report/Evidence Mismatch** | REPORT | Compares claims in Markdown/UI reports directly with underlying raw JSON outcome databases to detect discrepancies. | **CRITICAL** |
| **CHK-20** | **Source-Code/Result Mismatch** | LAB / JUDGES | Runs a deterministic execution of the registered source algorithm to confirm that claimed historical signals are exactly reproduced. | **HIGH** |

---

### 2. Standard Guardian Finding Data Contract

When the Guardian executes an inspection, every identified item MUST be structured according to the formal `GuardianFinding` schema:

```json
{
  "guardian_finding_id": "GF-20260821-CHK02-001",
  "check_id": "CHK-02",
  "severity": "CRITICAL",
  "file": "scripts/parsa_mission20_live_trial_engine.py",
  "line_or_function": "evaluate_outcomes_at_maturity()",
  "violation": "Attempted outcome scoring prior to horizon maturity timestamp",
  "evidence": "eval_timestamp (1787342000) < maturity_timestamp (1787342100)",
  "expected_behavior": "Evaluation must occur strictly when eval_timestamp >= 1787342100",
  "actual_behavior": "Function called 100 seconds before maturity window elapsed",
  "status": "FAIL",
  "timestamp_utc": 1787342005
}
```

---

### 3. Allowed Finding Statuses & Semantic Definitions

* **`PASS`**: Check executed completely; zero violations detected; evidence matches mathematical invariants.
* **`WARNING`**: Non-critical issue detected (e.g., minor data gap of $< 2$ non-trading bars or low sample size $N < 30$ noted for preliminary testing).
* **`FAIL`**: Explicit violation of protocol or mathematical invariant (e.g., hash mismatch, count inconsistency).
* **`INVALID`**: Fatal scientific corruption detected (e.g., look-ahead leakage, fake candles, hardcoded win rate). The entire associated trial batch is invalidated.
* **`DATA_UNAVAILABLE`**: Network feed dropped or historical endpoint unreachable; execution safely halted without synthetic fallback.
* **`UNVERIFIED`**: Artifact exists but lacks cryptographic provenance or parent execution node; requires further auditing.

---

### 4. Guardian Invariant Protocol

1. **Non-Intrusive Execution:**  
   The Guardian is an auditor, never an editor. Under no circumstances may Guardian code modify, recalculate, or overwrite experimental data to make an invalid experiment "pass".
2. **Immutable Audit Vault:**  
   All Guardian findings are written to a dedicated append-only log: `/audit_vault/guardian_findings.jsonl`.

---
*End of Guardian Specification (Phase 5).*
