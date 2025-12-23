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
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    connectionViewModel: ConnectionViewModel,
    homeViewModel: HomeViewModel,
    syncViewModel: SyncViewModel,
    navController: Navigator
) {
    val uiState by connectionViewModel.uiState.collectAsState()
    val isConnected = uiState.isConnected
    val deviceName = uiState.connectedDevice?.name ?: ""

    val todaySteps by syncViewModel.todaySteps.collectAsState()
    val isSyncing by syncViewModel.isSyncing.collectAsState()
    val heartData by syncViewModel.heartRateData.collectAsState()
    val hrvData by syncViewModel.hrvData.collectAsState()
    val spo2Data by syncViewModel.spo2Data.collectAsState()
    val tempData by syncViewModel.temperatureData.collectAsState()
    val pressureData by syncViewModel.pressureData.collectAsState()
    val autoSyncCompleted by syncViewModel.autoSyncCompleted.collectAsState()

    val stepGoal by homeViewModel.stepGoal.collectAsState()
    val homeSleepData by homeViewModel.todaySleep.collectAsState()
    val batteryLevel by homeViewModel.batteryValue.collectAsState()
    val deviceCapabilities by homeViewModel.deviceCapabilities.collectAsState()

    var selectedDate by remember { mutableStateOf(DateUtils.getCurrentDate()) }
    val selectedDateOffset = remember(selectedDate) {
        DateUtils.getDayOffsetFromToday(selectedDate)
    }

    var currentTab by remember { mutableStateOf(BottomTab.HOME) }

    LaunchedEffect(isConnected) {
        if(isConnected){
            homeViewModel.getBatteryLevel()
            homeViewModel.fetchDeviceCapabilities()
            if(!autoSyncCompleted){
                syncViewModel.startAutoSync()
            }
        }
    }

    // When user changes date, fetch that specific day's data
    LaunchedEffect(selectedDateOffset, isConnected, autoSyncCompleted) {
        if (isConnected && autoSyncCompleted) {
            syncViewModel.fetchStepsByOffset(selectedDateOffset)

            if (deviceCapabilities?.hasHeartRate == true) {
                syncViewModel.syncHeartRateDataForDay(selectedDateOffset, {}, {})
            }

            if (deviceCapabilities?.hasSpO2 == true) {
                syncViewModel.syncSpO2DataForDay(selectedDateOffset, {}, {})
            }

            if (deviceCapabilities?.hasHRV == true) {
                syncViewModel.syncHrvDataForDay(selectedDateOffset, {}, {})
            }

            if (deviceCapabilities?.hasBodyTemperature == true) {
                syncViewModel.syncAutoTemperatureForDay(selectedDateOffset, {}, {})
            }

            if (deviceCapabilities?.hasBloodPressure == true) {
                syncViewModel.syncPressureDataForDay(selectedDateOffset, {}, {})
            }
        }
    }

    // ========================================
    // CREATE METRICS BASED ON CAPABILITIES
    // ========================================

    val heartMetric = if (deviceCapabilities?.hasHeartRate == true) {
        MetricSummary(
            title = "Heart Rate",
            unit = "BPM",
            min = heartData?.minHeartRate ?: 0,
            avg = heartData?.averageHeartRate ?: 0,
            max = heartData?.maxHeartRate ?: 0,
            color = Red
        )
    } else null

    val hrvMetric = if (deviceCapabilities?.hasHRV == true) {
        MetricSummary(
            title = "HRV",
            unit = "ms",
            min = hrvData?.minHrv ?: 0,
            avg = hrvData?.averageHrv ?: 0,
            max = hrvData?.maxHrv ?: 0,
            color = Blue
        )
    } else null

    val spo2Metric = if (deviceCapabilities?.hasSpO2 == true) {
        MetricSummary(
            title = "Blood Oxygen",
            unit = "%",
            min = spo2Data?.minSpO2 ?: 0,
            avg = spo2Data?.averageSpO2 ?: 0,
            max = spo2Data?.maxSpO2 ?: 0,
            color = Indigo
        )
    } else null

    val stressMetric = if (deviceCapabilities?.hasHRV == true) {
        MetricSummary(
            title = "Stress",
            unit = "Level",
            min = 0,
            avg = calculateStressFromHrv(hrvData?.averageHrv ?: 0),
            max = 100,
            color = Amber
        )
    } else null

    val temperatureMetric = if (deviceCapabilities?.hasBodyTemperature == true) {
        MetricSummary(
            title = "Temperature",
            unit = "°C",
            min = tempData?.minTemp?.toInt() ?: 0,
            avg = tempData?.averageTemp?.toInt() ?: 0,
            max = tempData?.maxTemp?.toInt() ?: 0,
            color = Orange
        )
    } else null

    val pressureMetric = if (deviceCapabilities?.hasBloodPressure == true) {
        MetricSummary(
            title = "Pressure",
            unit = "hPa",
            min = pressureData?.minPressure?.toInt() ?: 0,
            avg = pressureData?.averagePressure?.toInt() ?: 0,
            max = pressureData?.maxPressure?.toInt() ?: 0,
            color = Purple
        )
    } else null

    // Blood Pressure metric (if device supports it)
    val bloodPressureMetric = if (deviceCapabilities?.hasBloodPressure == true) {
        MetricSummary(
            title = "Blood Pressure",
            unit = "mmHg",
            min = 0, // You'll need to add bpData state flow
            avg = 0,
            max = 0,
            color = Red
        )
    } else null

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            DashboardTopBar(
                isConnected = isConnected,
                deviceName = deviceName,
                batteryLevel = batteryLevel ?: 0
            )
        },
        bottomBar = {
            BottomNavBar(
                currentTab = currentTab,
                onTabSelected = { tab ->
                    currentTab = tab

                    when (tab) {
                        BottomTab.HOME -> navController.replace(HomeScreenSDK())
                        BottomTab.STRAIN -> navController.push(ExerciseScreenSDK())
                        BottomTab.RECOVERY -> navController.push(HeartRateScreenSDK())
                        BottomTab.STEP -> navController.push(StepsScreenSDK())
                        BottomTab.PROFILE -> navController.push(ProfileScreenSDK())
                    }
                }
            )
        }
    ) { padding ->

        if (!isConnected) {
            DeviceDisconnectedState(
                onRetry = {
                    navController.push(ProfileScreenSDK())
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                item {
                    DateSelector(
                        selectedDate = selectedDate,
                        onDateChange = { newDate ->
                            selectedDate = newDate
                        }
                    )
                }

                item {
                    val currentStepData = DayStepData(
                        totalSteps = todaySteps,
                    )

                    ProgressTripleRow(
                        stepData = currentStepData,
                        stepGoal = stepGoal,
                        sleepData = homeSleepData,
                        isLoadingSteps = isSyncing,
                        navController = navController
                    )
                }

                // ========================================
                // CONDITIONAL HEALTH METRICS DISPLAY
                // ========================================
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                        // Heart Rate
                        heartMetric?.let { metric ->
                            MetricSectionCard(metric) {
                                navController.push(HeartRateScreenSDK())
                            }
                        }

                        // HRV
                        hrvMetric?.let { metric ->
                            MetricSectionCard(metric) {
                                navController.push(HrvScreen())
                            }
                        }

                        // Blood Pressure
                        bloodPressureMetric?.let { metric ->
                            MetricSectionCard(metric) {
                                navController.push(BloodPressureScreen())
                            }
                        }

                        // Blood Oxygen (SpO2)
                        spo2Metric?.let { metric ->
                            MetricSectionCard(metric) {
                                navController.push(BloodOxygenScreen())
                            }
                        }

                        // Stress
                        stressMetric?.let { metric ->
                            MetricSectionCard(metric) {
                                navController.push(StressScreenSDK())
                            }
                        }

                        // Temperature
                        temperatureMetric?.let { metric ->
                            MetricSectionCard(metric) {
                                navController.push(TemperatureScreenSDK())
                            }
                        }

                        // Atmospheric Pressure
                        pressureMetric?.let { metric ->
                            MetricSectionCard(metric) {
                                navController.push(PressureSDK())
                            }
                        }

//                        // Sleep Summary (always show if data available)
//                        if (deviceCapabilities?.hasSleepTracking == true) {
//                            SleepSummaryCard(
//                                sleep = SleepSummary(
//                                    total = "7h 48m",
//                                    deep = 26,
//                                    light = 47,
//                                    rem = 19,
//                                    awake = 8
//                                ),
//                                data = TODO()
//                            ) {
//                                navController.push(SleepScreenSDK())
//                            }
//                        }
                    }
                }
            }
        }
    }
}


