package cut.the.crap.qreverywhere.qrcodescan

interface QRCodeFoundListener {
    fun onQRCodeFound(qrCode: String?)
    fun qrCodeNotFound()
}