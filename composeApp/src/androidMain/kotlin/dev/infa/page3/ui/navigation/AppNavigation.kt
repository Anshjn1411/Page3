package dev.infa.page3.ui.navigation

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.bluetooth.DeviceManager
import com.oudmon.ble.base.communication.CommandHandle
import com.oudmon.ble.base.scan.BleScannerHelper
import dev.infa.page3.models.DeviceCapabilities
import dev.infa.page3.models.HealthData
import dev.infa.page3.models.HealthSettings
import dev.infa.page3.R
import dev.infa.page3.models.SmartWatch
import dev.infa.page3.ui.screens.ConnectScreen
import dev.infa.page3.ui.screens.HomeScreen
import dev.infa.page3.ui.screens.MetricDetailScreen
import dev.infa.page3.ui.screens.MoreFunctionScreen
import dev.infa.page3.ui.screens.HeartRateScreen
import dev.infa.page3.ui.screens.StressScreen
import dev.infa.page3.ui.screens.BloodPressureScreen
import dev.infa.page3.ui.screens.BloodOxygenScreen
import dev.infa.page3.ui.screens.SleepScreen
import dev.infa.page3.ui.screens.StepScreen
import dev.infa.page3.ui.screens.HrvScreen
import dev.infa.page3.viewmodels.BloodOxygenViewModel
import dev.infa.page3.viewmodels.BloodPressureViewModel
import dev.infa.page3.viewmodels.ConnectionViewModel
import dev.infa.page3.viewmodels.DataSynchronization
import dev.infa.page3.viewmodels.HealthMeasurements
import dev.infa.page3.viewmodels.HealthMonitor
import dev.infa.page3.viewmodels.HealthMonitorCore
import dev.infa.page3.viewmodels.HeartRateViewModel
import dev.infa.page3.viewmodels.HomeViewModel
import dev.infa.page3.viewmodels.HrvViewModel
import dev.infa.page3.viewmodels.SleepViewModel
import dev.infa.page3.viewmodels.StepViewmodel
import dev.infa.page3.viewmodels.StressViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay


