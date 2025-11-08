package dev.infa.page3.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oudmon.ble.base.bean.SleepDisplay
import com.oudmon.ble.base.communication.ILargeDataLaunchSleepResponse
import com.oudmon.ble.base.communication.ILargeDataSleepResponse
import com.oudmon.ble.base.communication.LargeDataHandler
import com.oudmon.ble.base.communication.rsp.SleepNewProtoResp
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.text.SimpleDateFormat
import java.util.*

data class SleepData(
    val date: String,
    val totalDuration: Double,
    val deepSleep: Int,
    val lightSleep: Int,
    val remSleep: Int,
    val awakeDuration: Int,
    val sleepScore: Int,
    val sleepEfficiency: Double,
    val sleepStartTime: String,
    val sleepEndTime: String,
    val stages: List<SleepStage> = emptyList()
) {
    fun getFormattedTotalSleep(): String {
        val hours = totalDuration.toInt()
        val minutes = ((totalDuration - hours) * 60).toInt()
        return "${hours}h ${minutes}m"
    }

    fun getSleepQuality(): String {
        return when (sleepScore) {
            in 90..100 -> "Excellent"
            in 75..89 -> "Good"
            in 60..74 -> "Fair"
            else -> "Poor"
        }
    }
}

data class SleepStage(
    val type: String,
    val startTime: Long,
    val endTime: Long,
    val duration: Int
)

data class SleepStatistics(
    val averageSleepTime: Double = 0.0,
    val averageSleepScore: Double = 0.0,
    val averageSleepEfficiency: Double = 0.0,
    val bestSleepScore: Int = 0,
    val worstSleepScore: Int = 0,
    val totalDaysTracked: Int = 0,
    val dataRange: String = ""
) {
    fun getFormattedAverageSleepTime(): String {
        val hours = averageSleepTime.toInt()
        val minutes = ((averageSleepTime - hours) * 60).toInt()
        return "${hours}h ${minutes}m"
    }

    fun getFormattedAverageSleepScore(): String {
        return String.format("%.1f", averageSleepScore)
    }

    fun getFormattedAverageSleepEfficiency(): String {
        return String.format("%.1f%%", averageSleepEfficiency)
    }
}

sealed class SleepUiState {
    object Loading : SleepUiState()
    data class Success(val sleepData: List<SleepData>) : SleepUiState()
    data class Error(val message: String) : SleepUiState()
    object Empty : SleepUiState()
}

class SleepViewModel : ViewModel() {

    companion object {
        private const val TAG = "SleepViewModel"
        private const val TIMEOUT_MS = 6000L // 6 seconds to mirror sample responsiveness
        private const val DELAY_BETWEEN_REQUESTS = 800L // 800ms - CRITICAL for SDK
        private const val PROTOCOL_FALLBACK_DELAY_MS = 300L // kept for future, not used in strict sample mode
    }

    private val _uiState = MutableStateFlow<SleepUiState>(SleepUiState.Loading)
    val uiState: StateFlow<SleepUiState> = _uiState.asStateFlow()

    private val _selectedDate = MutableStateFlow(getYesterdayDate())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _sleepDataMap = MutableStateFlow<Map<String, SleepData>>(emptyMap())
    val sleepDataMap: StateFlow<Map<String, SleepData>> = _sleepDataMap.asStateFlow()

