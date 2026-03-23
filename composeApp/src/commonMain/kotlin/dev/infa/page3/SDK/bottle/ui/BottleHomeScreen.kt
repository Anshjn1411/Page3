package dev.infa.page3.SDK.bottle.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.SDK.bottle.data.*
import dev.infa.page3.SDK.bottle.viewmodel.BottleViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ─── Palette ──────────────────────────────────────────────────────────────────────

private val Blue1   = Color(0xFF4FC3F7)
private val Blue2   = Color(0xFF0288D1)
private val Cyan1   = Color(0xFF00E5FF)
private val Teal1   = Color(0xFF26C6DA)
private val Warm1   = Color(0xFFFF8A65)
private val BgDeep  = Color(0xFF0A0E1A)
private val BgCard2 = Color(0xFF1A2235)

// ─── Bottom nav height constant — screens pad their content by this amount ───────
// 72dp nav + 12dp bottom gap + ~34dp navigation bar inset ≈ 120dp safe bottom padding
private val NavBottomPadding = 130.dp

// ─── Home Screen ─────────────────────────────────────────────────────────────────

@Composable
fun BottleHomeScreen(
    viewModel: BottleViewModel,
    navigator: Navigator
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val batteryStatus   by viewModel.batteryStatus.collectAsState()
    val firmwareVersion by viewModel.firmwareVersion.collectAsState()
    val todayIntake     by viewModel.todayTotalIntake.collectAsState()
    val waterTemp       by viewModel.waterTemperature.collectAsState()
    val waterTarget     by viewModel.waterIntakeTarget.collectAsState()
    val currentDrink    by viewModel.currentDrink.collectAsState()
    val devices         by viewModel.devices.collectAsState()
    val waterLevel      by viewModel.waterLevelMl.collectAsState()
    val isSyncing       by viewModel.isSyncing.collectAsState()
    val isConnected = connectionState == "CONNECTED"

    LaunchedEffect(isConnected) { if (isConnected) viewModel.fetchAllSettings() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
    ) {
        // Ambient glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Blue2.copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(size.width * 0.5f, size.height * 0.22f),
                    radius = size.width * 0.72f
                ),
                radius = size.width * 0.72f,
                center = Offset(size.width * 0.5f, size.height * 0.22f)
            )
        }

        if (!isConnected) {
            HomeDisconnectedState(
                viewModel       = viewModel,
                devices         = devices,
                connectionState = connectionState,
                onScan    = { viewModel.startScan() },
                onConnect = { viewModel.connect(it) }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start  = 20.dp,
                    end    = 20.dp,
                    // top padding = status bar height so content starts below camera notch
                    top    = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp,
                    bottom = NavBottomPadding   // content clears the floating nav bar
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { HomeTopHeader(batteryStatus = batteryStatus ?: "—") }
                item {
                    WaterRingHero(
                        todayIntake = todayIntake,
                        targetMl    = waterTarget ?: 2000,
                        temperature = waterTemp ?: 0,
                        isSyncing   = isSyncing
                    )
                }
                item {
                    LiveSensorStrip(
                        waterLevel  = waterLevel ?: 0,
                        temperature = waterTemp ?: 0,
                        firmware    = firmwareVersion ?: "—"
                    )
                }
                currentDrink?.let { drink ->
                    item { RecentDrinkBanner(drink) }
                }
                item {
                    Text(
                        "Quick Actions",
                        color      = Color.White.copy(alpha = 0.9f),
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier   = Modifier.padding(top = 4.dp)
                    )
                }
                item {
                    HomeQuickActionGrid(
                        onSyncTime   = { viewModel.syncTime() },
                        onLight      = { viewModel.activateLight() },
                        onCalibrate  = { viewModel.calibrateSensor() },
                        onDisconnect = { viewModel.disconnect() }
                    )
                }
            }
        }
    }
}

// ─── Top Header ─────────────────────────────────────────────────────────────────