private fun calculateStressFromHrv(hrv: Int): Int {
    return when {
        hrv > 80 -> 20
        hrv > 60 -> 40
        hrv > 40 -> 60
        hrv > 20 -> 80
        else -> 100
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar(
    isConnected: Boolean,
    deviceName: String,
    batteryLevel: Int
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bluetooth,
                    contentDescription = "Bluetooth Status",
                    tint = if (isConnected) Color(0xFF00FF88) else Color.Red,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (isConnected) deviceName else "Disconnected",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        },
        actions = {
            if (isConnected) {
                Text(
                    text = "$batteryLevel%",
                    color = Color(0xFF00FF88),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.BatteryFull,
                    contentDescription = "Battery Level",
                    tint = Color(0xFF00FF88),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Black
        )
    )
}

@Composable
fun DeviceDisconnectedState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.BluetoothDisabled,
            contentDescription = "Device Disconnected",
            tint = Color.Red,
            modifier = Modifier.size(64.dp)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Device Disconnected",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Please reconnect your device to view data",
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00FF88)
            ),
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = Color.Black
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Retry Connection",
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ProgressTripleRow(
    stepData: DayStepData?,
    stepGoal: Int,
    sleepData: SleepData?,
    isLoadingSteps: Boolean,
    navController: Navigator
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Recovery Ring (placeholder - can be connected to heart rate data later)
        CircularProgressRing(
            value = 68,
            max = 100,
            label = "Recovery",
            color = Color(0xFF00FF88),
            icon = Icons.Default.Favorite,
            onClick = {
                // Navigate to steps detail screen
                navController.push(HeartRateScreenSDK())
            }
        )

        // Sleep Ring - Shows sleep score
        CircularProgressRing(
            value = sleepData?.sleepScore ?: 0,
            max = 100,
            label = "Sleep",
            color = Color(0xFF3B82F6),
            icon = Icons.Default.Nightlight,
            onClick = {
                // Navigate to steps detail screen
                navController.push(SleepScreenSDK())
            }
        )

        // Steps Ring - Connected to ViewModel with loading state
        if (isLoadingSteps) {
            CircularProgressRingLoading(
                label = "Steps",
                color = Color(0xFF6366F1),
                icon = Icons.Default.DirectionsWalk
            )
        } else {
            val stepValue = stepData?.totalSteps?.toInt() ?: 0
            val stepPercentage = if (stepGoal > 0) {
                ((stepValue.toFloat() / stepGoal.toFloat()) * 100).toInt().coerceIn(0, 100)
            } else {
                0
            }

            CircularProgressRing(
                value = stepPercentage,
                max = 100,
                label = "Steps",
                color = Color(0xFF6366F1),
                icon = Icons.Default.DirectionsWalk,
                onClick = {
                    // Navigate to steps detail screen
                    navController.push(StepsScreenSDK())
                }
            )
        }
    }
}

