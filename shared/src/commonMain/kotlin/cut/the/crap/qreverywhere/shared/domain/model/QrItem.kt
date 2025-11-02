package cut.the.crap.qreverywhere.shared.domain.model

import kotlinx.datetime.Instant

/**
 * Common QR code item model for KMP
 * This replaces the Android-specific QrItem
 */
class QrItem(
    val id: Int = 0,
    val textContent: String,
    val acquireType: AcquireType,
    val timestamp: Instant,
    val imageData: ByteArray? = null
)

enum class AcquireType {
    SCANNED,
    CREATED,
    FROM_FILE,
    ERROR_OCCURRED,
    EMPTY_DEFAULT
}