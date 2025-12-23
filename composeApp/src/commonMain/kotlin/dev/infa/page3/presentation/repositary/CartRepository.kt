package dev.infa.page3.presentation.repository

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import dev.infa.page3.data.model.CartItemWithAttributes
import dev.infa.page3.data.model.Product
import dev.infa.page3.data.model.WcImage
import dev.infa.page3.data.model.toCartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CartRepository(private val settings: Settings = Settings()) {

    private val CART_KEY = "cart_items"
    private val json = Json { ignoreUnknownKeys = true }

    private val _cartItems = MutableStateFlow<List<CartItemWithAttributes>>(loadCartFromSettings())
    val cartItems: StateFlow<List<CartItemWithAttributes>> = _cartItems.asStateFlow()

    private fun loadCartFromSettings(): List<CartItemWithAttributes> {
        val data = settings[CART_KEY, ""]
        return if (data.isNotBlank()) {
            try {
                val items = json.decodeFromString<List<CartItemWithAttributes>>(data)
                println("Loaded cart from settings: ${items.size} items")
                items
            } catch (e: Exception) {
                println("Failed to decode cart data: ${e.message}")
                emptyList()
            }
        } else {
            println("Cart is empty in settings")
            emptyList()
        }
    }

    private fun saveCartToSettings(items: List<CartItemWithAttributes>) {
        try {
            settings[CART_KEY] = json.encodeToString(items)
            println("Saved cart to settings: ${items.size} items")
        } catch (e: Exception) {
            println("Failed to save cart: ${e.message}")
        }
    }

    suspend fun addItemToCart(product: Product, selectedAttributes: Map<String, String> = emptyMap()) {
        println("Adding product to cart: ${product.name} (id: ${product.id}) with attributes: $selectedAttributes")
        val current = _cartItems.value.toMutableList()

        // Check if product with same attributes already exists
        val existingItemIndex = current.indexOfFirst {
            it.id == product.id && it.selectedAttributes == selectedAttributes
        }

        if (existingItemIndex >= 0) {
            // Increase quantity if same product with same attributes exists
            val existingItem = current[existingItemIndex]
            current[existingItemIndex] = existingItem.copy(
                quantity = existingItem.quantity + 1
            )
            println("Increased quantity for existing item: ${product.name}")
        } else {
            // Add as new item
            current.add(product.toCartItem(selectedAttributes))
            println("Added new item to cart: ${product.name}")
        }

        _cartItems.value = current
        saveCartToSettings(current)
    }

    suspend fun removeItemFromCart(productId: Int, attributes: Map<String, String>? = null) {
        println("Removing product from cart: id=$productId")
        val current = if (attributes != null) {
            _cartItems.value.filter { !(it.id == productId && it.selectedAttributes == attributes) }
        } else {
            _cartItems.value.filter { it.id != productId }
        }
        _cartItems.value = current
        saveCartToSettings(current)
    }

    suspend fun updateQuantity(productId: Int, attributes: Map<String, String>, quantity: Int) {
        println("Updating quantity for product: id=$productId, quantity=$quantity")
        val current = _cartItems.value.toMutableList()
        val itemIndex = current.indexOfFirst {
            it.id == productId && it.selectedAttributes == attributes
        }

        if (itemIndex >= 0) {
            if (quantity > 0) {
                current[itemIndex] = current[itemIndex].copy(quantity = quantity)
            } else {
                current.removeAt(itemIndex)
            }
            _cartItems.value = current
            saveCartToSettings(current)
        }
    }

    suspend fun clearCart() {
        println("Clearing cart")
        _cartItems.value = emptyList()
        saveCartToSettings(emptyList())
    }

    fun isInCart(productId: Int): Boolean {
        val inCart = _cartItems.value.any { it.id == productId }
        println("Check if product in cart: id=$productId, result=$inCart")
        return inCart
    }

    fun getCartCount(): Int {
        val count = _cartItems.value.size
        println("Current cart count: $count")
        return count
    }

    fun getCartTotalPrice(): Double {
        val total = _cartItems.value.sumOf { item ->
            val price = (item.salePrice ?: item.regularPrice ?: item.price)?.toDoubleOrNull() ?: 0.0
            price * item.quantity
        }
        println("Current cart total price: $total")
        return total
    }
}


