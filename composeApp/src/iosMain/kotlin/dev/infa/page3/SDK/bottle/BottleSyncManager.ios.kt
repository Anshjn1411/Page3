package dev.infa.page3.SDK.bottle

import dev.infa.page3.SDK.bottle.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * iOS stub for BottleSyncManager.
 * SDK disabled – all methods are no-ops returning defaults.
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

    actual override fun startScan() { /* no-op: SDK disabled */ }
    actual override fun stopScan() { /* no-op: SDK disabled */ }
    actual override fun connect(device: BottleDeviceInfo) { /* no-op: SDK disabled */ }
    actual override fun disconnect() { /* no-op: SDK disabled */ }
    actual override fun requestBatteryLevel() { /* no-op: SDK disabled */ }
    actual override fun requestFirmwareVersion() { /* no-op: SDK disabled */ }
    actual override fun requestAllAlarms() { /* no-op: SDK disabled */ }
    actual override fun requestDrinkingRecordDays() { /* no-op: SDK disabled */ }
    actual override fun requestDrinkingRecordData(dayIndex: Int) { /* no-op: SDK disabled */ }
    actual override fun requestWaterIntakeTarget() { /* no-op: SDK disabled */ }
    actual override fun requestFuncSwitch() { /* no-op: SDK disabled */ }
    actual override fun requestAutoStandby() { /* no-op: SDK disabled */ }
    actual override fun requestColorLight() { /* no-op: SDK disabled */ }
    actual override fun requestDoNotDisturb() { /* no-op: SDK disabled */ }
    actual override fun requestGradientOption() { /* no-op: SDK disabled */ }
    actual override fun requestReminderLight() { /* no-op: SDK disabled */ }
    actual override fun syncTime() { /* no-op: SDK disabled */ }
    actual override fun activateLight() { /* no-op: SDK disabled */ }
    actual override fun calibrateSensor() { /* no-op: SDK disabled */ }
    actual override fun setWaterIntakeTarget(targetMl: Int) { /* no-op: SDK disabled */ }
    actual override fun updateAlarm(alarm: BottleAlarm) { /* no-op: SDK disabled */ }
    actual override fun deleteAlarm(alarmId: Int) { /* no-op: SDK disabled */ }
    actual override fun confirmAcquisition(dayIndex: Int) { /* no-op: SDK disabled */ }
    actual override fun sendTotalDailyWaterIntake(totalMl: Int) { /* no-op: SDK disabled */ }
    actual override fun setFuncSwitch(smartReminderOn: Boolean) { /* no-op: SDK disabled */ }
    actual override fun setAutoStandby(option: Int) { /* no-op: SDK disabled */ }
    actual override fun setColorLight(on: Boolean, startColor: Int, endColor: Int) { /* no-op: SDK disabled */ }
    actual override fun setDoNotDisturb(on: Boolean, startH: Int, startM: Int, endH: Int, endM: Int) { /* no-op: SDK disabled */ }
    actual override fun setGradientOption(option: Int) { /* no-op: SDK disabled */ }
    actual override fun setReminderLight(colorIndex: Int) { /* no-op: SDK disabled */ }
    actual override fun factoryReset() { /* no-op: SDK disabled */ }
    actual override fun clearLogs() { _logs.value = emptyList() }
}
