package dev.infa.page3.SDK.`V-Band`.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.SDK.`V-Band`.data.*
import dev.infa.page3.SDK.`V-Band`.viewmodel.VBandViewModel
import dev.infa.page3.SDK.ui.utils.FormatUtils

// ─── Colors ──────────────────────────────────────────────────────────────────────

private val VBandAccent      = Color(0xFF26A69A)
private val VBandAccentLight = Color(0xFF80CBC4)
private val VBandAccentDark  = Color(0xFF00695C)
private val BgDeep           = Color(0xFF0A0E1A)
private val BgCard           = Color(0xFF111827)
private val BgCard2          = Color(0xFF1A2235)
private val HeartRed         = Color(0xFFEF5350)
private val SleepPurple      = Color(0xFF7E57C2)
private val StepsOrange      = Color(0xFFFF9800)
private val TempYellow       = Color(0xFFFDD835)

// ─── Home Screen ─────────────────────────────────────────────────────────────────

@Composable
fun VBandHomeScreen(
    viewModel: VBandViewModel,
    navigator: Navigator
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val batteryData by viewModel.batteryData.collectAsState()
    val sportData by viewModel.sportData.collectAsState()
    val heartData by viewModel.heartData.collectAsState()
    val sleepDataList by viewModel.sleepDataList.collectAsState()
    val devices by viewModel.devices.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val pwdData by viewModel.pwdData.collectAsState()

    val isConnected = connectionState == "CONNECTED"
    val isScanning = connectionState == "SCANNING"
    val isConnecting = connectionState == "CONNECTING"

    // Auto-confirm password and fetch data when connected
    LaunchedEffect(connectionState) {
        if (isConnected && pwdData == null) {
            viewModel.confirmPassword()
        }
    }

    // Auto-fetch settings after password confirmed
    LaunchedEffect(pwdData) {
        if (pwdData?.status == VBandPwdStatus.CHECK_SUCCESS) {
            viewModel.fetchAllSettings()
        }
    }

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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Header ─────────────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navigator.pop() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "V-Band",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
                // Connection status indicator
                ConnectionIndicator(connectionState)
            }
        }

        // ── Connection / Scan section ──────────────────────────────────────────
        if (!isConnected) {
            item {
                ScanConnectCard(
                    viewModel = viewModel,
                    devices = devices,
                    connectionState = connectionState,
                    isScanning = isScanning,
                    isConnecting = isConnecting
                )
            }
        }

        // ── Battery Card ───────────────────────────────────────────────────────
        if (isConnected) {
            item {
                BatteryCard(batteryData)
            }
        }

        // ── Syncing Progress ───────────────────────────────────────────────────
        if (isSyncing) {
            item {
                SyncingCard()
            }
        }

        // ── Steps / Distance / Calories ────────────────────────────────────────
        if (isConnected && sportData != null) {
            item {
                StepsOverviewCard(sportData!!)
            }
        }

        // ── Heart Rate ─────────────────────────────────────────────────────────
        if (isConnected) {
            item {
                HeartRateCard(
                    heartData = heartData,
                    onStartDetect = { viewModel.startDetectHeart() },
                    onStopDetect = { viewModel.stopDetectHeart() }
                )
            }
        }

        // ── Sleep Summary ──────────────────────────────────────────────────────
        if (isConnected && sleepDataList.isNotEmpty()) {
            item {
                SleepSummaryCard(sleepDataList.lastOrNull())
            }
        }

        // ── Quick Actions ──────────────────────────────────────────────────────
        if (isConnected) {
            item {
                QuickActionsRow(
                    onReadSteps = { viewModel.readSportStep() },
                    onReadBattery = { viewModel.readBattery() },
                    onSyncAll = { viewModel.readAllHealthData() }
                )
            }
        }
    }
}

// ─── Connection Status Indicator ─────────────────────────────────────────────────

