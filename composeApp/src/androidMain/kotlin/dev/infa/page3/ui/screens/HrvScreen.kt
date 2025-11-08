package dev.infa.page3.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.infa.page3.viewmodels.HrvViewModel
import java.text.SimpleDateFormat
import java.util.*



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HrvScreen(
    viewModel: HrvViewModel,
    onNavigateBack: () -> Unit
) {
    // Collect states
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isMeasuring by viewModel.isMeasuring.collectAsState()
    val measurementProgress by viewModel.measurementProgress.collectAsState()
    val currentHrv by viewModel.currentHrv.collectAsState()
    val instantHrv by viewModel.instantHrv.collectAsState()
    val averageHrv by viewModel.averageHrv.collectAsState()
    val minHrv by viewModel.minHrv.collectAsState()
    val maxHrv by viewModel.maxHrv.collectAsState()
    val allReadings by viewModel.allReadings.collectAsState()
    val intervalReadings by viewModel.intervalReadings.collectAsState()
    val isMonitoringEnabled by viewModel.isMonitoringEnabled.collectAsState()

    Scaffold(
        topBar = {
            HealthTopBar(
                title = "HRV Monitor",
                onNavigateBack = onNavigateBack,
                onRefresh = { viewModel.refresh(0) },
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
            // Current HRV Display
            item {
                HealthValueCard(
                    currentValue = currentHrv,
                    unit = "ms",
                    icon = Icons.Default.Favorite,
                    iconColor = HealthColors.PrimaryHRV,
                    valueColor = HealthColors.PrimaryHRV,
                    isMeasuring = isMeasuring,
                    measurementProgress = measurementProgress,
                    instantValue = instantHrv,
                    liveValue = currentHrv
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
                    buttonColor = HealthColors.PrimaryHRV,
                    buttonIcon = Icons.Default.Favorite,
                    onMeasure = { viewModel.measureHrvOnce() },
                    onStop = { viewModel.stopMeasurement() }
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
            if (averageHrv > 0 || minHrv > 0 || maxHrv > 0) {
                item {
                    StatisticsRow(
                        stats = listOf(
                            StatItem(
                                label = "Average",
                                value = if (averageHrv > 0) "$averageHrv ms" else "--",
                                icon = Icons.Default.FavoriteBorder,
                                color = HealthColors.PrimaryHRV
                            ),
                            StatItem(
                                label = "Minimum",
                                value = if (minHrv > 0) "$minHrv ms" else "--",
                                icon = Icons.Default.ArrowDownward,
                                color = HealthColors.Success
                            ),
                            StatItem(
                                label = "Maximum",
                                value = if (maxHrv > 0) "$maxHrv ms" else "--",
                                icon = Icons.Default.ArrowUpward,
                                color = HealthColors.Danger
                            )
                        )
                    )
                }
            }

            // HRV Chart
            if (intervalReadings.isNotEmpty()) {
                item {
                    ChartCard(
                        title = "30-Minute Intervals",
                        readingsCount = intervalReadings.size,
                        readings = intervalReadings,
                        getValue = { it.value },
                        chartColor = HealthColors.PrimaryHRV
                    )
                }
            }

            // All Readings List
            val validReadings = allReadings.filter { it.value > 0 }
            if (validReadings.isNotEmpty()) {
                item {
                    ReadingsListCard(
                        title = "Recent Readings",
                        totalCount = validReadings.size,
                        readings = validReadings,
                        maxDisplay = 10,
                        getTimestamp = { it.timestamp },
                        getValue = { it.value },
                        unit = "ms",
                        valueColor = HealthColors.PrimaryHRV,
                        timeFormat = "MMM dd, HH:mm"
                    )
                }
            }

            // Empty State
            if (intervalReadings.isEmpty() && !isLoading && !isMeasuring) {
                item {
                    EmptyStateCard(
                        icon = Icons.Default.Favorite,
                        title = "No HRV Data Yet",
                        subtitle = "Enable monitoring or measure now to start tracking"
                    )
                }
            }

            // Loading Indicator
            if (isLoading) {
                item {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = HealthColors.PrimaryHRV
                    )
                }
            }

            // Error Display
            error?.let { errorMessage ->
                item {
                    ErrorCard(message = errorMessage)
                }
            }
        }
    }
}



