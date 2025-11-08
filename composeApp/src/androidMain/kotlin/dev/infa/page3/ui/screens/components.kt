package dev.infa.page3.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

/**
 * Common Health Monitoring Components
 * Reusable UI components for Heart Rate, HRV, Blood Oxygen, etc.
 */

// ============================================================
// TOP APP BAR
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthTopBar(
    title: String,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit,
    isLoading: Boolean = false,
    isMeasuring: Boolean = false
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(
                onClick = onRefresh,
                enabled = !isLoading && !isMeasuring
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }
    )
}

// ============================================================
// MAIN VALUE DISPLAY CARD
// ============================================================

@Composable
fun HealthValueCard(
    currentValue: Int,
    unit: String,
    icon: ImageVector = Icons.Default.Favorite,
    iconColor: Color = HealthColors.Primary,
    valueColor: Color = HealthColors.Primary,
    isMeasuring: Boolean = false,
    measurementProgress: Int = 0,
    instantValue: Int? = null,
    liveValue: Int = 0,
    statusText: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = statusText ?: when {
                        isMeasuring -> "Measuring..."
                        instantValue != null && instantValue > 0 -> "Latest Measurement"
                        liveValue > 0 -> "Live Data"
                        else -> "Current"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = when {
                        isMeasuring -> HealthColors.Warning
                        liveValue > 0 -> HealthColors.Success
                        else -> HealthColors.TextSecondary
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Large Value Display
            Text(
                text = currentValue.toString(),
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )

            Text(
                text = unit,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Status Indicator
            StatusIndicator(
                isMeasuring = isMeasuring,
                measurementProgress = measurementProgress,
                hasInstantValue = instantValue != null && instantValue > 0,
                hasLiveValue = liveValue > 0
            )
        }
    }
}

@Composable
private fun StatusIndicator(
    isMeasuring: Boolean,
    measurementProgress: Int,
    hasInstantValue: Boolean,
    hasLiveValue: Boolean
) {
    when {
        isMeasuring -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = HealthColors.Warning
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Measuring: $measurementProgress%",
                    style = MaterialTheme.typography.bodySmall,
                    color = HealthColors.Warning
                )
            }
        }
        hasInstantValue -> {
            StatusRow(
                icon = Icons.Default.CheckCircle,
                text = "Measurement complete",
                color = HealthColors.Success,
                iconSize = 16.dp
            )
        }
        hasLiveValue -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(HealthColors.Success, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Receiving live data",
                    style = MaterialTheme.typography.bodySmall,
                    color = HealthColors.Success
                )
            }
        }
    }
}

// ============================================================
// INSTANT MEASUREMENT CARD
// ============================================================

@Composable
fun InstantMeasurementCard(
    title: String = "Instant Measurement",
    subtitle: String = "Takes ~30 seconds",
    isMeasuring: Boolean,
    measurementProgress: Int,
    isLoading: Boolean,
    buttonColor: Color = HealthColors.Primary,
    buttonIcon: ImageVector = Icons.Default.Favorite,
    onMeasure: () -> Unit,
    onStop: () -> Unit,
    onClear: (() -> Unit)? = null,
    showClearButton: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = HealthColors.CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(title = title, subtitle = subtitle)

                if (showClearButton && onClear != null) {
                    IconButton(onClick = onClear) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = HealthColors.TextSecondary
                        )
                    }
                }
            }

            if (isMeasuring) {
                MeasurementProgress(progress = measurementProgress)
            }

            Button(
                onClick = { if (isMeasuring) onStop() else onMeasure() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isMeasuring) HealthColors.Danger else buttonColor
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (isMeasuring) Icons.Default.Close else buttonIcon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isMeasuring) "Stop Measuring" else "Measure Now",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ============================================================
// CONTINUOUS MONITORING CARD
// ============================================================

@Composable
fun ContinuousMonitoringCard(
    title: String = "Continuous Monitoring",
    subtitle: String = "Auto monitoring at intervals",
    intervalMinutes: Int = 30,
    isEnabled: Boolean,
    isLoading: Boolean,
    isMeasuring: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = HealthColors.CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(
                    title = title,
                    subtitle = if (intervalMinutes > 0) "Every $intervalMinutes minutes" else subtitle
                )
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    enabled = !isLoading && !isMeasuring,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = HealthColors.Success
                    )
                )
            }

            if (isEnabled) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = HealthColors.Success.copy(alpha = 0.1f)
                    )
                ) {
                    StatusRow(
                        modifier = Modifier.padding(12.dp),
                        icon = Icons.Default.CheckCircle,
                        text = "Active - ${intervalMinutes}min interval",
                        color = HealthColors.Success
                    )
                }
            }
        }
    }
}

// ============================================================
// STATISTICS ROW
// ============================================================

@Composable
fun StatisticsRow(
    stats: List<StatItem>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        stats.forEach { stat ->
            StatCard(
                modifier = Modifier.weight(1f),
                label = stat.label,
                value = stat.value,
                icon = stat.icon,
                color = stat.color
            )
        }
    }
}

data class StatItem(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = HealthColors.TextSecondary
            )
        }
    }
}

// ============================================================
// CHART CARD (Generic)
// ============================================================