@Composable
fun CircularProgressRingLoading(
    label: String,
    color: Color,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(100.dp)
        ) {
            // Loading spinner
            CircularProgressIndicator(
                modifier = Modifier.size(100.dp),
                color = color,
                strokeWidth = 6.dp
            )

            // Icon in center
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = label,
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}


/* ---------- COLORS ---------- */

// ========================================
// DASHBOARD INTEGRATION - Add to your existing HealthDashboard
// ========================================

/* ---------- COLORS ---------- */
private val Blue = Color(0xFF3B82F6)
private val Green = Color(0xFF00FF88)
private val Indigo = Color(0xFF6366F1)
private val Amber = Color(0xFFF59E0B)
private val Red = Color(0xFFFF6B6B)
private val Orange = Color(0xFFFFA500)
private val Purple = Color(0xFF6366F1)

/* ---------- DATA MODELS ---------- */
data class MetricSummary(
    val title: String,
    val unit: String,
    val min: Int,
    val max: Int,
    val avg: Int,
    val color: Color,
)

data class SleepSummary(
    val total: String,
    val deep: Int,
    val light: Int,
    val rem: Int,
    val awake: Int
)

/* ---------- ROOT ---------- */
@Composable
fun HealthDashboard(
    heart: MetricSummary,
    hrv: MetricSummary,
    bloodPressure: MetricSummary,
    bloodOxygen: MetricSummary,
    stress: MetricSummary,
    temperature: MetricSummary,
    pressure: MetricSummary,
    sleep: SleepSummary,
    navController: Navigator
) {
    Column(modifier = Modifier.fillMaxSize()) {
        MetricSectionCard(heart, { navController.push(HeartRateScreenSDK()) })
        MetricSectionCard(hrv, { navController.push(HrvScreen()) })
        MetricSectionCard(bloodPressure, { navController.push(BloodPressureScreen()) })
        MetricSectionCard(bloodOxygen, { navController.push(BloodOxygenScreen()) })
        MetricSectionCard(stress, { navController.push(StressScreenSDK()) })
        MetricSectionCard(temperature, { navController.push(TemperatureScreenSDK()) })
        MetricSectionCard(pressure, { navController.push(PressureSDK()) })
        SleepSummaryCard(sleep, { navController.push(SleepScreenSDK()) })
    }
}

