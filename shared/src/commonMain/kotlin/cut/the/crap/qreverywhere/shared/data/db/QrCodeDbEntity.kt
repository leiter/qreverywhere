package cut.the.crap.qreverywhere.shared.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import cut.the.crap.qreverywhere.shared.domain.model.AcquireType
import cut.the.crap.qreverywhere.shared.domain.model.QrItem
import kotlinx.datetime.Instant

/**
 * Room Entity for QR code history - cross-platform compatible
 * Uses ByteArray for image storage instead of Android Bitmap
 */
@Entity(tableName = "qrcode_history")
data class QrCodeDbEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val textContent: String = "",
    val acquireType: Int = AcquireType.EMPTY_DEFAULT.ordinal,
    val timestamp: Long = 0L,
    val imageData: ByteArray? = null,
    val fileUriString: String = ""
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as QrCodeDbEntity

        if (id != other.id) return false
        if (textContent != other.textContent) return false
        if (acquireType != other.acquireType) return false
        if (timestamp != other.timestamp) return false
        if (imageData != null) {
            if (other.imageData == null) return false
            if (!imageData.contentEquals(other.imageData)) return false
        } else if (other.imageData != null) return false
        if (fileUriString != other.fileUriString) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + textContent.hashCode()
        result = 31 * result + acquireType
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + (imageData?.contentHashCode() ?: 0)
        result = 31 * result + fileUriString.hashCode()
        return result
    }
}

/**
 * Convert database entity to domain model
 */
fun QrCodeDbEntity.toQrItem(): QrItem {
    return QrItem(
        id = this.id,
        textContent = this.textContent,
        acquireType = AcquireType.entries.getOrElse(this.acquireType) { AcquireType.EMPTY_DEFAULT },
        timestamp = Instant.fromEpochMilliseconds(this.timestamp),
        imageData = this.imageData
    )
}

/**
 * Convert domain model to database entity
 */
fun QrItem.toDbEntity(): QrCodeDbEntity {
    return QrCodeDbEntity(
        id = this.id,
        textContent = this.textContent,
        acquireType = this.acquireType.ordinal,
        timestamp = this.timestamp.toEpochMilliseconds(),
        imageData = this.imageData,
        fileUriString = ""
    )
}
