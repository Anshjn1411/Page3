package dev.infa.page3.presentation.repositary

import dev.infa.page3.data.model.WcReview
import dev.infa.page3.data.model.WcReviewCreateRequest
import dev.infa.page3.data.remote.SessionManager
import dev.infa.page3.presentation.api.ApiService


class RatingRepository(
    private val api: ApiService,
    private val sessionManager: SessionManager
) {

    suspend fun createRating(productId: String, rating: Int): Boolean {
        return try {
            // WooCommerce stores reviews with rating; need reviewer info
            val pid = productId.toIntOrNull() ?: return false
            val body = WcReviewCreateRequest(
                productId = pid,
                review = "",
                reviewer = "App User",
                reviewer_email = "user@example.com",
                rating = rating
            )
            api.wcCreateReview(body)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getProductRatings(productId: String): List<WcReview> {
        return try {
            val pid = productId.toIntOrNull() ?: return emptyList()
            api.wcListReviews(pid)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserRatingForProduct(productId: String, userId: String): WcReview? {
        return try {
            val ratings = getProductRatings(productId)
            ratings.firstOrNull { it.reviewer?.contains("App User", ignoreCase = true) == true }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAverageRating(productId: String): Double {
        return try {
            val ratings = getProductRatings(productId)
            val scores = ratings.mapNotNull { it.rating }
            if (scores.isNotEmpty()) scores.sum().toDouble() / scores.size else 0.0
        } catch (e: Exception) {
            0.0
        }
    }

    suspend fun getRatingDistribution(productId: String): Map<Int, Int> {
        return try {
            val ratings = getProductRatings(productId)
            val distribution = mutableMapOf<Int, Int>()
            for (i in 1..5) {
                distribution[i] = 0
            }
            ratings.mapNotNull { it.rating }.forEach { rating ->
                distribution[rating] = distribution[rating]!! + 1
            }
            distribution
        } catch (e: Exception) {
            mapOf(1 to 0, 2 to 0, 3 to 0, 4 to 0, 5 to 0)
        }
    }
}