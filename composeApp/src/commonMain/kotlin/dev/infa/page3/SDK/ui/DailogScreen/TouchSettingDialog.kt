package dev.infa.page3.SDK.ui.DailogScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import dev.infa.page3.SDK.data.AppType
import dev.infa.page3.SDK.data.TouchSettings
import dev.infa.page3.SDK.ui.theme.*

@Composable
fun TouchSettingsDialog(
    touchSettings: TouchSettings,
    onDismiss: () -> Unit,
    onSave: (Int, Boolean, Int) -> Unit
) {
    var selectedAppType by remember { mutableStateOf(touchSettings.appType) }
    var isTouch by remember { mutableStateOf(touchSettings.isTouch) }
    var strength by remember { mutableStateOf(touchSettings.strength.toFloat()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.BackgroundCard,
        title = {
            Text(
                "Touch & Gesture Settings",
                color = AppColors.Primary,
                style = AppTypography.HeadingSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.ExtraLarge)
            ) {
                // App Type
                Text(
                    "App Type",
                    color = AppColors.TextPrimary,
                    style = AppTypography.LabelLarge
                )

                Column(verticalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.Medium)) {
                    AppType.values().forEach { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(AppShapes.CardMedium)
                                .background(
                                    if (selectedAppType == app.code) AppColors.Selected
                                    else Color.Transparent
                                )
                                .clickable { selectedAppType = app.code }
                                .padding(AppDimensions.CardPadding.Medium),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedAppType == app.code,
                                onClick = { selectedAppType = app.code },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = AppColors.Primary,
                                    unselectedColor = AppColors.TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(AppDimensions.Spacing.Default))
                            Text(app.displayName, color = AppColors.TextPrimary)
                        }
                    }
                }

                Divider(color = AppColors.BorderSecondary)

                // Touch Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Touch Mode", color = AppColors.TextPrimary)
                    Switch(
                        checked = isTouch,
                        onCheckedChange = { isTouch = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AppColors.BackgroundPrimary,
                            checkedTrackColor = AppColors.Primary,
                            uncheckedThumbColor = AppColors.TextTertiary,
                            uncheckedTrackColor = AppColors.TextSecondary
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedAppType, isTouch, strength.toInt()) },
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                shape = AppShapes.ButtonMedium
            ) {
                Text(
                    "Save",
                    color = AppColors.BackgroundPrimary,
                    style = AppTypography.ButtonMedium
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AppColors.Primary)
            }
        },
        shape = AppShapes.Dialog
    )
}
