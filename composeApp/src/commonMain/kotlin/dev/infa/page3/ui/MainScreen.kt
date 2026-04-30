package dev.infa.page3.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Person
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.SDK.bottle.navigation.BottleDashboardScreenNav
import dev.infa.page3.SDK.`V-Band`.navigation.VBandDashboardScreenNav
import dev.infa.page3.SDK.ui.navigation.HomeScreenSDK
import dev.infa.page3.data.model.WcCategory
import dev.infa.page3.navigation.*
import dev.infa.page3.presentation.uiSatateClaases.ListUiState
import dev.infa.page3.presentation.viewModel.CategoryViewModel
import dev.infa.page3.ui.components.*
import dev.infa.page3.presentation.viewModel.AuthViewModel
import dev.infa.page3.presentation.viewModel.CartViewModel
import dev.infa.page3.presentation.viewModel.ProductViewModel
import dev.infa.page3.presentation.viewmodel.WishlistViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import page3.composeapp.generated.resources.splash
import kotlin.math.absoluteValue
import page3.composeapp.generated.resources.Res
import page3.composeapp.generated.resources.bottel
import page3.composeapp.generated.resources.ring
import dev.infa.page3.ui.components.LocalAppNavVisibility
import dev.infa.page3.ui.components.AppFloatingNavBottomPadding
import dev.infa.page3.ui.components.ProvideAppBottomNav
import dev.infa.page3.ui.productscreen.components.AppStyleProductCarousel
import dev.infa.page3.ui.productscreen.components.CarouselProduct
import dev.infa.page3.ui.productscreen.components.SdkRoute
import dev.infa.page3.ui.productscreen.components.defaultConnectProducts
import page3.composeapp.generated.resources.vband


// ─── Palette ──────────────────────────────────────────────────────────────────────

private val RingGrad1   = Color(0xFF6C63FF)
private val RingGrad2   = Color(0xFF3A31C8)
private val BottleGrad1 = Color(0xFF4FC3F7)
private val BottleGrad2 = Color(0xFF0277BD)
private val BgDeep      = Color(0xFF0A0E1A)
private val BgCard      = Color(0xFF111827)
private val BgCard2     = Color(0xFF1A2235)

// ─── Device data ─────────────────────────────────────────────────────────────────

private data class DeviceSlide(
    val imageRes: DrawableResource,
    val title: String,
    val subtitle: String,
    val tag: String,
    val gradStart: Color,
    val gradEnd: Color,
    val accentColor: Color,
    val onClick: () -> Unit
)
// ─── Hardcoded extra categories ───────────────────────────────────────────────

data class HardcodedCategoryExtra(
    val category: WcCategory,
    val videoUrl: String
)

private val hardcodedCategories = listOf(
    HardcodedCategoryExtra(
        category = WcCategory(id = -1, name = "Luggage", slug = "gadgets", image = null, count = 0),
        videoUrl = "https://res.cloudinary.com/da0kwcojw/video/upload/v1776665100/Page_3_Suitcase_V2_2_cwbns1.mp4"
    ),
    HardcodedCategoryExtra(
        category = WcCategory(id = -2, name = "Essentials", slug = "essentials", image = null, count = 0),
        videoUrl = "hhttps://res.cloudinary.com/da0kwcojw/video/upload/v1776665101/Page_3_Bottle_1_wcjuk1.mp4"
    )
)

// ─── Main composable ─────────────────────────────────────────────────────────────

