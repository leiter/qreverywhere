package cut.the.crap.qreverywhere.shared.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Formats an Instant to a readable date-time string
 * Example: "Jan 15, 2024 10:30"
 */
fun Instant.toReadableString(): String {
    val localDateTime = this.toLocalDateTime(TimeZone.currentSystemDefault())

    val month = when (localDateTime.monthNumber) {
        1 -> "Jan"
        2 -> "Feb"
        3 -> "Mar"
        4 -> "Apr"
        5 -> "May"
        6 -> "Jun"
        7 -> "Jul"
        8 -> "Aug"
        9 -> "Sep"
        10 -> "Oct"
        11 -> "Nov"
        12 -> "Dec"
        else -> ""
    }

    val day = localDateTime.dayOfMonth
    val year = localDateTime.year
    val hour = localDateTime.hour
    val minute = localDateTime.minute.toString().padStart(2, '0')

    return "$month $day, $year $hour:$minute"
}
