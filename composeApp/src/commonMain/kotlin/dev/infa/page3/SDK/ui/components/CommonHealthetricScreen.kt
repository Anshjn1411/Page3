package dev.infa.page3.SDK.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.infa.page3.SDK.ui.utils.DateInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class HealthMetricData(
    val title: String,
    val icon: ImageVector,
    val iconColor: Color,
    val unit: String,
    val average: Int,
    val min: Int,
    val max: Int,
    val currentValue: Int = 0,
    val zone: String = "",
    val zoneColor: Color = Color.Gray,
    val measurementDurationSeconds: Int = 45
)

data class HealthMeasurement(
    val time: String,
    val rate: Int,
    val type: String, // "Normal", "High", "Low", etc.
    val color: Color
)

data class ContinuousMonitorConfig(
    val enabled: Boolean = true,
    val durations: List<Int> = listOf(10, 20, 30, 60)
)

// ========================================
// COMMON HEALTH METRICS SCREEN
// ========================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CommonHealthMetricsScreen(
    metricData: HealthMetricData,
    measurements: List<HealthMeasurement>,
    selectedDate: DateInfo,
    onDateChange: (DateInfo) -> Unit,
    onBack: () -> Unit,
    onMeasureClick: (onResult: (Int) -> Unit) -> Unit,
    continuousMonitorConfig: ContinuousMonitorConfig = ContinuousMonitorConfig(),
    onStartContinuousMonitor: ((Int) -> Unit)? = null
) {
    var currentDisplayValue by remember { mutableStateOf(metricData.average) }
    var currentZone by remember { mutableStateOf("") }
    var currentZoneColor by remember { mutableStateOf(Color.Gray) }
    var isMeasuring by remember { mutableStateOf(false) }
    var measurementProgress by remember { mutableStateOf(0f) }
    var selectedDuration by remember { mutableStateOf<Int?>(null) }

    // Update display value when metricData changes
    LaunchedEffect(metricData.average) {
        if (!isMeasuring && currentDisplayValue == 0) {
            currentDisplayValue = metricData.average
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }
            Text(
                metricData.title,
                color = Color.White,
                fontSize = 20.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        DateSelector(
            selectedDate = selectedDate,
            onDateChange = onDateChange
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // ========================================
            // SECTION 1: CURRENT MEASUREMENT
            // ========================================
            item {
                CurrentMeasurementSection(
                    metricData = metricData,
                    currentValue = currentDisplayValue,
                    currentZone = currentZone,
                    currentZoneColor = currentZoneColor,
                    isMeasuring = isMeasuring,
                    measurementProgress = measurementProgress,
                    onMeasureClick = {
                        isMeasuring = true
                        measurementProgress = 0f
                        currentDisplayValue = 0

                        // Animate progress over measurement duration
                        val animationJob = CoroutineScope(Dispatchers.Main).launch {
                            val steps = metricData.measurementDurationSeconds * 10
                            repeat(steps) { step ->
                                delay(100L)
                                measurementProgress = (step + 1).toFloat() / steps
                            }
                        }

                        // Call the measurement callback
                        onMeasureClick { result ->
                            animationJob.cancel()
                            isMeasuring = false
                            measurementProgress = 0f

                            if (result > 0) {
                                currentDisplayValue = result
                                // Determine zone based on value
                                when {
                                    result <= metricData.min + 5 -> {
                                        currentZone = "Low"
                                        currentZoneColor = Color(0xFF3B82F6)
                                    }
                                    result >= metricData.max - 5 -> {
                                        currentZone = "High"
                                        currentZoneColor = Color(0xFFFF6B6B)
                                    }
                                    else -> {
                                        currentZone = "Normal"
                                        currentZoneColor = Color(0xFF00FF88)
                                    }
                                }
                            } else {
                                currentDisplayValue = metricData.average
                            }
                        }
                    }
                )
            }

            // TODAY STATS (Average, Min, Max)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(metricData.min.toString(), "Min", Color(0xFF3B82F6))
                    StatItem(metricData.average.toString(), "Average", Color(0xFF00FF88))
                    StatItem(metricData.max.toString(), "Max", Color(0xFF6366F1))
                }
            }

            // ========================================
            // SECTION 2: 24-HOUR CHART
            // ========================================
            if (measurements.isNotEmpty()) {
                item {
                    TwentyFourHourChart(
                        measurements = measurements,
                        metricData = metricData
                    )
                }
            }

            // ========================================
            // CONTINUOUS MONITOR SECTION
            // ========================================
            if (continuousMonitorConfig.enabled && onStartContinuousMonitor != null) {
                item {
                    ContinuousMonitorSection(
                        durations = continuousMonitorConfig.durations,
                        selectedDuration = selectedDuration,
                        onDurationSelect = { selectedDuration = it },
                        onStartMonitor = { onStartContinuousMonitor(it) },
                        iconColor = metricData.iconColor
                    )
                }
            }

            // ========================================
            // SECTION 3: RECENT MEASUREMENTS
            // ========================================
            if (measurements.isNotEmpty()) {
                item {
                    Text(
                        "Recent Measurements",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                items(measurements) { measurement ->
                    RecentMeasurementRow(
                        measurement = measurement,
                        unit = metricData.unit
                    )
                }
            }
        }
    }
}

// ========================================
// SECTION 1: CURRENT MEASUREMENT
// ========================================

@Composable
fun CurrentMeasurementSection(
    metricData: HealthMetricData,
    currentValue: Int,
    currentZone: String,
    currentZoneColor: Color,
    isMeasuring: Boolean,
    measurementProgress: Float,
    onMeasureClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0D0D)),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier.padding(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon or Progress Indicator
            if (isMeasuring && currentValue == 0) {
                Box(
                    modifier = Modifier.size(76.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = measurementProgress,
                        modifier = Modifier.size(76.dp),
                        color = metricData.iconColor,
                        strokeWidth = 6.dp
                    )
                    Text(
                        "${(measurementProgress * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            } else {
                Icon(
                    metricData.icon,
                    null,
                    tint = metricData.iconColor,
                    modifier = Modifier.size(76.dp)
                )
            }

            Spacer(Modifier.height(14.dp))

            Text("Current ${metricData.title}", color = Color.Gray)

            // Display value
            val displayValue = if (isMeasuring && currentValue == 0) {
                "..."
            } else {
                currentValue.toString()
            }

            Text(
                text = displayValue,
                fontSize = 58.sp,
                fontWeight = FontWeight.Bold,
                color = if (currentValue > 0) currentZoneColor else Color(0xFF00FF88)
            )

            Text(metricData.unit, color = Color.Gray)

            Spacer(Modifier.height(14.dp))

            // Zone indicator
            if (currentZone.isNotEmpty() && currentValue > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(currentZoneColor, CircleShape)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(currentZone, color = currentZoneColor)
                }

                Spacer(Modifier.height(18.dp))
            }

            // Measure button
            Button(
                onClick = onMeasureClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = metricData.iconColor
                ),
                shape = RoundedCornerShape(16.dp),
                enabled = !isMeasuring
            ) {
                Icon(
                    if (isMeasuring) Icons.Default.Stop else Icons.Default.PlayArrow,
                    null
                )
                Spacer(Modifier.width(8.dp))
                Text(if (isMeasuring) "Measuring..." else "Measure Now")
            }
        }
    }
}

