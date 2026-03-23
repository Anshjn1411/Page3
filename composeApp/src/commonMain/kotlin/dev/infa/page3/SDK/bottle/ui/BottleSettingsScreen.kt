package dev.infa.page3.SDK.bottle.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.SDK.bottle.data.*
import dev.infa.page3.SDK.bottle.viewmodel.BottleViewModel

// ─── Palette ──────────────────────────────────────────────────────────────────────

private val Blue1   = Color(0xFF4FC3F7)
private val Blue2   = Color(0xFF0288D1)
private val BgDeep  = Color(0xFF0A0E1A)
private val BgCard  = Color(0xFF111827)
private val BgCard2 = Color(0xFF1A2235)
private val Divider = Color(0xFF1F2D45)

private val NavBottomPadding = 130.dp

// ─── Settings Screen ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottleSettingsScreen(
    viewModel: BottleViewModel,
    navigator: Navigator
) {
    val alarms        by viewModel.alarms.collectAsState()
    val colorLight    by viewModel.colorLight.collectAsState()
    val gradientOpt   by viewModel.gradientOption.collectAsState()
    val smartReminder by viewModel.funcSwitchSmartReminder.collectAsState()
    val standby       by viewModel.autoStandby.collectAsState()
    val dnd           by viewModel.doNotDisturb.collectAsState()
    val reminderColor by viewModel.reminderLightColor.collectAsState()
    val waterTarget   by viewModel.waterIntakeTarget.collectAsState()
    val logs          by viewModel.logs.collectAsState()

    var showResetDialog by remember { mutableStateOf(false) }
    var showLogsSection by remember { mutableStateOf(false) }

    // ── Scroll state → drive nav visibility ──────────────────────────────────────
    val listState       = rememberLazyListState()
    val navVisibility   = LocalNavVisibility.current

    // Track previous scroll offset to detect direction
    var prevScrollOffset by remember { mutableStateOf(0) }

    LaunchedEffect(listState.firstVisibleItemScrollOffset, listState.firstVisibleItemIndex) {
        val currentOffset = listState.firstVisibleItemIndex * 10000 + listState.firstVisibleItemScrollOffset
        val scrollingDown = currentOffset > prevScrollOffset
        prevScrollOffset = currentOffset

        // Hide nav when scrolling down, show when scrolling up or at top
        if (listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset < 50) {
            navVisibility.isVisible = true
        } else {
            navVisibility.isVisible = !scrollingDown
        }
    }

    // ── Factory Reset Dialog ──────────────────────────────────────────────────────
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Factory Reset", color = Color.White, fontWeight = FontWeight.Bold) },
            text  = {
                Text(
                    "This will erase all alarms, settings, and customizations. This cannot be undone.",
                    color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.factoryReset(); showResetDialog = false },
                    colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
                ) { Text("Reset", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.5f))
                }
            },
            containerColor = BgCard2,
            shape          = RoundedCornerShape(22.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
    ) {
        LazyColumn(
            state  = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start  = 20.dp,
                end    = 20.dp,
                top    = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp,
                bottom = NavBottomPadding
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // ── Title ─────────────────────────────────────────────────────────
            item {
                Text("Settings", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black, letterSpacing = 0.3.sp)
                Spacer(Modifier.height(2.dp))
                Text("Bottle preferences", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
            }

            // ── Drink Reminders ───────────────────────────────────────────────
            item { SettingsSectionLabel("⏰", "Drink Reminders") }

            if (alarms.isEmpty()) {
                item {
                    Box(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(BgCard2).padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) { Text("No alarms set", color = Color.White.copy(alpha = 0.35f), fontSize = 13.sp) }
                }
            } else {
                items(alarms) { alarm ->
                    SettingsAlarmRow(
                        alarm     = alarm,
                        onToggle  = { viewModel.updateAlarm(alarm.copy(isOn = it)) },
                        onDelete  = { viewModel.deleteAlarm(alarm.id) }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Blue1.copy(alpha = 0.08f))
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                            navigator.push(dev.infa.page3.SDK.bottle.navigation.BottleAlarmEditorNav())
                        }
                        .padding(16.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Add, null, tint = Blue1, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Add Alarm", color = Blue1, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // ── Water Target ──────────────────────────────────────────────────
            item { SettingsSectionLabel("🎯", "Daily Water Target") }
            item { SettingsWaterTarget(currentTarget = waterTarget ?: 2000, onSetTarget = { viewModel.setWaterIntakeTarget(it) }) }

            // ── Color Light ───────────────────────────────────────────────────
            item { SettingsSectionLabel("🌈", "Color Light") }
            item { SettingsColorLight(config = colorLight, onUpdate = { on, s, e -> viewModel.setColorLight(on, s, e) }) }

            // ── Gradient ──────────────────────────────────────────────────────
            item { SettingsSectionLabel("🔄", "Gradient Effect") }
            item {
                SettingsChipRow(
                    options       = GradientOption.entries.map { it.displayName },
                    selectedIndex = gradientOpt ?: 0,
                    onSelect      = { viewModel.setGradientOption(it) }
                )
            }

            // ── Smart Reminder ────────────────────────────────────────────────
            item { SettingsSectionLabel("🔔", "Smart Reminder") }
            item { SettingsToggleRow("Intelligent hydration reminders", smartReminder ?: false) { viewModel.setSmartReminder(it) } }

            // ── Auto Standby ──────────────────────────────────────────────────
            item { SettingsSectionLabel("😴", "Auto Standby") }
            item {
                SettingsChipRow(
                    options       = AutoStandbyOption.entries.map { it.displayName },
                    selectedIndex = standby ?: 0,
                    onSelect      = { viewModel.setAutoStandby(it) }
                )
            }

            // ── Do Not Disturb ────────────────────────────────────────────────
            item { SettingsSectionLabel("🌙", "Do Not Disturb") }
            item { SettingsDND(config = dnd, onUpdate = { on, sh, sm, eh, em -> viewModel.setDoNotDisturb(on, sh, sm, eh, em) }) }

            // ── Reminder Light ────────────────────────────────────────────────
            item { SettingsSectionLabel("💡", "Reminder Light Color") }
            item { SettingsReminderLight(colorIndex = reminderColor ?: 0, onSetColor = { viewModel.setReminderLight(it) }) }

            // ── Send Total Intake ─────────────────────────────────────────────
            item { SettingsSectionLabel("📤", "Send Total Intake") }
            item { SettingsSendIntake(onSend = { viewModel.sendTotalDailyWaterIntake(it) }) }

            // ── Factory Reset ─────────────────────────────────────────────────
            item { Spacer(Modifier.height(8.dp)) }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFEF5350).copy(alpha = 0.08f))
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { showResetDialog = true }
                        .padding(16.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.RestartAlt, null, tint = Color(0xFFEF5350), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Factory Reset", color = Color(0xFFEF5350), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // ── Debug Logs ────────────────────────────────────────────────────
            item { Spacer(Modifier.height(4.dp)) }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(BgCard2)
                        .clickable { showLogsSection = !showLogsSection }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text("📋 Debug Logs (${logs.size})", color = Color.White.copy(alpha = 0.55f), fontSize = 13.sp)
                    Icon(
                        if (showLogsSection) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        null,
                        tint     = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (showLogsSection) {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { viewModel.clearLogs() }) {
                            Text("Clear", color = Blue1, fontSize = 12.sp)
                        }
                    }
                }
                items(logs.take(50)) { log ->
                    Text(log, color = Color.White.copy(alpha = 0.28f), fontSize = 10.sp, lineHeight = 14.sp, modifier = Modifier.padding(vertical = 1.dp))
                }
            }
        }
    }
}

