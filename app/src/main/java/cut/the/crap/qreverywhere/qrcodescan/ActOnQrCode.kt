package cut.the.crap.qreverywhere.qrcodescan

import cut.the.crap.qreverywhere.stuff.Acquire

interface ActOnQrCode {

    fun handleQrCode(qrCode: String, @Acquire.Type type: Int )

}