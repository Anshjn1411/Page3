package dev.infa.page3.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.Navigator
import com.seiko.imageloader.rememberImagePainter
import dev.infa.page3.data.model.SearchFilters
import dev.infa.page3.data.model.SearchState
import dev.infa.page3.data.model.WcCategory
import dev.infa.page3.data.remote.SessionManager
import dev.infa.page3.navigation.CartScreenNav
import dev.infa.page3.navigation.CategoriesOverviewScreenNav
import dev.infa.page3.navigation.CategoryScreenNav
import dev.infa.page3.navigation.ProductDetail
import dev.infa.page3.navigation.ProfileScreenNav
import dev.infa.page3.navigation.WishlistScreenNav
import dev.infa.page3.presentation.api.*
import dev.infa.page3.presentation.repositary.CategoryRepository
import dev.infa.page3.presentation.repositary.ProductRepository
import dev.infa.page3.presentation.repository.WishlistRepository
import dev.infa.page3.presentation.viewModel.CategoryViewModel
import dev.infa.page3.presentation.uiSatateClaases.ListUiState
import dev.infa.page3.presentation.viewModel.AuthViewModel
import dev.infa.page3.presentation.viewModel.CartViewModel
import dev.infa.page3.presentation.viewModel.ProductViewModel
import dev.infa.page3.presentation.viewmodel.WishlistViewModel
import dev.infa.page3.ui.components.*
import dev.infa.page3.ui.components.AppFloatingNavBottomPadding
import dev.infa.page3.ui.productscreen.components.ProductCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    navigator: Navigator, wishlistViewModel: WishlistViewModel, productViewModel: ProductViewModel,
    cartViewModel: CartViewModel,
    authViewModel: AuthViewModel,
    categoryViewModel: CategoryViewModel
) {

    val categoriesState by categoryViewModel.categoriesState.collectAsState()
    val searchResultsState by productViewModel.searchResultsState.collectAsState()
    val productsState by productViewModel.productsState.collectAsState()

    var searchState by remember { mutableStateOf(SearchState()) }
    var showSortMenu by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf("categories") }
    var currentPage by remember { mutableStateOf(1) }
    var isLoadingMore by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Load initial products
    LaunchedEffect(Unit) {
        productViewModel.loadProducts(page = 1, perPage = 10)
    }
    val totalitem by cartViewModel.totalItems.collectAsState()

    val hasActiveSearch = searchState.query.isNotBlank()
    val hasActiveFilters = searchState.filters != SearchFilters()
    val shouldShowFilteredResults = hasActiveSearch || hasActiveFilters

    // Perform search/filter when query or filters change
    LaunchedEffect(searchState.query, searchState.filters) {
        if (searchState.query.isNotBlank()) {
            // If there's a search query, use search function
            productViewModel.searchProductsLocally(
                searchQuery = searchState.query,
                filters = searchState.filters
            )
        } else if (searchState.filters != SearchFilters()) {
            // If there's NO search query but filters ARE active, use filter-only function
            productViewModel.filterProductsLocally(
                filters = searchState.filters
            )
        } else {
            // No search query and no filters - clear results
            productViewModel.clearSearchResults()
        }
    }

    ModalNavigationDrawer(
        drawerContent = {
            AppSideBar(
                navigator = navigator, wishlistViewModel,
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
                    onClickShop = { navigator.push(CartScreenNav(
                    )) },
                    totalitem
                )
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = AppFloatingNavBottomPadding)
                ) {
                    Spacer(Modifier.height(8.dp))

                    // Search Bar with Filter Button
                    SearchBarWithFilters(
                        query = searchState.query,
                        onQueryChange = { searchState = searchState.copy(query = it) },
                        onFilterClick = {
                            searchState = searchState.copy(isFilterSheetOpen = true)
                        },
                        hasActiveFilters = searchState.filters != SearchFilters()
                    )

                    Spacer(Modifier.height(8.dp))

                    // Active Filters Display
                    if (searchState.filters != SearchFilters()) {
                        ActiveFiltersChips(
                            filters = searchState.filters,
                            onClearAll = {
                                searchState = searchState.copy(filters = SearchFilters())
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    // Sort Button Row (show when searching OR filtering)
                    if (shouldShowFilteredResults) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (searchResultsState) {
                                    is ListUiState.Success -> "${(searchResultsState as ListUiState.Success).data.size} Results"
                                    else -> "Searching..."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )

                            Box {
                                OutlinedButton(
                                    onClick = { showSortMenu = true },
                                    contentPadding = PaddingValues(
                                        horizontal = 12.dp,
                                        vertical = 4.dp
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sort,
                                        contentDescription = "Sort",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = searchState.filters.sort?.let {
                                            when (it) {
                                                "price_high" -> "Price ↓"
                                                "price_low" -> "Price ↑"
                                                "newest" -> "Newest"
                                                "rating" -> "Rating"
                                                else -> "Sort"
                                            }
                                        } ?: "Sort",
                                        fontSize = 14.sp
                                    )
                                }

                                SortDropdownMenu(
                                    expanded = showSortMenu,
                                    onDismiss = { showSortMenu = false },
                                    currentSort = searchState.filters.sort,
                                    onSortSelected = { sort ->
                                        searchState = searchState.copy(
                                            filters = searchState.filters.copy(sort = sort)
                                        )
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    // Main Content
                    if (shouldShowFilteredResults) {
                        // Show Search/Filter Results
                        when (searchResultsState) {
                            is ListUiState.Loading -> {
                                LoadingScreen(modifier = Modifier.fillMaxSize())
                            }

                            is ListUiState.Success -> {
                                val products = (searchResultsState as ListUiState.Success).data
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    contentPadding = PaddingValues(
                                        start = 12.dp,
                                        end = 12.dp,
                                        top = 12.dp,
                                        bottom = AppFloatingNavBottomPadding
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(products) { product ->
                                        ProductCard(
                                            product = product,
                                            onClick = { navigator.push(ProductDetail(
                                                product.id.toString()
                                            )) },
                                            isInWishlist = wishlistViewModel.isProductInWishlist(product.id)
                                        )
                                    }
                                }
                            }

                            is ListUiState.Error -> {
                                ErrorScreen(message = (searchResultsState as ListUiState.Error).message)
                            }

                            is ListUiState.Empty -> {
                                ProductsEmptyState(message = EmptyStateMessages.PRODUCTS_SOON)
                            }

                            ListUiState.Idle -> Unit
                        }
                    } else {
                        // Show Categories and Products (default view)
                        when (categoriesState) {
                            is ListUiState.Loading -> {
                                LoadingScreen(modifier = Modifier.fillMaxSize())
                            }

                            is ListUiState.Success -> {
                                val categories = (categoriesState as ListUiState.Success).data

                                LazyColumn(
                                    modifier = Modifier.fillMaxSize()
                                ) {

                                    // Categories Section
                                    item {
                                        Column {
                                            Spacer(Modifier.height(10.dp))

                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                val categoryScrollState = rememberLazyListState()

                                                LazyRow(
                                                    state = categoryScrollState,
                                                    contentPadding = PaddingValues(horizontal = 20.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    items(categories) { category ->
                                                        CategoryItem(
                                                            category = category,
                                                            onClick = { navigator.push(
                                                                CategoryScreenNav(category.id.toString(), category.name))}
                                                        )
                                                    }
                                                }

                                                val layoutInfo = categoryScrollState.layoutInfo
                                                val viewportWidth = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
                                                val totalWidth = layoutInfo.totalItemsCount * (layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 200)

                                                val scrollOffset = categoryScrollState.firstVisibleItemIndex *
                                                        (layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 200) +
                                                        categoryScrollState.firstVisibleItemScrollOffset

                                                val scrollProgress = if (totalWidth > viewportWidth) {
                                                    (scrollOffset.toFloat() / (totalWidth - viewportWidth)).coerceIn(0f, 1f)
                                                } else {
                                                    0f
                                                }

                                                val indicatorWidthFraction = 0.5f

                                                Box(
                                                    modifier = Modifier
                                                        .padding(top = 12.dp)
                                                        .height(3.dp)
                                                        .fillMaxWidth(indicatorWidthFraction)
                                                        .clip(RoundedCornerShape(50))
                                                        .background(Color.LightGray.copy(alpha = 0.3f))
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth(indicatorWidthFraction * 0.8f)
                                                            .fillMaxHeight()
                                                            .offset(
                                                                x = with(LocalDensity.current) {
                                                                    (scrollProgress * (1f - indicatorWidthFraction * 0.8f) *
                                                                            (viewportWidth * indicatorWidthFraction)).toDp()
                                                                }
                                                            )
                                                            .background(Color(0xFF4CAF50), shape = RoundedCornerShape(50))
                                                    )
                                                }
                                            }

                                            Spacer(Modifier.height(20.dp))
                                        }
                                    }

                                    // Products Grid Section
                                    item {
                                        Text(
                                            text = "All Products",
                                            style = MaterialTheme.typography.headlineSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 20.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                                        )
                                    }

                                    when (productsState) {
                                        is ListUiState.Loading -> {
                                            item {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(200.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    CircularProgressIndicator()
                                                }
                                            }
                                        }

                                        is ListUiState.Success -> {
                                            val allProducts = (productsState as ListUiState.Success).data

                                            items(allProducts.chunked(2)) { rowProducts ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                ) {
                                                    rowProducts.forEach { product ->
                                                        Box(modifier = Modifier.weight(1f)) {
                                                            ProductCard(
                                                                product = product,
                                                                onClick = {
                                                                    navigator.push(
                                                                        ProductDetail(
                                                                            product.id.toString(),

                                                                        )
                                                                    )
                                                                },
                                                                isInWishlist = wishlistViewModel.isProductInWishlist(product.id)
                                                            )
                                                        }
                                                    }
                                                    if (rowProducts.size == 1) {
                                                        Spacer(modifier = Modifier.weight(1f))
                                                    }
                                                }
                                            }

                                            // Load More Button
                                            item {
                                                if (!isLoadingMore) {
                                                    Button(
                                                        onClick = {
                                                            isLoadingMore = true
                                                            currentPage++
                                                            scope.launch {
                                                                productViewModel.loadProducts(
                                                                    page = currentPage,
                                                                    perPage = 10
                                                                )
                                                                delay(500)
                                                                isLoadingMore = false
                                                            }
                                                        },
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(16.dp),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = Color.Black
                                                        ),
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Text(
                                                            text = "Load More Products",
                                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                        )
                                                    }
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(16.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        CircularProgressIndicator()
                                                    }
                                                }
                                            }

                                            item {
                                                Spacer(Modifier.height(AppFloatingNavBottomPadding))
                                            }
                                        }

                                        is ListUiState.Error -> {
                                            item {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = (productsState as ListUiState.Error).message,
                                                        color = Color.Red
                                                    )
                                                }
                                            }
                                        }

                                        is ListUiState.Empty -> {
                                            item {
                                                ProductsEmptyState(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(220.dp),
                                                    message = EmptyStateMessages.PRODUCTS_SOON
                                                )
                                            }
                                        }

                                        ListUiState.Idle -> Unit
                                    }
                                }
                            }

                            is ListUiState.Error -> {
                                ErrorScreen(message = (categoriesState as ListUiState.Error).message)
                            }

                            is ListUiState.Empty -> {
                                ProductsEmptyState(message = EmptyStateMessages.CATEGORIES_SOON)
                            }

                            ListUiState.Idle -> Unit
                        }
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
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
        }

        // Filter Bottom Sheet
        if (searchState.isFilterSheetOpen) {
            ProductFilterBottomSheet(
                filters = searchState.filters,
                onDismiss = { searchState = searchState.copy(isFilterSheetOpen = false) },
                onApplyFilters = { newFilters ->
                    searchState = searchState.copy(
                        filters = newFilters,
                        isFilterSheetOpen = false
                    )
                }
            )
        }
    }
}
@Composable
fun CategoryItem(
    category: WcCategory,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val painter = rememberImagePainter(category.image?.src.toString())

        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
                .background(Color(0xFFF5F5F5))
        ) {
            Image(
                painter = painter,
                contentDescription = category.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = Color.Gray
        )
    }
}
