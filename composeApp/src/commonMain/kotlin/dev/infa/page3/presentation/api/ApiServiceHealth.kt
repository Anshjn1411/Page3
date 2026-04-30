package dev.infa.page3.presentation.api

import dev.infa.page3.SDK.data.AllHealthDataResponse
import dev.infa.page3.SDK.data.BloodPressureDataResponse
import dev.infa.page3.SDK.data.ExerciseDataResponse
import dev.infa.page3.SDK.data.HRVDataResponse
import dev.infa.page3.SDK.data.HealthApiResponse
import dev.infa.page3.SDK.data.HeartRateDataResponse
import dev.infa.page3.SDK.data.SaveBloodPressureRequest
import dev.infa.page3.SDK.data.SaveExerciseRequest
import dev.infa.page3.SDK.data.SaveHRVRequest
import dev.infa.page3.SDK.data.SaveHeartRateRequest
import dev.infa.page3.SDK.data.SaveSpO2Request
import dev.infa.page3.SDK.data.SaveStepDataRequest
import dev.infa.page3.SDK.data.SpO2DataResponse
import dev.infa.page3.SDK.data.StepDataResponse
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

suspend fun ApiService.saveStepData(
    token: String,
    request: SaveStepDataRequest
): HealthApiResponse<StepDataResponse> {
    val url = "$healthBaseUrl/steps"

    return logApiCall(
        apiName = "saveStepData",
        url = url,
        method = "POST",
        headers = mapOf("Authorization" to "Bearer $token"),
        requestBody = request
    ) {
        httpClient.post(url) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(request)
        }
    }
}

/**
 * Save or update heart rate data for a specific date
 * POST /api/health/heartrate
 */
suspend fun ApiService.saveHeartRateData(
    token: String,
    request: SaveHeartRateRequest
): HealthApiResponse<HeartRateDataResponse> {
    val url = "$healthBaseUrl/heartrate"

    return logApiCall(
        apiName = "saveHeartRateData",
        url = url,
        method = "POST",
        headers = mapOf("Authorization" to "Bearer $token"),
        requestBody = request
    ) {
        httpClient.post(url) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(request)
        }
    }
}

/**
 * Save or update SpO2 data for a specific date
 * POST /api/health/spo2
 */
suspend fun ApiService.saveSpO2Data(
    token: String,
    request: SaveSpO2Request
): HealthApiResponse<SpO2DataResponse> {
    val url = "$healthBaseUrl/spo2"

    return logApiCall(
        apiName = "saveSpO2Data",
        url = url,
        method = "POST",
        headers = mapOf("Authorization" to "Bearer $token"),
        requestBody = request
    ) {
        httpClient.post(url) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(request)
        }
    }
}

/**
 * Save or update HRV data for a specific date
 * POST /api/health/hrv
 */
suspend fun ApiService.saveHRVData(
    token: String,
    request: SaveHRVRequest
): HealthApiResponse<HRVDataResponse> {
    val url = "$healthBaseUrl/hrv"

    return logApiCall(
        apiName = "saveHRVData",
        url = url,
        method = "POST",
        headers = mapOf("Authorization" to "Bearer $token"),
        requestBody = request
    ) {
        httpClient.post(url) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(request)
        }
    }
}

/**
 * Save or update blood pressure data for a specific date
 * POST /api/health/bloodpressure
 */
suspend fun ApiService.saveBloodPressureData(
    token: String,
    request: SaveBloodPressureRequest
): HealthApiResponse<BloodPressureDataResponse> {
    val url = "$healthBaseUrl/bloodpressure"

    return logApiCall(
        apiName = "saveBloodPressureData",
        url = url,
        method = "POST",
        headers = mapOf("Authorization" to "Bearer $token"),
        requestBody = request
    ) {
        httpClient.post(url) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(request)
        }
    }
}

/**
 * Save exercise/workout data
 * POST /api/health/exercise
 */
suspend fun ApiService.saveExerciseData(
    token: String,
    request: SaveExerciseRequest
): HealthApiResponse<ExerciseDataResponse> {
    val url = "$healthBaseUrl/exercise"

    return logApiCall(
        apiName = "saveExerciseData",
        url = url,
        method = "POST",
        headers = mapOf("Authorization" to "Bearer $token"),
        requestBody = request
    ) {
        httpClient.post(url) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(request)
        }
    }
}

