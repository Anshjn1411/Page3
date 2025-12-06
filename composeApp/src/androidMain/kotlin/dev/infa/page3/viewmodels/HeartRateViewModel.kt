package dev.infa.page3.viewmodels

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.communication.CommandHandle
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.req.*
import com.oudmon.ble.base.communication.rsp.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*


class HealthMetricsCacheManager(context: Context) {

    val prefs: SharedPreferences =
        context.getSharedPreferences("health_metrics_cache", Context.MODE_PRIVATE)
    val gson = Gson()

    companion object {
        private const val CACHE_VALIDITY_MS = 5 * 60 * 1000L // 5 minutes
        private const val KEY_LAST_SYNC = "last_sync_"
    }

    // Generic save method
    fun <T> saveData(metricType: String, offset: Int, data: T) {
        val key = "${metricType}_$offset"
        val json = gson.toJson(data)
        prefs.edit()
            .putString(key, json)
            .putLong("${KEY_LAST_SYNC}$key", System.currentTimeMillis())
            .apply()
    }

    // Generic get method
    inline fun <reified T> getData(metricType: String, offset: Int): T? {
        val key = "${metricType}_$offset"
        if (!isCacheValid(key)) return null

        val json = prefs.getString(key, null) ?: return null
        return try {
            gson.fromJson(json, T::class.java)
        } catch (e: Exception) {
            Log.e("Cache", "Failed to parse $metricType: ${e.message}")
            null
        }
    }

    // Check if cache is valid
    fun isCacheValid(key: String): Boolean {
        val lastSync = prefs.getLong("${KEY_LAST_SYNC}$key", 0)
        return System.currentTimeMillis() - lastSync < CACHE_VALIDITY_MS
    }

    // Clear specific metric cache
    fun clearMetricCache(metricType: String) {
        val editor = prefs.edit()
        prefs.all.keys.filter { it.startsWith(metricType) }.forEach { key ->
            editor.remove(key)
            editor.remove("${KEY_LAST_SYNC}$key")
        }
        editor.apply()
    }

    // Clear all cache
    fun clearAllCache() {
        prefs.edit().clear().apply()
    }
}

