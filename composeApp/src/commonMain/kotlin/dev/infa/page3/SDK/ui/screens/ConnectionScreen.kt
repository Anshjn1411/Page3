package dev.infa.page3.SDK.ui.screens

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.SDK.connection.ConnectionState
import dev.infa.page3.SDK.connection.DeviceInfo
import dev.infa.page3.SDK.ui.components.CommonTopAppBar
import dev.infa.page3.SDK.ui.theme.*
import dev.infa.page3.SDK.viewModel.ConnectionViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    connectionViewModel: ConnectionViewModel,
    navController: Navigator
) {
    val uiState by connectionViewModel.uiState.collectAsState()

    val isScanning = uiState.isScanning
    val discoveredDevices = uiState.devices

    val isConnecting =
        uiState.connectionState == ConnectionState.CONNECTING ||
                uiState.connectionState == ConnectionState.RECONNECTING

    val isConnected = uiState.isConnected

    LaunchedEffect(isConnected) {
        if (isConnected) {
            navController.pop()
        }
    }

    val totalSeconds: Int = 30
    var remainingSeconds by remember { mutableStateOf(totalSeconds) }

    val infiniteTransition = rememberInfiniteTransition(label = "ballAnim")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ballOffset"
    )

    LaunchedEffect(Unit) {
        while (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
        }
    }

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "Scanning",
                onBackClick = { navController.pop() }
            )
        },
        containerColor = AppColors.BackgroundPrimary
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.BackgroundPrimary)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = AppDimensions.ScreenPadding.Horizontal)
            ) {
                Spacer(modifier = Modifier.height(AppDimensions.Spacing.Default))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isScanning) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            ScanningAnimation(
                                modifier = Modifier.size(200.dp)
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Ready to Connect",
                                color = AppColors.TextPrimary,
                                style = AppTypography.HeadingExtraSmall
                            )

                            Spacer(modifier = Modifier.height(AppDimensions.Spacing.Medium))

                            Text(
                                text = "Tap below to start scanning",
                                color = AppColors.TextTertiary,
                                style = AppTypography.LabelSmall
                            )

                            Spacer(modifier = Modifier.height(AppDimensions.CardPadding.Large))


                            Button(
                                onClick = { connectionViewModel.startScan() },
                                shape = AppShapes.Chip,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.Primary,
                                    contentColor = AppColors.BackgroundPrimary
                                ),
                                modifier = Modifier
                                    .height(AppDimensions.ButtonHeight.Medium)
                                    .width(180.dp)
                            ) {
                                Text(
                                    text = "Start Scan",
                                    style = AppTypography.ButtonMedium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppDimensions.Spacing.XXL))

                if (discoveredDevices.isNotEmpty()) {
                    Text(
                        text = "Available Devices",
                        color = AppColors.Primary,
                        style = AppTypography.BodyMedium
                    )

                    Spacer(modifier = Modifier.height(AppDimensions.Spacing.Medium))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.Medium),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(discoveredDevices) { device ->
                            DeviceScanItem(
                                device = device,
                                onClick = {
                                    connectionViewModel.connect(device)
                                }
                            )
                        }
                    }
                } else if (!isScanning) {
                    EmptyDeviceState()
                }

                Spacer(modifier = Modifier.height(AppDimensions.CardPadding.Large))
            }

            if (isConnecting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppColors.OverlayDark),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.Large)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(AppDimensions.Spacing.Large)
                                .offset(y = offsetY.dp)
                                .background(
                                    color = AppColors.Primary,
                                    shape = CircleShape
                                )
                        )

                        Text(
                            text = "Connecting… please do not go back, wait",
                            color = AppColors.TextPrimary,
                            style = AppTypography.BodyMedium
                        )

                        Text(
                            text = "Time remaining: $remainingSeconds s",
                            color = AppColors.TextSecondary,
                            style = AppTypography.BodySmall
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun DeviceScanItem(
    device: DeviceInfo,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.CardSmall)
            .background(AppColors.SurfaceVariant)
            .clickable { onClick() }
            .padding(AppDimensions.CardPadding.Medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = device.name,
                color = AppColors.TextPrimary,
                style = AppTypography.BodyMedium
            )
            Text(
                text = device.address,
                color = AppColors.TextSecondary,
                style = AppTypography.LabelMedium
            )
        }
        Text(
            text = "Bind",
            color = AppColors.Primary,
            style = AppTypography.LabelSmall
        )
    }
}
@Composable
private fun EmptyDeviceState() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AppDimensions.CardPadding.Large),
        shape = AppShapes.CardLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimensions.Elevation.Small),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.SurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = AppDimensions.CardPadding.XXL,
                    horizontal = AppDimensions.CardPadding.ExtraLarge
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.XXL)
        ) {
            Text(
                text = "⌚",
                style = AppTypography.DisplayLarge
            )
            Text(
                text = "No Devices Found",
                style = AppTypography.HeadingExtraSmall,
                color = AppColors.TextPrimary
            )

            Text(
                text = "Make sure your wearable is powered ON and in pairing mode",
                style = AppTypography.LabelSmall,
                color = AppColors.TextTertiary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.Small)
            ) {
                Box(
                    modifier = Modifier
                        .size(AppDimensions.IconSize.ExtraSmall)
                        .clip(CircleShape)
                        .background(AppColors.Primary)
                )

                Text(
                    text = "Tap Start Scan to refresh",
                    style = AppTypography.LabelMedium,
                    color = AppColors.Primary
                )
            }
        }
    }
}
@Composable
expect fun ScanningAnimation(
    modifier: Modifier = Modifier
)