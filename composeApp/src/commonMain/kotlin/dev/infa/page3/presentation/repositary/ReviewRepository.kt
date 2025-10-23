package dev.infa.page3.presentation.repositary

import dev.infa.page3.data.model.ReviewDetailed
import dev.infa.page3.data.model.ReviewRequest
import dev.infa.page3.data.remote.SessionManager
import dev.infa.page3.presentation.api.ApiService


class ReviewRepository(
    private val api: ApiService,
    private val sessionManager: SessionManager
)
{

    suspend fun createReview(productId: String, reviewText: String): Boolean {
        return try {
            val authToken = sessionManager.getAuthToken()
            if (authToken != null) {
                val reviewRequest = ReviewRequest(productId, reviewText)
                val response = api.createReview(reviewRequest, authToken)
                true // Review created successfully
            } else {
                false // User not authenticated
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getAllReviews(productId: String): List<ReviewDetailed> {
        return try {
            val authToken = sessionManager.getAuthToken()
            val response = api.getAllReviews(productId, authToken)
            response ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserReviewForProduct(productId: String, userId: String): ReviewDetailed? {
        return try {
            val reviews = getAllReviews(productId)
            reviews.find { it.user?._id == userId }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getReviewCount(productId: String): Int {
        return try {
            val reviews = getAllReviews(productId)
            reviews.size
        } catch (e: Exception) {
            0
        }
    }

    suspend fun getLatestReviews(productId: String, limit: Int = 5): List<ReviewDetailed> {
        return try {
            val reviews = getAllReviews(productId)
            reviews.sortedByDescending { it.createdAt }.take(limit)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun hasUserReviewed(productId: String, userId: String): Boolean {
        return try {
            getUserReviewForProduct(productId, userId) != null
        } catch (e: Exception) {
            false
        }
    }
}