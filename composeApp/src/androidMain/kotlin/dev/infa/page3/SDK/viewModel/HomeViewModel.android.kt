package dev.infa.page3.SDK.viewModel

import dev.infa.page3.SDK.data.DeviceCapabilities
import dev.infa.page3.SDK.data.ExerciseData

// ============================================
// androidMain/platform/HomeManager.android.kt
// ============================================

import android.util.Log
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.communication.CommandHandle
import com.oudmon.ble.base.communication.Constants
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.req.*
import com.oudmon.ble.base.communication.responseImpl.DeviceSportNotifyListener
import com.oudmon.ble.base.communication.rsp.*
import dev.infa.page3.SDK.data.ExerciseSummary
import kotlinx.coroutines.*
import kotlinx.coroutines.invoke
import java.text.SimpleDateFormat
import java.util.*
import kotlin.compareTo
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.invoke
import kotlin.text.get
import kotlin.text.toByte
import kotlin.text.toLong

actual class HomeManager {
    private val commandHandle = CommandHandle.getInstance()

    // Exercise tracking state
    private var isExercisingFlag = false
    private var isPausedFlag = false
    private var currentSportType: Int? = null
    private var elapsedSeconds = 0
    private var heartRate = 0
    private var steps = 0
    private var distanceMeters = 0
    private var calories = 0
    private var lastStartTimestamp: Long = 0

    private var tickJob: Job? = null
    private var onExerciseUpdate: ((ExerciseData) -> Unit)? = null
    private var onExerciseEnded: ((ExerciseSummary) -> Unit)? = null
    private var onError: ((String) -> Unit)? = null

    // Device response handlers
    private val gpsResponse = ICommandResponse<AppSportRsp> { result ->
        result ?: return@ICommandResponse
        when (result.gpsStatus) {
            6 -> {
                lastStartTimestamp = result.timeStamp.toLong()
                Log.d("HomeManager", "Start timestamp: $lastStartTimestamp")
            }
            2 -> {
                isPausedFlag = true
                notifyUpdate()
                Log.d("HomeManager", "Exercise paused")
            }
            3 -> {
                isPausedFlag = false
                notifyUpdate()
                Log.d("HomeManager", "Exercise resumed")
            }
            4 -> {
                handleExerciseEnded()
                Log.d("HomeManager", "Exercise ended")
            }
        }
    }

    private val sportNotifyListener = object : DeviceSportNotifyListener() {
        override fun onDataResponse(resultEntity: DeviceNotifyRsp?) {
            super.onDataResponse(resultEntity)
            val rsp = resultEntity ?: return
            if (rsp.status != BaseRspCmd.RESULT_OK) return

            val bytes = rsp.loadData
            if (bytes == null || bytes.isEmpty()) return

            // Parse device data
            val sportType = bytes2Int(byteArrayOf(bytes[0]))
            val status = bytes2Int(byteArrayOf(bytes[1]))
            val duration = bytes2Int(byteArrayOf(bytes[2], bytes[3]))
            val heart = bytes2Int(byteArrayOf(bytes[4]))
            val stepCount = bytes2Int(byteArrayOf(bytes[5], bytes[6], bytes[7]))
            val distance = bytes2Int(byteArrayOf(bytes[8], bytes[9], bytes[10]))
            val calorie = bytes2Int(byteArrayOf(bytes[11], bytes[12], bytes[13]))

            if (status == 0x03) {
                onError?.invoke("Device not worn properly")
                return
            }

            currentSportType = sportType
            elapsedSeconds = duration
            heartRate = heart
            steps = stepCount
            distanceMeters = distance
            calories = calorie

            notifyUpdate()
        }
    }

    actual suspend fun getBatteryLevel(): Int? =
        suspendCancellableCoroutine { continuation ->
        var resumed = false
        commandHandle.executeReqCmd(
            SimpleKeyReq(Constants.CMD_GET_DEVICE_ELECTRICITY_VALUE),
            object : ICommandResponse<BatteryRsp> {
                override fun onDataResponse(resultEntity: BatteryRsp) {
                    if (resumed || !continuation.isActive) return

                    resumed = true

                    if (resultEntity.status == BaseRspCmd.RESULT_OK) {
                        continuation.resume(resultEntity.batteryValue)
                    } else {
                        continuation.resume(null)
                    }
                }
            }
        )

            continuation.invokeOnCancellation {
                resumed = true
            }
        }

    actual suspend fun fetchDeviceCapabilities(): DeviceCapabilities? =
        suspendCancellableCoroutine { continuation ->

            var timeRsp: SetTimeRsp? = null
            var funcRsp: DeviceSupportFunctionRsp? = null
            var resumed = false

            fun tryResume() {
                if (resumed || timeRsp == null || funcRsp == null) return
                resumed = true

                val t = timeRsp!!
                val f = funcRsp!!

                continuation.resume(
                    DeviceCapabilities(

                        // Health
                        hasHeartRate = true, // baseline supported
                        hasSpO2 = t.mSupportBloodOxygen,
                        hasHRV = t.mSupportHrv,
                        hasBloodPressure = t.mSupportBloodPressure,
                        hasBodyTemperature = t.mSupportTemperature,
                        hasFatigue = t.mSupportFeature,
                        hasOneKeyCheck = t.mSupportOneKeyCheck,

                        // Activity & Sleep
                        hasSleepTracking = t.mNewSleepProtocol,
                        hasExerciseMode = true,

                        // Watch & System
                        supportsWatchFace = t.mSupportPlate,
                        supportsCustomWatchFace = t.mSupportCustomWallpaper,
                        maxWatchFaces = t.mMaxWatchFace,
                        supportsWeather = t.mSupportWeather,
                        supportsMenstruation = t.mSupportMenstruation,

                        // Functional
                        supportsTouch = f.supportTouch,
                        supportsGesture = f.supportGesture,
                        supportsBlePairing = f.supportBlePair,
                        supportsHeartRateCalibration = f.supportAPPRevision,

                        // Ring / App
                        supportsMusic = f.supportRingMusic,
                        supportsVideo = f.supportRingVideo,
                        supportsEbook = f.supportRingEbook,
                        supportsCamera = f.supportRingCamera,
                        supportsPhoneCall = f.supportRingPhoneCall,
                        supportsGame = f.supportRingGame,
                        supportsMuslimMode = f.supportMoslin
                    )
                )
            }

            // 1️⃣ Set Time + Health Capabilities
            CommandHandle.getInstance().executeReqCmd(
                SetTimeReq(0),
                object : ICommandResponse<SetTimeRsp> {
                    override fun onDataResponse(rsp: SetTimeRsp) {
                        if (rsp.status != BaseRspCmd.RESULT_OK || continuation.isCompleted) {
                            if (!resumed) continuation.resume(null)
                            return
                        }
                        timeRsp = rsp
                        tryResume()
                    }
                }
            )

            // 2️⃣ Functional Capabilities
            CommandHandle.getInstance().executeReqCmd(
                DeviceSupportReq.getReadInstance(),
                object : ICommandResponse<DeviceSupportFunctionRsp> {
                    override fun onDataResponse(rsp: DeviceSupportFunctionRsp) {
                        if (continuation.isCompleted) return
                        funcRsp = rsp
                        tryResume()
                    }
                }
            )
        }



    actual suspend fun setSportsGoals(
        stepGoal: Int,
        calorieGoal: Int,
        distanceGoal: Int,
        sportMinuteGoal: Int,
        sleepMinuteGoal: Int
    ): Boolean = suspendCoroutine { continuation ->
        commandHandle.executeReqCmd(
            TargetSettingReq.getWriteInstance(
                stepGoal,
                calorieGoal * 1000,
                distanceGoal,
                sportMinuteGoal,
                sleepMinuteGoal
            ),
            object : ICommandResponse<BaseRspCmd> {
                override fun onDataResponse(resultEntity: BaseRspCmd) {
                    continuation.resume(resultEntity.status == BaseRspCmd.RESULT_OK)
                }
            }
        )
    }

    actual fun startExercise(
        sportType: Int,
        onUpdate: (ExerciseData) -> Unit,
        onEnd: (ExerciseSummary) -> Unit,
        onError: (String) -> Unit
    ) {
        if (isExercisingFlag) {
            onError("Exercise already in progress")
            return
        }

        this.onExerciseUpdate = onUpdate
        this.onExerciseEnded = onEnd
        this.onError = onError

        resetState()
        currentSportType = sportType
        isExercisingFlag = true
        isPausedFlag = false

        BleOperateManager.getInstance().addSportDeviceListener(0x78, sportNotifyListener)

        commandHandle.executeReqCmd(
            PhoneSportReq.getSportStatus(1, sportType.toByte()),
            gpsResponse
        )

        startTimer()
    }

    actual fun pauseExercise() {
        if (!isExercisingFlag || isPausedFlag) return
        val sportType = currentSportType ?: return

        isPausedFlag = true
        notifyUpdate()

        commandHandle.executeReqCmd(
            PhoneSportReq.getSportStatus(2, sportType.toByte()),
            gpsResponse
        )
    }

    actual fun resumeExercise() {
        if (!isExercisingFlag || !isPausedFlag) return
        val sportType = currentSportType ?: return

        isPausedFlag = false
        notifyUpdate()

        commandHandle.executeReqCmd(
            PhoneSportReq.getSportStatus(3, sportType.toByte()),
            gpsResponse
        )
    }

    actual fun endExercise() {
        if (!isExercisingFlag) return
        val sportType = currentSportType ?: return

        commandHandle.executeReqCmd(
            PhoneSportReq.getSportStatus(4, sportType.toByte()),
            gpsResponse
        )

        handleExerciseEnded()
    }

    actual fun isExercising(): Boolean = isExercisingFlag

    actual fun isPaused(): Boolean = isPausedFlag

    actual fun cleanup() {
        BleOperateManager.getInstance().removeSportDeviceListener(0x78)
        stopTimer()
        resetState()
        onExerciseUpdate = null
        onExerciseEnded = null
        onError = null
    }

    // Private helpers
    private fun startTimer() {
        tickJob?.cancel()
        tickJob = CoroutineScope(Dispatchers.Main).launch {
            while (isExercisingFlag) {
                delay(1000)
                if (!isPausedFlag) {
                    elapsedSeconds++
                    notifyUpdate()
                }
            }
        }
    }

    private fun stopTimer() {
        tickJob?.cancel()
        tickJob = null
    }

    private fun notifyUpdate() {
        onExerciseUpdate?.invoke(getCurrentExerciseData())
    }

    private fun getCurrentExerciseData(): ExerciseData {
        return ExerciseData(
            sportType = currentSportType ?: 0,
            isActive = isExercisingFlag,
            isPaused = isPausedFlag,
            elapsedSeconds = elapsedSeconds,
            heartRate = heartRate,
            steps = steps,
            distanceMeters = distanceMeters,
            calories = calories
        )
    }

    private fun handleExerciseEnded() {
        val startTime = if (lastStartTimestamp > 0) {
            lastStartTimestamp
        } else {
            System.currentTimeMillis() / 1000
        }

        val summary = ExerciseSummary(
            sportType = currentSportType ?: 0,
            sportName = ExerciseUtils.getSportName(currentSportType ?: 0),
            startTimestamp = startTime,
            durationSeconds = elapsedSeconds,
            distanceMeters = distanceMeters,
            calories = calories,
            averageHeartRate = heartRate,
            steps = steps,
            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Date(startTime * 1000))
        )

        onExerciseEnded?.invoke(summary)
        internalCleanup()
    }

    private fun internalCleanup() {
        BleOperateManager.getInstance().removeSportDeviceListener(0x78)
        stopTimer()
        resetState()
    }

    private fun resetState() {
        isExercisingFlag = false
        isPausedFlag = false
        currentSportType = null
        elapsedSeconds = 0
        heartRate = 0
        steps = 0
        distanceMeters = 0
        calories = 0
        lastStartTimestamp = 0
    }

    private fun bytes2Int(data: ByteArray): Int {
        var res = 0
        for (i in data.indices) {
            res = res or ((data[i].toInt() and 0xFF) shl (8 * (data.size - 1 - i)))
        }
        return res
    }
}
