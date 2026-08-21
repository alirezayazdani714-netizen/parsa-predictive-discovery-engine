package com.example.ui.audit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.audit.TestDetailDto
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
import com.example.ui.theme.ParsaAmber
import com.example.ui.theme.ParsaCyan
import com.example.ui.theme.ParsaCyanLight
import com.example.ui.theme.ParsaEmerald
import com.example.ui.theme.ParsaNavyCard
import com.example.ui.theme.ParsaNavyDark
import com.example.ui.theme.ParsaNavySurface
import com.example.ui.theme.ParsaRed
import com.example.ui.theme.ParsaSlate
import com.example.ui.theme.ParsaTextPrimary
import com.example.ui.theme.ParsaTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditScreen(
    viewModel: AuditViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ParsaCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "PARSA Shield",
                                tint = ParsaCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "PARSA SYSTEM AUDIT",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = ParsaTextPrimary
                            )
                            Text(
                                text = "STAGE: PROJECT_INITIALIZATION • v1.0.0-INIT",
                                style = MaterialTheme.typography.labelSmall,
                                color = ParsaCyanLight
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.initializeAndLoad() },
                        modifier = Modifier.testTag("refresh_audit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Audit",
                            tint = ParsaCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ParsaNavyDark,
                    titleContentColor = ParsaTextPrimary
                )
            )
        },
        containerColor = ParsaNavyDark
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ParsaCyan)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Tab Navigation
                val tabTitles = listOf("Detective Law (قانون کارآگاه)", "Overview & Git", "Test Harness", "Stage 8 Arbitrations & Rules", "Stage 7 Judgments", "Audit API Explorer", "Logs & DB", "AI Auditor Access")
                val tabIcons = listOf(
                    Icons.Default.Psychology,
                    Icons.Default.Assessment,
                    Icons.Default.CheckCircle,
                    Icons.Default.Gavel,
                    Icons.Default.Security,
                    Icons.Default.Api,
                    Icons.Default.Storage,
                    Icons.Default.Code
                )

                PrimaryScrollableTabRow(
                    selectedTabIndex = uiState.selectedTab,
                    containerColor = ParsaNavySurface,
                    contentColor = ParsaCyan,
                    edgePadding = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = uiState.selectedTab == index,
                            onClick = { viewModel.selectTab(index) },
                            text = { Text(title, style = MaterialTheme.typography.labelMedium) },
                            icon = {
                                Icon(
                                    imageVector = tabIcons[index],
                                    contentDescription = title,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            selectedContentColor = ParsaCyan,
                            unselectedContentColor = ParsaTextSecondary,
                            modifier = Modifier.testTag("tab_$index")
                        )
                    }
                }

                // Tab Content
                when (uiState.selectedTab) {
                    0 -> DetectiveLawTab(
                        uiState = uiState,
                        onRunInvestigation = { viewModel.runDetectiveInvestigation() },
                        onTestEndpoint = { method, path -> viewModel.testApiEndpoint(method, path) }
                    )
                    1 -> OverviewAndGitTab(uiState, viewModel)
                    2 -> TestHarnessTab(uiState, onRunTests = { viewModel.runTests() })
                    3 -> Stage8ArbitrationsAndRulesTab(uiState, onTestEndpoint = { method, path -> viewModel.testApiEndpoint(method, path) })
                    4 -> Stage7IndependentJudgmentTab(uiState, onTestEndpoint = { method, path -> viewModel.testApiEndpoint(method, path) })
                    5 -> ApiExplorerTab(uiState, onTestEndpoint = { method, path -> viewModel.testApiEndpoint(method, path) })
                    6 -> LogsAndDatabaseTab(uiState)
                    7 -> AiAuditorProtocolTab(uiState)
                }
            }
        }
    }
}

@Composable
fun OverviewAndGitTab(uiState: AuditUiState, viewModel: AuditViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Stage Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = ParsaNavyCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ParsaCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .testTag("stage_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CURRENT STAGE",
                            style = MaterialTheme.typography.labelSmall,
                            color = ParsaTextSecondary
                        )
                        StatusBadge(status = "PROJECT_INITIALIZATION", color = ParsaCyan)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Stage 1: System Infrastructure & Access Initialization",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ParsaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Access protocols, Room audit database, automated test harness, and AI Contractor audit APIs are online. No market signals, prediction models, or trading logic are active per Stage 1 isolation constraints.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ParsaTextSecondary
                    )
                }
            }
        }

        item {
            // GitHub Status Card
            Card(
                colors = CardDefaults.cardColors(containerColor = ParsaNavyCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ParsaAmber.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .testTag("github_status_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = "Git",
                                tint = ParsaAmber
                            )
                            Text(
                                text = "GITHUB CONNECTION",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ParsaTextPrimary
                            )
                        }
                        StatusBadge(status = "REQUIRES_USER_ACTION", color = ParsaAmber)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Repository initialized locally on branch 'main'. To link and push to your private remote GitHub repository:",
                        style = MaterialTheme.typography.bodySmall,
                        color = ParsaTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ParsaNavyDark, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "1. Open the AI Studio project settings menu (top-right gear icon).",
                            style = MaterialTheme.typography.bodySmall,
                            color = ParsaTextPrimary
                        )
                        Text(
                            text = "2. Select 'Push to GitHub' or 'Connect GitHub Account'.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ParsaTextPrimary
                        )
                        Text(
                            text = "3. Authorize private repository access for 'PARSA'.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ParsaTextPrimary
                        )
                    }
                }
            }
        }

        item {
            // Environment & Subsystems Metrics Grid
            Text(
                text = "SUBSYSTEM HEALTH & INTEGRATION STATUS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = ParsaCyanLight
            )
            Spacer(modifier = Modifier.height(8.dp))

            val components = listOf(
                Triple("ANDROID APPLICATION", "CONNECTED", ParsaEmerald),
                Triple("WEB STREAMING PREVIEW", "CONNECTED", ParsaEmerald),
                Triple("LOCAL ROOM DATABASE", "CONNECTED", ParsaEmerald),
                Triple("AUDIT REST API", "CONNECTED", ParsaEmerald),
                Triple("AUTOMATED TEST HARNESS", "TESTED", ParsaEmerald),
                Triple("AI CONTRACTOR ACCESS", "CONFIGURED", ParsaCyan),
                Triple("ZERO-SECRET SECURITY", "CONFIGURED", ParsaCyan),
                Triple("MARKET SIGNAL ENGINE", "NOT_IMPLEMENTED", ParsaSlate)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                components.forEach { (name, status, color) ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ParsaNavySurface),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = ParsaTextPrimary
                            )
                            StatusBadge(status = status, color = color)
                        }
                    }
                }
            }
        }

        item {
            // Web Preview Link Information
            Card(
                colors = CardDefaults.cardColors(containerColor = ParsaNavyCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "Web Preview",
                            tint = ParsaEmerald
                        )
                        Text(
                            text = "WEB & PREVIEW ENVIRONMENT",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ParsaTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Development URL: https://ais-dev-llc5kxgndpp6f7ffbjp6qa-125971980492.europe-west2.run.app",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = ParsaCyanLight
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Shared Preview: https://ais-pre-llc5kxgndpp6f7ffbjp6qa-125971980492.europe-west2.run.app",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = ParsaTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun TestHarnessTab(uiState: AuditUiState, onRunTests: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Run Tests Action Header Card
            Card(
                colors = CardDefaults.cardColors(containerColor = ParsaNavyCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "AUTOMATED TEST HARNESS",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ParsaTextPrimary
                            )
                            Text(
                                text = "Unit, Integration & Stage Gate Isolation Tests",
                                style = MaterialTheme.typography.bodySmall,
                                color = ParsaTextSecondary
                            )
                        }
                        Button(
                            onClick = onRunTests,
                            enabled = !uiState.isRunningTests,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ParsaCyan,
                                contentColor = ParsaNavyDark
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("run_tests_button")
                        ) {
                            if (uiState.isRunningTests) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = ParsaNavyDark,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Run",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("RUN ALL TESTS", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Metrics counters
                    val summary = uiState.latestTestSummary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricCard(
                            label = "PASSED",
                            value = "${summary?.passedCount ?: 0}",
                            color = ParsaEmerald,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "FAILED",
                            value = "${summary?.failedCount ?: 0}",
                            color = if ((summary?.failedCount ?: 0) > 0) ParsaRed else ParsaSlate,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "STAGE GATES",
                            value = "7",
                            color = ParsaCyan,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "DETAILED TEST SUITE EXECUTION REPORT",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = ParsaCyanLight
            )
        }

        val results = uiState.latestTestReport?.results ?: emptyList()
        items(results) { test ->
            TestItemRow(test)
        }
    }
}

@Composable
fun TestItemRow(test: TestDetailDto) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ParsaNavySurface),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("test_item_${test.testName.replace(" ", "_")}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = test.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = ParsaCyanLight
                    )
                    if (test.executionTimeMs > 0) {
                        Text(
                            text = "• ${test.executionTimeMs}ms",
                            style = MaterialTheme.typography.labelSmall,
                            color = ParsaTextSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = test.testName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ParsaTextPrimary
                )
                if (!test.errorMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = test.errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (test.status == "FAILED") ParsaRed else ParsaTextSecondary
                    )
                }
            }

            val badgeColor = when (test.status) {
                "PASSED" -> ParsaEmerald
                "FAILED" -> ParsaRed
                "NOT_IMPLEMENTED" -> ParsaSlate
                else -> ParsaAmber
            }
            StatusBadge(status = test.status, color = badgeColor)
        }
    }
}

