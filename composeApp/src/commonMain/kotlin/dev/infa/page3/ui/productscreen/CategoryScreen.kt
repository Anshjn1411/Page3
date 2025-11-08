package dev.infa.page3.ui.productscreen


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.data.remote.SessionManager
import dev.infa.page3.navigation.CartScreenNav
import dev.infa.page3.navigation.ProductDetail
import dev.infa.page3.presentation.api.ApiService
import dev.infa.page3.presentation.repositary.ProductRepository
import dev.infa.page3.presentation.repository.WishlistRepository
import dev.infa.page3.presentation.uiSatateClaases.ListUiState
import dev.infa.page3.presentation.viewModel.AuthViewModel
import dev.infa.page3.presentation.viewModel.CartViewModel
import dev.infa.page3.presentation.viewModel.CategoryViewModel
import dev.infa.page3.presentation.viewModel.ProductViewModel
import dev.infa.page3.presentation.viewmodel.WishlistViewModel
import dev.infa.page3.ui.AppSideBar
import dev.infa.page3.ui.components.BottomNavBar
import dev.infa.page3.ui.components.EmptyScreen
import dev.infa.page3.ui.components.ErrorScreen
import dev.infa.page3.ui.components.LoadingScreen
import dev.infa.page3.ui.components.TopBarScreen
import dev.infa.page3.ui.productscreen.components.ProductCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    CategoryId: String,
    CategoryName: String,
    navigator: Navigator, wishlistViewModel: WishlistViewModel, productViewModel: ProductViewModel,

    cartViewModel: CartViewModel,
    authViewModel: AuthViewModel,
    categoryViewModel: CategoryViewModel
) {

    val productsState by productViewModel.productsState.collectAsState()

    LaunchedEffect(CategoryId) {
        productViewModel.loadProductsByCategory(categoryId = CategoryId)
    }
    val totalitem by cartViewModel.totalItems.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentTab by remember { mutableStateOf("home") }

    ModalNavigationDrawer(
        drawerContent = { AppSideBar(
            navigator = navigator, wishlistViewModel,
            authViewModel = authViewModel,
            productViewModel = productViewModel,
            cartViewModel =cartViewModel,
            categoryViewModel = categoryViewModel
        ) },
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
                // ✅ Category Info Section
                when (productsState) {
                    is ListUiState.Success -> {
                        val products = (productsState as ListUiState.Success).data
                        CategoryInfoSection(
                            categoryName = CategoryName,
                            totalProducts = products.size,
                            onFilterClick = { /* TODO: Open Filter Dialog */ }
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        val wishlistIds by wishlistViewModel.wishlistProductIds.collectAsState()


                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(products) { product ->
                                ProductCard(
                                    product = product,
                                    modifier = Modifier.fillMaxWidth(),
                                    onWishlistToggle = {
                                        wishlistViewModel.toggleWishlist(product)
                                    },
                                    onClick = {
                                        navigator.push(ProductDetail(product.id!!.toString()
                                        ))
                                    },
                                    isInWishlist = wishlistIds.contains(product.id)
                                )
                            }
                        }
                    }

                    is ListUiState.Loading -> LoadingScreen(modifier = Modifier.fillMaxSize())
                    is ListUiState.Error -> ErrorScreen(message = (productsState as ListUiState.Error).message)
                    is ListUiState.Empty -> EmptyScreen(message = "No products found")
                    ListUiState.Idle -> Unit
                }
            }
        }
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
            .background(Color(0xFFF8F8F8)) // light off-white background
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
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

    }
}


