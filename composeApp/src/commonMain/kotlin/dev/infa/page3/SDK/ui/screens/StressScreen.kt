package dev.infa.page3.SDK.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import dev.infa.page3.SDK.data.BpData
import dev.infa.page3.SDK.data.HrvData
import dev.infa.page3.SDK.ui.components.CommonHealthMetricsScreen
import dev.infa.page3.SDK.ui.components.ContinuousMonitorConfig
import dev.infa.page3.SDK.ui.components.ContinuousMonitoringDialog
import dev.infa.page3.SDK.ui.components.ErrorScreenSDK
import dev.infa.page3.SDK.ui.components.HealthMeasurement
import dev.infa.page3.SDK.ui.components.HealthMetricData
import dev.infa.page3.SDK.ui.components.MonitoringErrorDialog
import dev.infa.page3.SDK.ui.components.MonitoringSuccessDialog
import dev.infa.page3.SDK.ui.utils.DateUtils
import dev.infa.page3.SDK.ui.utils.formatTimestamp
import dev.infa.page3.SDK.viewModel.InstantMeasuresViewModel
import dev.infa.page3.SDK.viewModel.SyncViewModel
import dev.infa.page3.ui.components.LoadingScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun StressScreen(
    onBack: () -> Unit,
    viewModel: SyncViewModel,
    instantMeasuresViewModel: InstantMeasuresViewModel
) {
    var selectedDate by remember { mutableStateOf(DateUtils.getCurrentDate()) }
    var stressData by remember { mutableStateOf<HrvData?>(null) }
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
                stressData = HrvData(
                    date = "",
                    hrvValues = emptyList(),
                    averageHrv = 0,
                    maxHrv = 0,
                    minHrv = 0
                )
                isLoading = false
            }
        }

        viewModel.syncHrvDataForDay(
            offset = selectedDateOffset,
            onSuccess = { data ->
                callbackReturned = true
                stressData = data
                isLoading = false
            },
            onError = { error ->
                callbackReturned = true
                stressData = HrvData(
                    date = "",
                    hrvValues = emptyList(),
                    averageHrv = 0,
                    maxHrv = 0,
                    minHrv = 0
                )
                isLoading = false
            }
        )
    }

    // Convert HRV to Stress Level (inverse relationship)
    val measurements = remember(stressData) {
        stressData?.hrvValues?.map { entry ->
            val time = formatTimestamp(entry.timestamp)
            // Higher HRV = Lower Stress
            val stressLevel = when {
                entry.hrvValue > 80 -> 20  // Low stress
                entry.hrvValue > 60 -> 40  // Moderate stress
                entry.hrvValue > 40 -> 60  // Medium stress
                entry.hrvValue > 20 -> 80  // High stress
                else -> 100  // Very high stress
            }
            val status = when {
                stressLevel < 30 -> "Relaxed"
                stressLevel < 50 -> "Normal"
                stressLevel < 70 -> "Elevated"
                else -> "High"
            }
            val color = when {
                stressLevel < 30 -> Color(0xFF00FF88)
                stressLevel < 50 -> Color(0xFF3B82F6)
                stressLevel < 70 -> Color(0xFFFFA500)
                else -> Color(0xFFFF6B6B)
            }

            HealthMeasurement(time, stressLevel, status, color)
        } ?: emptyList()
    }

    // Calculate average stress from HRV
    val avgStress = if (stressData?.averageHrv != 0) {
        when {
            stressData?.averageHrv ?: 0 > 80 -> 20
            stressData?.averageHrv ?: 0 > 60 -> 40
            stressData?.averageHrv ?: 0 > 40 -> 60
            stressData?.averageHrv ?: 0 > 20 -> 80
            else -> 100
        }
    } else 0

    val metricData = HealthMetricData(
        title = "Stress Level",
        icon = Icons.Default.Psychology,
        iconColor = Color(0xFFFF6B6B),
        unit = "Level",
        average = avgStress,
        min = 0,
        max = 100,
        measurementDurationSeconds = 60
    )

    if (isLoading) {
        LoadingScreen(message = "Loading Stress data...", color = Color(0xFFFF6B6B))
        return
    }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var selectedMonitorDuration by remember { mutableStateOf(0) }

    CommonHealthMetricsScreen(
        metricData = metricData,
        measurements = measurements,
        selectedDate = selectedDate,
        onDateChange = { newDate -> selectedDate = newDate },
        onBack = onBack,
        onMeasureClick = { onResult ->
            instantMeasuresViewModel.measureHrvOnce { result ->
                // Result format: "HRV: 65 ms"
                val hrvValue = result.replace("HRV: ", "")
                    .replace("ms", "")
                    .trim()
                    .toIntOrNull() ?: 0

                // Convert HRV to stress
                val stressValue = when {
                    hrvValue > 80 -> 20
                    hrvValue > 60 -> 40
                    hrvValue > 40 -> 60
                    hrvValue > 20 -> 80
                    else -> 100
                }

                onResult(stressValue)

                if (hrvValue > 0) {
                    viewModel.syncHrvDataForDay(
                        offset = selectedDateOffset,
                        onSuccess = { data -> stressData = data },
                        onError = {
                            stressData = HrvData(
                                date = "",
                                hrvValues = emptyList(),
                                averageHrv = 0,
                                maxHrv = 0,
                                minHrv = 0
                            )
                        }
                    )
                }
            }
        },
        continuousMonitorConfig = ContinuousMonitorConfig(enabled = false)
    )
}

