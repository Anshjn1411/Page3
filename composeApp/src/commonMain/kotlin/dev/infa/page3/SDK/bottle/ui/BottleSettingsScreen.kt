package dev.infa.page3.SDK.bottle.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import dev.infa.page3.SDK.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

private val BottleBlue = Color(0xFF4FC3F7)
private val BottleBlueDark = Color(0xFF0288D1)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottleSettingsScreen(
    viewModel: BottleViewModel,
    navigator: Navigator
) {
    val alarms by viewModel.alarms.collectAsState()
    val colorLight by viewModel.colorLight.collectAsState()
    val gradientOpt by viewModel.gradientOption.collectAsState()
    val smartReminder by viewModel.funcSwitchSmartReminder.collectAsState()
    val standby by viewModel.autoStandby.collectAsState()
    val dnd by viewModel.doNotDisturb.collectAsState()
    val reminderColor by viewModel.reminderLightColor.collectAsState()
    val waterTarget by viewModel.waterIntakeTarget.collectAsState()
    val logs by viewModel.logs.collectAsState()

    var showFactoryResetDialog by remember { mutableStateOf(false) }
    var showLogsSection by remember { mutableStateOf(false) }

    // Factory Reset Confirmation
    if (showFactoryResetDialog) {
        AlertDialog(
            onDismissRequest = { showFactoryResetDialog = false },
            title = {
                Text("Factory Reset", color = AppColors.TextPrimary)
            },
            text = {
                Text(
                    "This will erase all alarms, settings, and customizations on the bottle. This action cannot be undone.",
                    color = AppColors.TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.factoryReset()
                        showFactoryResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Error)
                ) {
                    Text("Reset", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFactoryResetDialog = false }) {
                    Text("Cancel", color = AppColors.TextSecondary)
                }
            },
            containerColor = AppColors.BackgroundTertiary,
            shape = AppShapes.Dialog
        )
    }

    Scaffold(
        containerColor = AppColors.BackgroundPrimary,
        topBar = {
            TopAppBar(
                title = {
                    Text("Bottle Settings", color = AppColors.TextPrimary, style = AppTypography.HeadingSmall)
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = AppDimensions.ScreenPadding.Horizontal),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ─── Alarms Section ─────────────────────────────────────────────

            item {
                SectionHeader("⏰", "Drink Reminders")
            }

            if (alarms.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(AppShapes.CardMedium)
                            .background(AppColors.SurfaceVariant)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No alarms set", color = AppColors.TextSecondary, style = AppTypography.BodySmall)
                    }
                }
            } else {
                items(alarms) { alarm ->
                    AlarmCard(
                        alarm = alarm,
                        onToggle = { isOn ->
                            viewModel.updateAlarm(alarm.copy(isOn = isOn))
                        },
                        onDelete = { viewModel.deleteAlarm(alarm.id) }
                    )
                }
            }

            item {
                Button(
                    onClick = {
                        navigator.push(
                            dev.infa.page3.SDK.bottle.navigation.BottleAlarmEditorNav()
                        )
                    },
                    shape = AppShapes.Chip,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BottleBlue.copy(alpha = 0.15f),
                        contentColor = BottleBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = BottleBlue)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Alarm", style = AppTypography.ButtonMedium, color = BottleBlue)
                }
            }

            // ─── Water Target ───────────────────────────────────────────────

            item {
                SectionHeader("🎯", "Water Target")
            }

            item {
                WaterTargetCard(
                    currentTarget = waterTarget ?: 2000,
                    onSetTarget = { viewModel.setWaterIntakeTarget(it) }
                )
            }

            // ─── Color Light ────────────────────────────────────────────────

            item {
                SectionHeader("🌈", "Color Light")
            }

            item {
                ColorLightCard(
                    config = colorLight,
                    onUpdate = { on, start, end ->
                        viewModel.setColorLight(on, start, end)
                    }
                )
            }

            // ─── Gradient Effect ────────────────────────────────────────────

            item {
                SectionHeader("🔄", "Gradient Effect")
            }

            item {
                ChipSelector(
                    options = GradientOption.entries.map { it.displayName },
                    selectedIndex = gradientOpt ?: 0,
                    onSelect = { viewModel.setGradientOption(it) }
                )
            }

            // ─── Smart Reminder ─────────────────────────────────────────────

            item {
                SectionHeader("🔔", "Smart Reminder")
            }

            item {
                ToggleCard(
                    label = "Intelligent hydration reminders",
                    isOn = smartReminder ?: false,
                    onToggle = { viewModel.setSmartReminder(it) }
                )
            }

            // ─── Auto Standby ───────────────────────────────────────────────

            item {
                SectionHeader("😴", "Auto Standby")
            }

            item {
                ChipSelector(
                    options = AutoStandbyOption.entries.map { it.displayName },
                    selectedIndex = standby ?: 0,
                    onSelect = { viewModel.setAutoStandby(it) }
                )
            }

            // ─── Do Not Disturb ─────────────────────────────────────────────

            item {
                SectionHeader("🌙", "Do Not Disturb")
            }

            item {
                DoNotDisturbCard(
                    config = dnd,
                    onUpdate = { on, sh, sm, eh, em ->
                        viewModel.setDoNotDisturb(on, sh, sm, eh, em)
                    }
                )
            }

            // ─── Reminder Light Color ───────────────────────────────────────

            item {
                SectionHeader("💡", "Reminder Light Color")
            }

            item {
                ReminderLightCard(
                    colorIndex = reminderColor ?: 0,
                    onSetColor = { viewModel.setReminderLight(it) }
                )
            }

            // ─── Send Total Intake ──────────────────────────────────────────

            item {
                SectionHeader("📤", "Send Total Intake")
            }

            item {
                SendTotalIntakeCard(
                    onSend = { viewModel.sendTotalDailyWaterIntake(it) }
                )
            }

            // ─── Factory Reset ──────────────────────────────────────────────

            item {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { showFactoryResetDialog = true },
                    shape = AppShapes.Chip,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Error.copy(alpha = 0.15f),
                        contentColor = AppColors.Error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, tint = AppColors.Error)
                    Spacer(Modifier.width(8.dp))
                    Text("Factory Reset", style = AppTypography.ButtonMedium, color = AppColors.Error)
                }
            }

            // ─── Debug Logs ─────────────────────────────────────────────────

            item {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(AppShapes.CardMedium)
                        .background(AppColors.SurfaceVariant)
                        .clickable { showLogsSection = !showLogsSection }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📋 Debug Logs (${logs.size})", color = AppColors.TextSecondary, style = AppTypography.BodyMedium)
                    Icon(
                        if (showLogsSection) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = AppColors.TextSecondary
                    )
                }
            }

            if (showLogsSection) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { viewModel.clearLogs() }) {
                            Text("Clear", color = BottleBlue, style = AppTypography.LabelMedium)
                        }
                    }
                }
                items(logs.take(50)) { log ->
                    Text(
                        text = log,
                        color = AppColors.TextTertiary,
                        style = AppTypography.LabelExtraSmall,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }

            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

// ─── Section Header ─────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(emoji: String, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Text(emoji, fontSize = 18.sp)
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            color = AppColors.TextPrimary,
            style = AppTypography.HeadingExtraSmall
        )
    }
}

