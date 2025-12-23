package dev.infa.page3.SDK.platform

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.communication.CommandHandle
import com.oudmon.ble.base.communication.Constants
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.LargeDataHandler
import com.oudmon.ble.base.communication.bigData.BloodOxygenEntity
import com.oudmon.ble.base.communication.dfu_temperature.TemperatureEntity
import com.oudmon.ble.base.communication.entity.StartEndTimeEntity
import com.oudmon.ble.base.communication.file.FileHandle
import com.oudmon.ble.base.communication.file.SimpleCallback
import com.oudmon.ble.base.communication.req.*
import com.oudmon.ble.base.communication.rsp.*
import com.oudmon.ble.base.util.CalcBloodPressureByHeart
import com.russhwolf.settings.set
import dev.infa.page3.SDK.data.*
import dev.infa.page3.SDK.viewModel.IContinuousMonitoring
import dev.infa.page3.SDK.viewModel.IInstantMeasures
import dev.infa.page3.SDK.viewModel.ISyncManager
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.compareTo
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.text.toInt
import kotlin.text.toLong

actual class SyncManager : ISyncManager {

    private val cmdHandle = CommandHandle.getInstance()
    private val bleOps = BleOperateManager.getInstance()
    private val largeData = LargeDataHandler.getInstance()
    private val fileHandle = FileHandle.getInstance()
    private val mutex = Mutex()

    actual override suspend fun syncTodaySteps(): Int = mutex.withLock {
        suspendCoroutine { cont ->
            cmdHandle.executeReqCmd(
                SimpleKeyReq(Constants.CMD_GET_STEP_TODAY),
                object : ICommandResponse<TodaySportDataRsp> {
                    override fun onDataResponse(result: TodaySportDataRsp) {
                        val steps = if (result.status == BaseRspCmd.RESULT_OK)
                            result.sportTotal.totalSteps else 0
                        cont.resume(steps)
                    }
                }
            )
        }
    }

    actual override suspend fun syncStepsByOffset(offset: Int): DayStepData = mutex.withLock {
        suspendCoroutine { cont ->
            cmdHandle.executeReqCmd(
                ReadDetailSportDataReq(offset, 0, 95),
                object : ICommandResponse<ReadDetailSportDataRsp> {
                    override fun onDataResponse(result: ReadDetailSportDataRsp) {
                        if (result.status != BaseRspCmd.RESULT_OK) {
                            cont.resume(DayStepData(getDateForOffset(offset), 0, 0, 0, emptyList()))
                            return
                        }

                        val list = result.bleStepDetailses
                        val data = DayStepData(
                            date = if (list.isNotEmpty())
                                "${list.first().year}-${list.first().month}-${list.first().day}"
                            else getDateForOffset(offset),
                            totalSteps = list.sumOf { it.walkSteps },
                            totalCalories = list.sumOf { it.calorie },
                            totalDistance = list.sumOf { it.distance },
                            hourlyData = list.map {
                                val totalMin = it.timeIndex * 15
                                HourlyStepData(totalMin / 60, it.walkSteps, it.calorie)
                            }
                        )
                        cont.resume(data)
                    }
                }
            )
        }
    }

    actual override suspend fun syncHeartRate(offset: Int): HeartRateData = mutex.withLock {
        suspendCoroutine { cont ->
            val baseTime = getCurrentTimeWithTimezone()
            val targetTime = baseTime.toLong() - (offset * 24 * 60 * 60)

            cmdHandle.executeReqCmd(
                ReadHeartRateReq(targetTime),
                object : ICommandResponse<ReadHeartRateRsp> {
                    override fun onDataResponse(result: ReadHeartRateRsp) {
                        if (result.status != BaseRspCmd.RESULT_OK) {
                            cont.resume(HeartRateData(getDateForOffset(offset)))
                            return
                        }

                        val values = mutableListOf<HeartRateEntry>()
                        result.getmHeartRateArray()?.forEachIndexed { i, hr ->
                            if (hr.toInt() > 0) {
                                values.add(
                                    HeartRateEntry(
                                        result.getmUtcTime().toLong() + i * 5 * 60,
                                        hr.toInt(),
                                        i * 5
                                    )
                                )
                            }
                        }

                        cont.resume(
                            HeartRateData(
                                getDateForOffset(offset),
                                values,
                                if (values.isNotEmpty()) values.map { it.heartRate }.average().toInt() else 0,
                                values.maxOfOrNull { it.heartRate } ?: 0,
                                values.minOfOrNull { it.heartRate } ?: 0
                            )
                        )
                    }
                }
            )
        }
    }

    actual override suspend fun syncSpO2(offset: Int): SpO2Data = mutex.withLock {
        suspendCoroutine { cont ->
            largeData.syncBloodOxygenWithCallback { entityList ->
                if (entityList.isNullOrEmpty()) {
                    cont.resume(SpO2Data(getDateForOffset(offset)))
                    return@syncBloodOxygenWithCallback
                }

                val values = mutableListOf<SpO2Entry>()
                entityList.forEach { entity ->
                    val baseTime = resolveBaseTime(entity)
                    val arr = entity.maxArray ?: entity.minArray ?: emptyList()
                    arr.forEachIndexed { hour, value ->
                        if (value in 1..100) {
                            values.add(SpO2Entry(baseTime / 1000 + hour * 3600, value, hour))
                        }
                    }
                }

                cont.resume(
                    SpO2Data(
                        getDateForOffset(offset),
                        values,
                        if (values.isNotEmpty()) values.map { it.spo2Value }.average().toInt() else 0,
                        values.maxOfOrNull { it.spo2Value } ?: 0,
                        values.minOfOrNull { it.spo2Value } ?: 0
                    )
                )
            }
        }
    }

//    override suspend fun syncIntervalSpO2(offset: Int): IntervalSpO2Data = mutex.withLock {
//        suspendCoroutine { cont ->
//            largeData.syncBloodOxygen(offset) { data ->
//                val values = data?.mapIndexed { i, spo2 ->
//                    SpO2Entry(
//                        timestamp = System.currentTimeMillis() / 1000 + i * 60,
//                        spo2Value = spo2,
//                        hourOfDay = i / 60
//                    )
//                } ?: emptyList()
//
//                cont.resume(
//                    IntervalSpO2Data(
//                        date = getDateForOffset(offset),
//                        interval = 1,
//                        values = values
//                    )
//                )
//            }
//        }
//    }
//
//    override suspend fun syncAutoBloodPressure(offset: Int): BpData = mutex.withLock {
//        suspendCoroutine { cont ->
//            cmdHandle.executeReqCmd(
//                SimpleKeyReq(Constants.CMD_BP_TIMING_MONITOR_DATA),
//                object : ICommandResponse<BpDataRsp> {
//                    override fun onDataResponse(result: BpDataRsp) {
//                        if (result.status != BaseRspCmd.RESULT_OK) {
//                            cont.resume(BpData(getDateForOffset(offset)))
//                            return
//                        }
//
//                        val values = result.bpDataEntity?.flatMap { entity ->
//                            entity?.map { bpValue ->
//                                BpEntry(
//                                    timestamp = bpValue,
//                                    heartRate = bpValue.value,
//                                    systolic = CalcBloodPressureByHeart.cal_sbp(bpValue.value, 30),
//                                    diastolic = CalcBloodPressureByHeart.cal_dbp(
//                                        CalcBloodPressureByHeart.cal_sbp(bpValue.value, 30)
//                                    )
//                                )
//                            } ?: emptyList()
//                        } ?: emptyList()
//
//                        cont.resume(BpData(getDateForOffset(offset), values))
//                    }
//                }
//            )
//        }
//    }
//
//    override suspend fun syncManualBloodPressure(): List<BloodPressureOnceData> = mutex.withLock {
//        suspendCoroutine { cont ->
//            largeData.syncBloodPressure { bpList ->
//                val measurements = bpList?.map { bp ->
//                    BloodPressureOnceData(
//                        timestamp = bp.measureTime,
//                        systolic = bp.systolic,
//                        diastolic = bp.diastolic,
//                        heartRate = bp.heartRate
//                    )
//                } ?: emptyList()
//                cont.resume(measurements)
//            }
//        }
//    }

    actual override suspend fun confirmBloodPressureSync() = mutex.withLock {
        suspendCoroutine<Unit> { cont ->
            cmdHandle.executeReqCmd(BpReadConformReq(true), object : ICommandResponse<BaseRspCmd> {
                override fun onDataResponse(result: BaseRspCmd) {
                    cont.resume(Unit)
                }
            })
        }
    }

    actual override suspend fun syncHrv(offset: Int): HrvData = mutex.withLock {
        suspendCoroutine { cont ->
            cmdHandle.executeReqCmd(
                HRVReq(offset.toByte()),
                object : ICommandResponse<HRVRsp> {
                    override fun onDataResponse(result: HRVRsp) {
                        if (result.range <= 0) {
                            cont.resume(HrvData(getDateForOffset(offset)))
                            return
                        }

                        val calendar = Calendar.getInstance()
                        calendar.add(Calendar.DAY_OF_YEAR, -offset)
                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)
                        val baseTimestamp = calendar.timeInMillis / 1000

                        val values = mutableListOf<HrvEntry>()
                        result.hrvArray?.forEachIndexed { i, hrv ->
                            val hrvValue = hrv.toInt() and 0xFF
                            if (hrvValue > 0) {
                                val intervalMin = result.range.coerceAtLeast(1)
                                values.add(
                                    HrvEntry(
                                        baseTimestamp + i * intervalMin * 60,
                                        hrvValue,
                                        i * intervalMin
                                    )
                                )
                            }
                        }
                        cont.resume(
                            HrvData(
                                getDateForOffset(offset),
                                values,
                                if (values.isNotEmpty()) values.map { it.hrvValue }.average().toInt() else 0,
                                values.maxOfOrNull { it.hrvValue } ?: 0,
                                values.minOfOrNull { it.hrvValue } ?: 0
                            )
                        )
                    }
                }
            )
        }
    }

    actual override suspend fun syncPressure(offset: Int): PressureData = mutex.withLock {
        suspendCoroutine { cont ->
            cmdHandle.executeReqCmd(
                PressureReq(offset.toByte()),
                object : ICommandResponse<PressureRsp> {
                    override fun onDataResponse(result: PressureRsp) {
                        if (result.pressureArray == null) {
                            cont.resume(PressureData(getDateForOffset(offset)))
                            return
                        }

                        val values = mutableListOf<PressureEntry>()
                        result.pressureArray.forEachIndexed { i, pressure ->
                            val pressureValue = (pressure.toInt() and 0xFF) / 10f
                            if (pressureValue > 0) {
                                values.add(
                                    PressureEntry(
                                        minuteOfDay = i * result.range,
                                        pressureValue = pressureValue
                                    )
                                )
                            }
                        }

                        cont.resume(
                            PressureData(
                                getDateForOffset(offset),
                                values,
                                if (values.isNotEmpty())
                                    values.map { it.pressureValue }.average().toFloat() else 0f
                            )
                        )
                    }
                }
            )
        }
    }

    actual override suspend fun syncAutoTemperature(offset: Int): TemperatureData = mutex.withLock {
        suspendCoroutine { cont ->
            fileHandle.clearCallback()
            fileHandle.registerCallback(object : SimpleCallback() {
                override fun onUpdateTemperature(data: TemperatureEntity) {
                    val values = mutableListOf<TemperatureEntry>()

                    data.mValues?.forEachIndexed { i, temp ->
                        if (temp > 0) {
                            values.add(
                                TemperatureEntry(
                                    minuteOfDay = i * data.mTimeSpan,
                                    temperature = temp
                                )
                            )
                        }
                    }

                    cont.resume(
                        TemperatureData(
                            date = getDateForOffset(data.mIndex),
                            entries = values,
                            averageTemp = if (values.isNotEmpty())
                                values.map { it.temperature }.average().toFloat() else 0f,
                            maxTemp = values.maxOfOrNull { it.temperature } ?: 0f,
                            minTemp = values.minOfOrNull { it.temperature } ?: 0f
                        )
                    )
                }
            })
            fileHandle.initRegister()
            fileHandle.startObtainTemperatureSeries(offset)
        }
    }

