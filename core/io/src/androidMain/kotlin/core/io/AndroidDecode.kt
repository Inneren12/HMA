package core.io

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorSpace
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val sRgbColorSpace: ColorSpace by lazy { ColorSpace.get(ColorSpace.Named.SRGB) }

actual suspend fun platformDecodeHead(source: ImageSource): DecodedHead = withContext(Dispatchers.IO) {
    val bounds = try {
        decodeBounds(source)
    } catch (io: IOException) {
        throw ImageIoError.IoFailure(cause = io)
    }
    val exif = readExifOrientation(source)
    DecodedHead(
        width = bounds.width,
        height = bounds.height,
        exifOrientation = exif,
        hasColorProfile = bounds.hasColorProfile
    )
}

actual suspend fun platformDecodePreview(
    source: ImageSource,
    longSidePx: Int,
    forceSRGB: Boolean
): DecodedBitmap = withContext(Dispatchers.IO) {
    val context = AndroidImageIoContext.requireContext()
    val bounds = try {
        decodeBounds(source)
    } catch (io: IOException) {
        throw ImageIoError.IoFailure(cause = io)
    }
    val orientation = readExifOrientation(source)
    val targetSize = if (longSidePx <= 0) {
        bounds.width to bounds.height
    } else {
        fitInto(bounds.width, bounds.height, longSidePx)
    }
    val bitmap = decodeBitmap(source, context, bounds, targetSize, forceSRGB, orientation, preview = true)
    DecodedBitmap(
        bitmap = bitmap.asImageBitmap(),
        width = bitmap.width,
        height = bitmap.height,
        colorSpaceIsSRGB = bitmap.isInSrgb()
    )
}

actual suspend fun platformDecodeFull(source: ImageSource, forceSRGB: Boolean): DecodedBitmap = withContext(Dispatchers.IO) {
    val context = AndroidImageIoContext.requireContext()
    val bounds = try {
        decodeBounds(source)
    } catch (io: IOException) {
        throw ImageIoError.IoFailure(cause = io)
    }
    val orientation = readExifOrientation(source)
    val bitmap = decodeBitmap(source, context, bounds, bounds.width to bounds.height, forceSRGB, orientation, preview = false)
    DecodedBitmap(
        bitmap = bitmap.asImageBitmap(),
        width = bitmap.width,
        height = bitmap.height,
        colorSpaceIsSRGB = bitmap.isInSrgb()
    )
}

actual fun platformEstimateByteSize(source: ImageSource): Long? = when (source) {
    is ImageSource.Bytes -> source.data.size.toLong()
    is ImageSource.FilePath -> runCatching { File(source.path).length() }.getOrNull()
    is ImageSource.UriString -> {
        val context = runCatching { AndroidImageIoContext.requireContext() }.getOrNull() ?: return null
        val uri = Uri.parse(source.uri)
        when (uri.scheme) {
            null, "file" -> uri.path?.let { runCatching { File(it).length() }.getOrNull() }
            "content" -> context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length }
            else -> null
        }
    }
}

private data class BoundsInfo(val width: Int, val height: Int, val hasColorProfile: Boolean)

@Throws(IOException::class)
private fun decodeBounds(source: ImageSource): BoundsInfo {
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    when (source) {
        is ImageSource.Bytes -> BitmapFactory.decodeByteArray(source.data, 0, source.data.size, options)
        is ImageSource.FilePath -> FileInputStream(source.path).use { BitmapFactory.decodeStream(it, null, options) }
        is ImageSource.UriString -> {
            val context = AndroidImageIoContext.requireContext()
            context.contentResolver.openInputStream(Uri.parse(source.uri))?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            } ?: throw ImageIoError.IoFailure("Unable to open URI ${source.uri}")
        }
    }
    if (options.outWidth <= 0 || options.outHeight <= 0) {
        throw ImageIoError.UnsupportedFormat()
    }
    val hasProfile = if (Build.VERSION.SDK_INT >= 26) {
        val colorSpace = options.outColorSpace
        colorSpace != null && colorSpace != sRgbColorSpace
    } else {
        false
    }
    return BoundsInfo(options.outWidth, options.outHeight, hasProfile)
}

