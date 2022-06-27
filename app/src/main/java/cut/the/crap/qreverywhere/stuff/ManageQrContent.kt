package cut.the.crap.qreverywhere.stuff

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.annotation.IntDef
import androidx.core.content.res.ResourcesCompat
import com.google.zxing.*
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.stuff.QrCode.EMAIL
import cut.the.crap.qreverywhere.stuff.QrCode.PHONE
import cut.the.crap.qreverywhere.stuff.QrCode.SMS
import cut.the.crap.qreverywhere.stuff.QrCode.UNKNOWN_CONTENT
import cut.the.crap.qreverywhere.stuff.QrCode.WEB_URL
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


const val QRcodeWidth = 500 // todo should be calculated  / set stride also when ready
const val IMAGE_DIRECTORY = "QrEveryWhere"

object QrCode{
    const val EMAIL = 0
    const val PHONE = 1
    const val WEB_URL = 2
    const val SMS = 3
    const val UNKNOWN_CONTENT = 999

    @IntDef(EMAIL, PHONE, WEB_URL)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Type
}

object Acquire {
    const val SCANNED = 0
    const val CREATED = 1
    const val FROM_FILE = 2
    const val ERROR_OCCURRED = 3
    @IntDef(SCANNED, CREATED, FROM_FILE, ERROR_OCCURRED)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Type
}


fun determineType(contentString: String) : Int {
    return when {
        contentString.startsWith("tel:") -> PHONE
        contentString.startsWith("mailto:") -> EMAIL
        contentString.startsWith("http:") -> WEB_URL
        contentString.startsWith("https:") -> WEB_URL
        contentString.startsWith("sms:") -> SMS
        contentString.startsWith("smsto:") -> SMS
        else -> UNKNOWN_CONTENT
    }
}

//mailto:%s?subject=%s&body=%s

fun getQrTypeDrawable(contentString: String) : Int {
    return when {
        contentString.startsWith("tel:") -> R.drawable.ic_phone
        contentString.startsWith("mailto:") -> R.drawable.ic_mail_outline
        contentString.startsWith("http:") -> R.drawable.ic_open_in_browser
        contentString.startsWith("https:") -> R.drawable.ic_open_in_browser
        contentString.startsWith("sms:") -> R.drawable.ic_sms
        contentString.startsWith("smsto:") -> R.drawable.ic_sms
        else -> R.drawable.ic_content
    }
}


fun saveImageToFile(myBitmap: Bitmap, context: Context): String? {
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
        val f = File( //todo better naming
            directory, Calendar.getInstance()
                .timeInMillis.toString() + ".jpg"
        )
        f.createNewFile() // todo give read write permission
        val fo = FileOutputStream(f)
        fo.write(bytes.toByteArray())
        MediaScannerConnection.scanFile(context, arrayOf(f.path), arrayOf("image/jpeg"), null)
        fo.close()
        Log.d("TAG", "File Saved::--->" + f.absolutePath)
        return f.absolutePath
    } catch (e1: IOException) {
        e1.printStackTrace()
    }
    return ""
}

@Throws(WriterException::class)
fun textToImageEncoder(textContent: String, resources: Resources): Bitmap? {
    // todo 2953  chars are fine

    val bitMatrix: BitMatrix = try {
        MultiFormatWriter().encode(
            textContent,
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


// todo integrate settings display result or fire intent here
fun createIntent(qrString: String, context: Context): Intent? {
    return when {
        qrString.startsWith("tel:") -> Intent(Intent.ACTION_DIAL, Uri.parse(qrString))
        qrString.startsWith("mailto:") -> Intent(Intent.ACTION_SENDTO, Uri.parse(qrString))
        qrString.startsWith("http:") -> Intent(Intent.ACTION_VIEW, Uri.parse(qrString))
        qrString.startsWith("https:") -> Intent(Intent.ACTION_VIEW, Uri.parse(qrString))
        qrString.startsWith("sms:") -> Intent(Intent.ACTION_VIEW, Uri.parse(qrString))
        qrString.startsWith("smsto:") -> Intent(Intent.ACTION_SENDTO, Uri.parse(qrString))
        else -> {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(qrString))
            if (isIntentAvailable(i, context)) i
            else null
        }
    }
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

fun bitmapToArray(bmp: Bitmap): ByteArray {
    val stream = ByteArrayOutputStream()
    bmp.compress(Bitmap.CompressFormat.JPEG, 50, stream)
    return stream.toByteArray()
}