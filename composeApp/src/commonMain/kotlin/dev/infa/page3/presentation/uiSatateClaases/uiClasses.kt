package dev.infa.page3.presentation.uiSatateClaases

import dev.infa.page3.data.model.User

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object LoggedIn : AuthUiState()
    object LoginSuccess : AuthUiState()
    object RegisterSuccess : AuthUiState()
    data class OtpSent(val message: String, val mobile: String) : AuthUiState()
    data class NewUser(val mobile: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

sealed class ListUiState<out T> {
    object Idle : ListUiState<Nothing>()
    object Loading : ListUiState<Nothing>()
    data class Success<T>(val data: List<T>) : ListUiState<T>()
    data class Error(val message: String) : ListUiState<Nothing>()
    object Empty : ListUiState<Nothing>()
}

// Generic UI State for single items
sealed class SingleUiState<out T> {
    object Idle : SingleUiState<Nothing>()
    object Loading : SingleUiState<Nothing>()
    data class Success<T>(val data: T) : SingleUiState<T>()
    data class Error(val message: String) : SingleUiState<Nothing>()
}

// UI State for operations (create, update, delete)
sealed class OperationUiState {
    object Idle : OperationUiState()
    object Loading : OperationUiState()
    object Success : OperationUiState()
    data class Error(val message: String) : OperationUiState()
}

sealed class UserProfileUiState {
    object Idle : UserProfileUiState()
    object Loading : UserProfileUiState()
    data class Success(val user: User) : UserProfileUiState()
    data class Error(val message: String) : UserProfileUiState()
    data class UpdateSuccess(val user: User, val message: String = "Profile updated successfully") : UserProfileUiState()
}