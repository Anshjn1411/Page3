package dev.infa.page3.presentation.viewModel

import dev.infa.page3.data.model.Product
import dev.infa.page3.data.model.WcProductCreateRequest
import dev.infa.page3.data.model.WcProductUpdateRequest
import dev.infa.page3.network.NetworkException
import dev.infa.page3.presentation.repositary.ProductRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dev.infa.page3.presentation.uiSatateClaases.ListUiState
import dev.infa.page3.presentation.uiSatateClaases.OperationUiState
import dev.infa.page3.presentation.uiSatateClaases.SingleUiState
import kotlinx.coroutines.withContext

/** Loads product data including WooCommerce `images` for gallery UIs. */
class ProductViewModel(
    private val repository: ProductRepository
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // ====== StateFlows ======
    private val _productsState = MutableStateFlow<ListUiState<Product>>(ListUiState.Idle)
    val productsState: StateFlow<ListUiState<Product>> = _productsState.asStateFlow()

    private val _selectedProductState = MutableStateFlow<SingleUiState<Product>>(SingleUiState.Idle)
    val selectedProductState: StateFlow<SingleUiState<Product>> = _selectedProductState.asStateFlow()

    private val _searchResultsState = MutableStateFlow<ListUiState<Product>>(ListUiState.Idle)
    val searchResultsState: StateFlow<ListUiState<Product>> = _searchResultsState.asStateFlow()

    private val _operationState = MutableStateFlow<OperationUiState>(OperationUiState.Idle)
    val operationState: StateFlow<OperationUiState> = _operationState.asStateFlow()

    // ====== Caching ======
    private var cachedProducts: MutableMap<Int, Product> = mutableMapOf()
    private var cachedProductList: List<Product>? = null
    private var cachedFeaturedProducts: List<Product>? = null
    private var cachedSaleProducts: List<Product>? = null

    private var isProductsLoaded = false
    private var isFeaturedLoaded = false
    private var isSaleLoaded = false

    private var _currentPage = 1
    private var _currentPerPage = 20

    // Store all loaded products for local search
    private var allLoadedProducts: List<Product> = emptyList()

    // Category grid pagination (WooCommerce page/per_page)
    private val categoryPerPage = 20
    private var pagingCategoryId: String? = null
    private var categoryLoadedPages: MutableList<Product> = mutableListOf()
    private var categoryNextPage = 1
    private var categoryHasMoreInternal = true

    private val _categoryPagingLoadingMore = MutableStateFlow(false)
    val categoryPagingLoadingMore = _categoryPagingLoadingMore.asStateFlow()

    private val _categoryHasMore = MutableStateFlow(true)
    val categoryHasMore = _categoryHasMore.asStateFlow()

    // ====== Load all products with caching ======
    fun loadProducts(page: Int = 1, perPage: Int = 20, categoryId: Int? = null, search: String? = null) {
        if (isProductsLoaded && cachedProductList != null && categoryId == null && search.isNullOrBlank()) {
            _productsState.value = if (cachedProductList!!.isEmpty()) ListUiState.Empty else ListUiState.Success(cachedProductList!!)
            return
        }

        viewModelScope.launch {
            _productsState.value = ListUiState.Loading
            _currentPage = page
            _currentPerPage = perPage
            try {
                val products = withContext(Dispatchers.Default) {
                    repository.getAllProducts(page = page, perPage = perPage, categoryId = categoryId, search = search)
                }
                products.forEach { cachedProducts[it.id] = it }

                // Store all products for local search
                allLoadedProducts = (allLoadedProducts + products).distinctBy { it.id }

                if (categoryId == null && search.isNullOrBlank()) {
                    cachedProductList = products
                    isProductsLoaded = true
                }
                _productsState.value = if (products.isEmpty()) ListUiState.Empty else ListUiState.Success(products)
            } catch (e: NetworkException) {
                _productsState.value = ListUiState.Error(e.message ?: NetworkException.DEFAULT_MESSAGE)
            } catch (e: Exception) {
                _productsState.value = ListUiState.Error("Failed to load products: ${e.message}")
            }
        }
    }

    // ====== Load featured products ======
    fun loadFeaturedProducts(limit: Int = 10) {
        if (isFeaturedLoaded && cachedFeaturedProducts != null) {
            _productsState.value = if (cachedFeaturedProducts!!.isEmpty()) ListUiState.Empty else ListUiState.Success(cachedFeaturedProducts!!)
            return
        }

        viewModelScope.launch {
            _productsState.value = ListUiState.Loading
            try {
                val products = withContext(Dispatchers.Default) {
                    repository.getFeaturedProducts(limit)
                }
                cachedFeaturedProducts = products
                isFeaturedLoaded = true
                allLoadedProducts = (allLoadedProducts + products).distinctBy { it.id }
                _productsState.value = if (products.isEmpty()) ListUiState.Empty else ListUiState.Success(products)
            } catch (e: NetworkException) {
                _productsState.value = ListUiState.Error(e.message ?: NetworkException.DEFAULT_MESSAGE)
            } catch (e: Exception) {
                _productsState.value = ListUiState.Error("Failed to load featured products: ${e.message}")
            }
        }
    }

    // ====== Load sale products ======
    fun loadProductsOnSale(limit: Int = 20) {
        if (isSaleLoaded && cachedSaleProducts != null) {
            _productsState.value = if (cachedSaleProducts!!.isEmpty()) ListUiState.Empty else ListUiState.Success(cachedSaleProducts!!)
            return
        }

        viewModelScope.launch {
            _productsState.value = ListUiState.Loading
            try {
                val products = withContext(Dispatchers.Default) {
                    repository.getProductsOnSale(limit)
                }
                cachedSaleProducts = products
                isSaleLoaded = true
                allLoadedProducts = (allLoadedProducts + products).distinctBy { it.id }
                _productsState.value = if (products.isEmpty()) ListUiState.Empty else ListUiState.Success(products)
            } catch (e: NetworkException) {
                _productsState.value = ListUiState.Error(e.message ?: NetworkException.DEFAULT_MESSAGE)
            } catch (e: Exception) {
                _productsState.value = ListUiState.Error("Failed to load sale products: ${e.message}")
            }
        }
    }

    // ====== Get product by ID with caching ======
    fun getProductById(productId: String) {
        viewModelScope.launch {
            _selectedProductState.value = SingleUiState.Loading
            try {
                val cached = productId.toIntOrNull()?.let { cachedProducts[it] }
                val product = cached ?: withContext(Dispatchers.Default) {
                    repository.getProductById(productId)
                }
                product?.let { cachedProducts[it.id] = it }
                _selectedProductState.value = if (product != null) SingleUiState.Success(product)
                else SingleUiState.Error("Product not found")
            } catch (e: NetworkException) {
                _selectedProductState.value = SingleUiState.Error(e.message ?: NetworkException.DEFAULT_MESSAGE)
            } catch (e: Exception) {
                _selectedProductState.value = SingleUiState.Error("Failed to load product: ${e.message}")
            }
        }
    }

    // ====== CRUD operations ======
    fun createProduct(productRequest: WcProductCreateRequest) {
        viewModelScope.launch {
            _operationState.value = OperationUiState.Loading
            try {
                val result = withContext(Dispatchers.Default) {
                    repository.createProduct(productRequest)
                }
                _operationState.value = if (result != null) {
                    loadProducts(_currentPage, _currentPerPage)
                    OperationUiState.Success
                } else OperationUiState.Error("Failed to create product")
            } catch (e: NetworkException) {
                _operationState.value = OperationUiState.Error(e.message ?: NetworkException.DEFAULT_MESSAGE)
            } catch (e: Exception) {
                _operationState.value = OperationUiState.Error("Error creating product: ${e.message}")
            }
        }
    }

    fun updateProduct(productId: String, productRequest: WcProductUpdateRequest) {
        viewModelScope.launch {
            _operationState.value = OperationUiState.Loading
            try {
                val result = withContext(Dispatchers.Default) {
                    repository.updateProduct(productId, productRequest)
                }
                _operationState.value = if (result != null) {
                    loadProducts(_currentPage, _currentPerPage)
                    OperationUiState.Success
                } else OperationUiState.Error("Failed to update product")
            } catch (e: NetworkException) {
                _operationState.value = OperationUiState.Error(e.message ?: NetworkException.DEFAULT_MESSAGE)
            } catch (e: Exception) {
                _operationState.value = OperationUiState.Error("Error updating product: ${e.message}")
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            _operationState.value = OperationUiState.Loading
            try {
                val result = withContext(Dispatchers.Default) {
                    repository.deleteProduct(productId)
                }
                _operationState.value = if (result) {
                    loadProducts(_currentPage, _currentPerPage)
                    OperationUiState.Success
                } else OperationUiState.Error("Failed to delete product")
            } catch (e: NetworkException) {
                _operationState.value = OperationUiState.Error(e.message ?: NetworkException.DEFAULT_MESSAGE)
            } catch (e: Exception) {
                _operationState.value = OperationUiState.Error("Error deleting product: ${e.message}")
            }
        }
    }

    // ====== LOCAL SEARCH AND FILTER - No API Calls ======
    // ====== LOCAL SEARCH AND FILTER - No API Calls ======
    // Separate function for just filtering (no search query needed)
    fun filterProductsLocally(
        filters: dev.infa.page3.data.model.SearchFilters
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            withContext(Dispatchers.Main) {
                _searchResultsState.value = ListUiState.Loading
            }

            try {
                var filteredProducts = allLoadedProducts.toList()

                // 1. Filter by price range
                filters.minPrice?.let { min ->
                    filteredProducts = filteredProducts.filter { product ->
                        val price = product.price?.toDoubleOrNull()
                            ?: product.regularPrice?.toDoubleOrNull()
                            ?: 0.0
                        price >= min
                    }
                }

                filters.maxPrice?.let { max ->
                    filteredProducts = filteredProducts.filter { product ->
                        val price = product.price?.toDoubleOrNull()
                            ?: product.regularPrice?.toDoubleOrNull()
                            ?: Double.MAX_VALUE
                        price <= max
                    }
                }

                // 2. Filter by discount
                filters.minDiscount?.let { minDiscount ->
                    filteredProducts = filteredProducts.filter { product ->
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

                // 3. Filter by stock status
                filters.stock?.let { stockStatus ->
                    filteredProducts = filteredProducts.filter { product ->
                        when (stockStatus) {
                            "in_stock" -> product.stockStatus?.equals("instock", ignoreCase = true) == true
                            "out_of_stock" -> product.stockStatus?.equals("outofstock", ignoreCase = true) == true
                            else -> true
                        }
                    }
                }

                // 4. Filter by sizes
                if (filters.sizes.isNotEmpty()) {
                    filteredProducts = filteredProducts.filter { product ->
                        val hasSize = product.attributes?.any { attr ->
                            (attr.name?.equals("size", ignoreCase = true) == true ||
                                    attr.name?.equals("Size", ignoreCase = true) == true ||
                                    attr.name?.contains("size", ignoreCase = true) == true) &&
                                    attr.options?.any { option ->
                                        filters.sizes.any { selectedSize ->
                                            option.equals(selectedSize, ignoreCase = true)
                                        }
                                    } == true
                        } ?: false

                        hasSize
                    }
                }

                // 5. Sort results
                filteredProducts = when (filters.sort) {
                    "price_low" -> filteredProducts.sortedBy {
                        it.price?.toDoubleOrNull() ?: it.regularPrice?.toDoubleOrNull() ?: Double.MAX_VALUE
                    }
                    "price_high" -> filteredProducts.sortedByDescending {
                        it.price?.toDoubleOrNull() ?: it.regularPrice?.toDoubleOrNull() ?: 0.0
                    }
                    "newest" -> filteredProducts.sortedByDescending {
                        it.dateCreated
                    }
                    "oldest" -> filteredProducts.sortedBy {
                        it.dateCreated
                    }
                    "name_asc" -> filteredProducts.sortedBy {
                        it.name.lowercase()
                    }
                    "name_desc" -> filteredProducts.sortedByDescending {
                        it.name.lowercase()
                    }
                    else -> filteredProducts
                }

                withContext(Dispatchers.Main) {
                    _searchResultsState.value = when {
                        filteredProducts.isEmpty() -> ListUiState.Empty
                        else -> ListUiState.Success(filteredProducts)
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _searchResultsState.value = ListUiState.Error("Filter failed: ${e.message}")
                }
            }
        }
    }

    // Separate function for searching by query
    fun searchProductsLocally(
        searchQuery: String,
        filters: dev.infa.page3.data.model.SearchFilters
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            withContext(Dispatchers.Main) {
                _searchResultsState.value = ListUiState.Loading
            }

            try {
                var filteredProducts = allLoadedProducts.toList()

                // 1. Search by name (case-insensitive)
                if (searchQuery.isNotBlank()) {
                    filteredProducts = filteredProducts.filter { product ->
                        product.name.contains(searchQuery, ignoreCase = true) ||
                                product.description?.contains(searchQuery, ignoreCase = true) == true ||
                                product.shortDescription?.contains(searchQuery, ignoreCase = true) == true
                    }
                }

                // 2. Apply filters on top of search results
                filters.minPrice?.let { min ->
                    filteredProducts = filteredProducts.filter { product ->
                        val price = product.price?.toDoubleOrNull()
                            ?: product.regularPrice?.toDoubleOrNull()
                            ?: 0.0
                        price >= min
                    }
                }

                filters.maxPrice?.let { max ->
                    filteredProducts = filteredProducts.filter { product ->
                        val price = product.price?.toDoubleOrNull()
                            ?: product.regularPrice?.toDoubleOrNull()
                            ?: Double.MAX_VALUE
                        price <= max
                    }
                }

                filters.minDiscount?.let { minDiscount ->
                    filteredProducts = filteredProducts.filter { product ->
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
                    filteredProducts = filteredProducts.filter { product ->
                        when (stockStatus) {
                            "in_stock" -> product.stockStatus?.equals("instock", ignoreCase = true) == true
                            "out_of_stock" -> product.stockStatus?.equals("outofstock", ignoreCase = true) == true
                            else -> true
                        }
                    }
                }

                if (filters.sizes.isNotEmpty()) {
                    filteredProducts = filteredProducts.filter { product ->
                        val hasSize = product.attributes?.any { attr ->
                            (attr.name?.equals("size", ignoreCase = true) == true ||
                                    attr.name?.equals("Size", ignoreCase = true) == true ||
                                    attr.name?.contains("size", ignoreCase = true) == true) &&
                                    attr.options?.any { option ->
                                        filters.sizes.any { selectedSize ->
                                            option.equals(selectedSize, ignoreCase = true)
                                        }
                                    } == true
                        } ?: false

                        hasSize
                    }
                }

                // 3. Sort results
                filteredProducts = when (filters.sort) {
                    "price_low" -> filteredProducts.sortedBy {
                        it.price?.toDoubleOrNull() ?: it.regularPrice?.toDoubleOrNull() ?: Double.MAX_VALUE
                    }
                    "price_high" -> filteredProducts.sortedByDescending {
                        it.price?.toDoubleOrNull() ?: it.regularPrice?.toDoubleOrNull() ?: 0.0
                    }
                    "newest" -> filteredProducts.sortedByDescending {
                        it.dateCreated
                    }
                    "oldest" -> filteredProducts.sortedBy {
                        it.dateCreated
                    }
                    "name_asc" -> filteredProducts.sortedBy {
                        it.name.lowercase()
                    }
                    "name_desc" -> filteredProducts.sortedByDescending {
                        it.name.lowercase()
                    }
                    else -> filteredProducts
                }

                withContext(Dispatchers.Main) {
                    _searchResultsState.value = when {
                        filteredProducts.isEmpty() -> ListUiState.Empty
                        else -> ListUiState.Success(filteredProducts)
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _searchResultsState.value = ListUiState.Error("Search failed: ${e.message}")
                }
            }
        }
    }

    fun loadProductsByCategory(categoryId: String) {
        if (pagingCategoryId == categoryId && categoryLoadedPages.isNotEmpty()) {
            _productsState.value = ListUiState.Success(categoryLoadedPages.toList())
            return
        }
        pagingCategoryId = categoryId
        categoryLoadedPages = mutableListOf()
        categoryNextPage = 1
        categoryHasMoreInternal = true
        _categoryHasMore.value = true

        viewModelScope.launch {
            _productsState.value = ListUiState.Loading
            try {
                val products = withContext(Dispatchers.Default) {
                    repository.getProductsByCategory(categoryId, page = 1, perPage = categoryPerPage)
                }
                categoryLoadedPages = products.toMutableList()
                categoryHasMoreInternal = products.size >= categoryPerPage
                _categoryHasMore.value = categoryHasMoreInternal
                categoryNextPage = 2
                products.forEach { cachedProducts[it.id] = it }
                allLoadedProducts = (allLoadedProducts + products).distinctBy { it.id }
                _productsState.value = when {
                    products.isEmpty() -> ListUiState.Empty
                    else -> ListUiState.Success(categoryLoadedPages.toList())
                }
            } catch (e: NetworkException) {
                _productsState.value = ListUiState.Error(e.message ?: NetworkException.DEFAULT_MESSAGE)
            } catch (e: Exception) {
                _productsState.value = ListUiState.Error("Failed to load products: ${e.message}")
            }
        }
    }

    fun loadNextPageForCategory() {
        val catId = pagingCategoryId ?: return
        if (!categoryHasMoreInternal || _categoryPagingLoadingMore.value) return
        if (_productsState.value !is ListUiState.Success) return

        viewModelScope.launch {
            _categoryPagingLoadingMore.value = true
            try {
                val page = withContext(Dispatchers.Default) {
                    repository.getProductsByCategory(catId, page = categoryNextPage, perPage = categoryPerPage)
                }
                if (page.isEmpty()) {
                    categoryHasMoreInternal = false
                    _categoryHasMore.value = false
                } else {
                    val newItems = page.filter { p -> categoryLoadedPages.none { it.id == p.id } }
                    categoryLoadedPages.addAll(newItems)
                    categoryNextPage++
                    categoryHasMoreInternal = page.size >= categoryPerPage
                    _categoryHasMore.value = categoryHasMoreInternal
                    _productsState.value = ListUiState.Success(categoryLoadedPages.toList())
                    page.forEach { cachedProducts[it.id] = it }
                    allLoadedProducts = (allLoadedProducts + page).distinctBy { it.id }
                }
            } catch (e: NetworkException) {
                _productsState.value = ListUiState.Error(e.message ?: NetworkException.DEFAULT_MESSAGE)
            } catch (_: Exception) {
                // keep current list on page errors
            } finally {
                _categoryPagingLoadingMore.value = false
            }
        }
    }

    fun loadProductsBySubCategory(subCategoryId: String) {
        viewModelScope.launch {
            _productsState.value = ListUiState.Loading
            try {
                val products = withContext(Dispatchers.Default) {
                    repository.getProductsBySubCategory(subCategoryId, page = 1, perPage = categoryPerPage)
                }
                allLoadedProducts = (allLoadedProducts + products).distinctBy { it.id }
                _productsState.value = when {
                    products.isEmpty() -> ListUiState.Empty
                    else -> ListUiState.Success(products)
                }
            } catch (e: NetworkException) {
                _productsState.value = ListUiState.Error(e.message ?: NetworkException.DEFAULT_MESSAGE)
            } catch (e: Exception) {
                _productsState.value = ListUiState.Error("Failed to load products: ${e.message}")
            }
        }
    }

    fun clearSearchResults() {
        _searchResultsState.value = ListUiState.Idle
    }

    fun clearOperationState() {
        _operationState.value = OperationUiState.Idle
    }
}
