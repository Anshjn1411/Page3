package dev.infa.page3.connection

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
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
import dev.infa.page3.DeviceSDKActivity
import dev.infa.page3.MainActivity
import dev.infa.page3.models.BluetoothEvent
import dev.infa.page3.models.DeviceCapabilities
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * Simple and robust connection manager for BLE devices
 * Ensures device stays connected with automatic reconnection
 */
class BleConnectionService : Service() {

    companion object {
        const val CHANNEL_ID = "BleConnectionChannel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_DISCONNECT = "ACTION_DISCONNECT"

        private const val PREFS_NAME = "BleConnection"
        private const val KEY_DEVICE_NAME = "device_name"
        private const val KEY_DEVICE_ADDRESS = "device_address"
        private const val KEY_IS_CONNECTED = "is_connected"

        fun start(context: Context, deviceName: String, deviceAddress: String) {
            val intent = Intent(context, BleConnectionService::class.java).apply {
                action = ACTION_START
                putExtra(KEY_DEVICE_NAME, deviceName)
                putExtra(KEY_DEVICE_ADDRESS, deviceAddress)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, BleConnectionService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        @Volatile
        private var healthOpsPaused: Boolean = false

        fun setHealthOpsPaused(paused: Boolean) {
            healthOpsPaused = paused
        }

        fun suspendHealthOps(durationMs: Long) {
            healthOpsPaused = true
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                healthOpsPaused = false
            }, durationMs)
        }
    }

    private val bleManager by lazy { BleOperateManager.getInstance() }
    private val deviceManager by lazy { DeviceManager.getInstance() }
    private val commandHandle by lazy { CommandHandle.getInstance() }
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var prefs: SharedPreferences
    private var reconnectRunnable: Runnable? = null
    private var healthCheckRunnable: Runnable? = null
    private var deviceCapabilities: DeviceCapabilities? = null
    private var bluetoothStateReceiver: BroadcastReceiver? = null

    override fun onCreate() {
        super.onCreate()
        // Initialize SDK here to guarantee single initialization under Service lifecycle
        try {
            val app = applicationContext as? android.app.Application
            BleOperateManager.getInstance(app).apply {
                init()
                setNeedConnect(true)
                setBluetoothTurnOff(true)
            }
            // Touch singletons to ensure they are ready
            DeviceManager.getInstance()
            CommandHandle.getInstance()
        } catch (_: Exception) {}
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        createNotificationChannel()
        setupConnectionListener()
        registerBluetoothStateReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("BleConnectionService", "onStartCommand action=${intent?.action}")
        when (intent?.action) {
            ACTION_START -> {
                val name = intent.getStringExtra(KEY_DEVICE_NAME)
                val address = intent.getStringExtra(KEY_DEVICE_ADDRESS)
                if (!address.isNullOrEmpty()) {
                    startForegroundService(name ?: "Device", address)
                } else {
                    // No address provided, try resume from prefs
                    val savedAddress = prefs.getString(KEY_DEVICE_ADDRESS, null)
                    val savedName = prefs.getString(KEY_DEVICE_NAME, "Device") ?: "Device"
                    if (savedAddress != null) startForegroundService(savedName, savedAddress)
                    else startEmptyForeground()
                }
            }
            ACTION_STOP, ACTION_DISCONNECT -> {
                disconnectAndStop()
            }
            else -> {
                // Service restarted, try to reconnect to saved device
                val address = prefs.getString(KEY_DEVICE_ADDRESS, null)
                val name = prefs.getString(KEY_DEVICE_NAME, null)
                if (address != null && name != null) {
                    startForegroundService(name, address)
                } else {
                    startEmptyForeground()
                }
            }
        }
        return START_STICKY
    }

    private fun startForegroundService(deviceName: String, deviceAddress: String) {
        Log.d("BleConnectionService", "startForegroundService name=$deviceName address=$deviceAddress")
        if (deviceAddress.isNotEmpty()) {
            saveDeviceInfo(deviceName, deviceAddress)
        }

        val notification = createNotification(deviceName.ifEmpty { "Device" }, "Connecting...")
        startForeground(NOTIFICATION_ID, notification)
        if (deviceAddress.isNotEmpty()) {
            connectToDevice(deviceAddress)
        }
        startHealthChecks()
    }

