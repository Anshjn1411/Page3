package dev.infa.page3.platform

import platform.Foundation.NSLog

/**
 * iOS implementation of DeviceSDKLauncher
 * Since iOS doesn’t allow arbitrary activity launches like Android,
 * this can be used to show a placeholder or trigger native BLE SDK logic.
 */
actual class DeviceSDKLauncher actual constructor(actual val context: Any) {

    actual fun openDeviceManager() {

    }

    actual fun isSDKAvailable(): Boolean {
        NSLog("DeviceSDKLauncher: isSDKAvailable() called on iOS")
        // Modify this if you later integrate an iOS BLE SDK
        return true
    }
}
