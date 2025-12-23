package dev.infa.page3.presentation.viewModel


import dev.infa.page3.data.model.User
import dev.infa.page3.presentation.repository.UserRepository
import dev.infa.page3.presentation.uiSatateClaases.UserProfileUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
class UserProfileViewModel(
    private val repository: UserRepository
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _uiState = MutableStateFlow<UserProfileUiState>(UserProfileUiState.Idle)
    val uiState: StateFlow<UserProfileUiState> = _uiState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
//        viewModelScope.launch {
//            _uiState.value = UserProfileUiState.Loading
//
//            repository.getUserProfile()
//                .onSuccess { user ->
//                    _currentUser.value = user
//                    _uiState.value = UserProfileUiState.Success(user)
//                }
//                .onFailure { error ->
//                    _uiState.value = UserProfileUiState.Error(
//                        error.message ?: "Failed to load profile"
//                    )
//                }
//        }
    }

    fun updateProfile(
        firstName: String,
        lastName: String,
        email: String?,
        mobile: String,
        dob: String? = null
    ) {
        val userId = _currentUser.value?._id
        if (userId == null) {
            _uiState.value = UserProfileUiState.Error("User ID not found")
            return
        }

        // Validation
        if (firstName.isBlank()) {
            _uiState.value = UserProfileUiState.Error("First name is required")
            return
        }
        if (lastName.isBlank()) {
            _uiState.value = UserProfileUiState.Error("Last name is required")
            return
        }
        if (mobile.length != 10) {
            _uiState.value = UserProfileUiState.Error("Please enter valid 10-digit mobile number")
            return
        }
//        if (!email.isNullOrBlank() && !isValidEmail(email)) {
//            _uiState.value = UserProfileUiState.Error("Please enter a valid email address")
//            return
//        }

        viewModelScope.launch {
            _uiState.value = UserProfileUiState.Loading

//            repository.updateUserProfile(
//                userId = userId,
//                firstName = firstName,
//                lastName = lastName,
//                email = email,
//                mobile = mobile,
//                dob = dob
//            )
//                .onSuccess { updatedUser ->
//                    _currentUser.value = updatedUser.user
//                    _uiState.value = UserProfileUiState.UpdateSuccess(updatedUser.user)
//                }
//                .onFailure { error ->
//                    _uiState.value = UserProfileUiState.Error(
//                        error.message ?: "Failed to update profile"
//                    )
//                }
//        }
        }

        fun resetState() {
            _uiState.value = UserProfileUiState.Idle
        }

//        private fun isValidEmail(email: String): Boolean {
//            return true
//        }

        fun onCleared() {
            viewModelScope.cancel()
        }
    }
}