package dev.infa.page3.SDK.bottle.viewmodel

import dev.infa.page3.SDK.bottle.IBottleSyncManager
import dev.infa.page3.SDK.bottle.data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Shared ViewModel for the SGUAI-T30 Smart Bottle.
 * Wraps IBottleSyncManager and provides UI-friendly state.
 */
class BottleViewModel(
    private val syncManager: IBottleSyncManager
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // ─── Delegate all state flows from sync manager ─────────────────────────────

    val devices: StateFlow<List<BottleDeviceInfo>> = syncManager.devices
    val connectionState: StateFlow<String> = syncManager.connectionState
    val batteryStatus: StateFlow<String?> = syncManager.batteryStatus
    val firmwareVersion: StateFlow<String?> = syncManager.firmwareVersion
    val alarms: StateFlow<List<BottleAlarm>> = syncManager.alarms
    val waterIntakeTarget: StateFlow<Int?> = syncManager.waterIntakeTarget
    val drinkingRecordDays: StateFlow<Int?> = syncManager.drinkingRecordDays
    val drinkingRecords: StateFlow<List<DrinkingRecord>> = syncManager.drinkingRecords
    val currentDrink: StateFlow<DrinkingRecord?> = syncManager.currentDrink
    val funcSwitchSmartReminder: StateFlow<Boolean?> = syncManager.funcSwitchSmartReminder
    val autoStandby: StateFlow<Int?> = syncManager.autoStandby
    val colorLight: StateFlow<ColorLightConfig?> = syncManager.colorLight
    val doNotDisturb: StateFlow<DoNotDisturbConfig?> = syncManager.doNotDisturb
    val gradientOption: StateFlow<Int?> = syncManager.gradientOption
    val reminderLightColor: StateFlow<Int?> = syncManager.reminderLightColor
    val waterLevelMl: StateFlow<Int?> = syncManager.waterLevelMl
    val waterTemperature: StateFlow<Int?> = syncManager.waterTemperature
    val lastCommandSuccess: StateFlow<Boolean?> = syncManager.lastCommandSuccess
    val logs: StateFlow<List<String>> = syncManager.logs

    /**
     * Today's total water intake in mL, computed from drinking records.
     * Uses SharingStarted.Eagerly so the combine always runs —
     * never misses BLE responses that arrive before the UI subscribes.
     */
    val todayTotalIntake: StateFlow<Int> = combine(
        syncManager.drinkingRecords,
        syncManager.waterLevelMl
    ) { records, liveLevel ->
        val recordSum = records.sumOf { it.waterIntakeMl }
        if (recordSum > 0) recordSum else (liveLevel ?: 0)
    }.stateIn(scope, SharingStarted.Eagerly, 0)

    // ─── UI-specific state ──────────────────────────────────────────────────────

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** True while syncing data from bottle after connection */
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val isConnected: Boolean
        get() = connectionState.value == "CONNECTED"

    // ─── BLE Connection ─────────────────────────────────────────────────────────

    fun startScan() {
        syncManager.startScan()
    }

    fun stopScan() {
        syncManager.stopScan()
    }

    fun connect(device: BottleDeviceInfo) {
        syncManager.connect(device)
    }

    fun disconnect() {
        _isSyncing.value = false
        syncManager.disconnect()
    }

    // ─── Auto-fetch all settings after connection ───────────────────────────────

    fun fetchAllSettings() {
        scope.launch {
            _isSyncing.value = true
            _isLoading.value = true
            try {
                syncManager.requestBatteryLevel()
                syncManager.requestFirmwareVersion()
                syncManager.requestWaterIntakeTarget()
                syncManager.requestAllAlarms()
                syncManager.requestFuncSwitch()
                syncManager.requestAutoStandby()
                syncManager.requestColorLight()
                syncManager.requestDoNotDisturb()
                syncManager.requestGradientOption()
                syncManager.requestReminderLight()
                syncManager.requestDrinkingRecordData(0) // Fetch today's records

                // Wait for today's records to actually arrive from the device
                // (commands are non-blocking; data arrives async via GATT callbacks)
                withTimeoutOrNull(8000L) {
                    syncManager.drinkingRecords.first { it.isNotEmpty() }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to fetch settings: ${e.message}"
            } finally {
                _isLoading.value = false
                _isSyncing.value = false
            }
        }
    }

    // ─── Queries ────────────────────────────────────────────────────────────────

    fun requestBatteryLevel() = syncManager.requestBatteryLevel()
    fun requestFirmwareVersion() = syncManager.requestFirmwareVersion()
    fun requestAllAlarms() = syncManager.requestAllAlarms()
    fun requestDrinkingRecordDays() = syncManager.requestDrinkingRecordDays()
    fun requestDrinkingRecordData(dayIndex: Int = 0) = syncManager.requestDrinkingRecordData(dayIndex)
    fun requestWaterIntakeTarget() = syncManager.requestWaterIntakeTarget()

    // ─── Commands ───────────────────────────────────────────────────────────────

    fun syncTime() = syncManager.syncTime()
    fun activateLight() = syncManager.activateLight()
    fun calibrateSensor() = syncManager.calibrateSensor()

    fun setWaterIntakeTarget(targetMl: Int) {
        syncManager.setWaterIntakeTarget(targetMl)
    }

    fun updateAlarm(alarm: BottleAlarm) {
        syncManager.updateAlarm(alarm)
    }

    fun deleteAlarm(alarmId: Int) {
        syncManager.deleteAlarm(alarmId)
    }

    fun confirmAcquisition(dayIndex: Int = 0) {
        syncManager.confirmAcquisition(dayIndex)
    }

    fun sendTotalDailyWaterIntake(totalMl: Int) {
        syncManager.sendTotalDailyWaterIntake(totalMl)
    }

    fun setSmartReminder(enabled: Boolean) {
        syncManager.setFuncSwitch(enabled)
    }

    fun setAutoStandby(option: Int) {
        syncManager.setAutoStandby(option)
    }

    fun setColorLight(on: Boolean, startColor: Int, endColor: Int) {
        syncManager.setColorLight(on, startColor, endColor)
    }

    fun setDoNotDisturb(on: Boolean, startH: Int, startM: Int, endH: Int, endM: Int) {
        syncManager.setDoNotDisturb(on, startH, startM, endH, endM)
    }

    fun setGradientOption(option: Int) {
        syncManager.setGradientOption(option)
    }

    fun setReminderLight(colorIndex: Int) {
        syncManager.setReminderLight(colorIndex)
    }

    fun factoryReset() {
        syncManager.factoryReset()
    }

    // ─── Utility ────────────────────────────────────────────────────────────────

    fun clearLogs() = syncManager.clearLogs()

    fun clearError() {
        _errorMessage.value = null
    }
}
