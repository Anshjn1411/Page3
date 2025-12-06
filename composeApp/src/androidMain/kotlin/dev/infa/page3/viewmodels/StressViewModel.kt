package dev.infa.page3.viewmodels


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.communication.CommandHandle
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.req.BpSettingReq
import com.oudmon.ble.base.communication.req.ReadPressureReq
import com.oudmon.ble.base.communication.rsp.BaseRspCmd
import com.oudmon.ble.base.communication.rsp.BpSettingRsp
import com.oudmon.ble.base.communication.rsp.ReadBlePressureRsp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.text.toInt

class BloodPressureViewModel(
    private val cacheManager: HealthMetricsCacheManager
) : ViewModel()
{

    private val _bpData = MutableStateFlow<BpData?>(null)
    val bpData: StateFlow<BpData?> = _bpData.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    fun measureBpOnce(onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                BleOperateManager.getInstance().manualModePressure({ result ->
                    val systolic = result.value
                    if (systolic > 0) {
                        onResult("BP: $systolic mmHg")
                    } else {
                        onResult("Measurement failed")
                    }
                }, false)
            } catch (e: Exception) {
                onResult("Exception: ${e.message}")
            }
        }
    }

    fun syncBpDataForDay(
        offset: Int,
        onSuccess: (BpData) -> Unit,
        onError: (String) -> Unit,
        forceRefresh: Boolean = false
    ) {
        viewModelScope.launch {
            if (!forceRefresh) {
                cacheManager.getData<BpData>("bp", offset)?.let { cached ->
                    _bpData.value = cached
                    onSuccess(cached)
                    Log.d("BpVM", "✅ Using cached BP data")
                }
            }

            _isSyncing.value = true
            withContext(Dispatchers.IO) {
                try {
                    CommandHandle.getInstance().executeReqCmd(
                        com.oudmon.ble.base.communication.req.PressureReq(offset.toByte()),
                        object : ICommandResponse<com.oudmon.ble.base.communication.rsp.PressureRsp> {
                            override fun onDataResponse(resultEntity: com.oudmon.ble.base.communication.rsp.PressureRsp) {
                                val bpData = convertBpData(resultEntity, offset)
                                _bpData.value = bpData
                                onSuccess(bpData)
                                cacheManager.saveData("bp", offset, bpData)
                                Log.d("BpVM", "✅ Synced BP data")
                                _isSyncing.value = false
                            }
                        }
                    )
                } catch (e: Exception) {
                    onError("Exception: ${e.message}")
                    _isSyncing.value = false
                }
            }
        }
    }

    fun toggleBpMonitoring(enabled: Boolean, onComplete: () -> Unit) {
        try {
            CommandHandle.getInstance().executeReqCmd(
                com.oudmon.ble.base.communication.req.PressureSettingReq.getWriteInstance(enabled),
                null
            )
            onComplete()
        } catch (e: Exception) {
            Log.e("BpVM", "Exception: ${e.message}")
        }
    }

    private fun convertBpData(
        response: com.oudmon.ble.base.communication.rsp.PressureRsp,
        offset: Int
    ): BpData {
        val bpValues = mutableListOf<BpEntry>()
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -offset)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val baseTimestamp = calendar.timeInMillis / 1000
        val intervalMinutes = response.range.coerceAtLeast(1)

        response.pressureArray?.forEachIndexed { i, byte ->
            val value = (byte.toInt() and 0xFF)
            if (value in 60..200) {
                bpValues.add(
                    BpEntry(
                        timestamp = baseTimestamp + (i * intervalMinutes * 60),
                        systolic = value,
                        diastolic = (value * 0.7).toInt(),
                        minuteOfDay = i * intervalMinutes
                    )
                )
            }
        }

        return BpData(
            date = getDateForOffset(offset),
            bpValues = bpValues,
            averageSystolic = if (bpValues.isNotEmpty()) bpValues.map { it.systolic }.average().toInt() else 0,
            averageDiastolic = if (bpValues.isNotEmpty()) bpValues.map { it.diastolic }.average().toInt() else 0,
            maxSystolic = bpValues.maxOfOrNull { it.systolic } ?: 0,
            minSystolic = bpValues.minOfOrNull { it.systolic } ?: 0
        )
    }

    private fun getDateForOffset(offset: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -offset)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }

    fun forceRefresh(offset: Int, onSuccess: (BpData) -> Unit, onError: (String) -> Unit) {
        cacheManager.clearMetricCache("bp")
        syncBpDataForDay(offset, onSuccess, onError, forceRefresh = true)
    }
}
data class BpData(
    val date: String = "",
    val bpValues: List<BpEntry> = emptyList(),
    val averageSystolic: Int = 0,
    val averageDiastolic: Int = 0,
    val maxSystolic: Int = 0,
    val minSystolic: Int = 0
) {
    fun getFormattedAverageBp(): String = "$averageSystolic/$averageDiastolic mmHg"

    fun getBpStatus(): String {
        return when {
            averageSystolic >= 140 || averageDiastolic >= 90 -> "High"
            averageSystolic >= 130 || averageDiastolic >= 80 -> "Elevated"
            else -> "Normal"
        }
    }
}

data class BpEntry(
    val timestamp: Long = 0,
    val systolic: Int = 0,
    val diastolic: Int = 0,
    val minuteOfDay: Int = 0
)
