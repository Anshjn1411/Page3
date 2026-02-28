package dev.infa.page3.SDK.bottle.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.SDK.bottle.data.BottleAlarm
import dev.infa.page3.SDK.bottle.viewmodel.BottleViewModel
import dev.infa.page3.SDK.ui.theme.*

private val BottleBlue = Color(0xFF4FC3F7)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottleAlarmEditorScreen(
    viewModel: BottleViewModel,
    navigator: Navigator,
    existingAlarm: BottleAlarm? = null
) {
    val isEditing = existingAlarm != null

    var hour by remember { mutableStateOf(existingAlarm?.hour ?: 9) }
    var minute by remember { mutableStateOf(existingAlarm?.minute ?: 0) }
    var isOn by remember { mutableStateOf(existingAlarm?.isOn ?: true) }
    var repeatDays by remember {
        mutableStateOf(
            existingAlarm?.repeatDaysList()
                ?: listOf(true, true, true, true, true, false, false) // Mon-Fri default
        )
    }

    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Scaffold(
        containerColor = AppColors.BackgroundPrimary,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditing) "Edit Alarm" else "Add Alarm",
                        color = AppColors.TextPrimary,
                        style = AppTypography.HeadingSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = AppColors.TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.BackgroundPrimary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = AppDimensions.ScreenPadding.Horizontal),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            // ─── Time Picker ────────────────────────────────────────────────

            Text("⏰", fontSize = 48.sp)

            Spacer(Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Hour input
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = { hour = (hour + 1) % 24 }) {
                        Text("▲", color = BottleBlue, fontSize = 20.sp)
                    }
                    Box(
                        modifier = Modifier
                            .size(80.dp, 64.dp)
                            .clip(AppShapes.CardMedium)
                            .background(AppColors.SurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = hour.toString().padStart(2, '0'),
                            color = AppColors.TextPrimary,
                            style = AppTypography.DisplaySmall
                        )
                    }
                    IconButton(onClick = { hour = if (hour > 0) hour - 1 else 23 }) {
                        Text("▼", color = BottleBlue, fontSize = 20.sp)
                    }
                }

                Text(
                    ":",
                    color = AppColors.TextPrimary,
                    style = AppTypography.DisplaySmall,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                // Minute input
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = { minute = (minute + 1) % 60 }) {
                        Text("▲", color = BottleBlue, fontSize = 20.sp)
                    }
                    Box(
                        modifier = Modifier
                            .size(80.dp, 64.dp)
                            .clip(AppShapes.CardMedium)
                            .background(AppColors.SurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = minute.toString().padStart(2, '0'),
                            color = AppColors.TextPrimary,
                            style = AppTypography.DisplaySmall
                        )
                    }
                    IconButton(onClick = { minute = if (minute > 0) minute - 1 else 59 }) {
                        Text("▼", color = BottleBlue, fontSize = 20.sp)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // ─── Day Selector ───────────────────────────────────────────────

            Text(
                "Repeat Days",
                color = AppColors.TextSecondary,
                style = AppTypography.LabelLarge
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                dayLabels.forEachIndexed { index, label ->
                    val isSelected = repeatDays[index]
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) BottleBlue.copy(alpha = 0.2f)
                                else AppColors.SurfaceVariant
                            )
                            .clickable {
                                repeatDays = repeatDays.toMutableList().also {
                                    it[index] = !it[index]
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label.take(2),
                            color = if (isSelected) BottleBlue else AppColors.TextSecondary,
                            style = AppTypography.LabelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ─── Enable/Disable Toggle ──────────────────────────────────────

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(AppShapes.CardMedium)
                    .background(AppColors.SurfaceVariant)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Alarm enabled", color = AppColors.TextPrimary, style = AppTypography.BodyMedium)
                Switch(
                    checked = isOn,
                    onCheckedChange = { isOn = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = BottleBlue)
                )
            }

            Spacer(Modifier.weight(1f))

            // ─── Save / Cancel ──────────────────────────────────────────────

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { navigator.pop() },
                    shape = AppShapes.Chip,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AppColors.TextSecondary
                    )
                ) {
                    Text("Cancel", style = AppTypography.ButtonMedium)
                }

                Button(
                    onClick = {
                        val bitmask = BottleAlarm.buildRepeatBitmask(repeatDays)
                        val alarm = BottleAlarm(
                            id = existingAlarm?.id ?: 0,
                            isOn = isOn,
                            hour = hour,
                            minute = minute,
                            repeat = bitmask,
                            water = 0
                        )
                        viewModel.updateAlarm(alarm)
                        navigator.pop()
                    },
                    shape = AppShapes.Chip,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = BottleBlue)
                ) {
                    Text(
                        if (isEditing) "Update" else "Save",
                        color = Color.White,
                        style = AppTypography.ButtonMedium
                    )
                }
            }
        }
    }
}
