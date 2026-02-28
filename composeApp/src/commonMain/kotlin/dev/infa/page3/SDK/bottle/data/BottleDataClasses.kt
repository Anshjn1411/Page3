package dev.infa.page3.SDK.bottle.data

import kotlinx.serialization.Serializable

// ─── Alarm ──────────────────────────────────────────────────────────────────────

@Serializable
data class BottleAlarm(
    val id: Int,
    val isOn: Boolean,
    val hour: Int,
    val minute: Int,
    val repeat: Int,    // bitmask: bit7=Mon, bit6=Tue … bit1=Sun, bit0=reserved
    val water: Int      // reservation byte
) {
    fun repeatDaysString(): String {
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val result = mutableListOf<String>()
        for (i in 0..6) {
            if ((repeat shr (7 - i)) and 1 == 1) result.add(days[i])
        }
        return if (result.isEmpty()) "None" else result.joinToString(", ")
    }

    fun repeatDaysList(): List<Boolean> {
        return (0..6).map { i -> (repeat shr (7 - i)) and 1 == 1 }
    }

    companion object {
        fun buildRepeatBitmask(days: List<Boolean>): Int {
            var mask = 0
            for (i in days.indices) {
                if (days[i]) mask = mask or (1 shl (7 - i))
            }
            return mask
        }
    }
}

// ─── Drinking Record ────────────────────────────────────────────────────────────

@Serializable
data class DrinkingRecord(
    val timestamp: Long,    // Unix seconds
    val waterIntakeMl: Int,
    val temperatureC: Int
) {
    fun formattedTime(): String {
        // Simple formatting — platform-specific formatters can override
        val totalSeconds = timestamp
        val hours = ((totalSeconds % 86400) / 3600).toInt()
        val minutes = ((totalSeconds % 3600) / 60).toInt()
        return "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}"
    }
}

// ─── Color Light Config ─────────────────────────────────────────────────────────

@Serializable
data class ColorLightConfig(
    val isOn: Boolean,
    val startColorIndex: Int,   // 0~359 (hue degrees)
    val endColorIndex: Int      // 0~359 (hue degrees)
)

// ─── Do Not Disturb Config ──────────────────────────────────────────────────────

@Serializable
data class DoNotDisturbConfig(
    val isOn: Boolean,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int
) {
    fun formattedRange(): String {
        val sh = startHour.toString().padStart(2, '0')
        val sm = startMinute.toString().padStart(2, '0')
        val eh = endHour.toString().padStart(2, '0')
        val em = endMinute.toString().padStart(2, '0')
        return "$sh:$sm – $eh:$em"
    }
}

// ─── Bottle Status (aggregate state) ────────────────────────────────────────────

@Serializable
data class BottleStatus(
    val batteryText: String = "",
    val firmwareVersion: String = "",
    val waterLevelMl: Int = 0,
    val waterTemperatureC: Int = 0,
    val waterIntakeTargetMl: Int = 0,
    val isConnected: Boolean = false,
    val connectionState: String = "DISCONNECTED"
)

// ─── Bottle Device Info (for scan results) ──────────────────────────────────────

@Serializable
data class BottleDeviceInfo(
    val name: String,
    val address: String,
    val rssi: Int = 0
)

// ─── Gradient Option ────────────────────────────────────────────────────────────

enum class GradientOption(val code: Int, val displayName: String) {
    CLOCKWISE(0, "Clockwise"),
    COUNTER_CLOCKWISE(1, "Counter-CW"),
    TWO_COLOR(2, "Two-Color")
}

// ─── Auto Standby Option ────────────────────────────────────────────────────────

enum class AutoStandbyOption(val code: Int, val displayName: String) {
    FIVE_SECONDS(0, "5s"),
    TEN_SECONDS(1, "10s"),
    FIFTEEN_SECONDS(2, "15s"),
    ALWAYS_ON(3, "Always On")
}

// ─── Battery Level Mapping ──────────────────────────────────────────────────────

enum class BatteryLevel(val code: Int, val displayText: String, val percentage: Int) {
    CRITICAL(0x00, "<10%", 5),
    VERY_LOW(0x01, "10-20%", 15),
    LOW(0x02, "20-40%", 30),
    MEDIUM(0x03, "40-60%", 50),
    HIGH(0x04, "60-80%", 70),
    FULL(0x05, "80-100%", 90),
    CHARGING(0x10, "Charging", 100),
    FULLY_CHARGED(0x11, "Fully Charged", 100);

    companion object {
        fun fromCode(code: Int): BatteryLevel =
            entries.firstOrNull { it.code == code } ?: CRITICAL
    }
}
