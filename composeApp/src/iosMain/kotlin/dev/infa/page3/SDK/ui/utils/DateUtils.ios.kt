package dev.infa.page3.SDK.ui.utils

actual object DateUtils {
    actual fun getCurrentDate(): DateInfo {
        TODO("Not yet implemented")
    }

    actual fun addDays(
        dateInfo: DateInfo,
        days: Int
    ): DateInfo {
        TODO("Not yet implemented")
    }

    actual fun formatDateForDisplay(dateInfo: DateInfo): String {
        TODO("Not yet implemented")
    }

    actual fun isToday(dateInfo: DateInfo): Boolean {
        TODO("Not yet implemented")
    }

    actual fun getDayOffsetFromToday(dateInfo: DateInfo): Int {
        TODO("Not yet implemented")
    }

    actual fun getDateByOffset(offset: Int): DateInfo {
        TODO("Not yet implemented")
    }

    actual fun getDayOfWeekIndex(dateInfo: DateInfo): Int {
        TODO("Not yet implemented")
    }
}

actual fun formatMinuteOfDay(timestamp: Int): String {
    TODO("Not yet implemented")
}

actual fun formatTimestamp(timestamp: Long): String {
    TODO("Not yet implemented")
}

actual object FormatUtils {
    actual fun formatNumber(value: Int): String {
        TODO("Not yet implemented")
    }

    actual fun formatDecimal(value: Double, decimalPlaces: Int): String {
        TODO("Not yet implemented")
    }

    actual fun formatDistance(steps: Int): String {
        TODO("Not yet implemented")
    }
}