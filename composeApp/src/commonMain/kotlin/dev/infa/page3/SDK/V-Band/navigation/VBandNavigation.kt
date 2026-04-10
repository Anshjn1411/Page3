package dev.infa.page3.SDK.`V-Band`.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.infa.page3.SDK.`V-Band`.ui.*
import dev.infa.page3.navigation.AppViewModels

// ─── Main Container (with bottom nav — entry point) ─────────────────────────────

class VBandDashboardScreenNav : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        VBandMainContainer(
            viewModel = AppViewModels.vBandViewModel,
            navigator = navigator
        )
    }
}

// ─── V-Band Settings (standalone if pushed from outside) ─────────────────────────

class VBandSettingsScreenNav : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        VBandSettingsScreen(
            viewModel = AppViewModels.vBandViewModel,
            navigator = navigator
        )
    }
}

// ─── V-Band Data (standalone if pushed from outside) ─────────────────────────────

class VBandDataScreenNav : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        VBandDataScreen(
            viewModel = AppViewModels.vBandViewModel,
            navigator = navigator
        )
    }
}
