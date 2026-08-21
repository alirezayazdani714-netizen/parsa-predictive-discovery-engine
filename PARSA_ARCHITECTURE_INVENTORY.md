# 🏛️ PARSA ARCHITECTURE INVENTORY
## REPOSITORY FORENSIC AUDIT & COMPONENT REGISTRY (PHASE 1)

**Role:** PARSA Software Architect & Repository Refactoring Engineer  
**Objective:** Recursive forensic identification and characterization of all existing codebase components, data artifacts, historical reports, execution scripts, and database models across the PARSA ecosystem.  
**Audit Standard:** Zero Scientific Claims, Zero Historical File Deletion, Complete Provenance Tracking, Rigorous Risk Categorization.

---

### 1. Repository Overview & Artifact Counts

| Component Type | Count | Directory / Primary Locations |
| :--- | :---: | :--- |
| **Python Execution & Forensic Engines** | 10 | `/scripts/*.py` |
| **Android / Kotlin Data & Domain Models** | 22 | `/app/src/main/java/com/example/data/*` |
| **Android UI & ViewModel Layer** | 3 | `/app/src/main/java/com/example/ui/*` |
| **Android Unit & Robolectric Tests** | 3 | `/app/src/test/java/com/example/*` |
| **Historical Scientific Reports (Markdown)** | 9 | `/*.md`, `/mission_*/*.md`, `/live_truth_tests/*/*.md` |
| **JSON Registries & Data Stores** | 65+ | Root JSON files, `/mission_*/`, `/historical_data_audit/` |
| **Data Manifests & Cryptographic Hashlogs** | 8 | `audit_sha256_manifest.json`, `data_provenance.json`, etc. |

---

### 2. Comprehensive Inventory of Python Source Engines (`/scripts/`)

| File Path | Primary Purpose | Layer Classification | Inputs | Outputs | Reads Market Data? | Generates Predictions? | Evaluates Predictions? | Writes Results? | Hardcoded Thresholds? | Risk Level |
| :--- | :--- | :---: | :--- | :--- | :---: | :---: | :---: | :---: | :---: | :---: |
| `scripts/binance_live_truth_runner.py` | Historical live prediction tester | **EXECUTOR / TEST** | Binance REST API | `live_truth_tests/` artifacts | **YES** | **YES** | **YES** | **YES** | YES (15 bps friction, $N=20$) | **HIGH** (Couples prediction + scoring) |
| `scripts/data_ingestion_audit.py` | Audits market data availability & gaps | **GUARDIAN** | Binance REST API | `historical_data_audit/` | **YES** | NO | NO | **YES** | YES ($1000$ limit) | **LOW** |
| `scripts/massive_100k_discovery_search.py` | 100k hypothesis permutation search | **LABORATORY** | Historical candles | `massive_discovery_audit/` | **YES** | **YES** | **YES** | **YES** | YES ($60\%$ WR gate) | **MEDIUM** (High search space) |
| `scripts/parsa_mission11_forensic_auditor.py` | Forensic auditor of past claims | **GUARDIAN / JUDGES** | Prior JSON reports & candles | `mission_11_forensic_audit/` | **YES** | NO | **YES** | **YES** | YES (Rigorous thresholds) | **LOW** |
| `scripts/parsa_mission12_novel_discovery_lab.py` | Novel hypothesis discovery lab | **LABORATORY / TEST** | Multi-asset candles | `mission_12_novel_discovery/` | **YES** | **YES** | **YES** | **YES** | YES (15 bps, $N=30$) | **MEDIUM** |
| `scripts/parsa_mission13_final_approval_gate.py` | Formal law approval gatekeeper | **JUDGES / GUARDIAN** | Multi-mission registries | `mission_13_final_approval_gate/` | **YES** | NO | **YES** | **YES** | YES (Class A/B criteria) | **LOW** |
| `scripts/parsa_mission14_anti_fabrication_gate.py` | Anti-fabrication gate & cleaner | **GUARDIAN / JUDGES** | Raw Binance API & code | `mission_14_anti_fabrication_gate/` | **YES** | **YES** | **YES** | **YES** | YES (Strict friction) | **LOW** |
| `scripts/parsa_mission19_live_forecasting_lab.py` | 19-discovery multi-horizon test | **EXECUTOR / TEST** | 6 Binance assets (1m–1h) | `mission_19_live_forecasting_lab/` | **YES** | **YES** | **YES** | **YES** | YES (Movement bands) | **MEDIUM** |
| `scripts/parsa_mission20_live_trial_engine.py` | 11-hour combined 2-layer trial | **EXECUTOR / TEST / REPORT** | BTC/ETH 1m & 15m live | `mission_20_live_trial_vault/` | **YES** | **YES** | **YES** | **YES** | YES (4 horizon bands) | **MEDIUM** (Multi-layer coupling) |
| `scripts/parsa_scientific_detective.py` | Forensic rule refuter & auditor | **GUARDIAN / JUDGES** | Historical test suites | `mission_10_scientific_audit/` | **YES** | NO | **YES** | **YES** | YES (Bonferroni corrections) | **LOW** |

