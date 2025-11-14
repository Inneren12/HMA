package core.io

import androidx.compose.ui.graphics.ImageBitmap

data class DecodedHead(
    val width: Int,
    val height: Int,
    val exifOrientation: Int?,
    val hasColorProfile: Boolean
)

data class DecodedBitmap(
    val bitmap: ImageBitmap,
    val width: Int,
    val height: Int,
    val colorSpaceIsSRGB: Boolean
)

expect suspend fun platformDecodeHead(source: ImageSource): DecodedHead
expect suspend fun platformDecodePreview(source: ImageSource, longSidePx: Int, forceSRGB: Boolean): DecodedBitmap
expect suspend fun platformDecodeFull(source: ImageSource, forceSRGB: Boolean): DecodedBitmap
expect fun platformApplyExif(bitmap: ImageBitmap, exifOrientation: Int): ImageBitmap
expect fun platformToSRGB(bitmap: ImageBitmap): ImageBitmap
expect fun platformEstimateByteSize(source: ImageSource): Long?
expect fun platformScaleBitmap(bitmap: ImageBitmap, width: Int, height: Int): ImageBitmap
