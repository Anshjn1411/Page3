package dev.infa.page3.ui.otherScreen

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Android actual implementation of SimpleWebView using android.webkit.WebView.
 * Simply loads the given URL with JavaScript enabled for rendering website pages.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun SimpleWebView(
    url: String,
    modifier: Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.builtInZoomControls = false
                settings.displayZoomControls = false
                settings.setSupportMultipleWindows(false)
                settings.javaScriptCanOpenWindowsAutomatically = false

                webViewClient = WebViewClient() // Keeps navigation within the WebView
                webChromeClient = WebChromeClient()

                loadUrl(url)
            }
        }
    )
}
