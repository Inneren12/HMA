package core.io

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.test.core.app.ApplicationProvider
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class ImageIoAndroidTest {
    private lateinit var context: Context

    @BeforeTest
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        initializeImageIo(context)
    }

    @Test
    fun loadImageFromFileAppliesOrientation() = runBlocking {
        val file = createJpeg(1200, 800, ExifInterface.ORIENTATION_ROTATE_90)
        val source = ImageSource.FilePath(file.absolutePath)
        val loaded = loadImage(source)
        assertEquals(800, loaded.width)
        assertEquals(1200, loaded.height)
        val preview = loaded.preview
        assertTrue(max(preview.width, preview.height) <= 1024)
        assertTrue(preview.height >= preview.width)
        val full = loaded.fullRef.decode()
        assertEquals(800, full.width)
        assertEquals(1200, full.height)
        assertEquals(file.length(), loaded.fullRef.byteSizeEstimate)
    }

    @Test
    fun loadImageFromUri() = runBlocking {
        val file = createJpeg(600, 900, ExifInterface.ORIENTATION_ROTATE_270)
        val uri = Uri.fromFile(file)
        val source = ImageSource.UriString(uri.toString())
        val loaded = loadImage(source)
        assertEquals(900, loaded.width)
        assertEquals(600, loaded.height)
        val preview = loaded.preview
        assertTrue(max(preview.width, preview.height) <= 1024)
        assertTrue(preview.width >= preview.height)
        val full = loaded.fullRef.decode()
        assertEquals(900, full.width)
        assertEquals(600, full.height)
    }

    @Test
    fun loadImageFromBytes() = runBlocking {
        val file = createJpeg(400, 300, ExifInterface.ORIENTATION_NORMAL)
        val bytes = file.readBytes()
        val source = ImageSource.Bytes(bytes, file.name)
        val loaded = loadImage(source)
        assertEquals(400, loaded.width)
        assertEquals(300, loaded.height)
        val preview = loaded.preview
        assertEquals(400, preview.width)
        assertEquals(300, preview.height)
    }

    @Config(sdk = [26])
    @Test
    fun heicOnPre28Throws() = runBlocking {
        val heicFile = File.createTempFile("sample", ".heic", context.cacheDir)
        heicFile.writeBytes(ByteArray(16))
        val source = ImageSource.FilePath(heicFile.absolutePath)
        assertFailsWith<ImageIoError.PlatformMissingCodec> {
            loadImage(source)
        }
    }

    private fun createJpeg(width: Int, height: Int, orientation: Int): File {
        val file = File.createTempFile("image", ".jpg", context.cacheDir)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawColor(Color.rgb(120, 90, 200))
        paint.color = Color.YELLOW
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, width / 4f, height / 4f, paint)
        FileOutputStream(file).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
        }
        val exif = ExifInterface(file.absolutePath)
        exif.setAttribute(ExifInterface.TAG_ORIENTATION, orientation.toString())
        exif.saveAttributes()
        return file
    }
}
