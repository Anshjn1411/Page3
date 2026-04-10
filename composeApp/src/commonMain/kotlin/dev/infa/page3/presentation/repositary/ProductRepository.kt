package dev.infa.page3.presentation.repositary

import dev.infa.page3.data.model.MultipleProductsRequest
import dev.infa.page3.data.model.Product
import dev.infa.page3.data.model.WcImage
import dev.infa.page3.data.model.WcProductCreateRequest
import dev.infa.page3.data.model.WcProductUpdateRequest
import dev.infa.page3.data.remote.SessionManager
import dev.infa.page3.presentation.api.ApiService


class ProductRepository(
    private val api: ApiService,
    private val sessionManager: SessionManager
)
{

    private fun normalizeWcImageUrl(url: String?): String {
        val raw = url ?: ""
        return when {
            raw.startsWith("//") -> "https:$raw"
            raw.startsWith("/") -> "https://www.page3life.com$raw"
            else -> raw
        }
    }

    private fun normalizeWcImage(img: WcImage): WcImage = img.copy(
        src = normalizeWcImageUrl(img.src),
        thumbnail = img.thumbnail?.takeIf { it.isNotBlank() }?.let { normalizeWcImageUrl(it) }
    )

    private fun productWithNormalizedImages(p: Product): Product {
        val fixedImages = p.images.map { normalizeWcImage(it) }
        return p.copy(images = fixedImages)
    }

    // Simple in-memory caches
    private val productCache: MutableMap<Int, Product> = mutableMapOf()
    private val listCache: MutableMap<String, List<Product>> = mutableMapOf()

    suspend fun createProduct(productRequest: WcProductCreateRequest): Product? {
        return try {
            api.wcCreateProduct(productRequest)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createMultipleProducts(productsRequest: MultipleProductsRequest): List<Product> {
        return try {
            // Not supported directly in WooCommerce v3; caller should loop createProduct
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllProducts(page: Int = 1, perPage: Int = 20, categoryId: Int? = null, search: String? = null): List<Product> {
        return try {
            val cacheKey = "p=$page|s=$perPage|c=${categoryId ?: "-"}|q=${search ?: "-"}"
            listCache[cacheKey]?.let { return it }

            // Ensure each product has fully qualified image (and thumbnail) URLs and cache
            val result = api.wcListProducts(page = page, perPage = perPage, categoryId = categoryId, search = search).map { p ->
                val fixed = productWithNormalizedImages(p)
                productCache[p.id] = fixed
                fixed
            }
            listCache[cacheKey] = result
            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun searchProducts(search: String, page: Int = 1, perPage: Int = 20): List<Product> {
        return try {
            api.wcListProducts(page = page, perPage = perPage, search = search).map { productWithNormalizedImages(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getProductsByCategory(categoryId: String): List<Product> {
        return try {
            val catId = categoryId.toIntOrNull()
            if (catId != null) {
                api.wcListProducts(categoryId = catId).map { productWithNormalizedImages(it) }
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getProductsBySubCategory(subCategoryId: String): List<Product> {
        return try {
            // WooCommerce doesn't have subcategory endpoint; categories are hierarchical
            val catId = subCategoryId.toIntOrNull()
            if (catId != null) {
                api.wcListProducts(categoryId = catId).map { productWithNormalizedImages(it) }
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getProductById(productId: String): Product? {
        return try {
            val id = productId.toIntOrNull() ?: return null
            productCache[id] ?: api.wcGetProduct(id).let { p ->
                val fixed = productWithNormalizedImages(p)
                productCache[id] = fixed
                fixed
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateProduct(productId: String, productRequest: WcProductUpdateRequest): Product? {
        return try {
            val id = productId.toIntOrNull() ?: return null
            api.wcUpdateProduct(id, productRequest)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteProduct(productId: String): Boolean {
        return try {
            val id = productId.toIntOrNull() ?: return false
            api.wcDeleteProduct(id)
            true
        } catch (e: Exception) {
            false
        }
    }

    // Helper methods for common use cases
    suspend fun getFeaturedProducts(limit: Int = 10): List<Product> {
        return getAllProducts(page = 1, perPage = limit)
    }

    suspend fun getProductsOnSale(limit: Int = 20): List<Product> {
        // WooCommerce supports ?on_sale=true
        return try {
            api.wcListProducts(page = 1, perPage = limit, search = null)
                .map { productWithNormalizedImages(it) }
                .filter { (it.salePrice ?: it.price)?.isNotBlank() == true && (it.salePrice ?: "") != "0" }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getProductsByPriceRange(minPrice: Double, maxPrice: Double): List<Product> {
        // WooCommerce lacks direct min/max filters; use search or client-side filter
        return getAllProducts(page = 1, perPage = 100).filter {
            val p = it.price?.toDoubleOrNull() ?: it.regularPrice?.toDoubleOrNull() ?: 0.0
            p in minPrice..maxPrice
        }
    }

//    suspend fun searchProductsByKeyword(keyword: String, page: Int = 1, limit: Int = 20): List<Product> {
//        val searchParams = SearchQueryParams(
//            search = keyword,
//            page = page,
//            limit = limit
//        )
//        return searchProducts(searchParams)
//    }
}