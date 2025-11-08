//package dev.infa.page3.viewmodels
//
//import com.oudmon.ble.base.bluetooth.BleOperateManager
//import com.oudmon.ble.base.communication.CommandHandle
//import com.oudmon.ble.base.communication.Constants
//import com.oudmon.ble.base.communication.DfuHandle
//import com.oudmon.ble.base.communication.ICommandResponse
//import com.oudmon.ble.base.communication.req.CameraReq
//import com.oudmon.ble.base.communication.req.DeviceSupportReq
//import com.oudmon.ble.base.communication.req.FindDeviceReq
//import com.oudmon.ble.base.communication.req.PhoneSportReq
//import com.oudmon.ble.base.communication.req.RestoreKeyReq
//import com.oudmon.ble.base.communication.req.TargetSettingReq
//import com.oudmon.ble.base.communication.req.TouchControlReq
//import com.oudmon.ble.base.communication.rsp.AppSportRsp
//import com.oudmon.ble.base.communication.rsp.BaseRspCmd
//import com.oudmon.ble.base.communication.rsp.DeviceSupportFunctionRsp
//import com.oudmon.ble.base.communication.rsp.TouchControlResp
//import dev.infa.page3.models.DeviceCapabilities
//
//class DeviceControls(
//    private val commandHandle: CommandHandle?,
//    private val addLog: (String) -> Unit,
//    private val deviceCapabilitiesUpdater: (DeviceCapabilities) -> Unit
//)
//{
//    companion object {
//        const val TAG = "DeviceControls"
//    }
//
//    // Device Information Functions
//    fun readHardwareInfo() {
//        if (commandHandle == null) {
//            addLog("ERROR: CommandHandle not initialized")
//            return
//        }
//        addLog("Reading hardware information...")
//        try {
//            commandHandle.execReadCmd(commandHandle.readHwRequest)
//            addLog("Hardware info request sent")
//        } catch (e: Exception) {
//            addLog("ERROR: Exception reading hardware info: ${e.message}")
//        }
//    }
//
//    fun readFirmwareInfo() {
//        if (commandHandle == null) {
//            addLog("ERROR: CommandHandle not initialized")
//            return
//        }
//        addLog("Reading firmware information...")
//        try {
//            commandHandle.execReadCmd(commandHandle.readFmRequest)
//            addLog("Firmware info request sent")
//        } catch (e: Exception) {
//            addLog("ERROR: Exception reading firmware info: ${e.message}")
//        }
//    }
//
//    fun readDeviceSupportedFunctions() {
//        if (commandHandle == null) {
//            addLog("ERROR: CommandHandle not initialized")
//            return
//        }
//        addLog("Reading device supported functions...")
//        try {
//            commandHandle.executeReqCmd(
//                DeviceSupportReq.getReadInstance(),
//                object : ICommandResponse<DeviceSupportFunctionRsp> {
//                    override fun onDataResponse(resultEntity: DeviceSupportFunctionRsp) {
//                        if (resultEntity.status == BaseRspCmd.RESULT_OK) {
//                            deviceCapabilitiesUpdater(
//                                DeviceCapabilities(
//                                    supportsTouch = resultEntity.supportTouch,
//                                    supportsMuslim = resultEntity.supportMoslin,
//                                    supportsAppRevision = resultEntity.supportAPPRevision,
//                                    supportsBlePair = resultEntity.supportBlePair,
//                                    supportsGesture = resultEntity.supportGesture,
//                                    supportsMusic = resultEntity.supportRingMusic,
//                                    supportsVideo = resultEntity.supportRingVideo,
//                                    supportsEbook = resultEntity.supportRingEbook,
//                                    supportsCamera = resultEntity.supportRingCamera,
//                                    supportsPhoneCall = resultEntity.supportRingPhoneCall,
//                                    supportsGame = resultEntity.supportRingGame
//                                )
//                            )
//                            addLog("Device capabilities updated")
//                            addLog("Touch: ${resultEntity.supportTouch}, Gesture: ${resultEntity.supportGesture}")
//                            addLog("Camera: ${resultEntity.supportRingCamera}, Music: ${resultEntity.supportRingMusic}")
//                        } else {
//                            addLog("ERROR: Failed to read device capabilities")
//                        }
//                    }
//                }
//            )
//        } catch (e: Exception) {
//            addLog("ERROR: Exception reading device capabilities: ${e.message}")
//        }
//    }
//
//    // Sports Goal Setting
//    fun setSportsGoals(
//        stepGoal: Int,
//        calorieGoal: Int, // in kcal, will be multiplied by 1000
//        distanceGoal: Int, // in meters
//        sportMinuteGoal: Int, // in minutes
//        sleepMinuteGoal: Int // in minutes
//    ) {
//        if (commandHandle == null) {
//            addLog("ERROR: CommandHandle not initialized")
//            return
//        }
//
//        addLog("Setting sports goals...")
//        try {
//            commandHandle.executeReqCmd(
//                TargetSettingReq.getWriteInstance(
//                    stepGoal,
//                    calorieGoal * 1000, // Convert kcal to calories as per docs
//                    distanceGoal,
//                    sportMinuteGoal,
//                    sleepMinuteGoal
//                ),
//                null
//            )
//            addLog("Sports goals set - Steps: $stepGoal, Calories: ${calorieGoal}kcal, Distance: ${distanceGoal}m")
//            addLog("Sport time: ${sportMinuteGoal}min, Sleep time: ${sleepMinuteGoal}min")
//        } catch (e: Exception) {
//            addLog("ERROR: Exception setting sports goals: ${e.message}")
//        }
//    }
//
//    // Find Device
//    fun findDevice() {
//        if (commandHandle == null) {
//            addLog("ERROR: CommandHandle not initialized")
//            return
//        }
//
//        addLog("Finding device...")
//        try {
//            commandHandle.executeReqCmd(FindDeviceReq(), null)
//            addLog("Find device command sent")
//        } catch (e: Exception) {
//            addLog("ERROR: Exception finding device: ${e.message}")
//        }
//    }
//
//    // Factory Reset
//    fun factoryResetDevice() {
//        if (commandHandle == null) {
//            addLog("ERROR: CommandHandle not initialized")
//            return
//        }
//
//        addLog("Performing factory reset...")
//        try {
//            commandHandle.executeReqCmd(
//                RestoreKeyReq(Constants.CMD_RE_STORE),
//                null
//            )
//            addLog("Factory reset command sent")
//        } catch (e: Exception) {
//            addLog("ERROR: Exception during factory reset: ${e.message}")
//        }
//    }
//
//    // Wearing Calibration
//    fun startWearingCalibration() {
//        addLog("Starting wearing calibration...")
//        try {
//            BleOperateManager.getInstance().ringCalibration(true) { result ->
//                when (result.success) {
//                    1 -> addLog("Wearing calibration successful")
//                    2 -> addLog("Wearing calibration in progress...")
//                    3 -> addLog("Wearing calibration failed")
//                    else -> addLog("Wearing calibration status: ${result.success}")
//                }
//            }
//        } catch (e: Exception) {
//            addLog("ERROR: Exception starting wearing calibration: ${e.message}")
//        }
//    }
//
//    fun stopWearingCalibration() {
//        addLog("Stopping wearing calibration...")
//        try {
//            BleOperateManager.getInstance().ringCalibration(false) { result ->
//                addLog("Wearing calibration stopped")
//            }
//        } catch (e: Exception) {
//            addLog("ERROR: Exception stopping wearing calibration: ${e.message}")
//        }
//    }
//
//    // Camera Control
//    fun enterCameraMode() {
//        if (commandHandle == null) {
//            addLog("ERROR: CommandHandle not initialized")
//            return
//        }
//
//        addLog("Entering camera mode...")
//        try {
//            commandHandle.executeReqCmd(
//                CameraReq(CameraReq.ACTION_INTO_CAMARA_UI),
//                null
//            )
//            addLog("Camera mode activated")
//        } catch (e: Exception) {
//            addLog("ERROR: Exception entering camera mode: ${e.message}")
//        }
//    }
//
//    fun keepCameraScreenOn() {
//        if (commandHandle == null) {
//            addLog("ERROR: CommandHandle not initialized")
//            return
//        }
//
//        try {
//            commandHandle.executeReqCmd(
//                CameraReq(CameraReq.ACTION_KEEP_SCREEN_ON),
//                null
//            )
//        } catch (e: Exception) {
//            addLog("ERROR: Exception keeping camera screen on: ${e.message}")
//        }
//    }
//
//    fun exitCameraMode() {
//        if (commandHandle == null) {
//            addLog("ERROR: CommandHandle not initialized")
//            return
//        }
//
//        addLog("Exiting camera mode...")
//        try {
//            commandHandle.executeReqCmd(
//                CameraReq(CameraReq.ACTION_FINISH),
//                null
//            )
//            addLog("Camera mode exited")
//        } catch (e: Exception) {
//            addLog("ERROR: Exception exiting camera mode: ${e.message}")
//        }
//    }
//
//    // Touch and Gesture Control
//    fun readTouchGestureSettings(isTouch: Boolean = true) {
//        if (commandHandle == null) {
//            addLog("ERROR: CommandHandle not initialized")
//            return
//        }
//
//        addLog("Reading ${if (isTouch) "touch" else "gesture"} settings...")
//        try {
//            commandHandle.executeReqCmd(
//                TouchControlReq.getReadInstance(isTouch),
//                object : ICommandResponse<TouchControlResp> {
//                    override fun onDataResponse(resultEntity: TouchControlResp) {
//                        if (resultEntity.status == BaseRspCmd.RESULT_OK) {
//                            addLog("${if (isTouch) "Touch" else "Gesture"} settings:")
//                            addLog("App Type: ${resultEntity.appType}")
//                            addLog("Strength: ${resultEntity.strength}")
//                        } else {
//                            addLog("ERROR: Failed to read ${if (isTouch) "touch" else "gesture"} settings")
//                        }
//                    }
//                }
//            )
//        } catch (e: Exception) {
//            addLog("ERROR: Exception reading ${if (isTouch) "touch" else "gesture"} settings: ${e.message}")
//        }
//    }
//
//    fun writeTouchGestureSettings(appType: Int, isTouch: Boolean, strength: Int) {
//        if (commandHandle == null) {
//            addLog("ERROR: CommandHandle not initialized")
//            return
//        }
//
//        addLog("Setting ${if (isTouch) "touch" else "gesture"} - App: $appType, Strength: $strength")
//        try {
//            commandHandle.executeReqCmdNoCallback(
//                TouchControlReq.getWriteInstance(appType, isTouch, strength)
//            )
//            addLog("${if (isTouch) "Touch" else "Gesture"} settings updated")
//        } catch (e: Exception) {
//            addLog("ERROR: Exception setting ${if (isTouch) "touch" else "gesture"}: ${e.message}")
//        }
//    }
//
//    // Exercise Functions
//    fun startExercise(sportType: Int) {
//        if (commandHandle == null) {
//            addLog("ERROR: CommandHandle not initialized")
//            return
//        }
//
//        addLog("Starting exercise - Type: $sportType")
//        try {
//            commandHandle.executeReqCmd(
//                PhoneSportReq.getSportStatus(1, sportType.toByte()), // 1 = start
//                object : ICommandResponse<AppSportRsp> {
//                    override fun onDataResponse(resultEntity: AppSportRsp?) {
//                        if (resultEntity != null) {
//                            when (resultEntity.gpsStatus) {
//                                6 -> addLog("Exercise started at timestamp: ${resultEntity.timeStamp}")
//                                2 -> addLog("Exercise paused")
//                                3 -> addLog("Exercise resumed")
//                                4 -> addLog("Exercise ended")
//                            }
//                        }
//                    }
//                }
//            )
//        } catch (e: Exception) {
//            addLog("ERROR: Exception starting exercise: ${e.message}")
//        }
//    }
//
//    fun pauseExercise(sportType: Int) {
//        if (commandHandle == null) return
//        try {
//            commandHandle.executeReqCmd(
//                PhoneSportReq.getSportStatus(2, sportType.toByte()), // 2 = pause
//                null
//            )
//            addLog("Exercise paused")
//        } catch (e: Exception) {
//            addLog("ERROR: Exception pausing exercise: ${e.message}")
//        }
//    }
//
//    fun resumeExercise(sportType: Int) {
//        if (commandHandle == null) return
//        try {
//            commandHandle.executeReqCmd(
//                PhoneSportReq.getSportStatus(3, sportType.toByte()), // 3 = resume
//                null
//            )
//            addLog("Exercise resumed")
//        } catch (e: Exception) {
//            addLog("ERROR: Exception resuming exercise: ${e.message}")
//        }
//    }
//
//    fun endExercise(sportType: Int) {
//        if (commandHandle == null) return
//        try {
//            commandHandle.executeReqCmd(
//                PhoneSportReq.getSportStatus(4, sportType.toByte()), // 4 = end
//                null
//            )
//            addLog("Exercise ended")
//        } catch (e: Exception) {
//            addLog("ERROR: Exception ending exercise: ${e.message}")
//        }
//    }
//
//    // OTA/DFU Functions
//    fun checkFirmwareFile(filePath: String): Boolean {
//        return try {
//            val dfuHandle = DfuHandle.getInstance()
//            val isValid = dfuHandle.checkFile(filePath)
//            addLog("Firmware file check - Valid: $isValid")
//            isValid
//        } catch (e: Exception) {
//            addLog("ERROR: Exception checking firmware file: ${e.message}")
//            false
//        }
//    }
//
//    fun startFirmwareUpdate(filePath: String) {
//        if (!checkFirmwareFile(filePath)) {
//            addLog("ERROR: Invalid firmware file")
//            return
//        }
//
//        addLog("Starting firmware update...")
//        try {
//            val dfuHandle = DfuHandle.getInstance()
//            dfuHandle.initCallback()
//
//            val dfuOpResult = object : DfuHandle.IOpResult {
//                override fun onActionResult(type: Int, errCode: Int) {
//                    if (errCode == DfuHandle.RSP_OK) {
//                        when (type) {
//                            1 -> {
//                                addLog("DFU: Initializing...")
//                                dfuHandle.init()
//                            }
//                            2 -> {
//                                addLog("DFU: Sending packet...")
//                                dfuHandle.sendPacket()
//                            }
//                            3 -> {
//                                addLog("DFU: Checking...")
//                                dfuHandle.check()
//                            }
//                            4 -> {
//                                addLog("DFU: Update successful! Device will restart...")
//                                dfuHandle.endAndRelease()
//                            }
//                        }
//                    } else {
//                        addLog("ERROR: DFU failed with error code: $errCode")
//                    }
//                }
//
//                override fun onProgress(percent: Int) {
//                    addLog("DFU Progress: $percent%")
//                }
//            }
//
//            dfuHandle.start(dfuOpResult)
//
//        } catch (e: Exception) {
//            addLog("ERROR: Exception starting firmware update: ${e.message}")
//        }
//    }
//
//    // Complete device setup function
//    fun performCompleteDeviceSetup() {
//        addLog("Performing complete device setup...")
//
//        readDeviceSupportedFunctions()
//        readHardwareInfo()
//        readFirmwareInfo()
//
//        addLog("Complete device setup initiated")
//    }
//}