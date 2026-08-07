package cut.the.crap.qreverywhere.shared.platform

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import cut.the.crap.qreverywhere.shared.domain.model.MeCard
import cut.the.crap.qreverywhere.shared.domain.model.QrAction
import cut.the.crap.qreverywhere.shared.domain.usecase.QrActionLauncher
import cut.the.crap.qreverywhere.shared.domain.usecase.QrActionResult

/**
 * Android implementation of [QrActionLauncher] using system [Intent]s.
 *
 * Receives an application [Context] (see [AndroidSaveImageToFileUseCase] for the same
 * constructor-injection pattern used elsewhere in this codebase). Since intents are
 * launched from outside an Activity context, every intent gets
 * [Intent.FLAG_ACTIVITY_NEW_TASK].
 */
class AndroidQrActionLauncher(
    private val context: Context
) : QrActionLauncher {

    override fun launch(action: QrAction): QrActionResult {
        return try {
            when (action) {
                is QrAction.OpenUrl -> openUrl(action.url)
                is QrAction.DialPhone -> dialPhone(action.number)
                is QrAction.SendEmail -> sendEmail(action)
                is QrAction.SendSms -> sendSms(action)
                is QrAction.ShowLocation -> showLocation(action.location.toGeoUri())
                is QrAction.SaveContact -> saveContact(action.rawContactText)
                is QrAction.AddCalendarEvent -> addCalendarEvent(action)

                // V1 is reveal-only for WiFi - no auto-connect API is reliable across
                // Android versions without extra permissions/entitlements. The UI is
                // expected to show a WifiCredentialsCard instead of calling launch() for
                // this action, but we return NoHandlerAvailable defensively either way.
                is QrAction.ConnectWifi -> QrActionResult.NoHandlerAvailable

                is QrAction.NoAction -> QrActionResult.NoHandlerAvailable
            }
        } catch (e: ActivityNotFoundException) {
            QrActionResult.Failed(e.message ?: "No app found to handle this action")
        } catch (e: SecurityException) {
            QrActionResult.Failed(e.message ?: "Permission denied")
        }
    }

    private fun startActivity(intent: Intent): QrActionResult {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        return QrActionResult.Success
    }

    private fun openUrl(url: String): QrActionResult {
        return startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun dialPhone(number: String): QrActionResult {
        return startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
    }

    private fun sendEmail(action: QrAction.SendEmail): QrActionResult {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(action.to))
            action.subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
            action.body?.let { putExtra(Intent.EXTRA_TEXT, it) }
        }
        return startActivity(intent)
    }

    private fun sendSms(action: QrAction.SendSms): QrActionResult {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${action.number}")).apply {
            action.body?.let { putExtra("sms_body", it) }
        }
        return startActivity(intent)
    }

    private fun showLocation(geoUri: String): QrActionResult {
        return startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(geoUri)))
    }

    /**
     * [QrAction.SaveContact] carries raw contact text since it may be either MECARD: or
     * vCard format (see [QrAction.SaveContact]'s KDoc). MECARD content is parsed with
     * [MeCard.parse] and routed through [ContactsContract]'s insert-contact intent, the
     * same way the plan originally specified for a structured MeCard. vCard content has
     * no parser in this codebase, so instead of hand-rolling one just to populate the
     * same insert-contact extras, we fire [Intent.ACTION_SEND] with the raw vCard text
     * as [Intent.EXTRA_TEXT] so the user can hand it to Contacts (or any app that accepts
     * shared text) to import - this avoids needing a FileProvider (none is declared in
     * androidApp's manifest today) or a temp-file + ACTION_VIEW dance for a `text/x-vcard`
     * MIME type, at the cost of the target app needing to parse VCARD text itself, which
     * Android's Contacts app share-target does.
     */
    private fun saveContact(rawContactText: String): QrActionResult {
        return if (rawContactText.startsWith("MECARD:")) {
            val meCard = MeCard.parse(rawContactText)
                ?: return QrActionResult.Failed("Could not parse MeCard contact")
            val intent = Intent(Intent.ACTION_INSERT).apply {
                type = android.provider.ContactsContract.Contacts.CONTENT_TYPE
                putExtra(android.provider.ContactsContract.Intents.Insert.NAME, meCard.name)
                meCard.phone?.let { putExtra(android.provider.ContactsContract.Intents.Insert.PHONE, it) }
                meCard.email?.let { putExtra(android.provider.ContactsContract.Intents.Insert.EMAIL, it) }
            }
            startActivity(intent)
        } else if (rawContactText.contains("BEGIN:VCARD")) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/x-vcard"
                putExtra(Intent.EXTRA_TEXT, rawContactText)
            }
            startActivity(Intent.createChooser(intent, null))
        } else {
            QrActionResult.Failed("Unrecognized contact format")
        }
    }

    private fun addCalendarEvent(action: QrAction.AddCalendarEvent): QrActionResult {
        val event = action.event
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, event.title)
            event.description?.let { putExtra(CalendarContract.Events.DESCRIPTION, it) }
            event.location?.let { putExtra(CalendarContract.Events.EVENT_LOCATION, it) }
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.startDateTime.toEpochMilliseconds())
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.endDateTime.toEpochMilliseconds())
            putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, event.isAllDay)
        }
        return startActivity(intent)
    }
}
