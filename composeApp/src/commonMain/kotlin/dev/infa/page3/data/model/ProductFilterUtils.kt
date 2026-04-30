package dev.infa.page3.data.model

/**
 * Client-side filter/sort for a product list (e.g. current category grid).
 */
fun List<Product>.applySearchFilters(filters: SearchFilters): List<Product> {
    var filtered = this

    filters.minPrice?.let { min ->
        filtered = filtered.filter { product ->
            val price = product.price?.toDoubleOrNull()
                ?: product.regularPrice?.toDoubleOrNull()
                ?: 0.0
            price >= min
        }
    }

    filters.maxPrice?.let { max ->
        filtered = filtered.filter { product ->
            val price = product.price?.toDoubleOrNull()
                ?: product.regularPrice?.toDoubleOrNull()
                ?: Double.MAX_VALUE
            price <= max
        }
    }

    filters.minDiscount?.let { minDiscount ->
        filtered = filtered.filter { product ->
            val regularPrice = product.regularPrice?.toDoubleOrNull()
            val salePrice = product.salePrice?.toDoubleOrNull()
            if (regularPrice != null && salePrice != null && regularPrice > salePrice) {
                val discount = ((regularPrice - salePrice) / regularPrice) * 100
                discount >= minDiscount
            } else {
                false
            }
        }
    }

    filters.stock?.let { stockStatus ->
        filtered = filtered.filter { product ->
            when (stockStatus) {
                "in_stock" -> product.stockStatus?.equals("instock", ignoreCase = true) == true
                "out_of_stock" -> product.stockStatus?.equals("outofstock", ignoreCase = true) == true
                else -> true
            }
        }
    }

    if (filters.sizes.isNotEmpty()) {
        filtered = filtered.filter { product ->
            product.attributes.any { attr ->
                (attr.name.equals("size", ignoreCase = true) ||
                    attr.name.contains("size", ignoreCase = true)) &&
                    attr.options?.any { option ->
                        filters.sizes.any { selectedSize ->
                            option.equals(selectedSize, ignoreCase = true)
                        }
                    } == true
            }
        }
    }

    filtered = when (filters.sort) {
        "price_low" -> filtered.sortedBy {
            it.price?.toDoubleOrNull() ?: it.regularPrice?.toDoubleOrNull() ?: Double.MAX_VALUE
        }
        "price_high" -> filtered.sortedByDescending {
            it.price?.toDoubleOrNull() ?: it.regularPrice?.toDoubleOrNull() ?: 0.0
        }
        "newest" -> filtered.sortedByDescending { it.dateCreated }
        "oldest" -> filtered.sortedBy { it.dateCreated }
        "name_asc" -> filtered.sortedBy { it.name.lowercase() }
        "name_desc" -> filtered.sortedByDescending { it.name.lowercase() }
        else -> filtered
    }

    return filtered
}
