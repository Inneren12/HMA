package core.io

import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.exifinterface.media.ExifInterface

internal fun readExifOrientation(source: ImageSource): Int? = when (source) {
    is ImageSource.Bytes -> source.data.inputStream().use { stream ->
        runCatching { ExifInterface(stream).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED) }
            .getOrNull()?.normalizedOrientation()
    }
    is ImageSource.FilePath -> runCatching { ExifInterface(source.path) }.getOrNull()
        ?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
        ?.normalizedOrientation()
    is ImageSource.UriString -> {
        val context = AndroidImageIoContext.requireContext()
        val uri = Uri.parse(source.uri)
        val resolver = context.contentResolver
        resolver.openInputStream(uri)?.use { stream ->
            runCatching { ExifInterface(stream).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED) }
                .getOrNull()
                ?.normalizedOrientation()
        }
    }
}

private fun Int.normalizedOrientation(): Int? {
    return when (this) {
        ExifInterface.ORIENTATION_UNDEFINED -> null
        ExifInterface.ORIENTATION_NORMAL -> 1
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> 2
        ExifInterface.ORIENTATION_ROTATE_180 -> 3
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> 4
        ExifInterface.ORIENTATION_TRANSPOSE -> 5
        ExifInterface.ORIENTATION_ROTATE_90 -> 6
        ExifInterface.ORIENTATION_TRANSVERSE -> 7
        ExifInterface.ORIENTATION_ROTATE_270 -> 8
        else -> null
    }
}

internal fun applyExifToBitmapIfNeeded(bitmap: Bitmap, exifOrientation: Int?): Bitmap {
    val orientation = exifOrientation ?: return bitmap
    if (orientation == 1) return bitmap
    val matrix = Matrix()
    when (exifOrientationToTransform(orientation)) {
        ExifOrientationTransform.NONE -> return bitmap
        ExifOrientationTransform.MIRROR_X -> matrix.setScale(-1f, 1f)
        ExifOrientationTransform.ROTATE_180 -> matrix.setRotate(180f)
        ExifOrientationTransform.MIRROR_Y -> matrix.setScale(1f, -1f)
        ExifOrientationTransform.TRANSPOSE -> {
            matrix.setRotate(90f)
            matrix.postScale(-1f, 1f)
        }
        ExifOrientationTransform.ROTATE_90 -> matrix.setRotate(90f)
        ExifOrientationTransform.TRANSVERSE -> {
            matrix.setRotate(-90f)
            matrix.postScale(-1f, 1f)
        }
        ExifOrientationTransform.ROTATE_270 -> matrix.setRotate(-90f)
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

actual fun platformApplyExif(bitmap: ImageBitmap, exifOrientation: Int): ImageBitmap {
    val androidBitmap = bitmap.asAndroidBitmap()
    val transformed = applyExifToBitmapIfNeeded(androidBitmap, exifOrientation)
    return if (transformed === androidBitmap) bitmap else transformed.asImageBitmap()
}
