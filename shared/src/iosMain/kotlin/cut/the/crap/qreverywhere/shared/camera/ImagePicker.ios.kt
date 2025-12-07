package cut.the.crap.qreverywhere.shared.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cut.the.crap.qreverywhere.shared.platform.toByteArray
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerEditedImage
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject
import kotlin.coroutines.resume

/**
 * iOS implementation of ImagePicker using PHPickerViewController (iOS 14+)
 * Falls back to UIImagePickerController for older iOS versions
 */
@OptIn(ExperimentalForeignApi::class)
actual class ImagePicker {

    /**
     * Launch the image picker and return the selected image as ByteArray
     */
    actual suspend fun pickImage(): ImagePickerResult = suspendCancellableCoroutine { continuation ->
        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
        if (rootViewController == null) {
            continuation.resume(ImagePickerResult.Error("No root view controller available"))
            return@suspendCancellableCoroutine
        }

        // Use PHPickerViewController (iOS 14+)
        val configuration = PHPickerConfiguration().apply {
            filter = PHPickerFilter.imagesFilter
            selectionLimit = 1
        }

        val picker = PHPickerViewController(configuration)

        val delegate = object : NSObject(), PHPickerViewControllerDelegateProtocol {
            override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
                picker.dismissViewControllerAnimated(true, completion = null)

                val results = didFinishPicking.filterIsInstance<PHPickerResult>()
                if (results.isEmpty()) {
                    if (continuation.isActive) {
                        continuation.resume(ImagePickerResult.Cancelled)
                    }
                    return
                }

                val result = results.first()
                val itemProvider = result.itemProvider

                if (itemProvider.canLoadObjectOfClass(UIImage)) {
                    itemProvider.loadObjectOfClass(UIImage) { image, error ->
                        if (!continuation.isActive) return@loadObjectOfClass

                        when {
                            error != null -> {
                                continuation.resume(ImagePickerResult.Error(error.localizedDescription))
                            }
                            image is UIImage -> {
                                val imageData = UIImageJPEGRepresentation(image, 0.9)
                                if (imageData != null) {
                                    val byteArray = imageData.toByteArray()
                                    continuation.resume(ImagePickerResult.Success(byteArray))
                                } else {
                                    continuation.resume(ImagePickerResult.Error("Failed to convert image to data"))
                                }
                            }
                            else -> {
                                continuation.resume(ImagePickerResult.Error("Failed to load image"))
                            }
                        }
                    }
                } else {
                    if (continuation.isActive) {
                        continuation.resume(ImagePickerResult.Error("Cannot load image from selected item"))
                    }
                }
            }
        }

        picker.delegate = delegate
        rootViewController.presentViewController(picker, animated = true, completion = null)

        continuation.invokeOnCancellation {
            picker.dismissViewControllerAnimated(true, completion = null)
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

/**
 * Legacy image picker using UIImagePickerController for broader compatibility
 * Can be used as fallback for iOS versions < 14
 */
@OptIn(ExperimentalForeignApi::class)
class LegacyImagePicker {

    suspend fun pickImage(): ImagePickerResult = suspendCancellableCoroutine { continuation ->
        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
        if (rootViewController == null) {
            continuation.resume(ImagePickerResult.Error("No root view controller available"))
            return@suspendCancellableCoroutine
        }

        if (!UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary)) {
            continuation.resume(ImagePickerResult.Error("Photo library not available"))
            return@suspendCancellableCoroutine
        }

        val picker = UIImagePickerController().apply {
            sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
            allowsEditing = false
        }

        val delegate = object : NSObject(),
            UIImagePickerControllerDelegateProtocol,
            UINavigationControllerDelegateProtocol {

            override fun imagePickerController(
                picker: UIImagePickerController,
                didFinishPickingMediaWithInfo: Map<Any?, *>
            ) {
                picker.dismissViewControllerAnimated(true, completion = null)

                val image = (didFinishPickingMediaWithInfo[UIImagePickerControllerEditedImage]
                    ?: didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage]) as? UIImage

                if (image != null && continuation.isActive) {
                    val imageData = UIImageJPEGRepresentation(image, 0.9)
                    if (imageData != null) {
                        val byteArray = imageData.toByteArray()
                        continuation.resume(ImagePickerResult.Success(byteArray))
                    } else {
                        continuation.resume(ImagePickerResult.Error("Failed to convert image to data"))
                    }
                } else if (continuation.isActive) {
                    continuation.resume(ImagePickerResult.Error("Failed to get image from picker"))
                }
            }

            override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                picker.dismissViewControllerAnimated(true, completion = null)
                if (continuation.isActive) {
                    continuation.resume(ImagePickerResult.Cancelled)
                }
            }
        }

        picker.delegate = delegate
        rootViewController.presentViewController(picker, animated = true, completion = null)

        continuation.invokeOnCancellation {
            picker.dismissViewControllerAnimated(true, completion = null)
        }
    }
}
