package dev.infa.page3.SDK.repository

import platform.Foundation.NSUserDefaults
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

actual class ConnectionRepository {
    private val defaults = NSUserDefaults.standardUserDefaults

    private companion object {
        const val KEY_DEVICE_ADDRESS = "ble_device_address"
        const val KEY_DEVICE_NAME = "ble_device_name"
        const val KEY_DEVICE_TYPE = "ble_device_type"
        const val KEY_LAST_CONNECTED_TIMESTAMP = "ble_last_connected_timestamp"
        const val KEY_CONNECTION_ATTEMPTS = "ble_connection_attempts"
        const val KEY_LAST_CONNECTION_SUCCESS = "ble_last_connection_success"
        const val KEY_AUTO_CONNECT = "ble_auto_connect"
        const val TWENTY_FOUR_HOURS_MS = 24L * 60 * 60 * 1000
    }

    actual fun saveLastConnectedDevice(address: String, name: String, deviceType: String) {
        defaults.setObject(address, KEY_DEVICE_ADDRESS)
        defaults.setObject(name, KEY_DEVICE_NAME)
        defaults.setObject(deviceType, KEY_DEVICE_TYPE)
        defaults.setDouble(currentTimeMillis().toDouble(), KEY_LAST_CONNECTED_TIMESTAMP)
        defaults.synchronize()
    }

    actual fun getSavedDeviceInfo(): SavedDeviceInfo? {
        val address = defaults.stringForKey(KEY_DEVICE_ADDRESS) ?: return null
        val name = defaults.stringForKey(KEY_DEVICE_NAME) ?: return null
        val type = defaults.stringForKey(KEY_DEVICE_TYPE) ?: "Unknown"
        val timestamp = defaults.doubleForKey(KEY_LAST_CONNECTED_TIMESTAMP).toLong()
        val attempts = defaults.integerForKey(KEY_CONNECTION_ATTEMPTS).toInt()
        val wasSuccess = defaults.boolForKey(KEY_LAST_CONNECTION_SUCCESS)

        return SavedDeviceInfo(
            address = address,
            name = name,
            type = type,
            timestamp = timestamp,
            connectionAttempts = attempts,
            wasLastConnectionSuccessful = wasSuccess
        )
    }

    actual fun hasSavedDevice(): Boolean {
        return defaults.stringForKey(KEY_DEVICE_ADDRESS) != null
    }

    actual fun clearSavedDevice() {
        defaults.removeObjectForKey(KEY_DEVICE_ADDRESS)
        defaults.removeObjectForKey(KEY_DEVICE_NAME)
        defaults.removeObjectForKey(KEY_DEVICE_TYPE)
        defaults.removeObjectForKey(KEY_LAST_CONNECTED_TIMESTAMP)
        defaults.removeObjectForKey(KEY_CONNECTION_ATTEMPTS)
        defaults.removeObjectForKey(KEY_LAST_CONNECTION_SUCCESS)
        defaults.synchronize()
    }

    actual fun markConnectionSuccess() {
        defaults.setBool(true, KEY_LAST_CONNECTION_SUCCESS)
        defaults.setDouble(currentTimeMillis().toDouble(), KEY_LAST_CONNECTED_TIMESTAMP)
        defaults.setInteger(0, KEY_CONNECTION_ATTEMPTS)
        defaults.synchronize()
    }

    actual fun markConnectionFailed() {
        defaults.setBool(false, KEY_LAST_CONNECTION_SUCCESS)
        incrementConnectionAttempts()
    }

    actual fun getConnectionAttempts(): Int {
        return defaults.integerForKey(KEY_CONNECTION_ATTEMPTS).toInt()
    }

    actual fun incrementConnectionAttempts(): Int {
        val current = getConnectionAttempts() + 1
        defaults.setInteger(current.toLong(), KEY_CONNECTION_ATTEMPTS)
        defaults.synchronize()
        return current
    }

    actual fun resetConnectionAttempts() {
        defaults.setInteger(0, KEY_CONNECTION_ATTEMPTS)
        defaults.synchronize()
    }

    actual fun isAutoConnectEnabled(): Boolean {
        // Default to true if not set (objectForKey returns null)
        if (defaults.objectForKey(KEY_AUTO_CONNECT) == null) return true
        return defaults.boolForKey(KEY_AUTO_CONNECT)
    }

    actual fun setAutoConnectEnabled(enabled: Boolean) {
        defaults.setBool(enabled, KEY_AUTO_CONNECT)
        defaults.synchronize()
    }

    actual fun isDeviceRecent(): Boolean {
        val timestamp = defaults.doubleForKey(KEY_LAST_CONNECTED_TIMESTAMP).toLong()
        if (timestamp == 0L) return false
        return (currentTimeMillis() - timestamp) < TWENTY_FOUR_HOURS_MS
    }

    private fun currentTimeMillis(): Long {
        return (NSDate().timeIntervalSince1970 * 1000).toLong()
    }
}