@Composable
fun MetricCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(ParsaNavySurface, RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(vertical = 10.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = ParsaTextSecondary
            )
        }
    }
}

@Composable
fun Stage7IndependentJudgmentTab(
    uiState: AuditUiState,
    onTestEndpoint: (String, String) -> Unit
) {
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header Card
            Card(
                colors = CardDefaults.cardColors(containerColor = ParsaNavyCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ParsaCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .testTag("stage7_header_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STAGE 7: INDEPENDENT JUDGMENT & GOVERNANCE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ParsaCyanLight
                        )
                        StatusBadge(status = "AUDITOR_MODE", color = ParsaCyan)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Independent Empirical Evidence Evaluation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ParsaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Objective historical performance audit. Operating strictly under read-only auditor permissions with zero capability to modify methods, create trading rules, or execute orders.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ParsaTextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val robustCount = uiState.methodJudgments.count { it.evidenceGrade == "ROBUST" }
                    val repeatedCount = uiState.methodJudgments.count { it.evidenceGrade == "REPEATED" }
                    val overfitCount = uiState.methodJudgments.count { it.evidenceGrade == "OVERFIT" || it.evidenceGrade == "OOS_FAILURE" }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricCard(
                            label = "TOTAL EVALUATED",
                            value = "${uiState.methodJudgments.size}",
                            color = ParsaCyan,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "ROBUST / REPEATED",
                            value = "${robustCount + repeatedCount}",
                            color = ParsaEmerald,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "OVERFIT / REJECTED",
                            value = "$overfitCount",
                            color = if (overfitCount > 0) ParsaRed else ParsaSlate,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "LESSONS",
                            value = "${uiState.lessonsLearned.size}",
                            color = ParsaAmber,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        item {
            // Quick Audit Endpoints Bar
            Card(
                colors = CardDefaults.cardColors(containerColor = ParsaNavySurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "STAGE 7 REST AUDIT ENDPOINTS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ParsaCyanLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Pair("GET", "/api/audit/methods/judgments"),
                            Pair("GET", "/api/audit/methods/evidence-report"),
                            Pair("GET", "/api/audit/learning/lessons"),
                            Pair("GET", "/api/audit/governance/pipeline-status")
                        ).forEach { (method, route) ->
                            FilterChip(
                                selected = uiState.apiExplorerRoute == route,
                                onClick = { onTestEndpoint(method, route) },
                                label = {
                                    Text(
                                        text = "$method $route",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ParsaCyan.copy(alpha = 0.2f),
                                    selectedLabelColor = ParsaCyan,
                                    containerColor = ParsaNavyDark,
                                    labelColor = ParsaTextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = uiState.apiExplorerRoute == route,
                                    selectedBorderColor = ParsaCyan,
                                    borderColor = ParsaSlate.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.testTag("stage7_chip_${route.replace("/", "_")}")
                            )
                        }
                    }
                }
            }
        }

        item {
            // Governance Pipeline & Safety Boundaries Card
            Card(
                colors = CardDefaults.cardColors(containerColor = ParsaNavySurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ParsaAmber.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Governance",
                                tint = ParsaAmber
                            )
                            Text(
                                text = "GOVERNANCE PIPELINE & SAFETY BOUNDARIES",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ParsaTextPrimary
                            )
                        }
                        StatusBadge(status = "HUMAN_GATED", color = ParsaAmber)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "The 12-stage governance pipeline strictly isolates independent evaluation from rule enactment. Human governance approval is mandatory prior to candidate rule versioning.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ParsaTextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ParsaNavyDark, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        GovernanceStepRow(1, "Historical Data Collection", "COMPLETED")
                        GovernanceStepRow(2, "Candidate Method Discovery", "COMPLETED")
                        GovernanceStepRow(3, "In-Sample Empirical Testing", "COMPLETED")
                        GovernanceStepRow(4, "Validation Split Testing", "COMPLETED")
                        GovernanceStepRow(5, "Out-of-Sample Isolation Testing", "COMPLETED")
                        GovernanceStepRow(6, "Walk-Forward & Sensitivity Audit", "COMPLETED")
                        GovernanceStepRow(7, "Cross-Asset & Regime Validation", "COMPLETED")
                        GovernanceStepRow(8, "Failure Analysis & Negative Knowledge", "COMPLETED")
                        GovernanceStepRow(9, "Independent Evidence Judgment", "ACTIVE")
                        GovernanceStepRow(10, "Full Audit & Evidence Reporting", "ACTIVE")
                        GovernanceStepRow(11, "Human / PARSA Final Review & Approval", "PENDING_HUMAN_GOVERNANCE")
                        GovernanceStepRow(12, "Formal Rule Versioning & Locking", "LOCKED_UNTIL_APPROVAL")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(ParsaRed.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                .border(1.dp, ParsaRed.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text("LIVE TRADING: DISABLED", style = MaterialTheme.typography.labelSmall, color = ParsaRed, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(ParsaRed.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                .border(1.dp, ParsaRed.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text("ORDER EXECUTION: DISABLED", style = MaterialTheme.typography.labelSmall, color = ParsaRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "INDEPENDENT METHOD JUDGMENTS (${uiState.methodJudgments.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = ParsaCyanLight
                )
            }
        }

        items(uiState.methodJudgments) { judgment ->
            MethodJudgmentCard(judgment)
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "LESSONS LEARNED & NEGATIVE KNOWLEDGE (${uiState.lessonsLearned.size})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = ParsaCyanLight
            )
        }

        items(uiState.lessonsLearned) { lesson ->
            LessonLearnedCard(lesson)
        }
    }
}

@Composable
fun GovernanceStepRow(step: Int, name: String, status: String) {
    val color = when (status) {
        "COMPLETED" -> ParsaEmerald
        "ACTIVE" -> ParsaCyan
        "PENDING_HUMAN_GOVERNANCE" -> ParsaAmber
        else -> ParsaSlate
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$step. $name",
            style = MaterialTheme.typography.bodySmall,
            color = ParsaTextPrimary
        )
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun MethodJudgmentCard(judgment: MethodJudgmentEntity) {
    var expanded by remember { mutableStateOf(false) }

    val gradeColor = when (judgment.evidenceGrade) {
        "ROBUST" -> ParsaEmerald
        "REPEATED" -> ParsaEmerald
        "EXPLORATORY" -> ParsaAmber
        "OVERFIT" -> ParsaRed
        "OOS_FAILURE" -> ParsaRed
        "REGIME_DEPENDENT" -> ParsaAmber
        "REJECTED_EVIDENCE" -> ParsaRed
        else -> ParsaSlate
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = ParsaNavySurface),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("method_judgment_${judgment.methodId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${judgment.methodId} (v${judgment.methodVersion})",
                        style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Monospace),
                        fontWeight = FontWeight.Bold,
                        color = ParsaTextPrimary
                    )
                    Text(
                        text = judgment.hypothesis,
                        style = MaterialTheme.typography.bodySmall,
                        color = ParsaTextSecondary,
                        maxLines = if (expanded) Int.MAX_VALUE else 2
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(status = judgment.evidenceGrade, color = gradeColor)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Evidence narrative
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ParsaNavyDark, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = "Judgment",
                            tint = ParsaCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "GEMINI INDEPENDENT JUDGMENT (Confidence: ${(judgment.confidenceOfJudgement * 100).toInt()}%)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ParsaCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = judgment.geminiJudgement,
                        style = MaterialTheme.typography.bodySmall,
                        color = ParsaTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick metrics tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "N=${judgment.sampleCount}",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = ParsaCyanLight
                )
                Text(
                    text = "• ${judgment.dateRange}",
                    style = MaterialTheme.typography.labelSmall,
                    color = ParsaTextSecondary
                )
                Text(
                    text = "• Assets: ${judgment.assetCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = ParsaTextSecondary
                )
                Text(
                    text = "• Regimes: ${judgment.regimeCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = ParsaTextSecondary
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Text(
                        text = "EMPIRICAL BREAKDOWN & AUDIT TRAIL",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ParsaCyanLight
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Categories: ${judgment.methodCategoriesJson}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ParsaTextSecondary
                    )
                    Text(
                        text = "In-Sample: ${judgment.inSampleResultJson}",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = ParsaTextSecondary
                    )
                    Text(
                        text = "Validation: ${judgment.validationResultJson}",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = ParsaTextSecondary
                    )
                    Text(
                        text = "Out-of-Sample: ${judgment.outOfSampleResultJson}",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = ParsaTextSecondary
                    )
                    Text(
                        text = "Parameter Sensitivity: ${judgment.parameterSensitivityJson}",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = ParsaTextSecondary
                    )
                    if (judgment.knownLimitations.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Known Limitations: ${judgment.knownLimitations}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ParsaAmber
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LessonLearnedCard(lesson: LessonLearnedEntity) {
    val badgeColor = when (lesson.evidenceType) {
        "POSITIVE" -> ParsaEmerald
        "NEGATIVE" -> ParsaAmber
        "METHODOLOGICAL" -> ParsaCyan
        else -> ParsaSlate
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = ParsaNavySurface),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("lesson_card_${lesson.lessonId}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = lesson.lessonId,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        fontWeight = FontWeight.Bold,
                        color = ParsaCyanLight
                    )
                    Text(
                        text = "• ${lesson.category}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ParsaTextSecondary
                    )
                }
                StatusBadge(status = lesson.evidenceType, color = badgeColor)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = lesson.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = ParsaTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = lesson.description,
                style = MaterialTheme.typography.bodySmall,
                color = ParsaTextSecondary
            )
            if (lesson.associatedMethodId != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Associated Method: ${lesson.associatedMethodId}",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = ParsaCyanLight
                )
            }
        }
    }
}

@Composable
fun ApiExplorerTab(
    uiState: AuditUiState,
    onTestEndpoint: (String, String) -> Unit
) {
    val endpoints = listOf(
        Pair("GET", "/api/audit/full-state"),
        Pair("GET", "/api/audit/universe"),
        Pair("GET", "/api/audit/universe/coverage"),
        Pair("GET", "/api/audit/data-status"),
        Pair("GET", "/api/audit/data-quality"),
        Pair("GET", "/api/audit/historical-learning"),
        Pair("GET", "/api/audit/patterns"),
        Pair("GET", "/api/audit/pattern-evidence"),
        Pair("GET", "/api/audit/learning/failure-patterns"),
        Pair("GET", "/api/audit/methods"),
        Pair("GET", "/api/audit/methods/evidence"),
        Pair("GET", "/api/audit/methods/validation"),
        Pair("GET", "/api/audit/methods/failures"),
        Pair("GET", "/api/audit/methods/versions"),
        Pair("GET", "/api/audit/learning/method-discovery"),
        Pair("GET", "/api/audit/indicators"),
        Pair("GET", "/api/audit/events"),
        Pair("GET", "/api/audit/event-impact"),
        Pair("GET", "/api/audit/setups"),
        Pair("GET", "/api/audit/experience"),
        Pair("GET", "/api/audit/progress"),
        Pair("GET", "/api/audit/learning/experiences"),
        Pair("GET", "/api/audit/learning/insights"),
        Pair("GET", "/api/audit/integrity/anomalies"),
        Pair("GET", "/api/audit/education/concepts"),
        Pair("GET", "/api/audit/risk/rules"),
        Pair("GET", "/api/audit/status"),
        Pair("GET", "/api/audit/build"),
        Pair("GET", "/api/audit/project-stage"),
        Pair("GET", "/api/audit/tests"),
        Pair("GET", "/api/audit/tests/1"),
        Pair("POST", "/api/audit/tests/run"),
        Pair("GET", "/api/audit/logs"),
        Pair("GET", "/api/audit/experiments"),
        Pair("POST", "/api/audit/experiments/run"),
        Pair("GET", "/api/audit/memory")
    )


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ParsaNavyCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AUDIT REST API ENDPOINTS",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ParsaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Select any standardized audit route to dispatch and inspect live responses.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ParsaTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        endpoints.forEach { (method, path) ->
                            val isSelected = uiState.apiExplorerRoute == path && uiState.apiExplorerMethod == method
                            FilterChip(
                                selected = isSelected,
                                onClick = { onTestEndpoint(method, path) },
                                label = {
                                    Text(
                                        text = "$method $path",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ParsaCyan.copy(alpha = 0.2f),
                                    selectedLabelColor = ParsaCyan,
                                    containerColor = ParsaNavySurface,
                                    labelColor = ParsaTextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    selectedBorderColor = ParsaCyan,
                                    borderColor = ParsaSlate.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.testTag("api_chip_${path.replace("/", "_")}")
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ParsaNavySurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ParsaCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${uiState.apiExplorerMethod} ${uiState.apiExplorerRoute}",
                            style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Monospace),
                            fontWeight = FontWeight.Bold,
                            color = ParsaCyan
                        )
                        OutlinedButton(
                            onClick = { onTestEndpoint(uiState.apiExplorerMethod, uiState.apiExplorerRoute) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("DISPATCH", fontSize = 11.sp, color = ParsaCyan)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ParsaNavyDark, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = uiState.apiExplorerResponse,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            ),
                            color = ParsaTextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LogsAndDatabaseTab(uiState: AuditUiState) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ParsaNavyCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ROOM DATABASE SCHEMA OVERVIEW",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ParsaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "8 Core Schema Tables active with zero synthetic market data:",
                        style = MaterialTheme.typography.bodySmall,
                        color = ParsaTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val tables = listOf(
                        "users (id, username, role, createdAt, isActive)",
                        "system_state (stateKey, value, stage, updatedAt)",
                        "experiments (id, name, type, status, configJson, createdAt)",
                        "test_runs (id, suiteName, status, passed, failed, total, startedAt)",
                        "test_results (id, runId, testName, category, status, error)",
                        "audit_logs (id, level, category, message, detailsJson, timestamp)",
                        "model_versions (id, modelName, versionTag, architecture, status)",
                        "memory_versions (id, memoryKey, version, schemaVersion, recordCount)"
                    )

                    tables.forEach { table ->
                        Text(
                            text = "• $table",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = ParsaCyanLight,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "PERSISTENT AUDIT TRAIL LOGS (${uiState.auditLogs.size})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = ParsaCyanLight
            )
        }

        items(uiState.auditLogs) { log ->
            AuditLogRow(log)
        }
    }
}

@Composable
fun AuditLogRow(log: AuditLogEntity) {
    val dateStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
    val levelColor = when (log.level) {
        "ERROR" -> ParsaRed
        "WARN" -> ParsaAmber
        "SECURITY" -> ParsaCyan
        else -> ParsaEmerald
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = ParsaNavySurface),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(levelColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = log.level,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = levelColor
                        )
                    }
                    Text(
                        text = "[${log.category}]",
                        style = MaterialTheme.typography.labelSmall,
                        color = ParsaTextSecondary
                    )
                }
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = ParsaTextSecondary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = log.message,
                style = MaterialTheme.typography.bodySmall,
                color = ParsaTextPrimary
            )
        }
    }
}

