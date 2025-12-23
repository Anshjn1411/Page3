package dev.infa.page3.SDK.ui.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.infa.page3.SDK.ui.components.DeviceCapabilityScreen
import dev.infa.page3.SDK.ui.components.DeviceDetailScreen
import dev.infa.page3.SDK.ui.screens.*
import dev.infa.page3.SDK.ui.screens.ScannerScreen
import dev.infa.page3.SDK.ui.screens.ProfileScreen
import dev.infa.page3.navigation.AppViewModels

class HomeScreenSDK : Screen  {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        DashboardScreen(
            homeViewModel = AppViewModels.homeViewModel,
            connectionViewModel = AppViewModels.connectionViewModel,
            navController = navigator,
            syncViewModel = AppViewModels.syncViewModel
        )
    }
}


class StepsScreenSDK : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        StepsScreen(
            onBack = { navigator.pop() },
            viewModel = AppViewModels.syncViewModel,
            homeViewModel = AppViewModels.homeViewModel,
            navigator
        )
    }
}

class SleepScreenSDK : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        SleepScreen(onBack = { navigator.pop() })
    }
}

class HeartRateScreenSDK : Screen {


    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        HeartRateScreen(
            navController = navigator,
            viewModel = AppViewModels.syncViewModel,
            AppViewModels.instantMeasuresViewModel,
            AppViewModels.continuousMonitoringViewModel
        )
    }
}

class BloodOxygenScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        BloodOxygenScreen(
            onBack = { navigator.pop() },
            viewModel = AppViewModels.syncViewModel,
            AppViewModels.instantMeasuresViewModel,
            AppViewModels.continuousMonitoringViewModel
        )
    }
}

class HrvScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        HrvScreen(
            onBack = { navigator.pop() },
            viewModel = AppViewModels.syncViewModel,
            AppViewModels.instantMeasuresViewModel,
            AppViewModels.continuousMonitoringViewModel
        )
    }
}

class BloodPressureScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        BloodPressureScreen(
            onBack = { navigator.pop() },
            viewModel = AppViewModels.syncViewModel,
            AppViewModels.instantMeasuresViewModel,
            AppViewModels.continuousMonitoringViewModel
        )
    }
}

class StressScreenSDK : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        StressScreen(
            onBack = { navigator.pop() },
            viewModel = AppViewModels.syncViewModel,
            AppViewModels.instantMeasuresViewModel,
        )
    }
}
class TemperatureScreenSDK : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        TemperatureScreen(
            onBack = { navigator.pop() },
            viewModel = AppViewModels.syncViewModel,
            AppViewModels.instantMeasuresViewModel,
            AppViewModels.continuousMonitoringViewModel
        )
    }
}

class PressureSDK : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        PressureScreen(
            onBack = { navigator.pop() },
            viewModel = AppViewModels.syncViewModel,
            AppViewModels.instantMeasuresViewModel,
            AppViewModels.continuousMonitoringViewModel
        )
    }
}

class ExerciseScreenSDK : Screen{

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        ExerciseScreen(
            navController = navigator,
            viewModel = AppViewModels.homeViewModel
        )
    }
}

class ProfileScreenSDK : Screen{

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        ProfileScreen(
            connectionViewModel = AppViewModels.connectionViewModel,
            profileViewModel = AppViewModels.profileViewModel,
            navController = navigator
        )
    }
}

class ScannerScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        ScannerScreen(
            connectionViewModel = AppViewModels.connectionViewModel,
            navController = navigator
        )
    }
}

// ============= DEVICE DETAIL SCREEN =============
class DeviceDetailScreenSDK : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        DeviceDetailScreen(
            AppViewModels.connectionViewModel,
            navigator
        )

    }
}

// ============= DEVICE CAPABILITY SCREEN =============
class DeviceCapabilityScreenSDK : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        DeviceCapabilityScreen(
            AppViewModels.connectionViewModel,
            AppViewModels.homeViewModel
        )
    }
}

// ============= GOALS SETTINGS SCREEN =============
class GoalsSettingsScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        GoalsSettingsScreen(
            navController = navigator,
            homeViewModel = AppViewModels.homeViewModel
        )
    }
}
