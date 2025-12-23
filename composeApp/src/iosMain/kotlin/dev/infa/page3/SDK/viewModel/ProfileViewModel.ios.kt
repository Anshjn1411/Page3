package dev.infa.page3.SDK.viewModel

import dev.infa.page3.SDK.data.DeviceInfo
import dev.infa.page3.SDK.data.TimeFormat
import dev.infa.page3.SDK.data.TouchSettings
import dev.infa.page3.SDK.data.UnitSystem

actual class ProfileManager {
    actual suspend fun findMyDevice(): Boolean {
        TODO("Not yet implemented")
    }

    actual suspend fun getDeviceInfo(): DeviceInfo? {
        TODO("Not yet implemented")
    }

    actual suspend fun updateUnitSystem(unitSystem: UnitSystem): Boolean {
        TODO("Not yet implemented")
    }

    actual suspend fun updateTimeFormat(timeFormat: TimeFormat): Boolean {
        TODO("Not yet implemented")
    }

    actual suspend fun toggleLowBatteryPrompt(enabled: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    actual suspend fun loadTouchSettings(): TouchSettings? {
        TODO("Not yet implemented")
    }

    actual suspend fun updateTouchSettings(
        appType: Int,
        isTouch: Boolean,
        strength: Int
    ): Boolean {
        TODO("Not yet implemented")
    }

    actual fun cleanup() {
    }
}