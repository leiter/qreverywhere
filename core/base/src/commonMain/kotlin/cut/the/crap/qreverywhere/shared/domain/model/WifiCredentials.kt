package cut.the.crap.qreverywhere.shared.domain.model

/**
 * Parsed WiFi credentials from a WIFI: QR code format
 *
 * WiFi QR code format: WIFI:T:<security>;S:<ssid>;P:<password>;H:<hidden>;;
 * - T = Type (WPA, WEP, nopass, etc.)
 * - S = SSID (network name)
 * - P = Password
 * - H = Hidden (true/false, optional)
 */
data class WifiCredentials(
    val ssid: String,
    val password: String,
    val securityType: WifiSecurityType,
    val isHidden: Boolean = false
) {
    companion object {
        /**
         * Parse a WIFI: string into WifiCredentials
         * @param wifiString The raw WIFI: QR code content
         * @return Parsed WifiCredentials or null if parsing fails
         */
        fun parse(wifiString: String): WifiCredentials? {
            if (!wifiString.startsWith("WIFI:")) {
                return null
            }

            val content = wifiString.removePrefix("WIFI:")

            // Parse fields
            val fields = mutableMapOf<Char, String>()
            var currentKey: Char? = null
            var currentValue = StringBuilder()
            var i = 0

            while (i < content.length) {
                val char = content[i]

                when {
                    // Check for field start pattern (X:)
                    i + 1 < content.length && content[i + 1] == ':' && char.isLetter() -> {
                        // Save previous field if exists
                        currentKey?.let { key ->
                            fields[key] = currentValue.toString()
                        }
                        currentKey = char.uppercaseChar()
                        currentValue = StringBuilder()
                        i += 2 // Skip past "X:"
                    }
                    // End of string marker
                    char == ';' && (i + 1 >= content.length || content[i + 1] == ';') -> {
                        currentKey?.let { key ->
                            fields[key] = currentValue.toString()
                        }
                        break
                    }
                    // Field separator
                    char == ';' -> {
                        currentKey?.let { key ->
                            fields[key] = currentValue.toString()
                        }
                        currentKey = null
                        currentValue = StringBuilder()
                        i++
                    }
                    // Handle escaped characters
                    char == '\\' && i + 1 < content.length -> {
                        currentValue.append(content[i + 1])
                        i += 2
                    }
                    else -> {
                        currentValue.append(char)
                        i++
                    }
                }
            }

            // Ensure SSID exists
            val ssid = fields['S'] ?: return null

            // Get security type
            val securityType = when (fields['T']?.uppercase()) {
                "WPA", "WPA2", "WPA3", "WPA2-EAP", "WPA3-EAP" -> WifiSecurityType.WPA
                "WEP" -> WifiSecurityType.WEP
                "NOPASS", "", null -> WifiSecurityType.OPEN
                else -> WifiSecurityType.WPA // Default to WPA for unknown
            }

            // Get password (empty for open networks)
            val password = fields['P'] ?: ""

            // Get hidden status
            val isHidden = fields['H']?.lowercase() == "true"

            return WifiCredentials(
                ssid = ssid,
                password = password,
                securityType = securityType,
                isHidden = isHidden
            )
        }
    }

    /**
     * Convert back to WIFI: QR code format
     */
    fun toWifiString(): String {
        return buildString {
            append("WIFI:")
            append("T:${securityType.code};")
            append("S:${escapeWifiField(ssid)};")
            if (securityType != WifiSecurityType.OPEN && password.isNotEmpty()) {
                append("P:${escapeWifiField(password)};")
            }
            if (isHidden) {
                append("H:true;")
            }
            append(";")
        }
    }

    /**
     * Get a masked version of the password for display
     */
    fun getMaskedPassword(): String {
        return if (password.isEmpty()) {
            "(No password)"
        } else {
            "*".repeat(password.length.coerceAtMost(12))
        }
    }

    private fun escapeWifiField(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace(";", "\\;")
            .replace(",", "\\,")
            .replace(":", "\\:")
    }
}

/**
 * WiFi security types
 */
enum class WifiSecurityType(val code: String, val displayName: String) {
    OPEN("nopass", "Open (No Password)"),
    WEP("WEP", "WEP"),
    WPA("WPA", "WPA/WPA2/WPA3");

    companion object {
        fun fromCode(code: String): WifiSecurityType {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: WPA
        }
    }
}
