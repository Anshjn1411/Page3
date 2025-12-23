package dev.infa.page3.SDK.viewModel

import dev.infa.page3.SDK.data.*
// ============================================
// commonMain/viewmodels/ProfileViewModel.kt
// ============================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
/**
 * Platform-specific profile and device settings manager
 */
expect class ProfileManager {
    /**
     * Find device (make it vibrate)
     */
    suspend fun findMyDevice(): Boolean

    /**
     * Get device information
     */
    suspend fun getDeviceInfo(): DeviceInfo?

    /**
     * Update unit system on device
     */
    suspend fun updateUnitSystem(unitSystem: UnitSystem): Boolean

    /**
     * Update time format on device
     */
    suspend fun updateTimeFormat(timeFormat: TimeFormat): Boolean

    /**
     * Toggle low battery prompt
     */
    suspend fun toggleLowBatteryPrompt(enabled: Boolean): Boolean

    /**
     * Load touch settings from device
     */
    suspend fun loadTouchSettings(): TouchSettings?

    /**
     * Update touch settings on device
     */
    suspend fun updateTouchSettings(
        appType: Int,
        isTouch: Boolean,
        strength: Int
    ): Boolean

    /**
     * Cleanup resources
     */
    fun cleanup()
}



class ProfileViewModel(
    private val profileManager: ProfileManager
)  {

    // Loading and Error States
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // User Settings
    private val _userSettings = MutableStateFlow(UserSettings())
    val userSettings: StateFlow<UserSettings> = _userSettings.asStateFlow()

    // Touch Settings
    private val _touchSettings = MutableStateFlow(TouchSettings())
    val touchSettings: StateFlow<TouchSettings> = _touchSettings.asStateFlow()

    // Device Info
    private val _deviceInfo = MutableStateFlow<DeviceInfo?>(null)
    val deviceInfo: StateFlow<DeviceInfo?> = _deviceInfo.asStateFlow()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Dialog States
    private val _showUnitDialog = MutableStateFlow(false)
    val showUnitDialog: StateFlow<Boolean> = _showUnitDialog.asStateFlow()

    private val _showTimeFormatDialog = MutableStateFlow(false)
    val showTimeFormatDialog: StateFlow<Boolean> = _showTimeFormatDialog.asStateFlow()

    private val _showThemeDialog = MutableStateFlow(false)
    val showThemeDialog: StateFlow<Boolean> = _showThemeDialog.asStateFlow()

    private val _showTouchDialog = MutableStateFlow(false)
    val showTouchDialog: StateFlow<Boolean> = _showTouchDialog.asStateFlow()

    init {
        loadTouchSettings()
    }

    // ========== Find My Device ==========
    fun findMyDevice() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val success = profileManager.findMyDevice()

                if (success) {
                    _successMessage.value = "Device vibrating..."
                } else {
                    _errorMessage.value = "Failed to find device"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to find device: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    // ========== Unit System ==========
    fun showUnitDialog() {
        _showUnitDialog.value = true
    }

    fun dismissUnitDialog() {
        _showUnitDialog.value = false
    }

    fun updateUnitSystem(unitSystem: UnitSystem) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val success = profileManager.updateUnitSystem(unitSystem)

                if (success) {
                    _userSettings.value = _userSettings.value.copy(unitSystem = unitSystem)
                    _successMessage.value = "Unit system updated to ${unitSystem.displayName}"
                    _showUnitDialog.value = false
                } else {
                    _errorMessage.value = "Failed to update unit system"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ========== Time Format ==========
    fun showTimeFormatDialog() {
        _showTimeFormatDialog.value = true
    }

    fun dismissTimeFormatDialog() {
        _showTimeFormatDialog.value = false
    }

    fun updateTimeFormat(timeFormat: TimeFormat) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val success = profileManager.updateTimeFormat(timeFormat)

                if (success) {
                    _userSettings.value = _userSettings.value.copy(timeFormat = timeFormat)
                    _successMessage.value = "Time format updated to ${timeFormat.displayName}"
                    _showTimeFormatDialog.value = false
                } else {
                    _errorMessage.value = "Failed to update time format"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ========== Theme ==========
    fun showThemeDialog() {
        _showThemeDialog.value = true
    }

    fun dismissThemeDialog() {
        _showThemeDialog.value = false
    }

    fun updateThemeStyle(themeStyle: ThemeStyle) {
        _userSettings.value = _userSettings.value.copy(themeStyle = themeStyle)
        _showThemeDialog.value = false
        _successMessage.value = "Theme updated to ${themeStyle.displayName}"
    }

    // ========== Low Battery Prompt ==========
    fun toggleLowBatteryPrompt(enabled: Boolean) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val success = profileManager.toggleLowBatteryPrompt(enabled)

                if (success) {
                    _userSettings.value = _userSettings.value.copy(lowBatteryPrompt = enabled)
                    _successMessage.value = "Low battery prompt ${if (enabled) "enabled" else "disabled"}"
                } else {
                    _errorMessage.value = "Failed to update low battery prompt"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ========== Touch and Gestures ==========
    fun showTouchDialog() {
        _showTouchDialog.value = true
    }

    fun dismissTouchDialog() {
        _showTouchDialog.value = false
    }

    fun loadTouchSettings() {
        viewModelScope.launch {
            try {
                _isLoading.value = false
                val settings = profileManager.loadTouchSettings()
                settings?.let {
                    _touchSettings.value = it
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load settings: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTouchSettings(appType: Int, isTouch: Boolean, strength: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val success = profileManager.updateTouchSettings(appType, isTouch, strength)

                if (success) {
                    _touchSettings.value = TouchSettings(appType, isTouch, strength)
                    _successMessage.value = "Touch settings updated"
                    _showTouchDialog.value = false
                } else {
                    _errorMessage.value = "Failed to update touch settings"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ========== Message Clearing ==========
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

}
