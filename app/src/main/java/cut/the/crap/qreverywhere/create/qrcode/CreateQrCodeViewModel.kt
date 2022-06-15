package cut.the.crap.qreverywhere.create.qrcode

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.os.Environment
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModel
import com.google.zxing.*
import com.google.zxing.Reader
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import cut.the.crap.qreverywhere.R
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.*
import java.util.*
import javax.inject.Inject
import kotlin.Exception
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
        val wallpaperDirectory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), IMAGE_DIRECTORY
        )
        // have the object build the directory structure, if needed.
        if (!wallpaperDirectory.exists()) {
            Log.d("dirrrrrr", "" + wallpaperDirectory.mkdirs())
            wallpaperDirectory.mkdirs()
        }
        try {
            val f = File(
                wallpaperDirectory, Calendar.getInstance()
                    .timeInMillis.toString() + ".jpg"
            )
            f.createNewFile() //give read write permission
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


    fun loadBitmap(file: File) : Bitmap {
        val inputStream = BufferedInputStream(FileInputStream(file))
        return BitmapFactory.decodeStream(inputStream)
    }

    fun scanQrImage(sourceBitmap: Bitmap): String? {
        var contents: String? = null

        val intArray = IntArray(sourceBitmap.width * sourceBitmap.height)
        //copy pixel data from the Bitmap into the 'intArray' array
        //copy pixel data from the Bitmap into the 'intArray' array
        sourceBitmap.getPixels(intArray, 0, sourceBitmap.width, 0, 0, sourceBitmap.width, sourceBitmap.height)

        val source: LuminanceSource =
            RGBLuminanceSource(sourceBitmap.width, sourceBitmap.height, intArray)
        val bitmap = BinaryBitmap(HybridBinarizer(source))

        val reader: Reader = MultiFormatReader()
        try {
            val result: Result = reader.decode(bitmap)
            contents = result.text
        } catch (e: Exception) {
            Log.e("QrTest", "Error decoding barcode", e)
        }
        return contents
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
        } catch (Illegalargumentexception: IllegalArgumentException) {
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
        const val QRcodeWidth = 500
        const val IMAGE_DIRECTORY = "/QRcodeDemonuts";
    }


}