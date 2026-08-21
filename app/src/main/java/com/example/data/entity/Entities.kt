package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val role: String, // e.g. "AUDITOR", "SYSTEM", "DEVELOPER"
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

@Entity(tableName = "system_state")
data class SystemStateEntity(
    @PrimaryKey
    val stateKey: String,
    val value: String,
    val stage: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "experiments")
data class ExperimentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String,
    val status: String, // "QUEUED", "RUNNING", "COMPLETED", "NOT_IMPLEMENTED"
    val configJson: String,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

@Entity(tableName = "test_runs")
data class TestRunEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val suiteName: String,
    val status: String, // "PASSED", "FAILED", "RUNNING"
    val passedCount: Int,
    val failedCount: Int,
    val totalCount: Int,
    val startedAt: Long = System.currentTimeMillis(),
    val durationMs: Long = 0,
    val executedBy: String = "AUTOMATED_HARNESS"
)

@Entity(tableName = "test_results")
data class TestResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val runId: Long,
    val testName: String,
    val category: String, // "UNIT", "INTEGRATION", "VALIDATION", "FUTURE_STUB"
    val status: String, // "PASSED", "FAILED", "SKIPPED", "NOT_IMPLEMENTED"
    val errorMessage: String? = null,
    val executionTimeMs: Long = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val level: String, // "INFO", "WARN", "ERROR", "SECURITY"
    val category: String, // "BUILD", "DATABASE", "TEST", "API", "SYSTEM"
    val message: String,
    val detailsJson: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "model_versions")
data class ModelVersionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val modelName: String,
    val versionTag: String,
    val architecture: String,
    val status: String, // "INITIALIZED", "NOT_IMPLEMENTED", "READY"
    val weightsHash: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "memory_versions")
data class MemoryVersionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val memoryKey: String,
    val version: Int,
    val schemaVersion: String,
    val recordCount: Long = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "market_concepts")
data class MarketConceptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val conceptCode: String,
    val title: String,
    val category: String, // "MARKET_STRUCTURE", "ORDER_BOOK", "LIQUIDITY", "VOLATILITY", "RISK_CONTROL"
    val description: String,
    val difficultyLevel: Int = 1, // 1: Beginner, 2: Intermediate, 3: Advanced
    val deterministicRulesJson: String,
    val isVerified: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "risk_rules")
data class RiskRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ruleCode: String,
    val name: String,
    val category: String, // "POSITION_SIZING", "DRAWDOWN_LIMIT", "LEVERAGE_CAP", "EXPOSURE"
    val formulaOrLogic: String,
    val maxAllowedRiskPct: Double,
    val isMandatory: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "education_progress")
data class EducationProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val conceptCode: String,
    val isCompleted: Boolean = false,
    val scorePct: Double = 0.0,
    val lastEvaluatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "market_assets")
data class MarketAssetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val symbol: String, // e.g. "BTC/USDT"
    val name: String,
    val marketType: String = "SPOT", // "SPOT", "PERPETUAL", "FUTURES"
    val exchange: String = "PRIMARY_AGGREGATOR",
    val marketCapRank: Int = 1, // Supports 1 to 1200+
    val genesisTimestamp: Long? = null, // Real start date, never backfilled with fake data
    val firstSeenAt: Long = System.currentTimeMillis(),
    val lastSeenAt: Long = System.currentTimeMillis(),
    val dataAvailabilityPct: Double = 100.0,
    val supportedTimeframes: String = "1m,5m,15m,30m,1h,4h,1d,1w",
    val status: String = "ACTIVE", // "ACTIVE", "DELISTED", "UNINITIALIZED", "DISCONTINUED"
    val schemaVersion: Int = 1,
    val sourceMetadataJson: String = "{}"
)

@Entity(
    tableName = "historical_candles",
    indices = [
        Index(value = ["symbol", "timeframe", "openTime"], unique = true)
    ]
)
data class HistoricalCandleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val symbol: String,
    val timeframe: String, // "1m", "5m", "15m", "30m", "1h", "4h", "1d", "1w"
    val openTime: Long,
    val closeTime: Long,
    val openPrice: Double,
    val highPrice: Double,
    val lowPrice: Double,
    val closePrice: Double,
    val volume: Double,
    val quoteVolume: Double = 0.0,
    val tradesCount: Long = 0,
    val isClosed: Boolean = true,
    val integrityChecked: Boolean = true,
    val source: String = "HISTORICAL_ARCHIVE"
)

