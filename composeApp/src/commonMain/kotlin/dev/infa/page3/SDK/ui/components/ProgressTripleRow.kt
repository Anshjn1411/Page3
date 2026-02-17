package dev.infa.page3.SDK.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.SDK.data.DayStepData
import dev.infa.page3.SDK.ui.navigation.*
import dev.infa.page3.SDK.ui.theme.*

@Composable
fun ProgressTripleRow(
    stepData: DayStepData?,
    stepGoal: Int,
    sleepData: dev.infa.page3.SDK.viewModel.SleepData?,
    isLoadingSteps: Boolean,
    navController: Navigator
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimensions.Spacing.ExtraLarge, vertical = AppDimensions.Spacing.Medium),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        CircularProgressRing(
            value = 68,
            max = 100,
            label = "Recovery",
            color = AppColors.Primary,
            icon = Icons.Default.Favorite,
            onClick = {
                // Navigate to steps detail screen
                navController.push(HeartRateScreenSDK())
            }
        )

        // Sleep Ring - Shows sleep score
        CircularProgressRing(
            value = sleepData?.sleepScore ?: 0,
            max = 100,
            label = "Sleep",
            color = AppColors.Secondary,
            icon = Icons.Default.Nightlight,
            onClick = {
                // Navigate to steps detail screen
                navController.push(SleepScreenSDK())
            }
        )

        // Steps Ring - Connected to ViewModel with loading state
        if (isLoadingSteps) {
            CircularProgressRingLoading(
                label = "Steps",
                color = AppColors.Accent,
                icon = Icons.Default.DirectionsWalk
            )
        } else {
            val stepValue = stepData?.totalSteps?.toInt() ?: 0
            val stepPercentage = if (stepGoal > 0) {
                ((stepValue.toFloat() / stepGoal.toFloat()) * 100).toInt().coerceIn(0, 100)
            } else {
                0
            }
            CircularProgressRing(
                value = stepPercentage,
                max = 100,
                label = "Steps",
                color = AppColors.AccentPurple,
                icon = Icons.Default.DirectionsWalk,
                onClick = {
                    // Navigate to steps detail screen
                    navController.push(StepsScreenSDK())
                }
            )
        }
    }
}
