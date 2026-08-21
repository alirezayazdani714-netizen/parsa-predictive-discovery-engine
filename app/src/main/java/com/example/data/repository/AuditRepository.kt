package com.example.data.repository

import com.example.data.AppDatabase
import com.example.data.entity.AuditLogEntity
import com.example.data.entity.ExperimentEntity
import com.example.data.entity.MemoryVersionEntity
import com.example.data.entity.ModelVersionEntity
import com.example.data.entity.SystemStateEntity
import com.example.data.entity.TestResultEntity
import com.example.data.entity.TestRunEntity
import com.example.data.entity.UserEntity
import kotlinx.coroutines.flow.Flow

class AuditRepository(val db: AppDatabase) {
    val database: AppDatabase get() = db

    val allAuditLogs: Flow<List<AuditLogEntity>> = db.auditLogDao().getAllLogs()
    val allTestRuns: Flow<List<TestRunEntity>> = db.testRunDao().getAllTestRuns()
    val allSystemStates: Flow<List<SystemStateEntity>> = db.systemStateDao().getAllState()
    val allExperiments: Flow<List<ExperimentEntity>> = db.experimentDao().getAllExperiments()
    val allMemoryVersions: Flow<List<MemoryVersionEntity>> = db.memoryVersionDao().getAllMemoryVersions()

