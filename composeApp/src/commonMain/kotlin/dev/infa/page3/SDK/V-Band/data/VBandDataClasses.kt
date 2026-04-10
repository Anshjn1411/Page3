package dev.infa.page3.SDK.`V-Band`.data

import kotlinx.serialization.Serializable

// ═══════════════════════════════════════════════════════════════════════════════
// ENUMS
// ═══════════════════════════════════════════════════════════════════════════════

/** Function status — mirrors the VeePoo SDK EFunctionStatus */
enum class VBandFunctionStatus {
    UNSUPPORT, SUPPORT, SUPPORT_OPEN, SUPPORT_CLOSE, UNKNOWN
}

/** Password verification status */
enum class VBandPwdStatus {
    CHECK_SUCCESS, CHECK_FAIL, SETTING_SUCCESS, SETTING_FAIL, UNKNOWN
}

/** Gender enum */
enum class VBandSex { MAN, WOMAN, UNKNOWN }

/** Language enum — subset of the device-supported languages */
enum class VBandLanguage {
    CHINESE, CHINESE_TRADITIONAL, ENGLISH, JAPANESE, KOREAN, GERMAN,
    RUSSIAN, SPANISH, ITALIAN, FRENCH, VIETNAMESE, PORTUGUESE,
    THAI, POLISH, SWEDISH, TURKISH, DUTCH, CZECH, ARABIC,
    HUNGARIAN, GREEK, ROMANIAN, SLOVAK, INDONESIAN, BRAZILIAN_PORTUGUESE,
    CROATIAN, LITHUANIAN, UKRAINIAN, HINDI, HEBREW, DANISH,
    PERSIAN, FINNISH, MALAY, UNKNOWN
}

/** Operation status */
enum class VBandOperateStatus { SUCCESS, FAIL, UNKNOWN }

/** Health reminder types */
enum class VBandHealthRemindType {
    ALL, SEDENTARY, DRINK_WATER, OVERLOOK, SPORTS,
    TAKE_MEDICINE, READING, GOING_OUT, WASH
}

/** Heart rate measurement status */
enum class VBandHeartStatus {
    STATE_INIT, STATE_HEART_BUSY, STATE_HEART_DETECT,
    STATE_HEART_WEAR_ERROR, STATE_HEART_NORMAL
}

/** Heart warning status */
enum class VBandHeartWarningStatus {
    OPEN_SUCCESS, OPEN_FAIL, CLOSE_SUCCESS, CLOSE_FAIL,
    READ_SUCCESS, READ_FAIL, UNSUPPORT, UNKNOWN
}

/** Screen light status */
enum class VBandScreenLightStatus {
    SETTING_SUCCESS, SETTING_FAIL, READ_SUCCESS, READ_FAIL, UNKNOWN
}

/** Screen light time status */
enum class VBandScreenLightTimeStatus {
    SETTING_SUCCESS, SETTING_FAIL, READ_SUCCESS, READ_FAIL, UNKNOWN
}

/** Night turn wrist status */
enum class VBandNightTurnWristStatus {
    SUCCESS, FAIL, UNKNOWN
}

/** Temperature unit */
enum class VBandTemperatureUnit {
    NONE, CELSIUS, FAHRENHEIT
}

/** Blood glucose unit */
enum class VBandBloodGlucoseUnit {
    NONE, MMOL_L, MG_DL
}

// ═══════════════════════════════════════════════════════════════════════════════
// DATA CLASSES
// ═══════════════════════════════════════════════════════════════════════════════

// ─── Device Info (scan result) ───────────────────────────────────────────────

@Serializable
data class VBandDeviceInfo(
    val name: String,
    val address: String,
    val rssi: Int = 0
)

// ─── Password / Auth Data ────────────────────────────────────────────────────

@Serializable
data class VBandPwdData(
    val status: VBandPwdStatus = VBandPwdStatus.UNKNOWN,
    val password: String = "",
    val deviceNumber: Int = 0,
    val deviceVersion: String = "",
    val deviceTestVersion: String = "",
    val isHaveDrinkData: Boolean = false,
    val nightTurnWristStatus: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val findPhoneFunction: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val wearDetectFunction: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN
)

// ─── Device Function Support ─────────────────────────────────────────────────

@Serializable
data class VBandFunctionSupport(
    val bp: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val drink: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val longSeat: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val heartWarning: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val weChatSport: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val camera: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val fatigue: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val spO2: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val woman: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val countDown: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val screenLight: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val heartDetect: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val sportModel: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val nightTurnSetting: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val screenStyleFunction: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val breathFunction: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val hrvFunction: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val weatherFunction: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val screenLightTime: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val precisionSleep: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val ecg: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val multSportModel: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val findDeviceByPhone: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val temperatureFunction: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val bloodGlucose: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val watchDay: Int = 3,
    val contactMsgLength: Int = 0,
    val allMsgLength: Int = 0,
    val screenStyle: Int = 0,
    val originProtocolVersion: Int = 0,
    val cpuType: Int = 0
)

