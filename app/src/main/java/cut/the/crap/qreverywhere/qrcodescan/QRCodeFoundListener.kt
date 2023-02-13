package cut.the.crap.qreverywhere.qrcodescan

interface QRCodeFoundListener {
    fun onQRCodeFound(qrCode: com.google.zxing.Result)
    fun qrCodeNotFound()
}