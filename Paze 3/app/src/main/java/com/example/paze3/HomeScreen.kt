package com.example.paze3

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paze3.ui.theme.*

@Composable
fun HomeScreen(
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    onCarouselClick: (Product) -> Unit,
    onWishlistToggle: (Product) -> Unit,
    carouselIndex: Float = 0f,
    onCarouselIndexChanged: (Float) -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf("All") }

    val filteredProducts = remember(selectedCategory, products) {
        if (selectedCategory == "All") products else products.filter { it.category == selectedCategory }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                Column {
                    val carouselProducts = remember(products) { products.filter { it.showInCarousel } }

                    FeaturedProductsCarousel(
                        products = carouselProducts,
                        onProductClick = onCarouselClick,
                        initialIndex = carouselIndex,
                        onIndexChanged = onCarouselIndexChanged
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("Product Category", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    FilterChips(
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it }
                    )

                    // Promo Banner
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = PastelYellow)
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("New Offer", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                                Text("Discount 50% For The First Order Transaction.", fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {},
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkNav),
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text("Shop Now", fontSize = 12.sp)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .weight(0.8f)
                                    .fillMaxHeight()
                                    .background(Color(0xFF80CBC4))
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("All Products", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("See All ↗", color = PrimaryBlue, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            items(filteredProducts) { product ->
                ProductCard(
                    product = product,
                    onProductClick = onProductClick,
                    onWishlistClick = onWishlistToggle
                )
            }
        }
    }
}