object Routes {
    const val Home = "home"
    const val SetGoal = "set_goal"
    const val Setting = "setting"
    const val MoreFunctions = "more_function"
    const val Connect = "connect"
    const val Detail = "detail/{type}"
    const val Sleep = "sleep"
    const val Step = "step"
    const val Heart = "heart"
    const val Stress = "stress"
    const val BloodPressure = "bp"
    const val BloodOxygen = "spo2"
    const val HRV = "hrv"
}
@Composable
fun AppNavigation(
    connectionViewModel: ConnectionViewModel,
    healthMonitor: HealthMonitor,
    commandHandle: CommandHandle?
) {
    val navController = rememberNavController()

    // Collect connection states once at top level
    val isConnected by connectionViewModel.isConnected.collectAsState()
    val deviceAddress by connectionViewModel.deviceAddress.collectAsState()
    val deviceCapabilities by connectionViewModel.deviceCapabilities.collectAsState()
//    val healthData by connectionViewModel.healthData.collectAsState()
//    val healthSettings by connectionViewModel.healthSettings.collectAsState()
//    val isReadingHealth by connectionViewModel.isReadingHealth.collectAsState()
    val batteryLevel by healthMonitor.batteryLevel.collectAsState()

    NavHost(navController = navController, startDestination = Routes.Home) {

        // ============ Home Screen ============
        composable(Routes.Home) {
            val sleepFactory = remember { SleepViewModelFactory(commandHandle, deviceAddress) }
            val sleepVm: SleepViewModel = viewModel(factory = sleepFactory)

            val stepFactory = remember { StepViewModelFactory(commandHandle, deviceAddress) }
            val stepVm: StepViewmodel = viewModel(factory = stepFactory)

            val homeFactory = remember { HomeViewModelFactory(commandHandle, deviceCapabilities) }
            val homeVm: HomeViewModel = viewModel(factory = homeFactory)

            LaunchedEffect(isConnected) {
                if (isConnected) homeVm.refreshAll()
            }

            HomeScreen(
                modifier = Modifier.fillMaxSize(),
                stepViewmodel = stepVm,
                sleepViewModel = sleepVm,
                homeViewModel = homeVm,
                navController = navController
            )
        }

        // ============ More Functions Screen ============
        composable(Routes.MoreFunctions) {
//            MoreFunctionScreen(
//                modifier = Modifier.fillMaxSize(),
//                isConnected = isConnected,
//                healthData = healthData,
//                healthSettings = healthSettings,
//                deviceCapabilities = deviceCapabilities,
//                isReadingHealth = isReadingHealth,
//
//                // Health Monitoring Actions
//                onReadBattery = { healthMonitor.readBattery() },
//                onReadHeartRate = { healthMonitor.readHeartRateSettings() },
//                onToggleHeartRate = { enabled, interval ->
//                    healthMonitor.toggleHeartRate(enabled, interval)
//                },
//                onMeasureHeartOnce = { healthMonitor.measureHeartOnce() },
//                onReadBloodPressure = { healthMonitor.measureBloodPressureOnce() },
//                onToggleBloodPressure = { enabled ->
//                    healthMonitor.toggleBloodPressure(enabled)
//                },
//                onMeasureBloodPressureOnce = { healthMonitor.measureBloodPressureOnce() },
//                onReadHrv = { healthMonitor.measureHrvOnce() },
//                onToggleHrv = { enabled -> healthMonitor.toggleHrv(enabled) },
//                onMeasureHrvOnce = { healthMonitor.measureHrvOnce() },
//                onReadTemperature = { healthMonitor.measureTemperatureOnce() },
//                onToggleTemperature = { enabled ->
//                    healthMonitor.toggleTemperatureSettings(enabled)
//                },
//                onMeasureTemperatureOnce = { healthMonitor.measureTemperatureOnce() },
//                onReadPressure = { healthMonitor.readPressureSettings() },
//                onTogglePressure = { enabled -> healthMonitor.togglePressure(enabled) },
//                onMeasurePressureOnce = { healthMonitor.measurePressureOnce() },
//                onReadBloodOxygen = { healthMonitor.measureBloodOxygenOnce() },
//                onToggleBloodOxygen = { enabled ->
//                    healthMonitor.toggleBloodOxygen(enabled)
//                },
//                onMeasureBloodOxygenOnce = { healthMonitor.measureBloodOxygenOnce() },
//                onOneKeyDetection = { healthMonitor.performOneKeyMeasurement() },
//                onPerformOneKeyMeasurement = { healthMonitor.performOneKeyMeasurement() },
//
//                // Exercise Actions
//                onStartExercise = { type -> healthMonitor.startExercise(type) },
//                onPauseExercise = { type -> healthMonitor.pauseExercise(type) },
//                onResumeExercise = { type -> healthMonitor.resumeExercise(type) },
//                onEndExercise = { type -> healthMonitor.endExercise(type) },
//                onSetSportsGoals = { steps, calories, distance, sportMin, sleepMin ->
//                    healthMonitor.setSportsGoals(steps, calories, distance, sportMin, sleepMin)
//                },
//
//                // Device Utilities
//                onFindDevice = { healthMonitor.findDevice() },
//                onFactoryReset = { healthMonitor.factoryResetDevice() },
//                onStartCalibration = { healthMonitor.startWearingCalibration() },
//                onStopCalibration = { healthMonitor.stopWearingCalibration() },
//
//                // Camera
//                onEnterCameraMode = { healthMonitor.enterCameraMode() },
//                onKeepCameraScreenOn = { healthMonitor.keepCameraScreenOn() },
//                onExitCameraMode = { healthMonitor.exitCameraMode() },
//
//                // Message Push
//                onEnableMessagePush = { healthMonitor.enableMessagePush() },
//                onPushMessage = { type, msg -> healthMonitor.pushMessage(type, msg) },
//
//                // Touch & Gesture
//                onReadTouchSettings = { isTouch ->
//                    healthMonitor.readTouchGestureSettings(isTouch)
//                },
//                onWriteTouchSettings = { appType, isTouch, strength ->
//                    healthMonitor.writeTouchGestureSettings(appType, isTouch, strength)
//                },
//
//                // Connection Actions
//                onScanDevices = { connectionViewModel.startScanning() },
//                onConnectDevice = { connectionViewModel.connectToDevice() },
//                onDisconnectDevice = { connectionViewModel.disconnectDevice() },
//                onSelectDevice = { device -> connectionViewModel.selectDevice(device) },
//                onReinitializeSDK = { /* Handle in Activity */ },
//                onRequestPermissions = { /* Handle in Activity */ },
//                onTestBLEScan = { /* Deprecated */ },
//
//                // Sync Operations
//                onSyncTodaySteps = { healthMonitor.syncTodaySteps() },
//                onSyncHeartRateData = {
//                    healthMonitor.syncHeartRateData(healthMonitor.getCurrentTimeWithTimezone())
//                },
//                onSyncSleepData = { address, offset ->
//                    healthMonitor.syncSleepData(address, offset)
//                },
//                onSyncTemperatureData = { healthMonitor.syncAutoTemperatureData(2) },
//                onSyncTrainingRecords = { healthMonitor.syncTrainingRecords() },
//                onSyncMuslimData = { offset -> healthMonitor.syncMuslimData(offset) },
//                onSyncAllHealthData = { healthMonitor.syncAllHealthData() },
//                onSyncHistoricalData = { days -> healthMonitor.syncHistoricalData(days) },
//                onSyncManualBloodPressure = { healthMonitor.syncManualBloodPressureData() },
//                onConfirmBloodPressureSync = { healthMonitor.confirmBloodPressureSync() },
//                onSyncNewSleepData = { offset, includeLunch ->
//                    healthMonitor.syncNewSleepData(offset, includeLunch)
//                },
//                onSyncSedentaryData = { offset -> healthMonitor.syncSedentaryData(offset) },
//                onSyncDetailStepData = { offset -> healthMonitor.syncDetailStepData(offset) },
//                onSyncManualTemperature = { days ->
//                    healthMonitor.syncManualTemperatureData(days)
//                },
//
//                // Raw Data Measurements
//                onMeasureHeartRateRaw = { seconds ->
//                    healthMonitor.measureHeartRateRawData(seconds)
//                },
//                onMeasureBloodOxygenRaw = { seconds ->
//                    healthMonitor.measureBloodOxygenRawData(seconds)
//                },
//                onCalculateBloodPressure = { hr, age ->
//                    healthMonitor.calculateBloodPressureFromHR(hr, age)
//                },
//                onInitTemperatureCallback = { healthMonitor.initTemperatureCallback() },
//
//                // Navigation
//                onOpenConnect = { navController.navigate(Routes.Connect) },
//                onOpenDetail = { type -> navController.navigate("detail/$type") }
//            )
        }

        // ============ Sleep Screen ============
        composable(Routes.Sleep) {
            val factory = remember { SleepViewModelFactory(commandHandle, deviceAddress) }
            val viewModel: SleepViewModel = viewModel(factory = factory)
            SleepScreen(
                viewModel = viewModel,
                navController = navController
            )
        }

        // ============ Heart Rate Screen ============
        composable(Routes.Heart) {
            val factory = remember { HeartRateViewModelFactory(commandHandle) }
            val viewModel: HeartRateViewModel = viewModel(factory = factory)
            HeartRateScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ============ Stress Screen ============
        composable(Routes.Stress) {
            val factory = remember { StressViewModelFactory(commandHandle) }
            val viewModel: StressViewModel = viewModel(factory = factory)
            StressScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ============ Blood Pressure Screen ============
        composable(Routes.BloodPressure) {
            val factory = remember { BloodPressureViewModelFactory(commandHandle) }
            val viewModel: BloodPressureViewModel = viewModel(factory = factory)
            BloodPressureScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ============ Blood Oxygen Screen ============
        composable(Routes.BloodOxygen) {
            val factory = remember { BloodOxygenViewModelFactory(commandHandle) }
            val viewModel: BloodOxygenViewModel = viewModel(factory = factory)
            BloodOxygenScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ============ HRV Screen (Commented) ============
        composable(Routes.HRV) {
            val factory = remember { HrvViewModelFactory(commandHandle) }
            val viewModel: HrvViewModel = viewModel(factory = factory)
            HrvScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ============ Step Screen ============
        composable(Routes.Step) {
            val factory = remember { StepViewModelFactory(commandHandle, deviceAddress) }
            val viewModel: StepViewmodel = viewModel(factory = factory)
            StepScreen(
                viewModel = viewModel,
                navController = navController
            )
        }

        // ============ Connect Screen ============
        composable(Routes.Connect) {
            ConnectScreen(
                viewModel = connectionViewModel,
                navController = navController
            )
        }

        // ============ Settings Screen ============
        composable(Routes.Setting) {
            ConnectScreen(
                viewModel = connectionViewModel,
                navController = navController
            )
        }

        // ============ Detail Screen ============
        composable(Routes.Detail) { backStack ->
            val type = backStack.arguments?.getString("type") ?: "battery"
            MetricDetailScreen(
                type = type,
                isReading = true,
                healthData = HealthData(),
                battery = batteryLevel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

// ============ ViewModel Factories ============

class SleepViewModelFactory(
    private val commandHandle: CommandHandle?,
    private val deviceAddress: String = ""
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SleepViewModel::class.java)) {
            val dataSync = DataSynchronization(commandHandle) { log ->
                Log.d("SleepViewModel", log)
            }
            return SleepViewModel(dataSync, deviceAddress) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}

class StepViewModelFactory(
    private val commandHandle: CommandHandle?,
    private val deviceAddress: String = ""
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
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
    private val commandHandle: CommandHandle?,
    private val deviceCapabilities: DeviceCapabilities
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            val dataSync = DataSynchronization(commandHandle) { log ->
                Log.d("HomeViewModel", log)
            }
            return HomeViewModel(dataSync, deviceCapabilities) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}

class HeartRateViewModelFactory(
    private val commandHandle: CommandHandle?,
    private val healthMonitorCore: HealthMonitorCore? = null
) : ViewModelProvider.Factory {

    // Create a coroutine scope for the factory
    private val factoryScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HeartRateViewModel::class.java)) {

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
                        supportsTemperature = true,
                        supportsOneKeyCheck = true
                    )
                },
                setReadingStatus = { isReading ->
                    Log.d("Measurements", "Status: $isReading")
                }
            )

            return HeartRateViewModel(dataSync, healthCore, healthMeasurements) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}

data class DeviceCapabilities(
    val supportsTemperature: Boolean = true,
    val supportsOneKeyCheck: Boolean = true,
    val supportsBloodPressure: Boolean = true,
    val supportsSpO2: Boolean = true,
    val supportsHRV: Boolean = true
)

class StressViewModelFactory(
    private val commandHandle: CommandHandle?,
    private val healthMonitorCore: HealthMonitorCore? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    private val factoryScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StressViewModel::class.java)) {
            val dataSync = DataSynchronization(commandHandle) { log ->
                Log.d("StressViewModel", log)
            }
            
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
                    Log.d("Measurements", "Stress measurement: ${healthData.pressure}%")
                },
                deviceCapabilities = {
                    DeviceCapabilities(
                        supportsTemperature = true,
                        supportsOneKeyCheck = true
                    )
                },
                setReadingStatus = { isReading ->
                    Log.d("Measurements", "Status: $isReading")
                }
            )

            return StressViewModel(dataSync, healthCore, healthMeasurements) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}

class BloodPressureViewModelFactory(
    private val commandHandle: CommandHandle?,
    private val healthMonitorCore: HealthMonitorCore? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BloodPressureViewModel::class.java)) {
            val dataSync = DataSynchronization(commandHandle) { log -> Log.d("BloodPressureViewModel", log) }
            val healthCore = healthMonitorCore ?: HealthMonitorCore(commandHandle) { log -> Log.d("HealthCore", log) }
            val healthMeasurements = HealthMeasurements(
                commandHandle = commandHandle,
                coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob()),
                addLog = { log -> Log.d("Measurements", log) },
                healthDataUpdater = { hd -> healthCore.healthData = hd },
                deviceCapabilities = { DeviceCapabilities() },
                setReadingStatus = { }
            )
            return BloodPressureViewModel(dataSync, healthCore, healthMeasurements) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}

