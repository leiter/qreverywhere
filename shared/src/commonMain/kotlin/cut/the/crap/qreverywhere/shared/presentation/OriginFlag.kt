package cut.the.crap.qreverywhere.shared.presentation

/**
 * Origin flag constants to track where a QR code view was initiated from
 * Used for navigation and UI state management
 */
object OriginFlag {
    const val KEY = "originFlag"
    const val FROM_SCAN_QR = 0
    const val FROM_CREATE_CONTEXT = 1
    const val FROM_HISTORY_LIST = 2
}
