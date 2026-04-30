package dev.infa.page3.presentation.api

import dev.infa.page3.data.model.Product
import dev.infa.page3.data.model.WcCategory
import dev.infa.page3.data.model.WcCoupon
import dev.infa.page3.data.model.WcCountry
import dev.infa.page3.data.model.WcCreateOrderRequest
import dev.infa.page3.data.model.WcCurrencies
import dev.infa.page3.data.model.WcCustomer
import dev.infa.page3.data.model.WcCustomerCreateRequest
import dev.infa.page3.data.model.WcCustomerUpdateRequest
import dev.infa.page3.data.model.WcOrder
import dev.infa.page3.data.model.WcOrderActionRequest
import dev.infa.page3.data.model.WcOrderBatchRequest
import dev.infa.page3.data.model.WcOrderBatchResponse
import dev.infa.page3.data.model.WcOrderNote
import dev.infa.page3.data.model.WcOrderNoteCreateRequest
import dev.infa.page3.data.model.WcOrderNotes
import dev.infa.page3.data.model.WcOrders
import dev.infa.page3.data.model.WcPaymentGateways
import dev.infa.page3.data.model.WcProductCreateRequest
import dev.infa.page3.data.model.WcProductUpdateRequest
import dev.infa.page3.data.model.WcRefund
import dev.infa.page3.data.model.WcRefundCreateRequest
import dev.infa.page3.data.model.WcRefunds
import dev.infa.page3.data.model.WcReview
import dev.infa.page3.data.model.WcReviewCreateRequest
import dev.infa.page3.data.model.WcReviews
import dev.infa.page3.data.model.WcSettingGroups
import dev.infa.page3.data.model.WcSettingUpdateRequest
import dev.infa.page3.data.model.WcSettings
import dev.infa.page3.data.model.WcShippingMethods
import dev.infa.page3.data.model.WcShippingZones
import dev.infa.page3.data.model.WcTaxClasses
import dev.infa.page3.data.model.WcTaxRates
import dev.infa.page3.data.model.WcVariations
import dev.infa.page3.data.model.WcWebhook
import dev.infa.page3.data.model.WcWebhookRequest
import dev.infa.page3.data.model.WcWebhooks
import dev.infa.page3.data.model.WcCategories
import dev.infa.page3.data.model.WcCountries
import dev.infa.page3.data.model.WcCoupons
import dev.infa.page3.data.model.WcCustomers
import dev.infa.page3.data.model.WcProducts
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

suspend fun ApiService.wcListProducts(
    page: Int = 1,
    perPage: Int = 20,
    categoryId: Int? = null,
    search: String? = null,
): WcProducts {
    val base = "$wcApiBase/products"
    val url = applyWcQueryAuth(
        buildString {
            append(base)
            append("?page=$page&per_page=$perPage")
            categoryId?.let { append("&category=$it") }
            search?.let { append("&search=${it}") }
        }
    )

    return logApiCall(
        apiName = "wcListProducts",
        url = url,
        method = "GET",
        headers = emptyMap()
    ) {
        httpClient.get(url)
    }
}

suspend fun ApiService.wcCreateProduct(body: WcProductCreateRequest): Product {
    val url = applyWcQueryAuth("$wcApiBase/products")

    return logApiCall(
        apiName = "wcCreateProduct",
        url = url,
        method = "POST",
        headers = mapOf("Content-Type" to "application/json"),
        requestBody = body
    ) {
        httpClient.post(url) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }
}

suspend fun ApiService.wcUpdateProduct(productId: Int, body: WcProductUpdateRequest): Product {
    val url = applyWcQueryAuth("$wcApiBase/products/$productId")

    return logApiCall(
        apiName = "wcUpdateProduct",
        url = url,
        method = "PUT",
        headers = mapOf("Content-Type" to "application/json"),
        requestBody = body
    ) {
        httpClient.put(url) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }
}

suspend fun ApiService.wcDeleteProduct(productId: Int, force: Boolean = true): Product {
    val url = applyWcQueryAuth("$wcApiBase/products/$productId?force=$force")

    return logApiCall(
        apiName = "wcDeleteProduct",
        url = url,
        method = "DELETE",
        headers = emptyMap()
    ) {
        httpClient.delete(url)
    }
}

