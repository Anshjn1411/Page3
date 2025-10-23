package dev.infa.page3.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.bluetooth.DeviceManager
import com.oudmon.ble.base.communication.CommandHandle
import com.oudmon.ble.base.communication.Constants
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.req.SetTimeReq
import com.oudmon.ble.base.communication.req.SimpleKeyReq
import com.oudmon.ble.base.communication.rsp.BaseRspCmd
import com.oudmon.ble.base.communication.rsp.BatteryRsp
import com.oudmon.ble.base.communication.rsp.SetTimeRsp
import com.oudmon.ble.base.scan.BleScannerHelper
import dev.infa.page3.bluetooth.BluetoothPairingService
import dev.infa.page3.bluetooth.SimpleBluetoothManager
import dev.infa.page3.models.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive

/**
 * Sealed class representing all possible UI states
 */
sealed class ConnectionUiState {
    object Idle : ConnectionUiState()
    object Scanning : ConnectionUiState()
    data class ScanSuccess(val devices: List<SmartWatch>) : ConnectionUiState()
    data class ScanError(val message: String) : ConnectionUiState()
    object Connecting : ConnectionUiState()
    data class Connected(
        val deviceName: String,
        val deviceAddress: String,
        val deviceCapabilities: DeviceCapabilities
    ) : ConnectionUiState()
    data class ConnectionError(val message: String) : ConnectionUiState()
    object Disconnected : ConnectionUiState()
    data class HealthDataReceived(val healthData: HealthData) : ConnectionUiState()
    data class HealthSettingsReceived(val healthSettings: HealthSettings) : ConnectionUiState()
    data class Loading(val message: String) : ConnectionUiState()
}