// ─── Section Label ────────────────────────────────────────────────────────────────

@Composable
private fun SettingsSectionLabel(emoji: String, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    ) {
        Text(emoji, fontSize = 15.sp)
        Spacer(Modifier.width(7.dp))
        Text(title, color = Color.White.copy(alpha = 0.65f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp)
    }
}

// ─── Alarm Row ────────────────────────────────────────────────────────────────────

@Composable
private fun SettingsAlarmRow(alarm: BottleAlarm, onToggle: (Boolean) -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(BgCard2)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "${alarm.hour.toString().padStart(2,'0')}:${alarm.minute.toString().padStart(2,'0')}",
                color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp
            )
            Text(alarm.repeatDaysString(), color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
        }
        Switch(checked = alarm.isOn, onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(checkedTrackColor = Blue1, uncheckedTrackColor = Divider))
        Spacer(Modifier.width(4.dp))
        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Delete, null, tint = Color(0xFFEF5350).copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
        }
    }
}

// ─── Water Target ─────────────────────────────────────────────────────────────────

@Composable
private fun SettingsWaterTarget(currentTarget: Int, onSetTarget: (Int) -> Unit) {
    var text by remember { mutableStateOf(currentTarget.toString()) }
    LaunchedEffect(currentTarget) { text = currentTarget.toString() }
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(BgCard2).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it.filter { c -> c.isDigit() } },
            label = { Text("Target (mL)", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Blue1, unfocusedBorderColor = Divider,
                focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = Blue1
            ),
            singleLine = true
        )
        Spacer(Modifier.width(12.dp))
        Button(onClick = { text.toIntOrNull()?.let { onSetTarget(it) } }, shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue1)) {
            Text("Set", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// ─── Color Light ─────────────────────────────────────────────────────────────────

@Composable
private fun SettingsColorLight(config: ColorLightConfig?, onUpdate: (Boolean, Int, Int) -> Unit) {
    var isOn       by remember { mutableStateOf(config?.isOn ?: false) }
    var startColor by remember { mutableStateOf((config?.startColorIndex ?: 0).toFloat()) }
    var endColor   by remember { mutableStateOf((config?.endColorIndex ?: 180).toFloat()) }
    LaunchedEffect(config) {
        config?.let { isOn = it.isOn; startColor = it.startColorIndex.toFloat(); endColor = it.endColorIndex.toFloat() }
    }
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(BgCard2).padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Color Light", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Switch(checked = isOn, onCheckedChange = { isOn = it; onUpdate(it, startColor.toInt(), endColor.toInt()) },
                colors = SwitchDefaults.colors(checkedTrackColor = Blue1, uncheckedTrackColor = Divider))
        }
        Spacer(Modifier.height(14.dp))
        Text("Start  ${startColor.toInt()}°", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
        Slider(value = startColor, onValueChange = { startColor = it },
            onValueChangeFinished = { onUpdate(isOn, startColor.toInt(), endColor.toInt()) }, valueRange = 0f..359f,
            colors = SliderDefaults.colors(thumbColor = hueToColor(startColor.toInt()), activeTrackColor = hueToColor(startColor.toInt())))
        Text("End  ${endColor.toInt()}°", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
        Slider(value = endColor, onValueChange = { endColor = it },
            onValueChangeFinished = { onUpdate(isOn, startColor.toInt(), endColor.toInt()) }, valueRange = 0f..359f,
            colors = SliderDefaults.colors(thumbColor = hueToColor(endColor.toInt()), activeTrackColor = hueToColor(endColor.toInt())))
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Box(Modifier.size(28.dp).clip(CircleShape).background(hueToColor(startColor.toInt())))
            Spacer(Modifier.width(8.dp))
            Text("→", color = Color.White.copy(alpha = 0.4f), fontSize = 16.sp)
            Spacer(Modifier.width(8.dp))
            Box(Modifier.size(28.dp).clip(CircleShape).background(hueToColor(endColor.toInt())))
        }
    }
}