@Composable
private fun HomeTopHeader(batteryStatus: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "page3-T30",
                color      = Color.White,
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF4CAF50)))
                Spacer(Modifier.width(5.dp))
                Text("Connected", color = Color(0xFF4CAF50), fontSize = 12.sp)
                Text("  •  🔋 $batteryStatus", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
            }
        }
        Text("🍶", fontSize = 34.sp)
    }
}

// ─── Water Ring Hero ─────────────────────────────────────────────────────────────

@Composable
private fun WaterRingHero(
    todayIntake: Int,
    targetMl: Int,
    temperature: Int,
    isSyncing: Boolean
) {
    val progress = if (targetMl > 0) (todayIntake.toFloat() / targetMl).coerceIn(0f, 1f) else 0f
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(progress) { animProgress.animateTo(progress, tween(1400, easing = FastOutSlowInEasing)) }

    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.6f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(BgCard2, Color(0xFF111827))))
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(210.dp)) {
                Canvas(modifier = Modifier.size(210.dp)) {
                    val strokeWidth = 16.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    // Track
                    drawCircle(
                        color  = Color.White.copy(alpha = 0.05f),
                        radius = radius,
                        center = center,
                        style  = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    // Outer glow halo
                    drawCircle(
                        color  = Blue1.copy(alpha = 0.10f),
                        radius = radius + strokeWidth * 0.3f,
                        center = center,
                        style  = Stroke(width = strokeWidth * 1.8f, cap = StrokeCap.Round)
                    )

                    if (!isSyncing && animProgress.value > 0f) {
                        // Main progress arc
                        drawArc(
                            brush = Brush.sweepGradient(
                                0f   to Cyan1.copy(alpha = 0.6f),
                                0.4f to Blue1,
                                0.8f to Blue2,
                                1f   to Cyan1.copy(alpha = 0.6f)
                            ),
                            startAngle = -90f,
                            sweepAngle = 360f * animProgress.value,
                            useCenter  = false,
                            topLeft    = Offset(center.x - radius, center.y - radius),
                            size       = Size(radius * 2, radius * 2),
                            style      = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        // Glowing dot at arc tip
                        val endAngle = (-90f + 360f * animProgress.value) * (PI / 180.0)
                        val dotX = center.x + radius * cos(endAngle).toFloat()
                        val dotY = center.y + radius * sin(endAngle).toFloat()
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(Color.White, Blue1, Cyan1.copy(0f)),
                                center = Offset(dotX, dotY),
                                radius = strokeWidth * 1.5f
                            ),
                            radius = strokeWidth * 1.2f,
                            center = Offset(dotX, dotY)
                        )
                    }
                }

                if (isSyncing) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(Modifier.size(36.dp), color = Blue1, strokeWidth = 3.dp)
                        Spacer(Modifier.height(10.dp))
                        Text("Syncing…", color = Blue1, fontSize = 13.sp)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💧", fontSize = 30.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("$todayIntake", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Black, letterSpacing = (-1).sp)
                        Text("/ ${targetMl} mL", color = Color.White.copy(alpha = 0.45f), fontSize = 13.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "${(progress * 100).toInt()}% of daily goal",
                            color    = Blue1.copy(alpha = pulseAlpha),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Warm1.copy(alpha = 0.12f))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text("🌡️", fontSize = 15.sp)
                Spacer(Modifier.width(6.dp))
                Text("${temperature}°C", color = Warm1, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(12.dp))
                Text("Water Temp", color = Warm1.copy(alpha = 0.6f), fontSize = 12.sp)
            }
        }
    }
}

// ─── Live Sensor Strip ────────────────────────────────────────────────────────────

@Composable
private fun LiveSensorStrip(waterLevel: Int, temperature: Int, firmware: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        SensorPill(Modifier.weight(1f), "📊", "Level",  "$waterLevel mL",   Blue1)
        SensorPill(Modifier.weight(1f), "🌡️", "Temp",   "${temperature}°C", Warm1)
        SensorPill(Modifier.weight(1f), "📱", "FW",     firmware,            Teal1)
    }
}

