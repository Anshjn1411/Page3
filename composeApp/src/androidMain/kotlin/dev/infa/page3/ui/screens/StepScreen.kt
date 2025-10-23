package dev.infa.page3.ui.screens


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import dev.infa.page3.navigation.CartScreenNav
import dev.infa.page3.ui.components.AppSideBar
import dev.infa.page3.ui.components.BottomNavBar
import dev.infa.page3.ui.components.SDKTopBarScreen
import dev.infa.page3.ui.components.TopBarScreen
import dev.infa.page3.ui.navigation.Routes
import dev.infa.page3.viewmodels.StepData
import dev.infa.page3.viewmodels.StepViewmodel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StepScreen(navController: NavController ,viewModel: StepViewmodel) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedStepData by viewModel.selectedStepData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var currentTab by remember { mutableStateOf("step") }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerContent = {
            AppSideBar(navController)
        },
        drawerState = drawerState
    ) {

        Scaffold(
            topBar = {
                TopBarScreen(
                    onClickMenu = { scope.launch { drawerState.open() } }
                )
            },
            bottomBar = {
                BottomNavBar(
                    currentNav = currentTab,
                    navController,

                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Date Selector with Calendar Icon
                DateSelectorSection(
                    selectedDate = selectedDate,
                    onPreviousDay = {
                        val previousDate = getPreviousDate(selectedDate)
                        viewModel.selectDate(previousDate)
                    },
                    onNextDay = {
                        val nextDate = getNextDate(selectedDate)
                        val today = getTodayDate()
                        if (nextDate <= today) {
                            viewModel.selectDate(nextDate)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Week Calendar with Activity Indicators
                WeekCalendarSection(
                    selectedDate = selectedDate,
                    stepDataMap = viewModel.getAllStepData(),
                    onDateSelect = { date ->
                        viewModel.selectDate(date)
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (isLoading) {
                    LoadingSection()
                } else {
                    selectedStepData?.let { stepData ->
                        // Activity Rings Section
                        ActivityRingsSection(stepData = stepData)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Activity Stats Section
                        ActivityStatsSection(stepData = stepData)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Activity Score Section
                        ActivityScoreSection(stepData = stepData)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Hourly Steps Chart Section
                        HourlyStepsChartSection(stepData = stepData)

                        Spacer(modifier = Modifier.height(24.dp))
                    } ?: run {
                        EmptyDataSection(onSync = { viewModel.syncData() })
                    }
                }
            }
        }
    }
}


// Date Selector Section
@Composable
fun DateSelectorSection(
    selectedDate: String,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousDay) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Previous Day",
                tint = Color.Black,
                modifier = Modifier.size(32.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = selectedDate,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = "Calendar",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }

        IconButton(onClick = onNextDay) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Next Day",
                tint = Color.Black,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

// Week Calendar Section
@Composable
fun WeekCalendarSection(
    selectedDate: String,
    stepDataMap: Map<String, StepData>,
    onDateSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val weekDates = getWeekDates(selectedDate)

            weekDates.forEach { (date, dayOfWeek, dayOfMonth) ->
                val stepData = stepDataMap[date]
                val isSelected = date == selectedDate

                WeekDayItem(
                    dayOfWeek = dayOfWeek,
                    dayOfMonth = dayOfMonth,
                    isSelected = isSelected,
                    stepData = stepData,
                    onClick = { onDateSelect(date) }
                )
            }
        }
    }
}

@Composable
fun WeekDayItem(
    dayOfWeek: String,
    dayOfMonth: String,
    isSelected: Boolean,
    stepData: StepData?,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        // Day of Week
        Text(
            text = dayOfWeek,
            fontSize = 12.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Day Number Circle
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) Color(0xFFFF4444) else Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = dayOfMonth,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else Color.Black
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Activity Rainbow Indicator
        RainbowActivityIndicator(stepData = stepData)
    }
}

@Composable
fun RainbowActivityIndicator(stepData: StepData?) {
    Canvas(modifier = Modifier.size(40.dp, 20.dp)) {
        val colors = listOf(
            Color(0xFFFF6B9D), // Pink
            Color(0xFFFFD93D), // Yellow
            Color(0xFF6BCB77), // Green
            Color(0xFF4D96FF)  // Blue
        )

        val progress = if (stepData != null) {
            (stepData.totalSteps.toFloat() / 5000f).coerceIn(0f, 1f)
        } else {
            0.1f
        }

        val strokeWidth = 3.dp.toPx()
        val segmentAngle = 180f / colors.size

        colors.forEachIndexed { index, color ->
            val startAngle = 180f + (index * segmentAngle)
            val segmentProgress = ((progress * colors.size) - index).coerceIn(0f, 1f)
            val sweepAngle = segmentAngle * segmentProgress

            drawArc(
                color = if (segmentProgress > 0) color else color.copy(alpha = 0.2f),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(0f, 0f),
                size = Size(size.width, size.height * 2),
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )
        }
    }
}

// Activity Rings Section
@Composable
fun ActivityRingsSection(stepData: StepData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Three Concentric Rings
            ThreeRingProgress(stepData = stepData)
        }
    }
}

