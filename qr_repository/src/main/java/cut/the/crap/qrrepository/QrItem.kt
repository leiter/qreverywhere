package cut.the.crap.qrrepository

import android.graphics.Bitmap

data class QrItem(
    val id: Int,
    val img: Bitmap,
    val timestamp: Long,
    val textContent: String,
)