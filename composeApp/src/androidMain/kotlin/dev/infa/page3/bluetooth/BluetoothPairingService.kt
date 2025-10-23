package dev.infa.page3.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.util.BluetoothUtils
import dev.infa.page3.models.SmartWatch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Service to handle Bluetooth pairing and ensure system-level connection
 * This ensures the device appears as "Connected" in Android Bluetooth settings
 */
class BluetoothPairingService(private val context: Context) {
    
    companion object {
        private const val TAG = "BluetoothPairingService"
    }
    
    // State flows
    private val _isPairing = MutableStateFlow(false)
    val isPairing: StateFlow<Boolean> = _isPairing.asStateFlow()
    
    private val _pairingStatus = MutableStateFlow("Ready")
    val pairingStatus: StateFlow<String> = _pairingStatus.asStateFlow()
    
    private val _isPaired = MutableStateFlow(false)
    val isPaired: StateFlow<Boolean> = _isPaired.asStateFlow()
    
    // Bluetooth components
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val bleManager = BleOperateManager.getInstance()
    private var bondStateReceiver: BroadcastReceiver? = null
    
    init {
        registerBondStateReceiver()
    }
    
    /**
     * Pair with device and establish system-level connection
     */
    @SuppressLint("MissingPermission")
    fun pairAndConnect(device: SmartWatch): Boolean {
        if (!BluetoothUtils.isEnabledBluetooth(context)) {
            Log.w(TAG, "Bluetooth is disabled")
            return false
        }
        
        if (_isPairing.value) {
            Log.w(TAG, "Already pairing")
            return false
        }
        
        try {
            _isPairing.value = true
            _pairingStatus.value = "Starting pairing..."
            
            // Check if device is already paired
            if (isDevicePaired(device.deviceAddress)) {
                Log.d(TAG, "Device already paired, connecting...")
                return connectToPairedDevice(device)
            }
            
            // Get device for pairing
            val bluetoothDevice = getDeviceByAddress(device.deviceAddress)
            if (bluetoothDevice == null) {
                Log.e(TAG, "Device not found for pairing")
                _pairingStatus.value = "Device not found"
                _isPairing.value = false
                return false
            }
            
            // Start pairing process
            _pairingStatus.value = "Pairing with ${device.deviceName}..."
            Log.d(TAG, "Starting pairing with ${device.deviceName}")
            
            // Create bond (pair)
            val bondResult = bluetoothDevice.createBond()
            if (!bondResult) {
                Log.e(TAG, "Failed to start pairing")
                _pairingStatus.value = "Pairing failed"
                _isPairing.value = false
                return false
            }
            
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "Pairing error", e)
            _pairingStatus.value = "Pairing error: ${e.message}"
            _isPairing.value = false
            return false
        }
    }
    
    /**
     * Connect to already paired device
     */
    @SuppressLint("MissingPermission")
    private fun connectToPairedDevice(device: SmartWatch): Boolean {
        try {
            _pairingStatus.value = "Connecting to paired device..."
            
            // Connect using BLE manager
            bleManager.connectDirectly(device.deviceAddress)
            
            // Wait a moment for connection
            Thread.sleep(1000)
            
            if (bleManager.isConnected) {
                _isPaired.value = true
                _pairingStatus.value = "Connected to ${device.deviceName}"
                _isPairing.value = false
                Log.d(TAG, "Successfully connected to paired device")
                return true
            } else {
                // Try scan connection as fallback
                bleManager.connectWithScan(device.deviceAddress)
                Thread.sleep(2000)
                
                if (bleManager.isConnected) {
                    _isPaired.value = true
                    _pairingStatus.value = "Connected to ${device.deviceName}"
                    _isPairing.value = false
                    Log.d(TAG, "Successfully connected via scan")
                    return true
                } else {
                    _pairingStatus.value = "Connection failed"
                    _isPairing.value = false
                    return false
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Connection error", e)
            _pairingStatus.value = "Connection error: ${e.message}"
            _isPairing.value = false
            return false
        }
    }
    
    /**
     * Check if device is paired at system level
     */
    @SuppressLint("MissingPermission")
    fun isDevicePaired(deviceAddress: String): Boolean {
        if (!BluetoothUtils.isEnabledBluetooth(context)) return false
        
        val bondedDevices = bluetoothAdapter?.bondedDevices ?: return false
        val isPaired = bondedDevices.any { it.address == deviceAddress }
        
        Log.d(TAG, "Device $deviceAddress paired: $isPaired")
        return isPaired
    }
    
    /**
     * Get device by address
     */
    @SuppressLint("MissingPermission")
    private fun getDeviceByAddress(address: String): BluetoothDevice? {
        if (!BluetoothUtils.isEnabledBluetooth(context)) return null
        
        val bondedDevices = bluetoothAdapter?.bondedDevices
        return bondedDevices?.firstOrNull { it.address == address }
    }
    
    /**
     * Unpair device
     */
    @SuppressLint("MissingPermission")
    fun unpairDevice(deviceAddress: String): Boolean {
        return try {
            val device = getDeviceByAddress(deviceAddress)
            if (device != null) {
                // Remove bond
                val method = device.javaClass.getMethod("removeBond")
                val result = method.invoke(device) as? Boolean ?: false
                
                if (result) {
                    _isPaired.value = false
                    _pairingStatus.value = "Device unpaired"
                    Log.d(TAG, "Device unpaired successfully")
                }
                
                result
            } else {
                Log.w(TAG, "Device not found for unpairing")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unpairing error", e)
            false
        }
    }
    
    /**
     * Register broadcast receiver for bond state changes
     */
    private fun registerBondStateReceiver() {
        bondStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)
                    
                    device?.let { bluetoothDevice ->
                        handleBondStateChange(bluetoothDevice, bondState)
                    }
                }
            }
        }
        
        val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        context.registerReceiver(bondStateReceiver, filter)
        Log.d(TAG, "Bond state receiver registered")
    }
    
    /**
     * Handle bond state changes
     */
    private fun handleBondStateChange(device: BluetoothDevice, bondState: Int) {
        when (bondState) {
            BluetoothDevice.BOND_BONDING -> {
                _pairingStatus.value = "Pairing in progress..."
                Log.d(TAG, "Bonding in progress with ${device.name}")
            }
            
            BluetoothDevice.BOND_BONDED -> {
                _isPaired.value = true
                _pairingStatus.value = "Paired successfully"
                _isPairing.value = false
                Log.d(TAG, "Successfully paired with ${device.name}")
                
                // Auto-connect after pairing
                connectToPairedDevice(SmartWatch(
                    deviceName = device.name ?: "Unknown",
                    deviceAddress = device.address,
                    rssi = 0,
                    deviceType = "Device"
                ))
            }
            
            BluetoothDevice.BOND_NONE -> {
                _isPaired.value = false
                _pairingStatus.value = "Pairing failed"
                _isPairing.value = false
                Log.d(TAG, "Pairing failed with ${device.name}")
            }
        }
    }
    
    /**
     * Get human-readable bond state
     */
    fun getBondStateString(state: Int): String {
        return when (state) {
            BluetoothDevice.BOND_NONE -> "Not Paired"
            BluetoothDevice.BOND_BONDING -> "Pairing..."
            BluetoothDevice.BOND_BONDED -> "Paired"
            else -> "Unknown"
        }
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        bondStateReceiver?.let {
            try {
                context.unregisterReceiver(it)
                Log.d(TAG, "Bond state receiver unregistered")
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering receiver", e)
            }
        }
        bondStateReceiver = null
    }
}

