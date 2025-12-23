// ========================================
// COMMON MAIN - Data Classes
// File: commonMain/kotlin/dev/infa/sync/Models.kt
// ========================================
package dev.infa.page3.SDK.viewModel

import dev.infa.page3.SDK.data.*
import dev.infa.page3.data.remote.CacheManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


interface ISyncManager {

    /* -------------------- ACTIVITY -------------------- */

    /** Sync today total steps */
    suspend fun syncTodaySteps(): Int

    /** Sync steps by day offset (0=today, 1=yesterday...) */
    suspend fun syncStepsByOffset(offset: Int): DayStepData

    /** Sync detailed step data (96 points/day) */
    //suspend fun syncStepDetails(offset: Int): List<StepDetailData>

    /* -------------------- SLEEP -------------------- */

    /** Sync night sleep */
    //suspend fun syncSleep(offset: Int): SleepData

    /** Sync lunch / nap sleep if supported */
    //suspend fun syncLunchSleep(offset: Int): SleepData?

    /* -------------------- SEDENTARY -------------------- */

    /** Sync sedentary / long-sit data */
   // suspend fun syncSedentary(offset: Int): SedentaryData

    /* -------------------- HEART RATE -------------------- */

    /** Sync heart rate history (5 min interval, up to 3 days) */
    suspend fun syncHeartRate(offset: Int): HeartRateData

    /* -------------------- BLOOD OXYGEN -------------------- */

    /** Sync hourly SpO₂ (min/max per hour) */
    suspend fun syncSpO2(offset: Int): SpO2Data

    /** Sync interval SpO₂ (1-min resolution, 1440 points) */
//    suspend fun syncIntervalSpO2(offset: Int): IntervalSpO2Data

    /* -------------------- BLOOD PRESSURE -------------------- */

    /** Sync automatic blood pressure */
//    suspend fun syncAutoBloodPressure(offset: Int): BloodPressureData
//
//    /** Sync manual blood pressure */
//    suspend fun syncManualBloodPressure(): List<BloodPressureOnceData>

    /** Confirm BP sync (device clears synced records) */
    suspend fun confirmBloodPressureSync()

    /* -------------------- HRV -------------------- */

    /** Sync HRV data (30 min interval, up to 7 days) */
    suspend fun syncHrv(offset: Int): HrvData

    /* -------------------- STRESS / PRESSURE -------------------- */

    /** Sync stress / pressure data */
    suspend fun syncPressure(offset: Int): PressureData

    /* -------------------- TEMPERATURE -------------------- */

    /** Sync automatic skin/body temperature */
    suspend fun syncAutoTemperature(offset: Int): TemperatureData

    /** Sync manual temperature measurements */
//    suspend fun syncManualTemperature(): List<TemperatureOnceData>
}


interface IInstantMeasures {

    /** Manual heart rate measurement */
    suspend fun measureHeartRate(): String

    /** Manual blood oxygen measurement */
    suspend fun measureSpO2(): String

    /** Manual blood pressure measurement */
    suspend fun measureBloodPressure(): String

    /** Manual HRV measurement */
    suspend fun measureHrv(): String

    /** Manual stress / pressure measurement */
    suspend fun measurePressure(): String

    /** Manual skin/body temperature (value /10) */
    suspend fun measureTemperature(): String

    /** One-click measurement (HR + BP + SpO₂ + HRV + Temp) */
    suspend fun measureOneClick(): OneClickResult

    /** Raw heart rate PPG data */
    suspend fun measureHeartRateRawData(seconds: Int): RawDataResult

    /** Raw SpO₂ PPG data */
    suspend fun measureBloodOxygenRawData(seconds: Int): RawDataResult
}


interface IContinuousMonitoring {
    suspend fun toggleHeartRateMonitoring(enabled: Boolean, interval: Int): String
    suspend fun toggleHrvMonitoring(enabled: Boolean, interval: Int): String
    suspend fun toggleSpO2Monitoring(enabled: Boolean, interval: Int): String
    suspend fun toggleIntervalSpO2Monitoring(enabled: Boolean, interval: Int): String
    suspend fun toggleBloodPressureMonitoring(enabled: Boolean, startEndTime: StartEndTimeEntity, interval: Int): String
    suspend fun togglePressureMonitoring(enabled: Boolean): String
    suspend fun toggleTemperatureMonitoring(enabled: Boolean, interval: Int): String
}


