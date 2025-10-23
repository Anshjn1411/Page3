package dev.infa.page3.presentation.api

import dev.infa.page3.data.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.HttpHeaders.ContentEncoding
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString

// Configure HttpClient with Enhanced Logging
val jsonConfig = Json {
    ignoreUnknownKeys = true  // Ignore fields like _id, __v that aren't in your model
    isLenient = true           // Be lenient with malformed JSON
    prettyPrint = true
    coerceInputValues = true   // Coerce null to default values
    encodeDefaults = true      // Include default values when encoding
    explicitNulls = false      // Don't include null fields in output
}

val httpClient = HttpClient {
    install(ContentNegotiation) {
        json(jsonConfig)
    }

    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) {
                println("🌐 KTOR LOG: $message")
            }
        }
        level = LogLevel.INFO
        sanitizeHeader { header -> header == HttpHeaders.Authorization }
    }


    // Cache GET responses in-memory to avoid re-fetching unchanged data
    install(HttpCache)

    // Timeouts to fail fast instead of hanging
    install(HttpTimeout) {
        requestTimeoutMillis = 15000
        connectTimeoutMillis = 10000
        socketTimeoutMillis = 15000
    }

    // Retry transient errors/timeouts for idempotent requests (GET)
    install(HttpRequestRetry) {
        maxRetries = 2
        retryIf { request, response ->
            request.method == HttpMethod.Get && !response.status.isSuccess()
        }
        retryOnExceptionIf { request, cause ->
            request.method == HttpMethod.Get
        }
        exponentialDelay(base = 250.0, maxDelayMs = 2000)
    }
}


class ApiService {
    // WordPress site base (replace with your domain)
    private val baseUrl = "https://www.page3life.com"
    private val wpBase = "https://www.page3life.com" // e.g., https://yourstore.com
    private val wcApiBase = "$wpBase/wp-json/wc/v3"

    // Temporary hardcoded credentials (replace with secure storage)
    private val consumerKey = "ck_b40f065fef2b787e56bd5446b6446ffd16840360"
    private val consumerSecret = "cs_bd0cc66529a378e71dfc7f5e23595cd796f64343"

