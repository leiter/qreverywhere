package cut.the.crap.qreverywhere.shared.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image as SkiaImage

/**
 * Desktop implementation: Convert ImageResource to Painter
 * Loads images from resources directory
 */
@Composable
actual fun ImageResource.toPainter(): Painter {
    return remember(name) {
        try {
            // Load image from desktop resources by name
            // Desktop also uses string names, not Int IDs
            val resourcePath = "/drawable/$name.png"
            val imageBytes = object {}.javaClass.getResourceAsStream(resourcePath)
                ?.readBytes()

            if (imageBytes == null) {
                Logger.w("ImageResource") { "Image not found in desktop resources: $resourcePath" }
                return@remember BitmapPainter(ImageBitmap(1, 1))
            }

            // Decode using Skia
            val skiaImage = SkiaImage.makeFromEncoded(imageBytes)
            val imageBitmap = skiaImage.toComposeImageBitmap()
            BitmapPainter(imageBitmap)
        } catch (e: Exception) {
            Logger.e("ImageResource", e) { "Failed to load image: $name" }
            BitmapPainter(ImageBitmap(1, 1))
        }
    }
}

/**
 * Desktop implementation: Convert ByteArray to ImagePainter
 * Used for displaying generated QR codes
 */
@Composable
actual fun ByteArray.toImagePainter(): Painter? {
    if (isEmpty()) return null

    return remember(this) {
        try {
            val skiaImage = SkiaImage.makeFromEncoded(this)
            val imageBitmap = skiaImage.toComposeImageBitmap()
            BitmapPainter(imageBitmap)
        } catch (e: Exception) {
            Logger.e("ByteArray.toImagePainter", e) { "Failed to decode image" }
            null
        }
    }
}
