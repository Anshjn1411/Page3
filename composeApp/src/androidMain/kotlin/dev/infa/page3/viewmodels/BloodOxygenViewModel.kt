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

class BloodOxygenViewModel(
    private val dataSynchronization: DataSynchronization,
    private val healthMonitorCore: HealthMonitorCore,
    private val healthMeasurements: HealthMeasurements
) : ViewModel() {

    // Loading and error states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Synced blood oxygen readings for today
    private val _todaySpO2 = MutableStateFlow<List<BloodOxygenReading>>(emptyList())
    val todaySpO2: StateFlow<List<BloodOxygenReading>> = _todaySpO2.asStateFlow()

    // Live health data from device
    private val _liveHealthData = MutableStateFlow(HealthData())
    val liveHealthData: StateFlow<HealthData> = _liveHealthData.asStateFlow()

    // Health settings from device
    private val _healthSettings = MutableStateFlow(HealthSettings())
    val healthSettings: StateFlow<HealthSettings> = _healthSettings.asStateFlow()

    // Measurement states
    private val _isMeasuring = MutableStateFlow(false)
    val isMeasuring: StateFlow<Boolean> = _isMeasuring.asStateFlow()

    private val _measurementProgress = MutableStateFlow(0)
    val measurementProgress: StateFlow<Int> = _measurementProgress.asStateFlow()

    private val _instantBloodOxygen = MutableStateFlow<Int?>(null)
    val instantBloodOxygen: StateFlow<Int?> = _instantBloodOxygen.asStateFlow()

    private var measurementJob: Job? = null

    // Current SpO2 (%): prefer instant > live > latest synced
    val currentBloodOxygen: StateFlow<Int> = combine(
        _instantBloodOxygen,
        _liveHealthData,
        _todaySpO2
    ) { instant, live, today ->
        when {
            instant != null && instant > 0 -> instant
            live.spo2 > 0 -> live.spo2
            today.isNotEmpty() -> today.last().bloodOxygen
            else -> 0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Stats
    val averageBloodOxygen: StateFlow<Int> = _todaySpO2
        .map { list -> if (list.isEmpty()) 0 else list.map { it.bloodOxygen }.average().toInt() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val minBloodOxygen: StateFlow<Int> = _todaySpO2
        .map { list -> list.minOfOrNull { it.bloodOxygen } ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val maxBloodOxygen: StateFlow<Int> = _todaySpO2
        .map { list -> list.maxOfOrNull { it.bloodOxygen } ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // All readings today
    val allReadings: StateFlow<List<BloodOxygenReading>> = _todaySpO2
        .map { it.sortedBy { r -> r.timestamp } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 30-min interval readings
    val intervalReadings: StateFlow<List<BloodOxygenReading>> = _todaySpO2
        .map { getFixedIntervalReadings(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Monitoring status
    val isMonitoringEnabled: StateFlow<Boolean> = _healthSettings
        .map { it.bloodOxygenEnabled }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        setupDataCallbacks()
        loadInitialData()
    }

    private fun setupDataCallbacks() {
        dataSynchronization.setBloodOxygenCallback { bo ->
            // Convert hourly arrays into timestamped readings (use max per hour)
            val base = if (bo.unixTime > 0) bo.unixTime * 1000 else System.currentTimeMillis()
            val list = mutableListOf<BloodOxygenReading>()
            val maxArr = bo.maxArray
            for (h in maxArr.indices) {
                val value = maxArr[h].coerceIn(0, 100)
                val ts = base + h * 60L * 60L * 1000L
                list.add(BloodOxygenReading(timestamp = ts, bloodOxygen = value))
            }
            // Only keep today's
            _todaySpO2.value = mergeSameDay(_todaySpO2.value, list)
        }

        // Poll health data and settings
        viewModelScope.launch {
            while (true) {
                val healthData = healthMonitorCore.healthData
                _liveHealthData.value = healthData
                _healthSettings.value = healthMonitorCore.healthSettings
                if (_isMeasuring.value && healthData.spo2 > 0) {
                    _instantBloodOxygen.value = healthData.spo2
                }
                delay(500)
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                // Read settings
                healthMonitorCore.readBloodOxygenSettings()
                delay(300)
                // Sync data via handler
                withContext(Dispatchers.IO) { dataSynchronization.syncBloodOxygen() }
            } catch (e: Exception) {
                _error.value = "Failed to load data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                healthMonitorCore.readBloodOxygenSettings()
                delay(200)
                withContext(Dispatchers.IO) { dataSynchronization.syncBloodOxygen() }
            } catch (e: Exception) {
                _error.value = "Refresh failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun measureBloodOxygenOnce() {
        measurementJob?.cancel()
        measurementJob = viewModelScope.launch {
            try {
                _isMeasuring.value = true
                _measurementProgress.value = 0
                _error.value = null
                _instantBloodOxygen.value = null

                val start = System.currentTimeMillis()
                withContext(Dispatchers.IO) { healthMeasurements.measureBloodOxygenOnce() }

                val total = 30000L
                val step = 300L
                var last = 0
                while (System.currentTimeMillis() - start < total) {
                    if (!isActive) break
                    val elapsed = System.currentTimeMillis() - start
                    _measurementProgress.value = ((elapsed * 100) / total).toInt().coerceAtMost(99)
                    val v = _liveHealthData.value.spo2
                    if (v > 0 && v != last) { last = v; Log.d("BloodOxygenVM", "SpO2: $v") }
                    delay(step)
                }

                withContext(Dispatchers.IO) { healthMeasurements.stopBloodOxygenMeasurement() }
                _measurementProgress.value = 100
                delay(400)
                if (last > 0) _instantBloodOxygen.value = last else _error.value = "No valid measurement"
            } catch (e: Exception) {
                _error.value = "Measurement failed: ${e.message}"
            } finally {
                _isMeasuring.value = false
                _measurementProgress.value = 0
            }
        }
    }

    fun stopMeasurement() {
        measurementJob?.cancel()
        viewModelScope.launch {
            try {
                healthMeasurements.stopBloodOxygenMeasurement()
                delay(300)
                _isMeasuring.value = false
                _measurementProgress.value = 0
            } catch (e: Exception) {
                _error.value = "Failed to stop measurement: ${e.message}"
            }
        }
    }

    fun toggleContinuousMonitoring(enabled: Boolean) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                healthMonitorCore.toggleBloodOxygen(enabled)
                delay(800)
            } catch (e: Exception) {
                _error.value = "Failed to toggle monitoring: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearInstantMeasurement() { _instantBloodOxygen.value = null }
    fun clearError() { _error.value = null }

    private fun getFixedIntervalReadings(readings: List<BloodOxygenReading>): List<BloodOxygenReading> {
        if (readings.isEmpty()) return emptyList()
        val calendar = Calendar.getInstance()
        val roundedMinute = if (calendar.get(Calendar.MINUTE) >= 30) 30 else 0
        calendar.set(Calendar.MINUTE, roundedMinute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val result = mutableListOf<BloodOxygenReading>()
        var target = calendar.timeInMillis
        val sorted = readings.sortedBy { it.timestamp }
        repeat(48) {
            val closest = sorted
                .filter { kotlin.math.abs(it.timestamp - target) <= 10 * 60 * 1000 }
                .minByOrNull { kotlin.math.abs(it.timestamp - target) }
            if (closest != null) result.add(closest)
            target -= 30 * 60 * 1000
            if (target < sorted.first().timestamp) return@repeat
        }
        return result.reversed()
    }

    // Merge readings for the same day; prefer new values for identical timestamps
    private fun mergeSameDay(old: List<BloodOxygenReading>, new: List<BloodOxygenReading>): List<BloodOxygenReading> {
        if (old.isEmpty()) return new
        val map = LinkedHashMap<Long, BloodOxygenReading>()
        (old + new).forEach { map[it.timestamp] = it }
        return map.values.sortedBy { it.timestamp }
    }

    data class BloodOxygenReading(val timestamp: Long, val bloodOxygen: Int)

    override fun onCleared() {
        super.onCleared()
        measurementJob?.cancel()
        if (_isMeasuring.value) {
            healthMeasurements.stopBloodOxygenMeasurement()
        }
    }
}
