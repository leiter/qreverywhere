package cut.the.crap.qreverywhere.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import cut.the.crap.qreverywhere.utils.Acquire

@Entity(tableName = "qrcode_history")
data class QrCodeItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val img: Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888),
    val timestamp: Long = System.currentTimeMillis(),
    val textContent: String = "",
    @Acquire.Type
    val acquireType: Int = Acquire.EMPTY_DEFAULT,
    val fileUriString: String = "",
)