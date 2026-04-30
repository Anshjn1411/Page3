package dev.infa.page3.SDK.viewModel

import dev.infa.page3.SDK.data.DeviceInfo
import dev.infa.page3.SDK.data.TimeFormat
import dev.infa.page3.SDK.data.TouchSettings
import dev.infa.page3.SDK.data.UnitSystem

/**
 * iOS stub for ProfileManager.
 * SDK disabled – all methods return safe defaults.
 */
actual class ProfileManager {
    actual suspend fun findMyDevice(): Boolean {
        return false
    }

    actual suspend fun getDeviceInfo(): DeviceInfo? {
        return null
    }

    actual suspend fun updateUnitSystem(unitSystem: UnitSystem, currentTimeFormat: TimeFormat): Boolean {
        return false
    }

    actual suspend fun updateTimeFormat(timeFormat: TimeFormat): Boolean {
        return false
    }

    actual suspend fun toggleLowBatteryPrompt(enabled: Boolean): Boolean {
        return false
    }

    actual suspend fun loadTouchSettings(): TouchSettings? {
        return null
    }

    actual suspend fun updateTouchSettings(
        appType: Int,
        isTouch: Boolean,
        strength: Int
    ): Boolean {
        return false
    }

    actual fun cleanup() {
    }
}