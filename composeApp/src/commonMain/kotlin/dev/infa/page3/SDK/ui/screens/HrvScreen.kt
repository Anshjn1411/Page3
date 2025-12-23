package dev.infa.page3.SDK.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.infa.page3.SDK.data.HrvData
import dev.infa.page3.SDK.ui.DailogScreen.ContinuousMonitorDialog
import dev.infa.page3.SDK.ui.components.CommonHealthMetricsScreen
import dev.infa.page3.SDK.ui.components.ContinuousMonitorConfig
import dev.infa.page3.SDK.ui.components.ContinuousMonitoringDialog
import dev.infa.page3.SDK.ui.components.HealthMeasurement
import dev.infa.page3.SDK.ui.components.HealthMetricData
import dev.infa.page3.SDK.ui.components.MonitoringErrorDialog
import dev.infa.page3.SDK.ui.components.MonitoringSuccessDialog
import dev.infa.page3.SDK.ui.utils.DateUtils
import dev.infa.page3.SDK.ui.utils.formatTimestamp
import dev.infa.page3.SDK.viewModel.ContinuousMonitoringViewModel
import dev.infa.page3.SDK.viewModel.InstantMeasuresViewModel
import dev.infa.page3.SDK.viewModel.SyncViewModel

@Composable
fun HrvScreen(
    onBack: () -> Unit,
    viewModel: SyncViewModel,
    instantMeasuresViewModel: InstantMeasuresViewModel,
    continuousMonitoringViewModel: ContinuousMonitoringViewModel
) {
    // State management
    var selectedDate by remember { mutableStateOf(DateUtils.getCurrentDate()) }
    var hrvData by remember { mutableStateOf<HrvData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val selectedDateOffset = remember(selectedDate) {
        DateUtils.getDayOffsetFromToday(selectedDate)
    }

    LaunchedEffect(selectedDateOffset) {
        isLoading = true
        errorMessage = null

        viewModel.syncHrvDataForDay(
            offset = selectedDateOffset,
            onSuccess = { data ->
                hrvData = data
                isLoading = false
            },
            onError = { error ->
                errorMessage = error
                isLoading = false
            }
        )
    }

    // Convert HrvData to measurements for the chart
    val measurements = remember(hrvData) {
        hrvData?.hrvValues?.map { entry ->
            val time = formatTimestamp(entry.timestamp)
            val status = when {
                entry.hrvValue < 30 -> "Low"
                entry.hrvValue < 60 -> "Normal"
                else -> "Good"
            }
            val color = when {
                entry.hrvValue < 30 -> Color(0xFFFF6B6B)
                entry.hrvValue < 60 -> Color(0xFF3B82F6)
                else -> Color(0xFF00FF88)
            }
            HealthMeasurement(time, entry.hrvValue, status, color)
        } ?: emptyList()
    }

    // Prepare metric data
    val metricData = HealthMetricData(
        title = "HRV (Heart Rate Variability)",
        icon = Icons.Default.Favorite,
        iconColor = Color(0xFF00FF88),
        unit = "ms",
        average = hrvData?.averageHrv ?: 0,
        min = hrvData?.minHrv ?: 0,
        max = hrvData?.maxHrv ?: 0,
        measurementDurationSeconds = 30
    )

    // Show loading state
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color(0xFF00FF88))
                Spacer(Modifier.height(16.dp))
                Text("Loading HRV data...", color = Color.White)
            }
        }
        return
    }

    // Show error state
    if (errorMessage != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    tint = Color(0xFFFF6B6B),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    errorMessage ?: "Unknown error",
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF88))
                ) {
                    Text("Go Back")
                }
            }
        }
        return
    }
    var showContinuousMonitorDialog by remember { mutableStateOf(false) }
    var continuousMonitorDuration by remember { mutableStateOf(30) } // seco

    if (showContinuousMonitorDialog) {
        ContinuousMonitorDialog(
            durationSeconds = continuousMonitorDuration,
            onDismiss = {
                showContinuousMonitorDialog = false
            }
        )
    }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var selectedMonitorDuration by remember { mutableStateOf(0) }

    ContinuousMonitoringDialog(
        isVisible = showConfirmDialog,
        onDismiss = { showConfirmDialog = false },
        metricName = "Hrv",
        duration = selectedMonitorDuration,
        metricColor = Color(0xFF3B82F6),
        onConfirm = {
            continuousMonitoringViewModel.toggleHrvMonitoring(
                enabled = true
            ) { result ->
                if (result.contains("Success") || result.contains("enabled")) {
                    showSuccessDialog = true
                } else {
                    errorMessage = result
                    showErrorDialog = true
                }
            }
        }
    )

    MonitoringSuccessDialog(
        isVisible = showSuccessDialog,
        onDismiss = { showSuccessDialog = false },
        metricName = "Hrv",
        duration = selectedMonitorDuration,
        metricColor = Color(0xFF3B82F6)
    )

    MonitoringErrorDialog(
        isVisible = showErrorDialog,
        onDismiss = { showErrorDialog = false },
        errorMessage = errorMessage,
        onRetry = {
            showConfirmDialog = true
        }
    )

    // Main UI with CommonHealthMetricsScreen
    CommonHealthMetricsScreen(
        metricData = metricData,
        measurements = measurements,
        selectedDate = selectedDate,
        onDateChange = { newDate ->
            selectedDate = newDate
        },
        onBack = onBack,
        onMeasureClick = { onResult ->
            // Trigger manual HRV measurement
            instantMeasuresViewModel.measureHrvOnce { result ->
                // Parse the result string to get the HRV value
                val hrvValue = result.replace("HRV: ", "")
                    .replace(" ms", "")
                    .toIntOrNull() ?: 0

                // Return the measured value
                onResult(hrvValue)

                // Optionally refresh data after measurement
                if (hrvValue > 0) {
                    viewModel.syncHrvDataForDay(
                        offset = selectedDateOffset,
                        onSuccess = { data ->
                            hrvData = data
                        },
                        onError = { }
                    )
                }
            }
        },
        continuousMonitorConfig = ContinuousMonitorConfig(
            enabled = true,
            durations = listOf(10, 20, 30, 60)
        ),
        onStartContinuousMonitor = { durationMinutes ->
            selectedMonitorDuration = durationMinutes
            showConfirmDialog = true
        }
    )
}



