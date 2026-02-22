package cut.the.crap.qreverywhere.shared.camera

import cut.the.crap.qreverywhere.shared.platform.toNSData
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGImageRef
import platform.Foundation.NSData
import platform.UIKit.UIImage
import platform.Vision.VNBarcodeObservation
import platform.Vision.VNBarcodeSymbologyQR
import platform.Vision.VNDetectBarcodesRequest
import platform.Vision.VNImageRequestHandler

/**
 * iOS implementation of QR code detection using Vision framework
 * Supports detection from UIImage, CGImage, NSData, or ByteArray
 */
@OptIn(ExperimentalForeignApi::class)
actual class QrCodeDetector {

    /**
     * Detect QR codes from image data
     * @param imageData Can be UIImage, NSData, or ByteArray
     * @return List of detected QR codes with their content
     */
    actual suspend fun detectQrCodes(imageData: Any): List<QrCodeResult> = withContext(Dispatchers.Default) {
        try {
            val cgImage: CGImageRef? = when (imageData) {
                is UIImage -> imageData.CGImage
                is NSData -> UIImage.imageWithData(imageData)?.CGImage
                is ByteArray -> {
                    val nsData = imageData.toNSData()
                    UIImage.imageWithData(nsData)?.CGImage
                }
                else -> null
            }

            if (cgImage == null) {
                return@withContext emptyList()
            }

            detectQrCodesFromCGImage(cgImage)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Detect QR codes from a CGImage using Vision framework
     */
    private fun detectQrCodesFromCGImage(cgImage: CGImageRef): List<QrCodeResult> {
        val results = mutableListOf<QrCodeResult>()

        try {
            // Create Vision request handler
            val requestHandler = VNImageRequestHandler(cgImage, options = emptyMap<Any?, Any?>())

            // Create barcode detection request
            val request = VNDetectBarcodesRequest { request, error ->
                if (error != null) {
                    return@VNDetectBarcodesRequest
                }

                val observations = request?.results as? List<VNBarcodeObservation>
                observations?.forEach { observation ->
                    val payload = observation.payloadStringValue
                    if (payload != null) {
                        val format = when (observation.symbology) {
                            VNBarcodeSymbologyQR -> "QR_CODE"
                            else -> observation.symbology
                        }
                        results.add(
                            QrCodeResult(
                                text = payload,
                                format = format
                            )
                        )
                    }
                }
            }

            // Perform the detection request
            requestHandler.performRequests(listOf(request), error = null)
        } catch (e: Exception) {
            // Return empty list on error
        }

        return results
    }

    /**
     * Release resources - no cleanup needed for Vision framework
     */
    actual fun release() {
        // Vision framework handles its own memory management
        // No explicit cleanup required
    }
}