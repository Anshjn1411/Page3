package dev.infa.page3.SDK.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import dev.infa.page3.SDK.ui.theme.*
import dev.infa.page3.SDK.viewModel.ConnectionViewModel
import dev.infa.page3.SDK.viewModel.HomeViewModel


@Composable
fun DeviceCapabilityScreen(
    connectionViewModel: ConnectionViewModel,
    homeViewModel: HomeViewModel,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        homeViewModel.fetchDeviceCapabilities()
    }

    val uiState by connectionViewModel.uiState.collectAsState()
    val deviceName = uiState.connectedDevice?.name ?: "Device"

    val capabilities by homeViewModel.deviceCapabilities.collectAsState()

    val capabilityList = capabilities
        ?.toCapabilityMap()
        ?.toList()
        .orEmpty()

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CommonTopAppBar("Device Capabilities", { onBack() })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.BackgroundPrimary)
                .padding(AppDimensions.ScreenPadding.Horizontal)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.Default)
            ) {
                items(capabilityList) { (title, isAvailable) ->
                    CapabilityItem(
                        title = title,
                        isAvailable = isAvailable
                    )
                }
            }
        }
    }
}

@Composable
fun CapabilityItem(
    title: String,
    isAvailable: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.CardSmall)
            .background(AppColors.SurfaceVariant)
            .padding(AppDimensions.Spacing.ExtraLarge),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = title,
            color = AppColors.TextPrimary,
            style = AppTypography.BodyMedium,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = if (isAvailable) "Available" else "Not Available",
            color = if (isAvailable) AppColors.Success else AppColors.Error,
            style = AppTypography.BodySmall.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            )
        )
    }
}

