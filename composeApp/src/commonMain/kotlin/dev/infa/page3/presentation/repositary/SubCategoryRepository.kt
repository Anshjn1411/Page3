package dev.infa.page3.presentation.repositary

import dev.infa.page3.data.model.SubCategory
import dev.infa.page3.data.model.SubCategoryRequest
import dev.infa.page3.data.remote.SessionManager
import dev.infa.page3.presentation.api.ApiService

class SubCategoryRepository(
    private val api: ApiService,
    private val sessionManager: SessionManager
) {

    suspend fun createSubCategory(subCategoryRequest: SubCategoryRequest): SubCategory? {
        return try {
            val response = api.createSubCategory(subCategoryRequest)
            if (response.success) {
                response.data
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAllSubCategories(): List<SubCategory> {
        return try {
            val response = api.getAllSubCategories()
            if (response.success) {
                response.data ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getSubCategoryById(subCategoryId: String): SubCategory? {
        return try {
            val response = api.getSubCategoryById(subCategoryId)
            if (response.success) {
                response.data
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getSubCategoriesByCategory(categoryId: String): List<SubCategory> {
        return try {
            val response = api.getSubCategoriesByCategory(categoryId)
            if (response.success) {
                response.data ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateSubCategory(subCategoryId: String, subCategoryRequest: SubCategoryRequest): SubCategory? {
        return try {
            val response = api.updateSubCategory(subCategoryId, subCategoryRequest)
            if (response.success) {
                response.data
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteSubCategory(subCategoryId: String): Boolean {
        return try {
            val response = api.deleteSubCategory(subCategoryId)
            response.success
        } catch (e: Exception) {
            false
        }
    }
}