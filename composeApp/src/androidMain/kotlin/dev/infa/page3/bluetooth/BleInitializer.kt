package dev.infa.page3.bluetooth

import android.app.Application
import android.content.Context
import android.util.Log
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.bluetooth.DeviceManager
import com.oudmon.ble.base.communication.CommandHandle

/**
 * BLE SDK Initialization Manager
 *
 * Ensures that the Oudmon BLE SDK components are initialized exactly once
 * before any BLE operations are performed.
 *
 * Thread-safe singleton pattern.
 */
object BleInitializer {

    private const val TAG = "BleInitializer"

    @Volatile
    private var initialized = false

    private val lock = Any()

    /**
     * Initialize the BLE SDK components.
     * Safe to call multiple times - will only initialize once.
     *
     * @param context Application context (will be cast to Application if possible)
     * @return true if initialization succeeded or was already done, false on failure
     */
    fun initialize(context: Context): Boolean {
        if (initialized) {
            Log.d(TAG, "BLE SDK already initialized")
            return true
        }

        synchronized(lock) {
            // Double-check inside synchronized block
            if (initialized) {
                return true
            }

            return try {
                Log.d(TAG, "Initializing BLE SDK...")

                val app = context.applicationContext as? Application

                // Initialize BleOperateManager
                BleOperateManager.getInstance(app).apply {
                    init()
                    setNeedConnect(true)
                    setBluetoothTurnOff(true)
                    Log.d(TAG, "BleOperateManager initialized")
                }

                // Initialize DeviceManager
                DeviceManager.getInstance()
                Log.d(TAG, "DeviceManager initialized")

                // Initialize CommandHandle
                CommandHandle.getInstance()
                Log.d(TAG, "CommandHandle initialized")

                initialized = true
                Log.d(TAG, "BLE SDK initialization complete")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize BLE SDK", e)
                false
            }
        }
    }
}