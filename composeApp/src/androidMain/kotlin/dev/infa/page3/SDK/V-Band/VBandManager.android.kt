package dev.infa.page3.SDK.`V-Band`

import android.content.Context
import android.util.Log
import com.inuker.bluetooth.library.Code
import com.inuker.bluetooth.library.model.BleGattProfile
import com.inuker.bluetooth.library.search.SearchResult
import com.inuker.bluetooth.library.search.response.SearchResponse
import com.veepoo.protocol.VPOperateManager
import com.veepoo.protocol.listener.IHealthRemindListener
import com.veepoo.protocol.listener.base.IBleWriteResponse
import com.veepoo.protocol.listener.base.IConnectResponse
import com.veepoo.protocol.listener.base.INotifyResponse
import com.veepoo.protocol.listener.data.*
import com.veepoo.protocol.model.datas.*
import com.veepoo.protocol.model.enums.*
import com.veepoo.protocol.model.settings.*
import dev.infa.page3.SDK.`V-Band`.data.*
import dev.infa.page3.SDK.ui.utils.PlatformContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual class VBandManager : IVBandManager {

    companion object {
        private const val TAG = "VBandMgr"
        private const val SYNC_CMD_TIMEOUT_MS = 30_000L  // 30 seconds per command
        private const val SYNC_CMD_DELAY_MS = 300L        // 300ms between commands
    }

    // ─── Android Context ────────────────────────────────────────────────────────

    private val context: Context get() = PlatformContext.get() as Context
    private val vpManager: VPOperateManager get() = VPOperateManager.getInstance()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isVpInitialized = false

    /** Must be called before any VPOperateManager BLE operations */
    private fun ensureInitialized() {
        if (!isVpInitialized) {
            vpManager.init(context)
            isVpInitialized = true
            Log.d(TAG, "✓ VPOperateManager initialized with context")
        }
    }

    // ─── State Flows ────────────────────────────────────────────────────────────

    private val _devices = MutableStateFlow<List<VBandDeviceInfo>>(emptyList())
    actual override val devices: StateFlow<List<VBandDeviceInfo>> = _devices

    private val _connectionState = MutableStateFlow("DISCONNECTED")
    actual override val connectionState: StateFlow<String> = _connectionState

    private val _pwdData = MutableStateFlow<VBandPwdData?>(null)
    actual override val pwdData: StateFlow<VBandPwdData?> = _pwdData

    private val _functionSupport = MutableStateFlow<VBandFunctionSupport?>(null)
    actual override val functionSupport: StateFlow<VBandFunctionSupport?> = _functionSupport

    private val _batteryData = MutableStateFlow<VBandBatteryData?>(null)
    actual override val batteryData: StateFlow<VBandBatteryData?> = _batteryData

    private val _sportData = MutableStateFlow<VBandSportData?>(null)
    actual override val sportData: StateFlow<VBandSportData?> = _sportData

    private val _heartData = MutableStateFlow<VBandHeartData?>(null)
    actual override val heartData: StateFlow<VBandHeartData?> = _heartData

    private val _heartWarningData = MutableStateFlow<VBandHeartWarningData?>(null)
    actual override val heartWarningData: StateFlow<VBandHeartWarningData?> = _heartWarningData

    private val _sleepDataList = MutableStateFlow<List<VBandSleepData>>(emptyList())
    actual override val sleepDataList: StateFlow<List<VBandSleepData>> = _sleepDataList

    private val _originDataList = MutableStateFlow<List<VBandOriginData>>(emptyList())
    actual override val originDataList: StateFlow<List<VBandOriginData>> = _originDataList

    private val _originHalfHourDataList =
        MutableStateFlow<List<VBandOriginHalfHourData>>(emptyList())
    actual override val originHalfHourDataList: StateFlow<List<VBandOriginHalfHourData>> =
        _originHalfHourDataList

    private val _customSettingData = MutableStateFlow<VBandCustomSettingData?>(null)
    actual override val customSettingData: StateFlow<VBandCustomSettingData?> = _customSettingData

    private val _nightTurnWristData = MutableStateFlow<VBandNightTurnWristData?>(null)
    actual override val nightTurnWristData: StateFlow<VBandNightTurnWristData?> =
        _nightTurnWristData

    private val _screenLightData = MutableStateFlow<VBandScreenLightData?>(null)
    actual override val screenLightData: StateFlow<VBandScreenLightData?> = _screenLightData

    private val _screenLightTimeData = MutableStateFlow<VBandScreenLightTimeData?>(null)
    actual override val screenLightTimeData: StateFlow<VBandScreenLightTimeData?> =
        _screenLightTimeData

    private val _temperatureDetectData = MutableStateFlow<VBandTemperatureDetectData?>(null)
    actual override val temperatureDetectData: StateFlow<VBandTemperatureDetectData?> =
        _temperatureDetectData

    private val _temperatureRecords = MutableStateFlow<List<VBandTemperatureRecord>>(emptyList())
    actual override val temperatureRecords: StateFlow<List<VBandTemperatureRecord>> =
        _temperatureRecords

    private val _healthRemindList = MutableStateFlow<List<VBandHealthRemind>>(emptyList())
    actual override val healthRemindList: StateFlow<List<VBandHealthRemind>> = _healthRemindList

    private val _languageData = MutableStateFlow<VBandLanguageData?>(null)
    actual override val languageData: StateFlow<VBandLanguageData?> = _languageData

    private val _readProgress = MutableStateFlow(VBandReadProgress())
    actual override val readProgress: StateFlow<VBandReadProgress> = _readProgress

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    actual override val logs: StateFlow<List<String>> = _logs

    private val _syncState = MutableStateFlow(VBandSyncState())
    override val syncState: StateFlow<VBandSyncState> = _syncState

    private val _syncLogs = MutableStateFlow<List<VBandSyncLogEntry>>(emptyList())
     override val syncLogs: StateFlow<List<VBandSyncLogEntry>> = _syncLogs

    // ─── Command Queue (Mutex) ──────────────────────────────────────────────────

    private val syncMutex = Mutex()
    private var syncJob: Job? = null

    // ─── Logging ────────────────────────────────────────────────────────────────

    private fun addLog(message: String) {
        Log.d(TAG, message)
        scope.launch {
            val cur = _logs.value.toMutableList()
            cur.add(0, "[${System.currentTimeMillis() % 100000}] $message")
            if (cur.size > 200) cur.removeAt(cur.size - 1)
            _logs.value = cur
        }
    }

    private fun addSyncLog(level: VBandLogLevel, message: String, stepName: String = "") {
        val entry = VBandSyncLogEntry(
            timestamp = System.currentTimeMillis(),
            level = level,
            message = message,
            stepName = stepName
        )
        Log.d(TAG, "[SYNC:${level.name}] [$stepName] $message")
        scope.launch {
            val cur = _syncLogs.value.toMutableList()
            cur.add(0, entry)
            if (cur.size > 500) cur.removeAt(cur.size - 1)
            _syncLogs.value = cur
        }
    }

    private fun updateSyncState(
        status: VBandSyncStatus? = null,
        stepName: String? = null,
        stepIndex: Int? = null,
        totalSteps: Int? = null,
        stepProgress: Float? = null,
        errorMessage: String? = null,
        syncedDay: Int? = null
    ) {
        val current = _syncState.value
        _syncState.value = current.copy(
            status = status ?: current.status,
            currentStepName = stepName ?: current.currentStepName,
            currentStepIndex = stepIndex ?: current.currentStepIndex,
            totalSteps = totalSteps ?: current.totalSteps,
            stepProgress = stepProgress ?: current.stepProgress,
            errorMessage = errorMessage ?: current.errorMessage,
            syncedDay = syncedDay ?: current.syncedDay
        )
    }

    // ─── Write Response Helper ──────────────────────────────────────────────────

    private val writeResponse = IBleWriteResponse { code ->
        if (code != 0) {
            addLog("✗ Write command failed, code=$code")
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SCAN
    // ═══════════════════════════════════════════════════════════════════════════

    actual override fun startScan() {
        ensureInitialized()
        addLog("=== STARTING SCAN ===")
        _devices.value = emptyList()
        _connectionState.value = "SCANNING"

        vpManager.startScanDevice(object : SearchResponse {
            override fun onSearchStarted() {
                addLog("Scan started")
            }

            override fun onDeviceFounded(device: SearchResult?) {
                device ?: return
                val bleDevice = device.device ?: return
                val name = bleDevice.name ?: return
                val address = bleDevice.address ?: return
                val rssi = device.rssi

                val current = _devices.value.toMutableList()
                if (!current.any { it.address == address }) {
                    current.add(VBandDeviceInfo(name, address, rssi))
                    _devices.value = current
                    addLog("Device found: $name ($address) rssi=$rssi")
                }
            }

            override fun onSearchStopped() {
                addLog("Scan stopped")
                if (_connectionState.value == "SCANNING") {
                    _connectionState.value = "DISCONNECTED"
                }
            }

            override fun onSearchCanceled() {
                addLog("Scan canceled")
                if (_connectionState.value == "SCANNING") {
                    _connectionState.value = "DISCONNECTED"
                }
            }
        })

        // Auto-stop after 15 seconds
        scope.launch {
            delay(15000)
            stopScan()
        }
    }

    actual override fun stopScan() {
        addLog("Stopping scan")
        vpManager.stopScanDevice()
        if (_connectionState.value == "SCANNING") {
            _connectionState.value = "DISCONNECTED"
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CONNECTION
    // ═══════════════════════════════════════════════════════════════════════════

    actual override fun connect(device: VBandDeviceInfo) {
        ensureInitialized()
        addLog("=== CONNECTING: ${device.name} (${device.address}) ===")
        stopScan()
        _connectionState.value = "CONNECTING"

        vpManager.connectDevice(
            device.address,
            object : IConnectResponse {
                override fun connectState(
                    code: Int,
                    profile: BleGattProfile?,
                    isOadModel: Boolean
                ) {
                    if (code == Code.REQUEST_SUCCESS) {
                        addLog("✓ Connection successful, isOadModel=$isOadModel")
                        _connectionState.value = "CONNECTED"
                    } else {
                        addLog("✗ Connection failed, code=$code")
                        _connectionState.value = "DISCONNECTED"
                    }
                }
            },
            object : INotifyResponse {
                override fun notifyState(state: Int) {
                    if (state == Code.REQUEST_SUCCESS) {
                        addLog("✓ Notify enabled — READY for commands")
                    } else {
                        addLog("✗ Notify setup failed, state=$state")
                    }
                }
            }
        )
    }

    actual override fun disconnect() {
        addLog("=== DISCONNECTING ===")
        vpManager.disconnectWatch {
            addLog("Disconnect write result: $it")
        }
        _connectionState.value = "DISCONNECTED"
        _pwdData.value = null
        _functionSupport.value = null
        _batteryData.value = null
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // AUTH / PASSWORD
    // ═══════════════════════════════════════════════════════════════════════════

    actual override fun confirmPassword(pwd: String, is24Hour: Boolean) {
        addLog(">>> CMD: Confirm Password (pwd=$pwd, 24h=$is24Hour)")

        vpManager.confirmDevicePwd(
            writeResponse,
            // Password data listener
            object : IPwdDataListener {
                override fun onPwdDataChange(data: PwdData?) {
                    data ?: return
                    addLog("✓ Password result: status=${data.getmStatus()}, deviceNum=${data.deviceNumber}")

                    val status = when (data.getmStatus()) {
                        EPwdStatus.CHECK_SUCCESS -> VBandPwdStatus.CHECK_SUCCESS
                        EPwdStatus.CHECK_FAIL -> VBandPwdStatus.CHECK_FAIL
                        EPwdStatus.SETTING_SUCCESS -> VBandPwdStatus.SETTING_SUCCESS
                        EPwdStatus.SETTING_FAIL -> VBandPwdStatus.SETTING_FAIL
                        else -> VBandPwdStatus.UNKNOWN
                    }

                    _pwdData.value = VBandPwdData(
                        status = status,
                        password = "",
                        deviceNumber = data.deviceNumber,
                        deviceVersion = data.deviceVersion ?: "",
                        deviceTestVersion = data.deviceTestVersion ?: "",
                        isHaveDrinkData = data.isHaveDrinkData,
                        nightTurnWristStatus = mapFunctionStatus(data.isOpenNightTurnWriste),
                        findPhoneFunction = mapFunctionStatus(data.findPhoneFunction),
                        wearDetectFunction = mapFunctionStatus(data.wearDetectFunction)
                    )
                }
            },
            // Device function data listener
            object : IDeviceFuctionDataListener {
                override fun onFunctionSupportDataChange(data: FunctionDeviceSupportData?) {
                    data ?: return
                    addLog("✓ Function support data received, watchDay=${data.wathcDay}")

                    _functionSupport.value = VBandFunctionSupport(
                        bp = mapFunctionStatus(data.bp),
                        drink = mapFunctionStatus(data.drink),
                        longSeat = mapFunctionStatus(data.longseat),
                        heartWarning = mapFunctionStatus(data.heartWaring),
                        weChatSport = mapFunctionStatus(data.weChatSport),
                        camera = mapFunctionStatus(data.camera),
                        fatigue = mapFunctionStatus(data.fatigue),
                        spO2 = mapFunctionStatus(data.spo2H),
                        woman = mapFunctionStatus(data.women),
                        countDown = mapFunctionStatus(data.countDown),
                        screenLight = mapFunctionStatus(data.screenLight),
                        heartDetect = mapFunctionStatus(data.heartDetect),
                        sportModel = mapFunctionStatus(data.sportModel),
                        nightTurnSetting = mapFunctionStatus(data.nightTurnSetting),
                        screenStyleFunction = mapFunctionStatus(data.screenStyleFunction),
                        breathFunction = mapFunctionStatus(data.beathFunction),
                        hrvFunction = mapFunctionStatus(data.hrvFunction),
                        weatherFunction = mapFunctionStatus(data.weatherFunction),
                        screenLightTime = mapFunctionStatus(data.screenLightTime),
                        precisionSleep = mapFunctionStatus(data.precisionSleep),
                        ecg = mapFunctionStatus(data.ecg),
                        multSportModel = mapFunctionStatus(data.multSportModel),
                        findDeviceByPhone = mapFunctionStatus(data.findDeviceByPhone),
                        temperatureFunction = mapFunctionStatus(data.temperatureFunction),
                        bloodGlucose = mapFunctionStatus(data.bloodGlucose),
                        watchDay = data.wathcDay,
                        contactMsgLength = data.contactMsgLength,
                        allMsgLength = data.allMsgLength,
                        screenStyle = data.screenstyle,
                        originProtocolVersion = data.originProtcolVersion,
                        cpuType = data.cpuType
                    )
                }
            },
            // Social msg data listener
            object : ISocialMsgDataListener {
                override fun onSocialMsgSupportDataChange(data: FunctionSocailMsgData?) {
                    addLog("Social msg support data received (pack 1)")
                }

                override fun onSocialMsgSupportDataChange2(data: FunctionSocailMsgData?) {
                    addLog("Social msg support data received (pack 2)")
                }
            },
            // Custom setting listener
            object : ICustomSettingDataListener {
                override fun OnSettingDataChange(data: CustomSettingData?) {
                    data ?: return
                    addLog("✓ Custom settings received from password confirm")
                    updateCustomSettingData(data)
                }
            },
            pwd,
            is24Hour
        )
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PERSONAL INFO
    // ═══════════════════════════════════════════════════════════════════════════

    actual override fun syncPersonInfo(info: VBandPersonInfo) {
        addLog(">>> CMD: Sync Personal Info (h=${info.height}, w=${info.weight}, age=${info.age})")

        val sex = when (info.sex) {
            VBandSex.MAN -> ESex.MAN
            VBandSex.WOMAN -> ESex.WOMEN
            else -> ESex.MAN
        }

        vpManager.syncPersonInfo(
            writeResponse,
            { status ->
                addLog("PersonInfo sync result: $status")
            },
            PersonInfoData(sex, info.height, info.weight, info.age, info.stepGoal)
        )
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // BATTERY
    // ═══════════════════════════════════════════════════════════════════════════

    actual override fun readBattery() {
        addLog(">>> CMD: Read Battery")
        vpManager.readBattery(writeResponse) { data ->
            data ?: return@readBattery
            addLog(
                "✓ Battery: level=${data.batteryLevel}, percent=${data.batteryPercent}, " +
                        "model=${data.powerModel}, isPercent=${data.isPercent}"
            )

            _batteryData.value = VBandBatteryData(
                batteryLevel = data.batteryLevel,
                batteryPercent = data.batteryPercent,
                powerModel = data.powerModel,
                isLowBattery = data.isLowBattery,
                isPercent = data.isPercent
            )
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // STEPS / SPORT
    // ═══════════════════════════════════════════════════════════════════════════

    actual override fun readSportStep() {
        addLog(">>> CMD: Read Sport Step")
        vpManager.readSportStep(writeResponse) { data ->
            data ?: return@readSportStep
            addLog("✓ Steps: ${data.step}, dist=${data.dis}km, cal=${data.kcal}kcal")

            _sportData.value = VBandSportData(
                step = data.step,
                distance = data.dis,
                calories = data.kcal,
                calcType = data.calcType
            )
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DAILY DATA — ALL HEALTH DATA
    // ═══════════════════════════════════════════════════════════════════════════

    actual override fun readAllHealthData(watchDay: Int) {
        addLog(">>> CMD: Read All Health Data (watchDay=$watchDay)")
        _sleepDataList.value = emptyList()
        _originDataList.value = emptyList()
        _originHalfHourDataList.value = emptyList()

        vpManager.readAllHealthData(object : IAllHealthDataListener {
            override fun onProgress(progress: Float) {
                _readProgress.value = VBandReadProgress(progress, false)
            }

            override fun onSleepDataChange(day: String?, sleepData: SleepData?) {
                sleepData ?: return
                addLog("Sleep data: day=$day, total=${sleepData.allSleepTime}min")
                val current = _sleepDataList.value.toMutableList()
                current.add(mapSleepData(day ?: "", sleepData))
                _sleepDataList.value = current
            }

            override fun onReadSleepComplete() {
                addLog("Sleep data read complete")
            }

            override fun onOringinFiveMinuteDataChange(originData: OriginData?) {
                originData ?: return
                val current = _originDataList.value.toMutableList()
                current.add(mapOriginData(originData))
                _originDataList.value = current
            }

            override fun onOringinHalfHourDataChange(halfHourData: OriginHalfHourData?) {
                halfHourData ?: return
                val current = _originHalfHourDataList.value.toMutableList()
                current.add(mapOriginHalfHourData(halfHourData))
                _originHalfHourDataList.value = current
            }

            override fun onReadOriginComplete() {
                addLog("✓ All health data read complete")
                _readProgress.value = VBandReadProgress(1f, true)
            }
        }, watchDay)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DAILY DATA — ORIGIN DATA
    // ═══════════════════════════════════════════════════════════════════════════

    actual override fun readOriginData(watchDay: Int) {
        addLog(">>> CMD: Read Origin Data (watchDay=$watchDay)")
        _originDataList.value = emptyList()
        _originHalfHourDataList.value = emptyList()

        vpManager.readOriginData(writeResponse, object : IOriginDataListener {
            override fun onOringinFiveMinuteDataChange(originData: OriginData?) {
                originData ?: return
                val current = _originDataList.value.toMutableList()
                current.add(mapOriginData(originData))
                _originDataList.value = current
            }

            override fun onOringinHalfHourDataChange(halfHourData: OriginHalfHourData?) {
                halfHourData ?: return
                val current = _originHalfHourDataList.value.toMutableList()
                current.add(mapOriginHalfHourData(halfHourData))
                _originHalfHourDataList.value = current
            }

            override fun onReadOriginProgressDetail(
                day: Int,
                date: String?,
                allPackage: Int,
                currentPackage: Int
            ) {
                addLog("Origin data: day=$day, date=$date, $currentPackage/$allPackage")
            }

            override fun onReadOriginProgress(progress: Float) {
                _readProgress.value = VBandReadProgress(progress, false)
            }

            override fun onReadOriginComplete() {
                addLog("✓ Origin data read complete")
                _readProgress.value = VBandReadProgress(1f, true)
            }
        }, watchDay)
    }

     override fun readOriginDataSingleDay(day: Int, watchDay: Int) {
        addLog(">>> CMD: Read Origin Data Single Day (day=$day, watchDay=$watchDay)")
        _originDataList.value = emptyList()
        _originHalfHourDataList.value = emptyList()

        vpManager.readOriginDataSingleDay(writeResponse, object : IOriginDataListener {
            override fun onOringinFiveMinuteDataChange(originData: OriginData?) {
                originData ?: return
                val current = _originDataList.value.toMutableList()
                current.add(mapOriginData(originData))
                _originDataList.value = current
            }

            override fun onOringinHalfHourDataChange(halfHourData: OriginHalfHourData?) {
                halfHourData ?: return
                val current = _originHalfHourDataList.value.toMutableList()
                current.add(mapOriginHalfHourData(halfHourData))
                _originHalfHourDataList.value = current
            }

            override fun onReadOriginProgressDetail(
                day: Int,
                date: String?,
                allPackage: Int,
                currentPackage: Int
            ) {
                addLog("Origin single day: day=$day, date=$date, $currentPackage/$allPackage")
            }

            override fun onReadOriginProgress(progress: Float) {
                _readProgress.value = VBandReadProgress(progress, false)
            }

            override fun onReadOriginComplete() {
                addLog("✓ Origin single day read complete")
                _readProgress.value = VBandReadProgress(1f, true)
            }
        }, day, 1, watchDay)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SLEEP
    // ═══════════════════════════════════════════════════════════════════════════

    actual override fun readSleepData(watchDay: Int) {
        addLog(">>> CMD: Read Sleep Data (watchDay=$watchDay)")
        _sleepDataList.value = emptyList()

        vpManager.readSleepData(writeResponse, object : ISleepDataListener {
            override fun onSleepDataChange(day: String?, sleepData: SleepData?) {
                sleepData ?: return
                addLog("Sleep: day=$day, deep=${sleepData.deepSleepTime}, light=${sleepData.lowSleepTime}")
                val current = _sleepDataList.value.toMutableList()
                current.add(mapSleepData(day ?: "", sleepData))
                _sleepDataList.value = current
            }

            override fun onSleepProgress(progress: Float) {
                _readProgress.value = VBandReadProgress(progress, false)
            }

            override fun onSleepProgressDetail(day: String?, packageNumber: Int) {
                addLog("Sleep detail: day=$day, pkg=$packageNumber")
            }

            override fun onReadSleepComplete() {
                addLog("✓ Sleep data read complete")
                _readProgress.value = VBandReadProgress(1f, true)
            }
        }, watchDay)
    }

     override fun readSleepDataSingleDay(day: Int, watchDay: Int) {
        addLog(">>> CMD: Read Sleep Data Single Day (day=$day, watchDay=$watchDay)")
        _sleepDataList.value = emptyList()

        vpManager.readSleepDataSingleDay(writeResponse, object : ISleepDataListener {
            override fun onSleepDataChange(day: String?, sleepData: SleepData?) {
                sleepData ?: return
                addLog("Sleep single: day=$day, deep=${sleepData.deepSleepTime}, light=${sleepData.lowSleepTime}")
                val current = _sleepDataList.value.toMutableList()
                current.add(mapSleepData(day ?: "", sleepData))
                _sleepDataList.value = current
            }

            override fun onSleepProgress(progress: Float) {
                _readProgress.value = VBandReadProgress(progress, false)
            }

            override fun onSleepProgressDetail(day: String?, packageNumber: Int) {
                addLog("Sleep single detail: day=$day, pkg=$packageNumber")
            }

            override fun onReadSleepComplete() {
                addLog("✓ Sleep single day read complete")
                _readProgress.value = VBandReadProgress(1f, true)
            }
        }, day, watchDay)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // HEART RATE
    // ═══════════════════════════════════════════════════════════════════════════

    actual override fun startDetectHeart() {
        addLog(">>> CMD: Start Heart Rate Detection")
        vpManager.startDetectHeart(writeResponse, object : IHeartDataListener {
            override fun onDataChange(heartData: HeartData?) {
                heartData ?: return
                val status = when (heartData.heartStatus) {
                    EHeartStatus.STATE_INIT -> VBandHeartStatus.STATE_INIT
                    EHeartStatus.STATE_HEART_BUSY -> VBandHeartStatus.STATE_HEART_BUSY
                    EHeartStatus.STATE_HEART_DETECT -> VBandHeartStatus.STATE_HEART_DETECT
                    EHeartStatus.STATE_HEART_WEAR_ERROR -> VBandHeartStatus.STATE_HEART_WEAR_ERROR
                    EHeartStatus.STATE_HEART_NORMAL -> VBandHeartStatus.STATE_HEART_NORMAL
                    else -> VBandHeartStatus.STATE_INIT
                }
                addLog("Heart rate: ${heartData.data}, status=$status")
                _heartData.value = VBandHeartData(heartData.data, status)
            }
        })
    }

    actual override fun stopDetectHeart() {
        addLog(">>> CMD: Stop Heart Rate Detection")
        vpManager.stopDetectHeart(writeResponse)
    }

    actual override fun readHeartWarning() {
        addLog(">>> CMD: Read Heart Warning")
        vpManager.readHeartWarning(writeResponse, object : IHeartWaringDataListener {
            override fun onHeartWaringDataChange(data: HeartWaringData?) {
                data ?: return
                val status = mapHeartWarningStatus(data.status)
                addLog("✓ Heart warning: high=${data.heartHigh}, low=${data.heartLow}, open=${data.isOpen}")
                _heartWarningData.value =
                    VBandHeartWarningData(status, data.heartHigh, data.heartLow, data.isOpen)
            }
        })
    }

    actual override fun settingHeartWarning(high: Int, low: Int, isOpen: Boolean) {
        addLog(">>> CMD: Set Heart Warning (high=$high, low=$low, open=$isOpen)")
        val setting = HeartWaringSetting(high, low, isOpen)
        vpManager.settingHeartWarning(writeResponse, object : IHeartWaringDataListener {
            override fun onHeartWaringDataChange(data: HeartWaringData?) {
                data ?: return
                val status = mapHeartWarningStatus(data.status)
                addLog("Heart warning set result: $status")
                _heartWarningData.value =
                    VBandHeartWarningData(status, data.heartHigh, data.heartLow, data.isOpen)
            }
        }, setting)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEMPERATURE
    // ═══════════════════════════════════════════════════════════════════════════

    actual override fun startDetectTemperature() {
        addLog(">>> CMD: Start Temperature Detection")
        vpManager.startDetectTempture(writeResponse, object : ITemptureDetectDataListener {
            override fun onDataChange(data: TemptureDetectData?) {
                data ?: return
                addLog("Temperature: ${data.tempture}°C, progress=${data.progress}")
                _temperatureDetectData.value = VBandTemperatureDetectData(
                    isSupported = data.oprate != 0,
                    deviceState = data.deviceState,
                    progress = data.progress,
                    temperature = data.tempture,
                    baseTemperature = data.temptureBase
                )
            }
        })
    }

    actual override fun stopDetectTemperature() {
        addLog(">>> CMD: Stop Temperature Detection")
        vpManager.stopDetectTempture(writeResponse, null)
    }

    actual override fun readTemperatureData(day: Int, watchDay: Int) {
        addLog(">>> CMD: Read Temperature Data (day=$day, watchDay=$watchDay)")
        _temperatureRecords.value = emptyList()

        var currentAllPackage = 0
        var currentPackageNum = 0

        val readSetting = ReadOriginSetting(day, 1, false, watchDay)
        vpManager.readTemptureDataBySetting(writeResponse, object : ITemptureDataListener {
            override fun onTemptureDataListDataChange(dataList: MutableList<TemptureData>?) {
                dataList ?: return
                val records = dataList.mapIndexed { index, data ->
                    VBandTemperatureRecord(
                        allPackage = currentAllPackage,          // from onReadOriginProgressDetail
                        packageNumber = index,                   // no public getter, use index as fallback
                        hour = data.getmTime()?.hour ?: 0,      // must use getmTime() explicitly
                        minute = data.getmTime()?.minute ?: 0,
                        isManual = data.isFromHandler,
                        temperature = data.tempture,
                        baseTemperature = data.baseTempture
                    )
                }
                addLog("Temperature records: ${records.size} entries")
                _temperatureRecords.value = records
            }

            override fun onReadOriginProgressDetail(
                day: Int,
                date: String?,
                allPackage: Int,
                currentPackage: Int
            ) {
                currentAllPackage = allPackage   // capture here since TemptureData has no getter
                currentPackageNum = currentPackage
                addLog("Temp read: day=$day, $currentPackage/$allPackage")
            }

            override fun onReadOriginProgress(progress: Float) {
                _readProgress.value = VBandReadProgress(progress, false)
            }

            override fun onReadOriginComplete() {
                addLog("✓ Temperature data read complete")
                _readProgress.value = VBandReadProgress(1f, true)
            }
        }, readSetting)
    }

     override fun readTemperatureDataSingleDay(day: Int, watchDay: Int) {
        addLog(">>> CMD: Read Temperature Data Single Day (day=$day, watchDay=$watchDay)")
        _temperatureRecords.value = emptyList()

        var currentAllPackage = 0

        val readSetting = ReadOriginSetting(day, 1, true, watchDay)
        vpManager.readTemptureDataBySetting(writeResponse, object : ITemptureDataListener {
            override fun onTemptureDataListDataChange(dataList: MutableList<TemptureData>?) {
                dataList ?: return
                val records = dataList.mapIndexed { index, data ->
                    VBandTemperatureRecord(
                        allPackage = currentAllPackage,
                        packageNumber = index,
                        hour = data.getmTime()?.hour ?: 0,
                        minute = data.getmTime()?.minute ?: 0,
                        isManual = data.isFromHandler,
                        temperature = data.tempture,
                        baseTemperature = data.baseTempture
                    )
                }
                addLog("Temp single day records: ${records.size} entries")
                _temperatureRecords.value = records
            }

            override fun onReadOriginProgressDetail(
                day: Int,
                date: String?,
                allPackage: Int,
                currentPackage: Int
            ) {
                currentAllPackage = allPackage
                addLog("Temp single: day=$day, $currentPackage/$allPackage")
            }

            override fun onReadOriginProgress(progress: Float) {
                _readProgress.value = VBandReadProgress(progress, false)
            }

            override fun onReadOriginComplete() {
                addLog("✓ Temperature single day read complete")
                _readProgress.value = VBandReadProgress(1f, true)
            }
        }, readSetting)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PERSONALIZATION / CUSTOM SETTINGS
    // ═══════════════════════════════════════════════════════════════════════════

    actual override fun readCustomSetting() {
        addLog(">>> CMD: Read Custom Settings")
        vpManager.readCustomSetting(writeResponse, object : ICustomSettingDataListener {
            override fun OnSettingDataChange(data: CustomSettingData?) {
                data ?: return
                addLog("✓ Custom settings read")
                updateCustomSettingData(data)
            }
        })
    }

    actual override fun changeCustomSetting(setting: VBandCustomSettingData) {
        addLog(">>> CMD: Change Custom Settings")

        val customSetting = CustomSetting(
            setting.metricSystem != VBandFunctionStatus.UNSUPPORT,  // isHaveMetricSystem
            setting.metricSystem == VBandFunctionStatus.SUPPORT_OPEN, // isMetricSystem
            setting.is24Hour,                                          // is24Hour
            setting.autoHeartDetect == VBandFunctionStatus.SUPPORT_OPEN, // isOpenAutoHeartDetect
            setting.autoBpDetect == VBandFunctionStatus.SUPPORT_OPEN     // isOpenAutoBpDetect
        )

        // Correct setter names from Java class
        customSetting.setIsOpenSportRemain(mapToEFunctionStatus(setting.sportOverRemind))
        customSetting.setIsOpenSpo2hLowRemind(mapToEFunctionStatus(setting.lowSpo2Remind))
        customSetting.setIsOpenAutoHRV(mapToEFunctionStatus(setting.autoHrv))
        customSetting.setIsOpenDisconnectRemind(mapToEFunctionStatus(setting.disconnectRemind))
        customSetting.setIsOpenPPG(mapToEFunctionStatus(setting.ppg))
        customSetting.setIsOpenMusicControl(mapToEFunctionStatus(setting.musicControl))
        customSetting.setIsOpenAutoTemperatureDetect(mapToEFunctionStatus(setting.autoTemperatureDetect))
        customSetting.setIsOpenBloodGlucoseDetect(mapToEFunctionStatus(setting.bloodGlucoseDetection))

        customSetting.setTemperatureUnit(
            when (setting.temperatureUnit) {
                VBandTemperatureUnit.CELSIUS -> ETemperatureUnit.CELSIUS
                VBandTemperatureUnit.FAHRENHEIT -> ETemperatureUnit.FAHRENHEIT
                else -> ETemperatureUnit.NONE
            }
        )

        customSetting.setBloodGlucoseUnit(
            when (setting.bloodGlucoseUnit) {
                VBandBloodGlucoseUnit.MMOL_L -> EBloodGlucoseUnit.mmol_L
                VBandBloodGlucoseUnit.MG_DL -> EBloodGlucoseUnit.mg_dl
                else -> EBloodGlucoseUnit.NONE
            }
        )

        vpManager.changeCustomSetting(writeResponse, object : ICustomSettingDataListener {
            override fun OnSettingDataChange(data: CustomSettingData?) {
                data ?: return
                addLog("✓ Custom settings updated")
                updateCustomSettingData(data)
            }
        }, customSetting)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // NIGHT TURN WRIST
    // ═══════════════════════════════════════════════════════════════════════════

    actual override fun readNightTurnWrist() {
        addLog(">>> CMD: Read Night Turn Wrist")
        vpManager.readNightTurnWriste(writeResponse, object : INightTurnWristeDataListener {
            override fun onNightTurnWristeDataChange(data: NightTurnWristeData?) {
                data ?: return
                addLog("✓ Night turn wrist: open=${data.isNightTureWirsteStatusOpen}")
                _nightTurnWristData.value = mapNightTurnWristData(data)
            }
        })
    }

    actual override fun settingNightTurnWrist(
        isOpen: Boolean,
        startHour: Int, startMinute: Int,
        endHour: Int, endMinute: Int,
        level: Int
    ) {
        addLog(">>> CMD: Set Night Turn Wrist (open=$isOpen, $startHour:$startMinute-$endHour:$endMinute, level=$level)")
        val setting = NightTurnWristSetting(
            isOpen,
            TimeData(startHour, startMinute),
            TimeData(endHour, endMinute),
            level
        )
        vpManager.settingNightTurnWriste(writeResponse, object : INightTurnWristeDataListener {
            override fun onNightTurnWristeDataChange(data: NightTurnWristeData?) {
                data ?: return
                addLog("Night turn wrist set result")
                _nightTurnWristData.value = mapNightTurnWristData(data)
            }
        }, setting)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SCREEN BRIGHTNESS
    // ═══════════════════════════════════════════════════════════════════════════

    actual override fun readScreenLight() {
        addLog(">>> CMD: Read Screen Light")
        vpManager.readScreenLight(writeResponse) { data ->
            data ?: return@readScreenLight
            addLog("✓ Screen light: level=${data.screenSetting?.level}")

            val setting = data.screenSetting
            val status = when (data.status) {
                EScreenLight.SETTING_SUCCESS -> VBandScreenLightStatus.SETTING_SUCCESS
                EScreenLight.SETTING_FAIL -> VBandScreenLightStatus.SETTING_FAIL
                EScreenLight.READ_SUCCESS -> VBandScreenLightStatus.READ_SUCCESS
                EScreenLight.READ_FAIL -> VBandScreenLightStatus.READ_FAIL
                else -> VBandScreenLightStatus.UNKNOWN
            }

            _screenLightData.value = VBandScreenLightData(
                status = status,
                startHour = setting?.startHour ?: 22,
                startMinute = setting?.startMinute ?: 0,
                endHour = setting?.endHour ?: 7,
                endMinute = setting?.endMinute ?: 0,
                level = setting?.level ?: 2,
                otherLevel = setting?.otherLeverl ?: 4,
                autoMode = setting?.auto ?: 0,
                maxLevel = setting?.maxLevel ?: 5
            )
        }
    }

    actual override fun settingScreenLight(
        startHour: Int, startMinute: Int,
        endHour: Int, endMinute: Int,
        level: Int, otherLevel: Int
    ) {
        addLog(">>> CMD: Set Screen Light ($startHour:$startMinute-$endHour:$endMinute, level=$level, other=$otherLevel)")
        val setting = ScreenSetting(startHour, startMinute, endHour, endMinute, level, otherLevel)
        vpManager.settingScreenLight(writeResponse, { data ->
            data ?: return@settingScreenLight
            addLog("Screen light set result")

            val s = data.screenSetting
            _screenLightData.value = VBandScreenLightData(
                status = VBandScreenLightStatus.SETTING_SUCCESS,
                startHour = s?.startHour ?: startHour,
                startMinute = s?.startMinute ?: startMinute,
                endHour = s?.endHour ?: endHour,
                endMinute = s?.endMinute ?: endMinute,
                level = s?.level ?: level,
                otherLevel = s?.otherLeverl ?: otherLevel,
                autoMode = s?.auto ?: 0,
                maxLevel = s?.maxLevel ?: 5
            )
        }, setting)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SCREEN ON TIME
    // ═══════════════════════════════════════════════════════════════════════════

    actual override fun readScreenLightTime() {
        addLog(">>> CMD: Read Screen Light Time")
        vpManager.readScreenLightTime(writeResponse, object : IScreenLightTimeListener {
            override fun onScreenLightTimeDataChange(data: ScreenLightTimeData?) {
                data ?: return
                val status = when (data.screenLightState) {
                    EScreenLightTime.READ_SUCCESS -> VBandScreenLightTimeStatus.READ_SUCCESS
                    EScreenLightTime.READ_FAIL -> VBandScreenLightTimeStatus.READ_FAIL
                    EScreenLightTime.SETTING_SUCCESS -> VBandScreenLightTimeStatus.SETTING_SUCCESS
                    EScreenLightTime.SETTING_FAIL -> VBandScreenLightTimeStatus.SETTING_FAIL
                    else -> VBandScreenLightTimeStatus.UNKNOWN
                }
                addLog("✓ Screen light time: current=${data.currentDuration}s")
                _screenLightTimeData.value = VBandScreenLightTimeData(
                    status = status,
                    currentDuration = data.currentDuration,
                    recommendDuration = data.recommendDuration,
                    maxDuration = data.maxDuration,
                    minDuration = data.minDuration
                )
            }
        })
    }

    actual override fun setScreenLightTime(seconds: Int) {
        addLog(">>> CMD: Set Screen Light Time (${seconds}s)")
        vpManager.setScreenLightTime(writeResponse, object : IScreenLightTimeListener {
            override fun onScreenLightTimeDataChange(data: ScreenLightTimeData?) {
                data ?: return
                addLog("Screen light time set result")
                _screenLightTimeData.value = VBandScreenLightTimeData(
                    status = VBandScreenLightTimeStatus.SETTING_SUCCESS,
                    currentDuration = data.currentDuration,
                    recommendDuration = data.recommendDuration,
                    maxDuration = data.maxDuration,
                    minDuration = data.minDuration
                )
            }
        }, seconds)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // HEALTH REMINDERS
    // ═══════════════════════════════════════════════════════════════════════════

    actual override fun readHealthRemind(type: VBandHealthRemindType) {
        addLog(">>> CMD: Read Health Remind (type=$type)")
        val sdkType = mapToSdkHealthRemindType(type)

        vpManager.readHealthRemind(sdkType, object : IHealthRemindListener {
            override fun functionNotSupport() {
                addLog("Health remind: not supported")
            }

            override fun onHealthRemindRead(remind: HealthRemind) {
                addLog("✓ Health remind read: type=${remind.remindType}, on=${remind.status}")
                val current = _healthRemindList.value.toMutableList()
                val mapped = mapHealthRemind(remind)
                val idx = current.indexOfFirst { it.type == mapped.type }
                if (idx >= 0) current[idx] = mapped else current.add(mapped)
                _healthRemindList.value = current
            }

            override fun onHealthRemindReadFailed() {
                addLog("Health remind read failed")
            }

            override fun onHealthRemindReadingComplete() {
                addLog("Health remind reading complete")
            }

            override fun onHealthRemindReport(remind: HealthRemind) {
                addLog("Health remind reported")
            }

            override fun onHealthRemindReportFailed() {
                addLog("Health remind report failed")
            }

            override fun onHealthRemindSettingSuccess(remind: HealthRemind) {
                addLog("Health remind setting success")
            }

            override fun onHealthRemindSettingFailed(type: HealthRemindType) {
                addLog("Health remind setting failed: $type")
            }
        }, writeResponse)
    }

    actual override fun settingHealthRemind(remind: VBandHealthRemind) {
        addLog(">>> CMD: Set Health Remind (type=${remind.type}, on=${remind.isOn})")
        val sdkRemind = HealthRemind(
            mapToSdkHealthRemindType(remind.type),
            TimeData(remind.startHour, remind.startMinute),
            TimeData(remind.endHour, remind.endMinute),
            remind.interval,
            remind.isOn
        )

        vpManager.settingHealthRemind(sdkRemind, object : IHealthRemindListener {
            override fun functionNotSupport() { addLog("Health remind: not supported") }

            override fun onHealthRemindRead(remind: HealthRemind) {}

            override fun onHealthRemindReadFailed() {}

            override fun onHealthRemindReadingComplete() {}

            override fun onHealthRemindReport(remind: HealthRemind) {}

            override fun onHealthRemindReportFailed() {}

            override fun onHealthRemindSettingSuccess(remind: HealthRemind) {
                addLog("✓ Health remind set successfully")
                val current = _healthRemindList.value.toMutableList()
                val mapped = mapHealthRemind(remind)
                val idx = current.indexOfFirst { it.type == mapped.type }
                if (idx >= 0) current[idx] = mapped else current.add(mapped)
                _healthRemindList.value = current
            }

            override fun onHealthRemindSettingFailed(type: HealthRemindType) {
                addLog("Health remind setting failed: $type")
            }
        }, writeResponse)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // LANGUAGE
    // ═══════════════════════════════════════════════════════════════════════════

    actual override fun settingLanguage(language: VBandLanguage) {
        addLog(">>> CMD: Set Language ($language)")
        val sdkLang = mapToSdkLanguage(language)

        vpManager.settingDeviceLanguage(writeResponse, object : ILanguageDataListener {
            override fun onLanguageDataChange(data: LanguageData?) {
                data ?: return
                addLog("✓ Language set: ${data.language}")
                _languageData.value = VBandLanguageData(
                    status = if (data.stauts == EOprateStauts.OPRATE_SUCCESS)
                        VBandOperateStatus.SUCCESS else VBandOperateStatus.FAIL,
                    language = mapFromSdkLanguage(data.language)
                )
            }
        }, sdkLang)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SEQUENTIAL SYNC — SUSPEND WRAPPERS
    // ═══════════════════════════════════════════════════════════════════════════


    /** Suspend wrapper: read battery and wait for callback */
    private suspend fun suspendReadBattery() = withTimeout(SYNC_CMD_TIMEOUT_MS) {
        suspendCancellableCoroutine { cont ->
            vpManager.readBattery(writeResponse) { data ->
                if (data != null) {
                    _batteryData.value = VBandBatteryData(
                        batteryLevel = data.batteryLevel,
                        batteryPercent = data.batteryPercent,
                        powerModel = data.powerModel,
                        isLowBattery = data.isLowBattery,
                        isPercent = data.isPercent
                    )
                    addSyncLog(VBandLogLevel.SUCCESS, "Battery: ${if (data.isPercent) "${data.batteryPercent}%" else "level ${data.batteryLevel}"}", "Battery")
                    cont.resume(Unit)
                } else {
                    addSyncLog(VBandLogLevel.WARN, "Battery: null response", "Battery")
                    cont.resume(Unit)
                }
            }
        }
    }

    /** Suspend wrapper: read sport step and wait for callback */
    private suspend fun suspendReadSportStep() = withTimeout(SYNC_CMD_TIMEOUT_MS) {
        suspendCancellableCoroutine { cont ->
            vpManager.readSportStep(writeResponse) { data ->
                if (data != null) {
                    _sportData.value = VBandSportData(
                        step = data.step,
                        distance = data.dis,
                        calories = data.kcal,
                        calcType = data.calcType
                    )
                    addSyncLog(VBandLogLevel.SUCCESS, "Steps: ${data.step}, Dist: ${data.dis}km, Cal: ${data.kcal}kcal", "Steps")
                    cont.resume(Unit)
                } else {
                    addSyncLog(VBandLogLevel.WARN, "Steps: null response", "Steps")
                    cont.resume(Unit)
                }
            }
        }
    }

    /** Suspend wrapper: read sleep data (all days) and wait for complete callback */
    private suspend fun suspendReadSleepData(watchDay: Int) = withTimeout(SYNC_CMD_TIMEOUT_MS * 2) {
        suspendCancellableCoroutine { cont ->
            _sleepDataList.value = emptyList()
            var sleepCount = 0
            vpManager.readSleepData(writeResponse, object : ISleepDataListener {
                override fun onSleepDataChange(day: String?, sleepData: SleepData?) {
                    sleepData ?: return
                    sleepCount++
                    val current = _sleepDataList.value.toMutableList()
                    current.add(mapSleepData(day ?: "", sleepData))
                    _sleepDataList.value = current
                    addSyncLog(VBandLogLevel.INFO, "Sleep day=$day, total=${sleepData.allSleepTime}min", "Sleep")
                }

                override fun onSleepProgress(progress: Float) {
                    updateSyncState(stepProgress = progress)
                }

                override fun onSleepProgressDetail(day: String?, packageNumber: Int) {}

                override fun onReadSleepComplete() {
                    addSyncLog(VBandLogLevel.SUCCESS, "Sleep data complete: $sleepCount records", "Sleep")
                    cont.resume(Unit)
                }
            }, watchDay)
        }
    }

    /** Suspend wrapper: read sleep data for a single day */
    private suspend fun suspendReadSleepSingleDay(day: Int, watchDay: Int) = withTimeout(SYNC_CMD_TIMEOUT_MS) {
        suspendCancellableCoroutine { cont ->
            _sleepDataList.value = emptyList()
            vpManager.readSleepDataSingleDay(writeResponse, object : ISleepDataListener {
                override fun onSleepDataChange(dayStr: String?, sleepData: SleepData?) {
                    sleepData ?: return
                    val current = _sleepDataList.value.toMutableList()
                    current.add(mapSleepData(dayStr ?: "", sleepData))
                    _sleepDataList.value = current
                    addSyncLog(VBandLogLevel.INFO, "Sleep: total=${sleepData.allSleepTime}min, deep=${sleepData.deepSleepTime}min", "Sleep (Day $day)")
                }

                override fun onSleepProgress(progress: Float) {
                    updateSyncState(stepProgress = progress)
                }

                override fun onSleepProgressDetail(dayStr: String?, packageNumber: Int) {}

                override fun onReadSleepComplete() {
                    addSyncLog(VBandLogLevel.SUCCESS, "Sleep single day complete", "Sleep (Day $day)")
                    cont.resume(Unit)
                }
            }, day, watchDay)
        }
    }

    /** Suspend wrapper: read origin data (all days) and wait for complete callback */
    private suspend fun suspendReadOriginData(watchDay: Int) = withTimeout(SYNC_CMD_TIMEOUT_MS * 3) {
        suspendCancellableCoroutine { cont ->
            _originDataList.value = emptyList()
            _originHalfHourDataList.value = emptyList()
            var originCount = 0
            vpManager.readOriginData(writeResponse, object : IOriginDataListener {
                override fun onOringinFiveMinuteDataChange(originData: OriginData?) {
                    originData ?: return
                    originCount++
                    val current = _originDataList.value.toMutableList()
                    current.add(mapOriginData(originData))
                    _originDataList.value = current
                }

                override fun onOringinHalfHourDataChange(halfHourData: OriginHalfHourData?) {
                    halfHourData ?: return
                    val current = _originHalfHourDataList.value.toMutableList()
                    current.add(mapOriginHalfHourData(halfHourData))
                    _originHalfHourDataList.value = current
                }

                override fun onReadOriginProgressDetail(
                    day: Int, date: String?, allPackage: Int, currentPackage: Int
                ) {
                    addSyncLog(VBandLogLevel.DEBUG, "Origin: day=$day date=$date $currentPackage/$allPackage", "Health Data")
                }

                override fun onReadOriginProgress(progress: Float) {
                    updateSyncState(stepProgress = progress)
                }

                override fun onReadOriginComplete() {
                    addSyncLog(VBandLogLevel.SUCCESS, "Origin data complete: $originCount 5-min records, ${_originHalfHourDataList.value.size} 30-min records", "Health Data")
                    cont.resume(Unit)
                }
            }, watchDay)
        }
    }

    /** Suspend wrapper: read origin data for a single day */
    private suspend fun suspendReadOriginSingleDay(day: Int, watchDay: Int) = withTimeout(SYNC_CMD_TIMEOUT_MS * 2) {
        suspendCancellableCoroutine { cont ->
            _originDataList.value = emptyList()
            _originHalfHourDataList.value = emptyList()
            var originCount = 0
            vpManager.readOriginDataSingleDay(writeResponse, object : IOriginDataListener {
                override fun onOringinFiveMinuteDataChange(originData: OriginData?) {
                    originData ?: return
                    originCount++
                    val current = _originDataList.value.toMutableList()
                    current.add(mapOriginData(originData))
                    _originDataList.value = current
                }

                override fun onOringinHalfHourDataChange(halfHourData: OriginHalfHourData?) {
                    halfHourData ?: return
                    val current = _originHalfHourDataList.value.toMutableList()
                    current.add(mapOriginHalfHourData(halfHourData))
                    _originHalfHourDataList.value = current
                }

                override fun onReadOriginProgressDetail(
                    day: Int, date: String?, allPackage: Int, currentPackage: Int
                ) {
                    addSyncLog(VBandLogLevel.DEBUG, "Origin: $currentPackage/$allPackage", "Health (Day $day)")
                }

                override fun onReadOriginProgress(progress: Float) {
                    updateSyncState(stepProgress = progress)
                }

                override fun onReadOriginComplete() {
                    addSyncLog(VBandLogLevel.SUCCESS, "Origin single day complete: $originCount records", "Health (Day $day)")
                    cont.resume(Unit)
                }
            }, day, 1, watchDay)
        }
    }

    /** Suspend wrapper: read temperature data (all days) */
    private suspend fun suspendReadTemperatureData(watchDay: Int) = withTimeout(SYNC_CMD_TIMEOUT_MS * 2) {
        suspendCancellableCoroutine { cont ->
            _temperatureRecords.value = emptyList()
            var currentAllPackage = 0
            val readSetting = ReadOriginSetting(0, 1, false, watchDay)
            vpManager.readTemptureDataBySetting(writeResponse, object : ITemptureDataListener {
                override fun onTemptureDataListDataChange(dataList: MutableList<TemptureData>?) {
                    dataList ?: return
                    val records = dataList.mapIndexed { index, data ->
                        VBandTemperatureRecord(
                            allPackage = currentAllPackage,
                            packageNumber = index,
                            hour = data.getmTime()?.hour ?: 0,
                            minute = data.getmTime()?.minute ?: 0,
                            isManual = data.isFromHandler,
                            temperature = data.tempture,
                            baseTemperature = data.baseTempture
                        )
                    }
                    _temperatureRecords.value = records
                    addSyncLog(VBandLogLevel.INFO, "Temperature: ${records.size} records", "Temperature")
                }

                override fun onReadOriginProgressDetail(
                    day: Int, date: String?, allPackage: Int, currentPackage: Int
                ) {
                    currentAllPackage = allPackage
                }

                override fun onReadOriginProgress(progress: Float) {
                    updateSyncState(stepProgress = progress)
                }

                override fun onReadOriginComplete() {
                    addSyncLog(VBandLogLevel.SUCCESS, "Temperature data complete", "Temperature")
                    cont.resume(Unit)
                }
            }, readSetting)
        }
    }

    /** Suspend wrapper: read temperature for a single day */
    private suspend fun suspendReadTemperatureSingleDay(day: Int, watchDay: Int) = withTimeout(SYNC_CMD_TIMEOUT_MS) {
        suspendCancellableCoroutine { cont ->
            _temperatureRecords.value = emptyList()
            var currentAllPackage = 0
            val readSetting = ReadOriginSetting(day, 1, true, watchDay)
            vpManager.readTemptureDataBySetting(writeResponse, object : ITemptureDataListener {
                override fun onTemptureDataListDataChange(dataList: MutableList<TemptureData>?) {
                    dataList ?: return
                    val records = dataList.mapIndexed { index, data ->
                        VBandTemperatureRecord(
                            allPackage = currentAllPackage,
                            packageNumber = index,
                            hour = data.getmTime()?.hour ?: 0,
                            minute = data.getmTime()?.minute ?: 0,
                            isManual = data.isFromHandler,
                            temperature = data.tempture,
                            baseTemperature = data.baseTempture
                        )
                    }
                    _temperatureRecords.value = records
                    addSyncLog(VBandLogLevel.INFO, "Temp single day: ${records.size} records", "Temp (Day $day)")
                }

                override fun onReadOriginProgressDetail(
                    day: Int, date: String?, allPackage: Int, currentPackage: Int
                ) {
                    currentAllPackage = allPackage
                }

                override fun onReadOriginProgress(progress: Float) {
                    updateSyncState(stepProgress = progress)
                }

                override fun onReadOriginComplete() {
                    addSyncLog(VBandLogLevel.SUCCESS, "Temperature single day complete", "Temp (Day $day)")
                    cont.resume(Unit)
                }
            }, readSetting)
        }
    }

    /** Suspend wrapper: read custom settings */
    private suspend fun suspendReadCustomSetting() = withTimeout(SYNC_CMD_TIMEOUT_MS) {
        suspendCancellableCoroutine { cont ->
            vpManager.readCustomSetting(writeResponse, object : ICustomSettingDataListener {
                override fun OnSettingDataChange(data: CustomSettingData?) {
                    if (data != null) {
                        updateCustomSettingData(data)
                        addSyncLog(VBandLogLevel.SUCCESS, "Custom settings loaded", "Settings")
                    } else {
                        addSyncLog(VBandLogLevel.WARN, "Custom settings: null", "Settings")
                    }
                    cont.resume(Unit)
                }
            })
        }
    }

    /** Suspend wrapper: read heart warning */
    private suspend fun suspendReadHeartWarning() = withTimeout(SYNC_CMD_TIMEOUT_MS) {
        suspendCancellableCoroutine { cont ->
            vpManager.readHeartWarning(writeResponse, object : IHeartWaringDataListener {
                override fun onHeartWaringDataChange(data: HeartWaringData?) {
                    if (data != null) {
                        val status = mapHeartWarningStatus(data.status)
                        _heartWarningData.value = VBandHeartWarningData(status, data.heartHigh, data.heartLow, data.isOpen)
                        addSyncLog(VBandLogLevel.SUCCESS, "Heart warning: high=${data.heartHigh}, low=${data.heartLow}, open=${data.isOpen}", "Heart Warning")
                    } else {
                        addSyncLog(VBandLogLevel.WARN, "Heart warning: null", "Heart Warning")
                    }
                    cont.resume(Unit)
                }
            })
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SEQUENTIAL SYNC — PUBLIC API
    // ═══════════════════════════════════════════════════════════════════════════

     override suspend fun syncAllData(watchDay: Int) {
        syncMutex.withLock {
            addSyncLog(VBandLogLevel.INFO, "═══ Starting full sync (watchDay=$watchDay) ═══", "Sync")
            updateSyncState(
                status = VBandSyncStatus.SYNCING,
                stepName = "Battery",
                stepIndex = 1,
                totalSteps = 7,
                stepProgress = 0f,
                errorMessage = "",
                syncedDay = -1
            )

            try {
                // Step 1: Battery
                addSyncLog(VBandLogLevel.INFO, "Reading battery...", "Battery")
                suspendReadBattery()
                delay(SYNC_CMD_DELAY_MS)

                // Step 2: Steps
                updateSyncState(stepName = "Steps", stepIndex = 2, stepProgress = 0f)
                addSyncLog(VBandLogLevel.INFO, "Reading steps...", "Steps")
                suspendReadSportStep()
                delay(SYNC_CMD_DELAY_MS)

                // Step 3: Sleep
                updateSyncState(stepName = "Sleep Data", stepIndex = 3, stepProgress = 0f)
                addSyncLog(VBandLogLevel.INFO, "Reading sleep data...", "Sleep")
                suspendReadSleepData(watchDay)
                delay(SYNC_CMD_DELAY_MS)

                // Step 4: Origin/Health data
                updateSyncState(stepName = "Heart Rate & Activity", stepIndex = 4, stepProgress = 0f)
                addSyncLog(VBandLogLevel.INFO, "Reading origin data (heart rate, steps, BP)...", "Health Data")
                suspendReadOriginData(watchDay)
                delay(SYNC_CMD_DELAY_MS)

                // Step 5: Temperature
                updateSyncState(stepName = "Temperature", stepIndex = 5, stepProgress = 0f)
                addSyncLog(VBandLogLevel.INFO, "Reading temperature data...", "Temperature")
                suspendReadTemperatureData(watchDay)
                delay(SYNC_CMD_DELAY_MS)

                // Step 6: Settings
                updateSyncState(stepName = "Settings", stepIndex = 6, stepProgress = 0f)
                addSyncLog(VBandLogLevel.INFO, "Reading custom settings...", "Settings")
                suspendReadCustomSetting()
                delay(SYNC_CMD_DELAY_MS)

                // Step 7: Heart Warning
                updateSyncState(stepName = "Heart Warning", stepIndex = 7, stepProgress = 0f)
                addSyncLog(VBandLogLevel.INFO, "Reading heart warning...", "Heart Warning")
                suspendReadHeartWarning()

                updateSyncState(status = VBandSyncStatus.COMPLETED, stepProgress = 1f)
                addSyncLog(VBandLogLevel.SUCCESS, "═══ Full sync completed ═══", "Sync")

            } catch (e: TimeoutCancellationException) {
                val msg = "Sync timeout at step: ${_syncState.value.currentStepName}"
                addSyncLog(VBandLogLevel.ERROR, msg, "Sync")
                updateSyncState(status = VBandSyncStatus.ERROR, errorMessage = msg)
                throw e
            } catch (e: CancellationException) {
                addSyncLog(VBandLogLevel.WARN, "Sync cancelled", "Sync")
                updateSyncState(status = VBandSyncStatus.CANCELLED)
                throw e
            } catch (e: Exception) {
                val msg = "Sync failed: ${e.message}"
                addSyncLog(VBandLogLevel.ERROR, msg, "Sync")
                updateSyncState(status = VBandSyncStatus.ERROR, errorMessage = msg)
                throw e
            }
        }
    }

     override suspend fun syncDayData(day: Int, watchDay: Int) {
        syncMutex.withLock {
            val dayLabel = when (day) {
                0 -> "Today"
                1 -> "Yesterday"
                else -> "$day days ago"
            }
            addSyncLog(VBandLogLevel.INFO, "═══ Starting sync for $dayLabel ═══", "Sync")
            updateSyncState(
                status = VBandSyncStatus.SYNCING,
                stepName = "Sleep ($dayLabel)",
                stepIndex = 1,
                totalSteps = 3,
                stepProgress = 0f,
                errorMessage = "",
                syncedDay = day
            )

            try {
                // Step 1: Sleep for this day
                addSyncLog(VBandLogLevel.INFO, "Reading sleep for $dayLabel...", "Sleep ($dayLabel)")
                suspendReadSleepSingleDay(day, watchDay)
                delay(SYNC_CMD_DELAY_MS)

                // Step 2: Origin/Health data for this day
                updateSyncState(stepName = "Health ($dayLabel)", stepIndex = 2, stepProgress = 0f)
                addSyncLog(VBandLogLevel.INFO, "Reading health data for $dayLabel...", "Health ($dayLabel)")
                suspendReadOriginSingleDay(day, watchDay)
                delay(SYNC_CMD_DELAY_MS)

                // Step 3: Temperature for this day
                updateSyncState(stepName = "Temp ($dayLabel)", stepIndex = 3, stepProgress = 0f)
                addSyncLog(VBandLogLevel.INFO, "Reading temperature for $dayLabel...", "Temp ($dayLabel)")
                suspendReadTemperatureSingleDay(day, watchDay)

                updateSyncState(status = VBandSyncStatus.COMPLETED, stepProgress = 1f)
                addSyncLog(VBandLogLevel.SUCCESS, "═══ $dayLabel sync completed ═══", "Sync")

            } catch (e: TimeoutCancellationException) {
                val msg = "Day sync timeout at step: ${_syncState.value.currentStepName}"
                addSyncLog(VBandLogLevel.ERROR, msg, "Sync")
                updateSyncState(status = VBandSyncStatus.ERROR, errorMessage = msg)
                throw e
            } catch (e: CancellationException) {
                addSyncLog(VBandLogLevel.WARN, "Day sync cancelled", "Sync")
                updateSyncState(status = VBandSyncStatus.CANCELLED)
                throw e
            } catch (e: Exception) {
                val msg = "Day sync failed: ${e.message}"
                addSyncLog(VBandLogLevel.ERROR, msg, "Sync")
                updateSyncState(status = VBandSyncStatus.ERROR, errorMessage = msg)
                throw e
            }
        }
    }

     override fun cancelSync() {
        syncJob?.cancel()
        syncJob = null
        addSyncLog(VBandLogLevel.WARN, "Sync cancelled by user", "Sync")
        updateSyncState(status = VBandSyncStatus.CANCELLED)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UTILITY
    // ═══════════════════════════════════════════════════════════════════════════

    actual override fun clearLogs() {
        _logs.value = emptyList()
        addLog("Logs cleared")
    }

     override fun clearSyncLogs() {
        _syncLogs.value = emptyList()
        _syncState.value = VBandSyncState()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MAPPING HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    private fun mapFunctionStatus(status: EFunctionStatus?): VBandFunctionStatus {
        return when (status) {
            EFunctionStatus.UNSUPPORT -> VBandFunctionStatus.UNSUPPORT
            EFunctionStatus.SUPPORT -> VBandFunctionStatus.SUPPORT
            EFunctionStatus.SUPPORT_OPEN -> VBandFunctionStatus.SUPPORT_OPEN
            EFunctionStatus.SUPPORT_CLOSE -> VBandFunctionStatus.SUPPORT_CLOSE
            else -> VBandFunctionStatus.UNKNOWN
        }
    }

    private fun mapToEFunctionStatus(status: VBandFunctionStatus): EFunctionStatus {
        return when (status) {
            VBandFunctionStatus.UNSUPPORT -> EFunctionStatus.UNSUPPORT
            VBandFunctionStatus.SUPPORT -> EFunctionStatus.SUPPORT
            VBandFunctionStatus.SUPPORT_OPEN -> EFunctionStatus.SUPPORT_OPEN
            VBandFunctionStatus.SUPPORT_CLOSE -> EFunctionStatus.SUPPORT_CLOSE
            VBandFunctionStatus.UNKNOWN -> EFunctionStatus.UNKONW
        }
    }

    private fun mapHeartWarningStatus(status: EHeartWaringStatus?): VBandHeartWarningStatus {
        return when (status) {
            EHeartWaringStatus.OPEN_SUCCESS -> VBandHeartWarningStatus.OPEN_SUCCESS
            EHeartWaringStatus.OPEN_FAIL -> VBandHeartWarningStatus.OPEN_FAIL
            EHeartWaringStatus.CLOSE_SUCCESS -> VBandHeartWarningStatus.CLOSE_SUCCESS
            EHeartWaringStatus.CLOSE_FAIL -> VBandHeartWarningStatus.CLOSE_FAIL
            EHeartWaringStatus.READ_SUCCESS -> VBandHeartWarningStatus.READ_SUCCESS
            EHeartWaringStatus.READ_FAIL -> VBandHeartWarningStatus.READ_FAIL
            EHeartWaringStatus.UNSUPPORT -> VBandHeartWarningStatus.UNSUPPORT
            else -> VBandHeartWarningStatus.UNKNOWN
        }
    }

    private fun mapSleepData(day: String, data: SleepData): VBandSleepData {
        return VBandSleepData(
            date = data.date ?: day,
            sleepQuality = data.sleepQulity,
            wakeCount = data.wakeCount,
            deepSleepTime = data.deepSleepTime,
            lightSleepTime = data.lowSleepTime,
            totalSleepTime = data.allSleepTime,
            sleepLine = data.sleepLine ?: "",
            sleepDownHour = data.sleepDown?.hour ?: 0,
            sleepDownMinute = data.sleepDown?.minute ?: 0,
            sleepUpHour = data.sleepUp?.hour ?: 0,
            sleepUpMinute = data.sleepUp?.minute ?: 0
        )
    }

    private fun mapOriginData(data: OriginData): VBandOriginData {
        return VBandOriginData(
            date = data.date ?: "",
            allPackage = data.allPackage,
            packageNumber = data.packageNumber,
            hour = data.getmTime()?.hour ?: 0,
            minute = data.getmTime()?.minute ?: 0,
            rateValue = data.rateValue,
            sportValue = data.sportValue,
            stepValue = data.stepValue,
            highBpValue = data.highValue,
            lowBpValue = data.lowValue,
            calValue = data.calValue,
            disValue = data.disValue,
            temperature = data.temperature,
            baseTemperature = data.baseTemperature
        )
    }

    private fun mapOriginHalfHourData(data: OriginHalfHourData): VBandOriginHalfHourData {
        val rateList = data.halfHourRateDatas?.map { r ->
            VBandHalfHourRateData(
                date = r.date ?: "",
                hour = r.time?.hour ?: 0,
                minute = r.time?.minute ?: 0,
                rateValue = r.rateValue
            )
        } ?: emptyList()

        val bpList = data.halfHourBps?.map { b ->
            VBandHalfHourBpData(
                date = b.date ?: "",
                hour = b.time?.hour ?: 0,
                minute = b.time?.minute ?: 0,
                highValue = b.highValue,
                lowValue = b.lowValue
            )
        } ?: emptyList()

        val sportList = data.halfHourSportDatas?.map { s ->
            VBandHalfHourSportData(
                date = s.date ?: "",
                hour = s.time?.hour ?: 0,
                minute = s.time?.minute ?: 0,
                sportValue = s.sportValue,
                distance = s.disValue,
                calories = s.calValue
            )
        } ?: emptyList()

        return VBandOriginHalfHourData(
            halfHourRateData = rateList,
            halfHourBpData = bpList,
            halfHourSportData = sportList,
            allStep = data.allStep,
            date = data.date ?: ""
        )
    }

    private fun mapNightTurnWristData(data: NightTurnWristeData): VBandNightTurnWristData {
        val status = when (data.oprateStauts) {
            ENightTurnWristeStatus.SUCCESS -> VBandNightTurnWristStatus.SUCCESS
            ENightTurnWristeStatus.FAIL -> VBandNightTurnWristStatus.FAIL
            else -> VBandNightTurnWristStatus.UNKNOWN
        }
        return VBandNightTurnWristData(
            status = status,
            isSupportCustomTime = data.isSupportCustomSettingTime,
            isOpen = data.isNightTureWirsteStatusOpen,
            startHour = data.startTime?.hour ?: 20,
            startMinute = data.startTime?.minute ?: 0,
            endHour = data.endTime?.hour ?: 8,
            endMinute = data.endTime?.minute ?: 0,
            level = data.level,
            defaultLevel = data.defaultLevel
        )
    }

    private fun updateCustomSettingData(data: CustomSettingData) {
        _customSettingData.value = VBandCustomSettingData(
            is24Hour = data.is24Hour,
            metricSystem = mapFunctionStatus(data.metricSystem),
            autoHeartDetect = mapFunctionStatus(data.autoHeartDetect),
            autoBpDetect = mapFunctionStatus(data.autoBpDetect),
            sportOverRemind = mapFunctionStatus(data.sportOverRemain),
            lowSpo2Remind = mapFunctionStatus(data.lowSpo2hRemain),
            autoHrv = mapFunctionStatus(data.autoHrv),
            disconnectRemind = mapFunctionStatus(data.disconnectRemind),
            ppg = mapFunctionStatus(data.ppg),
            musicControl = mapFunctionStatus(data.musicControl),
            autoTemperatureDetect = mapFunctionStatus(data.autoTemperatureDetect),
            temperatureUnit = when (data.temperatureUnit) {
                ETemperatureUnit.CELSIUS -> VBandTemperatureUnit.CELSIUS
                ETemperatureUnit.FAHRENHEIT -> VBandTemperatureUnit.FAHRENHEIT
                else -> VBandTemperatureUnit.NONE
            },
            bloodGlucoseDetection = mapFunctionStatus(data.bloodGlucoseDetection),
            bloodGlucoseUnit = when (data.bloodGlucoseUnit) {
                EBloodGlucoseUnit.mmol_L -> VBandBloodGlucoseUnit.MMOL_L
                EBloodGlucoseUnit.mg_dl -> VBandBloodGlucoseUnit.MG_DL
                else -> VBandBloodGlucoseUnit.NONE
            }
        )
    }

    private fun mapHealthRemind(remind: HealthRemind): VBandHealthRemind {
        return VBandHealthRemind(
            type = mapFromSdkHealthRemindType(remind.remindType),
            startHour = remind.startTime?.hour ?: 8,
            startMinute = remind.startTime?.minute ?: 0,
            endHour = remind.endTime?.hour ?: 20,
            endMinute = remind.endTime?.minute ?: 0,
            interval = remind.interval,
            isOn = remind.status
        )
    }
    }

    private fun mapToSdkHealthRemindType(type: VBandHealthRemindType): HealthRemindType {
        return when (type) {
            VBandHealthRemindType.ALL -> HealthRemindType.ALL
            VBandHealthRemindType.SEDENTARY -> HealthRemindType.SEDENTARY
            VBandHealthRemindType.DRINK_WATER -> HealthRemindType.DRINK_WATER
            VBandHealthRemindType.OVERLOOK -> HealthRemindType.OVERLOOK
            VBandHealthRemindType.SPORTS -> HealthRemindType.SPORTS
            VBandHealthRemindType.TAKE_MEDICINE -> HealthRemindType.TAKE_MEDICINE
            VBandHealthRemindType.READING -> HealthRemindType.READING
            VBandHealthRemindType.GOING_OUT -> HealthRemindType.GOING_OUT
            VBandHealthRemindType.WASH -> HealthRemindType.WASH
        }
    }

    private fun mapFromSdkHealthRemindType(type: HealthRemindType?): VBandHealthRemindType {
        return when (type) {
            HealthRemindType.ALL -> VBandHealthRemindType.ALL
            HealthRemindType.SEDENTARY -> VBandHealthRemindType.SEDENTARY
            HealthRemindType.DRINK_WATER -> VBandHealthRemindType.DRINK_WATER
            HealthRemindType.OVERLOOK -> VBandHealthRemindType.OVERLOOK
            HealthRemindType.SPORTS -> VBandHealthRemindType.SPORTS
            HealthRemindType.TAKE_MEDICINE -> VBandHealthRemindType.TAKE_MEDICINE
            HealthRemindType.READING -> VBandHealthRemindType.READING
            HealthRemindType.GOING_OUT -> VBandHealthRemindType.GOING_OUT
            HealthRemindType.WASH -> VBandHealthRemindType.WASH
            else -> VBandHealthRemindType.ALL
        }
    }

    private fun mapToSdkLanguage(language: VBandLanguage): ELanguage {
        return when (language) {
            VBandLanguage.CHINESE -> ELanguage.CHINA
            VBandLanguage.CHINESE_TRADITIONAL -> ELanguage.CHINA_TRADITIONAL
            VBandLanguage.ENGLISH -> ELanguage.ENGLISH
            VBandLanguage.JAPANESE -> ELanguage.JAPAN
            VBandLanguage.KOREAN -> ELanguage.KOREA
            VBandLanguage.GERMAN -> ELanguage.DEUTSCH
            VBandLanguage.RUSSIAN -> ELanguage.RUSSIA
            VBandLanguage.SPANISH -> ELanguage.SPANISH
            VBandLanguage.ITALIAN -> ELanguage.ITALIA
            VBandLanguage.FRENCH -> ELanguage.FRENCH
            VBandLanguage.VIETNAMESE -> ELanguage.VIETNAM
            VBandLanguage.PORTUGUESE -> ELanguage.PORTUGUESA
            VBandLanguage.THAI -> ELanguage.THAI
            VBandLanguage.POLISH -> ELanguage.POLISH
            VBandLanguage.SWEDISH -> ELanguage.SWEDISH
            VBandLanguage.TURKISH -> ELanguage.TURKISH
            VBandLanguage.DUTCH -> ELanguage.DUTCH
            VBandLanguage.CZECH -> ELanguage.CZECH
            VBandLanguage.ARABIC -> ELanguage.ARABIC
            VBandLanguage.HUNGARIAN -> ELanguage.HUNGARY
            VBandLanguage.GREEK -> ELanguage.GREEK
            VBandLanguage.ROMANIAN -> ELanguage.ROMANIAN
            VBandLanguage.SLOVAK -> ELanguage.SLOVAK
            VBandLanguage.INDONESIAN -> ELanguage.INDONESIAN
            VBandLanguage.BRAZILIAN_PORTUGUESE -> ELanguage.BRAZIL_PORTUGAL
            VBandLanguage.CROATIAN -> ELanguage.CROATIAN
            VBandLanguage.LITHUANIAN -> ELanguage.LITHUANIAN
            VBandLanguage.UKRAINIAN -> ELanguage.UKRAINE
            VBandLanguage.HINDI -> ELanguage.HINDI
            VBandLanguage.HEBREW -> ELanguage.HEBREW
            VBandLanguage.DANISH -> ELanguage.DANISH
            VBandLanguage.PERSIAN -> ELanguage.PERSIAN
            VBandLanguage.FINNISH -> ELanguage.FINNISH
            VBandLanguage.MALAY -> ELanguage.MALAY
            VBandLanguage.UNKNOWN -> ELanguage.UNKONW
        }
    }

    private fun mapFromSdkLanguage(language: ELanguage?): VBandLanguage {
        return when (language) {
            ELanguage.CHINA -> VBandLanguage.CHINESE
            ELanguage.CHINA_TRADITIONAL -> VBandLanguage.CHINESE_TRADITIONAL
            ELanguage.ENGLISH -> VBandLanguage.ENGLISH
            ELanguage.JAPAN -> VBandLanguage.JAPANESE
            ELanguage.KOREA -> VBandLanguage.KOREAN
            ELanguage.DEUTSCH -> VBandLanguage.GERMAN
            ELanguage.RUSSIA -> VBandLanguage.RUSSIAN
            ELanguage.SPANISH -> VBandLanguage.SPANISH
            ELanguage.ITALIA -> VBandLanguage.ITALIAN
            ELanguage.FRENCH -> VBandLanguage.FRENCH
            ELanguage.VIETNAM -> VBandLanguage.VIETNAMESE
            ELanguage.PORTUGUESA -> VBandLanguage.PORTUGUESE
            ELanguage.THAI -> VBandLanguage.THAI
            ELanguage.POLISH -> VBandLanguage.POLISH
            ELanguage.SWEDISH -> VBandLanguage.SWEDISH
            ELanguage.TURKISH -> VBandLanguage.TURKISH
            ELanguage.DUTCH -> VBandLanguage.DUTCH
            ELanguage.CZECH -> VBandLanguage.CZECH
            ELanguage.ARABIC -> VBandLanguage.ARABIC
            ELanguage.HUNGARY -> VBandLanguage.HUNGARIAN
            ELanguage.GREEK -> VBandLanguage.GREEK
            ELanguage.ROMANIAN -> VBandLanguage.ROMANIAN
            ELanguage.SLOVAK -> VBandLanguage.SLOVAK
            ELanguage.INDONESIAN -> VBandLanguage.INDONESIAN
            ELanguage.BRAZIL_PORTUGAL -> VBandLanguage.BRAZILIAN_PORTUGUESE
            ELanguage.CROATIAN -> VBandLanguage.CROATIAN
            ELanguage.LITHUANIAN -> VBandLanguage.LITHUANIAN
            ELanguage.UKRAINE -> VBandLanguage.UKRAINIAN
            ELanguage.HINDI -> VBandLanguage.HINDI
            ELanguage.HEBREW -> VBandLanguage.HEBREW
            ELanguage.DANISH -> VBandLanguage.DANISH
            ELanguage.PERSIAN -> VBandLanguage.PERSIAN
            ELanguage.FINNISH -> VBandLanguage.FINNISH
            ELanguage.MALAY -> VBandLanguage.MALAY
            else -> VBandLanguage.UNKNOWN
        }
    }

