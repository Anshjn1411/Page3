package dev.infa.page3.presentation.viewModel

import dev.infa.page3.data.model.CartItemWithAttributes
import dev.infa.page3.data.model.Product
import dev.infa.page3.presentation.repository.CartRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dev.infa.page3.presentation.uiSatateClaases.ListUiState
import dev.infa.page3.presentation.uiSatateClaases.OperationUiState

class CartViewModel(
    private val cartRepository: CartRepository
){
    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())


    private val _cartState = MutableStateFlow<ListUiState<CartItemWithAttributes>>(ListUiState.Idle)
    val cartState: StateFlow<ListUiState<CartItemWithAttributes>> = _cartState.asStateFlow()

    private val _cartActionState = MutableStateFlow<OperationUiState>(OperationUiState.Idle)
    val cartActionState: StateFlow<OperationUiState> = _cartActionState.asStateFlow()
    private val _totalItems = MutableStateFlow(0)
    val totalItems: StateFlow<Int> = _totalItems.asStateFlow()

    init {
        println("CartViewModel initialized")
        loadCart()
    }

    fun loadCart() {
        println("Loading cart items...")
        viewModelScope.launch {
            _cartState.value = ListUiState.Loading
            try {
                val items = cartRepository.cartItems.value
                println("Cart items loaded: ${items.size}")
                _totalItems.value = items.size
                _cartState.value = if (items.isEmpty()) {
                    println("Cart is empty")
                    ListUiState.Empty
                } else {
                    ListUiState.Success(items)
                }
            } catch (e: Exception) {
                println("Failed to load cart: ${e.message}")
                _cartState.value = ListUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun addToCart(product: Product, selectedAttributes: Map<String, String> = emptyMap()) {
        println("Adding product to cart: ${product.name} (id: ${product.id})")
        viewModelScope.launch {
            _cartActionState.value = OperationUiState.Loading
            try {
                cartRepository.addItemToCart(product, selectedAttributes)
                println("Product added successfully: ${product.name}")
                loadCart()
                _cartActionState.value = OperationUiState.Success
            } catch (e: Exception) {
                println("Failed to add product: ${e.message}")
                _cartActionState.value = OperationUiState.Error(e.message ?: "Failed to add item")
            }
        }
    }

    fun removeFromCart(productId: Int, attributes: Map<String, String>? = null) {
        println("Removing product from cart: id=$productId")
        viewModelScope.launch {
            _cartActionState.value = OperationUiState.Loading
            try {
                cartRepository.removeItemFromCart(productId, attributes)
                println("Product removed successfully: id=$productId")
                loadCart()
                _cartActionState.value = OperationUiState.Success
            } catch (e: Exception) {
                println("Failed to remove product: ${e.message}")
                _cartActionState.value = OperationUiState.Error(e.message ?: "Failed to remove item")
            }
        }
    }

    fun updateQuantity(productId: Int, attributes: Map<String, String>, quantity: Int) {
        println("Updating quantity for product: id=$productId, quantity=$quantity")
        viewModelScope.launch {
            _cartActionState.value = OperationUiState.Loading
            try {
                cartRepository.updateQuantity(productId, attributes, quantity)
                loadCart()
                _cartActionState.value = OperationUiState.Success
            } catch (e: Exception) {
                _cartActionState.value = OperationUiState.Error(e.message ?: "Failed to update quantity")
            }
        }
    }

    fun isProductInCart(productId: Int): Boolean {
        val inCart = cartRepository.isInCart(productId)
        println("Is product in cart? id=$productId, result=$inCart")
        return inCart
    }

    fun getCartCount(): Int {
        val count = cartRepository.getCartCount()
        println("Current cart count: $count")
        return count
    }

    fun getCartTotalPrice(): Double {
        val total = cartRepository.getCartTotalPrice()
        println("Current cart total price: $total")
        return total
    }

    fun resetActionState() {
        println("Resetting cart action state")
        _cartActionState.value = OperationUiState.Idle
    }

    fun onCleared() {
        resetActionState()
    }
}
