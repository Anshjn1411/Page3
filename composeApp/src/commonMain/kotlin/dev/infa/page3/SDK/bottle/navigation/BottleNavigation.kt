package dev.infa.page3.SDK.bottle.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.infa.page3.SDK.bottle.ui.*
import dev.infa.page3.navigation.AppViewModels

// ─── Main Container (with bottom nav — entry point) ─────────────────────────────

class BottleDashboardScreenNav : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        // BottleMainContainer hosts all 3 tabs with the floating glassmorphic bottom nav
        BottleMainContainer(
            viewModel = AppViewModels.bottleViewModel,
            navigator = navigator
        )
    }
}

// ─── Bottle Settings (standalone if pushed from outside) ─────────────────────────

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

// ─── Drinking History (standalone if pushed from outside) ────────────────────────

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

// ─── Alarm Editor ────────────────────────────────────────────────────────────────

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