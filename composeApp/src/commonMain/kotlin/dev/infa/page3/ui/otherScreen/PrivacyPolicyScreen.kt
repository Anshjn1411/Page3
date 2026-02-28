package dev.infa.page3.ui.otherScreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PrivacyPolicyScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            CommonTopAppBar(title = "Privacy Policy", onBackClick = onBack)
        }
    ) { innerPadding ->
        SimpleWebView(
            url = "https://www.page3life.com/privacy-policy/",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}
