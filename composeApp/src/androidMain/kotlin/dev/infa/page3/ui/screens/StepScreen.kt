package dev.infa.page3.ui.screens


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import dev.infa.page3.navigation.CartScreenNav
import dev.infa.page3.ui.components.AppSideBar
import dev.infa.page3.ui.components.BottomNavBar
import dev.infa.page3.ui.components.DateSelector
import dev.infa.page3.ui.components.SDKTopBarScreen
import dev.infa.page3.ui.components.TopBarScreen
import dev.infa.page3.ui.navigation.Routes
import dev.infa.page3.viewmodels.ConnectionViewModel
import dev.infa.page3.viewmodels.HomeViewModel
import dev.infa.page3.viewmodels.StepData
import dev.infa.page3.viewmodels.StepViewmodel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StepsScreen(
    stepViewModel: StepViewmodel,
    connectionViewModel: ConnectionViewModel,
    homeViewModel: HomeViewModel,
    onBack: () -> Unit
) {
    // Connection state
    val uiState by connectionViewModel.uiState.collectAsState()
    val isConnected = uiState.isConnected
    val deviceName = uiState.connectedDevice?.deviceName ?: "Device"

    // Step data from ViewModel
    val selectedDateString by stepViewModel.selectedDate.collectAsState()
    val selectedStepData by stepViewModel.selectedStepData.collectAsState()
    val isLoadingSteps by stepViewModel.isLoading.collectAsState()

    // Goals from HomeViewModel
    val stepGoal by homeViewModel.stepGoal.collectAsState()
    val batteryLevel by homeViewModel.batteryValue.collectAsState()

    // Track if initial sync is done
    var hasInitialSyncDone by remember { mutableStateOf(false) }
    var hasSyncedToday by remember { mutableStateOf(false) }

    // Convert String date to LocalDate
    val selectedLocalDate = remember(selectedDateString) {
        try {
            LocalDate.parse(selectedDateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        } catch (e: Exception) {
            LocalDate.now()
        }
    }

    // Get week dates
    val weekDates = remember(selectedLocalDate) {
        val startOfWeek = selectedLocalDate.with(DayOfWeek.MONDAY)
        (0..6).map { startOfWeek.plusDays(it.toLong()) }
    }

    val todayDateString = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }

    // FIRST LaunchedEffect: Sync TODAY'S data ONCE when screen opens
    LaunchedEffect(Unit) {
        if (!hasSyncedToday && isConnected) {
            hasSyncedToday = true
            stepViewModel.syncTodaySteps()
            delay(1000) // Wait for today's sync to complete
        }
    }

    // SECOND LaunchedEffect: Fetch weekly data ONCE after today is synced
    LaunchedEffect(hasSyncedToday) {
        if (hasSyncedToday && !hasInitialSyncDone && isConnected) {
            hasInitialSyncDone = true

            // Sync rest of the week (skip today)
            weekDates.forEach { date ->
                val dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

                // Skip today since we already synced it
                if (dateString != todayDateString) {
                    delay(800) // Gap between each request

                    if (!stepViewModel.hasDataForDate(dateString)) {
                        stepViewModel.syncStepDataForDate(dateString)
                    }
                }
            }
        }
    }

    // THIRD LaunchedEffect: Handle user date changes ONLY (not automatic changes)
    LaunchedEffect(selectedDateString) {
        // Only fetch if:
        // 1. Initial sync is done
        // 2. Selected date is not today
        // 3. We don't have data for this date
        if (hasInitialSyncDone &&
            selectedDateString != todayDateString &&
            !stepViewModel.hasDataForDate(selectedDateString)) {

            delay(500) // Small delay before fetching
            stepViewModel.syncStepDataForDate(selectedDateString)
        }
    }

    // Build weekly data from ViewModel
    val weeklyData = remember(weekDates, selectedStepData) {
        weekDates.map { date ->
            val dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val data = stepViewModel.getStepDataForDate(dateString)
            val isToday = dateString == selectedDateString

            WeeklyStepData(
                day = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()),
                steps = data?.totalSteps?.toInt() ?: 0,
                isActive = isToday,
                date = dateString
            )
        }
    }

    val totalSteps = selectedStepData?.totalSteps?.toInt() ?: 0
    val distance = selectedStepData?.distance?.toInt() ?: 0
    val calories = selectedStepData?.calories?.toInt() ?: 0
    val activeDuration = selectedStepData?.sportDuration?.toInt() ?: 0
    val maxSteps = weeklyData.maxOfOrNull { it.steps } ?: 10000

    // Calculate weekly stats
    val weekTotal = weeklyData.sumOf { it.steps }
    val avgStepsPerHour = if (activeDuration > 0) totalSteps / (activeDuration / 60) else 0
    val goalsMetThisWeek = weeklyData.count { it.steps >= stepGoal }
    val personalBest = weeklyData.maxOfOrNull { it.steps } ?: 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Top Bar
        DashboardTopBar(
            isConnected = isConnected,
            deviceName = deviceName,
            batteryLevel = batteryLevel ?: 0
        )

        // Date Selector
        DateSelector(
            selectedDate = selectedLocalDate,
            onDateChange = { newDate ->
                val dateString = newDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                stepViewModel.selectDate(dateString)
            }
        )

        if (!isConnected) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Device not connected",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main Steps Card
                item {
                    if (isLoadingSteps && selectedStepData == null) {
                        LoadingStepsCard()
                    } else {
                        StepsMainCard(
                            totalSteps = totalSteps,
                            goal = stepGoal,
                            distance = distance / 1000f, // Convert to km
                            calories = calories.toInt(),
                            activeDuration = activeDuration.toInt()
                        )
                    }
                }

                // Weekly Bar Chart
                item {
                    WeeklyStepsChart(
                        data = weeklyData,
                        maxSteps = maxSteps,
                        isLoading = !hasInitialSyncDone
                    )
                }

                // Hourly Activity (using selected day data)
                item {
                    HourlyStepsChart(
                        stepData = selectedStepData,
                        isLoading = isLoadingSteps
                    )
                }

                // Extra Stats Grid
                item {
                    StepsExtraStats(
                        avgStepsPerHour = avgStepsPerHour,
                        goalsMetThisWeek = goalsMetThisWeek
                    )
                }

                // Personal Record
                item {
                    PersonalBestCard(
                        bestSteps = personalBest,
                        weeklyData = weeklyData
                    )
                }

                // Bottom spacing
                item {
                    Spacer(Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun LoadingStepsCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.06f),
                        Color.White.copy(alpha = 0.02f)
                    )
                ),
                RoundedCornerShape(28.dp)
            )
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(28.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = Color(0xFF00FF88),
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("Loading steps data...", color = Color.Gray, fontSize = 14.sp)
    }
}

