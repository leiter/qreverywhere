package cut.the.crap.qreverywhere.shared.camera

import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.Result
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer

actual class QrCodeDetector {
    private val multiFormatReader = MultiFormatReader().apply {
        val hints = mapOf(
            DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
            DecodeHintType.TRY_HARDER to true,
            DecodeHintType.CHARACTER_SET to "UTF-8"
        )
        setHints(hints)
    }

    private val qrCodeReader = QRCodeReader()

    actual suspend fun detectQrCodes(imageData: Any): List<QrCodeResult> = withContext(Dispatchers.Default) {
        when (imageData) {
            is ImageProxy -> detectFromImageProxy(imageData)
            is ByteArray -> detectFromByteArray(imageData)
            else -> emptyList()
        }
    }

    private fun detectFromImageProxy(image: ImageProxy): List<QrCodeResult> {
        val results = mutableListOf<QrCodeResult>()

        // Get the image data
        val planes = image.planes
        if (planes.isEmpty()) return results

        val buffer = planes[0].buffer
        val data = ByteArray(buffer.remaining())
        buffer.get(data)

        // Create luminance source from YUV data
        val source = PlanarYUVLuminanceSource(
            data,
            image.width,
            image.height,
            0,
            0,
            image.width,
            image.height,
            false
        )

        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

        try {
            // Try to decode QR code
            val result = multiFormatReader.decode(binaryBitmap)
            results.add(
                QrCodeResult(
                    text = result.text,
                    format = result.barcodeFormat?.name,
                    timestamp = System.currentTimeMillis()
                )
            )
        } catch (e: NotFoundException) {
            // No QR code found, that's okay
        } catch (e: Exception) {
            // Other errors, log but don't crash
            e.printStackTrace()
        }

        // Reset reader for next detection
        multiFormatReader.reset()

        return results
    }

    private fun detectFromByteArray(data: ByteArray): List<QrCodeResult> {
        // This method can be implemented if needed for decoding from static images
        // For now, returning empty list
        return emptyList()
    }

    actual fun release() {
        // Clean up resources if needed
        multiFormatReader.reset()
    }
}