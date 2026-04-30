package dev.infa.page3.presentation.api

import dev.infa.page3.data.model.CompleteProfileRequest
import dev.infa.page3.data.model.CompleteProfileResponse
import dev.infa.page3.data.model.LogoutResponse
import dev.infa.page3.data.model.ResendOtpRequest
import dev.infa.page3.data.model.SendOtpRequest
import dev.infa.page3.data.model.SendOtpResponse
import dev.infa.page3.data.model.UpdateProfileRequest
import dev.infa.page3.data.model.UpdateProfileResponse
import dev.infa.page3.data.model.UserResponse
import dev.infa.page3.data.model.VerifyOtpRequest
import dev.infa.page3.data.model.VerifyOtpResponse
import dev.infa.page3.data.model.WcAddress
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

suspend fun ApiService.sendOTP(phone: String): SendOtpResponse {
    val url = "$authBaseUrl/send-otp"
    val requestBody = SendOtpRequest(phone = phone)

    return logApiCall(
        apiName = "sendOTP",
        url = url,
        method = "POST",
        requestBody = requestBody
    ) {
        httpClient.post(url) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }
    }
}

/**
 * Resend OTP to phone number
 * Useful when user didn't receive the first OTP or it expired
 * @param phone User's phone number
 * @return SendOtpResponse with success status
 */
suspend fun ApiService.resendOTP(phone: String): SendOtpResponse {
    val url = "$authBaseUrl/resend-otp"
    val requestBody = ResendOtpRequest(phone = phone)

    return logApiCall(
        apiName = "resendOTP",
        url = url,
        method = "POST",
        requestBody = requestBody
    ) {
        httpClient.post(url) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }
    }
}

/**
 * Verify OTP - Core authentication step
 * - If user exists with phone: Returns login token + user data
 * - If user is new: Returns requiresSignup flag for profile completion
 * @param phone User's phone number
 * @param otp 6-digit OTP code
 * @return VerifyOtpResponse with auth token if existing user, or signup flag for new user
 */
suspend fun ApiService.verifyOTP(phone: String, otp: String): VerifyOtpResponse {
    val url = "$authBaseUrl/verify-otp"
    val requestBody = VerifyOtpRequest(phone = phone, otp = otp)

    return logApiCall(
        apiName = "verifyOTP",
        url = url,
        method = "POST",
        requestBody = requestBody
    ) {
        httpClient.post(url) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }
    }
}

/**
 * Complete User Profile (For new users after OTP verification)
 * Creates new user or updates existing user with profile details
 * @param phone Verified phone number (required)
 * @param email User's email (optional)
 * @param firstName User's first name (optional)
 * @param lastName User's last name (optional)
 * @param username Unique username (optional)
 * @param billing Billing address (optional)
 * @param shipping Shipping address (optional)
 * @return CompleteProfileResponse with token and user data
 */
suspend fun ApiService.completeProfile(
    phone: String,
    email: String? = null,
    firstName: String? = null,
    lastName: String? = null,
    username: String? = null,
    billing: WcAddress? = null,
    shipping: WcAddress? = null
): CompleteProfileResponse {
    val url = "$authBaseUrl/complete-profile"
    val requestBody = CompleteProfileRequest(
        phone = phone,
        email = email,
        first_name = firstName,
        last_name = lastName,
        username = username,
        billing = billing,
        shipping = shipping
    )

    return logApiCall(
        apiName = "completeProfile",
        url = url,
        method = "POST",
        requestBody = requestBody
    ) {
        httpClient.post(url) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }
    }
}

/**
 * Update User Profile (For authenticated users)
 * @param token JWT authentication token
 * @param email User's email (optional)
 * @param firstName User's first name (optional)
 * @param lastName User's last name (optional)
 * @param username Unique username (optional)
 * @param billing Billing address (optional)
 * @param shipping Shipping address (optional)
 * @return UpdateProfileResponse with updated user data
 */
suspend fun ApiService.updateProfile(
    token: String,
    email: String? = null,
    firstName: String? = null,
    lastName: String? = null,
    username: String? = null,
    billing: WcAddress? = null,
    shipping: WcAddress? = null
): UpdateProfileResponse {
    val url = "$authBaseUrl/update-profile"
    val requestBody = UpdateProfileRequest(
        email = email,
        first_name = firstName,
        last_name = lastName,
        username = username,
        billing = billing,
        shipping = shipping
    )

    return logApiCall(
        apiName = "updateProfile",
        url = url,
        method = "PUT",
        headers = mapOf("Authorization" to "Bearer $token"),
        requestBody = requestBody
    ) {
        httpClient.put(url) {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }
    }
}

/**
 * Get Current User Profile
 * @param token JWT authentication token
 * @return UserResponse with complete user profile data
 */
suspend fun ApiService.getCurrentUser(token: String): UserResponse {
    val url = "$authBaseUrl/me"

    return logApiCall(
        apiName = "getCurrentUser",
        url = url,
        method = "GET",
        headers = mapOf("Authorization" to "Bearer $token")
    ) {
        httpClient.get(url) {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }
}

/**
 * Logout User
 * Invalidates current session (token-based)
 * @param token JWT authentication token
 * @return LogoutResponse with success status
 */
suspend fun ApiService.logout(token: String): LogoutResponse {
    val url = "$authBaseUrl/logout"

    return logApiCall(
        apiName = "logout",
        url = url,
        method = "POST",
        headers = mapOf("Authorization" to "Bearer $token")
    ) {
        httpClient.post(url) {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
            contentType(ContentType.Application.Json)
        }
    }
}

// ======================== OTP AUTHENTICATION FLOW COMPLETE ========================

