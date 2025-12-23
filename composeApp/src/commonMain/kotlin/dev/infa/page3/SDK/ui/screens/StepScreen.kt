package dev.infa.page3.SDK.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.*
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.SDK.data.*
import dev.infa.page3.SDK.ui.components.*
import dev.infa.page3.SDK.ui.navigation.*
import dev.infa.page3.SDK.ui.utils.*
import dev.infa.page3.SDK.viewModel.*

@Composable
fun StepsScreen(
    onBack: () -> Unit,
    viewModel: SyncViewModel,
    homeViewModel: HomeViewModel,
    navController : Navigator
) {
    val todaySteps by viewModel.todaySteps.collectAsStateWithLifecycle()
    val hourlyData by viewModel.hourlyData.collectAsStateWithLifecycle()
    val weeklySummary by viewModel.weeklySummary.collectAsStateWithLifecycle()
    val stepGoal by homeViewModel.stepGoal.collectAsState()

    var selectedDate by remember { mutableStateOf(DateUtils.getCurrentDate()) }
    val selectedDateOffset = remember(selectedDate) {
        DateUtils.getDayOffsetFromToday(selectedDate)
    }
    LaunchedEffect(selectedDateOffset) {
        viewModel.fetchStepsByOffset(selectedDateOffset, forceRefresh = false)

    }
    var currentTab by remember { mutableStateOf(BottomTab.STEP) }


    Scaffold(
        containerColor = Color.Black,
        topBar = {
            ScreenHeader(title = "Activity", onBack = onBack)
        },
        bottomBar = {
            BottomNavBar(
                currentTab = currentTab,
                onTabSelected = { tab ->
                    currentTab = tab

                    when (tab) {
                        BottomTab.HOME -> navController.push(HomeScreenSDK())
                        BottomTab.STRAIN -> navController.push(ExerciseScreenSDK())
                        BottomTab.RECOVERY -> navController.push(HeartRateScreenSDK())
                        BottomTab.STEP -> navController.replace(StepsScreenSDK())

                        BottomTab.PROFILE -> navController.push(ProfileScreenSDK())
                    }
                }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header


                DateSelector(
                    selectedDate = selectedDate,
                    onDateChange = { newDate ->
                        selectedDate = newDate
                    }
                )

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Main Steps Display
                    MainStepsCard(steps = todaySteps, stepGoal)

                    // Weekly Chart
                    WeeklyChartCard(weeklySummary = weeklySummary)

                    // Hourly Activity
                    HourlyActivityCard(hourlyData = hourlyData)

                    // Additional Stats
                    AdditionalStatsRow(weeklySummary = weeklySummary)

                    // Personal Record
                    PersonalRecordCard(weeklySummary = weeklySummary)

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun ScreenHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MainStepsCard(steps: Int, stepGoal: Int = 10000) {
    val animatedSteps by animateIntAsState(
        targetValue = steps,
        animationSpec = tween(1500, easing = EaseOut),
        label = "steps"
    )

    val progress = (steps / stepGoal.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1500, easing = EaseOut),
        label = "progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total Steps",
                color = Color.Gray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = FormatUtils.formatNumber(animatedSteps),
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                style = TextStyle(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF00FF88),
                            Color(0xFF3B82F6)
                        )
                    )
                )
            )


            Text(
                text = "of ${FormatUtils.formatNumber(stepGoal)} goal",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF1A1A1A))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF00FF88), Color(0xFF00CC66), Color(0xFF3B82F6))
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .blur(4.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF00FF88), Color(0xFF3B82F6))
                            )
                        )
                        .alpha(0.5f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("0", color = Color.Gray, fontSize = 12.sp)
                Text(FormatUtils.formatNumber(stepGoal / 2), color = Color.Gray, fontSize = 12.sp)
                Text(FormatUtils.formatNumber(stepGoal), color = Color.Gray, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                QuickStatItem(
                    icon = Icons.Default.Place,
                    value = FormatUtils.formatDistance(steps),
                    label = "Distance",
                    color = Color(0xFF3B82F6),
                    modifier = Modifier.weight(1f)
                )
                QuickStatItem(
                    icon = Icons.Default.LocalFireDepartment,
                    value = FormatUtils.formatNumber((steps * 0.04).toInt()),
                    label = "Calories",
                    color = Color(0xFF00FF88),
                    modifier = Modifier.weight(1f)
                )
                QuickStatItem(
                    icon = Icons.Default.Bolt,
                    value = "${(steps / 180)}m",
                    label = "Active",
                    color = Color(0xFF3B82F6),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}


@Composable
fun QuickStatItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = label, color = Color.Gray, fontSize = 11.sp)
        }
    }
}

