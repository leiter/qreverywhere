package cut.the.crap.qreverywhere.shared.domain.model

import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Unit tests for QrItem domain model and extension functions
 */
class QrItemTest {

    private fun createTestQrItem(textContent: String): QrItem {
        return QrItem(
            id = 1,
            textContent = textContent,
            acquireType = AcquireType.SCANNED,
            timestamp = Clock.System.now()
        )
    }

    // ==================== Phone Number Tests ====================

    @Test
    fun `determineType returns PHONE for tel prefix`() {
        val item = createTestQrItem("tel:+1234567890")
        assertEquals(QrCodeType.PHONE, item.determineType())
    }

    @Test
    fun `determineType returns PHONE for tel prefix with local number`() {
        val item = createTestQrItem("tel:555-1234")
        assertEquals(QrCodeType.PHONE, item.determineType())
    }

    // ==================== Email Tests ====================

    @Test
    fun `determineType returns EMAIL for mailto prefix`() {
        val item = createTestQrItem("mailto:test@example.com")
        assertEquals(QrCodeType.EMAIL, item.determineType())
    }

    @Test
    fun `determineType returns EMAIL for mailto with subject`() {
        val item = createTestQrItem("mailto:test@example.com?subject=Hello")
        assertEquals(QrCodeType.EMAIL, item.determineType())
    }

    @Test
    fun `determineType returns EMAIL for mailto with subject and body`() {
        val item = createTestQrItem("mailto:test@example.com?subject=Hello&body=World")
        assertEquals(QrCodeType.EMAIL, item.determineType())
    }

    // ==================== URL Tests ====================

    @Test
    fun `determineType returns WEB_URL for https prefix`() {
        val item = createTestQrItem("https://www.example.com")
        assertEquals(QrCodeType.WEB_URL, item.determineType())
    }

    @Test
    fun `determineType returns WEB_URL for http prefix`() {
        val item = createTestQrItem("http://example.com")
        assertEquals(QrCodeType.WEB_URL, item.determineType())
    }

    @Test
    fun `determineType returns WEB_URL for https with path`() {
        val item = createTestQrItem("https://example.com/path/to/page")
        assertEquals(QrCodeType.WEB_URL, item.determineType())
    }

    @Test
    fun `determineType returns WEB_URL for https with query params`() {
        val item = createTestQrItem("https://example.com?foo=bar&baz=qux")
        assertEquals(QrCodeType.WEB_URL, item.determineType())
    }

    // ==================== SMS Tests ====================

    @Test
    fun `determineType returns SMS for sms prefix`() {
        val item = createTestQrItem("sms:+1234567890")
        assertEquals(QrCodeType.SMS, item.determineType())
    }

    @Test
    fun `determineType returns SMS for smsto prefix`() {
        val item = createTestQrItem("smsto:+1234567890")
        assertEquals(QrCodeType.SMS, item.determineType())
    }

    @Test
    fun `determineType returns SMS for sms with body`() {
        val item = createTestQrItem("sms:+1234567890?body=Hello%20World")
        assertEquals(QrCodeType.SMS, item.determineType())
    }

    // ==================== WiFi Tests ====================

    @Test
    fun `determineType returns WIFI for WIFI format`() {
        val item = createTestQrItem("WIFI:T:WPA;S:MyNetwork;P:password123;;")
        assertEquals(QrCodeType.WIFI, item.determineType())
    }

    @Test
    fun `determineType returns WIFI for WIFI with WEP security`() {
        val item = createTestQrItem("WIFI:T:WEP;S:OldNetwork;P:wepkey;;")
        assertEquals(QrCodeType.WIFI, item.determineType())
    }

    @Test
    fun `determineType returns WIFI for open WIFI network`() {
        val item = createTestQrItem("WIFI:T:nopass;S:OpenNetwork;;")
        assertEquals(QrCodeType.WIFI, item.determineType())
    }

    // ==================== vCard Tests ====================

    @Test
    fun `determineType returns CONTACT for vCard`() {
        val vcard = """
            BEGIN:VCARD
            VERSION:3.0
            N:Doe;John
            FN:John Doe
            TEL:+1234567890
            END:VCARD
        """.trimIndent()
        val item = createTestQrItem(vcard)
        assertEquals(QrCodeType.CONTACT, item.determineType())
    }

