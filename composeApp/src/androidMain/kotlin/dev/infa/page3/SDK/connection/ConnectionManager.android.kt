package dev.infa.page3.SDK.connection


import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanRecord
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.bluetooth.DeviceManager
import com.oudmon.ble.base.communication.CommandHandle
import com.oudmon.ble.base.communication.Constants
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.req.SimpleKeyReq
import com.oudmon.ble.base.communication.rsp.BatteryRsp
import com.oudmon.ble.base.scan.BleScannerHelper
import com.oudmon.ble.base.scan.ScanWrapperCallback
import dev.infa.page3.SDK.ui.utils.PlatformContext
import dev.infa.page3.connection.BleConnectionService
import dev.infa.page3.connection.DeviceConnectionBroadcast

// androidMain/ConnectionManager.android.kt
actual class ConnectionManager {

    private val context: Context
        get() = PlatformContext.get() as Context

    private val bleManager by lazy { BleOperateManager.getInstance() }
    private val scanner by lazy { BleScannerHelper.getInstance() }
    private val commandHandle by lazy { CommandHandle.getInstance() }
    private val handler = Handler(Looper.getMainLooper())

    private var stateCallback: ((ConnectionState) -> Unit)? = null

    // Connection state receiver
    private val connectionReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when (intent?.action) {
                DeviceConnectionBroadcast.ACTION_CONNECTED -> {
                    stateCallback?.invoke(ConnectionState.CONNECTED)
                }
                DeviceConnectionBroadcast.ACTION_DISCONNECTED -> {
                    stateCallback?.invoke(ConnectionState.DISCONNECTED)
                }
            }
        }
    }

    actual fun initialize() {
        // Register broadcast receiver for service events
        val filter = IntentFilter().apply {
            addAction(DeviceConnectionBroadcast.ACTION_CONNECTED)
            addAction(DeviceConnectionBroadcast.ACTION_DISCONNECTED)
        }
        ContextCompat.registerReceiver(
            context,
            connectionReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    actual fun hasPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_SCAN
                    ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    actual fun startScan(
        onDeviceFound: (DeviceInfo) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!hasPermissions()) {
            onError("Missing Bluetooth permissions")
            return
        }

        scanner.reSetCallback()
        scanner.scanDevice(context, null, object : ScanWrapperCallback {
            override fun onStart() {}
            override fun onStop() {}
            override fun onScanFailed(errorCode: Int) {
                handler.post { onError("Scan failed: $errorCode") }
            }

            override fun onLeScan(device: BluetoothDevice?, rssi: Int, scanRecord: ByteArray?) {
                if (device?.name.isNullOrEmpty()) return
                handler.post {
                    onDeviceFound(DeviceInfo(device!!.name, device.address, rssi))
                }
            }

            override fun onParsedData(device: BluetoothDevice?, scanRecord: com.oudmon.ble.base.scan.ScanRecord?) {}
            override fun onBatchScanResults(results: MutableList<ScanResult>?) {}
        })

        handler.postDelayed({ stopScan() }, 15000)
    }

    actual fun stopScan() {
        scanner.stopScan(context)
    }

    actual fun connect(deviceName: String, deviceAddress: String) {
        if (!hasPermissions()) {
            stateCallback?.invoke(ConnectionState.ERROR)
            return
        }

        stateCallback?.invoke(ConnectionState.CONNECTING)

        // Start service - service handles all connection logic
        BleConnectionService.start(context, deviceName, deviceAddress)
    }

    actual fun disconnect() {
        BleConnectionService.stop(context)
        stateCallback?.invoke(ConnectionState.DISCONNECTED)
    }

    actual fun isConnected(): Boolean {
        return bleManager.isConnected
    }

    actual fun getBatteryLevel(onResult: (Int?) -> Unit) {
        if (!isConnected()) {
            handler.post { onResult(null) }
            return
        }

        commandHandle.executeReqCmd(
            SimpleKeyReq(Constants.CMD_GET_DEVICE_ELECTRICITY_VALUE),
            object : ICommandResponse<BatteryRsp> {
                override fun onDataResponse(resultEntity: BatteryRsp) {
                    handler.post { onResult(resultEntity.batteryValue) }
                }
            }
        )
    }

    actual fun observeConnectionState(onStateChange: (ConnectionState) -> Unit) {
        stateCallback = onStateChange
    }

    fun cleanup() {
        try {
            context.unregisterReceiver(connectionReceiver)
        } catch (e: Exception) {
        }
    }
}

object ConnectionPlatform {
    var applicationContext: Context? = null
        get() = field ?: AndroidConnectionPlatformState.applicationContext
    
    fun initialize(context: Context) {
        AndroidConnectionPlatformState.applicationContext = context.applicationContext
    }
}

private object AndroidConnectionPlatformState {
    @Volatile
    var applicationContext: Context? = null
}