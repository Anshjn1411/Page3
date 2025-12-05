package dev.infa.page3.viewmodels

import android.util.Log
import com.oudmon.ble.base.bean.SleepDisplay
import com.oudmon.ble.base.communication.CommandHandle
import com.oudmon.ble.base.communication.Constants
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.LargeDataHandler
import com.oudmon.ble.base.communication.dfu_temperature.TemperatureEntity
import com.oudmon.ble.base.communication.dfu_temperature.TemperatureOnceEntity
import com.oudmon.ble.base.communication.file.FileHandle
import com.oudmon.ble.base.communication.file.SimpleCallback
import com.oudmon.ble.base.communication.req.*
import com.oudmon.ble.base.communication.rsp.*
import com.oudmon.ble.base.communication.sport.SportPlusHandle
import com.oudmon.ble.base.util.CalcBloodPressureByHeart
import com.oudmon.ble.base.util.ISleepCallback
import com.oudmon.ble.base.util.SleepAnalyzerUtils
import java.util.TimeZone
import java.text.SimpleDateFormat
import java.util.*

/**
 * Comprehensive data synchronization class following SDK documentation
 * Handles all data sync operations with proper callback handling
 */
class DataSynchronization(
    private val commandHandle: CommandHandle?,
    private val addLog: (String) -> Unit
) {
    companion object {
        const val TAG = "DataSynchronization"
    }

    private var isSyncing = false
    private val syncLock = Object()

    // Callbacks for data synchronization
    private var sleepDataCallback: ((SleepData) -> Unit)? = null
    private var stepDataCallback: ((StepData) -> Unit)? = null
    private var heartRateCallback: ((HeartRateData) -> Unit)? = null

    /**
     * Set callback for sleep data
     */
    fun setSleepDataCallback(callback: (SleepData) -> Unit) {
        sleepDataCallback = callback
    }

    /**
     * Set callback for step data
     */
    fun setStepDataCallback(callback: (StepData) -> Unit) {
        stepDataCallback = callback
    }

    /**
     * Set callback for heart rate data
     */
    fun setHeartRateCallback(callback: (HeartRateData) -> Unit) {
        heartRateCallback = callback
    }

//
    /**
     * Sync today's step data
     */
    fun syncTodaySteps()  {
        if (commandHandle == null) {
            addLog("ERROR: CommandHandle not initialized")
            return
        }

        addLog("Syncing today's step data...")
        try {
            commandHandle.executeReqCmd(
                SimpleKeyReq(Constants.CMD_GET_STEP_TODAY),
                object : ICommandResponse<TodaySportDataRsp> {
                    override fun onDataResponse(resultEntity: TodaySportDataRsp) {
                        try {
                            if (resultEntity.status == BaseRspCmd.RESULT_OK) {
                                val stepData = convertTodaySportDataToStepData(resultEntity)
                                stepDataCallback?.invoke(stepData)
                                
                                addLog("Step data synced - Steps: ${resultEntity.sportTotal.totalSteps}")
                                addLog("Calories: ${resultEntity.sportTotal.calorie}, Distance: ${resultEntity.sportTotal.walkDistance}m")
                            } else {
                                addLog("ERROR: Failed to sync step data - Status: ${resultEntity.status}")
                            }
                        } catch (e: Exception) {
                            addLog("ERROR: Step data processing failed: ${e.message}")
                            Log.e(TAG, "Step data processing error", e)
                        }
                    }
                }
            )
        } catch (e: Exception) {
            addLog("ERROR: Exception syncing step data: ${e.message}")
            Log.e(TAG, "Step sync error", e)
        }
    }

    fun syncDetailStepData(dayOffset: Int) {
        if (commandHandle == null) {
            addLog("ERROR: CommandHandle not initialized")
            return
        }

        addLog("Syncing detailed step data for day offset: $dayOffset")
        try {
            commandHandle.executeReqCmd(
                ReadDetailSportDataReq(dayOffset, 0, 95),
                object : ICommandResponse<ReadDetailSportDataRsp> {
                    override fun onDataResponse(resultEntity: ReadDetailSportDataRsp) {
                        try {
                            if (resultEntity.status == BaseRspCmd.RESULT_OK) {
                                val detailStepData = convertDetailStepData(resultEntity, dayOffset)
                                stepDataCallback?.invoke(detailStepData)
                                
                                addLog("Detailed step data synced for day offset: $dayOffset")
                            } else {
                                addLog("ERROR: Failed to sync detailed step data - Status: ${resultEntity.status}")
                            }
                        } catch (e: Exception) {
                            addLog("ERROR: Detail step processing failed: ${e.message}")
                            Log.e(TAG, "Detail step error", e)
                        }
                    }
                }
            )
        } catch (e: Exception) {
            addLog("ERROR: Exception syncing detailed step data: ${e.message}")
            Log.e(TAG, "Detail step sync error", e)
        }
    }

    /**
     * Sync heart rate data
     * @param nowTime Current time zone * 3600 + unix second value of current time
     */
    fun syncHeartRateData(nowTime: Int) {
        if (commandHandle == null) {
            addLog("ERROR: CommandHandle not initialized")
            return
        }

        addLog("Syncing heart rate data...")
        try {
            commandHandle.executeReqCmd(
                ReadHeartRateReq(nowTime.toLong()),
                object : ICommandResponse<ReadHeartRateRsp> {
                    override fun onDataResponse(resultEntity: ReadHeartRateRsp) {
                        try {
                            if (resultEntity.status == BaseRspCmd.RESULT_OK) {
                                val heartRateData = convertHeartRateData(resultEntity, nowTime)
                                heartRateCallback?.invoke(heartRateData)
                                
                                addLog("Heart rate data synced - UTC Time: ${resultEntity.getmUtcTime()}")
                            } else {
                                addLog("ERROR: Failed to sync heart rate data - Status: ${resultEntity.status}")
                            }
                        } catch (e: Exception) {
                            addLog("ERROR: Heart rate processing failed: ${e.message}")
                            Log.e(TAG, "Heart rate processing error", e)
                        }
                    }
                }
            )
        } catch (e: Exception) {
            addLog("ERROR: Exception syncing heart rate data: ${e.message}")
            Log.e(TAG, "Heart rate sync error", e)
        }
    }

    /**
     * Sync sedentary data
     * @param offset Day offset (0: today, 1: yesterday, etc.)
     */
    fun syncSedentaryData(offset: Int) {
        if (commandHandle == null) {
            addLog("ERROR: CommandHandle not initialized")
            return
        }

        addLog("Syncing sedentary data for offset: $offset")
        try {
            LargeDataHandler.getInstance().syncLongSitList(offset) { longSitResp ->
                try {
                    addLog("Sedentary data synced - ${longSitResp.list?.size ?: 0} entries")
                    // Process sedentary data here if needed
                } catch (e: Exception) {
                    addLog("ERROR: Sedentary data processing failed: ${e.message}")
                    Log.e(TAG, "Sedentary processing error", e)
                }
            }
        } catch (e: Exception) {
            addLog("ERROR: Exception syncing sedentary data: ${e.message}")
            Log.e(TAG, "Sedentary sync error", e)
        }
    }

    /**
     * Get current time with timezone
     */
    fun getCurrentTimeWithTimezone(): Int {
        return try {
            val timeZone = getTimeZone()
            val currentTime = System.currentTimeMillis() / 1000
            (timeZone * 3600 + currentTime).toInt()
        } catch (e: Exception) {
            addLog("ERROR: Exception getting current time: ${e.message}")
            Log.e(TAG, "Timezone error", e)
            0
        }
    }

    /**
     * Get timezone offset
     */
    private fun getTimeZone(): Int {
        return TimeZone.getDefault().rawOffset / (1000 * 60 * 60)
    }

    // Data conversion methods

    /**
     * Convert SleepDisplay from SDK to our SleepData class
     */
//    private fun convertSleepDisplayToSleepData(sleepDisplay: SleepDisplay, dayOffset: Int): SleepData {
//        val date = getDateForOffset(dayOffset)
//
//        // Convert sleep stages from SDK format
//        val sleepStages = mutableListOf<SleepStage>()
//        sleepDisplay.list?.forEach { sleepDataBean ->
//            val stageType = when (sleepDataBean.type) {
//                1 -> SleepType.DEEP
//                2 -> SleepType.LIGHT
//                3 -> SleepType.AWAKE
//                else -> SleepType.LIGHT
//            }
//
//            sleepStages.add(
//                SleepStage(
//                    timestamp = sleepDataBean.sleepStart.toLong(),
//                    type = stageType,
//                    duration = ((sleepDataBean.sleepEnd - sleepDataBean.sleepStart) / 60).toInt()
//                )
//            )
//        }
//
//        // Get durations from SDK
//        val totalDuration = sleepDisplay.totalSleepDuration.toLong()
//        val deepDuration = sleepDisplay.deepSleepDuration.toLong()
//        val lightDuration = sleepDisplay.shallowSleepDuration.toLong()
//        val awakeDuration = sleepDisplay.awakeDuration.toLong()
//        val remDuration = sleepDisplay.rapidDuration.toLong()
//
//        // Calculate sleep metrics
//        val sleepScore = calculateSleepScore(totalDuration, deepDuration, lightDuration)
//        val sleepEfficiency = calculateSleepEfficiency(totalDuration, deepDuration, lightDuration)
//        val sleepQuality = getSleepQuality(sleepScore)
//
//        return SleepData(
//            date = date,
//            totalDuration = totalDuration,
//            deepSleepDuration = deepDuration,
//            lightSleepDuration = lightDuration,
//            remDuration = remDuration,
//            awakeDuration = awakeDuration,
//            sleepTime = formatTimestampToTime(sleepDisplay.sleepTime),
//            wakeTime = formatTimestampToTime(sleepDisplay.wakeTime),
//            sleepScore = sleepScore,
//            sleepEfficiency = sleepEfficiency,
//            sleepQuality = sleepQuality,
//            sleepStages = sleepStages
//        )
//    }


    /**
     * Convert TodaySportDataRsp to StepData
     */
    private fun convertTodaySportDataToStepData(response: TodaySportDataRsp): StepData {
        val date = "${response.sportTotal.year}-${response.sportTotal.month.toString().padStart(2, '0')}-${response.sportTotal.day.toString().padStart(2, '0')}"
        
        return StepData(
            date = date,
            totalSteps = response.sportTotal.totalSteps.toLong(),
            runningSteps = response.sportTotal.runningSteps.toLong(),
            calories = response.sportTotal.calorie.toLong(),
            distance = response.sportTotal.walkDistance.toLong(),
            sportDuration = response.sportTotal.sportDuration.toLong(),
            sleepDuration = response.sportTotal.sleepDuration.toLong()
        )
    }

    /**
     * Convert ReadDetailSportDataRsp to StepData
     */
    private fun convertDetailStepData(response: ReadDetailSportDataRsp, dayOffset: Int): StepData {
        val firstData = response.bleStepDetailses.firstOrNull()
        val date = if (firstData != null) {
            "${firstData.year}-${firstData.month.toString().padStart(2, '0')}-${firstData.day.toString().padStart(2, '0')}"
        } else {
            getDateForOffset(dayOffset)
        }

        // Calculate totals from detailed data
        var totalSteps = 0L
        var totalCalories = 0L
        var totalDistance = 0L

        response.bleStepDetailses.forEach { detail ->
            totalSteps += detail.walkSteps
            totalCalories += detail.calorie
            totalDistance += detail.distance
        }

        return StepData(
            date = date,
            totalSteps = totalSteps,
            runningSteps = 0, // Not available in detail data
            calories = totalCalories,
            distance = totalDistance,
            sportDuration = 0, // Not available in detail data
            sleepDuration = 0  // Not available in detail data
        )
    }

    /**
     * Convert ReadHeartRateRsp to HeartRateData
     */
    private fun convertHeartRateData(response: ReadHeartRateRsp, nowTime: Int): HeartRateData {
        val heartRateValues = mutableListOf<HeartRateEntry>()
        
        response.getmHeartRateArray()?.let { hrArray ->
            for (i in hrArray.indices) {
                if (hrArray[i].toInt() > 0) {
                    heartRateValues.add(
                        HeartRateEntry(
                            timestamp = response.getmUtcTime().toLong() + (i * 5 * 60), // 5 minutes per data point
                            heartRate = hrArray[i].toInt(),
                            minuteOfDay = i * 5
                        )
                    )
                }
            }
        }

        return HeartRateData(
            date = getDateForOffset(0), // Assuming today's data
            heartRateValues = heartRateValues,
            averageHeartRate = if (heartRateValues.isNotEmpty()) {
                heartRateValues.map { it.heartRate }.average().toInt()
            } else 0,
            maxHeartRate = heartRateValues.maxOfOrNull { it.heartRate } ?: 0,
            minHeartRate = heartRateValues.minOfOrNull { it.heartRate } ?: 0
        )
    }

    /**
     * Calculate sleep score based on duration and quality
     */
    private fun calculateSleepScore(total: Long, deep: Long, light: Long): Int {
        val totalHours = total / 3600.0
        val deepPercentage = if (total > 0) (deep.toDouble() / total) * 100 else 0.0

        var score = 0

        // Duration score (0-40 points)
        score += when {
            totalHours >= 7 && totalHours <= 9 -> 40
            totalHours >= 6 && totalHours < 7 -> 30
            totalHours >= 5 && totalHours < 6 -> 20
            else -> 10
        }

        // Deep sleep score (0-40 points)
        score += when {
            deepPercentage >= 20 -> 40
            deepPercentage >= 15 -> 30
            deepPercentage >= 10 -> 20
            else -> 10
        }

        // Consistency score (0-20 points) - simplified
        score += 20

        return score.coerceIn(0, 100)
    }

    /**
     * Calculate sleep efficiency
     */
    private fun calculateSleepEfficiency(total: Long, deep: Long, light: Long): Int {
        val actualSleep = deep + light
        val efficiency = if (total > 0) (actualSleep.toDouble() / total * 100).toInt() else 0
        return efficiency.coerceIn(0, 100)
    }

    /**
     * Get sleep quality text
     */
    private fun getSleepQuality(score: Int): String {
        return when {
            score >= 80 -> "Good"
            score >= 60 -> "Fair"
            else -> "Poor"
        }
    }

    /**
     * Format timestamp to time string
     */
    private fun formatTimestampToTime(timestamp: Int): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp * 1000L
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        return format.format(calendar.time)
    }

    /**
     * Get date string for offset
     */
    private fun getDateForOffset(offset: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -offset)
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return format.format(calendar.time)
    }

    /**
     * Process raw sleep data from direct command response
     */
