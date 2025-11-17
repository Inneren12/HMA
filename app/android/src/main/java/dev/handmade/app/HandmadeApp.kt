package dev.handmade.app

import android.app.Application
import core.io.initializeImageIo

class HandmadeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeImageIo(this)
    }
}
