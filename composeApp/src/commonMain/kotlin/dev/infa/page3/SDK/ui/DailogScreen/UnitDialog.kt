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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.infa.page3.SDK.data.UnitSystem


@Composable
fun UnitSystemDialog(
    currentUnit: UnitSystem,
    onDismiss: () -> Unit,
    onSelect: (UnitSystem) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0D0D0D),
        title = {
            Text(
                "Select Unit System",
                color = Color(0xFF00FF88),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                UnitSystem.values().forEach { unit ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (unit == currentUnit) Color(0x2200FF88)
                                else Color.Transparent
                            )
                            .clickable { onSelect(unit) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = unit == currentUnit,
                            onClick = { onSelect(unit) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color(0xFF00FF88),
                                unselectedColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = unit.displayName,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF00FF88))
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