@Entity(
    tableName = "experience_memories",
    indices = [
        Index(value = ["assetSymbol", "timeframe", "timestamp"])
    ]
)
data class ExperienceMemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val experienceId: String,
    val assetSymbol: String,
    val timeframe: String,
    val timestamp: Long,
    val marketState: String, // "BULLISH_TREND", "BEARISH_TREND", "RANGE_BOUND", "HIGH_VOLATILITY", "ACCUMULATION", "DISTRIBUTION"
    val detectedPattern: String, // "BREAKOUT", "SUPPORT_BOUNCE", "RESISTANCE_REJECTION", "FALSE_BREAKOUT", "ORDERBOOK_ABSORPTION"
    val conceptCode: String,
    val ruleUsed: String,
    val expectedOutcome: String,
    val actualOutcome: String? = null,
    val errorMagnitude: Double? = null,
    val errorType: String = "NONE", // "NONE", "DIRECTIONAL_MISS", "MAGNITUDE_ERROR", "TIMING_ERROR", "REGIME_SHIFT"
    val lessonLearned: String,
    val confidence: Double = 1.0,
    val isWalkForwardVerified: Boolean = true,
    val memoryVersion: Int = 1,
    val crossAssetCorrelatedCount: Int = 0,
    val eventState: String = "NONE",
    val indicatorState: String = "{}",
    val marketRegime: String = "NEUTRAL",
    val prediction: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cross_asset_insights")
data class CrossAssetInsightEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val insightCode: String,
    val patternOrConcept: String,
    val primaryAsset: String,
    val correlatedAssetsJson: String,
    val sampleSize: Int,
    val statisticalConfidence: Double,
    val consistencyScore: Double,
    val findingsSummary: String,
    val evidenceHash: String,
    val isVerified: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "data_integrity_anomalies")
data class DataIntegrityAnomalyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val symbol: String,
    val timeframe: String,
    val anomalyType: String, // "MISSING_DATA", "DUPLICATE_DATA", "TIMESTAMP_ERROR", "OUT_OF_ORDER", "IMPOSSIBLE_PRICE", "ABNORMAL_GAP", "DELISTED_GAP", "INSUFFICIENT_HISTORY", "TIMEFRAME_BOUNDARY_MISMATCH", "INVALID_EVENT_TIMESTAMP"
    val severity: String, // "LOW", "MEDIUM", "HIGH", "CRITICAL"
    val targetTimestamp: Long,
    val details: String,
    val isResolved: Boolean = false,
    val detectedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "historical_events",
    indices = [
        Index(value = ["eventId"], unique = true),
        Index(value = ["eventTimestamp"])
    ]
)
data class HistoricalEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventId: String,
    val eventTimestamp: Long,
    val source: String,
    val title: String,
    val eventType: String, // "EXCHANGE_LISTING", "DELISTING", "HACK_EXPLOIT", "BANKRUPTCY", "PROTOCOL_UPGRADE", "ETF_DECISION", "CPI_RELEASE", "RATE_DECISION", "HALVING", "REGULATORY", "NETWORK_LAUNCH"
    val category: String = "MARKET_STRUCTURE", // "MACRO", "REGULATORY", "ON_CHAIN", "PROTOCOL", "EXCHANGE", "CREDIT", "MARKET_STRUCTURE"
    val severity: String = "HIGH", // "LOW", "MEDIUM", "HIGH", "CRITICAL"
    val primarySymbol: String = "BTC/USDT",
    val affectedAssetsJson: String,
    val sourceUrl: String = "",
    val confidence: Double = 1.0,
    val marketImpactStatus: String = "PENDING", // "ANALYZED", "DATA_UNAVAILABLE", "PENDING"
    val preEventState: String = "NEUTRAL",
    val postEventState: String = "NEUTRAL",
    val details: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "event_impacts",
    indices = [
        Index(value = ["eventId", "assetSymbol", "horizon"], unique = true)
    ]
)
data class EventImpactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventId: String,
    val assetSymbol: String,
    val horizon: String, // "1m", "5m", "15m", "30m", "1h", "4h", "24h"
    val priceBefore: Double,
    val priceAtEvent: Double,
    val priceAfter: Double,
    val pctChange: Double,
    val maxFavorableExcursion: Double = 0.0,
    val maxAdverseExcursion: Double = 0.0,
    val highLowExcursion: Double = 0.0,
    val volumeChangePct: Double,
    val volatilityChangePct: Double,
    val direction: String = "NEUTRAL", // "UP", "DOWN", "NEUTRAL"
    val trendChange: String, // "BULLISH_CONTINUATION", "BEARISH_CONTINUATION", "BULLISH_REVERSAL", "BEARISH_REVERSAL", "NEUTRAL"
    val recoveryTimeMs: Long? = null,
    val maxDrawdown: Double = 0.0,
    val impactScore: Double = 0.0,
    val btcCorrelation: Double,
    val isBtcDriven: Boolean,
    val calculatedAt: Long = System.currentTimeMillis(),
    val status: String = "VALID" // "VALID", "DATA_UNAVAILABLE"
)

