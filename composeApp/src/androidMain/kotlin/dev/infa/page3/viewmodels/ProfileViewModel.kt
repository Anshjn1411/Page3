package dev.infa.page3.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oudmon.ble.base.communication.CommandHandle
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.req.*
import com.oudmon.ble.base.communication.rsp.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Data classes
data class TouchSettings(
    val appType: Int = 0,
    val isTouch: Boolean = true,
    val strength: Int = 5
)

data class UserSettings(
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    val timeFormat: TimeFormat = TimeFormat.HOUR_24,
    val lowBatteryPrompt: Boolean = true,
    val themeStyle: ThemeStyle = ThemeStyle.LIGHT
)

enum class UnitSystem(val displayName: String) {
    METRIC("Metric System"),
    IMPERIAL("Imperial System")
}

enum class TimeFormat(val displayName: String) {
    HOUR_12("12 Hour"),
    HOUR_24("24 Hour")
}

enum class ThemeStyle(val displayName: String) {
    LIGHT("Light"),
    DARK("Dark"),
    AUTO("Auto")
}

enum class AppType(val code: Int, val displayName: String) {
    CLOSE(0, "Close"),
    MUSIC(1, "Music"),
    VIDEO(2, "Video"),
    MUSLIM(3, "Muslim"),
    EBOOK(4, "E-Book"),
    CAMERA(5, "Camera"),
    PHONE_CALL(6, "Phone Call"),
    GAME(7, "Game"),
    HEART(8, "Heart")
}

class ProfileViewModel(
    private val commandHandle: CommandHandle = CommandHandle.getInstance()
) : ViewModel() {

    companion object {
        private const val TAG = "ProfileViewModel"
    }

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
        Log.d(TAG, "ProfileViewModel initialized")
        loadTouchSettings()
    }

    // ========== Find My Device ==========
    fun findMyDevice() {
        Log.d(TAG, "Finding device...")
        viewModelScope.launch {
            try {
                _isLoading.value = true
                withContext(Dispatchers.IO) {
                    commandHandle.executeReqCmd(FindDeviceReq(), null)
                }
                _successMessage.value = "Device vibrating..."
                Log.d(TAG, "Find device command sent successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to find device", e)
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
        Log.d(TAG, "Updating unit system to: ${unitSystem.displayName}")
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Update local state
                _userSettings.value = _userSettings.value.copy(unitSystem = unitSystem)

                // TODO: Send to device if supported
                // commandHandle.executeReqCmd(...)

                _successMessage.value = "Unit system updated to ${unitSystem.displayName}"
                _showUnitDialog.value = false

                Log.d(TAG, "Unit system updated successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update unit system", e)
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
        Log.d(TAG, "Updating time format to: ${timeFormat.displayName}")
        viewModelScope.launch {
            try {
                _isLoading.value = true

                withContext(Dispatchers.IO) {
                    // Send time format to device
                    val is24Hour = timeFormat == TimeFormat.HOUR_24
//                    commandHandle.executeReqCmd(
//                        SetTimeReq.getWriteInstance(is24Hour),
//                        object : ICommandResponse<SetTimeRsp> {
//                            override fun onDataResponse(resultEntity: SetTimeRsp) {
//                                Log.d(TAG, "Time format response: $resultEntity")
//                            }
//                        }
//                    )
                }

                // Update local state
                _userSettings.value = _userSettings.value.copy(timeFormat = timeFormat)
                _successMessage.value = "Time format updated to ${timeFormat.displayName}"
                _showTimeFormatDialog.value = false

                Log.d(TAG, "Time format updated successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update time format", e)
                _errorMessage.value = "Failed to update: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ========== Theme Style ==========
    fun showThemeDialog() {
        _showThemeDialog.value = true
    }

    fun dismissThemeDialog() {
        _showThemeDialog.value = false
    }

    fun updateThemeStyle(themeStyle: ThemeStyle) {
        Log.d(TAG, "Updating theme to: ${themeStyle.displayName}")
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Update local state
                _userSettings.value = _userSettings.value.copy(themeStyle = themeStyle)

                _successMessage.value = "Theme updated to ${themeStyle.displayName}"
                _showThemeDialog.value = false

                Log.d(TAG, "Theme updated successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update theme", e)
                _errorMessage.value = "Failed to update: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ========== Low Battery Prompt ==========
    fun toggleLowBatteryPrompt(enabled: Boolean) {
        Log.d(TAG, "Toggling low battery prompt: $enabled")
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Update local state
                _userSettings.value = _userSettings.value.copy(lowBatteryPrompt = enabled)

                // TODO: Send to device if supported

                _successMessage.value = "Low battery prompt ${if (enabled) "enabled" else "disabled"}"

                Log.d(TAG, "Low battery prompt updated")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update low battery prompt", e)
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
        Log.d(TAG, "Loading touch settings...")
        viewModelScope.launch {
            try {
                _isLoading.value = true

                withContext(Dispatchers.IO) {
                    commandHandle.executeReqCmd(
                        TouchControlReq.getReadInstance(false),
                        object : ICommandResponse<TouchControlResp> {
                            override fun onDataResponse(resultEntity: TouchControlResp) {
                                Log.d(TAG, "Touch settings loaded: appType=${resultEntity.appType}, strength=${resultEntity.strength}")

                                _touchSettings.value = TouchSettings(
                                    appType = resultEntity.appType,
                                    isTouch = false, // false = gestures mode
                                    strength = resultEntity.strength
                                )
                            }
                        }
                    )
                }

                Log.d(TAG, "Touch settings loaded successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load touch settings", e)
                _errorMessage.value = "Failed to load settings: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTouchSettings(appType: Int, isTouch: Boolean, strength: Int) {
        Log.d(TAG, "Updating touch settings: appType=$appType, isTouch=$isTouch, strength=$strength")
        viewModelScope.launch {
            try {
                _isLoading.value = true

                withContext(Dispatchers.IO) {
                    commandHandle.executeReqCmdNoCallback(
                        TouchControlReq.getWriteInstance(appType, isTouch, strength)
                    )
                }

                // Update local state
                _touchSettings.value = TouchSettings(appType, isTouch, strength)
                _successMessage.value = "Touch settings updated"
                _showTouchDialog.value = false

                Log.d(TAG, "Touch settings updated successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update touch settings", e)
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

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ProfileViewModel cleared")
    }
}