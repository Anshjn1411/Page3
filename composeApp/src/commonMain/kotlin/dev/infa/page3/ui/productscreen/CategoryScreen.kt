package dev.infa.page3.ui.productscreen


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.data.model.SearchFilters
import dev.infa.page3.data.model.SearchState
import dev.infa.page3.data.model.applySearchFilters
import dev.infa.page3.navigation.CartScreenNav
import dev.infa.page3.navigation.ProductDetail
import dev.infa.page3.presentation.uiSatateClaases.ListUiState
import dev.infa.page3.presentation.viewModel.AuthViewModel
import dev.infa.page3.presentation.viewModel.CartViewModel
import dev.infa.page3.presentation.viewModel.CategoryViewModel
import dev.infa.page3.presentation.viewModel.ProductViewModel
import dev.infa.page3.presentation.viewmodel.WishlistViewModel
import dev.infa.page3.ui.AppSideBar
import dev.infa.page3.ui.components.BottomNavBar
import dev.infa.page3.ui.components.EmptyStateMessages
import dev.infa.page3.ui.components.ErrorScreen
import dev.infa.page3.ui.components.LoadingScreen
import dev.infa.page3.ui.components.ProductFilterBottomSheet
import dev.infa.page3.ui.components.ProductsEmptyState
import dev.infa.page3.ui.components.TopBarScreen
import dev.infa.page3.ui.productscreen.components.ProductCard
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    CategoryId: String,
    CategoryName: String,
    navigator: Navigator,
    wishlistViewModel: WishlistViewModel,
    productViewModel: ProductViewModel,
    cartViewModel: CartViewModel,
    authViewModel: AuthViewModel,
    categoryViewModel: CategoryViewModel
) {

    val productsState by productViewModel.productsState.collectAsState()
    val categoryHasMore by productViewModel.categoryHasMore.collectAsState()
    val categoryLoadingMore by productViewModel.categoryPagingLoadingMore.collectAsState()

    var filterUi by remember { mutableStateOf(SearchState()) }
    val gridState = rememberLazyGridState()

    LaunchedEffect(CategoryId) {
        filterUi = SearchState()
        productViewModel.loadProductsByCategory(categoryId = CategoryId)
    }

    LaunchedEffect(gridState, productsState, categoryHasMore) {
        snapshotFlow {
            val info = gridState.layoutInfo
            val total = info.totalItemsCount
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            total > 0 && lastVisible >= total - 3
        }
            .distinctUntilChanged()
            .filter { it && categoryHasMore && productsState is ListUiState.Success }
            .collect {
                productViewModel.loadNextPageForCategory()
            }
    }

    val totalitem by cartViewModel.totalItems.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentTab by remember { mutableStateOf("home") }

    ModalNavigationDrawer(
        drawerContent = {
            AppSideBar(
                navigator = navigator,
                wishlistViewModel,
                authViewModel = authViewModel,
                productViewModel = productViewModel,
                cartViewModel = cartViewModel,
                categoryViewModel = categoryViewModel
            )
        },
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                TopBarScreen(
                    onClickMenu = { scope.launch { drawerState.open() } },
                    onClickShop = { navigator.push(CartScreenNav()) },
                    totalitem
                )
            },
            bottomBar = {
                BottomNavBar(
                    currentNav = currentTab,
                    navigator,
                    categoryViewModel = categoryViewModel,
                    productViewModel = productViewModel,
                    wishListViewModel = wishlistViewModel,
                    cartViewModel = cartViewModel,
                    authViewModel = authViewModel
                )
            }
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (val state = productsState) {
                    is ListUiState.Success -> {
                        val rawProducts = state.data
                        val displayProducts by remember(rawProducts, filterUi.filters) {
                            derivedStateOf {
                                if (filterUi.filters == SearchFilters()) rawProducts
                                else rawProducts.applySearchFilters(filterUi.filters)
                            }
                        }

                        CategoryInfoSection(
                            categoryName = CategoryName,
                            totalProducts = rawProducts.size,
                            onFilterClick = { filterUi = filterUi.copy(isFilterSheetOpen = true) }
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        val wishlistIds by wishlistViewModel.wishlistProductIds.collectAsState()

                        if (displayProducts.isEmpty()) {
                            ProductsEmptyState(
                                modifier = Modifier.fillMaxSize(),
                                message = EmptyStateMessages.PRODUCTS_SOON
                            )
                        } else {
                            Box(Modifier.fillMaxSize()) {
                                LazyVerticalGrid(
                                    state = gridState,
                                    columns = GridCells.Fixed(2),
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(displayProducts, key = { it.id }) { product ->
                                        ProductCard(
                                            product = product,
                                            modifier = Modifier.fillMaxWidth(),
                                            onWishlistToggle = {
                                                wishlistViewModel.toggleWishlist(product)
                                            },
                                            onClick = {
                                                navigator.push(
                                                    ProductDetail(product.id.toString())
                                                )
                                            },
                                            isInWishlist = wishlistIds.contains(product.id)
                                        )
                                    }
                                }
                                if (categoryLoadingMore) {
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.BottomCenter)
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(36.dp),
                                            strokeWidth = 3.dp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    is ListUiState.Loading -> LoadingScreen(modifier = Modifier.fillMaxSize())
                    is ListUiState.Error -> ErrorScreen(message = state.message)
                    is ListUiState.Empty -> ProductsEmptyState(
                        modifier = Modifier.fillMaxSize(),
                        message = EmptyStateMessages.PRODUCTS_SOON
                    )
                    ListUiState.Idle -> Unit
                }
            }
        }
    }

    if (filterUi.isFilterSheetOpen) {
        ProductFilterBottomSheet(
            filters = filterUi.filters,
            onDismiss = { filterUi = filterUi.copy(isFilterSheetOpen = false) },
            onApplyFilters = { newFilters ->
                filterUi = filterUi.copy(
                    filters = newFilters,
                    isFilterSheetOpen = false
                )
            }
        )
    }
}

@Composable
fun CategoryInfoSection(
    categoryName: String,
    totalProducts: Int,
    onFilterClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8F8F8))
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = categoryName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "$totalProducts products",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
        IconButton(onClick = onFilterClick) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filters",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
