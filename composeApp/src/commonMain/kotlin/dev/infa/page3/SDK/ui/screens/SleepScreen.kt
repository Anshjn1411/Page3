package dev.infa.page3.SDK.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import dev.infa.page3.SDK.ui.components.CommonTopAppBar
import dev.infa.page3.SDK.ui.components.DateSelector
import dev.infa.page3.SDK.ui.theme.*
import dev.infa.page3.SDK.ui.utils.DateUtils
import kotlin.random.Random

@Composable
fun SleepScreen(
    onBack: () -> Unit
) {
    var selectedDate by remember { mutableStateOf(DateUtils.getCurrentDate()) }
    val selectedDateOffset = remember(selectedDate) {
        DateUtils.getDayOffsetFromToday(selectedDate)
    }
    val sleepStages = listOf(
        SleepStage("Deep", 2.3f, 28, AppColors.Primary),
        SleepStage("Light", 3.7f, 45, AppColors.Secondary),
        SleepStage("REM", 1.6f, 19, AppColors.Accent),
        SleepStage("Awake", 0.6f, 8, AppColors.TextTertiary),
    )

    val totalSleep = sleepStages.sumOf { it.duration.toDouble() }.toFloat()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BackgroundPrimary)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CommonTopAppBar("Sleep" , onBack)

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
                    .padding(AppDimensions.ScreenPadding.Content),
                verticalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.ExtraLarge)
            ) {
                CardBlock {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Sleep Performance",
                            color = AppColors.TextSecondary,
                            style = AppTypography.LabelMedium
                        )

                        Text(
                            "85%",
                            style = AppTypography.DisplayLarge,
                            color = AppColors.Primary
                        )

                        Spacer(modifier = Modifier.height(AppDimensions.Spacing.Default))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(AppColors.Selected, AppShapes.Chip)
                                .padding(
                                    horizontal = AppDimensions.Spacing.ExtraLarge,
                                    vertical = AppDimensions.Spacing.Small
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(AppDimensions.Component.ZoneIndicatorSize)
                                    .background(AppColors.Primary, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(AppDimensions.Spacing.Medium))
                            Text(
                                "Optimal Recovery",
                                color = AppColors.Primary,
                                style = AppTypography.LabelMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(AppDimensions.Spacing.ExtraLarge))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.Default)
                        ) {
                            SmallStatCard("Total Sleep", "${totalSleep}h")
                            SmallStatCard("Efficiency", "92%")
                        }
                    }
                }

                CardBlock {
                    Text(
                        "Sleep Stages",
                        color = AppColors.TextPrimary,
                        style = AppTypography.BodyMedium
                    )

                    Spacer(modifier = Modifier.height(AppDimensions.Spacing.Default))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(AppDimensions.Spacing.XXL)
                            .clip(AppShapes.CardLarge)
                    ) {
                        sleepStages.forEach {
                            Box(
                                modifier = Modifier
                                    .weight(it.percentage.toFloat())
                                    .fillMaxHeight()
                                    .background(it.color)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(AppDimensions.Spacing.Large))

                    sleepStages.forEach { stage ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AppColors.BackgroundTertiary, AppShapes.CardMedium)
                                .padding(AppDimensions.Spacing.Default),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("${stage.stage} Sleep", color = AppColors.TextPrimary)
                                Text(
                                    "${stage.percentage}% of total",
                                    style = AppTypography.LabelMedium,
                                    color = AppColors.TextSecondary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${stage.duration}h", color = AppColors.TextPrimary)
                                Text(
                                    "${(stage.duration * 60).toInt()}m",
                                    style = AppTypography.LabelMedium,
                                    color = AppColors.TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(AppDimensions.Spacing.Medium))
                    }
                }

                CardBlock {
                    Text(
                        "Sleep Vitals",
                        color = AppColors.TextPrimary,
                        style = AppTypography.BodyMedium
                    )

                    Spacer(modifier = Modifier.height(AppDimensions.Spacing.Default))

                    GridRow(
                        "Bedtime" to "11:24 PM",
                        "Wake Time" to "7:38 AM",
                        "Sleep Latency" to "8 min",
                        "Respiratory Rate" to "15.2"
                    )
                }

                CardBlock {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Heart Rate Overnight", color = AppColors.TextPrimary)
                        Text("52 avg", color = AppColors.Secondary)
                    }

                    Spacer(modifier = Modifier.height(AppDimensions.Spacing.Default))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(AppDimensions.ChartHeight.Medium),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.ExtraSmall)
                    ) {
                        repeat(30) {
                            val height = Random.nextInt(20, 100)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(height.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                AppColors.Secondary,
                                                AppColors.Secondary.copy(alpha = AppAlpha.Medium)
                                            )
                                        ),
                                        AppShapes.ChartBarLarge
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(AppDimensions.Spacing.Small))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "11 PM",
                            style = AppTypography.LabelExtraSmall,
                            color = AppColors.TextSecondary
                        )
                        Text(
                            "3 AM",
                            style = AppTypography.LabelExtraSmall,
                            color = AppColors.TextSecondary
                        )
                        Text(
                            "7 AM",
                            style = AppTypography.LabelExtraSmall,
                            color = AppColors.TextSecondary
                        )
                    }
                }

                CardBlock(borderColor = AppColors.Primary.copy(alpha = 0.3f)) {
                    Text(
                        "7-Day Average",
                        color = AppColors.TextPrimary,
                        style = AppTypography.BodyMedium
                    )

                    Spacer(modifier = Modifier.height(AppDimensions.Spacing.Default))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Sleep Duration",
                                color = AppColors.TextSecondary,
                                style = AppTypography.LabelMedium
                            )
                            Text(
                                "7h 52m",
                                color = AppColors.TextPrimary,
                                style = AppTypography.HeadingMedium
                            )
                        }
                        Column {
                            Text(
                                "Sleep Score",
                                color = AppColors.TextSecondary,
                                style = AppTypography.LabelMedium
                            )
                            Text(
                                "83%",
                                color = AppColors.TextPrimary,
                                style = AppTypography.HeadingMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(AppDimensions.Spacing.ExtraLarge))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(AppDimensions.Component.ZoneIndicatorSize)
                                .background(AppColors.Primary, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(AppDimensions.Spacing.Medium))
                        Text(
                            "Sleep consistency improving",
                            color = AppColors.TextSecondary,
                            style = AppTypography.LabelMedium
                        )
                    }
                }
            }
        }
    }
}

