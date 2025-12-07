package cut.the.crap.qreverywhere.utils.data

import android.net.Uri
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.shared.domain.model.ProtocolPrefix
import cut.the.crap.qreverywhere.shared.domain.model.QrCodeType
import cut.the.crap.qrrepository.QrItem

val QrItem.detailTitle: Int
    get() = when (determineType()) {
        QrCodeType.EMAIL -> R.string.detail_title_email
        QrCodeType.PHONE -> R.string.detail_title_phone
        QrCodeType.WEB_URL -> R.string.detail_title_web
        else -> R.string.detail_title_text
    }

val QrItem.fabLaunchIcon: Int
    get() = when (determineType()) {
        QrCodeType.EMAIL -> R.drawable.ic_mail_outline_white
        QrCodeType.PHONE -> R.drawable.ic_phone_white
        QrCodeType.WEB_URL -> R.drawable.ic_open_in_browser_white
        else -> R.string.detail_title_text
    }

fun QrItem.determineType(): Int {
    val decoded = Uri.decode(textContent)
    return when {
        decoded.startsWith(ProtocolPrefix.TEL) -> QrCodeType.PHONE
        decoded.startsWith(ProtocolPrefix.MAILTO) -> QrCodeType.EMAIL
        decoded.startsWith(ProtocolPrefix.HTTP) ||
            decoded.startsWith(ProtocolPrefix.HTTPS) -> QrCodeType.WEB_URL
//        decoded.startsWith(ProtocolPrefix.SMS) ||
//            decoded.startsWith(ProtocolPrefix.SMSTO) -> QrCodeType.SMS
        isVcard() -> QrCodeType.CONTACT
        else -> QrCodeType.UNKNOWN_CONTENT
    }
}

fun QrItem.isVcard(): Boolean {
    return textContent.startsWith("BEGIN:VCARD") && textContent.endsWith("END:VCARD\n")
}