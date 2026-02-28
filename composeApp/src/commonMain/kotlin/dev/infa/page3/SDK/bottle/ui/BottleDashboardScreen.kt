package dev.infa.page3.SDK.bottle.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.SDK.bottle.data.*
import dev.infa.page3.SDK.bottle.viewmodel.BottleViewModel
import dev.infa.page3.SDK.ui.theme.*

// ─── Bottle-specific accent colors ──────────────────────────────────────────────

private val BottleBlue = Color(0xFF4FC3F7)
private val BottleBlueDark = Color(0xFF0288D1)
private val BottleCyan = Color(0xFF00E5FF)
private val BottleTeal = Color(0xFF26A69A)
private val BottleWarm = Color(0xFFFF8A65)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottleDashboardScreen(
    viewModel: BottleViewModel,
    navigator: Navigator
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val batteryStatus by viewModel.batteryStatus.collectAsState()
    val firmwareVersion by viewModel.firmwareVersion.collectAsState()
    val waterLevel by viewModel.waterLevelMl.collectAsState()
    val waterTemp by viewModel.waterTemperature.collectAsState()
    val waterTarget by viewModel.waterIntakeTarget.collectAsState()
    val currentDrink by viewModel.currentDrink.collectAsState()
    val devices by viewModel.devices.collectAsState()
    val isConnected = connectionState == "CONNECTED"

    // Fetch all settings when connected
    LaunchedEffect(isConnected) {
        if (isConnected) {
            viewModel.fetchAllSettings()
        }
    }

    Scaffold(
        containerColor = AppColors.BackgroundPrimary,
        topBar = {
            BottleTopBar(
                isConnected = isConnected,
                batteryText = batteryStatus ?: "",
                deviceName = "SGUAI-T30"
            )
        }
    ) { padding ->
        if (!isConnected) {
            BottleDisconnectedState(
                viewModel = viewModel,
                devices = devices,
                connectionState = connectionState,
                onScan = { viewModel.startScan() },
                onConnect = { viewModel.connect(it) }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = AppDimensions.ScreenPadding.Horizontal),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hero Status Card
                item {
                    Spacer(Modifier.height(8.dp))
                    HeroStatusCard(
                        waterLevel = waterLevel ?: 0,
                        temperature = waterTemp ?: 0,
                        targetMl = waterTarget ?: 2000,
                        batteryText = batteryStatus ?: "—",
                        firmwareVersion = firmwareVersion ?: "—"
                    )
                }

                // Live Sensor Row
                item {
                    LiveSensorRow(
                        waterLevel = waterLevel ?: 0,
                        temperature = waterTemp ?: 0
                    )
                }

                // Recent Drink Alert
                currentDrink?.let { drink ->
                    item {
                        RecentDrinkCard(drink)
                    }
                }

                // Quick Action Grid
                item {
                    Text(
                        "Quick Actions",
                        color = AppColors.TextPrimary,
                        style = AppTypography.HeadingExtraSmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    QuickActionGrid(
                        onSyncTime = { viewModel.syncTime() },
                        onLight = { viewModel.activateLight() },
                        onCalibrate = { viewModel.calibrateSensor() },
                        onHistory = {
                            navigator.push(
                                dev.infa.page3.SDK.bottle.navigation.BottleDrinkingHistoryScreenNav()
                            )
                        },
                        onSettings = {
                            navigator.push(
                                dev.infa.page3.SDK.bottle.navigation.BottleSettingsScreenNav()
                            )
                        },
                        onDisconnect = { viewModel.disconnect() }
                    )
                }

                // Spacer at bottom
                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }
}

// ─── Top App Bar ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottleTopBar(
    isConnected: Boolean,
    batteryText: String,
    deviceName: String
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🍶",
                    fontSize = 22.sp
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = deviceName,
                        color = AppColors.TextPrimary,
                        style = AppTypography.BodyLarge
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isConnected) AppColors.BluetoothConnected
                                    else AppColors.BluetoothDisconnected
                                )
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isConnected) "Connected" else "Disconnected",
                            color = if (isConnected) AppColors.BluetoothConnected
                            else AppColors.TextSecondary,
                            style = AppTypography.LabelSmall
                        )
                        if (isConnected && batteryText.isNotEmpty()) {
                            Text(
                                text = "  •  🔋 $batteryText",
                                color = AppColors.TextSecondary,
                                style = AppTypography.LabelSmall
                            )
                        }
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppColors.BackgroundPrimary
        )
    )
}

