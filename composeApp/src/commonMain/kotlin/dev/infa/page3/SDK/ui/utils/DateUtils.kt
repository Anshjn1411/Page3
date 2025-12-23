package dev.infa.page3.SDK.ui.utils

// ============================================
// commonMain/DateUtils.kt - Expect declarations
// ============================================

/**
 * Platform-agnostic date representation
 */

/**
 * Platform-specific date utilities
 */
/**
 * Platform-agnostic date representation
 */
data class DateInfo(
    val year: Int,
    val month: Int,
    val day: Int,
    val dayOfYear: Int,
    val dayOfWeek: Int, // 1=Monday, 7=Sunday
    val timestamp: Long
)

/**
 * Platform-specific date utilities
 */
expect object DateUtils {
    /**
     * Get current date info
     */
    fun getCurrentDate(): DateInfo

    /**
     * Add days to a date
     */
    fun addDays(dateInfo: DateInfo, days: Int): DateInfo

    /**
     * Format date for display
     * Returns "Today", "Yesterday", or formatted date string
     */
    fun formatDateForDisplay(dateInfo: DateInfo): String

    /**
     * Check if date is today
     */
    fun isToday(dateInfo: DateInfo): Boolean

    /**
     * Calculate day offset from today (0 = today, 1 = yesterday, etc.)
     */
    fun getDayOffsetFromToday(dateInfo: DateInfo): Int

    /**
     * Get date by offset from today
     */
    fun getDateByOffset(offset: Int): DateInfo

    /**
     * Get day of week index (0 = Monday, 6 = Sunday)
     */
    fun getDayOfWeekIndex(dateInfo: DateInfo): Int
}

// ============================================
// commonMain/FormatUtils.kt - Number formatting
// ============================================



// ============================================
// androidMain/DateUtils.android.kt - Android implementation
// ============================================



expect object FormatUtils {
    /**
     * Format number with thousands separator
     * Example: 12345 -> "12,345"
     */
    fun formatNumber(value: Int): String

    /**
     * Format decimal number with specified decimal places
     * Example: 1.23456, 2 -> "1.23"
     */
    fun formatDecimal(value: Double, decimalPlaces: Int): String

    /**
     * Format distance in kilometers
     * Example: 1234 steps -> "0.96 km"
     */
    fun formatDistance(steps: Int): String
}

// commonMain
expect fun formatTimestamp(timestamp: Long): String

expect fun formatMinuteOfDay(timestamp: Int): String



