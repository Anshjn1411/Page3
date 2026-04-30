package dev.infa.page3.presentation.repository

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import dev.infa.page3.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


class WishlistRepository(private val settings: Settings = Settings()) {

    private val WISHLIST_KEY = "wishlist_items"

    private val _items = MutableStateFlow(readStoredItems())
    val wishlistItems: StateFlow<List<Product>> = _items.asStateFlow()

    suspend fun addToWishlist(product: Product) {
        val current = _items.value.toMutableList()
        if (current.none { it.id == product.id }) {
            val minimalProduct = Product(
                id = product.id,
                name = product.name,
                price = product.price,
                images = product.images.take(1),
                categories = product.categories.take(1),
                slug = product.slug
            )
            current.add(minimalProduct)
            persistAndEmit(current)
        }
    }

    suspend fun removeFromWishlist(productId: Int) {
        val current = _items.value.filter { it.id != productId }
        persistAndEmit(current)
    }

    fun isInWishlist(productId: Int): Boolean =
        _items.value.any { it.id == productId }

    private fun readStoredItems(): List<Product> {
        val json = settings[WISHLIST_KEY, "[]"]
        return try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun persistAndEmit(items: List<Product>) {
        settings[WISHLIST_KEY] = Json.encodeToString(items)
        _items.value = items
    }

    suspend fun clearWishlist() {
        persistAndEmit(emptyList())
    }
}
