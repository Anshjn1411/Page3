package dev.infa.page3.ui.components


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSKeyValueObservingOptionNew
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.Foundation.addObserver
import platform.Foundation.removeObserver
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalForeignApi::class)
@Composable
actual fun ProductWebViewScreen(
    url: String,
    title: String,
    onBack: () -> Unit,
    modifier: Modifier
) {
    var isLoading by remember { mutableStateOf(true) }
    var loadingProgress by remember { mutableFloatStateOf(0f) }
    var pageTitle by remember { mutableStateOf(title) }

    // Hold a stable reference to WKWebView so KVO observer can reference it
    val webView = remember {
        WKWebView(
            frame = CGRectZero.readValue(),
            configuration = WKWebViewConfiguration()
        )
    }

    // KVO observer for estimatedProgress + title
    val progressObserver = remember {
        object : NSObject() {
             fun observeValueForKeyPath(
                keyPath: String?,
                ofObject: Any?,
                change: Map<Any?, *>?,
                context: kotlinx.cinterop.COpaquePointer?
            ) {
                when (keyPath) {
                    "estimatedProgress" -> {
                        loadingProgress = webView.estimatedProgress.toFloat()
                        isLoading = loadingProgress < 1f
                    }
                    "title" -> {
                        pageTitle = webView.title ?: title
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = pageTitle, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                LinearProgressIndicator(
                    progress = { loadingProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                )
            }

            UIKitView(
                factory = {
                    // Attach KVO observers
                    webView.addObserver(
                        progressObserver,
                        forKeyPath = "estimatedProgress",
                        options = NSKeyValueObservingOptionNew,
                        context = null
                    )
                    webView.addObserver(
                        progressObserver,
                        forKeyPath = "title",
                        options = NSKeyValueObservingOptionNew,
                        context = null
                    )

                    // Load the initial URL
                    NSURL.URLWithString(url)?.let { nsUrl ->
                        webView.loadRequest(NSURLRequest.requestWithURL(nsUrl))
                    }

                    webView
                },
                onRelease = {
                    // Remove KVO observers when composable leaves composition
                    webView.removeObserver(progressObserver, forKeyPath = "estimatedProgress")
                    webView.removeObserver(progressObserver, forKeyPath = "title")
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}