package dev.infa.page3.presentation.repositary

import dev.infa.page3.data.model.WcCategory
import dev.infa.page3.data.remote.SessionManager
import dev.infa.page3.network.NetworkConnectivity
import dev.infa.page3.network.NetworkException
import dev.infa.page3.network.requireNetwork
import dev.infa.page3.presentation.api.*

class CategoryRepository(
    private val api: ApiService,
    private val sessionManager: SessionManager,
    private val network: NetworkConnectivity
) {

    suspend fun createCategory(categoryRequest: WcCategory): WcCategory? {
        return try {
            network.requireNetwork()
            api.wcCreateCategory(categoryRequest)
        } catch (e: NetworkException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAllCategories(): List<WcCategory> {
        return try {
            network.requireNetwork()
            api.wcListCategories()
        } catch (e: NetworkException) {
            throw e
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getCategoryById(categoryId: String): WcCategory? {
        return try {
            network.requireNetwork()
            val id = categoryId.toIntOrNull() ?: return null
            api.wcListCategories().firstOrNull { it.id == id }
        } catch (e: NetworkException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateCategory(categoryId: String, categoryRequest: WcCategory): WcCategory? {
        return try {
            network.requireNetwork()
            val id = categoryId.toIntOrNull() ?: return null
            api.wcUpdateCategory(id, categoryRequest)
        } catch (e: NetworkException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteCategory(categoryId: String): Boolean {
        return try {
            network.requireNetwork()
            val id = categoryId.toIntOrNull() ?: return false
            api.wcDeleteCategory(id)
            true
        } catch (e: NetworkException) {
            throw e
        } catch (e: Exception) {
            false
        }
    }
}