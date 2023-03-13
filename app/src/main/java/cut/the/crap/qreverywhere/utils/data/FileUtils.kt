package cut.the.crap.qreverywhere.utils.data

import android.os.Environment
import cut.the.crap.qreverywhere.IMAGE_DIRECTORY
import cut.the.crap.qrrepository.QrItem
import java.io.File
import java.text.SimpleDateFormat

class FileUtils {

    private val directory = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        IMAGE_DIRECTORY
    )

    private fun getAppDirectory(): File {
        if (!directory.exists()) {
            directory.mkdirs()
        }
        return directory
    }

    fun getQrImageFile(qrCodeItem: QrItem): File {
        val simpleDate = SimpleDateFormat.getDateTimeInstance()
        val currentDate = simpleDate.format(qrCodeItem.timestamp)
        val fileName = PREFIX + currentDate + FILE_TYPE
        val file = File(getAppDirectory(), fileName)
        file.createNewFile()
        return file
    }

    companion object {
        const val FILE_TYPE = ".jpg"
        const val PREFIX = "qrcode_"
    }
}