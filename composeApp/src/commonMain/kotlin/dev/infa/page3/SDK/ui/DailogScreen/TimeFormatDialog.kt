package dev.infa.page3.SDK.ui.DailogScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import dev.infa.page3.SDK.data.TimeFormat
import dev.infa.page3.SDK.ui.theme.*

@Composable
fun TimeFormatDialog(
    currentFormat: TimeFormat,
    onDismiss: () -> Unit,
    onSelect: (TimeFormat) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.BackgroundPrimary,
        title = {
            Text(
                "Select Time Format",
                color = AppColors.Primary,
                style = AppTypography.HeadingSmall
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.Default)) {
                TimeFormat.values().forEach { format ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(AppShapes.CardSmall)
                            .clickable { onSelect(format) }
                            .background(
                                if (format == currentFormat)
                                    AppColors.Selected
                                else Color.Transparent
                            )
                            .padding(AppDimensions.CardPadding.Medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = format == currentFormat,
                            onClick = { onSelect(format) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = AppColors.Primary
                            )
                        )
                        Spacer(modifier = Modifier.width(AppDimensions.Spacing.Default))
                        Text(
                            format.displayName,
                            color = AppColors.TextPrimary
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AppColors.TextPrimary)
            }
        }
    )
}
