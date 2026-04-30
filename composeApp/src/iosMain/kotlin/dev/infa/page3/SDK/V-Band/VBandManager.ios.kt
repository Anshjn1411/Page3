package dev.infa.page3.SDK.`V-Band`

import dev.infa.page3.SDK.`V-Band`.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * iOS stub for VBandManager.
 * SDK disabled – all methods are no-ops returning defaults.
 */
actual class VBandManager : IVBandManager {

    // ─── State Flows ────────────────────────────────────────────────────────────

    private val _devices = MutableStateFlow<List<VBandDeviceInfo>>(emptyList())
    actual override val devices: StateFlow<List<VBandDeviceInfo>> = _devices

    private val _connectionState = MutableStateFlow("DISCONNECTED")
    actual override val connectionState: StateFlow<String> = _connectionState

    private val _pwdData = MutableStateFlow<VBandPwdData?>(null)
    actual override val pwdData: StateFlow<VBandPwdData?> = _pwdData

    private val _functionSupport = MutableStateFlow<VBandFunctionSupport?>(null)
    actual override val functionSupport: StateFlow<VBandFunctionSupport?> = _functionSupport

    private val _batteryData = MutableStateFlow<VBandBatteryData?>(null)
    actual override val batteryData: StateFlow<VBandBatteryData?> = _batteryData

    private val _sportData = MutableStateFlow<VBandSportData?>(null)
    actual override val sportData: StateFlow<VBandSportData?> = _sportData

    private val _heartData = MutableStateFlow<VBandHeartData?>(null)
    actual override val heartData: StateFlow<VBandHeartData?> = _heartData

    private val _heartWarningData = MutableStateFlow<VBandHeartWarningData?>(null)
    actual override val heartWarningData: StateFlow<VBandHeartWarningData?> = _heartWarningData

    private val _sleepDataList = MutableStateFlow<List<VBandSleepData>>(emptyList())
    actual override val sleepDataList: StateFlow<List<VBandSleepData>> = _sleepDataList

    private val _originDataList = MutableStateFlow<List<VBandOriginData>>(emptyList())
    actual override val originDataList: StateFlow<List<VBandOriginData>> = _originDataList

    private val _originHalfHourDataList = MutableStateFlow<List<VBandOriginHalfHourData>>(emptyList())
    actual override val originHalfHourDataList: StateFlow<List<VBandOriginHalfHourData>> = _originHalfHourDataList

    private val _customSettingData = MutableStateFlow<VBandCustomSettingData?>(null)
    actual override val customSettingData: StateFlow<VBandCustomSettingData?> = _customSettingData

    private val _nightTurnWristData = MutableStateFlow<VBandNightTurnWristData?>(null)
    actual override val nightTurnWristData: StateFlow<VBandNightTurnWristData?> = _nightTurnWristData

    private val _screenLightData = MutableStateFlow<VBandScreenLightData?>(null)
    actual override val screenLightData: StateFlow<VBandScreenLightData?> = _screenLightData

    private val _screenLightTimeData = MutableStateFlow<VBandScreenLightTimeData?>(null)
    actual override val screenLightTimeData: StateFlow<VBandScreenLightTimeData?> = _screenLightTimeData

    private val _temperatureDetectData = MutableStateFlow<VBandTemperatureDetectData?>(null)
    actual override val temperatureDetectData: StateFlow<VBandTemperatureDetectData?> = _temperatureDetectData

    private val _temperatureRecords = MutableStateFlow<List<VBandTemperatureRecord>>(emptyList())
    actual override val temperatureRecords: StateFlow<List<VBandTemperatureRecord>> = _temperatureRecords

    private val _healthRemindList = MutableStateFlow<List<VBandHealthRemind>>(emptyList())
    actual override val healthRemindList: StateFlow<List<VBandHealthRemind>> = _healthRemindList

    private val _languageData = MutableStateFlow<VBandLanguageData?>(null)
    actual override val languageData: StateFlow<VBandLanguageData?> = _languageData

    private val _readProgress = MutableStateFlow(VBandReadProgress())
    actual override val readProgress: StateFlow<VBandReadProgress> = _readProgress

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    actual override val logs: StateFlow<List<String>> = _logs

    private val _syncState = MutableStateFlow(VBandSyncState())
    override val syncState: StateFlow<VBandSyncState> = _syncState

    private val _syncLogs = MutableStateFlow<List<VBandSyncLogEntry>>(emptyList())
    override val syncLogs: StateFlow<List<VBandSyncLogEntry>> = _syncLogs

    // ─── Scan (no-op) ───────────────────────────────────────────────────────────

    actual override fun startScan() { /* no-op: SDK disabled */ }
    actual override fun stopScan() { /* no-op: SDK disabled */ }

    // ─── Connection (no-op) ─────────────────────────────────────────────────────

    actual override fun connect(device: VBandDeviceInfo) { /* no-op: SDK disabled */ }
    actual override fun disconnect() { /* no-op: SDK disabled */ }

    // ─── Auth (no-op) ───────────────────────────────────────────────────────────

    actual override fun confirmPassword(pwd: String, is24Hour: Boolean) { /* no-op: SDK disabled */ }

    // ─── Personal Info (no-op) ──────────────────────────────────────────────────

    actual override fun syncPersonInfo(info: VBandPersonInfo) { /* no-op: SDK disabled */ }

    // ─── Battery (no-op) ────────────────────────────────────────────────────────

    actual override fun readBattery() { /* no-op: SDK disabled */ }

    // ─── Steps / Sport (no-op) ──────────────────────────────────────────────────

    actual override fun readSportStep() { /* no-op: SDK disabled */ }

    // ─── Daily Data (no-op) ─────────────────────────────────────────────────────

    actual override fun readAllHealthData(watchDay: Int) { /* no-op: SDK disabled */ }
    actual override fun readOriginData(watchDay: Int) { /* no-op: SDK disabled */ }
     override fun readOriginDataSingleDay(day: Int, watchDay: Int) { /* no-op: SDK disabled */ }

    // ─── Sleep (no-op) ──────────────────────────────────────────────────────────

    actual override fun readSleepData(watchDay: Int) { /* no-op: SDK disabled */ }
     override fun readSleepDataSingleDay(day: Int, watchDay: Int) { /* no-op: SDK disabled */ }

    // ─── Heart Rate (no-op) ─────────────────────────────────────────────────────

    actual override fun startDetectHeart() { /* no-op: SDK disabled */ }
    actual override fun stopDetectHeart() { /* no-op: SDK disabled */ }
    actual override fun readHeartWarning() { /* no-op: SDK disabled */ }
    actual override fun settingHeartWarning(high: Int, low: Int, isOpen: Boolean) { /* no-op: SDK disabled */ }

    // ─── Temperature (no-op) ────────────────────────────────────────────────────

    actual override fun startDetectTemperature() { /* no-op: SDK disabled */ }
    actual override fun stopDetectTemperature() { /* no-op: SDK disabled */ }
    actual override fun readTemperatureData(day: Int, watchDay: Int) { /* no-op: SDK disabled */ }
     override fun readTemperatureDataSingleDay(day: Int, watchDay: Int) { /* no-op: SDK disabled */ }

    // ─── Personalization (no-op) ────────────────────────────────────────────────

    actual override fun readCustomSetting() { /* no-op: SDK disabled */ }
    actual override fun changeCustomSetting(setting: VBandCustomSettingData) { /* no-op: SDK disabled */ }

    // ─── Night Turn Wrist (no-op) ───────────────────────────────────────────────

    actual override fun readNightTurnWrist() { /* no-op: SDK disabled */ }
    actual override fun settingNightTurnWrist(isOpen: Boolean, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int, level: Int) { /* no-op: SDK disabled */ }

    // ─── Screen Brightness (no-op) ──────────────────────────────────────────────

    actual override fun readScreenLight() { /* no-op: SDK disabled */ }
    actual override fun settingScreenLight(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int, level: Int, otherLevel: Int) { /* no-op: SDK disabled */ }

    // ─── Screen On Time (no-op) ─────────────────────────────────────────────────

    actual override fun readScreenLightTime() { /* no-op: SDK disabled */ }
    actual override fun setScreenLightTime(seconds: Int) { /* no-op: SDK disabled */ }

    // ─── Health Reminders (no-op) ───────────────────────────────────────────────

    actual override fun readHealthRemind(type: VBandHealthRemindType) { /* no-op: SDK disabled */ }
    actual override fun settingHealthRemind(remind: VBandHealthRemind) { /* no-op: SDK disabled */ }

    // ─── Language (no-op) ───────────────────────────────────────────────────────

    actual override fun settingLanguage(language: VBandLanguage) { /* no-op: SDK disabled */ }

    // ─── Sequential Sync (no-op) ────────────────────────────────────────────────

     override suspend fun syncAllData(watchDay: Int) { /* no-op: SDK disabled */ }
     override suspend fun syncDayData(day: Int, watchDay: Int) { /* no-op: SDK disabled */ }
     override fun cancelSync() { /* no-op */ }

    // ─── Utility ────────────────────────────────────────────────────────────────

    actual override fun clearLogs() { _logs.value = emptyList() }
     override fun clearSyncLogs() {
        _syncLogs.value = emptyList()
        _syncState.value = VBandSyncState()
    }
}
