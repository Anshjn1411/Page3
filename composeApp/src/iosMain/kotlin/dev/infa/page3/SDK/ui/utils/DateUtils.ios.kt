package dev.infa.page3.SDK.ui.utils

import platform.Foundation.*
import kotlinx.cinterop.toKString

actual object DateUtils {

    actual fun getCurrentDate(): DateInfo {
        val now = NSDate()
        val calendar = NSCalendar.currentCalendar

        val year = calendar.component(NSCalendarUnitYear, now).toInt()
        val month = calendar.component(NSCalendarUnitMonth, now).toInt()
        val day = calendar.component(NSCalendarUnitDay, now).toInt()
        val dayOfYear = calendar.ordinalityOfUnit(NSCalendarUnitDay, NSCalendarUnitYear, now).toInt()
        val dayOfWeek = calendar.component(NSCalendarUnitWeekday, now).toInt() // Sunday = 1

        val timestamp = (now.timeIntervalSince1970 * 1000).toLong()

        return DateInfo(
            year = year,
            month = month,
            day = day,
            dayOfYear = dayOfYear,
            dayOfWeek = if(dayOfWeek == 1) 7 else dayOfWeek - 1, // Convert Sunday=1 to 7, Monday=1
            timestamp = timestamp
        )
    }

    actual fun addDays(dateInfo: DateInfo, days: Int): DateInfo {
//        val date = NSDate.dateWithTimeIntervalSince1970(dateInfo.timestamp / 1000.0)
//        val calendar = NSCalendar.currentCalendar
//
//        val newDate = calendar.dateByAddingUnit(
//            NSCalendarUnitDay,
//            days.toLong(),
//            date,
//            NSCalendarOptions(0)
//        ) ?: date
//
//        val year = calendar.component(NSCalendarUnitYear, newDate).toInt()
//        val month = calendar.component(NSCalendarUnitMonth, newDate).toInt()
//        val day = calendar.component(NSCalendarUnitDay, newDate).toInt()
//        val dayOfYear = calendar.ordinalityOfUnit(NSCalendarUnitDay, NSCalendarUnitYear, newDate).toInt()
//        val dayOfWeek = calendar.component(NSCalendarUnitWeekday, newDate).toInt()
//
//        val timestamp = (newDate.timeIntervalSince1970 * 1000).toLong()
//
//        return DateInfo(
//            year = year,
//            month = month,
//            day = day,
//            dayOfYear = dayOfYear,
//            dayOfWeek = if(dayOfWeek == 1) 7 else dayOfWeek - 1,
//            timestamp = timestamp
//        )
        return DateInfo(1111,1,12,1,1,2332)
    }

    actual fun formatDateForDisplay(dateInfo: DateInfo): String {
        val now = NSDate()
        val calendar = NSCalendar.currentCalendar
        val date = NSDate.dateWithTimeIntervalSince1970(dateInfo.timestamp / 1000.0)

        val formatter = NSDateFormatter().apply {
            dateFormat = "MMM dd, yyyy"
            locale = NSLocale("en_US")
        }

        return when {
            isToday(dateInfo) -> "Today"
            getDayOffsetFromToday(dateInfo) == 1 -> "Yesterday"
            else -> formatter.stringFromDate(date)
        }
    }

    actual fun isToday(dateInfo: DateInfo): Boolean {
        val calendar = NSCalendar.currentCalendar
        val today = NSDate()
        val date = NSDate.dateWithTimeIntervalSince1970(dateInfo.timestamp / 1000.0)
        return calendar.isDateInToday(date)
    }

    actual fun getDayOffsetFromToday(dateInfo: DateInfo): Int {
//        val calendar = NSCalendar.currentCalendar
//        val today = NSDate()
//        val date = NSDate.dateWithTimeIntervalSince1970(dateInfo.timestamp / 1000.0)
//
//        // Reset time to midnight to calculate difference in days
//        val startOfToday = calendar.startOfDayForDate(today)
//        val startOfDate = calendar.startOfDayForDate(date)
//
//        val components = calendar.components(
//            NSCalendarUnitDay,
//            startOfDate,
//            startOfToday,
//            NSCalendarOptions(0)
//        )
//        return components.day.toInt()
        return 1
    }

    actual fun getDateByOffset(offset: Int): DateInfo {
//        val calendar = NSCalendar.currentCalendar
//        val now = NSDate()
//        val newDate = calendar.dateByAddingUnit(
//            NSCalendarUnitDay,
//            -offset.toLong(),
//            now,
//            NSCalendarOptions(0)
//        ) ?: now
//
//        val year = calendar.component(NSCalendarUnitYear, newDate).toInt()
//        val month = calendar.component(NSCalendarUnitMonth, newDate).toInt()
//        val day = calendar.component(NSCalendarUnitDay, newDate).toInt()
//        val dayOfYear = calendar.ordinalityOfUnit(NSCalendarUnitDay, NSCalendarUnitYear, newDate).toInt()
//        val dayOfWeek = calendar.component(NSCalendarUnitWeekday, newDate).toInt()
//        val timestamp = (newDate.timeIntervalSince1970 * 1000).toLong()
//
//        return DateInfo(
//            year = year,
//            month = month,
//            day = day,
//            dayOfYear = dayOfYear,
//            dayOfWeek = if(dayOfWeek == 1) 7 else dayOfWeek - 1,
//            timestamp = timestamp
//        )
        return DateInfo(1111,1,12,1,1,2332)
    }

    actual fun getDayOfWeekIndex(dateInfo: DateInfo): Int {
        return dateInfo.dayOfWeek - 1 // Convert 1-7 to 0-6 index
    }

    actual fun getHourOfDay(timestamp: Long): Int {
//        val calendar = Calendar.getInstance().apply {
//            timeInMillis = timestamp
//        }
        return 0
    }

    actual fun getMinuteOfDay(timestamp: Long): Int {
//        val calendar = Calendar.getInstance().apply {
//            timeInMillis = timestamp
//        }
        return 0
    }
}

actual fun formatMinuteOfDay(timestamp: Int): String {
    val hour = timestamp / 60
    val minute = timestamp % 60
    //return String.format("%02d:%02d", hour, minute)
    return ""
}

actual fun formatTimestamp(timestamp: Long): String {
    val date = NSDate.dateWithTimeIntervalSince1970(timestamp / 1000.0)
    val formatter = NSDateFormatter().apply {
        dateFormat = "hh:mm a"
        locale = NSLocale("en_US")
    }
    return formatter.stringFromDate(date)
}

actual object FormatUtils {

    actual fun formatNumber(value: Int): String {
        val formatter = NSNumberFormatter().apply {
            numberStyle = NSNumberFormatterDecimalStyle
            locale = NSLocale("en_US")
        }
        //return formatter.stringFromNumber(value) ?: value.toString()
        return ""
    }

    actual fun formatDecimal(value: Double, decimalPlaces: Int): String {
        val formatter = NSNumberFormatter().apply {
            numberStyle = NSNumberFormatterDecimalStyle
            maximumFractionDigits = decimalPlaces.toULong()
            minimumFractionDigits = decimalPlaces.toULong()
            locale = NSLocale("en_US")
        }
        //return formatter.stringFromNumber(value) ?: value.toString()
        return ""
    }

    actual fun formatDistance(steps: Int): String {
        val km = steps * 0.00078
        return "${formatDecimal(km, 1)} km"
    }
}
