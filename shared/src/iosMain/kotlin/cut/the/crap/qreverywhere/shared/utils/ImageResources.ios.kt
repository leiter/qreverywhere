package cut.the.crap.qreverywhere.shared.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import cut.the.crap.qreverywhere.shared.platform.toNSData
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.UIKit.UIImage
import platform.Foundation.NSData
import platform.posix.memcpy
import org.jetbrains.skia.Image as SkiaImage

/**
 * iOS implementation: Convert ImageResource to Painter
 * On iOS, there are NO Int resource IDs - images are loaded by name from asset catalogs
 */
@Composable
actual fun ImageResource.toPainter(): Painter {
    return remember(name) {
        try {
            // Load UIImage from asset catalog by name
            // This is NOT an Int - it's a string lookup!
            val uiImage = UIImage.imageNamed(name)

            if (uiImage == null) {
                Logger.w("ImageResource") { "Image not found in iOS assets: $name" }
                // Return empty painter
                return@remember BitmapPainter(
                    ImageBitmap(1, 1)
                )
            }

            // Convert UIImage to Compose ImageBitmap (safely)
            val imageBitmap = uiImage.toComposeImageBitmapSafe()
            if (imageBitmap == null) {
                Logger.w("ImageResource") { "Failed to convert image: $name" }
                return@remember BitmapPainter(ImageBitmap(1, 1))
            }
            BitmapPainter(imageBitmap)
        } catch (e: Exception) {
            Logger.e("ImageResource") { "Error loading image $name: ${e.message}" }
            BitmapPainter(ImageBitmap(1, 1))
        }
    }
}

/**
 * iOS implementation: Convert ByteArray to ImagePainter
 * Used for displaying generated QR codes
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun ByteArray.toImagePainter(): Painter? {
    if (isEmpty()) return null

    return remember(this) {
        try {
            // Convert ByteArray to NSData using extension function
            val bytes = this@toImagePainter
            val nsData = bytes.toNSData()

            // Create UIImage from NSData
            val uiImage = UIImage.imageWithData(nsData) ?: return@remember null

            // Convert to Compose ImageBitmap (may fail for certain image formats)
            val imageBitmap = uiImage.toComposeImageBitmapSafe() ?: return@remember null
            BitmapPainter(imageBitmap)
        } catch (e: Exception) {
            Logger.e("ImageResources") { "Failed to convert ByteArray to ImagePainter: ${e.message}" }
            null
        }
    }
}

/**
 * Extension to convert UIImage to Compose ImageBitmap
 * This is iOS-specific - uses CoreGraphics and Skia
 * Returns null on failure instead of throwing
 */
@OptIn(ExperimentalForeignApi::class)
private fun UIImage.toComposeImageBitmapSafe(): ImageBitmap? {
    return try {
        // Get PNG representation of UIImage
        val data = platform.UIKit.UIImagePNGRepresentation(this) ?: return null

        // Convert NSData to ByteArray
        val byteArray = ByteArray(data.length.toInt())
        byteArray.usePinned { pinned ->
            memcpy(pinned.addressOf(0), data.bytes, data.length)
        }

        // Use Skia to decode the image
        val skiaImage = SkiaImage.makeFromEncoded(byteArray)
        skiaImage.toComposeImageBitmap()
    } catch (e: Exception) {
        Logger.e("ImageResources") { "Failed to convert UIImage to ImageBitmap: ${e.message}" }
        null
    }
}

/**
 * Extension to convert UIImage to Compose ImageBitmap (throws on failure)
 * @deprecated Use toComposeImageBitmapSafe instead
 */
@OptIn(ExperimentalForeignApi::class)
private fun UIImage.toComposeImageBitmap(): ImageBitmap {
    return toComposeImageBitmapSafe()
        ?: throw IllegalStateException("Failed to convert UIImage to ImageBitmap")
}
