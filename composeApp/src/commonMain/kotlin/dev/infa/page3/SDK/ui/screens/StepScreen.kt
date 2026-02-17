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
import dev.infa.page3.SDK.ui.theme.*
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
        containerColor = AppColors.BackgroundPrimary,
        topBar = {
            CommonTopAppBar(title = "Activity",  onBack)
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
                .background(AppColors.BackgroundPrimary)
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                DateSelector(
                    selectedDate = selectedDate,
                    onDateChange = { newDate ->
                        selectedDate = newDate
                    }
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(
                            horizontal = AppDimensions.ScreenPadding.Horizontal,
                            vertical = AppDimensions.Spacing.Huge
                        ),
                    verticalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.ExtraLarge)
                ) {
                    MainStepsCard(steps = todaySteps, stepGoal)
                    WeeklyChartCard(weeklySummary = weeklySummary)
                    HourlyActivityCard(hourlyData = hourlyData)
                    AdditionalStatsRow(weeklySummary = weeklySummary)
                    PersonalRecordCard(weeklySummary = weeklySummary)
                    Spacer(modifier = Modifier.height(AppDimensions.Spacing.ExtraLarge))
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
            .background(AppColors.BackgroundPrimary)
            .padding(
                horizontal = AppDimensions.ScreenPadding.Horizontal,
                vertical = AppDimensions.ScreenPadding.Vertical
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = AppColors.TextPrimary
            )
        }
        Spacer(modifier = Modifier.width(AppDimensions.Spacing.Medium))
        Text(
            text = title,
            color = AppColors.TextPrimary,
            style = AppTypography.HeadingMedium
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
        colors = CardDefaults.cardColors(containerColor = AppColors.OverlayLight),
        shape = AppShapes.CardExtraLarge
    ) {
        Column(
            modifier = Modifier.padding(AppDimensions.CardPadding.ExtraLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total Steps",
                color = AppColors.TextSecondary,
                style = AppTypography.BodySmall
            )
            Spacer(modifier = Modifier.height(AppDimensions.Spacing.Medium))

            Text(
                text = FormatUtils.formatNumber(animatedSteps),
                style = AppTypography.DisplayMedium.copy(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            AppColors.GradientStart,
                            AppColors.GradientEnd
                        )
                    )
                )
            )

            Text(
                text = "of ${FormatUtils.formatNumber(stepGoal)} goal",
                color = AppColors.TextSecondary,
                style = AppTypography.BodySmall,
                modifier = Modifier.padding(top = AppDimensions.Spacing.Medium)
            )

            Spacer(modifier = Modifier.height(AppDimensions.Spacing.Huge))

            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppDimensions.Spacing.Default)
                    .clip(AppShapes.Progress)
                    .background(AppColors.BackgroundTertiary)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(AppShapes.Progress)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    AppColors.GradientStart,
                                    AppColors.PrimaryVariant,
                                    AppColors.GradientEnd
                                )
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .blur(AppDimensions.Elevation.Medium)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(AppColors.GradientStart, AppColors.GradientEnd)
                            )
                        )
                        .alpha(AppAlpha.Medium)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AppDimensions.Spacing.Medium),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("0", color = AppColors.TextSecondary, style = AppTypography.LabelMedium)
                Text(
                    FormatUtils.formatNumber(stepGoal / 2),
                    color = AppColors.TextSecondary,
                    style = AppTypography.LabelMedium
                )
                Text(
                    FormatUtils.formatNumber(stepGoal),
                    color = AppColors.TextSecondary,
                    style = AppTypography.LabelMedium
                )
            }

            Spacer(modifier = Modifier.height(AppDimensions.Spacing.Huge))

            // Quick Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.ExtraLarge)
            ) {
                QuickStatItem(
                    icon = Icons.Default.Place,
                    value = FormatUtils.formatDistance(steps),
                    label = "Distance",
                    color = AppColors.Secondary,
                    modifier = Modifier.weight(1f)
                )
                QuickStatItem(
                    icon = Icons.Default.LocalFireDepartment,
                    value = FormatUtils.formatNumber((steps * 0.04).toInt()),
                    label = "Calories",
                    color = AppColors.Primary,
                    modifier = Modifier.weight(1f)
                )
                QuickStatItem(
                    icon = Icons.Default.Bolt,
                    value = "${(steps / 180)}m",
                    label = "Active",
                    color = AppColors.Secondary,
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
        colors = CardDefaults.cardColors(containerColor = AppColors.OverlayDark),
        shape = AppShapes.CardSmall
    ) {
        Column(
            modifier = Modifier.padding(AppDimensions.CardPadding.Small),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(AppDimensions.IconSize.Default)
            )
            Spacer(modifier = Modifier.height(AppDimensions.Spacing.Medium))
            Text(
                text = value,
                color = AppColors.TextPrimary,
                style = AppTypography.BodySmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(text = label, color = AppColors.TextSecondary, style = AppTypography.LabelSmall)
        }
    }
}