@Entity(
    tableName = "historical_setups",
    indices = [
        Index(value = ["setupId"], unique = true),
        Index(value = ["eventId"]),
        Index(value = ["assetSymbol", "timestamp"])
    ]
)
data class HistoricalSetupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val setupId: String,
    val eventId: String,
    val assetSymbol: String,
    val timestamp: Long,
    val marketRegime: String,
    val trend: String,
    val indicatorStatesJson: String,
    val volumeState: String,
    val volatilityState: String,
    val marketStructure: String,
    val eventCharacteristicsJson: String,
    val historicalPrediction: String,
    val actualFutureOutcome: String? = null,
    val predictionError: Double? = null,
    val confidence: Double = 1.0,
    val evaluationHorizon: String = "1h",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "indicator_snapshots",
    indices = [
        Index(value = ["symbol", "timeframe", "timestamp"], unique = true)
    ]
)
data class HistoricalIndicatorSnapshotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val symbol: String,
    val timeframe: String,
    val timestamp: Long,
    val sma20: Double? = null,
    val ema20: Double? = null,
    val wma20: Double? = null,
    val rsi14: Double? = null,
    val macdLine: Double? = null,
    val macdSignal: Double? = null,
    val macdHist: Double? = null,
    val bbUpper: Double? = null,
    val bbMiddle: Double? = null,
    val bbLower: Double? = null,
    val atr14: Double? = null,
    val adx14: Double? = null,
    val stochK: Double? = null,
    val stochD: Double? = null,
    val cci20: Double? = null,
    val roc12: Double? = null,
    val vwap: Double? = null,
    val obv: Double? = null,
    val volatility: Double? = null,
    val momentum: Double? = null,
    val trendStrength: Double? = null,
    val supportLevel: Double? = null,
    val resistanceLevel: Double? = null,
    val calculatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "batch_processing_checkpoints")
data class BatchProcessingCheckpointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pipelineName: String,
    val lastProcessedAssetIndex: Int,
    val lastProcessedSymbol: String,
    val lastProcessedTimestamp: Long,
    val totalAssetsCount: Int,
    val processedRecordsCount: Long,
    val batchSize: Int = 50,
    val status: String = "COMPLETED", // "IN_PROGRESS", "PAUSED", "COMPLETED", "FAILED"
    val lastCheckpointTime: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
)

