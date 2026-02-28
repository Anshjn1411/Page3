package dev.infa.page3.presentation.viewModel

import dev.infa.page3.data.model.*
import dev.infa.page3.presentation.repository.AuthRepository
import dev.infa.page3.presentation.uiSatateClaases.AuthUiState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class AuthViewModel(
    private val repository: AuthRepository
)
{
    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    private val _currentUser = MutableStateFlow<WcCustomer?>(null)
    val currentUser: StateFlow<WcCustomer?> = _currentUser

    // ⚠️ DUMMY MODE: All OTP/Auth flows are dummy for testing.
    // No real API calls are made. Accept any phone and any OTP.

    fun sendOtp(phone: String) {
        if (phone.length < 10) {
            _uiState.value = AuthUiState.Error("Please enter valid mobile number")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            delay(500) // Simulate network delay
            _uiState.value = AuthUiState.OtpSent("OTP sent successfully (dummy)", phone)
        }
    }

    fun resendOtp(phone: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            delay(500) // Simulate network delay
            _uiState.value = AuthUiState.OtpSent("OTP resent successfully (dummy)", phone)
        }
    }

    fun verifyOtp(phone: String, otp: String) {
        if (otp.length != 6) {
            _uiState.value = AuthUiState.Error("Please enter 6-digit OTP")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            delay(500) // Simulate network delay

            // Dummy: create user locally and log in immediately
            val dummyUser = WcCustomer(
                id = phone,
                phone = phone,
                isPhoneVerified = true
            )
            _currentUser.value = dummyUser
            _uiState.value = AuthUiState.LoggedIn
        }
    }

    // ======================== USER MANAGEMENT ========================

    fun getCurrentUser() {
        // Dummy: no-op, user already set from verifyOtp
    }

    // ======================== PROFILE COMPLETION ========================

    fun completeProfile(
        phone: String,
        firstName: String,
        lastName: String,
        email: String,
        username: String? = null,
        address: String,
        city: String,
        state: String,
        postcode: String,
        country: String
    ) {
        // Dummy: just log in with phone
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            delay(300)
            val dummyUser = WcCustomer(
                id = phone,
                phone = phone,
                first_name = firstName,
                last_name = lastName,
                email = email,
                isPhoneVerified = true
            )
            _currentUser.value = dummyUser
            _uiState.value = AuthUiState.RegisterSuccess
        }
    }

    // ======================== PROFILE UPDATE ========================

    fun updateProfile(
        firstName: String? = null,
        lastName: String? = null,
        email: String? = null,
        username: String? = null,
        billing: WcAddress? = null,
        shipping: WcAddress? = null
    ) {
        // Dummy: just update local user
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            delay(300)
            val current = _currentUser.value
            _currentUser.value = current?.copy(
                first_name = firstName ?: current.first_name,
                last_name = lastName ?: current.last_name,
                email = email ?: current.email
            )
            _uiState.value = AuthUiState.ProfileUpdated
        }
    }

    // ======================== SESSION MANAGEMENT ========================

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _currentUser.value = null
            _uiState.value = AuthUiState.Idle
        }
    }

    fun checkLoginStatus() {
        _uiState.value = AuthUiState.Idle
    }

    fun autoLogin() {
        viewModelScope.launch {
            // Dummy: no persistent session, always start as Idle
            _uiState.value = AuthUiState.Idle
            println("⚠️ DUMMY MODE: autoLogin skipped, no API calls")
        }
    }

    // ======================== HELPERS ========================

    private fun isValidPhoneNumber(phone: String): Boolean {
        val regex = Regex("^[6-9]\\d{9}$")
        return regex.matches(phone)
    }

    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Idle
        }
    }

    fun onCleared() {
        viewModelScope.cancel()
    }
}