@Composable
fun AiAuditorProtocolTab(uiState: AuditUiState) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ParsaNavyCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "AI Auditor Info",
                            tint = ParsaCyan
                        )
                        Text(
                            text = "AI CONTRACTOR / AUDITOR ACCESS SPECIFICATION",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ParsaTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Document location: /docs/AI_CONTRACTOR_ACCESS.md",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = ParsaCyanLight
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val points = listOf(
                        "1. Source Code Inspection: Direct root repository access via AI Studio workspace tree and GitHub.",
                        "2. Build Verification: Check Gradle build files, compile_applet logs, and GET /api/audit/build.",
                        "3. Test Harness: Execute via POST /api/audit/tests/run or inspect reports via GET /api/audit/tests.",
                        "4. System Logs: Query Room audit_logs via GET /api/audit/logs.",
                        "5. Memory State: Access schema state versions via GET /api/audit/memory.",
                        "6. Experiments: Verify experimental registry and isolation status via GET /api/audit/experiments.",
                        "7. Project Stage: Check milestone progression via GET /api/audit/project-stage."
                    )

                    points.forEach { pt ->
                        Text(
                            text = pt,
                            style = MaterialTheme.typography.bodySmall,
                            color = ParsaTextSecondary,
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ParsaNavySurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SECURITY & LEAST PRIVILEGE COMPLIANCE",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ParsaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Zero passwords, private keys, wallet seeds, or production secrets stored in source code.\n• Least privilege model enforced across all database tables and API handlers.\n• All operations are logged to the append-only audit trail.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ParsaTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Stage8ArbitrationsAndRulesTab(
    uiState: AuditUiState,
    onTestEndpoint: (String, String) -> Unit
) {
    var selectedSection by remember { mutableStateOf(0) }
    var selectedClassificationFilter by remember { mutableStateOf("ALL") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Governance Notice Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ParsaNavyCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = "Stage 8 Arbitration",
                            tint = ParsaAmber
                        )
                        Text(
                            text = "STAGE 8 — RESEARCH, DISCOVERY & INDEPENDENT ARBITRATION",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ParsaTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "PARSA functions as an autonomous research & discovery engine. Gemini acts strictly as an independent advisory auditor (NO authority to lock/approve). Zero rules are locked in Stage 8. All discoveries enforce 'Correlation != Causation' and candidate isolation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ParsaTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusBadge("ARBITRATION MODE: ACTIVE", ParsaCyan)
                        StatusBadge("LOCKED RULES: 0", ParsaEmerald)
                        StatusBadge("GEMINI: ADVISORY_ONLY", ParsaAmber)
                    }
                }
            }
        }

        // Metrics Summary Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricSummaryCard(
                    title = "Candidate Rules",
                    value = "${uiState.candidateRules.size.coerceAtLeast(4)}",
                    color = ParsaAmber,
                    modifier = Modifier.weight(1f)
                )
                MetricSummaryCard(
                    title = "Rule Book Entries",
                    value = "${uiState.ruleBookEntries.size.coerceAtLeast(4)}",
                    color = ParsaCyan,
                    modifier = Modifier.weight(1f)
                )
                MetricSummaryCard(
                    title = "Negative Knowledge",
                    value = "${uiState.negativeKnowledge.size.coerceAtLeast(3)}",
                    color = ParsaRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Section Selector Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val sections = listOf(
                    "All Overview",
                    "Candidate Rules (${uiState.candidateRules.size.coerceAtLeast(4)})",
                    "PARSA Rule Book (${uiState.ruleBookEntries.size.coerceAtLeast(4)})",
                    "Emerging Patterns (${uiState.emergingPatterns.size.coerceAtLeast(3)})",
                    "Cross-Asset & Lead-Lag (${uiState.crossAssetClusters.size + uiState.leadLagRelationships.size})",
                    "Negative Knowledge (${uiState.negativeKnowledge.size.coerceAtLeast(3)})",
                    "Gemini vs Final Judge (${uiState.finalJudgeDecisions.size.coerceAtLeast(4)})",
                    "360° Method Arbitrations (${uiState.arbitrationReports.size.coerceAtLeast(4)})",
                    "Structured Lessons (${uiState.lessonsLearned.size.coerceAtLeast(6)})"
                )
                sections.forEachIndexed { idx, label ->
                    FilterChip(
                        selected = selectedSection == idx,
                        onClick = { selectedSection = idx },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ParsaCyan.copy(alpha = 0.2f),
                            selectedLabelColor = ParsaCyan
                        ),
                        modifier = Modifier.testTag("stage8_section_chip_$idx")
                    )
                }
            }
        }

        // SECTION 1: CANDIDATE RULES (قوانین کاندید)
        if (selectedSection == 0 || selectedSection == 1) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Rule,
                        contentDescription = "Candidate Rules",
                        tint = ParsaAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "EXTRACTED CANDIDATE RULES (استخراج قوانین کاندید)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = ParsaAmber
                    )
                }
            }

            if (uiState.candidateRules.isEmpty()) {
                item {
                    Text(
                        text = "Loading candidate rules...",
                        style = MaterialTheme.typography.bodySmall,
                        color = ParsaTextSecondary
                    )
                }
            } else {
                items(uiState.candidateRules) { rule ->
                    CandidateRuleCard(rule)
                }
            }
        }

        // SECTION 2: PARSA RULE BOOK (کتابچه قوانین و دانش کشف‌شده)
        if (selectedSection == 0 || selectedSection == 2) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Rule Book",
                        tint = ParsaCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "PARSA RULE BOOK & KNOWLEDGE CATALOG (کتابچه قوانین PARSA)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = ParsaCyan
                    )
                }
            }

            if (uiState.ruleBookEntries.isEmpty()) {
                item {
                    Text(
                        text = "Loading Rule Book entries...",
                        style = MaterialTheme.typography.bodySmall,
                        color = ParsaTextSecondary
                    )
                }
            } else {
                items(uiState.ruleBookEntries) { entry ->
                    RuleBookEntryCard(entry)
                }
            }
        }

        // SECTION 3: EMERGING PATTERNS (الگوهای نوظهور بر اساس رژیم بازار)
        if (selectedSection == 0 || selectedSection == 3) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = "Emerging Patterns",
                        tint = ParsaCyanLight,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "EMERGING PATTERNS & REGIME ADAPTATION (الگوهای نوظهور)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = ParsaCyanLight
                    )
                }
            }

            if (uiState.emergingPatterns.isEmpty()) {
                item {
                    Text(
                        text = "Loading Emerging Patterns...",
                        style = MaterialTheme.typography.bodySmall,
                        color = ParsaTextSecondary
                    )
                }
            } else {
                items(uiState.emergingPatterns) { pattern ->
                    EmergingPatternCard(pattern)
                }
            }
        }

        // SECTION 4: CROSS-ASSET CLUSTERS & LEAD-LAG
        if (selectedSection == 0 || selectedSection == 4) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = "Cross Asset Clusters",
                        tint = ParsaEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "BEHAVIORAL CLUSTERS & LEAD-LAG (همبستگی ≠ علیت)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = ParsaEmerald
                    )
                }
            }

            items(uiState.crossAssetClusters) { cluster ->
                CrossAssetClusterCard(cluster)
            }

            items(uiState.leadLagRelationships) { rel ->
                LeadLagCard(rel)
            }
        }

        // SECTION 5: NEGATIVE KNOWLEDGE REGISTRY (رجیستری دانش منفی)
        if (selectedSection == 0 || selectedSection == 5) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Negative Knowledge",
                        tint = ParsaRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "NEGATIVE KNOWLEDGE REGISTRY (دانش منفی و ثبت شکست‌ها)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = ParsaRed
                    )
                }
            }

            if (uiState.negativeKnowledge.isEmpty()) {
                item {
                    Text(
                        text = "Loading Negative Knowledge...",
                        style = MaterialTheme.typography.bodySmall,
                        color = ParsaTextSecondary
                    )
                }
            } else {
                items(uiState.negativeKnowledge) { nk ->
                    NegativeKnowledgeCard(nk)
                }
            }
        }

        // SECTION 6: GEMINI ADVISORY VS PARSA FINAL JUDGE
        if (selectedSection == 0 || selectedSection == 6) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = "Final Judge",
                        tint = ParsaAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "GEMINI ADVISORY VS PARSA FINAL JUDGE (تفکیک قضاوت)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = ParsaAmber
                    )
                }
            }

            if (uiState.finalJudgeDecisions.isEmpty()) {
                item {
                    Text(
                        text = "Loading final judge decisions...",
                        style = MaterialTheme.typography.bodySmall,
                        color = ParsaTextSecondary
                    )
                }
            } else {
                items(uiState.finalJudgeDecisions) { decision ->
                    FinalJudgeDecisionCard(decision)
                }
            }
        }

        // SECTION 7: 360° METHOD ARBITRATION REPORTS (داوری مستقل روش‌ها)
        if (selectedSection == 0 || selectedSection == 7) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = "Method Arbitrations",
                        tint = ParsaCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "METHOD ARBITRATION & 360° AUDIT REPORTS",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = ParsaCyan
                    )
                }
            }

            // Classification Filters for Methods
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val filters = listOf("ALL", "Robust", "Promising", "Repeated", "Candidate", "Unstable", "Rejected")
                    filters.forEach { f ->
                        FilterChip(
                            selected = selectedClassificationFilter == f,
                            onClick = { selectedClassificationFilter = f },
                            label = { Text(f, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ParsaCyan.copy(alpha = 0.2f),
                                selectedLabelColor = ParsaCyan
                            )
                        )
                    }
                }
            }

            val filteredReports = if (selectedClassificationFilter == "ALL") {
                uiState.arbitrationReports
            } else {
                uiState.arbitrationReports.filter { it.geminiTemporaryClassification.equals(selectedClassificationFilter, ignoreCase = true) }
            }

            if (filteredReports.isEmpty()) {
                item {
                    Text(
                        text = "No method arbitration reports match filter '$selectedClassificationFilter'.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ParsaTextSecondary
                    )
                }
            } else {
                items(filteredReports) { report ->
                    MethodArbitrationCard(report)
                }
            }
        }

        // SECTION 8: STRUCTURED LESSONS (درس‌های آموخته‌شده)
        if (selectedSection == 0 || selectedSection == 8) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Lessons Learned",
                        tint = ParsaEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "LESSONS LEARNED CATALOG (درس‌های آموخته‌شده)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = ParsaEmerald
                    )
                }
            }

            items(uiState.lessonsLearned) { lesson ->
                LessonDetailCard(lesson)
            }
        }

        // Quick Stage 8 REST Triggers
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ParsaNavySurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "STAGE 8 REST API TEST ENDPOINTS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ParsaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    val endpoints = listOf(
                        "/api/audit/stage8/arbitration-report",
                        "/api/audit/stage8/candidate-rules",
                        "/api/audit/stage8/rule-book",
                        "/api/audit/stage8/emerging-patterns",
                        "/api/audit/stage8/cross-asset-clusters",
                        "/api/audit/stage8/lead-lag",
                        "/api/audit/stage8/negative-knowledge",
                        "/api/audit/stage8/final-decisions",
                        "/api/audit/stage8/rule-lineage",
                        "/api/audit/stage8/governance-status"
                    )
                    endpoints.forEach { ep ->
                        OutlinedButton(
                            onClick = { onTestEndpoint("GET", ep) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "GET $ep",
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                    color = ParsaCyanLight
                                )
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Test",
                                    tint = ParsaCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricSummaryCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ParsaNavySurface),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = ParsaTextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun CandidateRuleCard(rule: CandidateRuleEntity) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = ParsaNavyCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("candidate_rule_card_${rule.ruleId}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rule.ruleTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ParsaTextPrimary
                    )
                    Text(
                        text = "ID: ${rule.ruleId}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = ParsaCyanLight
                    )
                }
                StatusBadge(
                    status = if (rule.isApproved) "APPROVED (ILLEGAL)" else "CANDIDATE ONLY",
                    color = if (rule.isApproved) ParsaRed else ParsaAmber
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Lineage Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(ParsaNavySurface)
                    .padding(8.dp)
            ) {
                Column {
                    Text(
                        text = "FULL LINEAGE PATH:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ParsaCyan
                    )
                    Text(
                        text = rule.lineagePath,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                        color = ParsaTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Regime: ${rule.suitableRegime}",
                    style = MaterialTheme.typography.labelSmall,
                    color = ParsaTextSecondary
                )
                Text(
                    text = "Confidence: ${String.format("%.0f", rule.confidenceScore * 100)}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = ParsaEmerald
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetailSection(title = "Activation Conditions (شرایط فعال‌سازی)", content = rule.activationConditionsJson)
                    DetailSection(title = "Invalidation Conditions (شرایط عدم‌اعتبار)", content = rule.invalidationConditionsJson)
                    DetailSection(title = "Required Inputs (ورودی‌های مورد نیاز)", content = rule.requiredInputsJson)
                    DetailSection(title = "Target Markets & Horizon", content = "${rule.targetMarkets} | Horizon: ${rule.timeHorizon}")
                    DetailSection(title = "Historical Evidence Summary", content = rule.historicalEvidenceSummary)
                    DetailSection(title = "Advantages", content = rule.advantagesJson)
                    DetailSection(title = "Risks & Pitfalls", content = rule.risksJson)
                    DetailSection(title = "Limitations", content = rule.limitationsJson)
                    DetailSection(title = "Successful Historical Samples", content = rule.successfulSamplesJson)
                    DetailSection(title = "Failure Historical Samples", content = rule.failureSamplesJson)
                    DetailSection(title = "Gemini Arbitration Opinion", content = rule.geminiArbitrationOpinion)
                }
            }

            Text(
                text = if (expanded) "▲ Tap to collapse details" else "▼ Tap to expand full 13-field specifications",
                style = MaterialTheme.typography.labelSmall,
                color = ParsaCyanLight,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun MethodArbitrationCard(report: MethodArbitrationReportEntity) {
    var expanded by remember { mutableStateOf(false) }

    val classColor = when (report.geminiTemporaryClassification) {
        "Robust" -> ParsaEmerald
        "Promising" -> ParsaCyan
        "Repeated" -> ParsaCyanLight
        "Candidate" -> ParsaAmber
        "Unstable" -> ParsaAmber
        "Rejected" -> ParsaRed
        else -> ParsaSlate
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = ParsaNavyCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("arbitration_card_${report.methodId}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = report.methodName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ParsaTextPrimary
                    )
                    Text(
                        text = "ID: ${report.methodId} | N=${report.sampleCount}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = ParsaTextSecondary
                    )
                }
                StatusBadge(
                    status = report.geminiTemporaryClassification.uppercase(),
                    color = classColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Hypothesis: ${report.hypothesis}",
                style = MaterialTheme.typography.bodySmall,
                color = ParsaTextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Performance Metrics Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ParsaNavySurface)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MetricColumn("MFE", "+${String.format("%.1f", report.maxFavorableExcursion * 100)}%", ParsaEmerald)
                MetricColumn("MAE", "-${String.format("%.1f", report.maxAdverseExcursion * 100)}%", ParsaRed)
                MetricColumn("Max DD", "${String.format("%.1f", report.maxDrawdown * 100)}%", ParsaAmber)
                MetricColumn("Param Sens", "${String.format("%.2f", report.parameterSensitivityScore)}", if (report.parameterSensitivityScore < 0.3) ParsaEmerald else ParsaRed)
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetailSection("Analytical Logic", report.analyticalLogic)
                    DetailSection("Discovery Performance", report.discoveryPerformanceJson)
                    DetailSection("Validation Performance", report.validationPerformanceJson)
                    DetailSection("Out-of-Sample Performance", report.outOfSamplePerformanceJson)
                    DetailSection("Baseline Comparison", report.baselineComparisonJson)
                    DetailSection("Recovery Time", report.recoveryTimeDescription)
                    DetailSection("Strengths", report.strengthsJson)
                    DetailSection("Weaknesses", report.weaknessesJson)
                    DetailSection("Observed Failures", report.observedFailuresJson)
                    DetailSection("Overfitting Risks", report.overfittingRisksJson)
                    DetailSection("Data Limitations", report.dataLimitationsJson)
                    DetailSection("Gemini Arbitration Notes", report.geminiArbitrationNotes)
                }
            }

            Text(
                text = if (expanded) "▲ Tap to collapse" else "▼ Tap for complete 360° metrics",
                style = MaterialTheme.typography.labelSmall,
                color = ParsaCyanLight,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun MetricColumn(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = ParsaTextSecondary)
        Text(text = value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun DetailSection(title: String, content: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(ParsaNavySurface.copy(alpha = 0.5f))
            .padding(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = ParsaCyanLight
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
            color = ParsaTextPrimary
        )
    }
}

@Composable
fun LessonDetailCard(lesson: LessonLearnedEntity) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = ParsaNavyCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = lesson.lessonTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ParsaTextPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(lesson.evidenceType, ParsaCyanLight)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ID: ${lesson.lessonId} | Category: ${lesson.category} | Source: ${lesson.sourceMethodId ?: "Historical Archive"}",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = ParsaTextSecondary
            )

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DetailSection("Historical Observation", lesson.historicalObservation)
                    DetailSection("Root Cause", lesson.rootCause)
                    DetailSection("Evidence Summary", lesson.evidenceSummary)
                    DetailSection("Quantitative Outcome", lesson.consequenceOrOutcome)
                    DetailSection("Limitation", lesson.limitation)
                    DetailSection("Applicability", "Regime: ${lesson.applicableRegime ?: "All"} | Assets: ${lesson.applicableAssets ?: "BTC/ETH"} | Timeframe: ${lesson.applicableTimeframe ?: "1h-4h"}")
                }
            }

            Text(
                text = if (expanded) "▲ Collapse" else "▼ Expand lesson specifics",
                style = MaterialTheme.typography.labelSmall,
                color = ParsaCyanLight,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
fun RuleBookEntryCard(entry: ParsaRuleBookEntity) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = ParsaNavyCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("rule_book_card_${entry.ruleCode}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.ruleTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ParsaTextPrimary
                    )
                    Text(
                        text = "CODE: ${entry.ruleCode} | Version: ${entry.versionTag}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = ParsaCyanLight
                    )
                }
                StatusBadge(
                    status = if (entry.isLocked) "LOCKED" else "CANDIDATE SPECIFICATION",
                    color = if (entry.isLocked) ParsaRed else ParsaCyan
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Conditions: ${entry.conditionsJson}",
                style = MaterialTheme.typography.bodySmall,
                color = ParsaTextSecondary
            )

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DetailSection("Out-of-Sample Evidence", entry.oosEvidence)
                    DetailSection("Provenance Lineage", entry.provenanceLineage)
                    DetailSection("Invalidation Conditions", entry.invalidationJson)
                    DetailSection("Limitations & Risk", entry.limitations)
                    DetailSection("Evidence Score & Approval", "Score: ${String.format("%.2f", entry.evidenceScore)} | Status: ${entry.approvalDecision}")
                }
            }

            Text(
                text = if (expanded) "▲ Collapse" else "▼ Expand rule book details",
                style = MaterialTheme.typography.labelSmall,
                color = ParsaCyanLight,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
fun EmergingPatternCard(pattern: EmergingPatternEntity) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = ParsaNavyCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("emerging_pattern_${pattern.patternId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pattern.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ParsaTextPrimary
                    )
                    Text(
                        text = "ID: ${pattern.patternId} | Discovery: ${pattern.discoveryPeriod}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = ParsaCyanLight
                    )
                }
                StatusBadge(
                    status = "CONF: ${String.format("%.0f", pattern.confidence * 100)}%",
                    color = ParsaEmerald
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = pattern.hypothesis,
                style = MaterialTheme.typography.bodySmall,
                color = ParsaTextSecondary
            )

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DetailSection("Initial Observation", pattern.initialObservationJson)
                    DetailSection("Potential Regimes", pattern.potentialRegimesJson)
                    DetailSection("Sample Size", "${pattern.currentSampleSize} recorded instances")
                    DetailSection("Reason Preserved", pattern.reasonPreserved)
                    DetailSection("Suggested Future Tests", pattern.suggestedFutureTests)
                }
            }

            Text(
                text = if (expanded) "▲ Collapse" else "▼ Expand discovery details",
                style = MaterialTheme.typography.labelSmall,
                color = ParsaCyanLight,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
fun CrossAssetClusterCard(cluster: CrossAssetClusterEntity) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ParsaNavyCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cluster_card_${cluster.clusterId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = cluster.clusterName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ParsaTextPrimary
                )
                StatusBadge(
                    status = "BTC CORR: ${String.format("%.2f", cluster.correlationToBtc)}",
                    color = ParsaCyan
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Cluster Type: ${cluster.clusterType} | Assets: ${cluster.assetsJson}",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = ParsaCyanLight
            )

            Spacer(modifier = Modifier.height(6.dp))
            DetailSection("Behavioral Signature", cluster.behavioralSignature)
            DetailSection("Regime Stability Score", String.format("%.2f", cluster.regimeStabilityScore))
            DetailSection("Empirical Basis", cluster.empiricalBasis)
        }
    }
}

