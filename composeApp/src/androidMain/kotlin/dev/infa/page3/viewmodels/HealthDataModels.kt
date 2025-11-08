package dev.infa.page3.viewmodels

import java.text.SimpleDateFormat
import java.util.*

//data class SleepData(
//    val date: String = "",                    // Date in format "yyyy-MM-dd"
//    val totalDuration: Long = 0,              // Total sleep in seconds
//    val deepSleepDuration: Long = 0,          // Deep sleep in seconds
//    val lightSleepDuration: Long = 0,         // Light sleep in seconds
//    val remDuration: Long = 0,                // REM sleep in seconds
//    val awakeDuration: Long = 0,              // Awake time in seconds
//    val sleepTime: String = "",               // Sleep start time "HH:mm"
//    val wakeTime: String = "",                // Wake up time "HH:mm"
//    val sleepScore: Int = 0,                  // Sleep quality score (0-100)
//    val sleepEfficiency: Int = 0,             // Sleep efficiency percentage
//    val sleepQuality: String = "",            // Quality text: "Good", "Fair", "Poor"
//    val sleepStages: List<SleepStage> = emptyList() // Detailed sleep stages
//) {
//    /**
//     * Convert total duration from seconds to formatted time
//     */
//    fun getFormattedDuration(): String {
//        val hours = totalDuration / 3600
//        val minutes = (totalDuration % 3600) / 60
//        return "${hours}h ${minutes}m"
//    }
//
//    /**
//     * Get deep sleep duration in minutes
//     */
//    fun getDeepSleepMinutes(): Int = (deepSleepDuration / 60).toInt()
//
//    /**
//     * Get light sleep duration in minutes
//     */
//    fun getLightSleepMinutes(): Int = (lightSleepDuration / 60).toInt()
//
//    /**
//     * Get REM duration in minutes
//     */
//    fun getRemMinutes(): Int = (remDuration / 60).toInt()
//
//    /**
//     * Get awake duration in minutes
//     */
//    fun getAwakeMinutes(): Int = (awakeDuration / 60).toInt()
//
//    /**
//     * Get sleep efficiency percentage
//     */
//    fun getSleepEfficiencyPercentage(): Int = sleepEfficiency
//
//}
//
///**
// * Data class for individual sleep stages throughout the night
// */
//data class SleepStage(
//    val timestamp: Long = 0,                  // Unix timestamp
//    val type: SleepType = SleepType.LIGHT,    // Type of sleep stage
//    val duration: Int = 0                     // Duration in minutes
//)
//
///**
// * Enum for sleep stage types based on SDK documentation
// */
//enum class SleepType {
//    DEEP,           // Deep sleep (type 1 in SDK)
//    LIGHT,          // Light sleep (type 2 in SDK)
//    REM,            // Rapid Eye Movement (derived)
//    AWAKE           // Wake up periods (type 3 in SDK)
//}
//
///**
// * Step data class for daily step tracking
// */
data class StepData(
    val date: String = "",                    // Date in format "yyyy-MM-dd"
    val totalSteps: Long = 0,                 // Total steps
    val runningSteps: Long = 0,               // Running/aerobic steps
    val calories: Long = 0,                   // Calories burned
    val distance: Long = 0,                   // Distance in meters
    val sportDuration: Long = 0,              // Sport duration in seconds
    val sleepDuration: Long = 0,              // Sleep duration in seconds
    val detailData: List<StepDetailEntry> = emptyList() // Detailed hourly data
) {
    /**
     * Get formatted distance in km
     */
    fun getFormattedDistance(): String {
        val km = distance / 1000.0
        return String.format("%.2f km", km)
    }

    /**
     * Get formatted calories
     */
    fun getFormattedCalories(): String {
        return "${calories} kcal"
    }

    /**
     * Get formatted sport duration
     */
    fun getFormattedSportDuration(): String {
        val hours = sportDuration / 3600
        val minutes = (sportDuration % 3600) / 60
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }
}

/**
 * Detailed step data entry (15-minute intervals)
 */
