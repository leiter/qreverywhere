package cut.the.crap.qreverywhere.shared.data

import cut.the.crap.qreverywhere.shared.domain.usecase.SaveImageToFileUseCase
import cut.the.crap.qreverywhere.shared.platform.toNSData
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Photos.PHAssetChangeRequest
import platform.Photos.PHAuthorizationStatus
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusDenied
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHAuthorizationStatusNotDetermined
import platform.Photos.PHAuthorizationStatusRestricted
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIImage
import kotlin.coroutines.resume

/**
 * iOS implementation for saving images to device storage
 */
@OptIn(ExperimentalForeignApi::class)
class IosSaveImageToFileUseCase : SaveImageToFileUseCase {

    override suspend fun saveImage(imageData: ByteArray, fileName: String?): String? {
        return try {
            // Convert byte array to UIImage
            val nsData = imageData.toNSData()
            val uiImage = UIImage.imageWithData(nsData)
                ?: return null

            // Check and request photo library permission
            val authStatus = checkPhotoLibraryPermission()
            if (!authStatus) {
                return null
            }

            // Save to photo library using PHPhotoLibrary
            val success = suspendCancellableCoroutine { continuation ->
                PHPhotoLibrary.sharedPhotoLibrary().performChanges(
                    changeBlock = {
                        PHAssetChangeRequest.creationRequestForAssetFromImage(uiImage)
                    },
                    completionHandler = { success, error ->
                        if (success) {
                            continuation.resume("photo_library://saved")
                        } else {
                            continuation.resume(null)
                        }
                    }
                )
            }

            success
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun checkPhotoLibraryPermission(): Boolean {
        val currentStatus = PHPhotoLibrary.authorizationStatus()

        return when (currentStatus) {
            PHAuthorizationStatusAuthorized, PHAuthorizationStatusLimited -> true
            PHAuthorizationStatusNotDetermined -> {
                // Request permission
                suspendCancellableCoroutine { continuation ->
                    PHPhotoLibrary.requestAuthorization { status ->
                        val granted = status == PHAuthorizationStatusAuthorized ||
                                     status == PHAuthorizationStatusLimited
                        continuation.resume(granted)
                    }
                }
            }
            PHAuthorizationStatusDenied, PHAuthorizationStatusRestricted -> false
            else -> false
        }
    }
}
