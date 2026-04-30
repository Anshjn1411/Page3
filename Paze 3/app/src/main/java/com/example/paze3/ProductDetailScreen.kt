package com.example.paze3

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.paze3.ui.theme.*

@Composable
fun ProductDetailScreen(
    product: Product,
    onBack: () -> Unit,
    onWishlistToggle: (Product) -> Unit
) {
    Scaffold(
        topBar = {
            TopBar(
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
                    .padding(24.dp)
            ) {
                Button(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(32.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Text(
                        "BUY NOW", 
                        fontSize = 20.sp, 
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = Color.White
                    )
                }
            }
        },
        containerColor = BgWhite
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Product Image Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(product.bgColor),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize().padding(24.dp)
                )
            }

            Column(
                modifier = Modifier
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(product.name, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
                        Text(product.rating, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("$${product.price}", color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 22.sp)

                Spacer(modifier = Modifier.height(24.dp))
                Text("Description", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    product.description,
                    color = Color.Gray,
                    lineHeight = 22.sp,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(24.dp))
                Text("Specifications", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
                
                product.specifications.forEach { (key, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(key, color = Color.Gray, fontSize = 14.sp)
                        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                }
            }
        }
    }
}
