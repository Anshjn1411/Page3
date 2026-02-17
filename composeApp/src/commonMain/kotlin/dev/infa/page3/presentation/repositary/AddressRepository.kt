package dev.infa.page3.presentation.repositary

import dev.infa.page3.data.model.Address
import dev.infa.page3.data.model.AddressDetail
import dev.infa.page3.data.model.AddressRequest
import dev.infa.page3.data.remote.SessionManager
import dev.infa.page3.presentation.api.ApiService

class AddressRepository(
    private val api: ApiService,
    private val sessionManager: SessionManager
) {

    /**
     * Create a new address
     */
    suspend fun createAddress(addressRequest: AddressRequest): AddressDetail? {
        return try {
            val authToken = sessionManager.getAuthToken()
            if (authToken != null) {
                val response = api.createAddress(addressRequest, authToken)
                response.address
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get current user's addresses (sorted with default first)
     */
    suspend fun getUserAddresses(): List<AddressDetail> {
        return try {
            val authToken = sessionManager.getAuthToken()
            if (authToken != null) {
                api.getUserAddresses(authToken)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get current user's default address
     */
    suspend fun getDefaultAddress(): AddressDetail? {
        return try {
            val authToken = sessionManager.getAuthToken()
            if (authToken != null) {
                val response = api.getDefaultAddress(authToken)
                response.address
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Set address as default
     */
    suspend fun setDefaultAddress(addressId: String): AddressDetail? {
        return try {
            val authToken = sessionManager.getAuthToken()
            if (authToken != null) {
                val response = api.setDefaultAddress(addressId, authToken)
                response.address
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get address by ID
     */
    suspend fun getAddressById(addressId: String): AddressDetail? {
        return try {
            val authToken = sessionManager.getAuthToken()
            if (authToken != null) {
                api.getAddressById(addressId, authToken)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Update address
     */
    suspend fun updateAddress(addressId: String, addressRequest: AddressRequest): AddressDetail? {
        return try {
            val authToken = sessionManager.getAuthToken()
            if (authToken != null) {
                val response = api.updateAddress(addressId, addressRequest, authToken)
                response.address
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Delete address
     */
    suspend fun deleteAddress(addressId: String): Boolean {
        return try {
            val authToken = sessionManager.getAuthToken()
            if (authToken != null) {
                api.deleteAddress(addressId, authToken)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get all addresses (admin use)
     */
    suspend fun getAllAddresses(): List<Address> {
        return try {
            val authToken = sessionManager.getAuthToken()
            if (authToken != null) {
                api.getAllAddresses(authToken)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}