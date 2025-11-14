package dev.handmade.core.domain

// --- Basic enums / types ---
enum class SceneType { PHOTO_CONTINUOUS, PIXEL_ART, DISCRETE_LINEART }
enum class CraftType { CROSS_STITCH /* DIAMOND_PAINTING, BEADS (future) */ }

enum class Branch { REALISTIC, PIXEL_PIPE, DISCRETE_DIRECT }

enum class DitheringMode { NONE, ORDERED_LOW, ORDERED_MID, FS_LOW }
enum class QuantAlgo { KMEANS_PP, MEDIAN_CUT, SPATIAL_KM }

enum class SimplifyPreset { DETAIL, SIMPLIFIED }

// Plain placeholder for an image handle in core domain (no platform dependency)
@JvmInline
value class ImageRef(val id: String)

data class SourceImage(
    val width: Int,
    val height: Int,
    val previewRef: ImageRef,
    val fullRef: ImageRef,
    val exifOrientation: Int? = null
)

data class PatternSpec(
    val targetWidthStitches: Int? = null,
    val targetHeightStitches: Int? = null,
    val fabricCount: Int? = null,           // e.g., 14/16/18 ct
    val physicalWidthCm: Double? = null,
    val maxColors: Int? = null,
    val dithering: DitheringMode? = null
)

data class ProcessingPlan(
    val sceneType: SceneType,
    val branch: Branch,
    val gridW: Int,
    val gridH: Int,
    val paletteLimit: Int,
    val quantAlgo: QuantAlgo,
    val dithering: DitheringMode,
    val simplify: SimplifyPreset,
    val craft: CraftType = CraftType.CROSS_STITCH
)

data class UserOverrides(
    val sceneType: SceneType? = null,
    val branch: Branch? = null,
    val gridW: Int? = null,
    val gridH: Int? = null,
    val paletteLimit: Int? = null,
    val dithering: DitheringMode? = null,
    val simplify: SimplifyPreset? = null
)

// --- Analyze & Masks placeholders ---
data class AnalyzeResult(
    val edgeDensity: Double = 0.0,
    val colorDiversity: Double = 0.0,
    val pixelationScore: Double = 0.0,
    val entropyScore: Double = 0.0
)

data class MaskSet(
    val edgeMaskPresent: Boolean = false,
    val flatMaskPresent: Boolean = false,
    val textureMaskPresent: Boolean = false
)

// --- Quality evaluation contracts ---
/**
 * Components of quality for Auto decisions.
 * Q in [0,1]; higher is better.
 */
data class QualityReport(
    val qColor: Double,
    val qEdge: Double,
    val qIslands: Double,
    val qTotal: Double,
    val details: Map<String, String> = emptyMap()
)

interface QualityEvaluator {
    /**
     * Evaluate quality of a generated pattern against the preprocessed source (not full-res).
     */
    fun evaluate(plan: ProcessingPlan, analyze: AnalyzeResult): QualityReport
}

// --- Pattern outputs (placeholders) ---
@JvmInline
value class ThreadColorId(val value: Int)

data class PatternCell(val threadColorId: ThreadColorId)

data class PatternGrid(
    val width: Int,
    val height: Int,
    val cells: List<PatternCell> // row-major
)
