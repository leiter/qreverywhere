package cut.the.crap.qreverywhere.shared.platform

import cut.the.crap.qreverywhere.shared.domain.usecase.SaveImageToFileUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * Desktop implementation for saving QR code images to device storage
 */
class DesktopSaveImageToFileUseCase : SaveImageToFileUseCase {

    override suspend fun saveImage(imageData: ByteArray, fileName: String?): String? {
        return withContext(Dispatchers.IO) {
            try {
                // Read image from byte array
                val inputStream = ByteArrayInputStream(imageData)
                val image = ImageIO.read(inputStream) ?: return@withContext null

                // Create file chooser for save dialog
                val fileChooser = JFileChooser().apply {
                    dialogTitle = "Save QR Code Image"
                    fileFilter = FileNameExtensionFilter("PNG Images (*.png)", "png")
                    selectedFile = File(fileName ?: "qr_code_${System.currentTimeMillis()}.png")
                }

                // Show save dialog
                val result = fileChooser.showSaveDialog(null)

                if (result == JFileChooser.APPROVE_OPTION) {
                    var file = fileChooser.selectedFile

                    // Ensure .png extension
                    if (!file.name.lowercase().endsWith(".png")) {
                        file = File(file.absolutePath + ".png")
                    }

                    // Write image to file
                    ImageIO.write(image, "PNG", file)
                    file.absolutePath
                } else {
                    // User cancelled
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
