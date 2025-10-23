package dev.infa.page3.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oudmon.ble.base.bluetooth.DeviceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * SleepViewModel following MVVM pattern
 * Handles sleep data synchronization and UI state management
 */
class SleepViewModel(
    private val dataSynchronization: DataSynchronization,
    private val deviceAddress: String = ""
) : ViewModel()
{

    companion object {
        private const val TAG = "SleepViewModel"
    }

    // UI State
    private val _uiState = MutableStateFlow<SleepUiState>(SleepUiState.Loading)
    val uiState: StateFlow<SleepUiState> = _uiState.asStateFlow()

    // Selected date
    private val _selectedDate = MutableStateFlow(getCurrentDate())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Sleep data storage (date -> SleepData)
    private val _sleepDataMap = MutableStateFlow<Map<String, SleepData>>(emptyMap())
    val sleepDataMap: StateFlow<Map<String, SleepData>> = _sleepDataMap.asStateFlow()

    // Sync logs
    private val _syncLogs = MutableStateFlow<List<String>>(emptyList())
    val syncLogs: StateFlow<List<String>> = _syncLogs.asStateFlow()

    // Sync status
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    init {
        setupDataCallbacks()
        loadInitialData()
    }

    /**
     * Setup callbacks for data synchronization
     */
    private fun setupDataCallbacks() {
        dataSynchronization.setSleepDataCallback { sleepData ->
            viewModelScope.launch {
                try {
                    val currentMap = _sleepDataMap.value.toMutableMap()
                    currentMap[sleepData.date] = sleepData
                    _sleepDataMap.value = currentMap
                    
                    addSyncLog("Sleep data received for ${sleepData.date}")
                    updateUiState()
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing sleep data callback", e)
                    addSyncLog("ERROR: Failed to process sleep data: ${e.message}")
                }
            }
        }
    }

    /**
     * Load initial sleep data for the past 7 days
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = SleepUiState.Loading
            addSyncLog("Starting sleep data sync for 7 days...")
            syncSleepData(7)
        }
    }

    /**
     * Sync sleep data from device for specified number of days
     */
    fun syncSleepData(days: Int = 7) {
        viewModelScope.launch {
            try {
                _isSyncing.value = true
                _uiState.value = SleepUiState.Loading
                addSyncLog("Starting sleep data sync for $days days...")

                withContext(Dispatchers.IO) {
                    // Sync data for each day
                    for (dayOffset in 0 until days) {
                        val date = getDateForOffset(dayOffset)
                        addSyncLog("Syncing sleep data for $date (offset: $dayOffset)")

                        // Sync sleep data using SDK
                        dataSynchronization.syncSleepData(getActualDeviceAddress(), dayOffset)

                        // Small delay to prevent overwhelming device
                        delay(1000)
                    }
                }

                addSyncLog("Sleep data sync completed")
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
     * Sync sleep data for a specific date
     */
    fun syncSleepDataForDate(date: String) {
        viewModelScope.launch {
            try {
                val dayOffset = getDayOffsetForDate(date)
                addSyncLog("Syncing sleep data for specific date: $date (offset: $dayOffset)")
                
                _isSyncing.value = true
                
                withContext(Dispatchers.IO) {
                    dataSynchronization.syncSleepData(getActualDeviceAddress(), dayOffset)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing sleep data for date: $date", e)
                addSyncLog("ERROR: Failed to sync data for $date: ${e.message}")
            } finally {
                _isSyncing.value = false
            }
        }
    }

    /**
     * Select a specific date to view
     */
    fun selectDate(date: String) {
        _selectedDate.value = date
        updateUiState()
        
        // Check if we have data for this date, if not, try to sync
        if (!_sleepDataMap.value.containsKey(date)) {
            addSyncLog("No data found for $date, attempting to sync...")
            syncSleepDataForDate(date)
        }
    }

    /**
     * Navigate to previous day
     */
    fun navigateToPreviousDay() {
        val currentDate = _selectedDate.value
        val previousDate = getPreviousDate(currentDate)
        selectDate(previousDate)
    }

    /**
     * Navigate to next day
     */
    fun navigateToNextDay() {
        val currentDate = _selectedDate.value
        val nextDate = getNextDate(currentDate)
        selectDate(nextDate)
    }

    /**
     * Get sleep data for selected date
     */
    fun getSleepDataForSelectedDate(): SleepData? {
        return _sleepDataMap.value[_selectedDate.value]
    }

    /**
     * Get sleep data for a specific date
     */
    fun getSleepDataForDate(date: String): SleepData? {
        return _sleepDataMap.value[date]
    }

    /**
     * Get sleep data for date range (for weekly view)
     */
    fun getSleepDataForWeek(): List<SleepData> {
        val weekDates = getWeekDates(_selectedDate.value)
        return weekDates.mapNotNull { _sleepDataMap.value[it] }
    }

    /**
     * Get all available sleep data
     */
    fun getAllSleepData(): List<SleepData> {
        return _sleepDataMap.value.values.toList().sortedByDescending { it.date }
    }

    /**
     * Refresh current data
     */
    fun refreshData() {
        viewModelScope.launch {
            addSyncLog("Refreshing sleep data...")
            syncSleepData(7)
        }
    }

    /**
     * Clear all sleep data
     */
    fun clearAllData() {
        viewModelScope.launch {
            _sleepDataMap.value = emptyMap()
            addSyncLog("All sleep data cleared")
            updateUiState()
        }
    }

    /**
     * Update UI state based on available data
     */
    private fun updateUiState() {
        val selectedData = _sleepDataMap.value[_selectedDate.value]

        _uiState.value = when {
            selectedData != null -> SleepUiState.Success(listOf(selectedData))
            _sleepDataMap.value.isEmpty() -> SleepUiState.Empty
            else -> SleepUiState.Success(_sleepDataMap.value.values.toList())
        }
    }

    /**
     * Add log message
     */
    private fun addSyncLog(message: String) {
        val logs = _syncLogs.value.toMutableList()
        logs.add(0, "${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}: $message")
        _syncLogs.value = logs.take(50) // Keep last 50 logs
    }

    /**
     * Get the actual device address to use for sync
     */
    private fun getActualDeviceAddress(): String {
        return if (deviceAddress.isNotEmpty()) {
            deviceAddress
        } else {
            DeviceManager.getInstance().deviceAddress.ifEmpty { 
                addSyncLog("WARNING: No device address available, using empty string")
                ""
            }
        }
    }

    // Utility Functions

    /**
     * Get current date
     */
    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    /**
     * Get date for offset
     */
    private fun getDateForOffset(offset: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -offset)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }

    /**
     * Get day offset for a specific date
     */
    private fun getDayOffsetForDate(date: String): Int {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val targetDate = dateFormat.parse(date) ?: Date()
        val today = Date()
        
        val diffInMillis = today.time - targetDate.time
        return (diffInMillis / (24 * 60 * 60 * 1000)).toInt()
    }

    /**
     * Get previous date
     */
    private fun getPreviousDate(currentDate: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.time = dateFormat.parse(currentDate) ?: Date()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return dateFormat.format(calendar.time)
    }

    /**
     * Get next date
     */
    private fun getNextDate(currentDate: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.time = dateFormat.parse(currentDate) ?: Date()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        return dateFormat.format(calendar.time)
    }

    /**
     * Get week dates
     */
    private fun getWeekDates(centerDate: String): List<String> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.time = dateFormat.parse(centerDate) ?: Date()

        val dates = mutableListOf<String>()
        for (i in -3..3) {
            val tempCalendar = calendar.clone() as Calendar
            tempCalendar.add(Calendar.DAY_OF_YEAR, i)
            dates.add(dateFormat.format(tempCalendar.time))
        }
        return dates
    }

    /**
     * Get device address - implement based on your device manager
     * TODO: Integrate with actual device manager
     */
    private fun getDeviceAddress(): String {
        // TODO: Get from your BLE device manager
        return DeviceManager.getInstance().deviceAddress
    }

    /**
     * Get sleep statistics for the selected period
     */
    fun getSleepStatistics(): SleepStatistics {
        val allData = getAllSleepData()
        
        if (allData.isEmpty()) {
            return SleepStatistics()
        }

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
            dataRange = if (allData.isNotEmpty()) {
                "${allData.last().date} to ${allData.first().date}"
            } else ""
        )
    }
}

/**
 * UI State for Sleep Screen
 */
sealed class SleepUiState {
    object Loading : SleepUiState()
    data class Success(val sleepData: List<SleepData>) : SleepUiState()
    data class Error(val message: String) : SleepUiState()
    object Empty : SleepUiState()
}

/**
 * Sleep statistics data class
 */
data class SleepStatistics(
    val averageSleepTime: Double = 0.0,           // Average sleep time in hours
    val averageSleepScore: Double = 0.0,          // Average sleep score
    val averageSleepEfficiency: Double = 0.0,     // Average sleep efficiency
    val bestSleepScore: Int = 0,                  // Best sleep score
    val worstSleepScore: Int = 0,                 // Worst sleep score
    val totalDaysTracked: Int = 0,                // Total days tracked
    val dataRange: String = ""                    // Date range of data
) {
    /**
     * Get formatted average sleep time
     */
    fun getFormattedAverageSleepTime(): String {
        val hours = averageSleepTime.toInt()
        val minutes = ((averageSleepTime - hours) * 60).toInt()
        return "${hours}h ${minutes}m"
    }

    /**
     * Get formatted average sleep score
     */
    fun getFormattedAverageSleepScore(): String {
        return String.format("%.1f", averageSleepScore)
    }

    /**
     * Get formatted average sleep efficiency
     */
    fun getFormattedAverageSleepEfficiency(): String {
        return String.format("%.1f%%", averageSleepEfficiency)
    }
}