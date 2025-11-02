package cut.the.crap.qreverywhere.shared.domain.usecase

/**
 * Platform-specific use case for saving QR code images to device storage
 */
interface SaveImageToFileUseCase {
    /**
     * Saves the image data to device storage
     * @param imageData The image bytes to save
     * @param fileName Optional filename (platform will generate if not provided)
     * @return The file path or URI of the saved image, or null if failed
     */
    suspend fun saveImage(imageData: ByteArray, fileName: String? = null): String?
}