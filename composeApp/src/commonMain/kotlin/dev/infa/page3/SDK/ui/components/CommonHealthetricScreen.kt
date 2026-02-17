package dev.infa.page3.SDK.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import dev.infa.page3.SDK.ui.theme.*
import dev.infa.page3.SDK.ui.utils.DateInfo
import kotlinx.coroutines.*


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
    var currentZoneColor by remember { mutableStateOf(AppColors.TextSecondary) }
    var isMeasuring by remember { mutableStateOf(false) }
    var measurementProgress by remember { mutableStateOf(0f) }
    var selectedDuration by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(metricData.average) {
        if (!isMeasuring && currentDisplayValue == 0) {
            currentDisplayValue = metricData.average
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BackgroundPrimary)
    ) {
        DateSelector(
            selectedDate = selectedDate,
            onDateChange = onDateChange
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(AppDimensions.ScreenPadding.Horizontal),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.XXL)
        ) {
            // SECTION 1: CURRENT MEASUREMENT
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

                        val animationJob = CoroutineScope(Dispatchers.Main).launch {
                            val steps = metricData.measurementDurationSeconds * 10
                            repeat(steps) { step ->
                                delay(100L)
                                measurementProgress = (step + 1).toFloat() / steps
                            }
                        }

                        onMeasureClick { result ->
                            animationJob.cancel()
                            isMeasuring = false
                            measurementProgress = 0f

                            if (result > 0) {
                                currentDisplayValue = result
                                when {
                                    result <= metricData.min + 5 -> {
                                        currentZone = "Low"
                                        currentZoneColor = AppColors.ZoneLow
                                    }
                                    result >= metricData.max - 5 -> {
                                        currentZone = "High"
                                        currentZoneColor = AppColors.ZoneHigh
                                    }
                                    else -> {
                                        currentZone = "Normal"
                                        currentZoneColor = AppColors.ZoneNormal
                                    }
                                }
                            } else {
                                currentDisplayValue = metricData.average
                            }
                        }
                    }
                )
            }

            // TODAY STATS
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(
                        metricData.min.toString(),
                        "Min",
                        AppColors.ZoneLow
                    )
                    StatItem(
                        metricData.average.toString(),
                        "Average",
                        AppColors.ZoneNormal
                    )
                    StatItem(
                        metricData.max.toString(),
                        "Max",
                        AppColors.BloodOxygen
                    )
                }
            }

            // SECTION 2: 24-HOUR CHART
            if (measurements.isNotEmpty()) {
                item {
                    TwentyFourHourChart(
                        measurements = measurements,
                        metricData = metricData
                    )
                }
            }

            // CONTINUOUS MONITOR SECTION
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

            // SECTION 3: RECENT MEASUREMENTS
            if (measurements.isNotEmpty()) {
                item {
                    Text(
                        "Recent Measurements",
                        color = AppColors.TextPrimary,
                        style = AppTypography.HeadingExtraSmall
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
        colors = CardDefaults.cardColors(
            containerColor = AppColors.BackgroundCard
        ),
        shape = AppShapes.CardXXL
    ) {
        Column(
            modifier = Modifier.padding(AppDimensions.CardPadding.XXL),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon or Progress Indicator
            if (isMeasuring && currentValue == 0) {
                Box(
                    modifier = Modifier.size(AppDimensions.IconSize.Massive),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { measurementProgress },
                        modifier = Modifier.size(AppDimensions.IconSize.Massive),
                        color = metricData.iconColor,
                        strokeWidth = AppDimensions.ProgressRing.Stroke
                    )
                    Text(
                        "${(measurementProgress * 100).toInt()}%",
                        color = AppColors.TextPrimary,
                        style = AppTypography.BodySmall
                    )
                }
            } else {
                Icon(
                    metricData.icon,
                    contentDescription = null,
                    tint = metricData.iconColor,
                    modifier = Modifier.size(AppDimensions.IconSize.Massive)
                )
            }

            Spacer(Modifier.height(AppDimensions.Spacing.Large))

            Text(
                "Current ${metricData.title}",
                color = AppColors.TextSecondary,
                style = AppTypography.BodyMedium
            )

            val displayValue = if (isMeasuring && currentValue == 0) {
                "..."
            } else {
                currentValue.toString()
            }

            Text(
                text = displayValue,
                style = AppTypography.DisplayLarge,
                color = if (currentValue > 0) currentZoneColor else AppColors.ZoneNormal
            )

            Text(
                metricData.unit,
                color = AppColors.TextSecondary,
                style = AppTypography.BodyMedium
            )

            Spacer(Modifier.height(AppDimensions.Spacing.Large))

            // Zone indicator
            if (currentZone.isNotEmpty() && currentValue > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(AppDimensions.Component.ZoneIndicatorSize)
                            .background(currentZoneColor, CircleShape)
                    )
                    Spacer(Modifier.width(AppDimensions.Spacing.Medium))
                    Text(
                        currentZone,
                        color = currentZoneColor,
                        style = AppTypography.BodyMedium
                    )
                }

                Spacer(Modifier.height(AppDimensions.Spacing.XXL))
            }

            // Measure button
            Button(
                onClick = onMeasureClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = metricData.iconColor
                ),
                shape = AppShapes.ButtonLarge,
                enabled = !isMeasuring
            ) {
                Icon(
                    if (isMeasuring) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(Modifier.width(AppDimensions.Spacing.Medium))
                Text(
                    if (isMeasuring) "Measuring..." else "Measure Now",
                    style = AppTypography.ButtonMedium
                )
            }
        }
    }
}
@Composable
fun TwentyFourHourChart(
    measurements: List<HealthMeasurement>,
    metricData: HealthMetricData
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = AppColors.BackgroundCard
        ),
        shape = AppShapes.TrendChart
    ) {
        Column(Modifier.padding(AppDimensions.CardPadding.Large)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "24-Hour Trend",
                    color = AppColors.TextPrimary,
                    style = AppTypography.BodyMedium
                )
                Icon(
                    Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = metricData.iconColor
                )
            }

            Spacer(Modifier.height(AppDimensions.Spacing.Large))

            // Chart bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppDimensions.ChartHeight.Large),
                verticalAlignment = Alignment.Bottom
            ) {
                val maxRate = measurements.maxOfOrNull { it.rate } ?: 100
                val minRate = measurements.minOfOrNull { it.rate } ?: 0
                val range = maxRate - minRate

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
                            .padding(horizontal = AppDimensions.Border.Thin)
                            .background(
                                measurement.color,
                                AppShapes.ChartBarLarge
                            )
                    )
                }
            }

            Spacer(Modifier.height(AppDimensions.Spacing.Small))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "12 AM",
                    color = AppColors.TextSecondary,
                    style = AppTypography.LabelSmall
                )
                Text(
                    "12 PM",
                    color = AppColors.TextSecondary,
                    style = AppTypography.LabelSmall
                )
                Text(
                    "Now",
                    color = AppColors.TextSecondary,
                    style = AppTypography.LabelSmall
                )
            }
        }
    }
}