@Composable
fun StepsMainCard(
    totalSteps: Int,
    goal: Int,
    distance: Float,
    calories: Int,
    activeDuration: Int
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(totalSteps, goal) {
        progress.animateTo(
            targetValue = (totalSteps / goal.toFloat()).coerceIn(0f, 1f),
            animationSpec = tween(1500)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.06f),
                        Color.White.copy(alpha = 0.02f)
                    )
                ),
                RoundedCornerShape(28.dp)
            )
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(28.dp))
            .padding(20.dp)
    ) {
        Text("Total Steps", color = Color.Gray, fontSize = 13.sp)

        Text(
            text = totalSteps.toString(),
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 6.dp),
            color = Color(0xFF00FF88)
        )

        Text("of $goal goal", color = Color.Gray, fontSize = 12.sp)

        Spacer(Modifier.height(16.dp))

        LinearProgressIndicator(
            progress = progress.value,
            color = Color(0xFF00FF88),
            trackColor = Color.DarkGray,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50))
        )

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickStat(String.format("%.1f km", distance), "Distance")
            QuickStat(calories.toString(), "Calories")
            QuickStat("${activeDuration}m", "Active")
        }
    }
}

@Composable
fun WeeklyStepsChart(
    data: List<WeeklyStepData>,
    maxSteps: Int,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(0.04f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("7-Day Trend", color = Color.White)
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color(0xFF00FF88),
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { item ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    val heightRatio = if (maxSteps > 0)
                        (item.steps / maxSteps.toFloat()).coerceIn(0f, 1f)
                    else 0f

                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .fillMaxHeight(if (heightRatio > 0) heightRatio else 0.05f)
                            .then(
                                if (item.isActive) {
                                    Modifier.background(
                                        Brush.verticalGradient(listOf(Color(0xFF00FF88), Color(0xFF3B82F6))),
                                        RoundedCornerShape(10.dp)
                                    )
                                } else if (item.steps > 0) {
                                    Modifier.background(
                                        Brush.verticalGradient(listOf(Color.Gray, Color.DarkGray)),
                                        RoundedCornerShape(10.dp)
                                    )
                                } else {
                                    Modifier.background(
                                        Color.DarkGray.copy(alpha = 0.3f),
                                        RoundedCornerShape(10.dp)
                                    )
                                }
                            )
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        item.day,
                        color = if (item.isActive) Color.White else Color.Gray,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun HourlyStepsChart(
    stepData: StepData?,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(0.04f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Hourly Breakdown", color = Color.White)
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color(0xFF00FF88),
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Generate hourly distribution based on total steps
            val totalSteps = stepData?.totalSteps ?: 0
            val hourlySteps = remember(totalSteps) {
                val random = kotlin.random.Random
                if (totalSteps > 0) {
                    // Simulate hourly distribution (peak hours: 8-10am, 6-8pm)
                    (0..23).map { hour ->
                        when (hour) {
                            in 8..10, in 18..20 -> (totalSteps / 24 * (1.2f + random.nextFloat() * 0.3f)).toInt()
                            in 12..14 -> (totalSteps / 24 * (1.0f + random.nextFloat() * 0.2f)).toInt()
                            in 0..6, in 22..23 -> (totalSteps / 24 * (0.1f + random.nextFloat() * 0.2f)).toInt()
                            else -> (totalSteps / 24 * (0.5f + random.nextFloat() * 0.5f)).toInt()
                        }
                    }
                } else {
                    List(24) { 0 }
                }
            }

            val maxHourlySteps = hourlySteps.maxOrNull() ?: 1

            hourlySteps.forEach { steps ->
                val heightRatio = if (maxHourlySteps > 0)
                    (steps / maxHourlySteps.toFloat()).coerceIn(0f, 1f)
                else 0f

                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .fillMaxHeight(if (heightRatio > 0) heightRatio else 0.05f)
                        .background(
                            if (steps > 0) Color(0xFF3B82F6) else Color.DarkGray.copy(alpha = 0.3f),
                            RoundedCornerShape(4.dp)
                        )
                )
            }
        }
    }
}

@Composable
fun QuickStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontWeight = FontWeight.Medium)
        Text(label, color = Color.Gray, fontSize = 11.sp)
    }
}

@Composable
fun StepsExtraStats(
    avgStepsPerHour: Int,
    goalsMetThisWeek: Int
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SmallStatCard(
            value = if (avgStepsPerHour > 0) avgStepsPerHour.toString() else "—",
            label = "Steps/Hour Avg"
        )
        SmallStatCard(
            value = "$goalsMetThisWeek/7",
            label = "Goals This Week"
        )
    }
}

@Composable
fun SmallStatCard(value: String, label: String) {
    Column(
        modifier = Modifier
            .background(Color.White.copy(0.04f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Text(value, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(label, color = Color.Gray, fontSize = 11.sp)
    }
}

@Composable
fun PersonalBestCard(
    bestSteps: Int,
    weeklyData: List<WeeklyStepData>
) {
    val bestDay = weeklyData.maxByOrNull { it.steps }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF00FF88).copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .border(1.dp, Color(0xFF00FF88).copy(0.3f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Text("Personal Best This Week", color = Color.Gray, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            bestSteps.toString(),
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            bestDay?.day ?: "—",
            color = Color.Gray,
            fontSize = 13.sp
        )
    }
}

// Updated data class with date field
data class WeeklyStepData(
    val day: String,
    val steps: Int,
    val isActive: Boolean,
    val date: String = ""
)