// ─── Hero Status Card ───────────────────────────────────────────────────────────

@Composable
private fun HeroStatusCard(
    waterLevel: Int,
    temperature: Int,
    targetMl: Int,
    batteryText: String,
    firmwareVersion: String
) {
    val progress = if (targetMl > 0) (waterLevel.toFloat() / targetMl).coerceIn(0f, 1f) else 0f

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(progress) {
        animProgress.animateTo(progress, tween(1200, easing = EaseOutCubic))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.CardExtraLarge)
            .background(
                Brush.linearGradient(
                    listOf(
                        BottleBlueDark.copy(alpha = 0.3f),
                        BottleTeal.copy(alpha = 0.15f),
                        AppColors.BackgroundCard
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Water intake ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(180.dp)
            ) {
                Canvas(modifier = Modifier.size(180.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    // Background ring
                    drawCircle(
                        color = Color.White.copy(alpha = 0.06f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Progress arc
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(BottleCyan, BottleBlue, BottleBlueDark)
                        ),
                        startAngle = -90f,
                        sweepAngle = 360f * animProgress.value,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "💧",
                        fontSize = 28.sp
                    )
                    Text(
                        text = "${waterLevel}",
                        color = AppColors.TextPrimary,
                        style = AppTypography.DisplaySmall
                    )
                    Text(
                        text = "/ ${targetMl} mL",
                        color = AppColors.TextSecondary,
                        style = AppTypography.LabelMedium
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Temperature badge
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(AppShapes.Chip)
                    .background(BottleWarm.copy(alpha = 0.15f))
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(text = "🌡️", fontSize = 16.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "${temperature}°C",
                    color = BottleWarm,
                    style = AppTypography.ValueMedium
                )
            }

            Spacer(Modifier.height(16.dp))

            // Info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InfoChip("🔋", batteryText)
                InfoChip("📱", firmwareVersion)
                InfoChip("🎯", "${targetMl}mL")
            }
        }
    }
}

@Composable
private fun InfoChip(emoji: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(AppShapes.Chip)
            .background(AppColors.OverlayMedium)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(emoji, fontSize = 12.sp)
        Spacer(Modifier.width(4.dp))
        Text(
            text = text,
            color = AppColors.TextSecondary,
            style = AppTypography.LabelSmall
        )
    }
}

// ─── Live Sensor Row ────────────────────────────────────────────────────────────

@Composable
private fun LiveSensorRow(waterLevel: Int, temperature: Int) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SensorCard(
            modifier = Modifier.weight(1f),
            emoji = "📊",
            label = "Water Level",
            value = "$waterLevel",
            unit = "mL",
            accentColor = BottleBlue
        )
        SensorCard(
            modifier = Modifier.weight(1f),
            emoji = "🌡️",
            label = "Temperature",
            value = "$temperature",
            unit = "°C",
            accentColor = BottleWarm
        )
    }
}

@Composable
private fun SensorCard(
    modifier: Modifier,
    emoji: String,
    label: String,
    value: String,
    unit: String,
    accentColor: Color
) {
    Box(
        modifier = modifier
            .clip(AppShapes.CardMedium)
            .background(
                Brush.linearGradient(
                    listOf(
                        accentColor.copy(alpha = 0.1f),
                        AppColors.BackgroundCard
                    )
                )
            )
            .padding(16.dp)
    ) {
        Column {
            Text(emoji, fontSize = 20.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = label,
                color = AppColors.TextSecondary,
                style = AppTypography.LabelMedium
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    color = AppColors.TextPrimary,
                    style = AppTypography.ValueLarge
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = unit,
                    color = accentColor,
                    style = AppTypography.LabelMedium,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

// ─── Recent Drink Card ──────────────────────────────────────────────────────────

@Composable
private fun RecentDrinkCard(drink: DrinkingRecord) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.CardMedium)
            .background(
                Brush.linearGradient(
                    listOf(
                        AppColors.Success.copy(alpha = 0.15f),
                        AppColors.BackgroundCard
                    )
                )
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Recent Drink",
                    color = AppColors.Success,
                    style = AppTypography.LabelLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "💧 ${drink.waterIntakeMl}mL  •  🌡️ ${drink.temperatureC}°C",
                    color = AppColors.TextPrimary,
                    style = AppTypography.BodyMedium
                )
            }
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(AppColors.Success.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text("💧", fontSize = 20.sp)
            }
        }
    }
}