@Composable
fun ContinuousMonitorSection(
    durations: List<Int>,
    selectedDuration: Int?,
    onDurationSelect: (Int) -> Unit,
    onStartMonitor: (Int) -> Unit,
    iconColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = AppColors.BackgroundCard
        ),
        shape = AppShapes.TrendChart
    ) {
        Column(Modifier.padding(AppDimensions.CardPadding.Large)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = iconColor
                )
                Spacer(Modifier.width(AppDimensions.Spacing.Small))
                Text(
                    "Continuous Monitor",
                    color = AppColors.TextPrimary,
                    style = AppTypography.BodyMedium
                )
            }

            Spacer(Modifier.height(AppDimensions.Spacing.Large))

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
                Spacer(Modifier.height(AppDimensions.Spacing.Large))

                Button(
                    onClick = { onStartMonitor(selectedDuration) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = iconColor),
                    shape = AppShapes.ButtonLarge
                ) {
                    Icon(Icons.Default.Favorite, contentDescription = null)
                    Spacer(Modifier.width(AppDimensions.Spacing.Medium))
                    Text(
                        "Start $selectedDuration min Monitor",
                        style = AppTypography.ButtonMedium
                    )
                }
            }
        }
    }
}

@Composable
fun RowScope.StatItem(value: String, label: String, color: Color) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = AppColors.BackgroundCard
        ),
        shape = AppShapes.StatCard,
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = AppDimensions.Spacing.ExtraSmall)
    ) {
        Column(
            modifier = Modifier
                .padding(AppDimensions.Spacing.ExtraLarge)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                color = color,
                style = AppTypography.ValueLarge
            )
            Text(
                label,
                color = AppColors.TextSecondary,
                style = AppTypography.LabelMedium
            )
        }
    }
}

