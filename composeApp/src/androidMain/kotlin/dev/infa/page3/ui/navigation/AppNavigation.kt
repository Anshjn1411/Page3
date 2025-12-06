package dev.infa.page3.ui.navigation

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import dev.infa.page3.ui.components.DeviceCapabilityScreen
import dev.infa.page3.ui.components.DeviceDetailScreen
import dev.infa.page3.ui.screens.HomeScreen
import dev.infa.page3.ui.screens.HeartRateScreen
import dev.infa.page3.ui.screens.BloodPressureScreen
import dev.infa.page3.ui.screens.BloodOxygenScreen
import dev.infa.page3.ui.screens.GoalsSettingsScreen
import dev.infa.page3.ui.screens.HrvScreen
import dev.infa.page3.ui.screens.ProfileScreen
import dev.infa.page3.ui.screens.ScannerScreen
import dev.infa.page3.ui.screens.ExerciseScreen
import dev.infa.page3.ui.screens.StepsScreen
import dev.infa.page3.viewmodels.BloodOxygenViewModel
import dev.infa.page3.viewmodels.BloodPressureViewModel
import dev.infa.page3.viewmodels.ConnectionViewModel
import dev.infa.page3.viewmodels.HeartRateViewModel
import dev.infa.page3.viewmodels.HomeViewModel
import dev.infa.page3.viewmodels.HrvViewModel
import dev.infa.page3.viewmodels.ExerciseViewModel
import dev.infa.page3.viewmodels.HealthMetricsCacheManager
import dev.infa.page3.viewmodels.ProfileViewModel
import dev.infa.page3.viewmodels.StepAnalyticsViewModel
import dev.infa.page3.viewmodels.StepAnalyticsViewModelFactory

object Routes {
    const val Home = "home"
    const val SetGoal = "set_goal"
    const val Setting = "setting"
    const val Profile = "profile"
    const val Detail = "detail/{type}"
    const val Sleep = "sleep"
    const val Step = "step"
    const val Exercise = "exercise"
    const val Heart = "heart"
    const val Stress = "stress"
    const val BloodPressure = "bp"
    const val BloodOxygen = "spo2"
    const val HRV = "hrv"
    const val Scan = "scan"
    const val DeviceDetail = "device_detail"
    const val DeviceCapability = "device_capability"
}
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val connectionViewModel: ConnectionViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    val uiState by connectionViewModel.uiState.collectAsState()
    val isConnected = uiState.isConnected
    val deviceAddress = uiState.connectedDevice?.deviceAddress ?: ""
    val homeVm: HomeViewModel = viewModel()
    val context = LocalContext.current
    val stepViewModel: StepAnalyticsViewModel = viewModel(
        factory = StepAnalyticsViewModelFactory(context)
    )
    val heartViewModel : HeartRateViewModel = viewModel(
        factory = HealthMetricsViewModelFactory(context)
    )
    val bloodPressureViewModel : BloodPressureViewModel = viewModel(
        factory = HealthMetricsViewModelFactory(context)
    )
    val bloodOxygenViewModel : BloodOxygenViewModel = viewModel(
        factory = HealthMetricsViewModelFactory(context)
    )
    val hrvViewModel : HrvViewModel = viewModel(
        factory = HealthMetricsViewModelFactory(context)
    )
    val exerciseViewModel : ExerciseViewModel = viewModel()

    NavHost(navController = navController, startDestination = Routes.Home) {

        composable(Routes.Home) {
            LaunchedEffect(isConnected) {

            }
            HomeScreen(
                Modifier,
                connectionViewModel
                ,homeVm,
                stepViewModel = stepViewModel,
                navController

            )
        }
        // ============ Exercise Screen ============
        composable(Routes.Exercise) {

            ExerciseScreen(
                viewModel = exerciseViewModel,
               onBack = {
                   navController.navigateUp()
               }
            )
        }

        // ============ Heart Rate Screen ============
        composable(Routes.Heart) {
            HeartRateScreen(
                viewModel = heartViewModel,
                onBack = {
                    navController.navigateUp()
                }
            )
        }

        // ============ Stress Screen ============
        composable(Routes.Stress) {
            BloodPressureScreen(
                viewModel = bloodPressureViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // ============ Blood Oxygen Screen ============
        composable(Routes.BloodOxygen) {

            BloodOxygenScreen(
                viewModel = bloodOxygenViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // ============ HRV Screen (Commented) ============
        composable(Routes.HRV) {
            HrvScreen(
                viewModel = hrvViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // ============ Step Screen ============
        composable(Routes.Step) {

            StepsScreen({navController.navigateUp()} , stepViewModel)
        }

        // ============ Connect Screen ============
        composable(Routes.Profile) {
            ProfileScreen(
                connectionViewModel = connectionViewModel,
                profileViewModel = profileViewModel,
                navController = navController
            )
        }

        composable(Routes.Scan) {
            ScannerScreen(
                connectionViewModel = connectionViewModel,
                navController = navController
            )
        }
        composable(Routes.SetGoal) {
            GoalsSettingsScreen(homeViewModel =  homeVm ,navController)
        }
        composable(Routes.DeviceDetail) {
            DeviceDetailScreen(connectionViewModel , navController)
        }
        composable(Routes.DeviceCapability) {
            DeviceCapabilityScreen(connectionViewModel)
        }
    }
}

class HealthMetricsViewModelFactory(
    private val context: Context
) : androidx.lifecycle.ViewModelProvider.Factory {

    private val cacheManager = HealthMetricsCacheManager(context)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return when {
            modelClass.isAssignableFrom(HeartRateViewModel::class.java) ->
                HeartRateViewModel(cacheManager) as T
            modelClass.isAssignableFrom(HrvViewModel::class.java) ->
                HrvViewModel(cacheManager) as T
            modelClass.isAssignableFrom(BloodOxygenViewModel::class.java) ->
                BloodOxygenViewModel(cacheManager) as T
            modelClass.isAssignableFrom(BloodPressureViewModel::class.java) ->
                BloodPressureViewModel(cacheManager) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}