package dev.infa.page3.ui.orderscreen

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Android actual implementation of PlatformWebView using android.webkit.WebView.
 *
 * Monitors URL changes to detect payment success/failure redirects.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun PlatformWebView(
    url: String,
    onPaymentSuccess: () -> Unit,
    onPaymentFailed: () -> Unit,
    onUrlChanged: (String) -> Unit
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.builtInZoomControls = false
                settings.displayZoomControls = false
                settings.setSupportMultipleWindows(false)
                settings.javaScriptCanOpenWindowsAutomatically = true

                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, pageUrl: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, pageUrl, favicon)
                        pageUrl?.let { currentUrl ->
                            onUrlChanged(currentUrl)
                            checkPaymentRedirect(currentUrl, onPaymentSuccess, onPaymentFailed)
                        }
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val requestUrl = request?.url?.toString() ?: return false
                        onUrlChanged(requestUrl)
                        return if (checkPaymentRedirect(requestUrl, onPaymentSuccess, onPaymentFailed)) {
                            true // Intercept - don't load the URL in WebView
                        } else {
                            false // Let WebView handle the URL
                        }
                    }
                }

                webChromeClient = WebChromeClient()

                loadUrl(url)
            }
        },
        update = { webView ->
            // Only reload if URL has changed
        }
    )
}

/**
 * Checks if the current URL indicates payment success or failure.
 * Returns true if the URL was intercepted (success or failure detected).
 */
private fun checkPaymentRedirect(
    url: String,
    onPaymentSuccess: () -> Unit,
    onPaymentFailed: () -> Unit
): Boolean {
    val lowerUrl = url.lowercase()
    return when {
        // WooCommerce order-received page means payment was successful
        lowerUrl.contains("order-received") ||
        lowerUrl.contains("thank-you") ||
        lowerUrl.contains("payment-success") ||
        lowerUrl.contains("checkout/order-received") -> {
            onPaymentSuccess()
            true
        }
        // Payment failure or cancellation
        lowerUrl.contains("payment-failed") ||
        lowerUrl.contains("payment-cancel") ||
        lowerUrl.contains("cancel") && lowerUrl.contains("order") -> {
            onPaymentFailed()
            true
        }
        else -> false
    }
}
