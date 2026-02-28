package dev.infa.page3.SDK.bottle

import dev.infa.page3.SDK.bottle.data.*
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface defining all SGUAI-T30 Smart Bottle operations.
 * Platform implementations (Android/iOS) provide BLE communication.
 */
interface IBottleSyncManager {

    // ─── State Flows (Observable state) ─────────────────────────────────────────

    val devices: StateFlow<List<BottleDeviceInfo>>
    val connectionState: StateFlow<String>
    val batteryStatus: StateFlow<String?>
    val firmwareVersion: StateFlow<String?>
    val alarms: StateFlow<List<BottleAlarm>>
    val waterIntakeTarget: StateFlow<Int?>
    val drinkingRecordDays: StateFlow<Int?>
    val drinkingRecords: StateFlow<List<DrinkingRecord>>
    val currentDrink: StateFlow<DrinkingRecord?>
    val funcSwitchSmartReminder: StateFlow<Boolean?>
    val autoStandby: StateFlow<Int?>          // 0=5s, 1=10s, 2=15s, 3=Always On
    val colorLight: StateFlow<ColorLightConfig?>
    val doNotDisturb: StateFlow<DoNotDisturbConfig?>
    val gradientOption: StateFlow<Int?>       // 0=CW, 1=CCW, 2=Two-Color
    val reminderLightColor: StateFlow<Int?>   // 0~359
    val waterLevelMl: StateFlow<Int?>
    val waterTemperature: StateFlow<Int?>
    val lastCommandSuccess: StateFlow<Boolean?>
    val logs: StateFlow<List<String>>

    // ─── BLE Connection ─────────────────────────────────────────────────────────

    fun startScan()
    fun stopScan()
    fun connect(device: BottleDeviceInfo)
    fun disconnect()

    // ─── Queries (Read from bottle) ─────────────────────────────────────────────

    /** Type 0x02 — Get battery level & charging status */
    fun requestBatteryLevel()

    /** Type 0x09 — Get firmware version */
    fun requestFirmwareVersion()

    /** Type 0x03 — Get all 8 alarms */
    fun requestAllAlarms()

    /** Type 0x0C — Query how many days of records exist */
    fun requestDrinkingRecordDays()

    /** Type 0x0D — Query drinking records for a specific day (0=today, 1-7) */
    fun requestDrinkingRecordData(dayIndex: Int = 0)

    /** Type 0x0E — Get water intake target */
    fun requestWaterIntakeTarget()

    /** Type 0x21 — Get func switch state */
    fun requestFuncSwitch()

    /** Type 0x27 — Get auto standby timer */
    fun requestAutoStandby()

    /** Type 0x28 — Get color light configuration */
    fun requestColorLight()

    /** Type 0x29 — Get Do Not Disturb period */
    fun requestDoNotDisturb()

    /** Type 0x2A — Get gradient option */
    fun requestGradientOption()

    /** Type 0x2B — Get reminder light color */
    fun requestReminderLight()

    // ─── Commands (Write to bottle) ─────────────────────────────────────────────

    /** Type 0x04 — Sync phone time to device */
    fun syncTime()

    /** Type 0x08 — Activate light */
    fun activateLight()

    /** Type 0x0A — Calibrate sensor */
    fun calibrateSensor()

    /** Type 0x0E — Set water intake target (mL) */
    fun setWaterIntakeTarget(targetMl: Int)

    /** Type 0x03 — Update/add a single alarm */
    fun updateAlarm(alarm: BottleAlarm)

    /** Type 0x03 — Delete a single alarm by ID */
    fun deleteAlarm(alarmId: Int)

    /** Type 0x10 — Confirm acquisition of historical data */
    fun confirmAcquisition(dayIndex: Int = 0)

    /** Type 0x12 — Send total daily water intake to device */
    fun sendTotalDailyWaterIntake(totalMl: Int)

    /** Type 0x21 — Set smart reminder on/off */
    fun setFuncSwitch(smartReminderOn: Boolean)

    /** Type 0x27 — Set auto standby: 0=5s, 1=10s, 2=15s, 3=Always On */
    fun setAutoStandby(option: Int)

    /** Type 0x28 — Set color light */
    fun setColorLight(on: Boolean, startColor: Int, endColor: Int)

    /** Type 0x29 — Set Do Not Disturb */
    fun setDoNotDisturb(on: Boolean, startH: Int, startM: Int, endH: Int, endM: Int)

    /** Type 0x2A — Set gradient: 0=CW, 1=CCW, 2=Two-Color */
    fun setGradientOption(option: Int)

    /** Type 0x2B — Set reminder light color (0-359) */
    fun setReminderLight(colorIndex: Int)

    /** Type 0xFC — Factory reset */
    fun factoryReset()

    // ─── Utility ────────────────────────────────────────────────────────────────

    fun clearLogs()
}
