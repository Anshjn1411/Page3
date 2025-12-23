// androidMain/kotlin/dev/infa/page3/Platform.android.kt

package dev.infa.page3

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

// ======================= OPEN URL FUNCTION =======================

// Global context holder (initialize in Application class)
private var applicationContext: Context? = null

fun initPlatform(context: Context) {
    applicationContext = context.applicationContext
}

fun openUrl(url: String) {
    applicationContext?.let { context ->
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            // Optionally handle error (e.g., show toast or log)
        }
    } ?: run {
        // Context not initialized
        println("Error: Context not initialized. Call initPlatform() in Application class.")
    }
}
