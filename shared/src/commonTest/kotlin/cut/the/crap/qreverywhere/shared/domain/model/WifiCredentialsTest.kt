package cut.the.crap.qreverywhere.shared.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Unit tests for WifiCredentials parsing and formatting
 */
class WifiCredentialsTest {

    // ==================== Parse Tests ====================

    @Test
    fun `parse WPA network with password`() {
        val wifiString = "WIFI:T:WPA;S:MyNetwork;P:password123;;"
        val result = WifiCredentials.parse(wifiString)

        assertNotNull(result)
        assertEquals("MyNetwork", result.ssid)
        assertEquals("password123", result.password)
        assertEquals(WifiSecurityType.WPA, result.securityType)
        assertFalse(result.isHidden)
    }

    @Test
    fun `parse WPA2 network`() {
        val wifiString = "WIFI:T:WPA2;S:SecureNet;P:MySecurePass;;"
        val result = WifiCredentials.parse(wifiString)

        assertNotNull(result)
        assertEquals("SecureNet", result.ssid)
        assertEquals("MySecurePass", result.password)
        assertEquals(WifiSecurityType.WPA, result.securityType)
    }

    @Test
    fun `parse WEP network`() {
        val wifiString = "WIFI:T:WEP;S:OldNetwork;P:wepkey;;"
        val result = WifiCredentials.parse(wifiString)

        assertNotNull(result)
        assertEquals("OldNetwork", result.ssid)
        assertEquals("wepkey", result.password)
        assertEquals(WifiSecurityType.WEP, result.securityType)
    }

    @Test
    fun `parse open network without password`() {
        val wifiString = "WIFI:T:nopass;S:FreeWifi;;"
        val result = WifiCredentials.parse(wifiString)

        assertNotNull(result)
        assertEquals("FreeWifi", result.ssid)
        assertEquals("", result.password)
        assertEquals(WifiSecurityType.OPEN, result.securityType)
    }

    @Test
    fun `parse hidden network`() {
        val wifiString = "WIFI:T:WPA;S:HiddenNet;P:secret;H:true;;"
        val result = WifiCredentials.parse(wifiString)

        assertNotNull(result)
        assertEquals("HiddenNet", result.ssid)
        assertTrue(result.isHidden)
    }

    @Test
    fun `parse non-hidden network explicitly`() {
        val wifiString = "WIFI:T:WPA;S:VisibleNet;P:pass;H:false;;"
        val result = WifiCredentials.parse(wifiString)

        assertNotNull(result)
        assertFalse(result.isHidden)
    }

    @Test
    fun `parse network with special characters in SSID`() {
        val wifiString = "WIFI:T:WPA;S:My\\;Network;P:pass;;"
        val result = WifiCredentials.parse(wifiString)

        assertNotNull(result)
        assertEquals("My;Network", result.ssid)
    }

    @Test
    fun `parse network with escaped colon in password`() {
        val wifiString = "WIFI:T:WPA;S:Network;P:pass\\:word;;"
        val result = WifiCredentials.parse(wifiString)

        assertNotNull(result)
        assertEquals("pass:word", result.password)
    }

    @Test
    fun `parse returns null for non-WIFI string`() {
        val result = WifiCredentials.parse("https://example.com")
        assertNull(result)
    }

    @Test
    fun `parse returns null for WIFI string without SSID`() {
        val result = WifiCredentials.parse("WIFI:T:WPA;P:password;;")
        assertNull(result)
    }

    @Test
    fun `parse handles missing type field`() {
        val wifiString = "WIFI:S:OpenNetwork;;"
        val result = WifiCredentials.parse(wifiString)

        assertNotNull(result)
        assertEquals("OpenNetwork", result.ssid)
        assertEquals(WifiSecurityType.OPEN, result.securityType)
    }

    // ==================== Format Tests ====================

    @Test
    fun `toWifiString formats WPA network correctly`() {
        val credentials = WifiCredentials(
            ssid = "MyNetwork",
            password = "mypassword",
            securityType = WifiSecurityType.WPA,
            isHidden = false
        )

        val result = credentials.toWifiString()

        assertTrue(result.startsWith("WIFI:"))
        assertTrue(result.contains("T:WPA;"))
        assertTrue(result.contains("S:MyNetwork;"))
        assertTrue(result.contains("P:mypassword;"))
        assertTrue(result.endsWith(";;"))
    }

