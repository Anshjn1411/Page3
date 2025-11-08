package dev.infa.page3.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.communication.CommandHandle
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.req.PressureReq
import com.oudmon.ble.base.communication.req.PressureSettingReq
import com.oudmon.ble.base.communication.rsp.PressureRsp
import com.oudmon.ble.base.communication.rsp.PressureSettingRsp
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*

data class StressPoint(
    val timestamp: Long = 0L,
    val level: Int = 0
)

class StressViewModel(
    private val commandHandle: CommandHandle = CommandHandle.getInstance()
) : ViewModel() {

    companion object {
        private const val TAG = "StressViewModel"
        private const val MEASUREMENT_DURATION_MS = 30000L
        private const val INTERVAL_MINUTES = 30
        private val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    }

    // UI State
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _isMeasuring = MutableStateFlow(false)
    val isMeasuring = _isMeasuring.asStateFlow()

    private val _measurementProgress = MutableStateFlow(0)
    val measurementProgress = _measurementProgress.asStateFlow()

    private val _instantStress = MutableStateFlow<Int?>(null)
    val instantStress = _instantStress.asStateFlow()

    // Data State
    private val _readings = MutableStateFlow<List<StressPoint>>(emptyList())
    val allReadings = _readings.asStateFlow()

    private val _isMonitoringEnabled = MutableStateFlow(false)
    val isMonitoringEnabled = _isMonitoringEnabled.asStateFlow()

    private var measurementJob: Job? = null

    // Computed Properties
    val currentStress: StateFlow<Int> = combine(
        _instantStress,
        _readings,
        _isMeasuring
    ) { instant, readings, measuring ->
        when {
            measuring -> instant ?: 0
            instant != null && instant > 0 -> instant
            readings.isNotEmpty() -> {
                readings.filter { it.level > 0 }
                    .maxByOrNull { it.timestamp }?.level ?: 0
            }
            else -> 0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val averageStress: StateFlow<Int> = _readings.map { readings ->
        calculateAverage(readings.filter { it.level > 0 })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val minStress: StateFlow<Int> = _readings.map { readings ->
        readings.filter { it.level > 0 }.minOfOrNull { it.level } ?: 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val maxStress: StateFlow<Int> = _readings.map { readings ->
        readings.filter { it.level > 0 }.maxOfOrNull { it.level } ?: 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val intervalReadings: StateFlow<List<StressPoint>> = _readings.map { readings ->
        getFixedIntervalReadings(readings)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        Log.d(TAG, "Initializing StressViewModel")
        refresh()
    }

    fun refresh(offset: Int = 0) {
        Log.d(TAG, "Refreshing stress data with offset=$offset")
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                withContext(Dispatchers.IO) {
                    syncStressData(offset)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing data", e)
                _error.value = "Failed to load data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun measureStressOnce() {
        Log.d(TAG, "Starting stress manual measurement (single reading)")
        measurementJob?.cancel()
        measurementJob = viewModelScope.launch {
            try {
                _isMeasuring.value = true
                _measurementProgress.value = 0
                _error.value = null
                _instantStress.value = null

                var measurementComplete = false

                // Start manual stress/pressure measurement using BLE
                Log.d(TAG, "Initiating BLE stress measurement...")
                BleOperateManager.getInstance().manualModePressure({ result ->
                    val stressValue = result.value
                    Log.d(TAG, "Stress reading received: $stressValue")

                    // Only process valid readings
                    if (stressValue > 0 && !measurementComplete) {
                        measurementComplete = true
                        viewModelScope.launch {
                            _instantStress.value = stressValue
                            _measurementProgress.value = 100
                            delay(500) // Show complete state briefly
                            _isMeasuring.value = false
                            syncStressData(0)
                        }
                    }
                }, false)

                // Simulate measurement progress (runs independently of callback)
                val startTime = System.currentTimeMillis()
                var lastProgress = 0

                while (isActive && !measurementComplete) {
                    val elapsed = System.currentTimeMillis() - startTime
                    val progress = ((elapsed * 100) / MEASUREMENT_DURATION_MS).toInt().coerceAtMost(99)

                    // Only update if progress changed
                    if (progress != lastProgress) {
                        _measurementProgress.value = progress
                        lastProgress = progress
                    }

                    delay(300)

                    // Timeout after 30 seconds
                    if (elapsed >= MEASUREMENT_DURATION_MS) {
                        Log.w(TAG, "Measurement timeout reached")
                        if (!measurementComplete) {
                            _error.value = "Measurement timeout - please try again"
                            _isMeasuring.value = false
                            _measurementProgress.value = 0
                        }
                        break
                    }
                }

                Log.d(TAG, "Stress measurement loop finished")

            } catch (e: Exception) {
                Log.e(TAG, "Error during stress measurement", e)
                _error.value = "Measurement failed: ${e.message}"
                _isMeasuring.value = false
                _measurementProgress.value = 0
            }
        }
    }

    fun stopMeasurement() {
        Log.d(TAG, "Stopping measurement")
        measurementJob?.cancel()
        _isMeasuring.value = false
        _measurementProgress.value = 0
        _instantStress.value = null
    }

    fun toggleContinuousMonitoring(enabled: Boolean) {
        Log.d(TAG, "Toggling continuous monitoring: $enabled")
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                withContext(Dispatchers.IO) {
                    toggleStressMonitoring(enabled)
                }

                _isMonitoringEnabled.value = enabled
                Log.d(TAG, "Continuous monitoring ${if (enabled) "enabled" else "disabled"}")
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling monitoring", e)
                _error.value = "Failed to toggle monitoring: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Private helper functions
    private fun syncStressData(offset: Int) {
        Log.d(TAG, "Syncing stress data with offset=$offset")
        try {
            commandHandle.executeReqCmd(
                PressureReq(offset.toByte()),
                object : ICommandResponse<PressureRsp> {
                    override fun onDataResponse(resultEntity: PressureRsp) {
                        Log.d(TAG, "Received stress response: range=${resultEntity.range}, dataSize=${resultEntity.pressureArray?.size}")
                        try {
                            val newReadings = parseStressResponse(resultEntity)
                            Log.d(TAG, "Parsed ${newReadings.size} readings (${newReadings.count { it.level > 0 }} valid)")

                            // Merge with existing readings and remove duplicates
                            val allReadings = (_readings.value + newReadings)
                                .distinctBy { it.timestamp }
                                .sortedBy { it.timestamp }

                            _readings.value = allReadings
                            Log.d(TAG, "Total readings: ${allReadings.size} (${allReadings.count { it.level > 0 }} valid)")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing stress response", e)
                        }
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error executing stress command", e)
            throw e
        }
    }

    private fun toggleStressMonitoring(enabled: Boolean) {
        try {
            commandHandle.executeReqCmd(
                PressureSettingReq.getWriteInstance(enabled),
                object : ICommandResponse<PressureSettingRsp> {
                    override fun onDataResponse(resultEntity: PressureSettingRsp) {
                        Log.d(TAG, "Monitoring toggle response: $resultEntity")
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling stress monitoring", e)
            throw e
        }
    }

    private fun parseStressResponse(response: PressureRsp): List<StressPoint> {
        val calendar = Calendar.getInstance()

        // Parse date from response or use current date
        try {
            if (response.today != null) {
                val year = response.today.year
                val month = response.today.month
                val day = response.today.day

                // Validate date values
                if (year in 2020..2030 && month in 1..12 && day in 1..31) {
                    calendar.set(year, month - 1, day, 0, 0, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                } else {
                    Log.w(TAG, "Invalid date from device: $year-$month-$day, using current date")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing date", e)
        }

        val baseTimestamp = calendar.timeInMillis
        val intervalMs = (response.range.coerceAtLeast(1)) * 60L * 1000L

        // Parse stress values - keep original byte values (0-100 range)
        val stressValues = (response.pressureArray ?: byteArrayOf()).map { byte ->
            (byte.toInt() and 0xFF)
        }

        Log.d(TAG, "Base time: ${calendar.time}, Interval: ${response.range} min, Values: $stressValues")

        return stressValues.mapIndexed { index, value ->
            StressPoint(
                timestamp = baseTimestamp + (index * intervalMs),
                level = value
            )
        }
    }

    private fun getFixedIntervalReadings(readings: List<StressPoint>): List<StressPoint> {
        if (readings.isEmpty()) return emptyList()

        val validReadings = readings
            .filter { it.level > 0 }
            .sortedBy { it.timestamp }

        if (validReadings.isEmpty()) return emptyList()

        val result = mutableListOf<StressPoint>()
        val calendar = Calendar.getInstance()

        // Round to nearest 30-minute mark
        val currentMinute = calendar.get(Calendar.MINUTE)
        val roundedMinute = if (currentMinute >= 30) 30 else 0
        calendar.set(Calendar.MINUTE, roundedMinute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        var targetTime = calendar.timeInMillis
        val intervalMs = INTERVAL_MINUTES * 60 * 1000L
        val toleranceMs = 10 * 60 * 1000L // 10 minutes tolerance

        // Go back 48 intervals (24 hours)
        repeat(48) {
            val matchingReading = validReadings
                .filter { kotlin.math.abs(it.timestamp - targetTime) <= toleranceMs }
                .minByOrNull { kotlin.math.abs(it.timestamp - targetTime) }

            if (matchingReading != null) {
                result.add(matchingReading)
            }

            targetTime -= intervalMs

            // Stop if we've gone before the first reading
            if (targetTime < validReadings.first().timestamp) return@repeat
        }

        return result.reversed()
    }

    private fun calculateAverage(readings: List<StressPoint>): Int {
        if (readings.isEmpty()) return 0
        return readings.map { it.level }.average().toInt()
    }

    override fun onCleared() {
        super.onCleared()
        measurementJob?.cancel()
        Log.d(TAG, "ViewModel cleared")
    }
}