// ─── Toggle Row ───────────────────────────────────────────────────────────────────

@Composable
private fun SettingsToggleRow(label: String, isOn: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(BgCard2).padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp, modifier = Modifier.weight(1f))
        Switch(checked = isOn, onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(checkedTrackColor = Blue1, uncheckedTrackColor = Divider))
    }
}

// ─── Chip Row ────────────────────────────────────────────────────────────────────

@Composable
private fun SettingsChipRow(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isSelected) Brush.horizontalGradient(listOf(Blue1.copy(0.25f), Blue2.copy(0.15f)))
                        else Brush.horizontalGradient(listOf(BgCard2, BgCard2))
                    )
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onSelect(index) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(label, color = if (isSelected) Blue1 else Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, textAlign = TextAlign.Center)
            }
        }
    }
}

// ─── Do Not Disturb ───────────────────────────────────────────────────────────────

@Composable
private fun SettingsDND(config: DoNotDisturbConfig?, onUpdate: (Boolean, Int, Int, Int, Int) -> Unit) {
    var isOn   by remember { mutableStateOf(config?.isOn ?: false) }
    var startH by remember { mutableStateOf(config?.startHour ?: 22) }
    var startM by remember { mutableStateOf(config?.startMinute ?: 0) }
    var endH   by remember { mutableStateOf(config?.endHour ?: 7) }
    var endM   by remember { mutableStateOf(config?.endMinute ?: 0) }
    LaunchedEffect(config) {
        config?.let { isOn = it.isOn; startH = it.startHour; startM = it.startMinute; endH = it.endHour; endM = it.endMinute }
    }
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(BgCard2).padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Do Not Disturb", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Switch(checked = isOn, onCheckedChange = { isOn = it; onUpdate(it, startH, startM, endH, endM) },
                colors = SwitchDefaults.colors(checkedTrackColor = Blue1, uncheckedTrackColor = Divider))
        }
        AnimatedVisibility(visible = isOn, enter = expandVertically(), exit = shrinkVertically()) {
            Column {
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    DNDTimeInput("Start", startH, startM,
                        onH = { startH = it; onUpdate(isOn, startH, startM, endH, endM) },
                        onM = { startM = it; onUpdate(isOn, startH, startM, endH, endM) })
                    Text("→", color = Color.White.copy(alpha = 0.4f), fontSize = 20.sp, modifier = Modifier.align(Alignment.CenterVertically))
                    DNDTimeInput("End", endH, endM,
                        onH = { endH = it; onUpdate(isOn, startH, startM, endH, endM) },
                        onM = { endM = it; onUpdate(isOn, startH, startM, endH, endM) })
                }
            }
        }
    }
}