---

### 3. Comprehensive Inventory of Android / Kotlin Source (`/app/src/main/java/`)

| File Path | Class / Responsibility | Architectural Layer | Layer Contract / Purpose | Risk Level |
| :--- | :--- | :---: | :--- | :---: |
| `data/AppDatabase.kt` | Room Database Definition | **DATA / INFRASTRUCTURE** | Local persistence for audit events, rules, and system state | **LOW** |
| `data/arbitration/ParsaFinalJudgeEngine.kt` | Law Arbitration & Rejection | **JUDGES** | High-level rule approval and falsification engine | **LOW** |
| `data/arbitration/Stage8ArbitrationEngine.kt` | Multi-phase statistical arbitration | **JUDGES** | Evaluates out-of-sample evidence against statistical criteria | **LOW** |
| `data/audit/AuditApiService.kt` | REST API for remote audit ingest | **INFRASTRUCTURE** | Network communication with audit data endpoints | **LOW** |
| `data/audit/AuditModels.kt` | Data transfer objects for audits | **DATA / CONTRACT** | Serializable data models for audit events | **LOW** |
| `data/batch/ResumableBatchProcessor.kt` | Resumable background processor | **EXECUTOR** | Executes large-scale historical processing jobs safely | **MEDIUM** |
| `data/dao/Daos.kt` | Room Data Access Objects | **DATA / ACCESS** | Read/write interface for Room database tables | **LOW** |
| `data/detective/DetectiveLawEngine.kt` | Detective rule refutation engine | **GUARDIAN** | Searches for rule anomalies, overfitting, and leakage | **LOW** |
| `data/entity/Entities.kt` | Room Entities definition | **DATA / MODEL** | Schema for stored rules, test results, and audit logs | **LOW** |
| `data/events/EventConditionAnalyzer.kt` | Orderflow & microstructural conditions | **LABORATORY** | Detects volume spikes, liquidity vacuums, orderflow imbalances | **MEDIUM** |
| `data/events/EventImpactAnalyzer.kt` | Post-event price excursion measurement | **TEST** | Measures MFE, MAE, and forward return after event triggers | **MEDIUM** |
| `data/events/HistoricalEventEngine.kt` | Historical event cataloguer | **LABORATORY / EXECUTOR** | Aggregates historical market anomalies across timeframes | **MEDIUM** |
| `data/indicators/HistoricalIndicatorEngine.kt`| Technical indicator calculation | **LABORATORY / EXECUTOR** | Computes RSI, MACD, EMA, ATR, Bollinger Bands | **LOW** |
| `data/integrity/DataIntegrityEngine.kt` | Data validation and checksums | **GUARDIAN** | Verifies candle continuity, gap detection, timestamp checks | **LOW** |
| `data/judgment/IndependentJudgmentEngine.kt` | Statistical verdict calculator | **JUDGES** | Computes t-statistics, p-values, and Bonferroni adjustments | **LOW** |
| `data/learning/BtcMarketRegimeEngine.kt` | Market regime classifier | **LABORATORY** | Classifies market state into Trend, Chop, Panic, Low-Vol | **MEDIUM** |
| `data/learning/HistoricalLearningEngine.kt` | Pattern learning and hypothesis engine | **LABORATORY** | Discovers candidate relationships across market regimes | **MEDIUM** |
| `data/live/BinanceLiveTruthWorker.kt` | Background WebSocket / REST worker | **EXECUTOR** | Fetches live Binance orderflow and candle streams | **HIGH** |
| `data/methods/AnalyticalMethodDiscoveryEngine.kt` | Automated method search engine | **LABORATORY** | Evaluates indicator permutations and signal rules | **MEDIUM** |
| `data/patterns/PatternDiscoveryEngine.kt` | Candlestick pattern extraction | **LABORATORY** | Identifies hammer, absorption wicks, kinetic thrust | **MEDIUM** |
| `data/provider/HistoricalDataProvider.kt` | Market data provider & cache | **INFRASTRUCTURE** | Abstracts local and remote candle data access | **LOW** |
| `data/repository/AuditRepository.kt` | Audit trail repository | **DATA / REPOSITORY** | Mediates between UI/Engine and database tables | **LOW** |
| `data/testing/AutomatedTestEngine.kt` | Automated backtesting engine | **TEST** | Replays historical data and executes backtest logic | **HIGH** |
| `data/timeframe/TimeframeAggregator.kt` | Multi-timeframe bar builder | **INFRASTRUCTURE** | Aggregates 1m candles into 5m, 15m, 45m, 1h, 1d | **LOW** |
| `data/universe/MarketUniverseManager.kt` | Asset universe configuration | **SCENARIO** | Defines tradable symbol universe and metadata | **LOW** |
| `ui/audit/AuditScreen.kt` | Compose UI for audit monitoring | **REPORT** | Renders audit logs, rule status, and system metrics | **LOW** |
| `ui/audit/AuditViewModel.kt` | ViewModel for audit dashboard | **REPORT** | Formats database logs for display | **LOW** |

