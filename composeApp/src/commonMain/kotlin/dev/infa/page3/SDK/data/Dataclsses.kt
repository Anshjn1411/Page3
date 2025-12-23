package dev.infa.page3.SDK.data

import androidx.lifecycle.ViewModel
import dev.infa.page3.SDK.ui.utils.FormatUtils
import dev.infa.page3.SDK.viewModel.SleepStage
import kotlinx.serialization.SealedSerializationApi
import kotlinx.serialization.Serializable

@Serializable
data class HourlyStepData(
    val hour: Int = 0,
    val steps: Int = 0,
    val calories: Int = 0
)

@Serializable
data class DayStepData(
    val date: String = "",
    val totalSteps: Int = 0,
    val totalCalories: Int = 0,
    val totalDistance: Int = 0,
    val hourlyData: List<HourlyStepData> = emptyList()
)

@Serializable
data class WeeklySummary(
    val bestDay: DayStepData? = null,
    val averageSteps: Int = 0,
    val averageCalories: Long = 0,
    val allDays: List<DayStepData> = emptyList()
)

@Serializable
data class HeartRateData(
    val date: String = "",
    val heartRateValues: List<HeartRateEntry> = emptyList(),
    val averageHeartRate: Int = 0,
    val maxHeartRate: Int = 0,
    val minHeartRate: Int = 0
)

@Serializable
data class HeartRateEntry(
    val timestamp: Long = 0,
    val heartRate: Int = 0,
    val minuteOfDay: Int = 0
)

@Serializable
data class SpO2Data(
    val date: String = "",
    val spo2Values: List<SpO2Entry> = emptyList(),
    val averageSpO2: Int = 0,
    val maxSpO2: Int = 0,
    val minSpO2: Int = 0
)

@Serializable
data class SpO2Entry(
    val timestamp: Long = 0,
    val spo2Value: Int = 0,
    val hourOfDay: Int = 0
)

@Serializable
data class HrvData(
    val date: String = "",
    val hrvValues: List<HrvEntry> = emptyList(),
    val averageHrv: Int = 0,
    val maxHrv: Int = 0,
    val minHrv: Int = 0
)

@Serializable
data class HrvEntry(
    val timestamp: Long = 0,
    val hrvValue: Int = 0,
    val minuteOfDay: Int = 0
)

@Serializable
data class BpData(
    val date: String = "",
    val bpValues: List<BpEntry> = emptyList(),
    val averageSystolic: Int = 0,
    val averageDiastolic: Int = 0,
    val maxSystolic: Int = 0,
    val minSystolic: Int = 0
) {
    fun getFormattedAverageBp(): String = "$averageSystolic/$averageDiastolic mmHg"

    fun getBpStatus(): String {
        return when {
            averageSystolic >= 140 || averageDiastolic >= 90 -> "High"
            averageSystolic >= 130 || averageDiastolic >= 80 -> "Elevated"
            else -> "Normal"
        }
    }
}
@Serializable
data class BpEntry(
    val timestamp: Long = 0,
    val heartRate: Int = 90,
    val systolic: Int = 0,
    val diastolic: Int = 0,
    val minuteOfDay: Int = 0
)

data class PressureData(
    val date: String,
    val entries: List<PressureEntry> = emptyList(),
    val averagePressure: Float = 0f,
    val maxPressure: Float = entries.maxOfOrNull { it.pressureValue } ?: 0f,
    val minPressure: Float = entries.minOfOrNull { it.pressureValue } ?: 0f
)

data class PressureEntry(
    val minuteOfDay: Int,
    val pressureValue: Float
)

data class TemperatureData(
    val date: String,
    val entries: List<TemperatureEntry> = emptyList(),
    val averageTemp: Float = 0f,
    val maxTemp: Float = 0f,
    val minTemp: Float = 0f
)

data class TemperatureEntry(
    val minuteOfDay: Int,
    val temperature: Float
)



