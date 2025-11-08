package dev.infa.page3.viewmodels

import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.communication.CommandHandle
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.rsp.StopHeartRateRsp
import com.oudmon.ble.base.util.CalcBloodPressureByHeart
import dev.infa.page3.models.DeviceCapabilities
import dev.infa.page3.models.HealthData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HealthMeasurements(
    private val commandHandle: CommandHandle?,
    private val coroutineScope: CoroutineScope,
    private val addLog: (String) -> Unit,
    private val healthDataUpdater: (HealthData) -> Unit,
    private val deviceCapabilities: () -> DeviceCapabilities,
    private val setReadingStatus: (Boolean) -> Unit
)
{
    companion object {
        const val TAG = "HealthMeasurements"
    }

    // Manual Single Measurements
    fun measureHeartOnce() {
        try {
            BleOperateManager.getInstance().manualModeHeart({ result ->
                coroutineScope.launch {
                    healthDataUpdater(HealthData().copy(heartRate = result.value))
                }
                addLog("Heart rate (once): ${result.value} BPM")
            }, false)
        } catch (e: Exception) {
            addLog("ERROR: Heart measurement failed: ${e.message}")
        }
    }

    fun measureBloodPressureOnce() {
        try {
            BleOperateManager.getInstance().manualModeBP({ result ->
                val systolic = result.sbp
                val diastolic = result.dbp

                coroutineScope.launch {
                    healthDataUpdater(
                        HealthData().copy(
                        systolic = systolic,
                        diastolic = diastolic
                    ))
                }
                addLog("Blood Pressure (once): ${systolic}/${diastolic} mmHg")
            }, false)
        } catch (e: Exception) {
            addLog("ERROR: Blood pressure measurement failed: ${e.message}")
        }
    }

    fun measureBloodOxygenOnce() {
        try {
            BleOperateManager.getInstance().manualModeSpO2({ result ->
                coroutineScope.launch {
                    healthDataUpdater(HealthData().copy(spo2 = result.value))
                }
                addLog("Blood oxygen (once): ${result.value}%")
            }, false)
        } catch (e: Exception) {
            addLog("ERROR: Blood oxygen measurement failed: ${e.message}")
        }
    }

    fun measureHrvOnce() {
        try {
            BleOperateManager.getInstance().manualModeHrv({ result ->
                val hrvValue = result.value

                coroutineScope.launch {
                    healthDataUpdater(HealthData().copy(hrvValue = hrvValue))
                }
                addLog("HRV (once): $hrvValue ms")
            }, false)
        } catch (e: Exception) {
            addLog("ERROR: HRV measurement failed: ${e.message}")
        }
    }

    fun measurePressureOnce() {
        try {
            BleOperateManager.getInstance().manualModePressure({ result ->
                val pressureValue = result.value
                coroutineScope.launch {
                    healthDataUpdater(HealthData().copy(pressure = pressureValue))
                }
                addLog("Pressure/Stress (once): $pressureValue")
            }, false)
        } catch (e: Exception) {
            addLog("ERROR: Pressure measurement failed: ${e.message}")
        }
    }

    fun measureTemperatureOnce() {
        if (!deviceCapabilities().supportTemperature) {
            addLog("ERROR: Device does not support temperature monitoring")
            return
        }

        try {
            BleOperateManager.getInstance().manualTemperature({ result ->
                // Temperature value should be divided by 10 to get normal temperature
                val temperatureValue = result.value / 10.0f
                coroutineScope.launch {
                    healthDataUpdater(HealthData().copy(temperature = temperatureValue))
                }
                addLog("Body temperature (once): ${temperatureValue}°C")
            }, false)
        } catch (e: Exception) {
            addLog("ERROR: Temperature measurement failed: ${e.message}")
        }
    }

    // Enhanced One-Key Detection
    fun performOneKeyMeasurement() {
        if (!deviceCapabilities().supportOneKeyCheck) {
            addLog("ERROR: Device does not support one-key measurement")
            return
        }

        addLog("Starting one-key comprehensive measurement...")
        setReadingStatus(true)

        try {
            BleOperateManager.getInstance().oneClickMeasurement({ result ->
                setReadingStatus(false)
                coroutineScope.launch {
                    healthDataUpdater(
                        HealthData().copy(
                        heartRate = result.heartRate,
                        spo2 = result.bloodOxygen,
                        systolic = result.sbp,
                        diastolic = result.dbp,
                        hrvValue = result.hrv,
                        pressure = result.stress,
                        temperature = result.temperature / 10.0f // Divide by 10 as per docs
                    ))
                }
                addLog("One-key measurement complete:")
                addLog("Heart Rate: ${result.heartRate} BPM")
                addLog("Blood Oxygen: ${result.bloodOxygen}%")
                addLog("Blood Pressure: ${result.sbp}/${result.dbp} mmHg")
                addLog("HRV: ${result.hrv} ms")
                addLog("Stress: ${result.stress}")
                addLog("Temperature: ${result.temperature / 10.0f}°C")
            }, false)
        } catch (e: Exception) {
            setReadingStatus(false)
            addLog("ERROR: One-key measurement failed: ${e.message}")
        }
    }

    // Raw Data Measurement Functions
    fun measureHeartRateRawData(seconds: Int) {
        addLog("Starting heart rate raw data measurement for $seconds seconds...")
        try {
            BleOperateManager.getInstance().manualModeHeartRateRawData(
                object : ICommandResponse<StopHeartRateRsp> {
                    override fun onDataResponse(resultEntity: StopHeartRateRsp) {
                        addLog("Heart Rate Raw Data - HR: ${resultEntity.heartRate}, PPG Count: ${resultEntity.ppgCount}")
                        addLog("Green Light PPG: ${resultEntity.greenLightPpgL}/${resultEntity.greenLightPpgH}")
                        addLog("Red Light PPG: ${resultEntity.redLightPpgL}/${resultEntity.redLightPpgH}")
                        addLog("Infrared PPG: ${resultEntity.infraredPpgL}/${resultEntity.infraredPpgH}")
                        addLog("Accelerometer - X: ${resultEntity.xl}/${resultEntity.xh}")
                        addLog("Accelerometer - Y: ${resultEntity.yl}/${resultEntity.yh}")
                        addLog("Accelerometer - Z: ${resultEntity.zl}/${resultEntity.zh}")
                        addLog("RRI: ${resultEntity.rri}")
                    }
                },
                seconds,
                false
            )
        } catch (e: Exception) {
            addLog("ERROR: Heart rate raw data measurement failed: ${e.message}")
        }
    }

    fun measureBloodOxygenRawData(seconds: Int) {
        addLog("Starting blood oxygen raw data measurement for $seconds seconds...")
        try {
            BleOperateManager.getInstance().manualModeBloodOxygenRawData(
                object : ICommandResponse<StopHeartRateRsp> {
                    override fun onDataResponse(resultEntity: StopHeartRateRsp) {
                        addLog("Blood Oxygen Raw Data - SpO2: ${resultEntity.bloodOxygen}")
                        addLog("PPG Data - Green: ${resultEntity.greenLightPpgL}/${resultEntity.greenLightPpgH}")
                        addLog("PPG Data - Red: ${resultEntity.redLightPpgL}/${resultEntity.redLightPpgH}")
                        addLog("PPG Data - Infrared: ${resultEntity.infraredPpgL}/${resultEntity.infraredPpgH}")
                        addLog("PPG Count: ${resultEntity.ppgCount}")
                    }
                },
                seconds,
                false
            )
        } catch (e: Exception) {
            addLog("ERROR: Blood oxygen raw data measurement failed: ${e.message}")
        }
    }

    // Blood Pressure Calculation Functions
    fun calculateBloodPressureFromHR(heartRate: Int, age: Int) {
        try {
            val sbp = CalcBloodPressureByHeart.cal_sbp(heartRate, age)
            val dbp = CalcBloodPressureByHeart.cal_dbp(sbp)

            addLog("Calculated BP from HR($heartRate) and age($age): $sbp/$dbp mmHg")

            coroutineScope.launch {
                healthDataUpdater(
                    HealthData().copy(
                    systolic = sbp,
                    diastolic = dbp
                ))
            }
        } catch (e: Exception) {
            addLog("ERROR: Exception calculating blood pressure: ${e.message}")
        }
    }

    // Stop measurement functions
    fun stopHeartRateMeasurement() {
        try {
            BleOperateManager.getInstance().manualModeHeart(null, true)
            addLog("Heart rate measurement stopped")
        } catch (e: Exception) {
            addLog("ERROR: Exception stopping heart rate measurement: ${e.message}")
        }
    }

    fun stopBloodPressureMeasurement() {
        try {
            BleOperateManager.getInstance().manualModeBP(null, true)
            addLog("Blood pressure measurement stopped")
        } catch (e: Exception) {
            addLog("ERROR: Exception stopping blood pressure measurement: ${e.message}")
        }
    }

    fun stopBloodOxygenMeasurement() {
        try {
            BleOperateManager.getInstance().manualModeSpO2(null, true)
            addLog("Blood oxygen measurement stopped")
        } catch (e: Exception) {
            addLog("ERROR: Exception stopping blood oxygen measurement: ${e.message}")
        }
    }

    fun stopHrvMeasurement() {
        try {
            BleOperateManager.getInstance().manualModeHrv(null, true)
            addLog("HRV measurement stopped")
        } catch (e: Exception) {
            addLog("ERROR: Exception stopping HRV measurement: ${e.message}")
        }
    }

    fun stopPressureMeasurement() {
        try {
            BleOperateManager.getInstance().manualModePressure(null, true)
            addLog("Pressure measurement stopped")
        } catch (e: Exception) {
            addLog("ERROR: Exception stopping pressure measurement: ${e.message}")
        }
    }

    fun stopTemperatureMeasurement() {
        try {
            BleOperateManager.getInstance().manualTemperature(null, true)
            addLog("Temperature measurement stopped")
        } catch (e: Exception) {
            addLog("ERROR: Exception stopping temperature measurement: ${e.message}")
        }
    }

    fun stopOneKeyMeasurement() {
        try {
            BleOperateManager.getInstance().oneClickMeasurement(null, true)
            addLog("One-key measurement stopped")
        } catch (e: Exception) {
            addLog("ERROR: Exception stopping one-key measurement: ${e.message}")
        }
    }

    // Batch measurement functions
    fun performAllIndividualMeasurements() {
        addLog("Starting all individual measurements...")

        measureHeartOnce()

        // Add small delays between measurements to avoid conflicts
        coroutineScope.launch {
            delay(2000)
            measureBloodPressureOnce()

            delay(3000)
            measureBloodOxygenOnce()

            delay(2000)
            measureHrvOnce()

            delay(2000)
            measurePressureOnce()

            if (deviceCapabilities().supportTemperature) {
                delay(2000)
                measureTemperatureOnce()
            }

            addLog("All individual measurements completed")
        }
    }


}