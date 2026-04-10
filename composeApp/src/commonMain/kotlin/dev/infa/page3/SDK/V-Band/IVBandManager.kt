package dev.infa.page3.SDK.`V-Band`

import dev.infa.page3.SDK.`V-Band`.data.*
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface defining all V-Band (VeePoo) smart watch operations.
 * Platform implementations (Android/iOS) provide BLE communication via VeePoo SDK.
 */
interface IVBandManager {

    // ═══════════════════════════════════════════════════════════════════════════
    // STATE FLOWS (Observable state)
    // ═══════════════════════════════════════════════════════════════════════════

    /** Discovered devices during scan */
    val devices: StateFlow<List<VBandDeviceInfo>>

    /** Current connection state: DISCONNECTED, SCANNING, CONNECTING, CONNECTED */
    val connectionState: StateFlow<String>

    /** Password verification data */
    val pwdData: StateFlow<VBandPwdData?>

    /** Device function support data */
    val functionSupport: StateFlow<VBandFunctionSupport?>

    /** Battery data */
    val batteryData: StateFlow<VBandBatteryData?>

    /** Current step/sport data */
    val sportData: StateFlow<VBandSportData?>

    /** Heart rate data (manual measurement) */
    val heartData: StateFlow<VBandHeartData?>

    /** Heart warning settings */
    val heartWarningData: StateFlow<VBandHeartWarningData?>

    /** Sleep data list */
    val sleepDataList: StateFlow<List<VBandSleepData>>

    /** 5-minute origin data list */
    val originDataList: StateFlow<List<VBandOriginData>>

    /** 30-minute aggregated data list */
    val originHalfHourDataList: StateFlow<List<VBandOriginHalfHourData>>

    /** Custom / personalization settings */
    val customSettingData: StateFlow<VBandCustomSettingData?>

    /** Night turn wrist (raise-to-wake) settings */
    val nightTurnWristData: StateFlow<VBandNightTurnWristData?>

    /** Screen light / brightness data */
    val screenLightData: StateFlow<VBandScreenLightData?>

    /** Screen on time data */
    val screenLightTimeData: StateFlow<VBandScreenLightTimeData?>

    /** Temperature detection data (manual) */
    val temperatureDetectData: StateFlow<VBandTemperatureDetectData?>

    /** Daily temperature records */
    val temperatureRecords: StateFlow<List<VBandTemperatureRecord>>

    /** Health reminders */
    val healthRemindList: StateFlow<List<VBandHealthRemind>>

    /** Language data */
    val languageData: StateFlow<VBandLanguageData?>

    /** Read progress for bulk operations */
    val readProgress: StateFlow<VBandReadProgress>

    /** Debug logs */
    val logs: StateFlow<List<String>>

    /** Sync state — tracks current sync step, progress, status */
    val syncState: StateFlow<VBandSyncState>

    /** Structured sync log entries */
    val syncLogs: StateFlow<List<VBandSyncLogEntry>>

    // ═══════════════════════════════════════════════════════════════════════════
    // BLE SCAN
    // ═══════════════════════════════════════════════════════════════════════════

    /** Start scanning for V-Band devices */
    fun startScan()

    /** Stop scanning */
    fun stopScan()

    // ═══════════════════════════════════════════════════════════════════════════
    // CONNECTION
    // ═══════════════════════════════════════════════════════════════════════════

    /** Connect to a device by MAC address */
    fun connect(device: VBandDeviceInfo)

    /** Disconnect from the current device */
    fun disconnect()

    // ═══════════════════════════════════════════════════════════════════════════
    // AUTH / PASSWORD
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Confirm device password — the first operation after successful connection.
     * Returns password data, function support data, social msg data, custom settings.
     * @param pwd 4-digit password string, default "0000"
     * @param is24Hour true for 24h time format, false for 12h
     */
    fun confirmPassword(pwd: String = "0000", is24Hour: Boolean = true)

    // ═══════════════════════════════════════════════════════════════════════════
    // PERSONAL INFO
    // ═══════════════════════════════════════════════════════════════════════════

    /** Sync personal info to device (height/weight affect calorie calculations) */
    fun syncPersonInfo(info: VBandPersonInfo)

    // ═══════════════════════════════════════════════════════════════════════════
    // BATTERY
    // ═══════════════════════════════════════════════════════════════════════════

    /** Read current battery level */
    fun readBattery()

    // ═══════════════════════════════════════════════════════════════════════════
    // STEPS / SPORT
    // ═══════════════════════════════════════════════════════════════════════════

    /** Read current step count (real-time) */
    fun readSportStep()

    // ═══════════════════════════════════════════════════════════════════════════
    // DAILY DATA
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Read all health data (sleep + 5-minute origin data).
     * @param watchDay max days the watch can store
     */
    fun readAllHealthData(watchDay: Int = 3)

    /**
     * Read raw origin data (5-minute intervals).
     * @param watchDay max days the watch can store
     */
    fun readOriginData(watchDay: Int = 3)