---

### 4. Comprehensive Inventory of JSON Registries & Artifacts

| Directory / File | Type | Originating Mission | Description & Forensic Status | Risk Level |
| :--- | :---: | :---: | :--- | :---: |
| `data_provenance.json` | Manifest | M19 / M20 | Contains Binance endpoint, timestamps, and SHA-256 hashes | **LOW** |
| `discovery_inventory.json` | Registry | M19 | Catalog of 19 PARSA discoveries with source function paths | **LOW** |
| `locked_predictions.json` | Lock Log | M19 | 172 immutable prediction records created at time $T$ | **LOW** |
| `completed_outcomes.json` | Outcome | M19 | 516 scored multi-horizon evaluations | **LOW** |
| `discovery_performance_summary.json` | Summary | M19 | Horizon win rates and final verdicts per discovery | **LOW** |
| `mission_20_live_trial_vault/immutable_forecast_logs.json` | Lock Log | M20 | 88 immutable forecast records across 44 cycles | **LOW** |
| `mission_20_live_trial_vault/completed_outcome_evaluations.json` | Outcome | M20 | 352 evaluated outcomes across $+1\text{m}, +15\text{m}, +45\text{m}, +60\text{m}$ | **LOW** |
| `mission_20_live_trial_vault/trial_statistical_summary.json` | Summary | M20 | Comprehensive 11-hour statistical tracking dictionary | **LOW** |
| `mission_14_anti_fabrication_gate/*.json` | Forensics | M14 | Audit of code repairs, duplicate exclusions, and 22 honest answers | **LOW** |
| `mission_13_final_approval_gate/*.json` | Gatekeeper | M13 | Comprehensive method tables, overfit methods, and candidate laws | **LOW** |
| `mission_12_novel_discovery/*.json` | Lab Catalog | M12 | Novel hypothesis registrations, duplicate checks, OOS results | **LOW** |
| `mission_11_forensic_audit/*.json` | Forensics | M11 | Claim verifications, hardcode audit, data leakage audit | **LOW** |
| `mission_10_scientific_audit/*.json` | Forensics | M10 | Scientific memory, walk-forward results, failed hypotheses | **LOW** |
| `historical_data_audit/*.json` | Manifest | Ingest Audit | SHA-256 checksums of all 39 Binance spot historical datasets | **LOW** |
| `live_truth_tests/*/*.jsonl` | Raw Logs | Live Truth | Line-delimited raw market data and locked predictions | **LOW** |

---

### 5. Architectural Findings & Coupling Vulnerabilities

1. **Tight Coupling in Script Execution:**  
   Historical scripts (e.g., `scripts/binance_live_truth_runner.py`, `scripts/parsa_mission19_live_forecasting_lab.py`, `scripts/parsa_mission20_live_trial_engine.py`) combined **Laboratory Discovery**, **Execution/Locking**, **Horizon Scoring (Test)**, **Judicial Verdicts (Judges)**, and **Report Generation** within a single linear file.
2. **Access Control Gaps:**  
   While cryptographic SHA-256 locking was rigorously enforced in M19 and M20, the code generating the forecast had logical visibility of the entire dataset array if not explicitly partitioned by index slicing.
3. **Requirement for Strict Boundary Separation:**  
   To prevent accidental look-ahead, data contamination, or unverified claims in future work, the repository must establish explicit, isolated interface contracts between the 7 designated layers.

---
*End of Inventory Registry (Phase 1).*
