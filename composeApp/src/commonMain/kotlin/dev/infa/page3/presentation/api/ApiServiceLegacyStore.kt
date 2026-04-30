package dev.infa.page3.presentation.api

import dev.infa.page3.data.model.AddToCartRequest
import dev.infa.page3.data.model.AddToCartResponse
import dev.infa.page3.data.model.Address
import dev.infa.page3.data.model.AddressDeleteResponse
import dev.infa.page3.data.model.AddressDetail
import dev.infa.page3.data.model.AddressRequest
import dev.infa.page3.data.model.AddressResponse
import dev.infa.page3.data.model.Cart
import dev.infa.page3.data.model.CategoryRequest
import dev.infa.page3.data.model.CreateCategoryResponse
import dev.infa.page3.data.model.CreateSubCategoryResponse
import dev.infa.page3.data.model.CreateMultipleProductsResponse
import dev.infa.page3.data.model.CreateOrderRequest
import dev.infa.page3.data.model.CreateProductResponse
import dev.infa.page3.data.model.CreateRatingResponse
import dev.infa.page3.data.model.CreateReviewResponse
import dev.infa.page3.data.model.DeleteCategoryResponse
import dev.infa.page3.data.model.DeleteProductResponse
import dev.infa.page3.data.model.DeleteSubCategoryResponse
import dev.infa.page3.data.model.GetAllProductsResponse
import dev.infa.page3.data.model.GetAllSubCategoriesResponse
import dev.infa.page3.data.model.GetCategoryByIdResponse
import dev.infa.page3.data.model.GetProductRatingsResponse
import dev.infa.page3.data.model.GetProductsByCategoryResponse
import dev.infa.page3.data.model.GetProductsBySubCategoryResponse
import dev.infa.page3.data.model.GetSubCategoryByIdResponse
import dev.infa.page3.data.model.MultipleProductsRequest
import dev.infa.page3.data.model.Order
import dev.infa.page3.data.model.OrderDetailed
import dev.infa.page3.data.model.PaymentLinkResponse
import dev.infa.page3.data.model.ProductQueryParams
import dev.infa.page3.data.model.ProductRequest
import dev.infa.page3.data.model.RatingRequest
import dev.infa.page3.data.model.ReviewRequest
import dev.infa.page3.data.model.SearchProductsResponse
import dev.infa.page3.data.model.SearchQueryParams
import dev.infa.page3.data.model.SubCategoryRequest
import dev.infa.page3.data.model.UpdateCategoryResponse
import dev.infa.page3.data.model.UpdateProductResponse
import dev.infa.page3.data.model.UpdateSubCategoryResponse
import dev.infa.page3.data.model.WishlistResponse
import dev.infa.page3.data.model.GetAllReviewsResponse
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.header
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

suspend fun ApiService.deleteUser(id: String, token: String) {
    val url = "$baseUrl/api/users/$id"
    val headers = mapOf("Authorization" to "Bearer $token")

    logApiCallUnit(
        apiName = "deleteUser",
        url = url,
        method = "DELETE",
        headers = headers
    ) {
        httpClient.delete(url) {
            header("Authorization", "Bearer $token")
        }
    }
}

suspend fun ApiService.createCategory(categoryRequest: CategoryRequest): CreateCategoryResponse {
    val url = "$baseUrl/categories"

    return logApiCall(
        apiName = "createCategory",
        url = url,
        method = "POST",
        headers = mapOf("Content-Type" to "application/json"),
        requestBody = categoryRequest
    ) {
        httpClient.post(url) {
            contentType(ContentType.Application.Json)
            setBody(categoryRequest)
        }
    }
}

suspend fun ApiService.createSubCategory(subCategoryRequest: SubCategoryRequest): CreateSubCategoryResponse {
    val url = "$baseUrl/subcategories"

    return logApiCall(
        apiName = "createSubCategory",
        url = url,
        method = "POST",
        headers = mapOf("Content-Type" to "application/json"),
        requestBody = subCategoryRequest
    ) {
        httpClient.post(url) {
            contentType(ContentType.Application.Json)
            setBody(subCategoryRequest)
        }
    }
}

suspend fun ApiService.getAllSubCategories(): GetAllSubCategoriesResponse {
    val url = "$baseUrl/subcategories"

    return logApiCall(
        apiName = "getAllSubCategories",
        url = url,
        method = "GET",
        headers = emptyMap()
    ) {
        httpClient.get(url)
    }
}

suspend fun ApiService.getSubCategoryById(subCategoryId: String): GetSubCategoryByIdResponse {
    val url = "$baseUrl/subcategories/$subCategoryId"

    return logApiCall(
        apiName = "getSubCategoryById",
        url = url,
        method = "GET",
        headers = emptyMap()
    ) {
        httpClient.get(url)
    }
}