// ============================================
// commonMain/models/ExerciseModels.kt - Data Classes
// ============================================
@Serializable
data class ExerciseData(
    val sportType: Int,
    val isActive: Boolean,
    val isPaused: Boolean,
    val elapsedSeconds: Int,
    val heartRate: Int,
    val steps: Int,
    val distanceMeters: Int,
    val calories: Int
) {
    fun getFormattedDuration(): String {
        val hours = elapsedSeconds / 3600
        val minutes = (elapsedSeconds % 3600) / 60
        val seconds = elapsedSeconds % 60
        return "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }

    fun getFormattedDistance(): String {
        return FormatUtils.formatDistance(distanceMeters)
    }

    fun getFormattedDistanceFromMeters(): String {
        return if (distanceMeters >= 1000) {
            FormatUtils.formatDecimal(distanceMeters / 1000.0, 2) + " km"
        } else {
            "$distanceMeters m"
        }
    }
}
@Serializable
data class ExerciseSummary(
    val sportType: Int,
    val sportName: String,
    val startTimestamp: Long,
    val durationSeconds: Int,
    val distanceMeters: Int,
    val calories: Int,
    val averageHeartRate: Int,
    val steps: Int,
    val date: String
) {
    fun getFormattedDuration(): String {
        val hours = durationSeconds / 3600
        val minutes = (durationSeconds % 3600) / 60
        return if (hours > 0) {
            "$hours hr $minutes min"
        } else {
            "$minutes min"
        }
    }

    fun getFormattedDistance(): String {
        return if (distanceMeters >= 1000) {
            FormatUtils.formatDecimal(distanceMeters / 1000.0, 2) + " km"
        } else {
            "$distanceMeters m"
        }
    }
}
@Serializable
data class SleepData(
    val date: String,
    val totalDuration: Double,
    val awakeDuration: Int,
    val sleepScore: Int,
    val sleepEfficiency: Double,
    val deepSleep: Int,
    val lightSleep: Int,
    val remSleep: Int,
    val sleepStartTime: String,
    val sleepEndTime: String,
    val stages: List<SleepStage>
)

data class DeviceCapabilities(

    // Health
    val hasHeartRate: Boolean = false,
    val hasSpO2: Boolean = false,
    val hasHRV: Boolean = false,
    val hasBloodPressure: Boolean = false,
    val hasBodyTemperature: Boolean = false,
    val hasFatigue: Boolean = false,
    val hasOneKeyCheck: Boolean = false,

    // Activity & Sleep
    val hasStepTracking: Boolean = true,
    val hasSleepTracking: Boolean = false,
    val hasExerciseMode: Boolean = false,

    // Watch & System
    val supportsWatchFace: Boolean = false,
    val supportsCustomWatchFace: Boolean = false,
    val maxWatchFaces: Int = 0,
    val supportsWeather: Boolean = false,
    val supportsMenstruation: Boolean = false,

    // Device Functional Support
    val supportsTouch: Boolean = false,
    val supportsGesture: Boolean = false,
    val supportsBlePairing: Boolean = false,
    val supportsHeartRateCalibration: Boolean = false,

    // Ring / App Features
    val supportsMusic: Boolean = false,
    val supportsVideo: Boolean = false,
    val supportsEbook: Boolean = false,
    val supportsCamera: Boolean = false,
    val supportsPhoneCall: Boolean = false,
    val supportsGame: Boolean = false,
    val supportsMuslimMode: Boolean = false
) {
    fun toCapabilityMap(): Map<String, Boolean> = mapOf(

        // Health
        "Heart Rate" to hasHeartRate,
        "SpO2" to hasSpO2,
        "HRV" to hasHRV,
        "Blood Pressure" to hasBloodPressure,
        "Body Temperature" to hasBodyTemperature,
        "Fatigue" to hasFatigue,
        "One Key Check" to hasOneKeyCheck,

        // Activity
        "Steps" to hasStepTracking,
        "Sleep Tracking" to hasSleepTracking,
        "Exercise Mode" to hasExerciseMode,

        // Watch
        "Watch Face" to supportsWatchFace,
        "Custom Watch Face" to supportsCustomWatchFace,
        "Weather" to supportsWeather,
        "Menstrual Cycle" to supportsMenstruation,

        // Device Features
        "Touch Support" to supportsTouch,
        "Gesture Support" to supportsGesture,
        "BLE Pairing" to supportsBlePairing,
        "HR Calibration" to supportsHeartRateCalibration,

        // Media / Ring
        "Music" to supportsMusic,
        "Video" to supportsVideo,
        "Ebook" to supportsEbook,
        "Camera" to supportsCamera,
        "Phone Call" to supportsPhoneCall,
        "Game" to supportsGame,

        // Others
        "Muslim Mode" to supportsMuslimMode
    )

}


@Serializable
data class TouchSettings(
    val appType: Int = 0,
    val isTouch: Boolean = true,
    val strength: Int = 5
)
@Serializable
data class UserSettings(
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    val timeFormat: TimeFormat = TimeFormat.HOUR_24,
    val lowBatteryPrompt: Boolean = true,
    val themeStyle: ThemeStyle = ThemeStyle.LIGHT
)
@Serializable
enum class UnitSystem(val displayName: String) {
    METRIC("Metric System"),
    IMPERIAL("Imperial System")
}

enum class TimeFormat(val displayName: String) {
    HOUR_12("12 Hour"),
    HOUR_24("24 Hour")
}

enum class ThemeStyle(val displayName: String) {
    LIGHT("Light"),
    DARK("Dark"),
    AUTO("Auto")
}

enum class AppType(val code: Int, val displayName: String) {
    CLOSE(0, "Close"),
    MUSIC(1, "Music"),
    VIDEO(2, "Video"),
    MUSLIM(3, "Muslim"),
    EBOOK(4, "E-Book"),
    CAMERA(5, "Camera"),
    PHONE_CALL(6, "Phone Call"),
    GAME(7, "Game"),
    HEART(8, "Heart")
}

data class DeviceInfo(
    val firmwareVersion: String = "Unknown",
    val hardwareVersion: String = "Unknown",
    val batteryLevel: Int = 0,
    val serialNumber: String = "Unknown"
)



@Serializable
data class MeasurementResult(
    val success: Boolean,
    val heartRate: Int = 0,
    val systolic: Int = 0,
    val diastolic: Int = 0,
    val message: String = ""
)
@Serializable
data class OneClickResult(
    val heartRate: Int,
    val bloodOxygen: Int,
    val systolic: Int,
    val diastolic: Int,
    val hrv: Int,
    val stress: Int,
    val temperature: Float,
    val rri: Int
) {
    override fun toString(): String {
        return """
            One-Click Measurement Results:
            ├─ Heart Rate: $heartRate bpm
            ├─ Blood Oxygen: $bloodOxygen%
            ├─ Blood Pressure: $systolic/$diastolic mmHg
            ├─ HRV: $hrv ms
            ├─ Stress Level: $stress
            ├─ Temperature: ${temperature}°C
            └─ RRI: $rri ms
        """.trimIndent()
    }
}
@Serializable
data class RawDataResult(
    val heartRate: Int = 0,
    val bloodOxygen: Int = 0,
    val hrv: Int = 0,
    val stress: Int = 0,
    val ppgCount: Int = 0,
    val greenLightPpgL: Int = 0,
    val greenLightPpgH: Int = 0,
    val redLightPpgL: Int = 0,
    val redLightPpgH: Int = 0,
    val infraredPpgL: Int = 0,
    val infraredPpgH: Int = 0,
    val xL: Int = 0,
    val xH: Int = 0,
    val yL: Int = 0,
    val yH: Int = 0,
    val zL: Int = 0,
    val zH: Int = 0
) {
    override fun toString(): String {
        return """
            Raw Sensor Data:
            ├─ Heart Rate: $heartRate bpm
            ├─ Blood Oxygen: $bloodOxygen%
            ├─ HRV: $hrv ms
            ├─ Stress: $stress
            ├─ PPG Count: $ppgCount
            ├─ Green Light PPG: L=$greenLightPpgL, H=$greenLightPpgH
            ├─ Red Light PPG: L=$redLightPpgL, H=$redLightPpgH
            ├─ Infrared PPG: L=$infraredPpgL, H=$infraredPpgH
            └─ Accelerometer: X=[$xL,$xH], Y=[$yL,$yH], Z=[$zL,$zH]
        """.trimIndent()
    }
}

@Serializable
data class StartEndTimeEntity(
    val startHour: Int,   // 0–23
    val startMinute: Int, // 0–59
    val endHour: Int,     // 0–23
    val endMinute: Int    // 0–59
)
