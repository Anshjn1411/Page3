package dev.infa.page3.SDK.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import dev.infa.page3.SDK.ui.components.*
import dev.infa.page3.SDK.ui.utils.*
import dev.infa.page3.SDK.viewModel.*
import dev.infa.page3.SDK.data.*
import dev.infa.page3.SDK.ui.navigation.ExerciseScreenSDK
import dev.infa.page3.SDK.ui.navigation.HeartRateScreenSDK
import dev.infa.page3.SDK.ui.navigation.HomeScreenSDK
import dev.infa.page3.SDK.ui.navigation.ProfileScreenSDK
import dev.infa.page3.SDK.ui.navigation.StepsScreenSDK
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TemperatureScreen(
    onBack: () -> Unit,
    viewModel: SyncViewModel,
    instantMeasuresViewModel: InstantMeasuresViewModel,
    continuousMonitoringViewModel: ContinuousMonitoringViewModel
) {
    var selectedDate by remember { mutableStateOf(DateUtils.getCurrentDate()) }
    var tempData by remember { mutableStateOf<TemperatureData?>(null) }
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
                tempData = TemperatureData(
                    date = "",
                    entries = emptyList(),
                    averageTemp = 0f,
                    maxTemp = 0f,
                    minTemp = 0f
                )
                isLoading = false
            }
        }

        viewModel.syncAutoTemperatureForDay(
            offset = selectedDateOffset,
            onSuccess = { data ->
                callbackReturned = true
                tempData = data
                isLoading = false
            },
            onError = { error ->
                callbackReturned = true
                tempData = TemperatureData(
                    date = "",
                    entries = emptyList(),
                    averageTemp = 0f,
                    maxTemp = 0f,
                    minTemp = 0f
                )
                isLoading = false
            }
        )
    }

    val measurements = remember(tempData) {
        tempData?.entries?.map { entry ->
            val time = formatMinuteOfDay(entry.minuteOfDay)
            val tempInt = entry.temperature.toInt()
            val status = when {
                entry.temperature < 36.1f -> "Low"
                entry.temperature > 37.2f -> "High"
                else -> "Normal"
            }
            val color = when {
                entry.temperature < 36.1f -> Color(0xFF3B82F6)
                entry.temperature > 37.2f -> Color(0xFFFF6B6B)
                else -> Color(0xFF00FF88)
            }

            HealthMeasurement(time, tempInt, status, color)
        } ?: emptyList()
    }

    val metricData = HealthMetricData(
        title = "Body Temperature",
        icon = Icons.Default.Thermostat,
        iconColor = Color(0xFFFFA500),
        unit = "°C",
        average = tempData?.averageTemp?.toInt() ?: 0,
        min = tempData?.minTemp?.toInt() ?: 0,
        max = tempData?.maxTemp?.toInt() ?: 0,
        measurementDurationSeconds = 30
    )

    if (isLoading) {
        LoadingScreen(message = "Loading Temperature data...", color = Color(0xFFFFA500))
        return
    }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var selectedMonitorDuration by remember { mutableStateOf(0) }
    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CommonTopAppBar(metricData.title, { onBack() })
        }
    ) { padding ->

        ContinuousMonitoringDialog(
            isVisible = showConfirmDialog,
            onDismiss = { showConfirmDialog = false },
            metricName = "Temperature",
            duration = selectedMonitorDuration,
            metricColor = Color(0xFF3B82F6),
            onConfirm = {
                continuousMonitoringViewModel.toggleTemperatureMonitoring(
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
            metricName = "Temperature",
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
                instantMeasuresViewModel.measureTemperatureOnce { result ->
                    // Result format: "Temperature: 36.5°C"
                    val tempValue = result.replace("Temperature: ", "")
                        .replace("°C", "")
                        .trim()
                        .toFloatOrNull()?.toInt() ?: 0

                    onResult(tempValue)

                    if (tempValue > 0) {
                        viewModel.syncAutoTemperatureForDay(
                            offset = selectedDateOffset,
                            onSuccess = { data -> tempData = data },
                            onError = {
                                tempData = TemperatureData(
                                    date = "",
                                    entries = emptyList(),
                                    averageTemp = 0f,
                                    maxTemp = 0f,
                                    minTemp = 0f
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
}