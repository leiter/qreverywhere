package cut.the.crap.qreverywhere.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import cut.the.crap.qreverywhere.data.State
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class QrRepositoryImpl @Inject constructor(@ApplicationContext val context: Context) :
    QrRepository {

    override fun scanQrImage(uri: Uri): State<String> {

        val inputStream = context.contentResolver.openInputStream(uri)
        val sourceBitmap: Bitmap = BitmapFactory.decodeStream(inputStream)

        val intArray = IntArray(sourceBitmap.width * sourceBitmap.height)
        sourceBitmap.getPixels(intArray, 0, sourceBitmap.width, 0, 0, sourceBitmap.width, sourceBitmap.height)

        val source: LuminanceSource =
            RGBLuminanceSource(sourceBitmap.width, sourceBitmap.height, intArray)
        val bitmap = BinaryBitmap(HybridBinarizer(source))

        val reader: Reader = MultiFormatReader()
        return try {
            val result: Result = reader.decode(bitmap)
            State.success(result.text)
        } catch (e: Exception) {
            Log.e("QrTest", "Error decoding barcode", e)
            State.error(error = e)
        }

    }

    override fun createQrCode(content: String): State<Bitmap> {
        TODO("Not yet implemented")
    }

    override fun saveQrCode(myBitmap: Bitmap): State<String> {
        TODO("Not yet implemented")
    }

}