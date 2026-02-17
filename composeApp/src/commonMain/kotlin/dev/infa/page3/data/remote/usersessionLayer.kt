package dev.infa.page3.data.remote

import com.russhwolf.settings.Settings
import dev.infa.page3.data.model.UserInfo

class SessionManager(private val settings: Settings = Settings()) {

    companion object {
        private const val KEY_AUTH_TOKEN = "jwt_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_PHONE = "user_phone"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_FIRST_NAME = "user_first_name"
        private const val KEY_USER_LAST_NAME = "user_last_name"
        private const val KEY_USER_USERNAME = "user_username"
        private const val KEY_IS_PROFILE_COMPLETE = "is_profile_complete"
        private const val KEY_IS_PHONE_VERIFIED = "is_phone_verified"
        private const val KEY_USER_CREATED_AT = "user_created_at"
    }

    // ======================== TOKEN MANAGEMENT ========================

    fun saveAuthToken(token: String) {
        settings.putString(KEY_AUTH_TOKEN, token)
    }

    fun getAuthToken(): String? = settings.getStringOrNull(KEY_AUTH_TOKEN)

    fun saveRefreshToken(token: String) {
        settings.putString(KEY_REFRESH_TOKEN, token)
    }

    fun getRefreshToken(): String? = settings.getStringOrNull(KEY_REFRESH_TOKEN)

    fun clearAuthToken() {
        settings.remove(KEY_AUTH_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
    }

    // ======================== USER DATA ========================

    fun saveUserInfo(userInfo: UserInfo) {
        userInfo.id?.let { settings.putString(KEY_USER_ID, it) }
        userInfo.phone?.let { settings.putString(KEY_USER_PHONE, it) }
        userInfo.email?.let { settings.putString(KEY_USER_EMAIL, it) }
        userInfo.first_name?.let { settings.putString(KEY_USER_FIRST_NAME, it) }
        userInfo.last_name?.let { settings.putString(KEY_USER_LAST_NAME, it) }
        userInfo.username?.let { settings.putString(KEY_USER_USERNAME, it) }
        settings.putBoolean(KEY_IS_PROFILE_COMPLETE, userInfo.isProfileComplete)
        settings.putBoolean(KEY_IS_PHONE_VERIFIED, userInfo.isPhoneVerified)
        userInfo.createdAt?.let { settings.putString(KEY_USER_CREATED_AT, it) }
    }

    fun updateUserData(userInfo: UserInfo) {
        userInfo.email?.let { settings.putString(KEY_USER_EMAIL, it) }
        userInfo.first_name?.let { settings.putString(KEY_USER_FIRST_NAME, it) }
        userInfo.last_name?.let { settings.putString(KEY_USER_LAST_NAME, it) }
        userInfo.username?.let { settings.putString(KEY_USER_USERNAME, it) }
        settings.putBoolean(KEY_IS_PROFILE_COMPLETE, userInfo.isProfileComplete)
    }

    fun getUserInfo(): UserInfo? {
        val userId = settings.getStringOrNull(KEY_USER_ID) ?: return null
        val phone = settings.getStringOrNull(KEY_USER_PHONE) ?: return null

        return UserInfo(
            id = userId,
            phone = phone,
            email = settings.getStringOrNull(KEY_USER_EMAIL),
            first_name = settings.getStringOrNull(KEY_USER_FIRST_NAME),
            last_name = settings.getStringOrNull(KEY_USER_LAST_NAME),
            username = settings.getStringOrNull(KEY_USER_USERNAME),
            isProfileComplete = settings.getBooleanOrNull(KEY_IS_PROFILE_COMPLETE) ?: false,
            isPhoneVerified = settings.getBooleanOrNull(KEY_IS_PHONE_VERIFIED) ?: false,
            createdAt = settings.getStringOrNull(KEY_USER_CREATED_AT)
        )
    }

    // ======================== SESSION STATUS ========================

    fun isLoggedIn(): Boolean = getAuthToken() != null

    fun isProfileComplete(): Boolean =
        settings.getBooleanOrNull(KEY_IS_PROFILE_COMPLETE) ?: false

    fun clearAllData() {
        clearAuthToken()
        settings.remove(KEY_USER_ID)
        settings.remove(KEY_USER_PHONE)
        settings.remove(KEY_USER_EMAIL)
        settings.remove(KEY_USER_FIRST_NAME)
        settings.remove(KEY_USER_LAST_NAME)
        settings.remove(KEY_USER_USERNAME)
        settings.remove(KEY_IS_PROFILE_COMPLETE)
        settings.remove(KEY_IS_PHONE_VERIFIED)
        settings.remove(KEY_USER_CREATED_AT)
    }
}