package cut.the.crap.qreverywhere.shared.camera

import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.LuminanceSource
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.Result
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.multi.GenericMultipleBarcodeReader
import com.google.zxing.multi.qrcode.QRCodeMultiReader

/**
 * Multi-strategy QR code detection helper for ZXing-based platforms.
 *
 * Tries progressively more expensive strategies, returning early on first success:
 * 1. Single MultiFormatReader.decode() + HybridBinarizer (fast, current behavior)
 * 2. QRCodeMultiReader.decodeMultiple() + HybridBinarizer (finds small QR in large image)
 * 3. GenericMultipleBarcodeReader.decodeMultiple() + HybridBinarizer (recursive sub-regions)
 * 4. QRCodeMultiReader.decodeMultiple() + GlobalHistogramBinarizer (low contrast)
 * 5. Downscale to ~1000px + retry strategies 1-2 (large phone photos)
 */
internal object ZxingDetectionHelper {

    private val hints = mapOf(
        DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
        DecodeHintType.TRY_HARDER to true,
        DecodeHintType.CHARACTER_SET to "UTF-8"
    )

    fun detectFromPixels(pixels: IntArray, width: Int, height: Int): List<QrCodeResult> {
        val source = RGBLuminanceSource(width, height, pixels)

        // Strategy 1: Single fast decode (existing behavior)
        tryStrategy1(source)?.let { return it }

        // Strategy 2: QRCodeMultiReader (finds QR finder patterns anywhere)
        tryStrategy2(source)?.let { return it }

        // Strategy 3: GenericMultipleBarcodeReader (recursive sub-regions)
        tryStrategy3(source)?.let { return it }

        // Strategy 4: Different binarizer for low contrast
        tryStrategy4(source)?.let { return it }

        // Strategy 5: Downscale large images and retry
        if (width > 1500 || height > 1500) {
            tryStrategy5(pixels, width, height)?.let { return it }
        }

        return emptyList()
    }

    /** Strategy 1: Standard single-pass decode with HybridBinarizer */
    private fun tryStrategy1(source: LuminanceSource): List<QrCodeResult>? {
        return try {
            val reader = MultiFormatReader().apply { setHints(hints) }
            val bitmap = BinaryBitmap(HybridBinarizer(source))
            val result = reader.decode(bitmap)
            reader.reset()
            listOf(result.toQrCodeResult())
        } catch (_: Exception) {
            null
        }
    }

    /** Strategy 2: QRCodeMultiReader scans for all QR finder patterns in the image */
    private fun tryStrategy2(source: LuminanceSource): List<QrCodeResult>? {
        return try {
            val bitmap = BinaryBitmap(HybridBinarizer(source))
            val results = QRCodeMultiReader().decodeMultiple(bitmap, hints)
            results?.takeIf { it.isNotEmpty() }?.map { it.toQrCodeResult() }
        } catch (_: Exception) {
            null
        }
    }

    /** Strategy 3: GenericMultipleBarcodeReader recursively splits into sub-regions */
    private fun tryStrategy3(source: LuminanceSource): List<QrCodeResult>? {
        return try {
            val delegate = MultiFormatReader().apply { setHints(hints) }
            val multiReader = GenericMultipleBarcodeReader(delegate)
            val bitmap = BinaryBitmap(HybridBinarizer(source))
            val results = multiReader.decodeMultiple(bitmap, hints)
            delegate.reset()
            results?.takeIf { it.isNotEmpty() }?.map { it.toQrCodeResult() }
        } catch (_: Exception) {
            null
        }
    }

    /** Strategy 4: QRCodeMultiReader with GlobalHistogramBinarizer for low contrast */
    private fun tryStrategy4(source: LuminanceSource): List<QrCodeResult>? {
        return try {
            val bitmap = BinaryBitmap(GlobalHistogramBinarizer(source))
            val results = QRCodeMultiReader().decodeMultiple(bitmap, hints)
            results?.takeIf { it.isNotEmpty() }?.map { it.toQrCodeResult() }
        } catch (_: Exception) {
            null
        }
    }

    /** Strategy 5: Downscale to ~1000px width and retry strategies 1-2 */
    private fun tryStrategy5(
        pixels: IntArray,
        width: Int,
        height: Int
    ): List<QrCodeResult>? {
        val targetWidth = 1000
        val scale = targetWidth.toFloat() / width
        val targetHeight = (height * scale).toInt()

        if (targetWidth >= width) return null // Already small enough

        val scaled = nearestNeighborScale(pixels, width, height, targetWidth, targetHeight)
        val source = RGBLuminanceSource(targetWidth, targetHeight, scaled)

        tryStrategy1(source)?.let { return it }
        tryStrategy2(source)?.let { return it }

        return null
    }

    /** Fast nearest-neighbor downscale - good enough for QR detection */
    private fun nearestNeighborScale(
        src: IntArray,
        srcWidth: Int,
        srcHeight: Int,
        dstWidth: Int,
        dstHeight: Int
    ): IntArray {
        val dst = IntArray(dstWidth * dstHeight)
        val xRatio = srcWidth.toFloat() / dstWidth
        val yRatio = srcHeight.toFloat() / dstHeight

        for (y in 0 until dstHeight) {
            val srcY = (y * yRatio).toInt().coerceIn(0, srcHeight - 1)
            for (x in 0 until dstWidth) {
                val srcX = (x * xRatio).toInt().coerceIn(0, srcWidth - 1)
                dst[y * dstWidth + x] = src[srcY * srcWidth + srcX]
            }
        }
        return dst
    }

    private fun Result.toQrCodeResult() = QrCodeResult(
        text = text,
        format = barcodeFormat?.name
    )
}
