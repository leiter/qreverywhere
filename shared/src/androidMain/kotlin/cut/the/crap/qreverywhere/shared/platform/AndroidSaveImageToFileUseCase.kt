package cut.the.crap.qreverywhere.shared.platform

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.os.Environment
import cut.the.crap.qreverywhere.shared.domain.usecase.SaveImageToFileUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Calendar

/**
 * Android implementation for saving QR code images to device storage
 */
class AndroidSaveImageToFileUseCase(
    private val context: Context
) : SaveImageToFileUseCase {

    override suspend fun saveImage(imageData: ByteArray, fileName: String?): String? {
        return withContext(Dispatchers.IO) {
            try {
                // Decode bitmap from byte array
                val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                    ?: return@withContext null

                // Create directory
                val directory = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    IMAGE_DIRECTORY
                )

                if (!directory.exists()) {
                    directory.mkdirs()
                }

                // Create file
                val actualFileName = fileName ?: "${Calendar.getInstance().timeInMillis}.jpg"
                val file = File(directory, actualFileName)
                file.createNewFile()

                // Write bitmap to file
                FileOutputStream(file).use { outputStream ->
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, outputStream)
                }

                // Scan media to make it visible in gallery
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(file.path),
                    arrayOf("image/jpeg"),
                    null
                )

                file.absolutePath
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }
    }

    companion object {
        private const val IMAGE_DIRECTORY = "QrEveryWhere"
    }
}