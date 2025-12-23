// commonMain/kotlin/dev/infa/page3/App.kt
package dev.infa.page3

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.infa.page3.navigation.MainNavigation
import dev.infa.page3.ui.theme.Page3Theme

@Composable
fun App() {
    Page3Theme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MainNavigation()
        }
    }
}