class HeartRateViewModel(
    private val cacheManager: HealthMetricsCacheManager
) : ViewModel() {

    private val _heartRateData = MutableStateFlow<HeartRateData?>(null)
    val heartRateData: StateFlow<HeartRateData?> = _heartRateData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    init {
        readHeartRateSettingsInline()
    }

    fun measureHeartRateOnce(onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                Log.i("HeartRateVM", "Starting manual heart rate measurement...")
                BleOperateManager.getInstance().manualModeHeart({ result ->
                    val hrValue = result.value.toInt()
                    val errCode = result.errCode.toInt()
                    if (errCode == 0 && hrValue > 0) {
                        Log.i("HeartRateVM", "Manual HR measured: $hrValue bpm")
                        onResult("Heart Rate: $hrValue bpm")
                    } else {
                        Log.w("HeartRateVM", "Measurement failed or invalid")
                        onResult("Measurement failed or invalid")
                    }
                }, false)
            } catch (e: Exception) {
                Log.e("HeartRateVM", "Exception: ${e.message}")
                onResult("Exception: ${e.message}")
            }
        }
    }

    fun syncHeartRateDataForDay(
        offset: Int,
        onSuccess: (HeartRateData) -> Unit,
        onError: (String) -> Unit,
        forceRefresh: Boolean = false
    ) {
        viewModelScope.launch {
            // Try cache first
            if (!forceRefresh) {
                cacheManager.getData<HeartRateData>("heart_rate", offset)?.let { cached ->
                    _heartRateData.value = cached
                    onSuccess(cached)
                    Log.d("HeartRateVM", "✅ Using cached HR data for offset $offset")
                }
            }

            // Sync from device in background
            _isSyncing.value = true
            withContext(Dispatchers.IO) {
                try {
                    val baseTime = getCurrentTimeWithTimezone()
                    val secondsInDay = 24 * 60 * 60
                    val targetTime = baseTime.toLong() - (offset * secondsInDay)

                    CommandHandle.getInstance().executeReqCmd(
                        ReadHeartRateReq(targetTime),
                        object : ICommandResponse<ReadHeartRateRsp> {
                            override fun onDataResponse(resultEntity: ReadHeartRateRsp) {
                                if (resultEntity.status == BaseRspCmd.RESULT_OK) {
                                    val hrData = convertHeartRateData(resultEntity, offset)

                                    // Update UI
                                    _heartRateData.value = hrData
                                    onSuccess(hrData)

                                    // Save to cache
                                    cacheManager.saveData("heart_rate", offset, hrData)

                                    Log.d("HeartRateVM", "✅ Synced HR data for offset $offset")
                                } else {
                                    onError("Failed to sync heart rate data")
                                }
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

    fun toggleHeartRateInline(enabled: Boolean, interval: Int) {
        try {
            CommandHandle.getInstance().executeReqCmd(
                HeartRateSettingReq.getWriteInstance(enabled, interval),
                object : ICommandResponse<HeartRateSettingRsp> {
                    override fun onDataResponse(resultEntity: HeartRateSettingRsp) {
                        if (resultEntity.status == BaseRspCmd.RESULT_OK) {
                            Log.d("HeartRateVM", "Monitoring ${if (enabled) "enabled" else "disabled"}")
                        }
                    }
                }
            )
        } catch (_: Exception) { }
    }

    private fun readHeartRateSettingsInline() {
        try {
            CommandHandle.getInstance().executeReqCmd(
                HeartRateSettingReq.getReadInstance(),
                object : ICommandResponse<HeartRateSettingRsp> {
                    override fun onDataResponse(resultEntity: HeartRateSettingRsp) {
                        if (resultEntity.status == BaseRspCmd.RESULT_OK) {
                            Log.i("HeartRateVM", "HR Settings: interval=${resultEntity.heartInterval}, enabled=${resultEntity.isEnable}")
                        }
                    }
                }
            )
        } catch (e: Exception) {
            Log.e("HeartRateVM", "Exception reading HR settings: ${e.message}")
        }
    }

    private fun getCurrentTimeWithTimezone(): Int {
        return try {
            val timeZoneHours = TimeZone.getDefault().rawOffset / (1000 * 60 * 60)
            val currentTimeSeconds = System.currentTimeMillis() / 1000
            (timeZoneHours * 3600 + currentTimeSeconds).toInt()
        } catch (e: Exception) {
            Log.e("HeartRateVM", "Exception getting time: ${e.message}")
            0
        }
    }

    private fun convertHeartRateData(response: ReadHeartRateRsp, offset: Int): HeartRateData {
        val heartRateValues = mutableListOf<HeartRateEntry>()
        response.getmHeartRateArray()?.let { hrArray ->
            for (i in hrArray.indices) {
                val hr = hrArray[i].toInt()
                if (hr > 0) {
                    heartRateValues.add(
                        HeartRateEntry(
                            timestamp = response.getmUtcTime().toLong() + i * 5 * 60,
                            heartRate = hr,
                            minuteOfDay = i * 5
                        )
                    )
                }
            }
        }
        val avgHR = if (heartRateValues.isNotEmpty())
            heartRateValues.map { it.heartRate }.average().toInt() else 0

        return HeartRateData(
            date = getDateForOffset(offset),
            heartRateValues = heartRateValues,
            averageHeartRate = avgHR,
            maxHeartRate = heartRateValues.maxOfOrNull { it.heartRate } ?: 0,
            minHeartRate = heartRateValues.minOfOrNull { it.heartRate } ?: 0
        )
    }

    private fun getDateForOffset(offset: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -offset)
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return format.format(calendar.time)
    }

    fun forceRefresh(offset: Int, onSuccess: (HeartRateData) -> Unit, onError: (String) -> Unit) {
        cacheManager.clearMetricCache("heart_rate")
        syncHeartRateDataForDay(offset, onSuccess, onError, forceRefresh = true)
    }
}
data class HeartRateData(
    val date: String = "",
    val heartRateValues: List<HeartRateEntry> = emptyList(),
    val averageHeartRate: Int = 0,
    val maxHeartRate: Int = 0,
    val minHeartRate: Int = 0
)

data class HeartRateEntry(
    val timestamp: Long = 0,
    val heartRate: Int = 0,
    val minuteOfDay: Int = 0
)



