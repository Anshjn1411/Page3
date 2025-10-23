package dev.infa.page3.bluetooth

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
import dev.infa.page3.DeviceSDKActivity
import dev.infa.page3.models.SmartWatch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Foreground service to maintain stable Bluetooth connection
 * Ensures device stays connected and handles reconnection
 */
class StableConnectionService : Service() {
    
    companion object {
        private const val TAG = "StableConnectionService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "stable_connection"
        private const val RECONNECT_INTERVAL_MS = 5000L // Check every 5 seconds
        
        fun start(context: Context, device: SmartWatch) {
            val intent = Intent(context, StableConnectionService::class.java).apply {
                putExtra("device_name", device.deviceName)
                putExtra("device_address", device.deviceAddress)
            }
            context.startForegroundService(intent)
        }
        
        fun stop(context: Context) {
            context.stopService(Intent(context, StableConnectionService::class.java))
        }
    }
    
    // Service state
    private var deviceName: String = ""
    private var deviceAddress: String = ""
    private val bleManager = BleOperateManager.getInstance()
    private val handler = Handler(Looper.getMainLooper())
    private var reconnectRunnable: Runnable? = null
    private val isServiceRunning = AtomicBoolean(false)
    
    // State flows
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private val _connectionStatus = MutableStateFlow("Starting...")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        createNotificationChannel()
        isServiceRunning.set(true)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        deviceName = intent?.getStringExtra("device_name") ?: "Unknown Device"
        deviceAddress = intent?.getStringExtra("device_address") ?: ""
        
        Log.d(TAG, "Service started for $deviceName @ $deviceAddress")
        
        // Start foreground immediately
        startForeground(NOTIFICATION_ID, buildNotification("Connecting..."))
        
        // Setup BLE manager for stable connection
        setupBleManager()
        
        // Start connection monitoring
        startConnectionMonitoring()
        
        return START_STICKY // Restart if killed
    }
    
    private fun setupBleManager() {
        try {
            // Enable system Bluetooth monitoring
            bleManager.setBluetoothTurnOff(true)
            // Enable auto-reconnect
            bleManager.setNeedConnect(true)
            Log.d(TAG, "BLE manager configured for stable connection")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to setup BLE manager", e)
        }
    }
    
    private fun startConnectionMonitoring() {
        reconnectRunnable?.let { handler.removeCallbacks(it) }
        
        reconnectRunnable = Runnable {
            if (isServiceRunning.get()) {
                checkAndMaintainConnection()
                // Schedule next check
                handler.postDelayed(reconnectRunnable!!, RECONNECT_INTERVAL_MS)
            }
        }
        
        // Start monitoring
        handler.postDelayed(reconnectRunnable!!, RECONNECT_INTERVAL_MS)
        Log.d(TAG, "Connection monitoring started")
    }
    
    private fun checkAndMaintainConnection() {
        if (deviceAddress.isEmpty()) {
            Log.w(TAG, "No device address, stopping service")
            stopSelf()
            return
        }
        
        val isCurrentlyConnected = bleManager.isConnected
        
        if (isCurrentlyConnected) {
            // Connection is alive
            _isConnected.value = true
            _connectionStatus.value = "Connected to $deviceName"
            updateNotification("Connected to $deviceName")
        } else {
            // Connection lost, try to reconnect
            _isConnected.value = false
            _connectionStatus.value = "Reconnecting..."
            updateNotification("Reconnecting to $deviceName...")
            
            Log.d(TAG, "Connection lost, attempting reconnection...")
            
            try {
                // Try direct connection first
                bleManager.connectDirectly(deviceAddress)
                
                // Check connection after a short delay
                handler.postDelayed({
                    if (bleManager.isConnected) {
                        _isConnected.value = true
                        _connectionStatus.value = "Reconnected to $deviceName"
                        updateNotification("Reconnected to $deviceName")
                        Log.d(TAG, "Successfully reconnected")
                    } else {
                        // Try scan connection as fallback
                        try {
                            bleManager.connectWithScan(deviceAddress)
                            handler.postDelayed({
                                if (bleManager.isConnected) {
                                    _isConnected.value = true
                                    _connectionStatus.value = "Reconnected to $deviceName"
                                    updateNotification("Reconnected to $deviceName")
                                    Log.d(TAG, "Successfully reconnected via scan")
                                } else {
                                    _connectionStatus.value = "Reconnection failed"
                                    updateNotification("Reconnection failed")
                                    Log.w(TAG, "Reconnection failed")
                                }
                            }, 2000)
                        } catch (e: Exception) {
                            Log.e(TAG, "Scan reconnection failed", e)
                        }
                    }
                }, 1000)
                
            } catch (e: Exception) {
                Log.e(TAG, "Reconnection error", e)
                _connectionStatus.value = "Reconnection error"
                updateNotification("Reconnection error")
            }
        }
    }
    
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Stable Connection",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Maintains stable Bluetooth connection"
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
            Log.e(TAG, "Notification update error", e)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        
        // Clean up
        isServiceRunning.set(false)
        reconnectRunnable?.let { handler.removeCallbacks(it) }
        handler.removeCallbacksAndMessages(null)
        
        // Don't disconnect - let BLE manager handle it
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}
