package dev.infa.page3.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oudmon.ble.base.bluetooth.BleOperateManager
import dev.infa.page3.models.HealthData
import com.oudmon.ble.base.communication.CommandHandle
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.req.HRVReq
import com.oudmon.ble.base.communication.req.HrvSettingReq
import com.oudmon.ble.base.communication.rsp.HRVRsp
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*

class HrvViewModel(
    private val commandHandle: CommandHandle = CommandHandle.getInstance()
) : ViewModel()
{

    companion object {
        private const val TAG = "HrvViewModel"
        private const val MEASUREMENT_DURATION_MS = 30000L
        private const val INTERVAL_MINUTES = 30
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

    private val _instantHrv = MutableStateFlow<Int?>(null)
    val instantHrv = _instantHrv.asStateFlow()

    // Data State
    private val _readings = MutableStateFlow<List<HrvReading>>(emptyList())
    val allReadings = _readings.asStateFlow()

    private val _isMonitoringEnabled = MutableStateFlow(false)
    val isMonitoringEnabled = _isMonitoringEnabled.asStateFlow()

    private var measurementJob: Job? = null

    // Computed Properties
    val currentHrv: StateFlow<Int> = combine(
        _instantHrv,
        _readings,
        _isMeasuring
    ) { instant, readings, measuring ->
        when {
            // If measuring, show instant value (even if 0) or 0
            measuring -> instant ?: 0
            // If we have a fresh instant measurement, show it
            instant != null && instant > 0 -> instant
            // Otherwise show latest valid reading
            readings.isNotEmpty() -> {
                readings.filter { it.value > 0 }
                    .maxByOrNull { it.timestamp }?.value ?: 0
            }
            else -> 0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val averageHrv: StateFlow<Int> = _readings.map { readings ->
        calculateAverage(readings.filter { it.value > 0 })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val minHrv: StateFlow<Int> = _readings.map { readings ->
        readings.filter { it.value > 0 }.minOfOrNull { it.value } ?: 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val maxHrv: StateFlow<Int> = _readings.map { readings ->
        readings.filter { it.value > 0 }.maxOfOrNull { it.value } ?: 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val intervalReadings: StateFlow<List<HrvReading>> = _readings.map { readings ->
        getFixedIntervalReadings(readings)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        Log.d(TAG, "Initializing HrvViewModel")
        refresh()
    }

    fun refresh(offset: Int = 0) {
        Log.d(TAG, "Refreshing HRV data with offset=$offset")
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                withContext(Dispatchers.IO) {
                    syncHrvData(offset)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing data", e)
                _error.value = "Failed to load data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Fixed measureHrvOnce() - Pattern from working heart rate implementation
    fun measureHrvOnce() {
        Log.d(TAG, "Starting HRV manual measurement (single reading)")
        measurementJob?.cancel()
        measurementJob = viewModelScope.launch {
            try {
                _isMeasuring.value = true
                _measurementProgress.value = 0
                _error.value = null
                _instantHrv.value = null

                var measurementComplete = false

                // Start manual HRV measurement
                Log.d(TAG, "Initiating BLE HRV measurement...")
                BleOperateManager.getInstance().manualModeHrv({ result ->
                    val hrvValue = result.value
                    Log.d(TAG, "HRV reading received: $hrvValue ms")

                    // Only process valid readings
                    if (hrvValue > 0 && !measurementComplete) {
                        measurementComplete = true
                        viewModelScope.launch {
                            _instantHrv.value = hrvValue
                            _measurementProgress.value = 100
                            delay(500) // Show complete state briefly
                            _isMeasuring.value = false
                            syncHrvData(0)
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

                Log.d(TAG, "HRV measurement loop finished")

            } catch (e: Exception) {
                Log.e(TAG, "Error during HRV measurement", e)
                _error.value = "Measurement failed: ${e.message}"
                _isMeasuring.value = false
                _measurementProgress.value = 0
            }
        }
    }



    // Alternative approach - if the above doesn't work, try this simpler version:
    fun measureHrvOnceSimple() {
        Log.d(TAG, "Starting HRV manual measurement (single reading)")
        measurementJob?.cancel()

        _isMeasuring.value = true
        _measurementProgress.value = 0
        _error.value = null
        _instantHrv.value = null

        measurementJob = viewModelScope.launch {
            try {
                // Progress animation loop
                val startTime = System.currentTimeMillis()

                launch {
                    while (isActive && _isMeasuring.value) {
                        val elapsed = System.currentTimeMillis() - startTime
                        val progress = ((elapsed * 100) / MEASUREMENT_DURATION_MS).toInt().coerceAtMost(99)
                        _measurementProgress.value = progress
                        delay(300)
                    }
                }

                // Start BLE measurement
                withContext(Dispatchers.IO) {
                    BleOperateManager.getInstance().manualModeHrv({ result ->
                        val hrvValue = result.value
                        Log.d(TAG, "HRV reading received: $hrvValue ms")

                        if (hrvValue > 0) {
                            viewModelScope.launch {
                                _instantHrv.value = hrvValue
                                _measurementProgress.value = 100
                                delay(500)
                                _isMeasuring.value = false
                                syncHrvData(0)
                            }
                        }
                    }, false)

                    // Wait for measurement or timeout
                    delay(MEASUREMENT_DURATION_MS)
                    if (_isMeasuring.value) {
                        _error.value = "Measurement timeout"
                        _isMeasuring.value = false
                        _measurementProgress.value = 0
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error during HRV measurement", e)
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
    }

    fun toggleContinuousMonitoring(enabled: Boolean) {
        Log.d(TAG, "Toggling continuous monitoring: $enabled")
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                withContext(Dispatchers.IO) {
                    toggleHrvMonitoring(enabled)
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
    private fun syncHrvData(offset: Int) {
        Log.d(TAG, "Syncing HRV data with offset=$offset")
        try {
            commandHandle.executeReqCmd(
                HRVReq(offset.toByte()),
                object : ICommandResponse<HRVRsp> {
                    override fun onDataResponse(resultEntity: HRVRsp) {
                        Log.d(TAG, "Received HRV response: range=${resultEntity.range}, dataSize=${resultEntity.hrvArray?.size}")
                        try {
                            val newReadings = parseHrvResponse(resultEntity)
                            Log.d(TAG, "Parsed ${newReadings.size} readings (${newReadings.count { it.value > 0 }} valid)")

                            // Merge with existing readings and remove duplicates
                            val allReadings = (_readings.value + newReadings)
                                .distinctBy { it.timestamp }
                                .sortedBy { it.timestamp }

                            _readings.value = allReadings
                            Log.d(TAG, "Total readings: ${allReadings.size} (${allReadings.count { it.value > 0 }} valid)")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing HRV response", e)
                        }
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error executing HRV command", e)
            throw e
        }
    }

    private fun toggleHrvMonitoring(enabled: Boolean) {
        try {
            commandHandle.executeReqCmd(
                HrvSettingReq(enabled),
                null
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling HRV monitoring", e)
            throw e
        }
    }

    private fun parseHrvResponse(response: HRVRsp): List<HrvReading> {
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

        // Parse HRV values - FIX: Don't divide by 10, use raw values
        val hrvValues = (response.hrvArray ?: byteArrayOf()).map { byte ->
            (byte.toInt() and 0xFF) // Remove the division by 10
        }

        Log.d(TAG, "Base time: ${calendar.time}, Interval: ${response.range} min, Values: $hrvValues")

        return hrvValues.mapIndexed { index, value ->
            HrvReading(
                timestamp = baseTimestamp + (index * intervalMs),
                value = value
            )
        }
    }

    private fun getFixedIntervalReadings(readings: List<HrvReading>): List<HrvReading> {
        if (readings.isEmpty()) return emptyList()

        val validReadings = readings
            .filter { it.value > 0 }
            .sortedBy { it.timestamp }

        if (validReadings.isEmpty()) return emptyList()

        val result = mutableListOf<HrvReading>()
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

    private fun calculateAverage(readings: List<HrvReading>): Int {
        if (readings.isEmpty()) return 0
        return readings.map { it.value }.average().toInt()
    }

    data class HrvReading(
        val timestamp: Long,
        val value: Int
    )

    override fun onCleared() {
        super.onCleared()
        measurementJob?.cancel()
        Log.d(TAG, "ViewModel cleared")
    }
}