@Entity(
    tableName = "discovered_patterns",
    indices = [
        Index(value = ["patternId"], unique = true),
        Index(value = ["timeframe"]),
        Index(value = ["evidenceGrade"])
    ]
)
data class DiscoveredPatternEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patternId: String,
    val patternName: String,
    val conditionsJson: String,
    val assetSymbolsJson: String,
    val timeframe: String,
    val historicalPeriod: String,
    val sampleSize: Int,
    val occurrences: Int,
    val positiveOutcomes: Int,
    val negativeOutcomes: Int,
    val neutralOutcomes: Int,
    val outcomeDistributionJson: String,
    val volatility: Double,
    val maxFavorableExcursion: Double = 0.0,
    val maxAdverseExcursion: Double = 0.0,
    val drawdown: Double = 0.0,
    val recoveryTimeMs: Long? = null,
    val confidence: Double = 1.0,
    val evidenceGrade: String = "EXPLORATORY", // "INSUFFICIENT_DATA", "EXPLORATORY", "REPEATED", "ROBUST"
    val sourceDataVersion: String = "HISTORICAL_ARCHIVE_V5",
    val discoveredAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "analytical_methods",
    indices = [
        Index(value = ["methodId", "methodVersion"], unique = true),
        Index(value = ["evidenceGrade"]),
        Index(value = ["status"]),
        Index(value = ["timeframe"])
    ]
)
data class AnalyticalMethodEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val methodId: String,
    val methodVersion: Int = 1,
    val methodName: String,
    val hypothesisDescription: String,
    val indicatorsUsedJson: String, // e.g. ["RSI","SMA20","EMA50","ATR14","BB"]
    val featuresUsedJson: String,   // e.g. ["MOMENTUM_ACCELERATION","VOLATILITY_RATIO","MARKET_STRUCTURE"]
    val eventFeaturesUsedJson: String, // e.g. ["POST_HALVING_WINDOW","MACRO_RATE_CYCLE"]
    val timeframe: String,          // e.g. "1h", "4h", "1d", "MULTI_TIMEFRAME"
    val assetUniverseJson: String,  // e.g. ["BTC/USDT","ETH/USDT","SOL/USDT","BNB/USDT"]
    val discoveryPeriod: String,    // e.g. "2018-01-01 to 2021-12-31"
    val validationPeriod: String,   // e.g. "2022-01-01 to 2023-06-30"
    val outOfSamplePeriod: String,  // e.g. "2023-07-01 to 2024-12-31"
    val sampleCount: Int,
    val positiveOutcomes: Int,
    val negativeOutcomes: Int,
    val neutralOutcomes: Int,
    val baselineSampleCount: Int,
    val baselinePositiveRate: Double,
    val methodPositiveRate: Double,
    val outperformanceVsBaseline: Double,
    val averageOutcome: Double,
    val medianOutcome: Double,
    val dispersion: Double,
    val volatility: Double,
    val maxFavorableExcursion: Double = 0.0,
    val maxAdverseExcursion: Double = 0.0,
    val maxDrawdown: Double = 0.0,
    val recoveryTimeMs: Long? = null,
    val evidenceGrade: String = "EXPLORATORY", // "INSUFFICIENT_DATA", "EXPLORATORY", "WEAK", "REPEATED", "ROBUST", "UNSTABLE", "REJECTED"
    val status: String = "UNDER_EVALUATION", // "RETAINED", "REJECTED", "INCONCLUSIVE", "UNDER_EVALUATION"
    val parameterSensitivityScore: Double = 0.0, // 0.0 to 1.0 (lower is more stable)
    val parameterStabilityGrade: String = "STABLE", // "STABLE", "MODERATE", "SENSITIVE", "UNSTABLE"
    val crossRegimeStabilityScore: Double = 0.0,
    val crossAssetStabilityScore: Double = 0.0,
    val outOfSampleSurvives: Boolean = false,
    val adversarialPassed: Boolean = false,
    val failureClassification: String? = null, // "OVERFIT", "INSUFFICIENT_SAMPLE", "REGIME_DEPENDENT", "ASSET_DEPENDENT", "TIMEFRAME_DEPENDENT", "EVENT_DEPENDENT", "PARAMETER_SENSITIVE", "BASELINE_NOT_BEATEN", "OUT_OF_SAMPLE_FAILURE", "DATA_QUALITY_FAILURE", "UNSTABLE_RELATIONSHIP"
    val failureReasonsJson: String? = null,
    val limitations: String = "Research hypothesis only — no live signals.",
    val sourceDataVersion: String = "HISTORICAL_ARCHIVE_V6",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "method_evaluations",
    indices = [
        Index(value = ["methodId"]),
        Index(value = ["evaluationType"]),
        Index(value = ["passed"])
    ]
)
data class MethodEvaluationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val evaluationId: String,
    val methodId: String,
    val methodVersion: Int,
    val evaluationType: String, // "WALK_FORWARD", "OUT_OF_SAMPLE", "ADVERSARIAL_SHOCK", "CROSS_REGIME", "CROSS_ASSET", "PARAMETER_SENSITIVITY", "EVENT_AWARE"
    val targetSymbol: String,
    val targetTimeframe: String,
    val evaluationWindow: String,
    val sampleSize: Int,
    val successRate: Double,
    val baselineSuccessRate: Double,
    val mfe: Double = 0.0,
    val mae: Double = 0.0,
    val passed: Boolean,
    val verdict: String,
    val detailsJson: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "method_judgments",
    indices = [
        Index(value = ["methodId"]),
        Index(value = ["methodVersion"]),
        Index(value = ["evidenceGrade"]),
        Index(value = ["timestamp"])
    ]
)
data class MethodJudgmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val judgmentId: String,
    val methodId: String,
    val methodVersion: Int,
    val methodCategoriesJson: String,
    val hypothesis: String,
    val evidenceGrade: String, // "INSUFFICIENT_EVIDENCE", "EXPLORATORY", "REPEATED", "ROBUST", "UNSTABLE", "OVERFIT", "REGIME_DEPENDENT", "OOS_FAILURE", "REJECTED_EVIDENCE"
    val sampleCount: Int,
    val dateRange: String,
    val assetCount: Int,
    val regimeCount: Int,
    val inSampleResultJson: String,
    val validationResultJson: String,
    val outOfSampleResultJson: String,
    val walkForwardResultJson: String,
    val baselineComparisonJson: String,
    val parameterSensitivityJson: String,
    val crossAssetResultJson: String,
    val crossRegimeResultJson: String,
    val multiTimeframeResultJson: String,
    val failurePatternsJson: String,
    val dataQualityJson: String,
    val futureLeakageResultJson: String,
    val knownLimitations: String,
    val lessonsLearnedJson: String,
    val geminiJudgement: String,
    val confidenceOfJudgement: Double,
    val sourceDatasetVersion: String = "HISTORICAL_ARCHIVE_V7",
    val sourceCodeVersion: String = "PARSA_PHASE_7_AUDIT",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "lessons_learned",
    indices = [
        Index(value = ["category"]),
        Index(value = ["evidenceType"])
    ]
)
data class LessonLearnedEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val lessonId: String,
    val category: String, // "Trend Lessons", "Momentum Lessons", "Volatility Lessons", "Volume Lessons", "Event Lessons", "Market Structure Lessons", "Multi-Timeframe Lessons", "Cross-Asset Lessons", "Regime Lessons", "Failure Lessons", "Overfitting Lessons", "Data Quality Lessons"
    val title: String,
    val description: String,
    val associatedMethodId: String? = null,
    val evidenceType: String = "POSITIVE", // "POSITIVE", "NEGATIVE", "OBSERVATIONAL", "METHODOLOGICAL"
    val confidence: Double = 0.90,
    val sourceMethodId: String? = null,
    val historicalObservation: String = "",
    val rootCause: String = "",
    val evidenceSummary: String = "",
    val consequenceOrOutcome: String = "",
    val limitation: String = "",
    val applicableRegime: String? = null,
    val applicableAssets: String? = null,
    val applicableTimeframe: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    val lessonTitle: String get() = title
}

