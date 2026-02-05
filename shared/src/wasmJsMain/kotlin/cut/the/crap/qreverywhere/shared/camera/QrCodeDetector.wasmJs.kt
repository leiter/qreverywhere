package cut.the.crap.qreverywhere.shared.camera

/**
 * Web QR code detector - stub for Option B
 * Can be implemented later using zxing-js or jsQR
 */
actual class QrCodeDetector {
    actual suspend fun detectQrCodes(imageData: Any): List<QrCodeResult> {
        // For Option B (generator only), QR detection is not needed
        return emptyList()
    }

    actual fun release() {
        // Nothing to release
    }
}
