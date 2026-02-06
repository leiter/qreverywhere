package cut.the.crap.qreverywhere.shared.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Unit tests for UrlSafetyChecker
 */
class UrlSafetyCheckerTest {

    private val checker = UrlSafetyChecker()

    // ==================== Safe URL Tests ====================

    @Test
    fun `checkUrl returns SAFE for normal https URL`() {
        val result = checker.checkUrl("https://www.example.com")
        assertEquals(SafetyStatus.SAFE, result.status)
        assertTrue(result.warnings.isEmpty())
    }

    @Test
    fun `checkUrl returns SAFE for normal http URL`() {
        val result = checker.checkUrl("http://example.com/page")
        assertEquals(SafetyStatus.SAFE, result.status)
    }

    @Test
    fun `checkUrl returns SAFE for URL with path and query`() {
        val result = checker.checkUrl("https://example.com/path/to/page?query=value")
        assertEquals(SafetyStatus.SAFE, result.status)
    }

    // ==================== Dangerous Extension Tests ====================

    @Test
    fun `checkUrl returns DANGEROUS for exe file`() {
        val result = checker.checkUrl("https://example.com/download.exe")
        assertEquals(SafetyStatus.DANGEROUS, result.status)
        assertTrue(result.warnings.any { it.contains("executable") })
    }

    @Test
    fun `checkUrl returns DANGEROUS for bat file`() {
        val result = checker.checkUrl("https://example.com/script.bat")
        assertEquals(SafetyStatus.DANGEROUS, result.status)
    }

    @Test
    fun `checkUrl returns DANGEROUS for msi file`() {
        val result = checker.checkUrl("https://example.com/installer.msi")
        assertEquals(SafetyStatus.DANGEROUS, result.status)
    }

    @Test
    fun `checkUrl returns DANGEROUS for exe with query string`() {
        val result = checker.checkUrl("https://example.com/download.exe?version=1")
        assertEquals(SafetyStatus.DANGEROUS, result.status)
    }

    @Test
    fun `checkUrl returns DANGEROUS for macOS dmg file`() {
        val result = checker.checkUrl("https://example.com/app.dmg")
        assertEquals(SafetyStatus.DANGEROUS, result.status)
    }

    // ==================== URL Shortener Tests ====================

    @Test
    fun `checkUrl returns WARNING for bit_ly URL`() {
        val result = checker.checkUrl("https://bit.ly/abc123")
        assertEquals(SafetyStatus.WARNING, result.status)
        assertTrue(result.warnings.any { it.contains("shortened") })
    }

    @Test
    fun `checkUrl returns WARNING for tinyurl`() {
        val result = checker.checkUrl("https://tinyurl.com/xyz789")
        assertEquals(SafetyStatus.WARNING, result.status)
    }

    @Test
    fun `checkUrl returns WARNING for t_co`() {
        val result = checker.checkUrl("https://t.co/abc")
        assertEquals(SafetyStatus.WARNING, result.status)
    }

    // ==================== Phishing Pattern Tests ====================

    @Test
    fun `checkUrl returns WARNING for URL with at symbol`() {
        val result = checker.checkUrl("https://legitimate.com@malicious.com/login")
        assertEquals(SafetyStatus.WARNING, result.status)
        assertTrue(result.warnings.any { it.contains("@") })
    }

    @Test
    fun `checkUrl returns WARNING for IP address URL`() {
        val result = checker.checkUrl("http://192.168.1.1/admin")
        assertEquals(SafetyStatus.WARNING, result.status)
        assertTrue(result.warnings.any { it.contains("IP address") })
    }

    @Test
    fun `checkUrl returns WARNING for excessive subdomains`() {
        val result = checker.checkUrl("https://login.secure.account.verify.example.com")
        assertEquals(SafetyStatus.WARNING, result.status)
        assertTrue(result.warnings.any { it.contains("complex domain") })
    }

    @Test
    fun `checkUrl returns WARNING for multiple suspicious keywords`() {
        val result = checker.checkUrl("https://login-verify-password.example.com")
        assertEquals(SafetyStatus.WARNING, result.status)
        assertTrue(result.warnings.any { it.contains("keywords") })
    }

    // ==================== Edge Cases ====================

    @Test
    fun `checkUrl handles empty URL`() {
        val result = checker.checkUrl("")
        assertEquals(SafetyStatus.SAFE, result.status)
    }

    @Test
    fun `checkUrl handles URL without protocol`() {
        val result = checker.checkUrl("example.com")
        assertEquals(SafetyStatus.SAFE, result.status)
    }

    @Test
    fun `checkUrl preserves original URL in result`() {
        val originalUrl = "https://example.com/path"
        val result = checker.checkUrl(originalUrl)
        assertEquals(originalUrl, result.originalUrl)
    }

    // ==================== Result Properties Tests ====================

    @Test
    fun `UrlSafetyResult isSafe property works correctly`() {
        val safeResult = UrlSafetyResult(SafetyStatus.SAFE, emptyList(), "https://example.com")
        assertTrue(safeResult.isSafe)
        assertFalse(safeResult.isWarning)
        assertFalse(safeResult.isDangerous)
    }

    @Test
    fun `UrlSafetyResult isWarning property works correctly`() {
        val warningResult = UrlSafetyResult(SafetyStatus.WARNING, listOf("warning"), "https://bit.ly/abc")
        assertFalse(warningResult.isSafe)
        assertTrue(warningResult.isWarning)
        assertFalse(warningResult.isDangerous)
    }

    @Test
    fun `UrlSafetyResult isDangerous property works correctly`() {
        val dangerousResult = UrlSafetyResult(SafetyStatus.DANGEROUS, listOf("dangerous"), "https://example.com/file.exe")
        assertFalse(dangerousResult.isSafe)
        assertFalse(dangerousResult.isWarning)
        assertTrue(dangerousResult.isDangerous)
    }
}
