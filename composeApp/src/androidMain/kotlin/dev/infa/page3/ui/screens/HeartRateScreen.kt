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
    val currentHeartRate by viewModel.currentHeartRate.collectAsState()
    val liveHealthData by viewModel.liveHealthData.collectAsState()
    val instantHeartRate by viewModel.instantHeartRate.collectAsState()
    val averageHR by viewModel.averageHeartRate.collectAsState()
    val minHR by viewModel.minHeartRate.collectAsState()
    val maxHR by viewModel.maxHeartRate.collectAsState()
    val allReadings by viewModel.allReadings.collectAsState()
    val intervalReadings by viewModel.intervalReadings.collectAsState()
    val isMonitoringEnabled by viewModel.isMonitoringEnabled.collectAsState()
    val monitoringInterval by viewModel.monitoringInterval.collectAsState()

    Scaffold(
        topBar = {
            HealthTopBar(
                title = "Heart Rate",
                onNavigateBack = onNavigateBack,
                onRefresh = { viewModel.refresh() },
                isLoading = isLoading,
                isMeasuring = isMeasuring
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
            // Current Heart Rate Display
            item {
                HealthValueCard(
                    currentValue = currentHeartRate,
                    unit = "bpm",
                    icon = Icons.Default.Favorite,
                    iconColor = HealthColors.Primary,
                    valueColor = HealthColors.Primary,
                    isMeasuring = isMeasuring,
                    measurementProgress = measurementProgress,
                    instantValue = instantHeartRate,
                    liveValue = liveHealthData.heartRate
                )
            }

            // Instant Measurement
            item {
                InstantMeasurementCard(
                    title = "Instant Measurement",
                    subtitle = "Takes ~30 seconds",
                    isMeasuring = isMeasuring,
                    measurementProgress = measurementProgress,
                    isLoading = isLoading,
                    buttonColor = HealthColors.Primary,
                    buttonIcon = Icons.Default.Favorite,
                    onMeasure = { viewModel.measureHeartRateOnce() },
                    onStop = { viewModel.stopMeasurement() },
                    onClear = { viewModel.clearInstantMeasurement() },
                    showClearButton = instantHeartRate != null && instantHeartRate!! > 0
                )
            }

            // Continuous Monitoring
            item {
                ContinuousMonitoringCard(
                    title = "Continuous Monitoring",
                    subtitle = if (monitoringInterval > 0)
                        "Every $monitoringInterval minutes" else "Auto monitoring",
                    intervalMinutes = monitoringInterval,
                    isEnabled = isMonitoringEnabled,
                    isLoading = isLoading,
                    isMeasuring = isMeasuring,
                    onToggle = { viewModel.toggleContinuousMonitoring(it) }
                )
            }

            // Statistics
            if (averageHR > 0 || minHR > 0 || maxHR > 0) {
                item {
                    StatisticsRow(
                        stats = listOf(
                            StatItem(
                                label = "Average",
                                value = if (averageHR > 0) averageHR.toString() else "--",
                                icon = Icons.Default.FavoriteBorder,
                                color = HealthColors.Primary
                            ),
                            StatItem(
                                label = "Minimum",
                                value = if (minHR > 0) minHR.toString() else "--",
                                icon = Icons.Default.ArrowDownward,
                                color = HealthColors.Success
                            ),
                            StatItem(
                                label = "Maximum",
                                value = if (maxHR > 0) maxHR.toString() else "--",
                                icon = Icons.Default.ArrowUpward,
                                color = HealthColors.Danger
                            )
                        )
                    )
                }
            }

            // Heart Rate Chart
            if (intervalReadings.isNotEmpty()) {
                item {
                    ChartCard(
                        title = "30-Minute Intervals",
                        readingsCount = intervalReadings.size,
                        readings = intervalReadings,
                        getValue = { it.heartRate },
                        chartColor = HealthColors.Primary
                    )
                }
            }

            // All Readings List
            if (allReadings.isNotEmpty() && allReadings.size > intervalReadings.size) {
                item {
                    ReadingsListCard(
                        title = "Recent Readings",
                        totalCount = allReadings.size,
                        readings = allReadings,
                        maxDisplay = 10,
                        getTimestamp = { it.timestamp },
                        getValue = { it.heartRate },
                        unit = "bpm",
                        valueColor = HealthColors.Primary,
                        timeFormat = "HH:mm:ss"
                    )
                }
            }

            // Empty State
            if (intervalReadings.isEmpty() && !isLoading && !isMeasuring) {
                item {
                    EmptyStateCard(
                        icon = Icons.Default.Favorite,
                        title = "No heart rate data",
                        subtitle = "Enable monitoring or measure now"
                    )
                }
            }

            // Loading Indicator
            if (isLoading) {
                item {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = HealthColors.Primary
                    )
                }
            }

            // Error Display
            error?.let { errorMessage ->
                item {
                    ErrorCard(
                        message = errorMessage,
                        onDismiss = { viewModel.clearError() }
                    )
                }
            }
        }
    }
}

