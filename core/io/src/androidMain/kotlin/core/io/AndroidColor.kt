package core.io

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorSpace
import android.graphics.Paint
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap

private val sRgbColorSpace: ColorSpace by lazy { ColorSpace.get(ColorSpace.Named.SRGB) }

actual fun platformToSRGB(bitmap: ImageBitmap): ImageBitmap {
    val androidBitmap = bitmap.asAndroidBitmap()
    if (Build.VERSION.SDK_INT < 26) {
        return bitmap
    }
    val colorSpace = androidBitmap.colorSpace
    if (colorSpace == null || colorSpace.isSrgb) {
        return bitmap
    }
    val converted = Bitmap.createBitmap(
        androidBitmap.width,
        androidBitmap.height,
        Bitmap.Config.ARGB_8888,
        androidBitmap.hasAlpha(),
        sRgbColorSpace
    )
    val canvas = Canvas(converted)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }
    canvas.drawBitmap(androidBitmap, 0f, 0f, paint)
    return converted.asImageBitmap()
}

actual fun platformScaleBitmap(bitmap: ImageBitmap, width: Int, height: Int): ImageBitmap {
    val androidBitmap = bitmap.asAndroidBitmap()
    if (androidBitmap.width == width && androidBitmap.height == height) return bitmap
    val scaled = Bitmap.createScaledBitmap(androidBitmap, width, height, true)
    return scaled.asImageBitmap()
}
