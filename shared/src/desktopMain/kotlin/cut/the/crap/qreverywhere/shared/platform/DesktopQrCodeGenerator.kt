package cut.the.crap.qreverywhere.shared.platform

import cut.the.crap.qreverywhere.shared.domain.usecase.QrCodeGenerator
import cut.the.crap.qreverywhere.shared.domain.usecase.QrCodeScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Desktop implementation of QR code generation
 * TODO: Implement using ZXing Java or kotlin-qrcode library
 */
class DesktopQrCodeGenerator : QrCodeGenerator {

    override suspend fun generateQrCode(
        text: String,
        size: Int,
        foregroundColor: Int,
        backgroundColor: Int
    ): ByteArray = withContext(Dispatchers.Default) {
        // TODO: Implement Desktop QR code generation
        // For now, return empty array
        // In real implementation, could use ZXing Java directly or kotlin-qrcode
        ByteArray(0)
    }
}

/**
 * Desktop implementation of QR code scanning
 * TODO: Implement using ZXing Java or webcam integration
 */
class DesktopQrCodeScanner : QrCodeScanner {

    override suspend fun decodeQrCode(imageData: ByteArray): String? = withContext(Dispatchers.Default) {
        // TODO: Implement Desktop QR code scanning
        // For now, return null
        // In real implementation, use ZXing Java for decoding
        null
    }
}