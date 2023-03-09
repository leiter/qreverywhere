package cut.the.crap.qreverywhere.utils.ui

import android.os.Bundle
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.utils.FROM_CREATE_CONTEXT
import cut.the.crap.qreverywhere.utils.FROM_HISTORY_LIST
import cut.the.crap.qreverywhere.utils.ORIGIN_FLAG

fun selectHistory(destinationId: Int, argument: Bundle?): Boolean {
    val argCheck =
        (argument?.containsKey(ORIGIN_FLAG) ?: false
            && (argument?.getInt(ORIGIN_FLAG) in arrayListOf(FROM_HISTORY_LIST, FROM_CREATE_CONTEXT)
            || (destinationId == R.id.qrFullscreenFragment )))
    return destinationId == R.id.qrHistoryFragment || argCheck
}

val navSelectorCreate = arrayListOf(
    R.id.createEmailQrCodeFragment,
    R.id.createOneLinerFragment,
    R.id.createQrCodeFragment
)