@Entity(
    tableName = "method_arbitrations",
    indices = [
        Index(value = ["methodId"]),
        Index(value = ["geminiTemporaryClassification"]),
        Index(value = ["timestamp"])
    ]
)
data class MethodArbitrationReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val arbitrationId: String,
    val methodId: String,
    val methodName: String,
    val hypothesis: String,
    val analyticalLogic: String,
    val assetsTestedJson: String,
    val timeframesJson: String,
    val sampleCount: Int,
    val discoveryPerformanceJson: String,
    val validationPerformanceJson: String,
    val outOfSamplePerformanceJson: String,
    val baselineComparisonJson: String,
    val maxFavorableExcursion: Double,
    val maxAdverseExcursion: Double,
    val maxDrawdown: Double,
    val recoveryTimeDescription: String,
    val parameterSensitivityScore: Double,
    val crossAssetStabilityScore: Double,
    val crossRegimeStabilityScore: Double,
    val strengthsJson: String,
    val weaknessesJson: String,
    val observedFailuresJson: String,
    val overfittingRisksJson: String,
    val dataLimitationsJson: String,
    val geminiTemporaryClassification: String, // "Candidate", "Promising", "Repeated", "Robust", "Unstable", "Rejected"
    val geminiArbitrationNotes: String,
    val confidence: Double = 0.90,
    val decisionAuthority: String = "ADVISORY_ONLY",
    val canApprove: Boolean = false,
    val canReject: Boolean = false,
    val canDeleteRule: Boolean = false,
    val advisoryNotice: String = "Advisory only. Zero governance effect. Final approval strictly requires PARSA Final Judge.",
    val datasetVersion: String = "HISTORICAL_ARCHIVE_V8",
    val timestamp: Long = System.currentTimeMillis()
)

data class MethodEvidencePacket(
    val methodId: String,
    val methodVersion: Int,
    val hypothesis: String,
    val discoveryPeriod: String,
    val validationPeriod: String,
    val outOfSamplePeriod: String,
    val sampleCount: Int,
    val baselineMetricsJson: String,
    val methodMetricsJson: String,
    val outperformance: Double,
    val maxFavorableExcursion: Double,
    val maxAdverseExcursion: Double,
    val maxDrawdown: Double,
    val recoveryTimeDescription: String,
    val parameterSensitivity: Double,
    val crossAssetStability: Double,
    val crossRegimeStability: Double,
    val timeframeResultsJson: String,
    val successfulSamplesJson: String,
    val failedSamplesJson: String,
    val failureClassification: String,
    val overfittingRisksJson: String,
    val dataLimitationsJson: String,
    val stage7JudgmentSummary: String,
    val stage7LessonsJson: String,
    val negativeKnowledgeJson: String,
    val historicalEvidenceSummary: String,
    val datasetVersion: String,
    val lineagePath: String
)