//    override suspend fun syncManualTemperature(): List<TemperatureOnceData> = mutex.withLock {
//        suspendCoroutine { cont ->
//            largeData.syncTemperature { tempList ->
//                val measurements = tempList?.map { temp ->
//                    TemperatureOnceData(
//                        timestamp = temp.measureTime,
//                        temperature = temp.temperature
//                    )
//                } ?: emptyList()
//                cont.resume(measurements)
//            }
//        }
//    }

//    override suspend fun syncSleep(offset: Int): SleepData = mutex.withLock {
//        suspendCoroutine { cont ->
//            largeData.syncSleepList(offset,
//                { sleepResponse ->
//                    val sleepSessions = mutableListOf<SleepSession>()
//
//                    sleepResponse?.list?.forEach { detail ->
//                        sleepSessions.add(
//                            SleepSession(
//                                startTime = sleepResponse.st.toLong(),
//                                endTime = sleepResponse.et.toLong(),
//                                duration = detail.d,
//                                type = when (detail.t) {
//                                    2 -> "Light Sleep"
//                                    3 -> "Deep Sleep"
//                                    4 -> "REM"
//                                    5 -> "Awake"
//                                    else -> "Unknown"
//                                }
//                            )
//                        )
//                    }
//
//                    cont.resume(
//                        SleepData(
//                            date = getDateForOffset(offset),
//                            sessions = sleepSessions,
//                            totalSleepMinutes = sleepSessions.sumOf { it.duration }
//                        )
//                    )
//                },
//                { }
//            )
//        }
//    }
//
//    override suspend fun syncLunchSleep(offset: Int): SleepData? = mutex.withLock {
//        suspendCoroutine { cont ->
//            largeData.syncSleepList(offset,
//                { },
//                { lunchSleepResponse ->
//                    if (lunchSleepResponse == null) {
//                        cont.resume(null)
//                        return@syncSleepList
//                    }
//
//                    val sleepSessions = mutableListOf<SleepSession>()
//                    lunchSleepResponse.list?.forEach { detail ->
//                        sleepSessions.add(
//                            SleepSession(
//                                startTime = lunchSleepResponse.st.toLong(),
//                                endTime = lunchSleepResponse.et.toLong(),
//                                duration = detail.d,
//                                type = when (detail.t) {
//                                    2 -> "Light Sleep"
//                                    3 -> "Deep Sleep"
//                                    4 -> "REM"
//                                    5 -> "Awake"
//                                    else -> "Unknown"
//                                }
//                            )
//                        )
//                    }
//
//                    cont.resume(
//                        SleepData(
//                            date = getDateForOffset(offset),
//                            sessions = sleepSessions,
//                            totalSleepMinutes = sleepSessions.sumOf { it.duration }
//                        )
//                    )
//                }
//            )
//        }
//    }
//
//    override suspend fun syncSedentary(offset: Int): SedentaryData = mutex.withLock {
//        suspendCoroutine { cont ->
//            cmdHandle.executeReqCmd(
//                SedentaryReq(offset.toByte()),
//                object : ICommandResponse<SedentaryRsp> {
//                    override fun onDataResponse(result: SedentaryRsp) {
//                        if (result.sedentaryArray == null) {
//                            cont.resume(SedentaryData(getDateForOffset(offset)))
//                            return
//                        }
//
//                        val entries = mutableListOf<SedentaryEntry>()
//                        result.sedentaryArray.forEachIndexed { i, sedentary ->
//                            if (sedentary > 0) {
//                                entries.add(
//                                    SedentaryEntry(
//                                        minuteOfDay = i * result.range,
//                                        sedentaryMinutes = sedentary
//                                    )
//                                )
//                            }
//                        }
//
//                        cont.resume(
//                            SedentaryData(
//                                date = getDateForOffset(offset),
//                                entries = entries,
//                                totalSedentaryMinutes = entries.sumOf { it.sedentaryMinutes }
//                            )
//                        )
//                    }
//                }
//            )
//        }
//}

    private fun getCurrentTimeWithTimezone(): Int {
        return try {
            val tzHours = TimeZone.getDefault().rawOffset / (1000 * 60 * 60)
            val currentSec = System.currentTimeMillis() / 1000
            (tzHours * 3600 + currentSec).toInt()
        } catch (e: Exception) {
            0
        }
    }

    private fun resolveBaseTime(entity: BloodOxygenEntity): Long {
        val unix = entity.unix_time
        if (unix > 0) return if (unix >= 1_000_000_000_000L) unix else unix * 1000

        entity.dateStr?.let { dateStr ->
            try {
                return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .parse(dateStr)?.time ?: System.currentTimeMillis()
            } catch (_: Exception) {}
        }

        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getDateForOffset(offset: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -offset)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }
}


