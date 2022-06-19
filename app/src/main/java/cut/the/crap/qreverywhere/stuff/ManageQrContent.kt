package cut.the.crap.qreverywhere.stuff

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import java.io.ByteArrayOutputStream


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
    //copy pixel data from the Bitmap into the 'intArray' array
    //copy pixel data from the Bitmap into the 'intArray' array
    sourceBitmap.getPixels(intArray, 0, sourceBitmap.width, 0, 0, sourceBitmap.width, sourceBitmap.height)

    val source: LuminanceSource =
        RGBLuminanceSource(sourceBitmap.width, sourceBitmap.height, intArray)

// todo integrate/test    probably not the right thing
//    val array: ByteArray = bitmapToArray(sourceBitmap)
//    val source: LuminanceSource = PlanarYUVLuminanceSource(
//        array,
//        sourceBitmap.getWidth(),
//        sourceBitmap.getHeight(),
//        0,
//        0,
//        sourceBitmap.getWidth(),
//        sourceBitmap.getHeight(),
//        false
//    )


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