package dev.infa.page3.SDK.server

import dev.infa.page3.SDK.data.*
import dev.infa.page3.SDK.ui.utils.DateInfo
import dev.infa.page3.SDK.ui.utils.DateUtils
import dev.infa.page3.presentation.api.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Complete Repository for Health Data Sync
 * Handles business logic and data transformation between SDK and API
 */
class HealthDataRepository(
    private val healthApiService: ApiService
) {

    companion object {
        private const val TAG = "HealthDataRepository"
    }

    // ======================== SYNC FUNCTIONS (POST) ========================

    /**
     * Sync step data to server
     */
    suspend fun syncStepData(
        token: String,
        date: String,
        totalSteps: Int,
        totalCalories: Int,
        totalDistance: Int,
        hourlyData: List<HourlyStepData>
    ): Result<StepDataResponse> {
        return try {
            val request = SaveStepDataRequest(
                date = date,
                totalSteps = totalSteps,
                totalCalories = totalCalories,
                totalDistance = totalDistance,
                hourlyData = hourlyData
            )

            val response = healthApiService.saveStepData(token, request)

            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error ?: "Failed to sync step data"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sync heart rate data to server
     */
    suspend fun syncHeartRateData(
        token: String,
        date: String,
        heartRateValues: List<HeartRateValue>,
        avgHR: Int,
        maxHR: Int,
        minHR: Int
    ): Result<HeartRateDataResponse> {
        return try {
            val request = SaveHeartRateRequest(
                date = date,
                heartRateValues = heartRateValues,
                averageHeartRate = avgHR,
                maxHeartRate = maxHR,
                minHeartRate = minHR
            )

            val response = healthApiService.saveHeartRateData(token, request)

            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error ?: "Failed to sync heart rate data"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sync SpO2 data to server
     */
    suspend fun syncSpO2Data(
        token: String,
        date: String,
        spo2Values: List<SpO2Value>,
        avgSpO2: Int,
        maxSpO2: Int,
        minSpO2: Int
    ): Result<SpO2DataResponse> {
        return try {
            val request = SaveSpO2Request(
                date = date,
                spo2Values = spo2Values,
                averageSpO2 = avgSpO2,
                maxSpO2 = maxSpO2,
                minSpO2 = minSpO2
            )

            val response = healthApiService.saveSpO2Data(token, request)

            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error ?: "Failed to sync SpO2 data"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sync HRV data to server
     */
    suspend fun syncHRVData(
        token: String,
        date: String,
        hrvValues: List<HRVValue>,
        avgHRV: Int,
        maxHRV: Int,
        minHRV: Int
    ): Result<HRVDataResponse> {
        return try {
            val request = SaveHRVRequest(
                date = date,
                hrvValues = hrvValues,
                averageHrv = avgHRV,
                maxHrv = maxHRV,
                minHrv = minHRV
            )

            val response = healthApiService.saveHRVData(token, request)

            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error ?: "Failed to sync HRV data"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sync blood pressure data to server
     */
    suspend fun syncBloodPressureData(
        token: String,
        date: String,
        bpValues: List<BloodPressureValue>,
        avgSystolic: Double,
        avgDiastolic: Double,
        maxSystolic: Int,
        minSystolic: Int
    ): Result<BloodPressureDataResponse> {
        return try {
            val request = SaveBloodPressureRequest(
                date = date,
                bpValues = bpValues,
                averageSystolic = avgSystolic,
                averageDiastolic = avgDiastolic,
                maxSystolic = maxSystolic,
                minSystolic = minSystolic
            )

            val response = healthApiService.saveBloodPressureData(token, request)

            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error ?: "Failed to sync blood pressure data"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sync exercise data to server
     */
    suspend fun syncExerciseData(
        token: String,
        date: String,
        sportType: Int,
        sportName: String?,
        startTimestamp: Long,
        durationSeconds: Int,
        distanceMeters: Int,
        calories: Int,
        avgHeartRate: Int,
        steps: Int
    ): Result<ExerciseDataResponse> {
        return try {
            val request = SaveExerciseRequest(
                date = date,
                sportType = sportType,
                sportName = sportName,
                startTimestamp = startTimestamp,
                durationSeconds = durationSeconds,
                distanceMeters = distanceMeters,
                calories = calories,
                averageHeartRate = avgHeartRate,
                steps = steps
            )

            val response = healthApiService.saveExerciseData(token, request)

            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error ?: "Failed to sync exercise data"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ======================== FETCH FUNCTIONS (GET) ========================

    /**
     * Fetch step data from server for a specific date
     * GET /api/health/steps/:date
     */
    suspend fun fetchStepData(token: String, date: String): Result<StepDataResponse> {
        return try {
            val response = healthApiService.getStepData(token, date)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error ?: "No step data found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch heart rate data from server for a specific date
     * GET /api/health/heartrate/:date
     */
    suspend fun fetchHeartRateData(token: String, date: String): Result<HeartRateDataResponse> {
        return try {
            val response = healthApiService.getHeartRateData(token, date)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error ?: "No heart rate data found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch SpO2 data from server for a specific date
     * GET /api/health/spo2/:date
     */
    suspend fun fetchSpO2Data(token: String, date: String): Result<SpO2DataResponse> {
        return try {
            val response = healthApiService.getSpO2Data(token, date)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error ?: "No SpO2 data found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch HRV data from server for a specific date
     * GET /api/health/hrv/:date
     */
    suspend fun fetchHRVData(token: String, date: String): Result<HRVDataResponse> {
        return try {
            val response = healthApiService.getHRVData(token, date)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error ?: "No HRV data found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch blood pressure data from server for a specific date
     * GET /api/health/bloodpressure/:date
     */
    suspend fun fetchBloodPressureData(token: String, date: String): Result<BloodPressureDataResponse> {
        return try {
            val response = healthApiService.getBloodPressureData(token, date)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error ?: "No blood pressure data found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch exercise data from server for a specific date
     * GET /api/health/exercise/:date
     * Returns a list of all exercises for that date
     */
    suspend fun fetchExerciseData(token: String, date: String): Result<List<ExerciseDataResponse>> {
        return try {
            val response = healthApiService.getExerciseData(token, date)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error ?: "No exercise data found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch all health data from server for a specific date
     * GET /api/health/all/:date
     */
    suspend fun fetchAllHealthData(token: String, date: String): Result<AllHealthDataResponse> {
        return try {
            val response = healthApiService.getAllHealthData(token, date)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error ?: "No data found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ======================== BATCH SYNC FUNCTIONS ========================

    /**
     * Sync all health data for a specific date with progress updates
     */
    suspend fun syncAllHealthData(
        token: String,
        date: String,
        stepData: SaveStepDataRequest? = null,
        heartRateData: SaveHeartRateRequest? = null,
        spo2Data: SaveSpO2Request? = null,
        hrvData: SaveHRVRequest? = null,
        bpData: SaveBloodPressureRequest? = null,
        exerciseData: List<SaveExerciseRequest>? = null
    ): Flow<SyncProgress> = flow {
        var successCount = 0
        var failureCount = 0
        val totalItems = listOfNotNull(
            stepData, heartRateData, spo2Data, hrvData, bpData, exerciseData
        ).size

        emit(SyncProgress.Started(totalItems))

        // Sync step data
        stepData?.let {
            try {
                val response = healthApiService.saveStepData(token, it)
                if (response.success) {
                    successCount++
                    emit(SyncProgress.ItemCompleted("Steps", true))
                } else {
                    failureCount++
                    emit(SyncProgress.ItemCompleted("Steps", false, response.error))
                }
            } catch (e: Exception) {
                failureCount++
                emit(SyncProgress.ItemCompleted("Steps", false, e.message))
            }
        }

        // Sync heart rate data
        heartRateData?.let {
            try {
                val response = healthApiService.saveHeartRateData(token, it)
                if (response.success) {
                    successCount++
                    emit(SyncProgress.ItemCompleted("Heart Rate", true))
                } else {
                    failureCount++
                    emit(SyncProgress.ItemCompleted("Heart Rate", false, response.error))
                }
            } catch (e: Exception) {
                failureCount++
                emit(SyncProgress.ItemCompleted("Heart Rate", false, e.message))
            }
        }

        // Sync SpO2 data
        spo2Data?.let {
            try {
                val response = healthApiService.saveSpO2Data(token, it)
                if (response.success) {
                    successCount++
                    emit(SyncProgress.ItemCompleted("SpO2", true))
                } else {
                    failureCount++
                    emit(SyncProgress.ItemCompleted("SpO2", false, response.error))
                }
            } catch (e: Exception) {
                failureCount++
                emit(SyncProgress.ItemCompleted("SpO2", false, e.message))
            }
        }

        // Sync HRV data
        hrvData?.let {
            try {
                val response = healthApiService.saveHRVData(token, it)
                if (response.success) {
                    successCount++
                    emit(SyncProgress.ItemCompleted("HRV", true))
                } else {
                    failureCount++
                    emit(SyncProgress.ItemCompleted("HRV", false, response.error))
                }
            } catch (e: Exception) {
                failureCount++
                emit(SyncProgress.ItemCompleted("HRV", false, e.message))
            }
        }

        // Sync blood pressure data
        bpData?.let {
            try {
                val response = healthApiService.saveBloodPressureData(token, it)
                if (response.success) {
                    successCount++
                    emit(SyncProgress.ItemCompleted("Blood Pressure", true))
                } else {
                    failureCount++
                    emit(SyncProgress.ItemCompleted("Blood Pressure", false, response.error))
                }
            } catch (e: Exception) {
                failureCount++
                emit(SyncProgress.ItemCompleted("Blood Pressure", false, e.message))
            }
        }

        // Sync exercise data
        exerciseData?.forEach { exercise ->
            try {
                val response = healthApiService.saveExerciseData(token, exercise)
                if (response.success) {
                    successCount++
                    emit(SyncProgress.ItemCompleted("Exercise: ${exercise.sportName}", true))
                } else {
                    failureCount++
                    emit(SyncProgress.ItemCompleted("Exercise: ${exercise.sportName}", false, response.error))
                }
            } catch (e: Exception) {
                failureCount++
                emit(SyncProgress.ItemCompleted("Exercise: ${exercise.sportName}", false, e.message))
            }
        }

        emit(SyncProgress.Completed(successCount, failureCount))
    }

    // ======================== HELPER FUNCTIONS ========================

    /**
     * Format timestamp to date string using DateUtils
     */
    fun timestampToDate(timestamp: Long): String {
        return DateUtils.formatDateForDisplay(
            DateInfo(0, 0, 0, 0, 0, timestamp)
        )
    }

    fun getCurrentDate(): String {
        return DateUtils.formatDateForDisplay(
            DateUtils.getCurrentDate()
        )
    }

    fun getMinuteOfDay(timestamp: Long): Int {
        return DateUtils.getMinuteOfDay(timestamp)
    }

    fun getHourOfDay(timestamp: Long): Int {
        return DateUtils.getHourOfDay(timestamp)
    }
}

/**
 * Sealed class to track sync progress
 */
sealed class SyncProgress {
    data class Started(val totalItems: Int) : SyncProgress()
    data class ItemCompleted(
        val itemName: String,
        val success: Boolean,
        val error: String? = null
    ) : SyncProgress()
    data class Completed(val successCount: Int, val failureCount: Int) : SyncProgress()
}