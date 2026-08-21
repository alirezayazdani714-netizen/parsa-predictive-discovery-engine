package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY id ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}

@Dao
interface SystemStateDao {
    @Query("SELECT * FROM system_state")
    fun getAllState(): Flow<List<SystemStateEntity>>

    @Query("SELECT * FROM system_state WHERE stateKey = :key LIMIT 1")
    suspend fun getStateByKey(key: String): SystemStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateState(state: SystemStateEntity)
}

@Dao
interface ExperimentDao {
    @Query("SELECT * FROM experiments ORDER BY createdAt DESC")
    fun getAllExperiments(): Flow<List<ExperimentEntity>>

    @Query("SELECT * FROM experiments ORDER BY createdAt DESC")
    suspend fun getExperimentsList(): List<ExperimentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExperiment(experiment: ExperimentEntity): Long

    @Update
    suspend fun updateExperiment(experiment: ExperimentEntity)
}

@Dao
interface TestRunDao {
    @Query("SELECT * FROM test_runs ORDER BY startedAt DESC")
    fun getAllTestRuns(): Flow<List<TestRunEntity>>

    @Query("SELECT * FROM test_runs WHERE id = :id LIMIT 1")
    suspend fun getTestRunById(id: Long): TestRunEntity?

    @Query("SELECT * FROM test_runs ORDER BY startedAt DESC LIMIT 1")
    suspend fun getLatestTestRun(): TestRunEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestRun(run: TestRunEntity): Long

    @Update
    suspend fun updateTestRun(run: TestRunEntity)
}

@Dao
interface TestResultDao {
    @Query("SELECT * FROM test_results WHERE runId = :runId ORDER BY id ASC")
    suspend fun getResultsForRun(runId: Long): List<TestResultEntity>

    @Query("SELECT * FROM test_results ORDER BY timestamp DESC LIMIT 100")
    fun getRecentTestResults(): Flow<List<TestResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResults(results: List<TestResultEntity>)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentLogs(limit: Int = 100): List<AuditLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLogEntity): Long
}

@Dao
interface ModelVersionDao {
    @Query("SELECT * FROM model_versions ORDER BY createdAt DESC")
    fun getAllModelVersions(): Flow<List<ModelVersionEntity>>

    @Query("SELECT * FROM model_versions ORDER BY createdAt DESC")
    suspend fun getModelVersionsList(): List<ModelVersionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModelVersion(model: ModelVersionEntity): Long
}

@Dao
interface MemoryVersionDao {
    @Query("SELECT * FROM memory_versions ORDER BY updatedAt DESC")
    fun getAllMemoryVersions(): Flow<List<MemoryVersionEntity>>

    @Query("SELECT * FROM memory_versions ORDER BY updatedAt DESC")
    suspend fun getMemoryVersionsList(): List<MemoryVersionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemoryVersion(memory: MemoryVersionEntity): Long
}

@Dao
interface MarketConceptDao {
    @Query("SELECT * FROM market_concepts ORDER BY difficultyLevel ASC, id ASC")
    fun getAllConcepts(): Flow<List<MarketConceptEntity>>

    @Query("SELECT * FROM market_concepts ORDER BY difficultyLevel ASC, id ASC")
    suspend fun getConceptsList(): List<MarketConceptEntity>

    @Query("SELECT * FROM market_concepts WHERE conceptCode = :code LIMIT 1")
    suspend fun getConceptByCode(code: String): MarketConceptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConcept(concept: MarketConceptEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConcepts(concepts: List<MarketConceptEntity>)
}

@Dao
interface RiskRuleDao {
    @Query("SELECT * FROM risk_rules ORDER BY id ASC")
    fun getAllRiskRules(): Flow<List<RiskRuleEntity>>

    @Query("SELECT * FROM risk_rules ORDER BY id ASC")
    suspend fun getRiskRulesList(): List<RiskRuleEntity>

    @Query("SELECT * FROM risk_rules WHERE ruleCode = :code LIMIT 1")
    suspend fun getRuleByCode(code: String): RiskRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRiskRule(rule: RiskRuleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRiskRules(rules: List<RiskRuleEntity>)
}

@Dao
interface EducationProgressDao {
    @Query("SELECT * FROM education_progress WHERE userId = :userId")
    fun getUserProgress(userId: Long): Flow<List<EducationProgressEntity>>

