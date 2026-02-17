package dev.infa.page3.presentation.viewModel//package dev.infa.page3.presentation.viewModel
//
//import dev.infa.page3.data.model.*
//import dev.infa.page3.presentation.repositary.*
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.SupervisorJob
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//import dev.infa.page3.presentation.uiSatateClaases.ListUiState
//
//// ======================= UI STATES =======================
//
//// Generic UI State for lists
//
//
//
//
//
//
//
//// ======================= CATEGORY VIEW MODEL =======================
//
//
//
//// ======================= SUBCATEGORY VIEW MODEL =======================
//
//
//// ======================= PRODUCT VIEW MODEL =======================
//
//
//
//// ======================= RATING VIEW MODEL =======================
//
//
//
//// ======================= REVIEW VIEW MODEL =======================
//
//
//
//// ======================= MAIN/HOME VIEW MODEL =======================
//
//class MainViewModel(
//    private val eCommerceRepository: ECommerceRepository
//) {
//    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
//
//    private val _categoriesWithSubsState = MutableStateFlow<ListUiState<CategoryWithSubCategories>>(ListUiState.Idle)
//    val categoriesWithSubsState: StateFlow<ListUiState<CategoryWithSubCategories>> = _categoriesWithSubsState.asStateFlow()
//
//    private val _featuredProductsState = MutableStateFlow<ListUiState<Product>>(ListUiState.Idle)
//    val featuredProductsState: StateFlow<ListUiState<Product>> = _featuredProductsState.asStateFlow()
//
//    private val _saleProductsState = MutableStateFlow<ListUiState<Product>>(ListUiState.Idle)
//    val saleProductsState: StateFlow<ListUiState<Product>> = _saleProductsState.asStateFlow()
//
//    init {
//        loadHomeData()
//    }
//
//    private fun loadHomeData() {
//        loadCategoriesWithSubCategories()
//        loadFeaturedProducts()
//        loadSaleProducts()
//    }
//
//    private fun loadCategoriesWithSubCategories() {
//        viewModelScope.launch {
//            _categoriesWithSubsState.value = ListUiState.Loading
//            try {
//                val categoriesWithSubs = eCommerceRepository.getCategoriesWithSubCategories()
//                _categoriesWithSubsState.value = when {
//                    categoriesWithSubs.isEmpty() -> ListUiState.Empty
//                    else -> ListUiState.Success(categoriesWithSubs)
//                }
//            } catch (e: Exception) {
//                _categoriesWithSubsState.value = ListUiState.Error("Failed to load categories: ${e.message}")
//            }
//        }
//    }
//
//    private fun loadFeaturedProducts() {
//        viewModelScope.launch {
//            _featuredProductsState.value = ListUiState.Loading
//            try {
//                val products = eCommerceRepository.productRepository.getFeaturedProducts(10)
//                _featuredProductsState.value = when {
//                    products.isEmpty() -> ListUiState.Empty
//                    else -> ListUiState.Success(products)
//                }
//            } catch (e: Exception) {
//                _featuredProductsState.value = ListUiState.Error("Failed to load featured products: ${e.message}")
//            }
//        }
//    }
//
//    private fun loadSaleProducts() {
//        viewModelScope.launch {
//            _saleProductsState.value = ListUiState.Loading
//            try {
//                val products = eCommerceRepository.productRepository.getProductsOnSale(15)
//                _saleProductsState.value = when {
//                    products.isEmpty() -> ListUiState.Empty
//                    else -> ListUiState.Success(products)
//                }
//            } catch (e: Exception) {
//                _saleProductsState.value = ListUiState.Error("Failed to load sale products: ${e.message}")
//            }
//        }
//    }
//
//    fun refreshHomeData() {
//        loadHomeData()
//    }
//}
//
//
//
//
//
