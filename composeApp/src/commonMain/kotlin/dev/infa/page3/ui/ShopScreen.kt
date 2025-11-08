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
import dev.infa.page3.presentation.api.ApiService
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
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
                                    contentPadding = PaddingValues(12.dp),
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
                                EmptyScreen(message = "No products found")
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
                                                Spacer(Modifier.height(20.dp))
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
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("No products available")
                                                }
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
                                EmptyScreen(message = "No categories found")
                            }

                            ListUiState.Idle -> Unit
                        }
                    }
                }
            }
        }

        // Filter Bottom Sheet
        if (searchState.isFilterSheetOpen) {
            FilterBottomSheet(
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




@Composable
fun SearchBarWithFilters(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    hasActiveFilters: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(50.dp)
            .border(
                width = 1.4.dp,
                color = if (hasActiveFilters) MaterialTheme.colorScheme.primary else Color.Gray,
                shape = RoundedCornerShape(7.dp)
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 12.dp, end = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.Gray,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search products...", color = Color.Gray, fontSize = 16.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                singleLine = true
            )

            // Filter Button
            IconButton(onClick = onFilterClick) {
                BadgedBox(
                    badge = {
                        if (hasActiveFilters) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(8.dp)
                            )
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filters",
                        tint = if (hasActiveFilters) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
            }
        }
    }
}
@Composable
fun ActiveFiltersChips(
    filters: SearchFilters,
    onClearAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = false,
            onClick = onClearAll,
            label = { Text("Clear All", fontSize = 12.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        )

        Text(
            text = filters.toQueryString(),
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

@Composable
fun SortDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    currentSort: String?,
    onSortSelected: (String) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        val sortOptions = listOf(
            "price_low" to "Price: Low to High",
            "price_high" to "Price: High to Low",
            "newest" to "Newest First",
            "oldest" to "Oldest First",
            "name_asc" to "Name: A to Z",
            "name_desc" to "Name: Z to A"
        )

        sortOptions.forEach { (value, label) ->
            DropdownMenuItem(
                text = { Text(label) },
                onClick = { onSortSelected(value) },
                leadingIcon = if (currentSort == value) {
                    { Icon(Icons.Default.Check, contentDescription = null) }
                } else null
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    filters: SearchFilters,
    onDismiss: () -> Unit,
    onApplyFilters: (SearchFilters) -> Unit
) {
    var tempFilters by remember { mutableStateOf(filters) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier
            .fillMaxHeight(0.9f)
            .background(Color.White),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filters",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                TextButton(onClick = { tempFilters = SearchFilters() }) {
                    Text("Reset All", color = Color.Black)
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray)

            // Scrollable Filters
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Price Range Filter
                item {
                    PriceRangeFilterWithSlider(
                        minPrice = tempFilters.minPrice?.toInt() ?: 0,
                        maxPrice = tempFilters.maxPrice?.toInt() ?: 1000,
                        onPriceChange = { min, max ->
                            tempFilters = tempFilters.copy(minPrice = min, maxPrice = max)
                        }
                    )
                }

                // Size Filter
                item {
                    SizeFilter(
                        selectedSizes = tempFilters.sizes,
                        onSizesChange = { tempFilters = tempFilters.copy(sizes = it) }
                    )
                }

                // Stock Status Filter
                item {
                    StockStatusFilter(
                        selectedStock = tempFilters.stock,
                        onStockChange = { tempFilters = tempFilters.copy(stock = it) }
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray)

            // Apply Button
            Button(
                onClick = { onApplyFilters(tempFilters) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Apply Filters", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// Price Range Filter with Range Slider
@Composable
fun PriceRangeFilterWithSlider(
    minPrice: Int,
    maxPrice: Int,
    onPriceChange: (Int, Int) -> Unit
) {
    var localMinPrice by remember { mutableStateOf(minPrice.toFloat()) }
    var localMaxPrice by remember { mutableStateOf(maxPrice.toFloat()) }

    val maxRange = 100f

    Column {
        Text(
            text = "Filter by price",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.Black
        )

        Spacer(Modifier.height(16.dp))

        // Range Slider with black styling
        RangeSlider(
            value = localMinPrice..localMaxPrice,
            onValueChange = { range ->
                localMinPrice = range.start
                localMaxPrice = range.endInclusive
            },
            valueRange = 0f..1000f,
            onValueChangeFinished = {
                onPriceChange(localMinPrice.toInt(), localMaxPrice.toInt())
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            steps = 99
        )
        Spacer(Modifier.height(16.dp))

        // Price Display Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp)
                )
                .border(1.dp, Color.LightGray, shape = RoundedCornerShape(12.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Price: ₹${localMinPrice.toInt()} — ₹${localMaxPrice.toInt()}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            Button(
                onClick = {
                    onPriceChange(localMinPrice.toInt(), localMaxPrice.toInt())
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text("Filter", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Size Filter
@Composable
fun SizeFilter(
    selectedSizes: List<String>,
    onSizesChange: (List<String>) -> Unit
) {
    val sizes = listOf("XS", "S", "M", "L", "XL", "XXL")

    Column {
        Text(
            text = "By size",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.Black
        )

        Spacer(Modifier.height(12.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            sizes.forEach { size ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            val newSizes = if (selectedSizes.contains(size)) {
                                selectedSizes.filter { it != size }
                            } else {
                                selectedSizes + size
                            }
                            onSizesChange(newSizes)
                        }
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = size,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (selectedSizes.contains(size)) Color.Black else Color.Gray
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(
                                    color = if (selectedSizes.contains(size)) Color.Black else Color.White,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .border(
                                    width = 2.dp,
                                    color = Color.Black,
                                    shape = RoundedCornerShape(4.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedSizes.contains(size)) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Stock Status Filter
@Composable
fun StockStatusFilter(
    selectedStock: String?,
    onStockChange: (String?) -> Unit
) {
    Column {
        Text(
            text = "Stock Status",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.Black
        )

        Spacer(Modifier.height(12.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf("in_stock" to "In Stock", "out_of_stock" to "Out of Stock").forEach { (value, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            onStockChange(if (selectedStock == value) null else value)
                        }
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (selectedStock == value) Color.Black else Color.Gray
                    )

                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                color = if (selectedStock == value) Color.Black else Color.White,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .border(
                                width = 2.dp,
                                color = Color.Black,
                                shape = RoundedCornerShape(4.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedStock == value) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


