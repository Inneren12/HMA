package core.io

import androidx.compose.ui.graphics.ImageBitmap

sealed class ImageSource {
    data class Bytes(val data: ByteArray, val fileNameHint: String? = null) : ImageSource()
    data class FilePath(val path: String) : ImageSource()
    data class UriString(val uri: String) : ImageSource()
}

interface ImageRef {
    suspend fun decode(): ImageBitmap
    val width: Int
    val height: Int
    val byteSizeEstimate: Long?
}

data class SourceImage(
    val width: Int,
    val height: Int,
    val preview: ImageBitmap,
    val fullRef: ImageRef,
    val exifOrientation: Int?
)

data class LoadOptions(
    val previewLongSidePx: Int = 1024,
    val preserveExif: Boolean = false
)

sealed class ImageIoError(message: String? = null, cause: Throwable? = null) : Throwable(message, cause) {
    class UnsupportedFormat(msg: String = "Unsupported format") : ImageIoError(msg)
    class CorruptData(msg: String = "Corrupt or unreadable data") : ImageIoError(msg)
    class TooLarge(msg: String = "Image too large to decode safely") : ImageIoError(msg)
    class IoFailure(msg: String = "I/O failure", cause: Throwable? = null) : ImageIoError(msg, cause)
    class PlatformMissingCodec(msg: String = "Codec not available on this platform") : ImageIoError(msg)
    class DecodeFailed(msg: String = "Decoder failed", cause: Throwable? = null) : ImageIoError(msg, cause)
}
