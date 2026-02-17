package cut.the.crap.qreverywhere.shared.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * Calendar event data for VEVENT QR code format
 *
 * Follows iCalendar (RFC 5545) VEVENT format:
 * ```
 * BEGIN:VCALENDAR
 * VERSION:2.0
 * BEGIN:VEVENT
 * SUMMARY:Event Title
 * DTSTART:20240101T100000Z
 * DTEND:20240101T110000Z
 * LOCATION:Meeting Room
 * DESCRIPTION:Event description
 * END:VEVENT
 * END:VCALENDAR
 * ```
 */
data class CalendarEvent(
    val title: String,
    val startDateTime: Instant,
    val endDateTime: Instant,
    val location: String? = null,
    val description: String? = null,
    val isAllDay: Boolean = false
) {
    /**
     * Convert to VEVENT QR code format
     */
    fun toVEvent(): String {
        return buildString {
            appendLine("BEGIN:VCALENDAR")
            appendLine("VERSION:2.0")
            appendLine("PRODID:-//QrEveryWhere//EN")
            appendLine("BEGIN:VEVENT")
            appendLine("SUMMARY:${escapeVCalText(title)}")

            if (isAllDay) {
                // All-day events use DATE format (YYYYMMDD)
                appendLine("DTSTART;VALUE=DATE:${formatDate(startDateTime)}")
                appendLine("DTEND;VALUE=DATE:${formatDate(endDateTime)}")
            } else {
                // Timed events use UTC format
                appendLine("DTSTART:${formatDateTime(startDateTime)}")
                appendLine("DTEND:${formatDateTime(endDateTime)}")
            }

            location?.let {
                appendLine("LOCATION:${escapeVCalText(it)}")
            }
            description?.let {
                appendLine("DESCRIPTION:${escapeVCalText(it)}")
            }

            // Generate a unique ID
            appendLine("UID:${startDateTime.epochSeconds}-${title.hashCode()}@qreverywhere")

            appendLine("END:VEVENT")
            append("END:VCALENDAR")
        }
    }

    /**
     * Format instant as iCalendar datetime (UTC)
     * Format: YYYYMMDDTHHMMSSZ
     */
    private fun formatDateTime(instant: Instant): String {
        val dt = instant.toLocalDateTime(TimeZone.UTC)
        return "${dt.year.pad(4)}${dt.monthNumber.pad(2)}${dt.dayOfMonth.pad(2)}T${dt.hour.pad(2)}${dt.minute.pad(2)}${dt.second.pad(2)}Z"
    }

    /**
     * Format instant as iCalendar date only
     * Format: YYYYMMDD
     */
    private fun formatDate(instant: Instant): String {
        val dt = instant.toLocalDateTime(TimeZone.UTC)
        return "${dt.year.pad(4)}${dt.monthNumber.pad(2)}${dt.dayOfMonth.pad(2)}"
    }

    private fun Int.pad(length: Int): String = this.toString().padStart(length, '0')

    /**
     * Escape special characters for iCalendar text
     */
    private fun escapeVCalText(text: String): String {
        return text
            .replace("\\", "\\\\")
            .replace(",", "\\,")
            .replace(";", "\\;")
            .replace("\n", "\\n")
    }

    companion object {
        /**
         * Parse a VEVENT string into CalendarEvent
         * @param vEventString The raw VEVENT QR code content
         * @return Parsed CalendarEvent or null if parsing fails
         */
        fun parse(vEventString: String): CalendarEvent? {
            if (!vEventString.contains("BEGIN:VEVENT")) {
                return null
            }

            val content = vEventString
                .substringAfter("BEGIN:VEVENT")
                .substringBefore("END:VEVENT")

            val lines = content.lines().map { it.trim() }

            var title: String? = null
            var startDateTime: Instant? = null
            var endDateTime: Instant? = null
            var location: String? = null
            var description: String? = null
            var isAllDay = false

            for (line in lines) {
                when {
                    line.startsWith("SUMMARY:") -> {
                        title = unescapeVCalText(line.substringAfter("SUMMARY:"))
                    }
                    line.startsWith("DTSTART;VALUE=DATE:") -> {
                        isAllDay = true
                        startDateTime = parseDateOnly(line.substringAfter("DTSTART;VALUE=DATE:"))
                    }
                    line.startsWith("DTSTART:") -> {
                        startDateTime = parseDateTime(line.substringAfter("DTSTART:"))
                    }
                    line.startsWith("DTEND;VALUE=DATE:") -> {
                        endDateTime = parseDateOnly(line.substringAfter("DTEND;VALUE=DATE:"))
                    }
                    line.startsWith("DTEND:") -> {
                        endDateTime = parseDateTime(line.substringAfter("DTEND:"))
                    }
                    line.startsWith("LOCATION:") -> {
                        location = unescapeVCalText(line.substringAfter("LOCATION:"))
                    }
                    line.startsWith("DESCRIPTION:") -> {
                        description = unescapeVCalText(line.substringAfter("DESCRIPTION:"))
                    }
                }
            }

            if (title == null || startDateTime == null || endDateTime == null) {
                return null
            }

            return CalendarEvent(
                title = title,
                startDateTime = startDateTime,
                endDateTime = endDateTime,
                location = location,
                description = description,
                isAllDay = isAllDay
            )
        }

        /**
         * Parse iCalendar datetime (UTC format: YYYYMMDDTHHMMSSZ)
         */
        private fun parseDateTime(dateTimeString: String): Instant? {
            return try {
                val clean = dateTimeString.trim().removeSuffix("Z")
                if (clean.length < 15 || !clean.contains("T")) return null

                val year = clean.substring(0, 4).toInt()
                val month = clean.substring(4, 6).toInt()
                val day = clean.substring(6, 8).toInt()
                val hour = clean.substring(9, 11).toInt()
                val minute = clean.substring(11, 13).toInt()
                val second = clean.substring(13, 15).toInt()

                val dateTime = kotlinx.datetime.LocalDateTime(year, month, day, hour, minute, second)
                dateTime.toInstant(TimeZone.UTC)
            } catch (e: Exception) {
                null
            }
        }

        /**
         * Parse iCalendar date only (format: YYYYMMDD)
         */
        private fun parseDateOnly(dateString: String): Instant? {
            return try {
                val clean = dateString.trim()
                if (clean.length < 8) return null

                val year = clean.substring(0, 4).toInt()
                val month = clean.substring(4, 6).toInt()
                val day = clean.substring(6, 8).toInt()

                val dateTime = kotlinx.datetime.LocalDateTime(year, month, day, 0, 0, 0)
                dateTime.toInstant(TimeZone.UTC)
            } catch (e: Exception) {
                null
            }
        }

        /**
         * Unescape iCalendar text
         */
        private fun unescapeVCalText(text: String): String {
            return text
                .replace("\\n", "\n")
                .replace("\\;", ";")
                .replace("\\,", ",")
                .replace("\\\\", "\\")
        }
    }
}
