package dev.infa.page3.data.remote

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CacheManager(val settings: Settings = Settings()) {

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    companion object {
        private const val CACHE_VALIDITY_MS = 5 * 60 * 1000L
        const val KEY_LAST_SYNC = "last_sync_"
    }

    // Generic save with timestamp
    inline fun <reified T> save(key: String, data: T) {
        try {
            val jsonString = json.encodeToString(data)
            settings[key] = jsonString
            settings["${KEY_LAST_SYNC}$key"] = getCurrentTimeMillis()
        } catch (e: Exception) {
            println("❌ Cache save error for $key: ${e.message}")
        }
    }

    // Generic get with cache validation
    inline fun <reified T> get(key: String): T? {
        if (!isValid(key)) return null

        return try {
            val jsonString = settings.getStringOrNull(key) ?: return null
            json.decodeFromString<T>(jsonString)
        } catch (e: Exception) {
            println("❌ Cache get error for $key: ${e.message}")
            null
        }
    }

    // Save Int
    fun saveInt(key: String, value: Int) {
        settings[key] = value
        settings["${KEY_LAST_SYNC}$key"] = getCurrentTimeMillis()
    }

    // Get Int
    fun getInt(key: String): Int? {
        if (!isValid(key)) return null
        return settings.getIntOrNull(key)
    }

    // Clear all cache
    fun clearAll() {
        settings.clear()
    }

    // Check if cache is valid
    fun isValid(key: String): Boolean {
        val lastSync = settings.getLongOrNull("${KEY_LAST_SYNC}$key") ?: return false
        return getCurrentTimeMillis() - lastSync < CACHE_VALIDITY_MS
    }

    // Platform-specific time function
    fun getCurrentTimeMillis(): Long {
        return dev.infa.page3.data.remote.getCurrentTimeMillis()
    }
}


expect fun getCurrentTimeMillis(): Long