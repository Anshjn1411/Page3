package dev.infa.page3.presentation.api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** Shared JSON config for all API layers (ignore unknown keys, lenient). */
val jsonConfig = Json {
    ignoreUnknownKeys = true
    isLenient = true
    prettyPrint = true
    coerceInputValues = true
    encodeDefaults = true
    explicitNulls = false
}

/** Single app-wide HTTP client: JSON, logging, cache, timeouts, GET retries. */
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

    install(HttpCache)

    install(HttpTimeout) {
        requestTimeoutMillis = 15000
        connectTimeoutMillis = 10000
        socketTimeoutMillis = 15000
    }

    install(HttpRequestRetry) {
        maxRetries = 2
        retryIf { request, response ->
            request.method == HttpMethod.Get && !response.status.isSuccess()
        }
        retryOnExceptionIf { request, _ ->
            request.method == HttpMethod.Get
        }
        exponentialDelay(base = 250.0, maxDelayMs = 2000)
    }
}
