package dev.infa.page3.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oudmon.ble.base.bluetooth.BleOperateManager
import dev.infa.page3.models.HealthData
import com.oudmon.ble.base.communication.CommandHandle
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.rsp.ReadHeartRateRsp
import com.oudmon.ble.base.communication.rsp.BaseRspCmd
import com.oudmon.ble.base.communication.req.ReadHeartRateReq
import com.oudmon.ble.base.communication.req.HeartRateSettingReq
import com.oudmon.ble.base.communication.rsp.HeartRateSettingRsp
import dev.infa.page3.models.HealthSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class HeartRateViewModel(
    private val commandHandle: CommandHandle = CommandHandle.getInstance()
) : ViewModel()
{

    // Loading and error states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Today's synced heart rate data
    private val _todayHeart = MutableStateFlow<HeartRateData?>(null)
    val todayHeart: StateFlow<HeartRateData?> = _todayHeart.asStateFlow()

    // Live health data placeholder (kept for UI compatibility)
    private val _liveHealthData = MutableStateFlow(HealthData())
    val liveHealthData: StateFlow<HealthData> = _liveHealthData.asStateFlow()

    // Heart rate settings managed directly via SDK
    private val _healthSettings = MutableStateFlow(HealthSettings())
    val healthSettings: StateFlow<HealthSettings> = _healthSettings.asStateFlow()

    // Measurement states
    private val _isMeasuring = MutableStateFlow(false)
    val isMeasuring: StateFlow<Boolean> = _isMeasuring.asStateFlow()

    // Measurement progress (0-100)
    private val _measurementProgress = MutableStateFlow(0)
    val measurementProgress: StateFlow<Int> = _measurementProgress.asStateFlow()

    // Latest instant measurement result
    private val _instantHeartRate = MutableStateFlow<Int?>(null)
    val instantHeartRate: StateFlow<Int?> = _instantHeartRate.asStateFlow()

    // Measurement timer job
    private var measurementJob: Job? = null

    // Current heart rate (live from device or latest synced)
    val currentHeartRate: StateFlow<Int> = combine(
        _instantHeartRate,
        _liveHealthData,
        _todayHeart
    ) { instant, liveData, todayData ->
        when {
            instant != null && instant > 0 -> instant
            liveData.heartRate > 0 -> liveData.heartRate
            todayData != null && todayData.heartRateValues.isNotEmpty() ->
                todayData.heartRateValues.last().heartRate
            else -> 0
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // Statistics (dynamic from actual data)
    val averageHeartRate: StateFlow<Int> = _todayHeart
        .map { data -> data?.averageHeartRate ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val minHeartRate: StateFlow<Int> = _todayHeart
        .map { data -> data?.minHeartRate ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val maxHeartRate: StateFlow<Int> = _todayHeart
        .map { data -> data?.maxHeartRate ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // All heart rate readings from today (sorted by time)
    val allReadings: StateFlow<List<HeartRateEntry>> = _todayHeart
        .map { data ->
            (data?.heartRateValues ?: emptyList()).sortedBy { it.timestamp }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Interval readings at fixed 30-min intervals (12:30, 12:00, 11:30, etc.)
    val intervalReadings: StateFlow<List<HeartRateEntry>> = _todayHeart
        .map { data ->
            getFixedIntervalReadings(data?.heartRateValues ?: emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Monitoring status
    val isMonitoringEnabled: StateFlow<Boolean> = _healthSettings
        .map { settings -> settings.heartRateEnabled }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val monitoringInterval: StateFlow<Int> = _healthSettings
        .map { settings -> settings.heartRateInterval }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 30)

    init {
        setupDataCallbacks()
        loadInitialData()
    }

    /**
     * Get readings at fixed 30-minute intervals (12:30, 12:00, 11:30, etc.)
     */
    private fun getFixedIntervalReadings(readings: List<HeartRateEntry>): List<HeartRateEntry> {
        if (readings.isEmpty()) return emptyList()

        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        // Get current time rounded down to nearest 30-min mark
        val currentMinute = calendar.get(Calendar.MINUTE)
        val roundedMinute = if (currentMinute >= 30) 30 else 0
        calendar.set(Calendar.MINUTE, roundedMinute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val intervalReadings = mutableListOf<HeartRateEntry>()
        var targetTime = calendar.timeInMillis

        // Go back and collect readings at 30-min intervals
        for (i in 0 until 48) { // Last 24 hours (48 intervals of 30 min)
            // Find reading closest to this target time (within ±10 minutes)
            val closest = readings
                .filter { Math.abs(it.timestamp - targetTime) <= 10 * 60 * 1000 }
                .minByOrNull { Math.abs(it.timestamp - targetTime) }

            if (closest != null) {
                intervalReadings.add(closest)
            }

            // Move back 30 minutes
            targetTime -= 30 * 60 * 1000

            if (targetTime < readings.first().timestamp) break
        }

        return intervalReadings.reversed() // Oldest to newest
    }

    /**
     * Setup callbacks to receive live data from device
     */
    private fun setupDataCallbacks() {
        // No continuous poll from a core; live health data feed would be wired here if needed
    }

    /**
     * Load initial data from device
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                // Load current settings from device (inline)
                readHeartRateSettingsInline()
                delay(500)

                // Sync today's data (inline)
                withContext(Dispatchers.IO) { syncHeartRateDataInline() }
            } catch (e: Exception) {
                _error.value = "Failed to load data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Refresh all data from device
     */
    fun refresh() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                // Refresh settings (inline)
                readHeartRateSettingsInline()
                delay(300)

                // Refresh today's synced data (inline)
                withContext(Dispatchers.IO) { syncHeartRateDataInline() }
            } catch (e: Exception) {
                _error.value = "Refresh failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Measure heart rate once (instant measurement)
     * Takes approximately 30 seconds
     */
    fun measureHeartRateOnce() {
        _error.value = null
        _isMeasuring.value = true
        _measurementProgress.value = 0
        _instantHeartRate.value = null

        measurementJob?.cancel()
        measurementJob = viewModelScope.launch {
            try {
                Log.d("HeartRateVM", "Manual measurement started")

                BleOperateManager.getInstance().manualModeHeart({ result ->
                    val hrValue = result.value.toInt()
                    val errCode = result.errCode.toInt()
                    Log.d("HeartRateVM", "Manual HR callback: value=$hrValue, err=$errCode")

                    if (errCode == 0 && hrValue > 0) {
                        _instantHeartRate.value = hrValue
                        _liveHealthData.value = _liveHealthData.value.copy(heartRate = hrValue)
                        Log.d("HeartRateVM", "Manual HR measured successfully: $hrValue bpm")
                    } else {
                        Log.d("HeartRateVM", "Manual HR measurement failed or invalid")
                    }
                }, false)

                // Wait up to 30 seconds (typical measurement time)
                for (i in 1..30) {
                    delay(1000)
                    _measurementProgress.value = i * 100 / 30
                }

                // Stop measurement cleanly
                BleOperateManager.getInstance().manualModeHeart(null, true)
                Log.d("HeartRateVM", "Manual measurement stopped")

                // ✅ Do NOT pull todayHeart here — keep manual result visible
                if (_instantHeartRate.value == null || _instantHeartRate.value == 0) {
                    _error.value = "No valid heart rate detected"
                }
            } catch (e: Exception) {
                _error.value = "Measurement failed: ${e.message}"
                Log.e("HeartRateVM", "Error during manual measure", e)
            } finally {
                _isMeasuring.value = false
                _measurementProgress.value = 0
                Log.d("HeartRateVM", "Manual measurement finished")
            }
        }
    }


    /**
     * Stop current measurement
     */
    fun stopMeasurement() {
        measurementJob?.cancel()
        _isMeasuring.value = false
        _measurementProgress.value = 0
    }

    /**
     * Toggle continuous heart rate monitoring
     */
    fun toggleContinuousMonitoring(enabled: Boolean) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val interval = _healthSettings.value.heartRateInterval.takeIf { it > 0 } ?: 30
                toggleHeartRateInline(enabled, interval)
                delay(600)
            } catch (e: Exception) {
                _error.value = "Failed to toggle monitoring: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear instant measurement result
     */
    fun clearInstantMeasurement() {
        _instantHeartRate.value = null
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Get heart rate readings for specific time range
     */
    fun getReadingsForLastHours(hours: Int): List<HeartRateEntry> {
        val allValues = _todayHeart.value?.heartRateValues ?: emptyList()
        if (allValues.isEmpty()) return emptyList()

        val cutoffTime = System.currentTimeMillis() - (hours * 60 * 60 * 1000)
        return allValues.filter { it.timestamp >= cutoffTime }
    }

    override fun onCleared() {
        super.onCleared()
        measurementJob?.cancel()
    }

    // ================= Inline Data Sync Logic (from DataSynchronization) =================

    private fun syncHeartRateDataInline() {
        try {
            val nowWithTz = getCurrentTimeWithTimezone()
            commandHandle.executeReqCmd(
                ReadHeartRateReq(nowWithTz.toLong()),
                object : ICommandResponse<ReadHeartRateRsp> {
                    override fun onDataResponse(resultEntity: ReadHeartRateRsp) {
                        try {
                            if (resultEntity.status == BaseRspCmd.RESULT_OK) {
                                val data = convertHeartRateData(resultEntity)
                                _todayHeart.value = data
                            } else {
                                _error.value = "Failed to sync heart rate (status=${resultEntity.status})"
                            }
                        } catch (e: Exception) {
                            _error.value = "HR processing failed: ${e.message}"
                        }
                    }
                }
            )
        } catch (e: Exception) {
            _error.value = "HR sync error: ${e.message}"
        }
    }

    private fun convertHeartRateData(response: ReadHeartRateRsp): HeartRateData {
        val heartRateValues = mutableListOf<HeartRateEntry>()
        response.getmHeartRateArray()?.let { hrArray ->
            for (i in hrArray.indices) {
                if (hrArray[i].toInt() > 0) {
                    heartRateValues.add(
                        HeartRateEntry(
                            timestamp = response.getmUtcTime().toLong() + (i * 5 * 60),
                            heartRate = hrArray[i].toInt(),
                            minuteOfDay = i * 5
                        )
                    )
                }
            }
        }
        return HeartRateData(
            date = getDateForOffset(0),
            heartRateValues = heartRateValues,
            averageHeartRate = if (heartRateValues.isNotEmpty()) heartRateValues.map { it.heartRate }.average().toInt() else 0,
            maxHeartRate = heartRateValues.maxOfOrNull { it.heartRate } ?: 0,
            minHeartRate = heartRateValues.minOfOrNull { it.heartRate } ?: 0
        )
    }

    private fun getCurrentTimeWithTimezone(): Int {
        return try {
            val timeZone = java.util.TimeZone.getDefault().rawOffset / (1000 * 60 * 60)
            val currentTime = System.currentTimeMillis() / 1000
            (timeZone * 3600 + currentTime).toInt()
        } catch (_: Exception) { 0 }
    }

    private fun getDateForOffset(offset: Int): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -offset)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return format.format(calendar.time)
    }

    private fun readHeartRateSettingsInline() {
        try {
            commandHandle.executeReqCmd(
                HeartRateSettingReq.getReadInstance(),
                object : ICommandResponse<HeartRateSettingRsp> {
                    override fun onDataResponse(resultEntity: HeartRateSettingRsp) {
                        if (resultEntity.status == BaseRspCmd.RESULT_OK) {
                            _healthSettings.value = _healthSettings.value.copy(
                                heartRateEnabled = resultEntity.isEnable,
                                heartRateInterval = resultEntity.heartInterval
                            )
                        }
                    }
                }
            )
        } catch (_: Exception) { }
    }

    private fun toggleHeartRateInline(enabled: Boolean, interval: Int) {
        try {
            commandHandle.executeReqCmd(
                HeartRateSettingReq.getWriteInstance(enabled, interval),
                object : ICommandResponse<HeartRateSettingRsp> {
                    override fun onDataResponse(resultEntity: HeartRateSettingRsp) {
                        if (resultEntity.status == BaseRspCmd.RESULT_OK) {
                            _healthSettings.value = _healthSettings.value.copy(
                                heartRateEnabled = enabled,
                                heartRateInterval = interval
                            )
                        }
                    }
                }
            )
        } catch (_: Exception) { }
    }
}


