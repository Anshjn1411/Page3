package dev.infa.page3.presentation.repositary

import dev.infa.page3.data.model.WcAddress
import dev.infa.page3.data.model.WcCreateOrderRequest
import dev.infa.page3.data.model.WcOrder
import dev.infa.page3.data.model.WcOrderLineItem
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