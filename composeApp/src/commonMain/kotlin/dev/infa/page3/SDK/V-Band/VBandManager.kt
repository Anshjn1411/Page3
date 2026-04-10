package dev.infa.page3.SDK.`V-Band`

import dev.infa.page3.SDK.`V-Band`.data.*
import kotlinx.coroutines.flow.StateFlow

/**
 * Platform-agnostic expect class for V-Band (VeePoo) BLE communication.
 * Android provides actual implementation via VPOperateManager SDK.
 * iOS provides a TODO stub.
 */
expect class VBandManager : IVBandManager {

    // ─── State Flows ────────────────────────────────────────────────────────────

    override val devices: StateFlow<List<VBandDeviceInfo>>
    override val connectionState: StateFlow<String>
    override val pwdData: StateFlow<VBandPwdData?>
    override val functionSupport: StateFlow<VBandFunctionSupport?>
    override val batteryData: StateFlow<VBandBatteryData?>
    override val sportData: StateFlow<VBandSportData?>
    override val heartData: StateFlow<VBandHeartData?>
    override val heartWarningData: StateFlow<VBandHeartWarningData?>
    override val sleepDataList: StateFlow<List<VBandSleepData>>
    override val originDataList: StateFlow<List<VBandOriginData>>
    override val originHalfHourDataList: StateFlow<List<VBandOriginHalfHourData>>
    override val customSettingData: StateFlow<VBandCustomSettingData?>
    override val nightTurnWristData: StateFlow<VBandNightTurnWristData?>
    override val screenLightData: StateFlow<VBandScreenLightData?>
    override val screenLightTimeData: StateFlow<VBandScreenLightTimeData?>
    override val temperatureDetectData: StateFlow<VBandTemperatureDetectData?>
    override val temperatureRecords: StateFlow<List<VBandTemperatureRecord>>
    override val healthRemindList: StateFlow<List<VBandHealthRemind>>
    override val languageData: StateFlow<VBandLanguageData?>
    override val readProgress: StateFlow<VBandReadProgress>
    override val logs: StateFlow<List<String>>

    // ─── BLE Scan ───────────────────────────────────────────────────────────────

    override fun startScan()
    override fun stopScan()

    // ─── Connection ─────────────────────────────────────────────────────────────

    override fun connect(device: VBandDeviceInfo)
    override fun disconnect()

    // ─── Auth ───────────────────────────────────────────────────────────────────

    override fun confirmPassword(pwd: String, is24Hour: Boolean)

    // ─── Personal Info ──────────────────────────────────────────────────────────

    override fun syncPersonInfo(info: VBandPersonInfo)

    // ─── Battery ────────────────────────────────────────────────────────────────

    override fun readBattery()

    // ─── Steps / Sport ──────────────────────────────────────────────────────────

    override fun readSportStep()

    // ─── Daily Data ─────────────────────────────────────────────────────────────

    override fun readAllHealthData(watchDay: Int)
    override fun readOriginData(watchDay: Int)

    // ─── Sleep ──────────────────────────────────────────────────────────────────

    override fun readSleepData(watchDay: Int)

    // ─── Heart Rate ─────────────────────────────────────────────────────────────

    override fun startDetectHeart()
    override fun stopDetectHeart()
    override fun readHeartWarning()
    override fun settingHeartWarning(high: Int, low: Int, isOpen: Boolean)

    // ─── Temperature ────────────────────────────────────────────────────────────

    override fun startDetectTemperature()
    override fun stopDetectTemperature()
    override fun readTemperatureData(day: Int, watchDay: Int)

    // ─── Personalization ────────────────────────────────────────────────────────

    override fun readCustomSetting()
    override fun changeCustomSetting(setting: VBandCustomSettingData)

    // ─── Night Turn Wrist ───────────────────────────────────────────────────────

    override fun readNightTurnWrist()
    override fun settingNightTurnWrist(
        isOpen: Boolean,
        startHour: Int, startMinute: Int,
        endHour: Int, endMinute: Int,
        level: Int
    )

    // ─── Screen Brightness ──────────────────────────────────────────────────────

    override fun readScreenLight()
    override fun settingScreenLight(
        startHour: Int, startMinute: Int,
        endHour: Int, endMinute: Int,
        level: Int, otherLevel: Int
    )

    // ─── Screen On Time ─────────────────────────────────────────────────────────

    override fun readScreenLightTime()
    override fun setScreenLightTime(seconds: Int)

    // ─── Health Reminders ───────────────────────────────────────────────────────

    override fun readHealthRemind(type: VBandHealthRemindType)
    override fun settingHealthRemind(remind: VBandHealthRemind)

    // ─── Language ───────────────────────────────────────────────────────────────

    override fun settingLanguage(language: VBandLanguage)

    // ─── Utility ────────────────────────────────────────────────────────────────

    override fun clearLogs()
}
