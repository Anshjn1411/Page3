package dev.infa.page3.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Page3 App Theme
 *
 * A Material 3 theme implementation for the Page3 app
 * with support for light and dark modes.
 */
@Composable
fun Page3Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Page3Typography,
        shapes = Page3Shapes,
        content = content
    )
}

/**
 * Preview theme for light mode
 */
@Composable
fun Page3ThemeLight(content: @Composable () -> Unit) {
    Page3Theme(darkTheme = false, content = content)
}

/**
 * Preview theme for dark mode
 */
@Composable
fun Page3ThemeDark(content: @Composable () -> Unit) {
    Page3Theme(darkTheme = true, content = content)
}

// Extension properties for easy access to custom text styles
val MaterialTheme.customTextStyles: Page3TextStyles
    @Composable
    get() = Page3TextStyles

// Extension properties for easy access to custom shapes
val MaterialTheme.customShapes: Page3CustomShapes
    @Composable
    get() = Page3CustomShapes
