package dev.infa.page3.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ======================== PhonePe Auth Token ========================

@Serializable
data class PhonePeAuthTokenResponse(
    @SerialName("access_token") val accessToken: String = "",
    @SerialName("expires_at") val expiresAt: Long = 0L,
    @SerialName("token_type") val tokenType: String = "",
    @SerialName("issued_at") val issuedAt: Long = 0L,
    @SerialName("session_expires_at") val sessionExpiresAt: Long = 0L
)

// ======================== PhonePe Create Order ========================

@Serializable
data class PhonePePaymentFlow(
    val type: String = "PG_CHECKOUT"
)

@Serializable
data class PhonePeCreateOrderRequest(
    val merchantOrderId: String,
    val amount: Long, // Amount in paise (INR * 100)
    val expireAfter: Int = 1200, // 20 minutes
    val paymentFlow: PhonePePaymentFlow = PhonePePaymentFlow()
)

@Serializable
data class PhonePeCreateOrderResponse(
    val orderId: String = "",
    val state: String = "",
    val expireAt: Long = 0L,
    val token: String = "",
    val redirectUrl: String = ""
) {
    /**
     * Get the checkout token for SDK.
     * Production API returns token inside redirectUrl, not as a separate field.
     * Falls back to extracting from redirectUrl query parameter.
     */
    fun getCheckoutToken(): String {
        // If token field is present, use it directly
        if (token.isNotEmpty()) return token

        // Extract token from redirectUrl query parameter
        if (redirectUrl.isNotEmpty() && redirectUrl.contains("token=")) {
            val tokenStart = redirectUrl.indexOf("token=") + 6
            val tokenEnd = redirectUrl.indexOf("&", tokenStart).let {
                if (it == -1) redirectUrl.length else it
            }
            return redirectUrl.substring(tokenStart, tokenEnd)
        }

        return ""
    }
}

// ======================== PhonePe Order Status ========================

@Serializable
data class PhonePeOrderStatusResponse(
    val orderId: String = "",
    val state: String = "", // COMPLETED, FAILED, PENDING
    val amount: Long = 0L,
    val expireAt: Long = 0L,
    val paymentDetails: List<PhonePePaymentDetail> = emptyList()
)

@Serializable
data class PhonePePaymentDetail(
    val paymentMode: String = "",
    val transactionId: String = "",
    val timestamp: Long = 0L,
    val amount: Long = 0L,
    val state: String = ""
)
