package dev.infa.page3.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.infa.page3.viewmodels.HeartRateViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeartRateScreen(
    viewModel: HeartRateViewModel,
    onNavigateBack: () -> Unit
) {
    // Collect all states
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isMeasuring by viewModel.isMeasuring.collectAsState()
    val measurementProgress by viewModel.measurementProgress.collectAsState()

    // Live data from device
    val currentHeartRate by viewModel.currentHeartRate.collectAsState()
    val liveHealthData by viewModel.liveHealthData.collectAsState()
    val instantHeartRate by viewModel.instantHeartRate.collectAsState()

    // Statistics (dynamic from actual data)
    val averageHR by viewModel.averageHeartRate.collectAsState()
    val minHR by viewModel.minHeartRate.collectAsState()
    val maxHR by viewModel.maxHeartRate.collectAsState()

    // All readings
    val allReadings by viewModel.allReadings.collectAsState()
    val intervalReadings by viewModel.intervalReadings.collectAsState()

    // Monitoring settings
    val isMonitoringEnabled by viewModel.isMonitoringEnabled.collectAsState()
    val monitoringInterval by viewModel.monitoringInterval.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Heart Rate") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refresh() },
                        enabled = !isLoading && !isMeasuring
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color(0xFFE74C3C),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when {
                                    isMeasuring -> "Measuring..."
                                    instantHeartRate != null && instantHeartRate!! > 0 -> "Latest Measurement"
                                    liveHealthData.heartRate > 0 -> "Live"
                                    else -> "Current"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = when {
                                    isMeasuring -> Color(0xFFFF9800)
                                    liveHealthData.heartRate > 0 -> Color(0xFF4CAF50)
                                    else -> Color(0xFF666666)
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = currentHeartRate.toString(),
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE74C3C)
                        )

                        Text(
                            text = "bpm",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )

                        // Show status indicator
                        Spacer(modifier = Modifier.height(8.dp))

                        when {
                            isMeasuring -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = Color(0xFFFF9800)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Measuring: $measurementProgress%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFFF9800)
                                    )
                                }
                            }
                            instantHeartRate != null && instantHeartRate!! > 0 -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Measurement complete",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF4CAF50)
                                    )
                                }
                            }
                            liveHealthData.heartRate > 0 -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                Color(0xFF4CAF50),
                                                shape = CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Receiving live data",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF4CAF50)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Instant Measurement Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
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
                            Column {
                                Text(
                                    text = "Instant Measurement",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Takes ~30 seconds",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF666666)
                                )
                            }

                            if (instantHeartRate != null && instantHeartRate!! > 0) {
                                IconButton(onClick = { viewModel.clearInstantMeasurement() }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = Color(0xFF666666)
                                    )
                                }
                            }
                        }

                        // Progress bar during measurement
                        if (isMeasuring) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                LinearProgressIndicator(
                                    progress = measurementProgress / 100f,
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color(0xFFFF9800),
                                    trackColor = Color(0xFFFFE0B2)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$measurementProgress% - Please wait...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF666666),
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (isMeasuring) {
                                    viewModel.stopMeasurement()
                                } else {
                                    viewModel.measureHeartRateOnce()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isMeasuring)
                                    Color(0xFFFF5722) else Color(0xFFE74C3C)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = if (isMeasuring)
                                    Icons.Default.Close else Icons.Default.Favorite,
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

            // Continuous Monitoring Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
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
                            Column {
                                Text(
                                    text = "Continuous Monitoring",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (monitoringInterval > 0)
                                        "Every $monitoringInterval minutes"
                                    else "Auto monitoring",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF666666)
                                )
                            }
                            Switch(
                                checked = isMonitoringEnabled,
                                onCheckedChange = { enabled ->
                                    viewModel.toggleContinuousMonitoring(enabled)
                                },
                                enabled = !isLoading && !isMeasuring,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF4CAF50)
                                )
                            )
                        }

                        if (isMonitoringEnabled) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Active - ${monitoringInterval}min interval",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF4CAF50),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Statistics
            if (averageHR > 0 || minHR > 0 || maxHR > 0) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            label = "Average",
                            value = if (averageHR > 0) averageHR.toString() else "--",
                            icon = Icons.Default.FavoriteBorder,
                            color = Color(0xFF3498DB)
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            label = "Minimum",
                            value = if (minHR > 0) minHR.toString() else "--",
                            icon = Icons.Default.ArrowDownward,
                            color = Color(0xFF4CAF50)
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            label = "Maximum",
                            value = if (maxHR > 0) maxHR.toString() else "--",
                            icon = Icons.Default.ArrowUpward,
                            color = Color(0xFFE74C3C)
                        )
                    }
                }
            }

            // Heart Rate Chart
            if (intervalReadings.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "30-Minute Intervals",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${intervalReadings.size} readings",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF666666)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(8.dp)
                            ) {
                                if (intervalReadings.size >= 2) {
                                    val maxValue = intervalReadings.maxOf { it.heartRate }.coerceAtLeast(1)
                                    val minValue = intervalReadings.minOf { it.heartRate }
                                    val range = (maxValue - minValue).coerceAtLeast(1)
                                    val stepX = size.width / (intervalReadings.size - 1).toFloat()

                                    var prevX = 0f
                                    var prevY = size.height

                                    intervalReadings.forEachIndexed { index, reading ->
                                        val x = index * stepX
                                        val normalized = (reading.heartRate - minValue).toFloat() / range.toFloat()
                                        val y = size.height - (normalized * size.height)

                                        if (index > 0) {
                                            drawLine(
                                                color = Color(0xFFE74C3C),
                                                start = androidx.compose.ui.geometry.Offset(prevX, prevY),
                                                end = androidx.compose.ui.geometry.Offset(x, y),
                                                strokeWidth = 6f,
                                                cap = StrokeCap.Round
                                            )
                                        }

                                        drawCircle(
                                            color = Color(0xFFE74C3C),
                                            radius = 5f,
                                            center = androidx.compose.ui.geometry.Offset(x, y)
                                        )

                                        prevX = x
                                        prevY = y
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // All Readings List (if different from interval)
            if (allReadings.isNotEmpty() && allReadings.size > intervalReadings.size) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "All Readings (${allReadings.size} total)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            allReadings.takeLast(10).reversed().forEach { reading ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                                            .format(Date(reading.timestamp)),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF666666)
                                    )
                                    Text(
                                        text = "${reading.heartRate} bpm",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFE74C3C)
                                    )
                                }
                                if (reading != allReadings.takeLast(10).reversed().last()) {
                                    Divider(color = Color(0xFFE0E0E0))
                                }
                            }

                            if (allReadings.size > 10) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Showing last 10 of ${allReadings.size} readings",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF999999),
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                        }
                    }
                }
            }

            // Empty State
            if (intervalReadings.isEmpty() && !isLoading && !isMeasuring) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color(0xFFCCCCCC),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No heart rate data",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF666666)
                            )
                            Text(
                                text = "Enable monitoring or measure now",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF999999)
                            )
                        }
                    }
                }
            }

            // Loading Indicator
            if (isLoading) {
                item {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFE74C3C)
                    )
                }
            }

            // Error Display
            if (error != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFEBEE)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = Color(0xFFE74C3C)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = error ?: "",
                                color = Color(0xFFE74C3C),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.clearError() }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = Color(0xFFE74C3C)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

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
                color = Color(0xFF666666)
            )
        }
    }
}