    suspend fun logAudit(level: String, category: String, message: String, detailsJson: String? = null): Long {
        return db.auditLogDao().insertLog(
            AuditLogEntity(
                level = level,
                category = category,
                message = message,
                detailsJson = detailsJson,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun getRecentLogs(limit: Int = 100): List<AuditLogEntity> {
        return db.auditLogDao().getRecentLogs(limit)
    }

    suspend fun recordTestRun(
        suiteName: String,
        status: String,
        passedCount: Int,
        failedCount: Int,
        totalCount: Int,
        durationMs: Long,
        results: List<TestResultEntity>
    ): Long {
        val runId = db.testRunDao().insertTestRun(
            TestRunEntity(
                suiteName = suiteName,
                status = status,
                passedCount = passedCount,
                failedCount = failedCount,
                totalCount = totalCount,
                durationMs = durationMs,
                startedAt = System.currentTimeMillis()
            )
        )
        val linkedResults = results.map { it.copy(runId = runId) }
        db.testResultDao().insertResults(linkedResults)
        return runId
    }

    suspend fun getLatestTestRun(): TestRunEntity? {
        return db.testRunDao().getLatestTestRun()
    }

    suspend fun getTestRunById(id: Long): Pair<TestRunEntity?, List<TestResultEntity>> {
        val run = db.testRunDao().getTestRunById(id)
        val results = if (run != null) db.testResultDao().getResultsForRun(id) else emptyList()
        return Pair(run, results)
    }

    suspend fun updateSystemState(key: String, value: String, stage: String) {
        db.systemStateDao().insertOrUpdateState(
            SystemStateEntity(
                stateKey = key,
                value = value,
                stage = stage,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun getSystemState(key: String): SystemStateEntity? {
        return db.systemStateDao().getStateByKey(key)
    }

    suspend fun getExperimentsList(): List<ExperimentEntity> {
        return db.experimentDao().getExperimentsList()
    }

    suspend fun getMemoryVersionsList(): List<MemoryVersionEntity> {
        return db.memoryVersionDao().getMemoryVersionsList()
    }

    suspend fun getModelVersionsList(): List<ModelVersionEntity> {
        return db.modelVersionDao().getModelVersionsList()
    }

    suspend fun initializeSystemStateIfNeeded() {
        if (db.userDao().getUserCount() == 0) {
            db.userDao().insertUser(
                UserEntity(
                    username = "ai_contractor_auditor",
                    role = "AUDITOR",
                    isActive = true
                )
            )
            db.userDao().insertUser(
                UserEntity(
                    username = "parsa_system_admin",
                    role = "SYSTEM",
                    isActive = true
                )
            )

            updateSystemState("CURRENT_STAGE", "STAGE_7_INDEPENDENT_JUDGMENT_AND_GOVERNANCE_PREP", "STAGE_7_INDEPENDENT_JUDGMENT_AND_GOVERNANCE_PREP")
            updateSystemState("CURRENT_PROJECT_STAGE", "STAGE_7_INDEPENDENT_JUDGMENT_AND_GOVERNANCE_PREP", "STAGE_7_INDEPENDENT_JUDGMENT_AND_GOVERNANCE_PREP")
            updateSystemState("BUILD_STATUS", "PASSED", "STAGE_7_INDEPENDENT_JUDGMENT_AND_GOVERNANCE_PREP")
            updateSystemState("GITHUB_INTEGRATION", "REQUIRES_USER_ACTION", "STAGE_7_INDEPENDENT_JUDGMENT_AND_GOVERNANCE_PREP")
            updateSystemState("ENVIRONMENT", "ANDROID_COMPOSE_WEB_STREAMING", "STAGE_7_INDEPENDENT_JUDGMENT_AND_GOVERNANCE_PREP")
            updateSystemState("TRADE_EXECUTION_ENGINE", "DISABLED", "STAGE_7_INDEPENDENT_JUDGMENT_AND_GOVERNANCE_PREP")
            updateSystemState("REAL_TIME_PREDICTIONS", "DISABLED", "STAGE_7_INDEPENDENT_JUDGMENT_AND_GOVERNANCE_PREP")
            updateSystemState("GEMINI_INDEPENDENT_JUDGMENT_ENGINE", "ACTIVE_REVIEWER_ONLY", "STAGE_7_INDEPENDENT_JUDGMENT_AND_GOVERNANCE_PREP")
            updateSystemState("RULE_ENACTMENT_ENGINE", "DISABLED_PENDING_HUMAN_GOVERNANCE", "STAGE_7_INDEPENDENT_JUDGMENT_AND_GOVERNANCE_PREP")

            db.memoryVersionDao().insertMemoryVersion(
                MemoryVersionEntity(
                    memoryKey = "SYSTEM_CORE_MEMORY",
                    version = 1,
                    schemaVersion = "1.0.0",
                    recordCount = 2
                )
            )

            db.modelVersionDao().insertModelVersion(
                ModelVersionEntity(
                    modelName = "BASE_PREDICTION_ARCHITECTURE",
                    versionTag = "v0.0.0-UNINITIALIZED",
                    architecture = "DEEP_TEMPORAL_GRAPH",
                    status = "NOT_IMPLEMENTED"
                )
            )

            // Initialize Deterministic Market Education Foundation (Stage 2 foundation)
            val existingConcepts = db.marketConceptDao().getConceptsList()
            if (existingConcepts.isEmpty()) {
                db.marketConceptDao().insertConcepts(
                    listOf(
                        com.example.data.entity.MarketConceptEntity(
                            conceptCode = "ORDER_BOOK_DYNAMICS",
                            title = "Order Book Dynamics & Depth",
                            category = "ORDER_BOOK",
                            description = "Understanding bid/ask queues, liquidity aggregation, and market depth without synthetic assumptions.",
                            difficultyLevel = 1,
                            deterministicRulesJson = """{"type":"ORDER_BOOK","rules":["Bids < Asks","Spread = Min(Ask) - Max(Bid)"]}""",
                            isVerified = true
                        ),
                        com.example.data.entity.MarketConceptEntity(
                            conceptCode = "SLIPPAGE_AND_SPREAD",
                            title = "Slippage & Spread Impact",
                            category = "LIQUIDITY",
                            description = "Mathematical calculation of execution slippage under varying book depth constraints.",
                            difficultyLevel = 1,
                            deterministicRulesJson = """{"type":"SLIPPAGE","formula":"abs(ExecPrice - ExpectedPrice) / ExpectedPrice"}""",
                            isVerified = true
                        ),
                        com.example.data.entity.MarketConceptEntity(
                            conceptCode = "POSITION_RISK_LIMIT",
                            title = "Fixed Capital & Position Risk Capping",
                            category = "RISK_CONTROL",
                            description = "Deterministic mathematical limits strictly capping per-trade exposure to protect capital.",
                            difficultyLevel = 2,
                            deterministicRulesJson = """{"type":"RISK_CAP","maxPositionRiskPct":0.02,"maxDrawdownPct":0.05}""",
                            isVerified = true
                        )
                    )
                )
            }

            val existingRules = db.riskRuleDao().getRiskRulesList()
            if (existingRules.isEmpty()) {
                db.riskRuleDao().insertRiskRules(
                    listOf(
                        com.example.data.entity.RiskRuleEntity(
                            ruleCode = "MAX_PORTFOLIO_RISK",
                            name = "Maximum Single Trade Risk",
                            category = "POSITION_SIZING",
                            formulaOrLogic = "RiskAmount <= TotalEquity * 0.02",
                            maxAllowedRiskPct = 0.02,
                            isMandatory = true
                        ),
                        com.example.data.entity.RiskRuleEntity(
                            ruleCode = "MAX_DRAWDOWN_CIRCUIT_BREAKER",
                            name = "Systemic Drawdown Circuit Breaker",
                            category = "DRAWDOWN_LIMIT",
                            formulaOrLogic = "If CurrentDrawdown >= 0.05 Then FreezeAllOrders",
                            maxAllowedRiskPct = 0.05,
                            isMandatory = true
                        )
                    )
                )
            }

            logAudit(
                level = "INFO",
                category = "SYSTEM",
                message = "PARSA System Environment and Audit Database initialized successfully with Market Education Foundation."
            )

            // Initialize Market Universe Manager with Benchmark Genesis Points
            val universeManager = com.example.data.universe.MarketUniverseManager(db)
            universeManager.initializeUniverseIfEmpty()

            // Initialize Historical Event Engine with Verified Real Market Events
            val eventEngine = com.example.data.events.HistoricalEventEngine(db)
            eventEngine.initializeEventsIfEmpty()
        }
    }

    suspend fun getMarketConcepts(): List<com.example.data.entity.MarketConceptEntity> =
        db.marketConceptDao().getConceptsList()

    suspend fun getRiskRules(): List<com.example.data.entity.RiskRuleEntity> =
        db.riskRuleDao().getRiskRulesList()

    suspend fun getUniverseAssetsPaged(limit: Int, offset: Int): List<com.example.data.entity.MarketAssetEntity> =
        db.marketAssetDao().getAssetsPaged(limit, offset)

    suspend fun getMarketAssets(): List<com.example.data.entity.MarketAssetEntity> =
        db.marketAssetDao().getAllAssetsList()

    suspend fun getUniverseCount(): Int =
        db.marketAssetDao().getAssetsCount()

    suspend fun getAssetBySymbol(symbol: String): com.example.data.entity.MarketAssetEntity? =
        db.marketAssetDao().getAssetBySymbol(symbol)

    suspend fun insertCandles(candles: List<com.example.data.entity.HistoricalCandleEntity>) =
        db.historicalCandleDao().insertCandles(candles)

    suspend fun getCandlesChronological(symbol: String, timeframe: String): List<com.example.data.entity.HistoricalCandleEntity> =
        db.historicalCandleDao().getCandlesChronological(symbol, timeframe)

    suspend fun getRecentExperiences(limit: Int = 100): List<com.example.data.entity.ExperienceMemoryEntity> =
        db.experienceMemoryDao().getExperiencesList(limit)

    suspend fun getCrossAssetInsights(): List<com.example.data.entity.CrossAssetInsightEntity> =
        db.crossAssetInsightDao().getInsightsList()

    suspend fun getIntegrityAnomalies(): List<com.example.data.entity.DataIntegrityAnomalyEntity> =
        db.dataIntegrityAnomalyDao().getAnomaliesList()

    suspend fun getHistoricalEvents(): List<com.example.data.entity.HistoricalEventEntity> =
        db.historicalEventDao().getEventsList()

    suspend fun getEventImpacts(): List<com.example.data.entity.EventImpactEntity> =
        db.eventImpactDao().getImpactsList()

    suspend fun getIndicatorSnapshots(symbol: String, timeframe: String): List<com.example.data.entity.HistoricalIndicatorSnapshotEntity> =
        db.historicalIndicatorDao().getSnapshots(symbol, timeframe)

    suspend fun getBatchCheckpoints(): List<com.example.data.entity.BatchProcessingCheckpointEntity> =
        db.batchProcessingCheckpointDao().getAllCheckpoints()

    suspend fun getLatestBatchCheckpoint(pipelineName: String = "HISTORICAL_RESEARCH_PIPELINE"): com.example.data.entity.BatchProcessingCheckpointEntity? =
        db.batchProcessingCheckpointDao().getLatestCheckpoint(pipelineName)

    suspend fun getHistoricalSetups(limit: Int = 100): List<com.example.data.entity.HistoricalSetupEntity> =
        db.historicalSetupDao().getSetupsList(limit)

    suspend fun getHistoricalSetupsByEvent(eventId: String): List<com.example.data.entity.HistoricalSetupEntity> =
        db.historicalSetupDao().getSetupsByEvent(eventId)

    suspend fun getDiscoveredPatterns(): List<com.example.data.entity.DiscoveredPatternEntity> {
        val patterns = db.discoveredPatternDao().getPatternsList()
        return if (patterns.isNotEmpty()) patterns else {
            val engine = com.example.data.patterns.PatternDiscoveryEngine(db)
            val discovered = engine.discoverHistoricalPatterns()
            db.discoveredPatternDao().insertPatterns(discovered)
            discovered
        }
    }

    suspend fun getDiscoveredPatternsByGrade(grade: String): List<com.example.data.entity.DiscoveredPatternEntity> =
        db.discoveredPatternDao().getPatternsByGrade(grade)

    suspend fun getDiscoveredPatternById(patternId: String): com.example.data.entity.DiscoveredPatternEntity? =
        db.discoveredPatternDao().getPatternById(patternId)

    suspend fun getAnalyticalMethods(): List<com.example.data.entity.AnalyticalMethodEntity> {
        val methods = db.analyticalMethodDao().getMethodsList()
        return if (methods.isNotEmpty()) methods else {
            val engine = com.example.data.methods.AnalyticalMethodDiscoveryEngine(db)
            engine.discoverAndEvaluateMethods()
        }
    }

    suspend fun getAnalyticalMethodVersions(methodId: String): List<com.example.data.entity.AnalyticalMethodEntity> =
        db.analyticalMethodDao().getMethodVersions(methodId)

    suspend fun getAnalyticalMethodByIdAndVersion(methodId: String, version: Int): com.example.data.entity.AnalyticalMethodEntity? =
        db.analyticalMethodDao().getMethodByIdAndVersion(methodId, version)

    suspend fun getAnalyticalMethodsByGrade(grade: String): List<com.example.data.entity.AnalyticalMethodEntity> =
        db.analyticalMethodDao().getMethodsByGrade(grade)

    suspend fun getAnalyticalMethodsByStatus(status: String): List<com.example.data.entity.AnalyticalMethodEntity> =
        db.analyticalMethodDao().getMethodsByStatus(status)

    suspend fun getFailedAnalyticalMethods(): List<com.example.data.entity.AnalyticalMethodEntity> =
        db.analyticalMethodDao().getFailedMethods()

    suspend fun getMethodEvaluations(methodId: String? = null): List<com.example.data.entity.MethodEvaluationEntity> =
        if (methodId != null) db.methodEvaluationDao().getEvaluationsForMethod(methodId)
        else db.methodEvaluationDao().getRecentEvaluations(100)

    suspend fun getMethodJudgments(): List<com.example.data.entity.MethodJudgmentEntity> {
        val judgments = db.methodJudgmentDao().getJudgmentsList()
        return if (judgments.isNotEmpty()) {
            judgments
        } else {
            val engine = com.example.data.judgment.IndependentJudgmentEngine(db)
            engine.auditAndJudgeAllMethods()
        }
    }

    suspend fun getJudgmentsForMethod(methodId: String): List<com.example.data.entity.MethodJudgmentEntity> =
        db.methodJudgmentDao().getJudgmentsForMethod(methodId)

    suspend fun getJudgmentsByGrade(grade: String): List<com.example.data.entity.MethodJudgmentEntity> =
        db.methodJudgmentDao().getJudgmentsByGrade(grade)

    suspend fun getLessonsLearned(): List<com.example.data.entity.LessonLearnedEntity> {
        val lessons = db.lessonLearnedDao().getLessonsList()
        return if (lessons.isNotEmpty()) {
            lessons
        } else {
            val engine = com.example.data.judgment.IndependentJudgmentEngine(db)
            val coreLessons = engine.getCoreLessonsLearned()
            db.lessonLearnedDao().insertLessons(coreLessons)
            coreLessons
        }
    }

    suspend fun getLessonsByCategory(category: String): List<com.example.data.entity.LessonLearnedEntity> =
        db.lessonLearnedDao().getLessonsByCategory(category)

    suspend fun getLessonsByEvidenceType(evidenceType: String): List<com.example.data.entity.LessonLearnedEntity> =
        db.lessonLearnedDao().getLessonsByEvidenceType(evidenceType)

    // Stage 8 Methods
    suspend fun getMethodArbitrationReports(): List<com.example.data.entity.MethodArbitrationReportEntity> {
        val reports = db.methodArbitrationReportDao().getArbitrationReportsList()
        return if (reports.isNotEmpty()) {
            reports
        } else {
            val engine = com.example.data.arbitration.Stage8ArbitrationEngine(db)
            val result = engine.executeStage8Arbitration()
            result.arbitrationReports
        }
    }

    suspend fun getGeminiArbitrationReports(): List<com.example.data.entity.GeminiArbitrationReportEntity> {
        val reports = db.geminiArbitrationReportDao().getGeminiReportsList()
        return if (reports.isNotEmpty()) {
            reports
        } else {
            val engine = com.example.data.arbitration.Stage8ArbitrationEngine(db)
            val result = engine.executeStage8Arbitration()
            result.geminiReports
        }
    }

    suspend fun getFinalJudgeDecisions(): List<com.example.data.entity.FinalJudgeDecisionEntity> {
        val decisions = db.finalJudgeDecisionDao().getDecisionsList()
        return if (decisions.isNotEmpty()) {
            decisions
        } else {
            val engine = com.example.data.arbitration.Stage8ArbitrationEngine(db)
            val result = engine.executeStage8Arbitration()
            result.finalDecisions
        }
    }

    suspend fun getCandidateRules(): List<com.example.data.entity.CandidateRuleEntity> {
        val rules = db.candidateRuleDao().getCandidateRulesList()
        return if (rules.isNotEmpty()) {
            rules
        } else {
            val engine = com.example.data.arbitration.Stage8ArbitrationEngine(db)
            val result = engine.executeStage8Arbitration()
            result.candidateRules
        }
    }

    suspend fun getCandidateRuleById(ruleId: String): com.example.data.entity.CandidateRuleEntity? =
        db.candidateRuleDao().getCandidateRuleById(ruleId)

    suspend fun getArbitrationReportByMethodId(methodId: String): com.example.data.entity.MethodArbitrationReportEntity? =
        db.methodArbitrationReportDao().getArbitrationReportByMethodId(methodId)

    suspend fun executeStage8Arbitration(): com.example.data.arbitration.Stage8ArbitrationResult {
        val engine = com.example.data.arbitration.Stage8ArbitrationEngine(db)
        return engine.executeStage8Arbitration()
    }

    suspend fun getEmergingPatterns(): List<com.example.data.entity.EmergingPatternEntity> {
        val patterns = db.emergingPatternDao().getEmergingPatternsList()
        return if (patterns.isNotEmpty()) {
            patterns
        } else {
            val engine = com.example.data.arbitration.Stage8ArbitrationEngine(db)
            val result = engine.executeStage8Arbitration()
            result.emergingPatterns
        }
    }

    suspend fun getCrossAssetClusters(): List<com.example.data.entity.CrossAssetClusterEntity> {
        val clusters = db.crossAssetClusterDao().getClustersList()
        return if (clusters.isNotEmpty()) {
            clusters
        } else {
            val engine = com.example.data.arbitration.Stage8ArbitrationEngine(db)
            val result = engine.executeStage8Arbitration()
            result.crossAssetClusters
        }
    }

    suspend fun getLeadLagRelationships(): List<com.example.data.entity.LeadLagRelationshipEntity> {
        val rels = db.leadLagRelationshipDao().getRelationshipsList()
        return if (rels.isNotEmpty()) {
            rels
        } else {
            val engine = com.example.data.arbitration.Stage8ArbitrationEngine(db)
            val result = engine.executeStage8Arbitration()
            result.leadLagRelationships
        }
    }

    suspend fun getNegativeKnowledge(): List<com.example.data.entity.NegativeKnowledgeEntity> {
        val nk = db.negativeKnowledgeDao().getNegativeKnowledgeList()
        return if (nk.isNotEmpty()) {
            nk
        } else {
            val engine = com.example.data.arbitration.Stage8ArbitrationEngine(db)
            val result = engine.executeStage8Arbitration()
            result.negativeKnowledge
        }
    }

    suspend fun getRuleBookEntries(): List<com.example.data.entity.ParsaRuleBookEntity> {
        val book = db.parsaRuleBookDao().getRuleBookList()
        return if (book.isNotEmpty()) {
            book
        } else {
            val engine = com.example.data.arbitration.Stage8ArbitrationEngine(db)
            val result = engine.executeStage8Arbitration()
            result.ruleBookEntries
        }
    }

    fun getStage8GovernanceAudit(): Map<String, Any> {
        val engine = com.example.data.arbitration.Stage8ArbitrationEngine(db)
        return engine.getStage8GovernanceAudit()
    }

    // ==========================================
    // DETECTIVE LAW (قانون کارآگاه) INTEGRATION
    // ==========================================

    suspend fun executeDetectiveInvestigation(): com.example.data.detective.DetectiveLawEngine.DetectiveInvestigationResult {
        val engine = com.example.data.detective.DetectiveLawEngine(db)
        return engine.executeInvestigation()
    }

    suspend fun getDetectiveClues(): List<com.example.data.entity.DetectiveClueEntity> {
        val clues = db.detectiveClueDao().getCluesList()
        return if (clues.isNotEmpty()) {
            clues
        } else {
            val engine = com.example.data.detective.DetectiveLawEngine(db)
            val result = engine.executeInvestigation()
            result.clues
        }
    }

    suspend fun getDetectiveHypotheses(): List<com.example.data.entity.DetectiveHypothesisEntity> {
        val hypotheses = db.detectiveHypothesisDao().getHypothesesList()
        return if (hypotheses.isNotEmpty()) {
            hypotheses
        } else {
            val engine = com.example.data.detective.DetectiveLawEngine(db)
            val result = engine.executeInvestigation()
            result.hypotheses
        }
    }

    suspend fun getCompetingHypotheses(): List<com.example.data.entity.CompetingHypothesisEntity> {
        val competing = db.competingHypothesisDao().getCompetingHypothesesList()
        return if (competing.isNotEmpty()) {
            competing
        } else {
            val engine = com.example.data.detective.DetectiveLawEngine(db)
            val result = engine.executeInvestigation()
            result.competingHypotheses
        }
    }

    suspend fun getDetectiveMethods(): List<com.example.data.entity.DetectiveMethodEntity> {
        val methods = db.detectiveMethodDao().getDetectiveMethodsList()
        return if (methods.isNotEmpty()) {
            methods
        } else {
            val engine = com.example.data.detective.DetectiveLawEngine(db)
            val result = engine.executeInvestigation()
            result.inventedMethods
        }
    }

    suspend fun getDetectiveRuns(): List<com.example.data.entity.DetectiveInvestigationRunEntity> {
        val runs = db.detectiveInvestigationRunDao().getRunsList()
        return if (runs.isNotEmpty()) {
            runs
        } else {
            val engine = com.example.data.detective.DetectiveLawEngine(db)
            val result = engine.executeInvestigation()
            listOf(result.run)
        }
    }

    suspend fun getDetectiveAuditTrail(): List<com.example.data.entity.DetectiveAuditTrailEntity> {
        val trail = db.detectiveAuditTrailDao().getAuditTrailList()
        return if (trail.isNotEmpty()) {
            trail
        } else {
            val engine = com.example.data.detective.DetectiveLawEngine(db)
            val result = engine.executeInvestigation()
            result.auditTrail
        }
    }

    fun getDetectiveLawPrinciplesSummary(): Map<String, Any> {
        return mapOf(
            "mission" to "آیا می‌توان از شواهد تاریخی موجود، رفتار آینده بازار را با دقتی بهتر از Baseline پیش‌بینی کرد؟ و اگر بله، چه رابطه، الگو، ساختار یا روش تحلیلی این توانایی را ایجاد می‌کند؟",
            "principleOfDiscoveryFreedom" to "Autonomous exploration not limited to pre-defined human rules/prompts.",
            "discoveryChain" to "CLUE -> HYPOTHESIS -> RIVAL TEST -> EVIDENCE -> JUDGMENT -> CANDIDATE RULE -> APPROVAL",
            "competingHypothesesPolicy" to "Mandatory evaluation of rival explanations (Momentum, Volatility, Volume, Regime, BTC Leader Effect, Cross-Asset Spillover, Random Noise, Data Mining Overfit).",
            "selfDeceptionPrevention" to "Zero lookahead bias, strict Out-of-Sample purity, Bonferroni multiple testing penalty applied.",
            "failurePolicy" to "Negative Knowledge extraction and classification across 11 root causes.",
            "ruleLockingPolicy" to "Zero rules prematurely locked. Only PARSA Final Judge can approve Candidate Rules.",
            "geminiRole" to "Independent Advisory Only (No rule modification/deletion/approval power)."
        )
    }
}




