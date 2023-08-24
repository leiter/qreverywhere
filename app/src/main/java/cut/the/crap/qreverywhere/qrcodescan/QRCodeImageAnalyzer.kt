package cut.the.crap.qreverywhere.qrcodescan

import android.graphics.ImageFormat
import android.os.Looper
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.multi.qrcode.QRCodeMultiReader
import java.nio.ByteBuffer


class QRCodeImageAnalyzer(private val listener: QRCodeFoundListener) : ImageAnalysis.Analyzer {

    private var throttle: Long = 0L
    private val waitingTime = 2000L

    override fun analyze(image: ImageProxy) {
        when (image.format) {
            ImageFormat.YUV_420_888,
            ImageFormat.YUV_422_888,
            ImageFormat.YUV_444_888,
            -> {
                val byteBuffer: ByteBuffer = image.planes[0].buffer
                val imageData = ByteArray(byteBuffer.capacity())
                byteBuffer[imageData]
                val source = PlanarYUVLuminanceSource(
                    imageData,
                    image.width, image.height,
                    0, 0,
                    image.width, image.height,
                    false
                )
                val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
                try {
                    val result = QRCodeMultiReader().decode(binaryBitmap)
                    val now = System.currentTimeMillis()
                    if (now - throttle > waitingTime) {
                        throttle = now
                        android.os.Handler(Looper.getMainLooper()).post {
                            listener.onQRCodeFound(result)
                        }
                    }
                } catch (e: FormatException) {
                    listener.qrCodeNotFound()
                } catch (e: ChecksumException) {
                    listener.qrCodeNotFound()
                } catch (e: NotFoundException) {
                    listener.qrCodeNotFound()
                }

                image.close()
            }
        }
    }
}