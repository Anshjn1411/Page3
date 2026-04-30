package com.example.paze3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.paze3.ui.theme.BgWhite
import com.example.paze3.ui.theme.Paze3Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Paze3Theme {
                MainContainer()
            }
        }
    }
}

@Composable
fun MainContainer() {
    var currentScreen by remember { mutableStateOf("Home") }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var webViewUrl by remember { mutableStateOf<String?>(null) }
    var cameFromWebView by remember { mutableStateOf(false) }
    var carouselIndex by remember { mutableStateOf(0f) }
    
    // Simple state management for demo purposes
    val productsState = remember { mutableStateListOf(*allProducts.toTypedArray()) }

    val onProductClick: (Product) -> Unit = { product ->
        selectedProduct = product
    }

    val onCarouselProductClick: (Product) -> Unit = { product ->
        webViewUrl = product.buyUrl
    }

    val onWishlistToggle: (Product) -> Unit = { product ->
        val index = productsState.indexOfFirst { it.id == product.id }
        if (index != -1) {
            productsState[index] = productsState[index].copy(isWishlisted = !productsState[index].isWishlisted)
        }
    }

    if (selectedProduct != null) {
        BackHandler {
            if (cameFromWebView) {
                cameFromWebView = false
                selectedProduct = null
            } else {
                selectedProduct = null
            }
        }
        ProductDetailScreen(
            product = selectedProduct!!,
            onBack = { 
                if (cameFromWebView) {
                    cameFromWebView = false
                    selectedProduct = null
                } else {
                    selectedProduct = null
                }
            },
            onWishlistToggle = onWishlistToggle
        )
    } else if (webViewUrl != null) {
        // WebViewScreen handles its own back navigation for the browser history
        WebViewScreen(
            url = webViewUrl!!, 
            onBack = { webViewUrl = null },
            onDeepLink = { id ->
                val product = allProducts.find { it.id == id }
                if (product != null) {
                    selectedProduct = product
                    cameFromWebView = true
                }
            }
        )
    } else {
        Scaffold(
            topBar = {
                val title = when (currentScreen) {
                    "Category" -> "Category"
                    "Wishlist" -> "Wishlist"
                    "Profile" -> "Profile"
                    else -> null
                }
                val onBack: (() -> Unit)? = if (currentScreen != "Home") {
                    { currentScreen = "Home" }
                } else null

                TopBar(
                    title = title,
                    onBack = onBack,
                    onSearchClick = { /* Handle search */ },
                    onNotificationClick = { /* Handle notifications */ },
                    onCartClick = { /* Handle cart */ }
                )
            },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    BottomNavBar(
                        activeScreen = currentScreen,
                        onScreenSelected = { currentScreen = it }
                    )
                }
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BgWhite)
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    "Home" -> HomeScreen(
                        products = productsState,
                        onProductClick = onProductClick,
                        onCarouselClick = { product ->
                            // Update carousel index to ensure product is in front
                            val carouselProducts = productsState.filter { it.showInCarousel }
                            val index = carouselProducts.indexOfFirst { it.id == product.id }
                            if (index != -1) {
                                carouselIndex = index.toFloat()
                            }
                            onCarouselProductClick(product)
                        },
                        onWishlistToggle = onWishlistToggle,
                        carouselIndex = carouselIndex,
                        onCarouselIndexChanged = { carouselIndex = it }
                    )
                    "Category" -> CategoryScreen(
                        products = productsState,
                        onProductClick = onProductClick,
                        onWishlistToggle = onWishlistToggle
                    )
                    "Wishlist" -> WishlistScreen(
                        products = productsState.filter { it.isWishlisted },
                        onProductClick = onProductClick,
                        onWishlistToggle = onWishlistToggle
                    )
                    "Profile" -> ProfileScreen()
                }
            }
        }
    }
}
