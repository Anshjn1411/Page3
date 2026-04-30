package dev.infa.page3.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

/**
 * Voyager Screen that wraps [ProductWebViewScreen].
 *
 * Push this onto the navigator when a carousel item is tapped:
 *
 *   navigator.push(ProductWebScreen(url = product.websiteUrl, title = product.label))
 */
data class ProductWebScreen(
    val url: String,
    val title: String
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        ProductWebViewScreen(
            url = url,
            title = title,
            onBack = { navigator.pop() }
        )
    }
}


/**
 * Opens the given URL in a full in-app WebView screen.
 * Android: android.webkit.WebView via AndroidView
 * iOS:     WKWebView via UIKitView
 */
@Composable
expect fun ProductWebViewScreen(
    url: String,
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
)