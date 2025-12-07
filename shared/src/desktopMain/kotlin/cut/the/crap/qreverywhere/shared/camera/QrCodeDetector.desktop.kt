package cut.the.crap.qreverywhere.shared.camera

import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

actual class QrCodeDetector {
    private val multiFormatReader = MultiFormatReader().apply {
        val hints = mapOf(
            DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
            DecodeHintType.TRY_HARDER to true,
            DecodeHintType.CHARACTER_SET to "UTF-8"
        )
        setHints(hints)
    }

    actual suspend fun detectQrCodes(imageData: Any): List<QrCodeResult> = withContext(Dispatchers.IO) {
        when (imageData) {
            is ByteArray -> detectFromByteArray(imageData)
            else -> emptyList()
        }
    }

    private fun detectFromByteArray(data: ByteArray): List<QrCodeResult> {
        val results = mutableListOf<QrCodeResult>()

        try {
            // Read image from ByteArray
            val inputStream = ByteArrayInputStream(data)
            val bufferedImage = ImageIO.read(inputStream) ?: return emptyList()

            val width = bufferedImage.width
            val height = bufferedImage.height
            val pixels = IntArray(width * height)
            bufferedImage.getRGB(0, 0, width, height, pixels, 0, width)

            // Create luminance source from RGB pixels
            val source = RGBLuminanceSource(width, height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

            try {
                val result = multiFormatReader.decode(binaryBitmap)
                results.add(
                    QrCodeResult(
                        text = result.text,
                        format = result.barcodeFormat?.name
                    )
                )
            } catch (e: NotFoundException) {
                // No QR code found
            }

            multiFormatReader.reset()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return results
    }

    actual fun release() {
        multiFormatReader.reset()
    }
}