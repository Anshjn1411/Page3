package dev.infa.page3.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.oudmon.ble.base.communication.CommandHandle
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.Constants
import com.oudmon.ble.base.communication.req.SimpleKeyReq
import com.oudmon.ble.base.communication.rsp.BpDataRsp
import com.oudmon.ble.base.communication.req.ReadPressureReq
import com.oudmon.ble.base.communication.rsp.ReadBlePressureRsp
import com.oudmon.ble.base.communication.req.BpSettingReq
import com.oudmon.ble.base.communication.rsp.BpSettingRsp
import com.oudmon.ble.base.communication.rsp.BaseRspCmd
import dev.infa.page3.models.HealthData


class BloodPressureViewModel(
    private val commandHandle: CommandHandle = CommandHandle.getInstance()
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isMeasuring = MutableStateFlow(false)
    val isMeasuring: StateFlow<Boolean> = _isMeasuring.asStateFlow()

    private val _measurementProgress = MutableStateFlow(0)
    val measurementProgress: StateFlow<Int> = _measurementProgress.asStateFlow()

    private val _instant = MutableStateFlow<BPDisplay?>(null)
    val instant: StateFlow<BPDisplay?> = _instant.asStateFlow()

    private val _readings = MutableStateFlow<List<BPReading>>(emptyList())
    val allReadings: StateFlow<List<BPReading>> = _readings.asStateFlow()

    private var measurementJob: Job? = null

    val latest: StateFlow<BPDisplay> = combine(
        _instant,
        MutableStateFlow(HealthData()),
        _readings
    ) { inst, live, list ->
        when {
            inst != null -> inst
            live.systolic > 0 && live.diastolic > 0 -> BPDisplay(live.systolic, live.diastolic)
            list.isNotEmpty() -> list.last().toDisplay()
            else -> BPDisplay()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BPDisplay())

    val averageSystolic: StateFlow<Int> = _readings.map { l -> if (l.isEmpty()) 0 else l.map { it.systolic }.average().toInt() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val minSystolic: StateFlow<Int> = _readings.map { it.minOfOrNull { r -> r.systolic } ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val maxSystolic: StateFlow<Int> = _readings.map { it.maxOfOrNull { r -> r.systolic } ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init { }

    fun refreshAuto(userAge: Int = 30) {
        viewModelScope.launch {
            try {
                _isLoading.value = true; _error.value = null
                withContext(Dispatchers.IO) { syncBpAutoInline(userAge) }
            }
            catch (e: Exception) { _error.value = e.message }
            finally { _isLoading.value = false }
        }
    }

    fun refreshManual() {
        viewModelScope.launch {
            try {
                _isLoading.value = true; _error.value = null
                withContext(Dispatchers.IO) { syncBpManualInline() }
            }
            catch (e: Exception) { _error.value = e.message }
            finally { _isLoading.value = false }
        }
    }

    fun measureOnce() {
        measurementJob?.cancel()
        measurementJob = viewModelScope.launch {
            try {
                _isMeasuring.value = true
                _measurementProgress.value = 0
                _error.value = null
                _instant.value = null
                val start = System.currentTimeMillis()
                val total = 30000L
                while (System.currentTimeMillis() - start < total) {
                    if (!isActive) break
                    val elapsed = System.currentTimeMillis() - start
                    _measurementProgress.value = ((elapsed * 100) / total).toInt().coerceAtMost(99)
                    delay(300)
                }
                _measurementProgress.value = 100
                delay(300)
                withContext(Dispatchers.IO) { syncBpManualInline() }
                _instant.value = _readings.value.lastOrNull()?.toDisplay()
                if (_instant.value == null) _error.value = "No valid measurement"
            } finally {
                _isMeasuring.value = false
                _measurementProgress.value = 0
            }
        }
    }

    fun stopMeasurement() { measurementJob?.cancel(); _isMeasuring.value = false; _measurementProgress.value = 0 }

    fun toggleContinuousMonitoring(enabled: Boolean) {
        viewModelScope.launch {
            try {
                _isLoading.value = true; _error.value = null
                toggleBpInline(enabled)
            }
            catch (e: Exception) { _error.value = e.message }
            finally { _isLoading.value = false }
        }
    }

    data class BPDisplay(val systolicBP: Int = 0, val diastolicBP: Int = 0)
    data class BPReading(val timestamp: Long, val systolic: Int, val diastolic: Int, val heartRate: Int) {
        fun toDisplay() = BPDisplay(systolic, diastolic)
    }

    // Inline SDK ops
    private fun syncBpAutoInline(userAge: Int) {
        try {
            commandHandle.executeReqCmd(
                SimpleKeyReq(Constants.CMD_BP_TIMING_MONITOR_DATA),
                object : ICommandResponse<BpDataRsp> {
                    override fun onDataResponse(resultEntity: BpDataRsp) {
                        try {
                            val entries = resultEntity.bpDataEntity?.bpValues ?: return
                            val now = System.currentTimeMillis()
                            val points = entries.map { e -> BPReading(timestamp = now, systolic = e.timeMinute, diastolic = e.value, heartRate = e.value) }
                            _readings.value = (_readings.value + points).sortedBy { it.timestamp }
                        } catch (_: Exception) {}
                    }
                }
            )
        } catch (_: Exception) {}
    }

    private fun syncBpManualInline() {
        try {
            commandHandle.executeReqCmd(
                ReadPressureReq(0),
                object : ICommandResponse<ReadBlePressureRsp> {
                    override fun onDataResponse(resultEntity: ReadBlePressureRsp) {
                        try {
                            val list = resultEntity.valueList ?: emptyList()
                            val now = System.currentTimeMillis()
                            val points = list.map { v -> BPReading(timestamp = now, systolic = v.sbp, diastolic = v.dbp, heartRate = 0) }
                            _readings.value = (_readings.value + points).sortedBy { it.timestamp }
                        } catch (_: Exception) {}
                    }
                }
            )
        } catch (_: Exception) {}
    }

    private fun toggleBpInline(enabled: Boolean) {
        try {
            commandHandle.executeReqCmd(
                BpSettingReq.getWriteInstance(enabled, null, 60),
                object : ICommandResponse<BpSettingRsp> {
                    override fun onDataResponse(resultEntity: BpSettingRsp) { /* no-op */ }
                }
            )
        } catch (_: Exception) {}
    }
}


