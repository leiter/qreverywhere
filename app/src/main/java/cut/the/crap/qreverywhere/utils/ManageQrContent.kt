package cut.the.crap.qreverywhere.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.annotation.IntDef
import androidx.core.content.res.ResourcesCompat
import com.google.zxing.*
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.db.QrCodeItem
import cut.the.crap.qreverywhere.utils.QrCode.EMAIL
import cut.the.crap.qreverywhere.utils.QrCode.PHONE
import cut.the.crap.qreverywhere.utils.QrCode.SMS
import cut.the.crap.qreverywhere.utils.QrCode.UNKNOWN_CONTENT
import cut.the.crap.qreverywhere.utils.QrCode.WEB_URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


const val QRcodeWidth = 500 // todo should be calculated  / set stride also when ready
const val IMAGE_DIRECTORY = "QrEveryWhere"

object QrCode {
    const val EMAIL = 0
    const val PHONE = 1
    const val WEB_URL = 2
    const val SMS = 3
    const val UNKNOWN_CONTENT = 999

    @IntDef(EMAIL, PHONE, WEB_URL, UNKNOWN_CONTENT)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Type
}

object Acquire {
    const val SCANNED = 0
    const val CREATED = 1
    const val FROM_FILE = 2
    const val ERROR_OCCURRED = 3
    const val EMPTY_DEFAULT = 4

    @IntDef(SCANNED, CREATED, FROM_FILE, ERROR_OCCURRED, EMPTY_DEFAULT)
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

@QrCode.Type
fun determineType(contentString: String): Int {
    val decoded = Uri.decode(contentString)
    return when {
        decoded.startsWith("tel:") -> PHONE
        decoded.startsWith("mailto:") -> EMAIL
        decoded.startsWith("http:") -> WEB_URL
        decoded.startsWith("https:") -> WEB_URL
        decoded.startsWith("sms:") -> SMS
        decoded.startsWith("smsto:") -> SMS
        else -> UNKNOWN_CONTENT
    }
}

fun getQrTypeDrawable(contentString: String): Int {
    val decoded = Uri.decode(contentString)

    return when {
        decoded.startsWith("tel:") -> R.drawable.ic_phone
        decoded.startsWith("mailto:") -> R.drawable.ic_mail_outline
        decoded.startsWith("http:") -> R.drawable.ic_open_in_browser
        decoded.startsWith("https:") -> R.drawable.ic_open_in_browser
        decoded.startsWith("sms:") -> R.drawable.ic_sms
        decoded.startsWith("smsto:") -> R.drawable.ic_sms
        else -> R.drawable.ic_content
    }
}

fun getQrLaunchText(contentString: String): Int {
    val decoded = Uri.decode(contentString)
    return when {
        decoded.startsWith("tel:") -> R.string.ic_phone
        decoded.startsWith("mailto:") -> R.string.ic_mail
        decoded.startsWith("http:") -> R.string.ic_open_in_browser
        decoded.startsWith("https:") -> R.string.ic_open_in_browser
        decoded.startsWith("sms:") -> R.string.ic_sms
        decoded.startsWith("smsto:") -> R.string.ic_sms
        else -> R.string.app_name
    }
}

fun textForHistoryList(text: String, context: Context) : String {
    val decodedText = Uri.decode(text)
    val qrType = determineType(decodedText)
    return when(qrType){
        PHONE -> context.getString(R.string.phone_template).format(decodedText.subSequence(4,decodedText.length-1))
        EMAIL -> context.getString(R.string.mail_template).format(decodedText.subSequence(7,decodedText.indexOf("?")))
        WEB_URL -> context.getString(R.string.open_in_browser_template).format(decodedText)
        UNKNOWN_CONTENT -> context.getString(R.string.text_template).format(decodedText)
        else -> context.getString(R.string.text_template).format(decodedText)
    }
}

suspend fun saveImageToFile(qrCodeItem: QrCodeItem, context: Context): String {
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
                directory, Calendar.getInstance()
                    .timeInMillis.toString() + ".jpg"
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
                Log.e("TAG", "MediaScanner::--->" + uri.toString())
            }
            fo.close()
            Log.d("TAG", "File Saved::--->" + f.absolutePath)
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


// todo integrate settings display result or fire intent here
fun createOpenIntent(qrString: String, context: Context): Intent? {
    val decoded = Uri.decode(qrString)
    return when {
        decoded.startsWith("tel:") -> Intent(Intent.ACTION_DIAL, Uri.parse(qrString))
        decoded.startsWith("mailto:") -> Intent(Intent.ACTION_SENDTO, Uri.parse(qrString))
        decoded.startsWith("http:") -> Intent(Intent.ACTION_VIEW, Uri.parse(decoded))
        decoded.startsWith("https:") -> Intent(Intent.ACTION_VIEW, Uri.parse(decoded))
        decoded.startsWith("sms:") -> Intent(Intent.ACTION_VIEW, Uri.parse(qrString))
        decoded.startsWith("smsto:") -> Intent(Intent.ACTION_SENDTO, Uri.parse(qrString))
        else -> {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(qrString))
            if (isIntentAvailable(i, context)) i
            else null
        }
    }
}

fun createShareIntent(qrImgUri: Uri): Intent {
    return Intent(Intent.ACTION_SEND).apply {
//        data = qrImgUri;
        type = "image/png";
        putExtra(Intent.EXTRA_STREAM, qrImgUri);
//        activity.startActivityForResult(Intent.createChooser(shareIntent, "Share Via"), Navigator.REQUEST_SHARE_ACTION);
    }
}

private fun shareBitmap(context: Context, bitmap: Bitmap, fileName: String) {
    try {
        val file: File = File(context.cacheDir, "$fileName.png")
        val fOut = FileOutputStream(file)
        bitmap.compress(CompressFormat.PNG, 100, fOut)
        fOut.flush()
        fOut.close()
//        file.setReadable(true, false)
        val intent = Intent(Intent.ACTION_SEND)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
        intent.type = "image/png"
        context.startActivity(intent)
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
}

fun screenShot(view: View): Bitmap? {
    val bitmap = Bitmap.createBitmap(
        view.width,
        view.height, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    view.draw(canvas)
    return bitmap
}

private fun isIntentAvailable(intent: Intent, context: Context): Boolean {
    val packageManager: PackageManager = context.packageManager
    val list = packageManager.queryIntentActivities(
        intent,
        PackageManager.MATCH_DEFAULT_ONLY
    )
    return list.size > 0
}


fun scanQrImage(uri: Uri, context: Context): String? {
    var contents: String? = null

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
        contents = result.text
    } catch (e: Exception) {
        Log.e("QrTest", "Error decoding barcode", e)
    }
    return contents
}

fun bitmapToArray(bmp: Bitmap): ByteArray {
    val stream = ByteArrayOutputStream()
    bmp.compress(Bitmap.CompressFormat.JPEG, 50, stream)
    return stream.toByteArray()
}

fun extractDomain(url: String): String {
    return android.util.Patterns.DOMAIN_NAME.toRegex().find(url)?.value ?: ""
}