actual class InstantMeasures : IInstantMeasures {

    private val bleOps = BleOperateManager.getInstance()
    private val mutex = Mutex()

    // ============ MANUAL MEASUREMENT FUNCTIONS ============

    actual override suspend fun measureHeartRate(): String = mutex.withLock {
        suspendCancellableCoroutine { cont ->
            val resumed = AtomicBoolean(false)
            bleOps.manualModeHeart({ result ->
                val hr = result.value.toInt()
                val success = result.errCode.toInt() == 0 && hr > 0
                if (success && cont.isActive && resumed.compareAndSet(false, true)) {
                    cont.resume("Heart Rate: $hr bpm")
                }
            }, false)
        }
    }

    actual override suspend fun measureSpO2(): String = mutex.withLock {
        suspendCancellableCoroutine { cont ->
            val resumed = AtomicBoolean(false)
            bleOps.manualModeSpO2({ result ->
                val spo2 = result.value.toInt()
                val success = result.errCode.toInt() == 0 && spo2 in 1..100
                if (success && cont.isActive && resumed.compareAndSet(false, true)) {
                    cont.resume("SpO2: $spo2%")
                }
            }, false)
        }
    }

    actual override suspend fun measureHrv(): String = mutex.withLock {
        suspendCancellableCoroutine { cont ->
            val resumed = AtomicBoolean(false)
            bleOps.manualModeHrv({ result ->
                val hrv = result.value.toInt()
                val success = result.errCode.toInt() == 0 && hrv > 0
                if (success && cont.isActive && resumed.compareAndSet(false, true)) {
                    cont.resume("HRV: $hrv ms")
                }
            }, false)
        }
    }

    actual override suspend fun measureBloodPressure(): String = mutex.withLock {
        suspendCancellableCoroutine { cont ->
            val resumed = AtomicBoolean(false)
            bleOps.manualModeBP({ result ->
                val success = result.errCode.toInt() == 0
                if (success && cont.isActive && resumed.compareAndSet(false, true)) {
                    cont.resume(
                        MeasurementResult(
                            success = true,
                            heartRate = result.value.toInt(),
                            systolic = result.sbp.toInt(),
                            diastolic = result.dbp.toInt(),
                            message = "BP: ${result.sbp.toInt()}/${result.dbp.toInt()} mmHg, HR: ${result.value.toInt()} bpm"
                        ).toString()
                    )
                }
            }, false)
        }
    }

    actual override suspend fun measurePressure(): String = mutex.withLock {
        suspendCancellableCoroutine { cont ->
            val resumed = AtomicBoolean(false)
            bleOps.manualModePressure({ result ->
                val pressure = result.value.toInt()
                val success = result.errCode.toInt() == 0 && pressure > 0
                if (success && cont.isActive && resumed.compareAndSet(false, true)) {
                    cont.resume("Stress Level: $pressure")
                }
            }, false)
        }
    }

    actual override suspend fun measureTemperature(): String = mutex.withLock {
        suspendCancellableCoroutine { cont ->
            val resumed = AtomicBoolean(false)
            bleOps.manualTemperature({ result ->
                val temp = result.value.toInt() / 10f
                val success = result.errCode.toInt() == 0 && temp > 0
                if (success && cont.isActive && resumed.compareAndSet(false, true)) {
                    cont.resume("Temperature: ${temp}°C")
                }
            }, false)
        }
    }

    actual override suspend fun measureOneClick(): OneClickResult  = mutex.withLock {
        suspendCancellableCoroutine { cont ->
            val resumed = AtomicBoolean(false)
            bleOps.oneClickMeasurement({ result ->
                if (cont.isActive && resumed.compareAndSet(false, true)) {
                    cont.resume(
                        OneClickResult(
                            heartRate = result.heartRate,
                            bloodOxygen = result.bloodOxygen,
                            systolic = result.sbp,
                            diastolic = result.dbp,
                            hrv = result.hrv,
                            stress = result.stress,
                            temperature = result.temperature / 10f,
                            rri = result.rri
                        )
                    )
                }
            }, false)
        }
    }

    actual override suspend fun measureHeartRateRawData(seconds: Int): RawDataResult = mutex.withLock {
        suspendCancellableCoroutine { cont ->
            val resumed = AtomicBoolean(false)
            bleOps.manualModeHeartRateRawData({ result ->
                if (cont.isActive && resumed.compareAndSet(false, true)) {
                    cont.resume(
                        RawDataResult(
                            heartRate = result.heartRate,
                            hrv = result.hrv,
                            stress = result.stress,
                            ppgCount = result.ppgCount,
                            greenLightPpgL = result.greenLightPpgL,
                            greenLightPpgH = result.greenLightPpgH,
                            xL = result.xl,
                            xH = result.xh,
                            yL = result.yl,
                            yH = result.yh,
                            zL = result.zl,
                            zH = result.zh
                        )
                    )
                }
            }, seconds, false)
        }
    }

    actual override suspend fun measureBloodOxygenRawData(seconds: Int): RawDataResult = mutex.withLock {
        suspendCancellableCoroutine { cont ->
            val resumed = AtomicBoolean(false)
            bleOps.manualModeBloodOxygenRawData({ result ->
                if (cont.isActive && resumed.compareAndSet(false, true)) {
                    cont.resume(
                        RawDataResult(
                            bloodOxygen = result.bloodOxygen,
                            heartRate = result.heartRate,
                            ppgCount = result.ppgCount,
                            redLightPpgL = result.redLightPpgL,
                            redLightPpgH = result.redLightPpgH,
                            infraredPpgL = result.infraredPpgL,
                            infraredPpgH = result.infraredPpgH,
                            greenLightPpgL = result.greenLightPpgL,
                            greenLightPpgH = result.greenLightPpgH
                        )
                    )
                }
            }, seconds, false)
        }
    }
}

