package cut.the.crap.qreverywhere.shared.platform

import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.WriterException
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeWriter
import cut.the.crap.qreverywhere.shared.domain.usecase.QrCodeGenerator
import cut.the.crap.qreverywhere.shared.domain.usecase.QrCodeScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

/**
 * Desktop implementation of QR code generation using ZXing
 */
class DesktopQrCodeGenerator : QrCodeGenerator {

    override suspend fun generateQrCode(
        text: String,
        size: Int,
        foregroundColor: Int,
        backgroundColor: Int
    ): ByteArray = withContext(Dispatchers.Default) {
        try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size)
            val image = createBufferedImage(bitMatrix, foregroundColor, backgroundColor)

            val stream = ByteArrayOutputStream()
            ImageIO.write(image, "PNG", stream)
            stream.toByteArray()
        } catch (e: WriterException) {
            throw Exception("Failed to generate QR code", e)
        }
    }

    private fun createBufferedImage(
        matrix: com.google.zxing.common.BitMatrix,
        foregroundColor: Int,
        backgroundColor: Int
    ): BufferedImage {
        val width = matrix.width
        val height = matrix.height
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

        for (x in 0 until width) {
            for (y in 0 until height) {
                image.setRGB(x, y, if (matrix.get(x, y)) foregroundColor else backgroundColor)
            }
        }

        return image
    }
}

/**
 * Desktop implementation of QR code scanning using ZXing
 */
class DesktopQrCodeScanner : QrCodeScanner {

    override suspend fun decodeQrCode(imageData: ByteArray): String? = withContext(Dispatchers.Default) {
        try {
            val inputStream = ByteArrayInputStream(imageData)
            val bufferedImage = ImageIO.read(inputStream) ?: return@withContext null

            val width = bufferedImage.width
            val height = bufferedImage.height
            val pixels = IntArray(width * height)
            bufferedImage.getRGB(0, 0, width, height, pixels, 0, width)

            val source = RGBLuminanceSource(width, height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

            val reader = MultiFormatReader()
            val result = reader.decode(binaryBitmap)
            result.text
        } catch (e: Exception) {
            null
        }
    }
}