// ======================== GET HEALTH DATA ========================

/**
 * Get step data for a specific date
 * GET /api/health/steps/:date
 */
suspend fun ApiService.getStepData(
    token: String,
    date: String
): HealthApiResponse<StepDataResponse> {
    val url = "$healthBaseUrl/steps/$date"

    return logApiCall(
        apiName = "getStepData",
        url = url,
        method = "GET",
        headers = mapOf("Authorization" to "Bearer $token")
    ) {
        httpClient.get(url) {
            header("Authorization", "Bearer $token")
        }
    }
}

/**
 * Get heart rate data for a specific date
 * GET /api/health/heartrate/:date
 */
suspend fun ApiService.getHeartRateData(
    token: String,
    date: String
): HealthApiResponse<HeartRateDataResponse> {
    val url = "$healthBaseUrl/heartrate/$date"

    return logApiCall(
        apiName = "getHeartRateData",
        url = url,
        method = "GET",
        headers = mapOf("Authorization" to "Bearer $token")
    ) {
        httpClient.get(url) {
            header("Authorization", "Bearer $token")
        }
    }
}

/**
 * Get SpO2 data for a specific date
 * GET /api/health/spo2/:date
 */
suspend fun ApiService.getSpO2Data(
    token: String,
    date: String
): HealthApiResponse<SpO2DataResponse> {
    val url = "$healthBaseUrl/spo2/$date"

    return logApiCall(
        apiName = "getSpO2Data",
        url = url,
        method = "GET",
        headers = mapOf("Authorization" to "Bearer $token")
    ) {
        httpClient.get(url) {
            header("Authorization", "Bearer $token")
        }
    }
}

/**
 * Get HRV data for a specific date
 * GET /api/health/hrv/:date
 */
suspend fun ApiService.getHRVData(
    token: String,
    date: String
): HealthApiResponse<HRVDataResponse> {
    val url = "$healthBaseUrl/hrv/$date"

    return logApiCall(
        apiName = "getHRVData",
        url = url,
        method = "GET",
        headers = mapOf("Authorization" to "Bearer $token")
    ) {
        httpClient.get(url) {
            header("Authorization", "Bearer $token")
        }
    }
}

/**
 * Get blood pressure data for a specific date
 * GET /api/health/bloodpressure/:date
 */
suspend fun ApiService.getBloodPressureData(
    token: String,
    date: String
): HealthApiResponse<BloodPressureDataResponse> {
    val url = "$healthBaseUrl/bloodpressure/$date"

    return logApiCall(
        apiName = "getBloodPressureData",
        url = url,
        method = "GET",
        headers = mapOf("Authorization" to "Bearer $token")
    ) {
        httpClient.get(url) {
            header("Authorization", "Bearer $token")
        }
    }
}

/**
 * Get exercise data for a specific date
 * GET /api/health/exercise/:date
 * Returns a list of exercises for that date
 */
suspend fun ApiService.getExerciseData(
    token: String,
    date: String
): HealthApiResponse<List<ExerciseDataResponse>> {
    val url = "$healthBaseUrl/exercise/$date"

    return logApiCall(
        apiName = "getExerciseData",
        url = url,
        method = "GET",
        headers = mapOf("Authorization" to "Bearer $token")
    ) {
        httpClient.get(url) {
            header("Authorization", "Bearer $token")
        }
    }
}

/**
 * Get all health data for a specific date
 * GET /api/health/all/:date
 * Includes: steps, heart rate, SpO2, HRV, blood pressure, and exercise data
 */
suspend fun ApiService.getAllHealthData(
    token: String,
    date: String
): HealthApiResponse<AllHealthDataResponse> {
    val url = "$healthBaseUrl/all/$date"

    return logApiCall(
        apiName = "getAllHealthData",
        url = url,
        method = "GET",
        headers = mapOf("Authorization" to "Bearer $token")
    ) {
        httpClient.get(url) {
            header("Authorization", "Bearer $token")
        }
    }
}
