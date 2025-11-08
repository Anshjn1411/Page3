package dev.infa.page3.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oudmon.ble.base.bluetooth.BleOperateManager
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
import com.oudmon.ble.base.communication.CommandHandle
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.LargeDataHandler
import com.oudmon.ble.base.communication.bigData.BloodOxygenEntity
import com.oudmon.ble.base.communication.req.BloodOxygenSettingReq
import com.oudmon.ble.base.communication.rsp.BloodOxygenSettingRsp

class BloodOxygenViewModel(
    private val commandHandle: CommandHandle = CommandHandle.getInstance()
) : ViewModel() {
    companion object { private const val TAG = "BloodOxygenVM" }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _todaySpO2 = MutableStateFlow<List<BloodOxygenReading>>(emptyList())
    val todaySpO2: StateFlow<List<BloodOxygenReading>> = _todaySpO2.asStateFlow()

    private val _liveHealthData = MutableStateFlow(HealthData())
    val liveHealthData: StateFlow<HealthData> = _liveHealthData.asStateFlow()

    private val _healthSettings = MutableStateFlow(HealthSettings())
    val healthSettings: StateFlow<HealthSettings> = _healthSettings.asStateFlow()

    private val _isMeasuring = MutableStateFlow(false)
    val isMeasuring: StateFlow<Boolean> = _isMeasuring.asStateFlow()

    private val _measurementProgress = MutableStateFlow(0)
    val measurementProgress: StateFlow<Int> = _measurementProgress.asStateFlow()

    private val _instantBloodOxygen = MutableStateFlow<Int?>(null)
    val instantBloodOxygen: StateFlow<Int?> = _instantBloodOxygen.asStateFlow()

    private var measurementJob: Job? = null

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

    val averageBloodOxygen: StateFlow<Int> = _todaySpO2
        .map { if (it.isEmpty()) 0 else it.map { r -> r.bloodOxygen }.average().toInt() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val minBloodOxygen: StateFlow<Int> = _todaySpO2
        .map { it.minOfOrNull { r -> r.bloodOxygen } ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val maxBloodOxygen: StateFlow<Int> = _todaySpO2
        .map { it.maxOfOrNull { r -> r.bloodOxygen } ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allReadings: StateFlow<List<BloodOxygenReading>> = _todaySpO2
        .map { it.sortedBy { r -> r.timestamp } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val intervalReadings: StateFlow<List<BloodOxygenReading>> = _todaySpO2
        .map { getFixedIntervalReadings(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isMonitoringEnabled: StateFlow<Boolean> = _healthSettings
        .map { it.bloodOxygenEnabled }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        Log.d(TAG, "🩸 BloodOxygenViewModel initialized")
        loadInitialData()
    }

    private fun loadInitialData() {
        Log.d(TAG, "🚀 loadInitialData() started")
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                readSpO2Settings()
                delay(300)
                syncSpO2Data()
                Log.d(TAG, "✅ loadInitialData() complete: readings=${_todaySpO2.value.size}")
            } catch (e: Exception) {
                _error.value = "Failed to load data: ${e.message}"
                Log.e(TAG, "❌ loadInitialData() error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        Log.d(TAG, "🔄 refresh() called")
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                readSpO2Settings()
                delay(200)
                syncSpO2Data()
                Log.d(TAG, "✅ refresh() complete: readings=${_todaySpO2.value.size}")
            } catch (e: Exception) {
                _error.value = "Refresh failed: ${e.message}"
                Log.e(TAG, "❌ refresh() error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun measureBloodOxygenOnce() {
        Log.d(TAG, "🧪 Starting manual SpO₂ measurement (single reading)")
        measurementJob?.cancel()
        measurementJob = viewModelScope.launch {
            try {
                _isMeasuring.value = true
                _measurementProgress.value = 0
                _error.value = null
                _instantBloodOxygen.value = null

                // Begin BLE manual measurement
                Log.d(TAG, "🔹 Initiating BLE SpO₂ measurement...")
                BleOperateManager.getInstance().manualModeSpO2({ result ->
                    val spo2Value = result.value
                    Log.d(TAG, "🩸 Received SpO₂ value: $spo2Value%")

                    viewModelScope.launch {
                        _instantBloodOxygen.value = spo2Value
                        _liveHealthData.value = _liveHealthData.value.copy(spo2 = spo2Value)
                        syncSpO2Data()
                    }

                    _measurementProgress.value = 100
                    _isMeasuring.value = false
                }, false)

                // Simulate measurement progress animation
                val start = System.currentTimeMillis()
                val total = 30000L
                val step = 300L

                while (System.currentTimeMillis() - start < total && _isMeasuring.value && isActive) {
                    val elapsed = System.currentTimeMillis() - start
                    _measurementProgress.value = ((elapsed * 100) / total).toInt().coerceAtMost(99)
                    delay(step)
                }

                Log.d(TAG, "✅ SpO₂ measurement completed or manually stopped")

            } catch (e: Exception) {
                _error.value = "Measurement failed: ${e.message}"
                Log.e(TAG, "❌ measureBloodOxygenOnce() exception", e)
                _isMeasuring.value = false
                _measurementProgress.value = 0
            }
        }
    }

    fun stopMeasurement() {
        Log.d(TAG, "🛑 stopMeasurement() called")
        measurementJob?.cancel()
        _isMeasuring.value = false
        _measurementProgress.value = 0
    }

    fun toggleContinuousMonitoring(enabled: Boolean) {
        Log.d(TAG, "⚙️ toggleContinuousMonitoring(enabled=$enabled)")
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                toggleSpO2Monitoring(enabled)
                delay(800)
                readSpO2Settings()
                Log.d(TAG, "✅ Monitoring toggled: enabled=$enabled")
            } catch (e: Exception) {
                _error.value = "Failed to toggle monitoring: ${e.message}"
                Log.e(TAG, "❌ toggleContinuousMonitoring() error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearInstantMeasurement() {
        Log.d(TAG, "🧹 clearInstantMeasurement()")
        _instantBloodOxygen.value = null
    }

    fun clearError() {
        Log.d(TAG, "🧹 clearError()")
        _error.value = null
    }

    // ========== COMMON FUNCTIONS (Reusable) ==========

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

    private fun mergeSameDayReadings(
        old: List<BloodOxygenReading>,
        new: List<BloodOxygenReading>
    ): List<BloodOxygenReading> {
        if (old.isEmpty()) return new
        val map = LinkedHashMap<Long, BloodOxygenReading>()
        (old + new).forEach { map[it.timestamp] = it }
        return map.values.sortedBy { it.timestamp }
    }

    // ========== DEVICE COMMUNICATION ==========

    private suspend fun readSpO2Settings() = withContext(Dispatchers.IO) {
        Log.d(TAG, "📥 readSpO2Settings()")
        try {
            commandHandle.executeReqCmd(
                BloodOxygenSettingReq.getReadInstance(),
                object : ICommandResponse<BloodOxygenSettingRsp> {
                    override fun onDataResponse(result: BloodOxygenSettingRsp) {
                        Log.d(TAG, "📡 SpO2 settings: status=${result.status}, enabled=${result.isEnable}")
                        if (result.status == com.oudmon.ble.base.communication.rsp.BaseRspCmd.RESULT_OK) {
                            _healthSettings.value = _healthSettings.value.copy(
                                bloodOxygenEnabled = result.isEnable
                            )
                        }
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ readSpO2Settings() failed", e)
            throw e
        }
    }

    private fun toggleSpO2Monitoring(enabled: Boolean) {
        Log.d(TAG, "📤 toggleSpO2Monitoring(enabled=$enabled)")
        try {
            commandHandle.executeReqCmd(
                BloodOxygenSettingReq.getWriteInstance(enabled),
                null
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ toggleSpO2Monitoring() failed", e)
            throw e
        }
    }

    private suspend fun syncSpO2Data() = withContext(Dispatchers.IO) {
        Log.d(TAG, "📡 syncSpO2Data() started")
        try {
            LargeDataHandler.getInstance().syncBloodOxygenWithCallback { entityList ->
                Log.d(TAG, "📩 Sync callback: received ${entityList?.size ?: 0} entities")

                if (entityList.isNullOrEmpty()) {
                    Log.w(TAG, "⚠️ No data received from device")
                    return@syncBloodOxygenWithCallback
                }

                try {
                    var allReadings = _todaySpO2.value.toList()

                    entityList.forEachIndexed { index, entity ->
                        Log.d(TAG, "📦 Entity[$index]: date=${entity.dateStr}, unix=${entity.unix_time}, " +
                                "maxArray=${entity.maxArray?.size ?: 0}, minArray=${entity.minArray?.size ?: 0}")

                        val baseTime = resolveBaseTime(entity)

                        // Priority: maxArray > minArray (as per device documentation)
                        val values: List<Int> = when {
                            !entity.maxArray.isNullOrEmpty() -> {
                                Log.d(TAG, "  Using maxArray with ${entity.maxArray!!.size} values")
                                entity.maxArray!!
                            }
                            !entity.minArray.isNullOrEmpty() -> {
                                Log.d(TAG, "  Using minArray with ${entity.minArray!!.size} values")
                                entity.minArray!!
                            }
                            else -> {
                                Log.w(TAG, "  ⚠️ Both arrays are empty!")
                                emptyList()
                            }
                        }

                        // Convert to readings (one per hour)
                        val newReadings = values.mapIndexedNotNull { hourIndex, value ->
                            // Filter out invalid values
                            if (value <= 0 || value > 100) {
                                Log.w(TAG, "  ⚠️ Invalid SpO2 value at hour $hourIndex: $value")
                                null
                            } else {
                                val timestamp = baseTime + (hourIndex * 60 * 60 * 1000L)
                                BloodOxygenReading(timestamp, value)
                            }
                        }

                        Log.d(TAG, "  ✅ Created ${newReadings.size} valid readings from ${values.size} values")
                        allReadings = mergeSameDayReadings(allReadings, newReadings)
                    }

                    _todaySpO2.value = allReadings
                    Log.d(TAG, "✅ syncSpO2Data complete: ${allReadings.size} total readings")

                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error processing sync data", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ syncSpO2Data() failed", e)
            throw e
        }
    }

    private fun resolveBaseTime(entity: BloodOxygenEntity): Long {
        // Try unix_time first
        val unix = entity.unix_time
        if (unix > 0) {
            return if (unix >= 1_000_000_000_000L) unix else unix * 1000
        }

        // Try dateStr
        val dateStr = entity.dateStr
        if (!dateStr.isNullOrEmpty()) {
            try {
                val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                return fmt.parse(dateStr)?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to parse date: $dateStr", e)
            }
        }

        // Fallback to today's date at 00:00
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    data class BloodOxygenReading(
        val timestamp: Long,
        val bloodOxygen: Int
    )

    override fun onCleared() {
        super.onCleared()
        measurementJob?.cancel()
        Log.d(TAG, "🧹 ViewModel cleared")
    }
}