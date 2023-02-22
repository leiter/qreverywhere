package cut.the.crap.qreverywhere.qrcodescan

import cut.the.crap.qrrepository.Acquire

interface OnQrCodeRecognition {

    fun handleQrCode(qrCode: com.google.zxing.Result, @Acquire.Type type: Int )

}