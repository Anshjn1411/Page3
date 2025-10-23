package dev.infa.page3.connection

import android.app.Service
import android.content.Context
import android.content.SharedPreferences

class ConnectionPersistenceManager(context: Context) {

    private val prefs = context.getSharedPreferences("device_connection", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_DEVICE_ADDRESS = "device_address"
        private const val KEY_DEVICE_NAME = "device_name"
        private const val KEY_LAST_CONNECTED = "last_connected"
    }

    fun saveConnection(address: String, name: String) {
        prefs.edit()
            .putString(KEY_DEVICE_ADDRESS, address)
            .putString(KEY_DEVICE_NAME, name)
            .putLong(KEY_LAST_CONNECTED, System.currentTimeMillis())
            .apply()
    }

    fun getLastDeviceAddress(): String? = prefs.getString(KEY_DEVICE_ADDRESS, null)

    fun getLastDeviceName(): String? = prefs.getString(KEY_DEVICE_NAME, null)

    fun clearConnection() {
        prefs.edit()
            .remove(KEY_DEVICE_ADDRESS)
            .remove(KEY_DEVICE_NAME)
            .remove(KEY_LAST_CONNECTED)
            .apply()
    }
}