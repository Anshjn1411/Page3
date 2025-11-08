package dev.infa.page3.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import com.oudmon.ble.base.communication.CommandHandle
import dev.infa.page3.models.DeviceCapabilities
import dev.infa.page3.models.HealthData
import dev.infa.page3.ui.screens.HomeScreen
import dev.infa.page3.ui.screens.MetricDetailScreen
import dev.infa.page3.ui.screens.HeartRateScreen
import dev.infa.page3.ui.screens.StressScreen
import dev.infa.page3.ui.screens.BloodPressureScreen
import dev.infa.page3.ui.screens.BloodOxygenScreen
import dev.infa.page3.ui.screens.GoalsSettingsScreen
import dev.infa.page3.ui.screens.SleepScreen
import dev.infa.page3.ui.screens.StepScreen
import dev.infa.page3.ui.screens.HrvScreen
import dev.infa.page3.ui.screens.ProfileScreen
import dev.infa.page3.ui.screens.ScannerScreen
import dev.infa.page3.ui.screens.ExerciseScreen
import dev.infa.page3.viewmodels.BloodOxygenViewModel
import dev.infa.page3.viewmodels.BloodPressureViewModel
import dev.infa.page3.viewmodels.ConnectionViewModel
import dev.infa.page3.viewmodels.DataSynchronization
import dev.infa.page3.viewmodels.HealthMeasurements
import dev.infa.page3.viewmodels.HealthMonitorCore
import dev.infa.page3.viewmodels.HeartRateViewModel
import dev.infa.page3.viewmodels.HomeViewModel
import dev.infa.page3.viewmodels.HrvViewModel
import dev.infa.page3.viewmodels.ExerciseViewModel
import dev.infa.page3.viewmodels.ProfileViewModel
import dev.infa.page3.viewmodels.SleepViewModel
import dev.infa.page3.viewmodels.StepViewmodel
import dev.infa.page3.viewmodels.StressViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

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
}
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val connectionViewModel: ConnectionViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    val uiState by connectionViewModel.uiState.collectAsState()
    val isConnected = uiState.isConnected
    val deviceAddress = uiState.connectedDevice?.deviceAddress ?: ""
    val exerciseVmFactory = remember { ExerciseViewModelFactory(null) }
    val exerciseViewModel: ExerciseViewModel = viewModel(factory = exerciseVmFactory)

    NavHost(navController = navController, startDestination = Routes.Home) {

        composable(Routes.Home) {
            val homeFactory = remember { HomeViewModelFactory( ) }
            val homeVm: HomeViewModel = viewModel(factory = homeFactory)
            LaunchedEffect(isConnected) {
                if (isConnected) homeVm.refreshAll()
            }
            HomeScreen(
                modifier = Modifier.fillMaxSize(),
                connectionViewModel = connectionViewModel,
                homeViewModel = homeVm,
                navController = navController
            )
        }
        // ============ Exercise Screen ============
        composable(Routes.Exercise) {

            ExerciseScreen(
                viewModel = exerciseViewModel,
                navController = navController
            )
        }
        composable(Routes.Sleep) {
            val factory = remember { SleepViewModelFactory( deviceAddress) }
            val viewModel: SleepViewModel = viewModel(factory = factory)
            SleepScreen(
                viewModel = viewModel,
                navController = navController
            )
        }

        // ============ Heart Rate Screen ============
        composable(Routes.Heart) {
            val factory = remember { HeartRateViewModelFactory() }
            val viewModel: HeartRateViewModel = viewModel(factory = factory)
            HeartRateScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ============ Stress Screen ============
        composable(Routes.Stress) {
            val factory = remember { StressViewModelFactory() }
            val viewModel: StressViewModel = viewModel(factory = factory)
            StressScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ============ Blood Pressure Screen ============
        composable(Routes.BloodPressure) {
            val factory = remember { BloodPressureViewModelFactory() }
            val viewModel: BloodPressureViewModel = viewModel(factory = factory)
            BloodPressureScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ============ Blood Oxygen Screen ============
        composable(Routes.BloodOxygen) {
            val factory = remember { BloodOxygenViewModelFactory() }
            val viewModel: BloodOxygenViewModel = viewModel(factory = factory)
            BloodOxygenScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ============ HRV Screen (Commented) ============
        composable(Routes.HRV) {
            val factory = remember { HrvViewModelFactory() }
            val viewModel: HrvViewModel = viewModel(factory = factory)
            HrvScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ============ Step Screen ============
        composable(Routes.Step) {
            val factory = remember { StepViewModelFactory( deviceAddress) }
            val viewModel: StepViewmodel = viewModel(factory = factory)
            StepScreen(
                viewModel = viewModel,
                navController = navController
            )
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
            val homeFactory = remember { HomeViewModelFactory( ) }
            val homeVm: HomeViewModel = viewModel(factory = homeFactory)
            GoalsSettingsScreen(homeViewModel =  homeVm ,navController)
        }
    }
}

// ============ ViewModel Factories ============

class SleepViewModelFactory(
    private val deviceAddress: String = ""
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SleepViewModel::class.java)) {
             val commandHandle = CommandHandle.getInstance()
            val dataSync = DataSynchronization(commandHandle) { log ->
                Log.d("SleepViewModel", log)
            }
            return SleepViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}

class StepViewModelFactory(
    private val deviceAddress: String = ""
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
         val commandHandle = CommandHandle.getInstance()
        if (modelClass.isAssignableFrom(StepViewmodel::class.java)) {
            val dataSync = DataSynchronization(commandHandle) { log ->
                Log.d("StepViewModel", log)
            }
            return StepViewmodel(dataSync) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}

class HomeViewModelFactory(
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
             val commandHandle = CommandHandle.getInstance()
            val dataSync = DataSynchronization(commandHandle) { log ->
                Log.d("HomeViewModel", log)
            }
            return HomeViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}

class HeartRateViewModelFactory(
    private val healthMonitorCore: HealthMonitorCore? = null
) : ViewModelProvider.Factory {

    // Create a coroutine scope for the factory
    private val factoryScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HeartRateViewModel::class.java)) {
            val commandHandle = CommandHandle.getInstance()

            val dataSync = DataSynchronization(
                commandHandle = commandHandle,
                addLog = { log ->
                    Log.d("DataSync", log)
                }
            )

            // Use existing HealthMonitorCore or create new one
            val healthCore = healthMonitorCore ?: HealthMonitorCore(
                commandHandle = commandHandle,
                addLog = { log ->
                    Log.d("HealthCore", log)
                }
            )

            val healthMeasurements = HealthMeasurements(
                commandHandle = commandHandle,
                coroutineScope = factoryScope,
                addLog = { log ->
                    Log.d("Measurements", log)
                },
                healthDataUpdater = { healthData ->
                    // CRITICAL: Update the healthCore's data when measurement callback fires
                    healthCore.healthData = healthData
                    Log.d("Measurements", "HR measurement: ${healthData.heartRate} BPM")
                },
                deviceCapabilities = {
                    DeviceCapabilities(
                        supportTemperature = true,
                        supportOneKeyCheck = true
                    )
                },
                setReadingStatus = { isReading ->
                    Log.d("Measurements", "Status: $isReading")
                }
            )

            return HeartRateViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}

class StressViewModelFactory() : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StressViewModel::class.java)) { return StressViewModel() as T }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}

class BloodPressureViewModelFactory() : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BloodPressureViewModel::class.java)) { return BloodPressureViewModel() as T }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}

class BloodOxygenViewModelFactory() : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BloodOxygenViewModel::class.java)) { return BloodOxygenViewModel() as T }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}

class HrvViewModelFactory() : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HrvViewModel::class.java)) { return HrvViewModel() as T }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}

class ExerciseViewModelFactory(private val repo: dev.infa.page3.data.repository.ExerciseRepository?) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExerciseViewModel::class.java)) {
            return ExerciseViewModel(repository = repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
