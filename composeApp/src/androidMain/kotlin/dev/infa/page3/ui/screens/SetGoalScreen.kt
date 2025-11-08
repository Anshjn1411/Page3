package dev.infa.page3.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.BorderStroke
import dev.infa.page3.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.infa.page3.models.DeviceCapabilities
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import dev.infa.page3.ui.components.AppSideBar
import dev.infa.page3.ui.components.BottomNavBar
import dev.infa.page3.ui.components.SDKTopBarScreen
import dev.infa.page3.ui.components.TopBarScreen
import dev.infa.page3.ui.navigation.Routes

import dev.infa.page3.ui.theme.Page3Theme
import dev.infa.page3.viewmodels.ConnectionViewModel
import dev.infa.page3.viewmodels.HomeViewModel
import dev.infa.page3.viewmodels.SleepViewModel

import dev.infa.page3.viewmodels.StepViewmodel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsSettingsScreen(
    homeViewModel: HomeViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // Collect states
    val stepGoal by homeViewModel.stepGoal.collectAsState()
    val calorieGoal by homeViewModel.calorieGoal.collectAsState()
    val distanceGoal by homeViewModel.distanceGoal.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    val goalSetSuccess by homeViewModel.goalSetSuccess.collectAsState()
    val errorMessage by homeViewModel.errorMessage.collectAsState()

    // Local editable states
    var editableStepGoal by remember { mutableStateOf(stepGoal.toString()) }
    var editableCalorieGoal by remember { mutableStateOf(calorieGoal.toString()) }
    var editableDistanceGoal by remember { mutableStateOf((distanceGoal / 1000f).toString()) } // Convert to km

    // Update local states when ViewModel states change
    LaunchedEffect(stepGoal, calorieGoal, distanceGoal) {
        editableStepGoal = stepGoal.toString()
        editableCalorieGoal = calorieGoal.toString()
        editableDistanceGoal = (distanceGoal / 1000f).toString()
    }

    // Show success/error messages
    LaunchedEffect(goalSetSuccess) {
        when (goalSetSuccess) {
            true -> {
                Log.d("GoalsSettings", "✅ Goals updated successfully!")
                homeViewModel.clearGoalSetStatus()
            }
            false -> {
                Log.e("GoalsSettings", "❌ Failed to update goals")
                homeViewModel.clearGoalSetStatus()
            }
            null -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Daily Goals",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4D96FF).copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFF4D96FF),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Set Your Daily Goals",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Stay motivated and track your progress",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // Step Goal
            item {
                GoalInputCard(
                    title = "Step Goal",
                    icon = Icons.Default.DirectionsWalk,
                    value = editableStepGoal,
                    unit = "steps",
                    iconColor = Color(0xFF4CAF50),
                    onValueChange = { editableStepGoal = it },
                    placeholder = "e.g., 10000"
                )
            }

            // Calorie Goal
            item {
                GoalInputCard(
                    title = "Calorie Goal",
                    icon = Icons.Default.LocalFireDepartment,
                    value = editableCalorieGoal,
                    unit = "kcal",
                    iconColor = Color(0xFFFF9800),
                    onValueChange = { editableCalorieGoal = it },
                    placeholder = "e.g., 500"
                )
            }

            // Distance Goal
            item {
                GoalInputCard(
                    title = "Distance Goal",
                    icon = Icons.Default.LocationOn,
                    value = editableDistanceGoal,
                    unit = "km",
                    iconColor = Color(0xFF2196F3),
                    onValueChange = { editableDistanceGoal = it },
                    placeholder = "e.g., 8.0"
                )
            }

            // Error message
            errorMessage?.let { error ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFEBEE)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = error,
                                color = Color(0xFFD32F2F),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // Save Button
            item {
                Button(
                    onClick = {
                        val steps = editableStepGoal.toIntOrNull() ?: stepGoal
                        val calories = editableCalorieGoal.toIntOrNull() ?: calorieGoal
                        val distance = ((editableDistanceGoal.toFloatOrNull() ?: (distanceGoal / 1000f)) * 1000).toInt()

                        Log.d("GoalsSettings", "Saving goals: Steps=$steps, Calories=$calories, Distance=$distance")

                        homeViewModel.setSportsGoals(
                            stepGoal = steps,
                            calorieGoal = calories,
                            distanceGoal = distance,
                            sportMinuteGoal = 30,
                            sleepMinuteGoal = 480
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4D96FF),
                        disabledContainerColor = Color(0xFFCCCCCC)
                    ),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Saving...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Save Goals",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Quick presets
            item {
                Text(
                    text = "Quick Presets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PresetButton(
                        text = "Beginner",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            editableStepGoal = "5000"
                            editableCalorieGoal = "300"
                            editableDistanceGoal = "4.0"
                        }
                    )
                    PresetButton(
                        text = "Active",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            editableStepGoal = "10000"
                            editableCalorieGoal = "500"
                            editableDistanceGoal = "8.0"
                        }
                    )
                    PresetButton(
                        text = "Athlete",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            editableStepGoal = "15000"
                            editableCalorieGoal = "800"
                            editableDistanceGoal = "12.0"
                        }
                    )
                }
            }

            // Bottom spacer
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun GoalInputCard(
    title: String,
    icon: ImageVector,
    value: String,
    unit: String,
    iconColor: Color,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(iconColor.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(placeholder) },
                suffix = { Text(unit, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = iconColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Composable
fun PresetButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color(0xFF4D96FF)
        ),
        border = BorderStroke(1.dp, Color(0xFF4D96FF).copy(alpha = 0.5f))
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}