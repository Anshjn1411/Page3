package dev.infa.page3

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import dev.infa.page3.navigation.AppViewModels
import dev.infa.page3.navigation.initializePlatform
import dev.infa.page3.payment.PhonePeSDKHelper
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

    // ======================== PhonePe SDK ========================

    /**
     * ActivityResultLauncher for PhonePe SDK checkout callback.
     * After the user completes (or cancels) the PhonePe payment,
     * this callback fires and we delegate to PhonePeSDKHelper.
     */
    private val phonePeResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            Log.d("MainActivity", "PhonePe SDK result received")
            PhonePeSDKHelper.onPaymentResult()
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Init platform & ViewModels immediately
        initializePlatformManagers()

        // 2. Permissions are still required for BLE features
        checkPermissions()

        // 3. Register PhonePe SDK with this Activity and the result launcher
        PhonePeSDKHelper.registerActivity(this, phonePeResultLauncher)

        // 4. Initialize PhonePe SDK
        initPhonePeSDK()

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

    override fun onDestroy() {
        super.onDestroy()
        PhonePeSDKHelper.cleanup()
    }

    /**
     * Initialize PhonePe SDK with merchant credentials from BuildConfig.
     */
    private fun initPhonePeSDK() {
        try {
            val merchantId = BuildConfig.PHONEPE_MERCHANT_ID
            if (merchantId.isNotEmpty()) {
                val isSandbox = false // PRODUCTION
                val result = PhonePeSDKHelper.initSDK(
                    merchantId = merchantId,
                    flowId = "page3_checkout",
                    isSandbox = isSandbox
                )
                Log.d("MainActivity", "PhonePe SDK init: $result")
            } else {
                Log.w("MainActivity", "PhonePe SDK credentials not configured. Update gradle.properties.")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "PhonePe SDK init failed: ${e.message}", e)
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
