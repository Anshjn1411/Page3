package dev.infa.page3.ui.screens


import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModel
import dev.infa.page3.ui.components.CommonHealthMetricsScreen
import dev.infa.page3.ui.components.ContinuousMonitorConfig
import dev.infa.page3.ui.components.HealthMeasurement
import dev.infa.page3.ui.components.HealthMetricData
import dev.infa.page3.ui.navigation.HealthMetricsViewModelFactory
import dev.infa.page3.viewmodels.BloodOxygenViewModel
import dev.infa.page3.viewmodels.BloodPressureViewModel
import dev.infa.page3.viewmodels.HealthMetricsCacheManager
import dev.infa.page3.viewmodels.HeartRateData
import dev.infa.page3.viewmodels.HeartRateViewModel
import dev.infa.page3.viewmodels.HrvViewModel
import java.text.SimpleDateFormat
import java.util.*


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HeartRateScreen(
    onBack: () -> Unit,
    viewModel: HeartRateViewModel
) {

    // State management
    var selectedDate by remember { mutableStateOf(Date()) }
    var heartRateData by remember { mutableStateOf<HeartRateData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Calculate offset from today for selected date
    val dateOffset = remember(selectedDate) {
        val today = Calendar.getInstance()
        val selected = Calendar.getInstance().apply { time = selectedDate }
        val diffInMillis = today.timeInMillis - selected.timeInMillis
        (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
    }

    // Load heart rate data when date changes
    LaunchedEffect(dateOffset) {
        isLoading = true
        errorMessage = null

        viewModel.syncHeartRateDataForDay(
            offset = dateOffset,
            onSuccess = { data ->
                heartRateData = data
                isLoading = false
            },
            onError = { error ->
                errorMessage = error
                isLoading = false
            }
        )
    }

    // Convert HeartRateData to measurements for the chart
    val measurements = remember(heartRateData) {
        heartRateData?.heartRateValues?.map { entry ->
            val time = formatTimestamp(entry.timestamp)
            val status = when {
                entry.heartRate < 60 -> "Low"
                entry.heartRate > 100 -> "High"
                else -> "Normal"
            }
            val color = when {
                entry.heartRate < 60 -> Color(0xFF3B82F6)
                entry.heartRate > 100 -> Color(0xFFFF6B6B)
                else -> Color(0xFF00FF88)
            }
            HealthMeasurement(time, entry.heartRate, status, color)
        } ?: emptyList()
    }

    // Prepare metric data
    val metricData = HealthMetricData(
        title = "Heart Rate",
        icon = Icons.Default.Favorite,
        iconColor = Color(0xFF3B82F6),
        unit = "BPM",
        average = heartRateData?.averageHeartRate ?: 0,
        min = heartRateData?.minHeartRate ?: 0,
        max = heartRateData?.maxHeartRate ?: 0,
        measurementDurationSeconds = 45
    )

    // Show loading or error state
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color(0xFF3B82F6))
                Spacer(Modifier.height(16.dp))
                Text("Loading heart rate data...", color = Color.White)
            }
        }
        return
    }

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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                ) {
                    Text("Go Back")
                }
            }
        }
        return
    }

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
            // Trigger manual heart rate measurement
            viewModel.measureHeartRateOnce { result ->
                // Parse the result string to get the heart rate value
                val hrValue = result.replace("Heart Rate: ", "")
                    .replace(" bpm", "")
                    .toIntOrNull() ?: 0

                // Return the measured value
                onResult(hrValue)

                // Optionally refresh data after measurement
                if (hrValue > 0) {
                    viewModel.syncHeartRateDataForDay(
                        offset = dateOffset,
                        onSuccess = { data ->
                            heartRateData = data
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
            val intervalSeconds = durationMinutes * 60
            viewModel.toggleHeartRateInline (
                enabled = true,
                interval = intervalSeconds
            )
        }
    )
}

// Helper function to format timestamp
fun formatTimestamp(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = Date(timestamp * 1000) // Convert seconds to milliseconds
        sdf.format(date)
    } catch (e: Exception) {
        "Invalid"
    }
}


