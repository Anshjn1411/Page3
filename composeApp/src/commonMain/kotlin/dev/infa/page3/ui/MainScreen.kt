package dev.infa.page3.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.navigation.*
import dev.infa.page3.presentation.uiSatateClaases.ListUiState
import dev.infa.page3.presentation.viewModel.CategoryViewModel
import dev.infa.page3.ui.components.*
import dev.infa.page3.platform.DeviceSDKLauncher
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
    val context = LocalContext.current
    val deviceSDKLauncher = remember { DeviceSDKLauncher(context) }


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

                    )) }
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
            }
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
                                            if (deviceSDKLauncher.isSDKAvailable()) {
                                                deviceSDKLauncher.openDeviceManager()
                                            }
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
fun AppSideBar(navigator: Navigator, wishlistViewModel: WishlistViewModel, productViewModel: ProductViewModel,

               cartViewModel: CartViewModel,
               authViewModel: AuthViewModel,
               categoryViewModel: CategoryViewModel) {
    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(Color(0xDEDCDC))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 🔹 Logo Header (Replaces Search Bar)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Image(
                    painter = painterResource(Res.drawable.splash),
                    contentDescription = "Logo",
                    modifier = Modifier.size(48.dp)
                )
            }

            Divider(
                color = Color(0xFFE0E0E0),
                thickness = 1.dp
            )

            // 🔹 Main Categories
            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                SidebarMenuItem(label = "Womens") { navigator.push(CategoryScreenNav("71","Women")) }
                SidebarMenuItem(label = "Mens") {navigator.push(CategoryScreenNav("72","Men")) }

                SidebarMenuItem(label = "Gadgets") { navigator.push(CategoryScreenNav("73","Gadgets")) }

            }

            Divider(
                color = Color(0xFFE0E0E0),
                thickness = 1.dp
            )

            // 🔹 Wishlist
            SidebarMenuItem(
                icon = Icons.Outlined.FavoriteBorder,
                label = "Wishlist"
            ) {
                navigator.push(WishlistScreenNav(

                ))
            }

            Divider(
                color = Color(0xFFE0E0E0),
                thickness = 1.dp
            )

            // 🔹 My Account Section with Dropdown
            var accountExpanded by remember { mutableStateOf(false) }

            Column {
                SidebarMenuItem(
                    icon = Icons.Outlined.Person,
                    label = "My Account",
                    hasDropdown = true,
                    isExpanded = accountExpanded
                ) {
                    accountExpanded = !accountExpanded
                }

                // 🔹 Account Submenu (Expanded)
                if (accountExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5F5F5))
                    ) {
                        SidebarSubMenuItem("Dashboard") { navigator.push(ProfileScreenNav()) }
                        SidebarSubMenuItem("Orders") { /* Navigate to Orders */ }
                        SidebarSubMenuItem("Downloads") { /* Navigate to Downloads */ }
                        SidebarSubMenuItem("Addresses") { /* Navigate to Addresses */ }
                        SidebarSubMenuItem("Account Details") { /* Navigate to Account Details */ }
                        SidebarSubMenuItem("Wishlist") { navigator.push(WishlistScreenNav(


                        )) }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun SidebarMenuItem(
    icon: ImageVector? = null,
    label: String,
    hasDropdown: Boolean = false,
    isExpanded: Boolean = false,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.textButtonColors(
            contentColor = Color(0xFF2C2C2C)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = Color(0xFF2C2C2C),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = Color(0xFF2C2C2C)
                )
            }

            if (hasDropdown) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = Color(0xFF2C2C2C),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun SidebarSubMenuItem(label: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(start = 16.dp),
        colors = ButtonDefaults.textButtonColors(
            contentColor = Color(0xFF666666)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(38.dp)) // Indent for submenu
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = Color(0xFF666666)
            )
        }
    }
}