// ─── Alarm Card ─────────────────────────────────────────────────────────────────

@Composable
private fun AlarmCard(
    alarm: BottleAlarm,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.CardMedium)
            .background(AppColors.SurfaceVariant)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${alarm.hour.toString().padStart(2, '0')}:${alarm.minute.toString().padStart(2, '0')}",
                color = AppColors.TextPrimary,
                style = AppTypography.ValueLarge
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = alarm.repeatDaysString(),
                color = AppColors.TextSecondary,
                style = AppTypography.LabelMedium
            )
        }

        Switch(
            checked = alarm.isOn,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedTrackColor = BottleBlue,
                uncheckedTrackColor = AppColors.SurfaceVariant
            )
        )

        Spacer(Modifier.width(8.dp))

        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, "Delete", tint = AppColors.Error)
        }
    }
}

// ─── Water Target Card ──────────────────────────────────────────────────────────

@Composable
private fun WaterTargetCard(
    currentTarget: Int,
    onSetTarget: (Int) -> Unit
) {
    var inputText by remember { mutableStateOf(currentTarget.toString()) }

    LaunchedEffect(currentTarget) {
        inputText = currentTarget.toString()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.CardMedium)
            .background(AppColors.SurfaceVariant)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it.filter { c -> c.isDigit() } },
            label = { Text("Target (mL)", color = AppColors.TextSecondary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BottleBlue,
                unfocusedBorderColor = AppColors.BorderPrimary,
                focusedTextColor = AppColors.TextPrimary,
                unfocusedTextColor = AppColors.TextPrimary,
                cursorColor = BottleBlue
            ),
            singleLine = true
        )

        Spacer(Modifier.width(12.dp))

        Button(
            onClick = {
                inputText.toIntOrNull()?.let { onSetTarget(it) }
            },
            shape = AppShapes.ButtonMedium,
            colors = ButtonDefaults.buttonColors(containerColor = BottleBlue)
        ) {
            Text("Set", color = Color.White, style = AppTypography.ButtonMedium)
        }
    }
}

// ─── Color Light Card ───────────────────────────────────────────────────────────

