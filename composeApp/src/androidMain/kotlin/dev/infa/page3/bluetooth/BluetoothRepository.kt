package dev.infa.page3.bluetooth

import android.content.Context
import android.content.SharedPreferences

/**
 * Repository for managing Bluetooth device persistence and settings
 * Handles storing/retrieving last connected device information
 */
class BluetoothRepository(private val context: Context) {
    
    companion object {
        private const val PREFS_NAME = "bluetooth_auto_connect_prefs"
        private const val KEY_LAST_DEVICE_ADDRESS = "last_device_address"
        private const val KEY_LAST_DEVICE_NAME = "last_device_name"
        private const val KEY_CONNECTION_TIMESTAMP = "connection_timestamp"
        private const val KEY_AUTO_CONNECT_ENABLED = "auto_connect_enabled"
        private const val KEY_DEVICE_TYPE = "device_type"
        private const val KEY_CONNECTION_ATTEMPTS = "connection_attempts"
        private const val KEY_LAST_CONNECTION_SUCCESS = "last_connection_success"
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * Save last connected device information
     */
    fun saveLastConnectedDevice(
        address: String,
        name: String,
        deviceType: String = "Unknown"
    ) {
        prefs.edit()
            .putString(KEY_LAST_DEVICE_ADDRESS, address)
            .putString(KEY_LAST_DEVICE_NAME, name)
            .putString(KEY_DEVICE_TYPE, deviceType)
            .putLong(KEY_CONNECTION_TIMESTAMP, System.currentTimeMillis())
            .putBoolean(KEY_LAST_CONNECTION_SUCCESS, true)
            .putInt(KEY_CONNECTION_ATTEMPTS, 0)
            .apply()
    }
    
    /**
     * Get last connected device address
     */
    fun getLastDeviceAddress(): String? = prefs.getString(KEY_LAST_DEVICE_ADDRESS, null)
    
    /**
     * Get last connected device name
     */
    fun getLastDeviceName(): String? = prefs.getString(KEY_LAST_DEVICE_NAME, null)
    
    /**
     * Get last connected device type
     */
    fun getLastDeviceType(): String = prefs.getString(KEY_DEVICE_TYPE, "Unknown") ?: "Unknown"
    
    /**
     * Get connection timestamp
     */
    fun getConnectionTimestamp(): Long = prefs.getLong(KEY_CONNECTION_TIMESTAMP, 0)
    
    /**
     * Check if we have a saved device
     */
    fun hasSavedDevice(): Boolean = getLastDeviceAddress() != null
    
    /**
     * Enable/disable auto-connect feature
     */
    fun setAutoConnectEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_CONNECT_ENABLED, enabled).apply()
    }
    
    /**
     * Check if auto-connect is enabled
     */
    fun isAutoConnectEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_CONNECT_ENABLED, true)
    
    /**
     * Increment connection attempts counter
     */
    fun incrementConnectionAttempts(): Int {
        val attempts = getConnectionAttempts() + 1
        prefs.edit().putInt(KEY_CONNECTION_ATTEMPTS, attempts).apply()
        return attempts
    }
    
    /**
     * Get current connection attempts count
     */
    fun getConnectionAttempts(): Int = prefs.getInt(KEY_CONNECTION_ATTEMPTS, 0)
    
    /**
     * Reset connection attempts counter
     */
    fun resetConnectionAttempts() {
        prefs.edit().putInt(KEY_CONNECTION_ATTEMPTS, 0).apply()
    }
    
    /**
     * Mark connection as successful
     */
    fun markConnectionSuccess() {
        prefs.edit()
            .putBoolean(KEY_LAST_CONNECTION_SUCCESS, true)
            .putInt(KEY_CONNECTION_ATTEMPTS, 0)
            .putLong(KEY_CONNECTION_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }
    
    /**
     * Mark connection as failed
     */
    fun markConnectionFailed() {
        prefs.edit().putBoolean(KEY_LAST_CONNECTION_SUCCESS, false).apply()
    }
    
    /**
     * Check if last connection was successful
     */
    fun wasLastConnectionSuccessful(): Boolean = prefs.getBoolean(KEY_LAST_CONNECTION_SUCCESS, false)
    
    /**
     * Check if device is recent (within 24 hours)
     */
    fun isDeviceRecent(): Boolean {
        val lastConnection = getConnectionTimestamp()
        val now = System.currentTimeMillis()
        val timeDiff = now - lastConnection
        return timeDiff < 24 * 60 * 60 * 1000 // 24 hours
    }
    
    /**
     * Clear all saved device data
     */
    fun clearSavedDevice() {
        prefs.edit()
            .remove(KEY_LAST_DEVICE_ADDRESS)
            .remove(KEY_LAST_DEVICE_NAME)
            .remove(KEY_DEVICE_TYPE)
            .remove(KEY_CONNECTION_TIMESTAMP)
            .remove(KEY_CONNECTION_ATTEMPTS)
            .remove(KEY_LAST_CONNECTION_SUCCESS)
            .apply()
    }
    
    /**
     * Get all saved device info as a data class
     */
    fun getSavedDeviceInfo(): SavedDeviceInfo? {
        val address = getLastDeviceAddress() ?: return null
        val name = getLastDeviceName() ?: return null
        val type = getLastDeviceType()
        val timestamp = getConnectionTimestamp()
        val attempts = getConnectionAttempts()
        val wasSuccessful = wasLastConnectionSuccessful()
        
        return SavedDeviceInfo(
            address = address,
            name = name,
            type = type,
            timestamp = timestamp,
            connectionAttempts = attempts,
            wasLastConnectionSuccessful = wasSuccessful
        )
    }
}

/**
 * Data class for saved device information
 */
data class SavedDeviceInfo(
    val address: String,
    val name: String,
    val type: String,
    val timestamp: Long,
    val connectionAttempts: Int,
    val wasLastConnectionSuccessful: Boolean
)
