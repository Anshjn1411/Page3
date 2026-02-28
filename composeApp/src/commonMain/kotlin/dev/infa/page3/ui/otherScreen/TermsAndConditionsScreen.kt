package dev.infa.page3.ui.otherScreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun TermsAndConditionsScreen() {
    Scaffold(
        topBar = {
            CommonTopAppBar(title = "Terms & Conditions")
        }
    ) { innerPadding ->
        SimpleWebView(
            url = "https://www.page3life.com/terms-of-services/",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}
