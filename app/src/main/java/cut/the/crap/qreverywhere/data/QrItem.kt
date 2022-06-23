package cut.the.crap.qreverywhere.data

import android.graphics.Bitmap

data class QrItem(
    val id: Int,
    val img: Bitmap,
    val timestamp: Long,
    val textContent: String,
)