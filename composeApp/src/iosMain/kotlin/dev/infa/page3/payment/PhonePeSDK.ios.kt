package dev.infa.page3.payment

/**
 * iOS stub implementation of PhonePeSDKHelper.
 * PhonePe SDK is Android-only, so this is a no-op.
 */
actual object PhonePeSDKHelper {

    actual fun initSDK(merchantId: String, flowId: String, isSandbox: Boolean): Boolean {
        println("⚠️ PhonePe SDK is not available on iOS")
        return false
    }

    actual fun startCheckout(token: String, orderId: String): Boolean {
        println("⚠️ PhonePe SDK is not available on iOS")
        return false
    }

    actual fun setPaymentResultCallback(callback: (Boolean) -> Unit) {
        // No-op on iOS
    }

    actual fun isInitialized(): Boolean = false
}
