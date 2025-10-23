package dev.infa.page3.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp // Replace with your navigator type
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.navigation.*
import dev.infa.page3.presentation.viewModel.*
import dev.infa.page3.presentation.viewmodel.WishlistViewModel

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
    NavigationBar(
        modifier = Modifier.height(56.dp),
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 4.dp
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
            }
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
}


@Composable
private fun RowScope.BottomNavItem(
    title: String,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = if (selected) selectedIcon else unselectedIcon,
                contentDescription = title,
                modifier = Modifier.size(28.dp)
            )
        },
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = Color.Transparent,
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurface,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}
