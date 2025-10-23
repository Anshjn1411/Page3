package dev.infa.page3.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Looper
import android.util.Log
import androidx.core.os.postDelayed
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.bluetooth.DeviceManager
import com.oudmon.ble.base.bluetooth.QCBluetoothCallbackCloneReceiver
import com.oudmon.ble.base.communication.CommandHandle
import com.oudmon.ble.base.communication.Constants
import com.oudmon.ble.base.communication.LargeDataHandler
import com.oudmon.ble.base.communication.req.SimpleKeyReq
import com.oudmon.ble.base.scan.BleScannerHelper
import com.oudmon.ble.base.scan.ScanWrapperCallback
import com.oudmon.ble.base.util.BluetoothUtils
import dev.infa.page3.connection.BackgroundConnectionService
import dev.infa.page3.models.BluetoothEvent
import dev.infa.page3.models.SmartWatch
import dev.infa.page3.viewmodels.DeviceInfoManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.concurrent.atomic.AtomicBoolean
import android.os.Handler
import kotlin.text.get
import kotlin.text.set

class SimpleBluetoothManager(private val context: Context) {

    companion object {
        private const val TAG = "SimpleBluetoothManager"
        private const val SCAN_TIMEOUT_MS = 15000L
        private const val MAX_SCAN_SIZE = 30
        private const val CONNECTION_TIMEOUT_MS = 30000L
        private const val BIND_COMMAND_DELAY = 3000L
    }

    // State flows
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<SmartWatch>>(emptyList())
    val discoveredDevices: StateFlow<List<SmartWatch>> = _discoveredDevices.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _isConnecting = MutableStateFlow(false)
    val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()

    private val _connectedDevice = MutableStateFlow<SmartWatch?>(null)
    val connectedDevice: StateFlow<SmartWatch?> = _connectedDevice.asStateFlow()

    private val _connectionStatus = MutableStateFlow("Disconnected")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    // Managers
    private val bleManager = BleOperateManager.getInstance()
    private val scanner = BleScannerHelper.getInstance()
    private val deviceManager = DeviceManager.getInstance()
    private val commandHandle = CommandHandle.getInstance()

    private var scanSize = 0
    private val handler = Handler(Looper.getMainLooper())
    private var scanTimeoutRunnable: Runnable? = null
    private var connectionTimeoutRunnable: Runnable? = null
    private var bindCommandRunnable: Runnable? = null
    private var pendingDevice: SmartWatch? = null
    private var isConnectionInProgress = false

    init {
        Log.d(TAG, "🔧 Initializing SimpleBluetoothManager")
        setupBluetoothManager()
        registerBluetoothReceiver()
        registerEventBus()
    }

