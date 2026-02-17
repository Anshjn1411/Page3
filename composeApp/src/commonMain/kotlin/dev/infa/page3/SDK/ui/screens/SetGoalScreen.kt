package dev.infa.page3.SDK.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.SDK.ui.components.CommonTopAppBar
import dev.infa.page3.SDK.ui.theme.*
import dev.infa.page3.SDK.viewModel.HomeViewModel


// ==================== Goals Settings Screen Components ====================

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
            .clip(AppShapes.HealthMetricCard)
            .background(AppColors.SurfaceVariant)
            .border(
                AppDimensions.Border.Thin,
                AppColors.Primary.copy(alpha = 0.3f),
                AppShapes.HealthMetricCard
            )
            .padding(AppDimensions.CardPadding.Default)
    ) {
        Text(
            text = title,
            color = AppColors.TextPrimary,
            style = AppTypography.LabelLarge
        )

        Spacer(modifier = Modifier.height(AppDimensions.Spacing.Small))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            suffix = { Text(unit, color = AppColors.TextSecondary) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Primary,
                unfocusedBorderColor = AppColors.BorderSecondary,
                focusedTextColor = AppColors.TextPrimary,
                unfocusedTextColor = AppColors.TextPrimary,
                cursorColor = AppColors.Primary
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

@Composable
fun DarkPresetButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = AppShapes.ButtonMedium,
        border = BorderStroke(AppDimensions.Border.Thin, AppColors.Primary),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = AppColors.Primary
        ),
        modifier = Modifier.height(AppDimensions.ButtonHeight.Medium)
    ) {
        Text(text, style = AppTypography.ButtonMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsSettingsScreen(
    homeViewModel: HomeViewModel,
    navController: Navigator
) {
    val stepGoal by homeViewModel.stepGoal.collectAsState()
    val calorieGoal by homeViewModel.calorieGoal.collectAsState()
    val distanceGoal by homeViewModel.distanceGoal.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    val goalSetSuccess by homeViewModel.goalSetSuccess.collectAsState()
    val errorMessage by homeViewModel.errorMessage.collectAsState()

    var editableStepGoal by remember { mutableStateOf(stepGoal.toString()) }
    var editableCalorieGoal by remember { mutableStateOf(calorieGoal.toString()) }
    var editableDistanceGoal by remember { mutableStateOf((distanceGoal / 1000f).toString()) }

    LaunchedEffect(stepGoal, calorieGoal, distanceGoal) {
        editableStepGoal = stepGoal.toString()
        editableCalorieGoal = calorieGoal.toString()
        editableDistanceGoal = (distanceGoal / 1000f).toString()
    }

    LaunchedEffect(goalSetSuccess) {
        goalSetSuccess?.let {
            homeViewModel.clearGoalSetStatus()
        }
    }

    Scaffold(
        topBar = {
            CommonTopAppBar("Daily Goals", {
                navController.pop()
            })
        },
        containerColor = AppColors.BackgroundPrimary
    ) { padding ->
        if (goalSetSuccess == true) {
            SuccessDialog(
                title = "Goals Updated 🎯",
                message = "Your daily goals have been saved successfully.",
                onDismiss = {
                    homeViewModel.clearGoalSetStatus()
                    navController.pop()
                }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(AppDimensions.ScreenPadding.Content),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.XXL)
        ) {
            item {
                Text(
                    text = "Set Your Daily Performance Targets",
                    color = AppColors.TextTertiary,
                    style = AppTypography.LabelSmall
                )
            }

            item {
                DarkGoalInputCard(
                    title = "Step Goal",
                    value = editableStepGoal,
                    unit = "steps",
                    onValueChange = { editableStepGoal = it }
                )
            }

            item {
                DarkGoalInputCard(
                    title = "Calorie Goal",
                    value = editableCalorieGoal,
                    unit = "kcal",
                    onValueChange = { editableCalorieGoal = it }
                )
            }

            item {
                DarkGoalInputCard(
                    title = "Distance Goal",
                    value = editableDistanceGoal,
                    unit = "km",
                    onValueChange = { editableDistanceGoal = it }
                )
            }

            errorMessage?.let { error ->
                item {
                    Text(
                        text = error,
                        color = AppColors.Error,
                        style = AppTypography.LabelSmall
                    )
                }
            }

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
                        .height(AppDimensions.ButtonHeight.Large),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Primary
                    ),
                    shape = AppShapes.ButtonMedium,
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(AppDimensions.IconSize.Medium),
                            color = AppColors.BackgroundPrimary
                        )
                    } else {
                        Text(
                            "Save Goals",
                            color = AppColors.BackgroundPrimary,
                            style = AppTypography.ButtonLarge
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Quick Presets",
                    color = AppColors.Primary,
                    style = AppTypography.LabelLarge
                )
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.Medium)
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
        containerColor = AppColors.BackgroundCard,
        shape = AppShapes.Dialog,
        icon = {
            Box(
                modifier = Modifier
                    .size(AppDimensions.IconSize.Huge)
                    .background(AppColors.Primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = AppColors.BackgroundPrimary,
                    modifier = Modifier.size(AppDimensions.IconSize.ExtraLarge)
                )
            }
        },
        title = {
            Text(
                text = title,
                style = AppTypography.HeadingSmall,
                color = AppColors.TextPrimary
            )
        },
        text = {
            Text(
                text = message,
                color = AppColors.TextTertiary,
                style = AppTypography.BodySmall
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "OK",
                    color = AppColors.Primary,
                    style = AppTypography.ButtonMedium
                )
            }
        }
    )
}