data class StepDetailEntry(
    val timeIndex: Int = 0,                   // Time index (0-95, 15-minute intervals)
    val steps: Int = 0,                       // Steps in this interval
    val calories: Int = 0,                    // Calories in this interval
    val distance: Int = 0,                    // Distance in this interval
    val runningSteps: Int = 0                 // Running steps in this interval
)

/**
 * Heart rate data class
 */
data class HeartRateData(
    val date: String = "",                    // Date in format "yyyy-MM-dd"
    val heartRateValues: List<HeartRateEntry> = emptyList(), // Heart rate entries
    val averageHeartRate: Int = 0,            // Average heart rate
    val maxHeartRate: Int = 0,                // Maximum heart rate
    val minHeartRate: Int = 0,                // Minimum heart rate
    val restingHeartRate: Int = 0             // Resting heart rate
) {
    /**
     * Get formatted average heart rate
     */
    fun getFormattedAverageHeartRate(): String {
        return "${averageHeartRate} bpm"
    }

    /**
     * Get heart rate zone
     */
    fun getHeartRateZone(age: Int): HeartRateZone {
        val maxHR = 220 - age
        val percentage = (averageHeartRate.toFloat() / maxHR) * 100
        
        return when {
            percentage < 50 -> HeartRateZone.RECOVERY
            percentage < 60 -> HeartRateZone.FAT_BURN
            percentage < 70 -> HeartRateZone.AEROBIC
            percentage < 80 -> HeartRateZone.ANAEROBIC
            percentage < 90 -> HeartRateZone.MAXIMUM
            else -> HeartRateZone.NEUROMUSCULAR
        }
    }
}

/**
 * Heart rate entry (5-minute intervals)
 */
data class HeartRateEntry(
    val timestamp: Long = 0,                  // Unix timestamp
    val heartRate: Int = 0,                   // Heart rate value
    val minuteOfDay: Int = 0                  // Minute of day (0-1439)
)

/**
 * Heart rate zones
 */
enum class HeartRateZone {
    RECOVERY,        // < 50% of max HR
    FAT_BURN,        // 50-60% of max HR
    AEROBIC,         // 60-70% of max HR
    ANAEROBIC,       // 70-80% of max HR
    MAXIMUM,         // 80-90% of max HR
    NEUROMUSCULAR    // > 90% of max HR
}

/**
 * Blood pressure data class
 */
data class BloodPressureData(
    val date: String = "",                    // Date in format "yyyy-MM-dd"
    val systolicBP: Int = 0,                  // Systolic blood pressure
    val diastolicBP: Int = 0,                 // Diastolic blood pressure
    val heartRate: Int = 0,                   // Heart rate during measurement
    val timestamp: Long = 0,                  // Measurement timestamp
    val measurementType: BloodPressureType = BloodPressureType.AUTOMATIC
) {
    /**
     * Get blood pressure category
     */
    fun getBloodPressureCategory(): BloodPressureCategory {
        return when {
            systolicBP < 120 && diastolicBP < 80 -> BloodPressureCategory.NORMAL
            systolicBP < 130 && diastolicBP < 80 -> BloodPressureCategory.ELEVATED
            systolicBP < 140 || diastolicBP < 90 -> BloodPressureCategory.HIGH_STAGE_1
            systolicBP < 180 || diastolicBP < 120 -> BloodPressureCategory.HIGH_STAGE_2
            else -> BloodPressureCategory.HYPERTENSIVE_CRISIS
        }
    }

    /**
     * Get formatted blood pressure
     */
    fun getFormattedBloodPressure(): String {
        return "$systolicBP/$diastolicBP mmHg"
    }
}

/**
 * Blood pressure measurement types
 */
enum class BloodPressureType {
    AUTOMATIC,       // Automatic measurement
    MANUAL           // Manual measurement
}

/**
 * Blood pressure categories
 */
enum class BloodPressureCategory {
    NORMAL,                    // < 120/80
    ELEVATED,                  // 120-129/<80
    HIGH_STAGE_1,              // 130-139/80-89
    HIGH_STAGE_2,              // 140-179/90-119
    HYPERTENSIVE_CRISIS        // >= 180/120
}

/**
 * Blood oxygen data class
 */
