package cut.the.crap.qreverywhere.create.qrcode

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Environment
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModel
import com.google.zxing.*
import com.google.zxing.common.BitMatrix
import cut.the.crap.qreverywhere.R
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.*
import java.util.*
import javax.inject.Inject
import kotlin.IllegalArgumentException
import kotlin.IntArray
import kotlin.String
import kotlin.Throws
import kotlin.arrayOf


@HiltViewModel
class CreateQrCodeViewModel @Inject constructor() : ViewModel() {


    fun saveImage(myBitmap: Bitmap, context: Context): String? {
        val bytes = ByteArrayOutputStream()
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
        val directory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            IMAGE_DIRECTORY
        )

        if (!directory.exists()) {
            directory.mkdirs()
        }
        try {
            val f = File(
                directory, Calendar.getInstance()
                    .timeInMillis.toString() + ".jpg"
            )
            f.createNewFile() // todo give read write permission
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            MediaScannerConnection.scanFile(context, arrayOf(f.getPath()), arrayOf("image/jpeg"), null)
            fo.close()
            Log.d("TAG", "File Saved::--->" + f.absolutePath)
            return f.absolutePath
        } catch (e1: IOException) {
            e1.printStackTrace()
        }
        return ""
    }

    @Throws(WriterException::class)
    fun textToImageEncode(Value: String, resources: Resources): Bitmap? {
        // todo 2953  chars are fine

        val bitMatrix: BitMatrix = try {
            MultiFormatWriter().encode(
                Value,
                BarcodeFormat.QR_CODE,
                QRcodeWidth, QRcodeWidth, null
            )
        } catch (e: IllegalArgumentException) {
            return null
        }
        val bitMatrixWidth = bitMatrix.width
        val bitMatrixHeight = bitMatrix.height
        val pixels = IntArray(bitMatrixWidth * bitMatrixHeight)
        for (y in 0 until bitMatrixHeight) {
            val offset = y * bitMatrixWidth
            for (x in 0 until bitMatrixWidth) {
                pixels[offset + x] =
                    if (bitMatrix[x, y]) ResourcesCompat.getColor(resources, R.color.black, null)
                    else ResourcesCompat.getColor(resources, R.color.white,null)
            }
        }
        val bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight)
        return bitmap
    }

    companion object {
        const val QRcodeWidth = 500 // should be calculated
        const val IMAGE_DIRECTORY = "QrEveryWhere";
    }


}