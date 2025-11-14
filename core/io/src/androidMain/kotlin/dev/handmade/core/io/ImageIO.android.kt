package dev.handmade.core.io

import android.graphics.Bitmap
import android.graphics.BitmapFactory

actual class NativeImage internal constructor(internal val bitmap: Bitmap)

actual fun load(path: String): NativeImage {
    // S1 skeleton: decode file to Bitmap. Add real error handling later.
    val bmp = BitmapFactory.decodeFile(path) ?: error("Failed to decode image at $path")
    return NativeImage(bmp)
}

actual fun exifRotate(img: NativeImage): NativeImage {
    // TODO: read EXIF and rotate if needed (S1 placeholder)
    return img
}

actual fun toSRGB(img: NativeImage): NativeImage {
    // TODO: ensure sRGB color space (S1 placeholder)
    return img
}

actual fun makePreview(img: NativeImage, maxSide: Int): NativeImage {
    val w = img.bitmap.width
    val h = img.bitmap.height
    if (w <= 0 || h <= 0) return img

    val scale = if (w >= h) maxSide.toFloat() / w.toFloat() else maxSide.toFloat() / h.toFloat()
    if (scale >= 1f) return img
    val nw = (w * scale).toInt().coerceAtLeast(1)
    val nh = (h * scale).toInt().coerceAtLeast(1)
    val preview = Bitmap.createScaledBitmap(img.bitmap, nw, nh, true)
    return NativeImage(preview)
}
