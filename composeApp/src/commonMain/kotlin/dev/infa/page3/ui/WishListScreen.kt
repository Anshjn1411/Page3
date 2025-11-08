package dev.infa.page3.ui
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import dev.infa.page3.presentation.uiSatateClaases.ListUiState
import dev.infa.page3.presentation.uiSatateClaases.OperationUiState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.data.model.Product
import dev.infa.page3.data.remote.SessionManager
import dev.infa.page3.navigation.AuthManager
import dev.infa.page3.navigation.CartScreenNav
import dev.infa.page3.navigation.CategoriesOverviewScreenNav
import dev.infa.page3.navigation.HomeMainScreen
import dev.infa.page3.navigation.OTPScreen
import dev.infa.page3.navigation.ProductDetail
import dev.infa.page3.navigation.ProfileScreenNav
import dev.infa.page3.presentation.api.ApiService
import dev.infa.page3.presentation.repositary.AuthRepository
import dev.infa.page3.presentation.repository.WishlistRepository
import dev.infa.page3.presentation.uiSatateClaases.AuthUiState
import dev.infa.page3.presentation.viewModel.AuthViewModel
import dev.infa.page3.presentation.viewModel.CartViewModel
import dev.infa.page3.presentation.viewModel.CategoryViewModel
import dev.infa.page3.presentation.viewModel.ProductViewModel
import dev.infa.page3.presentation.viewmodel.WishlistViewModel
import dev.infa.page3.ui.components.BottomNavBar
import dev.infa.page3.ui.components.ErrorScreen
import dev.infa.page3.ui.components.LoadingScreen
import dev.infa.page3.ui.components.LoginButton
import dev.infa.page3.ui.components.SignUpButton
import dev.infa.page3.ui.components.TopBarScreen
import dev.infa.page3.ui.productscreen.components.ProductCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(navigator: Navigator, wishlistViewModel: WishlistViewModel, productViewModel: ProductViewModel,

                   cartViewModel: CartViewModel,
                   authViewModel: AuthViewModel,
                   categoryViewModel: CategoryViewModel) {

    val wishlistState by wishlistViewModel.wishlistState.collectAsState()
    val wishlistProductIds by wishlistViewModel.wishlistProductIds.collectAsState()
    val actionState by wishlistViewModel.actionState.collectAsState()
    val totalitem by cartViewModel.totalItems.collectAsState()

    val uiState by AuthManager.authState.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        wishlistViewModel.loadWishlist()
    }

    var currentTab by remember { mutableStateOf("wishlist") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionState) {
        when (actionState) {
            is OperationUiState.Success -> {
                snackbarHostState.showSnackbar("Item updated successfully")
                wishlistViewModel.resetActionState()
            }

            is OperationUiState.Error -> {
                snackbarHostState.showSnackbar(
                    (actionState as OperationUiState.Error).message
                )
                wishlistViewModel.resetActionState()
            }

            else -> {}
        }
    }

    when (uiState) {
        is AuthUiState.LoggedIn -> {
            ModalNavigationDrawer(
                drawerContent = {
                    AppSideBar(
                        navigator = navigator, wishlistViewModel, cartViewModel = cartViewModel,
                        categoryViewModel = categoryViewModel,
                        productViewModel = productViewModel,
                        authViewModel = authViewModel,
                    )
                },
                drawerState = drawerState
            ) {

                Scaffold(
                    topBar = {
                        TopBarScreen(
                            onClickMenu = { scope.launch { drawerState.open() } },
                            onClickShop = {
                                navigator.push(
                                    CartScreenNav(
                                    )
                                )
                            },
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
                        when (val state = wishlistState) {
                            is ListUiState.Loading -> LoadingScreen()
                            is ListUiState.Empty -> {
                                EmptyWishlistView(onStartShopping = { navigator.pop() })
                            }

                            is ListUiState.Success -> {
                                Column(
                                    Modifier.fillMaxSize()
                                ) {
                                    TopSection(
                                        name = "WishList",
                                        totalItems = state.data.size// Assuming Product has a `price` property
                                    )
                                    WishlistGridContent(
                                        items = state.data,
                                        wishlistIds = wishlistProductIds,
                                        onWishlistToggle = { product ->
                                            wishlistViewModel.toggleWishlist(product)
                                        },
                                        onProductClick = { product ->
                                            navigator.push(
                                                ProductDetail(
                                                    product.id.toString(),

                                                )
                                            )
                                        }
                                    )

                                }


                            }

                            is ListUiState.Error -> {
                                ErrorScreen(message = state.message)
                            }

                            else -> {}
                        }
                    }
                }
            }
        }
            else -> {
                ModalNavigationDrawer(
                    drawerContent = {
                        AppSideBar(
                            navigator = navigator, wishlistViewModel, cartViewModel = cartViewModel,
                            categoryViewModel = categoryViewModel,
                            productViewModel = productViewModel,
                            authViewModel = authViewModel,
                        )
                    },
                    drawerState = drawerState
                ) {

                    Scaffold(
                        topBar = {
                            TopBarScreen(
                                onClickMenu = { scope.launch { drawerState.open() } },
                                onClickShop = {
                                    navigator.push(
                                        CartScreenNav(
                                        )
                                    )
                                },
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
                            modifier = Modifier.fillMaxSize().padding(innerPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                LoginButton { navigator.push(OTPScreen())}
                                Spacer(Modifier.height(10.dp))
                                SignUpButton {navigator.push(OTPScreen()) }
                            }
                        }
                    }
                }
            }
        }
    }


@Composable
private fun WishlistGridContent(
    items: List<Product>,
    wishlistIds: Set<Int>,
    onWishlistToggle: (Product) -> Unit,
    onProductClick: (Product) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items.size) { index ->
            val product = items[index]
            ProductCard(
                product = product,
                isInWishlist = wishlistIds.contains(product.id),
                onWishlistToggle = { onWishlistToggle(product) },
                onClick = { onProductClick(product) }
            )
        }
    }
}

@Composable
private fun EmptyWishlistView(
    onStartShopping: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FavoriteBorder,
            contentDescription = "Empty Wishlist",
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Your Wishlist is Empty",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Save your favorite items here to buy them later",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onStartShopping,
            modifier = Modifier.fillMaxWidth(0.6f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Start Shopping")
        }
    }
}

@Composable
fun TopSection(
    name:String,
    totalItems: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge
            )

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Total Items: $totalItems",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
