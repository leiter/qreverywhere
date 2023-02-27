package cut.the.crap.qreverywhere.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.view.View
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

private fun shareBitmap(context: Context, bitmap: Bitmap, fileName: String) {
    try {
        val file = File(context.cacheDir, "$fileName.png")
        val fOut = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut)
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

fun bitmapToArray(bmp: Bitmap): ByteArray {
    val stream = ByteArrayOutputStream()
    bmp.compress(Bitmap.CompressFormat.JPEG, 50, stream)
    return stream.toByteArray()
}

fun extractDomain(url: String): String {
    return android.util.Patterns.DOMAIN_NAME.toRegex().find(url)?.value ?: ""
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


fun createShareIntent(qrImgUri: Uri): Intent {
    return Intent(Intent.ACTION_SEND).apply {
//        data = qrImgUri;
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, qrImgUri)
//        activity.startActivityForResult(Intent.createChooser(shareIntent, "Share Via"), Navigator.REQUEST_SHARE_ACTION);
    }
}