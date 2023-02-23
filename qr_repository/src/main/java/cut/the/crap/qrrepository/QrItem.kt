package cut.the.crap.qrrepository

import android.graphics.Bitmap
import cut.the.crap.qrrepository.db.QrCodeDbItem

data class QrItem(
    val id: Int,
    val img: Bitmap,
    val timestamp: Long,
    val textContent: String,
    @Acquire.Type
    val acquireType: Int = Acquire.EMPTY_DEFAULT,
    val fileUriString: String = "",
)

fun QrItem.toQrCodeDbItem() : QrCodeDbItem {
    return QrCodeDbItem(
        this.id,
        this.img,
        this.timestamp,
        this.textContent,
        this.acquireType,
        this.fileUriString
    )
}