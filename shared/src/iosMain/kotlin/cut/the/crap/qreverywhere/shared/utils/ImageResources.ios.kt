package cut.the.crap.qreverywhere.shared.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
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

        // Convert UIImage to Compose ImageBitmap
        val imageBitmap = uiImage.toComposeImageBitmap()
        BitmapPainter(imageBitmap)
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
        // Convert ByteArray to NSData
        val nsData = this.usePinned { pinned ->
            NSData.dataWithBytes(pinned.addressOf(0), this.size.toULong())
        }

        // Create UIImage from NSData
        val uiImage = UIImage.imageWithData(nsData) ?: return@remember null

        // Convert to Compose ImageBitmap
        val imageBitmap = uiImage.toComposeImageBitmap()
        BitmapPainter(imageBitmap)
    }
}

/**
 * Extension to convert UIImage to Compose ImageBitmap
 * This is iOS-specific - uses CoreGraphics and Skia
 */
@OptIn(ExperimentalForeignApi::class)
private fun UIImage.toComposeImageBitmap(): ImageBitmap {
    // Get PNG representation of UIImage
    val data = platform.UIKit.UIImagePNGRepresentation(this)
        ?: throw IllegalStateException("Failed to get PNG data from UIImage")

    // Convert NSData to ByteArray
    val byteArray = ByteArray(data.length.toInt())
    byteArray.usePinned { pinned ->
        memcpy(pinned.addressOf(0), data.bytes, data.length)
    }

    // Use Skia to decode the image
    val skiaImage = SkiaImage.makeFromEncoded(byteArray)
    return skiaImage.toComposeImageBitmap()
}
