package dev.infa.page3.SDK.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardType
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.SDK.ui.components.CommonTopAppBar
import dev.infa.page3.SDK.viewModel.HomeViewModel

@Composable
fun DarkGoalInputCard(
    title: String,
    value: String,
    unit: String,
    onValueChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF111111))
            .border(
                1.dp,
                Color(0xFF00FF88).copy(alpha = 0.3f),
                RoundedCornerShape(18.dp)
            )
            .padding(16.dp)
    ) {

        Text(text = title, color = Color.White, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            suffix = { Text(unit, color = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00FF88),
                unfocusedBorderColor = Color.DarkGray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFF00FF88)
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}
@Composable
fun DarkPresetButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF00FF88)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color(0xFF00FF88)
        ),
        modifier = Modifier.height(48.dp)
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsSettingsScreen(
    homeViewModel: HomeViewModel,
    navController: Navigator
) {
    // ✅ ViewModel State (READ ONLY)
    val stepGoal by homeViewModel.stepGoal.collectAsState()
    val calorieGoal by homeViewModel.calorieGoal.collectAsState()
    val distanceGoal by homeViewModel.distanceGoal.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    val goalSetSuccess by homeViewModel.goalSetSuccess.collectAsState()
    val errorMessage by homeViewModel.errorMessage.collectAsState()

    // ✅ Local Editable State (ONLY UI edits)
    var editableStepGoal by remember { mutableStateOf(stepGoal.toString()) }
    var editableCalorieGoal by remember { mutableStateOf(calorieGoal.toString()) }
    var editableDistanceGoal by remember { mutableStateOf((distanceGoal / 1000f).toString()) }

    // ✅ Sync UI when ViewModel changes
    LaunchedEffect(stepGoal, calorieGoal, distanceGoal) {
        editableStepGoal = stepGoal.toString()
        editableCalorieGoal = calorieGoal.toString()
        editableDistanceGoal = (distanceGoal / 1000f).toString()
    }

    // ✅ Success / Error Reset
    LaunchedEffect(goalSetSuccess) {
        goalSetSuccess?.let {
            homeViewModel.clearGoalSetStatus()
        }
    }

    Scaffold(
        topBar = {
            CommonTopAppBar("Daily Goals" , {
                navController.pop()
            }
            )
        },
        containerColor = Color.Black
    ) { padding ->
        if (goalSetSuccess == true) {
            SuccessDialog(
                title = "Goals Updated 🎯",
                message = "Your daily goals have been saved successfully.",
                onDismiss = {
                    homeViewModel.clearGoalSetStatus()
                    navController.pop() // optional: go back
                }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            item {
                Text(
                    text = "Set Your Daily Performance Targets",
                    color = Color(0xFF9E9E9E),
                    fontSize = 13.sp
                )
            }

            // ✅ STEP GOAL
            item {
                DarkGoalInputCard(
                    title = "Step Goal",
                    value = editableStepGoal,
                    unit = "steps",
                    onValueChange = { editableStepGoal = it }
                )
            }

            // ✅ CALORIE GOAL
            item {
                DarkGoalInputCard(
                    title = "Calorie Goal",
                    value = editableCalorieGoal,
                    unit = "kcal",
                    onValueChange = { editableCalorieGoal = it }
                )
            }

            // ✅ DISTANCE GOAL
            item {
                DarkGoalInputCard(
                    title = "Distance Goal",
                    value = editableDistanceGoal,
                    unit = "km",
                    onValueChange = { editableDistanceGoal = it }
                )
            }

            // ✅ ERROR MESSAGE
            errorMessage?.let { error ->
                item {
                    Text(
                        text = error,
                        color = Color(0xFFFF5252),
                        fontSize = 13.sp
                    )
                }
            }

            // ✅ SAVE BUTTON (REAL VM CALL)
            item {
                Button(
                    onClick = {
                        val steps = editableStepGoal.toIntOrNull() ?: stepGoal
                        val calories = editableCalorieGoal.toIntOrNull() ?: calorieGoal
                        val distance =
                            ((editableDistanceGoal.toFloatOrNull()
                                ?: (distanceGoal / 1000f)) * 1000).toInt()

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
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00FF88)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.Black
                        )
                    } else {
                        Text(
                            "Save Goals",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ✅ PRESETS
            item {
                Text(
                    text = "Quick Presets",
                    color = Color(0xFF00FF88),
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DarkPresetButton("Beginner") {
                        editableStepGoal = "5000"
                        editableCalorieGoal = "300"
                        editableDistanceGoal = "4.0"
                    }
                    DarkPresetButton("Active") {
                        editableStepGoal = "10000"
                        editableCalorieGoal = "500"
                        editableDistanceGoal = "8.0"
                    }
                    DarkPresetButton("Athlete") {
                        editableStepGoal = "15000"
                        editableCalorieGoal = "800"
                        editableDistanceGoal = "12.0"
                    }
                }
            }
        }
    }
}

@Composable
fun SuccessDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0E1A14),
        shape = RoundedCornerShape(20.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFF00FF88), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 20.sp
            )
        },
        text = {
            Text(
                text = message,
                color = Color(0xFFB0B0B0),
                fontSize = 14.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "OK",
                    color = Color(0xFF00FF88),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}
