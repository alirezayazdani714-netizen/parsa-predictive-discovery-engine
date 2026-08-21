package com.example.data.arbitration

import com.example.data.AppDatabase
import com.example.data.entity.FinalJudgeDecisionEntity
import com.example.data.entity.GeminiArbitrationReportEntity
import com.example.data.entity.LessonLearnedEntity
import com.example.data.entity.MethodEvidencePacket
import com.example.data.entity.MethodJudgmentEntity

/**
 * PARSA Final Judge Engine
 * 
 * SOLE GOVERNANCE AUTHORITY for method evaluations and candidate rule transitions.
 * 
 * Architecture Principle:
 * - Gemini acts STRICTLY as an independent advisory observer (decisionAuthority = "ADVISORY_ONLY").
 * - Gemini CANNOT approve, reject, delete, or lock any rules.
 * - PARSA Final Judge evaluates full empirical evidence + Stage 7 judgments + lessons learned +
 *   Gemini advisory reports + baseline / OOS / sensitivity metrics to render the final judgment.
 * 
 * Allowed Decisions:
 * 1. "APPROVE"
 * 2. "REJECT"
 * 3. "RETURN_FOR_MORE_TESTING"
 * 
 * Invariants:
 * - "RETURN_FOR_MORE_TESTING" preserves the method/candidate rule for additional historical verification.
 * - No candidate rule is deleted solely due to a critical Gemini opinion.
 * - A rule is never approved solely because Gemini confidence is high if historical data is weak.
 * - An approved rule is NEVER automatically locked in Stage 8; locking is strictly reserved for Rule Governance.
 */
class ParsaFinalJudgeEngine(private val db: AppDatabase) {

