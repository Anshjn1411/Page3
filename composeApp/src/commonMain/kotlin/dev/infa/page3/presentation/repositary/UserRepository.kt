package dev.infa.page3.presentation.repository

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import dev.infa.page3.data.model.WcAddress
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class UserData(
    val firstName: String,
    val lastName: String,
    val email: String,
    val username: String,
    val phone: String,
    val billingAddress: WcAddress,
    val shippingAddress: WcAddress
)

class UserRepository(private val settings: Settings = Settings()) {
    
    private val USER_DATA_KEY = "user_data"
    private val IS_LOGGED_IN_KEY = "is_logged_in"
    private val CUSTOMER_ID_KEY = "customer_id"
    
    fun saveUserData(userData: UserData) {
        settings[USER_DATA_KEY] = Json.encodeToString(userData)
    }
    
    fun getUserData(): UserData? {
        return try {
            val json = settings[USER_DATA_KEY, ""]
            if (json.isBlank()) null else Json.decodeFromString(json)
        } catch (e: Exception) {
            null
        }
    }
    
    fun saveCustomerId(customerId: Int) {
        settings[CUSTOMER_ID_KEY] = customerId.toString()
    }
    
    fun getCustomerId(): Int? {
        return try {
            val id = settings[CUSTOMER_ID_KEY, ""]
            if (id.isBlank()) null else id.toInt()
        } catch (e: Exception) {
            null
        }
    }
    
    fun setLoggedIn(isLoggedIn: Boolean) {
        settings[IS_LOGGED_IN_KEY] = isLoggedIn
    }
    
    fun isLoggedIn(): Boolean {
        return settings[IS_LOGGED_IN_KEY, false]
    }
    
    fun hasUserData(): Boolean {
        return getUserData() != null
    }
    
    fun isFullyLoggedIn(): Boolean {
        return isLoggedIn() && hasUserData()
    }
    
    fun clearUserData() {
        settings.remove(USER_DATA_KEY)
        settings.remove(IS_LOGGED_IN_KEY)
        settings.remove(CUSTOMER_ID_KEY)
    }
}