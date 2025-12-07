package cut.the.crap.qreverywhere.shared.camera

import androidx.compose.runtime.Composable

/**
 * Result from image picker operation
 */
sealed class ImagePickerResult {
    /**
     * Image was successfully selected
     * @param imageData The raw image data as ByteArray
     */
    data class Success(val imageData: ByteArray) : ImagePickerResult() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Success) return false
            return imageData.contentEquals(other.imageData)
        }

        override fun hashCode(): Int = imageData.contentHashCode()
    }

    /**
     * User cancelled the picker
     */
    data object Cancelled : ImagePickerResult()

    /**
     * An error occurred
     */
    data class Error(val message: String) : ImagePickerResult()
}

/**
 * Platform-specific image picker for selecting images from the gallery
 * Used for scanning QR codes from saved images
 */
expect class ImagePicker {
    /**
     * Launch the image picker and return the selected image
     */
    suspend fun pickImage(): ImagePickerResult
}

/**
 * Composable function to remember an ImagePicker instance
 */
@Composable
expect fun rememberImagePicker(): ImagePicker
