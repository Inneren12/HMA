package dev.handmade.core.decision

import dev.handmade.core.domain.*

// --- Gate contracts ---
interface Gate {
    fun apply(input: EngineInput): EngineOutput
}

data class EngineInput(
    val analyze: AnalyzeResult,
    val masks: MaskSet,
    val craftType: CraftType,
    val userOverrides: UserOverrides? = null
)

data class EngineOutput(
    val candidatePlans: List<ProcessingPlan> = emptyList()
)

// --- ML Advisor contract (plug later) ---
interface MlPlanAdvisor {
    fun suggestPlans(context: PlanContext): List<ScoredPlan>
}

data class PlanContext(
    val analyze: AnalyzeResult,
    val masks: MaskSet,
    val craftType: CraftType,
    val userOverrides: UserOverrides?,
    val basePlans: List<ProcessingPlan>
)

data class ScoredPlan(
    val plan: ProcessingPlan,
    val score: Double,
    val reason: String? = null
)

// --- DecisionEngine facade ---
interface DecisionEngine {
    fun buildPlan(input: EngineInput): ProcessingPlan
}

// Empty stub implementation for S0/S1
class DecisionEngineImpl(
    private val gates: List<Gate>,
    private val mlAdvisor: MlPlanAdvisor? = null
) : DecisionEngine {

    override fun buildPlan(input: EngineInput): ProcessingPlan {
        // For now, return a hardcoded placeholder. Real logic will be added in S3+.
        val basePlan = ProcessingPlan(
            sceneType = SceneType.PHOTO_CONTINUOUS,
            branch = Branch.REALISTIC,
            gridW = 240, gridH = 160,
            paletteLimit = 32,
            quantAlgo = QuantAlgo.KMEANS_PP,
            dithering = DitheringMode.ORDERED_LOW,
            simplify = SimplifyPreset.SIMPLIFIED,
            craft = CraftType.CROSS_STITCH
        )
        return mlAdvisor
            ?.suggestPlans(PlanContext(input.analyze, input.masks, input.craftType, input.userOverrides, listOf(basePlan)))
            ?.maxByOrNull { it.score }
            ?.plan
            ?: basePlan
    }
}
