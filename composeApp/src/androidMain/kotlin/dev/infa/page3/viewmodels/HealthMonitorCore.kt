package dev.infa.page3.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.oudmon.ble.base.communication.CommandHandle
import com.oudmon.ble.base.communication.Constants
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.req.BloodOxygenSettingReq
import com.oudmon.ble.base.communication.req.BpSettingReq
import com.oudmon.ble.base.communication.req.HeartRateSettingReq
import com.oudmon.ble.base.communication.req.HrvSettingReq
import com.oudmon.ble.base.communication.req.PressureSettingReq
import com.oudmon.ble.base.communication.req.SimpleKeyReq
import com.oudmon.ble.base.communication.req.SugarLipidsSettingReq
import com.oudmon.ble.base.communication.rsp.BaseRspCmd
import com.oudmon.ble.base.communication.rsp.BatteryRsp
import com.oudmon.ble.base.communication.rsp.BloodOxygenSettingRsp
import com.oudmon.ble.base.communication.rsp.BloodSugarLipidsSettingRsp
import com.oudmon.ble.base.communication.rsp.BpSettingRsp
import com.oudmon.ble.base.communication.rsp.HRVSettingRsp
import com.oudmon.ble.base.communication.rsp.HeartRateSettingRsp
import com.oudmon.ble.base.communication.rsp.PressureSettingRsp
import dev.infa.page3.models.DeviceCapabilities
import dev.infa.page3.models.HealthData
import dev.infa.page3.models.HealthSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HealthMonitorCore(
    private val commandHandle: CommandHandle?,
    private val addLog: (String) -> Unit
)
{
    companion object {
        const val TAG = "HealthMonitorCore"
    }

    // Health monitoring state
    var healthData by mutableStateOf(HealthData())

    var healthSettings by mutableStateOf(HealthSettings())
        private set

    var deviceCapabilities by mutableStateOf(DeviceCapabilities())

    var isReadingHealth by mutableStateOf(false)

    private val _batteryLevel = MutableStateFlow(0)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()
    
    init {
        addLog("HealthMonitorCore initialized with battery level: ${_batteryLevel.value}")
    }

    fun readBattery() {
        if (commandHandle == null) {
            addLog("ERROR: CommandHandle not initialized")
            return
        }
        addLog("Reading battery level...")
        try {
            commandHandle.executeReqCmd(
                SimpleKeyReq(Constants.CMD_GET_DEVICE_ELECTRICITY_VALUE),
                object : ICommandResponse<BatteryRsp> {
                    override fun onDataResponse(resultEntity: BatteryRsp) {
                        if (resultEntity.status == BaseRspCmd.RESULT_OK) {
                            _batteryLevel.value = resultEntity.batteryValue
                            addLog("SUCCESS: Battery level = ${resultEntity.batteryValue}%")
                            addLog("StateFlow value set to: ${_batteryLevel.value}")
                        } else {
                            addLog("ERROR: Failed to read battery level")
                        }
                    }
                }
            )
        } catch (e: Exception) {
            addLog("ERROR: Exception during battery read: ${e.message}")
        }
    }

    fun updateBatteryLevel(battery: Int) {
        _batteryLevel.value = battery
        addLog("Battery level updated via notification: $battery%")
        addLog("StateFlow value set to: ${_batteryLevel.value}")
    }
    
    fun testBatteryUpdate() {
        _batteryLevel.value = 75
        addLog("TEST: Manual battery update to 75%")
    }

    fun readHeartRateSettings() {
        if (commandHandle == null) {
            addLog("ERROR: CommandHandle not initialized")
            return
        }
        isReadingHealth = true
        addLog("Reading heart rate settings...")
        try {
            commandHandle.executeReqCmd(
                HeartRateSettingReq.getReadInstance(),
                object : ICommandResponse<HeartRateSettingRsp> {
                    override fun onDataResponse(resultEntity: HeartRateSettingRsp) {
                        isReadingHealth = false
                        if (resultEntity.status == BaseRspCmd.RESULT_OK) {
                            healthSettings = healthSettings.copy(
                                heartRateEnabled = resultEntity.isEnable,
                                heartRateInterval = resultEntity.heartInterval
                            )
                            addLog("Heart rate settings - Enabled: ${resultEntity.isEnable}, Interval: ${resultEntity.heartInterval}min")
                        } else {
                            addLog("ERROR: Failed to read heart rate settings")
                        }
                    }
                }
            )
        } catch (e: Exception) {
            isReadingHealth = false
            addLog("ERROR: Exception reading heart rate settings: ${e.message}")
        }
    }

    fun toggleHeartRate(enabled: Boolean, interval: Int) {
        if (commandHandle == null) {
            addLog("ERROR: CommandHandle not initialized")
            return
        }
        addLog("${if (enabled) "Enabling" else "Disabling"} continuous heart rate (interval: ${interval}min)...")
        try {
            commandHandle.executeReqCmd(
                HeartRateSettingReq.getWriteInstance(enabled, interval),
                object : ICommandResponse<HeartRateSettingRsp> {
                    override fun onDataResponse(resultEntity: HeartRateSettingRsp) {
                        if (resultEntity.status == BaseRspCmd.RESULT_OK) {
                            healthSettings = healthSettings.copy(
                                heartRateEnabled = enabled,
                                heartRateInterval = interval
                            )
                            addLog("SUCCESS: Heart rate monitoring ${if (enabled) "enabled" else "disabled"}")
                        } else {
                            addLog("ERROR: Failed to toggle heart rate monitoring")
                        }
                    }
                }
            )
        } catch (e: Exception) {
            addLog("ERROR: Exception toggling heart rate: ${e.message}")
        }
    }

    fun toggleBloodPressure(enabled: Boolean) {
        if (commandHandle == null) {
            addLog("ERROR: CommandHandle not initialized")
            return
        }
        addLog("${if (enabled) "Enabling" else "Disabling"} blood pressure monitoring...")
        try {
            val startEndTimeEntity = null
            val multiple = 60
            commandHandle.executeReqCmd(
                BpSettingReq.getWriteInstance(enabled, startEndTimeEntity, multiple),
                object : ICommandResponse<BpSettingRsp> {
                    override fun onDataResponse(resultEntity: BpSettingRsp) {
                        if (resultEntity.status == BaseRspCmd.RESULT_OK) {
                            healthSettings = healthSettings.copy(bloodPressureEnabled = enabled)
                            addLog("SUCCESS: Blood pressure monitoring ${if (enabled) "enabled" else "disabled"}")
                        } else {
                            addLog("ERROR: Failed to toggle blood pressure monitoring")
                        }
                    }
                }
            )
        } catch (e: Exception) {
            addLog("ERROR: Exception toggling blood pressure: ${e.message}")
        }
    }

    fun toggleBloodOxygen(enabled: Boolean) {
        if (commandHandle == null) {
            addLog("ERROR: CommandHandle not initialized")
            return
        }
        addLog("${if (enabled) "Enabling" else "Disabling"} blood oxygen monitoring...")
        try {
            commandHandle.executeReqCmd(
                BloodOxygenSettingReq.getWriteInstance(enabled),
                object : ICommandResponse<BloodOxygenSettingRsp> {
                    override fun onDataResponse(resultEntity: BloodOxygenSettingRsp) {
                        if (resultEntity.status == BaseRspCmd.RESULT_OK) {
                            healthSettings = healthSettings.copy(bloodOxygenEnabled = enabled)
                            addLog("SUCCESS: Blood oxygen monitoring ${if (enabled) "enabled" else "disabled"}")
                        } else {
                            addLog("ERROR: Failed to toggle blood oxygen monitoring")
                        }
                    }
                }
            )
        } catch (e: Exception) {
            addLog("ERROR: Exception toggling blood oxygen: ${e.message}")
        }
    }

    fun readBloodOxygenSettings() {
        if (commandHandle == null) {
            addLog("ERROR: CommandHandle not initialized")
            return
        }
        isReadingHealth = true
        addLog("Reading blood oxygen settings...")
        try {
            commandHandle.executeReqCmd(
                BloodOxygenSettingReq.getReadInstance(),
                object : ICommandResponse<BloodOxygenSettingRsp> {
                    override fun onDataResponse(resultEntity: BloodOxygenSettingRsp) {
                        isReadingHealth = false
                        if (resultEntity.status == BaseRspCmd.RESULT_OK) {
                            healthSettings = healthSettings.copy(
                                bloodOxygenEnabled = resultEntity.isEnable
                            )
                            addLog("Blood oxygen settings - Enabled: ${resultEntity.isEnable}")
                        } else {
                            addLog("ERROR: Failed to read blood oxygen settings")
                        }
                    }
                }
            )
        } catch (e: Exception) {
            isReadingHealth = false
            addLog("ERROR: Exception reading blood oxygen settings: ${e.message}")
        }
    }

    fun toggleHrv(enabled: Boolean) {
        if (commandHandle == null) {
            addLog("ERROR: CommandHandle not initialized")
            return
        }
        addLog("${if (enabled) "Enabling" else "Disabling"} HRV monitoring...")
        try {
            commandHandle.executeReqCmd(
                HrvSettingReq(enabled),
                object : ICommandResponse<HRVSettingRsp> {
                    override fun onDataResponse(resultEntity: HRVSettingRsp) {
                        if (resultEntity.status == BaseRspCmd.RESULT_OK) {
                            healthSettings = healthSettings.copy(hrvEnabled = enabled)
                            addLog("SUCCESS: HRV monitoring ${if (enabled) "enabled" else "disabled"}")
                        } else {
                            addLog("ERROR: Failed to toggle HRV monitoring")
                        }
                    }
                }
            )
        } catch (e: Exception) {
            addLog("ERROR: Exception toggling HRV: ${e.message}")
        }
    }

    fun readPressureSettings() {
        if (commandHandle == null) {
            addLog("ERROR: CommandHandle not initialized")
            return
        }
        isReadingHealth = true
        addLog("Reading pressure settings...")
        try {
            commandHandle.executeReqCmd(
                PressureSettingReq.getReadInstance(),
                object : ICommandResponse<PressureSettingRsp> {
                    override fun onDataResponse(resultEntity: PressureSettingRsp) {
                        isReadingHealth = false
                        if (resultEntity.status == BaseRspCmd.RESULT_OK) {
                            healthSettings = healthSettings.copy(pressureEnabled = resultEntity.isEnable)
                            addLog("Pressure settings - Enabled: ${resultEntity.isEnable}")
                        } else {
                            addLog("ERROR: Failed to read pressure settings")
                        }
                    }
                }
            )
        } catch (e: Exception) {
            isReadingHealth = false
            addLog("ERROR: Exception reading pressure settings: ${e.message}")
        }
    }

    fun togglePressure(enabled: Boolean) {
        if (commandHandle == null) {
            addLog("ERROR: CommandHandle not initialized")
            return
        }
        addLog("${if (enabled) "Enabling" else "Disabling"} pressure monitoring...")
        try {
            commandHandle.executeReqCmd(
                PressureSettingReq.getWriteInstance(enabled),
                object : ICommandResponse<PressureSettingRsp> {
                    override fun onDataResponse(resultEntity: PressureSettingRsp) {
                        if (resultEntity.status == BaseRspCmd.RESULT_OK) {
                            healthSettings = healthSettings.copy(pressureEnabled = enabled)
                            addLog("SUCCESS: Pressure monitoring ${if (enabled) "enabled" else "disabled"}")
                        } else {
                            addLog("ERROR: Failed to toggle pressure monitoring")
                        }
                    }
                }
            )
        } catch (e: Exception) {
            addLog("ERROR: Exception toggling pressure: ${e.message}")
        }
    }

    fun toggleTemperatureSettings(enabled: Boolean) {
        if (!deviceCapabilities.supportTemperature) {
            addLog("ERROR: Device does not support temperature monitoring")
            return
        }

        if (commandHandle == null) {
            addLog("ERROR: CommandHandle not initialized")
            return
        }

        addLog("${if (enabled) "Enabling" else "Disabling"} body temperature monitoring...")
        try {
            commandHandle.executeReqCmd(
                SugarLipidsSettingReq.getWriteInstance(0x03, enabled, 0),
                object : ICommandResponse<BloodSugarLipidsSettingRsp> {
                    override fun onDataResponse(resultEntity: BloodSugarLipidsSettingRsp) {
                        if (resultEntity.status == BaseRspCmd.RESULT_OK) {
                            healthSettings = healthSettings.copy(temperatureEnabled = enabled)
                            addLog("SUCCESS: Temperature monitoring ${if (enabled) "enabled" else "disabled"}")
                        } else {
                            addLog("ERROR: Failed to toggle temperature monitoring")
                        }
                    }
                }
            )
        } catch (e: Exception) {
            addLog("ERROR: Exception toggling temperature: ${e.message}")
        }
    }

    fun resetHealthData() {
        healthData = HealthData()
        healthSettings = HealthSettings()
        deviceCapabilities = DeviceCapabilities()
        _batteryLevel.value = 0
        isReadingHealth = false
        addLog("Health data reset")
    }
}