@Composable
fun <T> ChartCard(
    title: String,
    readingsCount: Int,
    readings: List<T>,
    getValue: (T) -> Int,
    chartColor: Color = HealthColors.Primary
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$readingsCount readings",
                style = MaterialTheme.typography.bodySmall,
                color = HealthColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))

            LineChart(
                readings = readings,
                getValue = getValue,
                color = chartColor
            )
        }
    }
}

@Composable
private fun <T> LineChart(
    readings: List<T>,
    getValue: (T) -> Int,
    color: Color
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(8.dp)
    ) {
        if (readings.size >= 2) {
            val values = readings.map { getValue(it) }
            val maxValue = values.maxOrNull()?.coerceAtLeast(1) ?: 1
            val minValue = values.minOrNull() ?: 0
            val range = (maxValue - minValue).coerceAtLeast(1)
            val stepX = size.width / (readings.size - 1).toFloat()

            var prevX = 0f
            var prevY = size.height

            readings.forEachIndexed { index, reading ->
                val x = index * stepX
                val value = getValue(reading)
                val normalized = (value - minValue).toFloat() / range.toFloat()
                val y = size.height - (normalized * size.height)

                if (index > 0) {
                    drawLine(
                        color = color,
                        start = androidx.compose.ui.geometry.Offset(prevX, prevY),
                        end = androidx.compose.ui.geometry.Offset(x, y),
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )
                }

                drawCircle(
                    color = color,
                    radius = 5f,
                    center = androidx.compose.ui.geometry.Offset(x, y)
                )

                prevX = x
                prevY = y
            }
        }
    }
}

// ============================================================
// READINGS LIST CARD (Generic)
// ============================================================

@Composable
fun <T> ReadingsListCard(
    title: String,
    totalCount: Int,
    readings: List<T>,
    maxDisplay: Int = 10,
    getTimestamp: (T) -> Long,
    getValue: (T) -> Int,
    unit: String,
    valueColor: Color = HealthColors.Primary,
    timeFormat: String = "MMM dd, HH:mm"
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$totalCount total readings",
                style = MaterialTheme.typography.bodySmall,
                color = HealthColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))

            val displayReadings = readings.takeLast(maxDisplay).reversed()
            displayReadings.forEachIndexed { index, reading ->
                ReadingRow(
                    timestamp = getTimestamp(reading),
                    value = getValue(reading),
                    unit = unit,
                    valueColor = valueColor,
                    timeFormat = timeFormat
                )
                if (index < displayReadings.size - 1) {
                    Divider(color = HealthColors.Divider)
                }
            }

            if (totalCount > maxDisplay) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Showing last $maxDisplay of $totalCount readings",
                    style = MaterialTheme.typography.bodySmall,
                    color = HealthColors.TextSecondary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun ReadingRow(
    timestamp: Long,
    value: Int,
    unit: String,
    valueColor: Color,
    timeFormat: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = SimpleDateFormat(timeFormat, Locale.getDefault()).format(Date(timestamp)),
            style = MaterialTheme.typography.bodyMedium,
            color = HealthColors.TextSecondary
        )
        Text(
            text = "$value $unit",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

// ============================================================
// EMPTY STATE CARD
// ============================================================

@Composable
fun EmptyStateCard(
    icon: ImageVector = Icons.Default.Favorite,
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = HealthColors.CardBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFCCCCCC),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = HealthColors.TextSecondary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = HealthColors.TextTertiary
            )
        }
    }
}

// ============================================================
// ERROR CARD
// ============================================================

@Composable
fun ErrorCard(
    message: String,
    onDismiss: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = HealthColors.Danger
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                color = HealthColors.Danger,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
            if (onDismiss != null) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = HealthColors.Danger
                    )
                }
            }
        }
    }
}

// ============================================================
// UTILITY COMPOSABLES
// ============================================================

@Composable
fun SectionHeader(
    title: String,
    subtitle: String
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = HealthColors.TextSecondary
        )
    }
}

@Composable
fun MeasurementProgress(progress: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            progress = progress / 100f,
            modifier = Modifier.fillMaxWidth(),
            color = HealthColors.Warning,
            trackColor = HealthColors.Warning.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$progress% - Please wait...",
            style = MaterialTheme.typography.bodySmall,
            color = HealthColors.TextSecondary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun StatusRow(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    text: String,
    color: Color,
    iconSize: androidx.compose.ui.unit.Dp = 20.dp
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(iconSize)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

// ============================================================
// COLOR CONSTANTS
// ============================================================

object HealthColors {
    val Primary = Color(0xFFE74C3C)           // Heart Rate - Red
    val PrimaryHRV = Color(0xFF9C27B0)        // HRV - Purple
    val PrimarySpO2 = Color(0xFF2196F3)       // Blood Oxygen - Blue
    val PrimaryStress = Color(0xFFFF6B35)     // Stress - Orange
    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFFF9800)
    val Danger = Color(0xFFE74C3C)
    val TextPrimary = Color(0xFF000000)
    val TextSecondary = Color(0xFF666666)
    val TextTertiary = Color(0xFF999999)
    val CardBackground = Color(0xFFF8F9FA)
    val Divider = Color(0xFFE0E0E0)
}