class SyncViewModel(
    private val cache: CacheManager,
    private val syncManager: ISyncManager
)
{
    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _todaySteps = MutableStateFlow(0)
    val todaySteps: StateFlow<Int> = _todaySteps.asStateFlow()

    private val _hourlyData = MutableStateFlow<List<HourlyStepData>>(emptyList())
    val hourlyData: StateFlow<List<HourlyStepData>> = _hourlyData.asStateFlow()

    private val _weeklySummary = MutableStateFlow(WeeklySummary())
    val weeklySummary: StateFlow<WeeklySummary> = _weeklySummary.asStateFlow()

    private val _heartRateData = MutableStateFlow<HeartRateData?>(null)
    val heartRateData: StateFlow<HeartRateData?> = _heartRateData.asStateFlow()

    private val _spo2Data = MutableStateFlow<SpO2Data?>(null)
    val spo2Data: StateFlow<SpO2Data?> = _spo2Data.asStateFlow()

//    private val _intervalSpO2Data = MutableStateFlow<IntervalSpO2Data?>(null)
//    val intervalSpO2Data: StateFlow<IntervalSpO2Data?> = _intervalSpO2Data.asStateFlow()

    private val _hrvData = MutableStateFlow<HrvData?>(null)
    val hrvData: StateFlow<HrvData?> = _hrvData.asStateFlow()

//    private val _bloodPressureData = MutableStateFlow<BloodPressureData?>(null)
//    val bloodPressureData: StateFlow<BloodPressureData?> = _bloodPressureData.asStateFlow()

//    private val _manualBloodPressure = MutableStateFlow<List<BloodPressureOnceData>>(emptyList())
//    val manualBloodPressure: StateFlow<List<BloodPressureOnceData>> = _manualBloodPressure.asStateFlow()

    private val _pressureData = MutableStateFlow<PressureData?>(null)
    val pressureData: StateFlow<PressureData?> = _pressureData.asStateFlow()

    private val _temperatureData = MutableStateFlow<TemperatureData?>(null)
    val temperatureData: StateFlow<TemperatureData?> = _temperatureData.asStateFlow()

//    private val _manualTemperature = MutableStateFlow<List<TemperatureOnceData>>(emptyList())
//    val manualTemperature: StateFlow<List<TemperatureOnceData>> = _manualTemperature.asStateFlow()

    private val _sleepData = MutableStateFlow<SleepData?>(null)
    val sleepData: StateFlow<SleepData?> = _sleepData.asStateFlow()

    private val _lunchSleepData = MutableStateFlow<SleepData?>(null)
    val lunchSleepData: StateFlow<SleepData?> = _lunchSleepData.asStateFlow()

//    private val _sedentaryData = MutableStateFlow<SedentaryData?>(null)
//    val sedentaryData: StateFlow<SedentaryData?> = _sedentaryData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _autoSyncCompleted = MutableStateFlow(false)
    val autoSyncCompleted: StateFlow<Boolean> = _autoSyncCompleted.asStateFlow()

    init {
        loadCachedData()
    }

    private fun loadCachedData() {
        viewModelScope.launch {
            cache.getInt("today_steps")?.let { _todaySteps.value = it }
            cache.get<WeeklySummary>("weekly_summary")?.let { _weeklySummary.value = it }
        }
    }

    fun startAutoSync() {
        println("🚀 startAutoSync() called")

        if (_autoSyncCompleted.value) {
            println("⚠️ Auto sync already completed, returning early")
            return
        }

        viewModelScope.launch {
            println("🧵 Coroutine started on thread:")

            _isSyncing.value = true
            _isLoading.value = true
            println("🔄 Sync flags set: isSyncing=true, isLoading=true")

            withContext(Dispatchers.Default) {
                println("⚙️ Switched to Dispatchers.Default | Thread: ")

                try {
                    // -------- TODAY STEPS --------
                    println("👣 Syncing today's steps...")
                    val steps = syncManager.syncTodaySteps()
                    println("✅ Today's steps synced: $steps")

                    _todaySteps.value = steps
                    cache.saveInt("today_steps", steps)
                    println("💾 Cached today_steps = $steps")

                    // -------- LAST 7 DAYS STEPS --------
                    println("📅 Syncing last 7 days steps...")
                    val days = mutableListOf<DayStepData>()

                    for (i in 0..6) {
                        println("➡️ Syncing steps for day offset: $i")
                        val data = syncManager.syncStepsByOffset(i)
                        println("✅ Day $i data: $data")

                        days.add(data)
                        cache.save("day_$i", data)
                        println("💾 Cached day_$i")
                    }

                    println("📊 Calculating weekly summary...")
                    calculateWeeklySummary(days)
                    println("✅ Weekly summary calculated")

                    // -------- HEART RATE --------
                    println("❤️ Syncing heart rate...")
                    val hrData = syncManager.syncHeartRate(0)
                    println("✅ Heart rate data received: $hrData")

                    _heartRateData.value = hrData
                    cache.save("hr_0", hrData)
                    println("💾 Cached hr_0")

                    // -------- SPO2 (COMMENTED) --------
                    /*
                    println("🫁 Syncing SpO2...")
                    val spo2Data = syncManager.syncSpO2(0)
                    println("✅ SpO2 data received: $spo2Data")

                    _spo2Data.value = spo2Data
                    cache.save("spo2_0", spo2Data)
                    println("💾 Cached spo2_0")
                    */

                    // -------- HRV --------
                    println("📈 Syncing HRV...")
                    val hrvData = syncManager.syncHrv(0)
                    println("✅ HRV data received: $hrvData")

                    _hrvData.value = hrvData
                    cache.save("hrv_0", hrvData)
                    println("💾 Cached hrv_0")

                    _autoSyncCompleted.value = true
                    println("🎉 Auto sync completed successfully")

                } catch (e: Exception) {
                    println("❌ Auto-sync failed")
                    println("❌ Exception type: ${e::class.simpleName}")
                    println("❌ Message: ${e.message}")
                    println("❌ Stacktrace:")
                    e.printStackTrace()
                }
            }

            _isSyncing.value = false
            _isLoading.value = false
            println("🛑 Sync finished | isSyncing=false, isLoading=false")
        }
    }


    fun resetAutoSyncFlag() {
        _autoSyncCompleted.value = false
    }

    fun fetchTodaySteps(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!forceRefresh) {
                cache.getInt("today_steps")?.let {
                    _todaySteps.value = it
                    return@launch
                }
            }

            _isSyncing.value = true
            withContext(Dispatchers.Default) {
                try {
                    val steps = syncManager.syncTodaySteps()
                    _todaySteps.value = steps
                    cache.saveInt("today_steps", steps)
                } catch (e: Exception) {
                    println("❌ Error: ${e.message}")
                }
            }
            _isSyncing.value = false
        }
    }

    fun fetchStepsByOffset(offset: Int, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!forceRefresh) {
                cache.get<DayStepData>("day_$offset")?.let {
                    _todaySteps.value = it.totalSteps
                    _hourlyData.value = it.hourlyData
                    return@launch
                }
            }

            _isSyncing.value = true
            withContext(Dispatchers.Default) {
                try {
                    val data = syncManager.syncStepsByOffset(offset)
                    _todaySteps.value = data.totalSteps
                    _hourlyData.value = data.hourlyData
                    cache.save("day_$offset", data)
                } catch (e: Exception) {
                    println("❌ Error: ${e.message}")
                }
            }
            _isSyncing.value = false
        }
    }

    fun fetchLast7DaysData() {
        viewModelScope.launch {
            _isLoading.value = true
            val days = mutableListOf<DayStepData>()

            for (i in 0..6) {
                cache.get<DayStepData>("day_$i")?.let { days.add(it) }
            }
            if (days.size == 7) {
                calculateWeeklySummary(days)
                _isLoading.value = false
                return@launch
            }

            withContext(Dispatchers.Default) {
                days.clear()
                for (i in 0..6) {
                    try {
                        val data = syncManager.syncStepsByOffset(i)
                        days.add(data)
                        cache.save("day_$i", data)
                    } catch (e: Exception) {
                        println("❌ Error day $i: ${e.message}")
                    }
                }
                calculateWeeklySummary(days)
            }
            _isLoading.value = false
        }
    }

    fun syncHeartRateDataForDay(offset: Int, onSuccess: (HeartRateData) -> Unit, onError: (String) -> Unit, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!forceRefresh) {
                cache.get<HeartRateData>("hr_$offset")?.let {
                    _heartRateData.value = it
                    onSuccess(it)
                    return@launch
                }
            }

            _isSyncing.value = true
            withContext(Dispatchers.Default) {
                try {
                    val data = syncManager.syncHeartRate(offset)
                    _heartRateData.value = data
                    cache.save("hr_$offset", data)
                    onSuccess(data)
                } catch (e: Exception) {
                    onError("Error: ${e.message}")
                }
            }
            _isSyncing.value = false
        }
    }

    fun syncSpO2DataForDay(offset: Int, onSuccess: (SpO2Data) -> Unit, onError: (String) -> Unit, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!forceRefresh) {
                cache.get<SpO2Data>("spo2_$offset")?.let {
                    _spo2Data.value = it
                    onSuccess(it)
                    return@launch
                }
            }

            _isSyncing.value = true
            withContext(Dispatchers.Default) {
                try {
//                    val data = syncManager.syncSpO2(offset)
//                    _spo2Data.value = data
//                    cache.save("spo2_$offset", data)
//                    onSuccess(data)
                    _isSyncing.value = false
                } catch (e: Exception) {
                    onError("Error: ${e.message}")
                }
            }
            _isSyncing.value = false
        }
    }

