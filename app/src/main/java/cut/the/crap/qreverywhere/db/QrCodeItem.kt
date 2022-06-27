package cut.the.crap.qreverywhere.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import cut.the.crap.qreverywhere.data.QrItem
import cut.the.crap.qreverywhere.stuff.Acquire

@Entity(tableName = "qrcode_history")
data class QrCodeItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val img: Bitmap = Bitmap.createBitmap(0,0,Bitmap.Config.ARGB_8888),
    val timestamp: Long = System.currentTimeMillis(),
    val textContent: String = "",
    @Acquire.Type
    val acquireType: Int = Acquire.SCANNED
)

fun QrCodeItem.toQrItem() : QrItem {
    return QrItem(
        id = id,
        img = img,
        timestamp = timestamp,
        textContent = textContent
    )
}