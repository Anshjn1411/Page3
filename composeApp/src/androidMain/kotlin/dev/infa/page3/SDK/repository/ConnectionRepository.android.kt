package dev.infa.page3.SDK.repository

import android.content.Context
import android.content.SharedPreferences
import dev.infa.page3.SDK.connection.ConnectionPlatform

/**
 * Android implementation of ConnectionRepository using SharedPreferences.
 */
actual class ConnectionRepository {

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

    private val context: Context
        get() = ConnectionPlatform.applicationContext
            ?: throw IllegalStateException("Context not initialized. Call ConnectionPlatform.initialize(context) first.")

    private val prefs: SharedPreferences
        get() = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    actual fun saveLastConnectedDevice(
        address: String,
        name: String,
        deviceType: String
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

    actual fun getSavedDeviceInfo(): SavedDeviceInfo? {
        val address = prefs.getString(KEY_LAST_DEVICE_ADDRESS, null) ?: return null
        val name = prefs.getString(KEY_LAST_DEVICE_NAME, null) ?: return null
        val type = prefs.getString(KEY_DEVICE_TYPE, "Unknown") ?: "Unknown"
        val timestamp = prefs.getLong(KEY_CONNECTION_TIMESTAMP, 0)
        val attempts = prefs.getInt(KEY_CONNECTION_ATTEMPTS, 0)
        val wasSuccessful = prefs.getBoolean(KEY_LAST_CONNECTION_SUCCESS, false)

        return SavedDeviceInfo(
            address = address,
            name = name,
            type = type,
            timestamp = timestamp,
            connectionAttempts = attempts,
            wasLastConnectionSuccessful = wasSuccessful
        )
    }

    actual fun hasSavedDevice(): Boolean {
        return getSavedDeviceInfo() != null
    }

    actual fun clearSavedDevice() {
        prefs.edit()
            .remove(KEY_LAST_DEVICE_ADDRESS)
            .remove(KEY_LAST_DEVICE_NAME)
            .remove(KEY_DEVICE_TYPE)
            .remove(KEY_CONNECTION_TIMESTAMP)
            .remove(KEY_CONNECTION_ATTEMPTS)
            .remove(KEY_LAST_CONNECTION_SUCCESS)
            .apply()
    }

    actual fun markConnectionSuccess() {
        prefs.edit()
            .putBoolean(KEY_LAST_CONNECTION_SUCCESS, true)
            .putInt(KEY_CONNECTION_ATTEMPTS, 0)
            .putLong(KEY_CONNECTION_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }

    actual fun markConnectionFailed() {
        prefs.edit()
            .putBoolean(KEY_LAST_CONNECTION_SUCCESS, false)
            .apply()
    }

    actual fun getConnectionAttempts(): Int {
        return prefs.getInt(KEY_CONNECTION_ATTEMPTS, 0)
    }

    actual fun incrementConnectionAttempts(): Int {
        val attempts = getConnectionAttempts() + 1
        prefs.edit().putInt(KEY_CONNECTION_ATTEMPTS, attempts).apply()
        return attempts
    }

    actual fun resetConnectionAttempts() {
        prefs.edit().putInt(KEY_CONNECTION_ATTEMPTS, 0).apply()
    }

    actual fun isAutoConnectEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_CONNECT_ENABLED, true)
    }

    actual fun setAutoConnectEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_CONNECT_ENABLED, enabled).apply()
    }

    actual fun isDeviceRecent(): Boolean {
        val lastConnection = prefs.getLong(KEY_CONNECTION_TIMESTAMP, 0)
        val now = System.currentTimeMillis()
        val timeDiff = now - lastConnection
        return timeDiff < 24 * 60 * 60 * 1000 // 24 hours
    }
}
