package dev.infa.page3.SDK.`V-Band`

import dev.infa.page3.SDK.`V-Band`.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * iOS stub for VBandManager.
 * TODO: Implement VeePoo SDK-based BLE communication for iOS.
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
    actual override val syncState: StateFlow<VBandSyncState> = _syncState

    private val _syncLogs = MutableStateFlow<List<VBandSyncLogEntry>>(emptyList())
    actual override val syncLogs: StateFlow<List<VBandSyncLogEntry>> = _syncLogs

    // ─── Scan ───────────────────────────────────────────────────────────────────

    actual override fun startScan() { TODO("iOS V-Band not yet implemented") }
    actual override fun stopScan() { TODO("iOS V-Band not yet implemented") }

    // ─── Connection ─────────────────────────────────────────────────────────────

    actual override fun connect(device: VBandDeviceInfo) { TODO("iOS V-Band not yet implemented") }
    actual override fun disconnect() { TODO("iOS V-Band not yet implemented") }

    // ─── Auth ───────────────────────────────────────────────────────────────────

    actual override fun confirmPassword(pwd: String, is24Hour: Boolean) { TODO("iOS V-Band not yet implemented") }

    // ─── Personal Info ──────────────────────────────────────────────────────────

    actual override fun syncPersonInfo(info: VBandPersonInfo) { TODO("iOS V-Band not yet implemented") }

    // ─── Battery ────────────────────────────────────────────────────────────────

    actual override fun readBattery() { TODO("iOS V-Band not yet implemented") }

    // ─── Steps / Sport ──────────────────────────────────────────────────────────

    actual override fun readSportStep() { TODO("iOS V-Band not yet implemented") }

    // ─── Daily Data ─────────────────────────────────────────────────────────────

    actual override fun readAllHealthData(watchDay: Int) { TODO("iOS V-Band not yet implemented") }
    actual override fun readOriginData(watchDay: Int) { TODO("iOS V-Band not yet implemented") }
    actual override fun readOriginDataSingleDay(day: Int, watchDay: Int) { TODO("iOS V-Band not yet implemented") }

    // ─── Sleep ──────────────────────────────────────────────────────────────────

    actual override fun readSleepData(watchDay: Int) { TODO("iOS V-Band not yet implemented") }
    actual override fun readSleepDataSingleDay(day: Int, watchDay: Int) { TODO("iOS V-Band not yet implemented") }

    // ─── Heart Rate ─────────────────────────────────────────────────────────────

    actual override fun startDetectHeart() { TODO("iOS V-Band not yet implemented") }
    actual override fun stopDetectHeart() { TODO("iOS V-Band not yet implemented") }
    actual override fun readHeartWarning() { TODO("iOS V-Band not yet implemented") }
    actual override fun settingHeartWarning(high: Int, low: Int, isOpen: Boolean) { TODO("iOS V-Band not yet implemented") }

    // ─── Temperature ────────────────────────────────────────────────────────────

    actual override fun startDetectTemperature() { TODO("iOS V-Band not yet implemented") }
    actual override fun stopDetectTemperature() { TODO("iOS V-Band not yet implemented") }
    actual override fun readTemperatureData(day: Int, watchDay: Int) { TODO("iOS V-Band not yet implemented") }
    actual override fun readTemperatureDataSingleDay(day: Int, watchDay: Int) { TODO("iOS V-Band not yet implemented") }

    // ─── Personalization ────────────────────────────────────────────────────────

    actual override fun readCustomSetting() { TODO("iOS V-Band not yet implemented") }
    actual override fun changeCustomSetting(setting: VBandCustomSettingData) { TODO("iOS V-Band not yet implemented") }

    // ─── Night Turn Wrist ───────────────────────────────────────────────────────

    actual override fun readNightTurnWrist() { TODO("iOS V-Band not yet implemented") }
    actual override fun settingNightTurnWrist(isOpen: Boolean, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int, level: Int) { TODO("iOS V-Band not yet implemented") }

    // ─── Screen Brightness ──────────────────────────────────────────────────────

    actual override fun readScreenLight() { TODO("iOS V-Band not yet implemented") }
    actual override fun settingScreenLight(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int, level: Int, otherLevel: Int) { TODO("iOS V-Band not yet implemented") }

    // ─── Screen On Time ─────────────────────────────────────────────────────────

    actual override fun readScreenLightTime() { TODO("iOS V-Band not yet implemented") }
    actual override fun setScreenLightTime(seconds: Int) { TODO("iOS V-Band not yet implemented") }

    // ─── Health Reminders ───────────────────────────────────────────────────────

    actual override fun readHealthRemind(type: VBandHealthRemindType) { TODO("iOS V-Band not yet implemented") }
    actual override fun settingHealthRemind(remind: VBandHealthRemind) { TODO("iOS V-Band not yet implemented") }

    // ─── Language ───────────────────────────────────────────────────────────────

    actual override fun settingLanguage(language: VBandLanguage) { TODO("iOS V-Band not yet implemented") }

    // ─── Sequential Sync ────────────────────────────────────────────────────────

    actual override suspend fun syncAllData(watchDay: Int) { TODO("iOS V-Band not yet implemented") }
    actual override suspend fun syncDayData(day: Int, watchDay: Int) { TODO("iOS V-Band not yet implemented") }
    actual override fun cancelSync() { /* no-op */ }

    // ─── Utility ────────────────────────────────────────────────────────────────

    actual override fun clearLogs() { _logs.value = emptyList() }
    actual override fun clearSyncLogs() {
        _syncLogs.value = emptyList()
        _syncState.value = VBandSyncState()
    }
}

