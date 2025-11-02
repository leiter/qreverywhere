package cut.the.crap.qreverywhere.shared.domain.usecase

/**
 * Common interface for QR code generation
 * Platform-specific implementations will handle the actual generation
 */
interface QrCodeGenerator {
    /**
     * Generate QR code image data from text
     * @param text The text to encode in the QR code
     * @param size The size of the QR code image (width and height)
     * @return ByteArray containing the image data
     */
    suspend fun generateQrCode(
        text: String,
        size: Int = 512,
        foregroundColor: Int = 0xFF000000.toInt(),
        backgroundColor: Int = 0xFFFFFFFF.toInt()
    ): ByteArray
}

/**
 * Common interface for QR code scanning
 * Platform-specific implementations will handle the actual scanning
 */
interface QrCodeScanner {
    /**
     * Decode QR code from image data
     * @param imageData The image data to decode
     * @return The text content of the QR code, or null if decoding fails
     */
    suspend fun decodeQrCode(imageData: ByteArray): String?
}