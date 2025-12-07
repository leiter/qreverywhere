package cut.the.crap.qreverywhere.shared.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * Desktop implementation of ImagePicker using JFileChooser
 */
actual class ImagePicker {

    /**
     * Launch the file chooser and return the selected image as ByteArray
     */
    actual suspend fun pickImage(): ImagePickerResult = withContext(Dispatchers.IO) {
        try {
            val fileChooser = JFileChooser().apply {
                dialogTitle = "Select Image with QR Code"
                fileFilter = FileNameExtensionFilter(
                    "Image files",
                    "jpg", "jpeg", "png", "gif", "bmp", "webp"
                )
                isAcceptAllFileFilterUsed = false
                isMultiSelectionEnabled = false
            }

            val result = fileChooser.showOpenDialog(null)

            when (result) {
                JFileChooser.APPROVE_OPTION -> {
                    val selectedFile = fileChooser.selectedFile
                    if (selectedFile != null && selectedFile.exists()) {
                        val imageData = selectedFile.readBytes()
                        ImagePickerResult.Success(imageData)
                    } else {
                        ImagePickerResult.Error("Selected file does not exist")
                    }
                }
                JFileChooser.CANCEL_OPTION -> {
                    ImagePickerResult.Cancelled
                }
                else -> {
                    ImagePickerResult.Error("File selection failed")
                }
            }
        } catch (e: Exception) {
            ImagePickerResult.Error(e.message ?: "Unknown error occurred")
        }
    }
}

/**
 * Composable to remember an ImagePicker instance
 */
@Composable
actual fun rememberImagePicker(): ImagePicker {
    return remember { ImagePicker() }
}
