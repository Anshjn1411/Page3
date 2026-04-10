package dev.infa.page3.SDK.`V-Band`.viewmodel

import dev.infa.page3.SDK.`V-Band`.IVBandManager
import dev.infa.page3.SDK.`V-Band`.VBandManager
import dev.infa.page3.SDK.`V-Band`.data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Shared ViewModel for the V-Band (VeePoo) Smart Watch/Band.
 * Wraps VBandManager and provides UI-friendly state.
 */
class VBandViewModel(
    private val manager: VBandManager
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // ─── Delegate all state flows from manager ──────────────────────────────────

    val devices: StateFlow<List<VBandDeviceInfo>> = manager.devices
    val connectionState: StateFlow<String> = manager.connectionState
    val pwdData: StateFlow<VBandPwdData?> = manager.pwdData
    val functionSupport: StateFlow<VBandFunctionSupport?> = manager.functionSupport
    val batteryData: StateFlow<VBandBatteryData?> = manager.batteryData
    val sportData: StateFlow<VBandSportData?> = manager.sportData
    val heartData: StateFlow<VBandHeartData?> = manager.heartData
    val heartWarningData: StateFlow<VBandHeartWarningData?> = manager.heartWarningData
    val sleepDataList: StateFlow<List<VBandSleepData>> = manager.sleepDataList
    val originDataList: StateFlow<List<VBandOriginData>> = manager.originDataList
    val originHalfHourDataList: StateFlow<List<VBandOriginHalfHourData>> = manager.originHalfHourDataList
    val customSettingData: StateFlow<VBandCustomSettingData?> = manager.customSettingData
    val nightTurnWristData: StateFlow<VBandNightTurnWristData?> = manager.nightTurnWristData
    val screenLightData: StateFlow<VBandScreenLightData?> = manager.screenLightData
    val screenLightTimeData: StateFlow<VBandScreenLightTimeData?> = manager.screenLightTimeData
    val temperatureDetectData: StateFlow<VBandTemperatureDetectData?> = manager.temperatureDetectData
    val temperatureRecords: StateFlow<List<VBandTemperatureRecord>> = manager.temperatureRecords
    val healthRemindList: StateFlow<List<VBandHealthRemind>> = manager.healthRemindList
    val languageData: StateFlow<VBandLanguageData?> = manager.languageData
    val readProgress: StateFlow<VBandReadProgress> = manager.readProgress
    val logs: StateFlow<List<String>> = manager.logs

    // ─── Sync State Flows ───────────────────────────────────────────────────────

    val syncState: StateFlow<VBandSyncState> = manager.syncState
    val syncLogs: StateFlow<List<VBandSyncLogEntry>> = manager.syncLogs

    // ─── UI-specific state ──────────────────────────────────────────────────────

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /** Tracks the currently selected day for day-specific sync */
    private val _selectedSyncDay = MutableStateFlow(0)
    val selectedSyncDay: StateFlow<Int> = _selectedSyncDay.asStateFlow()

    private var currentSyncJob: Job? = null

    val isConnected: Boolean
        get() = connectionState.value == "CONNECTED"

    /** Get watch day capacity from function support, default 3 */
    private val watchDay: Int
        get() = functionSupport.value?.watchDay ?: 3

    // ─── BLE Connection ─────────────────────────────────────────────────────────

    fun startScan() {
        manager.startScan()
    }

    fun stopScan() {
        manager.stopScan()
    }

    fun connect(device: VBandDeviceInfo) {
        manager.connect(device)
    }

    fun disconnect() {
        cancelSync()
        _isSyncing.value = false
        manager.disconnect()
    }

    // ─── Auth / Password ────────────────────────────────────────────────────────

    fun confirmPassword(pwd: String = "0000", is24Hour: Boolean = true) {
        manager.confirmPassword(pwd, is24Hour)
    }

    // ─── Auto-fetch all settings after connection ───────────────────────────────

    fun fetchAllSettings() {
        // Use the new sequential sync for everything
        syncAll()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SEQUENTIAL SYNC (New)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Sync ALL data from the device sequentially.
     * Commands are processed one-at-a-time via the Mutex in VBandManager.
     */
    fun syncAll() {
        if (currentSyncJob?.isActive == true) {
            _errorMessage.value = "Sync already in progress"
            return
        }
        currentSyncJob = scope.launch {
            _isSyncing.value = true
            _isLoading.value = true
            _errorMessage.value = null
            try {
                manager.syncAllData(watchDay)
            } catch (e: Exception) {
                _errorMessage.value = "Sync failed: ${e.message}"
            } finally {
                _isLoading.value = false
                _isSyncing.value = false
            }
        }
    }

    /**
     * Sync data for a SINGLE specific day.
     * @param dayOffset 0=today, 1=yesterday, etc.
     */
    fun syncDay(dayOffset: Int) {
        if (currentSyncJob?.isActive == true) {
            _errorMessage.value = "Sync already in progress"
            return
        }
        _selectedSyncDay.value = dayOffset
        currentSyncJob = scope.launch {
            _isSyncing.value = true
            _isLoading.value = true
            _errorMessage.value = null
            try {
                manager.syncDayData(dayOffset, watchDay)
            } catch (e: Exception) {
                _errorMessage.value = "Day sync failed: ${e.message}"
            } finally {
                _isLoading.value = false
                _isSyncing.value = false
            }
        }
    }

    /** Cancel any in-progress sync */
    fun cancelSync() {
        currentSyncJob?.cancel()
        currentSyncJob = null
        manager.cancelSync()
        _isSyncing.value = false
    }

    /** Update the selected day for UI */
    fun selectSyncDay(dayOffset: Int) {
        _selectedSyncDay.value = dayOffset
    }

    // ─── Personal Info ──────────────────────────────────────────────────────────

    fun syncPersonInfo(info: VBandPersonInfo) {
        manager.syncPersonInfo(info)
    }

    // ─── Battery ────────────────────────────────────────────────────────────────

    fun readBattery() = manager.readBattery()

    // ─── Steps / Sport ──────────────────────────────────────────────────────────

    fun readSportStep() = manager.readSportStep()

    // ─── Daily Data ─────────────────────────────────────────────────────────────

    fun readAllHealthData(watchDay: Int = 3) = manager.readAllHealthData(watchDay)
    fun readOriginData(watchDay: Int = 3) = manager.readOriginData(watchDay)

    // ─── Sleep ──────────────────────────────────────────────────────────────────

    fun readSleepData(watchDay: Int = 3) = manager.readSleepData(watchDay)

    // ─── Heart Rate ─────────────────────────────────────────────────────────────

    fun startDetectHeart() = manager.startDetectHeart()
    fun stopDetectHeart() = manager.stopDetectHeart()
    fun readHeartWarning() = manager.readHeartWarning()
    fun settingHeartWarning(high: Int, low: Int, isOpen: Boolean) =
        manager.settingHeartWarning(high, low, isOpen)

    // ─── Temperature ────────────────────────────────────────────────────────────

    fun startDetectTemperature() = manager.startDetectTemperature()
    fun stopDetectTemperature() = manager.stopDetectTemperature()
    fun readTemperatureData(day: Int = 0, watchDay: Int = 3) =
        manager.readTemperatureData(day, watchDay)

    // ─── Personalization ────────────────────────────────────────────────────────

    fun readCustomSetting() = manager.readCustomSetting()
    fun changeCustomSetting(setting: VBandCustomSettingData) = manager.changeCustomSetting(setting)

    // ─── Night Turn Wrist ───────────────────────────────────────────────────────

    fun readNightTurnWrist() = manager.readNightTurnWrist()
    fun settingNightTurnWrist(
        isOpen: Boolean,
        startHour: Int, startMinute: Int,
        endHour: Int, endMinute: Int,
        level: Int = 5
    ) = manager.settingNightTurnWrist(isOpen, startHour, startMinute, endHour, endMinute, level)

    // ─── Screen Brightness ──────────────────────────────────────────────────────

    fun readScreenLight() = manager.readScreenLight()
    fun settingScreenLight(
        startHour: Int, startMinute: Int,
        endHour: Int, endMinute: Int,
        level: Int, otherLevel: Int
    ) = manager.settingScreenLight(startHour, startMinute, endHour, endMinute, level, otherLevel)

    // ─── Screen On Time ─────────────────────────────────────────────────────────

    fun readScreenLightTime() = manager.readScreenLightTime()
    fun setScreenLightTime(seconds: Int) = manager.setScreenLightTime(seconds)

    // ─── Health Reminders ───────────────────────────────────────────────────────

    fun readHealthRemind(type: VBandHealthRemindType = VBandHealthRemindType.ALL) =
        manager.readHealthRemind(type)
    fun settingHealthRemind(remind: VBandHealthRemind) = manager.settingHealthRemind(remind)

    // ─── Language ───────────────────────────────────────────────────────────────

    fun settingLanguage(language: VBandLanguage) = manager.settingLanguage(language)

    // ─── Utility ────────────────────────────────────────────────────────────────

    fun clearLogs() = manager.clearLogs()
    fun clearSyncLogs() = manager.clearSyncLogs()

    fun clearError() {
        _errorMessage.value = null
    }
}

