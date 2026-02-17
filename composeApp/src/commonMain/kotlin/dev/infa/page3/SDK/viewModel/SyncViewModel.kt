package dev.infa.page3.SDK.viewModel

import dev.infa.page3.SDK.data.*
import dev.infa.page3.SDK.server.HealthDataRepository
import dev.infa.page3.SDK.server.SyncProgress
import dev.infa.page3.SDK.ui.utils.DateInfo
import dev.infa.page3.SDK.ui.utils.DateUtils
import dev.infa.page3.data.remote.CacheManager
import dev.infa.page3.data.remote.getCurrentTimeMillis
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
    suspend fun measureOneClick() : OneClickResult

    /** Raw heart rate PPG data */
    suspend fun measureHeartRateRawData(seconds: Int): RawDataResult

    /** Raw SpO₂ PPG data */
    suspend fun measureBloodOxygenRawData(seconds: Int): RawDataResult
    suspend fun measureOneClickContinuous(
        onUpdate: (OneClickResult) -> Unit,
        onComplete: () -> Unit
    )
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
    private val syncManager: ISyncManager,
    private val healthRepository: HealthDataRepository,
    private val tokenProvider: () -> String?
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

    private val _hrvData = MutableStateFlow<HrvData?>(null)
    val hrvData: StateFlow<HrvData?> = _hrvData.asStateFlow()

    private val _pressureData = MutableStateFlow<PressureData?>(null)
    val pressureData: StateFlow<PressureData?> = _pressureData.asStateFlow()

    private val _temperatureData = MutableStateFlow<TemperatureData?>(null)
    val temperatureData: StateFlow<TemperatureData?> = _temperatureData.asStateFlow()

    private val _sleepData = MutableStateFlow<SleepData?>(null)
    val sleepData: StateFlow<SleepData?> = _sleepData.asStateFlow()

    private val _lunchSleepData = MutableStateFlow<SleepData?>(null)
    val lunchSleepData: StateFlow<SleepData?> = _lunchSleepData.asStateFlow()

    private val _bloodPressureData = MutableStateFlow<BpData?>(null)
    val bloodPressureData: StateFlow<BpData?> = _bloodPressureData.asStateFlow()

    private val _exerciseDataList = MutableStateFlow<List<ExerciseSummary>>(emptyList())
    val exerciseDataList: StateFlow<List<ExerciseSummary>> = _exerciseDataList.asStateFlow()

    private val _allHealthData = MutableStateFlow<AllHealthDataResponse?>(null)
    val allHealthData: StateFlow<AllHealthDataResponse?> = _allHealthData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _autoSyncCompleted = MutableStateFlow(false)
    val autoSyncCompleted: StateFlow<Boolean> = _autoSyncCompleted.asStateFlow()

    private val _serverSyncState = MutableStateFlow<ServerSyncState>(ServerSyncState.Idle)
    val serverSyncState: StateFlow<ServerSyncState> = _serverSyncState.asStateFlow()

    private val _batchSyncProgress = MutableStateFlow<SyncProgress?>(null)
    val batchSyncProgress: StateFlow<SyncProgress?> = _batchSyncProgress.asStateFlow()

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

                    syncStepsToServer(0, steps)

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

                        if (i > 0) {
                            syncStepsToServer(i, data.totalSteps, data)
                        }
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

                    hrData?.let { syncHeartRateToServer(0, it) }

                    // -------- HRV --------
                    println("📈 Syncing HRV...")
                    val hrvData = syncManager.syncHrv(0)
                    println("✅ HRV data received: $hrvData")

                    _hrvData.value = hrvData
                    cache.save("hrv_0", hrvData)
                    println("💾 Cached hrv_0")

                    hrvData?.let { syncHRVToServer(0, it) }

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

    // ======================== SERVER SYNC FUNCTIONS (POST) ========================

    /**
     * Sync step data to server
     */
    private suspend fun syncStepsToServer(
        offset: Int,
        totalSteps: Int,
        dayData: DayStepData? = null
    ) {
        val token = tokenProvider() ?: run {
            println("⚠️ No auth token available, skipping server sync")
            return
        }

        try {
            println("🌐 Syncing steps to server (offset: $offset)...")

            val date = healthRepository.timestampToDate(
                getCurrentTimeMillis() - (offset * 24 * 60 * 60 * 1000)
            )

            val hourlyData = dayData?.hourlyData?.map { sdkHourlyData ->
                HourlyStepData(
                    hour = sdkHourlyData.hour,
                    steps = sdkHourlyData.steps,
                    calories = sdkHourlyData.calories
                )
            } ?: emptyList()

            val result = healthRepository.syncStepData(
                token = token,
                date = date,
                totalSteps = dayData?.totalSteps ?: totalSteps,
                totalCalories = dayData?.totalCalories ?: 0,
                totalDistance = dayData?.totalDistance ?: 0,
                hourlyData = hourlyData
            )

            result.fold(
                onSuccess = { response ->
                    println("✅ Steps synced to server successfully (ID: ${response._id})")
                    _serverSyncState.value = ServerSyncState.Success("Steps synced")
                },
                onFailure = { error ->
                    println("❌ Failed to sync steps to server: ${error.message}")
                    _serverSyncState.value = ServerSyncState.Error(error.message ?: "Unknown error")
                }
            )
        } catch (e: Exception) {
            println("❌ Exception syncing steps to server: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Sync heart rate data to server
     */
    private suspend fun syncHeartRateToServer(offset: Int, hrData: HeartRateData) {
        val token = tokenProvider() ?: run {
            println("⚠️ No auth token available, skipping server sync")
            return
        }

        try {
            println("🌐 Syncing heart rate to server (offset: $offset)...")

            val date = healthRepository.timestampToDate(
                getCurrentTimeMillis() - (offset * 24 * 60 * 60 * 1000)
            )

            val heartRateValues = hrData.heartRateValues.map { sdkHrValue ->
                HeartRateValue(
                    timestamp = sdkHrValue.timestamp,
                    heartRate = sdkHrValue.heartRate,
                    minuteOfDay = healthRepository.getMinuteOfDay(sdkHrValue.timestamp)
                )
            }

            val result = healthRepository.syncHeartRateData(
                token = token,
                date = date,
                heartRateValues = heartRateValues,
                avgHR = hrData.averageHeartRate,
                maxHR = hrData.maxHeartRate,
                minHR = hrData.minHeartRate
            )

            result.fold(
                onSuccess = { response ->
                    println("✅ Heart rate synced to server successfully (ID: ${response._id})")
                    _serverSyncState.value = ServerSyncState.Success("Heart rate synced")
                },
                onFailure = { error ->
                    println("❌ Failed to sync heart rate to server: ${error.message}")
                    _serverSyncState.value = ServerSyncState.Error(error.message ?: "Unknown error")
                }
            )
        } catch (e: Exception) {
            println("❌ Exception syncing heart rate to server: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Sync HRV data to server
     */
    private suspend fun syncHRVToServer(offset: Int, hrvData: HrvData) {
        val token = tokenProvider() ?: run {
            println("⚠️ No auth token available, skipping server sync")
            return
        }

        try {
            println("🌐 Syncing HRV to server (offset: $offset)...")

            val date = healthRepository.timestampToDate(
                getCurrentTimeMillis() - (offset * 24 * 60 * 60 * 1000)
            )

            val hrvValues = hrvData.hrvValues.map { sdkHrvValue ->
                HRVValue(
                    timestamp = sdkHrvValue.timestamp,
                    hrvValue = sdkHrvValue.hrvValue,
                    minuteOfDay = healthRepository.getMinuteOfDay(sdkHrvValue.timestamp)
                )
            }

            val result = healthRepository.syncHRVData(
                token = token,
                date = date,
                hrvValues = hrvValues,
                avgHRV = hrvData.averageHrv,
                maxHRV = hrvData.maxHrv,
                minHRV = hrvData.minHrv
            )

            result.fold(
                onSuccess = { response ->
                    println("✅ HRV synced to server successfully (ID: ${response._id})")
                    _serverSyncState.value = ServerSyncState.Success("HRV synced")
                },
                onFailure = { error ->
                    println("❌ Failed to sync HRV to server: ${error.message}")
                    _serverSyncState.value = ServerSyncState.Error(error.message ?: "Unknown error")
                }
            )
        } catch (e: Exception) {
            println("❌ Exception syncing HRV to server: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Sync SpO2 data to server
     */
    private suspend fun syncSpO2ToServer(offset: Int, spo2Data: SpO2Data) {
        val token = tokenProvider() ?: run {
            println("⚠️ No auth token available, skipping server sync")
            return
        }

        try {
            println("🌐 Syncing SpO2 to server (offset: $offset)...")

            val date = healthRepository.timestampToDate(
                getCurrentTimeMillis() - (offset * 24 * 60 * 60 * 1000)
            )

            val spo2Values = spo2Data.spo2Values.map { sdkSpo2Value ->
                SpO2Value(
                    timestamp = sdkSpo2Value.timestamp,
                    spo2Value = sdkSpo2Value.spo2Value,
                    hourOfDay = healthRepository.getHourOfDay(sdkSpo2Value.timestamp)
                )
            }

            val result = healthRepository.syncSpO2Data(
                token = token,
                date = date,
                spo2Values = spo2Values,
                avgSpO2 = spo2Data.averageSpO2,
                maxSpO2 = spo2Data.maxSpO2,
                minSpO2 = spo2Data.minSpO2
            )

            result.fold(
                onSuccess = { response ->
                    println("✅ SpO2 synced to server successfully (ID: ${response._id})")
                    _serverSyncState.value = ServerSyncState.Success("SpO2 synced")
                },
                onFailure = { error ->
                    println("❌ Failed to sync SpO2 to server: ${error.message}")
                    _serverSyncState.value = ServerSyncState.Error(error.message ?: "Unknown error")
                }
            )
        } catch (e: Exception) {
            println("❌ Exception syncing SpO2 to server: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Sync Blood Pressure data to server
     */
    private suspend fun syncBloodPressureToServer(offset: Int, bpData: BpData) {
        val token = tokenProvider() ?: run {
            println("⚠️ No auth token available, skipping server sync")
            return
        }

        try {
            println("🌐 Syncing Blood Pressure to server (offset: $offset)...")

            val date = healthRepository.timestampToDate(
                getCurrentTimeMillis() - (offset * 24 * 60 * 60 * 1000)
            )

            val bpValues = bpData.bpValues.map { sdkBpValue ->
                BloodPressureValue(
                    timestamp = sdkBpValue.timestamp,
                    systolic = sdkBpValue.systolic,
                    diastolic = sdkBpValue.diastolic,
                    heartRate = sdkBpValue.heartRate,
                    minuteOfDay = sdkBpValue.minuteOfDay
                )
            }

            val result = healthRepository.syncBloodPressureData(
                token = token,
                date = date,
                bpValues = bpValues,
                avgSystolic = bpData.averageSystolic.toDouble(),
                avgDiastolic = bpData.averageDiastolic.toDouble(),
                maxSystolic = bpData.maxSystolic,
                minSystolic = bpData.minSystolic
            )

            result.fold(
                onSuccess = { response ->
                    println("✅ Blood Pressure synced to server successfully (ID: ${response._id})")
                    _serverSyncState.value = ServerSyncState.Success("Blood Pressure synced")
                },
                onFailure = { error ->
                    println("❌ Failed to sync Blood Pressure to server: ${error.message}")
                    _serverSyncState.value = ServerSyncState.Error(error.message ?: "Unknown error")
                }
            )
        } catch (e: Exception) {
            println("❌ Exception syncing Blood Pressure to server: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Sync Exercise data to server
     */
    private suspend fun syncExerciseToServer(exercise: ExerciseSummary) {
        val token = tokenProvider() ?: run {
            println("⚠️ No auth token available, skipping server sync")
            return
        }

        try {
            println("🌐 Syncing Exercise to server...")

            val result = healthRepository.syncExerciseData(
                token = token,
                date = exercise.date,
                sportType = exercise.sportType,
                sportName = exercise.sportName,
                startTimestamp = exercise.startTimestamp,
                durationSeconds = exercise.durationSeconds,
                distanceMeters = exercise.distanceMeters,
                calories = exercise.calories,
                avgHeartRate = exercise.averageHeartRate,
                steps = exercise.steps
            )

            result.fold(
                onSuccess = { response ->
                    println("✅ Exercise synced to server successfully (ID: ${response._id})")
                    _serverSyncState.value = ServerSyncState.Success("Exercise synced")
                },
                onFailure = { error ->
                    println("❌ Failed to sync Exercise to server: ${error.message}")
                    _serverSyncState.value = ServerSyncState.Error(error.message ?: "Unknown error")
                }
            )
        } catch (e: Exception) {
            println("❌ Exception syncing Exercise to server: ${e.message}")
            e.printStackTrace()
        }
    }

    // ======================== SERVER FETCH FUNCTIONS (GET) ========================

    /**
     * Fetch step data from server
     */
    fun fetchStepDataFromServer(date: String, onSuccess: (StepDataResponse) -> Unit, onError: (String) -> Unit) {
        val token = tokenProvider() ?: run {
            onError("No auth token available")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.Default) {
                try {
                    val result = healthRepository.fetchStepData(token, date)
                    result.fold(
                        onSuccess = { response ->
                            println("✅ Fetched step data from server: ${response._id}")
                            onSuccess(response)
                        },
                        onFailure = { error ->
                            println("❌ Failed to fetch step data: ${error.message}")
                            onError(error.message ?: "Unknown error")
                        }
                    )
                } catch (e: Exception) {
                    println("❌ Exception fetching step data: ${e.message}")
                    onError(e.message ?: "Unknown error")
                }
            }
            _isLoading.value = false
        }
    }

    /**
     * Fetch heart rate data from server
     */
    fun fetchHeartRateDataFromServer(date: String, onSuccess: (HeartRateDataResponse) -> Unit, onError: (String) -> Unit) {
        val token = tokenProvider() ?: run {
            onError("No auth token available")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.Default) {
                try {
                    val result = healthRepository.fetchHeartRateData(token, date)
                    result.fold(
                        onSuccess = { response ->
                            println("✅ Fetched heart rate data from server: ${response._id}")
                            onSuccess(response)
                        },
                        onFailure = { error ->
                            println("❌ Failed to fetch heart rate data: ${error.message}")
                            onError(error.message ?: "Unknown error")
                        }
                    )
                } catch (e: Exception) {
                    println("❌ Exception fetching heart rate data: ${e.message}")
                    onError(e.message ?: "Unknown error")
                }
            }
            _isLoading.value = false
        }
    }

    /**
     * Fetch SpO2 data from server
     */
    fun fetchSpO2DataFromServer(date: String, onSuccess: (SpO2DataResponse) -> Unit, onError: (String) -> Unit) {
        val token = tokenProvider() ?: run {
            onError("No auth token available")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.Default) {
                try {
                    val result = healthRepository.fetchSpO2Data(token, date)
                    result.fold(
                        onSuccess = { response ->
                            println("✅ Fetched SpO2 data from server: ${response._id}")
                            onSuccess(response)
                        },
                        onFailure = { error ->
                            println("❌ Failed to fetch SpO2 data: ${error.message}")
                            onError(error.message ?: "Unknown error")
                        }
                    )
                } catch (e: Exception) {
                    println("❌ Exception fetching SpO2 data: ${e.message}")
                    onError(e.message ?: "Unknown error")
                }
            }
            _isLoading.value = false
        }
    }

    /**
     * Fetch HRV data from server
     */
    fun fetchHRVDataFromServer(date: String, onSuccess: (HRVDataResponse) -> Unit, onError: (String) -> Unit) {
        val token = tokenProvider() ?: run {
            onError("No auth token available")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.Default) {
                try {
                    val result = healthRepository.fetchHRVData(token, date)
                    result.fold(
                        onSuccess = { response ->
                            println("✅ Fetched HRV data from server: ${response._id}")
                            onSuccess(response)
                        },
                        onFailure = { error ->
                            println("❌ Failed to fetch HRV data: ${error.message}")
                            onError(error.message ?: "Unknown error")
                        }
                    )
                } catch (e: Exception) {
                    println("❌ Exception fetching HRV data: ${e.message}")
                    onError(e.message ?: "Unknown error")
                }
            }
            _isLoading.value = false
        }
    }

    /**
     * Fetch blood pressure data from server
     */
    fun fetchBloodPressureDataFromServer(date: String, onSuccess: (BloodPressureDataResponse) -> Unit, onError: (String) -> Unit) {
        val token = tokenProvider() ?: run {
            onError("No auth token available")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.Default) {
                try {
                    val result = healthRepository.fetchBloodPressureData(token, date)
                    result.fold(
                        onSuccess = { response ->
                            println("✅ Fetched blood pressure data from server: ${response._id}")
                            onSuccess(response)
                        },
                        onFailure = { error ->
                            println("❌ Failed to fetch blood pressure data: ${error.message}")
                            onError(error.message ?: "Unknown error")
                        }
                    )
                } catch (e: Exception) {
                    println("❌ Exception fetching blood pressure data: ${e.message}")
                    onError(e.message ?: "Unknown error")
                }
            }
            _isLoading.value = false
        }
    }

    /**
     * Fetch exercise data from server
     */
    fun fetchExerciseDataFromServer(date: String, onSuccess: (List<ExerciseDataResponse>) -> Unit, onError: (String) -> Unit) {
        val token = tokenProvider() ?: run {
            onError("No auth token available")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.Default) {
                try {
                    val result = healthRepository.fetchExerciseData(token, date)
                    result.fold(
                        onSuccess = { response ->
                            println("✅ Fetched ${response.size} exercise(s) from server")
                            onSuccess(response)
                        },
                        onFailure = { error ->
                            println("❌ Failed to fetch exercise data: ${error.message}")
                            onError(error.message ?: "Unknown error")
                        }
                    )
                } catch (e: Exception) {
                    println("❌ Exception fetching exercise data: ${e.message}")
                    onError(e.message ?: "Unknown error")
                }
            }
            _isLoading.value = false
        }
    }

    /**
     * Fetch all health data from server for a specific date
     */
    fun fetchAllHealthDataFromServer(date: String, onSuccess: (AllHealthDataResponse) -> Unit, onError: (String) -> Unit) {
        val token = tokenProvider() ?: run {
            onError("No auth token available")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.Default) {
                try {
                    val result = healthRepository.fetchAllHealthData(token, date)
                    result.fold(
                        onSuccess = { response ->
                            println("✅ Fetched all health data from server")
                            _allHealthData.value = response
                            onSuccess(response)
                        },
                        onFailure = { error ->
                            println("❌ Failed to fetch all health data: ${error.message}")
                            onError(error.message ?: "Unknown error")
                        }
                    )
                } catch (e: Exception) {
                    println("❌ Exception fetching all health data: ${e.message}")
                    onError(e.message ?: "Unknown error")
                }
            }
            _isLoading.value = false
        }
    }

    // ======================== BATCH SYNC FUNCTION ========================

    /**
     * Sync all health data for a specific date with progress updates
     */
    fun syncAllHealthDataToServer(
        date: String,
        stepData: SaveStepDataRequest? = null,
        heartRateData: SaveHeartRateRequest? = null,
        spo2Data: SaveSpO2Request? = null,
        hrvData: SaveHRVRequest? = null,
        bpData: SaveBloodPressureRequest? = null,
        exerciseData: List<SaveExerciseRequest>? = null,
        onProgress: (SyncProgress) -> Unit
    ) {
        val token = tokenProvider() ?: run {
            println("⚠️ No auth token available")
            onProgress(SyncProgress.Completed(0, 1))
            return
        }

        viewModelScope.launch {
            _isSyncing.value = true

            healthRepository.syncAllHealthData(
                token = token,
                date = date,
                stepData = stepData,
                heartRateData = heartRateData,
                spo2Data = spo2Data,
                hrvData = hrvData,
                bpData = bpData,
                exerciseData = exerciseData
            ).collect { progress ->
                _batchSyncProgress.value = progress
                onProgress(progress)

                when (progress) {
                    is SyncProgress.Completed -> {
                        println("✅ Batch sync completed: ${progress.successCount} successful, ${progress.failureCount} failed")
                        _isSyncing.value = false
                    }
                    is SyncProgress.ItemCompleted -> {
                        if (progress.success) {
                            println("✅ ${progress.itemName} synced")
                        } else {
                            println("❌ ${progress.itemName} failed: ${progress.error}")
                        }
                    }
                    is SyncProgress.Started -> {
                        println("🔄 Starting batch sync of ${progress.totalItems} items")
                    }
                }
            }
        }
    }

    // ======================== EXISTING FUNCTIONS WITH SERVER SYNC ========================

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

                    syncStepsToServer(0, steps)
                } catch (e: Exception) {
                    println("❌ Error: ${e.message}")
                }
            }
            _isSyncing.value = false
        }
    }

    fun fetchStepsByOffset(offset: Int, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            // If offset > 7, fetch from server instead of device
            if (offset > 7) {
                fetchStepsByOffsetFromServer(offset)
                return@launch
            }

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

                    syncStepsToServer(offset, data.totalSteps, data)
                } catch (e: Exception) {
                    println("❌ Error: ${e.message}")
                }
            }
            _isSyncing.value = false
        }
    }

    /**
     * Fetch steps from server when offset > 7 (device only stores 7 days)
     */
    private fun fetchStepsByOffsetFromServer(offset: Int) {
        val token = tokenProvider() ?: run {
            println("⚠️ No auth token available")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.Default) {
                try {
                    val date = healthRepository.timestampToDate(
                        getCurrentTimeMillis() - (offset * 24 * 60 * 60 * 1000)
                    )

                    val result = healthRepository.fetchStepData(token, date)
                    result.fold(
                        onSuccess = { response ->
                            println("✅ Fetched step data from server for offset $offset")
                            _todaySteps.value = response.totalSteps
                            _hourlyData.value = response.hourlyData

                            // Cache the server data
                            val dayData = DayStepData(
                                date = response.date,
                                totalSteps = response.totalSteps,
                                totalCalories = response.totalCalories,
                                totalDistance = response.totalDistance,
                                hourlyData = response.hourlyData
                            )
                            cache.save("day_$offset", dayData)
                        },
                        onFailure = { error ->
                            println("❌ Failed to fetch step data from server: ${error.message}")
                        }
                    )
                } catch (e: Exception) {
                    println("❌ Exception fetching steps from server: ${e.message}")
                }
            }
            _isLoading.value = false
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

                        syncStepsToServer(i, data.totalSteps, data)
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
            // If offset > 7, fetch from server instead of device
            if (offset > 7) {
                syncHeartRateFromServer(offset, onSuccess, onError)
                return@launch
            }

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

                    syncHeartRateToServer(offset, data)

                    onSuccess(data)
                } catch (e: Exception) {
                    onError("Error: ${e.message}")
                }
            }
            _isSyncing.value = false
        }
    }

    /**
     * Fetch heart rate from server when offset > 7
     */
    private fun syncHeartRateFromServer(offset: Int, onSuccess: (HeartRateData) -> Unit, onError: (String) -> Unit) {
        val token = tokenProvider() ?: run {
            onError("No auth token available")
            return
        }

        viewModelScope.launch {
            _isSyncing.value = true
            withContext(Dispatchers.Default) {
                try {
                    val date = healthRepository.timestampToDate(
                        getCurrentTimeMillis() - (offset * 24 * 60 * 60 * 1000)
                    )

                    val result = healthRepository.fetchHeartRateData(token, date)
                    result.fold(
                        onSuccess = { response ->
                            println("✅ Fetched heart rate from server for offset $offset")

                            // Convert API response to local data model
                            val heartRateData = HeartRateData(
                                date = response.date,
                                heartRateValues = response.heartRateValues.map { apiValue ->
                                    HeartRateEntry(
                                        timestamp = apiValue.timestamp,
                                        heartRate = apiValue.heartRate,
                                        minuteOfDay = apiValue.minuteOfDay
                                    )
                                },
                                averageHeartRate = response.averageHeartRate,
                                maxHeartRate = response.maxHeartRate,
                                minHeartRate = response.minHeartRate
                            )

                            _heartRateData.value = heartRateData
                            cache.save("hr_$offset", heartRateData)
                            onSuccess(heartRateData)
                        },
                        onFailure = { error ->
                            println("❌ Failed to fetch heart rate from server: ${error.message}")
                            onError(error.message ?: "Unknown error")
                        }
                    )
                } catch (e: Exception) {
                    println("❌ Exception fetching heart rate from server: ${e.message}")
                    onError(e.message ?: "Unknown error")
                }
            }
            _isSyncing.value = false
        }
    }

    fun syncSpO2DataForDay(offset: Int, onSuccess: (SpO2Data) -> Unit, onError: (String) -> Unit, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            // If offset > 7, fetch from server instead of device
            if (offset > 7) {
                syncSpO2FromServer(offset, onSuccess, onError)
                return@launch
            }

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
                    val data = syncManager.syncSpO2(offset)
                    _spo2Data.value = data
                    cache.save("spo2_$offset", data)

                    syncSpO2ToServer(offset, data)

                    onSuccess(data)
                } catch (e: Exception) {
                    onError("Error: ${e.message}")
                }
            }
            _isSyncing.value = false
        }
    }

    /**
     * Fetch SpO2 from server when offset > 7
     */
    private fun syncSpO2FromServer(offset: Int, onSuccess: (SpO2Data) -> Unit, onError: (String) -> Unit) {
        val token = tokenProvider() ?: run {
            onError("No auth token available")
            return
        }

        viewModelScope.launch {
            _isSyncing.value = true
            withContext(Dispatchers.Default) {
                try {
                    val date = healthRepository.timestampToDate(
                        getCurrentTimeMillis() - (offset * 24 * 60 * 60 * 1000)
                    )

                    val result = healthRepository.fetchSpO2Data(token, date)
                    result.fold(
                        onSuccess = { response ->
                            println("✅ Fetched SpO2 from server for offset $offset")

                            // Convert API response to local data model
                            val spo2Data = SpO2Data(
                                date = response.date,
                                spo2Values = response.spo2Values.map { apiValue ->
                                    SpO2Entry(
                                        timestamp = apiValue.timestamp,
                                        spo2Value = apiValue.spo2Value,
                                        hourOfDay = apiValue.hourOfDay
                                    )
                                },
                                averageSpO2 = response.averageSpO2.toInt(),
                                maxSpO2 = response.maxSpO2,
                                minSpO2 = response.minSpO2
                            )

                            _spo2Data.value = spo2Data
                            cache.save("spo2_$offset", spo2Data)
                            onSuccess(spo2Data)
                        },
                        onFailure = { error ->
                            println("❌ Failed to fetch SpO2 from server: ${error.message}")
                            onError(error.message ?: "Unknown error")
                        }
                    )
                } catch (e: Exception) {
                    println("❌ Exception fetching SpO2 from server: ${e.message}")
                    onError(e.message ?: "Unknown error")
                }
            }
            _isSyncing.value = false
        }
    }

    fun syncHrvDataForDay(offset: Int, onSuccess: (HrvData) -> Unit, onError: (String) -> Unit, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            // If offset > 7, fetch from server instead of device
            if (offset > 7) {
                syncHrvFromServer(offset, onSuccess, onError)
                return@launch
            }

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

                    syncHRVToServer(offset, data)

                    onSuccess(data)
                } catch (e: Exception) {
                    onError("Error: ${e.message}")
                }
            }
            _isSyncing.value = false
        }
    }

    /**
     * Fetch HRV from server when offset > 7
     */
    private fun syncHrvFromServer(offset: Int, onSuccess: (HrvData) -> Unit, onError: (String) -> Unit) {
        val token = tokenProvider() ?: run {
            onError("No auth token available")
            return
        }

        viewModelScope.launch {
            _isSyncing.value = true
            withContext(Dispatchers.Default) {
                try {
                    val date = healthRepository.timestampToDate(
                        getCurrentTimeMillis() - (offset * 24 * 60 * 60 * 1000)
                    )

                    val result = healthRepository.fetchHRVData(token, date)
                    result.fold(
                        onSuccess = { response ->
                            println("✅ Fetched HRV from server for offset $offset")

                            // Convert API response to local data model
                            val hrvData = HrvData(
                                date = response.date,
                                hrvValues = response.hrvValues.map { apiValue ->
                                    HrvEntry(
                                        timestamp = apiValue.timestamp,
                                        hrvValue = apiValue.hrvValue,
                                        minuteOfDay = apiValue.minuteOfDay
                                    )
                                },
                                averageHrv = response.averageHrv.toInt(),
                                maxHrv = response.maxHrv,
                                minHrv = response.minHrv
                            )

                            _hrvData.value = hrvData
                            cache.save("hrv_$offset", hrvData)
                            onSuccess(hrvData)
                        },
                        onFailure = { error ->
                            println("❌ Failed to fetch HRV from server: ${error.message}")
                            onError(error.message ?: "Unknown error")
                        }
                    )
                } catch (e: Exception) {
                    println("❌ Exception fetching HRV from server: ${e.message}")
                    onError(e.message ?: "Unknown error")
                }
            }
            _isSyncing.value = false
        }
    }

    fun syncBloodPressureDataForDay(offset: Int, onSuccess: (BpData) -> Unit, onError: (String) -> Unit, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            // If offset > 7, fetch from server instead of device
            if (offset > 7) {
                syncBloodPressureFromServer(offset, onSuccess, onError)
                return@launch
            }

            if (!forceRefresh) {
                cache.get<BpData>("bp_$offset")?.let {
                    _bloodPressureData.value = it
                    onSuccess(it)
                    return@launch
                }
            }

            _isSyncing.value = true
            withContext(Dispatchers.Default) {
                try {
                    // Note: You'll need to add syncBloodPressure to ISyncManager
                    // For now, commenting out as it's not in the interface
                    // val data = syncManager.syncBloodPressure(offset)
                    // _bloodPressureData.value = data
                    // cache.save("bp_$offset", data)

                    // syncBloodPressureToServer(offset, data)

                    // onSuccess(data)

                    onError("Blood pressure sync not yet implemented in ISyncManager")
                } catch (e: Exception) {
                    onError("Error: ${e.message}")
                }
            }
            _isSyncing.value = false
        }
    }

    /**
     * Fetch Blood Pressure from server when offset > 7
     */
    private fun syncBloodPressureFromServer(offset: Int, onSuccess: (BpData) -> Unit, onError: (String) -> Unit) {
        val token = tokenProvider() ?: run {
            onError("No auth token available")
            return
        }

        viewModelScope.launch {
            _isSyncing.value = true
            withContext(Dispatchers.Default) {
                try {
                    val date = healthRepository.timestampToDate(
                        getCurrentTimeMillis() - (offset * 24 * 60 * 60 * 1000)
                    )

                    val result = healthRepository.fetchBloodPressureData(token, date)
                    result.fold(
                        onSuccess = { response ->
                            println("✅ Fetched Blood Pressure from server for offset $offset")

                            // Convert API response to local data model
                            val bpData = BpData(
                                date = response.date,
                                bpValues = response.bpValues.map { apiValue ->
                                    BpEntry(
                                        timestamp = apiValue.timestamp,
                                        heartRate = apiValue.heartRate,
                                        systolic = apiValue.systolic,
                                        diastolic = apiValue.diastolic,
                                        minuteOfDay = apiValue.minuteOfDay
                                    )
                                },
                                averageSystolic = response.averageSystolic.toInt(),
                                averageDiastolic = response.averageDiastolic.toInt(),
                                maxSystolic = response.maxSystolic,
                                minSystolic = response.minSystolic
                            )

                            _bloodPressureData.value = bpData
                            cache.save("bp_$offset", bpData)
                            onSuccess(bpData)
                        },
                        onFailure = { error ->
                            println("❌ Failed to fetch Blood Pressure from server: ${error.message}")
                            onError(error.message ?: "Unknown error")
                        }
                    )
                } catch (e: Exception) {
                    println("❌ Exception fetching Blood Pressure from server: ${e.message}")
                    onError(e.message ?: "Unknown error")
                }
            }
            _isSyncing.value = false
        }
    }

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

// ======================== SERVER SYNC STATE ========================

sealed class ServerSyncState {
    object Idle : ServerSyncState()
    data class Syncing(val message: String) : ServerSyncState()
    data class Success(val message: String) : ServerSyncState()
    data class Error(val message: String) : ServerSyncState()
}