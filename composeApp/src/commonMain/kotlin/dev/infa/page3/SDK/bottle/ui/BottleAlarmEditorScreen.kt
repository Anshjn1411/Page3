package dev.infa.page3.SDK.bottle.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.SDK.bottle.data.BottleAlarm
import dev.infa.page3.SDK.bottle.viewmodel.BottleViewModel
import dev.infa.page3.SDK.ui.theme.*

private val Blue1   = Color(0xFF4FC3F7)
private val Blue2   = Color(0xFF0288D1)
private val BgDeep  = Color(0xFF0A0E1A)
private val BgCard2 = Color(0xFF1A2235)
private val Divider = Color(0xFF1F2D45)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottleAlarmEditorScreen(
    viewModel: BottleViewModel,
    navigator: Navigator,
    existingAlarm: BottleAlarm? = null
) {
    val isEditing = existingAlarm != null

    var hour       by remember { mutableStateOf(existingAlarm?.hour ?: 9) }
    var minute     by remember { mutableStateOf(existingAlarm?.minute ?: 0) }
    var isOn       by remember { mutableStateOf(existingAlarm?.isOn ?: true) }
    var repeatDays by remember {
        mutableStateOf(existingAlarm?.repeatDaysList() ?: listOf(true, true, true, true, true, false, false))
    }
    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── App bar ─────────────────────────────────────────────────────
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navigator.pop() }) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White.copy(alpha = 0.7f))
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isEditing) "Edit Alarm" else "New Alarm",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(36.dp))

            // ── Time display ─────────────────────────────────────────────────
            Text("⏰", fontSize = 52.sp)
            Spacer(Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                TimeSpinner(value = hour, onIncrease = { hour = (hour + 1) % 24 }, onDecrease = { hour = if (hour > 0) hour - 1 else 23 })
                Text(":", color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 12.dp))
                TimeSpinner(value = minute, onIncrease = { minute = (minute + 1) % 60 }, onDecrease = { minute = if (minute > 0) minute - 1 else 59 })
            }

            Spacer(Modifier.height(36.dp))

            // ── Days selector ─────────────────────────────────────────────────
            Text("Repeat Days", color = Color.White.copy(alpha = 0.45f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                dayLabels.forEachIndexed { index, label ->
                    val isSelected = repeatDays[index]
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected)
                                    Brush.radialGradient(listOf(Blue1.copy(0.35f), Blue2.copy(0.15f)))
                                else
                                    Brush.radialGradient(listOf(BgCard2, BgCard2))
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                repeatDays = repeatDays.toMutableList().also { it[index] = !it[index] }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label.take(2),
                            color = if (isSelected) Blue1 else Color.White.copy(alpha = 0.35f),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Enable toggle ────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(BgCard2)
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Alarm enabled", color = Color.White.copy(alpha = 0.75f), fontSize = 14.sp)
                Switch(
                    checked = isOn,
                    onCheckedChange = { isOn = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = Blue1, uncheckedTrackColor = Divider)
                )
            }

            Spacer(Modifier.weight(1f))

            // ── Action Buttons ───────────────────────────────────────────────
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { navigator.pop() },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.5f)),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                ) {
                    Text("Cancel", fontWeight = FontWeight.SemiBold)
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
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue1)
                ) {
                    Text(if (isEditing) "Update" else "Save", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

// ─── Time Spinner Component ───────────────────────────────────────────────────────

@Composable
private fun TimeSpinner(value: Int, onIncrease: () -> Unit, onDecrease: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFF1A2235))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onIncrease() },
            contentAlignment = Alignment.Center
        ) {
            Text("▲", color = Blue1.copy(alpha = 0.8f), fontSize = 18.sp)
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .size(84.dp, 68.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(listOf(Color(0xFF1A2235), Color(0xFF111827)))
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                value.toString().padStart(2, '0'),
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-2).sp
            )
        }
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFF1A2235))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onDecrease() },
            contentAlignment = Alignment.Center
        ) {
            Text("▼", color = Blue1.copy(alpha = 0.8f), fontSize = 18.sp)
        }
    }
}