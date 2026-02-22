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
import platform.CoreGraphics.CGAffineTransformMakeScale
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
            // Create QR code filter with convenience method
            val filter = CIFilter.filterWithName(
                "CIQRCodeGenerator",
                withInputParameters = mapOf(
                    "inputMessage" to (text as NSString).dataUsingEncoding(NSUTF8StringEncoding),
                    "inputCorrectionLevel" to "H"
                )
            ) ?: throw Exception("CIQRCodeGenerator filter not available")

            // Get the output CIImage
            val outputImage = filter.outputImage
                ?: throw Exception("Failed to generate QR code image")

            // Scale the image to desired size using the extent
            val scaleX = size.toDouble() / 23.0  // QR code default is ~23 pixels
            val scaleY = size.toDouble() / 23.0
            val scaledImage = outputImage.imageByApplyingTransform(
                CGAffineTransformMakeScale(scaleX, scaleY)
            )

            // Convert to UIImage and then to ByteArray
            ciImageToByteArray(scaledImage)
        } catch (e: Exception) {
            // Return empty array on failure
            ByteArray(0)
        }
    }

    private fun ciImageToByteArray(ciImage: CIImage): ByteArray {
        // Convert CIImage to UIImage directly
        val uiImage = UIImage(cIImage = ciImage)
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
 * Creates a copy of the data for safe memory management
 */
@OptIn(ExperimentalForeignApi::class)
fun ByteArray.toNSData(): NSData {
    if (this.isEmpty()) return NSData()

    return this.usePinned { pinned ->
        // NSData.create with copy option ensures data is copied and safe after usePinned exits
        NSData.create(
            bytes = pinned.addressOf(0),
            length = this.size.toULong()
        )
    }
}
