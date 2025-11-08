package dev.infa.page3.ui.screens

import android.net.Uri
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import dev.infa.page3.ui.components.AppSideBar
import dev.infa.page3.ui.components.BottomNavBar
import dev.infa.page3.ui.components.TopBarScreen
import dev.infa.page3.viewmodels.SleepData
import dev.infa.page3.viewmodels.SleepUiState
import dev.infa.page3.viewmodels.SleepViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepScreen(
    navController: NavController,
    viewModel: SleepViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    var currentTab by remember { mutableStateOf("sleep") }

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
            ) {
                // Simple Date Selector
                SimpleDateSelector(
                    selectedDate = selectedDate,
                    onPreviousDay = { viewModel.navigateToPreviousDay() },
                    onNextDay = { viewModel.navigateToNextDay() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Content based on UI state
                when (val state = uiState) {
                    is SleepUiState.Loading -> {
                        LoadingContent()
                    }

                    is SleepUiState.Success -> {
                        val sleepData = viewModel.getSleepDataForSelectedDate()
                        if (sleepData != null) {
//                            if (sleepData.sleepEfficiency == "No Data") {
//                                NoDataContent()
//                            } else {
//                                SimpleSleepContent(sleepData)
//                            }
                        } else {
                            EmptyContent()
                        }
                    }

                    is SleepUiState.Empty -> {
                        EmptyContent()
                    }

                    is SleepUiState.Error -> {
                        ErrorContent(state.message)
                    }
                }

                // Sync Status
                if (isSyncing) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Syncing sleep data...", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleDateSelector(
    selectedDate: String,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousDay) {
                Icon(Icons.Default.KeyboardArrowLeft, "Previous day")
            }

            Text(
                text = formatDateForDisplay(selectedDate),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            IconButton(onClick = onNextDay) {
                Icon(Icons.Default.KeyboardArrowRight, "Next day")
            }
        }
    }
}

@Composable
fun WeekCalendar(
    selectedDate: String,
    onDateSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val weekDates = getWeekDatesForCalendar(selectedDate)

        weekDates.forEach { (date, dayOfWeek, dayOfMonth) ->
            WeekDayItem(
                dayOfWeek = dayOfWeek,
                dayOfMonth = dayOfMonth,
                isSelected = date == selectedDate,
                onClick = { onDateSelect(date) }
            )
        }
    }
}

@Composable
fun WeekDayItem(
    dayOfWeek: String,
    dayOfMonth: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = dayOfWeek,
            fontSize = 12.sp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = dayOfMonth,
                fontSize = 14.sp,
                color = if (isSelected) Color.White else Color.Black
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Sleep indicator dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    else Color.LightGray
                )
        )
    }
}

@Composable
fun SimpleSleepContent(sleepData: SleepData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Main Sleep Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Total Duration
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Sleep", fontSize = 14.sp, color = Color.Gray)
//                        Text(
//                            text = sleepData.getFormattedDuration(),
//                            fontSize = 28.sp,
//                            fontWeight = FontWeight.Bold
//                        )
//                        Text(
//                            text = "${sleepData.sleepTime} - ${sleepData.wakeTime}",
//                            fontSize = 12.sp,
//                            color = Color.Gray
//                        )
                    }

                    // Sleep Score
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Score", fontSize = 14.sp, color = Color.Gray)
                        Text(
                            text = "${sleepData.sleepScore}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sleep Quality and Efficiency
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
//                    SimpleMetricCard(
//                        value = sleepData.sleepQuality,
//                        label = "Quality"
//                    )
                    SimpleMetricCard(
                        value = "${sleepData.sleepEfficiency}%",
                        label = "Efficiency"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sleep Stages Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Sleep Stages", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                // Deep Sleep
//                SleepStageRow(
//                    label = "Deep Sleep",
//                    duration = sleepData.getDeepSleepMinutes(),
//                    color = Color(0xFF7E57C2)
//                )
//
//                // Light Sleep
//                SleepStageRow(
//                    label = "Light Sleep",
//                    duration = sleepData.getLightSleepMinutes(),
//                    color = Color(0xFFB39DDB)
//                )
//
//                // REM Sleep
//                SleepStageRow(
//                    label = "REM Sleep",
//                    duration = sleepData.getRemMinutes(),
//                    color = Color(0xFFE1BEE7)
//                )
//
//                // Awake
//                SleepStageRow(
//                    label = "Awake",
//                    duration = sleepData.getAwakeMinutes(),
//                    color = Color(0xFFFFE082)
//                )
            }
        }
    }
}