@Composable
fun ConnectToPage3Section(
    onRingClick:   () -> Unit,
    onBottleClick: () -> Unit,
    onVBandClick:  () -> Unit,
    modifier: Modifier = Modifier,
    products: List<CarouselProduct> = defaultConnectProducts,
    title: String = "My Devices"
) {
    // Map SdkRoute → the correct navigator callback
    val sdkHandlers: Map<SdkRoute, () -> Unit> = remember(onRingClick, onBottleClick, onVBandClick) {
        mapOf(
            SdkRoute.Ring   to onRingClick,
            SdkRoute.Bottle to onBottleClick,
            SdkRoute.VBand  to onVBandClick
        )
    }

    // Reuse the exact same carousel UI; just swap the click behaviour
    AppStyleProductCarousel(
        modifier  = modifier,
        products  = products,
        title     = title,
        onItemClick = { product, index ->
            val route = product.sdkRoute
            if (route != null) {
                // SDK route defined → open the matching SDK screen
                sdkHandlers[route]?.invoke()
            }
            // sdkRoute == null would fall through here; safe to ignore for connect products
        }
    )
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

    ProvideAppBottomNav {
        ModalNavigationDrawer(
            drawerContent = {
                AppSideBar(navigator = navigator , wishListViewModel, cartViewModel = cartViewModel,
                    categoryViewModel = categoryViewModel,
                    productViewModel = productViewModel,
                    authViewModel = authViewModel, )
            },
            drawerState = drawerState
        ) {

            val listState = rememberLazyListState()
            val navVisibility = LocalAppNavVisibility.current
            var prevScrollOffset by remember { mutableStateOf(0) }

            LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
                val currentOffset = listState.firstVisibleItemIndex * 10000 + listState.firstVisibleItemScrollOffset
                val scrollingDown = currentOffset > prevScrollOffset
                prevScrollOffset = currentOffset

                navVisibility.isVisible =
                    if (listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset < 50) {
                        true
                    } else {
                        !scrollingDown
                    }
            }

            Scaffold(
                topBar = {
                    TopBarScreen(
                        onClickMenu = { scope.launch { drawerState.open() } },
                        onClickShop = { navigator.push(CartScreenNav(

                        )) },
                        totalitem
                    )
                },
                contentWindowInsets = WindowInsets.safeDrawing
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xDEDCDC))
                        .padding(innerPadding)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (categoriesState) {
                            is ListUiState.Loading -> LoadingScreen(modifier = Modifier.fillMaxSize())
                            is ListUiState.Success -> {
                                val fetchedCategories = (categoriesState as ListUiState.Success).data
                                val allCategories = hardcodedCategories + fetchedCategories
                                LazyColumn(
                                    state = listState,
                                    contentPadding = PaddingValues(
                                        top = 8.dp,
                                        bottom = AppFloatingNavBottomPadding
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    // Hardcoded categories with custom videos
                                    items(hardcodedCategories) { extra ->
                                        CategoryCard(
                                            category = extra.category,
                                            videoUrl = extra.videoUrl,
                                            onClick = {
                                                when (extra.category.slug) {
                                                    "gadgets"    -> {  navigator.push(CategoryScreenNav(
                                                        "12",
                                                        "Luggage"
                                                    ))  }
                                                    "essentials" -> {  navigator.push(CategoryScreenNav(
                                                        "13",
                                                        "essentials"
                                                    )) }
                                                }
                                            }
                                        )
                                    }

                                    // Fetched categories — videoUrl stays null → default video plays
                                    items(fetchedCategories) { category ->
                                        CategoryCard(
                                            category = category,
                                            onClick = {
                                                navigator.push(
                                                    CategoryScreenNav(
                                                        category.id.toString(),
                                                        category.name
                                                    )
                                                )
                                            }
                                        )
                                    }
                                    item {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        ConnectToPage3Section(
                                            onRingClick   = { navigator.push(HomeScreenSDK()) },
                                            onBottleClick = { navigator.push(BottleDashboardScreenNav()) },
                                            onVBandClick  = { navigator.push(VBandDashboardScreenNav()) }
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                    item {
                                        AppStyleProductCarousel(
                                            onItemClick = { product, _ ->
                                                navigator.push(
                                                    ProductWebScreen(
                                                        url = product.websiteUrl,
                                                        title = product.label
                                                    )
                                                )
                                            }
                                        )
                                    }


                                    item {
                                        AppFooter()
                                        Spacer(modifier = Modifier.height(24.dp))
                                    }
                                }
                            }
                            is ListUiState.Error -> ErrorScreen((categoriesState as ListUiState.Error).message)
                            is ListUiState.Empty -> ProductsEmptyState(message = EmptyStateMessages.CATEGORIES_SOON)
                            ListUiState.Idle -> {}
                        }
                    }

                    // Floating nav over content (doesn't reserve space, avoids white gap)
                    Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                        BottomNavBar(
                            currentNav = currentTab,
                            navigator = navigator,
                            categoryViewModel = viewModel,
                            productViewModel = productViewModel,
                            wishListViewModel = wishListViewModel,
                            cartViewModel = cartViewModel,
                            authViewModel = authViewModel
                        )
                    }
                }
            }
        }
    }
}



@Composable
private fun SidebarCategoryCard(
    name: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF5F6FF),
        border = BorderStroke(1.dp, Color(0xFFE0E4FF)),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A1A),
                maxLines = 2
            )
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
    val categoriesState by categoryViewModel.categoriesState.collectAsState()

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

            Text(
                text = "Shop by category",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            when (categoriesState) {
                is ListUiState.Loading -> {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF6C63FF))
                    }
                }
                is ListUiState.Success -> {
                    val categories = (categoriesState as ListUiState.Success).data
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(categories, key = { it.id }) { category ->
                            SidebarCategoryCard(
                                name = category.name,
                                onClick = {
                                    navigator.push(
                                        CategoryScreenNav(
                                            category.id.toString(),
                                            category.name
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
                is ListUiState.Error -> {
                    Text(
                        text = (categoriesState as ListUiState.Error).message,
                        color = Color(0xFFB00020),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                is ListUiState.Empty -> {
                    Text(
                        text = EmptyStateMessages.CATEGORIES_SOON,
                        color = Color.Gray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                ListUiState.Idle -> Unit
            }

            Spacer(modifier = Modifier.height(16.dp))

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



