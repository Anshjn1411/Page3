package dev.infa.page3.platform

/**
 * Platform-specific interface for launching the BLE Device SDK
 * This allows the common UI to trigger Android-specific SDK functionality
 */
expect class DeviceSDKLauncher(context: Any) {
    val context: Any


    fun openDeviceManager()

    /**
     * Checks if the device SDK is available on this platform
     * @return true if SDK is available, false otherwise
     */
    fun isSDKAvailable(): Boolean
}