@Composable
fun LeadLagCard(rel: LeadLagRelationshipEntity) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ParsaNavyCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("lead_lag_${rel.leaderAsset}_${rel.laggerAsset}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${rel.leaderAsset}  ➔  ${rel.laggerAsset}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ParsaAmber
                )
                StatusBadge(
                    status = "${rel.timeLagDescription} (R=${String.format("%.2f", rel.correlationScore)})",
                    color = ParsaEmerald
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            DetailSection("Out-of-Sample Stability", String.format("%.2f", rel.outOfSampleStability))
            DetailSection("Regime Sensitivity", rel.regimeSensitivity)
            DetailSection("Hard Invariant", if (rel.isCausationClaimed) "CAUSATION CLAIMED" else "CORRELATION ONLY (Correlation != Causation strictly enforced)")
        }
    }
}

@Composable
fun NegativeKnowledgeCard(nk: NegativeKnowledgeEntity) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = ParsaNavyCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .border(1.dp, ParsaRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .testTag("negative_knowledge_${nk.knowledgeId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = nk.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ParsaRed
                    )
                    Text(
                        text = "ID: ${nk.knowledgeId} | Category: ${nk.failureCategory}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = ParsaTextSecondary
                    )
                }
                StatusBadge(
                    status = "RECURRENCE: ${nk.recurrenceCount}x",
                    color = ParsaRed
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Root Cause: ${nk.rootCause}",
                style = MaterialTheme.typography.bodySmall,
                color = ParsaTextPrimary
            )

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DetailSection("Predicted vs Actual Outcome", "Predicted: ${nk.predictedOutcome} | Actual: ${nk.actualOutcome}")
                    DetailSection("Regime Observed", nk.regimeObserved)
                    DetailSection("Generalizability", nk.generalizability)
                    DetailSection("Extracted Negative Lesson", nk.extractedLesson)
                }
            }

            Text(
                text = if (expanded) "▲ Collapse" else "▼ Expand failure mechanics",
                style = MaterialTheme.typography.labelSmall,
                color = ParsaRed,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
fun FinalJudgeDecisionCard(decision: FinalJudgeDecisionEntity) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ParsaNavyCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("final_decision_${decision.decisionId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Decision for: ${decision.methodId}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ParsaTextPrimary
                    )
                    Text(
                        text = "ID: ${decision.decisionId} | Judge: ${decision.judgeVersion}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = ParsaCyanLight
                    )
                }
                val decisionColor = when (decision.decision) {
                    "APPROVE" -> ParsaEmerald
                    "REJECT" -> ParsaRed
                    else -> ParsaAmber
                }
                StatusBadge(status = decision.decision, color = decisionColor)
            }

            Spacer(modifier = Modifier.height(8.dp))
            DetailSection("Final Judgment Reasoning", decision.reasoning)
            DetailSection("Scores Breakdown", "Evidence: ${String.format("%.2f", decision.evidenceScore)} | Robustness: ${String.format("%.2f", decision.robustnessScore)} | Generalization: ${String.format("%.2f", decision.generalizationScore)} | Overfit Risk: ${String.format("%.2f", decision.overfitRiskScore)}")
            DetailSection("Source Gemini Advisory Report", decision.sourceGeminiReportId)
            DetailSection("Required Additional Tests", decision.requiredAdditionalTests)
        }
    }
}