    private fun registerEventBus() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
            Log.d(TAG, "📡 EventBus registered")
        }
    }

    /**
     * ✅ THIS IS THE ONLY PLACE WHERE CONNECTION IS CONFIRMED
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: BluetoothEvent) {
        Log.d(TAG, "📩 [EventBus] BluetoothEvent: connect=${event.connect}")

        handler.post {
            cancelConnectionTimeout()
            cancelBindCommand()

            if (event.connect) {
                Log.d(TAG, "✅ [EventBus] CONNECTION CONFIRMED!")

                val device = pendingDevice ?: SmartWatch(
                    deviceName = deviceManager.deviceName.ifEmpty { "Unknown Device" },
                    deviceAddress = deviceManager.deviceAddress,
                    rssi = 0
                )

                isConnectionInProgress = false
                _isConnecting.value = false
                _isConnected.value = true
                _connectedDevice.value = device
                _connectionStatus.value = "Connected to ${device.deviceName}"
                pendingDevice = null

                Log.d(TAG, "✅ Device ready: ${device.deviceName} (${device.deviceAddress})")

                // ✅ CRITICAL: Start background service ONLY AFTER EventBus confirms!
                handler.postDelayed({
                    startBackgroundService(device)
                }, 2000) // Wait 2 more seconds for everything to stabilize

            } else {
                Log.e(TAG, "❌ [EventBus] CONNECTION FAILED")

                isConnectionInProgress = false
                _isConnecting.value = false
                _isConnected.value = false
                _connectedDevice.value = null
                _connectionStatus.value = "Connection failed"
                pendingDevice = null
            }
        }
    }

    private fun setupBluetoothManager() {
        bleManager.setBluetoothTurnOff(true)
        bleManager.setNeedConnect(true)
        Log.d(TAG, "✅ BLE Manager configured")
    }

    private fun registerBluetoothReceiver() {
        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        }
        context.registerReceiver(bluetoothReceiver, filter)
        Log.d(TAG, "📻 Bluetooth receiver registered")
    }

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                    when (state) {
                        BluetoothAdapter.STATE_OFF -> {
                            Log.i(TAG, "📴 Bluetooth OFF")
                            bleManager.setBluetoothTurnOff(false)
                            bleManager.disconnect()
                            onBluetoothDisconnected()
                        }
                        BluetoothAdapter.STATE_ON -> {
                            Log.i(TAG, "📶 Bluetooth ON")
                            bleManager.setBluetoothTurnOff(true)
                        }
                    }
                }
            }
        }
    }

    fun startScan(): Boolean {
        Log.d(TAG, "🔍 startScan() called")

        if (!BluetoothUtils.isEnabledBluetooth(context)) {
            Log.w(TAG, "❌ Bluetooth is disabled")
            _connectionStatus.value = "Bluetooth disabled"
            return false
        }

        if (_isScanning.value) {
            Log.w(TAG, "⚠️ Already scanning")
            return false
        }

        _discoveredDevices.value = emptyList()
        scanSize = 0
        _isScanning.value = true
        _connectionStatus.value = "Scanning..."

        scanner.reSetCallback()
        scanner.scanDevice(context, null, createScanCallback())

        scanTimeoutRunnable = Runnable {
            Log.d(TAG, "⏰ Scan timeout - stopping")
            stopScan()
        }
        handler.postDelayed(scanTimeoutRunnable!!, SCAN_TIMEOUT_MS)

        Log.d(TAG, "✅ Scan started (15s timeout)")
        return true
    }

    fun stopScan() {
        if (!_isScanning.value) return

        scanner.stopScan(context)
        _isScanning.value = false

        val deviceCount = _discoveredDevices.value.size
        _connectionStatus.value = if (deviceCount > 0) {
            "Found $deviceCount device${if (deviceCount != 1) "s" else ""}"
        } else {
            "No devices found"
        }

        scanTimeoutRunnable?.let { handler.removeCallbacks(it) }
        Log.d(TAG, "⏹️ Scan stopped - Total: $deviceCount devices")
    }

    fun connectToDevice(device: SmartWatch): Boolean {
        Log.d(TAG, "🔗 connectToDevice() called: ${device.deviceName}")

        if (!BluetoothUtils.isEnabledBluetooth(context)) {
            Log.w(TAG, "❌ Bluetooth disabled")
            _connectionStatus.value = "Bluetooth disabled"
            return false
        }

        if (isConnectionInProgress) {
            Log.w(TAG, "⚠️ Connection already in progress")
            return false
        }

        if (_isConnected.value) {
            Log.i(TAG, "⚠️ Already connected, disconnecting first")
            disconnect()
            handler.postDelayed({
                connectToDevice(device)
            }, 1500)
            return true
        }

        stopScan()
        cancelAllCallbacks()

        isConnectionInProgress = true
        _isConnecting.value = true
        _connectionStatus.value = "Connecting to ${device.deviceName}..."
        pendingDevice = device

        Log.d(TAG, "📍 Device: ${device.deviceName}")
        Log.d(TAG, "📍 Address: ${device.deviceAddress}")
        Log.d(TAG, "📍 RSSI: ${device.rssi} dBm")

        // Save device info
        deviceManager.deviceName = device.deviceName
        deviceManager.deviceAddress = device.deviceAddress
        bleManager.reConnectMac = device.deviceAddress

        startConnectionTimeout(device)

        Log.d(TAG, "📡 Starting GATT connection...")
        bleManager.connectDirectly(device.deviceAddress)

        scheduleBindCommand()

        return true
    }

    private fun scheduleBindCommand() {
        cancelBindCommand()

        bindCommandRunnable = Runnable {
            Log.d(TAG, "⏰ Bind command timer triggered")

            if (bleManager.isConnected) {
                Log.d(TAG, "📤 Sending CMD_BIND_SUCCESS")
                try {
                    commandHandle.executeReqCmd(
                        SimpleKeyReq(Constants.CMD_BIND_SUCCESS),
                        null
                    )
                    Log.d(TAG, "✅ Bind command sent successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error sending bind command: ${e.message}", e)

                    // Retry once
                    handler.postDelayed({
                        if (bleManager.isConnected && isConnectionInProgress) {
                            Log.d(TAG, "🔄 Retrying bind command...")
                            try {
                                commandHandle.executeReqCmd(
                                    SimpleKeyReq(Constants.CMD_BIND_SUCCESS),
                                    null
                                )
                            } catch (e2: Exception) {
                                Log.e(TAG, "❌ Retry failed: ${e2.message}")
                            }
                        }
                    }, 2000)
                }
            } else {
                Log.w(TAG, "⚠️ GATT not connected yet, retrying...")
                handler.postDelayed({
                    if (isConnectionInProgress && !_isConnected.value) {
                        scheduleBindCommand()
                    }
                }, 1000)
            }
        }

        handler.postDelayed(bindCommandRunnable!!, BIND_COMMAND_DELAY)
        Log.d(TAG, "⏲️ Scheduled bind command in ${BIND_COMMAND_DELAY}ms")
    }

    private fun cancelBindCommand() {
        bindCommandRunnable?.let {
            handler.removeCallbacks(it)
            bindCommandRunnable = null
        }
    }

    private fun startConnectionTimeout(device: SmartWatch) {
        cancelConnectionTimeout()

        connectionTimeoutRunnable = Runnable {
            Log.e(TAG, "⏰ CONNECTION TIMEOUT for ${device.deviceName}")

            if (isConnectionInProgress && !_isConnected.value) {
                handler.post {
                    bleManager.disconnect()

                    isConnectionInProgress = false
                    _isConnecting.value = false
                    _isConnected.value = false
                    _connectedDevice.value = null
                    _connectionStatus.value = "Connection timeout"
                    pendingDevice = null

                    Log.e(TAG, "❌ Connection timed out after ${CONNECTION_TIMEOUT_MS / 1000}s")
                }
            }
        }

        handler.postDelayed(connectionTimeoutRunnable!!, CONNECTION_TIMEOUT_MS)
        Log.d(TAG, "⏲️ Connection timeout set: ${CONNECTION_TIMEOUT_MS / 1000}s")
    }

    private fun cancelConnectionTimeout() {
        connectionTimeoutRunnable?.let {
            handler.removeCallbacks(it)
            connectionTimeoutRunnable = null
        }
    }

    private fun cancelAllCallbacks() {
        handler.removeCallbacks(scanTimeoutRunnable ?: Runnable {})
        handler.removeCallbacks(connectionTimeoutRunnable ?: Runnable {})
        handler.removeCallbacks(bindCommandRunnable ?: Runnable {})
        scanTimeoutRunnable = null
        connectionTimeoutRunnable = null
        bindCommandRunnable = null
    }

    fun disconnect(): Boolean {
        Log.d(TAG, "🔌 Disconnecting...")

        if (!_isConnected.value && !_isConnecting.value) {
            Log.w(TAG, "⚠️ Not connected")
            return false
        }

        // ✅ Stop background service FIRST
        BackgroundConnectionService.stop(context)

        cancelAllCallbacks()
        isConnectionInProgress = false
        pendingDevice = null

        bleManager.setNeedConnect(false)
        bleManager.unBindDevice()
        bleManager.disconnect()

        deviceManager.deviceName = ""
        deviceManager.deviceAddress = ""

        onBluetoothDisconnected()

        handler.postDelayed({
            bleManager.setNeedConnect(true)
        }, 1000)

        Log.d(TAG, "✅ Disconnected")
        return true
    }

    /**
     * ✅ Start background service ONLY after connection is confirmed via EventBus
     */
    private fun startBackgroundService(device: SmartWatch) {
        try {
            Log.d(TAG, "🚀 Starting background service for ${device.deviceName}")
            BackgroundConnectionService.start(
                context,
                device.deviceName,
                device.deviceAddress
            )
            Log.d(TAG, "✅ Background service started")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to start background service: ${e.message}", e)
        }
    }

    private fun createScanCallback(): ScanWrapperCallback {
        return object : ScanWrapperCallback {
            override fun onStart() {
                Log.d(TAG, "▶️ Scan callback: onStart")
            }

            override fun onStop() {
                Log.d(TAG, "⏸️ Scan callback: onStop")
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e(TAG, "❌ Scan failed: code=$errorCode")
                handler.post {
                    _isScanning.value = false
                    _connectionStatus.value = "Scan failed: $errorCode"
                }
            }

            override fun onLeScan(device: BluetoothDevice?, rssi: Int, scanRecord: ByteArray?) {
                if (device != null && !device.name.isNullOrEmpty()) {
                    val smartWatch = SmartWatch(
                        deviceName = device.name,
                        deviceAddress = device.address,
                        rssi = rssi
                    )

                    handler.post {
                        val currentList = _discoveredDevices.value.toMutableList()

                        val existingIndex = currentList.indexOfFirst { d ->
                            d.deviceAddress == smartWatch.deviceAddress
                        }

                        if (existingIndex >= 0) {
                            currentList[existingIndex] = smartWatch
                        } else {
                            scanSize++
                            currentList.add(smartWatch)
                            Log.d(TAG, "📱 Found: ${device.name} | ${device.address} | ${rssi}dBm")

                            if (scanSize >= MAX_SCAN_SIZE) {
                                Log.d(TAG, "⚠️ Max devices reached, stopping scan")
                                stopScan()
                            }
                        }

                        currentList.sortByDescending { d -> d.rssi }
                        _discoveredDevices.value = currentList
                    }
                }
            }

            override fun onParsedData(device: BluetoothDevice?, scanRecord: com.oudmon.ble.base.scan.ScanRecord?) {}
            override fun onBatchScanResults(results: MutableList<android.bluetooth.le.ScanResult>?) {}
        }
    }

    private fun onBluetoothDisconnected() {
        Log.d(TAG, "📴 Bluetooth disconnected")

        isConnectionInProgress = false
        _isConnected.value = false
        _isConnecting.value = false
        _connectedDevice.value = null
        _connectionStatus.value = "Disconnected"
        pendingDevice = null
    }

    fun cleanup() {
        Log.d(TAG, "🧹 Cleaning up...")

        stopScan()
        cancelAllCallbacks()
        isConnectionInProgress = false
        pendingDevice = null

        try {
            context.unregisterReceiver(bluetoothReceiver)

            if (EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().unregister(this)
                Log.d(TAG, "📡 EventBus unregistered")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Cleanup error", e)
        }

        Log.d(TAG, "✅ Cleanup complete")
    }
}

