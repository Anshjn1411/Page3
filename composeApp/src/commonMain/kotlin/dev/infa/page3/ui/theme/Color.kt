package dev.infa.page3.ui.theme

import androidx.compose.ui.graphics.Color

// Primary Brand Colors
val Page3Black = Color(0xFF000000)
val Page3White = Color(0xFFFFFFFF)
val Page3Charcoal = Color(0xFF1A1A1A)
val Page3DarkGrey = Color(0xFF2B2B2B)

// Accent Colors
val Page3Teal = Color(0xFF4A9B9B)
val Page3LightTeal = Color(0xFF6BB5B5)
val Page3DeepTeal = Color(0xFF2E7373)

// Background & Surface Colors
val BackgroundLight = Color(0xFFFAFAFA)
val BackgroundDark = Color(0xFF121212)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF1E1E1E)
val SurfaceVariantLight = Color(0xDEDCDC)
val SurfaceVariantDark = Color(0xFF2B2B2B)

// Text Colors
val TextPrimaryLight = Color(0xFF000000)
val TextPrimaryDark = Color(0xFFFFFFFF)
val TextSecondaryLight = Color(0xFF666666)
val TextSecondaryDark = Color(0xFFB3B3B3)
val TextTertiaryLight = Color(0xFF999999)
val TextTertiaryDark = Color(0xFF808080)

// Border & Divider Colors
val BorderLight = Color(0xFFE0E0E0)
val BorderDark = Color(0xFF3A3A3A)
val DividerLight = Color(0xFFEEEEEE)
val DividerDark = Color(0xFF2B2B2B)

// Status Colors
val ErrorColor = Color(0xFFDC2626)
val SuccessColor = Color(0xFF16A34A)
val WarningColor = Color(0xFFF59E0B)
val InfoColor = Color(0xFF3B82F6)

// Interactive States
val PressedStateLight = Color(0xFFF0F0F0)
val PressedStateDark = Color(0xFF2A2A2A)
val DisabledLight = Color(0xFFCCCCCC)
val DisabledDark = Color(0xFF4D4D4D)

val LightColors = androidx.compose.material3.lightColorScheme(
    primary = Page3Black,
    onPrimary = Page3White,
    primaryContainer = Page3Charcoal,
    onPrimaryContainer = Page3White,

    secondary = Page3Teal,
    onSecondary = Page3White,
    secondaryContainer = Page3LightTeal,
    onSecondaryContainer = Page3Black,

    tertiary = Page3DeepTeal,
    onTertiary = Page3White,
    tertiaryContainer = Page3LightTeal,
    onTertiaryContainer = Page3Black,

    background = BackgroundLight,
    onBackground = TextPrimaryLight,

    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,

    surfaceTint = Page3Black,
    inverseSurface = Page3Black,
    inverseOnSurface = Page3White,

    error = ErrorColor,
    onError = Page3White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = ErrorColor,

    outline = BorderLight,
    outlineVariant = DividerLight,
    scrim = Color(0x80000000)
)

val DarkColors = androidx.compose.material3.darkColorScheme(
    primary = Page3White,
    onPrimary = Page3Black,
    primaryContainer = Page3Charcoal,
    onPrimaryContainer = Page3White,

    secondary = Page3LightTeal,
    onSecondary = Page3Black,
    secondaryContainer = Page3DeepTeal,
    onSecondaryContainer = Page3White,

    tertiary = Page3Teal,
    onTertiary = Page3Black,
    tertiaryContainer = Page3DeepTeal,
    onTertiaryContainer = Page3White,

    background = BackgroundDark,
    onBackground = TextPrimaryDark,

    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,

    surfaceTint = Page3White,
    inverseSurface = Page3White,
    inverseOnSurface = Page3Black,

    error = ErrorColor,
    onError = Page3White,
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFCA5A5),

    outline = BorderDark,
    outlineVariant = DividerDark,
    scrim = Color(0x80000000)
)
