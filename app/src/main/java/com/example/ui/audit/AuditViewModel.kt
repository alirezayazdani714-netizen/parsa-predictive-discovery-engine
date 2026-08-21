package com.example.ui.audit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.audit.ApiResponse
import com.example.data.audit.AuditApiService
import com.example.data.audit.BuildAuditDto
import com.example.data.audit.MemoryInspectionDto
import com.example.data.audit.ProjectStageDto
import com.example.data.audit.SystemStatusDto
import com.example.data.audit.TestRunReportDto
import com.example.data.audit.TestSummaryDto
import com.example.data.entity.AuditLogEntity
import com.example.data.entity.CandidateRuleEntity
import com.example.data.entity.CrossAssetClusterEntity
import com.example.data.entity.EmergingPatternEntity
import com.example.data.entity.FinalJudgeDecisionEntity
import com.example.data.entity.GeminiArbitrationReportEntity
import com.example.data.entity.LeadLagRelationshipEntity
import com.example.data.entity.LessonLearnedEntity
import com.example.data.entity.MethodArbitrationReportEntity
import com.example.data.entity.MethodJudgmentEntity
import com.example.data.entity.NegativeKnowledgeEntity
import com.example.data.entity.ParsaRuleBookEntity
import com.example.data.entity.DetectiveClueEntity
import com.example.data.entity.DetectiveHypothesisEntity
import com.example.data.entity.CompetingHypothesisEntity
import com.example.data.entity.DetectiveMethodEntity
import com.example.data.entity.DetectiveInvestigationRunEntity
import com.example.data.entity.DetectiveAuditTrailEntity
import com.example.data.repository.AuditRepository
import com.example.data.testing.AutomatedTestEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuditUiState(
    val isLoading: Boolean = true,
    val isRunningTests: Boolean = false,
    val isRunningInvestigation: Boolean = false,
    val selectedTab: Int = 0,
    val systemStatus: SystemStatusDto? = null,
    val buildAudit: BuildAuditDto? = null,
    val projectStage: ProjectStageDto? = null,
    val latestTestSummary: TestSummaryDto? = null,
    val latestTestReport: TestRunReportDto? = null,
    val auditLogs: List<AuditLogEntity> = emptyList(),
    val methodJudgments: List<MethodJudgmentEntity> = emptyList(),
    val lessonsLearned: List<LessonLearnedEntity> = emptyList(),
    val arbitrationReports: List<MethodArbitrationReportEntity> = emptyList(),
    val geminiReports: List<GeminiArbitrationReportEntity> = emptyList(),
    val finalJudgeDecisions: List<FinalJudgeDecisionEntity> = emptyList(),
    val candidateRules: List<CandidateRuleEntity> = emptyList(),
    val emergingPatterns: List<EmergingPatternEntity> = emptyList(),
    val crossAssetClusters: List<CrossAssetClusterEntity> = emptyList(),
    val leadLagRelationships: List<LeadLagRelationshipEntity> = emptyList(),
    val negativeKnowledge: List<NegativeKnowledgeEntity> = emptyList(),
    val ruleBookEntries: List<ParsaRuleBookEntity> = emptyList(),
    val detectiveClues: List<DetectiveClueEntity> = emptyList(),
    val detectiveHypotheses: List<DetectiveHypothesisEntity> = emptyList(),
    val competingHypotheses: List<CompetingHypothesisEntity> = emptyList(),
    val detectiveMethods: List<DetectiveMethodEntity> = emptyList(),
    val detectiveRuns: List<DetectiveInvestigationRunEntity> = emptyList(),
    val detectiveAuditTrail: List<DetectiveAuditTrailEntity> = emptyList(),
    val detectiveMissionSummary: Map<String, Any> = emptyMap(),
    val memoryInfo: MemoryInspectionDto? = null,
    val apiExplorerRoute: String = "/api/audit/detective/mission-audit",
    val apiExplorerMethod: String = "GET",
    val apiExplorerResponse: String = "",
    val errorMessage: String? = null
)

class AuditViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = AuditRepository(database)
    private val testEngine = AutomatedTestEngine(repository)
    val apiService = AuditApiService(repository, testEngine)

    private val _uiState = MutableStateFlow(AuditUiState())
    val uiState: StateFlow<AuditUiState> = _uiState.asStateFlow()

    init {
        initializeAndLoad()
    }

    fun initializeAndLoad() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.initializeSystemStateIfNeeded()

                // Run initial tests if not present
                val latestRun = repository.getLatestTestRun()
                val runId = latestRun?.id ?: testEngine.runAllAutomatedTests()

                val statusRes = apiService.getStatus()
                val buildRes = apiService.getBuild()
                val stageRes = apiService.getProjectStage()
                val testRes = apiService.getTestById(runId)
                val memRes = apiService.getMemory()
                val logs = repository.getRecentLogs(50)
                val judgments = repository.getMethodJudgments()
                val lessons = repository.getLessonsLearned()
                val arbitrationReports = repository.getMethodArbitrationReports()
                val geminiReports = repository.getGeminiArbitrationReports()
                val finalDecisions = repository.getFinalJudgeDecisions()
                val candidateRules = repository.getCandidateRules()
                val emergingPatterns = repository.getEmergingPatterns()
                val crossAssetClusters = repository.getCrossAssetClusters()
                val leadLagRelationships = repository.getLeadLagRelationships()
                val negativeKnowledge = repository.getNegativeKnowledge()
                val ruleBookEntries = repository.getRuleBookEntries()

                // Detective Law data
                val detectiveClues = repository.getDetectiveClues()
                val detectiveHypotheses = repository.getDetectiveHypotheses()
                val competingHypotheses = repository.getCompetingHypotheses()
                val detectiveMethods = repository.getDetectiveMethods()
                val detectiveRuns = repository.getDetectiveRuns()
                val detectiveAuditTrail = repository.getDetectiveAuditTrail()
                val detectiveSummary = repository.getDetectiveLawPrinciplesSummary()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        systemStatus = statusRes.data,
                        buildAudit = buildRes.data,
                        projectStage = stageRes.data,
                        latestTestSummary = testRes.data?.run,
                        latestTestReport = testRes.data,
                        memoryInfo = memRes.data,
                        auditLogs = logs,
                        methodJudgments = judgments,
                        lessonsLearned = lessons,
                        arbitrationReports = arbitrationReports,
                        geminiReports = geminiReports,
                        finalJudgeDecisions = finalDecisions,
                        candidateRules = candidateRules,
                        emergingPatterns = emergingPatterns,
                        crossAssetClusters = crossAssetClusters,
                        leadLagRelationships = leadLagRelationships,
                        negativeKnowledge = negativeKnowledge,
                        ruleBookEntries = ruleBookEntries,
                        detectiveClues = detectiveClues,
                        detectiveHypotheses = detectiveHypotheses,
                        competingHypotheses = competingHypotheses,
                        detectiveMethods = detectiveMethods,
                        detectiveRuns = detectiveRuns,
                        detectiveAuditTrail = detectiveAuditTrail,
                        detectiveMissionSummary = detectiveSummary
                    )
                }

                // Initial API explorer load
                testApiEndpoint("GET", "/api/audit/detective/mission-audit")
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Initialization error: ${e.message}"
                    )
                }
            }
        }
    }

    fun runDetectiveInvestigation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRunningInvestigation = true) }
            try {
                val result = repository.executeDetectiveInvestigation()
                val logs = repository.getRecentLogs(50)
                _uiState.update {
                    it.copy(
                        isRunningInvestigation = false,
                        detectiveClues = result.clues,
                        detectiveHypotheses = result.hypotheses,
                        competingHypotheses = result.competingHypotheses,
                        detectiveMethods = result.inventedMethods,
                        negativeKnowledge = result.negativeKnowledgeItems,
                        ruleBookEntries = result.candidateRules,
                        detectiveRuns = listOf(result.run),
                        detectiveAuditTrail = result.auditTrail,
                        auditLogs = logs
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isRunningInvestigation = false,
                        errorMessage = "Detective investigation error: ${e.message}"
                    )
                }
            }
        }
    }

    fun selectTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    fun runTests() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRunningTests = true) }
            try {
                val runId = testEngine.runAllAutomatedTests()
                val testRes = apiService.getTestById(runId)
                val logs = repository.getRecentLogs(50)

                _uiState.update {
                    it.copy(
                        isRunningTests = false,
                        latestTestSummary = testRes.data?.run,
                        latestTestReport = testRes.data,
                        auditLogs = logs
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isRunningTests = false,
                        errorMessage = "Test run failed: ${e.message}"
                    )
                }
            }
        }
    }

    fun testApiEndpoint(method: String, path: String) {
        viewModelScope.launch {
            try {
                val response = apiService.dispatchRoute(method, path)
                val prettyJson = formatApiResponse(response)
                _uiState.update {
                    it.copy(
                        apiExplorerMethod = method,
                        apiExplorerRoute = path,
                        apiExplorerResponse = prettyJson
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        apiExplorerMethod = method,
                        apiExplorerRoute = path,
                        apiExplorerResponse = "Error invoking endpoint: ${e.message}"
                    )
                }
            }
        }
    }

    private fun formatApiResponse(res: ApiResponse<out Any>): String {
        return """
        {
          "success": ${res.success},
          "status": "${res.status ?: "UNKNOWN"}",
          "path": "${res.path}",
          "timestamp": ${res.timestamp},
          "data": ${res.data ?: "null"},
          "error": ${if (res.error != null) "\"${res.error}\"" else "null"}
        }
        """.trimIndent()
    }
}
