package dev.infa.page3.payment

import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import com.phonepe.intent.sdk.api.PhonePeKt
import com.phonepe.intent.sdk.api.models.PhonePeEnvironment

/**
 * Android actual implementation of PhonePeSDKHelper.
 * Uses PhonePeKt SDK for native checkout.
 */
actual object PhonePeSDKHelper {
    private const val TAG = "PhonePeSDK"
    private var sdkInitialized = false

    // These are set by MainActivity
    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null
    private var activityRef: ComponentActivity? = null

    // Callback to notify when payment flow completes
    private var paymentResultCallback: ((Boolean) -> Unit)? = null

    /**
     * Called from MainActivity to register the activity result launcher.
     */
    fun registerActivity(
        activity: ComponentActivity,
        launcher: ActivityResultLauncher<Intent>
    ) {
        activityRef = activity
        activityResultLauncher = launcher
    }

    /**
     * Set the callback for payment result.
     */
    actual fun setPaymentResultCallback(callback: (Boolean) -> Unit) {
        paymentResultCallback = callback
    }

    /**
     * Called from the ActivityResultLauncher callback in MainActivity.
     * Notifies the payment result to whoever is listening.
     */
    fun onPaymentResult() {
        paymentResultCallback?.invoke(true)
        paymentResultCallback = null
    }

    actual fun initSDK(merchantId: String, flowId: String, isSandbox: Boolean): Boolean {
        val context = activityRef ?: run {
            return false
        }

        return try {
            val environment = if (isSandbox) PhonePeEnvironment.SANDBOX else PhonePeEnvironment.RELEASE
            val result = PhonePeKt.init(
                context = context,
                merchantId = merchantId,
                flowId = flowId.ifEmpty { "page3_checkout" },
                phonePeEnvironment = environment,
                enableLogging = true, // Enable logging for debugging
                appId = null
            )
            sdkInitialized = result
            result
        } catch (e: Exception) {
            sdkInitialized = false
            false
        }
    }

    actual fun startCheckout(token: String, orderId: String): Boolean {
        if (!sdkInitialized) {
            return false
        }

        val context = activityRef ?: run {
            return false
        }

        val launcher = activityResultLauncher ?: run {
            return false
        }

        return try {
            PhonePeKt.startCheckoutPage(
                context = context,
                token = token,
                orderId = orderId,
                activityResultLauncher = launcher
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    actual fun isInitialized(): Boolean = sdkInitialized

    /**
     * Clean up references to avoid memory leaks.
     */
    fun cleanup() {
        activityRef = null
        activityResultLauncher = null
        paymentResultCallback = null
        sdkInitialized = false
    }
}
