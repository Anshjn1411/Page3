package dev.infa.page3

import android.app.Application
import android.util.Log
import dev.infa.page3.bluetooth.BleInitializer

/**
 * Custom Application class for Page3 app.
 * Initializes BLE SDK and other global components.
 */
// androidMain/Page3Application.kt

class Page3Application : Application() {

    companion object {
        private const val TAG = "Page3Application"

        @Volatile
        lateinit var instance: Application
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d(TAG, "Application onCreate")

        // ONLY place BLE SDK is initialized
        BleInitializer.initialize(this)
    }
}