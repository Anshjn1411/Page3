package dev.infa.page3.SDK.ui.utils


// ============================================
// androidMain/DateUtils.android.kt - Android implementation
// ============================================

import dev.infa.page3.SDK.ui.screens.format
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import kotlin.text.get
import kotlin.text.set
import kotlin.text.toInt
import java.text.NumberFormat
import java.util.Date
import java.util.Locale


actual object DateUtils {

    actual fun getCurrentDate(): DateInfo {
        val calendar = Calendar.getInstance()
        return DateInfo(
            year = calendar.get(Calendar.YEAR),
            month = calendar.get(Calendar.MONTH) + 1,
            day = calendar.get(Calendar.DAY_OF_MONTH),
            dayOfYear = calendar.get(Calendar.DAY_OF_YEAR),
            dayOfWeek = getDayOfWeekMondayBased(calendar),
            timestamp = calendar.timeInMillis
        )
    }

    actual fun addDays(dateInfo: DateInfo, days: Int): DateInfo {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = dateInfo.timestamp
            add(Calendar.DAY_OF_YEAR, days)
        }
        return DateInfo(
            year = calendar.get(Calendar.YEAR),
            month = calendar.get(Calendar.MONTH) + 1,
            day = calendar.get(Calendar.DAY_OF_MONTH),
            dayOfYear = calendar.get(Calendar.DAY_OF_YEAR),
            dayOfWeek = getDayOfWeekMondayBased(calendar),
            timestamp = calendar.timeInMillis
        )
    }

    actual fun formatDateForDisplay(dateInfo: DateInfo): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = dateInfo.timestamp
        }
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }

        return when {
            isSameDay(calendar, today) -> "Today"
            isSameDay(calendar, yesterday) -> "Yesterday"
            else -> SimpleDateFormat("MMM dd, yyyy", Locale.US).format(calendar.time)
        }
    }

    actual fun isToday(dateInfo: DateInfo): Boolean {
        val today = Calendar.getInstance()
        val dateCalendar = Calendar.getInstance().apply {
            timeInMillis = dateInfo.timestamp
        }
        return isSameDay(dateCalendar, today)
    }

    actual fun getDayOffsetFromToday(dateInfo: DateInfo): Int {
        val today = Calendar.getInstance()
        val date = Calendar.getInstance().apply {
            timeInMillis = dateInfo.timestamp
        }

        // Reset time to midnight for accurate day calculation
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        date.set(Calendar.HOUR_OF_DAY, 0)
        date.set(Calendar.MINUTE, 0)
        date.set(Calendar.SECOND, 0)
        date.set(Calendar.MILLISECOND, 0)

        val diffInMillis = today.timeInMillis - date.timeInMillis
        return (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
    }

    actual fun getDateByOffset(offset: Int): DateInfo {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -offset)
        }
        return DateInfo(
            year = calendar.get(Calendar.YEAR),
            month = calendar.get(Calendar.MONTH) + 1,
            day = calendar.get(Calendar.DAY_OF_MONTH),
            dayOfYear = calendar.get(Calendar.DAY_OF_YEAR),
            dayOfWeek = getDayOfWeekMondayBased(calendar),
            timestamp = calendar.timeInMillis
        )
    }

    actual fun getDayOfWeekIndex(dateInfo: DateInfo): Int {
        return dateInfo.dayOfWeek - 1 // Convert 1-7 to 0-6
    }

    private fun getDayOfWeekMondayBased(calendar: Calendar): Int {
        // Calendar.DAY_OF_WEEK: 1=Sunday, 2=Monday, ..., 7=Saturday
        // Convert to: 1=Monday, 7=Sunday
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> 7
            else -> calendar.get(Calendar.DAY_OF_WEEK) - 1
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}



actual object FormatUtils {

    actual fun formatNumber(value: Int): String {
        return NumberFormat.getNumberInstance(Locale.US).format(value)
    }

    actual fun formatDecimal(value: Double, decimalPlaces: Int): String {
        val pattern = "0.${"0".repeat(decimalPlaces)}"
        return DecimalFormat(pattern).format(value)
    }

    actual fun formatDistance(steps: Int): String {
        val km = steps * 0.00078
        return "${formatDecimal(km, 1)} km"
    }
}

// androidMain
actual fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.US)
    return sdf.format(Date(timestamp))
}

actual fun formatMinuteOfDay(timestamp: Int): String {
    val hour = timestamp / 60
    val minute = timestamp % 60
    return String.format("%02d:%02d", hour, minute)
}