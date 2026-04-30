package dev.infa.page3.presentation.repositary

import dev.infa.page3.data.model.WcAddress
import dev.infa.page3.data.model.WcCreateOrderRequest
import dev.infa.page3.data.model.WcOrder
import dev.infa.page3.data.model.WcOrderLineItem
import dev.infa.page3.data.model.PhonePeAuthTokenResponse
import dev.infa.page3.data.model.PhonePeCreateOrderRequest
import dev.infa.page3.data.model.PhonePeCreateOrderResponse
import dev.infa.page3.data.model.PhonePeOrderStatusResponse
import dev.infa.page3.data.model.WcOrderBatchRequest
import dev.infa.page3.data.model.WcOrderBatchResponse
import dev.infa.page3.data.remote.SessionManager
import dev.infa.page3.presentation.api.*


class OrderRepository(
    private val api: ApiService,
    private val sessionManager: SessionManager
) {

    // Cached PhonePe auth token
    private var cachedAuthToken: String? = null
    private var tokenExpiresAt: Long = 0L

    // ======================== COD Order ========================

    /**
     * Create order for Cash on Delivery.
     * Order is created with set_paid = true (COD is considered paid on creation).
     */
    suspend fun createCodOrder(
        address: WcAddress,
        lineItems: List<WcOrderLineItem>,
        customerId: Int? = null,
        customerNote: String? = null
    ): WcOrder? {
        return try {
            val body = WcCreateOrderRequest(
                payment_method = "cod",
                payment_method_title = "Cash on Delivery",
                set_paid = true,
                status = "processing",
                customerId = customerId,
                customerNote = customerNote,
                billing = address,
                shipping = address,
                lineItems = lineItems
            )
            api.wcCreateOrder(body)
        } catch (e: Exception) {
            println("❌ createCodOrder failed: ${e.message}")
            null
        }
    }

    // ======================== PhonePe Payment – Order Created ONLY on Success ========================

    /**
     * Create WooCommerce order AFTER payment succeeds.
     * This is called only when PhonePe payment is confirmed as COMPLETED.
     * The order is created with set_paid = true and status = "processing".
     */
    suspend fun createPaidOrder(
        address: WcAddress,
        lineItems: List<WcOrderLineItem>,
        transactionId: String,
        customerId: Int? = null
    ): WcOrder? {
        return try {
            val body = WcCreateOrderRequest(
                payment_method = "phonepe",
                payment_method_title = "PhonePe",
                set_paid = true,
                status = "processing",
                customerId = customerId,
                customerNote = "PhonePe transaction id: $transactionId",
                billing = address,
                shipping = address,
                lineItems = lineItems
            )
            val order = api.wcCreateOrder(body)
            println("✅ Paid order created: #${order.number} (ID: ${order.id}), txn: $transactionId")
            order
        } catch (e: Exception) {
            println("❌ createPaidOrder failed: ${e.message}")
            null
        }
    }

    /**
     * Update an existing WooCommerce order status.
     * Used to mark an order as paid/processing after payment success,
     * or to cancel it on payment failure.
     */
    suspend fun updateOrderStatus(orderId: Int, status: String, setPaid: Boolean): WcOrder? {
        return try {
            val updateBody = WcCreateOrderRequest(
                payment_method = "phonepe",
                payment_method_title = "PhonePe",
                set_paid = setPaid,
                status = status
            )
            val order = api.wcUpdateOrder(orderId, updateBody)
            println("✅ Order #$orderId updated → status: $status, paid: $setPaid")
            order
        } catch (e: Exception) {
            println("❌ updateOrderStatus failed for #$orderId: ${e.message}")
            null
        }
    }

    /**
     * Delete/cancel a WooCommerce order.
     * Called when payment fails and we need to clean up a pending order.
     */
    suspend fun cancelOrder(orderId: Int): Boolean {
        return try {
            // First try to update status to cancelled
            updateOrderStatus(orderId, "cancelled", false)
            println("✅ Order #$orderId cancelled")
            true
        } catch (e: Exception) {
            println("❌ cancelOrder failed for #$orderId: ${e.message}")
            false
        }
    }

    // ======================== PhonePe SDK API Methods ========================

    /**
     * Get a valid PhonePe auth token.
     * Returns cached token if available, else fetches a new one.
     */
    suspend fun getPhonePeAuthToken(
        clientId: String,
        clientSecret: String,
        clientVersion: String
    ): PhonePeAuthTokenResponse? {
        // Return cached token if available
        if (cachedAuthToken != null && tokenExpiresAt > 0) {
            return PhonePeAuthTokenResponse(
                accessToken = cachedAuthToken!!,
                expiresAt = tokenExpiresAt
            )
        }

        return try {
            val response = api.phonePeGetAuthToken(clientId, clientSecret, clientVersion)
            // Validate response — API may return 401/error with empty token
            if (response.accessToken.isEmpty()) {
                println("❌ PhonePe auth token is empty — likely invalid credentials or wrong environment")
                cachedAuthToken = null
                tokenExpiresAt = 0L
                return null
            }
            cachedAuthToken = response.accessToken
            tokenExpiresAt = response.expiresAt
            println("✅ PhonePe auth token fetched, expires at: ${response.expiresAt}")
            response
        } catch (e: Exception) {
            println("❌ PhonePe getAuthToken failed: ${e.message}")
            cachedAuthToken = null
            tokenExpiresAt = 0L
            null
        }
    }

    /**
     * Invalidate cached auth token (e.g., on auth failure).
     */
    fun invalidateAuthToken() {
        cachedAuthToken = null
        tokenExpiresAt = 0L
    }

    /**
     * Create a PhonePe payment order.
     * Returns the order with token needed for SDK checkout.
     */
    suspend fun createPhonePeOrder(
        authToken: String,
        merchantOrderId: String,
        amountInPaise: Long
    ): PhonePeCreateOrderResponse? {
        return try {
            val request = PhonePeCreateOrderRequest(
                merchantOrderId = merchantOrderId,
                amount = amountInPaise
            )
            val response = api.phonePeCreateOrder(authToken, request)
            println("✅ PhonePe order created: ${response.orderId}, state: ${response.state}")
            response
        } catch (e: Exception) {
            println("❌ PhonePe createOrder failed: ${e.message}")
            null
        }
    }

    /**
     * Check the status of a PhonePe payment.
     * Returns order status: COMPLETED, FAILED, PENDING, etc.
     */
    suspend fun checkPhonePeOrderStatus(
        authToken: String,
        merchantOrderId: String
    ): PhonePeOrderStatusResponse? {
        return try {
            val response = api.phonePeCheckOrderStatus(authToken, merchantOrderId)
            println("✅ PhonePe order status: ${response.state} for merchantOrderId: $merchantOrderId")
            response
        } catch (e: Exception) {
            println("❌ PhonePe checkOrderStatus failed: ${e.message}")
            null
        }
    }

    // ======================== General Order Methods ========================

    suspend fun getOrderById(orderId: String): WcOrder? {
        return try {
            val id = orderId.toIntOrNull() ?: return null
            api.wcGetOrder(id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getOrderByIdAndEmail(orderId: String, email: String): WcOrder? {
        val order = getOrderById(orderId) ?: return null
        val billingEmail = order.billing?.email?.trim()?.lowercase()
        return if (billingEmail == email.trim().lowercase()) order else null
    }

    suspend fun getOrderHistory(
        customerId: Int? = null,
        email: String? = null,
        status: String? = null
    ): List<WcOrder> {
        return try {
            val raw = api.wcListOrders(customer = customerId, status = status, search = email)
            if (email.isNullOrBlank()) raw
            else raw.filter { it.billing?.email?.trim()?.equals(email.trim(), ignoreCase = true) == true }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateOrder(orderId: Int, status: String, customerNote: String? = null): WcOrder? {
        return try {
            val body = WcCreateOrderRequest(
                payment_method = "cod",
                payment_method_title = "Cash on Delivery",
                set_paid = false,
                status = status,
                customerNote = customerNote
            )
            api.wcUpdateOrder(orderId, body)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun deleteOrder(orderId: Int, force: Boolean = false): WcOrder? {
        return try {
            api.wcDeleteOrder(orderId = orderId, force = force)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun refundOrder(orderId: Int, amount: String, reason: String?): Boolean {
        return try {
            api.wcCreateRefund(orderId, dev.infa.page3.data.model.WcRefundCreateRequest(amount = amount, reason = reason, refund_payment = true))
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun listRefunds(orderId: Int) = try {
        api.wcListRefunds(orderId)
    } catch (_: Exception) {
        emptyList()
    }

    suspend fun deleteRefund(
        orderId: Int,
        refundId: Int,
        force: Boolean = true
    ): Boolean {
        return try {
            api.wcDeleteRefund(orderId = orderId, refundId = refundId, force = force)
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun runOrderAction(orderId: Int, action: String): WcOrder? {
        return try {
            api.wcOrderAction(orderId, action)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun addOrderNote(orderId: Int, note: String, customerNote: Boolean = true): Boolean {
        return try {
            api.wcCreateOrderNote(orderId, dev.infa.page3.data.model.WcOrderNoteCreateRequest(note = note, customerNote = customerNote))
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun listOrderNotes(orderId: Int) = try {
        api.wcListOrderNotes(orderId)
    } catch (_: Exception) {
        emptyList()
    }

    suspend fun deleteOrderNote(orderId: Int, noteId: Int, force: Boolean = true): Boolean {
        return try {
            api.wcDeleteOrderNote(orderId, noteId, force)
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun batchOrders(body: WcOrderBatchRequest): WcOrderBatchResponse? {
        return try {
            api.wcBatchOrders(body)
        } catch (_: Exception) {
            null
        }
    }
}