data class SleepStage(
    val stage: String,
    val duration: Float,
    val percentage: Int,
    val color: Color
)

@Composable
fun CardBlock(
    borderColor: Color = AppColors.OverlayMedium,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.BackgroundTertiary, AppShapes.CardLarge)
            .border(AppDimensions.Border.Thin, borderColor, AppShapes.CardLarge)
            .padding(AppDimensions.CardPadding.Default),
        content = content
    )
}

@Composable
fun SmallStatCard(title: String, value: String) {
    Column(
        modifier = Modifier
            .background(AppColors.BackgroundTertiary, AppShapes.CardSmall)
            .padding(AppDimensions.CardPadding.Medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, style = AppTypography.BodyLarge, color = AppColors.TextPrimary)
        Text(title, style = AppTypography.LabelMedium, color = AppColors.TextSecondary)
    }
}

@Composable
fun GridRow(vararg items: Pair<String, String>) {
    Column(verticalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.Medium)) {
        var i = 0
        while (i < items.size) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppDimensions.Spacing.Default)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(AppColors.BackgroundTertiary, AppShapes.CardSmall)
                        .padding(AppDimensions.CardPadding.Medium)
                ) {
                    Text(items[i].second, color = AppColors.TextPrimary)
                    Text(
                        items[i].first,
                        style = AppTypography.LabelMedium,
                        color = AppColors.TextSecondary
                    )
                }

                if (i + 1 < items.size) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(AppColors.BackgroundTertiary, AppShapes.CardSmall)
                            .padding(AppDimensions.CardPadding.Medium)
                    ) {
                        Text(items[i + 1].second, color = AppColors.TextPrimary)
                        Text(
                            items[i + 1].first,
                            style = AppTypography.LabelMedium,
                            color = AppColors.TextSecondary
                        )
                    }
                }
            }
            i += 2
        }
    }
}
