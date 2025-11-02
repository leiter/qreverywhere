package cut.the.crap.qreverywhere.shared.platform

import cut.the.crap.qreverywhere.shared.domain.usecase.QrCodeGenerator
import cut.the.crap.qreverywhere.shared.domain.usecase.QrCodeScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * iOS implementation of QR code generation
 * TODO: Implement using CIFilter or external library
 */
class IosQrCodeGenerator : QrCodeGenerator {

    override suspend fun generateQrCode(
        text: String,
        size: Int,
        foregroundColor: Int,
        backgroundColor: Int
    ): ByteArray = withContext(Dispatchers.Default) {
        // TODO: Implement iOS QR code generation
        // For now, return empty array
        // In real implementation, use CIFilter.qrCodeGenerator
        ByteArray(0)
    }
}

/**
 * iOS implementation of QR code scanning
 * TODO: Implement using AVFoundation or Vision framework
 */
class IosQrCodeScanner : QrCodeScanner {

    override suspend fun decodeQrCode(imageData: ByteArray): String? = withContext(Dispatchers.Default) {
        // TODO: Implement iOS QR code scanning
        // For now, return null
        // In real implementation, use Vision framework's VNDetectBarcodesRequest
        null
    }
}