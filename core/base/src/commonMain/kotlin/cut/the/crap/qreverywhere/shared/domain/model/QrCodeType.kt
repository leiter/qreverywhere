package cut.the.crap.qreverywhere.shared.domain.model

/**
 * QR code content type constants
 * Used to categorize QR code content based on its format/protocol
 */
object QrCodeType {
    const val EMAIL = 0
    const val PHONE = 1
    const val WEB_URL = 2
    const val SMS = 3
    const val CONTACT = 4
    const val WIFI = 5
    const val CALENDAR = 6
    const val LOCATION = 7
    const val MECARD = 8
    const val UNKNOWN_CONTENT = 999
}
