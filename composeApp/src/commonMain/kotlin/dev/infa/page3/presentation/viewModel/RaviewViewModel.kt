package dev.infa.page3.presentation.viewModel

import dev.infa.page3.data.model.ReviewDetailed
import dev.infa.page3.presentation.repositary.ReviewRepository
import dev.infa.page3.presentation.uiSatateClaases.ListUiState
import dev.infa.page3.presentation.uiSatateClaases.OperationUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReviewViewModel(
    private val repository: ReviewRepository
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _reviewsState = MutableStateFlow<ListUiState<ReviewDetailed>>(ListUiState.Idle)
    val reviewsState: StateFlow<ListUiState<ReviewDetailed>> = _reviewsState.asStateFlow()

    private val _operationState = MutableStateFlow<OperationUiState>(OperationUiState.Idle)
    val operationState: StateFlow<OperationUiState> = _operationState.asStateFlow()

    private val _reviewCount = MutableStateFlow(0)
    val reviewCount: StateFlow<Int> = _reviewCount.asStateFlow()

    fun loadProductReviews(productId: String) {
        viewModelScope.launch {
            _reviewsState.value = ListUiState.Loading
            try {
                val reviews = repository.getAllReviews(productId)
                _reviewsState.value = when {
                    reviews.isEmpty() -> ListUiState.Empty
                    else -> ListUiState.Success(reviews)
                }

                _reviewCount.value = repository.getReviewCount(productId)

            } catch (e: Exception) {
                _reviewsState.value = ListUiState.Error("Failed to load reviews: ${e.message}")
            }
        }
    }

    fun createReview(productId: String, reviewText: String) {
        viewModelScope.launch {
            _operationState.value = OperationUiState.Loading
            try {
                val result = repository.createReview(productId, reviewText)
                _operationState.value = if (result) {
                    loadProductReviews(productId) // Refresh reviews
                    OperationUiState.Success
                } else {
                    OperationUiState.Error("Failed to create review")
                }
            } catch (e: Exception) {
                _operationState.value = OperationUiState.Error("Error creating review: ${e.message}")
            }
        }
    }

    fun clearOperationState() {
        _operationState.value = OperationUiState.Idle
    }
}