private fun decodeBitmap(
    source: ImageSource,
    context: Context,
    bounds: BoundsInfo,
    targetSize: Pair<Int, Int>,
    forceSRGB: Boolean,
    orientation: Int?,
    preview: Boolean
): Bitmap {
    ensureHeicSupport(source)
    return try {
        val decoded = if (Build.VERSION.SDK_INT >= 28) {
            decodeWithImageDecoder(source, context, targetSize, forceSRGB)
        } else {
            decodeWithBitmapFactory(source, bounds, targetSize, forceSRGB, preview)
        }
        val scaled = if (preview) maybeScale(decoded, targetSize) else decoded
        if (Build.VERSION.SDK_INT >= 28) {
            scaled
        } else {
            applyExifToBitmapIfNeeded(scaled, orientation)
        }
    } catch (oom: OutOfMemoryError) {
        throw ImageIoError.TooLarge()
    } catch (iae: IllegalArgumentException) {
        throw ImageIoError.UnsupportedFormat()
    } catch (io: IOException) {
        throw ImageIoError.IoFailure(cause = io)
    }
}

private fun ensureHeicSupport(source: ImageSource) {
    if (Build.VERSION.SDK_INT >= 28) return
    val name = when (source) {
        is ImageSource.Bytes -> source.fileNameHint
        is ImageSource.FilePath -> source.path
        is ImageSource.UriString -> Uri.parse(source.uri).path
    }?.lowercase()
    if (name != null && (name.endsWith(".heic") || name.endsWith(".heif"))) {
        throw ImageIoError.PlatformMissingCodec()
    }
}

@Throws(IOException::class)
private fun decodeWithBitmapFactory(
    source: ImageSource,
    bounds: BoundsInfo,
    targetSize: Pair<Int, Int>,
    forceSRGB: Boolean,
    preview: Boolean
): Bitmap {
    val options = BitmapFactory.Options().apply {
        inPreferredConfig = Bitmap.Config.ARGB_8888
        inMutable = false
        if (preview) {
            val sample = computeInSampleSize(bounds.width, bounds.height, targetSize.first, targetSize.second)
            if (sample > 1) inSampleSize = sample
        }
        if (Build.VERSION.SDK_INT >= 26 && forceSRGB) {
            inPreferredColorSpace = sRgbColorSpace
        }
    }
    val bitmap = when (source) {
        is ImageSource.Bytes -> BitmapFactory.decodeByteArray(source.data, 0, source.data.size, options)
        is ImageSource.FilePath -> FileInputStream(source.path).use { BitmapFactory.decodeStream(it, null, options) }
        is ImageSource.UriString -> {
            val context = AndroidImageIoContext.requireContext()
            context.contentResolver.openInputStream(Uri.parse(source.uri))?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }
        }
    } ?: throw ImageIoError.DecodeFailed()
    return bitmap
}

private fun computeInSampleSize(
    srcWidth: Int,
    srcHeight: Int,
    targetWidth: Int,
    targetHeight: Int
): Int {
    var inSampleSize = 1
    if (targetWidth <= 0 || targetHeight <= 0) return inSampleSize
    var halfWidth = srcWidth / 2
    var halfHeight = srcHeight / 2
    while (halfWidth / inSampleSize >= targetWidth && halfHeight / inSampleSize >= targetHeight) {
        inSampleSize *= 2
    }
    return inSampleSize
}

@android.annotation.SuppressLint("ObsoleteSdkInt")
private fun decodeWithImageDecoder(
    source: ImageSource,
    context: Context,
    targetSize: Pair<Int, Int>,
    forceSRGB: Boolean
): Bitmap {
    val decoderSource = when (source) {
        is ImageSource.Bytes -> ImageDecoder.createSource(ByteBuffer.wrap(source.data))
        is ImageSource.FilePath -> ImageDecoder.createSource(File(source.path))
        is ImageSource.UriString -> {
            val uri = Uri.parse(source.uri)
            if (uri.scheme == null || uri.scheme == "file") {
                ImageDecoder.createSource(File(uri.path!!))
            } else {
                ImageDecoder.createSource(context.contentResolver, uri)
            }
        }
    }
    return ImageDecoder.decodeBitmap(decoderSource) { decoder, info, _ ->
        val (targetWidth, targetHeight) = targetSize
        if (targetWidth > 0 && targetHeight > 0 && (targetWidth != info.size.width || targetHeight != info.size.height)) {
            decoder.setTargetSize(targetWidth, targetHeight)
        }
        if (forceSRGB) {
            decoder.setTargetColorSpace(sRgbColorSpace)
        }
        decoder.isMutableRequired = false
    }
}

private fun maybeScale(bitmap: Bitmap, targetSize: Pair<Int, Int>): Bitmap {
    val (targetWidth, targetHeight) = targetSize
    if (targetWidth <= 0 || targetHeight <= 0) return bitmap
    if (bitmap.width == targetWidth && bitmap.height == targetHeight) return bitmap
    return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
}

private fun Bitmap.isInSrgb(): Boolean = if (Build.VERSION.SDK_INT >= 26) {
    colorSpace?.isSrgb == true
} else {
    true
}
