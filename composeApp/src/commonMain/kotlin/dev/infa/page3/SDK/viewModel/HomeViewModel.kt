package dev.infa.page3.SDK.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.infa.page3.SDK.data.DeviceCapabilities
import dev.infa.page3.SDK.data.ExerciseData
import dev.infa.page3.SDK.data.ExerciseSummary
import dev.infa.page3.SDK.data.HeartRateData
import dev.infa.page3.SDK.ui.utils.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

class HomeViewModel(
    private val homeManager: HomeManager
)
{

    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _batteryValue = MutableStateFlow<Int?>(0)
    val batteryValue: StateFlow<Int?> = _batteryValue.asStateFlow()

    private val _todaySleep = MutableStateFlow<SleepData?>(null)
    val todaySleep: StateFlow<SleepData?> = _todaySleep.asStateFlow()

    private val _deviceCapabilities = MutableStateFlow<DeviceCapabilities?>(null)
    val deviceCapabilities: StateFlow<DeviceCapabilities?> = _deviceCapabilities.asStateFlow()

    private val _stepGoal = MutableStateFlow(5000)
    val stepGoal: StateFlow<Int> = _stepGoal.asStateFlow()

    private val _calorieGoal = MutableStateFlow(250)
    val calorieGoal: StateFlow<Int> = _calorieGoal.asStateFlow()

    private val _distanceGoal = MutableStateFlow(4000)
    val distanceGoal: StateFlow<Int> = _distanceGoal.asStateFlow()

    private val _goalSetSuccess = MutableStateFlow<Boolean?>(null)
    val goalSetSuccess: StateFlow<Boolean?> = _goalSetSuccess.asStateFlow()

    // Exercise states
    private val _currentExercise = MutableStateFlow<ExerciseData?>(null)
    val currentExercise: StateFlow<ExerciseData?> = _currentExercise.asStateFlow()

    private val _lastExerciseSummary = MutableStateFlow<ExerciseSummary?>(null)
    val lastExerciseSummary: StateFlow<ExerciseSummary?> = _lastExerciseSummary.asStateFlow()

    init {
        createHardcodedSleepData()
    }

    // ========================================
    // Battery & Device Info
    // ========================================

    fun getBatteryLevel() {
        viewModelScope.launch {
            try {
                val battery = homeManager.getBatteryLevel()
                _batteryValue.value = battery
            } catch (e: Exception) {
                _errorMessage.value = "Failed to get battery: ${e.message}"
            }
        }
    }

    fun fetchDeviceCapabilities() {
        viewModelScope.launch {
            try {
                val capabilities = homeManager.fetchDeviceCapabilities()
                _deviceCapabilities.value = capabilities
            } catch (e: Exception) {
                _errorMessage.value = "Failed to fetch capabilities: ${e.message}"
            }
        }
    }

    // ========================================
    // Goals Management
    // ========================================

    fun setSportsGoals(
        stepGoal: Int = 10000,
        calorieGoal: Int = 500,
        distanceGoal: Int = 8000,
        sportMinuteGoal: Int = 30,
        sleepMinuteGoal: Int = 480
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _goalSetSuccess.value = null

                val success = homeManager.setSportsGoals(
                    stepGoal,
                    calorieGoal,
                    distanceGoal,
                    sportMinuteGoal,
                    sleepMinuteGoal
                )

                if (success) {
                    _stepGoal.value = stepGoal
                    _calorieGoal.value = calorieGoal
                    _distanceGoal.value = distanceGoal
                    _goalSetSuccess.value = true
                } else {
                    _errorMessage.value = "Failed to set goals"
                    _goalSetSuccess.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error setting goals: ${e.message}"
                _goalSetSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateStepGoal(newGoal: Int) {
        _stepGoal.value = newGoal
    }

    fun updateCalorieGoal(newGoal: Int) {
        _calorieGoal.value = newGoal
    }

    fun updateDistanceGoal(newGoal: Int) {
        _distanceGoal.value = newGoal
    }

    fun clearGoalSetStatus() {
        _goalSetSuccess.value = null
    }

    // ========================================
    // Exercise Management
    // ========================================

    fun startExercise(sportType: Int) {
        homeManager.startExercise(
            sportType = sportType,
            onUpdate = { data ->
                _currentExercise.value = data
            },
            onEnd = { summary ->
                _lastExerciseSummary.value = summary
                _currentExercise.value = null
            },
            onError = { error ->
                _errorMessage.value = error
            }
        )
    }

    fun pauseExercise() {
        homeManager.pauseExercise()
    }

    fun resumeExercise() {
        homeManager.resumeExercise()
    }

    fun endExercise() {
        homeManager.endExercise()
    }

    fun isExercising(): Boolean {
        return homeManager.isExercising()
    }

    fun isPaused(): Boolean {
        return homeManager.isPaused()
    }

    fun clearLastSummary() {
        _lastExerciseSummary.value = null
    }

    // ========================================
    // Helpers
    // ========================================

    private fun createHardcodedSleepData() {
        val currentDate = DateUtils.getCurrentDate()
        val dateString = "${currentDate.year}-${currentDate.month.toString().padStart(2, '0')}-${currentDate.day.toString().padStart(2, '0')}"

        val sleepData = SleepData(
            date = dateString,
            totalDuration = 420.0,
            awakeDuration = 30,
            sleepScore = 85,
            sleepEfficiency = 90.0,
            deepSleep = 0,
            lightSleep = 0,
            remSleep = 0,
            sleepStartTime = "",
            sleepEndTime = "",
            stages = emptyList()
        )
        _todaySleep.value = sleepData
    }
}
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
@Serializable
data class SleepStage(
    val type: String,
    val startTime: Long,
    val endTime: Long,
    val duration: Int
)



// ============================================
// commonMain/utils/ExerciseUtils.kt
// ============================================

object ExerciseUtils {
    fun getSportName(sportType: Int): String {
        return when (sportType) {
            1 -> "GPS Run"
            2 -> "GPS Bike"
            3 -> "GPS Walk"
            4 -> "Walking"
            5 -> "Rope Skipping"
            6 -> "Swimming"
            7 -> "Running"
            8 -> "Hiking"
            9 -> "Cycling"
            10 -> "Exercise"
            20 -> "Climb"
            21 -> "Badminton"
            22 -> "Yoga"
            23 -> "Aerobics"
            24 -> "Spinning"
            31 -> "Basketball"
            32 -> "Football"
            33 -> "Volleyball"
            40 -> "Treadmill"
            50 -> "Outdoor Cycling"
            55 -> "Swimming Pool"
            80 -> "Stair Climber"
            88 -> "Strength Training"
            110 -> "Square Dance"
            130 -> "Boxing"
            150 -> "Beach Football"
            160 -> "Bowling"
            else -> "Sport $sportType"
        }
    }

    fun getPopularSportTypes(): List<Pair<Int, String>> {
        return listOf(
            1 to "Running",
            2 to "Cycling",
            3 to "Walking",
            4 to "Hiking",
            5 to "Rope Skipping",
            6 to "Swimming",
            22 to "Yoga",
            31 to "Basketball",
            32 to "Football",
            88 to "Strength Training"
        )
    }

    fun calculateStrain(exerciseData: ExerciseData): Float {
        val durationFactor = (exerciseData.elapsedSeconds / 60f) * 0.15f
        val hrFactor = if (exerciseData.heartRate > 100) {
            (exerciseData.heartRate - 100) * 0.05f
        } else {
            0f
        }
        return (durationFactor + hrFactor).coerceIn(0f, 21f)
    }
}

// ============================================
// commonMain/platform/HomeManager.kt - Expect
// ============================================

/**
 * Platform-specific home/health data manager
 */
expect class HomeManager {
    /**
     * Get battery level from device
     */
    suspend fun getBatteryLevel(): Int?

    /**
     * Fetch device capabilities
     */
    suspend fun fetchDeviceCapabilities(): DeviceCapabilities?

    /**
     * Set sports goals on device
     */
    suspend fun setSportsGoals(
        stepGoal: Int,
        calorieGoal: Int,
        distanceGoal: Int,
        sportMinuteGoal: Int,
        sleepMinuteGoal: Int
    ): Boolean

    /**
     * Start exercise tracking
     */
    fun startExercise(
        sportType: Int,
        onUpdate: (ExerciseData) -> Unit,
        onEnd: (ExerciseSummary) -> Unit,
        onError: (String) -> Unit
    )

    /**
     * Pause current exercise
     */
    fun pauseExercise()

    /**
     * Resume paused exercise
     */
    fun resumeExercise()

    /**
     * End current exercise
     */
    fun endExercise()

    /**
     * Check if currently exercising
     */
    fun isExercising(): Boolean

    /**
     * Check if exercise is paused
     */
    fun isPaused(): Boolean

    /**
     * Cleanup resources
     */
    fun cleanup()
}