    @Query("SELECT * FROM education_progress WHERE userId = :userId AND conceptCode = :conceptCode LIMIT 1")
    suspend fun getProgressForConcept(userId: Long, conceptCode: String): EducationProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: EducationProgressEntity): Long
}

@Dao
interface MarketAssetDao {
    @Query("SELECT * FROM market_assets ORDER BY marketCapRank ASC")
    fun getAllAssets(): Flow<List<MarketAssetEntity>>

    @Query("SELECT * FROM market_assets ORDER BY marketCapRank ASC LIMIT :limit OFFSET :offset")
    suspend fun getAssetsPaged(limit: Int, offset: Int): List<MarketAssetEntity>

    @Query("SELECT * FROM market_assets ORDER BY marketCapRank ASC")
    suspend fun getAllAssetsList(): List<MarketAssetEntity>

    @Query("SELECT COUNT(*) FROM market_assets")
    suspend fun getAssetsCount(): Int

    @Query("SELECT * FROM market_assets WHERE symbol = :symbol LIMIT 1")
    suspend fun getAssetBySymbol(symbol: String): MarketAssetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: MarketAssetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssets(assets: List<MarketAssetEntity>)
}

@Dao
interface HistoricalCandleDao {
    @Query("SELECT * FROM historical_candles WHERE symbol = :symbol AND timeframe = :timeframe ORDER BY openTime ASC")
    suspend fun getCandlesChronological(symbol: String, timeframe: String): List<HistoricalCandleEntity>

    @Query("SELECT * FROM historical_candles WHERE symbol = :symbol AND timeframe = :timeframe AND openTime <= :asOfTime ORDER BY openTime ASC")
    suspend fun getCandlesUpToTime(symbol: String, timeframe: String, asOfTime: Long): List<HistoricalCandleEntity>

    @Query("SELECT * FROM historical_candles WHERE symbol = :symbol AND timeframe = :timeframe AND openTime > :asOfTime ORDER BY openTime ASC LIMIT :limit")
    suspend fun getForwardEvaluationCandles(symbol: String, timeframe: String, asOfTime: Long, limit: Int): List<HistoricalCandleEntity>

    @Query("SELECT COUNT(*) FROM historical_candles WHERE symbol = :symbol")
    suspend fun getCandlesCountForSymbol(symbol: String): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCandles(candles: List<HistoricalCandleEntity>)
}

@Dao
interface ExperienceMemoryDao {
    @Query("SELECT * FROM experience_memories ORDER BY timestamp DESC")
    fun getAllExperiences(): Flow<List<ExperienceMemoryEntity>>

