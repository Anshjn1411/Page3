package dev.infa.page3.ui.screens

import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.infa.page3.ui.components.CommonHealthMetricsScreen
import dev.infa.page3.ui.components.ContinuousMonitorConfig
import dev.infa.page3.ui.components.ErrorScreen
import dev.infa.page3.ui.components.ErrorScreenSDK
import dev.infa.page3.ui.components.HealthMeasurement
import dev.infa.page3.ui.components.HealthMetricData
import dev.infa.page3.ui.components.LoadingScreen
import dev.infa.page3.ui.navigation.HealthMetricsViewModelFactory
import dev.infa.page3.viewmodels.BloodPressureViewModel
import dev.infa.page3.viewmodels.BpData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


@Composable
fun BloodPressureScreen(
    onBack: () -> Unit,
    viewModel: BloodPressureViewModel
) {
    var selectedDate by remember { mutableStateOf(Date()) }
    var bpData by remember { mutableStateOf<BpData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val dateOffset = remember(selectedDate) {
        val today = Calendar.getInstance()
        val selected = Calendar.getInstance().apply { time = selectedDate }
        val diffInMillis = today.timeInMillis - selected.timeInMillis
        (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
    }

    LaunchedEffect(dateOffset) {
        isLoading = true
        errorMessage = null

        var callbackReturned = false

        // ✅ HARD TIMEOUT (4 SECONDS)
        launch {
            delay(4000)

            if (!callbackReturned) {
                Log.e("BpScreen", "BLE TIMEOUT → Forcing ZERO BP UI")

                isLoading = false
            }
        }

        viewModel.syncBpDataForDay(
            offset = dateOffset,

            onSuccess = { data ->
                callbackReturned = true
                bpData = data
                isLoading = false

                Log.d("BpScreen", "BP Data Loaded Successfully")
            },

            onError = { error ->
                callbackReturned = true
                Log.e("BpScreen", "BLE Error: $error")

                isLoading = false
            }
        )
    }


    val measurements = remember(bpData) {
        bpData?.bpValues?.map { entry ->
            val time = formatTimestamp(entry.timestamp)
            val status = when {
                entry.systolic >= 140 || entry.diastolic >= 90 -> "High"
                entry.systolic >= 130 || entry.diastolic >= 80 -> "Elevated"
                else -> "Normal"
            }
            val color = when {
                entry.systolic >= 140 || entry.diastolic >= 90 -> Color(0xFFFF6B6B)
                entry.systolic >= 130 || entry.diastolic >= 80 -> Color(0xFFFFAA00)
                else -> Color(0xFF00FF88)
            }
            HealthMeasurement(time, entry.systolic, status, color)
        } ?: emptyList()
    }

    val metricData = HealthMetricData(
        title = "Blood Pressure",
        icon = Icons.Default.MonitorHeart,
        iconColor = Color(0xFFFF6B6B),
        unit = "mmHg",
        average = bpData?.averageSystolic ?: 0,
        min = bpData?.minSystolic ?: 0,
        max = bpData?.maxSystolic ?: 0,
        measurementDurationSeconds = 30
    )

    if (isLoading) {
        LoadingScreen(message = "Loading BP data...", color = Color(0xFFFF6B6B))
        return
    }

    if (errorMessage != null) {
        ErrorScreenSDK(message = errorMessage ?: "Unknown error", onBack = onBack)
        return
    }

    CommonHealthMetricsScreen(
        metricData = metricData,
        measurements = measurements,
        selectedDate = selectedDate,
        onDateChange = { newDate -> selectedDate = newDate },
        onBack = onBack,
        onMeasureClick = { onResult ->
            viewModel.measureBpOnce { result ->
                val bpValue = result.replace("BP: ", "")
                    .split("/")
                    .firstOrNull()
                    ?.replace(" mmHg", "")
                    ?.toIntOrNull() ?: 0
                onResult(bpValue)

                if (bpValue > 0) {
                    viewModel.syncBpDataForDay(
                        offset = dateOffset,
                        onSuccess = { data -> bpData = data },
                        onError = { }
                    )
                }
            }
        },
        continuousMonitorConfig = ContinuousMonitorConfig(enabled = false),
        onStartContinuousMonitor = null
    )
}

