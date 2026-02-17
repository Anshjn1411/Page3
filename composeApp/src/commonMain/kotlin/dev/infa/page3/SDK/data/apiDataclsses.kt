package dev.infa.page3.SDK.data


import kotlinx.serialization.Serializable

// ======================== REQUEST DATA CLASSES ========================

/**
 * Request model for saving step data
 */
@Serializable
data class SaveStepDataRequest(
    val date: String,                    // YYYY-MM-DD format
    val totalSteps: Int,
    val totalCalories: Int,
    val totalDistance: Int,              // in meters
    val hourlyData: List<HourlyStepData>
)
/**
 * Request model for saving heart rate data
 */
@Serializable
data class SaveHeartRateRequest(
    val date: String,                    // YYYY-MM-DD format
    val heartRateValues: List<HeartRateValue>,
    val averageHeartRate: Int,
    val maxHeartRate: Int,
    val minHeartRate: Int
)

@Serializable
data class HeartRateValue(
    val timestamp: Long,                 // Unix timestamp in milliseconds
    val heartRate: Int,                  // BPM
    val minuteOfDay: Int                 // 0-1439
)

/**
 * Request model for saving SpO2 data
 */
@Serializable
data class SaveSpO2Request(
    val date: String,                    // YYYY-MM-DD format
    val spo2Values: List<SpO2Value>,
    val averageSpO2: Int,
    val maxSpO2: Int,
    val minSpO2: Int
)

@Serializable
data class SpO2Value(
    val timestamp: Long,                 // Unix timestamp in milliseconds
    val spo2Value: Int,                  // SpO2 percentage (95-100 typical)
    val hourOfDay: Int                   // 0-23
)

/**
 * Request model for saving HRV data
 */
@Serializable
data class SaveHRVRequest(
    val date: String,                    // YYYY-MM-DD format
    val hrvValues: List<HRVValue>,
    val averageHrv: Int,
    val maxHrv: Int,
    val minHrv: Int
)

@Serializable
data class HRVValue(
    val timestamp: Long,                 // Unix timestamp in milliseconds
    val hrvValue: Int,                   // HRV in milliseconds
    val minuteOfDay: Int                 // 0-1439
)

/**
 * Request model for saving blood pressure data
 */
@Serializable
data class SaveBloodPressureRequest(
    val date: String,                    // YYYY-MM-DD format
    val bpValues: List<BloodPressureValue>,
    val averageSystolic: Double,
    val averageDiastolic: Double,
    val maxSystolic: Int,
    val minSystolic: Int
)

@Serializable
data class BloodPressureValue(
    val timestamp: Long,                 // Unix timestamp in milliseconds
    val heartRate: Int = 90,             // Default: 90
    val systolic: Int,
    val diastolic: Int,
    val minuteOfDay: Int                 // 0-1439
)

/**
 * Request model for saving exercise data
 */
@Serializable
data class SaveExerciseRequest(
    val date: String,                    // YYYY-MM-DD format
    val sportType: Int,
    val sportName: String? = null,
    val startTimestamp: Long,            // Unix timestamp in milliseconds
    val durationSeconds: Int,
    val distanceMeters: Int,
    val calories: Int,
    val averageHeartRate: Int,
    val steps: Int
)

// ======================== RESPONSE DATA CLASSES ========================

/**
 * Generic API response wrapper
 */
@Serializable
data class HealthApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val error: String? = null
)

/**
 * Step data response
 */
@Serializable
data class StepDataResponse(
    val _id: String,
    val userId: String,
    val date: String,
    val totalSteps: Int,
    val totalCalories: Int,
    val totalDistance: Int,
    val hourlyData: List<HourlyStepData>,
    val createdAt: String
)

@Serializable
data class HourlyStepDataResponse(
    val hour: Int,
    val steps: Int,
    val calories: Int,
    val _id: String
)

/**
 * Heart rate data response
 */
@Serializable
data class HeartRateDataResponse(
    val _id: String,
    val userId: String,
    val date: String,
    val heartRateValues: List<HeartRateValueResponse>,
    val averageHeartRate: Int,
    val maxHeartRate: Int,
    val minHeartRate: Int,
    val createdAt: String
)

@Serializable
data class HeartRateValueResponse(
    val timestamp: Long,
    val heartRate: Int,
    val minuteOfDay: Int,
    val _id: String
)

/**
 * SpO2 data response
 */
@Serializable
data class SpO2DataResponse(
    val _id: String,
    val userId: String,
    val date: String,
    val spo2Values: List<SpO2ValueResponse>,
    val averageSpO2: Double,
    val maxSpO2: Int,
    val minSpO2: Int,
    val createdAt: String
)

@Serializable
data class SpO2ValueResponse(
    val timestamp: Long,
    val spo2Value: Int,
    val hourOfDay: Int,
    val _id: String
)

/**
 * HRV data response
 */
@Serializable
data class HRVDataResponse(
    val _id: String,
    val userId: String,
    val date: String,
    val hrvValues: List<HRVValueResponse>,
    val averageHrv: Double,
    val maxHrv: Int,
    val minHrv: Int,
    val createdAt: String
)

@Serializable
data class HRVValueResponse(
    val timestamp: Long,
    val hrvValue: Int,
    val minuteOfDay: Int,
    val _id: String
)

/**
 * Blood pressure data response
 */
@Serializable
data class BloodPressureDataResponse(
    val _id: String,
    val userId: String,
    val date: String,
    val bpValues: List<BloodPressureValueResponse>,
    val averageSystolic: Double,
    val averageDiastolic: Double,
    val maxSystolic: Int,
    val minSystolic: Int,
    val createdAt: String
)

@Serializable
data class BloodPressureValueResponse(
    val timestamp: Long,
    val heartRate: Int,
    val systolic: Int,
    val diastolic: Int,
    val minuteOfDay: Int,
    val _id: String
)

/**
 * Exercise data response
 */
@Serializable
data class ExerciseDataResponse(
    val _id: String,
    val userId: String,
    val date: String,
    val sportType: Int,
    val sportName: String?,
    val startTimestamp: Long,
    val durationSeconds: Int,
    val distanceMeters: Int,
    val calories: Int,
    val averageHeartRate: Int,
    val steps: Int,
    val createdAt: String
)

/**
 * All health data response for a specific date
 */
@Serializable
data class AllHealthDataResponse(
    val stepData: StepDataResponse? = null,
    val heartRateData: HeartRateDataResponse? = null,
    val spo2Data: SpO2DataResponse? = null,
    val hrvData: HRVDataResponse? = null,
    val bpData: BloodPressureDataResponse? = null,
    val exerciseData: List<ExerciseDataResponse>? = null
)