    @Query("SELECT * FROM experience_memories ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getExperiencesList(limit: Int = 100): List<ExperienceMemoryEntity>

    @Query("SELECT * FROM experience_memories WHERE assetSymbol = :symbol ORDER BY timestamp ASC")
    suspend fun getExperiencesForAsset(symbol: String): List<ExperienceMemoryEntity>

    @Query("SELECT COUNT(*) FROM experience_memories")
    suspend fun getExperiencesCount(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExperience(experience: ExperienceMemoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExperiences(experiences: List<ExperienceMemoryEntity>)
}

@Dao
interface CrossAssetInsightDao {
    @Query("SELECT * FROM cross_asset_insights ORDER BY createdAt DESC")
    fun getAllInsights(): Flow<List<CrossAssetInsightEntity>>

    @Query("SELECT * FROM cross_asset_insights ORDER BY createdAt DESC")
    suspend fun getInsightsList(): List<CrossAssetInsightEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInsight(insight: CrossAssetInsightEntity): Long
}

@Dao
interface DataIntegrityAnomalyDao {
    @Query("SELECT * FROM data_integrity_anomalies ORDER BY detectedAt DESC")
    fun getAllAnomalies(): Flow<List<DataIntegrityAnomalyEntity>>

    @Query("SELECT * FROM data_integrity_anomalies ORDER BY detectedAt DESC")
    suspend fun getAnomaliesList(): List<DataIntegrityAnomalyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnomaly(anomaly: DataIntegrityAnomalyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnomalies(anomalies: List<DataIntegrityAnomalyEntity>)
}

@Dao
interface HistoricalEventDao {
    @Query("SELECT * FROM historical_events ORDER BY eventTimestamp DESC")
    fun getAllEvents(): Flow<List<HistoricalEventEntity>>

    @Query("SELECT * FROM historical_events ORDER BY eventTimestamp DESC")
    suspend fun getEventsList(): List<HistoricalEventEntity>

    @Query("SELECT * FROM historical_events WHERE eventId = :eventId LIMIT 1")
    suspend fun getEventById(eventId: String): HistoricalEventEntity?

    @Query("SELECT * FROM historical_events WHERE eventTimestamp BETWEEN :startTime AND :endTime ORDER BY eventTimestamp ASC")
    suspend fun getEventsInRange(startTime: Long, endTime: Long): List<HistoricalEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: HistoricalEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<HistoricalEventEntity>)
}

@Dao
interface EventImpactDao {
    @Query("SELECT * FROM event_impacts ORDER BY calculatedAt DESC")
    fun getAllImpacts(): Flow<List<EventImpactEntity>>

    @Query("SELECT * FROM event_impacts ORDER BY calculatedAt DESC")
    suspend fun getImpactsList(): List<EventImpactEntity>

    @Query("SELECT * FROM event_impacts WHERE eventId = :eventId")
    suspend fun getImpactsByEvent(eventId: String): List<EventImpactEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImpact(impact: EventImpactEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImpacts(impacts: List<EventImpactEntity>)
}

@Dao
interface HistoricalIndicatorDao {
    @Query("SELECT * FROM indicator_snapshots WHERE symbol = :symbol AND timeframe = :timeframe ORDER BY timestamp ASC")
    suspend fun getSnapshots(symbol: String, timeframe: String): List<HistoricalIndicatorSnapshotEntity>

    @Query("SELECT * FROM indicator_snapshots WHERE symbol = :symbol AND timeframe = :timeframe AND timestamp <= :asOfTime ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestSnapshotAsOf(symbol: String, timeframe: String, asOfTime: Long): HistoricalIndicatorSnapshotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: HistoricalIndicatorSnapshotEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshots(snapshots: List<HistoricalIndicatorSnapshotEntity>)
}

@Dao
interface BatchProcessingCheckpointDao {
    @Query("SELECT * FROM batch_processing_checkpoints WHERE pipelineName = :pipelineName ORDER BY lastCheckpointTime DESC LIMIT 1")
    suspend fun getLatestCheckpoint(pipelineName: String): BatchProcessingCheckpointEntity?

    @Query("SELECT * FROM batch_processing_checkpoints ORDER BY lastCheckpointTime DESC")
    suspend fun getAllCheckpoints(): List<BatchProcessingCheckpointEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCheckpoint(checkpoint: BatchProcessingCheckpointEntity): Long
}

@Dao
interface HistoricalSetupDao {
    @Query("SELECT * FROM historical_setups ORDER BY timestamp DESC")
    fun getAllSetups(): Flow<List<HistoricalSetupEntity>>

    @Query("SELECT * FROM historical_setups ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getSetupsList(limit: Int = 100): List<HistoricalSetupEntity>

    @Query("SELECT * FROM historical_setups WHERE eventId = :eventId ORDER BY timestamp ASC")
    suspend fun getSetupsByEvent(eventId: String): List<HistoricalSetupEntity>

    @Query("SELECT * FROM historical_setups WHERE assetSymbol = :symbol ORDER BY timestamp ASC")
    suspend fun getSetupsByAsset(symbol: String): List<HistoricalSetupEntity>

    @Query("SELECT * FROM historical_setups WHERE setupId = :setupId LIMIT 1")
    suspend fun getSetupById(setupId: String): HistoricalSetupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetup(setup: HistoricalSetupEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetups(setups: List<HistoricalSetupEntity>)
}

@Dao
interface DiscoveredPatternDao {
    @Query("SELECT * FROM discovered_patterns ORDER BY discoveredAt DESC")
    fun getAllPatterns(): Flow<List<DiscoveredPatternEntity>>

    @Query("SELECT * FROM discovered_patterns ORDER BY confidence DESC, sampleSize DESC")
    suspend fun getPatternsList(): List<DiscoveredPatternEntity>

    @Query("SELECT * FROM discovered_patterns WHERE evidenceGrade = :grade ORDER BY confidence DESC")
    suspend fun getPatternsByGrade(grade: String): List<DiscoveredPatternEntity>

    @Query("SELECT * FROM discovered_patterns WHERE patternId = :patternId LIMIT 1")
    suspend fun getPatternById(patternId: String): DiscoveredPatternEntity?

    @Query("SELECT * FROM discovered_patterns WHERE timeframe = :timeframe ORDER BY confidence DESC")
    suspend fun getPatternsByTimeframe(timeframe: String): List<DiscoveredPatternEntity>

    @Query("SELECT COUNT(*) FROM discovered_patterns")
    suspend fun getPatternsCount(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPattern(pattern: DiscoveredPatternEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatterns(patterns: List<DiscoveredPatternEntity>)
}

@Dao
interface AnalyticalMethodDao {
    @Query("SELECT * FROM analytical_methods ORDER BY createdAt DESC")
    fun getAllMethods(): Flow<List<AnalyticalMethodEntity>>

    @Query("SELECT * FROM analytical_methods ORDER BY createdAt DESC")
    suspend fun getMethodsList(): List<AnalyticalMethodEntity>

    @Query("SELECT * FROM analytical_methods WHERE methodId = :methodId ORDER BY methodVersion DESC")
    suspend fun getMethodVersions(methodId: String): List<AnalyticalMethodEntity>

    @Query("SELECT * FROM analytical_methods WHERE methodId = :methodId AND methodVersion = :version LIMIT 1")
    suspend fun getMethodByIdAndVersion(methodId: String, version: Int): AnalyticalMethodEntity?

    @Query("SELECT * FROM analytical_methods WHERE methodId = :methodId ORDER BY methodVersion DESC LIMIT 1")
    suspend fun getLatestMethod(methodId: String): AnalyticalMethodEntity?

    @Query("SELECT * FROM analytical_methods WHERE evidenceGrade = :grade ORDER BY methodPositiveRate DESC")
    suspend fun getMethodsByGrade(grade: String): List<AnalyticalMethodEntity>

    @Query("SELECT * FROM analytical_methods WHERE status = :status ORDER BY createdAt DESC")
    suspend fun getMethodsByStatus(status: String): List<AnalyticalMethodEntity>

    @Query("SELECT * FROM analytical_methods WHERE failureClassification IS NOT NULL")
    suspend fun getFailedMethods(): List<AnalyticalMethodEntity>

    @Query("SELECT COUNT(*) FROM analytical_methods")
    suspend fun getMethodsCount(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMethod(method: AnalyticalMethodEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMethods(methods: List<AnalyticalMethodEntity>)

    @Update
    suspend fun updateMethod(method: AnalyticalMethodEntity)
}

@Dao
interface MethodEvaluationDao {
    @Query("SELECT * FROM method_evaluations ORDER BY timestamp DESC")
    fun getAllEvaluations(): Flow<List<MethodEvaluationEntity>>

    @Query("SELECT * FROM method_evaluations WHERE methodId = :methodId ORDER BY timestamp DESC")
    suspend fun getEvaluationsForMethod(methodId: String): List<MethodEvaluationEntity>

    @Query("SELECT * FROM method_evaluations WHERE evaluationType = :type ORDER BY timestamp DESC")
    suspend fun getEvaluationsByType(type: String): List<MethodEvaluationEntity>

    @Query("SELECT * FROM method_evaluations ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentEvaluations(limit: Int = 100): List<MethodEvaluationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvaluation(evaluation: MethodEvaluationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvaluations(evaluations: List<MethodEvaluationEntity>)
}

@Dao
interface MethodJudgmentDao {
    @Query("SELECT * FROM method_judgments ORDER BY timestamp DESC")
    fun getAllJudgments(): Flow<List<MethodJudgmentEntity>>

    @Query("SELECT * FROM method_judgments ORDER BY timestamp DESC")
    suspend fun getJudgmentsList(): List<MethodJudgmentEntity>

    @Query("SELECT * FROM method_judgments WHERE methodId = :methodId ORDER BY methodVersion DESC")
    suspend fun getJudgmentsForMethod(methodId: String): List<MethodJudgmentEntity>

    @Query("SELECT * FROM method_judgments WHERE methodId = :methodId AND methodVersion = :version LIMIT 1")
    suspend fun getJudgmentByMethodAndVersion(methodId: String, version: Int): MethodJudgmentEntity?

    @Query("SELECT * FROM method_judgments WHERE evidenceGrade = :grade ORDER BY timestamp DESC")
    suspend fun getJudgmentsByGrade(grade: String): List<MethodJudgmentEntity>

    @Query("SELECT COUNT(*) FROM method_judgments")
    suspend fun getJudgmentsCount(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJudgment(judgment: MethodJudgmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJudgments(judgments: List<MethodJudgmentEntity>)
}

@Dao
interface LessonLearnedDao {
    @Query("SELECT * FROM lessons_learned ORDER BY timestamp DESC")
    fun getAllLessons(): Flow<List<LessonLearnedEntity>>

    @Query("SELECT * FROM lessons_learned ORDER BY timestamp DESC")
    suspend fun getLessonsList(): List<LessonLearnedEntity>

    @Query("SELECT * FROM lessons_learned WHERE category = :category ORDER BY timestamp DESC")
    suspend fun getLessonsByCategory(category: String): List<LessonLearnedEntity>

    @Query("SELECT * FROM lessons_learned WHERE evidenceType = :evidenceType ORDER BY timestamp DESC")
    suspend fun getLessonsByEvidenceType(evidenceType: String): List<LessonLearnedEntity>

    @Query("SELECT COUNT(*) FROM lessons_learned")
    suspend fun getLessonsCount(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: LessonLearnedEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<LessonLearnedEntity>)
}

@Dao
interface MethodArbitrationReportDao {
    @Query("SELECT * FROM method_arbitrations ORDER BY timestamp DESC")
    fun getAllArbitrationReports(): Flow<List<MethodArbitrationReportEntity>>

    @Query("SELECT * FROM method_arbitrations ORDER BY timestamp DESC")
    suspend fun getArbitrationReportsList(): List<MethodArbitrationReportEntity>

    @Query("SELECT * FROM method_arbitrations WHERE methodId = :methodId LIMIT 1")
    suspend fun getArbitrationReportByMethodId(methodId: String): MethodArbitrationReportEntity?

    @Query("SELECT * FROM method_arbitrations WHERE arbitrationId = :arbitrationId LIMIT 1")
    suspend fun getArbitrationReportById(arbitrationId: String): MethodArbitrationReportEntity?

    @Query("SELECT * FROM method_arbitrations WHERE geminiTemporaryClassification = :classification ORDER BY timestamp DESC")
    suspend fun getArbitrationReportsByClassification(classification: String): List<MethodArbitrationReportEntity>

    @Query("SELECT COUNT(*) FROM method_arbitrations")
    suspend fun getArbitrationReportsCount(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArbitrationReport(report: MethodArbitrationReportEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArbitrationReports(reports: List<MethodArbitrationReportEntity>)
}

@Dao
interface CandidateRuleDao {
    @Query("SELECT * FROM candidate_rules ORDER BY timestamp DESC")
    fun getAllCandidateRules(): Flow<List<CandidateRuleEntity>>

    @Query("SELECT * FROM candidate_rules ORDER BY timestamp DESC")
    suspend fun getCandidateRulesList(): List<CandidateRuleEntity>

    @Query("SELECT * FROM candidate_rules WHERE ruleId = :ruleId LIMIT 1")
    suspend fun getCandidateRuleById(ruleId: String): CandidateRuleEntity?

    @Query("SELECT * FROM candidate_rules WHERE sourceMethodId = :methodId")
    suspend fun getCandidateRulesForMethod(methodId: String): List<CandidateRuleEntity>

    @Query("SELECT * FROM candidate_rules WHERE status = :status ORDER BY timestamp DESC")
    suspend fun getCandidateRulesByStatus(status: String): List<CandidateRuleEntity>

    @Query("SELECT * FROM candidate_rules WHERE isApproved = 1")
    suspend fun getApprovedCandidateRules(): List<CandidateRuleEntity>

    @Query("SELECT COUNT(*) FROM candidate_rules")
    suspend fun getCandidateRulesCount(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCandidateRule(rule: CandidateRuleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCandidateRules(rules: List<CandidateRuleEntity>)
}

@Dao
interface GeminiArbitrationReportDao {
    @Query("SELECT * FROM gemini_arbitration_reports ORDER BY createdAt DESC")
    fun getAllGeminiReports(): Flow<List<GeminiArbitrationReportEntity>>

    @Query("SELECT * FROM gemini_arbitration_reports ORDER BY createdAt DESC")
    suspend fun getGeminiReportsList(): List<GeminiArbitrationReportEntity>

    @Query("SELECT * FROM gemini_arbitration_reports WHERE methodId = :methodId LIMIT 1")
    suspend fun getGeminiReportByMethodId(methodId: String): GeminiArbitrationReportEntity?

    @Query("SELECT * FROM gemini_arbitration_reports WHERE reportId = :reportId LIMIT 1")
    suspend fun getGeminiReportById(reportId: String): GeminiArbitrationReportEntity?

    @Query("SELECT COUNT(*) FROM gemini_arbitration_reports")
    suspend fun getGeminiReportsCount(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeminiReport(report: GeminiArbitrationReportEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeminiReports(reports: List<GeminiArbitrationReportEntity>)
}

@Dao
interface FinalJudgeDecisionDao {
    @Query("SELECT * FROM final_judge_decisions ORDER BY timestamp DESC")
    fun getAllDecisions(): Flow<List<FinalJudgeDecisionEntity>>

    @Query("SELECT * FROM final_judge_decisions ORDER BY timestamp DESC")
    suspend fun getDecisionsList(): List<FinalJudgeDecisionEntity>

    @Query("SELECT * FROM final_judge_decisions WHERE methodId = :methodId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestDecisionForMethod(methodId: String): FinalJudgeDecisionEntity?

    @Query("SELECT * FROM final_judge_decisions WHERE decisionId = :decisionId LIMIT 1")
    suspend fun getDecisionById(decisionId: String): FinalJudgeDecisionEntity?

    @Query("SELECT * FROM final_judge_decisions WHERE decision = :decision ORDER BY timestamp DESC")
    suspend fun getDecisionsByStatus(decision: String): List<FinalJudgeDecisionEntity>

    @Query("SELECT COUNT(*) FROM final_judge_decisions")
    suspend fun getDecisionsCount(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDecision(decision: FinalJudgeDecisionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDecisions(decisions: List<FinalJudgeDecisionEntity>)
}

@Dao
interface EmergingPatternDao {
    @Query("SELECT * FROM emerging_patterns ORDER BY timestamp DESC")
    fun getAllEmergingPatterns(): Flow<List<EmergingPatternEntity>>

    @Query("SELECT * FROM emerging_patterns ORDER BY timestamp DESC")
    suspend fun getEmergingPatternsList(): List<EmergingPatternEntity>

    @Query("SELECT * FROM emerging_patterns WHERE patternId = :patternId LIMIT 1")
    suspend fun getEmergingPatternById(patternId: String): EmergingPatternEntity?

    @Query("SELECT COUNT(*) FROM emerging_patterns")
    suspend fun getEmergingPatternsCount(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmergingPattern(pattern: EmergingPatternEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmergingPatterns(patterns: List<EmergingPatternEntity>)
}

@Dao
interface CrossAssetClusterDao {
    @Query("SELECT * FROM cross_asset_clusters ORDER BY timestamp DESC")
    fun getAllClusters(): Flow<List<CrossAssetClusterEntity>>

    @Query("SELECT * FROM cross_asset_clusters ORDER BY timestamp DESC")
    suspend fun getClustersList(): List<CrossAssetClusterEntity>

    @Query("SELECT * FROM cross_asset_clusters WHERE clusterId = :clusterId LIMIT 1")
    suspend fun getClusterById(clusterId: String): CrossAssetClusterEntity?

    @Query("SELECT COUNT(*) FROM cross_asset_clusters")
    suspend fun getClustersCount(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCluster(cluster: CrossAssetClusterEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClusters(clusters: List<CrossAssetClusterEntity>)
}

@Dao
interface LeadLagRelationshipDao {
    @Query("SELECT * FROM lead_lag_relationships ORDER BY correlationScore DESC")
    fun getAllRelationships(): Flow<List<LeadLagRelationshipEntity>>

    @Query("SELECT * FROM lead_lag_relationships ORDER BY correlationScore DESC")
    suspend fun getRelationshipsList(): List<LeadLagRelationshipEntity>

    @Query("SELECT * FROM lead_lag_relationships WHERE relationshipId = :relationshipId LIMIT 1")
    suspend fun getRelationshipById(relationshipId: String): LeadLagRelationshipEntity?

    @Query("SELECT COUNT(*) FROM lead_lag_relationships")
    suspend fun getRelationshipsCount(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelationship(relationship: LeadLagRelationshipEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelationships(relationships: List<LeadLagRelationshipEntity>)
}

@Dao
interface NegativeKnowledgeDao {
    @Query("SELECT * FROM negative_knowledge_registry ORDER BY timestamp DESC")
    fun getAllNegativeKnowledge(): Flow<List<NegativeKnowledgeEntity>>

    @Query("SELECT * FROM negative_knowledge_registry ORDER BY timestamp DESC")
    suspend fun getNegativeKnowledgeList(): List<NegativeKnowledgeEntity>

    @Query("SELECT * FROM negative_knowledge_registry WHERE failureCategory = :category")
    suspend fun getNegativeKnowledgeByCategory(category: String): List<NegativeKnowledgeEntity>

    @Query("SELECT COUNT(*) FROM negative_knowledge_registry")
    suspend fun getNegativeKnowledgeCount(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNegativeKnowledge(item: NegativeKnowledgeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNegativeKnowledgeList(items: List<NegativeKnowledgeEntity>)
}

@Dao
interface ParsaRuleBookDao {
    @Query("SELECT * FROM parsa_rule_book ORDER BY ruleCode ASC")
    fun getAllRuleBookEntries(): Flow<List<ParsaRuleBookEntity>>

    @Query("SELECT * FROM parsa_rule_book ORDER BY ruleCode ASC")
    suspend fun getRuleBookList(): List<ParsaRuleBookEntity>

    @Query("SELECT * FROM parsa_rule_book WHERE ruleCode = :ruleCode LIMIT 1")
    suspend fun getRuleByCode(ruleCode: String): ParsaRuleBookEntity?

    @Query("SELECT * FROM parsa_rule_book WHERE isLocked = 1")
    suspend fun getLockedRules(): List<ParsaRuleBookEntity>

    @Query("SELECT COUNT(*) FROM parsa_rule_book")
    suspend fun getRuleBookCount(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRuleBookEntry(entry: ParsaRuleBookEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRuleBookEntries(entries: List<ParsaRuleBookEntity>)
}

// ==========================================
// DETECTIVE LAW (قانون کارآگاه) DAOS
// ==========================================

@Dao
interface DetectiveClueDao {
    @Query("SELECT * FROM detective_clues ORDER BY timestamp DESC")
    fun getAllClues(): Flow<List<DetectiveClueEntity>>

    @Query("SELECT * FROM detective_clues ORDER BY timestamp DESC")
    suspend fun getCluesList(): List<DetectiveClueEntity>

    @Query("SELECT * FROM detective_clues WHERE clueId = :clueId LIMIT 1")
    suspend fun getClueById(clueId: String): DetectiveClueEntity?

    @Query("SELECT COUNT(*) FROM detective_clues")
    suspend fun getCluesCount(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClue(clue: DetectiveClueEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClues(clues: List<DetectiveClueEntity>)
}

@Dao
interface DetectiveHypothesisDao {
    @Query("SELECT * FROM detective_hypotheses ORDER BY timestamp DESC")
    fun getAllHypotheses(): Flow<List<DetectiveHypothesisEntity>>

    @Query("SELECT * FROM detective_hypotheses ORDER BY timestamp DESC")
    suspend fun getHypothesesList(): List<DetectiveHypothesisEntity>

    @Query("SELECT * FROM detective_hypotheses WHERE hypothesisId = :hypId LIMIT 1")
    suspend fun getHypothesisById(hypId: String): DetectiveHypothesisEntity?

    @Query("SELECT * FROM detective_hypotheses WHERE clueId = :clueId")
    suspend fun getHypothesesForClue(clueId: String): List<DetectiveHypothesisEntity>

    @Query("SELECT COUNT(*) FROM detective_hypotheses")
    suspend fun getHypothesesCount(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHypothesis(hypothesis: DetectiveHypothesisEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHypotheses(hypotheses: List<DetectiveHypothesisEntity>)
}

@Dao
interface CompetingHypothesisDao {
    @Query("SELECT * FROM competing_hypotheses ORDER BY timestamp DESC")
    fun getAllCompetingHypotheses(): Flow<List<CompetingHypothesisEntity>>

    @Query("SELECT * FROM competing_hypotheses ORDER BY timestamp DESC")
    suspend fun getCompetingHypothesesList(): List<CompetingHypothesisEntity>

    @Query("SELECT * FROM competing_hypotheses WHERE hypothesisId = :hypId")
    suspend fun getCompetingForHypothesis(hypId: String): List<CompetingHypothesisEntity>

    @Query("SELECT COUNT(*) FROM competing_hypotheses")
    suspend fun getCompetingCount(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompeting(competing: CompetingHypothesisEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompetingList(list: List<CompetingHypothesisEntity>)
}

@Dao
interface DetectiveMethodDao {
    @Query("SELECT * FROM detective_methods ORDER BY confidence DESC")
    fun getAllDetectiveMethods(): Flow<List<DetectiveMethodEntity>>

    @Query("SELECT * FROM detective_methods ORDER BY confidence DESC")
    suspend fun getDetectiveMethodsList(): List<DetectiveMethodEntity>

    @Query("SELECT * FROM detective_methods WHERE methodId = :methodId LIMIT 1")
    suspend fun getDetectiveMethodById(methodId: String): DetectiveMethodEntity?

    @Query("SELECT * FROM detective_methods WHERE evidenceGrade = :grade ORDER BY confidence DESC")
    suspend fun getMethodsByGrade(grade: String): List<DetectiveMethodEntity>

    @Query("SELECT COUNT(*) FROM detective_methods")
    suspend fun getDetectiveMethodsCount(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetectiveMethod(method: DetectiveMethodEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetectiveMethods(methods: List<DetectiveMethodEntity>)
}

@Dao
interface DetectiveInvestigationRunDao {
    @Query("SELECT * FROM detective_investigation_runs ORDER BY timestamp DESC")
    fun getAllRuns(): Flow<List<DetectiveInvestigationRunEntity>>

    @Query("SELECT * FROM detective_investigation_runs ORDER BY timestamp DESC")
    suspend fun getRunsList(): List<DetectiveInvestigationRunEntity>

    @Query("SELECT * FROM detective_investigation_runs WHERE runId = :runId LIMIT 1")
    suspend fun getRunById(runId: String): DetectiveInvestigationRunEntity?

    @Query("SELECT * FROM detective_investigation_runs ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestRun(): DetectiveInvestigationRunEntity?

    @Query("SELECT COUNT(*) FROM detective_investigation_runs")
    suspend fun getRunsCount(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: DetectiveInvestigationRunEntity): Long
}

@Dao
interface DetectiveAuditTrailDao {
    @Query("SELECT * FROM detective_audit_trail ORDER BY timestamp DESC")
    fun getAllAuditTrail(): Flow<List<DetectiveAuditTrailEntity>>

    @Query("SELECT * FROM detective_audit_trail ORDER BY timestamp DESC")
    suspend fun getAuditTrailList(): List<DetectiveAuditTrailEntity>

    @Query("SELECT * FROM detective_audit_trail WHERE targetEntityId = :targetId")
    suspend fun getTrailForTarget(targetId: String): List<DetectiveAuditTrailEntity>

    @Query("SELECT COUNT(*) FROM detective_audit_trail")
    suspend fun getAuditTrailCount(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditTrail(trail: DetectiveAuditTrailEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditTrails(trails: List<DetectiveAuditTrailEntity>)
}