@Composable
fun SleepScoreCircle(score: Int) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(120.dp)
    ) {
        CircularProgressIndicator(
            progress = score / 100f,
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 8.dp,
            color = MaterialTheme.colorScheme.primary
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = score.toString(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Score",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun SimpleMetricCard(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun SleepStageRow(label: String, duration: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, fontSize = 14.sp)
        }
        Text("${duration}M", fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SleepStagesChart(sleepData: SleepData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem("Wake up", Color(0xFFFFE082))
                LegendItem("Rapid Eye Movement", Color(0xFFE1BEE7))
                LegendItem("Light sleep", Color(0xFFB39DDB))
                LegendItem("Deep sleep", Color(0xFF7E57C2))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sleep stages chart based on actual data
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Generate chart bars based on sleep stages
//                val chartBars = generateSleepChartBars(sleepData.stages)
//
//                chartBars.forEach { bar ->
//                    Box(
//                        modifier = Modifier
//                            .width(8.dp)
//                            .height(bar.height.dp)
//                            .background(
//                                bar.color,
//                                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
//                            )
//                    )
//                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Time labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(sleepData.totalDuration.toString(), fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 10.sp, color = Color.Gray)
    }
}
//
//@Composable
//fun SleepDetailsCard(sleepData: SleepData) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            // Calculate percentages
//            val awakePercentage = if (sleepData.totalDuration > 0) {
//                ((sleepData.awakeDuration.toFloat() / sleepData.totalDuration) * 100).toInt()
//            } else 0
//
//            val remPercentage = if (sleepData.totalDuration > 0) {
//                ((sleepData.remDuration.toFloat() / sleepData.totalDuration) * 100).toInt()
//            } else 0
//
//            val lightSleepPercentage = if (sleepData.totalDuration > 0) {
//                ((sleepData.lightSleepDuration.toFloat() / sleepData.totalDuration) * 100).toInt()
//            } else 0
//
//            val deepSleepPercentage = if (sleepData.totalDuration > 0) {
//                ((sleepData.deepSleepDuration.toFloat() / sleepData.totalDuration) * 100).toInt()
//            } else 0
//
//            DetailRow("Total awake time", "${sleepData.getAwakeMinutes()}M", "${awakePercentage}%")
//            Divider(modifier = Modifier.padding(vertical = 8.dp))
//            DetailRow("Rapid eye movement duration", "${sleepData.getRemMinutes()}M", "${remPercentage}%")
//            Divider(modifier = Modifier.padding(vertical = 8.dp))
//            DetailRow("Total light sleep duration", "${sleepData.getLightSleepMinutes()}M", "${lightSleepPercentage}%")
//            Divider(modifier = Modifier.padding(vertical = 8.dp))
//            DetailRow("Total deep sleep duration", "${sleepData.getDeepSleepMinutes()}M", "${deepSleepPercentage}%")
//        }
//    }
//}

@Composable
fun DetailRow(label: String, value: String, percentage: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp)
        Text("$value  $percentage", fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun LoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun EmptyContent() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Bedtime,
                contentDescription = "No Data",
                modifier = Modifier.size(48.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("No sleep data available", fontSize = 16.sp, color = Color.Gray)
            Text("Sync your device to see sleep data", fontSize = 12.sp, color = Color.LightGray)
        }
    }
}

@Composable
fun NoDataContent() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = "No Data",
                modifier = Modifier.size(48.dp),
                tint = Color(0xFFFF9800)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("No sleep data for this date", fontSize = 16.sp, color = Color(0xFFE65100))
            Text("Device may not have recorded sleep data", fontSize = 12.sp, color = Color(0xFFBF360C))
        }
    }
}

@Composable
fun ErrorContent(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Warning, "Error", tint = Color.Red)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Error: $message", fontSize = 14.sp, color = Color.Red)
        }
    }
}

// Utility Functions
private fun formatDateForDisplay(dateStr: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = inputFormat.parse(dateStr) ?: Date()
        outputFormat.format(date)
    } catch (e: Exception) {
        dateStr
    }
}

private fun getWeekDatesForCalendar(centerDate: String): List<Triple<String, String, String>> {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val calendar = Calendar.getInstance()

    try {
        calendar.time = dateFormat.parse(centerDate) ?: Date()
    } catch (e: Exception) {
        calendar.time = Date()
    }

    // Go back to start of week (Monday)
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

/**
 * Data class for sleep chart bars
 */
data class SleepChartBar(
    val height: Int,
    val color: Color
)

/**
 * Generate sleep chart bars from sleep stages data
 */
//private fun generateSleepChartBars(sleepStages: List<dev.infa.page3.viewmodels.SleepStage>): List<SleepChartBar> {
//    // If no sleep stages data, generate sample data
//    if (sleepStages.isEmpty()) {
//        return (0..23).map { index ->
//            val height = (50..180).random()
//            val color = when (index % 4) {
//                0 -> Color(0xFF7E57C2) // Deep sleep
//                1 -> Color(0xFFB39DDB) // Light sleep
//                2 -> Color(0xFFE1BEE7) // REM
//                else -> Color(0xFFFFE082) // Awake
//            }
//            SleepChartBar(height, color)
//        }
//    }
//
//    // Convert sleep stages to chart bars (24 bars for 24 hours)
//    val bars = mutableListOf<SleepChartBar>()
//
//    for (hour in 0..23) {
//        // Find sleep stages that overlap with this hour
//        val hourStart = hour * 3600 // Convert to seconds
//        val hourEnd = (hour + 1) * 3600
//
//        val stagesInHour = sleepStages.filter { stage ->
//            val stageEnd = stage.timestamp + (stage.duration * 60) // Convert duration to seconds
//            stage.timestamp < hourEnd && stageEnd > hourStart
//        }
//
//        // Determine the predominant sleep type for this hour
//        val predominantType = if (stagesInHour.isNotEmpty()) {
//            stagesInHour.maxByOrNull { it.duration }?.type ?: SleepType.LIGHT
//        } else {
//            SleepType.LIGHT // Default to light sleep if no data
//        }
//
//        val color = when (predominantType) {
//            SleepType.DEEP -> Color(0xFF7E57C2)
//            SleepType.LIGHT -> Color(0xFFB39DDB)
//            SleepType.REM -> Color(0xFFE1BEE7)
//            SleepType.AWAKE -> Color(0xFFFFE082)
//        }
//
//        // Calculate height based on sleep quality (simplified)
//        val height = when (predominantType) {
//            SleepType.DEEP -> (150..180).random()
//            SleepType.LIGHT -> (100..150).random()
//            SleepType.REM -> (80..120).random()
//            SleepType.AWAKE -> (30..80).random()
//        }
//
//        bars.add(SleepChartBar(height, color))
//    }
//
//    return bars
//}




