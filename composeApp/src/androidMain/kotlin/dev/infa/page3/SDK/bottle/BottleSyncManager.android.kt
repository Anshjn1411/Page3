package dev.infa.page3.SDK.bottle

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import dev.infa.page3.SDK.bottle.data.*
import dev.infa.page3.SDK.ui.utils.PlatformContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

actual class BottleSyncManager : IBottleSyncManager {

    companion object {
        private const val TAG = "BottleSyncMgr"

        // Protocol frame markers — SGUAI-T30 uses 0xFF 0x55
        private const val HEADER1: Byte = 0xFF.toByte()
        private const val HEADER2: Byte = 0x55
        private const val TAIL1: Byte = 0x0D.toByte()
        private const val TAIL2: Byte = 0x0A.toByte()

        // R/W
        private const val READ: Byte = 0x01
        private const val WRITE: Byte = 0x02

        // Command types (App→Device)
        private const val TYPE_BATTERY: Byte = 0x02
        private const val TYPE_ALARM: Byte = 0x03
        private const val TYPE_TIME_SYNC: Byte = 0x04
        private const val TYPE_LIGHT: Byte = 0x08
        private const val TYPE_FW_VERSION: Byte = 0x09
        private const val TYPE_CALIBRATE: Byte = 0x0A
        private const val TYPE_RECORD_DAYS: Byte = 0x0C
        private const val TYPE_RECORD_DATA: Byte = 0x0D
        private const val TYPE_WATER_TARGET: Byte = 0x0E
        private const val TYPE_ACQ_CONFIRM: Byte = 0x10
        private const val TYPE_CURRENT_DRINK: Byte = 0x11
        private const val TYPE_TOTAL_INTAKE: Byte = 0x12
        private const val TYPE_FUNC_SWITCH: Byte = 0x21
        private const val TYPE_AUTO_STANDBY: Byte = 0x27
        private const val TYPE_COLOR_LIGHT: Byte = 0x28
        private const val TYPE_DND: Byte = 0x29
        private const val TYPE_GRADIENT: Byte = 0x2A.toByte()
        private const val TYPE_REMINDER_LIGHT: Byte = 0x2B.toByte()
        private const val TYPE_FACTORY_RESET: Byte = 0xFC.toByte()
        private const val TYPE_REALTIME_SENSOR: Byte = 0xC0.toByte()
    }

    // ─── Android Context & BLE plumbing ─────────────────────────────────────────

    private val context: Context get() = PlatformContext.get() as Context

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }

    private var bluetoothGatt: BluetoothGatt? = null
    private var scanner: BluetoothLeScanner? = null
    private var commandId: Int = 0
    private var isConnecting = false
    private var connectedDeviceAddress: String? = null
    private var retryCount = 0
    private val MAX_RETRIES = 3
    private val handler = Handler(Looper.getMainLooper())

    // Command queue to avoid GATT busy
    private val commandQueue = ArrayDeque<ByteArray>()
    private var isWriting = false

    // Coroutine scope for background work
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // BLE UUIDs per documentation
    private val SERVICE_UUID = UUID.fromString("0000ff00-0000-1000-8000-00805f9b34fb")
    private val WRITE_CHAR_UUID = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb")
    private val NOTIFY_CHAR_UUID = UUID.fromString("0000ff02-0000-1000-8000-00805f9b34fb")
    private val CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    // Packet reassembly buffer
    private val rxBuffer = mutableListOf<Byte>()

    // ─── State Flows ────────────────────────────────────────────────────────────

    private val _devices = MutableStateFlow<List<BottleDeviceInfo>>(emptyList())
    actual override val devices: StateFlow<List<BottleDeviceInfo>> = _devices

    private val _connectionState = MutableStateFlow("DISCONNECTED")
    actual override val connectionState: StateFlow<String> = _connectionState

    private val _batteryStatus = MutableStateFlow<String?>(null)
    actual override val batteryStatus: StateFlow<String?> = _batteryStatus

    private val _firmwareVersion = MutableStateFlow<String?>(null)
    actual override val firmwareVersion: StateFlow<String?> = _firmwareVersion

    private val _alarms = MutableStateFlow<List<BottleAlarm>>(emptyList())
    actual override val alarms: StateFlow<List<BottleAlarm>> = _alarms

    private val _waterIntakeTarget = MutableStateFlow<Int?>(null)
    actual override val waterIntakeTarget: StateFlow<Int?> = _waterIntakeTarget

    private val _drinkingRecordDays = MutableStateFlow<Int?>(null)
    actual override val drinkingRecordDays: StateFlow<Int?> = _drinkingRecordDays

    private val _drinkingRecords = MutableStateFlow<List<DrinkingRecord>>(emptyList())
    actual override val drinkingRecords: StateFlow<List<DrinkingRecord>> = _drinkingRecords

    private val _currentDrink = MutableStateFlow<DrinkingRecord?>(null)
    actual override val currentDrink: StateFlow<DrinkingRecord?> = _currentDrink

    private val _funcSwitchSmartReminder = MutableStateFlow<Boolean?>(null)
    actual override val funcSwitchSmartReminder: StateFlow<Boolean?> = _funcSwitchSmartReminder

    private val _autoStandby = MutableStateFlow<Int?>(null)
    actual override val autoStandby: StateFlow<Int?> = _autoStandby

    private val _colorLight = MutableStateFlow<ColorLightConfig?>(null)
    actual override val colorLight: StateFlow<ColorLightConfig?> = _colorLight

    private val _doNotDisturb = MutableStateFlow<DoNotDisturbConfig?>(null)
    actual override val doNotDisturb: StateFlow<DoNotDisturbConfig?> = _doNotDisturb

    private val _gradientOption = MutableStateFlow<Int?>(null)
    actual override val gradientOption: StateFlow<Int?> = _gradientOption

    private val _reminderLightColor = MutableStateFlow<Int?>(null)
    actual override val reminderLightColor: StateFlow<Int?> = _reminderLightColor

    private val _waterLevelMl = MutableStateFlow<Int?>(null)
    actual override val waterLevelMl: StateFlow<Int?> = _waterLevelMl

    private val _waterTemperature = MutableStateFlow<Int?>(null)
    actual override val waterTemperature: StateFlow<Int?> = _waterTemperature

    private val _lastCommandSuccess = MutableStateFlow<Boolean?>(null)
    actual override val lastCommandSuccess: StateFlow<Boolean?> = _lastCommandSuccess

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    actual override val logs: StateFlow<List<String>> = _logs

    // ─── Logging ────────────────────────────────────────────────────────────────

    private fun addLog(message: String) {
        val ts = System.currentTimeMillis()
        val logMessage = "[${ts % 100000}] $message"
        Log.d(TAG, logMessage)
        scope.launch {
            val cur = _logs.value.toMutableList()
            cur.add(0, logMessage)
            if (cur.size > 100) cur.removeAt(cur.size - 1)
            _logs.value = cur
        }
    }

    // ─── BLE Scan ───────────────────────────────────────────────────────────────

    @SuppressLint("MissingPermission")
    actual override fun startScan() {
        addLog("=== STARTING SCAN ===")
        scanner = bluetoothAdapter.bluetoothLeScanner
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        _devices.value = emptyList()
        _connectionState.value = "SCANNING"
        scanner?.startScan(null, settings, scanCallback)
        addLog("Scanning for PAGE3-T30...")

        scope.launch {
            delay(15000)
            stopScan()
        }
    }

    @SuppressLint("MissingPermission")
    actual override fun stopScan() {
        try {
            scanner?.stopScan(scanCallback)
        } catch (_: Exception) {}
        if (_connectionState.value == "SCANNING") {
            _connectionState.value = "DISCONNECTED"
        }
        addLog("Scan stopped. Found ${_devices.value.size} device(s)")
    }

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val name = device.name ?: return
            if (!name.contains("PAGE3", true) && !name.contains("SGUAI", true) && !name.contains("T30", true)) return
            val list = _devices.value.toMutableList()
            if (!list.any { it.address == device.address }) {
                list.add(BottleDeviceInfo(name, device.address, result.rssi))
                _devices.value = list
                addLog("Device found: $name (${device.address})")
            }
        }

        override fun onScanFailed(errorCode: Int) {
            addLog("ERROR: Scan failed code=$errorCode")
            _connectionState.value = "DISCONNECTED"
        }
    }

    // ─── BLE Connect / Disconnect ───────────────────────────────────────────────

    @SuppressLint("MissingPermission")
    actual override fun connect(device: BottleDeviceInfo) {
        if (isConnecting) {
            addLog("Connection already in progress, ignoring")
            return
        }
        isConnecting = true
        connectedDeviceAddress = device.address
        retryCount = 0
        addLog("=== CONNECTING: ${device.name} (${device.address}) ===")

        stopScan()

        val bleDevice = bluetoothAdapter.getRemoteDevice(device.address)

        val oldGatt = bluetoothGatt
        bluetoothGatt = null
        if (oldGatt != null) {
            oldGatt.disconnect()
            oldGatt.close()
            addLog("Closed old GATT, waiting before reconnect...")
            handler.postDelayed({
                performConnect(bleDevice)
            }, 600)
        } else {
            performConnect(bleDevice)
        }
    }

    @SuppressLint("MissingPermission")
    private fun performConnect(device: BluetoothDevice) {
        addLog("Initiating GATT connect (attempt ${retryCount + 1})...")
        _connectionState.value = "CONNECTING"
        bluetoothGatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    @SuppressLint("MissingPermission")
    private fun retryConnect() {
        val address = connectedDeviceAddress ?: return
        retryCount++
        if (retryCount > MAX_RETRIES) {
            addLog("Max retries ($MAX_RETRIES) reached, giving up")
            isConnecting = false
            _connectionState.value = "DISCONNECTED"
            return
        }
        addLog("Retrying connection (attempt ${retryCount + 1}/$MAX_RETRIES)...")
        val oldGatt = bluetoothGatt
        bluetoothGatt = null
        oldGatt?.close()
        val delayMs = (retryCount * 1000).toLong()
        val device = bluetoothAdapter.getRemoteDevice(address)
        handler.postDelayed({
            performConnect(device)
        }, delayMs)
    }

    @SuppressLint("MissingPermission")
    actual override fun disconnect() {
        addLog("=== DISCONNECTING ===")
        isConnecting = false
        connectedDeviceAddress = null
        retryCount = MAX_RETRIES + 1
        commandQueue.clear()
        isWriting = false
        handler.removeCallbacksAndMessages(null)
        val gatt = bluetoothGatt
        bluetoothGatt = null
        gatt?.disconnect()
        handler.postDelayed({
            gatt?.close()
        }, 300)
        _connectionState.value = "DISCONNECTED"
        _batteryStatus.value = null
        _firmwareVersion.value = null
        rxBuffer.clear()
    }

    // ─── GATT Callback ──────────────────────────────────────────────────────────

    private val gattCallback = object : BluetoothGattCallback() {

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    isConnecting = false
                    retryCount = 0
                    addLog("✓ CONNECTED (status=$status)")
                    _connectionState.value = "CONNECTED"

                    gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
                    addLog("Requested CONNECTION_PRIORITY_HIGH")

                    gatt.requestMtu(247)
                    addLog("Requested MTU 247")

                    handler.postDelayed({
                        addLog("Starting service discovery...")
                        gatt.discoverServices()
                    }, 500)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    addLog("✗ DISCONNECTED (status=$status)")
                    _connectionState.value = "DISCONNECTED"
                    commandQueue.clear()
                    isWriting = false

                    if (status == 133 || status == 8 || status == 19) {
                        if (connectedDeviceAddress != null && retryCount < MAX_RETRIES) {
                            addLog("Recoverable error (status=$status), will retry...")
                            retryConnect()
                        } else if (retryCount >= MAX_RETRIES) {
                            addLog("Max retries reached after status=$status")
                            isConnecting = false
                        }
                    } else {
                        isConnecting = false
                    }
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            addLog("MTU changed to $mtu (status=$status)")
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                addLog("ERROR: Service discovery failed ($status)")
                return
            }
            addLog("✓ Services discovered")

            val service = gatt.getService(SERVICE_UUID)
            if (service == null) {
                addLog("ERROR: Data service 0xFF00 not found!")
                gatt.services?.forEach { svc ->
                    addLog("  Available service: ${svc.uuid}")
                }
                return
            }

            val writeChar = service.getCharacteristic(WRITE_CHAR_UUID)
            val notifyChar = service.getCharacteristic(NOTIFY_CHAR_UUID)
            addLog("Write char (FF01): ${writeChar != null}, Notify char (FF02): ${notifyChar != null}")

            if (notifyChar != null) {
                handler.postDelayed({
                    enableNotification(gatt, notifyChar)
                }, 500)
            } else {
                addLog("ERROR: Notify characteristic FF02 not found!")
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic
        ) {
            val data = characteristic.value ?: return
            addLog("<<< RX ${data.size} bytes: ${data.toHexString()}")
            onDataReceived(data)
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int
        ) {
            addLog(if (status == BluetoothGatt.GATT_SUCCESS) "✓ Write OK" else "✗ Write FAIL ($status)")
            isWriting = false
            processCommandQueue()
        }

        @SuppressLint("MissingPermission")
        override fun onDescriptorWrite(
            gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                addLog("✓ Notifications ENABLED — READY")
                handler.postDelayed({
                    addLog("Auto-syncing time to device...")
                    syncTime()
                }, 500)
            } else {
                addLog("✗ Descriptor write FAILED (status=$status)")
                if (status == 133 && connectedDeviceAddress != null && retryCount < MAX_RETRIES) {
                    addLog("Descriptor write failed with 133, retrying connection...")
                    retryConnect()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableNotification(gatt: BluetoothGatt, char: BluetoothGattCharacteristic) {
        val setResult = gatt.setCharacteristicNotification(char, true)
        addLog("setCharacteristicNotification = $setResult")
        val desc = char.getDescriptor(CCCD_UUID)
        if (desc != null) {
            desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            val writeResult = gatt.writeDescriptor(desc)
            addLog("Writing CCCD descriptor... result=$writeResult")
        } else {
            addLog("WARN: CCCD descriptor not found, notifications may still work")
            addLog("✓ Notifications ENABLED (no CCCD) — READY")
            handler.postDelayed({ syncTime() }, 500)
        }
    }

    // ─── Packet Reassembly ──────────────────────────────────────────────────────

    private fun onDataReceived(data: ByteArray) {
        synchronized(rxBuffer) {
            for (b in data) rxBuffer.add(b)
            extractPackets()
        }
    }

    private fun extractPackets() {
        while (rxBuffer.size >= 7) {
            val h1 = rxBuffer.indexOf(HEADER1)
            if (h1 < 0 || h1 + 1 >= rxBuffer.size) {
                rxBuffer.clear()
                return
            }
            if (rxBuffer[h1 + 1] != HEADER2) {
                for (i in 0..h1) rxBuffer.removeAt(0)
                continue
            }
            for (i in 0 until h1) rxBuffer.removeAt(0)

            var tailIdx = -1
            for (i in 4 until rxBuffer.size - 1) {
                if (rxBuffer[i] == TAIL1 && rxBuffer[i + 1] == TAIL2) {
                    tailIdx = i
                    break
                }
            }

            if (tailIdx >= 0) {
                val packetLen = tailIdx + 2
                if (rxBuffer.size >= packetLen) {
                    val packet = ByteArray(packetLen) { rxBuffer[it] }
                    for (i in 0 until packetLen) rxBuffer.removeAt(0)
                    parseDeviceResponse(packet)
                } else {
                    return
                }
            } else {
                if (rxBuffer.size > 200) {
                    addLog("WARN: Buffer overflow, flushing")
                    rxBuffer.clear()
                }
                return
            }
        }
    }

    // ─── Response Parsing ───────────────────────────────────────────────────────

    private fun parseDeviceResponse(packet: ByteArray) {
        addLog("=== PARSE PACKET (${packet.size} bytes) ===")

        if (packet.size < 7) {
            addLog("Packet too short")
            return
        }

        // Success/Error response (exactly 7 bytes)
        if (packet.size == 7) {
            val successOrError = packet[2].toInt() and 0xFF
            val cmdType = packet[3].toInt() and 0xFF
            val cmdIdVal = packet[4].toInt() and 0xFF
            if (successOrError == 0) {
                addLog("✓ SUCCESS for type=0x${cmdType.toHex()}, id=$cmdIdVal")
                _lastCommandSuccess.value = true
            } else {
                addLog("✗ ERROR for type=0x${cmdType.toHex()}, id=$cmdIdVal")
                _lastCommandSuccess.value = false
            }
            return
        }

        // Device→App data response
        val cmdType = packet[2].toInt() and 0xFF
        val cmdIdVal = packet[3].toInt() and 0xFF
        val dataType = packet[4].toInt() and 0xFF

        addLog("CmdType=0x${cmdType.toHex()}, CmdID=$cmdIdVal, DataType=$dataType")

        val dataStart = 5
        val dataEnd = packet.size - 2
        val dataBytes = if (dataEnd > dataStart) packet.sliceArray(dataStart until dataEnd) else byteArrayOf()

        when (cmdType) {
            0x02 -> parseBatteryResponse(dataBytes)
            0x03 -> parseAlarmResponse(dataBytes, dataType)
            0x09 -> parseFirmwareVersion(dataBytes, dataType)
            0x0C -> parseDrinkingRecordDays(dataBytes, dataType)
            0x0D -> parseDrinkingRecordData(dataBytes, dataType)
            0x0E -> parseWaterIntakeTarget(dataBytes, dataType)
            0x11 -> parseCurrentDrinkNotification(dataBytes, dataType)
            0x21 -> parseFuncSwitch(dataBytes, dataType)
            0x27 -> parseAutoStandby(dataBytes, dataType)
            0x28 -> parseColorLight(dataBytes, dataType)
            0x29 -> parseDoNotDisturb(dataBytes, dataType)
            0x2A -> parseGradientOption(dataBytes, dataType)
            0x2B -> parseReminderLight(dataBytes, dataType)
            0xC0 -> parseRealtimeSensor(dataBytes, dataType)
            else -> addLog("Unknown response type 0x${cmdType.toHex()}")
        }
    }

    // ─── Individual Parsers ─────────────────────────────────────────────────────

    private fun parseBatteryResponse(data: ByteArray) {
        if (data.isEmpty()) { addLog("Battery: no data"); return }
        val code = data[0].toInt() and 0xFF
        val text = when (code) {
            0x00 -> "<10%"
            0x01 -> "10-20%"
            0x02 -> "20-40%"
            0x03 -> "40-60%"
            0x04 -> "60-80%"
            0x05 -> "80-100%"
            0x10 -> "Charging"
            0x11 -> "Fully Charged"
            else -> "Unknown (0x${code.toHex()})"
        }
        addLog("✓ Battery: $text")
        _batteryStatus.value = text
    }

    private fun parseAlarmResponse(data: ByteArray, dataType: Int) {
        if (data.isEmpty()) { addLog("Alarm: no data"); return }
        val count = data[0].toInt() and 0xFF
        addLog("Alarm count: $count")
        val alarmList = mutableListOf<BottleAlarm>()
        var offset = 1
        for (i in 0 until count) {
            if (offset + 5 >= data.size + 1) break
            val id = data[offset].toInt() and 0xFF
            val on = (data[offset + 1].toInt() and 0xFF) == 1
            val hour = data[offset + 2].toInt() and 0xFF
            val minute = data[offset + 3].toInt() and 0xFF
            val repeat = data[offset + 4].toInt() and 0xFF
            val water = data[offset + 5].toInt() and 0xFF
            alarmList.add(BottleAlarm(id, on, hour, minute, repeat, water))
            addLog("  Alarm $id: ${if (on) "ON" else "OFF"} $hour:${minute.toString().padStart(2, '0')}")
            offset += 6
        }
        _alarms.value = alarmList
    }

    private fun parseFirmwareVersion(data: ByteArray, dataType: Int) {
        if (data.size < 2) { addLog("FW: data too short"); return }
        val major = data[0].toInt() and 0xFF
        val minor = data[1].toInt() and 0xFF
        val ver = "V$major.$minor"
        addLog("✓ Firmware: $ver")
        _firmwareVersion.value = ver
    }

    private fun parseDrinkingRecordDays(data: ByteArray, dataType: Int) {
        if (data.isEmpty()) return
        val days = data[0].toInt() and 0xFF
        addLog("✓ Drinking record days: $days")
        _drinkingRecordDays.value = days
    }

    private fun parseDrinkingRecordData(data: ByteArray, dataType: Int) {
        if (data.size < 3) { addLog("Record data too short"); return }
        val dayIndex = data[0].toInt() and 0xFF
        val totalRecords = ((data[1].toInt() and 0xFF) shl 8) or (data[2].toInt() and 0xFF)
        addLog("Day $dayIndex, total records: $totalRecords")

        val records = mutableListOf<DrinkingRecord>()
        var offset = 3
        for (i in 0 until totalRecords) {
            if (offset + 6 >= data.size + 1) break
            val ts = ((data[offset].toLong() and 0xFF) shl 24) or
                    ((data[offset + 1].toLong() and 0xFF) shl 16) or
                    ((data[offset + 2].toLong() and 0xFF) shl 8) or
                    (data[offset + 3].toLong() and 0xFF)
            val waterMl = ((data[offset + 4].toInt() and 0xFF) shl 8) or
                    (data[offset + 5].toInt() and 0xFF)
            val temp = data[offset + 6].toInt() and 0xFF
            records.add(DrinkingRecord(ts, waterMl, temp))
            addLog("  Record: ${waterMl}mL, ${temp}°C")
            offset += 7
        }
        _drinkingRecords.value = records
    }

    private fun parseWaterIntakeTarget(data: ByteArray, dataType: Int) {
        if (data.size < 2) return
        val target = ((data[0].toInt() and 0xFF) shl 8) or (data[1].toInt() and 0xFF)
        addLog("✓ Water target: ${target}mL")
        _waterIntakeTarget.value = target
    }

    private fun parseCurrentDrinkNotification(data: ByteArray, dataType: Int) {
        if (data.size < 8) { addLog("Current drink data too short"); return }
        val ts = ((data[1].toLong() and 0xFF) shl 24) or
                ((data[2].toLong() and 0xFF) shl 16) or
                ((data[3].toLong() and 0xFF) shl 8) or
                (data[4].toLong() and 0xFF)
        val waterMl = ((data[5].toInt() and 0xFF) shl 8) or (data[6].toInt() and 0xFF)
        val temp = data[7].toInt() and 0xFF
        val record = DrinkingRecord(ts, waterMl, temp)
        addLog("✓ Current drink: ${waterMl}mL, ${temp}°C")
        _currentDrink.value = record
    }

    private fun parseFuncSwitch(data: ByteArray, dataType: Int) {
        if (data.size < 2) return
        val on = (data[1].toInt() and 0xFF) == 1
        addLog("✓ Smart Reminder: ${if (on) "ON" else "OFF"}")
        _funcSwitchSmartReminder.value = on
    }

    private fun parseAutoStandby(data: ByteArray, dataType: Int) {
        if (data.isEmpty()) return
        val option = data[0].toInt() and 0xFF
        val text = when (option) {
            0 -> "5s"; 1 -> "10s"; 2 -> "15s"; 3 -> "Always On"; else -> "Unknown"
        }
        addLog("✓ Auto Standby: $text")
        _autoStandby.value = option
    }

    private fun parseColorLight(data: ByteArray, dataType: Int) {
        if (data.size < 5) return
        val on = (data[0].toInt() and 0xFF) == 1
        val start = ((data[1].toInt() and 0xFF) shl 8) or (data[2].toInt() and 0xFF)
        val end = ((data[3].toInt() and 0xFF) shl 8) or (data[4].toInt() and 0xFF)
        addLog("✓ Color Light: ${if (on) "ON" else "OFF"}, $start→$end")
        _colorLight.value = ColorLightConfig(on, start, end)
    }

    private fun parseDoNotDisturb(data: ByteArray, dataType: Int) {
        if (data.size < 5) return
        val on = (data[0].toInt() and 0xFF) == 1
        val sh = data[1].toInt() and 0xFF
        val sm = data[2].toInt() and 0xFF
        val eh = data[3].toInt() and 0xFF
        val em = data[4].toInt() and 0xFF
        addLog("✓ DND: ${if (on) "ON" else "OFF"}, $sh:${sm.toString().padStart(2, '0')}-$eh:${em.toString().padStart(2, '0')}")
        _doNotDisturb.value = DoNotDisturbConfig(on, sh, sm, eh, em)
    }

    private fun parseGradientOption(data: ByteArray, dataType: Int) {
        if (data.isEmpty()) return
        val option = data[0].toInt() and 0xFF
        val text = when (option) {
            0 -> "Interval CW"; 1 -> "Interval CCW"; 2 -> "Two-Color"; else -> "Unknown"
        }
        addLog("✓ Gradient: $text")
        _gradientOption.value = option
    }

    private fun parseReminderLight(data: ByteArray, dataType: Int) {
        if (data.size < 2) return
        val color = ((data[0].toInt() and 0xFF) shl 8) or (data[1].toInt() and 0xFF)
        addLog("✓ Reminder Light Color: $color")
        _reminderLightColor.value = color
    }

    private var lastSensorLogTime = 0L

    private fun parseRealtimeSensor(data: ByteArray, dataType: Int) {
        if (data.size < 6) {
            addLog("Sensor data: ${data.map { "0x%02X".format(it) }.joinToString(" ")}")
            return
        }

        val waterRaw = ((data[0].toLong() and 0xFF) shl 24) or
                ((data[1].toLong() and 0xFF) shl 16) or
                ((data[2].toLong() and 0xFF) shl 8) or
                (data[3].toLong() and 0xFF)

        val waterMl = ((data[4].toInt() and 0xFF) shl 8) or (data[5].toInt() and 0xFF)
        val temp = if (data.size > 5) data[data.size - 1].toInt() and 0xFF else 0

        _waterLevelMl.value = waterRaw.toInt()
        _waterTemperature.value = temp

        val now = System.currentTimeMillis()
        if (now - lastSensorLogTime > 5000) {
            addLog("📊 Sensor: water=$waterRaw, intake=${waterMl}mL, temp=${temp}°C")
            lastSensorLogTime = now
        }
    }

    // ─── Command Builders (App→Device) ──────────────────────────────────────────

    private fun nextCmdId(): Byte {
        commandId++
        if (commandId > 0xFF) commandId = 1
        return commandId.toByte()
    }

    @SuppressLint("MissingPermission")
    private fun writeCommand(bytes: ByteArray) {
        if (bluetoothGatt == null || _connectionState.value != "CONNECTED") {
            addLog("ERROR: Cannot write — not connected")
            return
        }
        commandQueue.addLast(bytes)
        processCommandQueue()
    }

    @SuppressLint("MissingPermission")
    private fun processCommandQueue() {
        if (isWriting || commandQueue.isEmpty()) return
        val gatt = bluetoothGatt ?: return
        val service = gatt.getService(SERVICE_UUID) ?: run {
            addLog("ERROR: Service lost")
            return
        }
        val writeChar = service.getCharacteristic(WRITE_CHAR_UUID) ?: run {
            addLog("ERROR: Write characteristic lost")
            return
        }

        val bytes = commandQueue.removeFirst()
        isWriting = true
        addLog(">>> TX: ${bytes.toHexString()}")
        writeChar.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        writeChar.value = bytes
        val success = gatt.writeCharacteristic(writeChar)
        if (!success) {
            addLog("✗ Write initiation failed")
            isWriting = false
            handler.postDelayed({ processCommandQueue() }, 200)
        }
    }

    private fun buildReadCmd(type: Byte, data: Byte = 0x00): ByteArray {
        return byteArrayOf(HEADER1, HEADER2, 0x07, nextCmdId(), READ, type, data)
    }

    // ─── Public Command Methods ─────────────────────────────────────────────────

    actual override fun requestBatteryLevel() {
        addLog(">>> CMD: Get Battery")
        writeCommand(buildReadCmd(TYPE_BATTERY))
    }

    actual override fun requestFirmwareVersion() {
        addLog(">>> CMD: Get FW Version")
        writeCommand(buildReadCmd(TYPE_FW_VERSION, 0x01))
    }

    actual override fun requestAllAlarms() {
        addLog(">>> CMD: Get Alarms")
        writeCommand(buildReadCmd(TYPE_ALARM))
    }

    actual override fun requestDrinkingRecordDays() {
        addLog(">>> CMD: Get Record Days")
        writeCommand(buildReadCmd(TYPE_RECORD_DAYS))
    }

    actual override fun requestDrinkingRecordData(dayIndex: Int) {
        addLog(">>> CMD: Get Records Day=$dayIndex")
        writeCommand(buildReadCmd(TYPE_RECORD_DATA, dayIndex.toByte()))
    }

    actual override fun requestWaterIntakeTarget() {
        addLog(">>> CMD: Get Water Target")
        writeCommand(buildReadCmd(TYPE_WATER_TARGET))
    }

    actual override fun requestFuncSwitch() {
        addLog(">>> CMD: Get Func Switch")
        writeCommand(buildReadCmd(TYPE_FUNC_SWITCH))
    }

    actual override fun requestAutoStandby() {
        addLog(">>> CMD: Get Auto Standby")
        writeCommand(buildReadCmd(TYPE_AUTO_STANDBY))
    }

    actual override fun requestColorLight() {
        addLog(">>> CMD: Get Color Light")
        writeCommand(buildReadCmd(TYPE_COLOR_LIGHT))
    }

    actual override fun requestDoNotDisturb() {
        addLog(">>> CMD: Get DND")
        writeCommand(buildReadCmd(TYPE_DND))
    }

    actual override fun requestGradientOption() {
        addLog(">>> CMD: Get Gradient")
        writeCommand(buildReadCmd(TYPE_GRADIENT))
    }

    actual override fun requestReminderLight() {
        addLog(">>> CMD: Get Reminder Light")
        writeCommand(buildReadCmd(TYPE_REMINDER_LIGHT))
    }

    actual override fun syncTime() {
        addLog(">>> CMD: Sync Time")
        val id = nextCmdId()
        val timestamp = System.currentTimeMillis() / 1000
        val tz = TimeZone.getDefault()
        val offsetMs = tz.getOffset(System.currentTimeMillis())
        val offsetHours = offsetMs / 3600000
        val gmtValue = offsetHours * 100
        val gmtSign: Byte = if (offsetHours >= 0) 0x00 else 0x01

        val cmd = byteArrayOf(
            HEADER1, HEADER2,
            0x0D,
            id, WRITE, TYPE_TIME_SYNC,
            (timestamp shr 24).toByte(),
            (timestamp shr 16).toByte(),
            (timestamp shr 8).toByte(),
            timestamp.toByte(),
            gmtSign,
            (Math.abs(gmtValue) shr 8).toByte(),
            Math.abs(gmtValue).toByte()
        )
        writeCommand(cmd)
    }

    actual override fun activateLight() {
        addLog(">>> CMD: Activate Light")
        writeCommand(byteArrayOf(HEADER1, HEADER2, 0x07, nextCmdId(), WRITE, TYPE_LIGHT, 0x00))
    }

    actual override fun calibrateSensor() {
        addLog(">>> CMD: Calibrate Sensor")
        writeCommand(byteArrayOf(HEADER1, HEADER2, 0x07, nextCmdId(), WRITE, TYPE_CALIBRATE, 0x00))
    }

    actual override fun setWaterIntakeTarget(targetMl: Int) {
        addLog(">>> CMD: Set Water Target ${targetMl}mL")
        val id = nextCmdId()
        val cmd = byteArrayOf(
            HEADER1, HEADER2,
            0x08,
            id, WRITE, TYPE_WATER_TARGET,
            (targetMl shr 8).toByte(),
            targetMl.toByte()
        )
        writeCommand(cmd)
    }

    actual override fun updateAlarm(alarm: BottleAlarm) {
        addLog(">>> CMD: Update Alarm ${alarm.id}")
        val id = nextCmdId()
        val action: Byte = 0x02  // Add/Update
        val onOff: Byte = if (alarm.isOn) 0x01 else 0x00
        val cmd = byteArrayOf(
            HEADER1, HEADER2,
            0x0D,
            id, WRITE, TYPE_ALARM,
            action,
            alarm.id.toByte(),
            onOff,
            alarm.hour.toByte(),
            alarm.minute.toByte(),
            alarm.repeat.toByte(),
            alarm.water.toByte()
        )
        writeCommand(cmd)
    }

    actual override fun deleteAlarm(alarmId: Int) {
        addLog(">>> CMD: Delete Alarm $alarmId")
        val id = nextCmdId()
        val action: Byte = 0x03  // Delete
        val cmd = byteArrayOf(
            HEADER1, HEADER2,
            0x08,
            id, WRITE, TYPE_ALARM,
            action,
            alarmId.toByte()
        )
        writeCommand(cmd)
    }

    actual override fun confirmAcquisition(dayIndex: Int) {
        addLog(">>> CMD: Confirm Acquisition Day=$dayIndex")
        writeCommand(byteArrayOf(
            HEADER1, HEADER2,
            0x07, nextCmdId(), WRITE, TYPE_ACQ_CONFIRM,
            dayIndex.toByte()
        ))
    }

    actual override fun sendTotalDailyWaterIntake(totalMl: Int) {
        addLog(">>> CMD: Send Total Intake ${totalMl}mL")
        val id = nextCmdId()
        val cmd = byteArrayOf(
            HEADER1, HEADER2,
            0x08,
            id, WRITE, TYPE_TOTAL_INTAKE,
            (totalMl shr 8).toByte(),
            totalMl.toByte()
        )
        writeCommand(cmd)
    }

    actual override fun setFuncSwitch(smartReminderOn: Boolean) {
        addLog(">>> CMD: Set Smart Reminder ${if (smartReminderOn) "ON" else "OFF"}")
        val id = nextCmdId()
        val cmd = byteArrayOf(
            HEADER1, HEADER2,
            0x08,
            id, WRITE, TYPE_FUNC_SWITCH,
            0x03,  // UI-type: Smart Reminder
            if (smartReminderOn) 0x01 else 0x00
        )
        writeCommand(cmd)
    }

    actual override fun setAutoStandby(option: Int) {
        addLog(">>> CMD: Set Auto Standby $option")
        writeCommand(byteArrayOf(
            HEADER1, HEADER2, 0x07, nextCmdId(), WRITE, TYPE_AUTO_STANDBY, option.toByte()
        ))
    }

    actual override fun setColorLight(on: Boolean, startColor: Int, endColor: Int) {
        addLog(">>> CMD: Set Color Light on=$on, $startColor→$endColor")
        val id = nextCmdId()
        val cmd = byteArrayOf(
            HEADER1, HEADER2,
            0x0B,
            id, WRITE, TYPE_COLOR_LIGHT,
            if (on) 0x01 else 0x00,
            (startColor shr 8).toByte(), startColor.toByte(),
            (endColor shr 8).toByte(), endColor.toByte()
        )
        writeCommand(cmd)
    }

    actual override fun setDoNotDisturb(on: Boolean, startH: Int, startM: Int, endH: Int, endM: Int) {
        addLog(">>> CMD: Set DND on=$on, $startH:$startM-$endH:$endM")
        val id = nextCmdId()
        val cmd = byteArrayOf(
            HEADER1, HEADER2,
            0x0B,
            id, WRITE, TYPE_DND,
            if (on) 0x01 else 0x00,
            startH.toByte(), startM.toByte(),
            endH.toByte(), endM.toByte()
        )
        writeCommand(cmd)
    }

    actual override fun setGradientOption(option: Int) {
        addLog(">>> CMD: Set Gradient $option")
        writeCommand(byteArrayOf(
            HEADER1, HEADER2, 0x07, nextCmdId(), WRITE, TYPE_GRADIENT, option.toByte()
        ))
    }

    actual override fun setReminderLight(colorIndex: Int) {
        addLog(">>> CMD: Set Reminder Light $colorIndex")
        val id = nextCmdId()
        val cmd = byteArrayOf(
            HEADER1, HEADER2,
            0x08,
            id, WRITE, TYPE_REMINDER_LIGHT,
            (colorIndex shr 8).toByte(), colorIndex.toByte()
        )
        writeCommand(cmd)
    }

    actual override fun factoryReset() {
        addLog(">>> CMD: FACTORY RESET")
        writeCommand(buildReadCmd(TYPE_FACTORY_RESET))
    }

    // ─── Utility ────────────────────────────────────────────────────────────────

    actual override fun clearLogs() {
        _logs.value = emptyList()
        addLog("Logs cleared")
    }

    private fun ByteArray.toHexString(): String = joinToString(" ") { "0x%02X".format(it) }
    private fun Int.toHex(): String = "%02X".format(this)
}