// ─── Battery Data ────────────────────────────────────────────────────────────

@Serializable
data class VBandBatteryData(
    val batteryLevel: Int = 0,       // [1-4], 4 = full
    val batteryPercent: Int = 0,     // [1-100]
    val powerModel: Int = 0,         // 0x00 normal, 0x01 charging, 0x02 low, 0x03 full
    val isLowBattery: Boolean = false,
    val isPercent: Boolean = false   // true = use batteryPercent, false = use batteryLevel
)

// ─── Sport / Step Data ───────────────────────────────────────────────────────

@Serializable
data class VBandSportData(
    val step: Int = 0,
    val distance: Double = 0.0,    // km
    val calories: Double = 0.0,    // kcal
    val calcType: Int = 0
)

// ─── Heart Rate Data ─────────────────────────────────────────────────────────

@Serializable
data class VBandHeartData(
    val heartRate: Int = 0,
    val heartStatus: VBandHeartStatus = VBandHeartStatus.STATE_INIT
)

// ─── Heart Warning Data ──────────────────────────────────────────────────────

@Serializable
data class VBandHeartWarningData(
    val status: VBandHeartWarningStatus = VBandHeartWarningStatus.UNKNOWN,
    val heartHigh: Int = 150,
    val heartLow: Int = 40,
    val isOpen: Boolean = false
)

// ─── Sleep Data ──────────────────────────────────────────────────────────────

@Serializable
data class VBandSleepData(
    val date: String = "",
    val sleepQuality: Int = 0,
    val wakeCount: Int = 0,
    val deepSleepTime: Int = 0,    // minutes
    val lightSleepTime: Int = 0,   // minutes
    val totalSleepTime: Int = 0,   // minutes
    val sleepLine: String = "",
    val sleepDownHour: Int = 0,
    val sleepDownMinute: Int = 0,
    val sleepUpHour: Int = 0,
    val sleepUpMinute: Int = 0
)

// ─── Origin Data (5 minutes raw) ─────────────────────────────────────────────

@Serializable
data class VBandOriginData(
    val date: String = "",
    val allPackage: Int = 0,
    val packageNumber: Int = 0,
    val hour: Int = 0,
    val minute: Int = 0,
    val rateValue: Int = 0,         // heart rate [30-200]
    val sportValue: Int = 0,        // exercise intensity
    val stepValue: Int = 0,
    val highBpValue: Int = 0,       // systolic [60-300]
    val lowBpValue: Int = 0,        // diastolic [20-200]
    val calValue: Double = 0.0,
    val disValue: Double = 0.0,     // km
    val temperature: Double = 0.0,
    val baseTemperature: Double = 0.0
)

// ─── Half-Hour Aggregated Data ───────────────────────────────────────────────

@Serializable
data class VBandHalfHourRateData(
    val date: String = "",
    val hour: Int = 0,
    val minute: Int = 0,
    val rateValue: Int = 0
)

@Serializable
data class VBandHalfHourBpData(
    val date: String = "",
    val hour: Int = 0,
    val minute: Int = 0,
    val highValue: Int = 0,
    val lowValue: Int = 0
)

@Serializable
data class VBandHalfHourSportData(
    val date: String = "",
    val hour: Int = 0,
    val minute: Int = 0,
    val sportValue: Int = 0,
    val distance: Double = 0.0,
    val calories: Double = 0.0
)

@Serializable
data class VBandOriginHalfHourData(
    val halfHourRateData: List<VBandHalfHourRateData> = emptyList(),
    val halfHourBpData: List<VBandHalfHourBpData> = emptyList(),
    val halfHourSportData: List<VBandHalfHourSportData> = emptyList(),
    val allStep: Int = 0,
    val date: String = ""
)

// ─── Person Info ─────────────────────────────────────────────────────────────

@Serializable
data class VBandPersonInfo(
    val sex: VBandSex = VBandSex.MAN,
    val height: Int = 170,     // cm
    val weight: Int = 65,      // kg
    val age: Int = 25,
    val stepGoal: Int = 8000,
    val sleepGoal: Int = 480   // minutes
)

// ─── Custom / Personalization Settings ───────────────────────────────────────

@Serializable
data class VBandCustomSettingData(
    val is24Hour: Boolean = true,
    val metricSystem: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val autoHeartDetect: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val autoBpDetect: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val sportOverRemind: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val lowSpo2Remind: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val autoHrv: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val disconnectRemind: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val ppg: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val musicControl: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val autoTemperatureDetect: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val temperatureUnit: VBandTemperatureUnit = VBandTemperatureUnit.NONE,
    val bloodGlucoseDetection: VBandFunctionStatus = VBandFunctionStatus.UNKNOWN,
    val bloodGlucoseUnit: VBandBloodGlucoseUnit = VBandBloodGlucoseUnit.NONE
)

