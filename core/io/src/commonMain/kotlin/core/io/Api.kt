package core.io

import androidx.compose.ui.graphics.ImageBitmap
import kotlin.math.max

suspend fun loadImage(source: ImageSource, options: LoadOptions = LoadOptions()): SourceImage {
    val head = platformDecodeHead(source)
    val previewDecoded = platformDecodePreview(source, options.previewLongSidePx, forceSRGB = true)
    val previewBitmap = ensureSRGB(previewDecoded)
    val orientedSize = applyOrientationToSize(head.width, head.height, head.exifOrientation)
    val previewFinal = enforcePreviewBounds(previewBitmap, options.previewLongSidePx)
    val imageRef = DefaultImageRef(
        source = source,
        width = orientedSize.first,
        height = orientedSize.second,
        byteSizeEstimate = platformEstimateByteSize(source)
    )
    return SourceImage(
        width = imageRef.width,
        height = imageRef.height,
        preview = previewFinal,
        fullRef = imageRef,
        exifOrientation = head.exifOrientation
    )
}

fun toSRGB(bitmap: ImageBitmap): ImageBitmap = platformToSRGB(bitmap)

fun makePreview(bitmap: ImageBitmap, maxLongSide: Int = 1024): ImageBitmap {
    if (maxLongSide <= 0) return bitmap
    val target = fitInto(bitmap.width, bitmap.height, maxLongSide)
    if (target.first == bitmap.width && target.second == bitmap.height) return bitmap
    return platformScaleBitmap(bitmap, target.first, target.second)
}

fun fitInto(width: Int, height: Int, maxLongSide: Int): Pair<Int, Int> {
    if (width <= 0 || height <= 0) return 0 to 0
    if (maxLongSide <= 0) return width to height
    val currentLong = max(width, height)
    if (currentLong <= maxLongSide) return width to height
    val scale = maxLongSide.toDouble() / currentLong.toDouble()
    val scaledWidth = max(1, (width * scale).toInt())
    val scaledHeight = max(1, (height * scale).toInt())
    return scaledWidth to scaledHeight
}

internal fun applyOrientationToSize(width: Int, height: Int, orientation: Int?): Pair<Int, Int> {
    if (orientation == null || orientation == 1) return width to height
    return if (orientationRequiresAxisSwap(orientation)) {
        height to width
    } else {
        width to height
    }
}

internal fun orientationRequiresAxisSwap(orientation: Int): Boolean {
    return when (orientation)
    {
        5, 6, 7, 8 -> true
        else -> false
    }
}

internal enum class ExifOrientationTransform {
    NONE,
    MIRROR_X,
    ROTATE_180,
    MIRROR_Y,
    TRANSPOSE,
    ROTATE_90,
    TRANSVERSE,
    ROTATE_270
}

internal fun exifOrientationToTransform(orientation: Int): ExifOrientationTransform = when (orientation) {
    2 -> ExifOrientationTransform.MIRROR_X
    3 -> ExifOrientationTransform.ROTATE_180
    4 -> ExifOrientationTransform.MIRROR_Y
    5 -> ExifOrientationTransform.TRANSPOSE
    6 -> ExifOrientationTransform.ROTATE_90
    7 -> ExifOrientationTransform.TRANSVERSE
    8 -> ExifOrientationTransform.ROTATE_270
    else -> ExifOrientationTransform.NONE
}

private fun ensureSRGB(decoded: DecodedBitmap): ImageBitmap {
    return if (decoded.colorSpaceIsSRGB) {
        decoded.bitmap
    } else {
        platformToSRGB(decoded.bitmap)
    }
}

private fun enforcePreviewBounds(preview: ImageBitmap, maxLongSide: Int): ImageBitmap {
    if (maxLongSide <= 0) return preview
    val currentLong = max(preview.width, preview.height)
    if (currentLong <= maxLongSide) return preview
    val target = fitInto(preview.width, preview.height, maxLongSide)
    return platformScaleBitmap(preview, target.first, target.second)
}

private class DefaultImageRef(
    private val source: ImageSource,
    override val width: Int,
    override val height: Int,
    override val byteSizeEstimate: Long?
) : ImageRef {
    override suspend fun decode(): ImageBitmap {
        val decoded = platformDecodeFull(source, forceSRGB = true)
        val bitmap = ensureSRGB(decoded)
        return bitmap
    }
}
