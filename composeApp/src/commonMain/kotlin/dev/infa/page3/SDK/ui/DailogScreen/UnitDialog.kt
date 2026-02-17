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
import dev.infa.page3.SDK.data.UnitSystem
import dev.infa.page3.SDK.ui.theme.*

@Composable
fun UnitSystemDialog(
    currentUnit: UnitSystem,
    onDismiss: () -> Unit,
    onSelect: (UnitSystem) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.BackgroundCard,
        title = {
            Text(
                "Select Unit System",
                color = AppColors.Primary,
                style = AppTypography.HeadingSmall
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.Default)) {
                UnitSystem.values().forEach { unit ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(AppShapes.CardMedium)
                            .background(
                                if (unit == currentUnit) AppColors.Selected
                                else Color.Transparent
                            )
                            .clickable { onSelect(unit) }
                            .padding(AppDimensions.CardPadding.Default),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = unit == currentUnit,
                            onClick = { onSelect(unit) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = AppColors.Primary,
                                unselectedColor = AppColors.TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(AppDimensions.Spacing.Default))
                        Text(
                            text = unit.displayName,
                            color = AppColors.TextPrimary,
                            style = AppTypography.BodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AppColors.Primary)
            }
        },
        shape = AppShapes.Dialog
    )
}
