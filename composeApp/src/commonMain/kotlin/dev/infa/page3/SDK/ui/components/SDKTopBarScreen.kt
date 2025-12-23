package dev.infa.page3.SDK.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import page3.composeapp.generated.resources.Res
import page3.composeapp.generated.resources.splash

@Composable
fun SDKTopBarScreen(
    onClickMenu: () -> Unit,
    isConnected: Boolean,
    isLoadingSteps: Boolean,
    onClickSync: () -> Unit,
    onOpenConnect: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {

        // 🔹 Top App Bar
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // 🍔 Hamburger Menu (Left)
            IconButton(
                onClick = onClickMenu,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp)
                )
            }

            // 🏷️ Logo (Center)
            Image(
                painter = org.jetbrains.compose.resources.painterResource(Res.drawable.splash),
                contentDescription = "Logo",
                modifier = Modifier.size(100.dp)
            )

            // ⚙️ Right Icon (Dynamic)
            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                when {
                    isLoadingSteps -> {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.Center),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    isConnected -> {
                        IconButton(onClick = onClickSync) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync Data",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    else -> {
                        IconButton(onClick = onOpenConnect) {
                            Icon(
                                imageVector = Icons.Default.BluetoothConnected,
                                contentDescription = "Connect Device",
                                tint = Color(0xFFFF6B6B),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopBarScreen(
    onClickMenu: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // 🔹 Top App Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            // Hamburger Menu (Left)
            IconButton(
                onClick = onClickMenu,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Logo (Center)
            Image(
                painter = painterResource(Res.drawable.splash),
                contentDescription = "Logo",
                modifier = Modifier.size(100.dp)
            )

        }
    }
}