@Composable
private fun ColorLightCard(
    config: ColorLightConfig?,
    onUpdate: (Boolean, Int, Int) -> Unit
) {
    var isOn by remember { mutableStateOf(config?.isOn ?: false) }
    var startColor by remember { mutableStateOf((config?.startColorIndex ?: 0).toFloat()) }
    var endColor by remember { mutableStateOf((config?.endColorIndex ?: 180).toFloat()) }

    LaunchedEffect(config) {
        config?.let {
            isOn = it.isOn
            startColor = it.startColorIndex.toFloat()
            endColor = it.endColorIndex.toFloat()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.CardMedium)
            .background(AppColors.SurfaceVariant)
            .padding(16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Light", color = AppColors.TextPrimary, style = AppTypography.BodyMedium)
            Switch(
                checked = isOn,
                onCheckedChange = {
                    isOn = it
                    onUpdate(it, startColor.toInt(), endColor.toInt())
                },
                colors = SwitchDefaults.colors(checkedTrackColor = BottleBlue)
            )
        }

        Spacer(Modifier.height(12.dp))

        Text("Start Color: ${startColor.toInt()}°", color = AppColors.TextSecondary, style = AppTypography.LabelMedium)
        Slider(
            value = startColor,
            onValueChange = { startColor = it },
            onValueChangeFinished = { onUpdate(isOn, startColor.toInt(), endColor.toInt()) },
            valueRange = 0f..359f,
            colors = SliderDefaults.colors(
                thumbColor = hueToColor(startColor.toInt()),
                activeTrackColor = hueToColor(startColor.toInt())
            )
        )

        Text("End Color: ${endColor.toInt()}°", color = AppColors.TextSecondary, style = AppTypography.LabelMedium)
        Slider(
            value = endColor,
            onValueChange = { endColor = it },
            onValueChangeFinished = { onUpdate(isOn, startColor.toInt(), endColor.toInt()) },
            valueRange = 0f..359f,
            colors = SliderDefaults.colors(
                thumbColor = hueToColor(endColor.toInt()),
                activeTrackColor = hueToColor(endColor.toInt())
            )
        )

        // Color preview
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(hueToColor(startColor.toInt()))
            )
            Spacer(Modifier.width(8.dp))
            Text("→", color = AppColors.TextSecondary, style = AppTypography.BodyMedium)
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(hueToColor(endColor.toInt()))
            )
        }
    }
}

// ─── Do Not Disturb Card ────────────────────────────────────────────────────────

@Composable
private fun DoNotDisturbCard(
    config: DoNotDisturbConfig?,
    onUpdate: (Boolean, Int, Int, Int, Int) -> Unit
) {
    var isOn by remember { mutableStateOf(config?.isOn ?: false) }
    var startH by remember { mutableStateOf(config?.startHour ?: 22) }
    var startM by remember { mutableStateOf(config?.startMinute ?: 0) }
    var endH by remember { mutableStateOf(config?.endHour ?: 7) }
    var endM by remember { mutableStateOf(config?.endMinute ?: 0) }

    LaunchedEffect(config) {
        config?.let {
            isOn = it.isOn
            startH = it.startHour
            startM = it.startMinute
            endH = it.endHour
            endM = it.endMinute
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.CardMedium)
            .background(AppColors.SurfaceVariant)
            .padding(16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Do Not Disturb", color = AppColors.TextPrimary, style = AppTypography.BodyMedium)
            Switch(
                checked = isOn,
                onCheckedChange = {
                    isOn = it
                    onUpdate(it, startH, startM, endH, endM)
                },
                colors = SwitchDefaults.colors(checkedTrackColor = BottleBlue)
            )
        }

        if (isOn) {
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                TimeInput(
                    label = "Start",
                    hour = startH,
                    minute = startM,
                    onHourChange = { startH = it; onUpdate(isOn, startH, startM, endH, endM) },
                    onMinuteChange = { startM = it; onUpdate(isOn, startH, startM, endH, endM) }
                )
                Text("→", color = AppColors.TextSecondary, style = AppTypography.ValueMedium,
                    modifier = Modifier.align(Alignment.CenterVertically))
                TimeInput(
                    label = "End",
                    hour = endH,
                    minute = endM,
                    onHourChange = { endH = it; onUpdate(isOn, startH, startM, endH, endM) },
                    onMinuteChange = { endM = it; onUpdate(isOn, startH, startM, endH, endM) }
                )
            }
        }
    }
}

