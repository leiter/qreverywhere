package cut.the.crap.qreverywhere.shared.domain.usecase

/**
 * Fake implementation of QrCodeGenerator for testing
 */
class FakeQrCodeGenerator : QrCodeGenerator {
    var shouldThrow = false
    var generatorException: Exception = RuntimeException("Generation failed")
    var generatedBytes: ByteArray = byteArrayOf(1, 2, 3, 4, 5) // Fake image data
    var lastGeneratedText: String? = null
    var lastForegroundColor: Int? = null
    var lastBackgroundColor: Int? = null

    override suspend fun generateQrCode(
        text: String,
        size: Int,
        foregroundColor: Int,
        backgroundColor: Int
    ): ByteArray {
        if (shouldThrow) {
            throw generatorException
        }
        lastGeneratedText = text
        lastForegroundColor = foregroundColor
        lastBackgroundColor = backgroundColor
        return generatedBytes
    }

    fun reset() {
        shouldThrow = false
        generatedBytes = byteArrayOf(1, 2, 3, 4, 5)
        lastGeneratedText = null
        lastForegroundColor = null
        lastBackgroundColor = null
    }
}
