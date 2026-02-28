package dev.infa.page3.SDK.bottle

import dev.infa.page3.SDK.bottle.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * iOS stub for BottleSyncManager.
 * TODO: Implement CoreBluetooth-based BLE communication for iOS.
 */
actual class BottleSyncManager : IBottleSyncManager {

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

    actual override fun startScan() { TODO("iOS Bottle BLE not yet implemented") }
    actual override fun stopScan() { TODO("iOS Bottle BLE not yet implemented") }
    actual override fun connect(device: BottleDeviceInfo) { TODO("iOS Bottle BLE not yet implemented") }
    actual override fun disconnect() { TODO("iOS Bottle BLE not yet implemented") }
    actual override fun requestBatteryLevel() { TODO("iOS Bottle BLE not yet implemented") }
    actual override fun requestFirmwareVersion() { TODO("iOS Bottle BLE not yet implemented") }
    actual override fun requestAllAlarms() { TODO("iOS Bottle BLE not yet implemented") }
    actual override fun requestDrinkingRecordDays() { TODO("iOS Bottle BLE not yet implemented") }
    actual override fun requestDrinkingRecordData(dayIndex: Int) { TODO("iOS Bottle BLE not yet implemented") }
    actual override fun requestWaterIntakeTarget() { TODO("iOS Bottle BLE not yet implemented") }
    actual override fun requestFuncSwitch() { TODO("iOS Bottle BLE not yet implemented") }
    actual override fun requestAutoStandby() { TODO("iOS Bottle BLE not yet implemented") }
    actual override fun requestColorLight() { TODO("iOS Bottle BLE not yet implemented") }
    actual override fun requestDoNotDisturb() { TODO("iOS Bottle BLE not yet implemented") }
    actual override fun requestGradientOption() { TODO("iOS Bottle BLE not yet implemented") }
    actual override fun requestReminderLight() { TODO("iOS Bottle BLE not yet implemented") }
    actual override fun syncTime() { TODO("iOS Bottle BLE not yet implemented") }
    actual override fun activateLight() { TODO("iOS Bottle BLE not yet implemented") }
    actual override fun calibrateSensor() { TODO("iOS Bottle BLE not yet implemented") }
    actual override fun setWaterIntakeTarget(targetMl: Int) { TODO("iOS Bottle BLE not yet implemented") }
    actual override fun updateAlarm(alarm: BottleAlarm) { TODO("iOS Bottle BLE not yet implemented") }
    actual override fun deleteAlarm(alarmId: Int) { TODO("iOS Bottle BLE not yet implemented") }
    actual override fun confirmAcquisition(dayIndex: Int) { TODO("iOS Bottle BLE not yet implemented") }
    actual override fun sendTotalDailyWaterIntake(totalMl: Int) { TODO("iOS Bottle BLE not yet implemented") }
    actual override fun setFuncSwitch(smartReminderOn: Boolean) { TODO("iOS Bottle BLE not yet implemented") }
    actual override fun setAutoStandby(option: Int) { TODO("iOS Bottle BLE not yet implemented") }
    actual override fun setColorLight(on: Boolean, startColor: Int, endColor: Int) { TODO("iOS Bottle BLE not yet implemented") }
    actual override fun setDoNotDisturb(on: Boolean, startH: Int, startM: Int, endH: Int, endM: Int) { TODO("iOS Bottle BLE not yet implemented") }
    actual override fun setGradientOption(option: Int) { TODO("iOS Bottle BLE not yet implemented") }
    actual override fun setReminderLight(colorIndex: Int) { TODO("iOS Bottle BLE not yet implemented") }
    actual override fun factoryReset() { TODO("iOS Bottle BLE not yet implemented") }
    actual override fun clearLogs() { _logs.value = emptyList() }
}