suspend fun ApiService.getSubCategoriesByCategory(categoryId: String): GetAllSubCategoriesResponse {
    val url = "$baseUrl/subcategories/category/$categoryId"

    return logApiCall(
        apiName = "getSubCategoriesByCategory",
        url = url,
        method = "GET",
        headers = emptyMap()
    ) {
        httpClient.get(url)
    }
}

suspend fun ApiService.updateSubCategory(
    subCategoryId: String,
    subCategoryRequest: SubCategoryRequest
): UpdateSubCategoryResponse {
    val url = "$baseUrl/subcategories/$subCategoryId"

    return logApiCall(
        apiName = "updateSubCategory",
        url = url,
        method = "PUT",
        headers = mapOf("Content-Type" to "application/json"),
        requestBody = subCategoryRequest
    ) {
        httpClient.put(url) {
            contentType(ContentType.Application.Json)
            setBody(subCategoryRequest)
        }
    }
}

suspend fun ApiService.deleteSubCategory(subCategoryId: String): DeleteSubCategoryResponse {
    val url = "$baseUrl/subcategories/$subCategoryId"

    return logApiCall(
        apiName = "deleteSubCategory",
        url = url,
        method = "DELETE",
        headers = emptyMap()
    ) {
        httpClient.delete(url)
    }
}

suspend fun ApiService.updateProduct(
    productId: String,
    productRequest: ProductRequest,
    authToken: String? = null
): UpdateProductResponse {
    val url = "$baseUrl/api/products/$productId"
    val headers = mutableMapOf("Content-Type" to "application/json")
    authToken?.let { headers["Authorization"] = "Bearer $it" }

    return logApiCall(
        apiName = "updateProduct",
        url = url,
        method = "PUT",
        headers = headers,
        requestBody = productRequest
    ) {
        httpClient.put(url) {
            contentType(ContentType.Application.Json)
            authToken?.let { bearerAuth(it) }
            setBody(productRequest)
        }
    }
}

suspend fun ApiService.deleteProduct(
    productId: String,
    authToken: String? = null
): DeleteProductResponse {
    val url = "$baseUrl/api/products/$productId"
    val headers = mutableMapOf<String, String>()
    authToken?.let { headers["Authorization"] = "Bearer $it" }

    return logApiCall(
        apiName = "deleteProduct",
        url = url,
        method = "DELETE",
        headers = headers
    ) {
        httpClient.delete(url) {
            authToken?.let { bearerAuth(it) }
        }
    }
}

// ======================= LEGACY BELOW (kept temporarily) =======================

suspend fun ApiService.getProductRatings(
    productId: String,
    authToken: String? = null
): GetProductRatingsResponse {
    val url = "$baseUrl/api/ratings/product/$productId"
    val headers = mutableMapOf<String, String>()
    authToken?.let { headers["Authorization"] = "Bearer $it" }

    return logApiCall(
        apiName = "getProductRatings",
        url = url,
        method = "GET",
        headers = headers
    ) {
        httpClient.get(url) {
            authToken?.let { bearerAuth(it) }
        }
    }
}

// ======================= REVIEW API ENDPOINTS =======================

// REPLACE these functions in ApiService:
suspend fun ApiService.createRating(
    ratingRequest: RatingRequest,
    authToken: String
): CreateRatingResponse {
    val url = "$baseUrl/api/ratings/create"  // Changed: added /create

    return logApiCall(
        apiName = "createRating",
        url = url,
        method = "POST",
        headers = mapOf(
            "Content-Type" to "application/json",
            "Authorization" to "Bearer $authToken"
        ),
        requestBody = ratingRequest
    ) {
        httpClient.post(url) {
            contentType(ContentType.Application.Json)
            bearerAuth(authToken)
            setBody(ratingRequest)
        }
    }
}

suspend fun ApiService.createReview(
    reviewRequest: ReviewRequest,
    authToken: String
): CreateReviewResponse {
    val url = "$baseUrl/api/reviews/create"  // Changed: added /create

    return logApiCall(
        apiName = "createReview",
        url = url,
        method = "POST",
        headers = mapOf(
            "Content-Type" to "application/json",
            "Authorization" to "Bearer $authToken"
        ),
        requestBody = reviewRequest
    ) {
        httpClient.post(url) {
            contentType(ContentType.Application.Json)
            bearerAuth(authToken)
            setBody(reviewRequest)
        }
    }
}

suspend fun ApiService.getAllReviews(
    productId: String,
    authToken: String? = null
): GetAllReviewsResponse {
    val url = "$baseUrl/api/reviews/product/$productId"
    val headers = mutableMapOf<String, String>()
    authToken?.let { headers["Authorization"] = "Bearer $it" }

    return logApiCall(
        apiName = "getAllReviews",
        url = url,
        method = "GET",
        headers = headers
    ) {
        httpClient.get(url) {
            authToken?.let { bearerAuth(it) }
        }
    }
}
// ============= CART API FUNCTIONS =============