// ========================================
// SECTION 2: 24-HOUR CHART
// ========================================

@Composable
fun TwentyFourHourChart(
    measurements: List<HealthMeasurement>,
    metricData: HealthMetricData
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0D0D)),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("24-Hour Trend", color = Color.White)
                Icon(Icons.Default.TrendingUp, null, tint = metricData.iconColor)
            }

            Spacer(Modifier.height(14.dp))

            // Chart bars based on measurements
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                val maxRate = measurements.maxOfOrNull { it.rate } ?: 100
                val minRate = measurements.minOfOrNull { it.rate } ?: 0
                val range = maxRate - minRate

                // Display up to 48 bars
                val displayData = if (measurements.size > 48) {
                    measurements.takeLast(48)
                } else {
                    measurements
                }

                displayData.forEach { measurement ->
                    val normalizedHeight = if (range > 0) {
                        ((measurement.rate - minRate).toFloat() / range).coerceIn(0.2f, 1f)
                    } else {
                        0.5f
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(normalizedHeight)
                            .padding(horizontal = 1.dp)
                            .background(measurement.color, RoundedCornerShape(4.dp))
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("12 AM", color = Color.Gray, fontSize = 11.sp)
                Text("12 PM", color = Color.Gray, fontSize = 11.sp)
                Text("Now", color = Color.Gray, fontSize = 11.sp)
            }
        }
    }
}

