package dev.infa.page3.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
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
import dev.infa.page3.viewmodels.BloodOxygenViewModel
import android.util.Log
import androidx.compose.material.icons.filled.BubbleChart
import dev.infa.page3.viewmodels.HeartRateViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodOxygenScreen(
    viewModel: BloodOxygenViewModel,
    onNavigateBack: () -> Unit
) {
    // Collect states
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isMeasuring by viewModel.isMeasuring.collectAsState()
    val measurementProgress by viewModel.measurementProgress.collectAsState()
    val currentBloodOxygen by viewModel.currentBloodOxygen.collectAsState()
    val liveHealthData by viewModel.liveHealthData.collectAsState()
    val instantBloodOxygen by viewModel.instantBloodOxygen.collectAsState()
    val averageSpO2 by viewModel.averageBloodOxygen.collectAsState()
    val minSpO2 by viewModel.minBloodOxygen.collectAsState()
    val maxSpO2 by viewModel.maxBloodOxygen.collectAsState()
    val allReadings by viewModel.allReadings.collectAsState()
    val intervalReadings by viewModel.intervalReadings.collectAsState()
    val isMonitoringEnabled by viewModel.isMonitoringEnabled.collectAsState()

    Scaffold(
        topBar = {
            HealthTopBar(
                title = "Blood Oxygen (SpO₂)",
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
            // Current SpO₂ Display
            item {
                HealthValueCard(
                    currentValue = currentBloodOxygen,
                    unit = "%",
                    icon = Icons.Default.BubbleChart, // or any suitable icon
                    iconColor = HealthColors.PrimarySpO2,
                    valueColor = HealthColors.PrimarySpO2,
                    isMeasuring = isMeasuring,
                    measurementProgress = measurementProgress,
                    instantValue = instantBloodOxygen,
                    liveValue = liveHealthData.spo2
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
                    buttonColor = HealthColors.PrimarySpO2,
                    buttonIcon = Icons.Default.BubbleChart,
                    onMeasure = { viewModel.measureBloodOxygenOnce() },
                    onStop = { viewModel.stopMeasurement() },
                    onClear = { viewModel.clearInstantMeasurement() },
                    showClearButton = instantBloodOxygen != null && instantBloodOxygen!! > 0
                )
            }

            // Continuous Monitoring
            item {
                ContinuousMonitoringCard(
                    title = "Continuous Monitoring",
                    subtitle = "Auto monitoring at 30-min intervals",
                    intervalMinutes = 30,
                    isEnabled = isMonitoringEnabled,
                    isLoading = isLoading,
                    isMeasuring = isMeasuring,
                    onToggle = { viewModel.toggleContinuousMonitoring(it) }
                )
            }

            // Statistics
            if (averageSpO2 > 0 || minSpO2 > 0 || maxSpO2 > 0) {
                item {
                    StatisticsRow(
                        stats = listOf(
                            StatItem(
                                label = "Average",
                                value = if (averageSpO2 > 0) "$averageSpO2%" else "--",
                                icon = Icons.Default.FavoriteBorder,
                                color = HealthColors.PrimarySpO2
                            ),
                            StatItem(
                                label = "Minimum",
                                value = if (minSpO2 > 0) "$minSpO2%" else "--",
                                icon = Icons.Default.ArrowDownward,
                                color = HealthColors.Success
                            ),
                            StatItem(
                                label = "Maximum",
                                value = if (maxSpO2 > 0) "$maxSpO2%" else "--",
                                icon = Icons.Default.ArrowUpward,
                                color = HealthColors.Danger
                            )
                        )
                    )
                }
            }

            // SpO₂ Chart
            if (intervalReadings.isNotEmpty()) {
                item {
                    ChartCard(
                        title = "30-Minute Intervals",
                        readingsCount = intervalReadings.size,
                        readings = intervalReadings,
                        getValue = { it.bloodOxygen },
                        chartColor = HealthColors.PrimarySpO2
                    )
                }
            }

            // All Readings List
            if (allReadings.isNotEmpty()) {
                item {
                    ReadingsListCard(
                        title = "Recent Readings",
                        totalCount = allReadings.size,
                        readings = allReadings,
                        maxDisplay = 10,
                        getTimestamp = { it.timestamp },
                        getValue = { it.bloodOxygen },
                        unit = "%",
                        valueColor = HealthColors.PrimarySpO2,
                        timeFormat = "MMM dd, HH:mm"
                    )
                }
            }

            // Empty State
            if (intervalReadings.isEmpty() && !isLoading && !isMeasuring) {
                item {
                    EmptyStateCard(
                        icon = Icons.Default.BubbleChart,
                        title = "No SpO₂ data",
                        subtitle = "Enable monitoring or measure now"
                    )
                }
            }

            // Loading Indicator
            if (isLoading) {
                item {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = HealthColors.PrimarySpO2
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
                style = MaterialTheme.typography.titleLarge,
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


