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

    // ======================== OTP FLOW ========================

    fun sendOtp(phone: String) {
        if (!isValidPhoneNumber(phone)) {
            _uiState.value = AuthUiState.Error("Please enter valid 10-digit mobile number")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            repository.sendOtp(phone).fold(
                onSuccess = { response ->
                    _uiState.value = AuthUiState.OtpSent(response.message, phone)
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Failed to send OTP")
                }
            )
        }
    }

    fun resendOtp(phone: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            repository.resendOtp(phone).fold(
                onSuccess = { response ->
                    _uiState.value = AuthUiState.OtpSent(response.message, phone)
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Failed to resend OTP")
                }
            )
        }
    }

    fun verifyOtp(phone: String, otp: String) {
        if (otp.length != 6) {
            _uiState.value = AuthUiState.Error("Please enter 6-digit OTP")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            repository.verifyOtp(phone, otp).fold(
                onSuccess = { response ->
                    when {
                        response.requiresSignup == true -> {
                            _uiState.value = AuthUiState.NewUser(phone)
                        }
                        response.token != null && response.user != null -> {
                            _currentUser.value = response.user
                            _uiState.value = AuthUiState.LoggedIn
                        }
                        else -> {
                            _uiState.value = AuthUiState.Error("Verification failed")
                        }
                    }
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "OTP verification failed")
                }
            )
        }
    }

    // ======================== USER MANAGEMENT ========================

    fun getCurrentUser() {
        viewModelScope.launch {
            repository.getCurrentUser().fold(
                onSuccess = { user ->
                    _currentUser.value = user
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Failed to fetch user")
                }
            )
        }
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
        if (firstName.isBlank() || lastName.isBlank() || email.isBlank() ||
            address.isBlank() || city.isBlank() || state.isBlank() ||
            postcode.isBlank() || country.isBlank()) {
            _uiState.value = AuthUiState.Error("Please fill all required fields")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

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

            repository.completeProfile(
                phone = phone,
                email = email,
                firstName = firstName,
                lastName = lastName,
                username = username,
                billing = billingAddress,
                shipping = shippingAddress
            ).fold(
                onSuccess = { response ->
                    _currentUser.value = response.user
                    _uiState.value = AuthUiState.RegisterSuccess
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Profile completion failed")
                }
            )
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
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            repository.updateProfile(
                email = email,
                firstName = firstName,
                lastName = lastName,
                username = username,
                billing = billing,
                shipping = shipping
            ).fold(
                onSuccess = { response ->
                    _currentUser.value = response.user
                    _uiState.value = AuthUiState.ProfileUpdated
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Profile update failed")
                }
            )
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
        _uiState.value = if (repository.isLoggedIn()) {
            AuthUiState.LoggedIn
        } else {
            AuthUiState.Idle
        }
    }

    fun autoLogin() {
        viewModelScope.launch {

            println("the user login status -> ${repository.isLoggedIn()}")
            if (repository.isLoggedIn()) {
                repository.getCurrentUser().fold(
                    onSuccess = { user ->
                        _currentUser.value = user
                        _uiState.value = AuthUiState.LoggedIn
                    },
                    onFailure = {
                        repository.logout()
                        _currentUser.value = null
                        _uiState.value = AuthUiState.Idle
                    }
                )
                println("the user login status Logged In succesfully-> ${_currentUser.value?.first_name}")
            } else {
                _uiState.value = AuthUiState.Idle
                println("the user login status Logged In unsuccesfully-> ${_currentUser.value?.first_name}")
            }
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