@Entity(
    tableName = "gemini_arbitration_reports",
    indices = [
        Index(value = ["reportId"], unique = true),
        Index(value = ["methodId"]),
        Index(value = ["advisoryClassification"])
    ]
)
data class GeminiArbitrationReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val reportId: String,
    val methodId: String,
    val evidenceSnapshotJson: String,
    val strengthsJson: String,
    val weaknessesJson: String,
    val contradictionsJson: String,
    val overfittingConcernsJson: String,
    val regimeConcernsJson: String,
    val dataLimitationsJson: String,
    val suggestedAdditionalTestsJson: String,
    val advisoryClassification: String, // "Candidate", "Promising", "Repeated", "Robust", "Unstable", "Rejected"
    val confidence: Double = 0.85,
    val reasoning: String,
    val decisionAuthority: String = "ADVISORY_ONLY",
    val canApprove: Boolean = false,
    val canReject: Boolean = false,
    val canDeleteRule: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "final_judge_decisions",
    indices = [
        Index(value = ["decisionId"], unique = true),
        Index(value = ["methodId"]),
        Index(value = ["decision"])
    ]
)
data class FinalJudgeDecisionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val decisionId: String,
    val methodId: String,
    val decision: String, // "APPROVE", "REJECT", "RETURN_FOR_MORE_TESTING"
    val evidenceScore: Double,
    val robustnessScore: Double,
    val generalizationScore: Double,
    val overfitRiskScore: Double,
    val confidence: Double,
    val reasoning: String,
    val requiredAdditionalTests: String,
    val sourceGeminiReportId: String,
    val sourceEvidenceVersion: String,
    val judgeVersion: String = "PARSA_FINAL_JUDGE_V1",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "candidate_rules",
    indices = [
        Index(value = ["ruleId"], unique = true),
        Index(value = ["sourceMethodId"]),
        Index(value = ["status"]),
        Index(value = ["isApproved"])
    ]
)
data class CandidateRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ruleId: String,
    val ruleTitle: String,
    val sourceMethodId: String,
    val sourceLessonId: String,
    val sourceJudgmentId: String,
    val lineagePath: String, // "Discovery -> Evidence -> Gemini Judgment -> Lesson -> Candidate Rule"
    val activationConditionsJson: String,
    val invalidationConditionsJson: String,
    val requiredInputsJson: String,
    val timeHorizon: String,
    val targetMarkets: String,
    val suitableRegime: String,
    val historicalEvidenceSummary: String,
    val advantagesJson: String,
    val risksJson: String,
    val limitationsJson: String,
    val successfulSamplesJson: String,
    val failureSamplesJson: String,
    val status: String = "CANDIDATE_ONLY_PENDING_PARSA_FINAL_JUDGE", // "CANDIDATE", "UNDER_ARBITRATION", "RETURNED_FOR_TESTING", "APPROVED_PENDING_LOCK", "REJECTED", "LOCKED"
    val geminiArbitrationOpinion: String,
    val confidenceScore: Double = 0.88,
    val isApproved: Boolean = false, // Strictly invariant: false until human / PARSA final governance in Stage 9+
    val versionTag: String = "STAGE_8_CANDIDATE_V1",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "emerging_patterns",
    indices = [
        Index(value = ["patternId"], unique = true),
        Index(value = ["status"])
    ]
)
data class EmergingPatternEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patternId: String,
    val title: String,
    val hypothesis: String,
    val discoveryPeriod: String,
    val currentSampleSize: Int,
    val initialObservationJson: String,
    val potentialRegimesJson: String,
    val status: String = "EMERGING_PATTERN", // Preserved, not approved, not deleted
    val confidence: Double,
    val reasonPreserved: String,
    val suggestedFutureTests: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "cross_asset_clusters",
    indices = [
        Index(value = ["clusterId"], unique = true),
        Index(value = ["clusterType"])
    ]
)
data class CrossAssetClusterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clusterId: String,
    val clusterName: String,
    val clusterType: String, // "MOMENTUM", "MEAN_REVERSION", "VOLATILITY", "HIGH_BETA", "DEFENSIVE", "BTC_SENSITIVE"
    val assetsJson: String,
    val behavioralSignature: String,
    val correlationToBtc: Double,
    val regimeStabilityScore: Double,
    val empiricalBasis: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "lead_lag_relationships",
    indices = [
        Index(value = ["relationshipId"], unique = true),
        Index(value = ["leaderAsset", "laggerAsset"])
    ]
)
data class LeadLagRelationshipEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val relationshipId: String,
    val leaderAsset: String,
    val laggerAsset: String,
    val timeLagDescription: String,
    val sampleSize: Int,
    val correlationScore: Double,
    val outOfSampleStability: Double,
    val regimeSensitivity: String,
    val isCausationClaimed: Boolean = false, // Hard Invariant: Correlation != Causation
    val status: String = "CANDIDATE_LEAD_LAG_PENDING_GOVERNANCE",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "negative_knowledge_registry",
    indices = [
        Index(value = ["knowledgeId"], unique = true),
        Index(value = ["failureCategory"])
    ]
)
data class NegativeKnowledgeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val knowledgeId: String,
    val title: String,
    val failureCategory: String, // "TREND_FOLLOWING_FAILURE", "FALSE_BREAKOUT", "OVERFITTING", "REGIME_TRANSITION_FAILURE", "PARAMETER_SENSITIVITY", "OOS_FAILURE"
    val predictedOutcome: String,
    val actualOutcome: String,
    val rootCause: String,
    val regimeObserved: String,
    val recurrenceCount: Int,
    val generalizability: String,
    val extractedLesson: String,
    val sourceMethodId: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "parsa_rule_book",
    indices = [
        Index(value = ["ruleCode"], unique = true),
        Index(value = ["versionTag"]),
        Index(value = ["status"])
    ]
)
data class ParsaRuleBookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ruleCode: String, // "RULE-001"
    val versionTag: String, // "RULE-001-V1"
    val ruleTitle: String,
    val status: String = "STAGE_8_CANDIDATE_SPECIFICATION", // "STAGE_8_CANDIDATE_SPECIFICATION", "APPROVED_RULE_V1", "LOCKED_RULE"
    val evidenceScore: Double,
    val conditionsJson: String,
    val invalidationJson: String,
    val applicableAssetsJson: String,
    val applicableRegimesJson: String,
    val applicableTimeframesJson: String,
    val oosEvidence: String,
    val limitations: String,
    val provenanceLineage: String,
    val approvalDecision: String = "PENDING_STAGE_9_PARSA_FINAL_APPROVAL",
    val isLocked: Boolean = false, // Strictly invariant: false in Stage 8
    val updatedAt: Long = System.currentTimeMillis()
)