@Composable
fun ThreeRingProgress(stepData: StepData) {
    Box(
        modifier = Modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer Ring - Pink (Calories)
        val calorieProgress = (stepData.calories.toFloat() / 300f).coerceIn(0f, 1f)
        AnimatedCircularProgress(
            progress = calorieProgress,
            size = 240.dp,
            strokeWidth = 16.dp,
            activeColor = Color(0xFFFFB3D9),
            backgroundColor = Color(0xFFFFE5F0)
        )

        // Middle Ring - Green (Distance)
        val distanceProgress = (stepData.distance / 3000f).coerceIn(0f, 1f)
        AnimatedCircularProgress(
            progress = distanceProgress,
            size = 190.dp,
            strokeWidth = 16.dp,
            activeColor = Color(0xFF6BCB77),
            backgroundColor = Color(0xFFE8F5E9)
        )

        // Inner Ring - Blue (Steps)
        val stepProgress = (stepData.totalSteps.toFloat() / 5000f).coerceIn(0f, 1f)
        AnimatedCircularProgress(
            progress = stepProgress,
            size = 140.dp,
            strokeWidth = 16.dp,
            activeColor = Color(0xFF4D96FF),
            backgroundColor = Color(0xFFE3F2FD)
        )
    }
}

@Composable
fun AnimatedCircularProgress(
    progress: Float,
    size: androidx.compose.ui.unit.Dp,
    strokeWidth: androidx.compose.ui.unit.Dp,
    activeColor: Color,
    backgroundColor: Color
) {
    Canvas(modifier = Modifier.size(size)) {
        val canvasSize = this.size.minDimension
        val radius = (canvasSize - strokeWidth.toPx()) / 2
        val center = Offset(canvasSize / 2, canvasSize / 2)

        // Background arc
        drawArc(
            color = backgroundColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(
                center.x - radius,
                center.y - radius
            ),
            size = Size(radius * 2, radius * 2),
            style = Stroke(
                width = strokeWidth.toPx(),
                cap = StrokeCap.Round
            )
        )

        // Progress arc
        drawArc(
            color = activeColor,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = Offset(
                center.x - radius,
                center.y - radius
            ),
            size = Size(radius * 2, radius * 2),
            style = Stroke(
                width = strokeWidth.toPx(),
                cap = StrokeCap.Round
            )
        )
    }
}

// Activity Stats Section
@Composable
fun ActivityStatsSection(stepData: StepData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Total Steps
        ActivityStatItem(
            label = "Total Steps",
            value = stepData.totalSteps.toString(),
            target = "/5000Steps",
            color = Color(0xFF4D96FF)
        )

        // Total Mileage
        ActivityStatItem(
            label = "Total Mileage",
            value = stepData.getFormattedDistance().replace(" km", ""),
            target = "/3Km",
            color = Color(0xFF6BCB77)
        )

        // Total Calories
        ActivityStatItem(
            label = "Total Calories",
            value = (stepData.calories.toInt()/1000).toString(),
            target = "/300Kcal",
            color = Color(0xFFFF6B9D)
        )
    }
}