suspend fun ApiService.getUserCart(authToken: String): Cart {
    val url = "$baseUrl/api/cart"
    val headers = mapOf("Authorization" to "Bearer $authToken")

    return logApiCall(
        apiName = "getUserCart",
        url = url,
        method = "GET",
        headers = headers
    ) {
        httpClient.get(url) {
            bearerAuth(authToken)
        }
    }
}

suspend fun ApiService.addItemToCart(
    request: AddToCartRequest,
    authToken: String
): AddToCartResponse {
    val url = "$baseUrl/api/cart/add"
    val headers = mapOf(
        "Authorization" to "Bearer $authToken",
        "Content-Type" to "application/json"
    )

    return logApiCall(
        apiName = "addItemToCart",
        url = url,
        method = "PUT",
        headers = headers,
    ) {
        httpClient.put(url) {
            bearerAuth(authToken)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}

// ============= WISHLIST API FUNCTIONS =============

suspend fun ApiService.getWishlist(
    authToken: String,
    sortBy: String? = null
): WishlistResponse {
    val url = buildString {
        append("$baseUrl/api/wishlist")
        if (sortBy != null) {
            append("?sortBy=$sortBy")
        }
    }
    val headers = mapOf("Authorization" to "Bearer $authToken")

    return logApiCall(
        apiName = "getWishlist",
        url = url,
        method = "GET",
        headers = headers
    ) {
        httpClient.get(url) {
            bearerAuth(authToken)
        }
    }
}

suspend fun ApiService.addToWishlist(
    productId: String,
    authToken: String
): WishlistResponse {
    val url = "$baseUrl/api/wishlist/add/$productId"
    val headers = mapOf("Authorization" to "Bearer $authToken")

    return logApiCall(
        apiName = "addToWishlist",
        url = url,
        method = "POST",
        headers = headers
    ) {
        httpClient.post(url) {
            bearerAuth(authToken)
        }
    }
}

suspend fun ApiService.removeFromWishlist(
    productId: String,
    authToken: String
): WishlistResponse {
    val url = "$baseUrl/api/wishlist/remove/$productId"
    val headers = mapOf("Authorization" to "Bearer $authToken")


    return logApiCall(
        apiName = "removeFromWishlist",
        url = url,
        method = "DELETE",
        headers = headers
    ) {
        httpClient.delete(url) {
            bearerAuth(authToken)
        }
    }
}


suspend fun ApiService.createOrder(
    orderRequest: CreateOrderRequest,
    authToken: String
): Order {
    val url = "$baseUrl/api/orders/"
    val headers = mapOf("Authorization" to "Bearer $authToken")

    return logApiCall(
        apiName = "createOrder",
        url = url,
        method = "POST",
        headers = headers
    ) {
        httpClient.post(url) {
            bearerAuth(authToken)
            contentType(ContentType.Application.Json)
            setBody(orderRequest)
        }
    }
}

suspend fun ApiService.getOrderById(
    orderId: String,
    authToken: String
): OrderDetailed {
    val url = "$baseUrl/api/orders/$orderId"
    val headers = mapOf("Authorization" to "Bearer $authToken")

    return logApiCall(
        apiName = "getOrderById",
        url = url,
        method = "GET",
        headers = headers
    ) {
        httpClient.get(url) {
            bearerAuth(authToken)
        }
    }
}

suspend fun ApiService.getOrderHistory(
    authToken: String
): List<OrderDetailed> {
    val url = "$baseUrl/api/orders/user"
    val headers = mapOf("Authorization" to "Bearer $authToken")

    return logApiCall(
        apiName = "getOrderHistory",
        url = url,
        method = "GET",
        headers = headers
    ) {
        httpClient.get(url) {
            bearerAuth(authToken)
        }
    }
}

suspend fun ApiService.createPaymentLink(
    orderId: String,
    authToken: String
): PaymentLinkResponse {
    val url = "$baseUrl/api/payments/$orderId"
    val headers = mapOf("Authorization" to "Bearer $authToken")

    return logApiCall(
        apiName = "createPaymentLink",
        url = url,
        method = "POST",
        headers = headers
    ) {
        httpClient.post(url) {
            bearerAuth(authToken)
        }
    }
}

suspend fun ApiService.updatePaymentInformation(
    paymentId: String,
    orderId: String,
    authToken: String
): Map<String, Any> {
    val url = "$baseUrl/api/payments/?payment_id=$paymentId&order_id=$orderId"
    val headers = mapOf("Authorization" to "Bearer $authToken")

    return logApiCall(
        apiName = "updatePaymentInformation",
        url = url,
        method = "GET",
        headers = headers
    ) {
        httpClient.get(url) {
            bearerAuth(authToken)
        }
    }
}

suspend fun ApiService.downloadInvoice(
    orderNumber: String,
    authToken: String
): ByteArray {
    val url = "$baseUrl/api/orders/invoice/$orderNumber"

    return httpClient.get(url) {
        bearerAuth(authToken)
    }.body()
}


suspend fun ApiService.createAddress(
    addressRequest: AddressRequest,
    authToken: String
): AddressResponse {
    val url = "$baseUrl/api/addresses/"
    val headers = mapOf("Authorization" to "Bearer $authToken")

    return logApiCall(
        apiName = "createAddress",
        url = url,
        method = "POST",
        headers = headers
    ) {
        httpClient.post(url) {
            bearerAuth(authToken)
            contentType(ContentType.Application.Json)
            setBody(addressRequest)
        }
    }
}

/**
 * Get current user's addresses (sorted with default first)
 * GET /api/address/user
 */
suspend fun ApiService.getUserAddresses(
    authToken: String
): List<AddressDetail> {
    val url = "$baseUrl/api/addresses/user"
    val headers = mapOf("Authorization" to "Bearer $authToken")

    return logApiCall(
        apiName = "getUserAddresses",
        url = url,
        method = "GET",
        headers = headers
    ) {
        httpClient.get(url) {
            bearerAuth(authToken)
        }
    }
}

/**
 * Get current user's default address
 * GET /api/address/user/default
 */
suspend fun ApiService.getDefaultAddress(
    authToken: String
): AddressResponse {
    val url = "$baseUrl/api/addresses/user/default"
    val headers = mapOf("Authorization" to "Bearer $authToken")

    return logApiCall(
        apiName = "getDefaultAddress",
        url = url,
        method = "GET",
        headers = headers
    ) {
        httpClient.get(url) {
            bearerAuth(authToken)
        }
    }
}

/**
 * Set address as default
 * PATCH /api/address/:id/default
 */
suspend fun ApiService.setDefaultAddress(
    addressId: String,
    authToken: String
): AddressResponse {
    val url = "$baseUrl/api/addresses/$addressId/default"
    val headers = mapOf("Authorization" to "Bearer $authToken")

    return logApiCall(
        apiName = "setDefaultAddress",
        url = url,
        method = "PATCH",
        headers = headers
    ) {
        httpClient.patch(url) {
            bearerAuth(authToken)
        }
    }
}

/**
 * Get address by ID
 * GET /api/address/:id
 */
suspend fun ApiService.getAddressById(
    addressId: String,
    authToken: String
): AddressDetail {
    val url = "$baseUrl/api/addresses/$addressId"
    val headers = mapOf("Authorization" to "Bearer $authToken")

    return logApiCall(
        apiName = "getAddressById",
        url = url,
        method = "GET",
        headers = headers
    ) {
        httpClient.get(url) {
            bearerAuth(authToken)
        }
    }
}

/**
 * Update address
 * PUT /api/address/:id
 */
suspend fun ApiService.updateAddress(
    addressId: String,
    addressRequest: AddressRequest,
    authToken: String
): AddressResponse {
    val url = "$baseUrl/api/addresses/$addressId"
    val headers = mapOf("Authorization" to "Bearer $authToken")

    return logApiCall(
        apiName = "updateAddress",
        url = url,
        method = "PUT",
        headers = headers
    ) {
        httpClient.put(url) {
            bearerAuth(authToken)
            contentType(ContentType.Application.Json)
            setBody(addressRequest)
        }
    }
}

/**
 * Delete address
 * DELETE /api/address/:id
 */
suspend fun ApiService.deleteAddress(
    addressId: String,
    authToken: String
): AddressDeleteResponse {
    val url = "$baseUrl/api/addresses/$addressId"
    val headers = mapOf("Authorization" to "Bearer $authToken")

    return logApiCall(
        apiName = "deleteAddress",
        url = url,
        method = "DELETE",
        headers = headers
    ) {
        httpClient.delete(url) {
            bearerAuth(authToken)
        }
    }
}

/**
 * Get all addresses (admin use)
 * GET /api/address/
 */
suspend fun ApiService.getAllAddresses(
    authToken: String
): List<Address> {
    val url = "$baseUrl/api/addresses/"
    val headers = mapOf("Authorization" to "Bearer $authToken")

    return logApiCall(
        apiName = "getAllAddresses",
        url = url,
        method = "GET",
        headers = headers
    ) {
        httpClient.get(url) {
            bearerAuth(authToken)
        }
    }
}