@Composable
private fun DNDTimeInput(label: String, hour: Int, minute: Int, onH: (Int) -> Unit, onM: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            CompactTimeField(hour.toString().padStart(2,'0')) {
                it.filter { c -> c.isDigit() }.take(2).toIntOrNull()?.let { h -> if (h in 0..23) onH(h) }
            }
            Text(":", color = Color.White, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 4.dp))
            CompactTimeField(minute.toString().padStart(2,'0')) {
                it.filter { c -> c.isDigit() }.take(2).toIntOrNull()?.let { m -> if (m in 0..59) onM(m) }
            }
        }
    }
}

@Composable
private fun CompactTimeField(value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onChange,
        modifier = Modifier.width(50.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Blue1, unfocusedBorderColor = Divider,
            focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = Blue1
        ),
        singleLine = true,
        textStyle  = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 15.sp, fontWeight = FontWeight.Bold),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

// ─── Reminder Light ───────────────────────────────────────────────────────────────

@Composable
private fun SettingsReminderLight(colorIndex: Int, onSetColor: (Int) -> Unit) {
    var sliderValue by remember { mutableStateOf(colorIndex.toFloat()) }
    LaunchedEffect(colorIndex) { sliderValue = colorIndex.toFloat() }
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(BgCard2).padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Color: ${sliderValue.toInt()}°", color = Color.White.copy(alpha = 0.55f), fontSize = 12.sp)
            Box(Modifier.size(26.dp).clip(CircleShape).background(hueToColor(sliderValue.toInt())))
        }
        Spacer(Modifier.height(6.dp))
        Slider(value = sliderValue, onValueChange = { sliderValue = it },
            onValueChangeFinished = { onSetColor(sliderValue.toInt()) }, valueRange = 0f..359f,
            colors = SliderDefaults.colors(thumbColor = hueToColor(sliderValue.toInt()), activeTrackColor = hueToColor(sliderValue.toInt())))
    }
}

// ─── Send Intake ─────────────────────────────────────────────────────────────────

@Composable
private fun SettingsSendIntake(onSend: (Int) -> Unit) {
    var text by remember { mutableStateOf("") }
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(BgCard2).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text, onValueChange = { text = it.filter { c -> c.isDigit() } },
            label = { Text("Total intake (mL)", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Blue1, unfocusedBorderColor = Divider,
                focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = Blue1
            ),
            singleLine = true
        )
        Spacer(Modifier.width(12.dp))
        Button(onClick = { text.toIntOrNull()?.let { onSend(it) } }, shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue1)) {
            Text("Send", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// ─── Utility: Hue → Compose Color ────────────────────────────────────────────────

internal fun hueToColor(hue: Int): Color {
    val h = hue.coerceIn(0, 359)
    val c = 1f
    val x = c * (1f - kotlin.math.abs((h / 60f) % 2f - 1f))
    val (r1, g1, b1) = when (h) {
        in 0..59    -> Triple(c, x, 0f)
        in 60..119  -> Triple(x, c, 0f)
        in 120..179 -> Triple(0f, c, x)
        in 180..239 -> Triple(0f, x, c)
        in 240..299 -> Triple(x, 0f, c)
        else        -> Triple(c, 0f, x)
    }
    return Color(r1, g1, b1)
}