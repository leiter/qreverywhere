package cut.the.crap.qreverywhere.shared.domain.model

/**
 * Geographic location data for geo: URI QR codes
 *
 * Follows the geo: URI scheme (RFC 5870):
 * ```
 * geo:latitude,longitude
 * geo:latitude,longitude;u=uncertainty
 * geo:latitude,longitude?q=label
 * ```
 *
 * Examples:
 * - geo:37.7749,-122.4194 (San Francisco)
 * - geo:48.8584,2.2945?q=Eiffel%20Tower
 */
data class GeoLocation(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
    val uncertainty: Double? = null,
    val label: String? = null
) {
    init {
        require(latitude in -90.0..90.0) { "Latitude must be between -90 and 90" }
        require(longitude in -180.0..180.0) { "Longitude must be between -180 and 180" }
    }

    /**
     * Convert to geo: URI format for QR code
     */
    fun toGeoUri(): String {
        return buildString {
            append("geo:")
            append(formatCoordinate(latitude))
            append(",")
            append(formatCoordinate(longitude))

            altitude?.let {
                append(",")
                append(formatCoordinate(it))
            }

            // Add parameters
            val params = mutableListOf<String>()
            uncertainty?.let { params.add("u=${formatCoordinate(it)}") }

            if (params.isNotEmpty()) {
                append(";")
                append(params.joinToString(";"))
            }

            // Add query label
            label?.let {
                append("?q=")
                append(encodeLabel(it))
            }
        }
    }

    /**
     * Format coordinate with appropriate precision
     */
    private fun formatCoordinate(value: Double): String {
        // Use 6 decimal places (approximately 0.1 meter precision)
        // KMP-compatible: avoid String.format which is JVM-only
        val isNegative = value < 0
        val abs = if (isNegative) -value else value
        val intPart = abs.toLong()
        val fracPart = ((abs - intPart) * 1_000_000 + 0.5).toLong()
        val fracStr = fracPart.toString().padStart(6, '0').take(6)
        return "${if (isNegative) "-" else ""}${intPart}.${fracStr}"
    }

    /**
     * URL-encode the label for the query string
     */
    private fun encodeLabel(text: String): String {
        return text
            .replace("%", "%25")
            .replace(" ", "%20")
            .replace("&", "%26")
            .replace("=", "%3D")
            .replace("?", "%3F")
            .replace("#", "%23")
    }

    /**
     * Get a display-friendly string
     */
    fun toDisplayString(): String {
        val lat = if (latitude >= 0) "${formatCoordinate(latitude).trimEnd('0').trimEnd('.')}°N"
                  else "${formatCoordinate(-latitude).trimEnd('0').trimEnd('.')}°S"
        val lon = if (longitude >= 0) "${formatCoordinate(longitude).trimEnd('0').trimEnd('.')}°E"
                  else "${formatCoordinate(-longitude).trimEnd('0').trimEnd('.')}°W"

        return label?.let { "$it ($lat, $lon)" } ?: "$lat, $lon"
    }

    companion object {
        /**
         * Parse a geo: URI into GeoLocation
         * @param geoUri The raw geo: URI string
         * @return Parsed GeoLocation or null if parsing fails
         */
        fun parse(geoUri: String): GeoLocation? {
            if (!geoUri.startsWith("geo:")) {
                return null
            }

            try {
                val content = geoUri.removePrefix("geo:")

                // Split into coordinates part and query part
                val (coordPart, queryPart) = if (content.contains("?")) {
                    content.substringBefore("?") to content.substringAfter("?")
                } else {
                    content to null
                }

                // Parse coordinates and parameters
                val coordAndParams = coordPart.split(";")
                val coords = coordAndParams[0].split(",")

                if (coords.size < 2) return null

                val latitude = coords[0].toDoubleOrNull() ?: return null
                val longitude = coords[1].toDoubleOrNull() ?: return null
                val altitude = if (coords.size >= 3) coords[2].toDoubleOrNull() else null

                // Parse parameters
                var uncertainty: Double? = null
                for (i in 1 until coordAndParams.size) {
                    val param = coordAndParams[i]
                    when {
                        param.startsWith("u=") -> {
                            uncertainty = param.substringAfter("u=").toDoubleOrNull()
                        }
                    }
                }

                // Parse query for label
                var label: String? = null
                queryPart?.let {
                    for (param in it.split("&")) {
                        when {
                            param.startsWith("q=") -> {
                                label = decodeLabel(param.substringAfter("q="))
                            }
                        }
                    }
                }

                return GeoLocation(
                    latitude = latitude,
                    longitude = longitude,
                    altitude = altitude,
                    uncertainty = uncertainty,
                    label = label
                )
            } catch (e: Exception) {
                return null
            }
        }

        /**
         * URL-decode the label from the query string
         */
        private fun decodeLabel(encoded: String): String {
            return encoded
                .replace("%20", " ")
                .replace("%26", "&")
                .replace("%3D", "=")
                .replace("%3F", "?")
                .replace("%23", "#")
                .replace("%25", "%")
        }

        /**
         * Check if a string is a valid geo: URI
         */
        fun isGeoUri(text: String): Boolean {
            return text.startsWith("geo:") && parse(text) != null
        }
    }
}
