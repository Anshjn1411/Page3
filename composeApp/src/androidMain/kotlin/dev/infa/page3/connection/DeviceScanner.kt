//package dev.infa.page3.connection
//
//import android.annotation.SuppressLint
//import android.bluetooth.BluetoothAdapter
//import android.bluetooth.BluetoothDevice
//import android.bluetooth.le.ScanResult
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.content.IntentFilter
//import android.os.Handler
//import android.os.Looper
//import android.util.Log
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.setValue
//import com.oudmon.ble.base.bluetooth.BleOperateManager
//import com.oudmon.ble.base.bluetooth.DeviceManager
//import com.oudmon.ble.base.communication.CommandHandle
//import com.oudmon.ble.base.communication.ICommandResponse
//import com.oudmon.ble.base.communication.req.DeviceSupportReq
//import com.oudmon.ble.base.communication.req.SetTimeReq
//import com.oudmon.ble.base.communication.rsp.BaseRspCmd
//import com.oudmon.ble.base.communication.rsp.DeviceSupportFunctionRsp
//import com.oudmon.ble.base.communication.rsp.SetTimeRsp
//import com.oudmon.ble.base.scan.BleScannerHelper
//import com.oudmon.ble.base.scan.ScanRecord
//import com.oudmon.ble.base.scan.ScanWrapperCallback
//import com.oudmon.ble.base.util.BluetoothUtils
//import dev.infa.page3.models.BluetoothEvent
//import dev.infa.page3.models.SmartWatch
//import dev.infa.page3.bluetooth.BluetoothRepository
//import dev.infa.page3.models.DeviceCapabilities
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import org.greenrobot.eventbus.EventBus
//import org.greenrobot.eventbus.Subscribe
//import org.greenrobot.eventbus.ThreadMode
//
//class DeviceScanner(
//    private val context: Context,
//    private val bleOperateManager: BleOperateManager?,
//    private val deviceManager: DeviceManager?,
//    val commandHandle: CommandHandle?,
//    private val addLog: (String) -> Unit
//) {
//    companion object {
//        private const val SCAN_TIMEOUT_MS = 30000L
//        private const val CONNECTION_TIMEOUT_MS = 15000L
//        private const val MAX_RETRY_ATTEMPTS = 2
//    }
//
//    // State Flows
//    private val _discoveredDevices = MutableStateFlow<List<SmartWatch>>(emptyList())
//    val discoveredDevices: StateFlow<List<SmartWatch>> = _discoveredDevices.asStateFlow()
//
//    private val _selectedDevice = MutableStateFlow<SmartWatch?>(null)
//    val selectedDevice: StateFlow<SmartWatch?> = _selectedDevice.asStateFlow()
//
//    private val _isScanning = MutableStateFlow(false)
//    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()
//
//    private val _isConnected = MutableStateFlow(false)
//    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
//
//    private val _isConnecting = MutableStateFlow(false)
//    val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()
//
//    private val _deviceName = MutableStateFlow("")
//    val deviceName: StateFlow<String> = _deviceName.asStateFlow()
//
//    private val _deviceAddress = MutableStateFlow("")
//    val deviceAddress: StateFlow<String> = _deviceAddress.asStateFlow()
//
//    private val _deviceCapabilities = MutableStateFlow(DeviceCapabilities())
//    val deviceCapabilities: StateFlow<DeviceCapabilities> = _deviceCapabilities.asStateFlow()
//
//    private val _connectionStatus = MutableStateFlow("Not connected")
//    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()
//
//    // Managers
//    private val persistenceManager = ConnectionPersistenceManager(context)
//    private val bluetoothRepository = BluetoothRepository(context)
//    private val handler = Handler(Looper.getMainLooper())
//    private val scanTimeoutRunnable = Runnable { stopScanning() }
//    private var connectionTimeoutRunnable: Runnable? = null
//    private var retryAttempts = 0
//    private var isEventBusRegistered = false
//
//    init {
//        registerEventBus()
//        setupBleManager()
//    }
//
//    private fun registerEventBus() {
//        if (!EventBus.getDefault().isRegistered(this)) {
//            EventBus.getDefault().register(this)
//            isEventBusRegistered = true
//        }
//    }
//
//    private fun setupBleManager() {
//        bleOperateManager?.apply {
//            setBluetoothTurnOff(true) // Enable system Bluetooth monitoring
//            setNeedConnect(true) // Enable auto-reconnect
//        }
//    }
//
//    // ============ EventBus Listener ============
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onBluetoothEvent(event: BluetoothEvent) {
//        addLog("📡 Event: connect=${event.connect}, device=${event.deviceName}")
//
//        if (event.connect) {
//            handleConnectionSuccess(event.deviceName, event.deviceAddress)
//        } else {
//            handleConnectionLost()
//        }
//    }
//
//    // ============ Scanning ============
//    fun startScanning(): Boolean {
//        if (!BluetoothUtils.isEnabledBluetooth(context)) {
//            addLog("❌ Bluetooth is disabled")
//            return false
//        }
//
//        // Stop any existing scan
//        stopScanning()
//
//        _isScanning.value = true
//        _discoveredDevices.value = emptyList()
//        addLog("🔍 Starting scan...")
//
//        return try {
//            val scanner = BleScannerHelper.getInstance()
//            scanner.reSetCallback()
//            scanner.scanDevice(context, null, scanCallback)
//            handler.postDelayed(scanTimeoutRunnable, SCAN_TIMEOUT_MS)
//            true
//        } catch (e: Exception) {
//            addLog("❌ Scan failed: ${e.message}")
//            _isScanning.value = false
//            false
//        }
//    }
//
//    fun stopScanning() {
//        if (_isScanning.value) {
//            try {
//                BleScannerHelper.getInstance().stopScan(context)
//            } catch (e: Exception) {
//                addLog("⚠️ Stop scan error: ${e.message}")
//            }
//            handler.removeCallbacks(scanTimeoutRunnable)
//            _isScanning.value = false
//            addLog("⏹️ Scan stopped - ${_discoveredDevices.value.size} devices found")
//        }
//    }
//
//    private val scanCallback = object : ScanWrapperCallback {
//        override fun onStart() {
//            addLog("🔍 Scan started")
//        }
//
//        override fun onStop() {
//            _isScanning.value = false
//            addLog("⏹️ Scan finished")
//        }
//
//        override fun onScanFailed(errorCode: Int) {
//            addLog("❌ Scan error: $errorCode")
//            _isScanning.value = false
//        }
//
//        override fun onLeScan(device: BluetoothDevice?, rssi: Int, scanRecord: ByteArray?) {
//            device?.let {
//                if (isQCDevice(it.name, scanRecord)) {
//                    val smartWatch = SmartWatch(
//                        deviceName = it.name ?: "Unknown",
//                        deviceAddress = it.address,
//                        rssi = rssi,
//                        deviceType = determineDeviceType(it.name)
//                    )
//                    val current = _discoveredDevices.value.toMutableList()
//                    val existing = current.find { d -> d.deviceAddress == smartWatch.deviceAddress }
//
//                    if (existing == null) {
//                        current.add(smartWatch)
//                        current.sortByDescending { d -> d.rssi }
//                        _discoveredDevices.value = current
//                        addLog("📱 Found: ${smartWatch.deviceName} (${smartWatch.rssi} dBm)")
//                    } else if (existing.rssi != rssi) {
//                        // Update RSSI if changed
//                        existing.rssi = rssi
//                        current.sortByDescending { d -> d.rssi }
//                        _discoveredDevices.value = current
//                    }
//                }
//            }
//        }
//
//        override fun onParsedData(device: BluetoothDevice?, scanRecord: ScanRecord?) {}
//        override fun onBatchScanResults(results: MutableList<ScanResult>?) {}
//    }
//
//    private fun isQCDevice(name: String?, scanRecord: ByteArray?): Boolean {
//        val deviceName = name?.uppercase() ?: return false
//        val patterns = listOf("QC_", "O_", "Q_", "R10_", "RING", "BAND", "WATCH", "GREEN", "ORANGE")
//        if (patterns.any { deviceName.contains(it) }) return true
//
//        scanRecord?.let {
//            val hex = it.joinToString("") { b -> "%02x".format(b) }
//            if (hex.contains("ffe0") || hex.contains("fff0")) return true
//        }
//        return deviceName.isNotBlank() && deviceName != "UNKNOWN DEVICE"
//    }
//
//    private fun determineDeviceType(name: String?): String {
//        val upper = name?.uppercase() ?: return "Device"
//        return when {
//            upper.contains("RING") -> "Ring"
//            upper.contains("BAND") -> "Band"
//            upper.contains("WATCH") || upper.contains("R10") -> "Watch"
//            else -> "Device"
//        }
//    }
//
//    // ============ Connection ============
//    fun selectDevice(device: SmartWatch) {
//        _selectedDevice.value = device
//        _deviceName.value = device.deviceName
//        _deviceAddress.value = device.deviceAddress
//        addLog("✓ Selected: ${device.deviceName}")
//    }
//
//    fun connectDevice(): Boolean {
//        val address = _deviceAddress.value
//        val name = _deviceName.value
//
//        if (address.isEmpty() || name.isEmpty()) {
//            addLog("❌ No device selected")
//            return false
//        }
//
//        if (bleOperateManager == null) {
//            addLog("❌ BLE Manager not initialized")
//            return false
//        }
//
//        if (_isConnecting.value) {
//            addLog("⚠️ Already connecting...")
//            return false
//        }
//
//        // Stop scanning first
//        stopScanning()
//
//        _isConnecting.value = true
//        _connectionStatus.value = "Connecting..."
//        retryAttempts = 0
//        addLog("🔄 Connecting to $name...")
//
//        // Clean any previous connection
//        try {
//            if (bleOperateManager.isConnected) {
//                addLog("⚠️ Disconnecting previous device...")
//                bleOperateManager.unBindDevice()
//                bleOperateManager.disconnect()
//                Thread.sleep(500) // Wait for clean disconnect
//            }
//        } catch (e: Exception) {
//            addLog("⚠️ Cleanup: ${e.message}")
//        }
//
//        // CRITICAL: Update DeviceManager BEFORE connecting
//        deviceManager?.apply {
//            this.deviceName = name
//            this.deviceAddress = address
//            addLog("✓ Device manager updated: $name @ $address")
//        }
//
//        // Start connection with timeout
//        startConnectionWithTimeout()
//
//        // Connect using scan method (more reliable)
//        try {
//            bleOperateManager.connectWithScan(address)
//            addLog("📡 Connection initiated...")
//            return true
//        } catch (e: Exception) {
//            addLog("❌ Connection error: ${e.message}")
//            onConnectionFailed("Connection error: ${e.message}")
//            return false
//        }
//    }
//
//    private fun startConnectionWithTimeout() {
//        connectionTimeoutRunnable?.let { handler.removeCallbacks(it) }
//        connectionTimeoutRunnable = Runnable {
//            if (_isConnecting.value && !_isConnected.value) {
//                handleConnectionTimeout()
//            }
//        }
//        handler.postDelayed(connectionTimeoutRunnable!!, CONNECTION_TIMEOUT_MS)
//    }
//
//    private fun handleConnectionTimeout() {
//        if (retryAttempts < MAX_RETRY_ATTEMPTS) {
//            retryAttempts++
//            addLog("⚠️ Timeout, retrying ($retryAttempts/$MAX_RETRY_ATTEMPTS)...")
//
//            try {
//                // Try direct connection on retry
//                if (retryAttempts == 1) {
//                    bleOperateManager?.connectWithScan(_deviceAddress.value)
//                } else {
//                    bleOperateManager?.connectDirectly(_deviceAddress.value)
//                }
//                startConnectionWithTimeout()
//            } catch (e: Exception) {
//                addLog("❌ Retry error: ${e.message}")
//                onConnectionFailed("Retry failed")
//            }
//        } else {
//            onConnectionFailed("Connection timeout after $MAX_RETRY_ATTEMPTS attempts")
//        }
//    }
//
//    private fun handleConnectionSuccess(name: String, address: String) {
//        connectionTimeoutRunnable?.let { handler.removeCallbacks(it) }
//
//        if (_isConnected.value) {
//            addLog("⚠️ Already connected, ignoring duplicate event")
//            return
//        }
//
//        _isConnected.value = true
//        _isConnecting.value = false
//        _connectionStatus.value = "Connected"
//        _deviceName.value = name
//        _deviceAddress.value = address
//
//        addLog("✅ Connected to $name")
//        addLog("📍 Address: $address")
//
//        // Save connection
//        persistenceManager.saveConnection(address, name)
//        bluetoothRepository.saveLastConnectedDevice(
//            address, name, _selectedDevice.value?.deviceType ?: "Device"
//        )
//        bluetoothRepository.markConnectionSuccess()
//        bluetoothRepository.resetConnectionAttempts()
//
//        // Enable auto-reconnect
//        bleOperateManager?.setNeedConnect(true)
//
//        // Start background service
//        BackgroundConnectionService.start(context, name, address)
//
//        // Fetch device capabilities
//        handler.postDelayed({
//            fetchDeviceCapabilities()
//        }, 1000)
//    }
//
//    private fun onConnectionFailed(reason: String) {
//        connectionTimeoutRunnable?.let { handler.removeCallbacks(it) }
//
//        _isConnecting.value = false
//        _connectionStatus.value = "Failed"
//        addLog("❌ Connection failed: $reason")
//
//        bluetoothRepository.markConnectionFailed()
//        val attempts = bluetoothRepository.incrementConnectionAttempts()
//        addLog("⚠️ Failed attempts: $attempts")
//
//        // Clean up
//        try {
//            bleOperateManager?.disconnect()
//        } catch (e: Exception) {}
//    }
//
//    private fun handleConnectionLost() {
//        if (!_isConnected.value) return
//
//        addLog("⚠️ Connection lost")
//        _isConnected.value = false
//        _connectionStatus.value = "Disconnected"
//
//        // Auto-reconnect if enabled and we have a device
//        if (bluetoothRepository.isAutoConnectEnabled() &&
//            _deviceAddress.value.isNotEmpty() &&
//            bluetoothRepository.getConnectionAttempts() < MAX_RETRY_ATTEMPTS) {
//
//            addLog("🔄 Auto-reconnecting in 3s...")
//            handler.postDelayed({
//                if (!_isConnected.value && !_isConnecting.value) {
//                    bluetoothRepository.incrementConnectionAttempts()
//                    connectDevice()
//                }
//            }, 3000)
//        } else {
//            addLog("⚠️ Auto-reconnect disabled or max attempts reached")
//        }
//    }
//
//    fun disconnectDevice(): Boolean {
//        if (!_isConnected.value && !_isConnecting.value) {
//            addLog("⚠️ Not connected")
//            return false
//        }
//
//        try {
//            addLog("🔌 Disconnecting...")
//
//            // Stop background service
//            BackgroundConnectionService.stop(context)
//
//            // Disable auto-reconnect
//            bleOperateManager?.setNeedConnect(false)
//
//            // Disconnect
//            bleOperateManager?.unBindDevice()
//            bleOperateManager?.disconnect()
//
//            // Reset state
//            _isConnected.value = false
//            _isConnecting.value = false
//            _connectionStatus.value = "Disconnected"
//
//            addLog("✅ Disconnected")
//
//            // Don't clear device info - keep for reconnection
//            return true
//        } catch (e: Exception) {
//            addLog("❌ Disconnect error: ${e.message}")
//            return false
//        }
//    }
//
//    fun clearDeviceAndDisconnect() {
//        disconnectDevice()
//
//        _deviceName.value = ""
//        _deviceAddress.value = ""
//        _selectedDevice.value = null
//
//        persistenceManager.clearConnection()
//        bluetoothRepository.clearSavedDevice()
//
//        addLog("✅ Device cleared")
//    }
//
//    fun attemptAutoReconnect(): Boolean {
//        if (!bluetoothRepository.isAutoConnectEnabled()) {
//            addLog("⚠️ Auto-connect disabled")
//            return false
//        }
//
//        val savedInfo = bluetoothRepository.getSavedDeviceInfo()
//        if (savedInfo == null) {
//            addLog("⚠️ No saved device")
//            return false
//        }
//
//        // Check if already connected
//        if (bleOperateManager?.isConnected == true) {
//            addLog("✅ Already connected to ${savedInfo.name}")
//            _isConnected.value = true
//            _deviceName.value = savedInfo.name
//            _deviceAddress.value = savedInfo.address
//            _connectionStatus.value = "Connected"
//
//            // Update device manager
//            deviceManager?.apply {
//                deviceName = savedInfo.name
//                deviceAddress = savedInfo.address
//            }
//
//            BackgroundConnectionService.start(context, savedInfo.name, savedInfo.address)
//            fetchDeviceCapabilities()
//            return true
//        }
//
//        // Reconnect
//        addLog("🔄 Auto-reconnecting to ${savedInfo.name}")
//        _deviceAddress.value = savedInfo.address
//        _deviceName.value = savedInfo.name
//        _selectedDevice.value = SmartWatch(savedInfo.name, savedInfo.address, 0, savedInfo.type)
//
//        // Update device manager
//        deviceManager?.apply {
//            deviceName = savedInfo.name
//            deviceAddress = savedInfo.address
//        }
//
//        return connectDevice()
//    }
//
//    // ============ Device Capabilities ============
//    private fun fetchDeviceCapabilities() {
//        addLog("📊 Fetching capabilities...")
//
//        commandHandle?.executeReqCmd(
//            SetTimeReq(0),
//            object : ICommandResponse<SetTimeRsp> {
//                override fun onDataResponse(rsp: SetTimeRsp?) {
//                    rsp?.let {
//                        _deviceCapabilities.value = DeviceCapabilities(
//                            supportsTemperature = it.mSupportTemperature,
//                            supportsBloodOxygen = it.mSupportBloodOxygen,
//                            supportsBloodPressure = it.mSupportBloodPressure,
//                            supportsHrv = it.mSupportHrv,
//                            supportsOneKeyCheck = it.mSupportOneKeyCheck
//                        )
//                        addLog("✅ Capabilities loaded")
//                    } ?: addLog("⚠️ No capabilities response")
//                }
//            }
//        )
//    }
//
//    fun cleanup() {
//        addLog("🧹 Cleaning up...")
//        stopScanning()
//        connectionTimeoutRunnable?.let { handler.removeCallbacks(it) }
//        handler.removeCallbacksAndMessages(null)
//
//        if (isEventBusRegistered) {
//            EventBus.getDefault().unregister(this)
//            isEventBusRegistered = false
//        }
//    }
//}
//object BluetoothConnectionHelper {
//
//    /**
//     * Check if device is bonded (paired) at system level
//     */
//    @SuppressLint("MissingPermission")
//    fun isDeviceBonded(context: Context, deviceAddress: String): Boolean {
//        if (!BluetoothUtils.isEnabledBluetooth(context)) return false
//
//        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ?: return false
//        val bondedDevices = bluetoothAdapter.bondedDevices
//
//        return bondedDevices.any { it.address == deviceAddress }
//    }
//
//    /**
//     * Get bonded device by address
//     */
//    @SuppressLint("MissingPermission")
//    fun getBondedDevice(context: Context, deviceAddress: String): BluetoothDevice? {
//        if (!BluetoothUtils.isEnabledBluetooth(context)) return null
//
//        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ?: return null
//        return bluetoothAdapter.bondedDevices.firstOrNull { it.address == deviceAddress }
//    }
//
//    /**
//     * Create bond (pair) with device
//     * Note: This requires BLUETOOTH_ADMIN permission
//     */
//    @SuppressLint("MissingPermission")
//    fun createBond(device: BluetoothDevice): Boolean {
//        return try {
//            device.createBond()
//        } catch (e: Exception) {
//            false
//        }
//    }
//
//    /**
//     * Remove bond from device
//     */
//    @SuppressLint("MissingPermission")
//    fun removeBond(device: BluetoothDevice): Boolean {
//        return try {
//            val method = device.javaClass.getMethod("removeBond")
//            method.invoke(device) as? Boolean ?: false
//        } catch (e: Exception) {
//            false
//        }
//    }
//
//    /**
//     * Register broadcast receiver for bond state changes
//     */
//    fun registerBondStateReceiver(
//        context: Context,
//        onBondStateChanged: (BluetoothDevice, Int) -> Unit
//    ): BroadcastReceiver {
//        val receiver = object : BroadcastReceiver() {
//            override fun onReceive(context: Context?, intent: Intent?) {
//                if (intent?.action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
//                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
//                    val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)
//                    device?.let { onBondStateChanged(it, bondState) }
//                }
//            }
//        }
//
//        val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
//        context.registerReceiver(receiver, filter)
//        return receiver
//    }
//
//    /**
//     * Get human-readable bond state
//     */
//    fun getBondStateString(state: Int): String {
//        return when (state) {
//            BluetoothDevice.BOND_NONE -> "Not Bonded"
//            BluetoothDevice.BOND_BONDING -> "Bonding..."
//            BluetoothDevice.BOND_BONDED -> "Bonded"
//            else -> "Unknown"
//        }
//    }
//}
//
///**
// * Extension functions for easier device management
// */
//@SuppressLint("MissingPermission")
//fun BluetoothDevice.getBondStateString(): String {
//    return BluetoothConnectionHelper.getBondStateString(this.bondState)
//}
//
//@SuppressLint("MissingPermission")
//fun BluetoothDevice.isBonded(): Boolean {
//    return this.bondState == BluetoothDevice.BOND_BONDED
//}
//
//@SuppressLint("MissingPermission")
//fun BluetoothDevice.bondIfNeeded(): Boolean {
//    if (this.bondState == BluetoothDevice.BOND_BONDED) return true
//    return BluetoothConnectionHelper.createBond(this)
//}