@Composable
fun ActivityStatItem(
    label: String,
    value: String,
    target: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = color,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        Text(
            text = target,
            fontSize = 10.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

// Activity Score Section
@Composable
fun ActivityScoreSection(stepData: StepData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Activity Score",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = calculateActivityScore(stepData).toString(),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = getActivityLevel(stepData),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View Details",
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// Hourly Steps Chart Section
@Composable
fun HourlyStepsChartSection(stepData: StepData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Steps",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4D96FF)
                )

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "0",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "Steps",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "23:00",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Chart
            HourlyStepsChart(stepData = stepData)

            Spacer(modifier = Modifier.height(8.dp))

            // Time Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "0",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
                Text(
                    text = "23:59",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun HourlyStepsChart(stepData: StepData) {
    val hourlyData = generateHourlyStepData(stepData)
    val maxValue = hourlyData.maxOrNull() ?: 1

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        hourlyData.forEach { steps ->
            val heightFraction = if (maxValue > 0) {
                (steps.toFloat() / maxValue).coerceIn(0.05f, 1f)
            } else {
                0.05f
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(heightFraction)
                    .padding(horizontal = 1.dp)
                    .background(
                        Color(0xFF4D96FF),
                        RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                    )
            )
        }
    }
}

// Loading Section
@Composable
fun LoadingSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(64.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color(0xFF4D96FF)
        )
    }
}

// Empty Data Section
@Composable
fun EmptyDataSection(onSync: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No activity data available",
                fontSize = 16.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sync your device to see your activity",
                fontSize = 12.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onSync,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4D96FF)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sync Now")
            }
        }
    }
}

// Utility Functions
private fun getTodayDate(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
}

private fun getPreviousDate(currentDate: String): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.time = dateFormat.parse(currentDate) ?: Date()
    calendar.add(Calendar.DAY_OF_YEAR, -1)
    return dateFormat.format(calendar.time)
}

private fun getNextDate(currentDate: String): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.time = dateFormat.parse(currentDate) ?: Date()
    calendar.add(Calendar.DAY_OF_YEAR, 1)
    return dateFormat.format(calendar.time)
}

private fun getWeekDates(centerDate: String): List<Triple<String, String, String>> {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val calendar = Calendar.getInstance()

    try {
        calendar.time = dateFormat.parse(centerDate) ?: Date()
    } catch (e: Exception) {
        calendar.time = Date()
    }

    // Get to Monday of the week
    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val diff = if (dayOfWeek == Calendar.SUNDAY) -6 else Calendar.MONDAY - dayOfWeek
    calendar.add(Calendar.DAY_OF_YEAR, diff)

    val weekDates = mutableListOf<Triple<String, String, String>>()

    for (i in 0..6) {
        val date = dateFormat.format(calendar.time)
        val day = dayFormat.format(calendar.time)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')

        weekDates.add(Triple(date, day, dayOfMonth))
        calendar.add(Calendar.DAY_OF_YEAR, 1)
    }

    return weekDates
}

private fun calculateActivityScore(stepData: StepData): Int {
    val stepScore = (stepData.totalSteps.toFloat() / 5000f * 40).toInt()
    val distanceScore = (stepData.distance.toFloat() / 3000f * 30).toInt()
    val calorieScore = (stepData.calories.toFloat() / 300f * 30).toInt()
    return (stepScore + distanceScore + calorieScore).coerceIn(0, 100)
}

private fun getActivityLevel(stepData: StepData): String {
    val score = calculateActivityScore(stepData)
    return when {
        score >= 80 -> "Very active"
        score >= 60 -> "Active"
        score >= 40 -> "Moderate"
        score >= 20 -> "Light exercise"
        else -> "Less exercise"
    }
}

private fun generateHourlyStepData(stepData: StepData): List<Int> {
    if (stepData.detailData.isNotEmpty()) {
        val hourlySteps = IntArray(24)
        stepData.detailData.forEach { detail ->
            val hour = detail.timeIndex / 4
            hourlySteps[hour % 24] += detail.steps
        }
        return hourlySteps.toList()
    }

    return List(24) { hour ->
        when (hour) {
            in 6..8 -> (100..300).random()
            in 12..14 -> (200..500).random()
            in 17..20 -> (150..400).random()
            else -> (0..100).random()
        }
    }
}