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

// ─── Main composable ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConnectToPage3Section(
    onRingClick: () -> Unit,
    onBottleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val slides = remember(onRingClick, onBottleClick) {
        listOf(
            DeviceSlide(
                imageRes = Res.drawable.ring,
                title = "Smart Ring",
                subtitle = "Health & Fitness Tracker",
                tag = "RING",
                gradStart = RingGrad1,
                gradEnd = RingGrad2,
                accentColor = Color(0xFF9D97FF),
                onClick = onRingClick
            ),
            DeviceSlide(
                imageRes = Res.drawable.bottel,
                title = "Smart Bottle",
                subtitle = "Hydration & Wellness",
                tag = "BOTTLE",
                gradStart = BottleGrad1,
                gradEnd = BottleGrad2,
                accentColor = Color(0xFF81D4FA),
                onClick = onBottleClick
            )
        )
    }

    val pagerState = rememberPagerState(initialPage = 0) { slides.size }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Section title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text       = "My Devices",
                fontSize   = 22.sp,
                fontWeight = FontWeight.Black,
                color      = Color.White,
                letterSpacing = 0.3.sp
            )
            // Page indicator pills
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                slides.forEachIndexed { index, _ ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue   = if (isSelected) 20.dp else 6.dp,
                        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
                        label         = "pill_width"
                    )
                    val color by animateColorAsState(
                        targetValue   = if (isSelected) slides[index].accentColor else Color.White.copy(alpha = 0.25f),
                        animationSpec = tween(250),
                        label         = "pill_color"
                    )
                    Box(
                        modifier = Modifier
                            .width(width)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(color)
                    )
                }
            }
        }

        // Horizontal pager
        HorizontalPager(
            state    = pagerState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 24.dp),
            pageSpacing    = 16.dp
        ) { page ->
            val slide      = slides[page]
            val pageOffset = (pagerState.currentPage - page + pagerState.currentPageOffsetFraction).absoluteValue
            val scale      = lerp(start = 0.92f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
            val alpha      = lerp(start = 0.55f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))

            DeviceCarouselCard(
                slide = slide,
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
            )
        }
    }
}

// ─── Device carousel card ─────────────────────────────────────────────────────────

@Composable
private fun DeviceCarouselCard(
    slide: DeviceSlide,
    modifier: Modifier = Modifier
) {
    // Pulsing glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.25f,
        targetValue   = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
    val imageScale by infiniteTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "img_scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        slide.gradStart.copy(alpha = 0.18f),
                        BgCard2,
                        BgCard
                    )
                )
            )
    ) {
        // Top glow blob
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-40).dp)
                .background(
                    Brush.radialGradient(
                        listOf(slide.gradStart.copy(alpha = glowAlpha), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Device tag pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(slide.gradStart.copy(alpha = 0.18f))
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            ) {
                Text(
                    text       = slide.tag,
                    color      = slide.accentColor,
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }

            Spacer(Modifier.height(20.dp))

            // Device image
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                slide.gradStart.copy(alpha = 0.12f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter            = painterResource(
                        resource = slide.imageRes
                    ),
                    contentDescription = slide.title,
                    contentScale       = ContentScale.Fit,
                    modifier           = Modifier
                        .size(140.dp)
                        .scale(imageScale)
                )
            }

            Spacer(Modifier.height(20.dp))

            // Title
            Text(
                text       = slide.title,
                color      = Color.White,
                fontSize   = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.3.sp,
                textAlign  = TextAlign.Center
            )

            Spacer(Modifier.height(4.dp))

            // Subtitle
            Text(
                text      = slide.subtitle,
                color     = Color.White.copy(alpha = 0.45f),
                fontSize  = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // Connect button
            Button(
                onClick = slide.onClick,
                shape   = RoundedCornerShape(22.dp),
                colors  = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor   = Color.White
                ),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(listOf(slide.gradStart, slide.gradEnd)),
                            shape = RoundedCornerShape(22.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text       = "Start Connect",
                            color      = Color.White,
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.3.sp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("→", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Subtle hint text
            Text(
                text      = "Swipe to see other devices",
                color     = Color.White.copy(alpha = 0.22f),
                fontSize  = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── lerp helper ─────────────────────────────────────────────────────────────────

private fun lerp(start: Float, stop: Float, fraction: Float): Float =
    start + fraction * (stop - start)

@Composable
private fun DeviceCard(
    emoji: String,
    title: String,
    subtitle: String,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(gradientColors)
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Emoji icon in a frosted circle
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, fontSize = 26.sp)
                }

                Column {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
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
                                val categories = (categoriesState as ListUiState.Success).data
                                LazyColumn(
                                    state = listState,
                                    contentPadding = PaddingValues(
                                        top = 8.dp,
                                        bottom = AppFloatingNavBottomPadding
                                    ),
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
                                            onRingClick = {
                                                navigator.push(HomeScreenSDK())
                                            },
                                            onBottleClick = {
                                                navigator.push(BottleDashboardScreenNav())
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }

                                    item {
                                        AppFooter()
                                        Spacer(modifier = Modifier.height(24.dp))
                                    }
                                }
                            }
                            is ListUiState.Error -> ErrorScreen((categoriesState as ListUiState.Error).message)
                            is ListUiState.Empty -> EmptyScreen("No categories found")
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
                    onClick = { navigator.push(CategoryScreenNav("92", "Gadgets")) }
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



