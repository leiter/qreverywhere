package cut.the.crap.qreverywhere.qrcodescan

import cut.the.crap.qreverywhere.utils.Acquire

interface ActOnQrCode {

    fun handleQrCode(qrCode: com.google.zxing.Result, @Acquire.Type type: Int )

}