// ─── Quick Action Grid ──────────────────────────────────────────────────────────

private data class QuickAction(
    val emoji: String,
    val label: String,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
private fun QuickActionGrid(
    onSyncTime: () -> Unit,
    onLight: () -> Unit,
    onCalibrate: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onDisconnect: () -> Unit
) {
    val actions = listOf(
        QuickAction("🕐", "Sync Time", BottleBlue, onSyncTime),
        QuickAction("💡", "Light", Color(0xFFFFD54F), onLight),
        QuickAction("🔧", "Calibrate", BottleTeal, onCalibrate),
        QuickAction("💧", "History", Color(0xFF4FC3F7), onHistory),
        QuickAction("⚙️", "Settings", Color(0xFF90A4AE), onSettings),
        QuickAction("🔌", "Disconnect", AppColors.Error, onDisconnect)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        userScrollEnabled = false
    ) {
        items(actions) { action ->
            QuickActionItem(action)
        }
    }
}

@Composable
private fun QuickActionItem(action: QuickAction) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(AppShapes.CardMedium)
            .background(action.color.copy(alpha = 0.08f))
            .clickable { action.onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(action.emoji, fontSize = 28.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = action.label,
                color = AppColors.TextPrimary,
                style = AppTypography.LabelMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Disconnected State ─────────────────────────────────────────────────────────

@Composable
private fun BottleDisconnectedState(
    viewModel: BottleViewModel,
    devices: List<BottleDeviceInfo>,
    connectionState: String,
    onScan: () -> Unit,
    onConnect: (BottleDeviceInfo) -> Unit
) {
    val isScanning = connectionState == "SCANNING"
    val isConnecting = connectionState == "CONNECTING"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppDimensions.ScreenPadding.Horizontal),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(80.dp))

        // Bottle emoji with pulse
        Text("🍶", fontSize = 72.sp)

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Smart Bottle",
            color = AppColors.TextPrimary,
            style = AppTypography.HeadingLarge
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Scan and connect to your SGUAI-T30",
            color = AppColors.TextSecondary,
            style = AppTypography.BodySmall
        )

        Spacer(Modifier.height(32.dp))

        // Scan / Connecting button
        Button(
            onClick = onScan,
            enabled = !isScanning && !isConnecting,
            shape = AppShapes.Chip,
            colors = ButtonDefaults.buttonColors(
                containerColor = BottleBlue,
                contentColor = Color.White,
                disabledContainerColor = BottleBlue.copy(alpha = 0.4f)
            ),
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(AppDimensions.ButtonHeight.Medium)
        ) {
            if (isScanning || isConnecting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isConnecting) "Connecting…" else "Scanning…",
                    style = AppTypography.ButtonMedium
                )
            } else {
                Text("Start Scan", style = AppTypography.ButtonMedium)
            }
        }

        Spacer(Modifier.height(24.dp))

        // Device list
        if (devices.isNotEmpty()) {
            Text(
                text = "Available Devices",
                color = BottleBlue,
                style = AppTypography.BodyMedium,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(Modifier.height(12.dp))

            devices.forEach { device ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(AppShapes.CardSmall)
                        .background(AppColors.SurfaceVariant)
                        .clickable { onConnect(device) }
                        .padding(AppDimensions.CardPadding.Medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🍶", fontSize = 20.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = device.name,
                            color = AppColors.TextPrimary,
                            style = AppTypography.BodyMedium
                        )
                        Text(
                            text = device.address,
                            color = AppColors.TextSecondary,
                            style = AppTypography.LabelMedium
                        )
                    }
                    Text(
                        text = "Connect",
                        color = BottleBlue,
                        style = AppTypography.LabelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
