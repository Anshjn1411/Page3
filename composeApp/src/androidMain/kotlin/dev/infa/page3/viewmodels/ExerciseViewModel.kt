package dev.infa.page3.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.communication.CommandHandle
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.req.PhoneSportReq
import com.oudmon.ble.base.communication.sport.SportPlusHandle
import com.oudmon.ble.base.communication.responseImpl.DeviceSportNotifyListener
import com.oudmon.ble.base.communication.rsp.AppSportRsp
import com.oudmon.ble.base.communication.rsp.BaseRspCmd
import com.oudmon.ble.base.communication.rsp.DeviceNotifyRsp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dev.infa.page3.data.repository.ExerciseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

data class ExerciseSummary(
    val sportType: Int,
    val durationSec: Int,
    val distanceMeters: Int,
    val calories: Int,
    val avgHeartRate: Int,
    val steps: Int,
    val startedAtSec: Long
)

data class LiveExerciseState(
    val isActive: Boolean = false,
    val isPaused: Boolean = false,
    val countdown: Int? = null,
    val elapsedSec: Int = 0,
    val heartRate: Int = 0,
    val steps: Int = 0,
    val distanceMeters: Int = 0,
    val calories: Int = 0,
    val sportType: Int? = null,
    val error: String? = null
)

class ExerciseViewModel(
    private val commandHandle: CommandHandle = CommandHandle.getInstance(),
    private val repository: ExerciseRepository? = null
) : ViewModel() {

    private val _liveState = MutableStateFlow(LiveExerciseState())
    val liveState: StateFlow<LiveExerciseState> = _liveState.asStateFlow()

    private val _recent = MutableStateFlow<List<ExerciseSummary>>(emptyList())
    val recent: StateFlow<List<ExerciseSummary>> = _recent.asStateFlow()

    // Present a minimal curated list of sport types from docs
    val availableSportTypes: List<Pair<Int, String>> = allSportTypes()

    private var tickJob: Job? = null
    private var lastStartTimestampSec: Long = 0

    init {
        // If repository is present, hydrate from cache; else do nothing
        repository?.let { repo ->
            viewModelScope.launch {
//                repo.recent().collect { list ->
//                    _recent.value = list.map {
//                        ExerciseSummary(
//                            sportType = it.sportType,
//                            durationSec = it.durationSec,
//                            distanceMeters = it.distanceMeters,
//                            calories = it.calories,
//                            avgHeartRate = it.avgHeartRate,
//                            steps = it.steps,
//                            startedAtSec = it.startedAtSec
//                        )
//                    }
//                }
            }
        }
    }

    private val gpsResponse = ICommandResponse<AppSportRsp> { result ->
        result ?: return@ICommandResponse
        when (result.gpsStatus) {
            6 -> { // start timestamp provided by device
                lastStartTimestampSec = result.timeStamp.toLong()
            }
            2 -> { // pause
                _liveState.value = _liveState.value.copy(isPaused = true)
            }
            3 -> { // resume
                _liveState.value = _liveState.value.copy(isPaused = false)
            }
            4 -> { // end
                onExerciseEnded()
            }
        }
    }

    private val sportNotifyListener = object : DeviceSportNotifyListener() {
        override fun onDataResponse(resultEntity: DeviceNotifyRsp?) {
            super.onDataResponse(resultEntity)
            val rsp = resultEntity ?: return
            if (rsp.status != BaseRspCmd.RESULT_OK) return

            // Parse payload indices per docs
            val bytes = rsp.loadData
            if (bytes == null || bytes.isEmpty()) return

            val sportType = bytes2Int(byteArrayOf(bytes[0]))
            val status = bytes2Int(byteArrayOf(bytes[1]))
            val duration = bytes2Int(byteArrayOf(bytes[2], bytes[3]))
            val heart = bytes2Int(byteArrayOf(bytes[4]))
            val steps = bytes2Int(byteArrayOf(bytes[5], bytes[6], bytes[7]))
            val distance = bytes2Int(byteArrayOf(bytes[8], bytes[9], bytes[10]))
            val calorie = bytes2Int(byteArrayOf(bytes[11], bytes[12], bytes[13]))

            if (status == 0x03) {
                _liveState.value = _liveState.value.copy(error = "Device not worn")
            }

            _liveState.value = _liveState.value.copy(
                sportType = sportType,
                elapsedSec = duration,
                heartRate = heart,
                steps = steps,
                distanceMeters = distance,
                calories = calorie
            )
        }
    }

    fun startExercise(selectedSportType: Int) {
        viewModelScope.launch {
            startCountdownThenStart(selectedSportType)
        }
    }

    private suspend fun startCountdownThenStart(sportType: Int) {
        // Countdown 3..1
        _liveState.value = LiveExerciseState(countdown = 3, sportType = sportType)
        for (i in 3 downTo 1) {
            _liveState.value = _liveState.value.copy(countdown = i)
            delay(1000)
        }
        _liveState.value = _liveState.value.copy(countdown = null, isActive = true, isPaused = false)

        BleOperateManager.getInstance().addSportDeviceListener(0x78, sportNotifyListener)
        commandHandle.executeReqCmd(
            PhoneSportReq.getSportStatus(1, sportType.toByte()),
            gpsResponse
        )

        startTick()
    }

    fun pauseExercise() {
        val sportType = _liveState.value.sportType ?: return
        // Optimistic UI update to pause immediately
        _liveState.value = _liveState.value.copy(isPaused = true)
        commandHandle.executeReqCmd(
            PhoneSportReq.getSportStatus(2, sportType.toByte()),
            gpsResponse
        )
    }

    fun resumeExercise() {
        val sportType = _liveState.value.sportType ?: return
        // Optimistic UI update to resume immediately
        _liveState.value = _liveState.value.copy(isPaused = false)
        commandHandle.executeReqCmd(
            PhoneSportReq.getSportStatus(3, sportType.toByte()),
            gpsResponse
        )
    }

    fun endExercise(saveIfEligible: Boolean = true) {
        val sportType = _liveState.value.sportType ?: return
        // Stop UI immediately while sending SDK end command
        onExerciseEnded(saveIfEligible)
        commandHandle.executeReqCmd(
            PhoneSportReq.getSportStatus(4, sportType.toByte()),
            gpsResponse
        )
        // Pull latest summary from device
        syncRecentFromDevice()
    }

    private fun onExerciseEnded(saveIfEligible: Boolean = true) {
        BleOperateManager.getInstance().removeSportDeviceListener(0x78)
        stopTick()
        val s = _liveState.value
        if (s.sportType != null && (!saveIfEligible || s.elapsedSec >= 60)) {
            val started = if (lastStartTimestampSec != 0L) lastStartTimestampSec else (System.currentTimeMillis() / 1000)
//            val entity = ExerciseEntity(
//                sportType = s.sportType,
//                startedAtSec = started,
//                durationSec = s.elapsedSec,
//                distanceMeters = s.distanceMeters,
//                calories = s.calories,
//                avgHeartRate = s.heartRate,
//                maxHeartRate = s.heartRate,
//                steps = s.steps
//            )
            if (repository != null) {
                //viewModelScope.launch { repository.save(entity) }
            } else {
//                val summary = ExerciseSummary(
//                    sportType = entity.sportType,
//                    durationSec = entity.durationSec,
//                    distanceMeters = entity.distanceMeters,
//                    calories = entity.calories,
//                    avgHeartRate = entity.avgHeartRate,
//                    steps = entity.steps,
//                    startedAtSec = entity.startedAtSec
//                )
                _recent.value = _recent.value
            }
        }
        _liveState.value = LiveExerciseState()
    }

    private fun syncRecentFromDevice() {
        try {
            val syncSport = SportPlusHandle()
            syncSport.timeFormat = "yyyy-MM-dd HH:mm"
            syncSport.syncSportPlus { _, t ->
//                try {
//                    val type = t?.toString() ?: return@syncSportPlus
//                    val entity =
//                    if (repository != null) {
//                        viewModelScope.launch { repository.save(entity) }
//                    } else {
//                        val summary = ExerciseSummary(
//                            sportType = entity.sportType,
//                            durationSec = entity.durationSec,
//                            distanceMeters = entity.distanceMeters,
//                            calories = entity.calories,
//                            avgHeartRate = entity.avgHeartRate,
//                            steps = entity.steps,
//                            startedAtSec = entity.startedAtSec
//                        )
//                        _recent.value = listOf(summary) + _recent.value
//                    }
//                } catch (_: Exception) {}
            }
            syncSport.cmdSummary(0)
        } catch (_: Exception) {
            // Ignore if SDK class not available
        }
    }

    fun endExerciseDontSave() { endExercise(saveIfEligible = false) }

    private fun startTick() {
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val st = _liveState.value
                if (st.isActive && !st.isPaused) {
                    _liveState.value = st.copy(elapsedSec = st.elapsedSec + 1)
                }
            }
        }
    }

    private fun stopTick() {
        tickJob?.cancel()
        tickJob = null
    }

    override fun onCleared() {
        super.onCleared()
        BleOperateManager.getInstance().removeSportDeviceListener(0x78)
        stopTick()
    }

    private fun bytes2Int(data: ByteArray): Int {
        var res = 0
        for (i in data.indices) {
            res = res or ((data[i].toInt() and 0xFF) shl (8 * (data.size - 1 - i)))
        }
        return res
    }

    // Full sport type mapping from docs (major set); unknown fallbacks
    private fun allSportTypes(): List<Pair<Int, String>> {
        val map = linkedMapOf(
            1 to "GPS Run",
            2 to "GPS Bike",
            3 to "GPS Walk",
            4 to "Walking",
            5 to "Rope Skipping",
            6 to "Swimming",
            7 to "Running (Outdoor)",
            8 to "Hiking",
            9 to "Cycling",
            10 to "Exercise (Others)",
            11 to "Swing",
            20 to "Climb",
            21 to "Badminton",
            22 to "Yoga",
            23 to "Aerobics",
            24 to "Spinning Bike",
            25 to "Kayaking",
            26 to "Elliptical",
            27 to "Rowing Machine",
            28 to "Table Tennis",
            29 to "Tennis",
            30 to "Golf",
            31 to "Basketball",
            32 to "Football",
            33 to "Volleyball",
            34 to "Rock Climbing",
            35 to "Dance",
            36 to "Roller Skating",
            40 to "Treadmill",
            41 to "Indoor Walking",
            42 to "Trail Running",
            43 to "Race Walk",
            44 to "Playground Running",
            45 to "Fat Loss Running",
            50 to "Outdoor Cycling",
            51 to "Indoor Cycling",
            52 to "Mountain Biking",
            53 to "BMX",
            55 to "Swimming Pool",
            56 to "Outdoor Swimming",
            57 to "Fin Swimming",
            58 to "Synchronized Swimming",
            60 to "Outdoor Hiking",
            61 to "Orienteering",
            62 to "Fishing",
            63 to "Hunting",
            64 to "Skateboard",
            65 to "Parkour",
            66 to "ATV",
            67 to "Motocross",
            68 to "Racing",
            69 to "Hand Bike",
            70 to "Marathon",
            71 to "Obstacle Course",
            80 to "Stair Climber",
            81 to "Stair Stepper",
            82 to "Mixed Aerobic",
            83 to "Kickboxing",
            84 to "Core Training",
            85 to "Cross Training",
            86 to "Indoor Fitness",
            87 to "Group Gymnastics",
            88 to "Strength Training",
            89 to "Gap Training",
            90 to "Free Training",
            91 to "Flexibility Training",
            92 to "Gymnastics",
            93 to "Stretching",
            94 to "Pilates",
            95 to "Horizontal Bar",
            96 to "Parallel Bars",
            97 to "Battle Rope",
            98 to "Fitness",
            99 to "Balance Training",
            100 to "Step Training",
            110 to "Square Dance",
            111 to "Ballroom Dancing",
            112 to "Belly Dance",
            113 to "Ballet",
            114 to "Street Dance",
            115 to "Zumba",
            116 to "Latin Dance",
            117 to "Latin Jazz",
            118 to "Hip-Hop Dance",
            119 to "Pole Dancing",
            120 to "Break Dance",
            121 to "Folk Dance",
            122 to "New Dance",
            123 to "Modern Dance",
            124 to "Disco",
            125 to "Tap Dance",
            126 to "Other Dance",
            130 to "Boxing",
            131 to "Wrestling",
            132 to "Martial Arts",
            133 to "Tai Chi",
            134 to "Muay Thai",
            135 to "Judo",
            136 to "Taekwondo",
            137 to "Karate",
            138 to "Free Sparring",
            139 to "Swordsmanship",
            140 to "Jiu-Jitsu",
            141 to "Fencing",
            142 to "Kendo",
            150 to "Beach Football",
            151 to "Beach Volleyball",
            152 to "Baseball",
            153 to "Softball",
            154 to "Rugby",
            155 to "Hockey",
            156 to "Squash",
            157 to "Gateball",
            158 to "Cricket",
            159 to "Handball",
            160 to "Bowling",
            161 to "Polo",
            162 to "Racquetball",
            163 to "Billiards",
            164 to "Sepak Takraw",
            165 to "Dodgeball",
            166 to "Water Polo",
            167 to "Ice Hockey",
            168 to "Shuttlecock",
            169 to "Indoor Soccer",
            170 to "Sandbag Ball",
            171 to "Floor Bocce",
            172 to "Jai Alai",
            173 to "Floorball",
            174 to "Australian Rules Football",
            175 to "Pickleball",
            180 to "Outdoor Rowing",
            181 to "Sailing",
            182 to "Dragon Boat",
            183 to "Surfing",
            184 to "Kitesurfing",
            185 to "Paddling",
            186 to "Paddleboard",
            187 to "Indoor Surfing",
            188 to "Rafting",
            189 to "Snorkeling",
            190 to "Skiing",
            191 to "Snowboard",
            192 to "Alpine Skiing",
            193 to "Cross-Country Skiing",
            194 to "Ski Orienteering",
            195 to "Biathlon",
            196 to "Outdoor Skating",
            197 to "Indoor Skating",
            198 to "Curling",
            199 to "Bobsleigh",
            200 to "Sled",
            201 to "Snowmobile",
            202 to "Snowshoe Hiking",
            210 to "Hula Hoop",
            211 to "Frisbee",
            212 to "Darts",
            213 to "Kite Flying",
            214 to "Tug of War",
            215 to "Esports",
            216 to "Walking Machine",
            217 to "Swing (New)",
            218 to "Shuffleboard",
            219 to "Table Football",
            220 to "Somatosensory Game",
            221 to "Bungee Jumping",
            222 to "Skydiving",
            223 to "Anusara",
            224 to "Yin Yoga",
            225 to "Pregnancy Yoga"
        )
        return map.entries.map { it.toPair() }
    }
}


