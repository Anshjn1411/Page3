package dev.infa.page3.data.remote

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

class SessionManager(private val settings: Settings = Settings()) {

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
    }

    fun saveAuthToken(token: String) {
        settings[KEY_AUTH_TOKEN] = token
    }

    fun getAuthToken(): String? {
        return settings.getStringOrNull(KEY_AUTH_TOKEN)
    }

    fun saveRefreshToken(token: String) {
        settings[KEY_REFRESH_TOKEN] = token
    }

    fun getRefreshToken(): String? {
        return settings.getStringOrNull(KEY_REFRESH_TOKEN)
    }

    fun clearAuthToken() {
        settings.remove(KEY_AUTH_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
    }

    fun isLoggedIn(): Boolean {
        return getAuthToken() != null
    }
}
