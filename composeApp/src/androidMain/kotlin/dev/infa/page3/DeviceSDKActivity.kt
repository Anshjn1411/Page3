package dev.infa.page3

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.collectAsState
import androidx.core.app.ActivityCompat
import dev.infa.page3.ui.theme.Page3Theme
import com.oudmon.ble.base.bluetooth.*
import com.oudmon.ble.base.communication.*
import com.oudmon.ble.base.scan.BleScannerHelper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.oudmon.ble.base.util.BluetoothUtils
import dev.infa.page3.init.PermissionHandler
import dev.infa.page3.init.SDKInitializer
import dev.infa.page3.ui.navigation.AppNavigation
import dev.infa.page3.viewmodels.ConnectionViewModel
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

/**
 * Android Activity containing all BLE SDK functionality
 * This is launched from the common UI when "My Devices" button is clicked
 */
class DeviceSDKActivity : ComponentActivity() {
    companion object {
        const val TAG = "DeviceSDKActivity"
    }

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            checkBluetoothEnabled()
        } else {
            Log.e(MainActivity.Companion.TAG, "Permissions denied")
        }
    }

    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Log.d(MainActivity.Companion.TAG, "Bluetooth enabled")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Request permissions (SDK is initialized via EnsureBackgroundConnection/Service)
        requestRequiredPermissions()
        setContent {
            Page3Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation()
                }
            }
        }
    }

    // SDK initialization moved to platform EnsureBackgroundConnection and Service

    private fun requestRequiredPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        val allPermissionsGranted = permissions.all {
            ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (!allPermissionsGranted) {
            requestPermissions.launch(permissions)
        } else {
            checkBluetoothEnabled()
        }
    }

    private fun checkBluetoothEnabled() {
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            BleOperateManager.getInstance().disconnect()
        } catch (e: Exception) {
            Log.e(MainActivity.Companion.TAG, "Error during cleanup: ${e.message}")
        }
    }
}


