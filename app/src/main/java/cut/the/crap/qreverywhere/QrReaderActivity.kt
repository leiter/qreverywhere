package cut.the.crap.qreverywhere

import android.content.Intent
import android.graphics.PointF
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dlazaro66.qrcodereaderview.QRCodeReaderView


class QrReaderActivity : AppCompatActivity(), QRCodeReaderView.OnQRCodeReadListener {

    private lateinit var mDecoderView: QRCodeReaderView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_reader)
        mDecoderView = findViewById(R.id.qr_decoder_view)
        mDecoderView.setOnQRCodeReadListener(this);
        mDecoderView.setQRDecodingEnabled(true);
        mDecoderView.setAutofocusInterval(2000L);
        mDecoderView.setTorchEnabled(true);
        mDecoderView.setFrontCamera();
        mDecoderView.setBackCamera();
    }

    override fun onPause() {
        super.onPause()
        mDecoderView.stopCamera()
    }

    override fun onResume() {
        super.onResume()
        mDecoderView.startCamera()
    }

    companion object {
        const val EXTRA_QR_DATA = "qr_data"
    }

    override fun onQRCodeRead(text: String?, points: Array<PointF?>?) {
        val data = Intent()
        data.putExtra(EXTRA_QR_DATA, text)
        setResult(RESULT_OK, data)
        finish()
    }
}