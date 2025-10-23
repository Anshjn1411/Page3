package dev.infa.page3.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPasteGo
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import dev.infa.page3.ui.navigation.Routes
import page3.composeapp.generated.resources.Res
import page3.composeapp.generated.resources.splash

@Composable
fun AppSideBar(navController: NavController) {
    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().background(Color.White)
        ) {
            // 🔹 Logo Header (Replaces Search Bar)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Image(
                    painter = org.jetbrains.compose.resources.painterResource(Res.drawable.splash),
                    contentDescription = "Logo",
                    modifier = Modifier.size(48.dp)
                )
            }

            Divider(
                color = Color(0xFFE0E0E0),
                thickness = 1.dp
            )

            // 🔹 Main Categories
            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                SidebarMenuItem(label = "Step Activity") { navController.navigate(Routes.Step) }
                SidebarMenuItem(label = "Sleep Activity") { navController.navigate(Routes.Sleep) }
                SidebarMenuItem(label = "Heart Rate ") {  navController.navigate(Routes.Heart)  }

            }

            Divider(
                color = Color(0xFFE0E0E0),
                thickness = 1.dp
            )

            // 🔹 Wishlist
            SidebarMenuItem(
                icon = Icons.Default.ContentPasteGo,
                label = "Set Goal"
            ) {
                navController.navigate(Routes.SetGoal)

            }

            Divider(
                color = Color(0xFFE0E0E0),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun SidebarMenuItem(
    icon: ImageVector? = null,
    label: String,
    hasDropdown: Boolean = false,
    isExpanded: Boolean = false,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.textButtonColors(
            contentColor = Color(0xFF2C2C2C)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = Color(0xFF2C2C2C),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = Color(0xFF2C2C2C)
                )
            }

            if (hasDropdown) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = Color(0xFF2C2C2C),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}