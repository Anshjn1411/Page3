package dev.infa.page3.SDK.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.SDK.ui.components.BottomNavBar
import dev.infa.page3.SDK.ui.components.BottomTab
import dev.infa.page3.SDK.ui.navigation.ExerciseScreenSDK
import dev.infa.page3.SDK.ui.navigation.HeartRateScreenSDK
import dev.infa.page3.SDK.ui.navigation.HomeScreenSDK
import dev.infa.page3.SDK.ui.navigation.ProfileScreenSDK
import dev.infa.page3.SDK.ui.navigation.StepsScreenSDK
import dev.infa.page3.SDK.ui.utils.FormatUtils
import dev.infa.page3.SDK.viewModel.ExerciseUtils
import dev.infa.page3.SDK.viewModel.HomeViewModel

@Composable
fun ExerciseScreen(
    navController : Navigator,
    viewModel: HomeViewModel
) {
    // Collect states from ViewModel
    val exerciseData by viewModel.currentExercise.collectAsState()
    val lastSummary by viewModel.lastExerciseSummary.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var selectedSportType by remember { mutableStateOf(1) } // Default: Running
    var showSummary by remember { mutableStateOf(false) }

    // Show summary when we receive one
    LaunchedEffect(lastSummary) {
        if (lastSummary != null) {
            showSummary = true
        }
    }
    var currentTab by remember { mutableStateOf(BottomTab.STRAIN) }

    // Calculate strain based on duration and heart rate
    val strain = remember(exerciseData) {
        exerciseData?.let { ExerciseUtils.calculateStrain(it) } ?: 0f
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
                        BottomTab.STRAIN -> navController.replace(ExerciseScreenSDK())
                        BottomTab.RECOVERY -> navController.push(HeartRateScreenSDK())
                        BottomTab.STEP -> navController.push(StepsScreenSDK())
                        BottomTab.PROFILE -> navController.push(ProfileScreenSDK())
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header with back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {  }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    "Strain & Activity",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(16.dp))

            // Error Message
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0x22FF0000))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(error, color = Color.Red, fontSize = 12.sp)
                        TextButton(onClick = { /* Clear error via ViewModel if needed */ }) {
                            Text("✕", color = Color.Red)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // ACTIVE WORKOUT CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (exerciseData?.isActive == true)
                        Color(0x2200FF88) else Color(0x22111111)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        when {
                            exerciseData?.isPaused == true -> "Workout Paused"
                            exerciseData?.isActive == true -> "Workout Active"
                            else -> "Ready To Train"
                        },
                        color = if (exerciseData?.isPaused == true) Color.Yellow else Color.Gray
                    )

                    Spacer(Modifier.height(12.dp))

                    // Timer Display
                    Text(
                        exerciseData?.getFormattedDuration() ?: "00:00:00",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (exerciseData?.isActive == true) Color.White else Color.Gray
                    )

                    // Strain Display (when active)
                    if (exerciseData?.isActive == true) {
                        Spacer(Modifier.height(10.dp))

                        Text(
                            "Strain: ${FormatUtils.formatDecimal(strain.toDouble(), 1)}",
                            fontSize = 32.sp,
                            color = getStrainColor(strain)
                        )

                        Spacer(Modifier.height(8.dp))

                        // Strain Progress Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(Color.DarkGray, RoundedCornerShape(50))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth((strain / 21f).coerceIn(0f, 1f))
                                    .height(8.dp)
                                    .background(getStrainColor(strain), RoundedCornerShape(50))
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // STATS GRID
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem(
                            "🔥",
                            FormatUtils.formatNumber(exerciseData?.calories ?: 0),
                            "kcal"
                        )
                        StatItem(
                            "❤️",
                            (exerciseData?.heartRate ?: 0).toString(),
                            "BPM"
                        )
                        StatItem(
                            "👣",
                            FormatUtils.formatNumber(exerciseData?.steps ?: 0),
                            "steps"
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // Control Buttons
                    if (exerciseData?.isActive == true) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Pause/Resume Button
                            Button(
                                onClick = {
                                    if (exerciseData?.isPaused == true) {
                                        viewModel.resumeExercise()
                                    } else {
                                        viewModel.pauseExercise()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF3B82F6)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    if (exerciseData?.isPaused == true) "Resume" else "Pause",
                                    color = Color.White
                                )
                            }

                            // End Button
                            Button(
                                onClick = { viewModel.endExercise() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Red
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("End Workout", color = Color.White)
                            }
                        }
                    } else {
                        // Start Button
                        Button(
                            onClick = {
                                viewModel.startExercise(sportType = selectedSportType)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00FF88)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Start ${ExerciseUtils.getSportName(selectedSportType)}",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // LIVE STATS (when active)
            AnimatedVisibility(exerciseData?.isActive == true) {
                Column {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InfoCard(
                            "📍 ${exerciseData?.getFormattedDistanceFromMeters() ?: "0 m"}",
                            "Distance"
                        )
                        InfoCard(
                            "⚡ ${
                                exerciseData?.let {
                                    if (it.elapsedSeconds > 0) {
                                        FormatUtils.formatDecimal(
                                            it.calories.toDouble() / it.elapsedSeconds * 60,
                                            1
                                        )
                                    } else "0.0"
                                } ?: "0.0"
                            }",
                            "kcal/min"
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            // EXERCISE TYPE SELECTOR (when inactive)
            AnimatedVisibility(exerciseData?.isActive != true && !showSummary) {
                Column {
                    Text("Activity Type", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))

                    val sportTypes = ExerciseUtils.getPopularSportTypes()

                    // First row
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ExerciseTypeButton(
                            "🏃", sportTypes[0].second, sportTypes[0].first,
                            selectedSportType == sportTypes[0].first
                        ) { selectedSportType = sportTypes[0].first }

                        ExerciseTypeButton(
                            "🚴", sportTypes[1].second, sportTypes[1].first,
                            selectedSportType == sportTypes[1].first
                        ) { selectedSportType = sportTypes[1].first }

                        ExerciseTypeButton(
                            "🚶", sportTypes[2].second, sportTypes[2].first,
                            selectedSportType == sportTypes[2].first
                        ) { selectedSportType = sportTypes[2].first }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Second row
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ExerciseTypeButton(
                            "🏊", sportTypes[5].second, sportTypes[5].first,
                            selectedSportType == sportTypes[5].first
                        ) { selectedSportType = sportTypes[5].first }

                        ExerciseTypeButton(
                            "🧘", sportTypes[6].second, sportTypes[6].first,
                            selectedSportType == sportTypes[6].first
                        ) { selectedSportType = sportTypes[6].first }

                        ExerciseTypeButton(
                            "💪", sportTypes[9].second, sportTypes[9].first,
                            selectedSportType == sportTypes[9].first
                        ) { selectedSportType = sportTypes[9].first }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // WORKOUT SUMMARY (after ending)
            AnimatedVisibility(showSummary && lastSummary != null) {
                Column {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0x22111111)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Workout Complete! 🎉",
                                color = Color(0xFF00FF88),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(12.dp))

                            lastSummary?.let { summary ->
                                Text(
                                    summary.sportName,
                                    color = Color.White,
                                    fontSize = 18.sp
                                )
                                Spacer(Modifier.height(8.dp))

                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    SummaryDetail("⏱️", summary.getFormattedDuration())
                                    SummaryDetail("📍", summary.getFormattedDistance())
                                    SummaryDetail(
                                        "🔥",
                                        "${FormatUtils.formatNumber(summary.calories)} kcal"
                                    )
                                }

                                Spacer(Modifier.height(8.dp))

                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    SummaryDetail("❤️", "${summary.averageHeartRate} bpm")
                                    SummaryDetail(
                                        "👣",
                                        "${FormatUtils.formatNumber(summary.steps)} steps"
                                    )
                                    SummaryDetail(
                                        "💪",
                                        FormatUtils.formatDecimal(strain.toDouble(), 1)
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    showSummary = false
                                    viewModel.clearLastSummary()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00FF88)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Done", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun StatItem(icon: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 22.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(unit, color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun ExerciseTypeButton(
    icon: String,
    name: String,
    sportType: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) Color(0x4400FF88) else Color(0x22111111)
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) Color(0xFF00FF88) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(icon, fontSize = 24.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            name,
            color = if (isSelected) Color(0xFF00FF88) else Color.White,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun InfoCard(value: String, label: String) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x22111111))
            .padding(16.dp)
    ) {
        Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(label, color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun SummaryDetail(icon: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 18.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, color = Color.White, fontSize = 14.sp)
    }
}

fun getStrainColor(value: Float): Color {
    return when {
        value < 7 -> Color.Gray
        value < 14 -> Color(0xFF3B82F6)
        else -> Color(0xFF00FF88)
    }
}