// ─── Night Turn Wrist ────────────────────────────────────────────────────────

@Serializable
data class VBandNightTurnWristData(
    val status: VBandNightTurnWristStatus = VBandNightTurnWristStatus.UNKNOWN,
    val isSupportCustomTime: Boolean = false,
    val isOpen: Boolean = false,
    val startHour: Int = 20,
    val startMinute: Int = 0,
    val endHour: Int = 8,
    val endMinute: Int = 0,
    val level: Int = 5,
    val defaultLevel: Int = 5
)

// ─── Screen Light ────────────────────────────────────────────────────────────

@Serializable
data class VBandScreenLightData(
    val status: VBandScreenLightStatus = VBandScreenLightStatus.UNKNOWN,
    val startHour: Int = 22,
    val startMinute: Int = 0,
    val endHour: Int = 7,
    val endMinute: Int = 0,
    val level: Int = 2,
    val otherLevel: Int = 4,
    val autoMode: Int = 0,      // 0=old, 1=auto, 2=manual
    val maxLevel: Int = 5
)

// ─── Screen Light Time ───────────────────────────────────────────────────────

@Serializable
data class VBandScreenLightTimeData(
    val status: VBandScreenLightTimeStatus = VBandScreenLightTimeStatus.UNKNOWN,
    val currentDuration: Int = 5,
    val recommendDuration: Int = 5,
    val maxDuration: Int = 30,
    val minDuration: Int = 3
)

// ─── Temperature Detect (manual measurement) ────────────────────────────────

@Serializable
data class VBandTemperatureDetectData(
    val isSupported: Boolean = false,
    val deviceState: Int = 0,      // 0x00=available, 0x01-0x07=busy, 0x08=low power
    val progress: Int = 0,
    val temperature: Float = 0f,
    val baseTemperature: Float = 0f
)

// ─── Temperature Record (daily) ─────────────────────────────────────────────

@Serializable
data class VBandTemperatureRecord(
    val allPackage: Int = 0,
    val packageNumber: Int = 0,
    val hour: Int = 0,
    val minute: Int = 0,
    val isManual: Boolean = false,
    val temperature: Float = 0f,
    val baseTemperature: Float = 0f
)

// ─── Health Remind ───────────────────────────────────────────────────────────

@Serializable
data class VBandHealthRemind(
    val type: VBandHealthRemindType = VBandHealthRemindType.ALL,
    val startHour: Int = 8,
    val startMinute: Int = 0,
    val endHour: Int = 20,
    val endMinute: Int = 0,
    val interval: Int = 30,
    val isOn: Boolean = false
)

// ─── Language Data ───────────────────────────────────────────────────────────

@Serializable
data class VBandLanguageData(
    val status: VBandOperateStatus = VBandOperateStatus.UNKNOWN,
    val language: VBandLanguage = VBandLanguage.UNKNOWN
)

// ─── All Health Read Progress ────────────────────────────────────────────────

@Serializable
data class VBandReadProgress(
    val progress: Float = 0f,   // [0-1]
    val isComplete: Boolean = false
)

// ═══════════════════════════════════════════════════════════════════════════════
// SYNC STATE MANAGEMENT
// ═══════════════════════════════════════════════════════════════════════════════

/** Sync operation status */
enum class VBandSyncStatus {
    IDLE, SYNCING, COMPLETED, ERROR, CANCELLED
}

/** Structured log level */
enum class VBandLogLevel {
    INFO, WARN, ERROR, SUCCESS, DEBUG
}

/** Overall sync state — pushed to UI for real-time feedback */
@Serializable
data class VBandSyncState(
    val status: VBandSyncStatus = VBandSyncStatus.IDLE,
    val currentStepName: String = "",
    val currentStepIndex: Int = 0,
    val totalSteps: Int = 0,
    val stepProgress: Float = 0f,   // [0-1] progress within current step
    val errorMessage: String = "",
    val syncedDay: Int = -1          // -1 = all days, 0 = today, 1 = yesterday etc.
)

/** A single structured log entry */
data class VBandSyncLogEntry(
    val timestamp: Long = 0L,
    val level: VBandLogLevel = VBandLogLevel.INFO,
    val message: String = "",
    val stepName: String = ""
)

/** Represents a selectable sync day */
data class VBandSyncDay(
    val dayOffset: Int,        // 0 = today, 1 = yesterday, etc.
    val label: String,         // "Today", "Yesterday", "2 days ago"
    val dateString: String     // "2026-04-04"
)