@Composable
private fun ConnectionIndicator(connectionState: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val (color, label) = when (connectionState) {
        "CONNECTED" -> VBandAccent to "Connected"
        "CONNECTING" -> Color(0xFFFFA726) to "Connecting"
        "SCANNING" -> Color(0xFF42A5F5) to "Scanning"
        else -> Color(0xFF757575) to "Disconnected"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .scale(if (connectionState == "CONNECTED") 1f else pulseScale)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ─── Scan & Connect Card ─────────────────────────────────────────────────────────

@Composable
private fun ScanConnectCard(
    viewModel: VBandViewModel,
    devices: List<VBandDeviceInfo>,
    connectionState: String,
    isScanning: Boolean,
    isConnecting: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scan_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    GlassCard(
        gradStart = VBandAccent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Scanning animation
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                VBandAccent.copy(alpha = if (isScanning) glowAlpha else 0.1f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("⌚", fontSize = 40.sp)
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = if (isConnecting) "Connecting..."
                       else if (isScanning) "Scanning for V-Band..."
                       else "Connect Your V-Band",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = if (isScanning) "${devices.size} device(s) found"
                       else "Tap scan to find nearby devices",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            // Scan / Stop button
            Button(
                onClick = {
                    if (isScanning) viewModel.stopScan()
                    else viewModel.startScan()
                },
                enabled = !isConnecting,
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(listOf(VBandAccent, VBandAccentDark)),
                            shape = RoundedCornerShape(22.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isScanning) "Stop Scan" else "Scan for Devices",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Device list
            if (devices.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                devices.forEach { device ->
                    DeviceListItem(
                        device = device,
                        onClick = { viewModel.connect(device) }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

// ─── Device List Item ────────────────────────────────────────────────────────────

@Composable
private fun DeviceListItem(
    device: VBandDeviceInfo,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = device.name,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = device.address,
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 11.sp
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // RSSI bars
            val signal = when {
                device.rssi > -50 -> 4
                device.rssi > -60 -> 3
                device.rssi > -70 -> 2
                else -> 1
            }
            repeat(4) { i ->
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height((8 + i * 4).dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (i < signal) VBandAccent
                            else Color.White.copy(alpha = 0.15f)
                        )
                )
            }
            Text(
                text = "Connect →",
                color = VBandAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ─── Battery Card ────────────────────────────────────────────────────────────────

@Composable
private fun BatteryCard(batteryData: VBandBatteryData?) {
    val percent = if (batteryData?.isPercent == true) batteryData.batteryPercent
                  else (batteryData?.batteryLevel ?: 0) * 25

    val isCharging = batteryData?.powerModel == 1

    GlassCard(
        gradStart = if (isCharging) Color(0xFF66BB6A) else VBandAccent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Battery",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$percent",
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "%",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                if (isCharging) {
                    Text(
                        text = "⚡ Charging",
                        color = Color(0xFF66BB6A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Battery visual
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(percent / 100f)
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    if (percent > 20) VBandAccent else HeartRed,
                                    if (percent > 20) VBandAccentLight else Color(0xFFFF7043)
                                )
                            )
                        )
                )
            }
        }
    }
}

// ─── Steps Overview Card ─────────────────────────────────────────────────────────

@Composable
private fun StepsOverviewCard(sportData: VBandSportData) {
    GlassCard(
        gradStart = StepsOrange,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "TODAY'S ACTIVITY",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    emoji = "👟",
                    value = "${sportData.step}",
                    label = "Steps",
                    color = StepsOrange
                )
                StatItem(
                    emoji = "📏",
                    value = FormatUtils.formatDecimal(sportData.distance, 1),
                    label = "km",
                    color = Color(0xFF42A5F5)
                )
                StatItem(
                    emoji = "🔥",
                    value = FormatUtils.formatDecimal(sportData.calories, 0),
                    label = "kcal",
                    color = HeartRed
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    emoji: String,
    value: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 28.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            text = value,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = label,
            color = color.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ─── Heart Rate Card ─────────────────────────────────────────────────────────────

@Composable
private fun HeartRateCard(
    heartData: VBandHeartData?,
    onStartDetect: () -> Unit,
    onStopDetect: () -> Unit
) {
    val isDetecting = heartData?.heartStatus == VBandHeartStatus.STATE_HEART_DETECT
    val heartRate = heartData?.heartRate ?: 0

    val infiniteTransition = rememberInfiniteTransition(label = "heart")
    val heartScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (heartRate > 0) (60000 / heartRate.coerceAtLeast(40)) else 1000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heartbeat"
    )

    GlassCard(
        gradStart = HeartRed,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HEART RATE",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                // Measure button
                TextButton(
                    onClick = { if (isDetecting) onStopDetect() else onStartDetect() }
                ) {
                    Text(
                        text = if (isDetecting) "Stop" else "Measure",
                        color = HeartRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "❤️",
                    fontSize = 32.sp,
                    modifier = Modifier.scale(if (isDetecting || heartRate > 0) heartScale else 1f)
                )
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = if (heartRate > 0) "$heartRate" else "--",
                            color = Color.White,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = " bpm",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    if (isDetecting) {
                        Text(
                            text = "Measuring...",
                            color = HeartRed.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

// ─── Sleep Summary Card ──────────────────────────────────────────────────────────

@Composable
private fun SleepSummaryCard(sleepData: VBandSleepData?) {
    if (sleepData == null) return

    val totalMinutes = sleepData.totalSleepTime
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    GlassCard(
        gradStart = SleepPurple,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "LAST SLEEP",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${hours}h ${minutes}m",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = sleepData.date,
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 12.sp
                    )
                }

                // Sleep breakdown
                Column(horizontalAlignment = Alignment.End) {
                    SleepMiniStat("Deep", sleepData.deepSleepTime, SleepPurple)
                    SleepMiniStat("Light", sleepData.lightSleepTime, Color(0xFFB39DDB))
                    SleepMiniStat("Awake", sleepData.wakeCount, HeartRed.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
private fun SleepMiniStat(label: String, value: Int, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = "$label: ${value}m",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 11.sp
        )
    }
}

// ─── Syncing Card ────────────────────────────────────────────────────────────────

@Composable
private fun SyncingCard() {
    GlassCard(
        gradStart = VBandAccent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = VBandAccent,
                strokeWidth = 2.dp,
                strokeCap = StrokeCap.Round
            )
            Text(
                text = "Syncing data from your V-Band...",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }
    }
}

// ─── Quick Actions Row ───────────────────────────────────────────────────────────

@Composable
private fun QuickActionsRow(
    onReadSteps: () -> Unit,
    onReadBattery: () -> Unit,
    onSyncAll: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionButton(
            emoji = "👟",
            label = "Steps",
            color = StepsOrange,
            onClick = onReadSteps,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            emoji = "🔋",
            label = "Battery",
            color = VBandAccent,
            onClick = onReadBattery,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            emoji = "🔄",
            label = "Sync All",
            color = Color(0xFF42A5F5),
            onClick = onSyncAll,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionButton(
    emoji: String,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        color.copy(alpha = 0.15f),
                        BgCard2
                    )
                )
            )
            .border(1.dp, color.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 22.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ─── Glass Card (shared component) ───────────────────────────────────────────────

@Composable
internal fun GlassCard(
    gradStart: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        gradStart.copy(alpha = 0.12f),
                        BgCard2,
                        BgCard
                    )
                )
            )
            .border(1.dp, gradStart.copy(alpha = 0.10f), RoundedCornerShape(24.dp))
    ) {
        content()
    }
}