data class BloodOxygenData(
    val date: String = "",                    // Date in format "yyyy-MM-dd"
    val averageSpO2: Int = 0,                 // Average SpO2 percentage
    val minSpO2: Int = 0,                     // Minimum SpO2 percentage
    val maxSpO2: Int = 0,                     // Maximum SpO2 percentage
    val hourlyData: List<SpO2Entry> = emptyList() // Hourly data points
) {
    /**
     * Get SpO2 level
     */
    fun getSpO2Level(): SpO2Level {
        return when {
            averageSpO2 >= 95 -> SpO2Level.NORMAL
            averageSpO2 >= 90 -> SpO2Level.MILD_HYPOXEMIA
            averageSpO2 >= 85 -> SpO2Level.MODERATE_HYPOXEMIA
            else -> SpO2Level.SEVERE_HYPOXEMIA
        }
    }

    /**
     * Get formatted average SpO2
     */
    fun getFormattedAverageSpO2(): String {
        return "${averageSpO2}%"
    }
}

/**
 * SpO2 entry for hourly data
 */
data class SpO2Entry(
    val hour: Int = 0,                        // Hour of day (0-23)
    val minSpO2: Int = 0,                     // Minimum SpO2 for this hour
    val maxSpO2: Int = 0,                     // Maximum SpO2 for this hour
    val timestamp: Long = 0                   // Hour timestamp
)

/**
 * SpO2 levels
 */
enum class SpO2Level {
    NORMAL,                 // >= 95%
    MILD_HYPOXEMIA,        // 90-94%
    MODERATE_HYPOXEMIA,    // 85-89%
    SEVERE_HYPOXEMIA       // < 85%
}

/**
 * Temperature data class
 */
data class TemperatureData(
    val date: String = "",                    // Date in format "yyyy-MM-dd"
    val averageTemperature: Float = 0f,       // Average temperature in Celsius
    val minTemperature: Float = 0f,           // Minimum temperature
    val maxTemperature: Float = 0f,           // Maximum temperature
    val temperatureEntries: List<TemperatureEntry> = emptyList() // Temperature entries
) {
    /**
     * Get formatted average temperature
     */
    fun getFormattedAverageTemperature(): String {
        return String.format("%.1f°C", averageTemperature)
    }

    /**
     * Get temperature status
     */
    fun getTemperatureStatus(): TemperatureStatus {
        return when {
            averageTemperature < 36.0 -> TemperatureStatus.LOW
            averageTemperature <= 37.2 -> TemperatureStatus.NORMAL
            averageTemperature <= 38.0 -> TemperatureStatus.ELEVATED
            else -> TemperatureStatus.HIGH
        }
    }
}

/**
 * Temperature entry
 */
data class TemperatureEntry(
    val timestamp: Long = 0,                  // Measurement timestamp
    val temperature: Float = 0f,              // Temperature value
    val measurementType: TemperatureType = TemperatureType.AUTOMATIC
)

/**
 * Temperature measurement types
 */
enum class TemperatureType {
    AUTOMATIC,       // Automatic measurement
    MANUAL           // Manual measurement
}

/**
 * Temperature status
 */
enum class TemperatureStatus {
    LOW,            // < 36.0°C
    NORMAL,         // 36.0-37.2°C
    ELEVATED,       // 37.2-38.0°C
    HIGH            // > 38.0°C
}

/**
 * HRV (Heart Rate Variability) data class
 */
data class HRVData(
    val date: String = "",                    // Date in format "yyyy-MM-dd"
    val averageHRV: Int = 0,                  // Average HRV value
    val minHRV: Int = 0,                      // Minimum HRV value
    val maxHRV: Int = 0,                      // Maximum HRV value
    val hrvEntries: List<HRVEntry> = emptyList() // HRV entries (30-minute intervals)
) {
    /**
     * Get formatted average HRV
     */
    fun getFormattedAverageHRV(): String {
        return "${averageHRV} ms"
    }

    /**
     * Get HRV status
     */
    fun getHRVStatus(): HRVStatus {
        return when {
            averageHRV >= 50 -> HRVStatus.EXCELLENT
            averageHRV >= 40 -> HRVStatus.GOOD
            averageHRV >= 30 -> HRVStatus.FAIR
            averageHRV >= 20 -> HRVStatus.POOR
            else -> HRVStatus.VERY_POOR
        }
    }
}

