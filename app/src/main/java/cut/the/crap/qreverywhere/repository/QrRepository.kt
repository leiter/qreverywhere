package cut.the.crap.qreverywhere.repository

import android.graphics.Bitmap
import android.net.Uri
import cut.the.crap.qreverywhere.data.State

interface QrRepository {

    fun scanQrImage(uri: Uri) : State<String>

    fun createQrCode(content: String) : State<Bitmap>

    fun saveQrCode(myBitmap: Bitmap) : State<String>

}

