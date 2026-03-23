package dev.infa.page3.SDK.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.SDK.data.DayStepData
import dev.infa.page3.SDK.ui.components.*
import dev.infa.page3.SDK.ui.components.LocalSdkNavVisibility
import dev.infa.page3.SDK.ui.components.SdkNavVisibilityState
import dev.infa.page3.SDK.ui.navigation.*
import dev.infa.page3.SDK.ui.utils.*
import dev.infa.page3.SDK.viewModel.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import dev.infa.page3.SDK.ui.screens.helperFuntions.calculateStressFromHrv
import dev.infa.page3.SDK.ui.theme.AppColors
import dev.infa.page3.SDK.ui.theme.AppDimensions
import dev.infa.page3.SDK.ui.theme.AppShapes
import dev.infa.page3.SDK.ui.theme.AppTypography
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    connectionViewModel: ConnectionViewModel,
    homeViewModel: HomeViewModel,
    syncViewModel: SyncViewModel,
    instantMeasuresViewModel : InstantMeasuresViewModel,
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
    val navVisibilityState = remember { SdkNavVisibilityState() }

    // Guard: only fetch capabilities once per connection
    var capsFetched by remember { mutableStateOf(false) }

    LaunchedEffect(isConnected) {
        if(isConnected){
            homeViewModel.getBatteryLevel()
            if (!capsFetched) {
                homeViewModel.fetchDeviceCapabilities()
                capsFetched = true
            }
        } else {
            capsFetched = false  // reset on disconnect
        }
    }

    // Start auto-sync once capabilities are available
    LaunchedEffect(isConnected, deviceCapabilities) {
        if (isConnected && !autoSyncCompleted && deviceCapabilities != null) {
            syncViewModel.startAutoSync(deviceCapabilities)
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
            color = AppColors.HeartRate
        )
    } else null

    val hrvMetric = if (deviceCapabilities?.hasHRV == true) {
        MetricSummary(
            title = "HRV",
            unit = "ms",
            min = hrvData?.minHrv ?: 0,
            avg = hrvData?.averageHrv ?: 0,
            max = hrvData?.maxHrv ?: 0,
            color = AppColors.Secondary
        )
    } else null

    val spo2Metric = if (deviceCapabilities?.hasSpO2 == true) {
        MetricSummary(
            title = "Blood Oxygen",
            unit = "%",
            min = spo2Data?.minSpO2 ?: 0,
            avg = spo2Data?.averageSpO2 ?: 0,
            max = spo2Data?.maxSpO2 ?: 0,
            color = AppColors.Accent
        )
    } else null

    val stressMetric = if (deviceCapabilities?.hasHRV == true) {
        MetricSummary(
            title = "Stress",
            unit = "Level",
            min = 0,
            avg = calculateStressFromHrv(hrvData?.averageHrv ?: 0),
            max = 100,
            color = AppColors.AccentAmber
        )
    } else null

    val temperatureMetric = if (deviceCapabilities?.hasBodyTemperature == true) {
        MetricSummary(
            title = "Temperature",
            unit = "°C",
            min = tempData?.minTemp?.toInt() ?: 0,
            avg = tempData?.averageTemp?.toInt() ?: 0,
            max = tempData?.maxTemp?.toInt() ?: 0,
            color = AppColors.AccentOrange
        )
    } else null

    val pressureMetric = if (deviceCapabilities?.hasBloodPressure == true) {
        MetricSummary(
            title = "Pressure",
            unit = "hPa",
            min = pressureData?.minPressure?.toInt() ?: 0,
            avg = pressureData?.averagePressure?.toInt() ?: 0,
            max = pressureData?.maxPressure?.toInt() ?: 0,
            color = AppColors.AccentPurple
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
            color = AppColors.HeartRate
        )
    } else null

    val oneKeyMetric =
        if (deviceCapabilities?.hasOneKeyCheck == true) Unit else null

    CompositionLocalProvider(LocalSdkNavVisibility provides navVisibilityState) {
        val listState = rememberLazyListState()
        val navVisibility = LocalSdkNavVisibility.current
        var prevScrollOffset by remember { mutableStateOf(0) }

        LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
            val currentOffset =
                listState.firstVisibleItemIndex * 10000 + listState.firstVisibleItemScrollOffset
            val scrollingDown = currentOffset > prevScrollOffset
            prevScrollOffset = currentOffset

            navVisibility.isVisible =
                if (listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset < 50) {
                    true
                } else {
                    !scrollingDown
                }
        }

        Scaffold(
            containerColor = AppColors.BackgroundPrimary,
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
                    state = listState,
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

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.ExtraLarge)) {

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

                            oneKeyMetric?.let {
                                OneKeyMeasurementCard(viewModel = instantMeasuresViewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceDisconnectedState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppDimensions.CardPadding.ExtraLarge),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.BluetoothDisabled,
            contentDescription = "Device Disconnected",
            tint = AppColors.Error,
            modifier = Modifier.size(AppDimensions.IconSize.Large)
        )

        Spacer(Modifier.height(AppDimensions.Spacing.ExtraLarge))

        Text(
            text = "Device Disconnected",
            color = AppColors.TextPrimary,
            style = AppTypography.HeadingSmall
        )

        Spacer(Modifier.height(AppDimensions.Spacing.Medium))

        Text(
            text = "Please reconnect your device to view data",
            color = AppColors.TextSecondary,
            style = AppTypography.BodySmall
        )

        Spacer(Modifier.height(AppDimensions.Spacing.Huge))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.Primary
            ),
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(AppDimensions.ButtonHeight.Medium)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = AppColors.BackgroundPrimary
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Retry Connection",
                color = AppColors.BackgroundPrimary,
                style = AppTypography.LabelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

