package dev.infa.page3.presentation.viewmodel

import dev.infa.page3.presentation.repository.WishlistRepository
import dev.infa.page3.data.model.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import dev.infa.page3.presentation.uiSatateClaases.ListUiState
import dev.infa.page3.presentation.uiSatateClaases.OperationUiState

class WishlistViewModel(
    private val wishlistRepository: WishlistRepository
)  {
    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _wishlistState = MutableStateFlow<ListUiState<Product>>(ListUiState.Idle)
    val wishlistState: StateFlow<ListUiState<Product>> = _wishlistState.asStateFlow()

    private val _actionState = MutableStateFlow<OperationUiState>(OperationUiState.Idle)
    val actionState: StateFlow<OperationUiState> = _actionState.asStateFlow()

    private val _wishlistProductIds = MutableStateFlow<Set<Int>>(emptySet())
    val wishlistProductIds: StateFlow<Set<Int>> = _wishlistProductIds.asStateFlow()

    init {
        viewModelScope.launch {
            wishlistRepository.wishlistItems.collect { items ->
                _wishlistProductIds.value = items.map { it.id }.toSet()
                _wishlistState.value = when {
                    items.isEmpty() -> ListUiState.Empty
                    else -> ListUiState.Success(items)
                }
            }
        }
    }

    fun loadWishlist() {
        // State is driven by [WishlistRepository.wishlistItems]; no-op for API compatibility.
    }

    fun toggleWishlist(product: Product) {
        viewModelScope.launch {
            _actionState.value = OperationUiState.Loading
            try {
                withContext(Dispatchers.Default) {
                    val isInWishlist = _wishlistProductIds.value.contains(product.id)
                    if (isInWishlist) {
                        wishlistRepository.removeFromWishlist(product.id)
                    } else {
                        wishlistRepository.addToWishlist(product)
                    }
                }
                _actionState.value = OperationUiState.Success
            } catch (e: Exception) {
                _actionState.value = OperationUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun isProductInWishlist(productId: Int): Boolean {
        return _wishlistProductIds.value.contains(productId)
    }

    fun resetActionState() {
        _actionState.value = OperationUiState.Idle
    }

    fun onCleared() {
        resetActionState()
    }
}