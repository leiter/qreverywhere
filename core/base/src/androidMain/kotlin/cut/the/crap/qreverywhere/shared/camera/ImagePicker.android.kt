package cut.the.crap.qreverywhere.shared.camera

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Android implementation of ImagePicker using Photo Picker API
 */
actual class ImagePicker(
    private val context: Context,
    private val onImagePicked: ((Uri?) -> Unit) -> Unit
) {
    private var pendingCallback: ((ImagePickerResult) -> Unit)? = null

    actual suspend fun pickImage(): ImagePickerResult = suspendCancellableCoroutine { continuation ->
        pendingCallback = { result ->
            if (continuation.isActive) {
                continuation.resume(result)
            }
        }

        onImagePicked { uri ->
            val result = if (uri != null) {
                try {
                    val imageData = readImageData(uri)
                    if (imageData != null) {
                        ImagePickerResult.Success(imageData)
                    } else {
                        ImagePickerResult.Error("Failed to read image data")
                    }
                } catch (e: Exception) {
                    ImagePickerResult.Error(e.message ?: "Unknown error")
                }
            } else {
                ImagePickerResult.Cancelled
            }
            pendingCallback?.invoke(result)
            pendingCallback = null
        }

        continuation.invokeOnCancellation {
            pendingCallback = null
        }
    }

    private fun readImageData(uri: Uri): ByteArray? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes()
            }
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Composable to remember an ImagePicker instance with the photo picker launcher
 */
@Composable
actual fun rememberImagePicker(): ImagePicker {
    val context = LocalContext.current
    var currentCallback by remember { mutableStateOf<((Uri?) -> Unit)?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        currentCallback?.invoke(uri)
        currentCallback = null
    }

    return remember(context) {
        ImagePicker(
            context = context,
            onImagePicked = { callback ->
                currentCallback = callback
                launcher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )
    }
}