/* ---------- DATA MODELS ---------- */
data class MetricSummary(
    val title: String,
    val unit: String,
    val min: Int,
    val max: Int,
    val avg: Int,
    val color: Color,
)

/* ---------- COMMON METRIC CARD ---------- */
@Composable
fun MetricSectionCard(data: MetricSummary, onClick: () -> Unit) {
    CardContainer(onClick = { onClick() }) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                data.title,
                color = AppColors.TextPrimary,
                style = AppTypography.BodyMedium
            )
            Text(
                data.unit,
                color = data.color,
                style = AppTypography.LabelMedium
            )
        }

        Spacer(Modifier.height(AppDimensions.Spacing.Default))

        Row(horizontalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.Default)) {
            ValueBox("Min", data.min.toString(), data.unit)
            ValueBox("Avg", data.avg.toString(), data.unit)
            ValueBox("Max", data.max.toString(), data.unit)
        }

        Spacer(Modifier.height(AppDimensions.Spacing.ExtraLarge))

        MiniTrendChart(color = data.color)
    }
}

/* ---------- MINI TREND ---------- */
@Composable
private fun MiniTrendChart(color: Color) {
    Row(
        modifier = Modifier
            .height(AppDimensions.Spacing.Large)
            .fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.ExtraSmall)
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
                    .clip(AppShapes.ChartBar)
                    .background(
                        Brush.verticalGradient(
                            listOf(color, color.copy(alpha = 0.5f))
                        )
                    )
            )
        }
    }
}

@Composable
private fun ValueBox(label: String, value: String, unit: String) {
    Box(
        Modifier
            .clip(AppShapes.MetricValueBox)
            .background(AppColors.OverlayDark)
            .padding(AppDimensions.Component.ValueBoxPadding)
    ) {
        Column {
            Text(
                label,
                color = AppColors.TextSecondary,
                style = AppTypography.LabelMedium
            )
            Text(
                "$value $unit",
                color = AppColors.TextPrimary,
                style = AppTypography.ValueMedium
            )
        }
    }
}

/* ---------- COMMON CARD ---------- */
@Composable
private fun CardContainer(
    onClick: () -> Unit,
    gradient: Brush = Brush.linearGradient(
        listOf(AppColors.BackgroundPrimary.copy(0.02f), AppColors.BackgroundPrimary.copy(0.01f))
    ),
    content: @Composable ColumnScope.() -> Unit,

) {
    Box(
        Modifier
            .padding(horizontal =  AppDimensions.Spacing.XXXL, vertical =  AppDimensions.Spacing.Medium)
            .clip(AppShapes.CardLarge)
            .background(gradient)
            .padding(AppDimensions.Spacing.XXXL)
            .clickable{
                onClick()
            }
    ) {
        Column(content = content)
    }
}