// ========================================
// CONTINUOUS MONITOR SECTION
// ========================================

@Composable
fun ContinuousMonitorSection(
    durations: List<Int>,
    selectedDuration: Int?,
    onDurationSelect: (Int) -> Unit,
    onStartMonitor: (Int) -> Unit,
    iconColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0D0D)),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccessTime, null, tint = iconColor)
                Spacer(Modifier.width(6.dp))
                Text("Continuous Monitor", color = Color.White)
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                durations.forEach { min ->
                    DurationButton(
                        value = min,
                        selected = selectedDuration == min,
                        accentColor = iconColor
                    ) {
                        onDurationSelect(min)
                    }
                }
            }

            if (selectedDuration != null) {
                Spacer(Modifier.height(14.dp))

                Button(
                    onClick = { onStartMonitor(selectedDuration) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = iconColor),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Favorite, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Start $selectedDuration min Monitor")
                }
            }
        }
    }
}

// ========================================
// HELPER COMPONENTS
// ========================================

@Composable
fun RowScope.StatItem(value: String, label: String, color: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0D0D)),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = color, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(label, color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
fun DurationButton(value: Int, selected: Boolean, accentColor: Color, onClick: () -> Unit) {
    val bg = if (selected) accentColor.copy(alpha = 0.1f) else Color(0xFF0D0D0D)
    val textColor = if (selected) accentColor else Color.Gray

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(1.dp, textColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text("$value min", color = textColor, fontSize = 12.sp)
    }
}

@Composable
fun RecentMeasurementRow(measurement: HealthMeasurement, unit: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D0D0D), RoundedCornerShape(16.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("${measurement.rate} $unit", color = Color.White)
            Text(measurement.time, color = Color.Gray, fontSize = 11.sp)
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(measurement.type, color = measurement.color, fontSize = 12.sp)
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(measurement.color, CircleShape)
            )
        }
    }
}


@Composable
fun ContinuousMonitoringDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    metricName: String,
    duration: Int,
    metricColor: Color,
    onConfirm: () -> Unit
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Color(0xFF1A1A1A),
            shape = RoundedCornerShape(24.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = metricColor,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        "Start Continuous Monitor?",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "This will continuously monitor your $metricName for $duration minutes.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = metricColor.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = metricColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    "Duration: $duration minutes",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = metricColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    "You'll be notified when complete",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Text(
                        "⚠️ Keep your device connected during monitoring",
                        color = Color(0xFFFFA500),
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = metricColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(0.48f)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Start", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Gray
                    ),
                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(0.48f)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ========================================
// SUCCESS DIALOG
// ========================================

@Composable
fun MonitoringSuccessDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    metricName: String,
    duration: Int,
    metricColor: Color
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Color(0xFF1A1A1A),
            shape = RoundedCornerShape(24.dp),
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                metricColor.copy(alpha = 0.2f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = metricColor,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Monitoring Started!",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Your $metricName will be monitored for the next $duration minutes.",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = metricColor.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = metricColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "You can continue using the app normally",
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = metricColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Got it!", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// ========================================
// ERROR DIALOG
// ========================================

@Composable
fun MonitoringErrorDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    errorMessage: String?,
    onRetry: () -> Unit
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Color(0xFF1A1A1A),
            shape = RoundedCornerShape(24.dp),
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                Color(0xFFFF6B6B).copy(alpha = 0.2f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = Color(0xFFFF6B6B),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Monitoring Failed",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    errorMessage?.let {
                        Text(
                            it,
                            color = Color.Gray,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFF6B6B).copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Common issues:",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "• Device disconnected",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                            Text(
                                "• Low battery",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                            Text(
                                "• Feature not supported",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRetry()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3B82F6)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(0.48f)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Retry", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Gray
                    ),
                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(0.48f)
                ) {
                    Text("Close")
                }
            }
        )
    }
}
