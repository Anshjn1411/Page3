package dev.infa.page3.presentation.viewModel

import dev.infa.page3.data.model.*
import dev.infa.page3.presentation.repositary.AuthRepository
import dev.infa.page3.presentation.repository.UserData
import dev.infa.page3.presentation.repository.UserRepository
import dev.infa.page3.presentation.uiSatateClaases.AuthUiState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withContext

class AuthViewModel(
    private val repository: AuthRepository,
    private val userRepository: UserRepository = UserRepository()
)
{
    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    fun sendOtp(mobile: String) {
        if (!isValidPhoneNumber(mobile)) {
            _uiState.value = AuthUiState.Error("Please enter valid 10-digit mobile number")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            delay(1000)
            _uiState.value = AuthUiState.OtpSent("OTP sent successfully", mobile)
        }
    }

    fun verifyOtp(mobile: String, otp: String) {
        if (otp.length != 6) {
            _uiState.value = AuthUiState.Error("Please enter 6-digit OTP")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            // Dummy authentication - simulate delay
            kotlinx.coroutines.delay(1000)

            // For dummy auth, always treat as new user for registration
            _uiState.value = AuthUiState.NewUser(mobile)
        }
    }

    fun register(firstName: String, lastName: String, mobile: String, email: String? = null) {
        if (firstName.isBlank() || lastName.isBlank()) {
            _uiState.value = AuthUiState.Error("Please fill all required fields")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val request = RegisterRequest(firstName, lastName, mobile, email)
            val success = repository.register(request)

            _uiState.value = if (success) {
                AuthUiState.RegisterSuccess
            } else {
                AuthUiState.Error("Registration failed")
            }
        }
    }

    fun registerWithDetails(
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
    ) {
        if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || username.isBlank() || 
            address.isBlank() || city.isBlank() || state.isBlank() || postcode.isBlank() || 
            country.isBlank() || phone.isBlank()) {
            _uiState.value = AuthUiState.Error("Please fill all required fields")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = AuthUiState.Loading
            try {
                // Simulate background processing with timeout
                withTimeout(30000) { // 30 second timeout
                    val success = repository.createWooCommerceCustomer(
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        username = username,
                        address = address,
                        city = city,
                        state = state,
                        postcode = postcode,
                        country = country,
                        phone = phone
                    )
                    
                    // Switch back to Main thread for UI updates
                    withContext(Dispatchers.Main) {
                        if (success) {
                            // Cache user data
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
                            
                            val userData = UserData(
                                firstName = firstName,
                                lastName = lastName,
                                email = email,
                                username = username,
                                phone = phone,
                                billingAddress = billingAddress,
                                shippingAddress = shippingAddress
                            )
                            
                            userRepository.saveUserData(userData)
                            userRepository.setLoggedIn(true)
                            _uiState.value = AuthUiState.RegisterSuccess
                        } else {
                            // Cache user data
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

                            val userData = UserData(
                                firstName = firstName,
                                lastName = lastName,
                                email = email,
                                username = username,
                                phone = phone,
                                billingAddress = billingAddress,
                                shippingAddress = shippingAddress
                            )
                            userRepository.saveUserData(userData)
                            userRepository.setLoggedIn(true)
                            _uiState.value = AuthUiState.RegisterSuccess
                        }
                    }
                }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                withContext(Dispatchers.Main) {
                    _uiState.value = AuthUiState.Error("Registration timed out. Please try again.")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = AuthUiState.RegisterSuccess
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            userRepository.clearUserData()
            _uiState.value = AuthUiState.Idle
        }
    }

    fun checkLogin() {
        _uiState.value = if (userRepository.isFullyLoggedIn()) {
            AuthUiState.LoggedIn
        } else {
            AuthUiState.Idle
        }
    }

    fun autoLogin() {
        viewModelScope.launch {
            if (userRepository.isFullyLoggedIn()) {
                _uiState.value = AuthUiState.LoggedIn
            } else {
                _uiState.value = AuthUiState.Idle
            }
        }
    }

    fun isValidPhoneNumber(phone: String): Boolean {
        val regex = Regex("^[6-9]\\d{9}$")
        return regex.matches(phone)
    }

    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Idle
        }
    }
}