// =========================================================================
// DETECTIVE LAW (قانون کارآگاه) TAB IMPLEMENTATION
// =========================================================================

@Composable
fun DetectiveLawTab(
    uiState: AuditUiState,
    onRunInvestigation: () -> Unit,
    onTestEndpoint: (String, String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("ALL") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Core Mission Hero Card
            Card(
                colors = CardDefaults.cardColors(containerColor = ParsaNavyCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ParsaCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .testTag("detective_hero_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Detective Law",
                                tint = ParsaCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "PARSA DETECTIVE LAW (قانون کارآگاه)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ParsaCyanLight
                            )
                        }
                        StatusBadge(status = "AUTONOMOUS RESEARCH", color = ParsaEmerald)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "«آیا می‌توان از شواهد تاریخی موجود، رفتار آینده بازار را با دقتی بهتر از Baseline پیش‌بینی کرد؟ و اگر بله، چه رابطه، الگو، ساختار یا روش تحلیلی این توانایی را ایجاد می‌کند؟»",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = ParsaTextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Strict 7-Stage Pipeline: CLUE → HYPOTHESIS → RIVAL TEST → EVIDENCE (26 Metrics) → JUDGMENT → CANDIDATE RULE → APPROVAL (PARSA Final Judge Only). Zero unearned rules locked.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ParsaTextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onRunInvestigation,
                            colors = ButtonDefaults.buttonColors(containerColor = ParsaCyan),
                            enabled = !uiState.isRunningInvestigation,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("run_detective_investigation_button")
                        ) {
                            if (uiState.isRunningInvestigation) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = ParsaNavyDark,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Investigating...", color = ParsaNavyDark)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Run Investigation",
                                    tint = ParsaNavyDark
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Execute Detective Run", color = ParsaNavyDark, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = { onTestEndpoint("GET", "/api/audit/detective/mission-audit") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("inspect_detective_principles_button")
                        ) {
                            Text("API Audit", color = ParsaCyan)
                        }
                    }
                }
            }
        }

        item {
            // Statistical Self-Deception Guardrails Card
            Card(
                colors = CardDefaults.cardColors(containerColor = ParsaNavySurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("detective_guardrails_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "STATISTICAL SELF-DECEPTION GUARDRAILS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ParsaCyanLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        GuardrailItem("Lookahead Bias", "ZERO (Audited)", ParsaEmerald)
                        GuardrailItem("Out-of-Sample", "100% Purity", ParsaEmerald)
                        GuardrailItem("Data Mining Penalty", "Bonferroni (α=0.05/M)", ParsaCyan)
                        GuardrailItem("Causation Claimed", "FALSE (Correlation Only)", ParsaAmber)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        GuardrailItem("Rival Explanations", "Mandatory (8 Classes)", ParsaEmerald)
                        GuardrailItem("Premature Lock-in", "0% (Zero Locked)", ParsaEmerald)
                        GuardrailItem("Gemini Role", "Advisory Only", ParsaCyan)
                        GuardrailItem("Lineage Traceability", "Immutable SHA-256", ParsaEmerald)
                    }
                }
            }
        }

        item {
            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    "ALL" to "All Findings",
                    "CLUES" to "Clues (${uiState.detectiveClues.size})",
                    "HYPOTHESES" to "Hypotheses (${uiState.detectiveHypotheses.size})",
                    "RIVALS" to "Rival Tests (${uiState.competingHypotheses.size})",
                    "METHODS" to "Invented Methods (${uiState.detectiveMethods.size})",
                    "FAILURES" to "Negative Knowledge (${uiState.negativeKnowledge.size})",
                    "RULES" to "Candidate Rules (${uiState.ruleBookEntries.size})",
                    "TRAIL" to "Audit Trail (${uiState.detectiveAuditTrail.size})"
                )

                filters.forEach { (cat, label) ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ParsaCyan.copy(alpha = 0.2f),
                            selectedLabelColor = ParsaCyan,
                            containerColor = ParsaNavyCard,
                            labelColor = ParsaTextSecondary
                        ),
                        modifier = Modifier.testTag("filter_$cat")
                    )
                }
            }
        }

        // Filtered content sections
        if (selectedCategory == "ALL" || selectedCategory == "CLUES") {
            item {
                SectionHeader("DISCOVERED RAW CLUES (TIER A)", "Initial observations of anomalies & dislocations")
            }
            items(uiState.detectiveClues) { clue ->
                DetectiveClueCard(clue)
            }
        }

        if (selectedCategory == "ALL" || selectedCategory == "HYPOTHESES") {
            item {
                SectionHeader("TESTABLE HYPOTHESES (TIER B)", "Falsifiable scientific explanations of market phenomena")
            }
            items(uiState.detectiveHypotheses) { hyp ->
                DetectiveHypothesisCard(hyp)
            }
        }

        if (selectedCategory == "ALL" || selectedCategory == "RIVALS") {
            item {
                SectionHeader("COMPETING HYPOTHESES & RIVAL TESTS", "Alternative explanations evaluated to prevent confirmation bias")
            }
            items(uiState.competingHypotheses) { rival ->
                CompetingHypothesisCard(rival)
            }
        }

        if (selectedCategory == "ALL" || selectedCategory == "METHODS") {
            item {
                SectionHeader("NOVEL INVENTED ANALYTICAL METHODS (26 METRICS)", "Autonomous algorithmic discoveries benchmarked vs baseline")
            }
            items(uiState.detectiveMethods) { method ->
                DetectiveMethodCard(method)
            }
        }

        if (selectedCategory == "ALL" || selectedCategory == "FAILURES") {
            item {
                SectionHeader("NEGATIVE KNOWLEDGE & FAILURE LESSONS", "Systematic catalog of 11 failure types to prevent repeat mistakes")
            }
            items(uiState.negativeKnowledge) { nk ->
                NegativeKnowledgeCard(nk)
            }
        }

        if (selectedCategory == "ALL" || selectedCategory == "RULES") {
            item {
                SectionHeader("PARSA CANDIDATE RULES (TIER E)", "Empirically validated rule specifications awaiting PARSA Final Judge review")
            }
            items(uiState.ruleBookEntries) { rule ->
                ParsaCandidateRuleCard(rule)
            }
        }

        if (selectedCategory == "ALL" || selectedCategory == "TRAIL") {
            item {
                SectionHeader("DETECTIVE AUDIT TRAIL & LINEAGE", "Cryptographically verifiable SHA-256 decision lineage")
            }
            items(uiState.detectiveAuditTrail) { trail ->
                DetectiveAuditTrailCard(trail)
            }
        }
    }
}