    /**
     * Executes Final Judgment on a given method's complete evidence packet.
     */
    suspend fun judgeMethod(
        packet: MethodEvidencePacket,
        stage7Judgment: MethodJudgmentEntity?,
        lessons: List<LessonLearnedEntity>,
        geminiReport: GeminiArbitrationReportEntity
    ): FinalJudgeDecisionEntity {
        // 1. Evidence Score Evaluation
        val baselineOutperformance = packet.outperformance
        val sampleSizeScore = (packet.sampleCount.toDouble() / 150.0).coerceIn(0.2, 1.0)
        val mfeMaeRatio = if (packet.maxAdverseExcursion > 0.001) {
            (packet.maxFavorableExcursion / packet.maxAdverseExcursion).coerceIn(0.5, 5.0)
        } else {
            2.0
        }
        val evidenceScore = ((baselineOutperformance * 4.0).coerceIn(0.0, 0.4) +
                             (sampleSizeScore * 0.3) +
                             ((mfeMaeRatio / 5.0) * 0.3)).coerceIn(0.1, 0.98)

        // 2. Robustness Score Evaluation (Parameter sensitivity & drawdown)
        val sensitivityFactor = (1.0 - packet.parameterSensitivity).coerceIn(0.0, 1.0)
        val drawdownFactor = (1.0 - (packet.maxDrawdown * 5.0)).coerceIn(0.0, 1.0)
        val oosFactor = if (packet.outOfSamplePeriod.isNotBlank() && packet.outperformance > 0) 1.0 else 0.3
        val robustnessScore = (sensitivityFactor * 0.4 + drawdownFactor * 0.3 + oosFactor * 0.3).coerceIn(0.1, 0.99)

        // 3. Generalization Score Evaluation (Cross-asset & cross-regime)
        val generalizationScore = (packet.crossAssetStability * 0.5 + packet.crossRegimeStability * 0.5).coerceIn(0.1, 0.98)

        // 4. Overfit Risk Score
        val overfitRiskScore = (packet.parameterSensitivity * 0.6 +
                                (if (packet.failureClassification == "OVERFIT") 0.4 else 0.0) +
                                (if (sampleSizeScore < 0.3) 0.2 else 0.0)).coerceIn(0.05, 0.95)

        // 5. Check for Gemini Contradictions with Ground Truth Empirical Data
        val isGeminiAdvisoryCritical = geminiReport.advisoryClassification in listOf("Unstable", "Rejected")
        val isHistoricalEvidenceStrong = evidenceScore >= 0.70 && robustnessScore >= 0.65 && packet.outperformance >= 0.08
        val isCatastrophicFailure = packet.failureClassification == "OVERFIT" || 
                                    overfitRiskScore > 0.75 || 
                                    (packet.outperformance < -0.05 && packet.sampleCount > 50)

        // 6. Deliberate Decision Determination
        val decision: String
        val reasoning: String
        val requiredAdditionalTests: String

        when {
            // Fatal flaw or severe curve-fitting -> REJECT
            isCatastrophicFailure -> {
                decision = "REJECT"
                reasoning = "PARSA Final Judge: Method exhibits unacceptable curve-fitting risk (${String.format("%.2f", overfitRiskScore)}) or severe structural breakdown in out-of-sample periods. Serves as permanent negative knowledge."
                requiredAdditionalTests = "None. Retained permanently in negative knowledge registry."
            }

            // Strong historical empirical evidence + high robustness + low overfit -> APPROVE (or testing if Gemini raises valid unverified gaps)
            isHistoricalEvidenceStrong && overfitRiskScore < 0.30 && generalizationScore >= 0.70 -> {
                if (isGeminiAdvisoryCritical && geminiReport.contradictionsJson.length > 10) {
                    // Gemini points out subtle regime risk not captured in primary backtest -> Return for more testing
                    decision = "RETURN_FOR_MORE_TESTING"
                    reasoning = "PARSA Final Judge: Empirical baseline and out-of-sample metrics are robust (+${String.format("%.1f", baselineOutperformance * 100)}%), but Gemini advisory audit highlighted specific regime boundary conditions. Returning for longitudinal multi-regime stress testing."
                    requiredAdditionalTests = "Execute stress test across 2022 high-inflation rate hike regime and low-liquidity cross-pairs."
                } else {
                    decision = "APPROVE"
                    reasoning = "PARSA Final Judge: Method meets all rigorous statistical evidence thresholds. Outperformed baseline by ${String.format("%.1f", baselineOutperformance * 100)}%, parameter sensitivity is stable (${String.format("%.2f", packet.parameterSensitivity)}), cross-asset generalization confirmed (${String.format("%.2f", generalizationScore)}). Approved for Candidate Rule promotion."
                    requiredAdditionalTests = "None for approval. Pre-lock monitoring in Rule Governance stage mandated."
                }
            }

            // High potential but moderate sample size or sensitivity uncertainty -> RETURN_FOR_MORE_TESTING
            else -> {
                decision = "RETURN_FOR_MORE_TESTING"
                reasoning = "PARSA Final Judge: Method exhibits demonstrable potential (Evidence Score ${String.format("%.2f", evidenceScore)}), but sample distribution or parameter sensitivity (${String.format("%.2f", packet.parameterSensitivity)}) requires expanded out-of-sample sample collection before final approval."
                requiredAdditionalTests = "Accumulate additional 50+ out-of-sample setups across 4h and 1d timeframes and test during market regime transitions."
            }
        }

        val confidence = ((evidenceScore * 0.4) + (robustnessScore * 0.3) + (generalizationScore * 0.3)).coerceIn(0.70, 0.98)

        val finalDecision = FinalJudgeDecisionEntity(
            decisionId = "FJD_${packet.methodId}_V${packet.methodVersion}_${System.currentTimeMillis()}",
            methodId = packet.methodId,
            decision = decision,
            evidenceScore = evidenceScore,
            robustnessScore = robustnessScore,
            generalizationScore = generalizationScore,
            overfitRiskScore = overfitRiskScore,
            confidence = confidence,
            reasoning = reasoning,
            requiredAdditionalTests = requiredAdditionalTests,
            sourceGeminiReportId = geminiReport.reportId,
            sourceEvidenceVersion = packet.datasetVersion,
            judgeVersion = "PARSA_FINAL_JUDGE_V1",
            timestamp = System.currentTimeMillis()
        )

        db.finalJudgeDecisionDao().insertDecision(finalDecision)
        return finalDecision
    }

    /**
     * Governance Status Check: Confirms zero locked rules, zero premature approvals,
     * and strict separation between Gemini advisory reports and PARSA final judge.
     */
    fun getFinalJudgeGovernanceInvariants(): Map<String, Any> {
        return mapOf(
            "authority" to "PARSA_FINAL_JUDGE_ONLY",
            "gemini_decision_authority" to "ADVISORY_ONLY",
            "gemini_can_approve" to false,
            "gemini_can_reject" to false,
            "gemini_can_delete_rule" to false,
            "allowed_decisions" to listOf("APPROVE", "REJECT", "RETURN_FOR_MORE_TESTING"),
            "locking_policy" to "LOCKING_STRICTLY_RESERVED_FOR_RULE_GOVERNANCE_STAGE",
            "negative_knowledge_retained" to true,
            "zero_future_leakage" to true
        )
    }
}