class BloodOxygenViewModelFactory(
    private val commandHandle: CommandHandle?,
    private val healthMonitorCore: HealthMonitorCore? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    private val factoryScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BloodOxygenViewModel::class.java)) {

            val dataSync = DataSynchronization(commandHandle) { log ->
                Log.d("BloodOxygenViewModel", log)
            }
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
                        supportsTemperature = true,
                        supportsOneKeyCheck = true
                    )
                },
                setReadingStatus = { isReading ->
                    Log.d("Measurements", "Status: $isReading")
                }
            )

            return BloodOxygenViewModel(
                dataSynchronization = dataSync,
                healthMonitorCore = healthCore,
                healthMeasurements = healthMeasurements
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}

class HrvViewModelFactory(
    private val commandHandle: CommandHandle?,
    private val healthMonitorCore: HealthMonitorCore? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    private val factoryScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HrvViewModel::class.java)) {
            val dataSync = DataSynchronization(commandHandle) { log ->
                Log.d("HrvViewModel", log)
            }
            
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
                    Log.d("Measurements", "HRV measurement: ${healthData.hrvValue} ms")
                },
                deviceCapabilities = {
                    DeviceCapabilities(
                        supportsTemperature = true,
                        supportsOneKeyCheck = true
                    )
                },
                setReadingStatus = { isReading ->
                    Log.d("Measurements", "Status: $isReading")
                }
            )

            return HrvViewModel(dataSync, healthCore, healthMeasurements) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
