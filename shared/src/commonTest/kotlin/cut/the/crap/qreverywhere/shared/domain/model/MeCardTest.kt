package cut.the.crap.qreverywhere.shared.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Unit tests for MeCard parsing and formatting
 */
class MeCardTest {

    // ==================== Format Tests ====================

    @Test
    fun `toMeCard creates valid format with all fields`() {
        val mecard = MeCard(
            name = "John Doe",
            phone = "+1234567890",
            email = "john@example.com",
            address = "123 Main St",
            organization = "Acme Inc",
            note = "VIP Customer",
            url = "https://example.com",
            birthday = "19900101"
        )

        val result = mecard.toMeCard()

        assertTrue(result.startsWith("MECARD:"))
        assertTrue(result.contains("N:John Doe;"))
        assertTrue(result.contains("TEL:+1234567890;"))
        assertTrue(result.contains("EMAIL:john@example.com;"))
        assertTrue(result.contains("ADR:123 Main St;"))
        assertTrue(result.contains("ORG:Acme Inc;"))
        assertTrue(result.contains("NOTE:VIP Customer;"))
        assertTrue(result.contains("URL:https\\://example.com;"))
        assertTrue(result.contains("BDAY:19900101;"))
        assertTrue(result.endsWith(";;"))
    }

    @Test
    fun `toMeCard creates minimal format with just name`() {
        val mecard = MeCard(name = "Jane Smith")

        val result = mecard.toMeCard()

        assertEquals("MECARD:N:Jane Smith;;", result)
    }

    @Test
    fun `toMeCard escapes special characters`() {
        val mecard = MeCard(
            name = "Test; Name",
            address = "123: Main St"
        )

        val result = mecard.toMeCard()

        assertTrue(result.contains("N:Test\\; Name;"))
        assertTrue(result.contains("ADR:123\\: Main St;"))
    }

    // ==================== Parse Tests ====================

    @Test
    fun `parse extracts all fields correctly`() {
        val mecardString = "MECARD:N:John Doe;TEL:+1234567890;EMAIL:john@example.com;ADR:123 Main St;ORG:Acme Inc;NOTE:VIP;URL:https://example.com;BDAY:19900101;;"

        val result = MeCard.parse(mecardString)

        assertNotNull(result)
        assertEquals("John Doe", result.name)
        assertEquals("+1234567890", result.phone)
        assertEquals("john@example.com", result.email)
        assertEquals("123 Main St", result.address)
        assertEquals("Acme Inc", result.organization)
        assertEquals("VIP", result.note)
        assertEquals("https://example.com", result.url)
        assertEquals("19900101", result.birthday)
    }

    @Test
    fun `parse handles minimal mecard with just name`() {
        val result = MeCard.parse("MECARD:N:Jane Smith;;")

        assertNotNull(result)
        assertEquals("Jane Smith", result.name)
        assertNull(result.phone)
        assertNull(result.email)
    }

    @Test
    fun `parse returns null for non-mecard string`() {
        val result = MeCard.parse("BEGIN:VCARD")
        assertNull(result)
    }

    @Test
    fun `parse returns null for mecard without name`() {
        val result = MeCard.parse("MECARD:TEL:+1234567890;;")
        assertNull(result)
    }

    @Test
    fun `parse unescapes special characters`() {
        val mecardString = "MECARD:N:Test\\; Name;ADR:123\\: Main St;;"

        val result = MeCard.parse(mecardString)

        assertNotNull(result)
        assertEquals("Test; Name", result.name)
        assertEquals("123: Main St", result.address)
    }

    // ==================== Helper Tests ====================

    @Test
    fun `isMeCard returns true for valid mecard`() {
        assertTrue(MeCard.isMeCard("MECARD:N:John;;"))
    }

    @Test
    fun `isMeCard returns false for non-mecard`() {
        assertFalse(MeCard.isMeCard("BEGIN:VCARD"))
    }

    @Test
    fun `simple factory creates basic mecard`() {
        val mecard = MeCard.simple("John Doe", "+1234567890", "john@example.com")

        assertEquals("John Doe", mecard.name)
        assertEquals("+1234567890", mecard.phone)
        assertEquals("john@example.com", mecard.email)
        assertNull(mecard.address)
    }

    @Test
    fun `getDisplayName returns name`() {
        val mecard = MeCard(name = "John Doe")
        assertEquals("John Doe", mecard.getDisplayName())
    }

    @Test
    fun `hasContactDetails returns true when phone exists`() {
        val mecard = MeCard(name = "John", phone = "+1234567890")
        assertTrue(mecard.hasContactDetails())
    }

    @Test
    fun `hasContactDetails returns true when email exists`() {
        val mecard = MeCard(name = "John", email = "john@example.com")
        assertTrue(mecard.hasContactDetails())
    }

    @Test
    fun `hasContactDetails returns false for name only`() {
        val mecard = MeCard(name = "John")
        assertFalse(mecard.hasContactDetails())
    }

    // ==================== Round-Trip Tests ====================

    @Test
    fun `parse and toMeCard round-trip preserves data`() {
        val original = MeCard(
            name = "Test User",
            phone = "+9876543210",
            email = "test@test.com",
            address = "456 Oak Ave"
        )

        val formatted = original.toMeCard()
        val parsed = MeCard.parse(formatted)

        assertNotNull(parsed)
        assertEquals(original.name, parsed.name)
        assertEquals(original.phone, parsed.phone)
        assertEquals(original.email, parsed.email)
        assertEquals(original.address, parsed.address)
    }
}