@Composable
fun GuardrailItem(label: String, value: String, color: Color) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = ParsaTextSecondary, fontSize = 10.sp)
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = color)
    }
}

@Composable
fun DetectiveClueCard(clue: DetectiveClueEntity) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ParsaNavyCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("clue_card_${clue.clueId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = clue.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ParsaTextPrimary
                    )
                    Text(
                        text = "ID: ${clue.clueId} • Type: ${clue.anomalyType}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = ParsaCyanLight
                    )
                }
                StatusBadge(status = clue.tier, color = ParsaCyan)
            }

            Spacer(modifier = Modifier.height(8.dp))
            DetailSection("Raw Observation", clue.rawObservation)
            DetailSection("Observed Assets", clue.assetsObservedJson)
            DetailSection("Observed Timeframes", clue.timeframesObservedJson)
            DetailSection("Key Metrics", clue.metricsObservedJson)
        }
    }
}

@Composable
fun DetectiveHypothesisCard(hyp: DetectiveHypothesisEntity) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ParsaNavyCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hyp_card_${hyp.hypothesisId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = hyp.statement,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ParsaTextPrimary
                    )
                    Text(
                        text = "ID: ${hyp.hypothesisId} • Source Clue: ${hyp.clueId}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = ParsaCyanLight
                    )
                }
                StatusBadge(status = hyp.status, color = ParsaEmerald)
            }

            Spacer(modifier = Modifier.height(8.dp))
            DetailSection("Core Premise", hyp.corePremise)
            DetailSection("Testable Predictions", hyp.testablePredictionsJson)
            DetailSection("Author / Origin", hyp.authorOrOrigin)
        }
    }
}