//    private fun processRawSleepData(response: ReadSleepDetailsRsp, dayOffset: Int): SleepData {
//        try {
//            addLog("Processing raw sleep data for offset: $dayOffset")
//
//            // Calculate totals from raw sleep details
//            var totalDuration = 0L
//            var deepDuration = 0L
//            var lightDuration = 0L
//            var awakeDuration = 0L
//            var remDuration = 0L
//
//            val sleepStages = mutableListOf<SleepStage>()
//
//            response.bleSleepDetailses.forEach { detail ->
//                addLog("Processing sleep detail: ${detail.toString()}")
//
//                // Process sleep qualities array (each element represents a time period)
//                detail.sleepQualities?.forEachIndexed { index, quality ->
//                    val duration = 1L // Each quality represents 1 minute
//                    totalDuration += duration
//
//                    val stageType = when (quality) {
//                        1 -> {
//                            deepDuration += duration
//                            SleepType.DEEP
//                        }
//                        2 -> {
//                            lightDuration += duration
//                            SleepType.LIGHT
//                        }
//                        3 -> {
//                            awakeDuration += duration
//                            SleepType.AWAKE
//                        }
//                        else -> SleepType.LIGHT
//                    }
//
//                    sleepStages.add(
//                        SleepStage(
//                            timestamp = (detail.timeIndex + index).toLong(),
//                            type = stageType,
//                            duration = 1 // 1 minute per quality entry
//                        )
//                    )
//                }
//            }
//
//            // Calculate sleep metrics
//            val sleepScore = calculateSleepScore(totalDuration, deepDuration, lightDuration)
//            val sleepEfficiency = calculateSleepEfficiency(totalDuration, deepDuration, lightDuration)
//            val sleepQuality = getSleepQuality(sleepScore)
//
//            val date = getDateForOffset(dayOffset)
//
//            return SleepData(
//                date = date,
//                totalDuration = totalDuration,
//                deepSleepDuration = deepDuration,
//                lightSleepDuration = lightDuration,
//                remDuration = remDuration,
//                awakeDuration = awakeDuration,
//                sleepTime = formatTimestampToTime(response.bleSleepDetailses.firstOrNull()?.timeIndex ?: 0),
//                wakeTime = formatTimestampToTime((response.bleSleepDetailses.lastOrNull()?.timeIndex ?: 0) + (response.bleSleepDetailses.lastOrNull()?.sleepQualities?.size ?: 0)),
//                sleepScore = sleepScore,
//                sleepEfficiency = sleepEfficiency,
//                sleepQuality = sleepQuality,
//                sleepStages = sleepStages
//            )
//
//        } catch (e: Exception) {
//            addLog("ERROR: Processing raw sleep data failed: ${e.message}")
//            Log.e(TAG, "Raw sleep data processing error", e)
//            return createEmptySleepData(dayOffset)
//        }
//    }

    /**
     * Create empty sleep data when no data is available
     */
    private fun createEmptySleepData(dayOffset: Int): SleepData {
        val date = getDateForOffset(dayOffset)
        
        return SleepData(
            date = date,
            awakeDuration = 0,
            sleepScore = 0,
            sleepEfficiency = 0.0,
            deepSleep = 0,
            lightSleep = 0,
            remSleep = 0,
            sleepStartTime = "",
            sleepEndTime = "",
            stages = emptyList(),
            totalDuration = 0.0
        )
    }

    // ================= Additional syncs (optimized, concise) =================

    // Callbacks
    private var bloodPressureCallback: ((BloodPressureData) -> Unit)? = null
    private var bloodOxygenCallback: ((BloodOxygenData) -> Unit)? = null
    private var stressCallback: ((StressData) -> Unit)? = null
    private var hrvCallback: ((HrvData) -> Unit)? = null
    private var temperatureSeriesCallback: ((TemperatureSeriesData) -> Unit)? = null
    private var temperatureOnceCallback: ((List<TemperatureOnceData>) -> Unit)? = null

    fun setBloodPressureCallback(cb: (BloodPressureData) -> Unit) { bloodPressureCallback = cb }
    fun setBloodOxygenCallback(cb: (BloodOxygenData) -> Unit) { bloodOxygenCallback = cb }
    fun setStressCallback(cb: (StressData) -> Unit) { stressCallback = cb }
    fun setHrvCallback(cb: (HrvData) -> Unit) { hrvCallback = cb }
    fun setTemperatureSeriesCallback(cb: (TemperatureSeriesData) -> Unit) { temperatureSeriesCallback = cb }
    fun setTemperatureOnceCallback(cb: (List<TemperatureOnceData>) -> Unit) { temperatureOnceCallback = cb }

    // Blood Pressure
    fun syncBloodPressureAuto(age: Int) {
        if (commandHandle == null) { addLog("ERROR: CommandHandle not initialized"); return }
        addLog("Syncing auto blood pressure (hourly)")
        try {
            commandHandle.executeReqCmd(
                SimpleKeyReq(Constants.CMD_BP_TIMING_MONITOR_DATA),
                object : ICommandResponse<BpDataRsp> {
                    override fun onDataResponse(resultEntity: BpDataRsp) {
                        try {
                            val entries = mutableListOf<BloodPressureEntry>()
                            resultEntity.bpDataEntity?.bpValues?.forEach { v ->
                                val hr = v.value
                                val sbp = CalcBloodPressureByHeart.cal_sbp(hr, age)
                                val dbp = CalcBloodPressureByHeart.cal_dbp(sbp)
                                entries.add(BloodPressureEntry(timeMinute = v.timeMinute, sbp = sbp, dbp = dbp, hr = hr))
                            }
                            val date = resultEntity.bpDataEntity?.let { "${it.year}-${it.mouth.toString().padStart(2, '0')}-${it.day.toString().padStart(2, '0')}" } ?: getDateForOffset(0)
                            val data = BloodPressureData(date = date, entries = entries)
                            bloodPressureCallback?.invoke(data)
                            // confirm deletion on device
                            commandHandle.executeReqCmd(BpReadConformReq(true), null)
                            addLog("Auto blood pressure synced: ${entries.size} entries")
                        } catch (e: Exception) { addLog("ERROR: BP auto processing: ${e.message}") }
                    }
                }
            )
        } catch (e: Exception) { addLog("ERROR: BP auto sync: ${e.message}") }
    }

    fun syncBloodPressureManual() {
        if (commandHandle == null) { addLog("ERROR: CommandHandle not initialized"); return }
        addLog("Syncing manual blood pressure")
        try {
            commandHandle.executeReqCmd(
                ReadPressureReq(0),
                object : ICommandResponse<ReadBlePressureRsp> {
                    override fun onDataResponse(resultEntity: ReadBlePressureRsp) {
                        try {
                            val list = resultEntity.valueList ?: emptyList()
                            val entries = list.map { BloodPressureEntry(timeMinute = 0, sbp = it.sbp, dbp = it.dbp, hr = 0) }
                            val data = BloodPressureData(date = getDateForOffset(0), entries = entries)
                            bloodPressureCallback?.invoke(data)
                            addLog("Manual blood pressure synced: ${entries.size} entries")
                        } catch (e: Exception) { addLog("ERROR: BP manual processing: ${e.message}") }
                    }
                }
            )
        } catch (e: Exception) { addLog("ERROR: BP manual sync: ${e.message}") }
    }

    // Blood Oxygen
    fun syncBloodOxygen() {
        addLog("Syncing blood oxygen")
        try {
            LargeDataHandler.getInstance().syncBloodOxygenWithCallback { list ->
                try {
                    if (list.isNullOrEmpty()) { addLog("Blood oxygen: no data"); return@syncBloodOxygenWithCallback }
                    list.forEach { e ->
                        val data = BloodOxygenData(
                            date = e.dateStr ?: getDateForOffset(0),
                            minArray = e.minArray ?: emptyList(),
                            maxArray = e.maxArray ?: emptyList(),
                            unixTime = e.unix_time
                        )
                        bloodOxygenCallback?.invoke(data)
                    }
                    addLog("Blood oxygen synced: ${list.size} day(s)")
                } catch (e: Exception) { addLog("ERROR: Blood oxygen processing: ${e.message}") }
            }
        } catch (e: Exception) { addLog("ERROR: Blood oxygen sync: ${e.message}") }
    }

    // Stress (Pressure)
    fun syncStress(offset: Int) {
        if (commandHandle == null) { addLog("ERROR: CommandHandle not initialized"); return }
        addLog("Syncing stress for offset: $offset")
        try {
            commandHandle.executeReqCmd(
                PressureReq(offset.toByte()),
                object : ICommandResponse<PressureRsp> {
                    override fun onDataResponse(resultEntity: PressureRsp) {
                        try {
                            val values = (resultEntity.pressureArray ?: byteArrayOf()).map { (it.toInt() and 0xFF) / 10 }
                            val data = StressData(
                                date = resultEntity.today?.let { String.format("%04d-%02d-%02d", it.year, it.month, it.day) } ?: getDateForOffset(offset),
                                rangeMinutes = resultEntity.range,
                                values = values
                            )
                            stressCallback?.invoke(data)
                            addLog("Stress synced: ${values.size} points")
                        } catch (e: Exception) { addLog("ERROR: Stress processing: ${e.message}") }
                    }
                }
            )
        } catch (e: Exception) { addLog("ERROR: Stress sync: ${e.message}") }
    }

    // HRV
    fun syncHrv(offset: Int) {
        if (commandHandle == null) { addLog("ERROR: CommandHandle not initialized"); return }
        addLog("Syncing HRV for offset: $offset")
        try {
            commandHandle.executeReqCmd(
                HRVReq(offset.toByte()),
                object : ICommandResponse<HRVRsp> {
                    override fun onDataResponse(resultEntity: HRVRsp) {
                        try {
                            val values = (resultEntity.hrvArray?: byteArrayOf()).map { (it.toInt() and 0xFF) / 10 }
                            val data = HrvData(
                                date = resultEntity.today?.let { String.format("%04d-%02d-%02d", it.year, it.month, it.day) } ?: getDateForOffset(offset),
                                rangeMinutes = resultEntity.range,
                                values = values
                            )
                            hrvCallback?.invoke(data)
                            addLog("HRV synced: ${values.size} points")
                        } catch (e: Exception) { addLog("ERROR: HRV processing: ${e.message}") }
                    }
                }
            )
        } catch (e: Exception) { addLog("ERROR: HRV sync: ${e.message}") }
    }

    // Temperature
    fun registerTempCallback() {
        try {
            FileHandle.getInstance().clearCallback()
            FileHandle.getInstance().registerCallback(TempCallback())
            FileHandle.getInstance().initRegister()
            addLog("Temperature callback registered")
        } catch (e: Exception) { addLog("ERROR: Register temp: ${e.message}") }
    }

    fun syncAutoTemperature(days: Int = 2) {
        try { FileHandle.getInstance().startObtainTemperatureSeries(days); addLog("Auto temperature sync started (days=$days)") } catch (e: Exception) { addLog("ERROR: Auto temp: ${e.message}") }
    }

    fun syncManualTemperature(days: Int = 0) {
        try { FileHandle.getInstance().startObtainTemperatureOnce(days); addLog("Manual temperature sync started (days=$days)") } catch (e: Exception) { addLog("ERROR: Manual temp: ${e.message}") }
    }

    inner class TempCallback : SimpleCallback() {
        override fun onUpdateTemperature(data: TemperatureEntity) {
            try { temperatureSeriesCallback?.invoke(TemperatureSeriesData(index = data.mIndex, timeSpanMinutes = data.mTimeSpan, values = data.mValues?.toList() ?: emptyList())) } catch (_: Exception) {}
        }
        override fun onUpdateTemperatureList(array: MutableList<TemperatureOnceEntity>) {
            try { temperatureOnceCallback?.invoke(array.map { TemperatureOnceData(time = it.mTime, value = it.mValue) }) } catch (_: Exception) {}
        }
    }

    // ================= Models for new data =================
    data class BloodPressureData(val date: String, val entries: List<BloodPressureEntry>)
    data class BloodPressureEntry(val timeMinute: Int, val sbp: Int, val dbp: Int, val hr: Int?)
    data class BloodOxygenData(val date: String, val minArray: List<Int>, val maxArray: List<Int>, val unixTime: Long)
    data class StressData(val date: String, val rangeMinutes: Int, val values: List<Int>)
    data class HrvData(val date: String, val rangeMinutes: Int, val values: List<Int>)
    data class TemperatureSeriesData(val index: Int, val timeSpanMinutes: Int, val values: List<Float>)
    data class TemperatureOnceData(val time: Long, val value: Float)
}