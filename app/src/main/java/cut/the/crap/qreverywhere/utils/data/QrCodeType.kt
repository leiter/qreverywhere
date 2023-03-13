package cut.the.crap.qreverywhere.utils.data

import androidx.annotation.IntDef

object QrCodeType {
    const val EMAIL = 0
    const val PHONE = 1
    const val WEB_URL = 2
    const val SMS = 3
    const val CONTACT = 4
    const val UNKNOWN_CONTENT = 999

    @IntDef(EMAIL, PHONE, WEB_URL, CONTACT, UNKNOWN_CONTENT)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Type
}