// ==========================================
// DETECTIVE LAW (قانون کارآگاه) ENTITIES
// ==========================================

@Entity(
    tableName = "detective_clues",
    indices = [
        Index(value = ["clueId"], unique = true),
        Index(value = ["anomalyType"]),
        Index(value = ["tier"])
    ]
)
data class DetectiveClueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clueId: String, // e.g. "CLUE-2026-BTC-VOL-SPIKE-01"
    val title: String,
    val anomalyType: String, // "CROSS_ASSET_DISLOCATION", "VOLATILITY_VOLUME_SPIKE", "REGIME_TRANSITION_LEAD", "TEMPORAL_SEQUENCE_ANOMALY", "NON_LINEAR_CLUSTER"
    val assetsObservedJson: String, // e.g. "[\"BTC\", \"ETH\", \"SOL\"]"
    val timeframesObservedJson: String, // e.g. "[\"4H\", \"1D\"]"
    val metricsObservedJson: String, // e.g. "{\"skew\": 2.4, \"z_score\": 3.1, \"volume_ratio\": 4.2}"
    val rawObservation: String,
    val discoverySource: String = "AUTONOMOUS_DETECTIVE_OBSERVATION",
    val tier: String = "TIER_A_DISCOVERY",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "detective_hypotheses",
    indices = [
        Index(value = ["hypothesisId"], unique = true),
        Index(value = ["clueId"]),
        Index(value = ["status"])
    ]
)
data class DetectiveHypothesisEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hypothesisId: String, // e.g. "HYP-2026-BTC-LEAD-ETH-01"
    val clueId: String,
    val statement: String, // Testable explanation
    val corePremise: String,
    val testablePredictionsJson: String,
    val competingHypothesesJson: String,
    val authorOrOrigin: String = "PARSA_AUTONOMOUS_DETECTIVE",
    val tier: String = "TIER_B_EXPLORATORY",
    val status: String = "UNDER_EMPIRICAL_TESTING", // "UNDER_EMPIRICAL_TESTING", "SUPPORTED", "REFUTED", "INSUFFICIENT_EVIDENCE", "NEEDS_MORE_TESTING"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "competing_hypotheses",
    indices = [
        Index(value = ["competingId"], unique = true),
        Index(value = ["hypothesisId"]),
        Index(value = ["explanationType"])
    ]
)
data class CompetingHypothesisEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val competingId: String, // e.g. "RIVAL-HYP-01-MOM"
    val hypothesisId: String,
    val explanationType: String, // "MOMENTUM", "VOLATILITY", "VOLUME", "REGIME", "BTC_LEAD_EFFECT", "CROSS_ASSET_SPILLOVER", "HISTORICAL_EVENT", "RANDOM_NOISE_OR_LUCK", "DATA_MINING_OVERFIT"
    val title: String,
    val rationale: String,
    val isFavored: Boolean = false,
    val empiricalTestResultJson: String,
    val refutationOrConfirmationReason: String,
    val pValueOrMetricScore: Double,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "detective_methods",
    indices = [
        Index(value = ["methodId"], unique = true),
        Index(value = ["evidenceGrade"]),
        Index(value = ["discoveryOrigin"])
    ]
)
data class DetectiveMethodEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val methodId: String, // e.g. "DM-2026-CROSS-REGIME-MOMENTUM"
    val name: String,
    val hypothesis: String,
    val discoveryOrigin: String, // "AUTONOMOUS_DETECTIVE_SEARCH", "COMBINATORIAL_EXPLORATION", "CROSS_ASSET_DISCOVERY", "NON_LINEAR_FEATURE_EXTRACTION"
    val dataUsed: String,
    val featuresUsed: String,
    val methodLogic: String,
    val activationConditions: String,
    val invalidationConditions: String,
    val winningSamplesCount: Int,
    val failingSamplesCount: Int,
    val inSampleResult: Double,
    val validationResult: Double,
    val outOfSampleResult: Double,
    val walkForwardResult: Double,
    val baselineComparison: Double, // Advantage over baseline (e.g. +0.14 = 14% over baseline)
    val crossAssetResult: Double,
    val crossRegimeResult: Double,
    val parameterSensitivity: Double, // Low score = robust, high score = fragile
    val maxFavorableExcursion: Double, // MFE
    val maxAdverseExcursion: Double, // MAE
    val drawdown: Double,
    val recoveryFactor: Double,
    val failureClassification: String,
    val evidenceGrade: String, // "TIER_A_DISCOVERY", "TIER_B_EXPLORATORY", "TIER_C_REPEATED", "TIER_D_ROBUST", "TIER_E_CANDIDATE_RULE", "TIER_F_APPROVED_RULE", "REJECTED"
    val confidence: Double,
    val provenanceLineage: String,
    val isApprovedByFinalJudge: Boolean = false, // Strictly false unless PARSA Final Judge approves
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "detective_investigation_runs",
    indices = [
        Index(value = ["runId"], unique = true),
        Index(value = ["timestamp"])
    ]
)
data class DetectiveInvestigationRunEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val runId: String, // e.g. "INV-RUN-2026-001"
    val missionStatement: String,
    val cluesFoundCount: Int,
    val hypothesesTestedCount: Int,
    val competingHypothesesEvaluatedCount: Int,
    val methodsInventedCount: Int,
    val negativeLessonsLearnedCount: Int,
    val candidateRulesProposedCount: Int,
    val statisticalGuardrailsPassed: Boolean,
    val multipleTestingPenaltyApplied: Double, // Bonferroni / FWER penalty
    val lookaheadBiasAuditStatus: String = "AUDITED_ZERO_LOOKAHEAD",
    val outOfSamplePurityStatus: String = "STRICT_OOS_PURITY_CONFIRMED",
    val lineageHash: String,
    val executiveSummary: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "detective_audit_trail",
    indices = [
        Index(value = ["auditId"], unique = true),
        Index(value = ["targetEntityId"])
    ]
)
data class DetectiveAuditTrailEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val auditId: String,
    val step: String, // "CLUE", "HYPOTHESIS", "TEST", "EVIDENCE", "JUDGMENT", "CANDIDATE_RULE", "APPROVAL"
    val targetEntityId: String,
    val actionTaken: String,
    val guardrailVerification: String,
    val lineageBefore: String,
    val lineageAfter: String,
    val immutableHash: String,
    val timestamp: Long = System.currentTimeMillis()
)





