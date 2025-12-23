// commonMain/kotlin/dev/infa/page3/viewmodels/ConnectionViewModel.kt
package dev.infa.page3.SDK.viewModel

import dev.infa.page3.SDK.connection.ConnectionManager
import dev.infa.page3.SDK.connection.ConnectionState
import dev.infa.page3.SDK.connection.DeviceInfo
import dev.infa.page3.SDK.repository.ConnectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ConnectionUiState(
    val isScanning: Boolean = false,
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val devices: List<DeviceInfo> = emptyList(),
    val connectedDevice: DeviceInfo? = null,
    val batteryLevel: Int? = null,
    val errorMessage: String? = null,
    val hasPermissions: Boolean = false
) {
    // Convenience property for backward compatibility
    val isConnected: Boolean
        get() = connectionState == ConnectionState.CONNECTED
}

/**
 * ViewModel for managing BLE device connection UI state.
 * 
 * Responsibilities:
 * - Manage connection UI state
 * - Handle device scanning
 * - Handle device connection/disconnection
 * - Auto-connect to saved device on initialization
 * - Persist device information via Repository
 */
class ConnectionViewModel(
    private val manager: ConnectionManager,
    private val repository: ConnectionRepository
) {
    
    private val _uiState = MutableStateFlow(ConnectionUiState())
    val uiState: StateFlow<ConnectionUiState> = _uiState.asStateFlow()
    
    private var autoConnectAttempted = false
    
    init {
        manager.initialize()
        checkPermissions()

        // Observe connection state changes from ConnectionManager
        manager.observeConnectionState { state ->
            updateConnectionState(state)

            // When connected, save device and mark success
            if (state == ConnectionState.CONNECTED) {
                val device = _uiState.value.connectedDevice
                if (device != null) {
                    repository.saveLastConnectedDevice(
                        address = device.address,
                        name = device.name
                    )
                    repository.markConnectionSuccess()
                }
            } else if (state == ConnectionState.ERROR) {
                repository.markConnectionFailed()
            }
        }
        attemptAutoConnect()
    }
    
    /**
     * Check if permissions are granted and update UI state.
     * Should be called when permissions change (e.g., after user grants permissions).
     */
    fun checkPermissions() {
        val hasPermissions = manager.hasPermissions()
        _uiState.value = _uiState.value.copy(hasPermissions = hasPermissions)
        
        if (!hasPermissions && _uiState.value.connectionState != ConnectionState.DISCONNECTED) {
            // Permissions lost, disconnect
            _uiState.value = _uiState.value.copy(
                connectionState = ConnectionState.DISCONNECTED,
                connectedDevice = null,
                errorMessage = "Bluetooth permissions required"
            )
        } else if (hasPermissions && !autoConnectAttempted) {
            // Permissions just granted, try auto-connect
            attemptAutoConnect()
        }
    }
    
    /**
     * Attempt to auto-connect to saved device if:
     * - Auto-connect hasn't been attempted yet
     * - Permissions are granted
     * - Repository has a saved device
     * - Auto-connect is enabled
     */
    private fun attemptAutoConnect() {
        if (autoConnectAttempted) return
        
        if (!manager.hasPermissions()) {
            // Will retry when permissions are granted
            return
        }
        
        if (!repository.isAutoConnectEnabled()) {
            autoConnectAttempted = true
            return
        }
        
        val savedDevice = repository.getSavedDeviceInfo()
        if (savedDevice != null) {
            autoConnectAttempted = true
            
            // Only auto-connect if device is recent (within 24 hours)
            if (repository.isDeviceRecent()) {
                _uiState.value = _uiState.value.copy(
                    connectedDevice = savedDevice.toDeviceInfo(),
                    connectionState = ConnectionState.CONNECTING
                )
                
                try {
                    manager.connect(savedDevice.name, savedDevice.address)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        connectionState = ConnectionState.ERROR,
                        errorMessage = "Auto-connect failed: ${e.message}"
                    )
                }
            } else {
                // Device is too old, clear it
                repository.clearSavedDevice()
            }
        } else {
            autoConnectAttempted = true
        }
    }
    
    /**
     * Update connection state and sync connected device info.
     */
    private fun updateConnectionState(state: ConnectionState) {
        val currentDevice = _uiState.value.connectedDevice
        
        when (state) {
            ConnectionState.CONNECTED -> {
                // Update connection state but keep current device
                _uiState.value = _uiState.value.copy(
                    connectionState = state,
                    errorMessage = null
                )
                // Optionally refresh battery when connected
                refreshBattery()
            }
            ConnectionState.CONNECTING, ConnectionState.RECONNECTING -> {
                _uiState.value = _uiState.value.copy(
                    connectionState = state,
                    errorMessage = null
                )
            }
            ConnectionState.DISCONNECTED -> {
                _uiState.value = _uiState.value.copy(
                    connectionState = state,
                    errorMessage = null
                )
                // Keep connectedDevice in state for UI, but state shows disconnected
            }
            ConnectionState.ERROR -> {
                _uiState.value = _uiState.value.copy(
                    connectionState = state,
                    errorMessage = "Connection failed"
                )
            }
        }
    }
    
    /**
     * Start scanning for BLE devices.
     */
    fun startScan() {
        if (!manager.hasPermissions()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Bluetooth permissions required",
                hasPermissions = false
            )
            return
        }
        
        _uiState.value = _uiState.value.copy(
            isScanning = true,
            devices = emptyList(),
            errorMessage = null
        )
        
        manager.startScan(
            onDeviceFound = { device ->
                val updatedDevices = _uiState.value.devices
                    .filter { it.address != device.address }
                    .plus(device)
                    .sortedByDescending { it.rssi }
                
                _uiState.value = _uiState.value.copy(devices = updatedDevices)
            },
            onError = { error ->
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    errorMessage = error
                )
            }
        )
    }
    
    /**
     * Stop scanning for devices.
     */
    fun stopScan() {
        manager.stopScan()
        _uiState.value = _uiState.value.copy(isScanning = false)
    }
    
    /**
     * Connect to a device.
     * This saves the device to repository and initiates connection.
     */
    fun connect(device: DeviceInfo) {
        if (!manager.hasPermissions()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Bluetooth permissions required",
                hasPermissions = false
            )
            return
        }
        
        stopScan()
        
        // Save device info immediately
        repository.saveLastConnectedDevice(
            address = device.address,
            name = device.name
        )
        
        _uiState.value = _uiState.value.copy(
            connectedDevice = device,
            connectionState = ConnectionState.CONNECTING,
            errorMessage = null
        )
        
        manager.connect(device.name, device.address)
    }
    
    /**
     * Disconnect from current device.
     * This does NOT clear the saved device - user can reconnect.
     */
    fun disconnect() {
        manager.disconnect()
        forgetDevice()
        _uiState.value = _uiState.value.copy(
            connectedDevice = null,
            batteryLevel = null,
            connectionState = ConnectionState.DISCONNECTED,
            errorMessage = null
        )
    }
    
    /**
     * Clear saved device and disconnect.
     */
    fun forgetDevice() {
        repository.clearSavedDevice()
    }
    
    /**
     * Refresh battery level from connected device.
     */
    fun refreshBattery() {
        if (_uiState.value.connectionState != ConnectionState.CONNECTED) return
        
        manager.getBatteryLevel { level ->
            _uiState.value = _uiState.value.copy(batteryLevel = level)
        }
    }
    
    /**
     * Clear error message.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * Retry auto-connect (useful when permissions are granted).
     */
    fun retryAutoConnect() {
        autoConnectAttempted = false
        attemptAutoConnect()
    }
}