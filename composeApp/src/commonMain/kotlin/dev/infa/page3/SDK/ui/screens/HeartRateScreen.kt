package dev.infa.page3.SDK.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.SDK.data.*
import dev.infa.page3.SDK.ui.components.*
import dev.infa.page3.SDK.ui.navigation.*
import dev.infa.page3.SDK.ui.utils.*
import dev.infa.page3.SDK.viewModel.*

@Composable
fun HeartRateScreen(
    navController : Navigator,
    viewModel: SyncViewModel,
    instantMeasuresViewModel: InstantMeasuresViewModel,
    continuousMonitoringViewModel: ContinuousMonitoringViewModel
) {

    // State management
    var selectedDate by remember { mutableStateOf(DateUtils.getCurrentDate()) }
    var heartRateData by remember { mutableStateOf<HeartRateData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Calculate offset from today for selected date
    val selectedDateOffset = remember(selectedDate) {
        DateUtils.getDayOffsetFromToday(selectedDate)
    }
    var showContinuousMonitorDialog by remember { mutableStateOf(false) }
    var continuousMonitorDuration by remember { mutableStateOf(30) } // seco

    LaunchedEffect(selectedDateOffset) {
        isLoading = true
        errorMessage = null

        viewModel.syncHeartRateDataForDay(
            offset = selectedDateOffset,
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
    var currentTab by remember { mutableStateOf(BottomTab.RECOVERY) }

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
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                ) {
                    Text("Go Back")
                }
            }
        }
        return
    }



    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            BottomNavBar(
                currentTab = currentTab,
                onTabSelected = { tab ->
                    currentTab = tab

                    when (tab) {
                        BottomTab.HOME -> navController.push(HomeScreenSDK())
                        BottomTab.STRAIN -> navController.push(ExerciseScreenSDK())
                        BottomTab.RECOVERY -> navController.replace(HeartRateScreenSDK())
                        BottomTab.STEP -> navController.push(StepsScreenSDK())
                        BottomTab.PROFILE -> navController.push(ProfileScreenSDK())
                    }
                }
            )
        }
    ) { padding ->

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


        // Main UI with CommonHealthMetricsScreen
        CommonHealthMetricsScreen(
            metricData = metricData,
            measurements = measurements,
            selectedDate = selectedDate,
            onDateChange = { newDate ->
                selectedDate = newDate
            },
            onBack = {},
            onMeasureClick = { onResult ->
                // Trigger manual heart rate measurement
                instantMeasuresViewModel.measureHeartRateOnce { result ->
                    // Parse the result string to get the heart rate value
                    val hrValue = result.replace("Heart Rate: ", "")
                        .replace(" bpm", "")
                        .toIntOrNull() ?: 0

                    // Return the measured value
                    onResult(hrValue)

                    // Optionally refresh data after measurement
                    if (hrValue > 0) {
                        viewModel.syncHeartRateDataForDay(
                            offset = selectedDateOffset,
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
                selectedMonitorDuration = durationMinutes
                showConfirmDialog = true
            }
        )
    }
}



