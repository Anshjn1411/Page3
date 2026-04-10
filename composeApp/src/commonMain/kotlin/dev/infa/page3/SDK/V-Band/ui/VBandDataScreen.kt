package dev.infa.page3.SDK.`V-Band`.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
private val TempYellow       = Color(0xFFFDD835)
private val StepsOrange      = Color(0xFFFF9800)
private val SyncBlue         = Color(0xFF42A5F5)
private val LogGreen         = Color(0xFF66BB6A)
private val LogRed           = Color(0xFFEF5350)
private val LogYellow        = Color(0xFFFFA726)

// ─── Data Screen ─────────────────────────────────────────────────────────────────

@Composable
fun VBandDataScreen(
    viewModel: VBandViewModel,
    navigator: Navigator
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val sleepDataList by viewModel.sleepDataList.collectAsState()
    val originDataList by viewModel.originDataList.collectAsState()
    val originHalfHourDataList by viewModel.originHalfHourDataList.collectAsState()
    val temperatureRecords by viewModel.temperatureRecords.collectAsState()
    val readProgress by viewModel.readProgress.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val syncLogs by viewModel.syncLogs.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val selectedDay by viewModel.selectedSyncDay.collectAsState()
    val functionSupport by viewModel.functionSupport.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val isConnected = connectionState == "CONNECTED"
    val watchDay = functionSupport?.watchDay ?: 3

    var selectedDataType by remember { mutableStateOf(DataType.SLEEP) }
    var showSyncLogs by remember { mutableStateOf(false) }

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
                text = "Health Data",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        if (!isConnected) {
            item {
                GlassCard(gradStart = Color(0xFF757575), modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📊", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Connect V-Band to sync data",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            return@LazyColumn
        }

        // ── Day Selector ───────────────────────────────────────────────────────
        item {
            DaySelector(
                watchDay = watchDay,
                selectedDay = selectedDay,
                onDaySelected = { viewModel.selectSyncDay(it) }
            )
        }

        // ── Sync Status Card ───────────────────────────────────────────────────
        if (syncState.status == VBandSyncStatus.SYNCING ||
            syncState.status == VBandSyncStatus.COMPLETED ||
            syncState.status == VBandSyncStatus.ERROR
        ) {
            item {
                SyncStatusCard(
                    syncState = syncState,
                    onCancel = { viewModel.cancelSync() }
                )
            }
        }

        // ── Error message ──────────────────────────────────────────────────────
        if (errorMessage != null) {
            item {
                GlassCard(gradStart = LogRed, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠️", fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "DISMISS",
                            color = LogRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { viewModel.clearError() }
                        )
                    }
                }
            }
        }

        // ── Sync Buttons ───────────────────────────────────────────────────────
        item {
            SyncActionsRow(
                selectedDay = selectedDay,
                isSyncing = isSyncing,
                onSyncAll = { viewModel.syncAll() },
                onSyncDay = { viewModel.syncDay(selectedDay) }
            )
        }

        // ── Sync Logs Toggle ───────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.03f))
                    .clickable { showSyncLogs = !showSyncLogs }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📋 Sync Log (${syncLogs.size})",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (syncLogs.isNotEmpty()) {
                        Text(
                            text = "CLEAR",
                            color = SyncBlue.copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { viewModel.clearSyncLogs() }
                        )
                    }
                    Text(
                        text = if (showSyncLogs) "▲ HIDE" else "▼ SHOW",
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // ── Sync Log Entries ───────────────────────────────────────────────────
        if (showSyncLogs && syncLogs.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0D1117))
                        .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                        .padding(8.dp)
                        .animateContentSize(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    syncLogs.take(50).forEach { entry ->
                        SyncLogEntryRow(entry)
                    }
                }
            }
        }

        // ── Data Type Selector ─────────────────────────────────────────────────
        item {
            DataTypeSelector(
                selectedType = selectedDataType,
                onTypeSelected = { selectedDataType = it }
            )
        }

        // ── Data Content ───────────────────────────────────────────────────────
        when (selectedDataType) {
            DataType.SLEEP -> {
                if (sleepDataList.isEmpty()) {
                    item { EmptyDataCard("No sleep data", "Tap sync to fetch sleep records") }
                } else {
                    items(sleepDataList) { sleep ->
                        SleepDataCard(sleep)
                    }
                }
            }
            DataType.HEART_RATE -> {
                val heartRatePoints = originDataList.filter { it.rateValue > 0 }
                if (heartRatePoints.isEmpty()) {
                    item { EmptyDataCard("No heart rate data", "Tap sync to fetch origin data") }
                } else {
                    item {
                        HeartRateSummaryCard(heartRatePoints)
                    }
                    items(heartRatePoints.take(50)) { data ->
                        OriginDataCard(data, DataType.HEART_RATE)
                    }
                }
            }
            DataType.TEMPERATURE -> {
                if (temperatureRecords.isEmpty()) {
                    item { EmptyDataCard("No temperature data", "Tap sync to fetch records") }
                } else {
                    items(temperatureRecords) { record ->
                        TemperatureRecordCard(record)
                    }
                }
            }
            DataType.STEPS -> {
                val stepsData = originHalfHourDataList
                if (stepsData.isEmpty()) {
                    item { EmptyDataCard("No step data", "Tap sync to fetch activity data") }
                } else {
                    items(stepsData) { halfHour ->
                        HalfHourStepsCard(halfHour)
                    }
                }
            }
        }
    }
}