//    fun syncIntervalSpO2DataForDay(offset: Int, onSuccess: (IntervalSpO2Data) -> Unit, onError: (String) -> Unit, forceRefresh: Boolean = false) {
//        viewModelScope.launch {
//            if (!forceRefresh) {
//                cache.get<IntervalSpO2Data>("interval_spo2_$offset")?.let {
//                    _intervalSpO2Data.value = it
//                    onSuccess(it)
//                    return@launch
//                }
//            }
//
//            _isSyncing.value = true
//            withContext(Dispatchers.Default) {
//                try {
//                    val data = syncManager.syncIntervalSpO2(offset)
//                    _intervalSpO2Data.value = data
//                    cache.save("interval_spo2_$offset", data)
//                    onSuccess(data)
//                } catch (e: Exception) {
//                    onError("Error: ${e.message}")
//                }
//            }
//            _isSyncing.value = false
//        }
//    }

    fun syncHrvDataForDay(offset: Int, onSuccess: (HrvData) -> Unit, onError: (String) -> Unit, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!forceRefresh) {
                cache.get<HrvData>("hrv_$offset")?.let {
                    _hrvData.value = it
                    onSuccess(it)
                    return@launch
                }
            }

            _isSyncing.value = true
            withContext(Dispatchers.Default) {
                try {
                    val data = syncManager.syncHrv(offset)
                    _hrvData.value = data
                    cache.save("hrv_$offset", data)
                    onSuccess(data)
                } catch (e: Exception) {
                    onError("Error: ${e.message}")
                }
            }
            _isSyncing.value = false
        }
    }
