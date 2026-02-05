package cut.the.crap.qreverywhere.shared.platform

import cut.the.crap.qreverywhere.shared.domain.usecase.SaveImageToFileUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Web implementation for saving QR code images
 * Triggers a browser download of the image
 */
class WebSaveImageToFileUseCase : SaveImageToFileUseCase {

    override suspend fun saveImage(imageData: ByteArray, fileName: String?): String? {
        return withContext(Dispatchers.Default) {
            try {
                val actualFileName = fileName ?: "qr_code_${System.currentTimeMillis()}.png"
                downloadImage(imageData, actualFileName)
                actualFileName // Return filename as "path"
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun downloadImage(imageData: ByteArray, fileName: String) {
        triggerDownload(imageData, fileName)
    }
}

// Simple timestamp for web
private object System {
    fun currentTimeMillis(): Long = currentTimeMillisJs().toLong()
}

@JsFun("() => Date.now()")
private external fun currentTimeMillisJs(): Double

// Download trigger using Blob and anchor element
@JsFun("""
(data, fileName) => {
    const blob = new Blob([data], { type: 'image/png' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = fileName;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}
""")
private external fun triggerDownloadJs(data: JsAny, fileName: String)

private fun triggerDownload(imageData: ByteArray, fileName: String) {
    val jsArray = byteArrayToJsArray(imageData)
    triggerDownloadJs(jsArray, fileName)
}

@JsFun("(size) => new Uint8Array(size)")
private external fun createUint8Array(size: Int): JsAny

@JsFun("(arr, i, value) => { arr[i] = value; }")
private external fun setUint8ArrayValue(arr: JsAny, index: Int, value: Int)

private fun byteArrayToJsArray(data: ByteArray): JsAny {
    val jsArray = createUint8Array(data.size)
    for (i in data.indices) {
        setUint8ArrayValue(jsArray, i, data[i].toInt() and 0xFF)
    }
    return jsArray
}