actual class ContinuousMonitoring : IContinuousMonitoring {

    private val cmdHandle = CommandHandle.getInstance()
    private val mutex = Mutex()

    actual override suspend fun toggleHeartRateMonitoring(enabled: Boolean, interval: Int): String = mutex.withLock {
        suspendCoroutine { cont ->
            val validInterval = when {
                interval in listOf(10, 15, 20, 30, 60) -> interval
                else -> 30
            }

            cmdHandle.executeReqCmd(
                HeartRateSettingReq.getWriteInstance(enabled, validInterval),
                object : ICommandResponse<HeartRateSettingRsp> {
                    override fun onDataResponse(result: HeartRateSettingRsp) {
                        val status = if (result.status == BaseRspCmd.RESULT_OK) {
                            if (enabled) "enabled with $validInterval min interval"
                            else "disabled"
                        } else "failed"
                        cont.resume("Heart rate monitoring $status")
                    }
                }
            )
        }
    }

    actual override suspend fun toggleHrvMonitoring(enabled: Boolean, interval: Int): String = mutex.withLock {
        suspendCoroutine { cont ->
            val request = if (interval > 0) {
                HrvSettingReq(enabled)
            } else {
                HrvSettingReq(enabled)
            }

            cmdHandle.executeReqCmd(
                request,
                object : ICommandResponse<HRVSettingRsp> {
                    override fun onDataResponse(result: HRVSettingRsp) {
                        val status = if (result.status == BaseRspCmd.RESULT_OK) {
                            if (enabled && interval > 0) "enabled with $interval min interval"
                            else if (enabled) "enabled"
                            else "disabled"
                        } else "failed"
                        cont.resume("HRV monitoring $status")
                    }
                }
            )
        }
    }

    actual override suspend fun toggleSpO2Monitoring(enabled: Boolean, interval: Int): String = mutex.withLock {
        suspendCoroutine { cont ->
            val request = BloodOxygenSettingReq.getWriteInstance(enabled)

            cmdHandle.executeReqCmd(
                request,
                object : ICommandResponse<BloodOxygenSettingRsp> {
                    override fun onDataResponse(result: BloodOxygenSettingRsp) {
                        val status = if (result.status == BaseRspCmd.RESULT_OK) {
                            if (enabled) "enabled" else "disabled"
                        } else "failed"
                        cont.resume("SpO2 monitoring $status")
                    }
                }
            )
        }
    }

    actual override suspend fun toggleIntervalSpO2Monitoring(enabled: Boolean, interval: Int): String = mutex.withLock {
        suspendCoroutine { cont ->
            cmdHandle.executeReqCmd(
                BloodOxygenSettingReq.getWriteInstance(enabled),
                object : ICommandResponse<BloodOxygenSettingRsp> {
                    override fun onDataResponse(result: BloodOxygenSettingRsp) {
                        val status = if (result.status == BaseRspCmd.RESULT_OK) {
                            if (enabled) "enabled with $interval min interval"
                            else "disabled"
                        } else "failed"
                        cont.resume("Interval SpO2 monitoring $status")
                    }
                }
            )
        }
    }

    actual override suspend fun toggleBloodPressureMonitoring(
        enabled: Boolean,
        startEndTime: dev.infa.page3.SDK.data.StartEndTimeEntity,
        interval: Int
    ): String = mutex.withLock {
        suspendCoroutine { cont ->
            val multiple = if (interval > 0) interval else 60

            cmdHandle.executeReqCmd(
                BpSettingReq.getWriteInstance(enabled, startEndTime as StartEndTimeEntity, multiple),
                object : ICommandResponse<BpSettingRsp> {
                    override fun onDataResponse(result: BpSettingRsp) {
                        val status = if (result.status == BaseRspCmd.RESULT_OK) {
                            if (enabled) "enabled with $multiple min interval"
                            else "disabled"
                        } else "failed"
                        cont.resume("Blood pressure monitoring $status")
                    }
                }
            )
        }
    }

    actual override suspend fun togglePressureMonitoring(enabled: Boolean): String = mutex.withLock {
        suspendCoroutine { cont ->
            cmdHandle.executeReqCmd(
                PressureSettingReq.getWriteInstance(enabled),
                object : ICommandResponse<PressureSettingRsp> {
                    override fun onDataResponse(result: PressureSettingRsp) {
                        val status = if (result.status == BaseRspCmd.RESULT_OK) {
                            if (enabled) "enabled" else "disabled"
                        } else "failed"
                        cont.resume("Stress monitoring $status")
                    }
                }
            )
        }
    }

    actual override suspend fun toggleTemperatureMonitoring(enabled: Boolean, interval: Int): String = mutex.withLock {
        suspendCoroutine { cont ->
            val validInterval = interval.coerceIn(2, 120)

            cmdHandle.executeReqCmd(
                SugarLipidsSettingReq.getWriteInstance(0x03, enabled, validInterval),
                object : ICommandResponse<BloodSugarLipidsSettingRsp> {
                    override fun onDataResponse(result: BloodSugarLipidsSettingRsp) {
                        val status = if (result.status == BaseRspCmd.RESULT_OK) {
                            if (enabled) "enabled with $validInterval sec interval"
                            else "disabled"
                        } else "failed"
                        cont.resume("Temperature monitoring $status")
                    }
                }
            )
        }
    }
}
