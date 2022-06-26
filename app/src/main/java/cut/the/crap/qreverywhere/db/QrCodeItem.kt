package cut.the.crap.qreverywhere.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import cut.the.crap.qreverywhere.data.QrItem

@Entity(tableName = "qrcode_history")
data class QrCodeItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val img: Bitmap,
    val timestamp: Long = System.currentTimeMillis(),
    val textContent: String,

//    var textContent: String = "",
// type (email,tel,... ) via extensionfunction
//    var avgSpeedInKMH: Float = 0f,
//    var distanceInMeters: Int = 0,
//    var timeInMillis: Long = 0,
//    var caloriesBurned: Int = 0
) {

}

fun QrCodeItem.toQrItem() : QrItem {
    return QrItem(
        id = id,
        img = img,
        timestamp = timestamp,
        textContent = textContent
    )
}