    private fun startEmptyForeground() {
        Log.d("BleConnectionService", "startEmptyForeground - no saved device")
        val notification = createNotification("Device", "Waiting for device...")
        startForeground(NOTIFICATION_ID, notification)
        startHealthChecks()
    }

    private fun setupConnectionListener() {
        Log.d("BleConnectionService", "setupConnectionListener")
        bleManager.addNotifyListener(0x01, object : ICommandResponse<BaseRspCmd> {
            override fun onDataResponse(resultEntity: BaseRspCmd?) {
                if (resultEntity?.status == BaseRspCmd.RESULT_OK) {
                    handler.post {
                        onConnectionEstablished()
                    }
                }
            }
        })
    }

    private fun connectToDevice(address: String) {
        cancelReconnect()

        Log.d("BleConnectionService", "connectToDevice address=$address")
        deviceManager.deviceAddress = address
        deviceManager.deviceName = prefs.getString(KEY_DEVICE_NAME, "") ?: ""
        bleManager.reConnectMac = address
        bleManager.setNeedConnect(true)

        // First attempt: direct connection
        bleManager.connectDirectly(address)

        // Probe and fallback to connect-with-scan if direct fails quickly
        handler.postDelayed({
            if (!bleManager.isConnected) {
                Log.d("BleConnectionService", "direct connect not yet established; trying connectWithScan")
                bleManager.connectWithScan(address)
            }
        }, 2500)

        // If still not connected after scan, trigger a device-specific scan to wake
        handler.postDelayed({
            if (!bleManager.isConnected) {
                Log.d("BleConnectionService", "still not connected; triggering scanTheDevice wake")
                try {
                    com.oudmon.ble.base.scan.BleScannerHelper.getInstance().scanTheDevice(
                        this,
                        address,
                        object : com.oudmon.ble.base.scan.OnTheScanResult {
                            override fun onResult(device: android.bluetooth.BluetoothDevice?) {
                                Log.d("BleConnectionService", "scanTheDevice found device: ${device?.address}")
                            }

                            override fun onScanFailed(errorCode: Int) {
                                Log.d("BleConnectionService", "scanTheDevice failed: $errorCode")
                            }
                        }
                    )
                } catch (_: Exception) {}
            }
        }, 5000)

        // Light probe to keep requesting data path establishment
        handler.postDelayed({
            if (!bleManager.isConnected) {
                Log.d("BleConnectionService", "probing with SetTimeReq")
                commandHandle.executeReqCmd(SetTimeReq(), null)
            }
        }, 3000)

        scheduleReconnectCheck()
    }

