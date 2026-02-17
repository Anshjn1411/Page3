package dev.infa.page3.SDK.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.SDK.ui.components.*
import dev.infa.page3.SDK.ui.navigation.*
import dev.infa.page3.SDK.ui.theme.*
import dev.infa.page3.SDK.ui.utils.FormatUtils
import dev.infa.page3.SDK.viewModel.*

@Composable
fun ExerciseScreen(
    navController: Navigator,
    viewModel: HomeViewModel
) {
    val exerciseData by viewModel.currentExercise.collectAsState()
    val lastSummary by viewModel.lastExerciseSummary.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var selectedSportType by remember { mutableStateOf(1) }
    var showSummary by remember { mutableStateOf(false) }

    LaunchedEffect(lastSummary) {
        if (lastSummary != null) {
            showSummary = true
        }
    }

    var currentTab by remember { mutableStateOf(BottomTab.STRAIN) }

    val strain = remember(exerciseData) {
        exerciseData?.let { ExerciseUtils.calculateStrain(it) } ?: 0f
    }

    Scaffold(
        containerColor = AppColors.BackgroundPrimary,
        topBar = {
            CommonTopAppBar("Exercise"  ,  onBackClick = {navController.pop()})
        },
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
                .background(AppColors.BackgroundPrimary)
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

            errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.Error.copy(alpha = AppAlpha.VeryLight)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(AppDimensions.Spacing.Default),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            error,
                            color = AppColors.Error,
                            style = AppTypography.LabelMedium
                        )
                        TextButton(onClick = { }) {
                            Text("✕", color = AppColors.Error)
                        }
                    }
                }
                Spacer(Modifier.height(AppDimensions.Spacing.Default))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (exerciseData?.isActive == true)
                        AppColors.Selected else AppColors.Unselected
                )
            ) {
                Column(
                    modifier = Modifier.padding(AppDimensions.CardPadding.Large),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        when {
                            exerciseData?.isPaused == true -> "Workout Paused"
                            exerciseData?.isActive == true -> "Workout Active"
                            else -> "Ready To Train"
                        },
                        color = if (exerciseData?.isPaused == true)
                            AppColors.Warning else AppColors.TextSecondary
                    )

                    Spacer(Modifier.height(AppDimensions.Spacing.Default))

                    Text(
                        exerciseData?.getFormattedDuration() ?: "00:00:00",
                        style = AppTypography.DisplayMedium,
                        color = if (exerciseData?.isActive == true)
                            AppColors.TextPrimary else AppColors.TextSecondary
                    )

                    if (exerciseData?.isActive == true) {
                        Spacer(Modifier.height(AppDimensions.Spacing.Medium))

                        Text(
                            "Strain: ${FormatUtils.formatDecimal(strain.toDouble(), 1)}",
                            style = AppTypography.HeadingLarge,
                            color = getStrainColor(strain)
                        )

                        Spacer(Modifier.height(AppDimensions.Spacing.Medium))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(AppDimensions.Spacing.Medium)
                                .background(AppColors.BorderSecondary, AppShapes.Progress)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth((strain / 21f).coerceIn(0f, 1f))
                                    .height(AppDimensions.Spacing.Medium)
                                    .background(getStrainColor(strain), AppShapes.Progress)
                            )
                        }
                    }

                    Spacer(Modifier.height(AppDimensions.CardPadding.Large))

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

                    Spacer(Modifier.height(AppDimensions.CardPadding.Large))

                    if (exerciseData?.isActive == true) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.Medium)
                        ) {
                            Button(
                                onClick = {
                                    if (exerciseData?.isPaused == true) {
                                        viewModel.resumeExercise()
                                    } else {
                                        viewModel.pauseExercise()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.Secondary
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    if (exerciseData?.isPaused == true) "Resume" else "Pause",
                                    color = AppColors.TextPrimary
                                )
                            }

                            Button(
                                onClick = { viewModel.endExercise() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.Error
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("End Workout", color = AppColors.TextPrimary)
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                viewModel.startExercise(sportType = selectedSportType)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.Primary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Start ${ExerciseUtils.getSportName(selectedSportType)}",
                                color = AppColors.BackgroundPrimary,
                                style = AppTypography.ButtonLarge
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(AppDimensions.CardPadding.Large))

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
                    Spacer(Modifier.height(AppDimensions.Spacing.ExtraLarge))
                }
            }

            AnimatedVisibility(exerciseData?.isActive != true && !showSummary) {
                Column {
                    Text(
                        "Activity Type",
                        color = AppColors.TextPrimary,
                        style = AppTypography.LabelLarge
                    )
                    Spacer(Modifier.height(AppDimensions.Spacing.Medium))

                    val sportTypes = ExerciseUtils.getPopularSportTypes()

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

                    Spacer(Modifier.height(AppDimensions.Spacing.Medium))

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

            Spacer(Modifier.height(AppDimensions.CardPadding.Large))

            AnimatedVisibility(showSummary && lastSummary != null) {
                Column {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = AppColors.Unselected
                        )
                    ) {
                        Column(modifier = Modifier.padding(AppDimensions.CardPadding.Default)) {
                            Text(
                                "Workout Complete! 🎉",
                                color = AppColors.Primary,
                                style = AppTypography.HeadingSmall
                            )

                            Spacer(Modifier.height(AppDimensions.Spacing.Default))

                            lastSummary?.let { summary ->
                                Text(
                                    summary.sportName,
                                    color = AppColors.TextPrimary,
                                    style = AppTypography.HeadingExtraSmall
                                )
                                Spacer(Modifier.height(AppDimensions.Spacing.Medium))

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

                                Spacer(Modifier.height(AppDimensions.Spacing.Medium))

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

                            Spacer(Modifier.height(AppDimensions.Spacing.ExtraLarge))

                            Button(
                                onClick = {
                                    showSummary = false
                                    viewModel.clearLastSummary()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.Primary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Done",
                                    color = AppColors.BackgroundPrimary,
                                    style = AppTypography.ButtonLarge
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(AppDimensions.Spacing.ExtraLarge))
                }
            }
        }
    }
}

@Composable
fun StatItem(icon: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, style = AppTypography.HeadingMedium)
        Spacer(Modifier.height(AppDimensions.Spacing.ExtraSmall))
        Text(
            value,
            color = AppColors.TextPrimary,
            style = AppTypography.BodyLarge
        )
        Text(unit, color = AppColors.TextSecondary, style = AppTypography.LabelMedium)
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
            .width(AppDimensions.Component.ExerciseTypeCardWidth)
            .clip(AppShapes.ExerciseTypeCard)
            .background(
                if (isSelected) AppColors.Selected else AppColors.Unselected
            )
            .border(
                width = if (isSelected) AppDimensions.Border.Medium else AppDimensions.Border.Thin,
                color = if (isSelected) AppColors.SelectedBorder else Color.Transparent,
                shape = AppShapes.ExerciseTypeCard
            )
            .clickable { onClick() }
            .padding(AppDimensions.Component.ExerciseTypeCardPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(icon, style = AppTypography.HeadingMedium)
        Spacer(Modifier.height(AppDimensions.Spacing.ExtraSmall))
        Text(
            name,
            color = if (isSelected) AppColors.Primary else AppColors.TextPrimary,
            style = if (isSelected) AppTypography.LabelLarge else AppTypography.LabelMedium
        )
    }
}

@Composable
fun InfoCard(value: String, label: String) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .clip(AppShapes.CardMedium)
            .background(AppColors.Unselected)
            .padding(AppDimensions.CardPadding.Default)
    ) {
        Text(
            value,
            color = AppColors.TextPrimary,
            style = AppTypography.BodyMedium
        )
        Spacer(Modifier.height(AppDimensions.Spacing.ExtraSmall))
        Text(label, color = AppColors.TextSecondary, style = AppTypography.LabelMedium)
    }
}

@Composable
fun SummaryDetail(icon: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, style = AppTypography.HeadingExtraSmall)
        Spacer(Modifier.height(AppDimensions.Spacing.ExtraSmall))
        Text(value, color = AppColors.TextPrimary, style = AppTypography.BodySmall)
    }
}

fun getStrainColor(value: Float): Color {
    return when {
        value < 7 -> AppColors.TextSecondary
        value < 14 -> AppColors.Secondary
        else -> AppColors.Primary
    }
}