@Composable
private fun TimeInput(
    label: String,
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = AppColors.TextSecondary, style = AppTypography.LabelMedium)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = hour.toString().padStart(2, '0'),
                onValueChange = {
                    it.filter { c -> c.isDigit() }.take(2).toIntOrNull()?.let { h ->
                        if (h in 0..23) onHourChange(h)
                    }
                },
                modifier = Modifier.width(52.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BottleBlue,
                    unfocusedBorderColor = AppColors.BorderPrimary,
                    focusedTextColor = AppColors.TextPrimary,
                    unfocusedTextColor = AppColors.TextPrimary,
                    cursorColor = BottleBlue
                ),
                singleLine = true,
                textStyle = AppTypography.ValueSmall.copy(textAlign = TextAlign.Center),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Text(":", color = AppColors.TextPrimary, style = AppTypography.ValueMedium,
                modifier = Modifier.padding(horizontal = 4.dp))
            OutlinedTextField(
                value = minute.toString().padStart(2, '0'),
                onValueChange = {
                    it.filter { c -> c.isDigit() }.take(2).toIntOrNull()?.let { m ->
                        if (m in 0..59) onMinuteChange(m)
                    }
                },
                modifier = Modifier.width(52.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BottleBlue,
                    unfocusedBorderColor = AppColors.BorderPrimary,
                    focusedTextColor = AppColors.TextPrimary,
                    unfocusedTextColor = AppColors.TextPrimary,
                    cursorColor = BottleBlue
                ),
                singleLine = true,
                textStyle = AppTypography.ValueSmall.copy(textAlign = TextAlign.Center),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    }
}

// ─── Reminder Light Card ────────────────────────────────────────────────────────

@Composable
private fun ReminderLightCard(
    colorIndex: Int,
    onSetColor: (Int) -> Unit
) {
    var sliderValue by remember { mutableStateOf(colorIndex.toFloat()) }

    LaunchedEffect(colorIndex) {
        sliderValue = colorIndex.toFloat()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.CardMedium)
            .background(AppColors.SurfaceVariant)
            .padding(16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Color: ${sliderValue.toInt()}°", color = AppColors.TextSecondary, style = AppTypography.LabelMedium)
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(hueToColor(sliderValue.toInt()))
            )
        }
        Spacer(Modifier.height(8.dp))
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = { onSetColor(sliderValue.toInt()) },
            valueRange = 0f..359f,
            colors = SliderDefaults.colors(
                thumbColor = hueToColor(sliderValue.toInt()),
                activeTrackColor = hueToColor(sliderValue.toInt())
            )
        )
    }
}

// ─── Send Total Intake Card ─────────────────────────────────────────────────────

@Composable
private fun SendTotalIntakeCard(onSend: (Int) -> Unit) {
    var inputText by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.CardMedium)
            .background(AppColors.SurfaceVariant)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it.filter { c -> c.isDigit() } },
            label = { Text("Total intake (mL)", color = AppColors.TextSecondary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BottleBlue,
                unfocusedBorderColor = AppColors.BorderPrimary,
                focusedTextColor = AppColors.TextPrimary,
                unfocusedTextColor = AppColors.TextPrimary,
                cursorColor = BottleBlue
            ),
            singleLine = true
        )

        Spacer(Modifier.width(12.dp))

        Button(
            onClick = {
                inputText.toIntOrNull()?.let { onSend(it) }
            },
            shape = AppShapes.ButtonMedium,
            colors = ButtonDefaults.buttonColors(containerColor = BottleBlue)
        ) {
            Text("Send", color = Color.White, style = AppTypography.ButtonMedium)
        }
    }
}

// ─── Toggle Card ────────────────────────────────────────────────────────────────

@Composable
private fun ToggleCard(
    label: String,
    isOn: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.CardMedium)
            .background(AppColors.SurfaceVariant)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = AppColors.TextPrimary, style = AppTypography.BodyMedium, modifier = Modifier.weight(1f))
        Switch(
            checked = isOn,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(checkedTrackColor = BottleBlue)
        )
    }
}

// ─── Chip Selector ──────────────────────────────────────────────────────────────

@Composable
private fun ChipSelector(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(AppShapes.Chip)
                    .background(
                        if (isSelected) BottleBlue.copy(alpha = 0.2f)
                        else AppColors.SurfaceVariant
                    )
                    .clickable { onSelect(index) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) BottleBlue else AppColors.TextSecondary,
                    style = AppTypography.LabelLarge.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                )
            }
        }
    }
}

// ─── Utility: Hue to Color ──────────────────────────────────────────────────────

internal fun hueToColor(hue: Int): Color {
    val h = hue.coerceIn(0, 359)
    val s = 1.0f
    val v = 1.0f

    val c = v * s
    val x = c * (1 - kotlin.math.abs((h / 60f) % 2 - 1))
    val m = v - c

    val (r1, g1, b1) = when (h) {
        in 0..59 -> Triple(c, x, 0f)
        in 60..119 -> Triple(x, c, 0f)
        in 120..179 -> Triple(0f, c, x)
        in 180..239 -> Triple(0f, x, c)
        in 240..299 -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }

    return Color(r1 + m, g1 + m, b1 + m)
}
