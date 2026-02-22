package cut.the.crap.qreverywhere.shared.domain.model

/**
 * Protocol prefix constants for QR code content types
 * Used to identify and construct URI schemes
 */
object ProtocolPrefix {
    const val TEL = "tel:"
    const val MAILTO = "mailto:"
    const val HTTP = "http:"
    const val HTTPS = "https:"
    const val SMS = "sms:"
    const val SMSTO = "smsto:"
}
