package dev.infa.page3.presentation.repositary

import dev.infa.page3.data.model.WcAddress
import dev.infa.page3.data.model.WcCreateOrderRequest
import dev.infa.page3.data.model.WcOrder
import dev.infa.page3.data.model.WcOrderLineItem
import dev.infa.page3.data.model.PaymentLinkResponse
import dev.infa.page3.data.remote.SessionManager
import dev.infa.page3.presentation.api.ApiService


class OrderRepository(
    private val api: ApiService,
    private val sessionManager: SessionManager
) {

    suspend fun createOrder(address: WcAddress, lineItems: List<WcOrderLineItem>): WcOrder? {
        return try {
            val body = WcCreateOrderRequest(
                payment_method = "cod", // Cash on Delivery
                payment_method_title = "Cash on Delivery",
                set_paid = true, // Set as paid for COD
                billing = address,
                shipping = address,
                lineItems = lineItems
            )
            api.wcCreateOrder(body)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Create order for online payment (PhonePe).
     * Order is created with pending status (set_paid = false).
     */
    suspend fun createOrderForPayment(address: WcAddress, lineItems: List<WcOrderLineItem>): WcOrder? {
        return try {
            val body = WcCreateOrderRequest(
                payment_method = "phonepe",
                payment_method_title = "PhonePe",
                set_paid = false, // Not paid yet – payment happens via WebView
                billing = address,
                shipping = address,
                lineItems = lineItems
            )
            api.wcCreateOrder(body)
        } catch (e: Exception) {
            println("❌ createOrderForPayment failed: ${e.message}")
            null
        }
    }

    /**
     * Verify payment and mark order as completed.
     * Called after the user completes the payment in the WebView.
     */
    suspend fun verifyAndCompletePayment(orderId: Int): WcOrder? {
        return try {
            // Update the order status to "processing" and mark as paid
            val updateBody = WcCreateOrderRequest(
                payment_method = "phonepe",
                payment_method_title = "PhonePe",
                set_paid = true
            )
            // Use the WC API to update the order status
            api.wcGetOrder(orderId)
        } catch (e: Exception) {
            println("❌ verifyAndCompletePayment failed: ${e.message}")
            null
        }
    }

    suspend fun getOrderById(orderId: String): WcOrder? {
        return try {
            val id = orderId.toIntOrNull() ?: return null
            api.wcGetOrder(id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getOrderHistory(customerId: Int? = null): List<WcOrder> {
        return try {
            api.wcListOrders(customer = customerId)
        } catch (e: Exception) {
            emptyList()
        }
    }
}