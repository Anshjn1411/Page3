package dev.infa.page3.viewmodels

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.bluetooth.DeviceManager
import com.oudmon.ble.base.communication.CommandHandle
import com.oudmon.ble.base.communication.Constants
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.req.DeviceSupportReq
import com.oudmon.ble.base.communication.req.SetTimeReq
import com.oudmon.ble.base.communication.req.SimpleKeyReq
import com.oudmon.ble.base.communication.rsp.BaseRspCmd
import com.oudmon.ble.base.communication.rsp.BatteryRsp
import com.oudmon.ble.base.communication.rsp.DeviceSupportFunctionRsp
import com.oudmon.ble.base.communication.rsp.SetTimeRsp
import com.oudmon.ble.base.scan.BleScannerHelper
import com.oudmon.ble.base.scan.ScanWrapperCallback
import com.oudmon.ble.base.util.BluetoothUtils
import dev.infa.page3.platform.ConnectionServiceController
import dev.infa.page3.connection.DeviceConnectionBroadcast
import dev.infa.page3.models.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow



data class DeviceUiState(
    val isBluetoothOn: Boolean = false,          // Indicates if Bluetooth is turned on
    val isScanning: Boolean = false,             // Indicates if device scanning is ongoing
    val isConnecting: Boolean = false,           // Indicates if a device is in process of connecting
    val isConnected: Boolean = false,            // Indicates if device is successfully connected
    val isReconnecting: Boolean = false,         // True if the app is trying to reconnect after disconnect
    val hasError: Boolean = false,               // True if any error occurs
    val errorMessage: String? = null,            // Description of the error
    val connectionStatus: String = "Disconnected", // Human-readable connection status
    val batteryLevel: Int? = null,               // Current device battery
    val devices: List<SmartWatch> = emptyList(),  // Scanned BLE devices
    val connectedDevice: SmartWatch? = null,      // Current connected device
    val deviceCapabilities: DeviceCapabilities? = null // Device-supported features
)

class ConnectionViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(DeviceUiState())
    val uiState: StateFlow<DeviceUiState> = _uiState.asStateFlow()

    private val context: Context = application.applicationContext
    private val handler = Handler(Looper.getMainLooper())

    private val bleManager = BleOperateManager.getInstance()
    private val scanner = BleScannerHelper.getInstance()
    private val deviceManager = DeviceManager.getInstance()
    private val commandHandle = CommandHandle.getInstance()

    private var scanTimeoutRunnable: Runnable? = null
    private lateinit var prefs: SharedPreferences
    private var deviceCapabilities: DeviceCapabilities? = null


    private val connectionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                DeviceConnectionBroadcast.ACTION_CONNECTED -> {
                    loadConnectedDevice()
                }
                DeviceConnectionBroadcast.ACTION_DISCONNECTED -> {
                    _uiState.value = _uiState.value.copy(
                        isConnected = false,
                        connectedDevice = null,
                        connectionStatus = "Disconnected",
                        batteryLevel = null,
                        deviceCapabilities = null
                    )
                }
                DeviceConnectionBroadcast.ACTION_CAPABILITIES -> {
                    val json = intent.getStringExtra("capabilities")
                    json?.let {
                        val caps = DeviceCapabilities.fromJson(it)
                        _uiState.value = _uiState.value.copy(deviceCapabilities = caps)
                    }
                }
            }
        }
    }

    init {
        prefs = context.getSharedPreferences("BleConnection", Context.MODE_PRIVATE)
        registerConnectionReceiver()
        registerBluetoothReceiver()
        loadConnectedDevice()
    }

    private fun registerConnectionReceiver() {
        val filter = IntentFilter().apply {
            addAction(DeviceConnectionBroadcast.ACTION_CONNECTED)
            addAction(DeviceConnectionBroadcast.ACTION_DISCONNECTED)
            addAction(DeviceConnectionBroadcast.ACTION_CAPABILITIES)
        }
        ContextCompat.registerReceiver(
            context,
            connectionReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun registerBluetoothReceiver() {
        val filter = IntentFilter(android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED)
        context.registerReceiver(bluetoothReceiver, filter)
    }

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val state = intent?.getIntExtra(android.bluetooth.BluetoothAdapter.EXTRA_STATE, -1)
            when (state) {
                android.bluetooth.BluetoothAdapter.STATE_OFF -> {
                    bleManager.setBluetoothTurnOff(false)
                    _uiState.value = _uiState.value.copy(
                        isBluetoothOn = false,
                        connectionStatus = "Bluetooth Off"
                    )
                }
                android.bluetooth.BluetoothAdapter.STATE_ON -> {
                    bleManager.setBluetoothTurnOff(true)
                    _uiState.value = _uiState.value.copy(
                        isBluetoothOn = true,
                        connectionStatus = "Bluetooth On"
                    )
                }
            }
        }
    }


    private fun loadConnectedDevice() {
        val name = prefs.getString("device_name", null)
        val address = prefs.getString("device_address", null)
        val isConnected = prefs.getBoolean("is_connected", false)
        val capsJson = prefs.getString("capabilities", null)

        if (name != null && address != null) {
            _uiState.value = _uiState.value.copy(
                isConnected = isConnected && bleManager.isConnected,
                connectedDevice = SmartWatch(name, address, 0),
                connectionStatus = if (isConnected && bleManager.isConnected) "Connected" else "Connecting...",
                deviceCapabilities = capsJson?.let { DeviceCapabilities.fromJson(it) }
            )
        }
    }

    fun startScan() {
        if (!BluetoothUtils.isEnabledBluetooth(context) || _uiState.value.isScanning) return

        _uiState.value = _uiState.value.copy(
            isBluetoothOn = true,
            isScanning = true,
            hasError = false,
            errorMessage = null,
            connectionStatus = "Scanning..."
        )

        viewModelScope.launch {
            scanner.reSetCallback()
            scanner.scanDevice(context, null, createScanCallback())

            scanTimeoutRunnable = Runnable { stopScan() }
            handler.postDelayed(scanTimeoutRunnable!!, 15000L)
        }
    }


    fun stopScan() {
        if (!_uiState.value.isScanning) return

        scanner.stopScan(context)
        scanTimeoutRunnable?.let { handler.removeCallbacks(it) }

        val count = _uiState.value.devices.size
        _uiState.value = _uiState.value.copy(
            isScanning = false
        )
    }

    private fun createScanCallback() = object : ScanWrapperCallback {
        override fun onStart() {}
        override fun onStop() {}
        override fun onScanFailed(errorCode: Int) {
            handler.post {
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    errorMessage = "Scan failed: $errorCode"
                )
            }
        }

        override fun onLeScan(device: BluetoothDevice?, rssi: Int, scanRecord: ByteArray?) {
            if (device?.name.isNullOrEmpty()) return

            val bleDevice = SmartWatch(device!!.name, device.address, rssi)
            handler.post {
                val list = _uiState.value.devices.toMutableList()
                val index = list.indexOfFirst { it.deviceAddress == bleDevice.deviceAddress }

                if (index >= 0) list[index] = bleDevice
                else list.add(bleDevice)

                list.sortByDescending { it.rssi }
                _uiState.value = _uiState.value.copy(devices = list)
            }
        }

        override fun onParsedData(device: BluetoothDevice?, scanRecord: com.oudmon.ble.base.scan.ScanRecord?) {}
        override fun onBatchScanResults(results: MutableList<android.bluetooth.le.ScanResult>?) {}
    }

    fun connectToDevice(device: SmartWatch) {
        if (!BluetoothUtils.isEnabledBluetooth(context)) {
            _uiState.value = _uiState.value.copy(
                hasError = true,
                errorMessage = "Bluetooth is turned off",
                isBluetoothOn = false
            )
            return
        }

        stopScan()

        _uiState.value = _uiState.value.copy(
            connectedDevice = device,
            isConnecting = true,
            isConnected = false,
            connectionStatus = "Connecting...",
            hasError = false
        )

        ConnectionServiceController.start(device.deviceName, device.deviceAddress)

    }


    fun disconnect() {
        ConnectionServiceController.stop()
        _uiState.value = _uiState.value.copy(
            isConnected = false,
            isConnecting = false,
            isReconnecting = false,
            connectedDevice = null,
            connectionStatus = "Disconnected",
            batteryLevel = null,
            deviceCapabilities = null
        )
    }


    fun getBatteryLevel() {
        if (!_uiState.value.isConnected || !bleManager.isConnected) {
            _uiState.value = _uiState.value.copy(errorMessage = "Device not connected")
            return
        }

        viewModelScope.launch {
            commandHandle.executeReqCmd(
                SimpleKeyReq(Constants.CMD_GET_DEVICE_ELECTRICITY_VALUE),
                object : ICommandResponse<BatteryRsp> {
                    override fun onDataResponse(resultEntity: BatteryRsp) {
                        if (resultEntity.status == BaseRspCmd.RESULT_OK) {
                            handler.post {
                                _uiState.value = _uiState.value.copy(
                                    batteryLevel = resultEntity.batteryValue,
                                    errorMessage = null
                                )
                            }
                        }
                    }
                }
            )
        }
    }
    private fun fetchDeviceCapabilities() {
        // Get time sync response for capabilities
        commandHandle.executeReqCmd(SetTimeReq(), object : ICommandResponse<SetTimeRsp> {
            override fun onDataResponse(resultEntity: SetTimeRsp?) {
                resultEntity?.let {
                    deviceCapabilities = DeviceCapabilities(
                        supportTemperature = it.mSupportTemperature,
                        supportPlate = it.mSupportPlate,
                        supportMenstruation = it.mSupportMenstruation,
                        supportCustomWallpaper = it.mSupportCustomWallpaper,
                        supportBloodOxygen = it.mSupportBloodOxygen,
                        supportBloodPressure = it.mSupportBloodPressure,
                        supportFeature = it.mSupportFeature,
                        supportOneKeyCheck = it.mSupportOneKeyCheck,
                        supportWeather = it.mSupportWeather,
                        newSleepProtocol = it.mNewSleepProtocol,
                        maxWatchFace = it.mMaxWatchFace,
                        supportHrv = it.mSupportHrv
                    )
                }
            }
        })

        // Get device support functions
        commandHandle.executeReqCmd(
            DeviceSupportReq.getReadInstance(),
            object : ICommandResponse<DeviceSupportFunctionRsp> {
                override fun onDataResponse(resultEntity: DeviceSupportFunctionRsp?) {
                    resultEntity?.let {
                        val caps = deviceCapabilities ?: DeviceCapabilities()
                        deviceCapabilities = caps.copy(
                            supportTouch = it.supportTouch,
                            supportMoslin = it.supportMoslin,
                            supportAPPRevision = it.supportAPPRevision,
                            supportBlePair = it.supportBlePair,
                            supportGesture = it.supportGesture,
                            supportRingMusic = it.supportRingMusic,
                            supportRingVideo = it.supportRingVideo,
                            supportRingEbook = it.supportRingEbook,
                            supportRingCamera = it.supportRingCamera,
                            supportRingPhoneCall = it.supportRingPhoneCall,
                            supportRingGame = it.supportRingGame
                        )
                    }
                }
            }
        )
    }

    private fun onConnected(device: SmartWatch) {
        _uiState.value = _uiState.value.copy(
            isConnecting = false,
            isConnected = true,
            connectionStatus = "Connected",
            hasError = false
        )
    }


    override fun onCleared() {
        super.onCleared()
        stopScan()
        scanTimeoutRunnable?.let { handler.removeCallbacks(it) }
        try {
            context.unregisterReceiver(bluetoothReceiver)
            context.unregisterReceiver(connectionReceiver)
        } catch (_: Exception) {}
    }
}
