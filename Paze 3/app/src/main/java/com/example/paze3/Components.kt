package com.example.paze3

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paze3.ui.theme.*
import coil.compose.AsyncImage

@Composable
fun TopBar(
    title: String? = null,
    onBack: (() -> Unit)? = null,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onCartClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                Box(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(44.dp)
                        .background(Color.White, CircleShape)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black, modifier = Modifier.size(22.dp))
                }
            }
            
            if (title != null) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            } else {
                // Logo Image
                AsyncImage(
                    model = "https://motion.inventiko.com/paze3/suitcase/logo-b.png",
                    contentDescription = "PAZE Logo",
                    modifier = Modifier
                        .height(32.dp)
                        .wrapContentWidth()
                )
            }
        }

        // Actions
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White, CircleShape)
                    .clickable { onSearchClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Black, modifier = Modifier.size(22.dp))
            }
            
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White, CircleShape)
                    .clickable { onNotificationClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = Color.Black, modifier = Modifier.size(22.dp))
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White, CircleShape)
                    .clickable { onCartClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.ShoppingBag, contentDescription = "Cart", tint = Color.Black, modifier = Modifier.size(22.dp))
            }
        }
    }
}

@Composable
fun BottomNavBar(activeScreen: String, onScreenSelected: (String) -> Unit) {
    val items = listOf(
        NavItem("Home", Icons.Outlined.Home, Icons.Filled.Home),
        NavItem("Category", Icons.Outlined.GridView, Icons.Filled.GridView),
        NavItem("Wishlist", Icons.Outlined.FavoriteBorder, Icons.Filled.Favorite),
        NavItem("Profile", Icons.Outlined.Person, Icons.Filled.Person)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp, start = 24.dp, end = 24.dp)
            .height(70.dp)
            .background(DarkNav, RoundedCornerShape(35.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isActive = activeScreen == item.label
                if (isActive) {
                    Row(
                        modifier = Modifier
                            .height(50.dp)
                            .background(ActiveLime, RoundedCornerShape(25.dp))
                            .padding(horizontal = 16.dp)
                            .clickable { onScreenSelected(item.label) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(item.activeIcon, contentDescription = null, tint = DarkNav)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(item.label, color = DarkNav, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                } else {
                    IconButton(onClick = { onScreenSelected(item.label) }) {
                        Icon(item.icon, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(product: Product, onProductClick: (Product) -> Unit, onWishlistClick: (Product) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable { onProductClick(product) },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = product.bgColor)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Wishlist Icon (Essential for functionality)
            IconButton(
                onClick = { onWishlistClick(product) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = if (product.isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = if (product.isWishlisted) Color.Red else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Increased size of Product Image
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Product Name (The only text metadata shown)
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun FilterChips(selectedCategory: String, onCategorySelected: (String) -> Unit) {
    val filters = listOf("All", "Woman", "Man", "Kids")
    LazyRow(
        modifier = Modifier.padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { filter ->
            val isActive = filter == selectedCategory
            Surface(
                shape = CircleShape,
                color = if (isActive) PrimaryBlue else Color(0xFFF5F5F5),
                modifier = Modifier.clickable { onCategorySelected(filter) }
            ) {
                Text(
                    text = filter,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    color = if (isActive) Color.White else Color.Gray,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