suspend fun ApiService.wcListVariations(productId: Int, page: Int = 1, perPage: Int = 50): WcVariations {
    val base = "$wcApiBase/products/$productId/variations"
    val url = applyWcQueryAuth("$base?page=$page&per_page=$perPage")

    return logApiCall(
        apiName = "wcListVariations",
        url = url,
        method = "GET",
        headers = emptyMap()
    ) {
        httpClient.get(url)
    }
}

suspend fun ApiService.wcListCategories(
    page: Int = 1,
    perPage: Int = 100,
    parent: Int? = 0
): WcCategories {
    val base = "$wcApiBase/products/categories"
    val url = applyWcQueryAuth(buildString {
        append(base)
        append("?page=$page&per_page=$perPage")
        parent?.let { append("&parent=$it") }
    })

    return logApiCall(
        apiName = "wcListCategories",
        url = url,
        method = "GET",
        headers = emptyMap()
    ) {
        httpClient.get(url)
    }
}

// ======================= WooCommerce: CATEGORIES CRUD =======================
suspend fun ApiService.wcCreateCategory(body: WcCategory): WcCategory {
    val url = applyWcQueryAuth("$wcApiBase/products/categories")

    return logApiCall(
        apiName = "wcCreateCategory",
        url = url,
        method = "POST",
        headers = mapOf("Content-Type" to "application/json"),
        requestBody = body
    ) {
        httpClient.post(url) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }
}

suspend fun ApiService.wcUpdateCategory(categoryId: Int, body: WcCategory): WcCategory {
    val url = applyWcQueryAuth("$wcApiBase/products/categories/$categoryId")

    return logApiCall(
        apiName = "wcUpdateCategory",
        url = url,
        method = "PUT",
        headers = mapOf("Content-Type" to "application/json"),
        requestBody = body
    ) {
        httpClient.put(url) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }
}

suspend fun ApiService.wcDeleteCategory(categoryId: Int, force: Boolean = true): WcCategory {
    val url = applyWcQueryAuth("$wcApiBase/products/categories/$categoryId?force=$force")

    return logApiCall(
        apiName = "wcDeleteCategory",
        url = url,
        method = "DELETE",
        headers = emptyMap()
    ) {
        httpClient.delete(url)
    }
}

/** Fetches one WooCommerce product; response includes `images[]` (full gallery: `src`, `thumbnail`, etc.). */
suspend fun ApiService.wcGetProduct(productId: Int): Product {
    val url = applyWcQueryAuth("$wcApiBase/products/$productId")

    return logApiCall(
        apiName = "wcGetProduct",
        url = url,
        method = "GET",
        headers = emptyMap()
    ) {
        httpClient.get(url)
    }
}

// ======================= WooCommerce: REVIEWS =======================
suspend fun ApiService.wcListReviews(productId: Int, page: Int = 1, perPage: Int = 20): WcReviews {
    val base = "$wcApiBase/products/reviews"
    val url = applyWcQueryAuth("$base?page=$page&per_page=$perPage&product=$productId")

    return logApiCall(
        apiName = "wcListReviews",
        url = url,
        method = "GET",
        headers = emptyMap()
    ) {
        httpClient.get(url)
    }
}

suspend fun ApiService.wcCreateReview(body: WcReviewCreateRequest): WcReview {
    val url = applyWcQueryAuth("$wcApiBase/products/reviews")

    return logApiCall(
        apiName = "wcCreateReview",
        url = url,
        method = "POST",
        headers = mapOf("Content-Type" to "application/json"),
        requestBody = body
    ) {
        httpClient.post(url) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }
}

// ======================= WooCommerce: CUSTOMERS =======================
suspend fun ApiService.wcListCustomers(
    page: Int = 1,
    perPage: Int = 20,
    search: String? = null
): WcCustomers {
    val base = "$wcApiBase/customers"
    val url = applyWcQueryAuth(
        buildString {
            append(base)
            append("?page=$page&per_page=$perPage")
            search?.let { append("&search=${it}") }
        }
    )

    return logApiCall(
        apiName = "wcListCustomers",
        url = url,
        method = "GET",
        headers = emptyMap()
    ) {
        httpClient.get(url)
    }
}

