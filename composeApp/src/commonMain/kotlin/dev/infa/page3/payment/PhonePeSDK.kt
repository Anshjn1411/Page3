package dev.infa.page3.payment

/**
 * Expect declarations for PhonePe SDK operations.
 * Android actual uses PhonePeKt SDK, iOS actual is a no-op stub.
 */

/**
 * Singleton helper for PhonePe SDK initialization and checkout.
 */
expect object PhonePeSDKHelper {
    /**
     * Initialize the PhonePe SDK. Must be called before startCheckout.
     * @param merchantId Your PhonePe merchant ID
     * @param flowId A unique flow identifier (can be empty string)
     * @param isSandbox true for sandbox/testing, false for production
     * @return true if initialization succeeded
     */
    fun initSDK(merchantId: String, flowId: String, isSandbox: Boolean): Boolean

    /**
     * Launch the PhonePe checkout page.
     * @param token The order token from PhonePe Create Order API
     * @param orderId The PhonePe order ID from Create Order API
     * @return true if checkout was launched, false if SDK not initialized
     */
    fun startCheckout(token: String, orderId: String): Boolean

    /**
     * Set a callback to be invoked when SDK checkout completes.
     * The Boolean parameter is true when the SDK returned (caller should check order status).
     */
    fun setPaymentResultCallback(callback: (Boolean) -> Unit)

    /**
     * Whether the SDK has been initialized.
     */
    fun isInitialized(): Boolean
}
