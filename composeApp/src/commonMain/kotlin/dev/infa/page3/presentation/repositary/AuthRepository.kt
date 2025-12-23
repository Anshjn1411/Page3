package dev.infa.page3.presentation.repositary

import dev.infa.page3.data.model.*
import dev.infa.page3.data.remote.SessionManager
import dev.infa.page3.presentation.api.ApiService


class AuthRepository(
    private val api: ApiService,
    private val sessionManager: SessionManager
) {

    suspend fun sendOtp(mobile: String): Boolean {
        return try {
           // val response = api.sendOtp(mobile)
            true // OTP sent successfully
        } catch (e: Exception) {
            false
        }
    }

    suspend fun verifyOtp(mobile: String, otp: String): VerifyOtpResponse? {
        return try {
    //            val response = api.verifyOtp(mobile, otp)
    //            if (response.jwt != null) {
    //                sessionManager.saveAuthToken(response.jwt)
    //                response.refreshToken?.let { sessionManager.saveRefreshToken(it) }
    //            }
    //            response
        } catch (e: Exception) {
            null
        } as VerifyOtpResponse?
    }

    suspend fun register(request: RegisterRequest): Boolean {
        return try {
            //val response = api.register(request)
            //sessionManager.saveAuthToken(response.jwt)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun logout(): Boolean {
        return try {
            sessionManager.getAuthToken()?.let { token ->
                //api.logout(token)
            }
            sessionManager.clearAuthToken()
            true
        } catch (e: Exception) {
            sessionManager.clearAuthToken() // Clear token even if API call fails
            true
        }
    }

    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()

    fun getToken(): String? = sessionManager.getAuthToken()

    suspend fun createWooCommerceCustomer(
        firstName: String,
        lastName: String,
        email: String,
        username: String,
        address: String,
        city: String,
        state: String,
        postcode: String,
        country: String,
        phone: String
    ): Boolean {
        return try {
            val billingAddress = WcAddress(
                first_name = firstName,
                last_name = lastName,
                address_1 = address,
                city = city,
                state = state,
                postcode = postcode,
                country = country,
                email = email,
                phone = phone
            )
            
            val shippingAddress = WcAddress(
                first_name = firstName,
                last_name = lastName,
                address_1 = address,
                city = city,
                state = state,
                postcode = postcode,
                country = country
            )
            
            val customerRequest = WcCustomerCreateRequest(
                email = email,
                first_name = firstName,
                last_name = lastName,
                username = username,
                billing = billingAddress,
                shipping = shippingAddress
            )
            
            val customer = api.wcCreateCustomer(customerRequest)
            customer.id != null
        } catch (e: Exception) {
            false
        }
    }
}

