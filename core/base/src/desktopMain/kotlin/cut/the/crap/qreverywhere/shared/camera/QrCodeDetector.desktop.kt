package cut.the.crap.qreverywhere.shared.camera

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

actual class QrCodeDetector {

    actual suspend fun detectQrCodes(imageData: Any): List<QrCodeResult> = withContext(Dispatchers.IO) {
        when (imageData) {
            is ByteArray -> detectFromByteArray(imageData)
            else -> emptyList()
        }
    }

    private fun detectFromByteArray(data: ByteArray): List<QrCodeResult> {
        try {
            val inputStream = ByteArrayInputStream(data)
            val bufferedImage = ImageIO.read(inputStream) ?: return emptyList()

            val width = bufferedImage.width
            val height = bufferedImage.height
            val pixels = IntArray(width * height)
            bufferedImage.getRGB(0, 0, width, height, pixels, 0, width)

            // Use multi-strategy helper for imported images
            return ZxingDetectionHelper.detectFromPixels(pixels, width, height)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }

    actual fun release() {
        // No resources to release
    }
}
