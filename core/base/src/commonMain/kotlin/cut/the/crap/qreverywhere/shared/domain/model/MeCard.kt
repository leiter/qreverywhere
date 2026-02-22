package cut.the.crap.qreverywhere.shared.domain.model

/**
 * MeCard contact data format (simpler alternative to vCard)
 *
 * Format: MECARD:N:Name;TEL:Phone;EMAIL:Email;ADR:Address;NOTE:Note;URL:Website;;
 *
 * MeCard is commonly used in Japan and is simpler than vCard.
 * It's widely supported by QR code readers.
 *
 * Example:
 * MECARD:N:John Doe;TEL:+1234567890;EMAIL:john@example.com;ADR:123 Main St;;
 */
data class MeCard(
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val organization: String? = null,
    val note: String? = null,
    val url: String? = null,
    val birthday: String? = null // Format: YYYYMMDD
) {
    /**
     * Convert to MECARD QR code format
     */
    fun toMeCard(): String {
        return buildString {
            append("MECARD:")
            append("N:${escapeMeCardField(name)};")
            phone?.let { append("TEL:${escapeMeCardField(it)};") }
            email?.let { append("EMAIL:${escapeMeCardField(it)};") }
            address?.let { append("ADR:${escapeMeCardField(it)};") }
            organization?.let { append("ORG:${escapeMeCardField(it)};") }
            note?.let { append("NOTE:${escapeMeCardField(it)};") }
            url?.let { append("URL:${escapeMeCardField(it)};") }
            birthday?.let { append("BDAY:$it;") }
            append(";")
        }
    }

    /**
     * Escape special characters in MeCard fields
     */
    private fun escapeMeCardField(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace(";", "\\;")
            .replace(":", "\\:")
    }

    /**
     * Get the display name
     */
    fun getDisplayName(): String = name

    /**
     * Check if this MeCard has any contact details
     */
    fun hasContactDetails(): Boolean {
        return phone != null || email != null
    }

    companion object {
        /**
         * Parse a MECARD string into MeCard
         * @param meCardString The raw MECARD QR code content
         * @return Parsed MeCard or null if parsing fails
         */
        fun parse(meCardString: String): MeCard? {
            if (!meCardString.startsWith("MECARD:")) {
                return null
            }

            val content = meCardString.removePrefix("MECARD:")

            // Parse fields
            val fields = mutableMapOf<String, String>()
            var currentKey: String? = null
            var currentValue = StringBuilder()
            var i = 0

            while (i < content.length) {
                val char = content[i]

                when {
                    // Handle escaped characters
                    char == '\\' && i + 1 < content.length -> {
                        currentValue.append(content[i + 1])
                        i += 2
                    }
                    // End of field
                    char == ';' -> {
                        currentKey?.let { key ->
                            fields[key] = currentValue.toString()
                        }
                        currentKey = null
                        currentValue = StringBuilder()
                        i++
                    }
                    // Field separator
                    char == ':' && currentKey == null -> {
                        currentKey = currentValue.toString()
                        currentValue = StringBuilder()
                        i++
                    }
                    else -> {
                        currentValue.append(char)
                        i++
                    }
                }
            }

            // Name is required
            val name = fields["N"] ?: return null

            return MeCard(
                name = name,
                phone = fields["TEL"],
                email = fields["EMAIL"],
                address = fields["ADR"],
                organization = fields["ORG"],
                note = fields["NOTE"],
                url = fields["URL"],
                birthday = fields["BDAY"]
            )
        }

        /**
         * Check if a string is a MeCard format
         */
        fun isMeCard(text: String): Boolean {
            return text.startsWith("MECARD:")
        }

        /**
         * Create a simple MeCard with just name and phone
         */
        fun simple(name: String, phone: String? = null, email: String? = null): MeCard {
            return MeCard(
                name = name,
                phone = phone,
                email = email
            )
        }
    }
}
