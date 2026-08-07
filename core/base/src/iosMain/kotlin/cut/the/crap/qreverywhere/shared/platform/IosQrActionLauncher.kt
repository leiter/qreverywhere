package cut.the.crap.qreverywhere.shared.platform

import cut.the.crap.qreverywhere.shared.domain.model.QrAction
import cut.the.crap.qreverywhere.shared.domain.usecase.QrActionLauncher
import cut.the.crap.qreverywhere.shared.domain.usecase.QrActionResult
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

/**
 * iOS implementation of [QrActionLauncher].
 *
 * `tel:`, `sms:`, `mailto:` and `http(s):` are all native URL schemes on iOS, so they're
 * all routed through `UIApplication.sharedApplication.openURL(NSURL)`, mirroring the
 * NSURL/UIKit interop conventions already used in [IosQrCodeGenerator]/[IosQrCodeScanner]
 * in this file's sibling.
 *
 * [QrAction.SaveContact], [QrAction.AddCalendarEvent] and [QrAction.ConnectWifi] are not
 * implemented in V1 (would need Contacts/EventKit/NEHotspotConfiguration frameworks and
 * entitlements) - they return [QrActionResult.NoHandlerAvailable] so the UI can fall back
 * to Share sheet / the WiFi reveal card, per the plan.
 */
@OptIn(ExperimentalForeignApi::class)
class IosQrActionLauncher : QrActionLauncher {

    override fun launch(action: QrAction): QrActionResult {
        return when (action) {
            is QrAction.OpenUrl -> openUrl(action.url)
            is QrAction.DialPhone -> openUrl("tel:${action.number}")
            is QrAction.SendSms -> openUrl("sms:${action.number}")
            is QrAction.SendEmail -> openUrl(buildMailtoUri(action))
            is QrAction.ShowLocation -> openUrl(
                "http://maps.apple.com/?ll=${action.location.latitude},${action.location.longitude}"
            )

            is QrAction.SaveContact -> QrActionResult.NoHandlerAvailable
            is QrAction.AddCalendarEvent -> QrActionResult.NoHandlerAvailable
            is QrAction.ConnectWifi -> QrActionResult.NoHandlerAvailable
            is QrAction.NoAction -> QrActionResult.NoHandlerAvailable
        }
    }

    private fun openUrl(urlString: String): QrActionResult {
        val url = NSURL.URLWithString(urlString) ?: return QrActionResult.Failed("Invalid URL: $urlString")
        val application = UIApplication.sharedApplication

        return if (application.canOpenURL(url)) {
            application.openURL(url)
            QrActionResult.Success
        } else {
            QrActionResult.NoHandlerAvailable
        }
    }

    private fun buildMailtoUri(action: QrAction.SendEmail): String {
        val params = buildList {
            action.subject?.let { add("subject=${it.percentEncoded()}") }
            action.body?.let { add("body=${it.percentEncoded()}") }
        }
        val query = if (params.isNotEmpty()) "?" + params.joinToString("&") else ""
        return "mailto:${action.to}$query"
    }

    /**
     * Minimal percent-encoding for mailto: query params, done in pure Kotlin rather than
     * via NSCharacterSet/NSString interop to sidestep Kotlin/Native API-availability
     * differences - mirrors the equally hand-rolled decodePercentEncoded() used for the
     * reverse operation in QrAction.kt.
     */
    private fun String.percentEncoded(): String {
        val unreserved = ('A'..'Z') + ('a'..'z') + ('0'..'9') + listOf('-', '_', '.', '~')
        return buildString {
            for (byte in this@percentEncoded.encodeToByteArray()) {
                val c = byte.toInt().toChar()
                if (c in unreserved) {
                    append(c)
                } else {
                    val hex = (byte.toInt() and 0xFF).toString(16).uppercase().padStart(2, '0')
                    append('%').append(hex)
                }
            }
        }
    }
}
