package dev.infa.page3.viewmodels


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oudmon.ble.base.communication.CommandHandle
import com.oudmon.ble.base.communication.LargeDataHandler
import com.oudmon.ble.base.communication.bigData.BloodOxygenEntity
import com.oudmon.ble.base.communication.req.BloodOxygenSettingReq
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import com.oudmon.ble.base.bluetooth.BleOperateManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.Calendar
class BloodOxygenViewModel(
    private val cacheManager: HealthMetricsCacheManager
) : ViewModel()
{

    private val _spo2Data = MutableStateFlow<SpO2Data?>(null)
    val spo2Data: StateFlow<SpO2Data?> = _spo2Data.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    fun measureSpO2Once(onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                BleOperateManager.getInstance().manualModeSpO2({ result ->
                    val spo2Value = result.value
                    if (spo2Value > 0 && spo2Value <= 100) {
                        onResult("SpO2: $spo2Value%")
                    } else {
                        onResult("Measurement failed or invalid")
                    }
                }, false)
            } catch (e: Exception) {
                onResult("Exception: ${e.message}")
            }
        }
    }

    fun syncSpO2DataForDay(
        offset: Int,
        onSuccess: (SpO2Data) -> Unit,
        onError: (String) -> Unit,
        forceRefresh: Boolean = false
    ) {
        viewModelScope.launch {
            if (!forceRefresh) {
                cacheManager.getData<SpO2Data>("spo2", offset)?.let { cached ->
                    _spo2Data.value = cached
                    onSuccess(cached)
                    Log.d("SpO2VM", "✅ Using cached SpO2 data")
                }
            }

            _isSyncing.value = true
            withContext(Dispatchers.IO) {
                try {
                    LargeDataHandler.getInstance().syncBloodOxygenWithCallback { entityList ->
                        if (!entityList.isNullOrEmpty()) {
                            val spo2Data = convertSpO2Data(entityList, offset)
                            _spo2Data.value = spo2Data
                            onSuccess(spo2Data)
                            cacheManager.saveData("spo2", offset, spo2Data)
                            Log.d("SpO2VM", "✅ Synced SpO2 data")
                        } else {
                            onError("No SpO2 data available")
                        }
                        _isSyncing.value = false
                    }
                } catch (e: Exception) {
                    onError("Exception: ${e.message}")
                    _isSyncing.value = false
                }
            }
        }
    }

    fun toggleSpO2Monitoring(enabled: Boolean, onComplete: () -> Unit) {
        try {
            CommandHandle.getInstance().executeReqCmd(
                BloodOxygenSettingReq.getWriteInstance(enabled),
                null
            )
            onComplete()
        } catch (e: Exception) {
            Log.e("SpO2VM", "Exception: ${e.message}")
        }
    }

    private fun convertSpO2Data(entityList: List<BloodOxygenEntity>, offset: Int): SpO2Data {
        val spo2Values = mutableListOf<SpO2Entry>()
        entityList.forEach { entity ->
            val baseTime = resolveBaseTime(entity)
            val values = entity.maxArray ?: entity.minArray ?: emptyList()
            values.forEachIndexed { hourIndex, value ->
                if (value in 1..100) {
                    spo2Values.add(
                        SpO2Entry(
                            timestamp = baseTime / 1000 + (hourIndex * 60 * 60),
                            spo2Value = value,
                            hourOfDay = hourIndex
                        )
                    )
                }
            }
        }

        val avgSpO2 = if (spo2Values.isNotEmpty())
            spo2Values.map { it.spo2Value }.average().toInt() else 0

        return SpO2Data(
            date = getDateForOffset(offset),
            spo2Values = spo2Values,
            averageSpO2 = avgSpO2,
            maxSpO2 = spo2Values.maxOfOrNull { it.spo2Value } ?: 0,
            minSpO2 = spo2Values.minOfOrNull { it.spo2Value } ?: 0
        )
    }

    private fun resolveBaseTime(entity: BloodOxygenEntity): Long {
        val unix = entity.unix_time
        if (unix > 0) return if (unix >= 1_000_000_000_000L) unix else unix * 1000

        entity.dateStr?.let { dateStr ->
            try {
                return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .parse(dateStr)?.time ?: System.currentTimeMillis()
            } catch (_: Exception) { }
        }

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getDateForOffset(offset: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -offset)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }

    fun forceRefresh(offset: Int, onSuccess: (SpO2Data) -> Unit, onError: (String) -> Unit) {
        cacheManager.clearMetricCache("spo2")
        syncSpO2DataForDay(offset, onSuccess, onError, forceRefresh = true)
    }
}

data class SpO2Data(
    val date: String = "",
    val spo2Values: List<SpO2Entry> = emptyList(),
    val averageSpO2: Int = 0,
    val maxSpO2: Int = 0,
    val minSpO2: Int = 0
) {
    fun getFormattedAverageSpO2(): String = "$averageSpO2%"

    fun getSpO2Status(): String {
        return when {
            averageSpO2 < 90 -> "Low"
            averageSpO2 < 95 -> "Normal"
            else -> "Excellent"
        }
    }
}

data class SpO2Entry(
    val timestamp: Long = 0,
    val spo2Value: Int = 0,
    val hourOfDay: Int = 0
)
