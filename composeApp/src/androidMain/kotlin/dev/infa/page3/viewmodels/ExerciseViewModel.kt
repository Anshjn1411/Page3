package dev.infa.page3.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.communication.CommandHandle
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.req.PhoneSportReq
import com.oudmon.ble.base.communication.responseImpl.DeviceSportNotifyListener
import com.oudmon.ble.base.communication.rsp.AppSportRsp
import com.oudmon.ble.base.communication.rsp.BaseRspCmd
import com.oudmon.ble.base.communication.rsp.DeviceNotifyRsp
import com.oudmon.ble.base.communication.sport.SportPlusHandle
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ExerciseViewModel : ViewModel() {

    private var tickJob: Job? = null
    private var lastStartTimestamp: Long = 0

    // Current exercise tracking
    var isExercising = false
        private set
    var isPaused = false
        private set
    var currentSportType: Int? = null
        private set
    var elapsedSeconds = 0
        private set
    var heartRate = 0
        private set
    var steps = 0
        private set
    var distanceMeters = 0
        private set
    var calories = 0
        private set

    // Callbacks
    private var onExerciseUpdate: ((ExerciseData) -> Unit)? = null
    private var onExerciseEnded: ((ExerciseSummary) -> Unit)? = null
    private var onError: ((String) -> Unit)? = null

    // Device response handlers
    private val gpsResponse = ICommandResponse<AppSportRsp> { result ->
        result ?: return@ICommandResponse
        when (result.gpsStatus) {
            6 -> { // Start timestamp from device
                lastStartTimestamp = result.timeStamp.toLong()
                Log.d("ExerciseVM", "Start timestamp: $lastStartTimestamp")
            }
            2 -> { // Pause
                isPaused = true
                notifyUpdate()
                Log.d("ExerciseVM", "Exercise paused")
            }
            3 -> { // Resume
                isPaused = false
                notifyUpdate()
                Log.d("ExerciseVM", "Exercise resumed")
            }
            4 -> { // End
                handleExerciseEnded()
                Log.d("ExerciseVM", "Exercise ended")
            }
        }
    }

    private val sportNotifyListener = object : DeviceSportNotifyListener() {
        override fun onDataResponse(resultEntity: DeviceNotifyRsp?) {
            super.onDataResponse(resultEntity)
            val rsp = resultEntity ?: return
            if (rsp.status != BaseRspCmd.RESULT_OK) return

            val bytes = rsp.loadData
            if (bytes == null || bytes.isEmpty()) return

            // Parse device data (indices from documentation)
            val sportType = bytes2Int(byteArrayOf(bytes[0]))
            val status = bytes2Int(byteArrayOf(bytes[1]))
            val duration = bytes2Int(byteArrayOf(bytes[2], bytes[3]))
            val heart = bytes2Int(byteArrayOf(bytes[4]))
            val stepCount = bytes2Int(byteArrayOf(bytes[5], bytes[6], bytes[7]))
            val distance = bytes2Int(byteArrayOf(bytes[8], bytes[9], bytes[10]))
            val calorie = bytes2Int(byteArrayOf(bytes[11], bytes[12], bytes[13]))

            // Check device wear status
            if (status == 0x03) {
                onError?.invoke("Device not worn properly")
                return
            }

            // Update tracking data
            currentSportType = sportType
            elapsedSeconds = duration
            heartRate = heart
            steps = stepCount
            distanceMeters = distance
            calories = calorie

            notifyUpdate()
        }
    }

    // ========================================
    // PUBLIC API - Start Exercise
    // ========================================

    fun startExercise(
        sportType: Int,
        onUpdate: (ExerciseData) -> Unit,
        onEnd: (ExerciseSummary) -> Unit,
        onErrorCallback: (String) -> Unit
    ) {
        if (isExercising) {
            onErrorCallback("Exercise already in progress")
            return
        }

        // Set callbacks
        this.onExerciseUpdate = onUpdate
        this.onExerciseEnded = onEnd
        this.onError = onErrorCallback

        viewModelScope.launch {
            try {
                Log.i("ExerciseVM", "Starting exercise: sportType=$sportType")

                // Reset state
                resetState()
                currentSportType = sportType
                isExercising = true
                isPaused = false

                // Register listener
                BleOperateManager.getInstance().addSportDeviceListener(0x78, sportNotifyListener)

                // Start exercise on device
                CommandHandle.getInstance().executeReqCmd(
                    PhoneSportReq.getSportStatus(1, sportType.toByte()),
                    gpsResponse
                )

                // Start local timer
                startTimer()

                Log.d("ExerciseVM", "Exercise started successfully")
            } catch (e: Exception) {
                Log.e("ExerciseVM", "Failed to start exercise: ${e.message}")
                onError?.invoke("Failed to start: ${e.message}")
                cleanup()
            }
        }
    }

    // ========================================
    // PUBLIC API - Control Exercise
    // ========================================

    fun pauseExercise() {
        if (!isExercising || isPaused) return

        val sportType = currentSportType ?: return
        isPaused = true
        notifyUpdate()

        try {
            CommandHandle.getInstance().executeReqCmd(
                PhoneSportReq.getSportStatus(2, sportType.toByte()),
                gpsResponse
            )
            Log.d("ExerciseVM", "Pause command sent")
        } catch (e: Exception) {
            Log.e("ExerciseVM", "Failed to pause: ${e.message}")
        }
    }

    fun resumeExercise() {
        if (!isExercising || !isPaused) return

        val sportType = currentSportType ?: return
        isPaused = false
        notifyUpdate()

        try {
            CommandHandle.getInstance().executeReqCmd(
                PhoneSportReq.getSportStatus(3, sportType.toByte()),
                gpsResponse
            )
            Log.d("ExerciseVM", "Resume command sent")
        } catch (e: Exception) {
            Log.e("ExerciseVM", "Failed to resume: ${e.message}")
        }
    }

    fun endExercise() {
        if (!isExercising) return

        val sportType = currentSportType ?: return

        try {
            CommandHandle.getInstance().executeReqCmd(
                PhoneSportReq.getSportStatus(4, sportType.toByte()),
                gpsResponse
            )
            Log.d("ExerciseVM", "End command sent")

            // Handle end locally (will also be called from device response)
            handleExerciseEnded()
        } catch (e: Exception) {
            Log.e("ExerciseVM", "Failed to end exercise: ${e.message}")
            handleExerciseEnded()
        }
    }

    // ========================================
    // PUBLIC API - Get Recent Exercises
    // ========================================

    fun syncRecentExercises(onSuccess: (List<ExerciseSummary>) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val syncSport = SportPlusHandle()
                syncSport.timeFormat = "yyyy-MM-dd HH:mm"

                val summaries = mutableListOf<ExerciseSummary>()

                syncSport.syncSportPlus { _, data ->
                    try {
                        // Parse exercise data from device
                        // This is simplified - adjust based on actual data structure
                        val summary = parseExerciseData(data)
                        if (summary != null) {
                            summaries.add(summary)
                        }
                    } catch (e: Exception) {
                        Log.e("ExerciseVM", "Failed to parse exercise: ${e.message}")
                    }
                }

                syncSport.cmdSummary(0)

                // Wait a bit for data to be received
                delay(2000)
                onSuccess(summaries)

            } catch (e: Exception) {
                Log.e("ExerciseVM", "Failed to sync exercises: ${e.message}")
                onError("Failed to sync: ${e.message}")
            }
        }
    }

    // ========================================
    // HELPER FUNCTIONS
    // ========================================

    private fun startTimer() {
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            while (isExercising) {
                delay(1000)
                if (!isPaused) {
                    elapsedSeconds++
                    notifyUpdate()
                }
            }
        }
    }

    private fun stopTimer() {
        tickJob?.cancel()
        tickJob = null
    }

    private fun notifyUpdate() {
        onExerciseUpdate?.invoke(getCurrentExerciseData())
    }

    private fun getCurrentExerciseData(): ExerciseData {
        return ExerciseData(
            sportType = currentSportType ?: 0,
            isActive = isExercising,
            isPaused = isPaused,
            elapsedSeconds = elapsedSeconds,
            heartRate = heartRate,
            steps = steps,
            distanceMeters = distanceMeters,
            calories = calories
        )
    }

    private fun handleExerciseEnded() {
        val startTime = if (lastStartTimestamp > 0)
            lastStartTimestamp
        else
            System.currentTimeMillis() / 1000

        val summary = ExerciseSummary(
            sportType = currentSportType ?: 0,
            sportName = getSportName(currentSportType ?: 0),
            startTimestamp = startTime,
            durationSeconds = elapsedSeconds,
            distanceMeters = distanceMeters,
            calories = calories,
            averageHeartRate = heartRate,
            steps = steps,
            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Date(startTime * 1000))
        )

        onExerciseEnded?.invoke(summary)
        cleanup()
    }

    private fun cleanup() {
        BleOperateManager.getInstance().removeSportDeviceListener(0x78)
        stopTimer()
        resetState()

        onExerciseUpdate = null
        onExerciseEnded = null
        onError = null
    }

    private fun resetState() {
        isExercising = false
        isPaused = false
        currentSportType = null
        elapsedSeconds = 0
        heartRate = 0
        steps = 0
        distanceMeters = 0
        calories = 0
        lastStartTimestamp = 0
    }

    private fun bytes2Int(data: ByteArray): Int {
        var res = 0
        for (i in data.indices) {
            res = res or ((data[i].toInt() and 0xFF) shl (8 * (data.size - 1 - i)))
        }
        return res
    }

    private fun parseExerciseData(data: Any?): ExerciseSummary? {
        // Implement based on your actual data structure
        // This is a placeholder
        return null
    }

    override fun onCleared() {
        super.onCleared()
        cleanup()
        Log.d("ExerciseVM", "ViewModel cleared")
    }
}

