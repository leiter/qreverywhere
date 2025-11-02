package cut.the.crap.qreverywhere.shared.domain.model

import kotlinx.datetime.Instant

/**
 * Common QR code item model for KMP
 * This replaces the Android-specific QrItem
 */
data class QrItem(
    val id: Int = 0,
    val textContent: String,
    val acquireType: AcquireType,
    val timestamp: Instant,
    val imageData: ByteArray? = null
) {
    // Custom equals and hashCode to handle ByteArray properly
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as QrItem

        if (id != other.id) return false
        if (textContent != other.textContent) return false
        if (acquireType != other.acquireType) return false
        if (timestamp != other.timestamp) return false
        if (imageData != null) {
            if (other.imageData == null) return false
            if (!imageData.contentEquals(other.imageData)) return false
        } else if (other.imageData != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + textContent.hashCode()
        result = 31 * result + acquireType.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + (imageData?.contentHashCode() ?: 0)
        return result
    }
}

enum class AcquireType {
    SCANNED,
    CREATED,
    FROM_FILE,
    ERROR_OCCURRED,
    EMPTY_DEFAULT
}