package dev.infa.page3.ui.otherScreen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSURL
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.WebKit.WKWebpagePreferences

/**
 * iOS actual implementation of SimpleWebView using WKWebView.
 * Simply loads the given URL for rendering website pages.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun SimpleWebView(
    url: String,
    modifier: Modifier
) {
//    UIKitView(
//        modifier = modifier,
//        factory = {
//            val config = WKWebViewConfiguration().apply {
//                defaultWebpagePreferences = WKWebpagePreferences().apply {
//                    allowsContentJavaScript = true
//                }
//            }
//            WKWebView(frame = CGRectZero, configuration = config).apply {
//                val nsUrl = NSURL.URLWithString(url) ?: return@apply
//                val request = NSMutableURLRequest.requestWithURL(nsUrl)
//                loadRequest(request)
//            }
//        }
//    )
}
