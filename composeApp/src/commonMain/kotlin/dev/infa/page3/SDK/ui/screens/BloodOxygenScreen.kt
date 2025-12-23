package dev.infa.page3.SDK.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.infa.page3.SDK.data.SpO2Data
import dev.infa.page3.SDK.ui.components.CommonHealthMetricsScreen
import dev.infa.page3.SDK.ui.components.ContinuousMonitorConfig
import dev.infa.page3.SDK.ui.components.ContinuousMonitoringDialog
import dev.infa.page3.SDK.ui.components.HealthMeasurement
import dev.infa.page3.SDK.ui.components.HealthMetricData
import dev.infa.page3.SDK.ui.components.LoadingScreen
import dev.infa.page3.SDK.ui.components.MonitoringErrorDialog
import dev.infa.page3.SDK.ui.components.MonitoringSuccessDialog
import dev.infa.page3.SDK.ui.utils.DateUtils
import dev.infa.page3.SDK.ui.utils.formatTimestamp
import dev.infa.page3.SDK.viewModel.ContinuousMonitoringViewModel
import dev.infa.page3.SDK.viewModel.InstantMeasuresViewModel
import dev.infa.page3.SDK.viewModel.SyncViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BloodOxygenScreen(
    onBack: () -> Unit,
    viewModel: SyncViewModel,
    instantMeasuresViewModel: InstantMeasuresViewModel,
    continuousMonitoringViewModel: ContinuousMonitoringViewModel
) {
    var selectedDate by remember { mutableStateOf(DateUtils.getCurrentDate()) }
    var spo2Data by remember { mutableStateOf<SpO2Data?>(null) }
    var isLoading by remember { mutableStateOf(false) }   // ✅ DEFAULT FALSE
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

                spo2Data = SpO2Data(
                    date = "",
                    spo2Values = emptyList(),
                    averageSpO2 = 0,
                    maxSpO2 = 0,
                    minSpO2 = 0
                )

                isLoading = false
            }
        }

//        viewModel.syncSpO2DataForDay(
//            offset = selectedDateOffset,
//
//            onSuccess = { data ->
//                callbackReturned = true
//                spo2Data = data
//                isLoading = false
//            },
//
//            onError = { error ->
//                callbackReturned = true
//
//                spo2Data = SpO2Data(
//                    date = "",
//                    spo2Values = emptyList(),
//                    averageSpO2 = 0,
//                    maxSpO2 = 0,
//                    minSpO2 = 0
//                )
//
//                isLoading = false
//            }
//        )
    }

    // ✅ SAFE LAUNCH — NEVER STUCK



    // ✅ SAFE MEASUREMENTS (EMPTY → ZERO)
    val measurements = remember(spo2Data) {
        spo2Data?.spo2Values?.map { entry ->
            val time = formatTimestamp(entry.timestamp)
            val status = when {
                entry.spo2Value < 90 -> "Low"
                entry.spo2Value < 95 -> "Normal"
                else -> "Excellent"
            }
            val color = when {
                entry.spo2Value < 90 -> Color(0xFFFF6B6B)
                entry.spo2Value < 95 -> Color(0xFF3B82F6)
                else -> Color(0xFF00FF88)
            }

            HealthMeasurement(time, entry.spo2Value, status, color)

        } ?: emptyList()
    }

    // ✅ SAFE METRIC DATA (NULL → ZERO)
    val metricData = HealthMetricData(
        title = "Blood Oxygen (SpO₂)",
        icon = Icons.Default.Air,
        iconColor = Color(0xFF3B82F6),
        unit = "%",
        average = spo2Data?.averageSpO2 ?: 0,
        min = spo2Data?.minSpO2 ?: 0,
        max = spo2Data?.maxSpO2 ?: 0,
        measurementDurationSeconds = 30
    )

    // ✅ ONLY SHOW LOADING WHILE ACTUALLY LOADING
    if (isLoading) {
        LoadingScreen(message = "Loading SpO₂ data...", color = Color(0xFF3B82F6))
        return
    }


    var showConfirmDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var selectedMonitorDuration by remember { mutableStateOf(0) }

    ContinuousMonitoringDialog(
        isVisible = showConfirmDialog,
        onDismiss = { showConfirmDialog = false },
        metricName = "Blood Oxygen",
        duration = selectedMonitorDuration,
        metricColor = Color(0xFF3B82F6),
        onConfirm = {
            continuousMonitoringViewModel.toggleSpO2Monitoring(
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
        metricName = "Blood Oxygen",
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

    // ✅ NEVER BLOCK UI WITH ERROR
    CommonHealthMetricsScreen(
        metricData = metricData,
        measurements = measurements,
        selectedDate = selectedDate,
        onDateChange = { newDate -> selectedDate = newDate },
        onBack = onBack,

        onMeasureClick = { onResult ->
            instantMeasuresViewModel.measureSpO2Once { result ->
                val spo2Value = result.replace("SpO2: ", "")
                    .replace("%", "")
                    .toIntOrNull() ?: 0

                onResult(spo2Value)

                if (spo2Value > 0) {
                    viewModel.syncSpO2DataForDay(
                        offset = selectedDateOffset,
                        onSuccess = { data -> spo2Data = data },
                        onError = {
                            // ✅ FALLBACK ZERO AFTER MEASURE FAIL
                            spo2Data = SpO2Data(
                                date = "",
                                spo2Values = emptyList(),
                                averageSpO2 = 0,
                                maxSpO2 = 0,
                                minSpO2 = 0
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


