package com.example.paze3

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
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
fun CategoryScreen(
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    onWishlistToggle: (Product) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val filteredProducts = remember(searchQuery, selectedCategory, products) {
        products.filter { product ->
            (selectedCategory == "All" || product.category == selectedCategory) &&
            (product.name.contains(searchQuery, ignoreCase = true) || 
             product.description.contains(searchQuery, ignoreCase = true))
        }
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
                    // Search Bar
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = { Icon(Icons.Default.Tune, contentDescription = null) },
                        shape = CircleShape,
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Shop By Category", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("See All ↗", color = PrimaryBlue, fontWeight = FontWeight.SemiBold)
                    }

                    FilterChips(
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it }
                    )
                }
            }
            
            if (filteredProducts.isEmpty()) {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("No products found", color = Color.Gray)
                    }
                }
            } else {
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
}