    companion object {
        private const val TAG = "ApiService"

        // JSON serializer for logging
        private val jsonLogger = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }
    }

    // Enhanced logging function with detailed request/response info
    private suspend inline fun <reified T> logApiCall(
        apiName: String,
        url: String,
        method: String,
        headers: Map<String, String> = emptyMap(),
        requestBody: Any? = null,
        crossinline block: suspend () -> HttpResponse
    ): T {

        try {
            // Log Request Details
            println("\n" + "=".repeat(60))
            println("🚀 API REQUEST START")
            println("=".repeat(60))
            println("📍 Endpoint: $apiName")
            println("🔗 URL: $url")
            println("📝 Method: $method")

            // Log Headers
            if (headers.isNotEmpty()) {
                println("📋 Headers:")
                headers.forEach { (key, value) ->
                    val sanitizedValue = if (key.contains("Authorization", true)) {
                        "Bearer ***${value}"
                    } else value
                    println("   $key: $sanitizedValue")
                }
            }

            // Log Request Body
            requestBody?.let { body ->
                println("📦 Request Body:")
                when (body) {
                    is String -> println("   $body")
                    else -> {
                        try {
                            val jsonString = jsonLogger.encodeToString(body)
                            println("   $jsonString")
                        } catch (e: Exception) {
                            println("   ${body.toString()}")
                        }
                    }
                }
            }

            // Make API Call
            val response = withContext(Dispatchers.IO) { block() }

            // Log Response Details
            println("\n📨 API RESPONSE:")
            println("✅ Status: ${response.status.value} ${response.status.description}")
            println("📏 Content Length: ${response.contentLength() ?: "Unknown"}")
            println("🏷️ Content Type: ${response.contentType()}")

            // Log Response Headers
            println("📋 Response Headers:")
            response.headers.forEach { key, values ->
                println("   $key: ${values.joinToString(", ")}")
            }

            // Get and log response body
            val responseText = response.bodyAsText()
            println("📦 Response Body:")

            try {
                // Try to pretty print JSON
                val prettyJson = Json { prettyPrint = true }.parseToJsonElement(responseText)
                println("   $prettyJson")
            } catch (e: Exception) {
                // If not JSON, print as is
                println("   $responseText")
            }

            println("=".repeat(60))
            println("✅ API REQUEST COMPLETED")
            println("=".repeat(60) + "\n")

            // Parse response body to expected type using configured JSON (ignores unknown keys)
            return jsonConfig.decodeFromString(responseText)

        } catch (e: Exception) {

            // Log Error Details
            println("\n" + "❌".repeat(20))
            println("💥 API REQUEST FAILED")
            println("❌".repeat(60))
            println("📍 Endpoint: $apiName")
            println("🔗 URL: $url")
            println("📝 Method: $method")
            println("🚨 Error Type: ${e::class.simpleName}")
            println("💬 Error Message: ${e.message}")
            println("📚 Stack Trace:")
            e.printStackTrace()
            println("❌".repeat(60) + "\n")

            throw e
        }
    }

    // Helper function for Unit responses (like delete)
    private suspend fun logApiCallUnit(
        apiName: String,
        url: String,
        method: String,
        headers: Map<String, String> = emptyMap(),
        requestBody: Any? = null,
        block: suspend () -> HttpResponse
    ) {

        try {
            // Log Request Details
            println("\n" + "=".repeat(60))
            println("🚀 API REQUEST START")
            println("=".repeat(60))
            println("📍 Endpoint: $apiName")
            println("🔗 URL: $url")
            println("📝 Method: $method")

            // Log Headers
            if (headers.isNotEmpty()) {
                println("📋 Headers:")
                headers.forEach { (key, value) ->
                    val sanitizedValue = if (key.contains("Authorization", true)) {
                        "Bearer ***${value.takeLast(4)}"
                    } else value
                    println("   $key: $sanitizedValue")
                }
            }

            // Make API Call
            val response = withContext(Dispatchers.IO) { block() }

            // Log Response Details
            println("\n📨 API RESPONSE:")
            println("✅ Status: ${response.status.value} ${response.status.description}")
            println("📦 Response: Unit (No Content)")

            println("=".repeat(60))
            println("✅ API REQUEST COMPLETED")
            println("=".repeat(60) + "\n")

        } catch (e: Exception) {

            // Log Error Details
            println("\n" + "❌".repeat(20))
            println("💥 API REQUEST FAILED")
            println("❌".repeat(60))
            println("📍 Endpoint: $apiName")
            println("🔗 URL: $url")
            println("📝 Method: $method")
            println("🚨 Error Type: ${e::class.simpleName}")
            println("💬 Error Message: ${e.message}")
            println("📚 Stack Trace:")
            e.printStackTrace()
            println("❌".repeat(60) + "\n")

            throw e
        }
    }


    // ======================= WooCommerce: BASIC AUTH VIA QUERY KEYS =======================
    private fun String.withAuth(): String =
        if (this.contains("?")) "$this&consumer_key=$consumerKey&consumer_secret=$consumerSecret"
        else "$this?consumer_key=$consumerKey&consumer_secret=$consumerSecret"

    // ======================= WooCommerce: PRODUCTS =======================
    suspend fun wcListProducts(
        page: Int = 1,
        perPage: Int = 20,
        categoryId: Int? = null,
        search: String? = null,
    ): WcProducts {
        val base = "$wcApiBase/products"
        val url = buildString {
            append(base)
            append("?page=$page&per_page=$perPage")
            categoryId?.let { append("&category=$it") }
            search?.let { append("&search=${it}") }
        }.withAuth()

        return logApiCall(
            apiName = "wcListProducts",
            url = url,
            method = "GET",
            headers = emptyMap()
        ) {
            httpClient.get(url)
        }
    }

    suspend fun wcCreateProduct(body: WcProductCreateRequest): Product {
        val url = "$wcApiBase/products".withAuth()

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

    suspend fun wcUpdateProduct(productId: Int, body: WcProductUpdateRequest): Product {
        val url = "$wcApiBase/products/$productId".withAuth()

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

    suspend fun wcDeleteProduct(productId: Int, force: Boolean = true): Product {
        val url = "$wcApiBase/products/$productId?force=$force".withAuth()

        return logApiCall(
            apiName = "wcDeleteProduct",
            url = url,
            method = "DELETE",
            headers = emptyMap()
        ) {
            httpClient.delete(url)
        }
    }

    suspend fun wcListVariations(productId: Int, page: Int = 1, perPage: Int = 50): WcVariations {
        val base = "$wcApiBase/products/$productId/variations"
        val url = "$base?page=$page&per_page=$perPage".withAuth()

        return logApiCall(
            apiName = "wcListVariations",
            url = url,
            method = "GET",
            headers = emptyMap()
        ) {
            httpClient.get(url)
        }
    }

    suspend fun getAllUsers(token: String): List<User> {
        val url = "$baseUrl/api/users"
        val headers = mapOf("Authorization" to "Bearer $token")

        return logApiCall(
            apiName = "getAllUsers",
            url = url,
            method = "GET",
            headers = headers
        ) {
            httpClient.get(url) {
                header("Authorization", "Bearer $token")
            }
        }
    }

    suspend fun updateUser(id: String, request: UpdateUserRequest, token: String): UpdateUserResponse {
        val url = "$baseUrl/api/users/$id"
        val headers = mapOf(
            "Authorization" to "Bearer $token",
            "Content-Type" to "application/json"
        )

        return logApiCall(
            apiName = "updateUser",
            url = url,
            method = "PUT",
            headers = headers,
            requestBody = request
        ) {
            httpClient.put(url) {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    suspend fun deleteUser(id: String, token: String) {
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

    suspend fun createCategory(categoryRequest: CategoryRequest): CreateCategoryResponse {
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

    suspend fun wcListCategories(
        page: Int = 1,
        perPage: Int = 100,
        parent: Int? = 0
    ): WcCategories {
        val base = "$wcApiBase/products/categories"
        val url = buildString {
            append(base)
            append("?page=$page&per_page=$perPage")
            parent?.let { append("&parent=$it") }
        }.withAuth()

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
    suspend fun wcCreateCategory(body: WcCategory): WcCategory {
        val url = "$wcApiBase/products/categories".withAuth()

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

    suspend fun wcUpdateCategory(categoryId: Int, body: WcCategory): WcCategory {
        val url = "$wcApiBase/products/categories/$categoryId".withAuth()

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

    suspend fun wcDeleteCategory(categoryId: Int, force: Boolean = true): WcCategory {
        val url = "$wcApiBase/products/categories/$categoryId?force=$force".withAuth()

        return logApiCall(
            apiName = "wcDeleteCategory",
            url = url,
            method = "DELETE",
            headers = emptyMap()
        ) {
            httpClient.delete(url)
        }
    }

    suspend fun getCategoryById(categoryId: String): GetCategoryByIdResponse {
        val url = "$baseUrl/categories/$categoryId"

        return logApiCall(
            apiName = "getCategoryById",
            url = url,
            method = "GET",
            headers = emptyMap()
        ) {
            httpClient.get(url)
        }
    }

    suspend fun updateCategory(categoryId: String, categoryRequest: CategoryRequest): UpdateCategoryResponse {
        val url = "$baseUrl/categories/$categoryId"

        return logApiCall(
            apiName = "updateCategory",
            url = url,
            method = "PUT",
            headers = mapOf("Content-Type" to "application/json"),
            requestBody = categoryRequest
        ) {
            httpClient.put(url) {
                contentType(ContentType.Application.Json)
                setBody(categoryRequest)
            }
        }
    }

    suspend fun deleteCategory(categoryId: String): DeleteCategoryResponse {
        val url = "$baseUrl/categories/$categoryId"

        return logApiCall(
            apiName = "deleteCategory",
            url = url,
            method = "DELETE",
            headers = emptyMap()
        ) {
            httpClient.delete(url)
        }
    }

// ======================= SUBCATEGORY API ENDPOINTS =======================

    suspend fun createSubCategory(subCategoryRequest: SubCategoryRequest): CreateSubCategoryResponse {
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

    suspend fun getAllSubCategories(): GetAllSubCategoriesResponse {
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

    suspend fun getSubCategoryById(subCategoryId: String): GetSubCategoryByIdResponse {
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

    suspend fun getSubCategoriesByCategory(categoryId: String): GetAllSubCategoriesResponse {
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

    suspend fun updateSubCategory(subCategoryId: String, subCategoryRequest: SubCategoryRequest): UpdateSubCategoryResponse {
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

    suspend fun deleteSubCategory(subCategoryId: String): DeleteSubCategoryResponse {
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

// ======================= PRODUCT API ENDPOINTS =======================

    suspend fun createProduct(
        productRequest: ProductRequest,
        authToken: String? = null
    ): CreateProductResponse {
        val url = "$baseUrl/products"
        val headers = mutableMapOf("Content-Type" to "application/json")
        authToken?.let { headers["Authorization"] = "Bearer $it" }

        return logApiCall(
            apiName = "createProduct",
            url = url,
            method = "POST",
            headers = headers,
            requestBody = productRequest
        ) {
            httpClient.post(url) {
                contentType(ContentType.Application.Json)
                authToken?.let { bearerAuth(it) }
                setBody(productRequest)
            }
        }
    }

    suspend fun createMultipleProducts(
        productsRequest: MultipleProductsRequest,
        authToken: String? = null
    ): CreateMultipleProductsResponse {
        val url = "$baseUrl/products/creates"
        val headers = mutableMapOf("Content-Type" to "application/json")
        authToken?.let { headers["Authorization"] = "Bearer $it" }

        return logApiCall(
            apiName = "createMultipleProducts",
            url = url,
            method = "POST",
            headers = headers,
            requestBody = productsRequest
        ) {
            httpClient.post(url) {
                contentType(ContentType.Application.Json)
                authToken?.let { bearerAuth(it) }
                setBody(productsRequest)
            }
        }
    }

    suspend fun getAllProducts(
        queryParams: ProductQueryParams? = null,
        authToken: String? = null
    ): GetAllProductsResponse {
        val url = "$baseUrl/api/products"
        val headers = mutableMapOf<String, String>()
        authToken?.let { headers["Authorization"] = "Bearer $it" }

        return logApiCall(
            apiName = "getAllProducts",
            url = url,
            method = "GET",
            headers = headers
        ) {
            httpClient.get(url) {
                authToken?.let { bearerAuth(it) }
                queryParams?.let { params ->
                    params.page?.let { parameter("page", it) }
                    params.limit?.let { parameter("limit", it) }
                    params.category?.let { parameter("category", it) }
                    params.subCategory?.let { parameter("subCategory", it) }
                    params.brand?.let { parameter("brand", it) }
                    params.color?.let { parameter("color", it) }
                    params.minPrice?.let { parameter("minPrice", it) }
                    params.maxPrice?.let { parameter("maxPrice", it) }
                    params.search?.let { parameter("search", it) }
                    params.sortBy?.let { parameter("sortBy", it) }
                    params.sortOrder?.let { parameter("sortOrder", it) }
                }
            }
        }
    }

    // REPLACE your searchProducts function in ApiService:
    suspend fun searchProducts(
        searchParams: SearchQueryParams,
        authToken: String? = null
    ): SearchProductsResponse {
        val url = "$baseUrl/api/products/search"
        val headers = mutableMapOf<String, String>()
        authToken?.let { headers["Authorization"] = "Bearer $it" }

        return logApiCall(
            apiName = "searchProducts",
            url = url,
            method = "GET",
            headers = headers
        ) {
            httpClient.get(url) {
                authToken?.let { bearerAuth(it) }
                parameter("search", searchParams.search)
                searchParams.pageNumber?.let { parameter("pageNumber", it) }  // Changed
                searchParams.pageSize?.let { parameter("pageSize", it) }      // Changed
                searchParams.color?.let { parameter("color", it) }
                searchParams.sizes?.let { parameter("sizes", it) }
                searchParams.minPrice?.let { parameter("minPrice", it) }
                searchParams.maxPrice?.let { parameter("maxPrice", it) }
                searchParams.minDiscount?.let { parameter("minDiscount", it) }
                searchParams.sort?.let { parameter("sort", it) }
                searchParams.stock?.let { parameter("stock", it) }
                searchParams.brand?.let { parameter("brand", it) }
            }
        }
    }
    suspend fun getProductsByCategory(
        categoryId: String,
        authToken: String? = null
    ): GetProductsByCategoryResponse {
        val url = "$baseUrl/api/products/category/$categoryId"
        val headers = mutableMapOf<String, String>()
        authToken?.let { headers["Authorization"] = "Bearer $it" }

        return logApiCall(
            apiName = "getProductsByCategory",
            url = url,
            method = "GET",
            headers = headers
        ) {
            httpClient.get(url) {
                authToken?.let { bearerAuth(it) }
            }
        }
    }

    suspend fun getProductsBySubCategory(
        subCategoryId: String,
        authToken: String? = null
    ): GetProductsBySubCategoryResponse {
        val url = "$baseUrl/api/products/subcategory/$subCategoryId"
        val headers = mutableMapOf<String, String>()
        authToken?.let { headers["Authorization"] = "Bearer $it" }

        return logApiCall(
            apiName = "getProductsBySubCategory",
            url = url,
            method = "GET",
            headers = headers
        ) {
            httpClient.get(url) {
                authToken?.let { bearerAuth(it) }
            }
        }
    }

    suspend fun wcGetProduct(productId: Int): Product {
        val url = "$wcApiBase/products/$productId".withAuth()

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
    suspend fun wcListReviews(productId: Int, page: Int = 1, perPage: Int = 20): WcReviews {
        val base = "$wcApiBase/products/reviews"
        val url = "$base?page=$page&per_page=$perPage&product=$productId".withAuth()

        return logApiCall(
            apiName = "wcListReviews",
            url = url,
            method = "GET",
            headers = emptyMap()
        ) {
            httpClient.get(url)
        }
    }

    suspend fun wcCreateReview(body: WcReviewCreateRequest): WcReview {
        val url = "$wcApiBase/products/reviews".withAuth()

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
    suspend fun wcListCustomers(page: Int = 1, perPage: Int = 20, search: String? = null): WcCustomers {
        val base = "$wcApiBase/customers"
        val url = buildString {
            append(base)
            append("?page=$page&per_page=$perPage")
            search?.let { append("&search=${it}") }
        }.withAuth()

        return logApiCall(
            apiName = "wcListCustomers",
            url = url,
            method = "GET",
            headers = emptyMap()
        ) {
            httpClient.get(url)
        }
    }

    suspend fun wcCreateCustomer(body: WcCustomerCreateRequest): WcCustomer {
        val url = "$wcApiBase/customers".withAuth()

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

    suspend fun wcUpdateCustomer(customerId: Int, body: WcCustomerUpdateRequest): WcCustomer {
        val url = "$wcApiBase/customers/$customerId".withAuth()

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

    suspend fun wcDeleteCustomer(customerId: Int, force: Boolean = true): WcCustomer {
        val url = "$wcApiBase/customers/$customerId?force=$force".withAuth()

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
    suspend fun wcListOrders(page: Int = 1, perPage: Int = 20, status: String? = null, customer: Int? = null): WcOrders {
        val base = "$wcApiBase/orders"
        val url = buildString {
            append(base)
            append("?page=$page&per_page=$perPage")
            status?.let { append("&status=$it") }
            customer?.let { append("&customer=$it") }
        }.withAuth()

        return logApiCall(
            apiName = "wcListOrders",
            url = url,
            method = "GET",
            headers = emptyMap()
        ) {
            httpClient.get(url)
        }
    }

    suspend fun wcCreateOrder(body: WcCreateOrderRequest): WcOrder {
        val url = "$wcApiBase/orders".withAuth()

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

    suspend fun wcGetOrder(orderId: Int): WcOrder {
        val url = "$wcApiBase/orders/$orderId".withAuth()

        return logApiCall(
            apiName = "wcGetOrder",
            url = url,
            method = "GET",
            headers = emptyMap()
        ) {
            httpClient.get(url)
        }
    }

    suspend fun wcDeleteOrder(orderId: Int, force: Boolean = true): WcOrder {
        val url = "$wcApiBase/orders/$orderId?force=$force".withAuth()

        return logApiCall(
            apiName = "wcDeleteOrder",
            url = url,
            method = "DELETE",
            headers = emptyMap()
        ) {
            httpClient.delete(url)
        }
    }

    // Refunds
    suspend fun wcListRefunds(orderId: Int): WcRefunds {
        val url = "$wcApiBase/orders/$orderId/refunds".withAuth()

        return logApiCall(
            apiName = "wcListRefunds",
            url = url,
            method = "GET",
            headers = emptyMap()
        ) {
            httpClient.get(url)
        }
    }

    suspend fun wcCreateRefund(orderId: Int, body: WcRefundCreateRequest): WcRefund {
        val url = "$wcApiBase/orders/$orderId/refunds".withAuth()

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

    // ======================= WooCommerce: COUPONS and CURRENCIES =======================
    suspend fun wcListCoupons(page: Int = 1, perPage: Int = 20): WcCoupons {
        val base = "$wcApiBase/coupons"
        val url = "$base?page=$page&per_page=$perPage".withAuth()

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
    suspend fun wcListTaxRates(page: Int = 1, perPage: Int = 100): WcTaxRates {
        val base = "$wcApiBase/taxes"
        val url = "$base?page=$page&per_page=$perPage".withAuth()

        return logApiCall(
            apiName = "wcListTaxRates",
            url = url,
            method = "GET",
            headers = emptyMap()
        ) {
            httpClient.get(url)
        }
    }

    suspend fun wcListTaxClasses(): WcTaxClasses {
        val url = "$wcApiBase/taxes/classes".withAuth()

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
    suspend fun wcListShippingZones(): WcShippingZones {
        val url = "$wcApiBase/shipping/zones".withAuth()

        return logApiCall(
            apiName = "wcListShippingZones",
            url = url,
            method = "GET",
            headers = emptyMap()
        ) {
            httpClient.get(url)
        }
    }

    suspend fun wcListShippingZoneMethods(zoneId: Int): WcShippingMethods {
        val url = "$wcApiBase/shipping/zones/$zoneId/methods".withAuth()

        return logApiCall(
            apiName = "wcListShippingZoneMethods",
            url = url,
            method = "GET",
            headers = emptyMap()
        ) {
            httpClient.get(url)
        }
    }

    suspend fun wcListCurrencies(): WcCurrencies {
        val url = "$wcApiBase/data/currencies".withAuth()

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
    suspend fun wcListSettingGroups(): WcSettingGroups {
        val url = "$wcApiBase/settings".withAuth()

        return logApiCall(
            apiName = "wcListSettingGroups",
            url = url,
            method = "GET",
            headers = emptyMap()
        ) { httpClient.get(url) }
    }

    suspend fun wcListSettings(groupId: String): WcSettings {
        val url = "$wcApiBase/settings/$groupId".withAuth()

        return logApiCall(
            apiName = "wcListSettings",
            url = url,
            method = "GET",
            headers = emptyMap()
        ) { httpClient.get(url) }
    }

    suspend fun wcUpdateSettings(groupId: String, updates: List<WcSettingUpdateRequest>): WcSettings {
        val url = "$wcApiBase/settings/$groupId/batch".withAuth()
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
    suspend fun wcListWebhooks(): WcWebhooks {
        val url = "$wcApiBase/webhooks".withAuth()

        return logApiCall(
            apiName = "wcListWebhooks",
            url = url,
            method = "GET",
            headers = emptyMap()
        ) { httpClient.get(url) }
    }

    suspend fun wcCreateWebhook(body: WcWebhookRequest): WcWebhook {
        val url = "$wcApiBase/webhooks".withAuth()

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

    suspend fun wcDeleteWebhook(webhookId: Int): WcWebhook {
        val url = "$wcApiBase/webhooks/$webhookId".withAuth()

        return logApiCall(
            apiName = "wcDeleteWebhook",
            url = url,
            method = "DELETE",
            headers = emptyMap()
        ) { httpClient.delete(url) }
    }

    // Payment gateways
    suspend fun wcListPaymentGateways(): WcPaymentGateways {
        val url = "$wcApiBase/payment_gateways".withAuth()

        return logApiCall(
            apiName = "wcListPaymentGateways",
            url = url,
            method = "GET",
            headers = emptyMap()
        ) { httpClient.get(url) }
    }

    // Data endpoints (countries)
    suspend fun wcListCountries(): WcCountries {
        val url = "$wcApiBase/data/countries".withAuth()

        return logApiCall(
            apiName = "wcListCountries",
            url = url,
            method = "GET",
            headers = emptyMap()
        ) { httpClient.get(url) }
    }

    suspend fun wcGetCountry(code: String): WcCountry {
        val url = "$wcApiBase/data/countries/${'$'}code".withAuth()

        return logApiCall(
            apiName = "wcGetCountry",
            url = url,
            method = "GET",
            headers = emptyMap()
        ) { httpClient.get(url) }
    }

    suspend fun updateProduct(
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

    suspend fun deleteProduct(
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

    suspend fun getProductRatings(
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
    suspend fun createRating(
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

    suspend fun createReview(
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

    suspend fun getAllReviews(
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

    suspend fun getUserCart(authToken: String): Cart {
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

    suspend fun addItemToCart(
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

    suspend fun getWishlist(
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
    val json = Json {
        ignoreUnknownKeys = true // <-- this is key to ignore unexpected fields
        isLenient = true
        encodeDefaults = true
    }

    suspend fun addToWishlist(
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

    suspend fun removeFromWishlist(
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


    suspend fun createOrder(
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

    suspend fun getOrderById(
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

    suspend fun getOrderHistory(
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

    suspend fun createPaymentLink(
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

    suspend fun updatePaymentInformation(
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

    suspend fun downloadInvoice(
        orderNumber: String,
        authToken: String
    ): ByteArray {
        val url = "$baseUrl/api/orders/invoice/$orderNumber"

        return httpClient.get(url) {
            bearerAuth(authToken)
        }.body()
    }


    suspend fun createAddress(
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
    suspend fun getUserAddresses(
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
    suspend fun getDefaultAddress(
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
    suspend fun setDefaultAddress(
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
    suspend fun getAddressById(
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
    suspend fun updateAddress(
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
    suspend fun deleteAddress(
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
    suspend fun getAllAddresses(
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




}