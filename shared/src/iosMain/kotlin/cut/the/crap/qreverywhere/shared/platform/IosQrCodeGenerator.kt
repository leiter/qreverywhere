package cut.the.crap.qreverywhere.shared.platform

import cut.the.crap.qreverywhere.shared.domain.usecase.QrCodeGenerator
import cut.the.crap.qreverywhere.shared.domain.usecase.QrCodeScanner
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGBitmapContextCreateImage
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGContextDrawImage
import platform.CoreGraphics.CGContextSetFillColor
import platform.CoreGraphics.CGContextFillRect
import platform.CoreGraphics.CGContextScaleCTM
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.CGImageGetHeight
import platform.CoreGraphics.CGImageGetWidth
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.kCGImageAlphaPremultipliedLast
import platform.CoreImage.CIColor
import platform.CoreImage.CIContext
import platform.CoreImage.CIFilter
import platform.CoreImage.CIImage
import platform.CoreImage.filterWithName
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.UIKit.UIImage
import platform.UIKit.UIImagePNGRepresentation
import platform.Vision.VNBarcodeObservation
import platform.Vision.VNDetectBarcodesRequest
import platform.Vision.VNImageRequestHandler
import platform.posix.memcpy

/**
 * iOS implementation of QR code generation using CIFilter
 */
@OptIn(ExperimentalForeignApi::class)
class IosQrCodeGenerator : QrCodeGenerator {

    override suspend fun generateQrCode(
        text: String,
        size: Int,
        foregroundColor: Int,
        backgroundColor: Int
    ): ByteArray = withContext(Dispatchers.Default) {
        try {
            // Create QR code filter
            val filter = CIFilter.filterWithName("CIQRCodeGenerator")
                ?: throw Exception("CIQRCodeGenerator filter not available")

            // Convert text to NSData
            val textData = (text as NSString).dataUsingEncoding(NSUTF8StringEncoding)
                ?: throw Exception("Failed to encode text")

            filter.setValue(textData, forKey = "inputMessage")
            filter.setValue("H", forKey = "inputCorrectionLevel") // High error correction

            // Get the output CIImage
            val outputImage = filter.outputImage
                ?: throw Exception("Failed to generate QR code image")

            // Scale the image to desired size
            val scaleX = size.toDouble() / outputImage.extent.size.width
            val scaleY = size.toDouble() / outputImage.extent.size.height
            val scaledImage = outputImage.imageByApplyingTransform(
                platform.CoreGraphics.CGAffineTransformMakeScale(scaleX, scaleY)
            )

            // Apply colors using CIFalseColor filter
            val colorFilter = CIFilter.filterWithName("CIFalseColor")
            if (colorFilter != null) {
                colorFilter.setValue(scaledImage, forKey = "inputImage")

                // Convert Int colors to CIColor (ARGB format)
                val fgAlpha = ((foregroundColor shr 24) and 0xFF) / 255.0
                val fgRed = ((foregroundColor shr 16) and 0xFF) / 255.0
                val fgGreen = ((foregroundColor shr 8) and 0xFF) / 255.0
                val fgBlue = (foregroundColor and 0xFF) / 255.0

                val bgAlpha = ((backgroundColor shr 24) and 0xFF) / 255.0
                val bgRed = ((backgroundColor shr 16) and 0xFF) / 255.0
                val bgGreen = ((backgroundColor shr 8) and 0xFF) / 255.0
                val bgBlue = (backgroundColor and 0xFF) / 255.0

                val fgColor = CIColor.colorWithRed(fgRed, fgGreen, fgBlue, fgAlpha)
                val bgColor = CIColor.colorWithRed(bgRed, bgGreen, bgBlue, bgAlpha)

                colorFilter.setValue(fgColor, forKey = "inputColor0")
                colorFilter.setValue(bgColor, forKey = "inputColor1")

                val coloredImage = colorFilter.outputImage
                if (coloredImage != null) {
                    return@withContext ciImageToByteArray(coloredImage, size)
                }
            }

            // Fallback: return scaled image without color modification
            ciImageToByteArray(scaledImage, size)
        } catch (e: Exception) {
            // Return empty array on failure
            ByteArray(0)
        }
    }

    private fun ciImageToByteArray(ciImage: CIImage, size: Int): ByteArray {
        val context = CIContext.contextWithOptions(null)
        val cgImage = context.createCGImage(ciImage, fromRect = ciImage.extent)
            ?: return ByteArray(0)

        val uiImage = UIImage.imageWithCGImage(cgImage)
        val pngData = UIImagePNGRepresentation(uiImage)
            ?: return ByteArray(0)

        return pngData.toByteArray()
    }
}

/**
 * iOS implementation of QR code scanning using Vision framework
 */
@OptIn(ExperimentalForeignApi::class)
class IosQrCodeScanner : QrCodeScanner {

    override suspend fun decodeQrCode(imageData: ByteArray): String? = withContext(Dispatchers.Default) {
        try {
            if (imageData.isEmpty()) return@withContext null

            // Convert ByteArray to NSData
            val nsData = imageData.toNSData()

            // Create UIImage from data
            val uiImage = UIImage.imageWithData(nsData)
                ?: return@withContext null

            // Get CGImage
            val cgImage = uiImage.CGImage
                ?: return@withContext null

            // Create Vision request handler
            val requestHandler = VNImageRequestHandler(cgImage, options = emptyMap<Any?, Any?>())

            // Create barcode detection request
            var detectedText: String? = null
            val request = VNDetectBarcodesRequest { request, error ->
                if (error != null) {
                    return@VNDetectBarcodesRequest
                }

                val results = request?.results as? List<VNBarcodeObservation>
                if (!results.isNullOrEmpty()) {
                    // Get the first QR code found
                    for (observation in results) {
                        val payload = observation.payloadStringValue
                        if (payload != null) {
                            detectedText = payload
                            break
                        }
                    }
                }
            }

            // Perform the request
            requestHandler.performRequests(listOf(request), error = null)

            detectedText
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Extension function to convert NSData to ByteArray
 */
@OptIn(ExperimentalForeignApi::class)
fun NSData.toByteArray(): ByteArray {
    val length = this.length.toInt()
    if (length == 0) return ByteArray(0)

    val bytes = ByteArray(length)
    bytes.usePinned { pinned ->
        memcpy(pinned.addressOf(0), this.bytes, this.length)
    }
    return bytes
}

/**
 * Extension function to convert ByteArray to NSData
 */
@OptIn(ExperimentalForeignApi::class)
fun ByteArray.toNSData(): NSData {
    if (this.isEmpty()) return NSData()

    return memScoped {
        NSData.create(
            bytes = allocArrayOf(this@toNSData),
            length = this@toNSData.size.toULong()
        )
    }
}