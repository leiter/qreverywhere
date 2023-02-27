package cut.the.crap.qreverywhere.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import androidx.annotation.IntDef
import androidx.core.content.res.ResourcesCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.LuminanceSource
import com.google.zxing.MultiFormatReader
import com.google.zxing.MultiFormatWriter
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.Reader
import com.google.zxing.Result
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.utils.ProtocolPrefix.HTTP
import cut.the.crap.qreverywhere.utils.ProtocolPrefix.HTTPS
import cut.the.crap.qreverywhere.utils.ProtocolPrefix.MAILTO
import cut.the.crap.qreverywhere.utils.ProtocolPrefix.TEL
import cut.the.crap.qreverywhere.utils.QrCodeType.CONTACT
import cut.the.crap.qreverywhere.utils.QrCodeType.EMAIL
import cut.the.crap.qreverywhere.utils.QrCodeType.PHONE
import cut.the.crap.qreverywhere.utils.QrCodeType.UNKNOWN_CONTENT
import cut.the.crap.qreverywhere.utils.QrCodeType.WEB_URL
import cut.the.crap.qrrepository.QrItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Calendar

const val QRcodeWidth = 500 // todo should be calculated  / set stride also when ready
const val IMAGE_DIRECTORY = "QrEveryWhere"

object QrCodeType {
    const val EMAIL = 0
    const val PHONE = 1
    const val WEB_URL = 2
    const val SMS = 3
    const val CONTACT = 4
    const val UNKNOWN_CONTENT = 999

    @IntDef(EMAIL, PHONE, WEB_URL, CONTACT, UNKNOWN_CONTENT)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Type
}

object ProtocolPrefix {
    const val TEL = "tel:"
    const val MAILTO = "mailto:"
    const val HTTP = "http:"
    const val HTTPS = "https:"
    const val SMS = "sms:"
    const val SMSTO = "smsto:"
}

@QrCodeType.Type
fun determineType(contentString: String): Int {
    val decoded = Uri.decode(contentString)
    return when {
        decoded.startsWith(TEL) -> PHONE
        decoded.startsWith(MAILTO) -> EMAIL
        decoded.startsWith(HTTP) -> WEB_URL
        decoded.startsWith(HTTPS) -> WEB_URL
        isVcard(decoded) -> CONTACT
        decoded.startsWith(ProtocolPrefix.SMS) -> QrCodeType.SMS
        decoded.startsWith(ProtocolPrefix.SMSTO) -> QrCodeType.SMS
        else -> UNKNOWN_CONTENT
    }
}

fun isVcard(contentString: String): Boolean {
    return contentString.startsWith("BEGIN:VCARD") && contentString.endsWith("END:VCARD\n")
}

suspend fun saveImageToFile(qrCodeItem: QrItem, context: Context): String {
    return withContext(Dispatchers.IO) {

        val bytes = ByteArrayOutputStream()
        qrCodeItem.img.compress(CompressFormat.JPEG, 90, bytes)
        val directory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            IMAGE_DIRECTORY
        )

        if (!directory.exists()) {
            directory.mkdirs()
        }
        try {
            val f = File( //todo better naming
                directory, Calendar.getInstance().timeInMillis.toString() + ".jpg"
            )
            f.createNewFile() // todo give read write permission
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            MediaScannerConnection.scanFile(
                context,
                arrayOf(f.path),
                arrayOf("image/jpeg")
            ) { path, uri ->
                val updateQritem = qrCodeItem.copy(fileUriString = uri.toString())
                Timber.e("MediaScanner::--->$uri")
            }
            fo.close()
            Timber.d("File Saved::--->" + f.absolutePath)
            f.absolutePath
        } catch (e1: IOException) {
            e1.printStackTrace()
            ""
        }
    }
}

@Throws(WriterException::class)
suspend fun textToImageEnc(textContent: String, resources: Resources): Bitmap {
    // todo 2953  chars are fine
    return withContext(Dispatchers.IO) {
        val bitMatrix: BitMatrix = try {
            MultiFormatWriter().encode(
                textContent,
                BarcodeFormat.QR_CODE,
                QRcodeWidth, QRcodeWidth, null
            )
        } catch (e: WriterException) {
            throw WriterException()
        }
        val bitMatrixWidth = bitMatrix.width
        val bitMatrixHeight = bitMatrix.height
        val pixels = IntArray(bitMatrixWidth * bitMatrixHeight)
        for (y in 0 until bitMatrixHeight) {
            val offset = y * bitMatrixWidth
            for (x in 0 until bitMatrixWidth) {
                pixels[offset + x] =
                    if (bitMatrix[x, y]) ResourcesCompat.getColor(resources, R.color.black, null)
                    else ResourcesCompat.getColor(resources, R.color.white, null)
            }
        }
        val bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight)

        bitmap
    }
}

fun Intent.startIntentGracefully(context: Context, notExecutable: (() -> Unit)? = null)  {
    val packageManager: PackageManager = context.packageManager
    val list = packageManager.queryIntentActivities(
        this,
        PackageManager.MATCH_DEFAULT_ONLY
    )
    if(list.size > 0) {
        context.startActivity(this)
    } else {
        notExecutable?.invoke()
    }
}

fun scanQrImage(uri: Uri, context: Context): Result? {
    var contents: Result? = null

    val inputStream = context.contentResolver.openInputStream(uri)
    val sourceBitmap: Bitmap = BitmapFactory.decodeStream(inputStream)

    val intArray = IntArray(sourceBitmap.width * sourceBitmap.height)
    sourceBitmap.getPixels(
        intArray,
        0,
        sourceBitmap.width,
        0,
        0,
        sourceBitmap.width,
        sourceBitmap.height
    )

    val source: LuminanceSource =
        RGBLuminanceSource(sourceBitmap.width, sourceBitmap.height, intArray)

    val bitmap = BinaryBitmap(HybridBinarizer(source))

    val reader: Reader = MultiFormatReader()
    try {
        val result: Result = reader.decode(bitmap)
        contents = result
    } catch (e: Exception) {
        Timber.e(e, "Error decoding barcode")
    }
    return contents
}
