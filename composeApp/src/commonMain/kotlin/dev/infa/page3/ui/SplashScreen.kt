package dev.infa.page3.ui

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import dev.infa.page3.navigation.AppViewModels
import dev.infa.page3.navigation.AuthManager
import dev.infa.page3.navigation.HomeMainScreen
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import page3.composeapp.generated.resources.Res
import page3.composeapp.generated.resources.splash

@Composable
fun SplashScreen(navigator : Navigator) {
    LaunchedEffect(Unit) {
        while (!AppViewModels.isInitialized) {
            delay(32)
        }
        AuthManager.init()
        navigator.push(HomeMainScreen())
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(Res.drawable.splash),
                contentDescription = null
            )
        }
    }
}
