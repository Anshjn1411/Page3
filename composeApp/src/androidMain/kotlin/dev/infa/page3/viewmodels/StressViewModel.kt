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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class StressPoint(
    val timestamp: Long = 0L,
    val level: Int = 0
)

class StressViewModel(
    private val dataSynchronization: DataSynchronization,
    private val healthMonitorCore: HealthMonitorCore,
    private val healthMeasurements: HealthMeasurements
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isMeasuring = MutableStateFlow(false)
    val isMeasuring: StateFlow<Boolean> = _isMeasuring.asStateFlow()

    private val _measurementProgress = MutableStateFlow(0)
    val measurementProgress: StateFlow<Int> = _measurementProgress.asStateFlow()

    private val _instantStress = MutableStateFlow<Int?>(null)
    val instantStress: StateFlow<Int?> = _instantStress.asStateFlow()

    private val _readings = MutableStateFlow<List<StressPoint>>(emptyList())
    val allReadings: StateFlow<List<StressPoint>> = _readings.asStateFlow()

    private var measurementJob: Job? = null

    // Current stress: instant > live > latest synced
    val currentStress: StateFlow<Int> = combine(
        _instantStress,
        MutableStateFlow(healthMonitorCore.healthData),
        _readings
    ) { instant, live, list ->
        when {
            instant != null && instant > 0 -> instant
            live.pressure > 0 -> live.pressure
            list.isNotEmpty() -> list.last().level
            else -> 0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Stats
    val averageStress: StateFlow<Int> = _readings.map { l -> if (l.isEmpty()) 0 else l.map { it.level }.average().toInt() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val minStress: StateFlow<Int> = _readings.map { it.minOfOrNull { r -> r.level } ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val maxStress: StateFlow<Int> = _readings.map { it.maxOfOrNull { r -> r.level } ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // 30-min interval readings
    val intervalReadings: StateFlow<List<StressPoint>> = _readings.map { getFixedIntervalReadings(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Monitoring status
    val isMonitoringEnabled: StateFlow<Boolean> = MutableStateFlow(healthMonitorCore.healthSettings.pressureEnabled)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        dataSynchronization.setStressCallback { data ->
            val points = mapToPoints(data)
            _readings.value = (_readings.value + points).sortedBy { it.timestamp }
        }

        viewModelScope.launch {
            while (true) {
                val hd = healthMonitorCore.healthData
                if (_isMeasuring.value && hd.pressure > 0) {
                    _instantStress.value = hd.pressure
                }
                delay(500)
            }
        }
    }

    fun refresh(offset: Int = 0) {
        viewModelScope.launch {
            try { _isLoading.value = true; _error.value = null; withContext(Dispatchers.IO) { dataSynchronization.syncStress(offset) } }
            catch (e: Exception) { _error.value = e.message }
            finally { _isLoading.value = false }
        }
    }

    fun measureStressOnce() {
        measurementJob?.cancel()
        measurementJob = viewModelScope.launch {
            try {
                _isMeasuring.value = true
                _measurementProgress.value = 0
                _error.value = null
                _instantStress.value = null

                withContext(Dispatchers.IO) { healthMeasurements.measurePressureOnce() }
                val start = System.currentTimeMillis()
                val total = 30000L
                while (System.currentTimeMillis() - start < total) {
                    if (!isActive) break
                    val elapsed = System.currentTimeMillis() - start
                    _measurementProgress.value = ((elapsed * 100) / total).toInt().coerceAtMost(99)
                    delay(300)
                }
                withContext(Dispatchers.IO) { healthMeasurements.stopPressureMeasurement() }
                _measurementProgress.value = 100
                delay(300)
                if (_instantStress.value == null) _error.value = "No valid measurement"
            } catch (e: Exception) {
                _error.value = "Measurement failed: ${e.message}"
                Log.e("StressViewModel", "measure error", e)
            } finally {
                _isMeasuring.value = false
                _measurementProgress.value = 0
            }
        }
    }

    fun stopMeasurement() {
        measurementJob?.cancel()
        viewModelScope.launch { healthMeasurements.stopPressureMeasurement(); _isMeasuring.value = false; _measurementProgress.value = 0 }
    }

    fun toggleContinuousMonitoring(enabled: Boolean) {
        viewModelScope.launch {
            try { _isLoading.value = true; _error.value = null; healthMonitorCore.togglePressure(enabled) }
            catch (e: Exception) { _error.value = e.message }
            finally { _isLoading.value = false }
        }
    }

    fun clearInstantMeasurement() { _instantStress.value = null }
    fun clearError() { _error.value = null }

    private fun mapToPoints(data: DataSynchronization.StressData): List<StressPoint> {
        val cal = Calendar.getInstance()
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            cal.time = sdf.parse(data.date) ?: Calendar.getInstance().time
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
        } catch (_: Exception) {}
        val base = cal.timeInMillis
        val stepMs = (data.rangeMinutes.coerceAtLeast(1)) * 60L * 1000L
        return data.values.mapIndexed { idx, v ->
            StressPoint(timestamp = base + idx * stepMs, level = v)
        }
    }

    private fun getFixedIntervalReadings(readings: List<StressPoint>): List<StressPoint> {
        if (readings.isEmpty()) return emptyList()
        val calendar = Calendar.getInstance()
        val roundedMinute = if (calendar.get(Calendar.MINUTE) >= 30) 30 else 0
        calendar.set(Calendar.MINUTE, roundedMinute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val result = mutableListOf<StressPoint>()
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

    override fun onCleared() {
        super.onCleared()
        measurementJob?.cancel()
        if (_isMeasuring.value) {
            healthMeasurements.stopPressureMeasurement()
        }
    }
}