/* ---------- COMMON METRIC CARD ---------- */
@Composable
fun MetricSectionCard(data: MetricSummary , onClick:()->Unit) {
    CardContainer(
        onClick = {onClick()}
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(data.title, color = Color.White)
            Text(data.unit, color = data.color)
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ValueBox("Min", data.min.toString(), data.unit)
            ValueBox("Avg", data.avg.toString(), data.unit)
            ValueBox("Max", data.max.toString(), data.unit)
        }

        Spacer(Modifier.height(16.dp))

        MiniTrendChart(color = data.color)
    }
}

/* ---------- MINI TREND ---------- */
@Composable
private fun MiniTrendChart(color: Color) {
    Row(
        modifier = Modifier
            .height(64.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        repeat(24) { i ->
            val base = 60 + sin(i / 4f) * 15
            val value = ((base - 50) / 40f).coerceIn(0.1f, 1f)

            val animHeight = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                animHeight.animateTo(value, tween(500))
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(animHeight.value)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(color, color.copy(alpha = 0.5f))
                        )
                    )
            )
        }
    }
}

/* ---------- VALUE BOX ---------- */
@Composable
private fun ValueBox(label: String, value: String, unit: String) {
    Box(
        Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black.copy(alpha = 0.25f))
            .padding(12.dp)
    ) {
        Column {
            Text(label, color = Color.Gray, fontSize = 12.sp)
            Text(
                "$value $unit",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/* ---------- SLEEP ---------- */
@Composable
fun SleepSummaryCard(data: SleepSummary , onClick: () -> Unit) {
    CardContainer(
        { onClick() },
        gradient = Brush.linearGradient(
            listOf(Blue.copy(0.12f), Green.copy(0.12f))
        )
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Last Night's Sleep", color = Color.White)
            Text(data.total, color = Blue, fontSize = 20.sp)
        }

        Spacer(Modifier.height(12.dp))

        Row(Modifier.height(32.dp).fillMaxWidth()) {
            Box(Modifier.weight(data.deep / 100f).fillMaxHeight().background(Green))
            Box(Modifier.weight(data.light / 100f).fillMaxHeight().background(Blue))
            Box(Modifier.weight(data.rem / 100f).fillMaxHeight().background(Indigo))
            Box(Modifier.weight(data.awake / 100f).fillMaxHeight().background(Color.Gray))
        }

        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            SleepStat("${data.deep}%", "Deep", Green)
            SleepStat("${data.light}%", "Light", Blue)
            SleepStat("${data.rem}%", "REM", Indigo)
            SleepStat("${data.awake}%", "Awake", Color.Gray)
        }
    }
}

/* ---------- COMMON CARD ---------- */
@Composable
private fun CardContainer(
    onClick: () -> Unit,
    gradient: Brush = Brush.linearGradient(
        listOf(Color.White.copy(0.02f), Color.White.copy(0.01f))
    ),
    content: @Composable ColumnScope.() -> Unit,

) {
    Box(
        Modifier
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(gradient)
            .padding(20.dp)
            .clickable{
                onClick()
            }
    ) {
        Column(content = content)
    }
}

@Composable
private fun SleepStat(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontSize = 12.sp)
        Text(label, color = Color.Gray, fontSize = 11.sp)
    }
}

