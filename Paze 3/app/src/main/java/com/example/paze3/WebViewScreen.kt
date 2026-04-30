package com.example.paze3

import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewScreen(url: String, onBack: () -> Unit, onDeepLink: (Int) -> Unit) {
    var webView: WebView? by remember { mutableStateOf(null) }

    BackHandler(enabled = true) {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        } else {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                onBack = {
                    if (webView?.canGoBack() == true) {
                        webView?.goBack()
                    } else {
                        onBack()
                    }
                },
                onSearchClick = { /* Handle search */ },
                onNotificationClick = { /* Handle notifications */ },
                onCartClick = { /* Handle cart */ }
            )
        }
    ) { padding ->
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            val urlStr = request?.url?.toString()
                            if (urlStr != null && urlStr.startsWith("paze3://")) {
                                val uri = Uri.parse(urlStr)
                                val id = uri.getQueryParameter("id")?.toIntOrNull()
                                if (id != null) {
                                    onDeepLink(id)
                                    return true // Handled
                                }
                            }
                            return false // Normal loading
                        }
                    }
                    settings.javaScriptEnabled = true
                    loadUrl(url)
                    webView = this
                }
            },
            update = { view ->
                webView = view
                // Only load if it's a new URL to avoid refresh loops
                if (view.url != url && !url.startsWith("paze3://")) {
                    view.loadUrl(url)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}
