package dev.infa.page3.SDK.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import dev.infa.page3.SDK.ui.components.*
import dev.infa.page3.SDK.ui.utils.*
import dev.infa.page3.SDK.viewModel.*
import dev.infa.page3.SDK.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PressureScreen(
    onBack: () -> Unit,
    viewModel: SyncViewModel,
    instantMeasuresViewModel: InstantMeasuresViewModel,
    continuousMonitoringViewModel: ContinuousMonitoringViewModel
) {
    var selectedDate by remember { mutableStateOf(DateUtils.getCurrentDate()) }
    var pressureData by remember { mutableStateOf<PressureData?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val selectedDateOffset = remember(selectedDate) {
        DateUtils.getDayOffsetFromToday(selectedDate)
    }

    LaunchedEffect(selectedDateOffset) {
        isLoading = true
        errorMessage = null

        var callbackReturned = false
        launch {
            delay(4000)

            if (!callbackReturned) {
                pressureData = PressureData(
                    date = "",
                    entries = emptyList(),
                    averagePressure = 0f,
                    maxPressure = 0f,
                    minPressure = 0f
                )
                isLoading = false
            }
        }

        viewModel.syncPressureDataForDay(
            offset = selectedDateOffset,
            onSuccess = { data ->
                callbackReturned = true
                pressureData = data
                isLoading = false
            },
            onError = { error ->
                callbackReturned = true
                pressureData = PressureData(
                    date = "",
                    entries = emptyList(),
                    averagePressure = 0f,
                    maxPressure = 0f,
                    minPressure = 0f
                )
                isLoading = false
            }
        )
    }

    val measurements = remember(pressureData) {
        pressureData?.entries?.map { entry ->
            val time = formatMinuteOfDay(entry.minuteOfDay)
            val pressureInt = entry.pressureValue.toInt()
            val status = when {
                entry.pressureValue < 1000f -> "Low"
                entry.pressureValue > 1020f -> "High"
                else -> "Normal"
            }
            val color = when {
                entry.pressureValue < 1000f -> Color(0xFF3B82F6)
                entry.pressureValue > 1020f -> Color(0xFFFF6B6B)
                else -> Color(0xFF00FF88)
            }

            HealthMeasurement(time, pressureInt, status, color)
        } ?: emptyList()
    }

    val metricData = HealthMetricData(
        title = "Atmospheric Pressure",
        icon = Icons.Default.Compress,
        iconColor = Color(0xFF6366F1),
        unit = "hPa",
        average = pressureData?.averagePressure?.toInt() ?: 0,
        min = pressureData?.minPressure?.toInt() ?: 0,
        max = pressureData?.maxPressure?.toInt() ?: 0,
        measurementDurationSeconds = 15
    )

    if (isLoading) {
        LoadingScreen(message = "Loading Pressure data...", color = Color(0xFF6366F1))
        return
    }

    var showConfirmDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var selectedMonitorDuration by remember { mutableStateOf(0) }

    ContinuousMonitoringDialog(
        isVisible = showConfirmDialog,
        onDismiss = { showConfirmDialog = false },
        metricName = "Heart Rate",
        duration = selectedMonitorDuration,
        metricColor = Color(0xFF3B82F6),
        onConfirm = {
            continuousMonitoringViewModel.toggleHeartRateMonitoring(
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
        metricName = "Heart Rate",
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

    CommonHealthMetricsScreen(
        metricData = metricData,
        measurements = measurements,
        selectedDate = selectedDate,
        onDateChange = { newDate -> selectedDate = newDate },
        onBack = onBack,
        onMeasureClick = { onResult ->
            instantMeasuresViewModel.measurePressureOnce { result ->
                // Result format: "Pressure: 1013 hPa"
                val pressureValue = result.replace("Pressure: ", "")
                    .replace("hPa", "")
                    .trim()
                    .toIntOrNull() ?: 0

                onResult(pressureValue)

                if (pressureValue > 0) {
                    viewModel.syncPressureDataForDay(
                        offset = selectedDateOffset,
                        onSuccess = { data -> pressureData = data },
                        onError = {
                            pressureData = PressureData(
                                date = "",
                                entries = emptyList(),
                                averagePressure = 0f,
                                maxPressure = 0f,
                                minPressure = 0f
                            )
                        }
                    )
                }
            }
        },
        continuousMonitorConfig = ContinuousMonitorConfig(enabled = true),
        onStartContinuousMonitor = { durationMinutes ->
            selectedMonitorDuration = durationMinutes
            showConfirmDialog = true
        }
    )
}