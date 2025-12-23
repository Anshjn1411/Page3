package dev.infa.page3

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import dev.infa.page3.navigation.AppViewModels
import dev.infa.page3.navigation.initializePlatform
import dev.infa.page3.ui.theme.Page3Theme
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private var allPermissionsGranted = false

    private val requiredPermissions: Array<String>
        get() = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_CONNECT)
            } else {
                add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            allPermissionsGranted = result.values.all { it }

            if (allPermissionsGranted) {
                if (isBluetoothEnabled()) {
                    initializePlatformManagers()
                } else {
                    requestEnableBluetooth()
                }
            } else {
                Toast.makeText(
                    this,
                    "Bluetooth permission is required",
                    Toast.LENGTH_LONG
                ).show()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Init platform & ViewModels immediately
        initializePlatformManagers()

        // 2. Permissions are still required for BLE features
        checkPermissions()

        setContent {
            Page3Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (allPermissionsGranted && !isBluetoothEnabled()) {
            suggestEnableBluetooth()
        }
    }


    private fun checkPermissions() {
        val allGranted = requiredPermissions.all {
            checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            allPermissionsGranted = true
        } else {
            permissionLauncher.launch(requiredPermissions)
        }
    }


    private fun suggestEnableBluetooth() {
        Toast.makeText(
            this,
            "Bluetooth is off. Turn it on for better experience.",
            Toast.LENGTH_LONG
        ).show()

        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        enableBluetoothLauncher.launch(intent)
    }


    private fun initializePlatformManagers() {
        if (AppViewModels.isInitialized) return

        lifecycleScope.launch {
            initializePlatform()
        }
    }

    private val enableBluetoothLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (isBluetoothEnabled()) {
                Toast.makeText(this, "Bluetooth enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    "Bluetooth remains off. Some features may not work.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun isBluetoothEnabled(): Boolean {
        val bluetoothManager =
            getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        return bluetoothManager.adapter?.isEnabled == true
    }
    private fun requestEnableBluetooth() {
        if (!isBluetoothEnabled()) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(intent)
        }
    }
}