@Composable
fun DurationButton(
    value: Int,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val bg = if (selected) {
        accentColor.copy(alpha = AppAlpha.VeryLight)
    } else {
        AppColors.BackgroundCard
    }
    val textColor = if (selected) accentColor else AppColors.TextSecondary

    Box(
        modifier = Modifier
            .clip(AppShapes.CardSmall)
            .background(bg)
            .border(
                AppDimensions.Border.Thin,
                textColor.copy(alpha = AppAlpha.Disabled),
                AppShapes.CardSmall
            )
            .clickable(onClick = onClick)
            .padding(
                horizontal = AppDimensions.Spacing.XXL,
                vertical = AppDimensions.Spacing.Default
            )
    ) {
        Text(
            "$value min",
            color = textColor,
            style = AppTypography.LabelMedium
        )
    }
}

@Composable
fun RecentMeasurementRow(measurement: HealthMeasurement, unit: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                AppColors.BackgroundCard,
                AppShapes.CardMedium
            )
            .padding(AppDimensions.Spacing.Large),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                "${measurement.rate} $unit",
                color = AppColors.TextPrimary,
                style = AppTypography.BodyMedium
            )
            Text(
                measurement.time,
                color = AppColors.TextSecondary,
                style = AppTypography.LabelSmall
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                measurement.type,
                color = measurement.color,
                style = AppTypography.LabelMedium
            )
            Box(
                modifier = Modifier
                    .size(AppDimensions.Spacing.Small)
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
            containerColor = AppColors.BackgroundTertiary,
            shape = AppShapes.Dialog,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.Default)
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = metricColor,
                        modifier = Modifier.size(AppDimensions.IconSize.ExtraLarge)
                    )
                    Text(
                        "Start Continuous Monitor?",
                        color = AppColors.TextPrimary,
                        style = AppTypography.HeadingExtraSmall
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.ExtraLarge)
                ) {
                    Text(
                        "This will continuously monitor your $metricName for $duration minutes.",
                        color = AppColors.TextSecondary,
                        style = AppTypography.BodySmall
                    )

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = metricColor.copy(alpha = AppAlpha.VeryLight)
                        ),
                        shape = AppShapes.CardMedium
                    ) {
                        Column(
                            modifier = Modifier.padding(AppDimensions.Spacing.ExtraLarge),
                            verticalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.Medium)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.Medium),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = metricColor,
                                    modifier = Modifier.size(AppDimensions.IconSize.Default)
                                )
                                Text(
                                    "Duration: $duration minutes",
                                    color = AppColors.TextPrimary,
                                    style = AppTypography.BodySmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.Medium),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = metricColor,
                                    modifier = Modifier.size(AppDimensions.IconSize.Default)
                                )
                                Text(
                                    "You'll be notified when complete",
                                    color = AppColors.TextSecondary,
                                    style = AppTypography.LabelMedium
                                )
                            }
                        }
                    }

                    Text(
                        "⚠️ Keep your device connected during monitoring",
                        color = AppColors.Warning,
                        style = AppTypography.LabelMedium.copy(fontStyle = FontStyle.Italic)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = metricColor),
                    shape = AppShapes.ButtonSmall,
                    modifier = Modifier.fillMaxWidth(0.48f)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(AppDimensions.Spacing.Medium))
                    Text("Start", style = AppTypography.ButtonMedium)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AppColors.TextSecondary
                    ),
                    border = BorderStroke(
                        AppDimensions.Border.Thin,
                        AppColors.TextSecondary.copy(alpha = AppAlpha.Disabled)
                    ),
                    shape = AppShapes.ButtonSmall,
                    modifier = Modifier.fillMaxWidth(0.48f)
                ) {
                    Text("Cancel", style = AppTypography.ButtonMedium)
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
            containerColor = AppColors.BackgroundTertiary,
            shape = AppShapes.Dialog,
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(AppDimensions.IconSize.Huge)
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
                            modifier = Modifier.size(AppDimensions.IconSize.XXL)
                        )
                    }
                    Spacer(Modifier.height(AppDimensions.Spacing.ExtraLarge))
                    Text(
                        "Monitoring Started!",
                        color = AppColors.TextPrimary,
                        style = AppTypography.HeadingSmall
                    )
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.Default)
                ) {
                    Text(
                        "Your $metricName will be monitored for the next $duration minutes.",
                        color = AppColors.TextSecondary,
                        style = AppTypography.BodySmall,
                        textAlign = TextAlign.Center
                    )

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = metricColor.copy(alpha = AppAlpha.VeryLight)
                        ),
                        shape = AppShapes.CardMedium
                    ) {
                        Row(
                            modifier = Modifier.padding(AppDimensions.Spacing.ExtraLarge),
                            horizontalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.Default),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = metricColor,
                                modifier = Modifier.size(AppDimensions.IconSize.Large)
                            )
                            Text(
                                "You can continue using the app normally",
                                color = AppColors.TextPrimary,
                                style = AppTypography.BodyExtraSmall
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = metricColor),
                    shape = AppShapes.ButtonSmall,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Got it!", style = AppTypography.ButtonMedium)
                }
            }
        )
    }
}

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
            containerColor = AppColors.BackgroundTertiary,
            shape = AppShapes.Dialog,
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(AppDimensions.IconSize.Huge)
                            .background(
                                AppColors.Error.copy(alpha = 0.2f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = AppColors.Error,
                            modifier = Modifier.size(AppDimensions.IconSize.XXL)
                        )
                    }
                    Spacer(Modifier.height(AppDimensions.Spacing.ExtraLarge))
                    Text(
                        "Monitoring Failed",
                        color = AppColors.TextPrimary,
                        style = AppTypography.HeadingSmall
                    )
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.Default)
                ) {
                    errorMessage?.let {
                        Text(
                            it,
                            color = AppColors.TextSecondary,
                            style = AppTypography.BodySmall,
                            textAlign = TextAlign.Center
                        )
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = AppColors.Error.copy(alpha = AppAlpha.VeryLight)
                        ),
                        shape = AppShapes.CardMedium
                    ) {
                        Column(
                            modifier = Modifier.padding(AppDimensions.Spacing.ExtraLarge),
                            verticalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.Medium)
                        ) {
                            Text(
                                "Common issues:",
                                color = AppColors.TextPrimary,
                                style = AppTypography.BodyExtraSmall.copy(
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                                )
                            )
                            Text(
                                "• Device disconnected",
                                color = AppColors.TextSecondary,
                                style = AppTypography.LabelMedium
                            )
                            Text(
                                "• Low battery",
                                color = AppColors.TextSecondary,
                                style = AppTypography.LabelMedium
                            )
                            Text(
                                "• Feature not supported",
                                color = AppColors.TextSecondary,
                                style = AppTypography.LabelMedium
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
                        containerColor = AppColors.Secondary
                    ),
                    shape = AppShapes.ButtonSmall,
                    modifier = Modifier.fillMaxWidth(0.48f)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(AppDimensions.Spacing.Medium))
                    Text("Retry", style = AppTypography.ButtonMedium)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AppColors.TextSecondary
                    ),
                    border = BorderStroke(
                        AppDimensions.Border.Thin,
                        AppColors.TextSecondary.copy(alpha = AppAlpha.Disabled)
                    ),
                    shape = AppShapes.ButtonSmall,
                    modifier = Modifier.fillMaxWidth(0.48f)
                ) {
                    Text("Close", style = AppTypography.ButtonMedium)
                }
            }
        )
    }
}
