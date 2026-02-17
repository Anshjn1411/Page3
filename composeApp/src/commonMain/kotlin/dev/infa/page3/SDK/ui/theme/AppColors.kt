package dev.infa.page3.SDK.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Primary Brand Colors
 */
object AppColors {
    // Primary
    val Primary = Color(0xFF00FF88)
    val PrimaryVariant = Color(0xFF00CC6A)
    
    // Secondary
    val Secondary = Color(0xFF3B82F6)
    val SecondaryVariant = Color(0xFF2563EB)
    
    // Accent Colors
    val Accent = Color(0xFF6366F1)
    val AccentAmber = Color(0xFFF59E0B)
    val AccentOrange = Color(0xFFFFA500)
    val AccentPurple = Color(0xFF6366F1)
    
    // Status Colors
    val Success = Color(0xFF00FF88)
    val Error = Color(0xFFFF6B6B)
    val Warning = Color(0xFFFFB020)
    val Info = Color(0xFF3B82F6)
    
    // Background Colors
    val BackgroundPrimary = Color.Black
    val BackgroundSecondary = Color(0xFF0D0D0D)
    val BackgroundTertiary = Color(0xFF1A1A1A)
    val BackgroundCard = Color(0xFF0D0D0D)
    
    // Surface Colors
    val Surface = Color(0xFF1A1F2E)
    val SurfaceVariant = Color(0xFF111111)
    
    // Text Colors
    val TextPrimary = Color.White
    val TextSecondary = Color.Gray
    val TextTertiary = Color(0xFF6B7280)
    val TextDisabled = Color(0xFF4B5563)
    
    // Border Colors
    val BorderPrimary = Color(0xFF111111)
    val BorderSecondary = Color(0xFF1F1F1F)
    
    // Overlay Colors
    val OverlayLight = Color.White.copy(alpha = 0.02f)
    val OverlayMedium = Color.White.copy(alpha = 0.05f)
    val OverlayDark = Color.Black.copy(alpha = 0.25f)
    val OverlayDarker = Color.Black.copy(alpha = 0.5f)
    
    // Health Metric Colors
    val HeartRate = Color(0xFFFF6B6B)
    val HeartRateZone = Color(0xFFFF6B6B)
    val BloodOxygen = Color(0xFF6366F1)
    val HRV = Color(0xFF3B82F6)
    val Stress = Color(0xFFF59E0B)
    val Temperature = Color(0xFFFFA500)
    val Pressure = Color(0xFF6366F1)
    val Sleep = Color(0xFF3B82F6)
    val Steps = Color(0xFF6366F1)
    val Strain = Color(0xFF00FF88)
    val Recovery = Color(0xFF00FF88)
    
    // Gradient Colors
    val GradientStart = Color(0xFF00FF88)
    val GradientEnd = Color(0xFF3B82F6)
    
    // Special Colors
    val BluetoothConnected = Color(0xFF00FF88)
    val BluetoothDisconnected = Color.Red
    val BatteryGood = Color(0xFF00FF88)
    val BatteryLow = Color(0xFFFFB020)
    val BatteryCritical = Color(0xFFFF6B6B)
    
    // Zone Colors (for health metrics)
    val ZoneLow = Color(0xFF3B82F6)
    val ZoneNormal = Color(0xFF00FF88)
    val ZoneHigh = Color(0xFFFF6B6B)
    
    // Selection States
    val Selected = Color(0x4400FF88)
    val SelectedBorder = Color(0xFF00FF88)
    val Unselected = Color(0x22111111)
}

/**
 * Alpha values for consistency
 */
object AppAlpha {
    const val Disabled = 0.3f
    const val Medium = 0.5f
    const val Light = 0.6f
    const val VeryLight = 0.1f
    const val Subtle = 0.05f
}