suspend fun ApiService.wcCreateCustomer(body: WcCustomerCreateRequest): WcCustomer {
    val url = applyWcQueryAuth("$wcApiBase/customers")

    return logApiCall(
        apiName = "wcCreateCustomer",
        url = url,
        method = "POST",
        headers = mapOf("Content-Type" to "application/json"),
        requestBody = body
    ) {
        httpClient.post(url) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }
}

suspend fun ApiService.wcUpdateCustomer(customerId: Int, body: WcCustomerUpdateRequest): WcCustomer {
    val url = applyWcQueryAuth("$wcApiBase/customers/$customerId")

    return logApiCall(
        apiName = "wcUpdateCustomer",
        url = url,
        method = "PUT",
        headers = mapOf("Content-Type" to "application/json"),
        requestBody = body
    ) {
        httpClient.put(url) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }
}

suspend fun ApiService.wcDeleteCustomer(customerId: Int, force: Boolean = true): WcCustomer {
    val url = applyWcQueryAuth("$wcApiBase/customers/$customerId?force=$force")

    return logApiCall(
        apiName = "wcDeleteCustomer",
        url = url,
        method = "DELETE",
        headers = emptyMap()
    ) {
        httpClient.delete(url)
    }
}

// ======================= WooCommerce: ORDERS =======================
suspend fun ApiService.wcListOrders(
    page: Int = 1,
    perPage: Int = 20,
    status: String? = null,
    customer: Int? = null,
    search: String? = null,
    after: String? = null
): WcOrders {
    val base = "$wcApiBase/orders"
    val url = applyWcQueryAuth(buildString {
        append(base)
        append("?page=$page&per_page=$perPage")
        status?.let { append("&status=$it") }
        customer?.let { append("&customer=$it") }
        search?.let { append("&search=$it") }
        after?.let { append("&after=$it") }
    })

    return logApiCall(
        apiName = "wcListOrders",
        url = url,
        method = "GET",
        headers = emptyMap()
    ) {
        httpClient.get(url)
    }
}

suspend fun ApiService.wcCreateOrder(body: WcCreateOrderRequest): WcOrder {
    val url = applyWcQueryAuth("$wcApiBase/orders")

    return logApiCall(
        apiName = "wcCreateOrder",
        url = url,
        method = "POST",
        headers = mapOf("Content-Type" to "application/json"),
        requestBody = body
    ) {
        httpClient.post(url) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }
}

suspend fun ApiService.wcGetOrder(orderId: Int): WcOrder {
    val url = applyWcQueryAuth("$wcApiBase/orders/$orderId")

    return logApiCall(
        apiName = "wcGetOrder",
        url = url,
        method = "GET",
        headers = emptyMap()
    ) {
        httpClient.get(url)
    }
}

/**
 * Update an existing WooCommerce Order.
 * PUT /orders/{id}
 * Used to update order status, payment info, etc. after payment.
 */
suspend fun ApiService.wcUpdateOrder(orderId: Int, body: WcCreateOrderRequest): WcOrder {
    val url = applyWcQueryAuth("$wcApiBase/orders/$orderId")

    return logApiCall(
        apiName = "wcUpdateOrder",
        url = url,
        method = "PUT",
        headers = mapOf("Content-Type" to "application/json"),
        requestBody = body
    ) {
        httpClient.put(url) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }
}

suspend fun ApiService.wcDeleteOrder(orderId: Int, force: Boolean = true): WcOrder {
    val url = applyWcQueryAuth("$wcApiBase/orders/$orderId?force=$force")

    return logApiCall(
        apiName = "wcDeleteOrder",
        url = url,
        method = "DELETE",
        headers = emptyMap()
    ) {
        httpClient.delete(url)
    }
}

suspend fun ApiService.wcBatchOrders(body: WcOrderBatchRequest): WcOrderBatchResponse {
    val url = applyWcQueryAuth("$wcApiBase/orders/batch")

    return logApiCall(
        apiName = "wcBatchOrders",
        url = url,
        method = "POST",
        headers = mapOf("Content-Type" to "application/json"),
        requestBody = body
    ) {
        httpClient.post(url) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }
}

