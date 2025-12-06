package dev.infa.page3.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import dev.infa.page3.viewmodels.BloodOxygenViewModel
import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.infa.page3.ui.components.CommonHealthMetricsScreen
import dev.infa.page3.ui.components.ContinuousMonitorConfig
import dev.infa.page3.ui.components.HealthMeasurement
import dev.infa.page3.ui.components.HealthMetricData
import dev.infa.page3.ui.components.LoadingScreen
import dev.infa.page3.ui.navigation.HealthMetricsViewModelFactory
import dev.infa.page3.viewmodels.SpO2Data
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

@Composable
fun BloodOxygenScreen(
    onBack: () -> Unit,
    viewModel: BloodOxygenViewModel
) {
    var selectedDate by remember { mutableStateOf(Date()) }
    var spo2Data by remember { mutableStateOf<SpO2Data?>(null) }
    var isLoading by remember { mutableStateOf(false) }   // ✅ DEFAULT FALSE
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val dateOffset = remember(selectedDate) {
        val today = Calendar.getInstance()
        val selected = Calendar.getInstance().apply { time = selectedDate }
        val diffInMillis = today.timeInMillis - selected.timeInMillis
        (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
    }

    // ✅ SAFE LAUNCH — NEVER STUCK
    LaunchedEffect(dateOffset) {
        isLoading = true
        errorMessage = null

        var callbackReturned = false
        launch {
            delay(4000)

            if (!callbackReturned) {
                Log.e("SpO2Screen", "BLE TIMEOUT → Forcing ZERO UI")

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

        viewModel.syncSpO2DataForDay(
            offset = dateOffset,

            onSuccess = { data ->
                callbackReturned = true
                spo2Data = data
                isLoading = false

                Log.d("SpO2Screen", "Data Loaded Successfully")
            },

            onError = { error ->
                callbackReturned = true

                Log.e("SpO2Screen", "BLE Error: $error")

                spo2Data = SpO2Data(
                    date = "",
                    spo2Values = emptyList(),
                    averageSpO2 = 0,
                    maxSpO2 = 0,
                    minSpO2 = 0
                )

                isLoading = false
            }
        )
    }


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

    // ✅ NEVER BLOCK UI WITH ERROR
    CommonHealthMetricsScreen(
        metricData = metricData,
        measurements = measurements,
        selectedDate = selectedDate,
        onDateChange = { newDate -> selectedDate = newDate },
        onBack = onBack,

        onMeasureClick = { onResult ->
            viewModel.measureSpO2Once { result ->
                val spo2Value = result.replace("SpO2: ", "")
                    .replace("%", "")
                    .toIntOrNull() ?: 0

                onResult(spo2Value)

                if (spo2Value > 0) {
                    viewModel.syncSpO2DataForDay(
                        offset = dateOffset,
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
            viewModel.toggleSpO2Monitoring(enabled = true) {
                Log.d("SpO2Screen", "Monitoring started for $durationMinutes minutes")
            }
        }
    )
}


