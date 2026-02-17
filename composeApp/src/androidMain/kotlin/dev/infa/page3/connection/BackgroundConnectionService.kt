package dev.infa.page3.connection

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
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
import com.oudmon.ble.base.communication.req.SetTimeReq
import com.oudmon.ble.base.communication.req.SimpleKeyReq
import com.oudmon.ble.base.communication.rsp.BaseRspCmd
import com.oudmon.ble.base.scan.BleScannerHelper
import com.oudmon.ble.base.scan.OnTheScanResult
import dev.infa.page3.MainActivity
import dev.infa.page3.SDK.repository.ConnectionRepository

// androidMain/BleConnectionService.kt
class BleConnectionService : Service() {

    companion object {
        const val CHANNEL_ID = "BleConnectionChannel"
        const val NOTIFICATION_ID = 1001

        private const val ACTION_START = "ACTION_START"
        private const val ACTION_STOP = "ACTION_STOP"

        private const val KEY_DEVICE_NAME = "device_name"
        private const val KEY_DEVICE_ADDRESS = "device_address"

        fun start(context: Context, deviceName: String, deviceAddress: String) {
            val intent = Intent(context, BleConnectionService::class.java).apply {
                action = ACTION_START
                putExtra(KEY_DEVICE_NAME, deviceName)
                putExtra(KEY_DEVICE_ADDRESS, deviceAddress)
            }

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: SecurityException) {
                Log.e("BleConnectionService", "Failed to start: ${e.message}")
                throw e
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, BleConnectionService::class.java))
        }

        @Volatile
        var healthOpsPaused: Boolean = false
    }

    private val bleManager by lazy { BleOperateManager.getInstance() }
    private val deviceManager by lazy { DeviceManager.getInstance() }
    private val commandHandle by lazy { CommandHandle.getInstance() }
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var repository: ConnectionRepository
    private var reconnectRunnable: Runnable? = null
    private var healthCheckRunnable: Runnable? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("BleConnectionService", "onCreate")

        // Repository for persistence
        repository = ConnectionRepository()

        createNotificationChannel()
        setupConnectionListener()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("BleConnectionService", "onStartCommand action=${intent?.action}")

        if (!hasAllRequiredPermissions()) {
            Log.e("BleConnectionService", "Missing permissions - stopping")
            stopSelf()
            return START_NOT_STICKY
        }

        when (intent?.action) {
            ACTION_START -> {
                val name = intent.getStringExtra(KEY_DEVICE_NAME) ?: "Device"
                val address = intent.getStringExtra(KEY_DEVICE_ADDRESS)

                if (address != null) {
                    startForegroundAndConnect(name, address)
                } else {
                    // Try saved device
                    val saved = repository.getSavedDeviceInfo()
                    if (saved != null) {
                        startForegroundAndConnect(saved.name, saved.address)
                    } else {
                        Log.w("BleConnectionService", "No device to connect")
                        stopSelf()
                    }
                }
            }
            ACTION_STOP -> disconnectAndStop()
            else -> {
                // Restart scenario
                val saved = repository.getSavedDeviceInfo()
                if (saved != null && repository.isAutoConnectEnabled()) {
                    startForegroundAndConnect(saved.name, saved.address)
                } else {
                    stopSelf()
                }
            }
        }

        return START_STICKY
    }

    private fun hasAllRequiredPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val hasConnect = checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) ==
                    PackageManager.PERMISSION_GRANTED
            val hasScan = checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) ==
                    PackageManager.PERMISSION_GRANTED

            if (!hasConnect || !hasScan) {
                Log.e("BleConnectionService", "Missing BT permissions")
                return false
            }
        }
        return true
    }

    private fun startForegroundAndConnect(deviceName: String, deviceAddress: String) {
        try {
            val notification = createNotification(deviceName, "Connecting...")
            startForeground(NOTIFICATION_ID, notification)

            repository.saveLastConnectedDevice(deviceAddress, deviceName)
            connectToDevice(deviceAddress)
            startHealthChecks()

        } catch (e: SecurityException) {
            Log.e("BleConnectionService", "startForeground failed: ${e.message}")
            stopSelf()
        }
    }

    private fun setupConnectionListener() {
        bleManager.addNotifyListener(0x01, object : ICommandResponse<BaseRspCmd> {
            override fun onDataResponse(resultEntity: BaseRspCmd?) {
                if (resultEntity?.status == BaseRspCmd.RESULT_OK) {
                    handler.post { onConnectionEstablished() }
                }
            }
        })
    }

    private fun connectToDevice(address: String) {
        cancelReconnect()

        val savedDevice = repository.getSavedDeviceInfo()
        deviceManager.deviceAddress = address
        deviceManager.deviceName = savedDevice?.name ?: ""
        bleManager.reConnectMac = address
        bleManager.setNeedConnect(true)

        // Connection attempts
        try {
            bleManager.connectDirectly(address)
        } catch (ex: Exception) {
            Log.w("BleConnectionService", "connectDirectly failed: ${ex.message}")
        }

        handler.postDelayed({
            if (!bleManager.isConnected) {
                try {
                    bleManager.connectWithScan(address)
                } catch (ex: Exception) {
                    Log.w("BleConnectionService", "connectWithScan failed")
                }
            }
        }, 2500)

        handler.postDelayed({
            if (!bleManager.isConnected) {
                try {
                    BleScannerHelper.getInstance().scanTheDevice(
                        this, address,
                        object : OnTheScanResult {
                            override fun onResult(device: BluetoothDevice?) {}
                            override fun onScanFailed(errorCode: Int) {}
                        }
                    )
                } catch (e: Exception) {
                    Log.w("BleConnectionService", "scanTheDevice failed")
                }
            }
        }, 5000)

        scheduleReconnectCheck()
    }

    private fun onConnectionEstablished() {
        Log.d("BleConnectionService", "Connection established")
        repository.markConnectionSuccess()

        val deviceName = repository.getSavedDeviceInfo()?.name ?: "Device"
        updateNotification(deviceName, "Connected")

        DeviceConnectionBroadcast.sendConnected(this)
    }

    private fun scheduleReconnectCheck() {
        cancelReconnect()
        reconnectRunnable = Runnable {
            if (!bleManager.isConnected) {
                val address = repository.getSavedDeviceInfo()?.address
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
                val name = repository.getSavedDeviceInfo()?.name ?: "Device"

                updateNotification(name, if (connected) "Connected" else "Connecting...")

                if (connected) {
                    DeviceConnectionBroadcast.sendConnected(this@BleConnectionService)

                    if (!healthOpsPaused) {
                        try {
                            commandHandle.executeReqCmd(
                                SimpleKeyReq(Constants.CMD_GET_DEVICE_ELECTRICITY_VALUE),
                                null
                            )
                        } catch (e: Exception) {
                            Log.w("BleConnectionService", "Health check failed")
                        }
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
        Log.d("BleConnectionService", "Disconnecting and stopping")
        cancelReconnect()
        stopHealthChecks()

        bleManager.setNeedConnect(false)
        bleManager.unBindDevice()
        bleManager.disconnect()

        DeviceConnectionBroadcast.sendDisconnected(this)

        stopForeground(true)
        stopSelf()
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
            getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(deviceName: String, status: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
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
        try {
            val notification = createNotification(deviceName, status)
            getSystemService(NotificationManager::class.java)
                ?.notify(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.w("BleConnectionService", "Failed to update notification")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d("BleConnectionService", "onDestroy")
        cancelReconnect()
        stopHealthChecks()
    }
}

object DeviceConnectionBroadcast {
    const val ACTION_CONNECTED = "DEVICE_CONNECTED"
    const val ACTION_DISCONNECTED = "DEVICE_DISCONNECTED"

    fun sendConnected(context: Context) {
        context.sendBroadcast(Intent(ACTION_CONNECTED))
    }

    fun sendDisconnected(context: Context) {
        context.sendBroadcast(Intent(ACTION_DISCONNECTED))
    }
}