    @Test
    fun `toWifiString formats open network without password`() {
        val credentials = WifiCredentials(
            ssid = "FreeWifi",
            password = "",
            securityType = WifiSecurityType.OPEN,
            isHidden = false
        )

        val result = credentials.toWifiString()

        assertTrue(result.contains("T:nopass;"))
        assertFalse(result.contains("P:"))
    }

    @Test
    fun `toWifiString includes hidden flag when true`() {
        val credentials = WifiCredentials(
            ssid = "HiddenNet",
            password = "secret",
            securityType = WifiSecurityType.WPA,
            isHidden = true
        )

        val result = credentials.toWifiString()

        assertTrue(result.contains("H:true;"))
    }

    @Test
    fun `toWifiString escapes special characters`() {
        val credentials = WifiCredentials(
            ssid = "Net;work",
            password = "pass:word",
            securityType = WifiSecurityType.WPA,
            isHidden = false
        )

        val result = credentials.toWifiString()

        assertTrue(result.contains("S:Net\\;work;"))
        assertTrue(result.contains("P:pass\\:word;"))
    }

    // ==================== Password Masking Tests ====================

    @Test
    fun `getMaskedPassword returns masked string for non-empty password`() {
        val credentials = WifiCredentials(
            ssid = "Network",
            password = "secret123",
            securityType = WifiSecurityType.WPA
        )

        val masked = credentials.getMaskedPassword()

        assertTrue(masked.all { it == '*' })
        assertEquals(9, masked.length)
    }

    @Test
    fun `getMaskedPassword limits mask length to 12 characters`() {
        val credentials = WifiCredentials(
            ssid = "Network",
            password = "thisIsAVeryLongPassword",
            securityType = WifiSecurityType.WPA
        )

        val masked = credentials.getMaskedPassword()

        assertEquals(12, masked.length)
    }

    @Test
    fun `getMaskedPassword returns placeholder for empty password`() {
        val credentials = WifiCredentials(
            ssid = "OpenNetwork",
            password = "",
            securityType = WifiSecurityType.OPEN
        )

        val masked = credentials.getMaskedPassword()

        assertEquals("(No password)", masked)
    }

    // ==================== SecurityType Tests ====================

    @Test
    fun `WifiSecurityType fromCode returns correct type`() {
        assertEquals(WifiSecurityType.WPA, WifiSecurityType.fromCode("WPA"))
        assertEquals(WifiSecurityType.WEP, WifiSecurityType.fromCode("WEP"))
        assertEquals(WifiSecurityType.OPEN, WifiSecurityType.fromCode("nopass"))
    }

    @Test
    fun `WifiSecurityType fromCode is case insensitive`() {
        assertEquals(WifiSecurityType.WPA, WifiSecurityType.fromCode("wpa"))
        assertEquals(WifiSecurityType.WEP, WifiSecurityType.fromCode("Wep"))
    }

    @Test
    fun `WifiSecurityType fromCode returns WPA for unknown code`() {
        assertEquals(WifiSecurityType.WPA, WifiSecurityType.fromCode("UNKNOWN"))
    }

    @Test
    fun `WifiSecurityType has correct display names`() {
        assertEquals("WPA/WPA2/WPA3", WifiSecurityType.WPA.displayName)
        assertEquals("WEP", WifiSecurityType.WEP.displayName)
        assertEquals("Open (No Password)", WifiSecurityType.OPEN.displayName)
    }

    // ==================== Round-Trip Tests ====================

    @Test
    fun `parse and toWifiString round-trip preserves data`() {
        val original = "WIFI:T:WPA;S:MyNetwork;P:password123;;"
        val parsed = WifiCredentials.parse(original)
        assertNotNull(parsed)

        val formatted = parsed.toWifiString()
        val reparsed = WifiCredentials.parse(formatted)
        assertNotNull(reparsed)

        assertEquals(parsed.ssid, reparsed.ssid)
        assertEquals(parsed.password, reparsed.password)
        assertEquals(parsed.securityType, reparsed.securityType)
        assertEquals(parsed.isHidden, reparsed.isHidden)
    }
}
