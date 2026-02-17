package dev.infa.page3.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.SDK.ui.navigation.HomeScreenSDK
import dev.infa.page3.navigation.*
import dev.infa.page3.presentation.uiSatateClaases.ListUiState
import dev.infa.page3.presentation.viewModel.CategoryViewModel
import dev.infa.page3.ui.components.*
import dev.infa.page3.presentation.viewModel.AuthViewModel
import dev.infa.page3.presentation.viewModel.CartViewModel
import dev.infa.page3.presentation.viewModel.ProductViewModel
import dev.infa.page3.presentation.viewmodel.WishlistViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import page3.composeapp.generated.resources.Res
import page3.composeapp.generated.resources.splash

@Composable
fun ConnectToPage3Section(
    onDeviceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Connect to Page3",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Text(
                text = "Manage your smart gadgets and devices",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onDeviceClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "My Device",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navigator: Navigator,
    viewModel: CategoryViewModel,
    productViewModel: ProductViewModel,
    wishListViewModel: WishlistViewModel,
    cartViewModel: CartViewModel,
    categoryViewModel: CategoryViewModel,
    authViewModel: AuthViewModel
) {
    val totalitem by cartViewModel.totalItems.collectAsState()


    LaunchedEffect(Unit) { AuthManager.init() }

    val categoriesState by viewModel.categoriesState.collectAsState()
    var currentTab by remember { mutableStateOf("home") }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerContent = {
            AppSideBar(navigator = navigator , wishListViewModel, cartViewModel = cartViewModel,
                categoryViewModel = categoryViewModel,
                productViewModel = productViewModel,
                authViewModel = authViewModel, )
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
                    categoryViewModel = viewModel,
                    productViewModel = productViewModel,
                    wishListViewModel = wishListViewModel,
                    cartViewModel = cartViewModel,
                    authViewModel = authViewModel

                )
            },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xDEDCDC)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (categoriesState) {
                        is ListUiState.Loading -> LoadingScreen(modifier = Modifier.fillMaxSize())
                        is ListUiState.Success -> {
                            val categories = (categoriesState as ListUiState.Success).data
                            LazyColumn(
                                contentPadding = PaddingValues(vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(categories) { category ->
                                    CategoryCard(
                                        category = category,
                                        onClick = {
                                            navigator.push(
                                                CategoryScreenNav(
                                                    category.id.toString(),
                                                    category.name,

                                                )
                                            )
                                        }
                                    )
                                }

                                item {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    ConnectToPage3Section(
                                        onDeviceClick = {
                                            navigator.push(HomeScreenSDK())

                                        }
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }
                        is ListUiState.Error -> ErrorScreen((categoriesState as ListUiState.Error).message)
                        is ListUiState.Empty -> EmptyScreen("No categories found")
                        ListUiState.Idle -> {}
                    }
                }
            }
        }
    }
}



@Composable
fun AppSideBar(
    navigator: Navigator,
    wishlistViewModel: WishlistViewModel,
    productViewModel: ProductViewModel,
    cartViewModel: CartViewModel,
    authViewModel: AuthViewModel,
    categoryViewModel: CategoryViewModel
) {
    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp),
        drawerContainerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // 🔹 Centered Logo Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.splash),
                    contentDescription = "Logo",
                    modifier = Modifier.size(100.dp),
                    contentScale = ContentScale.Fit
                )
            }

            // 🔹 Main Categories with Modern Button Design
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModernButton(
                    label = "Women's",
                    onClick = { navigator.push(CategoryScreenNav("71", "Women")) }
                )
                ModernButton(
                    label = "Men's",
                    onClick = { navigator.push(CategoryScreenNav("72", "Men")) }
                )
                ModernButton(
                    label = "Gadgets",
                    onClick = { navigator.push(CategoryScreenNav("73", "Gadgets")) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 🔹 Wishlist Button
            ModernButton(
                label = "Wishlist",
                icon = Icons.Outlined.FavoriteBorder,
                onClick = { navigator.push(WishlistScreenNav()) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 🔹 My Account Section with Dropdown
            var accountExpanded by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                ModernButton(
                    label = "My Account",
                    icon = Icons.Outlined.Person,
                    hasDropdown = true,
                    isExpanded = accountExpanded,
                    onClick = { accountExpanded = !accountExpanded }
                )

                // 🔹 Account Submenu (Expanded)
                AnimatedVisibility(
                    visible = accountExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SubMenuItem("Dashboard") { navigator.push(ProfileScreenNav()) }
                        SubMenuItem("Orders") { /* Navigate to Orders */ }
                        SubMenuItem("Downloads") { /* Navigate to Downloads */ }
                        SubMenuItem("Addresses") { /* Navigate to Addresses */ }
                        SubMenuItem("Account Details") { /* Navigate to Account Details */ }
                        SubMenuItem("Wishlist") { navigator.push(WishlistScreenNav()) }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ModernButton(
    label: String,
    icon: ImageVector? = null,
    hasDropdown: Boolean = false,
    isExpanded: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, Color(0xFFE8E8E8)),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = Color(0xFF2C2C2C),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = label,
                    color = Color(0xFF1A1A1A),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.3.sp
                )
            }

            if (hasDropdown) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFF6B6B6B),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun SubMenuItem(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Text(
            text = label,
            color = Color(0xFF4A4A4A),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            letterSpacing = 0.2.sp
        )
    }
}