    /**
     * Read origin data for a single specific day only.
     * @param day 0=today, 1=yesterday, etc.
     * @param watchDay max days the watch can store
     */
    fun readOriginDataSingleDay(day: Int, watchDay: Int = 3)

    // ═══════════════════════════════════════════════════════════════════════════
    // SLEEP
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Read sleep data.
     * @param watchDay max days the watch can store
     */
    fun readSleepData(watchDay: Int = 3)

    /**
     * Read sleep data for a single specific day only.
     * @param day 0=today, 1=yesterday, etc.
     * @param watchDay max days the watch can store
     */
    fun readSleepDataSingleDay(day: Int, watchDay: Int = 3)

    // ═══════════════════════════════════════════════════════════════════════════
    // HEART RATE
    // ═══════════════════════════════════════════════════════════════════════════

    /** Start manual heart rate measurement */
    fun startDetectHeart()

    /** Stop manual heart rate measurement */
    fun stopDetectHeart()

    /** Read heart rate alarm settings */
    fun readHeartWarning()

    /** Set heart rate alarm thresholds */
    fun settingHeartWarning(high: Int, low: Int, isOpen: Boolean)

    // ═══════════════════════════════════════════════════════════════════════════
    // TEMPERATURE
    // ═══════════════════════════════════════════════════════════════════════════

    /** Start manual temperature measurement */
    fun startDetectTemperature()

    /** Stop manual temperature measurement */
    fun stopDetectTemperature()

    /**
     * Read daily temperature records.
     * @param day 0=today, 1=yesterday, etc.
     * @param watchDay max days the watch can store
     */
    fun readTemperatureData(day: Int = 0, watchDay: Int = 3)

    /**
     * Read temperature data for a single specific day only.
     * @param day 0=today, 1=yesterday, etc.
     * @param watchDay max days the watch can store
     */
    fun readTemperatureDataSingleDay(day: Int, watchDay: Int = 3)

    // ═══════════════════════════════════════════════════════════════════════════
    // PERSONALIZATION / CUSTOM SETTINGS
    // ═══════════════════════════════════════════════════════════════════════════

    /** Read personalized settings */
    fun readCustomSetting()

    /** Modify personalized settings */
    fun changeCustomSetting(setting: VBandCustomSettingData)

    // ═══════════════════════════════════════════════════════════════════════════
    // NIGHT TURN WRIST (RAISE TO WAKE)
    // ═══════════════════════════════════════════════════════════════════════════

    /** Read raise-to-wake settings */
    fun readNightTurnWrist()

    /** Set raise-to-wake settings */
    fun settingNightTurnWrist(
        isOpen: Boolean,
        startHour: Int, startMinute: Int,
        endHour: Int, endMinute: Int,
        level: Int = 5
    )

    // ═══════════════════════════════════════════════════════════════════════════
    // SCREEN BRIGHTNESS
    // ═══════════════════════════════════════════════════════════════════════════

    /** Read screen brightness settings */
    fun readScreenLight()

    /** Set screen brightness */
    fun settingScreenLight(
        startHour: Int, startMinute: Int,
        endHour: Int, endMinute: Int,
        level: Int, otherLevel: Int
    )

    // ═══════════════════════════════════════════════════════════════════════════
    // SCREEN ON TIME
    // ═══════════════════════════════════════════════════════════════════════════

    /** Read screen on duration */
    fun readScreenLightTime()

    /** Set screen on duration (seconds) */
    fun setScreenLightTime(seconds: Int)

    // ═══════════════════════════════════════════════════════════════════════════
    // HEALTH REMINDERS
    // ═══════════════════════════════════════════════════════════════════════════

    /** Read health reminders */
    fun readHealthRemind(type: VBandHealthRemindType = VBandHealthRemindType.ALL)

    /** Set a health reminder */
    fun settingHealthRemind(remind: VBandHealthRemind)

    // ═══════════════════════════════════════════════════════════════════════════
    // LANGUAGE
    // ═══════════════════════════════════════════════════════════════════════════

    /** Set device language */
    fun settingLanguage(language: VBandLanguage)

    // ═══════════════════════════════════════════════════════════════════════════
    // SEQUENTIAL SYNC (Command Queue)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Sync ALL data from the device sequentially (one command at a time).
     * Battery → Steps → Sleep → Origin → Temperature → Settings → HeartWarning
     * @param watchDay max days the watch can store
     */
    suspend fun syncAllData(watchDay: Int = 3)

    /**
     * Sync data for a SINGLE specific day sequentially.
     * Sleep(day) → Origin(day) → Temperature(day)
     * @param day 0=today, 1=yesterday, etc.
     * @param watchDay max days the watch can store
     */
    suspend fun syncDayData(day: Int, watchDay: Int = 3)

    /**
     * Cancel any in-progress sync operation.
     */
    fun cancelSync()

    // ═══════════════════════════════════════════════════════════════════════════
    // UTILITY
    // ═══════════════════════════════════════════════════════════════════════════

    /** Clear debug logs */
    fun clearLogs()

    /** Clear sync logs */
    fun clearSyncLogs()
}

