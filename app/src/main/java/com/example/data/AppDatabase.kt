package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.*
import com.example.data.entity.*

@Database(
    entities = [
        UserEntity::class,
        SystemStateEntity::class,
        ExperimentEntity::class,
        TestRunEntity::class,
        TestResultEntity::class,
        AuditLogEntity::class,
        ModelVersionEntity::class,
        MemoryVersionEntity::class,
        MarketConceptEntity::class,
        RiskRuleEntity::class,
        EducationProgressEntity::class,
        MarketAssetEntity::class,
        HistoricalCandleEntity::class,
        ExperienceMemoryEntity::class,
        CrossAssetInsightEntity::class,
        DataIntegrityAnomalyEntity::class,
        HistoricalEventEntity::class,
        EventImpactEntity::class,
        HistoricalSetupEntity::class,
        HistoricalIndicatorSnapshotEntity::class,
        BatchProcessingCheckpointEntity::class,
        DiscoveredPatternEntity::class,
        AnalyticalMethodEntity::class,
        MethodEvaluationEntity::class,
        MethodJudgmentEntity::class,
        LessonLearnedEntity::class,
        MethodArbitrationReportEntity::class,
        CandidateRuleEntity::class,
        GeminiArbitrationReportEntity::class,
        FinalJudgeDecisionEntity::class,
        EmergingPatternEntity::class,
        CrossAssetClusterEntity::class,
        LeadLagRelationshipEntity::class,
        NegativeKnowledgeEntity::class,
        ParsaRuleBookEntity::class,
        DetectiveClueEntity::class,
        DetectiveHypothesisEntity::class,
        CompetingHypothesisEntity::class,
        DetectiveMethodEntity::class,
        DetectiveInvestigationRunEntity::class,
        DetectiveAuditTrailEntity::class
    ],
    version = 12,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun systemStateDao(): SystemStateDao
    abstract fun experimentDao(): ExperimentDao
    abstract fun testRunDao(): TestRunDao
    abstract fun testResultDao(): TestResultDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun modelVersionDao(): ModelVersionDao
    abstract fun memoryVersionDao(): MemoryVersionDao
    abstract fun marketConceptDao(): MarketConceptDao
    abstract fun riskRuleDao(): RiskRuleDao
    abstract fun educationProgressDao(): EducationProgressDao
    abstract fun marketAssetDao(): MarketAssetDao
    abstract fun historicalCandleDao(): HistoricalCandleDao
    abstract fun experienceMemoryDao(): ExperienceMemoryDao
    abstract fun crossAssetInsightDao(): CrossAssetInsightDao
    abstract fun dataIntegrityAnomalyDao(): DataIntegrityAnomalyDao
    abstract fun historicalEventDao(): HistoricalEventDao
    abstract fun eventImpactDao(): EventImpactDao
    abstract fun historicalSetupDao(): HistoricalSetupDao
    abstract fun historicalIndicatorDao(): HistoricalIndicatorDao
    abstract fun batchProcessingCheckpointDao(): BatchProcessingCheckpointDao
    abstract fun discoveredPatternDao(): DiscoveredPatternDao
    abstract fun analyticalMethodDao(): AnalyticalMethodDao
    abstract fun methodEvaluationDao(): MethodEvaluationDao
    abstract fun methodJudgmentDao(): MethodJudgmentDao
    abstract fun lessonLearnedDao(): LessonLearnedDao
    abstract fun methodArbitrationReportDao(): MethodArbitrationReportDao
    abstract fun candidateRuleDao(): CandidateRuleDao
    abstract fun geminiArbitrationReportDao(): GeminiArbitrationReportDao
    abstract fun finalJudgeDecisionDao(): FinalJudgeDecisionDao
    abstract fun emergingPatternDao(): EmergingPatternDao
    abstract fun crossAssetClusterDao(): CrossAssetClusterDao
    abstract fun leadLagRelationshipDao(): LeadLagRelationshipDao
    abstract fun negativeKnowledgeDao(): NegativeKnowledgeDao
    abstract fun parsaRuleBookDao(): ParsaRuleBookDao
    abstract fun detectiveClueDao(): DetectiveClueDao
    abstract fun detectiveHypothesisDao(): DetectiveHypothesisDao
    abstract fun competingHypothesisDao(): CompetingHypothesisDao
    abstract fun detectiveMethodDao(): DetectiveMethodDao
    abstract fun detectiveInvestigationRunDao(): DetectiveInvestigationRunDao
    abstract fun detectiveAuditTrailDao(): DetectiveAuditTrailDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "parsa_core_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

