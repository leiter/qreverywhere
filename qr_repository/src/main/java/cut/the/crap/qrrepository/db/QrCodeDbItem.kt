package cut.the.crap.qrrepository.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import cut.the.crap.qrrepository.Acquire
import cut.the.crap.qrrepository.QrItem

@Entity(tableName = "qrcode_history")
data class QrCodeDbItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val img: Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888),
    val timestamp: Long = System.currentTimeMillis(),
    val textContent: String = "",
    @Acquire.Type
    val acquireType: Int = Acquire.EMPTY_DEFAULT,
    val fileUriString: String = "",
)

fun QrCodeDbItem.toItem() : QrItem {
    return QrItem(
        this.id,
        this.img,
        this.timestamp,
        this.textContent,
        this.acquireType,
        this.fileUriString
    )
}