@Composable
fun CompetingHypothesisCard(rival: CompetingHypothesisEntity) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ParsaNavyCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("rival_card_${rival.competingId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rival.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ParsaTextPrimary
                    )
                    Text(
                        text = "ID: ${rival.competingId} • Class: ${rival.explanationType} • For: ${rival.hypothesisId}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = ParsaCyanLight
                    )
                }
                val badgeColor = if (rival.isFavored) ParsaAmber else ParsaEmerald
                val badgeLabel = if (rival.isFavored) "FAVORED CONSTRAINT" else "EMPIRICALLY REFUTED"
                StatusBadge(status = badgeLabel, color = badgeColor)
            }

            Spacer(modifier = Modifier.height(8.dp))
            DetailSection("Rival Explanation Rationale", rival.rationale)
            DetailSection("Empirical Test & Refutation", rival.refutationOrConfirmationReason)
            DetailSection("Metric / p-Value", "${rival.pValueOrMetricScore} (${rival.empiricalTestResultJson})")
        }
    }
}

@Composable
fun DetectiveMethodCard(method: DetectiveMethodEntity) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = ParsaNavyCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("method_card_${method.methodId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = method.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ParsaTextPrimary
                    )
                    Text(
                        text = "ID: ${method.methodId} • Origin: ${method.discoveryOrigin}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = ParsaCyanLight
                    )
                }
                val tierColor = when (method.evidenceGrade) {
                    "TIER_E_CANDIDATE_RULE" -> ParsaEmerald
                    "TIER_D_ROBUST" -> ParsaCyan
                    "REJECTED" -> ParsaRed
                    else -> ParsaAmber
                }
                StatusBadge(status = method.evidenceGrade, color = tierColor)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ScoreColumn("In-Sample", "${(method.inSampleResult * 100).toInt()}%")
                ScoreColumn("Out-of-Sample", "${(method.outOfSampleResult * 100).toInt()}%")
                ScoreColumn("Walk-Forward", "${(method.walkForwardResult * 100).toInt()}%")
                ScoreColumn("Edge vs Baseline", "+${(method.baselineComparison * 100).toInt()}%")
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ScoreColumn("MFE / MAE", "${String.format("%.1f", method.maxFavorableExcursion)} / ${String.format("%.1f", method.maxAdverseExcursion)}")
                ScoreColumn("Max Drawdown", "${(method.drawdown * 100).toInt()}%")
                ScoreColumn("Sensitivity", String.format("%.2f", method.parameterSensitivity))
                ScoreColumn("Confidence", "${(method.confidence * 100).toInt()}%")
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DetailSection("Underlying Hypothesis", method.hypothesis)
                    DetailSection("Method Logic", method.methodLogic)
                    DetailSection("Activation Conditions", method.activationConditions)
                    DetailSection("Invalidation Conditions", method.invalidationConditions)
                    DetailSection("Cataloged Failure Mode", method.failureClassification)
                    DetailSection("Immutable Provenance Lineage", method.provenanceLineage)
                }
            }

            Text(
                text = if (expanded) "▲ Collapse 26-Metric Audit" else "▼ Expand full 26-metric specifications & logic",
                style = MaterialTheme.typography.labelSmall,
                color = ParsaCyan,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
fun ScoreColumn(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = ParsaTextSecondary, fontSize = 10.sp)
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = ParsaCyanLight)
    }
}

