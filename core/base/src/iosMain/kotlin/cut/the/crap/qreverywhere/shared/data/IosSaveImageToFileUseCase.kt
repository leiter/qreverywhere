package cut.the.crap.qreverywhere.shared.data

import cut.the.crap.qreverywhere.shared.domain.usecase.SaveImageToFileUseCase
import cut.the.crap.qreverywhere.shared.platform.toNSData
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.writeToURL
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIImage
import platform.UIKit.UIImageWriteToSavedPhotosAlbum

/**
 * iOS implementation for saving images to device storage
 */
@OptIn(ExperimentalForeignApi::class)
class IosSaveImageToFileUseCase : SaveImageToFileUseCase {

    override suspend fun saveImage(imageData: ByteArray, fileName: String?): String? {
        return try {
            val nsData = imageData.toNSData()
            val uiImage = UIImage.imageWithData(nsData)
                ?: return null

            // Save to photo library
            UIImageWriteToSavedPhotosAlbum(uiImage, null, null, null)

            // Return a placeholder path indicating success
            // iOS doesn't give direct file path for photo library saves
            "photo_library://saved"
        } catch (e: Exception) {
            null
        }
    }
}
