package dev.infa.page3.ui.otherScreen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Expect declaration for a simple WebView composable that just loads a URL.
 * Used for displaying static website pages (policies, about us, contact, etc.)
 *
 * @param url The URL to load in the WebView
 * @param modifier Modifier for the WebView layout
 */
@Composable
expect fun SimpleWebView(
    url: String,
    modifier: Modifier = Modifier
)
