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

class HrvViewModel(
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

    private val _instantHrv = MutableStateFlow<Int?>(null)
    val instantHrv: StateFlow<Int?> = _instantHrv.asStateFlow()

    private val _readings = MutableStateFlow<List<HrvReading>>(emptyList())
    val allReadings: StateFlow<List<HrvReading>> = _readings.asStateFlow()

    private var measurementJob: Job? = null

    // Current HRV: instant > live > latest synced
    val currentHrv: StateFlow<Int> = combine(
        _instantHrv,
        MutableStateFlow(healthMonitorCore.healthData),
        _readings
    ) { instant, live, list ->
        when {
            instant != null && instant > 0 -> instant
            live.hrvValue > 0 -> live.hrvValue
            list.isNotEmpty() -> list.last().value
            else -> 0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Stats
    val averageHrv: StateFlow<Int> = _readings.map { l -> if (l.isEmpty()) 0 else l.map { it.value }.average().toInt() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val minHrv: StateFlow<Int> = _readings.map { it.minOfOrNull { r -> r.value } ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val maxHrv: StateFlow<Int> = _readings.map { it.maxOfOrNull { r -> r.value } ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // 30-min interval readings
    val intervalReadings: StateFlow<List<HrvReading>> = _readings.map { getFixedIntervalReadings(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Monitoring status
    val isMonitoringEnabled: StateFlow<Boolean> = MutableStateFlow(healthMonitorCore.healthSettings.hrvEnabled)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        dataSynchronization.setHrvCallback { data ->
            val points = mapToPoints(data)
            _readings.value = (_readings.value + points).sortedBy { it.timestamp }
        }

        viewModelScope.launch {
            while (true) {
                val hd = healthMonitorCore.healthData
                if (_isMeasuring.value && hd.hrvValue > 0) {
                    _instantHrv.value = hd.hrvValue
                }
                delay(500)
            }
        }
    }

    fun refresh(offset: Int = 0) {
        viewModelScope.launch {
            try { _isLoading.value = true; _error.value = null; withContext(Dispatchers.IO) { dataSynchronization.syncHrv(offset) } }
            catch (e: Exception) { _error.value = e.message }
            finally { _isLoading.value = false }
        }
    }

    fun measureHrvOnce() {
        measurementJob?.cancel()
        measurementJob = viewModelScope.launch {
            try {
                _isMeasuring.value = true
                _measurementProgress.value = 0
                _error.value = null
                _instantHrv.value = null

                withContext(Dispatchers.IO) { healthMeasurements.measureHrvOnce() }
                val start = System.currentTimeMillis()
                val total = 30000L
                while (System.currentTimeMillis() - start < total) {
                    if (!isActive) break
                    val elapsed = System.currentTimeMillis() - start
                    _measurementProgress.value = ((elapsed * 100) / total).toInt().coerceAtMost(99)
                    delay(300)
                }
                withContext(Dispatchers.IO) { healthMeasurements.stopHrvMeasurement() }
                _measurementProgress.value = 100
                delay(300)
                if (_instantHrv.value == null) _error.value = "No valid measurement"
            } catch (e: Exception) {
                _error.value = "Measurement failed: ${e.message}"
                Log.e("HrvViewModel", "measure error", e)
            } finally {
                _isMeasuring.value = false
                _measurementProgress.value = 0
            }
        }
    }

    fun stopMeasurement() {
        measurementJob?.cancel()
        viewModelScope.launch { healthMeasurements.stopHrvMeasurement(); _isMeasuring.value = false; _measurementProgress.value = 0 }
    }

    fun toggleContinuousMonitoring(enabled: Boolean) {
        viewModelScope.launch {
            try { _isLoading.value = true; _error.value = null; healthMonitorCore.toggleHrv(enabled) }
            catch (e: Exception) { _error.value = e.message }
            finally { _isLoading.value = false }
        }
    }

    fun clearInstantMeasurement() { _instantHrv.value = null }
    fun clearError() { _error.value = null }

    private fun mapToPoints(data: DataSynchronization.HrvData): List<HrvReading> {
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
        return data.values.mapIndexed { idx, v -> HrvReading(timestamp = base + idx * stepMs, value = v) }
    }

    private fun getFixedIntervalReadings(readings: List<HrvReading>): List<HrvReading> {
        if (readings.isEmpty()) return emptyList()
        val calendar = Calendar.getInstance()
        val roundedMinute = if (calendar.get(Calendar.MINUTE) >= 30) 30 else 0
        calendar.set(Calendar.MINUTE, roundedMinute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val result = mutableListOf<HrvReading>()
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

    data class HrvReading(val timestamp: Long, val value: Int)

    override fun onCleared() {
        super.onCleared()
        measurementJob?.cancel()
        if (_isMeasuring.value) {
            healthMeasurements.stopHrvMeasurement()
        }
    }
}

//package dev.infa.page3.viewmodels
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//
//data class HrvData(
//    val value: Int = 0,
//    val timestamp: Long = 0L
//)
//
//class HrvViewModel(
//    private val dataSynchronization: DataSynchronization
//) : ViewModel() {
//
//    private val _isLoading = MutableStateFlow(false)
//    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
//
//    private val _error = MutableStateFlow<String?>(null)
//    val error: StateFlow<String?> = _error.asStateFlow()
//
//    private val _latest = MutableStateFlow(HrvData())
//    val latest: StateFlow<HrvData> = _latest.asStateFlow()
//
//    fun refresh() {
//        viewModelScope.launch {
//            try {
//                _isLoading.value = true
//                _error.value = null
//                withContext(Dispatchers.IO) {
//                    // Placeholder: integrate with HRV sync when available in DataSynchronization
//                }
//            } catch (e: Exception) {
//                _error.value = e.message
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }
//}
//
//
