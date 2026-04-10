package dev.infa.page3.SDK.`V-Band`.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.SDK.`V-Band`.data.*
import dev.infa.page3.SDK.`V-Band`.viewmodel.VBandViewModel
import kotlin.math.roundToInt

// ─── Colors ──────────────────────────────────────────────────────────────────────

private val VBandAccent      = Color(0xFF26A69A)
private val VBandAccentDark  = Color(0xFF00695C)
private val BgDeep           = Color(0xFF0A0E1A)
private val BgCard           = Color(0xFF111827)
private val BgCard2          = Color(0xFF1A2235)
private val HeartRed         = Color(0xFFEF5350)
private val SleepPurple      = Color(0xFF7E57C2)
private val TempYellow       = Color(0xFFFDD835)

// ─── Settings Screen ─────────────────────────────────────────────────────────────

@Composable
fun VBandSettingsScreen(
    viewModel: VBandViewModel,
    navigator: Navigator
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val customSettingData by viewModel.customSettingData.collectAsState()
    val nightTurnWristData by viewModel.nightTurnWristData.collectAsState()
    val screenLightData by viewModel.screenLightData.collectAsState()
    val screenLightTimeData by viewModel.screenLightTimeData.collectAsState()
    val heartWarningData by viewModel.heartWarningData.collectAsState()
    val healthRemindList by viewModel.healthRemindList.collectAsState()
    val batteryData by viewModel.batteryData.collectAsState()

    val isConnected = connectionState == "CONNECTED"

    val navVisibility = LocalVBandNavVisibility.current
    val listState = rememberLazyListState()
    var prevScrollOffset by remember { mutableStateOf(0) }

    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        val currentOffset = listState.firstVisibleItemIndex * 10000 + listState.firstVisibleItemScrollOffset
        val scrollingDown = currentOffset > prevScrollOffset
        prevScrollOffset = currentOffset
        navVisibility.isVisible =
            if (listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset < 50) true
            else !scrollingDown
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep),
        contentPadding = PaddingValues(
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
            bottom = 100.dp,
            start = 16.dp,
            end = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Header ─────────────────────────────────────────────────────────────
        item {
            Text(
                text = "Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (!isConnected) {
            item {
                GlassCard(gradStart = Color(0xFF757575), modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("⌚", fontSize = 28.sp)
                        Column {
                            Text(
                                text = "Not Connected",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Connect your V-Band to manage settings",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
            return@LazyColumn
        }

        // ── Device Info Section ────────────────────────────────────────────────
        item {
            SettingsSectionHeader("DEVICE")
        }

        item {
            val percent = if (batteryData?.isPercent == true) batteryData?.batteryPercent ?: 0
                          else (batteryData?.batteryLevel ?: 0) * 25
            SettingsItem(
                emoji = "🔋",
                title = "Battery",
                subtitle = "$percent%",
                onClick = { viewModel.readBattery() }
            )
        }

        // ── Display Section ────────────────────────────────────────────────────
        item {
            SettingsSectionHeader("DISPLAY")
        }

        // Screen Brightness
        item {
            val level = screenLightData?.level ?: 0
            val maxLevel = screenLightData?.maxLevel ?: 5
            SettingsItem(
                emoji = "☀️",
                title = "Screen Brightness",
                subtitle = "Level $level / $maxLevel",
                onClick = { viewModel.readScreenLight() }
            ) {
                // Brightness slider
                var sliderValue by remember(level) { mutableStateOf(level.toFloat()) }
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    onValueChangeFinished = {
                        val newLevel = sliderValue.roundToInt()
                        viewModel.settingScreenLight(
                            screenLightData?.startHour ?: 22,
                            screenLightData?.startMinute ?: 0,
                            screenLightData?.endHour ?: 7,
                            screenLightData?.endMinute ?: 0,
                            newLevel,
                            screenLightData?.otherLevel ?: 4
                        )
                    },
                    valueRange = 1f..maxLevel.toFloat(),
                    steps = maxLevel - 2,
                    colors = SliderDefaults.colors(
                        thumbColor = VBandAccent,
                        activeTrackColor = VBandAccent,
                        inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        // Screen On Time
        item {
            val duration = screenLightTimeData?.currentDuration ?: 5
            SettingsItem(
                emoji = "⏱️",
                title = "Screen On Time",
                subtitle = "${duration}s",
                onClick = { viewModel.readScreenLightTime() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(3, 5, 10, 15, 20).forEach { sec ->
                        val isSelected = duration == sec
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) VBandAccent.copy(alpha = 0.25f)
                                    else Color.White.copy(alpha = 0.06f)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) VBandAccent.copy(alpha = 0.5f)
                                    else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.setScreenLightTime(sec) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "${sec}s",
                                color = if (isSelected) VBandAccent else Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // ── Gestures Section ───────────────────────────────────────────────────
        item {
            SettingsSectionHeader("GESTURES")
        }

        // Night Turn Wrist (Raise to Wake)
        item {
            val isOpen = nightTurnWristData?.isOpen ?: false
            SettingsItem(
                emoji = "🤚",
                title = "Raise to Wake",
                subtitle = if (isOpen) "On" else "Off",
                onClick = { viewModel.readNightTurnWrist() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isOpen) "Enabled" else "Disabled",
                        color = if (isOpen) VBandAccent else Color.White.copy(alpha = 0.4f),
                        fontSize = 13.sp
                    )
                    Switch(
                        checked = isOpen,
                        onCheckedChange = { newValue ->
                            viewModel.settingNightTurnWrist(
                                isOpen = newValue,
                                startHour = nightTurnWristData?.startHour ?: 20,
                                startMinute = nightTurnWristData?.startMinute ?: 0,
                                endHour = nightTurnWristData?.endHour ?: 8,
                                endMinute = nightTurnWristData?.endMinute ?: 0,
                                level = nightTurnWristData?.level ?: 5
                            )
                        },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = VBandAccent,
                            uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }

        // ── Health Section ─────────────────────────────────────────────────────
        item {
            SettingsSectionHeader("HEALTH")
        }

        // Heart Rate Warning
        item {
            val isOpen = heartWarningData?.isOpen ?: false
            val high = heartWarningData?.heartHigh ?: 150
            val low = heartWarningData?.heartLow ?: 40
            SettingsItem(
                emoji = "❤️",
                title = "Heart Rate Alert",
                subtitle = if (isOpen) "High: $high | Low: $low" else "Off",
                onClick = { viewModel.readHeartWarning() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isOpen) "Alerts enabled" else "Enable alerts",
                        color = if (isOpen) HeartRed else Color.White.copy(alpha = 0.4f),
                        fontSize = 13.sp
                    )
                    Switch(
                        checked = isOpen,
                        onCheckedChange = { newValue ->
                            viewModel.settingHeartWarning(high, low, newValue)
                        },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = HeartRed,
                            uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }

        // ── General Section ────────────────────────────────────────────────────
        item {
            SettingsSectionHeader("GENERAL")
        }

        // 24-Hour Format
        item {
            val is24Hour = customSettingData?.is24Hour ?: true
            SettingsItem(
                emoji = "🕐",
                title = "24-Hour Format",
                subtitle = if (is24Hour) "On" else "Off",
                onClick = {}
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (is24Hour) "24-hour" else "12-hour (AM/PM)",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 13.sp
                    )
                    Switch(
                        checked = is24Hour,
                        onCheckedChange = { newValue ->
                            customSettingData?.let { current ->
                                viewModel.changeCustomSetting(current.copy(is24Hour = newValue))
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = VBandAccent,
                            uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }

        // ── Danger Zone ────────────────────────────────────────────────────────
        item {
            Spacer(Modifier.height(8.dp))
        }

        item {
            // Disconnect button
            Button(
                onClick = { viewModel.disconnect() },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = HeartRed
                ),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.5.dp, HeartRed.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .background(HeartRed.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Disconnect V-Band",
                        color = HeartRed,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ─── Settings Section Header ─────────────────────────────────────────────────────

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = VBandAccent.copy(alpha = 0.6f),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

// ─── Settings Item ───────────────────────────────────────────────────────────────

@Composable
private fun SettingsItem(
    emoji: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    expandedContent: (@Composable () -> Unit)? = null
) {
    var isExpanded by remember { mutableStateOf(false) }

    GlassCard(
        gradStart = Color(0xFF26A69A),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (expandedContent != null) isExpanded = !isExpanded
                onClick()
            }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(emoji, fontSize = 22.sp)
                    Column {
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = subtitle,
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 12.sp
                        )
                    }
                }

                if (expandedContent != null) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp
                                      else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (expandedContent != null && isExpanded) {
                Spacer(Modifier.height(12.dp))
                expandedContent()
            }
        }
    }
}
