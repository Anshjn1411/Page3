package dev.infa.page3.SDK.bottle.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.infa.page3.SDK.bottle.ui.*
import dev.infa.page3.navigation.AppViewModels

// ─── Bottle Dashboard ───────────────────────────────────────────────────────────

class BottleDashboardScreenNav : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        BottleDashboardScreen(
            viewModel = AppViewModels.bottleViewModel,
            navigator = navigator
        )
    }
}

// ─── Bottle Settings ────────────────────────────────────────────────────────────

class BottleSettingsScreenNav : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        BottleSettingsScreen(
            viewModel = AppViewModels.bottleViewModel,
            navigator = navigator
        )
    }
}

// ─── Drinking History ───────────────────────────────────────────────────────────

class BottleDrinkingHistoryScreenNav : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        BottleDrinkingHistoryScreen(
            viewModel = AppViewModels.bottleViewModel,
            navigator = navigator
        )
    }
}

// ─── Alarm Editor ───────────────────────────────────────────────────────────────

class BottleAlarmEditorNav : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        BottleAlarmEditorScreen(
            viewModel = AppViewModels.bottleViewModel,
            navigator = navigator
        )
    }
}
