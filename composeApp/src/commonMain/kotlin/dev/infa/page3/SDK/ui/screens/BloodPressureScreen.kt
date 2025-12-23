package dev.infa.page3.SDK.ui.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.SDK.data.DayStepData
import dev.infa.page3.SDK.ui.components.*
import dev.infa.page3.SDK.ui.navigation.*
import dev.infa.page3.SDK.ui.utils.*
import dev.infa.page3.SDK.viewModel.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import dev.infa.page3.SDK.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin
// ========================================
// BLOOD PRESSURE SCREEN
// ========================================
@Composable
fun BloodPressureScreen(
    onBack: () -> Unit,
    viewModel: SyncViewModel,
    instantMeasuresViewModel: InstantMeasuresViewModel,
    continuousMonitoringViewModel: ContinuousMonitoringViewModel
) {
    var selectedDate by remember { mutableStateOf(DateUtils.getCurrentDate()) }
    var bpData by remember { mutableStateOf<BpData?>(null) }
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
                bpData = BpData(
                    date = "",
                    bpValues = emptyList(),
                    averageSystolic = 0,
                    averageDiastolic = 0,
                    maxSystolic = 0,
                    minSystolic = 0
                )
                isLoading = false
            }
        }

//        viewModel.syncAutoBloodPressureForDay(
//            offset = selectedDateOffset,
//            onSuccess = { data ->
//                callbackReturned = true
//                bpData = data
//                isLoading = false
//            },
//            onError = { error ->
//                callbackReturned = true
//                bpData = BpData(
//                    date = "",
//                    bpValues = emptyList(),
//                    averageSystolic = 0,
//                    averageDiastolic = 0,
//                    maxSystolic = 0,
//                    minSystolic = 0
//                )
//                isLoading = false
//            }
//        )
    }

    val measurements = remember(bpData) {
        bpData?.bpValues?.map { entry ->
            val time = formatTimestamp(entry.timestamp)
            val status = when {
                entry.systolic >= 140 || entry.diastolic >= 90 -> "High"
                entry.systolic >= 130 || entry.diastolic >= 80 -> "Elevated"
                entry.systolic < 90 || entry.diastolic < 60 -> "Low"
                else -> "Normal"
            }
            val color = when {
                entry.systolic >= 140 || entry.diastolic >= 90 -> Color(0xFFFF6B6B)
                entry.systolic >= 130 || entry.diastolic >= 80 -> Color(0xFFFFA500)
                entry.systolic < 90 || entry.diastolic < 60 -> Color(0xFF3B82F6)
                else -> Color(0xFF00FF88)
            }

            HealthMeasurement(time, entry.systolic, status, color)
        } ?: emptyList()
    }

    val metricData = HealthMetricData(
        title = "Blood Pressure",
        icon = Icons.Default.Favorite,
        iconColor = Color(0xFFFF6B6B),
        unit = "mmHg",
        average = bpData?.averageSystolic ?: 0,
        min = bpData?.minSystolic ?: 0,
        max = bpData?.maxSystolic ?: 0,
        measurementDurationSeconds = 45
    )

    if (isLoading) {
        LoadingScreen(message = "Loading Blood Pressure data...", color = Color(0xFFFF6B6B))
        return
    }

    CommonHealthMetricsScreen(
        metricData = metricData,
        measurements = measurements,
        selectedDate = selectedDate,
        onDateChange = { newDate -> selectedDate = newDate },
        onBack = onBack,
        onMeasureClick = { onResult ->
            instantMeasuresViewModel.measureBloodPressureOnce { result ->
                // Result format: "BP: 120/80 mmHg, HR: 75 bpm"
                val systolicValue = result.substringAfter("BP: ")
                    .substringBefore("/")
                    .trim()
                    .toIntOrNull() ?: 0

                onResult(systolicValue)

                if (systolicValue > 0) {
//                    viewModel.syncAutoBloodPressureForDay(
//                        offset = selectedDateOffset,
//                        onSuccess = { data -> bpData = data },
//                        onError = {
//                            bpData = BpData(
//                                date = "",
//                                bpValues = emptyList(),
//                                averageSystolic = 0,
//                                averageDiastolic = 0,
//                                maxSystolic = 0,
//                                minSystolic = 0
//                            )
//                        }
//                    )
                }
            }
        },
        continuousMonitorConfig = ContinuousMonitorConfig(enabled = true),
        onStartContinuousMonitor = { durationMinutes ->
            val startEndTime = StartEndTimeEntity(
                startHour = 8,
                startMinute = 0,
                endHour = 22,
                endMinute = 0
            )
            continuousMonitoringViewModel.toggleBloodPressureMonitoring(
                enabled = true,
                startEndTime = startEndTime,
                interval = durationMinutes
            ) { }
        }
    )
}

// ========================================
// TEMPERATURE SCREEN
// ========================================


// ========================================
// PRESSURE SCREEN (Atmospheric/Barometric)
// ========================================


// ========================================
// STRESS/FATIGUE SCREEN
// ========================================


@Composable
fun LoadingScreen(message: String, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(60.dp),
                color = color,
                strokeWidth = 6.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}