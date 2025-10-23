package dev.infa.page3.platform

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import dev.infa.page3.DeviceSDKActivity

/**
 * Android implementation of DeviceSDKLauncher
 * Handles launching the BLE SDK Activity from common code
 */
actual class DeviceSDKLauncher actual constructor(actual val context: Any) {
    private val androidContext = context as Context
    
    actual fun openDeviceManager() {
        try {
            val intent = Intent(androidContext, DeviceSDKActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            
            // Add any additional data if needed
            intent.putExtra("source", "main_app")
            
            androidContext.startActivity(intent)
        } catch (e: Exception) {
            // Log error or handle gracefully
            android.util.Log.e("DeviceSDKLauncher", "Failed to open device manager: ${e.message}")
        }
    }
    
    actual fun isSDKAvailable(): Boolean {
        return try {
            // Check if the SDK Activity exists and can be launched
            val intent = Intent(androidContext, DeviceSDKActivity::class.java)
            intent.resolveActivity(androidContext.packageManager) != null
        } catch (e: Exception) {
            false
        }
    }
}