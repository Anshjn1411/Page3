package dev.infa.page3.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.infa.page3.models.HealthData
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
    private val dataSynchronization: DataSynchronization,
    private val healthMonitorCore: HealthMonitorCore,
    private val healthMeasurements: HealthMeasurements
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

    // Live health data from device
    private val _liveHealthData = MutableStateFlow(HealthData())
    val liveHealthData: StateFlow<HealthData> = _liveHealthData.asStateFlow()

    // Health settings from device
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
        // Setup heart rate sync callback
        dataSynchronization.setHeartRateCallback { hr ->
            _todayHeart.value = hr
        }

        // Poll for health data and settings updates
        viewModelScope.launch {
            while (true) {
                val healthData = healthMonitorCore.healthData

                // Update live data
                _liveHealthData.value = healthData

                // If we're measuring and got a valid heart rate, update instant value
                if (_isMeasuring.value && healthData.heartRate > 0) {
                    _instantHeartRate.value = healthData.heartRate
                }

                // Update settings
                _healthSettings.value = healthMonitorCore.healthSettings

                delay(500) // Poll every 500ms
            }
        }
    }

    /**
     * Load initial data from device
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                // Load current settings from device
                healthMonitorCore.readHeartRateSettings()
                delay(500)

                // Sync today's data
                withContext(Dispatchers.IO) {
                    val nowWithTz = dataSynchronization.getCurrentTimeWithTimezone()
                    dataSynchronization.syncHeartRateData(nowWithTz)
                }
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

                // Refresh settings
                healthMonitorCore.readHeartRateSettings()
                delay(300)

                // Refresh today's synced data
                withContext(Dispatchers.IO) {
                    val nowWithTz = dataSynchronization.getCurrentTimeWithTimezone()
                    dataSynchronization.syncHeartRateData(nowWithTz)
                }
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
        // Cancel any existing measurement
        measurementJob?.cancel()

        measurementJob = viewModelScope.launch {
            try {
                _isMeasuring.value = true
                _measurementProgress.value = 0
                _error.value = null
                _instantHeartRate.value = null

                var lastValidValue = 0
                val startTime = System.currentTimeMillis()

                // Start measurement
                withContext(Dispatchers.IO) {
                    healthMeasurements.measureHeartOnce()
                }

                // Monitor for 30 seconds
                val totalDuration = 30000L // 30 seconds
                val updateInterval = 300L // Update every 300ms

                while (System.currentTimeMillis() - startTime < totalDuration) {
                    if (!isActive) break

                    // Update progress
                    val elapsed = System.currentTimeMillis() - startTime
                    _measurementProgress.value = ((elapsed * 100) / totalDuration).toInt().coerceAtMost(99)

                    // Check for valid heart rate from live data
                    val currentHR = _liveHealthData.value.heartRate
                    if (currentHR > 0 && currentHR != lastValidValue) {
                        lastValidValue = currentHR
                        Log.d("HeartRateViewModel", "Captured HR: $currentHR")
                    }

                    delay(updateInterval)
                }

                // Stop measurement on device
                withContext(Dispatchers.IO) {
                    healthMeasurements.stopHeartRateMeasurement()
                }

                _measurementProgress.value = 100
                delay(500)

                // Use the last valid value received
                if (lastValidValue > 0) {
                    _instantHeartRate.value = lastValidValue
                    Log.d("HeartRateViewModel", "Final measurement result: $lastValidValue BPM")
                } else {
                    _error.value = "No valid measurement received"
                    Log.e("HeartRateViewModel", "Measurement failed - no valid value")
                }

            } catch (e: Exception) {
                _error.value = "Measurement failed: ${e.message}"
                Log.e("HeartRateViewModel", "Measurement error: ${e.message}", e)
            } finally {
                _isMeasuring.value = false
                _measurementProgress.value = 0
            }
        }
    }

    /**
     * Stop current measurement
     */
    fun stopMeasurement() {
        measurementJob?.cancel()
        viewModelScope.launch {
            try {
                healthMeasurements.stopHeartRateMeasurement()
                delay(500) // Wait for device to stop
                _isMeasuring.value = false
                _measurementProgress.value = 0
            } catch (e: Exception) {
                _error.value = "Failed to stop measurement: ${e.message}"
            }
        }
    }

    /**
     * Toggle continuous heart rate monitoring
     */
    fun toggleContinuousMonitoring(enabled: Boolean) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                // Get current interval or use default 30 minutes
                val interval = _healthSettings.value.heartRateInterval.takeIf { it > 0 } ?: 30

                // Send command to device
                healthMonitorCore.toggleHeartRate(enabled, interval)

                // Wait for device response
                delay(1000)

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
        // Clean up if measurement is running
        measurementJob?.cancel()
        if (_isMeasuring.value) {
            healthMeasurements.stopHeartRateMeasurement()
        }
    }
}


