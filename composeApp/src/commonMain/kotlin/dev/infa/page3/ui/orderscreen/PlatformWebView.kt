package dev.infa.page3.ui.orderscreen

import androidx.compose.runtime.Composable

/**
 * Expect declaration for the platform-specific WebView composable.
 *
 * @param url The URL to load in the WebView
 * @param onPaymentSuccess Called when the WebView detects a success redirect URL
 * @param onPaymentFailed Called when the WebView detects a failure/cancel URL
 * @param onUrlChanged Called whenever the URL in the WebView changes (for monitoring)
 */
@Composable
expect fun PlatformWebView(
    url: String,
    onPaymentSuccess: () -> Unit,
    onPaymentFailed: () -> Unit,
    onUrlChanged: (String) -> Unit
)
