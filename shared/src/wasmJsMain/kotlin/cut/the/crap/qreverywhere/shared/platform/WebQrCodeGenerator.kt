package cut.the.crap.qreverywhere.shared.platform

import cut.the.crap.qreverywhere.shared.domain.usecase.QrCodeGenerator
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Web implementation of QR code generation
 * Uses the qrcode.js library loaded via script tag in HTML
 */
class WebQrCodeGenerator : QrCodeGenerator {

    override suspend fun generateQrCode(
        text: String,
        size: Int,
        foregroundColor: Int,
        backgroundColor: Int
    ): ByteArray = suspendCancellableCoroutine { continuation ->
        try {
            val fgColor = colorToHex(foregroundColor)
            val bgColor = colorToHex(backgroundColor)

            generateQrCodeJs(
                text = text,
                size = size,
                darkColor = fgColor,
                lightColor = bgColor,
                onSuccess = { dataUrl ->
                    val byteArray = dataUrlToByteArray(dataUrl)
                    continuation.resume(byteArray)
                },
                onError = { error ->
                    continuation.resumeWithException(Exception(error))
                }
            )
        } catch (e: Exception) {
            continuation.resume(ByteArray(0))
        }
    }

    private fun colorToHex(color: Int): String {
        val r = (color shr 16) and 0xFF
        val g = (color shr 8) and 0xFF
        val b = color and 0xFF
        return "#${r.toString(16).padStart(2, '0')}${g.toString(16).padStart(2, '0')}${b.toString(16).padStart(2, '0')}"
    }

    private fun dataUrlToByteArray(dataUrl: String): ByteArray {
        val base64Data = dataUrl.substringAfter("base64,")
        return decodeBase64(base64Data)
    }
}

// External JS function declarations for Wasm interop
@JsFun("(text, size, darkColor, lightColor, onSuccess, onError) => { if (typeof QRCode !== 'undefined') { QRCode.toDataURL(text, { width: size, height: size, margin: 1, color: { dark: darkColor, light: lightColor } }).then(onSuccess).catch(err => onError(err.message)); } else { onError('QRCode library not loaded'); } }")
private external fun generateQrCodeJs(
    text: String,
    size: Int,
    darkColor: String,
    lightColor: String,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
)

@JsFun("(base64) => { const binaryString = atob(base64); const bytes = new Uint8Array(binaryString.length); for (let i = 0; i < binaryString.length; i++) { bytes[i] = binaryString.charCodeAt(i); } return bytes; }")
private external fun decodeBase64Js(base64: String): JsAny

private fun decodeBase64(base64: String): ByteArray {
    val jsArray = decodeBase64Js(base64)
    return jsArrayToByteArray(jsArray)
}

@JsFun("(arr) => arr.length")
private external fun jsArrayLength(arr: JsAny): Int

@JsFun("(arr, i) => arr[i]")
private external fun jsArrayGet(arr: JsAny, index: Int): Int

private fun jsArrayToByteArray(jsArray: JsAny): ByteArray {
    val length = jsArrayLength(jsArray)
    return ByteArray(length) { jsArrayGet(jsArray, it).toByte() }
}
