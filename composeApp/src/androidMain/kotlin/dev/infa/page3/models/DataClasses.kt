package dev.infa.page3.models

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
    var supportsTemperature: Boolean = false,
    var supportsPlate: Boolean = false,
    var supportsMenstruation: Boolean = false,
    var supportsCustomWallpaper: Boolean = false,
    var supportsBloodOxygen: Boolean = false,
    var supportsBloodPressure: Boolean = false,
    var supportsFatigue: Boolean = false,
    var supportsOneKeyCheck: Boolean = false,
    var supportsWeather: Boolean = false,
    var newSleepProtocol: Boolean = false,
    var maxWatchFace: Int = 0,
    var supportsHrv: Boolean = false,
    var supportsMuslim: Boolean = false,

    // From DeviceSupportFunctionRsp
    var supportsTouch: Boolean = false,
    var supportsMoslin: Boolean = false,
    var supportsAppRevision: Boolean = false,
    var supportsBlePair: Boolean = false,
    var supportsGesture: Boolean = false,
    var supportsMusic: Boolean = false,
    var supportsVideo: Boolean = false,
    var supportsEbook: Boolean = false,
    var supportsCamera: Boolean = false,
    var supportsPhoneCall: Boolean = false,
    var supportsGame: Boolean = false
)
