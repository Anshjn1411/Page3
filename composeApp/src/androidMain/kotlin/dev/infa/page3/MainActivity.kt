package dev.infa.page3

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dev.infa.page3.bluetooth.BluetoothRepository
import dev.infa.page3.connection.BackgroundConnectionService
import dev.infa.page3.ui.theme.Page3Theme

/**
 * Main Activity for the Compose Multiplatform app
 * Handles starting the background Bluetooth service
 */
class MainActivity : ComponentActivity() {

    companion object {
        const val TAG = "MainActivity"
    }

    private lateinit var bluetoothRepository: BluetoothRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Log.d(TAG, "MainActivity created")

        bluetoothRepository = BluetoothRepository(this)

        // Check if we have saved device - start background service immediately
        checkAndStartBackgroundService()

        setContent {
            Page3Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    dev.infa.page3.App()
                }
            }
        }
    }

    private fun checkAndStartBackgroundService() {
        val savedInfo = bluetoothRepository.getSavedDeviceInfo()
        if (savedInfo != null) {
            Log.d(TAG, "Found saved device: ${savedInfo.name}")
            // Start background service to maintain connection
            BackgroundConnectionService.start(this, savedInfo.name, savedInfo.address)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "MainActivity resumed")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "MainActivity paused - service keeps connection")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "MainActivity destroyed - service keeps connection")
    }
}