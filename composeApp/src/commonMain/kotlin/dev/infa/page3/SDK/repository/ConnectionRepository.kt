package dev.infa.page3.SDK.repository

import dev.infa.page3.SDK.connection.DeviceInfo

/**
 * Repository interface for managing device connection persistence.
 * Platform-specific implementations handle storage (SharedPreferences on Android, UserDefaults on iOS).
 */
expect class ConnectionRepository {
    
    /**
     * Save the last connected device information.
     */
    fun saveLastConnectedDevice(address: String, name: String, deviceType: String = "Unknown")
    
    /**
     * Get the last connected device information.
     * @return SavedDeviceInfo if a device was saved, null otherwise
     */
    fun getSavedDeviceInfo(): SavedDeviceInfo?
    
    /**
     * Check if a device is saved.
     */
    fun hasSavedDevice(): Boolean
    
    /**
     * Clear the saved device information.
     */
    fun clearSavedDevice()
    
    /**
     * Mark connection as successful (updates timestamp, resets attempts).
     */
    fun markConnectionSuccess()
    
    /**
     * Mark connection as failed.
     */
    fun markConnectionFailed()
    
    /**
     * Get connection attempts count.
     */
    fun getConnectionAttempts(): Int
    
    /**
     * Increment connection attempts count.
     */
    fun incrementConnectionAttempts(): Int
    
    /**
     * Reset connection attempts count.
     */
    fun resetConnectionAttempts()
    
    /**
     * Check if auto-connect is enabled (default: true).
     */
    fun isAutoConnectEnabled(): Boolean
    
    /**
     * Set auto-connect enabled/disabled.
     */
    fun setAutoConnectEnabled(enabled: Boolean)
    
    /**
     * Check if the saved device is recent (within 24 hours).
     */
    fun isDeviceRecent(): Boolean
}

/**
 * Data class representing saved device information.
 */
data class SavedDeviceInfo(
    val address: String,
    val name: String,
    val type: String = "Unknown",
    val timestamp: Long,
    val connectionAttempts: Int = 0,
    val wasLastConnectionSuccessful: Boolean = false
) {
    /**
     * Convert to DeviceInfo for use in ConnectionManager.
     */
    fun toDeviceInfo(): DeviceInfo {
        return DeviceInfo(name, address, 0)
    }
}
