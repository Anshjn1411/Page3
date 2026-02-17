package dev.infa.page3.SDK.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.*
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.SDK.ui.theme.*
import dev.infa.page3.SDK.viewModel.ConnectionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailScreen(
    connectionViewModel: ConnectionViewModel,
    navController: Navigator
) {
    val uiState by connectionViewModel.uiState.collectAsState()
    val isConnected = uiState.isConnected
    val deviceName = uiState.connectedDevice?.name ?: ""
    val deviceAddress = uiState.connectedDevice?.address ?: ""
    val batteryLevel = uiState.batteryLevel ?: 0

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "Device Details",
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
                    .padding(AppDimensions.ScreenPadding.Horizontal),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(AppDimensions.Spacing.XXXL))

                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(AppShapes.TrendChart)
                        .background(AppColors.Primary.copy(alpha = AppAlpha.Subtle)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Watch,
                        contentDescription = null,
                        tint = AppColors.Primary,
                        modifier = Modifier.size(AppDimensions.IconSize.Huge)
                    )
                }

                Spacer(modifier = Modifier.height(AppDimensions.Spacing.ExtraLarge))

                Text(
                    text = deviceName,
                    color = AppColors.Primary,
                    style = AppTypography.ValueLarge
                )

                Spacer(modifier = Modifier.height(AppDimensions.Spacing.Small))

                Text(
                    text = deviceAddress,
                    color = AppColors.TextPrimary,
                    style = AppTypography.BodySmall.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    )
                )

                Spacer(modifier = Modifier.height(AppDimensions.Spacing.Huge))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.Primary.copy(alpha = 0.05f)
                    ),
                    shape = AppShapes.TrendChart
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppDimensions.CardPadding.Large),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Column {
                            Text(
                                text = "Battery Level",
                                color = AppColors.TextSecondary,
                                style = AppTypography.BodyExtraSmall
                            )
                            Spacer(modifier = Modifier.height(AppDimensions.Spacing.ExtraSmall))
                            Text(
                                text = "$batteryLevel%",
                                color = AppColors.TextPrimary,
                                style = AppTypography.ValueLarge
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.BatteryFull,
                            contentDescription = null,
                            tint = AppColors.BatteryGood,
                            modifier = Modifier.size(AppDimensions.IconSize.ExtraLarge)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppDimensions.Spacing.Huge))

                InfoRow(title = "Connection Status", value = "Connected")
                InfoRow(title = "Last Sync", value = "2 minutes ago")
                InfoRow(title = "Firmware Version", value = "v1.2.4")

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        connectionViewModel.disconnect()
                        navController.pop()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Primary,
                        contentColor = AppColors.BackgroundPrimary
                    ),
                    shape = AppShapes.ButtonLarge
                ) {
                    Text(
                        text = "Disconnect Device",
                        style = AppTypography.ButtonMedium
                    )
                }

                Spacer(modifier = Modifier.height(AppDimensions.Spacing.Default))
            }
        }
    }
}

@Composable
fun InfoRow(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppDimensions.Spacing.Default),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = AppColors.TextSecondary,
            style = AppTypography.BodySmall
        )
        Text(
            text = value,
            color = AppColors.TextPrimary,
            style = AppTypography.BodySmall.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            )
        )
    }
}
