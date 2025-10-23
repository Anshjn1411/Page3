package dev.infa.page3.presentation.viewModel

import dev.infa.page3.data.model.Rating
import dev.infa.page3.presentation.repositary.RatingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dev.infa.page3.presentation.uiSatateClaases.ListUiState
import dev.infa.page3.presentation.uiSatateClaases.OperationUiState

class RatingViewModel(
    private val repository: RatingRepository
)
{
    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _ratingsState = MutableStateFlow<ListUiState<Rating>>(ListUiState.Idle)
    val ratingsState: StateFlow<ListUiState<Rating>> = _ratingsState.asStateFlow()

    private val _operationState = MutableStateFlow<OperationUiState>(OperationUiState.Idle)
    val operationState: StateFlow<OperationUiState> = _operationState.asStateFlow()

    private val _averageRating = MutableStateFlow(0.0)
    val averageRating: StateFlow<Double> = _averageRating.asStateFlow()

    private val _ratingDistribution = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val ratingDistribution: StateFlow<Map<Int, Int>> = _ratingDistribution.asStateFlow()

    fun loadProductRatings(productId: String) {
        viewModelScope.launch {
            _ratingsState.value = ListUiState.Loading
            try {
                val ratings = repository.getProductRatings(productId)
//                _ratingsState.value = when {
//                    ratings.isEmpty() -> ListUiState.Empty
//                    else -> ListUiState.Success(ratings)
//                }

                // Load additional rating data
                _averageRating.value = repository.getAverageRating(productId)
                _ratingDistribution.value = repository.getRatingDistribution(productId)

            } catch (e: Exception) {
                _ratingsState.value = ListUiState.Error("Failed to load ratings: ${e.message}")
            }
        }
    }

    fun createRating(productId: String, rating: Int) {
        viewModelScope.launch {
            _operationState.value = OperationUiState.Loading
            try {
                val result = repository.createRating(productId, rating)
                _operationState.value = if (result) {
                    loadProductRatings(productId) // Refresh ratings
                    OperationUiState.Success
                } else {
                    OperationUiState.Error("Failed to create rating")
                }
            } catch (e: Exception) {
                _operationState.value = OperationUiState.Error("Error creating rating: ${e.message}")
            }
        }
    }

    fun clearOperationState() {
        _operationState.value = OperationUiState.Idle
    }
}