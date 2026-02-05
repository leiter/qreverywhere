package cut.the.crap.qreverywhere.shared.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Web image picker - stub for Option B
 * Can be implemented later using file input element
 */
actual class ImagePicker {
    actual suspend fun pickImage(): ImagePickerResult {
        // For Option B (generator only), image picking is not needed
        return ImagePickerResult.Error("Image picking is not available in the web version")
    }
}

@Composable
actual fun rememberImagePicker(): ImagePicker {
    return remember { ImagePicker() }
}
