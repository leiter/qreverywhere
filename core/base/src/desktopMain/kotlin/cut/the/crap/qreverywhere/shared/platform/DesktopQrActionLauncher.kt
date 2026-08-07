package cut.the.crap.qreverywhere.shared.platform

import cut.the.crap.qreverywhere.shared.domain.model.QrAction
import cut.the.crap.qreverywhere.shared.domain.usecase.QrActionLauncher
import cut.the.crap.qreverywhere.shared.domain.usecase.QrActionResult
import java.awt.Desktop
import java.io.IOException
import java.net.URI
import java.net.URLEncoder

/**
 * Desktop implementation of [QrActionLauncher] using `java.awt.Desktop`.
 *
 * There's no dialer/SMS app/WiFi-connect API on desktop, and [QrAction.SaveContact] /
 * [QrAction.AddCalendarEvent] have no `Desktop` API to hand a raw vCard/VEVENT string to
 * (writing a temp `.vcf`/`.ics` and opening it is called out in the plan as a V1
 * fast-follow, not required here) - all of those return [QrActionResult.NoHandlerAvailable].
 */
class DesktopQrActionLauncher : QrActionLauncher {

    override fun launch(action: QrAction): QrActionResult {
        return when (action) {
            is QrAction.OpenUrl -> browse(action.url)
            is QrAction.SendEmail -> mail(action)
            is QrAction.ShowLocation -> browse(
                "https://maps.google.com/?q=${action.location.latitude},${action.location.longitude}"
            )

            is QrAction.DialPhone -> QrActionResult.NoHandlerAvailable
            is QrAction.SendSms -> QrActionResult.NoHandlerAvailable
            is QrAction.ConnectWifi -> QrActionResult.NoHandlerAvailable
            is QrAction.SaveContact -> QrActionResult.NoHandlerAvailable
            is QrAction.AddCalendarEvent -> QrActionResult.NoHandlerAvailable
            is QrAction.NoAction -> QrActionResult.NoHandlerAvailable
        }
    }

    private fun browse(url: String): QrActionResult {
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            return QrActionResult.NoHandlerAvailable
        }
        return try {
            Desktop.getDesktop().browse(URI(url))
            QrActionResult.Success
        } catch (e: IOException) {
            QrActionResult.Failed(e.message ?: "Failed to open browser")
        } catch (e: UnsupportedOperationException) {
            QrActionResult.NoHandlerAvailable
        }
    }

    private fun mail(action: QrAction.SendEmail): QrActionResult {
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.MAIL)) {
            return QrActionResult.NoHandlerAvailable
        }
        return try {
            val params = buildList {
                action.subject?.let { add("subject=${it.urlEncoded()}") }
                action.body?.let { add("body=${it.urlEncoded()}") }
            }
            val query = if (params.isNotEmpty()) "?" + params.joinToString("&") else ""
            Desktop.getDesktop().mail(URI("mailto:${action.to}$query"))
            QrActionResult.Success
        } catch (e: IOException) {
            QrActionResult.Failed(e.message ?: "Failed to open mail client")
        } catch (e: UnsupportedOperationException) {
            QrActionResult.NoHandlerAvailable
        }
    }

    private fun String.urlEncoded(): String = URLEncoder.encode(this, "UTF-8")
}
