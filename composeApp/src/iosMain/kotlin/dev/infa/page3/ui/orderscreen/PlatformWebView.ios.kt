package dev.infa.page3.ui.orderscreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSURL
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.WebKit.WKWebpagePreferences
import platform.darwin.NSObject

/**
 * iOS actual implementation of PlatformWebView using WKWebView.
 *
 * Monitors URL changes to detect payment success/failure redirects.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformWebView(
    url: String,
    onPaymentSuccess: () -> Unit,
    onPaymentFailed: () -> Unit,
    onUrlChanged: (String) -> Unit
) {
    val navigationDelegate = remember {
        WebViewNavigationDelegate(onPaymentSuccess, onPaymentFailed, onUrlChanged)
    }

    UIKitView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            val config = WKWebViewConfiguration().apply {
                defaultWebpagePreferences = WKWebpagePreferences().apply {
                    allowsContentJavaScript = true
                }
            }
            WKWebView(frame = CGRectZero.readValue(), configuration = config).apply {
                this.navigationDelegate = navigationDelegate
                val nsUrl = NSURL.URLWithString(url) ?: return@apply
                val request = NSMutableURLRequest.requestWithURL(nsUrl)
                loadRequest(request)
            }
        }
    )
}

/**
 * Navigation delegate for WKWebView to intercept URL changes.
 */
private class WebViewNavigationDelegate(
    private val onPaymentSuccess: () -> Unit,
    private val onPaymentFailed: () -> Unit,
    private val onUrlChanged: (String) -> Unit
) : NSObject(), WKNavigationDelegateProtocol {

    override fun webView(
        webView: WKWebView,
        decidePolicyForNavigationAction: WKNavigationAction,
        decisionHandler: (WKNavigationActionPolicy) -> Unit
    ) {
        val urlString = decidePolicyForNavigationAction.request.URL?.absoluteString ?: ""
        onUrlChanged(urlString)

        if (checkPaymentRedirect(urlString)) {
            decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
        } else {
            decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
        }
    }

    override fun webView(webView: WKWebView, didFinishNavigation: WKNavigation?) {
        val urlString = webView.URL?.absoluteString ?: ""
        onUrlChanged(urlString)
        checkPaymentRedirect(urlString)
    }

    private fun checkPaymentRedirect(url: String): Boolean {
        val lowerUrl = url.lowercase()
        return when {
            lowerUrl.contains("order-received") ||
            lowerUrl.contains("thank-you") ||
            lowerUrl.contains("payment-success") ||
            lowerUrl.contains("checkout/order-received") -> {
                onPaymentSuccess()
                true
            }
            lowerUrl.contains("payment-failed") ||
            lowerUrl.contains("payment-cancel") ||
            (lowerUrl.contains("cancel") && lowerUrl.contains("order")) -> {
                onPaymentFailed()
                true
            }
            else -> false
        }
    }
}
