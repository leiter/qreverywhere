package cut.the.crap.qreverywhere.shared.camera

import android.graphics.BitmapFactory
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.multi.qrcode.QRCodeMultiReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual class QrCodeDetector {
    private val hints = mapOf(
        DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
        DecodeHintType.TRY_HARDER to true,
        DecodeHintType.CHARACTER_SET to "UTF-8"
    )

    private val multiFormatReader = MultiFormatReader().apply { setHints(hints) }

    actual suspend fun detectQrCodes(imageData: Any): List<QrCodeResult> = withContext(Dispatchers.Default) {
        when (imageData) {
            is ImageProxy -> detectFromImageProxy(imageData)
            is ByteArray -> detectFromByteArray(imageData)
            else -> emptyList()
        }
    }

    private fun detectFromImageProxy(image: ImageProxy): List<QrCodeResult> {
        val planes = image.planes
        if (planes.isEmpty()) return emptyList()

        val buffer = planes[0].buffer
        val data = ByteArray(buffer.remaining())
        buffer.get(data)

        val source = PlanarYUVLuminanceSource(
            data, image.width, image.height,
            0, 0, image.width, image.height, false
        )
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

        // Fast path: single decode for camera frames
        try {
            val result = multiFormatReader.decode(binaryBitmap)
            multiFormatReader.reset()
            return listOf(QrCodeResult(text = result.text, format = result.barcodeFormat?.name))
        } catch (_: NotFoundException) {
            // Fall through to multi-reader
        } catch (e: Exception) {
            e.printStackTrace()
        }
        multiFormatReader.reset()

        // Fallback: QRCodeMultiReader for harder-to-find codes in camera frames
        return try {
            val results = QRCodeMultiReader().decodeMultiple(binaryBitmap, hints)
            results?.map { QrCodeResult(text = it.text, format = it.barcodeFormat?.name) }
                ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun detectFromByteArray(data: ByteArray): List<QrCodeResult> {
        try {
            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                ?: return emptyList()

            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            bitmap.recycle()

            // Use multi-strategy helper for imported images
            return ZxingDetectionHelper.detectFromPixels(pixels, width, height)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }

    actual fun release() {
        multiFormatReader.reset()
    }
}
