package core.io

import android.app.Application
import android.content.Context
import java.util.concurrent.atomic.AtomicReference

internal object AndroidImageIoContext {
    private val contextRef = AtomicReference<Context?>()

    fun initialize(context: Context) {
        contextRef.set(context.applicationContext)
    }

    fun requireContext(): Context {
        contextRef.get()?.let { return it }
        val detected = detectApplication() ?: throw IllegalStateException("Android context not initialized")
        contextRef.compareAndSet(null, detected)
        return detected
    }

    private fun detectApplication(): Context? {
        return try {
            val activityThread = Class.forName("android.app.ActivityThread")
            val method = activityThread.getDeclaredMethod("currentApplication")
            method.invoke(null) as? Application
        } catch (t: Throwable) {
            null
        }
    }
}

fun initializeImageIo(context: Context) {
    AndroidImageIoContext.initialize(context)
}