suspend fun ApiService.wcOrderAction(orderId: Int, action: String): WcOrder {
    val url = applyWcQueryAuth("$wcApiBase/orders/$orderId/actions")
    val body = WcOrderActionRequest(action = action)

    return logApiCall(
        apiName = "wcOrderAction",
        url = url,
        method = "POST",
        headers = mapOf("Content-Type" to "application/json"),
        requestBody = body
    ) {
        httpClient.post(url) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }
}

suspend fun ApiService.wcListOrderNotes(orderId: Int): WcOrderNotes {
    val url = applyWcQueryAuth("$wcApiBase/orders/$orderId/notes")

    return logApiCall(
        apiName = "wcListOrderNotes",
        url = url,
        method = "GET",
        headers = emptyMap()
    ) {
        httpClient.get(url)
    }
}

suspend fun ApiService.wcCreateOrderNote(orderId: Int, body: WcOrderNoteCreateRequest): WcOrderNote {
    val url = applyWcQueryAuth("$wcApiBase/orders/$orderId/notes")

    return logApiCall(
        apiName = "wcCreateOrderNote",
        url = url,
        method = "POST",
        headers = mapOf("Content-Type" to "application/json"),
        requestBody = body
    ) {
        httpClient.post(url) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }
}

suspend fun ApiService.wcDeleteOrderNote(orderId: Int, noteId: Int, force: Boolean = true): WcOrderNote {
    val url = applyWcQueryAuth("$wcApiBase/orders/$orderId/notes/$noteId?force=$force")

    return logApiCall(
        apiName = "wcDeleteOrderNote",
        url = url,
        method = "DELETE",
        headers = emptyMap()
    ) {
        httpClient.delete(url)
    }
}

// Refunds
suspend fun ApiService.wcListRefunds(orderId: Int): WcRefunds {
    val url = applyWcQueryAuth("$wcApiBase/orders/$orderId/refunds")

    return logApiCall(
        apiName = "wcListRefunds",
        url = url,
        method = "GET",
        headers = emptyMap()
    ) {
        httpClient.get(url)
    }
}

suspend fun ApiService.wcCreateRefund(orderId: Int, body: WcRefundCreateRequest): WcRefund {
    val url = applyWcQueryAuth("$wcApiBase/orders/$orderId/refunds")

    return logApiCall(
        apiName = "wcCreateRefund",
        url = url,
        method = "POST",
        headers = mapOf("Content-Type" to "application/json"),
        requestBody = body
    ) {
        httpClient.post(url) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }
}

/**
 * Delete a specific order refund.
 * DELETE /orders/{order_id}/refunds/{refund_id}?force=true
 */
suspend fun ApiService.wcDeleteRefund(
    orderId: Int,
    refundId: Int,
    force: Boolean = true
): WcRefund {
    val url = applyWcQueryAuth("$wcApiBase/orders/$orderId/refunds/$refundId?force=$force")

    return logApiCall(
        apiName = "wcDeleteRefund",
        url = url,
        method = "DELETE",
        headers = emptyMap()
    ) {
        httpClient.delete(url)
    }
}

// ======================= WooCommerce: COUPONS and CURRENCIES =======================
suspend fun ApiService.wcListCoupons(page: Int = 1, perPage: Int = 20): WcCoupons {
    val base = "$wcApiBase/coupons"
    val url = applyWcQueryAuth("$base?page=$page&per_page=$perPage")

    return logApiCall(
        apiName = "wcListCoupons",
        url = url,
        method = "GET",
        headers = emptyMap()
    ) {
        httpClient.get(url)
    }
}

// Taxes
suspend fun ApiService.wcListTaxRates(page: Int = 1, perPage: Int = 100): WcTaxRates {
    val base = "$wcApiBase/taxes"
    val url = applyWcQueryAuth("$base?page=$page&per_page=$perPage")

    return logApiCall(
        apiName = "wcListTaxRates",
        url = url,
        method = "GET",
        headers = emptyMap()
    ) {
        httpClient.get(url)
    }
}

suspend fun ApiService.wcListTaxClasses(): WcTaxClasses {
    val url = applyWcQueryAuth("$wcApiBase/taxes/classes")

    return logApiCall(
        apiName = "wcListTaxClasses",
        url = url,
        method = "GET",
        headers = emptyMap()
    ) {
        httpClient.get(url)
    }
}

