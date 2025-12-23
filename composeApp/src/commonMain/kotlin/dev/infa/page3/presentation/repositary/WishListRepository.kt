package dev.infa.page3.presentation.repository

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import dev.infa.page3.data.model.Product
import dev.infa.page3.data.model.WcImage
import dev.infa.page3.ui.productscreen.components.ProductCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


class WishlistRepository(private val settings: Settings = Settings()) {

    private val WISHLIST_KEY = "wishlist_items"

    suspend fun addToWishlist(product: Product) {
        val current = getAllWishlistItemsOnce().toMutableList()
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
            saveWishlist(current)
        }
    }

    suspend fun removeFromWishlist(productId: Int) {
        val current = getAllWishlistItemsOnce().filter { it.id != productId }
        saveWishlist(current)
    }

    fun getAllWishlistItems(): Flow<List<Product>> {
        return flow { emit(getAllWishlistItemsOnce()) }
    }

    fun isInWishlist(productId: Int): Boolean {
        return getAllWishlistItemsOnce().any { it.id == productId }
    }

    private fun getAllWishlistItemsOnce(): List<Product> {
        val json = settings[WISHLIST_KEY, "[]"]
        return try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveWishlist(items: List<Product>) {
        settings[WISHLIST_KEY] = Json.encodeToString(items)
    }

    suspend fun clearWishlist() {
        settings[WISHLIST_KEY] = "[]"
    }
}
