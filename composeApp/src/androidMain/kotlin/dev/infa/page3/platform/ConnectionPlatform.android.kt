package dev.infa.page3.platform

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.oudmon.ble.base.bluetooth.BleOperateManager
import dev.infa.page3.connection.BleConnectionService
import android.util.Log

private object AndroidConnectionPlatformState {
    @Volatile
    var sdkInitialized: Boolean = false

    @Volatile
    var applicationContext: Context? = null
}

actual object ConnectionServiceController {
    actual fun start() {
        AndroidConnectionPlatformState.applicationContext?.let { ctx ->
            // Start service without extras to allow auto-resume from saved prefs
            val intent = Intent(ctx, BleConnectionService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                ctx.startForegroundService(intent)
            } else {
                ctx.startService(intent)
            }
        }
    }

    actual fun start(deviceName: String, deviceAddress: String) {
        AndroidConnectionPlatformState.applicationContext?.let { ctx ->
            BleConnectionService.start(ctx, deviceName, deviceAddress)
        }
    }

    actual fun stop() {
        AndroidConnectionPlatformState.applicationContext?.let { ctx ->
            BleConnectionService.stop(ctx)
        }
    }
}

@Composable
actual fun EnsureBackgroundConnection() {
    val context = LocalContext.current.applicationContext
    val remembered = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!remembered.value) {
            AndroidConnectionPlatformState.applicationContext = context

            if (!AndroidConnectionPlatformState.sdkInitialized) {
                try {
                    Log.d("ConnectionPlatform", "Initializing BLE SDK")
                    BleOperateManager.getInstance(context as? android.app.Application).apply {
                        init()
                        setNeedConnect(true)
                        setBluetoothTurnOff(true)
                    }
                    AndroidConnectionPlatformState.sdkInitialized = true
                } catch (_: Exception) {
                }
            }

            ConnectionServiceController.start()
            remembered.value = true
        }
    }
}