@Composable
fun WeeklyChartCard(weeklySummary: WeeklySummary) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    val todayDate = DateUtils.getCurrentDate()
    val todayIndex = DateUtils.getDayOfWeekIndex(todayDate)

    val weeklySteps = (0..6).map { dayIndex ->
        if (dayIndex <= todayIndex) {
            val offset = todayIndex - dayIndex
            weeklySummary.allDays.getOrNull(offset)?.totalSteps ?: 0
        } else {
            0
        }
    }

    val maxSteps = weeklySteps.maxOrNull()?.coerceAtLeast(1) ?: 1

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.OverlayLight),
        shape = AppShapes.CardMedium
    ) {
        Column(modifier = Modifier.padding(AppDimensions.CardPadding.ExtraLarge)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "7-Day Trend",
                    color = AppColors.TextPrimary,
                    style = AppTypography.HeadingExtraSmall
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(AppDimensions.IconSize.Default)
                )
            }

            Spacer(modifier = Modifier.height(AppDimensions.Spacing.ExtraLarge))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppDimensions.ChartHeight.Large),
                horizontalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.Medium),
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
                                            colors = listOf(
                                                AppColors.GradientStart,
                                                AppColors.GradientEnd
                                            )
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                AppColors.TextTertiary,
                                                AppColors.BackgroundTertiary
                                            )
                                        )
                                    }
                                )
                        )

                        Spacer(modifier = Modifier.height(AppDimensions.Spacing.Medium))

                        Text(
                            text = days[index],
                            color = if (isActive) AppColors.TextPrimary else AppColors.TextSecondary,
                            style = AppTypography.LabelMedium
                        )

                        if (isActive) {
                            Spacer(modifier = Modifier.height(AppDimensions.Spacing.ExtraSmall))
                            Box(
                                modifier = Modifier
                                    .size(AppDimensions.Spacing.ExtraSmall)
                                    .clip(CircleShape)
                                    .background(AppColors.Primary)
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
        colors = CardDefaults.cardColors(containerColor = AppColors.OverlayLight),
        shape = AppShapes.CardMedium
    ) {
        Column(modifier = Modifier.padding(AppDimensions.CardPadding.ExtraLarge)) {
            Text(
                "Hourly Breakdown",
                color = AppColors.TextPrimary,
                style = AppTypography.HeadingExtraSmall
            )

            Spacer(modifier = Modifier.height(AppDimensions.Spacing.ExtraLarge))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppDimensions.ChartHeight.Medium),
                horizontalArrangement = Arrangement.spacedBy(AppDimensions.Border.Medium),
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
                            .clip(AppShapes.ChartBar)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        AppColors.Secondary.copy(alpha = 0.8f),
                                        AppColors.Secondary.copy(alpha = 0.4f)
                                    )
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppDimensions.Spacing.Medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("12 AM", color = AppColors.TextSecondary, style = AppTypography.LabelMedium)
                Text("12 PM", color = AppColors.TextSecondary, style = AppTypography.LabelMedium)
                Text("11 PM", color = AppColors.TextSecondary, style = AppTypography.LabelMedium)
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
        horizontalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.Default)
    ) {
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = AppColors.OverlayLight),
            shape = AppShapes.CardMedium
        ) {
            Column(modifier = Modifier.padding(AppDimensions.Spacing.ExtraLarge)) {
                Icon(
                    imageVector = Icons.Default.DirectionsWalk,
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(AppDimensions.IconSize.Default)
                )
                Spacer(modifier = Modifier.height(AppDimensions.Spacing.Medium))
                Text(
                    text = FormatUtils.formatNumber(avgStepsPerHour),
                    color = AppColors.TextPrimary,
                    style = AppTypography.ValueMedium
                )
                Text(
                    "Steps/Hour Avg",
                    color = AppColors.TextSecondary,
                    style = AppTypography.LabelSmall
                )
            }
        }

        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = AppColors.OverlayLight),
            shape = AppShapes.CardMedium
        ) {
            Column(modifier = Modifier.padding(AppDimensions.Spacing.ExtraLarge)) {
                Icon(
                    imageVector = Icons.Default.GpsFixed,
                    contentDescription = null,
                    tint = AppColors.Secondary,
                    modifier = Modifier.size(AppDimensions.IconSize.Default)
                )
                Spacer(modifier = Modifier.height(AppDimensions.Spacing.Medium))
                Text(
                    text = "$goalsThisWeek/7",
                    color = AppColors.TextPrimary,
                    style = AppTypography.ValueMedium
                )
                Text(
                    "Goals This Week",
                    color = AppColors.TextSecondary,
                    style = AppTypography.LabelSmall
                )
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
            containerColor = AppColors.Primary.copy(alpha = AppAlpha.Subtle)
        ),
        shape = AppShapes.CardMedium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimensions.CardPadding.ExtraLarge),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.Medium)
                ) {
                    Box(
                        modifier = Modifier
                            .size(AppDimensions.Spacing.Medium)
                            .clip(CircleShape)
                            .background(AppColors.Primary)
                    )
                    Text(
                        "Personal Best",
                        color = AppColors.TextSecondary,
                        style = AppTypography.BodySmall
                    )
                }

                Spacer(modifier = Modifier.height(AppDimensions.Spacing.Medium))

                Text(
                    text = FormatUtils.formatNumber(bestDay?.totalSteps ?: 15680),
                    color = AppColors.TextPrimary,
                    style = AppTypography.DisplaySmall
                )

                Text(
                    text = bestDay?.date ?: "Nov 28, 2024",
                    color = AppColors.TextSecondary,
                    style = AppTypography.BodySmall,
                    modifier = Modifier.padding(top = AppDimensions.Spacing.ExtraSmall)
                )
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.Primary.copy(alpha = AppAlpha.VeryLight)
                ),
                shape = AppShapes.CardSmall
            ) {
                Box(modifier = Modifier.padding(AppDimensions.Spacing.Default)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = AppColors.Primary,
                        modifier = Modifier.size(AppDimensions.IconSize.Large)
                    )
                }
            }
        }
    }
}

fun Double.format(digits: Int) = FormatUtils.formatNumber(digits)


