package dev.infa.page3.SDK.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * App Shapes - Consistent corner radius across the app
 */
object AppShapes {
    // Card Shapes
    val CardSmall: Shape = RoundedCornerShape(14.dp)
    val CardMedium: Shape = RoundedCornerShape(16.dp)
    val CardLarge: Shape = RoundedCornerShape(20.dp)
    val CardExtraLarge: Shape = RoundedCornerShape(24.dp)
    val CardXXL: Shape = RoundedCornerShape(28.dp)
    
    // Button Shapes
    val ButtonSmall: Shape = RoundedCornerShape(12.dp)
    val ButtonMedium: Shape = RoundedCornerShape(14.dp)
    val ButtonLarge: Shape = RoundedCornerShape(16.dp)
    
    // Input Shapes
    val Input: Shape = RoundedCornerShape(14.dp)
    val InputLarge: Shape = RoundedCornerShape(16.dp)
    
    // Component Shapes
    val Chip: Shape = RoundedCornerShape(50.dp)
    val Badge: Shape = RoundedCornerShape(50.dp)
    val Progress: Shape = RoundedCornerShape(50.dp)
    
    // Chart/Graph Shapes
    val ChartBar: Shape = RoundedCornerShape(2.dp)
    val ChartBarLarge: Shape = RoundedCornerShape(4.dp)
    
    // Bottom Sheet Shapes
    val BottomSheet: Shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    
    // Dialog Shapes
    val Dialog: Shape = RoundedCornerShape(24.dp)
    
    // Specific component shapes
    val BottomNavIndicator: Shape = RoundedCornerShape(50.dp)
    val MetricValueBox: Shape = RoundedCornerShape(14.dp)
    val ExerciseTypeCard: Shape = RoundedCornerShape(16.dp)
    val HealthMetricCard: Shape = RoundedCornerShape(18.dp)
    val StatCard: Shape = RoundedCornerShape(18.dp)
    val TrendChart: Shape = RoundedCornerShape(22.dp)
}