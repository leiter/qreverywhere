package cut.the.crap.qreverywhere.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.view.View
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.reflect.KClass

private fun shareBitmap(context: Context, bitmap: Bitmap, fileName: String) {
    try {
        val file = File(context.cacheDir, "$fileName.png")
        val fOut = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
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

fun isPermissionDialogDisplayed(context: Context) :Boolean {
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
    val cn = am!!.getRunningTasks(1)[0].topActivity
    if ("com.android.packageinstaller.permission.ui.GrantPermissionsActivity" == cn!!.className) {
        return true
    }
    return false
}

//fun (() -> Unit).catch(vararg exceptions: KClass<out Throwable>, catchBlock: (Throwable) -> Unit) {
//    try {
//        this()
//    } catch (e: Throwable) {
//        if (e::class in exceptions) catchBlock(e) else throw e
//    }
//}
//
//fun <R> (() -> R).catch(vararg exceptions: KClass<out Throwable>, catchBlock: (Throwable) -> Unit) {
//    try {
//        this()
//    } catch (e: Throwable) {
//        if (e::class in exceptions) catchBlock(e) else throw e
//    }
//}


//data class State<out T>(
//    val status: Status,
//    val data: T?,
//    val message: String?
//) {
//
//    companion object {
//
//        fun <T> success(msg: String?, data: T?): State<T> = State(Status.SUCCESS, data, msg)
//        fun <T> loading(data: T?): State<T> = State(Status.LOADING, data, null)
//        fun <T> error(msg: String, data: T?): State<T> = State(Status.ERROR, data, msg)
//    }
//}
//
//enum class Status {
//    SUCCESS,
//    ERROR,
//    LOADING
//}

//@RequiresApi(Build.VERSION_CODES.Q)
//private fun saveFileToExternalStorage(displayName: String, content: String) {
//    val externalUri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
//    val relativeLocation = Environment.DIRECTORY_DOCUMENTS
//    val contentValues = ContentValues()
//    contentValues.put(MediaStore.Files.FileColumns.DISPLAY_NAME, "$displayName.txt")
//    contentValues.put(MediaStore.Files.FileColumns.MIME_TYPE, "application/text")
//    contentValues.put(MediaStore.Files.FileColumns.TITLE, "Test")
//    contentValues.put(MediaStore.Files.FileColumns.DATE_ADDED, System.currentTimeMillis() / 1000)
//    contentValues.put(MediaStore.Files.FileColumns.RELATIVE_PATH, relativeLocation)
//    contentValues.put(MediaStore.Files.FileColumns.DATE_TAKEN, System.currentTimeMillis())
//    val fileUri: Uri = getContentResolver().insert(externalUri, contentValues)
//    try {
//        val outputStream: OutputStream = getContentResolver().openOutputStream(fileUri)
//        outputStream.write(content.toByteArray())
//        outputStream.close()
//    } catch (e: IOException) {
//        e.printStackTrace()
//    }
//}
