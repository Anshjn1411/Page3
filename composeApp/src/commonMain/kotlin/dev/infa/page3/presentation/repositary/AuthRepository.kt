package dev.infa.page3.presentation.repository

import dev.infa.page3.data.model.*
import dev.infa.page3.data.remote.SessionManager
import dev.infa.page3.presentation.api.ApiService

/**
 * AuthRepository - Handles authentication flow
 *
 * Flow:
 * 1. sendOtp() -> Send OTP to phone
 * 2. verifyOtp() -> Verify OTP
 *    - Existing user: Auto login (has token)
 *    - New user: requiresSignup=true -> go to step 3
 * 3. completeProfile() -> New user completes signup
 * 4. User logged in
 */
class AuthRepository(
    private val api: ApiService,
    private val sessionManager: SessionManager
) {

    // ======================== OTP FLOW ========================

    suspend fun sendOtp(phone: String): Result<SendOtpResponse> {
        return try {
            val response = api.sendOTP(phone)
            if (response.success) {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resendOtp(phone: String): Result<SendOtpResponse> {
        return try {
            val response = api.resendOTP(phone)
            if (response.success) {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyOtp(phone: String, otp: String): Result<VerifyOtpResponse> {
        return try {
            val response = api.verifyOTP(phone, otp)

            if (response.success) {
                // Existing user - save session
                if (response.token != null && response.user != null) {
                    sessionManager.saveAuthToken(response.token)
                    sessionManager.saveUserInfo(response.user.toUserInfo())
                }
                Result.success(response)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun completeProfile(
        phone: String,
        email: String? = null,
        firstName: String? = null,
        lastName: String? = null,
        username: String? = null,
        billing: WcAddress? = null,
        shipping: WcAddress? = null
    ): Result<CompleteProfileResponse> {
        return try {
            val response = api.completeProfile(
                phone = phone,
                email = email,
                firstName = firstName,
                lastName = lastName,
                username = username,
                billing = billing,
                shipping = shipping
            )

            if (response.success && response.token != null && response.user != null) {
                sessionManager.saveAuthToken(response.token)
                sessionManager.saveUserInfo(response.user.toUserInfo())
                Result.success(response)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ======================== USER PROFILE ========================

    suspend fun updateProfile(
        email: String? = null,
        firstName: String? = null,
        lastName: String? = null,
        username: String? = null,
        billing: WcAddress? = null,
        shipping: WcAddress? = null
    ): Result<UpdateProfileResponse> {
        return try {
            val token = sessionManager.getAuthToken()
                ?: return Result.failure(Exception("Not authenticated"))

            val response = api.updateProfile(
                token = token,
                email = email,
                firstName = firstName,
                lastName = lastName,
                username = username,
                billing = billing,
                shipping = shipping
            )

            if (response.success && response.user != null) {
                sessionManager.updateUserData(response.user.toUserInfo())
                Result.success(response)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): Result<WcCustomer> {
        return try {
            val token = sessionManager.getAuthToken()
                ?: return Result.failure(Exception("Not authenticated"))

            val response = api.getCurrentUser(token)

            if (response.success && response.user != null) {
                sessionManager.saveUserInfo(response.user.toUserInfo())
                Result.success(response.user)
            } else {
                Result.failure(Exception(response.message ?: "Failed to get user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ======================== SESSION ========================

    suspend fun logout(): Result<Boolean> {
        return try {
            val token = sessionManager.getAuthToken()

            // Try server logout
            if (token != null) {
                try {
                    api.logout(token)
                } catch (e: Exception) {
                    // Continue even if server logout fails
                }
            }

            // Always clear local session
            sessionManager.clearAllData()
            Result.success(true)
        } catch (e: Exception) {
            sessionManager.clearAllData()
            Result.success(true)
        }
    }

    fun isLoggedIn(): Boolean {
        println("this is Token :- ${sessionManager.getAuthToken()}")
        println("this is name :- ${sessionManager.getUserInfo()?.first_name}")
        println("this is last :- ${sessionManager.getUserInfo()?.last_name}")
        println("this is emaail :- ${sessionManager.getUserInfo()?.email}")

        return sessionManager.isLoggedIn()
    }


    fun getAuthToken(): String? = sessionManager.getAuthToken()

    fun getUserInfo(): UserInfo? = sessionManager.getUserInfo()
}