/**
 * HRV entry (30-minute intervals)
 */
data class HRVEntry(
    val timestamp: Long = 0,                  // Measurement timestamp
    val hrvValue: Int = 0,                    // HRV value
    val minuteOfDay: Int = 0                  // Minute of day
)

/**
 * HRV status levels
 */
enum class HRVStatus {
    EXCELLENT,      // >= 50ms
    GOOD,           // 40-49ms
    FAIR,           // 30-39ms
    POOR,           // 20-29ms
    VERY_POOR       // < 20ms
}

/**
 * Pressure data class (stress level)
 */
data class PressureData(
    val date: String = "",                    // Date in format "yyyy-MM-dd"
    val averagePressure: Int = 0,             // Average pressure value
    val minPressure: Int = 0,                 // Minimum pressure value
    val maxPressure: Int = 0,                 // Maximum pressure value
    val pressureEntries: List<PressureEntry> = emptyList() // Pressure entries (30-minute intervals)
) {
    /**
     * Get formatted average pressure
     */
    fun getFormattedAveragePressure(): String {
        return "${averagePressure / 10}" // Divide by 10 as per SDK documentation
    }

    /**
     * Get pressure status
     */
    fun getPressureStatus(): PressureStatus {
        val normalizedValue = averagePressure / 10
        return when {
            normalizedValue < 30 -> PressureStatus.RELAXED
            normalizedValue < 50 -> PressureStatus.NORMAL
            normalizedValue < 70 -> PressureStatus.STRESSED
            normalizedValue < 85 -> PressureStatus.HIGH_STRESS
            else -> PressureStatus.VERY_HIGH_STRESS
        }
    }
}

/**
 * Pressure entry (30-minute intervals)
 */
data class PressureEntry(
    val timestamp: Long = 0,                  // Measurement timestamp
    val pressureValue: Int = 0,               // Pressure value
    val minuteOfDay: Int = 0                  // Minute of day
)

/**
 * Pressure status levels
 */
enum class PressureStatus {
    RELAXED,              // < 30
    NORMAL,               // 30-49
    STRESSED,             // 50-69
    HIGH_STRESS,          // 70-84
    VERY_HIGH_STRESS      // >= 85
}

/**
 * Comprehensive health data for a specific date
 */
data class DailyHealthData(
    val date: String = "",                    // Date in format "yyyy-MM-dd"
    val sleepData: SleepData? = null,         // Sleep data for the day
    val stepData: StepData? = null,           // Step data for the day
    val heartRateData: HeartRateData? = null, // Heart rate data for the day
    val bloodPressureData: List<BloodPressureData> = emptyList(), // Blood pressure measurements
    val bloodOxygenData: BloodOxygenData? = null, // Blood oxygen data
    val temperatureData: TemperatureData? = null, // Temperature data
    val hrvData: HRVData? = null,             // HRV data
    val pressureData: PressureData? = null    // Pressure/stress data
) {
    /**
     * Check if any health data is available for this date
     */
    fun hasAnyData(): Boolean {
        return sleepData != null || 
               stepData != null || 
               heartRateData != null || 
               bloodPressureData.isNotEmpty() ||
               bloodOxygenData != null ||
               temperatureData != null ||
               hrvData != null ||
               pressureData != null
    }

    /**
     * Get overall health score for the day (0-100)
     */
    fun getOverallHealthScore(): Int {
        var score = 0
        var count = 0

        sleepData?.let {
            score += it.sleepScore
            count++
        }

        stepData?.let {
            // Calculate step score based on 10,000 steps target
            val stepScore = ((it.totalSteps.toFloat() / 10000) * 100).toInt().coerceIn(0, 100)
            score += stepScore
            count++
        }

        heartRateData?.let {
            // Calculate heart rate score based on normal range
            val hrScore = when {
                it.averageHeartRate in 60..100 -> 100
                it.averageHeartRate in 50..110 -> 80
                it.averageHeartRate in 40..120 -> 60
                else -> 40
            }
            score += hrScore
            count++
        }

        return if (count > 0) score / count else 0
    }
}
