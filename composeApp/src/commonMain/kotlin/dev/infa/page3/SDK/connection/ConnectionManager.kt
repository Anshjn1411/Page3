// commonMain/kotlin/dev/infa/page3/connection/ConnectionManager.kt
package dev.infa.page3.SDK.connection

/**
 * Platform-agnostic connection manager interface.
 * Android/iOS will provide actual implementations.
 */
expect class ConnectionManager {
    
    /**
     * Initialize the BLE SDK (once per app lifecycle)
     */
    fun initialize()
    
    /**
     * Check if all required permissions are granted
     */
    fun hasPermissions(): Boolean
    
    /**
     * Start scanning for devices
     */
    fun startScan(onDeviceFound: (DeviceInfo) -> Unit, onError: (String) -> Unit)
    
    /**
     * Stop scanning
     */
    fun stopScan()
    
    /**
     * Connect to a device (starts background service)
     */
    fun connect(deviceName: String, deviceAddress: String)
    
    /**
     * Disconnect from current device (stops background service)
     */
    fun disconnect()
    
    /**
     * Check if currently connected
     */
    fun isConnected(): Boolean
    
    /**
     * Get battery level (returns null if not connected)
     */
    fun getBatteryLevel(onResult: (Int?) -> Unit)
    
    /**
     * Observe connection state changes
     */
    fun observeConnectionState(onStateChange: (ConnectionState) -> Unit)
}

/**
 * Simple device info model
 */
data class DeviceInfo(
    val name: String,
    val address: String,
    val rssi: Int
)

/**
 * Connection states
 */
enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    ERROR
}