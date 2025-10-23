package dev.infa.page3.connection

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat.START_STICKY
import androidx.core.app.ServiceCompat.startForeground
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.communication.CommandHandle
import com.oudmon.ble.base.communication.Constants
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.req.SimpleKeyReq
import com.oudmon.ble.base.communication.rsp.BatteryRsp
import dev.infa.page3.DeviceSDKActivity
import dev.infa.page3.models.BluetoothEvent
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Simple and robust connection manager for BLE devices
 * Ensures device stays connected with automatic reconnection
 */
class BackgroundConnectionService : Service() {
    companion object {
        private const val TAG = "BG_Service"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "device_connection"

        // ✅ CRITICAL: Wait 60 seconds before first check!
        private const val INITIAL_DELAY_MS = 60000L // 60 seconds

        // ✅ Check every 3 minutes (not too often)
        private const val RECONNECT_CHECK_DELAY_MS = 180000L // 3 minutes

        fun start(context: Context, deviceName: String, deviceAddress: String) {
            val intent = Intent(context, BackgroundConnectionService::class.java).apply {
                putExtra("device_name", deviceName)
                putExtra("device_address", deviceAddress)
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, BackgroundConnectionService::class.java))
        }
    }

    private var deviceName: String = ""
    private var deviceAddress: String = ""
    private var bleOperateManager: BleOperateManager? = null
    private var commandHandle: CommandHandle? = null
    private val handler = Handler(Looper.getMainLooper())
    private var reconnectRunnable: Runnable? = null
    private var keepAliveRunnable: Runnable? = null
    private var isReconnecting = false
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 3
    private var lastConnectionCheck = 0L
    private var serviceStartTime = 0L

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "✅ Service created")
        createNotificationChannel()
        bleOperateManager = BleOperateManager.getInstance()
        commandHandle = CommandHandle.getInstance()
        serviceStartTime = System.currentTimeMillis()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        deviceName = intent?.getStringExtra("device_name") ?: ""
        deviceAddress = intent?.getStringExtra("device_address") ?: ""

        Log.d(TAG, "🚀 Service started for $deviceName @ $deviceAddress")

        // Start foreground immediately
        startForeground(NOTIFICATION_ID, buildNotification("Connected to $deviceName"))

        // Register EventBus
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
            Log.d(TAG, "📡 EventBus registered")
        }

        // Setup BLE manager
        bleOperateManager?.apply {
            setBluetoothTurnOff(true)
            setNeedConnect(true)
        }

        // ✅ Start monitoring with LONG delay
        startMonitoring()

        // ✅ Start keep-alive pings
        startKeepAlive()

        return START_STICKY
    }

    private fun startMonitoring() {
        reconnectRunnable?.let { handler.removeCallbacks(it) }

        reconnectRunnable = Runnable {
            checkConnectionAndReconnect()
            // Schedule next check
            handler.postDelayed(reconnectRunnable!!, RECONNECT_CHECK_DELAY_MS)
        }

        // ✅ CRITICAL: Wait 60 seconds before first check!
        handler.postDelayed(reconnectRunnable!!, INITIAL_DELAY_MS)
        Log.d(TAG, "📊 Monitoring started (60s initial delay, then 3min intervals)")
    }

    /**
     * ✅ Send keep-alive pings every 5 minutes to maintain connection
     */
    private fun startKeepAlive() {
        keepAliveRunnable?.let { handler.removeCallbacks(it) }

        keepAliveRunnable = Runnable {
            sendKeepAlive()
            // Schedule next keep-alive
            handler.postDelayed(keepAliveRunnable!!, 300000L) // Every 5 minutes
        }

        // Start after 90 seconds (after service is stable)
        handler.postDelayed(keepAliveRunnable!!, 90000L)
        Log.d(TAG, "💚 Keep-alive started (90s initial, then 5min intervals)")
    }

    private fun checkConnectionAndReconnect() {
        if (deviceAddress.isEmpty()) {
            Log.w(TAG, "❌ No device address, stopping service")
            stopSelf()
            return
        }

        // ✅ Don't check too soon after service start
        val timeSinceStart = System.currentTimeMillis() - serviceStartTime
        if (timeSinceStart < 45000) { // 45 seconds
            Log.d(TAG, "⏳ Service just started (${timeSinceStart}ms ago), skipping check")
            return
        }

        // ✅ Don't interrupt ongoing reconnection
        if (isReconnecting) {
            val timeSinceLastCheck = System.currentTimeMillis() - lastConnectionCheck
            if (timeSinceLastCheck < 20000) { // 20 seconds
                Log.d(TAG, "⏳ Reconnection in progress (${timeSinceLastCheck}ms ago), waiting...")
                return
            }
        }

        val isConnected = bleOperateManager?.isConnected == true

        Log.d(TAG, "🔍 Connection check: isConnected=$isConnected, reconnecting=$isReconnecting, attempts=$reconnectAttempts")

        when {
            isConnected -> {
                // ✅ Connection is alive
                reconnectAttempts = 0
                isReconnecting = false
                lastConnectionCheck = System.currentTimeMillis()
                updateNotification("Connected to $deviceName")
                Log.d(TAG, "✅ Connection healthy")
            }

            !isConnected && reconnectAttempts < maxReconnectAttempts -> {
                // ✅ Connection lost, try to reconnect
                reconnectAttempts++
                isReconnecting = true
                lastConnectionCheck = System.currentTimeMillis()

                Log.d(TAG, "📡 Connection lost, reconnecting... (attempt $reconnectAttempts/$maxReconnectAttempts)")
                updateNotification("Reconnecting... ($reconnectAttempts/$maxReconnectAttempts)")

                try {
                    bleOperateManager?.reConnectMac = deviceAddress
                    bleOperateManager?.connectDirectly(deviceAddress)

                    // ✅ Send bind command after delay
                    handler.postDelayed({
                        if (bleOperateManager?.isConnected == true) {
                            try {
                                commandHandle?.executeReqCmd(
                                    SimpleKeyReq(Constants.CMD_BIND_SUCCESS),
                                    null
                                )
                                Log.d(TAG, "📤 Sent CMD_BIND_SUCCESS")
                            } catch (e: Exception) {
                                Log.e(TAG, "❌ Error sending bind command: ${e.message}")
                            }
                        }
                        isReconnecting = false
                    }, 5000) // Wait 5 seconds

                } catch (e: Exception) {
                    Log.e(TAG, "❌ Reconnection error: ${e.message}", e)
                    isReconnecting = false
                }
            }

            reconnectAttempts >= maxReconnectAttempts -> {
                Log.w(TAG, "❌ Max reconnection attempts reached")
                updateNotification("Connection lost")
                // Don't stop service - wait for user or EventBus to reconnect
                handler.postDelayed({
                    // Reset attempts after 10 minutes
                    reconnectAttempts = 0
                    isReconnecting = false
                }, 600000L)
            }
        }
    }

    /**
     * ✅ Send keep-alive ping to maintain connection
     */
    private fun sendKeepAlive() {
        if (!bleOperateManager?.isConnected!!) {
            Log.d(TAG, "⚠️ Not connected, skipping keep-alive")
            return
        }

        try {
            commandHandle?.executeReqCmd(
                SimpleKeyReq(Constants.CMD_GET_DEVICE_ELECTRICITY_VALUE),
                object : ICommandResponse<BatteryRsp> {
                    override fun onDataResponse(result: BatteryRsp?) {
                        Log.d(TAG, "💚 Keep-alive ping successful: battery=${result?.batteryValue ?: "?"}%")
                        lastConnectionCheck = System.currentTimeMillis()
                        reconnectAttempts = 0
                    }
                }
            )
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Keep-alive ping failed: ${e.message}")
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBluetoothEvent(event: BluetoothEvent) {
        Log.d(TAG, "📩 EventBus: BluetoothEvent connect=${event.connect}")

        if (event.connect) {
            Log.d(TAG, "✅ Device connected via EventBus")
            updateNotification("Connected to $deviceName")
            reconnectAttempts = 0
            isReconnecting = false
            lastConnectionCheck = System.currentTimeMillis()
        } else {
            Log.d(TAG, "⚠️ Device disconnected via EventBus")
            updateNotification("Disconnected")
            // Will be picked up by next monitoring cycle
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Device Connection",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Maintains Bluetooth connection to your device"
            setShowBadge(false)
            enableLights(false)
            enableVibration(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(status: String): Notification {
        val intent = Intent(this, DeviceSDKActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(deviceName.ifEmpty { "Smart Device" })
            .setContentText(status)
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(status: String) {
        try {
            val manager = getSystemService(NotificationManager::class.java)
            manager?.notify(NOTIFICATION_ID, buildNotification(status))
        } catch (e: Exception) {
            Log.e(TAG, "❌ Notification update error: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "🛑 Service destroyed")

        reconnectRunnable?.let { handler.removeCallbacks(it) }
        keepAliveRunnable?.let { handler.removeCallbacks(it) }
        handler.removeCallbacksAndMessages(null)

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
