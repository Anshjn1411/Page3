package dev.infa.page3.presentation.repositary

import dev.infa.page3.data.model.WcReview
import dev.infa.page3.data.model.WcReviewCreateRequest
import dev.infa.page3.network.NetworkConnectivity
import dev.infa.page3.network.requireNetwork
import dev.infa.page3.presentation.api.ApiService
import dev.infa.page3.presentation.api.wcCreateReview
import dev.infa.page3.presentation.api.wcListReviews

/**
 * WooCommerce REST product reviews: list (paginated), aggregate stats, create.
 */
class WooCommerceReviewRepository(
    private val api: ApiService,
    private val network: NetworkConnectivity
) {

    suspend fun listReviewsForProduct(
        productId: String,
        page: Int = 1,
        perPage: Int = 20
    ): List<WcReview> {
        network.requireNetwork()
        val pid = productId.toIntOrNull() ?: return emptyList()
        return api.wcListReviews(pid, page, perPage)
    }

    suspend fun createReview(body: WcReviewCreateRequest): WcReview? {
        return try {
            network.requireNetwork()
            api.wcCreateReview(body)
        } catch (_: Exception) {
            null
        }
    }
}
