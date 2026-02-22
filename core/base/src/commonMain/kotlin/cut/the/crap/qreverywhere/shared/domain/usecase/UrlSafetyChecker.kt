package cut.the.crap.qreverywhere.shared.domain.usecase

/**
 * Utility for checking URL safety and detecting potentially malicious links
 * Helps protect users from phishing and malware distribution via QR codes
 */
class UrlSafetyChecker {

    /**
     * Check a URL for potential safety issues
     * @param url The URL to check
     * @return UrlSafetyResult indicating safety status and any warnings
     */
    fun checkUrl(url: String): UrlSafetyResult {
        val normalizedUrl = url.lowercase().trim()

        // Check for dangerous file extensions
        if (hasDangerousExtension(normalizedUrl)) {
            return UrlSafetyResult(
                status = SafetyStatus.DANGEROUS,
                warnings = listOf("This link appears to download an executable file"),
                originalUrl = url
            )
        }

        // Check for potential phishing patterns
        val phishingWarnings = checkPhishingPatterns(normalizedUrl)
        if (phishingWarnings.isNotEmpty()) {
            return UrlSafetyResult(
                status = SafetyStatus.WARNING,
                warnings = phishingWarnings,
                originalUrl = url
            )
        }

        // Check for URL shorteners (not dangerous, but worth noting)
        if (isUrlShortener(normalizedUrl)) {
            return UrlSafetyResult(
                status = SafetyStatus.WARNING,
                warnings = listOf("This is a shortened URL - the destination cannot be verified"),
                originalUrl = url
            )
        }

        return UrlSafetyResult(
            status = SafetyStatus.SAFE,
            warnings = emptyList(),
            originalUrl = url
        )
    }

    /**
     * Check if URL has dangerous file extensions that could execute malware
     */
    private fun hasDangerousExtension(url: String): Boolean {
        val dangerousExtensions = listOf(
            ".exe", ".bat", ".cmd", ".scr", ".pif",
            ".msi", ".msp", ".hta", ".cpl", ".msc", ".jar",
            ".vbs", ".vbe", ".jse", ".ws", ".wsf",
            ".ps1", ".ps1xml", ".ps2", ".ps2xml", ".psc1", ".psc2",
            ".scf", ".lnk", ".inf", ".reg",
            ".dll", ".sys", ".drv",
            ".app", ".dmg", ".pkg" // macOS
        )
        // Note: .com and .js are excluded as they can be valid domain TLDs or safe file types

        // Extract path from URL (part after :// and before ? or #)
        val pathStart = url.indexOf("://")
        val path = if (pathStart >= 0) {
            val afterProtocol = url.substring(pathStart + 3)
            val pathOnlyStart = afterProtocol.indexOf("/")
            if (pathOnlyStart >= 0) {
                afterProtocol.substring(pathOnlyStart)
                    .substringBefore("?")
                    .substringBefore("#")
            } else {
                ""
            }
        } else {
            url.substringBefore("?").substringBefore("#")
        }

        // Check if the path ends with a dangerous extension
        return dangerousExtensions.any { ext ->
            path.endsWith(ext, ignoreCase = true)
        }
    }

    /**
     * Check for common phishing URL patterns
     */
    private fun checkPhishingPatterns(url: String): List<String> {
        val warnings = mutableListOf<String>()

        // Check for @ symbol before domain (userinfo in URL - often used in phishing)
        // Pattern: http://legitimate.com@malicious.com
        if (url.contains("@") && !url.startsWith("mailto:")) {
            val atIndex = url.indexOf("@")
            val protocolEnd = url.indexOf("://")
            if (protocolEnd >= 0 && atIndex > protocolEnd) {
                warnings.add("URL contains suspicious @ symbol that may hide the real destination")
            }
        }

        // Check for IP address instead of domain name
        if (containsIpAddress(url)) {
            warnings.add("URL uses IP address instead of domain name")
        }

        // Check for excessive subdomains (often used in phishing)
        val subdomainCount = countSubdomains(url)
        if (subdomainCount > 3) {
            warnings.add("URL has an unusually complex domain structure")
        }

        // Check for suspicious keywords in URL
        val suspiciousKeywords = listOf(
            "login", "signin", "verify", "secure", "account",
            "update", "confirm", "banking", "password", "credential"
        )
        val foundKeywords = suspiciousKeywords.filter { url.contains(it) }
        if (foundKeywords.size >= 2) {
            warnings.add("URL contains multiple security-related keywords")
        }

        // Check for homograph attacks (mixing characters from different scripts)
        if (containsMixedScripts(url)) {
            warnings.add("URL may contain misleading characters from different alphabets")
        }

        return warnings
    }

    /**
     * Check if URL is a known URL shortener
     */
    private fun isUrlShortener(url: String): Boolean {
        val shorteners = listOf(
            "bit.ly", "tinyurl.com", "goo.gl", "t.co", "ow.ly",
            "is.gd", "buff.ly", "adf.ly", "tiny.cc", "lnkd.in",
            "db.tt", "qr.ae", "j.mp", "bitly.com", "cutt.ly",
            "rb.gy", "shorturl.at", "s.id", "clck.ru"
        )

        return shorteners.any { shortener ->
            url.contains(shortener)
        }
    }

    /**
     * Check if URL contains an IP address
     */
    private fun containsIpAddress(url: String): Boolean {
        // Match IPv4 pattern after :// and before next / or end
        val ipv4Pattern = Regex("""://(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})""")
        return ipv4Pattern.containsMatchIn(url)
    }

    /**
     * Count subdomains in URL
     */
    private fun countSubdomains(url: String): Int {
        val domainMatch = Regex("""://([^/:]+)""").find(url) ?: return 0
        val domain = domainMatch.groupValues[1]
        return domain.count { it == '.' }
    }

    /**
     * Check for potential homograph attacks using mixed character scripts
     */
    private fun containsMixedScripts(url: String): Boolean {
        // Extract domain from URL
        val domainMatch = Regex("""://([^/:]+)""").find(url) ?: return false
        val domain = domainMatch.groupValues[1]

        // Check for non-ASCII characters in domain (simplified check)
        // Full homograph detection would require Unicode script analysis
        return domain.any { char ->
            val code = char.code
            // Check for Cyrillic, Greek, or other scripts that have Latin lookalikes
            code in 0x0400..0x04FF || // Cyrillic
            code in 0x0370..0x03FF || // Greek
            code in 0x2000..0x206F    // General punctuation lookalikes
        }
    }
}

/**
 * Result of URL safety check
 */
data class UrlSafetyResult(
    val status: SafetyStatus,
    val warnings: List<String>,
    val originalUrl: String
) {
    val isSafe: Boolean get() = status == SafetyStatus.SAFE
    val isWarning: Boolean get() = status == SafetyStatus.WARNING
    val isDangerous: Boolean get() = status == SafetyStatus.DANGEROUS
}

/**
 * Safety status levels for URLs
 */
enum class SafetyStatus {
    SAFE,       // No issues detected
    WARNING,    // Potential issues, user should be cautious
    DANGEROUS   // High likelihood of malicious content
}
