package cut.the.crap.qreverywhere.shared.domain.model

/**
 * Represents the concrete system action a piece of QR code content resolves to
 * (dial a number, open a URL, send an email, etc.), used to drive the
 * "Test"/"Open" action feature on Detail and Create screens.
 */
sealed class QrAction {
    data class OpenUrl(val url: String) : QrAction()
    data class DialPhone(val number: String) : QrAction()
    data class SendEmail(val to: String, val subject: String?, val body: String?) : QrAction()
    data class SendSms(val number: String, val body: String?) : QrAction()
    data class ConnectWifi(val credentials: WifiCredentials) : QrAction()

    /**
     * Holds the raw contact text, since the existing domain parsers only cover one format
     * each: [MeCard.parse] only accepts `MECARD:`-prefixed content, while vCard content
     * (`BEGIN:VCARD...END:VCARD`, detected via [QrItem.isVcard]) has no corresponding
     * `MeCard`-producing parser in this codebase. Rather than force vCard text through the
     * MeCard model (or drop vCard support here), this variant carries whichever raw format
     * matched - vCard or MECARD - so the platform launcher can act on the text directly
     * (e.g. hand it to the OS as-is, or run format-specific parsing itself).
     */
    data class SaveContact(val rawContactText: String) : QrAction()

    data class AddCalendarEvent(val event: CalendarEvent) : QrAction()
    data class ShowLocation(val location: GeoLocation) : QrAction()
    data class NoAction(val text: String) : QrAction()
}

/**
 * Resolves this raw QR content string to the [QrAction] it represents.
 * Reuses [QrItem.determineType]/[ProtocolPrefix] for detection and the existing
 * `parse()` helpers on [WifiCredentials]/[MeCard]/[GeoLocation]/[CalendarEvent] for
 * structured extraction - no new parsing logic here, just dispatch.
 */
fun String.toQrAction(): QrAction {
    // Wrap in a throwaway QrItem to reuse the existing determineType()/isVcard() logic
    // without duplicating its prefix-matching rules here.
    val probe = QrItem(
        textContent = this,
        acquireType = AcquireType.SCANNED,
        timestamp = kotlinx.datetime.Instant.DISTANT_PAST
    )

    return when (probe.determineType()) {
        QrCodeType.PHONE -> {
            QrAction.DialPhone(number = this.removePrefix(ProtocolPrefix.TEL))
        }

        QrCodeType.SMS -> {
            val number = when {
                this.startsWith(ProtocolPrefix.SMSTO) -> this.removePrefix(ProtocolPrefix.SMSTO)
                this.startsWith(ProtocolPrefix.SMS) -> this.removePrefix(ProtocolPrefix.SMS)
                else -> this
            }
            // Neither ProtocolPrefix nor CreateTextScreen's sms/smsto builder supports a
            // body/message param in this codebase - sms/smsto QR content is just the number.
            QrAction.SendSms(number = number, body = null)
        }

        QrCodeType.EMAIL -> {
            val withoutPrefix = this.removePrefix(ProtocolPrefix.MAILTO)
            val to = withoutPrefix.substringBefore("?")
            val query = withoutPrefix.substringAfter("?", missingDelimiterValue = "")
            var subject: String? = null
            var body: String? = null
            if (query.isNotEmpty()) {
                for (param in query.split("&")) {
                    when {
                        param.startsWith("subject=") ->
                            subject = decodePercentEncoded(param.substringAfter("subject="))
                        param.startsWith("body=") ->
                            body = decodePercentEncoded(param.substringAfter("body="))
                    }
                }
            }
            QrAction.SendEmail(to = to, subject = subject, body = body)
        }

        QrCodeType.WEB_URL -> QrAction.OpenUrl(url = this)

        QrCodeType.WIFI -> {
            WifiCredentials.parse(this)?.let { QrAction.ConnectWifi(it) } ?: QrAction.NoAction(this)
        }

        QrCodeType.CONTACT -> QrAction.SaveContact(rawContactText = this)

        QrCodeType.MECARD -> QrAction.SaveContact(rawContactText = this)

        QrCodeType.CALENDAR -> {
            CalendarEvent.parse(this)?.let { QrAction.AddCalendarEvent(it) } ?: QrAction.NoAction(this)
        }

        QrCodeType.LOCATION -> {
            GeoLocation.parse(this)?.let { QrAction.ShowLocation(it) } ?: QrAction.NoAction(this)
        }

        else -> QrAction.NoAction(this)
    }
}

fun QrItem.toQrAction(): QrAction = textContent.toQrAction()

/**
 * Short human-readable label describing what this action will do, used as the message
 * for the Snackbar-with-action confirmation flow on Detail/Create screens. Plain Kotlin
 * string templates (not compose-resource lookups) match the existing precedent for
 * building display strings from domain data - see [GeoLocation.toDisplayString].
 */
fun QrAction.snackbarLabel(): String = when (this) {
    is QrAction.OpenUrl -> "Open $url"
    is QrAction.DialPhone -> "Call $number"
    is QrAction.SendEmail -> "Send email to $to"
    is QrAction.SendSms -> "Send SMS to $number"
    is QrAction.ConnectWifi -> "Connect to ${credentials.ssid}"
    is QrAction.SaveContact -> "Save contact"
    is QrAction.AddCalendarEvent -> "Add to calendar: ${event.title}"
    is QrAction.ShowLocation -> "Show location"
    is QrAction.NoAction -> ""
}

/**
 * Basic percent-decoding for mailto: query params (subject=/body=), mirroring the
 * limited decodeUrlComponent() approach already used privately in QrItem.kt (that function
 * is private to its file, so it can't be reused directly here).
 */
private fun decodePercentEncoded(text: String): String {
    return text
        .replace("+", " ")
        .replace("%20", " ")
        .replace("%3A", ":")
        .replace("%2F", "/")
        .replace("%3F", "?")
        .replace("%3D", "=")
        .replace("%26", "&")
        .replace("%23", "#")
        .replace("%0A", "\n")
        .replace("%0D", "\r")
        .replace("%25", "%")
}
