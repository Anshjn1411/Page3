package dev.infa.page3.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oudmon.ble.base.bluetooth.DeviceManager
import com.oudmon.ble.base.communication.CommandHandle
import com.oudmon.ble.base.communication.Constants
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.req.DeviceSupportReq
import com.oudmon.ble.base.communication.req.SetTimeReq
import com.oudmon.ble.base.communication.req.SimpleKeyReq
import com.oudmon.ble.base.communication.req.TargetSettingReq
import com.oudmon.ble.base.communication.rsp.*
import dev.infa.page3.models.DeviceCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume

class HomeViewModel : ViewModel() {
    private val commandHandle = CommandHandle.getInstance()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _batterValue = MutableStateFlow<Int?>(0)
    val batteryValue: StateFlow<Int?> = _batterValue.asStateFlow()


    private val _todaySleep = MutableStateFlow<SleepData?>(null)
    val todaySleep: StateFlow<SleepData?> = _todaySleep.asStateFlow()

    private val _latestHeart = MutableStateFlow<HeartRateData?>(null)
    val latestHeart: StateFlow<HeartRateData?> = _latestHeart.asStateFlow()

    private val _deviceCapabilities = MutableStateFlow<DeviceCapabilities?>(null)
    val deviceCapabilities: StateFlow<DeviceCapabilities?> = _deviceCapabilities.asStateFlow()
    private val _stepGoal = MutableStateFlow(10000) // Default 10k steps
    val stepGoal: StateFlow<Int> = _stepGoal.asStateFlow()

    private val _calorieGoal = MutableStateFlow(500) // Default 500 kcal
    val calorieGoal: StateFlow<Int> = _calorieGoal.asStateFlow()

    private val _distanceGoal = MutableStateFlow(8000) // Default 8km in meters
    val distanceGoal: StateFlow<Int> = _distanceGoal.asStateFlow()

    private val _goalSetSuccess = MutableStateFlow<Boolean?>(null)
    val goalSetSuccess: StateFlow<Boolean?> = _goalSetSuccess.asStateFlow()

    init {
        createHardcodedSleepData()
    }

    fun fetchDeviceCapabilities() {
        viewModelScope.launch {
            try {
                commandHandle.executeReqCmd(
                    SetTimeReq(),
                    object : ICommandResponse<SetTimeRsp> {
                        override fun onDataResponse(resultEntity: SetTimeRsp?) {
                            resultEntity?.let {
                                _deviceCapabilities.value = DeviceCapabilities(
                                    supportTemperature = it.mSupportTemperature,
                                    supportPlate = it.mSupportPlate,
                                    supportMenstruation = it.mSupportMenstruation,
                                    supportCustomWallpaper = it.mSupportCustomWallpaper,
                                    supportBloodOxygen = it.mSupportBloodOxygen,
                                    supportBloodPressure = it.mSupportBloodPressure,
                                    supportFeature = it.mSupportFeature,
                                    supportOneKeyCheck = it.mSupportOneKeyCheck,
                                    supportWeather = it.mSupportWeather,
                                    newSleepProtocol = it.mNewSleepProtocol,
                                    maxWatchFace = it.mMaxWatchFace,
                                    supportHrv = it.mSupportHrv
                                )
                            }
                        }
                    }
                )

                commandHandle.executeReqCmd(
                    DeviceSupportReq.getReadInstance(),
                    object : ICommandResponse<DeviceSupportFunctionRsp> {
                        override fun onDataResponse(resultEntity: DeviceSupportFunctionRsp?) {
                            resultEntity?.let {
                                val caps = _deviceCapabilities.value ?: DeviceCapabilities()
                                _deviceCapabilities.value = caps.copy(
                                    supportTouch = it.supportTouch,
                                    supportMoslin = it.supportMoslin,
                                    supportAPPRevision = it.supportAPPRevision,
                                    supportBlePair = it.supportBlePair,
                                    supportGesture = it.supportGesture,
                                    supportRingMusic = it.supportRingMusic,
                                    supportRingVideo = it.supportRingVideo,
                                    supportRingEbook = it.supportRingEbook,
                                    supportRingCamera = it.supportRingCamera,
                                    supportRingPhoneCall = it.supportRingPhoneCall,
                                    supportRingGame = it.supportRingGame
                                )
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = "Failed to fetch capabilities"
            }
        }
    }

    private fun createHardcodedSleepData() {
        val sleepData = SleepData(
            date = getCurrentDate(),
            totalDuration = 420.0,
            awakeDuration = 30,
            sleepScore = 85,
            sleepEfficiency = 90.0,
            deepSleep = 0,
            lightSleep = 0,
            remSleep = 0,
            sleepStartTime = "",
            sleepEndTime = "",
            stages = emptyList()
        )
        _todaySleep.value = sleepData
    }


    fun getBatteryLevel() {
        viewModelScope.launch {
            commandHandle.executeReqCmd(
                SimpleKeyReq(Constants.CMD_GET_DEVICE_ELECTRICITY_VALUE),
                object : ICommandResponse<BatteryRsp> {
                    override fun onDataResponse(resultEntity: BatteryRsp) {
                        if (resultEntity.status == BaseRspCmd.RESULT_OK) {
                            _batterValue.value = resultEntity.batteryValue
                        }
                    }
                }
            )
        }
    }


    fun setSportsGoals(
        stepGoal: Int = 10000,
        calorieGoal: Int = 500, // in kcal
        distanceGoal: Int = 8000, // in meters
        sportMinuteGoal: Int = 30, // in minutes
        sleepMinuteGoal: Int = 480 // 8 hours in minutes
    ) {

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _goalSetSuccess.value = null

                withContext(Dispatchers.IO) {
                    commandHandle.executeReqCmd(
                        TargetSettingReq.getWriteInstance(
                            stepGoal,
                            calorieGoal * 1000, // Convert kcal to calories
                            distanceGoal,
                            sportMinuteGoal,
                            sleepMinuteGoal
                        ),
                        object : ICommandResponse<BaseRspCmd> {
                            override fun onDataResponse(resultEntity: BaseRspCmd) {
                                if (resultEntity.status == BaseRspCmd.RESULT_OK) {

                                    _stepGoal.value = stepGoal
                                    _calorieGoal.value = calorieGoal
                                    _distanceGoal.value = distanceGoal
                                    _goalSetSuccess.value = true
                                } else {

                                    _errorMessage.value = "Failed to set goals"
                                    _goalSetSuccess.value = false
                                }
                            }
                        }
                    )
                }


            } catch (e: Exception) {

                _errorMessage.value = "Error setting goals: ${e.message}"
                _goalSetSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update individual goal values (local state only, call setSportsGoals to sync to device)
     */
    fun updateStepGoal(newGoal: Int) {
        _stepGoal.value = newGoal
    }

    fun updateCalorieGoal(newGoal: Int) {
        _calorieGoal.value = newGoal
    }

    fun updateDistanceGoal(newGoal: Int) {
        _distanceGoal.value = newGoal
    }

    fun clearGoalSetStatus() {
        _goalSetSuccess.value = null
    }

    private fun getCurrentDate(): String {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return format.format(Date())
    }
}