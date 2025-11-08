package dev.infa.page3

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.infa.page3.navigation.MainNavigation
import dev.infa.page3.ui.theme.Page3Theme
import dev.infa.page3.platform.EnsureBackgroundConnection
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import page3.composeapp.generated.resources.Res
import page3.composeapp.generated.resources.compose_multiplatform

@Composable
@Preview
fun App() {
    Page3Theme {
        EnsureBackgroundConnection()
        MainNavigation()
    }
}