@Composable
private fun SensorPill(modifier: Modifier, emoji: String, label: String, value: String, accent: Color) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(accent.copy(alpha = 0.07f))
            .padding(vertical = 14.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 18.sp)
        Spacer(Modifier.height(4.dp))
        Text(label, color = Color.White.copy(alpha = 0.45f), fontSize = 10.sp)
        Spacer(Modifier.height(2.dp))
        Text(value, color = accent, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

// ─── Recent Drink Banner ─────────────────────────────────────────────────────────

@Composable
private fun RecentDrinkBanner(drink: DrinkingRecord) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF1B3A2A), Color(0xFF1A2235))))
            .padding(16.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Recent Drink", color = Color(0xFF66BB6A), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text("💧 ${drink.waterIntakeMl}mL  •  🌡️ ${drink.temperatureC}°C",
                color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        Box(
            Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF66BB6A).copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) { Text("💧", fontSize = 18.sp) }
    }
}

// ─── Quick Action Grid ────────────────────────────────────────────────────────────

private data class Action(val emoji: String, val label: String, val color: Color, val onClick: () -> Unit)

@Composable
private fun HomeQuickActionGrid(
    onSyncTime: () -> Unit,
    onLight: () -> Unit,
    onCalibrate: () -> Unit,
    onDisconnect: () -> Unit
) {
    val actions = listOf(
        Action("🕐", "Sync Time",   Blue1,            onSyncTime),
        Action("💡", "Light",       Color(0xFFFFD54F), onLight),
        Action("🔧", "Calibrate",   Teal1,            onCalibrate),
        Action("🔌", "Disconnect",  Color(0xFFEF5350), onDisconnect)
    )
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement   = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth().height(100.dp),
        userScrollEnabled = false
    ) {
        items(actions) { action ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(action.color.copy(alpha = 0.08f))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = action.onClick),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(action.emoji, fontSize = 22.sp)
                Spacer(Modifier.height(4.dp))
                Text(action.label, color = action.color.copy(alpha = 0.85f), fontSize = 9.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            }
        }
    }
}

// ─── Disconnected State ──────────────────────────────────────────────────────────

@Composable
private fun HomeDisconnectedState(
    viewModel: BottleViewModel,
    devices: List<BottleDeviceInfo>,
    connectionState: String,
    onScan: () -> Unit,
    onConnect: (BottleDeviceInfo) -> Unit
) {
    val isScanning   = connectionState == "SCANNING"
    val isConnecting = connectionState == "CONNECTING"

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue  = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .windowInsetsPadding(WindowInsets.statusBars),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🍶", fontSize = 82.sp, modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale))
        Spacer(Modifier.height(20.dp))
        Text("Smart Water Bottle", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Connect your page3-T30 to start tracking", color = Color.White.copy(alpha = 0.45f), fontSize = 13.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(36.dp))
        Button(
            onClick  = onScan,
            enabled  = !isScanning && !isConnecting,
            shape    = RoundedCornerShape(30.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = Blue1),
            modifier = Modifier.height(52.dp).fillMaxWidth(0.6f)
        ) {
            if (isScanning || isConnecting) {
                CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text(if (isConnecting) "Connecting…" else "Scanning…", color = Color.White, fontWeight = FontWeight.Bold)
            } else {
                Text("Start Scan", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        if (devices.isNotEmpty()) {
            Spacer(Modifier.height(28.dp))
            Text("Nearby Devices", color = Blue1, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            devices.forEach { device ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(BgCard2)
                        .clickable { onConnect(device) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🍶", fontSize = 18.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(device.name,    color = Color.White,                  fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(device.address, color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
                    }
                    Text("Connect →", color = Blue1, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}