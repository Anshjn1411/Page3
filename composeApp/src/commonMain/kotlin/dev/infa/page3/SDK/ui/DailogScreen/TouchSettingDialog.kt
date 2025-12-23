package dev.infa.page3.SDK.ui.DailogScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.infa.page3.SDK.data.AppType
import dev.infa.page3.SDK.data.TouchSettings

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
        containerColor = Color(0xFF0D0D0D),
        title = {
            Text(
                "Touch & Gesture Settings",
                color = Color(0xFF00FF88),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // App Type
                Text("App Type", color = Color.White, fontWeight = FontWeight.Medium)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppType.values().forEach { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selectedAppType == app.code) Color(0x2200FF88)
                                    else Color.Transparent
                                )
                                .clickable { selectedAppType = app.code }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedAppType == app.code,
                                onClick = { selectedAppType = app.code },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFF00FF88),
                                    unselectedColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(app.displayName, color = Color.White)
                        }
                    }
                }

                Divider(color = Color.DarkGray)

                // Touch Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Touch Mode", color = Color.White)
                    Switch(
                        checked = isTouch,
                        onCheckedChange = { isTouch = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = Color(0xFF00FF88),
                            uncheckedThumbColor = Color.DarkGray,
                            uncheckedTrackColor = Color.Gray
                        )
                    )
                }

            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedAppType, isTouch, strength.toInt()) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF88)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF00FF88))
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
