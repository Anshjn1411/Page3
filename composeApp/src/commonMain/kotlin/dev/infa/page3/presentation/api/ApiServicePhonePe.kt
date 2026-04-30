package dev.infa.page3.presentation.api

import dev.infa.page3.data.model.PhonePeAuthTokenResponse
import dev.infa.page3.data.model.PhonePeCreateOrderRequest
import dev.infa.page3.data.model.PhonePeCreateOrderResponse
import dev.infa.page3.data.model.PhonePeOrderStatusResponse
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.http.formUrlEncode

suspend fun ApiService.phonePeGetAuthToken(
    clientId: String,
    clientSecret: String,
    clientVersion: String
): dev.infa.page3.data.model.PhonePeAuthTokenResponse {
    val url = "$phonePeAuthBase/v1/oauth/token"

    return logApiCall(
        apiName = "phonePeGetAuthToken",
        url = url,
        method = "POST",
        headers = mapOf("Content-Type" to "application/x-www-form-urlencoded")
    ) {
        httpClient.post(url) {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(
                Parameters.build {
                        append("client_id", clientId)
                        append("client_version", clientVersion)
                        append("client_secret", clientSecret)
                        append("grant_type", "client_credentials")
                    }.formUrlEncode()
            )
        }
    }
}

/**
 * Create PhonePe SDK Order
 * POST /checkout/v2/sdk/order  (Android SDK flow)
 * Requires auth token in Authorization header
 *
 * Reference:
 *  - Android SDK Intro / Integration Steps / SDK Setup
 *    (`https://developer.phonepe.com/payment-gateway/mobile-app-integration/standard-checkout-mobile/android-sdk/integration-steps`)
 */
suspend fun ApiService.phonePeCreateOrder(
    authToken: String,
    request: dev.infa.page3.data.model.PhonePeCreateOrderRequest
): dev.infa.page3.data.model.PhonePeCreateOrderResponse {
    val url = "$phonePeCheckoutBase/checkout/v2/sdk/order"

    return logApiCall(
        apiName = "phonePeCreateOrder",
        url = url,
        method = "POST",
        headers = mapOf(
            "Authorization" to "O-Bearer $authToken",
            "Content-Type" to "application/json"
        ),
        requestBody = request
    ) {
        httpClient.post(url) {
            header("Authorization", "O-Bearer $authToken")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}

/**
 * Check PhonePe Order Status
 * GET /checkout/v2/order/{merchantOrderId}/status
 * Requires auth token in Authorization header
 */
suspend fun ApiService.phonePeCheckOrderStatus(
    authToken: String,
    merchantOrderId: String
): dev.infa.page3.data.model.PhonePeOrderStatusResponse {
    val url = "$phonePeCheckoutBase/checkout/v2/order/$merchantOrderId/status?details=false"

    return logApiCall(
        apiName = "phonePeCheckOrderStatus",
        url = url,
        method = "GET",
        headers = mapOf("Authorization" to "O-Bearer $authToken")
    ) {
        httpClient.get(url) {
            header("Authorization", "O-Bearer $authToken")
            contentType(ContentType.Application.Json)
        }
    }
}