package cut.the.crap.qreverywhere.shared.domain.model

import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Unit tests for CalendarEvent parsing and formatting
 */
class CalendarEventTest {

    // ==================== Format Tests ====================

    @Test
    fun `toVEvent creates valid VCALENDAR format`() {
        val event = CalendarEvent(
            title = "Team Meeting",
            startDateTime = Instant.fromEpochSeconds(1704110400), // 2024-01-01 12:00:00 UTC
            endDateTime = Instant.fromEpochSeconds(1704114000),   // 2024-01-01 13:00:00 UTC
            location = "Conference Room A",
            description = "Weekly team sync"
        )

        val result = event.toVEvent()

        assertTrue(result.startsWith("BEGIN:VCALENDAR"))
        assertTrue(result.contains("VERSION:2.0"))
        assertTrue(result.contains("BEGIN:VEVENT"))
        assertTrue(result.contains("SUMMARY:Team Meeting"))
        assertTrue(result.contains("DTSTART:"))
        assertTrue(result.contains("DTEND:"))
        assertTrue(result.contains("LOCATION:Conference Room A"))
        assertTrue(result.contains("DESCRIPTION:Weekly team sync"))
        assertTrue(result.contains("END:VEVENT"))
        assertTrue(result.endsWith("END:VCALENDAR"))
    }

    @Test
    fun `toVEvent handles all-day events`() {
        val event = CalendarEvent(
            title = "Holiday",
            startDateTime = Instant.fromEpochSeconds(1704067200), // 2024-01-01 00:00:00 UTC
            endDateTime = Instant.fromEpochSeconds(1704153600),   // 2024-01-02 00:00:00 UTC
            isAllDay = true
        )

        val result = event.toVEvent()

        assertTrue(result.contains("DTSTART;VALUE=DATE:"))
        assertTrue(result.contains("DTEND;VALUE=DATE:"))
    }

    @Test
    fun `toVEvent escapes special characters`() {
        val event = CalendarEvent(
            title = "Meeting; Important",
            startDateTime = Instant.fromEpochSeconds(1704110400),
            endDateTime = Instant.fromEpochSeconds(1704114000),
            description = "Line 1\nLine 2"
        )

        val result = event.toVEvent()

        assertTrue(result.contains("SUMMARY:Meeting\\; Important"))
        assertTrue(result.contains("DESCRIPTION:Line 1\\nLine 2"))
    }

    // ==================== Parse Tests ====================

    @Test
    fun `parse extracts event details correctly`() {
        val vevent = """
            BEGIN:VCALENDAR
            VERSION:2.0
            BEGIN:VEVENT
            SUMMARY:Project Kickoff
            DTSTART:20240115T090000Z
            DTEND:20240115T100000Z
            LOCATION:Board Room
            DESCRIPTION:Initial meeting
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val result = CalendarEvent.parse(vevent)

        assertNotNull(result)
        assertEquals("Project Kickoff", result.title)
        assertEquals("Board Room", result.location)
        assertEquals("Initial meeting", result.description)
        assertFalse(result.isAllDay)
    }

    @Test
    fun `parse handles all-day events`() {
        val vevent = """
            BEGIN:VCALENDAR
            VERSION:2.0
            BEGIN:VEVENT
            SUMMARY:Conference
            DTSTART;VALUE=DATE:20240120
            DTEND;VALUE=DATE:20240122
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val result = CalendarEvent.parse(vevent)

        assertNotNull(result)
        assertEquals("Conference", result.title)
        assertTrue(result.isAllDay)
    }

    @Test
    fun `parse returns null for non-VEVENT string`() {
        val result = CalendarEvent.parse("https://example.com")
        assertNull(result)
    }

    @Test
    fun `parse returns null for VEVENT without required fields`() {
        val vevent = """
            BEGIN:VCALENDAR
            BEGIN:VEVENT
            DESCRIPTION:Missing title and dates
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val result = CalendarEvent.parse(vevent)
        assertNull(result)
    }

    @Test
    fun `parse unescapes special characters`() {
        val vevent = """
            BEGIN:VCALENDAR
            VERSION:2.0
            BEGIN:VEVENT
            SUMMARY:Meeting\; Important
            DTSTART:20240115T090000Z
            DTEND:20240115T100000Z
            DESCRIPTION:Line 1\nLine 2
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val result = CalendarEvent.parse(vevent)

        assertNotNull(result)
        assertEquals("Meeting; Important", result.title)
        assertEquals("Line 1\nLine 2", result.description)
    }

    // ==================== Round-Trip Tests ====================

    @Test
    fun `parse and toVEvent round-trip preserves essential data`() {
        val original = CalendarEvent(
            title = "Test Event",
            startDateTime = Instant.fromEpochSeconds(1704110400),
            endDateTime = Instant.fromEpochSeconds(1704114000),
            location = "Test Location",
            description = "Test Description"
        )

        val vevent = original.toVEvent()
        val parsed = CalendarEvent.parse(vevent)

        assertNotNull(parsed)
        assertEquals(original.title, parsed.title)
        assertEquals(original.location, parsed.location)
        assertEquals(original.description, parsed.description)
    }
}