class ConnectionViewModel(
    private val context: Context,
    private val commandHandle: CommandHandle?
) : ViewModel()
{

    companion object {
        private const val TAG = "ConnectionViewModel"
    }

    // ✅ FIXED: Only one bluetooth manager
    private val bluetoothManager = SimpleBluetoothManager(context)

    // UI State
    private val _uiState = MutableStateFlow<ConnectionUiState>(ConnectionUiState.Idle)
    val uiState: StateFlow<ConnectionUiState> = _uiState.asStateFlow()
    val exposedCommandHandle: CommandHandle? = commandHandle

    // Logs
    private val _logMessages = MutableStateFlow<List<String>>(emptyList())
    val logMessages: StateFlow<List<String>> = _logMessages.asStateFlow()

    // Expose Bluetooth Manager States
    val discoveredDevices: StateFlow<List<SmartWatch>> = bluetoothManager.discoveredDevices
    val isScanning: StateFlow<Boolean> = bluetoothManager.isScanning
    val isConnected: StateFlow<Boolean> = bluetoothManager.isConnected
    val isConnecting: StateFlow<Boolean> = bluetoothManager.isConnecting
    val connectedDevice: StateFlow<SmartWatch?> = bluetoothManager.connectedDevice
    val connectionStatus: StateFlow<String> = bluetoothManager.connectionStatus

    // Device Information
    val deviceName: StateFlow<String> = connectedDevice.map { it?.deviceName ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val deviceAddress: StateFlow<String> = connectedDevice.map { it?.deviceAddress ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    // Device Capabilities
    private val _deviceCapabilities = MutableStateFlow(DeviceCapabilities())
    val deviceCapabilities: StateFlow<DeviceCapabilities> = _deviceCapabilities.asStateFlow()

    // Battery monitoring
    private var batteryMonitoringJob: Job? = null

    init {
        addLog("✅ ConnectionViewModel initialized")
        observeConnectionStates()
        syncToDeviceInfoManager()
    }

    /**
     * ✅ OPTIMIZED: Observe connection states
     */
    private fun observeConnectionStates() {
        viewModelScope.launch {
            combine(
                isScanning,
                isConnecting,
                isConnected,
                connectedDevice
            ) { scanning, connecting, connected, device ->
                when {
                    scanning -> ConnectionUiState.Scanning
                    connecting -> ConnectionUiState.Connecting
                    connected && device != null -> ConnectionUiState.Connected(
                        deviceName = device.deviceName,
                        deviceAddress = device.deviceAddress,
                        deviceCapabilities = deviceCapabilities.value
                    )
                    else -> ConnectionUiState.Idle
                }
            }.collect { state ->
                _uiState.value = state
                addLog("📊 State changed: ${state.javaClass.simpleName}")

                // Handle connected state
                if (state is ConnectionUiState.Connected) {
                    // Wait for device to be ready
                    delay(1000)
                    addLog("🔋 Requesting battery level...")
                    requestBatteryLevel()

                    addLog("⚙️ Fetching device capabilities...")
                    fetchDeviceCapabilities()

                    addLog("📈 Starting battery monitoring...")
                    startBatteryMonitoring()
                }

                // Handle disconnected state
                if (state is ConnectionUiState.Idle) {
                    stopBatteryMonitoring()
                }
            }
        }
    }

    /**
     * ✅ OPTIMIZED: Sync to DeviceInfoManager
     */
    private fun syncToDeviceInfoManager() {
        viewModelScope.launch {
            launch {
                isConnected.collect { DeviceInfoManager.setConnected(it) }
            }
            launch {
                isConnecting.collect { DeviceInfoManager.setConnecting(it) }
            }
            launch {
                combine(deviceName, deviceAddress) { name, address -> Pair(name, address) }
                    .collect { (name, address) -> DeviceInfoManager.setDeviceInfo(name, address) }
            }
            launch {
                connectionStatus.collect { DeviceInfoManager.setConnectionStatus(it) }
            }
            launch {
                connectedDevice.collect { device ->
                    device?.let {
                        DeviceInfoManager.setDeviceInfo(it.deviceName, it.deviceAddress)
                        DeviceInfoManager.setSignalStrength(it.rssi)
                    }
                }
            }
        }
    }

    // ==================== SCANNING ====================

    fun startScanning() {
        addLog("🔍 Starting scan...")
        val success = bluetoothManager.startScan()
        if (!success) {
            addLog("❌ Failed to start scan")
            _uiState.value = ConnectionUiState.ScanError("Failed to start scanning")
        }
    }

    fun stopScanning() {
        addLog("⏹️ Stopping scan...")
        bluetoothManager.stopScan()
    }

    // ==================== CONNECTION ====================

    fun connectToDevice(device: SmartWatch) {
        viewModelScope.launch {
            addLog("🔗 Connecting to ${device.deviceName}...")
            addLog("📍 Address: ${device.deviceAddress}")
            addLog("📶 RSSI: ${device.rssi} dBm")

            val success = bluetoothManager.connectToDevice(device)
            if (!success) {
                addLog("❌ Failed to initiate connection")
                _uiState.value = ConnectionUiState.ConnectionError("Connection failed")
            }
        }
    }

    fun disconnectDevice() {
        viewModelScope.launch {
            addLog("🔌 Disconnecting...")

            val success = bluetoothManager.disconnect()
            if (success) {
                stopBatteryMonitoring()
                DeviceInfoManager.reset()
                addLog("✅ Disconnected successfully")
            } else {
                addLog("❌ Disconnect failed")
            }
        }
    }

    /**
     * ✅ Auto-reconnect to last device
     */
    fun autoReconnect() {
        val lastAddress = DeviceManager.getInstance().deviceAddress
        if (lastAddress.isEmpty()) {
            addLog("⚠️ No saved device for reconnection")
            return
        }

        addLog("🔄 Auto-reconnecting to saved device: $lastAddress")

        viewModelScope.launch {
            delay(1000) // Small delay for Bluetooth to be ready

            val device = SmartWatch(
                deviceName = DeviceManager.getInstance().deviceName.ifEmpty { "Saved Device" },
                deviceAddress = lastAddress,
                rssi = 0
            )

            bluetoothManager.connectToDevice(device)
        }
    }

    // ==================== BATTERY ====================

    fun requestBatteryLevel() {
        viewModelScope.launch {
            try {
                if (commandHandle == null) {
                    addLog("⚠️ CommandHandle is null")
                    return@launch
                }

                addLog("🔋 Requesting battery...")

                commandHandle.executeReqCmd(
                    SimpleKeyReq(Constants.CMD_GET_DEVICE_ELECTRICITY_VALUE),
                    object : ICommandResponse<BatteryRsp> {
                        override fun onDataResponse(resultEntity: BatteryRsp) {
                            viewModelScope.launch {
                                if (resultEntity.status == BaseRspCmd.RESULT_OK) {
                                    val battery = resultEntity.batteryValue
                                    DeviceInfoManager.setBatteryLevel(battery)
                                    addLog("🔋 Battery: $battery% (${DeviceInfoManager.getBatteryStatus()})")
                                } else {
                                    addLog("⚠️ Battery request failed - Status: ${resultEntity.status}")
                                }
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                addLog("❌ Battery request error: ${e.message}")
                Log.e(TAG, "Battery error", e)
            }
        }
    }

    private fun startBatteryMonitoring() {
        batteryMonitoringJob?.cancel()

        batteryMonitoringJob = viewModelScope.launch {
            while (isActive && isConnected.value) {
                requestBatteryLevel()
                delay(300_000) // 5 minutes
            }
        }
        addLog("📈 Battery monitoring started (5 min interval)")
    }

    private fun stopBatteryMonitoring() {
        batteryMonitoringJob?.cancel()
        batteryMonitoringJob = null
        addLog("⏸️ Battery monitoring stopped")
    }

    // ==================== DEVICE CAPABILITIES ====================

    private fun fetchDeviceCapabilities() {
        viewModelScope.launch {
            try {
                if (commandHandle == null) {
                    addLog("⚠️ CommandHandle is null")
                    return@launch
                }

                commandHandle.executeReqCmd(
                    SetTimeReq(0),
                    object : ICommandResponse<SetTimeRsp> {
                        override fun onDataResponse(rsp: SetTimeRsp?) {
                            viewModelScope.launch {
                                rsp?.let {
                                    val capabilities = DeviceCapabilities(
                                        supportsTemperature = it.mSupportTemperature,
                                        supportsBloodOxygen = it.mSupportBloodOxygen,
                                        supportsBloodPressure = it.mSupportBloodPressure,
                                        supportsHrv = it.mSupportHrv,
                                        supportsOneKeyCheck = it.mSupportOneKeyCheck
                                    )
                                    _deviceCapabilities.value = capabilities
                                    DeviceInfoManager.setDeviceCapabilities(capabilities)
                                    addLog("✅ Device capabilities loaded")
                                } ?: addLog("⚠️ No capabilities response")
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                addLog("❌ Capabilities error: ${e.message}")
                Log.e(TAG, "Capabilities error", e)
            }
        }
    }

    // ==================== UTILITIES ====================

    fun addLog(message: String) {
        viewModelScope.launch {
            val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
            val logEntry = "[$timestamp] $message"

            val currentLogs = _logMessages.value.toMutableList()
            currentLogs.add(0, logEntry)
            if (currentLogs.size > 100) currentLogs.removeAt(100)
            _logMessages.value = currentLogs

            Log.d(TAG, message)
        }
    }

    fun clearLogs() {
        _logMessages.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        stopBatteryMonitoring()
        bluetoothManager.cleanup()
        addLog("🧹 ViewModel cleared")
    }
}





/**
 * Singleton object to manage device information across the app
 * Provides live updates of device connection status, battery, and other info
 */
object DeviceInfoManager {

    // Connection State
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _isConnecting = MutableStateFlow(false)
    val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()

    // Device Information
    private val _deviceName = MutableStateFlow("Unknown Device")
    val deviceName: StateFlow<String> = _deviceName.asStateFlow()

    private val _deviceAddress = MutableStateFlow("")
    val deviceAddress: StateFlow<String> = _deviceAddress.asStateFlow()

    private val _batteryLevel = MutableStateFlow(0)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    private val _connectionStatus = MutableStateFlow("Disconnected")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    private val _deviceCapabilities = MutableStateFlow(DeviceCapabilities())
    val deviceCapabilities: StateFlow<DeviceCapabilities> = _deviceCapabilities.asStateFlow()

    // Device Type/Model
    private val _deviceModel = MutableStateFlow("")
    val deviceModel: StateFlow<String> = _deviceModel.asStateFlow()

    private val _firmwareVersion = MutableStateFlow("")
    val firmwareVersion: StateFlow<String> = _firmwareVersion.asStateFlow()

    // Signal Strength (RSSI)
    private val _signalStrength = MutableStateFlow(-100)
    val signalStrength: StateFlow<Int> = _signalStrength.asStateFlow()

    // Last sync time
    private val _lastSyncTime = MutableStateFlow<Long?>(null)
    val lastSyncTime: StateFlow<Long?> = _lastSyncTime.asStateFlow()

    /**
     * Update connection state
     */
    fun setConnected(connected: Boolean) {
        _isConnected.value = connected
        _connectionStatus.value = if (connected) "Connected" else "Disconnected"

        // Clear data on disconnect
        if (!connected) {
            clearDeviceData()
        }
    }

    fun setConnecting(connecting: Boolean) {
        _isConnecting.value = connecting
        if (connecting) {
            _connectionStatus.value = "Connecting..."
        }
    }

    /**
     * Update device information
     */
    fun setDeviceInfo(name: String, address: String) {
        _deviceName.value = name
        _deviceAddress.value = address
    }

    fun setDeviceName(name: String) {
        _deviceName.value = name
    }

    fun setDeviceAddress(address: String) {
        _deviceAddress.value = address
    }

    fun setBatteryLevel(level: Int) {
        _batteryLevel.value = level.coerceIn(0, 100)
    }

    fun setConnectionStatus(status: String) {
        _connectionStatus.value = status
    }

    fun setDeviceCapabilities(capabilities: DeviceCapabilities) {
        _deviceCapabilities.value = capabilities
    }

    fun setDeviceModel(model: String) {
        _deviceModel.value = model
    }

    fun setFirmwareVersion(version: String) {
        _firmwareVersion.value = version
    }

    fun setSignalStrength(rssi: Int) {
        _signalStrength.value = rssi
    }

    fun updateLastSyncTime() {
        _lastSyncTime.value = System.currentTimeMillis()
    }


    /**
     * Get battery status description
     */
    fun getBatteryStatus(): String {
        return when (_batteryLevel.value) {
            in 80..100 -> "Excellent"
            in 50..79 -> "Good"
            in 20..49 -> "Low"
            in 0..19 -> "Critical"
            else -> "Unknown"
        }
    }


    /**
     * Clear all device data (on disconnect)
     */
    private fun clearDeviceData() {
        _deviceName.value = "Unknown Device"
        _deviceAddress.value = ""
        _batteryLevel.value = 0
        _deviceModel.value = ""
        _firmwareVersion.value = ""
        _signalStrength.value = -100
        _lastSyncTime.value = null
        _deviceCapabilities.value = DeviceCapabilities()
    }

    /**
     * Reset all state (for logout or app reset)
     */
    fun reset() {
        _isConnected.value = false
        _isConnecting.value = false
        _connectionStatus.value = "Disconnected"
        clearDeviceData()
    }

}