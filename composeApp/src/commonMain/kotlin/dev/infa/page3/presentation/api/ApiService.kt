package dev.infa.page3.presentation.api

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Central HTTP API access for Page3 (auth backend, WordPress custom API, WooCommerce REST, health, PhonePe).
 *
 * Endpoint implementations live in sibling files as [ApiService] extensions (same package).
 */
class ApiService {

    internal val authBaseUrl = "https://page3-backend.onrender.com/api/auth"
    internal val healthBaseUrl = "https://page3-backend.onrender.com/health"
    internal val baseUrl = "https://www.page3life.com"
    internal val wpBase = "https://www.page3life.com"
    internal val wcApiBase = "$wpBase/wp-json/wc/v3"

    /** Move to secure server-side proxy before production release. */
    internal val consumerKey = "ck_b40f065fef2b787e56bd5446b6446ffd16840360"
    internal val consumerSecret = "cs_bd0cc66529a378e71dfc7f5e23595cd796f64343"

    internal val phonePeAuthBase = "https://api.phonepe.com/apis/identity-manager"
    internal val phonePeCheckoutBase = "https://api.phonepe.com/apis/pg"

    companion object {
        internal val requestBodyJsonLogger: Json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }
    }

    internal suspend inline fun <reified T> logApiCall(
        apiName: String,
        url: String,
        method: String,
        headers: Map<String, String> = emptyMap(),
        requestBody: Any? = null,
        crossinline block: suspend () -> HttpResponse
    ): T {
        try {
            println("\n" + "=".repeat(60))
            println("🚀 API REQUEST START")
            println("=".repeat(60))
            println("📍 Endpoint: $apiName")
            println("🔗 URL: $url")
            println("📝 Method: $method")

            if (headers.isNotEmpty()) {
                println("📋 Headers:")
                headers.forEach { (key, value) ->
                    val sanitizedValue = if (key.contains("Authorization", true)) {
                        "Bearer ***${value}"
                    } else value
                    println("   $key: $sanitizedValue")
                }
            }

            requestBody?.let { body ->
                println("📦 Request Body:")
                when (body) {
                    is String -> println("   $body")
                    else -> {
                        try {
                            println("   ${requestBodyJsonLogger.encodeToString(body)}")
                        } catch (e: Exception) {
                            println("   ${body.toString()}")
                        }
                    }
                }
            }

            val response = withContext(Dispatchers.Default) { block() }

            println("\n📨 API RESPONSE:")
            println("✅ Status: ${response.status.value} ${response.status.description}")
            println("📏 Content Length: ${response.headers[HttpHeaders.ContentLength] ?: "Unknown"}")
            println("🏷️ Content Type: ${response.headers[HttpHeaders.ContentType]}")

            println("📋 Response Headers:")
            response.headers.forEach { key, values ->
                println("   $key: ${values.joinToString(", ")}")
            }

            val responseText = response.bodyAsText()
            println("📦 Response Body:")
            println("   $responseText")

            println("=".repeat(60))
            println("✅ API REQUEST COMPLETED")
            println("=".repeat(60) + "\n")

            return jsonConfig.decodeFromString(responseText)
        } catch (e: Exception) {
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

    internal suspend fun logApiCallUnit(
        apiName: String,
        url: String,
        method: String,
        headers: Map<String, String> = emptyMap(),
        requestBody: Any? = null,
        block: suspend () -> HttpResponse
    ) {
        try {
            println("\n" + "=".repeat(60))
            println("🚀 API REQUEST START")
            println("=".repeat(60))
            println("📍 Endpoint: $apiName")
            println("🔗 URL: $url")
            println("📝 Method: $method")

            if (headers.isNotEmpty()) {
                println("📋 Headers:")
                headers.forEach { (key, value) ->
                    val sanitizedValue = if (key.contains("Authorization", true)) {
                        "Bearer ***${value.takeLast(4)}"
                    } else value
                    println("   $key: $sanitizedValue")
                }
            }

            val response = withContext(Dispatchers.Default) { block() }

            println("\n📨 API RESPONSE:")
            println("✅ Status: ${response.status.value} ${response.status.description}")
            println("📦 Response: Unit (No Content)")

            println("=".repeat(60))
            println("✅ API REQUEST COMPLETED")
            println("=".repeat(60) + "\n")
        } catch (e: Exception) {
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

    /** Append WooCommerce REST consumer key/secret query parameters. */
    internal fun applyWcQueryAuth(url: String): String =
        if (url.contains("?")) "$url&consumer_key=$consumerKey&consumer_secret=$consumerSecret"
        else "$url?consumer_key=$consumerKey&consumer_secret=$consumerSecret"
}
