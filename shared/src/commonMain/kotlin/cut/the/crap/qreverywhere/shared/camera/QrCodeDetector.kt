package cut.the.crap.qreverywhere.shared.camera

/**
 * Platform-specific QR code detector
 * Analyzes images/frames for QR codes
 */
expect class QrCodeDetector {
    /**
     * Detect QR codes from image data
     * @param imageData Platform-specific image data
     * @return List of detected QR codes
     */
    suspend fun detectQrCodes(imageData: Any): List<QrCodeResult>

    /**
     * Release resources used by the detector
     */
    fun release()
}