    private val _syncLogs = MutableStateFlow<List<String>>(emptyList())
    val syncLogs: StateFlow<List<String>> = _syncLogs.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = SleepUiState.Loading
            addSyncLog("Starting initial sleep data sync...")
            addSyncLog("NOTE: Starting from yesterday as today may have no data yet")
            syncSleepData(7, startOffset = 1)
        }
    }

    /**
     * CRITICAL FIX: Must process requests SEQUENTIALLY with proper delays
     * The SDK's respMap can only handle ONE callback at a time per cmdType
     */
    fun syncSleepData(days: Int = 7, startOffset: Int = 1) {
        viewModelScope.launch {
            try {
                _isSyncing.value = true
                _uiState.value = SleepUiState.Loading
                addSyncLog("Syncing $days days of sleep data (starting from offset $startOffset)...")

                var successCount = 0
                var failCount = 0

                // SEQUENTIAL processing with delays between requests
                for (i in 0 until days) {
                    val dayOffset = startOffset + i
                    val date = getDateForOffset(dayOffset)
                    addSyncLog("Syncing sleep data for $date (offset: $dayOffset)")

                    // Wait for this day's data
                    val success = syncSingleDaySleep(dayOffset, date)

                    if (success) {
                        successCount++
                        addSyncLog("✓ Successfully synced data for $date")
                    } else {
                        failCount++
                        addSyncLog("✗ No data available for $date")
                    }

                    // CRITICAL: Wait before next request to avoid callback collision
                    if (i < days - 1) {
                        delay(DELAY_BETWEEN_REQUESTS)
                    }
                }

                addSyncLog("Sync completed: $successCount success, $failCount no data")
                updateUiState()

            } catch (e: Exception) {
                Log.e(TAG, "Error syncing sleep data", e)
                addSyncLog("ERROR: ${e.message}")
                _uiState.value = SleepUiState.Error(e.message ?: "Unknown error")
            } finally {
                _isSyncing.value = false
            }
        }
    }

    /**
     * FIXED: Synchronous callback handling with proper completion signaling
     */
    private suspend fun syncSingleDaySleep(dayOffset: Int, date: String): Boolean {
        // Ensure BLE is connected before attempting a request; wait briefly if needed
        waitForBleConnection()

        return withContext(Dispatchers.Main) {
            // Pause background health ops to prevent SDK callback collisions during large-data transfer
            try {
                dev.infa.page3.connection.BleConnectionService.suspendHealthOps(TIMEOUT_MS + 1000)
            } catch (_: Exception) {}
            try {
                Log.d(TAG, "═══ Sleep sync for offset $dayOffset (IndianDemand ONLY, per sample) ═══")

                // Strict sample mode: use only IndianDemand and await callback; retry once if null/timeout
                fun request(callback: ILargeDataSleepResponse) {
                    LargeDataHandler.getInstance().syncSleepListIndianDemand(dayOffset, callback)
                }

                suspend fun awaitOnce(): Boolean {
                    val deferred = CompletableDeferred<Boolean>()
                    request(object : ILargeDataSleepResponse {
                        override fun sleepData(sleepDisplay: SleepDisplay?) {
                            val handled = handleSleepCallback(date, sleepDisplay) != null
                            deferred.complete(handled)
                        }
                    })
                    addSyncLog("Request sent (IndianDemand) for $date...")
                    return withTimeoutOrNull(TIMEOUT_MS) { deferred.await() } ?: false
                }

                awaitOnce()

            } catch (e: Exception) {
                Log.e(TAG, "Exception in syncSingleDaySleep", e)
                addSyncLog("ERROR: ${e.message}")
                false
            }
        }
    }

    private fun handleSleepCallback(date: String, sleepDisplay: SleepDisplay?): SleepData? {
        return try {
            if (sleepDisplay == null) return null
            if (sleepDisplay.totalSleepDuration <= 0) return null
            val sleepData = convertToSleepData(sleepDisplay, date)
            val currentMap = _sleepDataMap.value.toMutableMap()
            currentMap[date] = sleepData
            _sleepDataMap.value = currentMap
            addSyncLog("✓ Sleep data: ${sleepData.getFormattedTotalSleep()} for $date")
            Log.d(TAG, "  Deep: ${sleepData.deepSleep} min, Light: ${sleepData.lightSleep} min")
            updateUiState()
            sleepData
        } catch (e: Exception) {
            Log.e(TAG, "Error converting sleep data", e)
            addSyncLog("ERROR: ${e.message}")
            null
        }
    }

    private suspend fun waitForBleConnection(timeoutMs: Long = 8000L) {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < timeoutMs) {
            try {
                val connected = com.oudmon.ble.base.bluetooth.BleOperateManager.getInstance().isConnected
                if (connected) return
            } catch (_: Exception) {}
            delay(250)
        }
    }

    private fun convertToSleepData(sleepDisplay: SleepDisplay, date: String): SleepData {
        val deepMinutes = sleepDisplay.deepSleepDuration / 60
        val lightMinutes = sleepDisplay.shallowSleepDuration / 60
        val remMinutes = sleepDisplay.rapidDuration / 60
        val awakeMinutes = sleepDisplay.awakeDuration / 60
        val totalMinutes = sleepDisplay.totalSleepDuration / 60
        val totalHours = totalMinutes / 60.0

        val sleepScore = calculateSleepScore(deepMinutes, lightMinutes, remMinutes, totalMinutes)

        val sleepEfficiency = if (totalMinutes > 0) {
            ((deepMinutes + lightMinutes + remMinutes).toDouble() / totalMinutes * 100)
        } else {
            0.0
        }

        val sleepStartTime = formatTimestamp(sleepDisplay.sleepTime.toLong())
        val sleepEndTime = formatTimestamp(sleepDisplay.wakeTime.toLong())
        val stages = convertSleepStages(sleepDisplay)

        return SleepData(
            date = date,
            totalDuration = totalHours,
            deepSleep = deepMinutes,
            lightSleep = lightMinutes,
            remSleep = remMinutes,
            awakeDuration = awakeMinutes,
            sleepScore = sleepScore,
            sleepEfficiency = sleepEfficiency,
            sleepStartTime = sleepStartTime,
            sleepEndTime = sleepEndTime,
            stages = stages
        )
    }

    private fun calculateSleepScore(deep: Int, light: Int, rem: Int, total: Int): Int {
        if (total == 0) return 0
        val deepPercentage = (deep * 100.0 / total)
        val remPercentage = (rem * 100.0 / total)
        var score = 100
        when {
            total < 300 -> score -= 30
            total < 360 -> score -= 15
            total > 600 -> score -= 10
        }
        when {
            deepPercentage < 10 -> score -= 25
            deepPercentage < 15 -> score -= 15
            deepPercentage < 18 -> score -= 5
        }
        when {
            remPercentage < 15 -> score -= 20
            remPercentage < 18 -> score -= 10
            remPercentage < 20 -> score -= 5
        }
        return score.coerceIn(0, 100)
    }

    private fun convertSleepStages(sleepDisplay: SleepDisplay): List<SleepStage> {
        val stages = mutableListOf<SleepStage>()
        sleepDisplay.list?.forEach { bean ->
            val type = when (bean.type) {
                1 -> "Deep"
                2 -> "Light"
                3 -> "Awake"
                4 -> "REM"
                else -> "Unknown"
            }
            val duration = ((bean.sleepEnd - bean.sleepStart) / 60).toInt()
            stages.add(SleepStage(type, bean.sleepStart, bean.sleepEnd, duration))
        }
        return stages
    }

    private fun formatTimestamp(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            sdf.format(Date(timestamp * 1000))
        } catch (e: Exception) {
            "N/A"
        }
    }

    fun syncSleepDataForDate(date: String) {
        viewModelScope.launch {
            try {
                val dayOffset = getDayOffsetForDate(date)
                addSyncLog("Syncing $date (offset: $dayOffset)")
                _isSyncing.value = true
                val success = syncSingleDaySleep(dayOffset, date)
                addSyncLog(if (success) "✓ Data synced for $date" else "✗ No data for $date")
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing date: $date", e)
                addSyncLog("ERROR: ${e.message}")
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun selectDate(date: String) {
        _selectedDate.value = date
        updateUiState()
        if (!_sleepDataMap.value.containsKey(date)) {
            addSyncLog("No cached data for $date, syncing...")
            syncSleepDataForDate(date)
        }
    }

    fun navigateToPreviousDay() {
        val currentDate = _selectedDate.value
        val previousDate = getPreviousDate(currentDate)
        selectDate(previousDate)
    }

    fun navigateToNextDay() {
        val currentDate = _selectedDate.value
        val today = getCurrentDate()
        val nextDate = getNextDate(currentDate)
        if (nextDate <= today) {
            selectDate(nextDate)
        }
    }

    fun getSleepDataForSelectedDate(): SleepData? = _sleepDataMap.value[_selectedDate.value]
    fun getSleepDataForDate(date: String): SleepData? = _sleepDataMap.value[date]
    fun getAllSleepData(): List<SleepData> = _sleepDataMap.value.values.toList().sortedByDescending { it.date }

    fun refreshData() {
        viewModelScope.launch {
            addSyncLog("Refreshing...")
            syncSleepData(7, startOffset = 1)
        }
    }

    fun clearAllData() {
        _sleepDataMap.value = emptyMap()
        addSyncLog("Data cleared")
        updateUiState()
    }

    private fun updateUiState() {
        val selectedData = _sleepDataMap.value[_selectedDate.value]
        _uiState.value = when {
            _isSyncing.value -> SleepUiState.Loading
            selectedData != null -> SleepUiState.Success(listOf(selectedData))
            _sleepDataMap.value.isEmpty() -> SleepUiState.Empty
            else -> SleepUiState.Success(_sleepDataMap.value.values.toList())
        }
    }

    private fun addSyncLog(message: String) {
        val logs = _syncLogs.value.toMutableList()
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        logs.add(0, "$timestamp: $message")
        Log.d(TAG, message)
        _syncLogs.value = logs.take(100)
    }

    fun getSleepStatistics(): SleepStatistics {
        val allData = getAllSleepData()
        if (allData.isEmpty()) return SleepStatistics()
        val totalSleepTimes = allData.map { it.totalDuration }
        val sleepScores = allData.map { it.sleepScore }
        val sleepEfficiencies = allData.map { it.sleepEfficiency }
        return SleepStatistics(
            averageSleepTime = totalSleepTimes.average(),
            averageSleepScore = sleepScores.average(),
            averageSleepEfficiency = sleepEfficiencies.average(),
            bestSleepScore = sleepScores.maxOrNull() ?: 0,
            worstSleepScore = sleepScores.minOrNull() ?: 0,
            totalDaysTracked = allData.size,
            dataRange = if (allData.isNotEmpty()) "${allData.last().date} to ${allData.first().date}" else ""
        )
    }

    private fun getCurrentDate() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    private fun getYesterdayDate(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }
    private fun getDateForOffset(offset: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -offset)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }
    private fun getDayOffsetForDate(date: String): Int {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val targetDate = dateFormat.parse(date) ?: Date()
        val today = Date()
        val diffInMillis = today.time - targetDate.time
        return (diffInMillis / (24 * 60 * 60 * 1000)).toInt()
    }
    private fun getPreviousDate(currentDate: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.time = dateFormat.parse(currentDate) ?: Date()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return dateFormat.format(calendar.time)
    }
    private fun getNextDate(currentDate: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.time = dateFormat.parse(currentDate) ?: Date()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        return dateFormat.format(calendar.time)
    }
}