//
//    fun syncAutoBloodPressureForDay(offset: Int, onSuccess: (BloodPressureData) -> Unit, onError: (String) -> Unit, forceRefresh: Boolean = false) {
//        viewModelScope.launch {
//            if (!forceRefresh) {
//                cache.get<BloodPressureData>("bp_$offset")?.let {
//                    _bloodPressureData.value = it
//                    onSuccess(it)
//                    return@launch
//                }
//            }
//
//            _isSyncing.value = true
//            withContext(Dispatchers.Default) {
//                try {
//                    val data = syncManager.syncAutoBloodPressure(offset)
//                    _bloodPressureData.value = data
//                    cache.save("bp_$offset", data)
//                    onSuccess(data)
//                } catch (e: Exception) {
//                    onError("Error: ${e.message}")
//                }
//            }
//            _isSyncing.value = false
//        }
//    }

//    fun syncManualBloodPressure(onSuccess: (List<BloodPressureOnceData>) -> Unit, onError: (String) -> Unit) {
//        viewModelScope.launch {
//            _isSyncing.value = true
//            withContext(Dispatchers.Default) {
//                try {
//                    val data = syncManager.syncManualBloodPressure()
//                    _manualBloodPressure.value = data
//                    onSuccess(data)
//                } catch (e: Exception) {
//                    onError("Error: ${e.message}")
//                }
//            }
//            _isSyncing.value = false
//        }
//    }

    fun confirmBloodPressureSync(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                try {
                    syncManager.confirmBloodPressureSync()
                    onSuccess()
                } catch (e: Exception) {
                    onError("Error: ${e.message}")
                }
            }
        }
    }

    fun syncPressureDataForDay(offset: Int, onSuccess: (PressureData) -> Unit, onError: (String) -> Unit, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!forceRefresh) {
                cache.get<PressureData>("pressure_$offset")?.let {
                    _pressureData.value = it
                    onSuccess(it)
                    return@launch
                }
            }

            _isSyncing.value = true
            withContext(Dispatchers.Default) {
                try {
                    val data = syncManager.syncPressure(offset)
                    _pressureData.value = data
                    cache.save("pressure_$offset", data)
                    onSuccess(data)
                } catch (e: Exception) {
                    onError("Error: ${e.message}")
                }
            }
            _isSyncing.value = false
        }
    }

    fun syncAutoTemperatureForDay(offset: Int, onSuccess: (TemperatureData) -> Unit, onError: (String) -> Unit, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!forceRefresh) {
                cache.get<TemperatureData>("temp_$offset")?.let {
                    _temperatureData.value = it
                    onSuccess(it)
                    return@launch
                }
            }

            _isSyncing.value = true
            withContext(Dispatchers.Default) {
                try {
                    val data = syncManager.syncAutoTemperature(offset)
                    _temperatureData.value = data
                    cache.save("temp_$offset", data)
                    onSuccess(data)
                } catch (e: Exception) {
                    onError("Error: ${e.message}")
                }
            }
            _isSyncing.value = false
        }
    }