    private fun onConnectionEstablished() {
        Log.d("BleConnectionService", "onConnectionEstablished")
        prefs.edit().putBoolean(KEY_IS_CONNECTED, true).apply()

        val deviceName = prefs.getString(KEY_DEVICE_NAME, "Device") ?: "Device"
        updateNotification(deviceName, "Connected")

        fetchDeviceCapabilities()

        DeviceConnectionBroadcast.sendConnected(this)
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
                    saveCapabilities(deviceCapabilities!!)
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
                        saveCapabilities(deviceCapabilities!!)
                    }
                }
            }
        )
    }

    private fun scheduleReconnectCheck() {
        cancelReconnect()
        reconnectRunnable = Runnable {
            if (!bleManager.isConnected) {
                Log.d("BleConnectionService", "reconnect check: not connected, retrying")
                val address = prefs.getString(KEY_DEVICE_ADDRESS, null)
                if (address != null) {
                    connectToDevice(address)
                }
            } else {
                scheduleReconnectCheck()
            }
        }
        handler.postDelayed(reconnectRunnable!!, 10000)
    }

    private fun startHealthChecks() {
        stopHealthChecks()
        healthCheckRunnable = object : Runnable {
            override fun run() {
                val connected = bleManager.isConnected
                val name = prefs.getString(KEY_DEVICE_NAME, "Device") ?: "Device"
                prefs.edit().putBoolean(KEY_IS_CONNECTED, connected).apply()
                updateNotification(name, if (connected) "Connected" else "Connecting...")
                if (connected) {
                    DeviceConnectionBroadcast.sendConnected(this@BleConnectionService)
                    if (!healthOpsPaused) {
                        // Periodically fetch battery to both update UI and keep link active
                        try {
                            commandHandle.executeReqCmd(
                                SimpleKeyReq(Constants.CMD_GET_DEVICE_ELECTRICITY_VALUE),
                                null
                            )
                        } catch (_: Exception) {}
                    }
                }
                handler.postDelayed(this, 5000)
            }
        }
        handler.post(healthCheckRunnable!!)
    }

    private fun stopHealthChecks() {
        healthCheckRunnable?.let { handler.removeCallbacks(it) }
        healthCheckRunnable = null
    }

    private fun cancelReconnect() {
        reconnectRunnable?.let { handler.removeCallbacks(it) }
        reconnectRunnable = null
    }

    private fun disconnectAndStop() {
        cancelReconnect()
        stopHealthChecks()

        bleManager.setNeedConnect(false)
        bleManager.unBindDevice()
        bleManager.disconnect()

        clearDeviceInfo()
        DeviceConnectionBroadcast.sendDisconnected(this)

        stopForeground(true)
        stopSelf()
    }

    private fun saveDeviceInfo(name: String, address: String) {
        prefs.edit().apply {
            putString(KEY_DEVICE_NAME, name)
            putString(KEY_DEVICE_ADDRESS, address)
            putBoolean(KEY_IS_CONNECTED, false)
            apply()
        }
    }

    private fun clearDeviceInfo() {
        prefs.edit().clear().apply()
        deviceCapabilities = null
    }

    private fun saveCapabilities(caps: DeviceCapabilities) {
        val json = caps.toJson()
        prefs.edit().putString("capabilities", json).apply()
        DeviceConnectionBroadcast.sendCapabilities(this, caps)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "BLE Connection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Maintains Bluetooth connection"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(deviceName: String, status: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Connected to $deviceName")
            .setContentText(status)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(deviceName: String, status: String) {
        val notification = createNotification(deviceName, status)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        cancelReconnect()
        stopHealthChecks()
        unregisterBluetoothStateReceiver()
    }

    private fun registerBluetoothStateReceiver() {
        if (bluetoothStateReceiver != null) return
        bluetoothStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED) {
                    val state = intent.getIntExtra(android.bluetooth.BluetoothAdapter.EXTRA_STATE, -1)
                    when (state) {
                        android.bluetooth.BluetoothAdapter.STATE_OFF -> {
                            Log.d("BleConnectionService", "Bluetooth OFF; notifying SDK")
                            bleManager.setBluetoothTurnOff(false)
                        }
                        android.bluetooth.BluetoothAdapter.STATE_ON -> {
                            Log.d("BleConnectionService", "Bluetooth ON; enabling SDK monitor")
                            bleManager.setBluetoothTurnOff(true)
                            // Attempt reconnect if we have a saved device
                            val address = prefs.getString(KEY_DEVICE_ADDRESS, null)
                            val name = prefs.getString(KEY_DEVICE_NAME, "Device") ?: "Device"
                            if (address != null) startForegroundService(name, address)
                        }
                    }
                }
            }
        }
        registerReceiver(bluetoothStateReceiver, IntentFilter(android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED))
    }

    private fun unregisterBluetoothStateReceiver() {
        try {
            bluetoothStateReceiver?.let { unregisterReceiver(it) }
        } catch (_: Exception) {}
        bluetoothStateReceiver = null
    }
}

// Helper object for broadcasting connection state
object DeviceConnectionBroadcast {
    const val ACTION_CONNECTED = "DEVICE_CONNECTED"
    const val ACTION_DISCONNECTED = "DEVICE_DISCONNECTED"
    const val ACTION_CAPABILITIES = "DEVICE_CAPABILITIES"

    fun sendConnected(context: Context) {
        context.sendBroadcast(Intent(ACTION_CONNECTED))
    }

    fun sendDisconnected(context: Context) {
        context.sendBroadcast(Intent(ACTION_DISCONNECTED))
    }

    fun sendCapabilities(context: Context, capabilities: DeviceCapabilities) {
        val intent = Intent(ACTION_CAPABILITIES).apply {
            putExtra("capabilities", capabilities.toJson())
        }
        context.sendBroadcast(intent)
    }
}




