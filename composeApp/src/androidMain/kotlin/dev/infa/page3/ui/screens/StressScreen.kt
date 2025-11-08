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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.infa.page3.viewmodels.StressPoint
import dev.infa.page3.viewmodels.StressViewModel
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StressScreen(
    viewModel: StressViewModel,
    onNavigateBack: () -> Unit
) {
    // Collect states
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isMeasuring by viewModel.isMeasuring.collectAsState()
    val measurementProgress by viewModel.measurementProgress.collectAsState()
    val currentStress by viewModel.currentStress.collectAsState()
    val instantStress by viewModel.instantStress.collectAsState()
    val averageStress by viewModel.averageStress.collectAsState()
    val minStress by viewModel.minStress.collectAsState()
    val maxStress by viewModel.maxStress.collectAsState()
    val allReadings by viewModel.allReadings.collectAsState()
    val intervalReadings by viewModel.intervalReadings.collectAsState()
    val isMonitoringEnabled by viewModel.isMonitoringEnabled.collectAsState()

    Scaffold(
        topBar = {
            HealthTopBar(
                title = "Stress Monitor",
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
            // Current Stress Display
            item {
                HealthValueCard(
                    currentValue = currentStress,
                    unit = "Level",
                    icon = Icons.Default.Psychology,
                    iconColor = HealthColors.PrimaryStress,
                    valueColor = HealthColors.PrimaryStress,
                    isMeasuring = isMeasuring,
                    measurementProgress = measurementProgress,
                    instantValue = instantStress,
                    liveValue = currentStress
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
                    buttonColor = HealthColors.PrimaryStress,
                    buttonIcon = Icons.Default.Psychology,
                    onMeasure = { viewModel.measureStressOnce() },
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
            if (averageStress > 0 || minStress > 0 || maxStress > 0) {
                item {
                    StatisticsRow(
                        stats = listOf(
                            StatItem(
                                label = "Average",
                                value = if (averageStress > 0) "$averageStress" else "--",
                                icon = Icons.Default.PsychologyAlt,
                                color = HealthColors.PrimaryStress
                            ),
                            StatItem(
                                label = "Minimum",
                                value = if (minStress > 0) "$minStress" else "--",
                                icon = Icons.Default.ArrowDownward,
                                color = HealthColors.Success
                            ),
                            StatItem(
                                label = "Maximum",
                                value = if (maxStress > 0) "$maxStress" else "--",
                                icon = Icons.Default.ArrowUpward,
                                color = HealthColors.Danger
                            )
                        )
                    )
                }
            }

            // Stress Chart
            if (intervalReadings.isNotEmpty()) {
                item {
                    ChartCard(
                        title = "30-Minute Intervals",
                        readingsCount = intervalReadings.size,
                        readings = intervalReadings,
                        getValue = { it.level },
                        chartColor = HealthColors.PrimaryStress
                    )
                }
            }

            // All Readings List
            val validReadings = allReadings.filter { it.level > 0 }
            if (validReadings.isNotEmpty()) {
                item {
                    ReadingsListCard(
                        title = "Recent Readings",
                        totalCount = validReadings.size,
                        readings = validReadings,
                        maxDisplay = 10,
                        getTimestamp = { it.timestamp },
                        getValue = { it.level },
                        unit = "Level",
                        valueColor = HealthColors.PrimaryStress,
                        timeFormat = "MMM dd, HH:mm"
                    )
                }
            }

            // Empty State
            if (intervalReadings.isEmpty() && !isLoading && !isMeasuring) {
                item {
                    EmptyStateCard(
                        icon = Icons.Default.Psychology,
                        title = "No Stress Data Yet",
                        subtitle = "Enable monitoring or measure now to start tracking"
                    )
                }
            }

            // Loading Indicator
            if (isLoading) {
                item {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = HealthColors.PrimaryStress
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