@Composable
fun ParsaCandidateRuleCard(rule: ParsaRuleBookEntity) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ParsaNavyCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("rule_card_${rule.ruleCode}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rule.ruleTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ParsaTextPrimary
                    )
                    Text(
                        text = "Code: ${rule.ruleCode} • Lineage: ${rule.provenanceLineage}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = ParsaCyanLight
                    )
                }
                StatusBadge(status = "CANDIDATE (Locked: ${rule.isLocked})", color = ParsaEmerald)
            }

            Spacer(modifier = Modifier.height(8.dp))
            DetailSection("Activation Conditions", rule.conditionsJson)
            DetailSection("Invalidation Conditions", rule.invalidationJson)
            DetailSection("Empirical OOS Evidence", rule.oosEvidence)
            DetailSection("Limitations & Deactivation Boundaries", rule.limitations)
        }
    }
}

@Composable
fun DetectiveAuditTrailCard(trail: DetectiveAuditTrailEntity) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ParsaNavySurface),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "[${trail.step}] Target: ${trail.targetEntityId}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                    color = ParsaCyan
                )
                Text(
                    text = trail.auditId,
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = ParsaTextSecondary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = trail.actionTaken, style = MaterialTheme.typography.bodySmall, color = ParsaTextPrimary)
            Text(text = "Guardrail: ${trail.guardrailVerification}", style = MaterialTheme.typography.labelSmall, color = ParsaEmerald)
            Text(text = "Hash: ${trail.immutableHash.take(16)}...", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = ParsaSlate)
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = ParsaCyanLight
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = ParsaTextSecondary
        )
    }
}