//    fun syncManualTemperature(onSuccess: (List<TemperatureOnceData>) -> Unit, onError: (String) -> Unit) {
//        viewModelScope.launch {
//            _isSyncing.value = true
//            withContext(Dispatchers.Default) {
//                try {
//                    val data = syncManager.syncManualTemperature()
//                    _manualTemperature.value = data
//                    onSuccess(data)
//                } catch (e: Exception) {
//                    onError("Error: ${e.message}")
//                }
//            }
//            _isSyncing.value = false
//        }
//    }

//    fun syncSleepDataForDay(offset: Int, onSuccess: (SleepData) -> Unit, onError: (String) -> Unit, forceRefresh: Boolean = false) {
//        viewModelScope.launch {
//            if (!forceRefresh) {
//                cache.get<SleepData>("sleep_$offset")?.let {
//                    _sleepData.value = it
//                    onSuccess(it)
//                    return@launch
//                }
//            }
//
//            _isSyncing.value = true
//            withContext(Dispatchers.Default) {
//                try {
//                    val data = syncManager.syncSleep(offset)
//                    _sleepData.value = data
//                    cache.save("sleep_$offset", data)
//                    onSuccess(data)
//                } catch (e: Exception) {
//                    onError("Error: ${e.message}")
//                }
//            }
//            _isSyncing.value = false
//        }
//    }

//    fun syncLunchSleepDataForDay(offset: Int, onSuccess: (SleepData?) -> Unit, onError: (String) -> Unit, forceRefresh: Boolean = false) {
//        viewModelScope.launch {
//            if (!forceRefresh) {
//                cache.get<SleepData>("lunch_sleep_$offset")?.let {
//                    _lunchSleepData.value = it
//                    onSuccess(it)
//                    return@launch
//                }
//            }
//
//            _isSyncing.value = true
//            withContext(Dispatchers.Default) {
//                try {
//                    val data = syncManager.syncLunchSleep(offset)
//                    _lunchSleepData.value = data
//                    data?.let { cache.save("lunch_sleep_$offset", it) }
//                    onSuccess(data)
//                } catch (e: Exception) {
//                    onError("Error: ${e.message}")
//                }
//            }
//            _isSyncing.value = false
//        }
//    }
//
//    fun syncSedentaryDataForDay(offset: Int, onSuccess: (SedentaryData) -> Unit, onError: (String) -> Unit, forceRefresh: Boolean = false) {
//        viewModelScope.launch {
//            if (!forceRefresh) {
//                cache.get<SedentaryData>("sedentary_$offset")?.let {
//                    _sedentaryData.value = it
//                    onSuccess(it)
//                    return@launch
//                }
//            }
//
//            _isSyncing.value = true
//            withContext(Dispatchers.Default) {
//                try {
//                    val data = syncManager.syncSedentary(offset)
//                    _sedentaryData.value = data
//                    cache.save("sedentary_$offset", data)
//                    onSuccess(data)
//                } catch (e: Exception) {
//                    onError("Error: ${e.message}")
//                }
//            }
//            _isSyncing.value = false
//        }
//    }

    private fun calculateWeeklySummary(days: List<DayStepData>) {
        val best = days.maxByOrNull { it.totalSteps }
        val avgSteps = days.map { it.totalSteps }.average().toInt()
        val avgCal = days.map { it.totalCalories }.average().toLong()

        val summary = WeeklySummary(best, avgSteps, avgCal, days)
        _weeklySummary.value = summary
        cache.save("weekly_summary", summary)
    }

    fun forceRefresh() {
        cache.clearAll()
        fetchTodaySteps(true)
        fetchLast7DaysData()
    }
}
