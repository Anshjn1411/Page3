package dev.infa.page3.models

import org.json.JSONObject

// Data class for discovered devices
data class SmartWatch(
    val deviceName: String,
    val deviceAddress: String,
    var rssi: Int,
    val deviceType: String = "Unknown"
)

// Event class for connection status
data class BluetoothEvent(
    val connect: Boolean,
    val deviceName: String = "",
    val deviceAddress: String = ""
)

// Health data classes
data class HealthData(
    // Heart metrics
    var heartRate: Int = 0,        // BPM
    var hrvValue: Int = 0,         // HRV in ms
    var pressure: Int = 0,         // Stress / pressure value

    // Blood metrics
    var spo2: Int = 0,             // Blood oxygen %
    var systolic: Int = 0,
    var diastolic: Int = 0,

    // Body temperature
    var temperature: Float = 0.0f, // Celsius

    // Sports / activity metrics
    var steps: Int = 0,
    var calories: Int = 0,         // kcal
    var distance: Int = 0,         // meters

    // Exercise / sports tracking
    var sportStatus: String? = null, // e.g., "started", "paused", "ended"
)

data class HealthSettings(
    // Heart Rate
    var heartRateEnabled: Boolean = false,
    var heartRateInterval: Int = 30, // minutes

    // Blood Oxygen (SpO2)
    var bloodOxygenEnabled: Boolean = false,

    // Blood Pressure
    var bloodPressureEnabled: Boolean = false,
    var bloodPressureSystolic: Int? = null,
    var bloodPressureDiastolic: Int? = null,

    // Body Temperature
    var temperatureEnabled: Boolean = false,

    // HRV
    var hrvEnabled: Boolean = false,

    // Pressure/Stress
    var pressureEnabled: Boolean = false,

    // Sports Goals
    var stepGoal: Int? = null,
    var calorieGoal: Int? = null,     // kcal
    var distanceGoal: Int? = null,    // meters
    var sportMinuteGoal: Int? = null, // minutes
    var sleepMinuteGoal: Int? = null, // minutes

    // Wearing Calibration
    var wearingCalibrationInProgress: Boolean = false,

    // Camera Control
    var cameraModeActive: Boolean = false,

    // Message Push
    var messagePushEnabled: Boolean = false,

    // Touch / Gesture Control
    var touchControlAppType: Int? = null,
    var touchControlStrength: Int? = null,
    var gestureControlAppType: Int? = null,
    var gestureControlStrength: Int? = null
)

data class DeviceCapabilities(
    // From SetTimeRsp
    val supportTemperature: Boolean = false,
    val supportPlate: Boolean = false,
    val supportMenstruation: Boolean = false,
    val supportCustomWallpaper: Boolean = false,
    val supportBloodOxygen: Boolean = false,
    val supportBloodPressure: Boolean = false,
    val supportFeature: Boolean = false,
    val supportOneKeyCheck: Boolean = false,
    val supportWeather: Boolean = false,
    val newSleepProtocol: Boolean = false,
    val maxWatchFace: Int = 0,
    val supportHrv: Boolean = false,

    // From DeviceSupportFunctionRsp
    val supportTouch: Boolean = false,
    val supportMoslin: Boolean = false,
    val supportAPPRevision: Boolean = false,
    val supportBlePair: Boolean = false,
    val supportGesture: Boolean = false,
    val supportRingMusic: Boolean = false,
    val supportRingVideo: Boolean = false,
    val supportRingEbook: Boolean = false,
    val supportRingCamera: Boolean = false,
    val supportRingPhoneCall: Boolean = false,
    val supportRingGame: Boolean = false
) {
    fun toJson(): String {
        return JSONObject().apply {
            put("supportTemperature", supportTemperature)
            put("supportPlate", supportPlate)
            put("supportMenstruation", supportMenstruation)
            put("supportCustomWallpaper", supportCustomWallpaper)
            put("supportBloodOxygen", supportBloodOxygen)
            put("supportBloodPressure", supportBloodPressure)
            put("supportFeature", supportFeature)
            put("supportOneKeyCheck", supportOneKeyCheck)
            put("supportWeather", supportWeather)
            put("newSleepProtocol", newSleepProtocol)
            put("maxWatchFace", maxWatchFace)
            put("supportHrv", supportHrv)
            put("supportTouch", supportTouch)
            put("supportMoslin", supportMoslin)
            put("supportAPPRevision", supportAPPRevision)
            put("supportBlePair", supportBlePair)
            put("supportGesture", supportGesture)
            put("supportRingMusic", supportRingMusic)
            put("supportRingVideo", supportRingVideo)
            put("supportRingEbook", supportRingEbook)
            put("supportRingCamera", supportRingCamera)
            put("supportRingPhoneCall", supportRingPhoneCall)
            put("supportRingGame", supportRingGame)
        }.toString()
    }
    fun toList(): List<Pair<String, Boolean>> {
        return listOf(
            "Temperature" to supportTemperature,
            "Plate" to supportPlate,
            "Menstruation" to supportMenstruation,
            "Custom Wallpaper" to supportCustomWallpaper,
            "Blood Oxygen" to supportBloodOxygen,
            "Blood Pressure" to supportBloodPressure,
            "Feature" to supportFeature,
            "One Key Check" to supportOneKeyCheck,
            "Weather" to supportWeather,
            "New Sleep Protocol" to newSleepProtocol,
            "HRV" to supportHrv,
            "Touch" to supportTouch,
            "Gesture" to supportGesture,
            "Ring Music" to supportRingMusic,
            "Ring Video" to supportRingVideo,
            "Ring Ebook" to supportRingEbook,
            "Ring Camera" to supportRingCamera,
            "Ring Phone Call" to supportRingPhoneCall,
            "Ring Game" to supportRingGame
        )
    }


    companion object {
        fun fromJson(json: String): DeviceCapabilities? {
            return try {
                val obj = JSONObject(json)
                DeviceCapabilities(
                    supportTemperature = obj.optBoolean("supportTemperature", false),
                    supportPlate = obj.optBoolean("supportPlate", false),
                    supportMenstruation = obj.optBoolean("supportMenstruation", false),
                    supportCustomWallpaper = obj.optBoolean("supportCustomWallpaper", false),
                    supportBloodOxygen = obj.optBoolean("supportBloodOxygen", false),
                    supportBloodPressure = obj.optBoolean("supportBloodPressure", false),
                    supportFeature = obj.optBoolean("supportFeature", false),
                    supportOneKeyCheck = obj.optBoolean("supportOneKeyCheck", false),
                    supportWeather = obj.optBoolean("supportWeather", false),
                    newSleepProtocol = obj.optBoolean("newSleepProtocol", false),
                    maxWatchFace = obj.optInt("maxWatchFace", 0),
                    supportHrv = obj.optBoolean("supportHrv", false),
                    supportTouch = obj.optBoolean("supportTouch", false),
                    supportMoslin = obj.optBoolean("supportMoslin", false),
                    supportAPPRevision = obj.optBoolean("supportAPPRevision", false),
                    supportBlePair = obj.optBoolean("supportBlePair", false),
                    supportGesture = obj.optBoolean("supportGesture", false),
                    supportRingMusic = obj.optBoolean("supportRingMusic", false),
                    supportRingVideo = obj.optBoolean("supportRingVideo", false),
                    supportRingEbook = obj.optBoolean("supportRingEbook", false),
                    supportRingCamera = obj.optBoolean("supportRingCamera", false),
                    supportRingPhoneCall = obj.optBoolean("supportRingPhoneCall", false),
                    supportRingGame = obj.optBoolean("supportRingGame", false)
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
