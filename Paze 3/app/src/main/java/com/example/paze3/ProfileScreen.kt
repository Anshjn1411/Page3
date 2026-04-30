package com.example.paze3

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paze3.ui.theme.*

@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Info
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(PastelLavender),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(50.dp), tint = DarkNav)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("James Leo", fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text("james.leo@example.com", color = Color.Gray, fontSize = 14.sp)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                Text("Edit Profile", color = Color.White)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Menu Items
            ProfileMenuItem("My Account", Icons.Outlined.AccountCircle)
            ProfileMenuItem("My Orders", Icons.Outlined.ShoppingBag)
            ProfileMenuItem("My Wishlist", Icons.Outlined.FavoriteBorder)
            ProfileMenuItem("Settings", Icons.Outlined.Settings)
            ProfileMenuItem("Help & Support", Icons.Outlined.HelpOutline)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.ExitToApp, contentDescription = null, tint = Color.Red)
                Spacer(modifier = Modifier.width(16.dp))
                Text("Logout", color = Color.Red, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfileMenuItem(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = DarkNav, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
    }
}
