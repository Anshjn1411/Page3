package dev.infa.page3.presentation.repositary

import dev.infa.page3.data.model.WcCategory
import dev.infa.page3.data.remote.SessionManager
import dev.infa.page3.presentation.api.ApiService

class CategoryRepository(
    private val api: ApiService,
    private val sessionManager: SessionManager
) {

    suspend fun createCategory(categoryRequest: WcCategory): WcCategory? {
        return try {
            api.wcCreateCategory(categoryRequest)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAllCategories(): List<WcCategory> {
        return try {
            api.wcListCategories()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getCategoryById(categoryId: String): WcCategory? {
        return try {
            val id = categoryId.toIntOrNull() ?: return null
            api.wcListCategories().firstOrNull { it.id == id }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateCategory(categoryId: String, categoryRequest: WcCategory): WcCategory? {
        return try {
            val id = categoryId.toIntOrNull() ?: return null
            api.wcUpdateCategory(id, categoryRequest)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteCategory(categoryId: String): Boolean {
        return try {
            val id = categoryId.toIntOrNull() ?: return false
            api.wcDeleteCategory(id)
            true
        } catch (e: Exception) {
            false
        }
    }
}