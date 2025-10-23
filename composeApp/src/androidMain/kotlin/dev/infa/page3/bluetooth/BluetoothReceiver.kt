package dev.infa.page3.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.RequiresPermission


class BluetoothReceiver : BroadcastReceiver() {
    
    companion object {
        const val TAG = "BluetoothReceiver"
        
        // Action callbacks
        const val ACTION_BLUETOOTH_STATE_CHANGED = "dev.infa.page3.BLUETOOTH_STATE_CHANGED"
        const val ACTION_DEVICE_DISCOVERED = "dev.infa.page3.DEVICE_DISCOVERED"
        const val ACTION_DEVICE_CONNECTED = "dev.infa.page3.DEVICE_CONNECTED"
        const val ACTION_DEVICE_DISCONNECTED = "dev.infa.page3.DEVICE_DISCONNECTED"
        
        // Intent extras
        const val EXTRA_DEVICE_ADDRESS = "device_address"
        const val EXTRA_DEVICE_NAME = "device_name"
        const val EXTRA_BLUETOOTH_STATE = "bluetooth_state"
        const val EXTRA_DEVICE_RSSI = "device_rssi"
    }
    
    private var onBluetoothStateChanged: ((Boolean) -> Unit)? = null
    private var onDeviceDiscovered: ((BluetoothDevice, Int) -> Unit)? = null
    private var onDeviceConnected: ((BluetoothDevice) -> Unit)? = null
    private var onDeviceDisconnected: ((BluetoothDevice) -> Unit)? = null
    
    /**
     * Set callback for Bluetooth state changes
     */
    fun setOnBluetoothStateChanged(callback: (Boolean) -> Unit) {
        onBluetoothStateChanged = callback
    }
    
    /**
     * Set callback for device discovery
     */
    fun setOnDeviceDiscovered(callback: (BluetoothDevice, Int) -> Unit) {
        onDeviceDiscovered = callback
    }
    
    /**
     * Set callback for device connection
     */
    fun setOnDeviceConnected(callback: (BluetoothDevice) -> Unit) {
        onDeviceConnected = callback
    }
    
    /**
     * Set callback for device disconnection
     */
    fun setOnDeviceDisconnected(callback: (BluetoothDevice) -> Unit) {
        onDeviceDisconnected = callback
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                handleBluetoothStateChanged(intent)
            }
            
            BluetoothDevice.ACTION_FOUND -> {
                handleDeviceFound(intent)
            }
            
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                handleDeviceConnected(intent)
            }
            
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                handleDeviceDisconnected(intent)
            }
            
            BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                Log.d(TAG, "Bluetooth discovery started")
            }
            
            BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                Log.d(TAG, "Bluetooth discovery finished")
            }
        }
    }
    
    private fun handleBluetoothStateChanged(intent: Intent) {
        val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
        val isEnabled = state == BluetoothAdapter.STATE_ON
        
        Log.d(TAG, "Bluetooth state changed: $state, enabled: $isEnabled")
        
        onBluetoothStateChanged?.invoke(isEnabled)
        
        // Note: We can't send broadcasts from a BroadcastReceiver
        // The callbacks will handle the communication
    }
    
    private fun handleDeviceFound(intent: Intent) {
        val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
        val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE).toInt()
        
        if (device != null) {
            Log.d(TAG, "Device found: ${device.name} (${device.address}) RSSI: $rssi")
            
            onDeviceDiscovered?.invoke(device, rssi)
            
            // Note: We can't send broadcasts from a BroadcastReceiver
            // The callbacks will handle the communication
        }
    }
    
    private fun handleDeviceConnected(intent: Intent) {
        val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
        
        if (device != null) {
            Log.d(TAG, "Device connected: ${device.name} (${device.address})")
            
            onDeviceConnected?.invoke(device)
            
            // Note: We can't send broadcasts from a BroadcastReceiver
            // The callbacks will handle the communication
        }
    }
    
    private fun handleDeviceDisconnected(intent: Intent) {
        val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
        
        if (device != null) {
            Log.d(TAG, "Device disconnected: ${device.name} (${device.address})")
            
            onDeviceDisconnected?.invoke(device)
            
            // Note: We can't send broadcasts from a BroadcastReceiver
            // The callbacks will handle the communication
        }
    }
    
    /**
     * Check if a discovered device matches our saved device
     */
    fun isTargetDevice(device: BluetoothDevice, targetAddress: String?): Boolean {
        return device.address.equals(targetAddress, ignoreCase = true)
    }
    
    /**
     * Check if a device is a QC-compatible device
     */
    fun isQCCompatibleDevice(device: BluetoothDevice): Boolean {
        val name = device.name ?: return false
        val nameUpper = name.uppercase()
        
        val qcPatterns = listOf(
            "QC_", "O_", "Q_", "QC", "RING", "BAND", "WATCH",
            "GREEN", "ORANGE", "WIRELESS", "HEALTH", "FITNESS",
            "BLE", "SMART", "WEARABLE"
        )
        
        return qcPatterns.any { pattern ->
            nameUpper.contains(pattern) || nameUpper.startsWith(pattern)
        }
    }
    
    /**
     * Get device type from name
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun getDeviceType(device: BluetoothDevice): String {
        val name = device.name ?: return "Unknown"
        val nameUpper = name.uppercase()
        
        return when {
            nameUpper.contains("RING") -> "Ring"
            nameUpper.contains("BAND") -> "Band"
            nameUpper.contains("WATCH") -> "Watch"
            nameUpper.contains("QC_R") || nameUpper.contains("O_R") -> "Ring"
            nameUpper.contains("QC_B") || nameUpper.contains("O_B") -> "Band"
            nameUpper.contains("QC_W") || nameUpper.contains("O_W") -> "Watch"
            else -> "Device"
        }
    }
}
