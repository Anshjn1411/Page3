package dev.infa.page3

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import dev.infa.page3.viewmodels.HealthMonitor
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

    private lateinit var viewModel: ConnectionViewModel
    private lateinit var healthMonitor: HealthMonitor
    private lateinit var permissionHandler: PermissionHandler

    private val bluetoothEnableLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            addLog("Bluetooth enabled")
            // Bluetooth is now enabled, ready for scanning
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setupViewModel()
        setupHealthMonitor()
        setupPermissionHandler()
        setupUI()
    }

    private fun setupViewModel() {
        val sdkInitializer = SDKInitializer(application) { log -> Log.d(TAG, log) }
        val components = sdkInitializer.initializeSDK()

        viewModel = ViewModelProvider(
            this,
            ConnectionViewModelFactory(
                context = this,
                bleOperateManager = components.bleOperateManager,
                deviceManager = components.deviceManager,
                commandHandle = components.commandHandle,
                bleScannerHelper = components.bleScannerHelper
            )
        )[ConnectionViewModel::class.java]
    }

    private fun setupHealthMonitor() {
        healthMonitor = HealthMonitor(
            commandHandle = viewModel.exposedCommandHandle,
            coroutineScope = lifecycleScope,
            addLog = { addLog(it) }
        )
    }

    private fun setupPermissionHandler() {
        permissionHandler = PermissionHandler(
            activity = this,
            onPermissionsGranted = {
                if (BluetoothUtils.isEnabledBluetooth(this)) {
                    addLog("Permissions granted, ready for device connection")
                } else {
                    ensureBluetoothEnabled()
                }
            },
            onPermissionsDenied = { },
            addLog = { addLog(it) }
        )
        permissionHandler.checkAndRequestPermissions()
    }

    private fun ensureBluetoothEnabled() {
        if (!BluetoothUtils.isEnabledBluetooth(this)) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            bluetoothEnableLauncher.launch(intent)
        }
    }

    private fun setupUI() {
        setContent {
            Page3Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(
                        connectionViewModel = viewModel,
                        healthMonitor = healthMonitor,
                        commandHandle = viewModel.exposedCommandHandle
                    )
                    permissionHandler.PermissionDialog()
                }
            }
        }
    }

    private fun addLog(message: String) {
        if (::viewModel.isInitialized) {
            lifecycleScope.launch { viewModel.addLog(message) }
        } else {
            Log.d(TAG, message)
        }
    }

    override fun onResume() {
        super.onResume()
        // Connection maintained in background - no action needed
    }

    override fun onPause() {
        super.onPause()
        // Keep connection alive - only stop scanning
        if (viewModel.isScanning.value) {
            viewModel.stopScanning()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            EventBus.getDefault().unregister(this)
        } catch (e: Exception) {
            Log.d(TAG, "EventBus unregister: ${e.message}")
        }
    }
}

// ============ ViewModel Factory ============
class ConnectionViewModelFactory(
    private val context: Context,
    private val bleOperateManager: BleOperateManager?,
    private val deviceManager: DeviceManager?,
    private val commandHandle: CommandHandle?,
    private val bleScannerHelper: BleScannerHelper?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConnectionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ConnectionViewModel(
                context,
                commandHandle
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

