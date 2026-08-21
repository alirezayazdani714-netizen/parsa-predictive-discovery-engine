# 🗺️ PARSA LAYER MAP
## LOGICAL LAYER MAPPING & ARCHITECTURAL TAXONOMY (PHASE 2)

**Standard:** Clean Separation of Concerns, Explicit Isolation Boundaries, Auditable Contracts.  
**System Taxonomy:** 7 Autonomous Layers + Infrastructure / Unclassified.

---

### 1. The 7 Core Architectural Layers

```
                               ┌─────────────────────────────┐
                               │ 7. SCENARIO / EXPERIMENT    │
                               │    (Universe, Timeframes)   │
                               └──────────────┬──────────────┘
                                              ▼
┌─────────────────────────────┐  Approved      ┌─────────────────────────────┐
│ 1. LABORATORY               │  Protocols     │ 2. EXECUTOR                 │
│    (Hypotheses, Algorithms) ├───────────────►│    (Data Ingestion up to T,  │
└─────────────────────────────┘                │     Prediction Locking)     │
                                               └──────────────┬──────────────┘
                                                              │ Immutable Locked
                                                              │ Predictions
                                                              ▼
┌─────────────────────────────┐  Evaluations   ┌─────────────────────────────┐
│ 4. JUDGES                   │◄───────────────┤ 3. TEST                     │
│    (Statistical Verdicts,   │                │    (Maturity Scoring at     │
│     Law Classifications)    │                │     T+Δt, MFE/MAE Metrics)  │
└──────────────┬──────────────┘                └──────────────┬──────────────┘
               │                                              │
               │ Verified Verdicts                            │ Validated Outcomes
               ▼                                              ▼
┌────────────────────────────────────────────────────────────────────────────┐
│ 6. REPORT (Visual Dashboards, UI Screen, Final Scientific Markdown)        │
└────────────────────────────────────────────────────────────────────────────┘
               ▲
               │ Continuous Audit, Invalidation & Violation Detection
┌──────────────┴─────────────────────────────────────────────────────────────┐
│ 5. GUARDIAN / INSPECTOR (Independent Verification of All Layers)            │
└────────────────────────────────────────────────────────────────────────────┘
```

---

### 2. Detailed Mapping of Codebase Components

#### LAYER 1: LABORATORY (`LAB`)
*Responsibility:* Hypothesis formulation, discovery search algorithms, mathematical signal definitions, pattern recognition logic, and feature engineering.
*Access Boundary:* Strictly prohibited from observing any future price candles ($t > T$).
*Mapped Files:*
* `scripts/massive_100k_discovery_search.py` (Hypothesis search algorithms)
* `scripts/parsa_mission12_novel_discovery_lab.py` [Lab Subroutines] (Discovery formulation)
* `app/src/main/java/com/example/data/events/EventConditionAnalyzer.kt` (Microstructure signal definitions)
* `app/src/main/java/com/example/data/events/HistoricalEventEngine.kt` (Anomaly cataloguer)
* `app/src/main/java/com/example/data/indicators/HistoricalIndicatorEngine.kt` (Indicator calculations)
* `app/src/main/java/com/example/data/learning/BtcMarketRegimeEngine.kt` (Regime classification logic)
* `app/src/main/java/com/example/data/learning/HistoricalLearningEngine.kt` (Pattern extraction)
* `app/src/main/java/com/example/data/methods/AnalyticalMethodDiscoveryEngine.kt` (Permutation generator)
* `app/src/main/java/com/example/data/patterns/PatternDiscoveryEngine.kt` (Candle pattern definitions)

---

#### LAYER 2: EXECUTOR (`EXECUTOR`)
*Responsibility:* Safe live and historical data ingestion, real-time feature computation, execution event generation, and immutable cryptographic prediction locking at time $T$.
*Access Boundary:* Ingests market data strictly up to current timestamp $T$. Cannot read future bars ($t > T$). Cannot score its own predictions.
*Mapped Files:*
* `scripts/binance_live_truth_runner.py` [Execution Engine]
* `scripts/parsa_mission19_live_forecasting_lab.py` [Ingestion & Lock Engine]
* `scripts/parsa_mission20_live_trial_engine.py` [Data Fetcher & Lock Engine]
* `app/src/main/java/com/example/data/live/BinanceLiveTruthWorker.kt` (Binance REST/WS worker)
* `app/src/main/java/com/example/data/batch/ResumableBatchProcessor.kt` (Execution queue processor)

---