// ─── Day Selector ────────────────────────────────────────────────────────────────

@Composable
private fun DaySelector(
    watchDay: Int,
    selectedDay: Int,
    onDaySelected: (Int) -> Unit
) {
    Column {
        Text(
            text = "SELECT DAY",
            color = Color.White.copy(alpha = 0.3f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // "All" chip
            DayChip(
                label = "All Days",
                isSelected = selectedDay == -1,
                color = SyncBlue,
                onClick = { onDaySelected(-1) }
            )
            // Individual days
            for (day in 0 until watchDay) {
                val label = when (day) {
                    0 -> "Today"
                    1 -> "Yesterday"
                    else -> "${day}d ago"
                }
                DayChip(
                    label = label,
                    isSelected = selectedDay == day,
                    color = VBandAccent,
                    onClick = { onDaySelected(day) }
                )
            }
        }
    }
}

@Composable
private fun DayChip(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) color.copy(alpha = 0.2f)
                else Color.White.copy(alpha = 0.04f)
            )
            .border(
                1.dp,
                if (isSelected) color.copy(alpha = 0.5f)
                else Color.Transparent,
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) color else Color.White.copy(alpha = 0.4f),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ─── Sync Status Card ────────────────────────────────────────────────────────────

@Composable
private fun SyncStatusCard(
    syncState: VBandSyncState,
    onCancel: () -> Unit
) {
    val isSyncing = syncState.status == VBandSyncStatus.SYNCING
    val isCompleted = syncState.status == VBandSyncStatus.COMPLETED
    val isError = syncState.status == VBandSyncStatus.ERROR

    val statusColor = when {
        isCompleted -> LogGreen
        isError -> LogRed
        else -> SyncBlue
    }
    val statusEmoji = when {
        isCompleted -> "✅"
        isError -> "❌"
        else -> "🔄"
    }

    // Pulsing animation for syncing
    val pulseAlpha by rememberInfiniteTransition().animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        )
    )

    GlassCard(gradStart = statusColor, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp).animateContentSize()) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(statusEmoji, fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = when {
                            isCompleted -> "Sync Complete"
                            isError -> "Sync Failed"
                            else -> "Syncing..."
                        },
                        color = Color.White.copy(alpha = if (isSyncing) pulseAlpha else 1f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (isSyncing) {
                    Text(
                        text = "CANCEL",
                        color = LogRed.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(LogRed.copy(alpha = 0.1f))
                            .clickable(onClick = onCancel)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Step info
            if (isSyncing && syncState.currentStepName.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Step ${syncState.currentStepIndex}/${syncState.totalSteps}: ${syncState.currentStepName}",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${(syncState.stepProgress * 100).toInt()}%",
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(6.dp))
                // Overall progress
                val overallProgress = if (syncState.totalSteps > 0) {
                    ((syncState.currentStepIndex - 1).toFloat() + syncState.stepProgress) / syncState.totalSteps
                } else 0f
                LinearProgressIndicator(
                    progress = { overallProgress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = statusColor,
                    trackColor = Color.White.copy(alpha = 0.08f),
                    strokeCap = StrokeCap.Round
                )
            }

            // Error message
            if (isError && syncState.errorMessage.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = syncState.errorMessage,
                    color = LogRed.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

// ─── Sync Log Entry Row ──────────────────────────────────────────────────────────

@Composable
private fun SyncLogEntryRow(entry: VBandSyncLogEntry) {
    val levelColor = when (entry.level) {
        VBandLogLevel.SUCCESS -> LogGreen
        VBandLogLevel.ERROR -> LogRed
        VBandLogLevel.WARN -> LogYellow
        VBandLogLevel.DEBUG -> Color.White.copy(alpha = 0.3f)
        VBandLogLevel.INFO -> SyncBlue
    }
    val levelIcon = when (entry.level) {
        VBandLogLevel.SUCCESS -> "✓"
        VBandLogLevel.ERROR -> "✗"
        VBandLogLevel.WARN -> "⚠"
        VBandLogLevel.DEBUG -> "·"
        VBandLogLevel.INFO -> "→"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Timestamp
        Text(
            text = formatTimestamp(entry.timestamp),
            color = Color.White.copy(alpha = 0.2f),
            fontSize = 9.sp,
            modifier = Modifier.width(50.dp)
        )
        // Level icon
        Text(
            text = levelIcon,
            color = levelColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(16.dp)
        )
        // Step name
        if (entry.stepName.isNotEmpty()) {
            Text(
                text = "[${entry.stepName}]",
                color = levelColor.copy(alpha = 0.6f),
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(end = 4.dp)
            )
        }
        // Message
        Text(
            text = entry.message,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 9.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val seconds = (timestamp / 1000) % 60
    val minutes = (timestamp / 60000) % 60
    val hours = (timestamp / 3600000) % 24
    return "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}

// ─── Data Type Enum ──────────────────────────────────────────────────────────────

enum class DataType(val emoji: String, val label: String, val color: Color) {
    SLEEP("😴", "Sleep", Color(0xFF7E57C2)),
    HEART_RATE("❤️", "Heart", Color(0xFFEF5350)),
    TEMPERATURE("🌡️", "Temp", Color(0xFFFDD835)),
    STEPS("👟", "Steps", Color(0xFFFF9800))
}

// ─── Data Type Selector ──────────────────────────────────────────────────────────

@Composable
private fun DataTypeSelector(
    selectedType: DataType,
    onTypeSelected: (DataType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DataType.entries.forEach { type ->
            val isSelected = type == selectedType
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) type.color.copy(alpha = 0.18f)
                        else Color.White.copy(alpha = 0.04f)
                    )
                    .border(
                        1.dp,
                        if (isSelected) type.color.copy(alpha = 0.4f)
                        else Color.Transparent,
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { onTypeSelected(type) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(type.emoji, fontSize = 18.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = type.label,
                        color = if (isSelected) type.color else Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

// ─── Sync Actions Row ────────────────────────────────────────────────────────────

@Composable
private fun SyncActionsRow(
    selectedDay: Int,
    isSyncing: Boolean,
    onSyncAll: () -> Unit,
    onSyncDay: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Sync All button
        Button(
            onClick = onSyncAll,
            enabled = !isSyncing,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = Color.White.copy(alpha = 0.3f)
            ),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                SyncBlue.copy(alpha = if (isSyncing) 0.08f else 0.2f),
                                SyncBlue.copy(alpha = if (isSyncing) 0.03f else 0.08f)
                            )
                        ),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .border(1.dp, SyncBlue.copy(alpha = if (isSyncing) 0.1f else 0.25f), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isSyncing) "⏳ Syncing..." else "🔄 Sync All",
                    color = if (isSyncing) SyncBlue.copy(alpha = 0.5f) else SyncBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Sync Day button  
        if (selectedDay >= 0) {
            val dayLabel = when (selectedDay) {
                0 -> "Today"
                1 -> "Yesterday"
                else -> "${selectedDay}d ago"
            }
            Button(
                onClick = onSyncDay,
                enabled = !isSyncing,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = Color.White.copy(alpha = 0.3f)
                ),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    VBandAccent.copy(alpha = if (isSyncing) 0.08f else 0.2f),
                                    VBandAccent.copy(alpha = if (isSyncing) 0.03f else 0.08f)
                                )
                            ),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .border(1.dp, VBandAccent.copy(alpha = if (isSyncing) 0.1f else 0.25f), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📅 $dayLabel",
                        color = if (isSyncing) VBandAccent.copy(alpha = 0.5f) else VBandAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ─── Empty Data Card ─────────────────────────────────────────────────────────────

@Composable
private fun EmptyDataCard(title: String, subtitle: String) {
    GlassCard(gradStart = Color(0xFF757575), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("📭", fontSize = 36.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.35f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Sleep Data Card ─────────────────────────────────────────────────────────────

@Composable
private fun SleepDataCard(sleep: VBandSleepData) {
    val totalHours = sleep.totalSleepTime / 60
    val totalMinutes = sleep.totalSleepTime % 60

    GlassCard(gradStart = SleepPurple, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sleep.date.ifEmpty { "Unknown" },
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
                Text(
                    text = "Quality: ${sleep.sleepQuality}",
                    color = SleepPurple,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${totalHours}h ${totalMinutes}m",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SleepStatChip("Deep", "${sleep.deepSleepTime}m", SleepPurple)
                SleepStatChip("Light", "${sleep.lightSleepTime}m", Color(0xFFB39DDB))
                SleepStatChip("Woke", "${sleep.wakeCount}x", HeartRed.copy(alpha = 0.7f))
            }
            if (sleep.sleepDownHour > 0 || sleep.sleepUpHour > 0) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "🛏️ ${sleep.sleepDownHour}:${sleep.sleepDownMinute.toString().padStart(2, '0')} → " +
                           "☀️ ${sleep.sleepUpHour}:${sleep.sleepUpMinute.toString().padStart(2, '0')}",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun SleepStatChip(label: String, value: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = "$label $value",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp
        )
    }
}

// ─── Heart Rate Summary Card ─────────────────────────────────────────────────────

@Composable
private fun HeartRateSummaryCard(data: List<VBandOriginData>) {
    val avg = data.map { it.rateValue }.average().toInt()
    val max = data.maxOf { it.rateValue }
    val min = data.minOf { it.rateValue }

    GlassCard(gradStart = HeartRed, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "HEART RATE SUMMARY",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HeartStatItem("Avg", "$avg", "bpm")
                HeartStatItem("Max", "$max", "bpm")
                HeartStatItem("Min", "$min", "bpm")
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${data.size} data points",
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun HeartStatItem(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 11.sp
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = unit,
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 10.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    }
}

// ─── Origin Data Card (Heart Rate) ───────────────────────────────────────────────

@Composable
private fun OriginDataCard(data: VBandOriginData, type: DataType) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(HeartRed.copy(alpha = 0.06f), BgCard)
                )
            )
            .border(1.dp, HeartRed.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "${data.hour.toString().padStart(2, '0')}:${data.minute.toString().padStart(2, '0')}",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 12.sp
            )
            Text(
                text = "${data.rateValue} bpm",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Mini bar for heart rate
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White.copy(alpha = 0.06f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(data.rateValue / 200f)
                    .clip(RoundedCornerShape(3.dp))
                    .background(HeartRed)
            )
        }
    }
}

// ─── Temperature Record Card ─────────────────────────────────────────────────────

@Composable
private fun TemperatureRecordCard(record: VBandTemperatureRecord) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(TempYellow.copy(alpha = 0.06f), BgCard)
                )
            )
            .border(1.dp, TempYellow.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("🌡️", fontSize = 18.sp)
            Column {
                Text(
                    text = "${record.hour.toString().padStart(2, '0')}:${record.minute.toString().padStart(2, '0')}",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 11.sp
                )
                Text(
                    text = "${record.temperature}°C",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        if (record.isManual) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(TempYellow.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Manual",
                    color = TempYellow,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ─── Half Hour Steps Card ────────────────────────────────────────────────────────

@Composable
private fun HalfHourStepsCard(halfHour: VBandOriginHalfHourData) {
    GlassCard(gradStart = StepsOrange, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = halfHour.date.ifEmpty { "Unknown" },
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp
                )
                Text(
                    text = "${halfHour.allStep} steps",
                    color = StepsOrange,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))

            // Show sport data summary
            val sportEntries = halfHour.halfHourSportData
            if (sportEntries.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val totalDist = sportEntries.sumOf { it.distance }
                    val totalCal = sportEntries.sumOf { it.calories }
                    Text(
                        text = "📏 ${FormatUtils.formatDecimal(totalDist, 1)} km",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "🔥 ${FormatUtils.formatDecimal(totalCal, 0)} kcal",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
