package dev.infa.page3.SDK.bottle

import dev.infa.page3.SDK.bottle.data.*
import kotlinx.coroutines.flow.StateFlow

/**
 * Platform-agnostic expect class for Bottle BLE communication.
 * Android/iOS provide actual implementations with real BLE stack.
 */
expect class BottleSyncManager : IBottleSyncManager {

    // ─── State Flows ────────────────────────────────────────────────────────────

    override val devices: StateFlow<List<BottleDeviceInfo>>
    override val connectionState: StateFlow<String>
    override val batteryStatus: StateFlow<String?>
    override val firmwareVersion: StateFlow<String?>
    override val alarms: StateFlow<List<BottleAlarm>>
    override val waterIntakeTarget: StateFlow<Int?>
    override val drinkingRecordDays: StateFlow<Int?>
    override val drinkingRecords: StateFlow<List<DrinkingRecord>>
    override val currentDrink: StateFlow<DrinkingRecord?>
    override val funcSwitchSmartReminder: StateFlow<Boolean?>
    override val autoStandby: StateFlow<Int?>
    override val colorLight: StateFlow<ColorLightConfig?>
    override val doNotDisturb: StateFlow<DoNotDisturbConfig?>
    override val gradientOption: StateFlow<Int?>
    override val reminderLightColor: StateFlow<Int?>
    override val waterLevelMl: StateFlow<Int?>
    override val waterTemperature: StateFlow<Int?>
    override val lastCommandSuccess: StateFlow<Boolean?>
    override val logs: StateFlow<List<String>>

    // ─── BLE Connection ─────────────────────────────────────────────────────────

    override fun startScan()
    override fun stopScan()
    override fun connect(device: BottleDeviceInfo)
    override fun disconnect()

    // ─── Queries ────────────────────────────────────────────────────────────────

    override fun requestBatteryLevel()
    override fun requestFirmwareVersion()
    override fun requestAllAlarms()
    override fun requestDrinkingRecordDays()
    override fun requestDrinkingRecordData(dayIndex: Int)
    override fun requestWaterIntakeTarget()
    override fun requestFuncSwitch()
    override fun requestAutoStandby()
    override fun requestColorLight()
    override fun requestDoNotDisturb()
    override fun requestGradientOption()
    override fun requestReminderLight()

    // ─── Commands ───────────────────────────────────────────────────────────────

    override fun syncTime()
    override fun activateLight()
    override fun calibrateSensor()
    override fun setWaterIntakeTarget(targetMl: Int)
    override fun updateAlarm(alarm: BottleAlarm)
    override fun deleteAlarm(alarmId: Int)
    override fun confirmAcquisition(dayIndex: Int)
    override fun sendTotalDailyWaterIntake(totalMl: Int)
    override fun setFuncSwitch(smartReminderOn: Boolean)
    override fun setAutoStandby(option: Int)
    override fun setColorLight(on: Boolean, startColor: Int, endColor: Int)
    override fun setDoNotDisturb(on: Boolean, startH: Int, startM: Int, endH: Int, endM: Int)
    override fun setGradientOption(option: Int)
    override fun setReminderLight(colorIndex: Int)
    override fun factoryReset()

    // ─── Utility ────────────────────────────────────────────────────────────────

    override fun clearLogs()
}