#### LAYER 3: TEST (`TEST`)
*Responsibility:* Post-maturity outcome evaluation ($T + \Delta t$), forward price excursion calculation (MFE, MAE, realized return), friction-adjusted trade simulation, and test replay.
*Access Boundary:* May only inspect outcomes after a prediction's maturity timestamp has passed. Cannot alter locked prediction parameters.
*Mapped Files:*
* `scripts/parsa_mission19_live_forecasting_lab.py` [Scoring Subroutine]
* `scripts/parsa_mission20_live_trial_engine.py` [Multi-Horizon Evaluation Engine]
* `app/src/main/java/com/example/data/events/EventImpactAnalyzer.kt` (Excursion & return measurement)
* `app/src/main/java/com/example/data/testing/AutomatedTestEngine.kt` (Backtest evaluation harness)
* `app/src/test/java/com/example/ExampleRobolectricTest.kt` (Unit test suite)

---

#### LAYER 4: JUDGES (`JUDGES`)
*Responsibility:* Falsification testing, statistical significance calculations (t-statistic, p-value, Bonferroni adjustment, false discovery rate), out-of-sample stability validation, and scientific law classification (Class A/B/Candidate/Rejected).
*Access Boundary:* Operates strictly on validated test outputs from Layer 3. Cannot modify raw execution logs.
*Mapped Files:*
* `scripts/parsa_mission13_final_approval_gate.py` (Approval arbitration engine)
* `scripts/parsa_scientific_detective.py` (Statistical refutation engine)
* `app/src/main/java/com/example/data/arbitration/ParsaFinalJudgeEngine.kt` (Final decision arbitrator)
* `app/src/main/java/com/example/data/arbitration/Stage8ArbitrationEngine.kt` (Multi-stage statistical gate)
* `app/src/main/java/com/example/data/judgment/IndependentJudgmentEngine.kt` (Significance calculator)

---

#### LAYER 5: GUARDIAN / INSPECTOR (`GUARDIAN`)
*Responsibility:* Autonomous, non-intrusive forensic audit of all layers. Detects look-ahead leakage, fake candles, synthetic fallbacks, hardcoded results, duplicate discoveries, timestamp discrepancies, and unauthorized cross-layer file access.
*Access Boundary:* Read-only inspection across all layers, artifacts, and source code. Prohibited from mutating any scientific or execution data.
*Mapped Files:*
* `scripts/data_ingestion_audit.py` (Data continuity and gap auditor)
* `scripts/parsa_mission11_forensic_auditor.py` (Forensic integrity verifier)
* `scripts/parsa_mission14_anti_fabrication_gate.py` (Anti-fabrication scanner)
* `app/src/main/java/com/example/data/detective/DetectiveLawEngine.kt` (Anomaly and overfitting inspector)
* `app/src/main/java/com/example/data/integrity/DataIntegrityEngine.kt` (Data hash & gap verifier)

---

#### LAYER 6: REPORT (`REPORT`)
*Responsibility:* Synthesis of validated results into human-readable markdown reports, audit dashboards, and Jetpack Compose Android UI visualizations.
*Access Boundary:* Read-only consumer of Judge verdicts and Guardian certificates. Strictly prohibited from manufacturing, altering, or projecting unverified data.
*Mapped Files:*
* `app/src/main/java/com/example/ui/audit/AuditScreen.kt` (Compose UI Dashboard)
* `app/src/main/java/com/example/ui/audit/AuditViewModel.kt` (UI Presentation state)
* `app/src/main/java/com/example/MainActivity.kt` (UI entry point)
* All Markdown historical mission summaries (`MISSION_11_*.md`, `MISSION_13_*.md`, `MISSION_14_*.md`, `MISSION_19_*.md`, `MISSION_20_*.md`)

---

#### LAYER 7: SCENARIO / EXPERIMENT DESIGN (`SCENARIO`)
*Responsibility:* Specification of experimental parameters: tradable asset universe, timeframe resolutions, transaction friction assumptions, evaluation horizon grids ($+1\text{m}, +15\text{m}, +45\text{m}, +60\text{m}$), and out-of-sample date splits.
*Access Boundary:* Provides configuration inputs to LAB and EXECUTOR. Cannot alter active execution or declare experiment outcomes.
*Mapped Files:*
* `app/src/main/java/com/example/data/universe/MarketUniverseManager.kt` (Asset universe and parameter configurations)

---

#### INFRASTRUCTURE & DATA PERSISTENCE (`INFRASTRUCTURE`)
*Responsibility:* Storage engines, database DAOs, network REST clients, and timeframe bar aggregation utilities.
*Mapped Files:*
* `app/src/main/java/com/example/data/AppDatabase.kt`
* `app/src/main/java/com/example/data/dao/Daos.kt`
* `app/src/main/java/com/example/data/entity/Entities.kt`
* `app/src/main/java/com/example/data/repository/AuditRepository.kt`
* `app/src/main/java/com/example/data/provider/HistoricalDataProvider.kt`
* `app/src/main/java/com/example/data/timeframe/TimeframeAggregator.kt`
* `app/src/main/java/com/example/data/audit/AuditApiService.kt`
* `app/src/main/java/com/example/data/audit/AuditModels.kt`

---
*End of Layer Taxonomy Map (Phase 2).*
