package dev.infa.page3.presentation.viewModel

import dev.infa.page3.data.model.WcReview
import dev.infa.page3.data.model.WcReviewCreateRequest
import dev.infa.page3.network.NetworkException
import dev.infa.page3.presentation.repositary.WooCommerceReviewRepository
import dev.infa.page3.presentation.uiSatateClaases.ListUiState
import dev.infa.page3.presentation.uiSatateClaases.OperationUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Product reviews and ratings backed by WooCommerce REST (`/products/reviews`).
 */
class ProductReviewsViewModel(
    private val repository: WooCommerceReviewRepository
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _reviewsState = MutableStateFlow<ListUiState<WcReview>>(ListUiState.Idle)
    val reviewsState: StateFlow<ListUiState<WcReview>> = _reviewsState.asStateFlow()

    private val _averageRating = MutableStateFlow(0.0)
    val averageRating: StateFlow<Double> = _averageRating.asStateFlow()

    private val _reviewCount = MutableStateFlow(0)
    val reviewCount: StateFlow<Int> = _reviewCount.asStateFlow()

    private val _operationState = MutableStateFlow<OperationUiState>(OperationUiState.Idle)
    val operationState: StateFlow<OperationUiState> = _operationState.asStateFlow()

    fun loadProductReviews(productId: String, page: Int = 1, perPage: Int = 100) {
        viewModelScope.launch {
            _reviewsState.value = ListUiState.Loading
            try {
                val reviews = withContext(Dispatchers.Default) {
                    repository.listReviewsForProduct(productId, page, perPage)
                }
                val scores = reviews.mapNotNull { it.rating }
                _averageRating.value = if (scores.isEmpty()) 0.0 else scores.sum().toDouble() / scores.size
                _reviewCount.value = reviews.size
                _reviewsState.value = when {
                    reviews.isEmpty() -> ListUiState.Empty
                    else -> ListUiState.Success(reviews)
                }
            } catch (e: NetworkException) {
                _reviewsState.value = ListUiState.Error(e.message ?: NetworkException.DEFAULT_MESSAGE)
            } catch (e: Exception) {
                _reviewsState.value = ListUiState.Error("Failed to load reviews: ${e.message}")
            }
        }
    }

    fun submitReview(body: WcReviewCreateRequest, productIdForReload: String) {
        viewModelScope.launch {
            _operationState.value = OperationUiState.Loading
            try {
                val result = withContext(Dispatchers.Default) {
                    repository.createReview(body)
                }
                _operationState.value = if (result != null) {
                    loadProductReviews(productIdForReload)
                    OperationUiState.Success
                } else {
                    OperationUiState.Error("Failed to submit review")
                }
            } catch (e: NetworkException) {
                _operationState.value = OperationUiState.Error(e.message ?: NetworkException.DEFAULT_MESSAGE)
            } catch (e: Exception) {
                _operationState.value = OperationUiState.Error(e.message ?: "Error submitting review")
            }
        }
    }

    fun clearOperationState() {
        _operationState.value = OperationUiState.Idle
    }
}