    @Test
    fun `determineType returns CONTACT for vCard with newline ending`() {
        val vcard = """
            BEGIN:VCARD
            VERSION:3.0
            N:Doe;John
            FN:John Doe
            END:VCARD

        """.trimIndent()
        val item = createTestQrItem(vcard)
        assertEquals(QrCodeType.CONTACT, item.determineType())
    }

    @Test
    fun `isVcard returns true for valid vCard`() {
        val vcard = """
            BEGIN:VCARD
            VERSION:3.0
            FN:John Doe
            END:VCARD
        """.trimIndent()
        val item = createTestQrItem(vcard)
        assertTrue(item.isVcard())
    }

    @Test
    fun `isVcard returns false for non-vCard content`() {
        val item = createTestQrItem("https://example.com")
        assertFalse(item.isVcard())
    }

    @Test
    fun `isVcard returns false for incomplete vCard`() {
        val item = createTestQrItem("BEGIN:VCARD\nFN:John Doe")
        assertFalse(item.isVcard())
    }

    // ==================== Unknown Content Tests ====================

    @Test
    fun `determineType returns UNKNOWN_CONTENT for plain text`() {
        val item = createTestQrItem("Hello World")
        assertEquals(QrCodeType.UNKNOWN_CONTENT, item.determineType())
    }

    @Test
    fun `determineType returns UNKNOWN_CONTENT for random string`() {
        val item = createTestQrItem("abc123xyz")
        assertEquals(QrCodeType.UNKNOWN_CONTENT, item.determineType())
    }

    @Test
    fun `determineType returns UNKNOWN_CONTENT for empty string`() {
        val item = createTestQrItem("")
        assertEquals(QrCodeType.UNKNOWN_CONTENT, item.determineType())
    }

    // ==================== URL Encoding Tests ====================

    @Test
    fun `determineType handles URL-encoded tel prefix`() {
        val item = createTestQrItem("tel%3A+1234567890")
        assertEquals(QrCodeType.PHONE, item.determineType())
    }

    @Test
    fun `determineType handles URL-encoded mailto prefix`() {
        val item = createTestQrItem("mailto%3Atest@example.com")
        assertEquals(QrCodeType.EMAIL, item.determineType())
    }

    @Test
    fun `determineType handles URL-encoded https prefix`() {
        val item = createTestQrItem("https%3A%2F%2Fexample.com")
        assertEquals(QrCodeType.WEB_URL, item.determineType())
    }

    // ==================== QrItem Data Class Tests ====================

    @Test
    fun `QrItem equals returns true for same content`() {
        val timestamp = Clock.System.now()
        val item1 = QrItem(1, "test", AcquireType.SCANNED, timestamp, null)
        val item2 = QrItem(1, "test", AcquireType.SCANNED, timestamp, null)
        assertEquals(item1, item2)
    }

    @Test
    fun `QrItem equals handles ByteArray properly`() {
        val timestamp = Clock.System.now()
        val imageData = byteArrayOf(1, 2, 3, 4)
        val item1 = QrItem(1, "test", AcquireType.SCANNED, timestamp, imageData)
        val item2 = QrItem(1, "test", AcquireType.SCANNED, timestamp, imageData.copyOf())
        assertEquals(item1, item2)
    }

    @Test
    fun `QrItem hashCode consistent with equals`() {
        val timestamp = Clock.System.now()
        val item1 = QrItem(1, "test", AcquireType.SCANNED, timestamp, null)
        val item2 = QrItem(1, "test", AcquireType.SCANNED, timestamp, null)
        assertEquals(item1.hashCode(), item2.hashCode())
    }

    // ==================== AcquireType Tests ====================

    @Test
    fun `AcquireType has correct entries`() {
        assertEquals(5, AcquireType.entries.size)
        assertTrue(AcquireType.entries.contains(AcquireType.SCANNED))
        assertTrue(AcquireType.entries.contains(AcquireType.CREATED))
        assertTrue(AcquireType.entries.contains(AcquireType.FROM_FILE))
        assertTrue(AcquireType.entries.contains(AcquireType.ERROR_OCCURRED))
        assertTrue(AcquireType.entries.contains(AcquireType.EMPTY_DEFAULT))
    }
}
