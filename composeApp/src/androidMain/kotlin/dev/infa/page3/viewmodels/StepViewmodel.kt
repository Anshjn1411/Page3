package dev.infa.page3.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oudmon.ble.base.communication.CommandHandle
import com.oudmon.ble.base.communication.Constants
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.req.ReadDetailSportDataReq
import com.oudmon.ble.base.communication.req.SimpleKeyReq
import com.oudmon.ble.base.communication.rsp.BaseRspCmd
import com.oudmon.ble.base.communication.rsp.ReadDetailSportDataRsp
import com.oudmon.ble.base.communication.rsp.TodaySportDataRsp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class StepViewmodel(
    private val commandHandle: CommandHandle? = CommandHandle.getInstance()
) : ViewModel() {
    companion object {
        private const val TAG = "StepViewModel"
    }

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

    // Track ongoing sync operations to prevent duplicates
    private val syncingDates = mutableSetOf<String>()
    private val syncLock = Any()

    init {
        // Auto-sync today's data on init
        syncTodaySteps()
    }

    /**
     * Sync today's step data - Direct implementation
     */
    fun syncTodaySteps() {
        if (commandHandle == null) {
            _errorMessage.value = "CommandHandle not initialized"
            return
        }

        val todayDate = getTodayDate()

        // Prevent duplicate syncs
        synchronized(syncLock) {
            if (syncingDates.contains(todayDate)) {
                Log.d(TAG, "Already syncing today's data, skipping...")
                return
            }
            syncingDates.add(todayDate)
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                commandHandle.executeReqCmd(
                    SimpleKeyReq(Constants.CMD_GET_STEP_TODAY),
                    object : ICommandResponse<TodaySportDataRsp> {
                        override fun onDataResponse(resultEntity: TodaySportDataRsp) {
                            synchronized(syncLock) {
                                syncingDates.remove(todayDate)
                            }

                            try {
                                if (resultEntity.status == BaseRspCmd.RESULT_OK) {
                                    val stepData = convertTodaySportDataToStepData(resultEntity)

                                    // Store in map
                                    val updatedMap = _stepDataMap.value.toMutableMap()
                                    updatedMap[stepData.date] = stepData
                                    _stepDataMap.value = updatedMap

                                    // Update selected if it's today
                                    if (stepData.date == _selectedDate.value) {
                                        _selectedStepData.value = stepData
                                    }

                                    _isLoading.value = false
                                    Log.d(TAG, "Today's steps synced: ${stepData.totalSteps}")
                                } else {
                                    _errorMessage.value = "Failed to sync - Status: ${resultEntity.status}"
                                    _isLoading.value = false
                                }
                            } catch (e: Exception) {
                                _errorMessage.value = "Error processing data: ${e.message}"
                                _isLoading.value = false
                                Log.e(TAG, "Processing error", e)
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                synchronized(syncLock) {
                    syncingDates.remove(todayDate)
                }
                _errorMessage.value = "Sync error: ${e.message}"
                _isLoading.value = false
                Log.e(TAG, "Sync error", e)
            }
        }
    }

    /**
     * Sync step data for specific date - Direct implementation
     */
    fun syncStepDataForDate(date: String) {
        if (commandHandle == null) {
            _errorMessage.value = "CommandHandle not initialized"
            return
        }

        // Prevent duplicate syncs for same date
        synchronized(syncLock) {
            if (syncingDates.contains(date)) {
                Log.d(TAG, "Already syncing data for $date, skipping...")
                return
            }
            syncingDates.add(date)
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val dayOffset = calculateDayOffset(date)

            try {
                commandHandle.executeReqCmd(
                    ReadDetailSportDataReq(dayOffset, 0, 95),
                    object : ICommandResponse<ReadDetailSportDataRsp> {
                        override fun onDataResponse(resultEntity: ReadDetailSportDataRsp) {
                            synchronized(syncLock) {
                                syncingDates.remove(date)
                            }

                            try {
                                if (resultEntity.status == BaseRspCmd.RESULT_OK) {
                                    val stepData = convertDetailStepData(resultEntity, dayOffset)

                                    // Store in map
                                    val updatedMap = _stepDataMap.value.toMutableMap()
                                    updatedMap[stepData.date] = stepData
                                    _stepDataMap.value = updatedMap

                                    // Update selected if matches
                                    if (stepData.date == _selectedDate.value) {
                                        _selectedStepData.value = stepData
                                    }

                                    _isLoading.value = false
                                    Log.d(TAG, "Step data synced for $date: ${stepData.totalSteps}")
                                } else {
                                    _errorMessage.value = "Failed to sync - Status: ${resultEntity.status}"
                                    _isLoading.value = false
                                }
                            } catch (e: Exception) {
                                _errorMessage.value = "Error processing data: ${e.message}"
                                _isLoading.value = false
                                Log.e(TAG, "Processing error", e)
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                synchronized(syncLock) {
                    syncingDates.remove(date)
                }
                _errorMessage.value = "Sync error: ${e.message}"
                _isLoading.value = false
                Log.e(TAG, "Sync error", e)
            }
        }
    }

    /**
     * Select a date to view
     */
    fun selectDate(date: String) {
        _selectedDate.value = date
        val data = _stepDataMap.value[date]
        _selectedStepData.value = data

        // If no data exists, fetch it
        if (data == null) {
            syncStepDataForDate(date)
        }
    }

    /**
     * Convert today's sport data to StepData
     */
    private fun convertTodaySportDataToStepData(response: TodaySportDataRsp): StepData {
        return StepData(
            date = getTodayDate(),
            totalSteps = response.sportTotal.totalSteps.toLong(),
            calories = response.sportTotal.calorie.toLong(),
            distance = response.sportTotal.walkDistance.toLong(),
            sleepDuration = 0
        )
    }

    /**
     * Convert detailed step data to StepData
     */
    private fun convertDetailStepData(response: ReadDetailSportDataRsp, dayOffset: Int): StepData {
        val firstData = response.bleStepDetailses.firstOrNull()
        val date = if (firstData != null) {
            "${firstData.year}-${firstData.month.toString().padStart(2, '0')}-${firstData.day.toString().padStart(2, '0')}"
        } else {
            getDateForOffset(dayOffset)
        }

        // Calculate totals from detailed data
        var totalSteps = 0L
        var totalCalories = 0L
        var totalDistance = 0L

        response.bleStepDetailses.forEach { detail ->
            totalSteps += detail.walkSteps
            totalCalories += detail.calorie
            totalDistance += detail.distance
        }

        return StepData(
            date = date,
            totalSteps = totalSteps,
            runningSteps = 0,
            calories = totalCalories,
            distance = totalDistance,
            sportDuration = 0,
            sleepDuration = 0
        )
    }

    /**
     * Get date for offset
     */
    private fun getDateForOffset(dayOffset: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -dayOffset)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }

    /**
     * Calculate day offset from date string
     */
    private fun calculateDayOffset(date: String): Int {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val targetDate = dateFormat.parse(date) ?: Date()
        val today = Date()
        val diffInMillis = today.time - targetDate.time
        return (diffInMillis / (24 * 60 * 60 * 1000)).toInt()
    }

    /**
     * Get today's date as string
     */
    private fun getTodayDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    /**
     * Refresh current selected date
     */
    fun refreshCurrentDate() {
        if (_selectedDate.value == getTodayDate()) {
            syncTodaySteps()
        } else {
            syncStepDataForDate(_selectedDate.value)
        }
    }

    /**
     * Get step data for specific date
     */
    fun getStepDataForDate(date: String): StepData? {
        return _stepDataMap.value[date]
    }

    /**
     * Navigate to previous day
     */
    fun goToPreviousDay() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.time = dateFormat.parse(_selectedDate.value) ?: Date()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        selectDate(dateFormat.format(calendar.time))
    }

    /**
     * Navigate to next day
     */
    fun goToNextDay() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.time = dateFormat.parse(_selectedDate.value) ?: Date()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val nextDate = dateFormat.format(calendar.time)

        if (nextDate <= getTodayDate()) {
            selectDate(nextDate)
        }
    }
    fun hasDataForDate(date: String): Boolean {
        return _stepDataMap.value.containsKey(date)
    }

    /**
     * Go to today
     */
    fun goToToday() {
        selectDate(getTodayDate())
    }
}