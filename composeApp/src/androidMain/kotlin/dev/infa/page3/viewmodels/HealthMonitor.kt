//package dev.infa.page3.viewmodels
//
//import com.oudmon.ble.base.communication.CommandHandle
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.flow.StateFlow
//
///**
// * Health monitoring class for managing all health-related data and operations
// */
//class HealthMonitor(
//    private val commandHandle: CommandHandle?,
//    private val coroutineScope: CoroutineScope,
//    private val addLog: (String) -> Unit
//)
//{
//    companion object {
//        const val TAG = "HealthMonitor"
//    }
//
//    // Initialize all sub-components
//    private val healthCore = HealthMonitorCore(commandHandle,  addLog)
//    private val measurements = HealthMeasurements(
//        commandHandle,
//        coroutineScope,
//        addLog,
//        { newData -> healthCore.healthData = newData },
//        { healthCore.deviceCapabilities },
//        { reading -> healthCore.isReadingHealth = reading }
//    )
//    private val dataSync = DataSynchronization(commandHandle, addLog)
//    private val deviceControls = DeviceControls(
//        commandHandle,
//        addLog,
//        { capabilities -> healthCore.deviceCapabilities = capabilities }
//    )
//    private val notifications = NotificationsAndListeners(
//        commandHandle,
//        coroutineScope,
//        addLog,
//        { battery -> healthCore.updateBatteryLevel(battery) }
//    )
//
//    // Expose the health data and settings from core
//    val healthData get() = healthCore.healthData
//    val healthSettings get() = healthCore.healthSettings
//    var deviceCapabilities get() = healthCore.deviceCapabilities
//        set(value) {}
//    val isReadingHealth get() = healthCore.isReadingHealth
//    val batteryLevel: StateFlow<Int> get() = healthCore.batteryLevel
//
//    init {
//        addLog("HealthMonitor initialized, battery level StateFlow: ${healthCore.batteryLevel}")
//    }
//
//    // Core Health Functions (delegate to HealthMonitorCore)
//    fun readBattery() = healthCore.readBattery()
//    fun testBatteryUpdate() = healthCore.testBatteryUpdate()
//    fun readHeartRateSettings() = healthCore.readHeartRateSettings()
//    fun toggleHeartRate(enabled: Boolean, interval: Int) = healthCore.toggleHeartRate(enabled, interval)
//    fun toggleBloodPressure(enabled: Boolean) = healthCore.toggleBloodPressure(enabled)
//    fun toggleBloodOxygen(enabled: Boolean) = healthCore.toggleBloodOxygen(enabled)
//    fun toggleHrv(enabled: Boolean) = healthCore.toggleHrv(enabled)
//    fun readPressureSettings() = healthCore.readPressureSettings()
//    fun togglePressure(enabled: Boolean) = healthCore.togglePressure(enabled)
//    fun toggleTemperatureSettings(enabled: Boolean) = healthCore.toggleTemperatureSettings(enabled)
//    fun resetHealthData() = healthCore.resetHealthData()
//
//    // Manual Measurement Functions (delegate to HealthMeasurements)
//    fun measureHeartOnce() = measurements.measureHeartOnce()
//    fun measureBloodPressureOnce() = measurements.measureBloodPressureOnce()
//    fun measureBloodOxygenOnce() = measurements.measureBloodOxygenOnce()
//    fun measureHrvOnce() = measurements.measureHrvOnce()
//    fun measurePressureOnce() = measurements.measurePressureOnce()
//    fun measureTemperatureOnce() = measurements.measureTemperatureOnce()
//    fun performOneKeyMeasurement() = measurements.performOneKeyMeasurement()
//    fun measureHeartRateRawData(seconds: Int) = measurements.measureHeartRateRawData(seconds)
//    fun measureBloodOxygenRawData(seconds: Int) = measurements.measureBloodOxygenRawData(seconds)
//    fun calculateBloodPressureFromHR(heartRate: Int, age: Int) = measurements.calculateBloodPressureFromHR(heartRate, age)
//
//    // Data Synchronization Functions (delegate to DataSynchronization)
//    fun syncTodaySteps() = dataSync.syncTodaySteps()
//    fun syncDetailStepData(dayOffset: Int) = dataSync.syncDetailStepData(dayOffset)
//    fun syncSleepData(deviceAddress: String, dayOffset: Int) = dataSync.syncSleepData(deviceAddress, dayOffset)
//    fun syncNewSleepData(offset: Int, includeLunch: Boolean = false) =
//        {  }
//    fun syncSedentaryData(offset: Int) = {}
//    fun syncHeartRateData(nowTime: Int) = dataSync.syncHeartRateData(nowTime)
//    fun syncBloodOxygenData() = {  }
//    fun syncAutomaticBloodPressureData() = {  }
//    fun syncManualBloodPressureData() = {  }
//    fun confirmBloodPressureSync() = {  }
//    fun syncPressureData(offset: Int) = {  }
//    fun syncHrvData(offset: Int) = {  }
//    fun initTemperatureCallback() = {  }
//    fun syncAutoTemperatureData(days: Int = 2) = {  }
//    fun syncManualTemperatureData(days: Int = 0) = {  }
//    fun syncTrainingRecords(lastSyncTime: Long = 0) = {  }
//    fun syncMuslimData(dayOffset: Int) = {  }
//    fun getCurrentTimeWithTimezone() = dataSync.getCurrentTimeWithTimezone()
//    fun syncAllHealthData() = {  }
//    fun syncHistoricalData(days: Int = 7) = {  }
//
//    // Device Control Functions (delegate to DeviceControls)
//    fun readHardwareInfo() = deviceControls.readHardwareInfo()
//    fun readFirmwareInfo() = deviceControls.readFirmwareInfo()
//    fun readDeviceSupportedFunctions() = deviceControls.readDeviceSupportedFunctions()
//    fun setSportsGoals(stepGoal: Int, calorieGoal: Int, distanceGoal: Int, sportMinuteGoal: Int, sleepMinuteGoal: Int) =
//        deviceControls.setSportsGoals(stepGoal, calorieGoal, distanceGoal, sportMinuteGoal, sleepMinuteGoal)
//    fun findDevice() = deviceControls.findDevice()
//    fun factoryResetDevice() = deviceControls.factoryResetDevice()
//    fun startWearingCalibration() = deviceControls.startWearingCalibration()
//    fun stopWearingCalibration() = deviceControls.stopWearingCalibration()
//    fun enterCameraMode() = deviceControls.enterCameraMode()
//    fun keepCameraScreenOn() = deviceControls.keepCameraScreenOn()
//    fun exitCameraMode() = deviceControls.exitCameraMode()
//    fun readTouchGestureSettings(isTouch: Boolean = true) = deviceControls.readTouchGestureSettings(isTouch)
//    fun writeTouchGestureSettings(appType: Int, isTouch: Boolean, strength: Int) =
//        deviceControls.writeTouchGestureSettings(appType, isTouch, strength)
//    fun startExercise(sportType: Int) = deviceControls.startExercise(sportType)
//    fun pauseExercise(sportType: Int) = deviceControls.pauseExercise(sportType)
//    fun resumeExercise(sportType: Int) = deviceControls.resumeExercise(sportType)
//    fun endExercise(sportType: Int) = deviceControls.endExercise(sportType)
//    fun checkFirmwareFile(filePath: String) = deviceControls.checkFirmwareFile(filePath)
//    fun startFirmwareUpdate(filePath: String) = deviceControls.startFirmwareUpdate(filePath)
//    fun performCompleteDeviceSetup() = deviceControls.performCompleteDeviceSetup()
//
//    // Notification and Listener Functions (delegate to NotificationsAndListeners)
//    fun enableMessagePush() = notifications.enableMessagePush()
//    fun pushMessage(type: Int, message: String) = notifications.pushMessage(type, message)
//    fun pushCallReminder(message: String) = notifications.pushCallReminder(message)
//    fun pushSMSReminder(message: String) = notifications.pushSMSReminder(message)
//    fun pushQQReminder(message: String) = notifications.pushQQReminder(message)
//    fun pushWeChatReminder(message: String) = notifications.pushWeChatReminder(message)
//    fun pushIncomingCallAction(message: String) = notifications.pushIncomingCallAction(message)
//    fun pushFacebookReminder(message: String) = notifications.pushFacebookReminder(message)
//    fun pushWhatsAppReminder(message: String) = notifications.pushWhatsAppReminder(message)
//    fun pushTwitterReminder(message: String) = notifications.pushTwitterReminder(message)
//    fun pushSkypeReminder(message: String) = notifications.pushSkypeReminder(message)
//    fun pushLineReminder(message: String) = notifications.pushLineReminder(message)
//    fun pushLinkedInReminder(message: String) = notifications.pushLinkedInReminder(message)
//    fun pushInstagramReminder(message: String) = notifications.pushInstagramReminder(message)
//    fun pushTIMReminder(message: String) = notifications.pushTIMReminder(message)
//    fun pushSnapchatReminder(message: String) = notifications.pushSnapchatReminder(message)
//    fun pushOtherNotification(message: String) = notifications.pushOtherNotification(message)
//    fun addDeviceDataListener() = notifications.addDeviceDataListener()
//    fun removeDeviceDataListener() = notifications.removeDeviceDataListener()
//    fun addSportDataListener() = notifications.addSportDataListener()
//    fun removeSportDataListener() = notifications.removeSportDataListener()
//    fun addCameraPhotoListener() = notifications.addCameraPhotoListener()
//    fun removeCameraPhotoListener() = notifications.removeCameraPhotoListener()
//    fun addHeartRateListener() = notifications.addHeartRateListener()
//    fun addBloodPressureListener() = notifications.addBloodPressureListener()
//    fun addBloodOxygenListener() = notifications.addBloodOxygenListener()
//    fun addTemperatureListener() = notifications.addTemperatureListener()
//    fun addSportRecordListener() = notifications.addSportRecordListener()
//    fun removeHeartRateListener() = notifications.removeHeartRateListener()
//    fun removeBloodPressureListener() = notifications.removeBloodPressureListener()
//    fun removeBloodOxygenListener() = notifications.removeBloodOxygenListener()
//    fun removeTemperatureListener() = notifications.removeTemperatureListener()
//    fun removeSportRecordListener() = notifications.removeSportRecordListener()
//    fun addAllListeners() = notifications.addAllListeners()
//    fun removeAllListeners() = notifications.removeAllListeners()
//
//    // Comprehensive workflow functions
//    fun initializeDevice() {
//        addLog("Initializing device with comprehensive setup...")
//
//        // Step 1: Read device capabilities and info
//        readDeviceSupportedFunctions()
//        readHardwareInfo()
//        readFirmwareInfo()
//
//        // Step 2: Read current health settings
//        readHeartRateSettings()
//        readPressureSettings()
//        readBattery()
//
//        // Step 3: Set up listeners
//        addAllListeners()
//
//        // Step 4: Enable message push
//        enableMessagePush()
//
//        // Step 5: Initialize temperature callbacks if supported
//        if (deviceCapabilities.supportsTemperature) {
//            initTemperatureCallback()
//        }
//
//        addLog("Device initialization completed")
//    }
//
//    fun performFullHealthSync() {
//        addLog("Starting full health data synchronization...")
//
//        // Sync all current health data
//        syncAllHealthData()
//
//        // Sync training records
//        syncTrainingRecords()
//
//        // Sync Muslim data if supported
//        if (deviceCapabilities.supportsMuslim) {
//            syncMuslimData(0)
//        }
//
//        addLog("Full health data sync completed")
//    }
//
//    fun performComprehensiveHealthCheck() {
//        if (!deviceCapabilities.supportsOneKeyCheck) {
//            addLog("Performing individual measurements (device doesn't support one-key)...")
//            measureHeartOnce()
//            measureBloodPressureOnce()
//            measureBloodOxygenOnce()
//            measureHrvOnce()
//            measurePressureOnce()
//            if (deviceCapabilities.supportsTemperature) {
//                measureTemperatureOnce()
//            }
//        } else {
//            addLog("Performing one-key comprehensive measurement...")
//            performOneKeyMeasurement()
//        }
//    }performOneKeyMeasurement
//
//    fun cleanupAndDisconnect() {
//        addLog("Cleaning up resources and preparing for disconnection...")
//
//        // Remove all listeners
//        removeAllListeners()
//
//        // Reset health data
//        resetHealthData()
//
//        addLog("Cleanup completed - ready for disconnection")
//    }
//}
//
//
