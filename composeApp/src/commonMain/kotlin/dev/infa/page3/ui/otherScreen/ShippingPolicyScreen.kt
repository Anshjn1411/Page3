package dev.infa.page3.ui.otherScreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ShippingPolicyScreen() {
    Scaffold(
        topBar = {
            CommonTopAppBar(title = "Shipping Policy")
        }
    ) { innerPadding ->
        SimpleWebView(
            url = "https://www.page3life.com/shipping-policy/",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}
