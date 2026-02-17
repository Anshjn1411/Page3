package dev.infa.page3.SDK.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.*
import dev.infa.page3.SDK.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar(
    isConnected: Boolean,
    deviceName: String,
    batteryLevel: Int
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bluetooth,
                    contentDescription = "Bluetooth Status",
                    tint = if (isConnected) AppColors.BluetoothConnected else AppColors.BluetoothDisconnected,
                    modifier = Modifier.size(AppDimensions.IconSize.Small)
                )
                Spacer(Modifier.width(AppDimensions.Spacing.Small))
                Text(
                    text = if (isConnected) deviceName else "Disconnected",
                    style = AppTypography.LabelMedium,
                    color = AppColors.TextSecondary
                )
            }
        },
        actions = {
            if (isConnected) {
                Text(
                    text = "$batteryLevel%",
                    color = AppColors.BatteryGood,
                    style = AppTypography.LabelMedium.copy(fontWeight = FontWeight.Medium)
                )
                Spacer(Modifier.width(AppDimensions.Spacing.Small))
                Icon(
                    imageVector = Icons.Default.BatteryFull,
                    contentDescription = "Battery Level",
                    tint = AppColors.BatteryGood,
                    modifier = Modifier.size(AppDimensions.IconSize.Default)
                )
                Spacer(Modifier.width(AppDimensions.Spacing.Medium))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppColors.BackgroundPrimary
        )
    )
}
