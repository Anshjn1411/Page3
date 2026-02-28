package dev.infa.page3.ui.otherScreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun RefundReturnPolicyScreen() {
    Scaffold(
        topBar = {
            CommonTopAppBar(title = "Refund & Return Policy")
        }
    ) { innerPadding ->
        SimpleWebView(
            url = "https://www.page3life.com/refund_returns/",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}