@Composable
fun WeeklyChartCard(weeklySummary: WeeklySummary) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    // Get today's day of week index (0 = Monday, 6 = Sunday)
    val todayDate = DateUtils.getCurrentDate()
    val todayIndex = DateUtils.getDayOfWeekIndex(todayDate)

    // Build 7-day data from Monday to today
    val weeklySteps = (0..6).map { dayIndex ->
        if (dayIndex <= todayIndex) {
            // Get data for this day
            val offset = todayIndex - dayIndex
            weeklySummary.allDays.getOrNull(offset)?.totalSteps ?: 0
        } else {
            // Future days have 0 steps
            0
        }
    }

    val maxSteps = weeklySteps.maxOrNull()?.coerceAtLeast(1) ?: 1

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("7-Day Trend", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = Color(0xFF00FF88),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                weeklySteps.forEachIndexed { index, steps ->
                    val height = if (maxSteps > 0) (steps.toFloat() / maxSteps) else 0f
                    val isActive = index == todayIndex

                    val animatedHeight by animateFloatAsState(
                        targetValue = height,
                        animationSpec = tween(800, delayMillis = 200 + index * 100),
                        label = "height"
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(animatedHeight.coerceAtLeast(0.15f))
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    brush = if (isActive) {
                                        Brush.verticalGradient(
                                            colors = listOf(Color(0xFF00FF88), Color(0xFF3B82F6))
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            colors = listOf(Color(0xFF4A4A4A), Color(0xFF2A2A2A))
                                        )
                                    }
                                )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = days[index],
                            color = if (isActive) Color.White else Color.Gray,
                            fontSize = 12.sp
                        )

                        if (isActive) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00FF88))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HourlyActivityCard(hourlyData: List<HourlyStepData>) {
    val displayData = if (hourlyData.isEmpty()) {
        List(24) { 0 }
    } else {
        List(24) { hour ->
            hourlyData.find { it.hour == hour }?.steps ?: 0
        }
    }

    val maxValue = displayData.maxOrNull()?.coerceAtLeast(1) ?: 1

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Hourly Breakdown", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                displayData.forEachIndexed { index, value ->
                    val height = if (maxValue > 0) (value.toFloat() / maxValue) else 0f

                    val animatedHeight by animateFloatAsState(
                        targetValue = height,
                        animationSpec = tween(600, delayMillis = 300 + index * 20),
                        label = "hourlyHeight"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(animatedHeight.coerceAtLeast(0.04f))
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF3B82F6).copy(alpha = 0.8f),
                                        Color(0xFF3B82F6).copy(alpha = 0.4f)
                                    )
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("12 AM", color = Color.Gray, fontSize = 12.sp)
                Text("12 PM", color = Color.Gray, fontSize = 12.sp)
                Text("11 PM", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun AdditionalStatsRow(weeklySummary: WeeklySummary) {
    val avgStepsPerHour = if (weeklySummary.averageSteps > 0) weeklySummary.averageSteps / 24 else 0
    val goalsThisWeek = weeklySummary.allDays.count { it.totalSteps >= 10000 }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Icon(
                    imageVector = Icons.Default.DirectionsWalk,
                    contentDescription = null,
                    tint = Color(0xFF00FF88),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = FormatUtils.formatNumber(avgStepsPerHour),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("Steps/Hour Avg", color = Color.Gray, fontSize = 11.sp)
            }
        }

        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Icon(
                    imageVector = Icons.Default.GpsFixed,
                    contentDescription = null,
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$goalsThisWeek/7",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("Goals This Week", color = Color.Gray, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun PersonalRecordCard(weeklySummary: WeeklySummary) {
    val bestDay = weeklySummary.bestDay

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF00FF88).copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00FF88))
                    )
                    Text("Personal Best", color = Color.Gray, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = FormatUtils.formatNumber(bestDay?.totalSteps ?: 15680),
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = bestDay?.date ?: "Nov 28, 2024",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF00FF88).copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(modifier = Modifier.padding(12.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = Color(0xFF00FF88),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

fun Double.format(digits: Int) = FormatUtils.formatNumber(digits)


