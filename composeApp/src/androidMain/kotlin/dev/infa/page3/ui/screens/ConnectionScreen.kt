package dev.infa.page3.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.lottiefiles.dotlottie.core.compose.ui.DotLottieAnimation
import com.lottiefiles.dotlottie.core.util.DotLottieSource
import dev.infa.page3.models.SmartWatch
import dev.infa.page3.ui.components.CommonTopAppBar
import dev.infa.page3.viewmodels.ConnectionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    connectionViewModel: ConnectionViewModel,
    navController: NavController
) {
    val uiState by connectionViewModel.uiState.collectAsState()

    val isScanning = uiState.isScanning
    val discoveredDevices = uiState.devices
    val isConnecting = uiState.isConnecting
    val isConnected = uiState.isConnected

    // ✅ Auto navigate back when connected
    LaunchedEffect(isConnected) {
        if (isConnected) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "Scanning",
                onBackClick = { navController.navigateUp() }
            )
        },
        containerColor = Color.Black   // ✅ FIXED: FULL BLACK
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(innerPadding)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {

                Spacer(modifier = Modifier.height(12.dp))

                // ✅ CENTER SCAN CTA
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isScanning) {

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            DotLottieAnimation(
                                source = DotLottieSource.Url(
                                    "https://lottiefiles-mobile-templates.s3.amazonaws.com/ar-stickers/swag_sticker_piggy.lottie"
                                ),
                                autoplay = true,
                                loop = true,
                                speed = 1.4f,
                                modifier = Modifier.size(200.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Scanning for nearby devices...",
                                color = Color(0xFF00FF88),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                    } else {

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {

                            Text(
                                text = "Ready to Connect",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Tap below to start scanning",
                                color = Color(0xFF9E9E9E),
                                fontSize = 13.sp
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = { connectionViewModel.startScan() },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00FF88),
                                    contentColor = Color.Black
                                ),
                                modifier = Modifier
                                    .height(50.dp)
                                    .width(180.dp)
                            ) {
                                Text(
                                    text = "Start Scan",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }


                Spacer(modifier = Modifier.height(18.dp))

                // ✅ AVAILABLE DEVICES LIST
                if (discoveredDevices.isNotEmpty()) {

                    Text(
                        text = "Available Devices",
                        color = Color(0xFF00FF88),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(discoveredDevices) { device ->
                            DeviceScanItem(
                                device = device,
                                onClick = {
                                    connectionViewModel.connectToDevice(device)
                                }
                            )
                        }
                    }

                } else if (!isScanning) {
                    EmptyDeviceState()
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // ✅ FULL-SCREEN CONNECTING LOADER
            if (isConnecting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Connecting...",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceScanItem(
    device: SmartWatch,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF121212))
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = device.deviceName,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = device.deviceAddress,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        Text(
            text = "Bind",
            color = Color(0xFF00FF88),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EmptyDeviceState() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF121212)   // ✅ Dark card like your scan items
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 36.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            // ✅ ICON / EMOJI
            Text(
                text = "⌚",
                fontSize = 58.sp
            )

            // ✅ TITLE
            Text(
                text = "No Devices Found",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // ✅ DESCRIPTION
            Text(
                text = "Make sure your wearable is powered ON and in pairing mode",
                fontSize = 13.sp,
                color = Color(0xFF9E9E9E),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            // ✅ SOFT ACTION HINT
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00FF88))
                )

                Text(
                    text = "Tap Start Scan to refresh",
                    fontSize = 12.sp,
                    color = Color(0xFF00FF88),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
