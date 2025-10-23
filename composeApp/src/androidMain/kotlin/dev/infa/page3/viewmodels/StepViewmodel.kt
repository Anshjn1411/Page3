package dev.infa.page3.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StepViewmodel(
    private val dataSynchronization: DataSynchronization
) : ViewModel() {

    companion object {
        private const val TAG = "StepViewModel"
    }

    // Store all synced data by date
    private val _stepDataMap = MutableStateFlow<Map<String, StepData>>(emptyMap())

    // Currently selected date
    private val _selectedDate = MutableStateFlow(getTodayDate())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Data for selected date
    private val _selectedStepData = MutableStateFlow<StepData?>(null)
    val selectedStepData: StateFlow<StepData?> = _selectedStepData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        setupCallback()
        syncData()
    }

    /**
     * Setup callback - when data syncs, store it by date
     */
    private fun setupCallback() {
        dataSynchronization.setStepDataCallback { stepData ->
            viewModelScope.launch {
                // Store data in map
                val updatedMap = _stepDataMap.value.toMutableMap()
                updatedMap[stepData.date] = stepData
                _stepDataMap.value = updatedMap

                // Update selected data if it matches
                if (stepData.date == _selectedDate.value) {
                    _selectedStepData.value = stepData
                }

                _isLoading.value = false
                Log.d(TAG, "Data received for ${stepData.date}: ${stepData.totalSteps} steps")
            }
        }
    }

    /**
     * Sync data for today
     */
    fun syncData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                withContext(Dispatchers.IO) {
                    dataSynchronization.syncTodaySteps()
                }

            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = e.message
                Log.e(TAG, "Sync error", e)
            }
        }
    }

    /**
     * Sync data for specific date by day offset
     */
    fun syncDataForDate(date: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val dayOffset = calculateDayOffset(date)

                withContext(Dispatchers.IO) {
                    dataSynchronization.syncDetailStepData(dayOffset)
                }

            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = e.message
                Log.e(TAG, "Sync error for date $date", e)
            }
        }
    }

    /**
     * Select a date to view
     */
    fun selectDate(date: String) {
        _selectedDate.value = date

        // Get data from map
        val data = _stepDataMap.value[date]
        _selectedStepData.value = data

        // If no data, sync it
        if (data == null) {
            syncDataForDate(date)
        }
    }

    /**
     * Get all available dates
     */
    fun getAvailableDates(): List<String> {
        return _stepDataMap.value.keys.sortedDescending()
    }

    /**
     * Get all step data map (for week calendar view)
     */
    fun getAllStepData(): Map<String, StepData> {
        return _stepDataMap.value
    }

    /**
     * Get step data for specific date
     */
    fun getStepDataForDate(date: String): StepData? {
        return _stepDataMap.value[date]
    }

    /**
     * Get week dates around selected date
     */
    fun getWeekDates(): List<String> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        try {
            calendar.time = dateFormat.parse(_selectedDate.value) ?: Date()
        } catch (e: Exception) {
            calendar.time = Date()
        }

        // Get to Monday of the week
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val diff = if (dayOfWeek == Calendar.SUNDAY) -6 else Calendar.MONDAY - dayOfWeek
        calendar.add(Calendar.DAY_OF_YEAR, diff)

        val weekDates = mutableListOf<String>()
        for (i in 0..6) {
            weekDates.add(dateFormat.format(calendar.time))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return weekDates
    }

    /**
     * Navigate to previous day
     */
    fun goToPreviousDay() {
        val previousDate = getPreviousDate(_selectedDate.value)
        selectDate(previousDate)
    }

    /**
     * Navigate to next day
     */
    fun goToNextDay() {
        val nextDate = getNextDate(_selectedDate.value)
        val today = getTodayDate()
        if (nextDate <= today) {
            selectDate(nextDate)
        }
    }

    /**
     * Go to today
     */
    fun goToToday() {
        selectDate(getTodayDate())
    }

    /**
     * Check if data exists for date
     */
    fun hasDataForDate(date: String): Boolean {
        return _stepDataMap.value.containsKey(date)
    }

    /**
     * Sync multiple dates (e.g., current week)
     */
    fun syncWeekData() {
        viewModelScope.launch {
            val weekDates = getWeekDates()
            weekDates.forEach { date ->
                if (!hasDataForDate(date)) {
                    syncDataForDate(date)
                }
            }
        }
    }

    /**
     * Clear all data
     */
    fun clearData() {
        _stepDataMap.value = emptyMap()
        _selectedStepData.value = null
    }

    /**
     * Refresh current date
     */
    fun refreshCurrentDate() {
        syncDataForDate(_selectedDate.value)
    }

    // Helper functions
    private fun getTodayDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun calculateDayOffset(date: String): Int {
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

    /**
     * Calculate activity score for a given date
     */
    fun getActivityScoreForDate(date: String): Int {
        val stepData = _stepDataMap.value[date] ?: return 0

        val stepScore = (stepData.totalSteps.toFloat() / 5000f * 40).toInt()
        val distanceScore = (stepData.distance.toFloat() / 3000f * 30).toInt()
        val calorieScore = (stepData.calories.toFloat() / 300f * 30).toInt()

        return (stepScore + distanceScore + calorieScore).coerceIn(0, 100)
    }

    /**
     * Get activity level text for a given score
     */
    fun getActivityLevelForDate(date: String): String {
        val score = getActivityScoreForDate(date)
        return when {
            score >= 80 -> "Very active"
            score >= 60 -> "Active"
            score >= 40 -> "Moderate"
            score >= 20 -> "Light exercise"
            else -> "Less exercise"
        }
    }

    /**
     * Get total steps for current week
     */
    fun getWeekTotalSteps(): Long {
        val weekDates = getWeekDates()
        return weekDates.sumOf { date ->
            _stepDataMap.value[date]?.totalSteps ?: 0
        }
    }

    /**
     * Get average steps for current week
     */
    fun getWeekAverageSteps(): Int {
        val weekDates = getWeekDates()
        val datesWithData = weekDates.filter { _stepDataMap.value.containsKey(it) }

        if (datesWithData.isEmpty()) return 0

        val totalSteps = datesWithData.sumOf { date ->
            _stepDataMap.value[date]?.totalSteps ?: 0
        }

        return  (totalSteps / datesWithData.size).toInt()
    }
}