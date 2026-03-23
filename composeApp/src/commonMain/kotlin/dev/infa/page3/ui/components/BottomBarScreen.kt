package dev.infa.page3.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.navigation.*
import dev.infa.page3.presentation.viewModel.*
import dev.infa.page3.presentation.viewmodel.WishlistViewModel

// ─── Scroll-aware bottom nav visibility for Ecommerce app ──────────────────────────

class AppNavVisibilityState {
    var isVisible by mutableStateOf(true)
}

val LocalAppNavVisibility = compositionLocalOf { AppNavVisibilityState() }

// Content should pad by this so last items don't hide behind the floating nav.
val AppFloatingNavBottomPadding = 130.dp

@Composable
fun ProvideAppBottomNav(
    content: @Composable () -> Unit
) {
    val navVisibility = remember { AppNavVisibilityState() }
    CompositionLocalProvider(LocalAppNavVisibility provides navVisibility, content = content)
}

@Composable
fun BottomNavBar(
    currentNav: String,
    navigator: Navigator,
    categoryViewModel: CategoryViewModel,
    productViewModel: ProductViewModel,
    wishListViewModel: WishlistViewModel,
    cartViewModel: CartViewModel,
    authViewModel: AuthViewModel
) {
    val totalitem by cartViewModel.totalItems.collectAsState()
    val navVisibility = LocalAppNavVisibility.current

    val navOffsetY by animateDpAsState(
        targetValue = if (navVisibility.isVisible) 0.dp else 120.dp,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f),
        label = "ecom_nav_offset"
    )
    val navAlpha by animateFloatAsState(
        targetValue = if (navVisibility.isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "ecom_nav_alpha"
    )

    Box(
        modifier = Modifier
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .graphicsLayer(alpha = navAlpha)
            .shadow(
                elevation = 28.dp,
                shape = RoundedCornerShape(30.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            )
            // Opaque glassmorphism background (frosted look, but less see-through)
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.86f)
                    )
                )
            )
            .clip(RoundedCornerShape(30.dp))
            .graphicsLayer { translationY = navOffsetY.toPx() },
        contentAlignment = Alignment.Center
    ) {
        NavigationBar(
            modifier = Modifier.height(64.dp),
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            BottomNavItem(
                title = "Home",
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                selected = currentNav == "home",
                onClick = {
                    navigator.push(
                        HomeMainScreen(

                        )
                    )
                }
            )
            BottomNavItem(
                title = "Categories",
                selectedIcon = Icons.Filled.ViewModule,
                unselectedIcon = Icons.Outlined.ViewModule,
                selected = currentNav == "categories",
                onClick = {
                    navigator.push(
                        CategoriesOverviewScreenNav(
                        )
                    )
                }
            )
            BottomNavItem(
                title = "Cart",
                selectedIcon = Icons.Filled.ShoppingCart,
                unselectedIcon = Icons.Outlined.ShoppingCart,
                selected = currentNav == "cart",
                onClick = {
                    navigator.push(CartScreenNav())
                },
                badgeCount = if (totalitem != 0) totalitem else 0
            )

            BottomNavItem(
                title = "Wishlist",
                selectedIcon = Icons.Filled.Favorite,
                unselectedIcon = Icons.Outlined.FavoriteBorder,
                selected = currentNav == "wishlist",
                onClick = {
                    navigator.push(WishlistScreenNav())
                }
            )
            BottomNavItem(
                title = "Profile",
                selectedIcon = Icons.Filled.Person,
                unselectedIcon = Icons.Outlined.Person,
                selected = currentNav == "profile",
                onClick = {
                    navigator.push(ProfileScreenNav())
                }
            )
        }

        // Subtle glass top highlight
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .height(1.dp)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Thin glass edge stroke
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.14f),
                            Color.Transparent,
                            Color.White.copy(alpha = 0.06f)
                        )
                    )
                )
        )
    }
}

@Composable
private fun RowScope.BottomNavItem(
    title: String,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    badgeCount: Int = 0
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            BadgedBox(
                badge = {
                    if (badgeCount > 0) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ) {
                            Text(
                                text = badgeCount.toString(),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = if (selected) selectedIcon else unselectedIcon,
                    contentDescription = title,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = Color.Transparent,
            selectedIconColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