// Shipping
suspend fun ApiService.wcListShippingZones(): WcShippingZones {
    val url = applyWcQueryAuth("$wcApiBase/shipping/zones")

    return logApiCall(
        apiName = "wcListShippingZones",
        url = url,
        method = "GET",
        headers = emptyMap()
    ) {
        httpClient.get(url)
    }
}

suspend fun ApiService.wcListShippingZoneMethods(zoneId: Int): WcShippingMethods {
    val url = applyWcQueryAuth("$wcApiBase/shipping/zones/$zoneId/methods")

    return logApiCall(
        apiName = "wcListShippingZoneMethods",
        url = url,
        method = "GET",
        headers = emptyMap()
    ) {
        httpClient.get(url)
    }
}

suspend fun ApiService.wcListCurrencies(): WcCurrencies {
    val url = applyWcQueryAuth("$wcApiBase/data/currencies")

    return logApiCall(
        apiName = "wcListCurrencies",
        url = url,
        method = "GET",
        headers = emptyMap()
    ) {
        httpClient.get(url)
    }
}

// Settings
suspend fun ApiService.wcListSettingGroups(): WcSettingGroups {
    val url = applyWcQueryAuth("$wcApiBase/settings")

    return logApiCall(
        apiName = "wcListSettingGroups",
        url = url,
        method = "GET",
        headers = emptyMap()
    ) { httpClient.get(url) }
}

suspend fun ApiService.wcListSettings(groupId: String): WcSettings {
    val url = applyWcQueryAuth("$wcApiBase/settings/$groupId")

    return logApiCall(
        apiName = "wcListSettings",
        url = url,
        method = "GET",
        headers = emptyMap()
    ) { httpClient.get(url) }
}

suspend fun ApiService.wcUpdateSettings(
    groupId: String,
    updates: List<WcSettingUpdateRequest>
): WcSettings {
    val url = applyWcQueryAuth("$wcApiBase/settings/$groupId/batch")
    val body = mapOf("update" to updates)

    return logApiCall(
        apiName = "wcUpdateSettings",
        url = url,
        method = "POST",
        headers = mapOf("Content-Type" to "application/json"),
        requestBody = body
    ) {
        httpClient.post(url) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }
}

// Webhooks
suspend fun ApiService.wcListWebhooks(): WcWebhooks {
    val url = applyWcQueryAuth("$wcApiBase/webhooks")

    return logApiCall(
        apiName = "wcListWebhooks",
        url = url,
        method = "GET",
        headers = emptyMap()
    ) { httpClient.get(url) }
}

suspend fun ApiService.wcCreateWebhook(body: WcWebhookRequest): WcWebhook {
    val url = applyWcQueryAuth("$wcApiBase/webhooks")

    return logApiCall(
        apiName = "wcCreateWebhook",
        url = url,
        method = "POST",
        headers = mapOf("Content-Type" to "application/json"),
        requestBody = body
    ) {
        httpClient.post(url) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }
}

suspend fun ApiService.wcDeleteWebhook(webhookId: Int): WcWebhook {
    val url = applyWcQueryAuth("$wcApiBase/webhooks/$webhookId")

    return logApiCall(
        apiName = "wcDeleteWebhook",
        url = url,
        method = "DELETE",
        headers = emptyMap()
    ) { httpClient.delete(url) }
}

// Payment gateways
suspend fun ApiService.wcListPaymentGateways(): WcPaymentGateways {
    val url = applyWcQueryAuth("$wcApiBase/payment_gateways")

    return logApiCall(
        apiName = "wcListPaymentGateways",
        url = url,
        method = "GET",
        headers = emptyMap()
    ) { httpClient.get(url) }
}

// Data endpoints (countries)
suspend fun ApiService.wcListCountries(): WcCountries {
    val url = applyWcQueryAuth("$wcApiBase/data/countries")

    return logApiCall(
        apiName = "wcListCountries",
        url = url,
        method = "GET",
        headers = emptyMap()
    ) { httpClient.get(url) }
}

suspend fun ApiService.wcGetCountry(code: String): WcCountry {
    val url = applyWcQueryAuth("$wcApiBase/data/countries/$code")

    return logApiCall(
        apiName = "wcGetCountry",
        url = url,
        method = "GET",
        headers = emptyMap()
    ) { httpClient.get(url) }
}
