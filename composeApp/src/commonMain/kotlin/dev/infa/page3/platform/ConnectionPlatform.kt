package dev.infa.page3.platform

import androidx.compose.runtime.Composable

/**
 * Expect/actual bridge for platform-specific background connection control.
 * The Android actual will initialize the SDK once and manage the Service lifecycle.
 */
expect object ConnectionServiceController {
    fun start()
    fun start(deviceName: String, deviceAddress: String)
    fun stop()
}

/**
 * Ensures the background connection service is started and the SDK is initialized once
 * when the app's root composable is first composed.
 */
@Composable
expect fun EnsureBackgroundConnection()


