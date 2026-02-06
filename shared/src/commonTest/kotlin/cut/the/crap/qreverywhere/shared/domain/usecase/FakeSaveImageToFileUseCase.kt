package cut.the.crap.qreverywhere.shared.domain.usecase

/**
 * Fake implementation of SaveImageToFileUseCase for testing
 */
class FakeSaveImageToFileUseCase : SaveImageToFileUseCase {
    var shouldThrow = false
    var saveException: Exception = RuntimeException("Save failed")
    var savedFilePath: String? = "/fake/path/qr_code.png"
    var lastSavedImageData: ByteArray? = null
    var lastSavedFileName: String? = null

    override suspend fun saveImage(imageData: ByteArray, fileName: String?): String? {
        if (shouldThrow) {
            throw saveException
        }
        lastSavedImageData = imageData
        lastSavedFileName = fileName
        return savedFilePath
    }

    fun reset() {
        shouldThrow = false
        savedFilePath = "/fake/path/qr_code.png"
        lastSavedImageData = null
        lastSavedFileName = null
    }
}
