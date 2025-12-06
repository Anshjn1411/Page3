package dev.infa.page3.viewmodels

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.oudmon.ble.base.communication.CommandHandle
import com.oudmon.ble.base.communication.Constants
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.entity.BleStepDetails
import com.oudmon.ble.base.communication.req.ReadDetailSportDataReq
import com.oudmon.ble.base.communication.req.SimpleKeyReq
import com.oudmon.ble.base.communication.rsp.BaseRspCmd
import com.oudmon.ble.base.communication.rsp.ReadDetailSportDataRsp
import com.oudmon.ble.base.communication.rsp.TodaySportDataRsp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*


class StepCacheManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("step_cache", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val CACHE_VALIDITY_MS = 5 * 60 * 1000L // 5 minutes
        private const val KEY_TODAY_STEPS = "today_steps"
        private const val KEY_DAY_DATA = "day_data_"
        private const val KEY_WEEKLY_SUMMARY = "weekly_summary"
        private const val KEY_LAST_SYNC = "last_sync_"
    }

    // Save today's steps
    fun saveTodaySteps(steps: Int) {
        prefs.edit()
            .putInt(KEY_TODAY_STEPS, steps)
            .putLong("${KEY_LAST_SYNC}today", System.currentTimeMillis())
            .apply()
    }

    // Get today's steps
    fun getTodaySteps(): Int? {
        if (!isCacheValid("today")) return null
        return prefs.getInt(KEY_TODAY_STEPS, -1).takeIf { it != -1 }
    }

    // Save day data
    fun saveDayData(offset: Int, data: DayStepData) {
        val json = gson.toJson(data)
        prefs.edit()
            .putString("$KEY_DAY_DATA$offset", json)
            .putLong("${KEY_LAST_SYNC}day_$offset", System.currentTimeMillis())
            .apply()
    }

    // Get day data
    fun getDayData(offset: Int): DayStepData? {
        if (!isCacheValid("day_$offset")) return null
        val json = prefs.getString("$KEY_DAY_DATA$offset", null) ?: return null
        return try {
            gson.fromJson(json, DayStepData::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // Save weekly summary
    fun saveWeeklySummary(summary: WeeklySummary) {
        val json = gson.toJson(summary)
        prefs.edit()
            .putString(KEY_WEEKLY_SUMMARY, json)
            .putLong("${KEY_LAST_SYNC}weekly", System.currentTimeMillis())
            .apply()
    }

    // Get weekly summary
    fun getWeeklySummary(): WeeklySummary? {
        if (!isCacheValid("weekly")) return null
        val json = prefs.getString(KEY_WEEKLY_SUMMARY, null) ?: return null
        return try {
            gson.fromJson(json, WeeklySummary::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // Check if cache is still valid (within 5 minutes)
    private fun isCacheValid(key: String): Boolean {
        val lastSync = prefs.getLong("${KEY_LAST_SYNC}$key", 0)
        return System.currentTimeMillis() - lastSync < CACHE_VALIDITY_MS
    }

    // Clear all cache
    fun clearCache() {
        prefs.edit().clear().apply()
    }
}

class StepAnalyticsViewModel(
    private val cacheManager: StepCacheManager
) : ViewModel()
{

    private val commandHandle = CommandHandle.getInstance()

    // UI States
    private val _todaySteps = MutableStateFlow(0)
    val todaySteps: StateFlow<Int> = _todaySteps.asStateFlow()

    private val _hourlyData = MutableStateFlow<List<HourlyStepData>>(emptyList())
    val hourlyData: StateFlow<List<HourlyStepData>> = _hourlyData.asStateFlow()

    private val _weeklySummary = MutableStateFlow(WeeklySummary())
    val weeklySummary: StateFlow<WeeklySummary> = _weeklySummary.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    init {
        // Load cached data immediately, then sync in background
        loadCachedData()
        fetchLast7DaysData()
    }

    // ========================================
    // LOAD CACHED DATA IMMEDIATELY
    // ========================================

    private fun loadCachedData() {
        viewModelScope.launch {
            // Load today's steps from cache
            cacheManager.getTodaySteps()?.let { cached ->
                _todaySteps.value = cached
                Log.d("StepVM", "✅ Loaded cached today steps: $cached")
            }

            // Load weekly summary from cache
            cacheManager.getWeeklySummary()?.let { cached ->
                _weeklySummary.value = cached
                Log.d("StepVM", "✅ Loaded cached weekly summary")
            }
        }
    }

    // ========================================
    // 1️⃣ TODAY STEPS (Cache + Background Sync)
    // ========================================

    fun fetchTodaySteps(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            // Return cached data immediately if available
            if (!forceRefresh) {
                cacheManager.getTodaySteps()?.let { cached ->
                    _todaySteps.value = cached
                    Log.d("StepVM", "✅ Using cached today steps: $cached")
                }
            }

            // Sync from device in background
            _isSyncing.value = true
            withContext(Dispatchers.IO) {
                try {
                    commandHandle.executeReqCmd(
                        SimpleKeyReq(Constants.CMD_GET_STEP_TODAY),
                        object : ICommandResponse<TodaySportDataRsp> {
                            override fun onDataResponse(result: TodaySportDataRsp) {
                                if (result.status == BaseRspCmd.RESULT_OK) {
                                    val steps = result.sportTotal.totalSteps

                                    // Update UI
                                    _todaySteps.value = steps

                                    // Save to cache
                                    cacheManager.saveTodaySteps(steps)

                                    Log.d("StepVM", "✅ Synced today steps: $steps")
                                }
                                _isSyncing.value = false
                            }
                        }
                    )
                } catch (e: Exception) {
                    Log.e("StepVM", "❌ Today step sync error", e)
                    _isSyncing.value = false
                }
            }
        }
    }

    // ========================================
    // 2️⃣ ANY DAY BY OFFSET (Cache + Background Sync)
    // ========================================

    fun fetchStepsByOffset(dayOffset: Int, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            // Return cached data immediately if available
            if (!forceRefresh) {
                cacheManager.getDayData(dayOffset)?.let { cached ->
                    _todaySteps.value = cached.totalSteps
                    _hourlyData.value = cached.hourlyData
                    Log.d("StepVM", "✅ Using cached day $dayOffset data")
                }
            }

            // Sync from device in background
            _isSyncing.value = true
            getStepDataForOffset(dayOffset) { dayData ->
                // Update UI
                _todaySteps.value = dayData.totalSteps
                _hourlyData.value = dayData.hourlyData

                // Save to cache
                cacheManager.saveDayData(dayOffset, dayData)

                Log.d("StepVM", "✅ Synced day $dayOffset data")
                _isSyncing.value = false
            }
        }
    }

    // ========================================
    // 3️⃣ FETCH LAST 7 DAYS (Cache + Background Sync)
    // ========================================

    private fun fetchLast7DaysData() {
        viewModelScope.launch {
            _isLoading.value = true
            val last7DaysData = mutableListOf<DayStepData>()

            withContext(Dispatchers.IO) {
                // First, try to load from cache
                var cacheHits = 0
                for (i in 0..6) {
                    cacheManager.getDayData(i)?.let { cached ->
                        last7DaysData.add(cached)
                        cacheHits++
                    }
                }

                if (cacheHits == 7) {
                    // All data available in cache, update UI immediately
                    withContext(Dispatchers.Main) {
                        calculateWeeklySummary(last7DaysData)
                        Log.d("StepVM", "✅ Loaded all 7 days from cache")
                    }
                }

                // Now sync from device in background
                last7DaysData.clear()
                for (i in 0..6) {
                    getStepDataForOffset(i) { dayData ->
                        last7DaysData.add(dayData)

                        // Save to cache
                        cacheManager.saveDayData(i, dayData)

                        // Update UI as data comes in
                        if (last7DaysData.size == 7) {
                            calculateWeeklySummary(last7DaysData)
                            _isLoading.value = false
                            Log.d("StepVM", "✅ Synced all 7 days from device")
                        }
                    }
                }
            }
        }
    }

    // ========================================
    // 4️⃣ WEEKLY SUMMARY CALCULATION
    // ========================================

    private fun calculateWeeklySummary(last7DaysData: List<DayStepData>) {
        val bestDay = last7DaysData.maxByOrNull { it.totalSteps }
        val avgSteps = last7DaysData.map { it.totalSteps }.average().toInt()
        val avgCalories = last7DaysData.map { it.totalCalories }.average().toLong()

        val summary = WeeklySummary(
            bestDay = bestDay,
            averageSteps = avgSteps,
            averageCalories = avgCalories,
            allDays = last7DaysData
        )

        _weeklySummary.value = summary

        // Save to cache
        cacheManager.saveWeeklySummary(summary)

        Log.d("StepVM", "✅ Weekly Summary: Best=${bestDay?.totalSteps}, Avg=$avgSteps")
    }

    // ========================================
    // 5️⃣ FORCE REFRESH (Clear cache and reload)
    // ========================================

    fun forceRefresh() {
        Log.d("StepVM", "🔄 Force refresh triggered")
        cacheManager.clearCache()
        fetchTodaySteps(forceRefresh = true)
        fetchLast7DaysData()
    }

    // ========================================
    // HELPER FUNCTIONS (Same as before)
    // ========================================

    private fun convertToHourlyData(details: List<BleStepDetails>): List<HourlyStepData> {
        return details.map { item ->
            val totalMinutes = item.timeIndex * 15
            val hour = totalMinutes / 60
            HourlyStepData(
                hour = hour,
                steps = item.walkSteps,
                calories = item.calorie
            )
        }
    }

    private fun getStepDataForOffset(
        dayOffset: Int,
        onResult: (DayStepData) -> Unit
    ) {
        commandHandle.executeReqCmd(
            ReadDetailSportDataReq(dayOffset, 0, 95),
            object : ICommandResponse<ReadDetailSportDataRsp> {
                override fun onDataResponse(result: ReadDetailSportDataRsp) {
                    if (result.status != BaseRspCmd.RESULT_OK) return

                    val list = result.bleStepDetailses
                    val totalSteps = list.sumOf { it.walkSteps }
                    val totalCalories = list.sumOf { it.calorie }
                    val totalDistance = list.sumOf { it.distance }
                    val hourly = convertToHourlyData(list)

                    val date = if (list.isNotEmpty()) {
                        "${list.first().year}-${list.first().month}-${list.first().day}"
                    } else {
                        getDateForOffset(dayOffset)
                    }

                    onResult(
                        DayStepData(
                            date = date,
                            totalSteps = totalSteps,
                            totalCalories = totalCalories,
                            totalDistance = totalDistance,
                            hourlyData = hourly
                        )
                    )
                }
            }
        )
    }

    private fun getDateForOffset(offset: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -offset)
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return format.format(calendar.time)
    }
}

data class HourlyStepData(
    val hour: Int,
    val steps: Int,
    val calories: Int
)

data class DayStepData(
    val date: String,
    val totalSteps: Int,
    val totalCalories: Int,
    val totalDistance: Int,
    val hourlyData: List<HourlyStepData>
)

data class WeeklySummary(
    val bestDay: DayStepData? = null,
    val averageSteps: Int = 0,
    val averageCalories: Long = 0,
    val allDays: List<DayStepData> = emptyList()
)


class StepAnalyticsViewModelFactory(
    private val context: Context
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StepAnalyticsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StepAnalyticsViewModel(StepCacheManager(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