data class ExerciseData(
    val sportType: Int,
    val isActive: Boolean,
    val isPaused: Boolean,
    val elapsedSeconds: Int,
    val heartRate: Int,
    val steps: Int,
    val distanceMeters: Int,
    val calories: Int
) {
    fun getFormattedDuration(): String {
        val hours = elapsedSeconds / 3600
        val minutes = (elapsedSeconds % 3600) / 60
        val seconds = elapsedSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    fun getFormattedDistance(): String {
        return if (distanceMeters >= 1000) {
            String.format("%.2f km", distanceMeters / 1000.0)
        } else {
            "$distanceMeters m"
        }
    }
}

data class ExerciseSummary(
    val sportType: Int,
    val sportName: String,
    val startTimestamp: Long,
    val durationSeconds: Int,
    val distanceMeters: Int,
    val calories: Int,
    val averageHeartRate: Int,
    val steps: Int,
    val date: String
) {
    fun getFormattedDuration(): String {
        val hours = durationSeconds / 3600
        val minutes = (durationSeconds % 3600) / 60
        return if (hours > 0) {
            "$hours hr $minutes min"
        } else {
            "$minutes min"
        }
    }

    fun getFormattedDistance(): String {
        return if (distanceMeters >= 1000) {
            String.format("%.2f km", distanceMeters / 1000.0)
        } else {
            "$distanceMeters m"
        }
    }
}
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