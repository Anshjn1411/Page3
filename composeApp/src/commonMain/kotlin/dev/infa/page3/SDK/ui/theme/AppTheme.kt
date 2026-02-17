package dev.infa.page3.SDK.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush

/**
 * Main App Theme
 * 
 * Usage:
 * ```
 * AppTheme {
 *     // Your composable content
 * }
 * ```
 */
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val colorScheme = darkColorScheme(
        primary = AppColors.Primary,
        secondary = AppColors.Secondary,
        background = AppColors.BackgroundPrimary,
        surface = AppColors.Surface,
        error = AppColors.Error,
        onPrimary = AppColors.TextPrimary,
        onSecondary = AppColors.TextPrimary,
        onBackground = AppColors.TextPrimary,
        onSurface = AppColors.TextPrimary,
        onError = AppColors.TextPrimary
    )
    
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

/**
 * Common Gradients used across the app
 */
object AppGradients {
    val PrimaryGradient = Brush.horizontalGradient(
        listOf(AppColors.GradientStart, AppColors.GradientEnd)
    )
    
    val CardGradient = Brush.linearGradient(
        listOf(AppColors.OverlayLight, AppColors.OverlayLight.copy(alpha = 0.01f))
    )
    
    val VerticalPrimary = Brush.verticalGradient(
        listOf(AppColors.Primary, AppColors.Primary.copy(alpha = 0.5f))
    )
    
    fun metricGradient(color: androidx.compose.ui.graphics.Color) = Brush.verticalGradient(
        listOf(color, color.copy(alpha = 0.5f))
    )
}

/**
 * Extension functions for easy theme access
 */
object ThemeExtensions {
    /**
     * Get health metric color by type
     */
    fun getHealthMetricColor(type: HealthMetricType): androidx.compose.ui.graphics.Color {
        return when(type) {
            HealthMetricType.HEART_RATE -> AppColors.HeartRate
            HealthMetricType.BLOOD_OXYGEN -> AppColors.BloodOxygen
            HealthMetricType.HRV -> AppColors.HRV
            HealthMetricType.STRESS -> AppColors.Stress
            HealthMetricType.TEMPERATURE -> AppColors.Temperature
            HealthMetricType.PRESSURE -> AppColors.Pressure
            HealthMetricType.SLEEP -> AppColors.Sleep
            HealthMetricType.STEPS -> AppColors.Steps
            HealthMetricType.STRAIN -> AppColors.Strain
            HealthMetricType.RECOVERY -> AppColors.Recovery
        }
    }
    
    /**
     * Get zone color based on health value
     */
    fun getZoneColor(isLow: Boolean, isHigh: Boolean): androidx.compose.ui.graphics.Color {
        return when {
            isLow -> AppColors.ZoneLow
            isHigh -> AppColors.ZoneHigh
            else -> AppColors.ZoneNormal
        }
    }
}

/**
 * Health metric types for color mapping
 */
enum class HealthMetricType {
    HEART_RATE,
    BLOOD_OXYGEN,
    HRV,
    STRESS,
    TEMPERATURE,
    PRESSURE,
    SLEEP,
    STEPS,
    STRAIN,
    RECOVERY
}