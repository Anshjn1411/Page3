package dev.infa.page3.SDK.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import dev.infa.page3.SDK.ui.theme.*

@Composable
fun CircularProgressRingLoading(
    label: String,
    color: Color,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(AppDimensions.Spacing.Medium)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(AppDimensions.ChartHeight.Medium)
        ) {
            // Loading spinner
            CircularProgressIndicator(
                modifier = Modifier.size(AppDimensions.ProgressRing.Default),
                color = color,
                strokeWidth = AppDimensions.ProgressRing.Stroke
            )

            // Icon in center
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color.copy(alpha = 0.6f),
                modifier = Modifier.size(AppDimensions.IconSize.Large)
            )
        }

        Spacer(Modifier.height(AppDimensions.Spacing.Small))

        Text(
            text = label,
            color = AppColors.TextSecondary,
            style = AppTypography.LabelMedium
        )
    }
}
