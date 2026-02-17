package dev.infa.page3.SDK.ui.DailogScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import dev.infa.page3.SDK.ui.theme.*

@Composable
fun ConnectionRequiredAlert() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.OverlayDarker)
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(AppDimensions.Spacing.Massive)
                .fillMaxWidth(),
            shape = AppShapes.CardLarge,
            colors = CardDefaults.cardColors(containerColor = AppColors.TextPrimary)
        ) {
            Column(
                modifier = Modifier.padding(AppDimensions.CardPadding.ExtraLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.ExtraLarge)
            ) {
                Box(
                    modifier = Modifier
                        .size(AppDimensions.IconSize.Huge)
                        .background(AppColors.Error.copy(alpha = AppAlpha.VeryLight), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LinkOff,
                        contentDescription = null,
                        tint = AppColors.Error,
                        modifier = Modifier.size(AppDimensions.IconSize.ExtraLarge)
                    )
                }
                Text(
                    text = "Device Not Connected",
                    style = AppTypography.HeadingMedium,
                    color = AppColors.BackgroundPrimary
                )
                Text(
                    text = "Please connect to a device first",
                    style = AppTypography.BodyMedium,
                    color = AppColors.TextTertiary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}