package cut.the.crap.qreverywhere.utils.data

import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.utils.QrCodeType
import cut.the.crap.qreverywhere.utils.determineType
import cut.the.crap.qrrepository.QrItem

val QrItem.detailTitle: Int
    get() = when (determineType(textContent)) {
        QrCodeType.EMAIL -> R.string.detail_title_email
        QrCodeType.PHONE -> R.string.detail_title_phone
        QrCodeType.WEB_URL -> R.string.detail_title_web
        else -> R.string.detail_title_text
    }

val QrItem.fabLaunchIcon: Int
    get() = when (determineType(textContent)) {
        QrCodeType.EMAIL -> R.drawable.ic_mail_outline_white
        QrCodeType.PHONE -> R.drawable.ic_phone_white
        QrCodeType.WEB_URL -> R.drawable.ic_open_in_browser_white
        else -> R.string.detail_title_text
    }