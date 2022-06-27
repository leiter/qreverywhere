package cut.the.crap.qreverywhere

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.common.util.concurrent.ListenableFuture
import cut.the.crap.qreverywhere.qrcodescan.QRCodeFoundListener
import cut.the.crap.qreverywhere.qrcodescan.QRCodeImageAnalyzer
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors

class QrReaderActivity : AppCompatActivity(){

    private val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> by lazy {
        ProcessCameraProvider.getInstance(this)
    }
    private lateinit var previewView: PreviewView
    private lateinit var extendedFab: ExtendedFloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_reader)
        extendedFab = findViewById(R.id.qrScanFromFile)
        previewView = findViewById(R.id.qr_decoder_view_view)
        extendedFab.setOnClickListener {
            val data = Intent()
            data.putExtra(EXTRA_START_IMAGE_PICKER, true)
            setResult(RESULT_OK, data)
            finish()
        }
        startCamera()
    }

    private fun startCamera() {
        cameraProviderFuture.addListener({
            try {
                val cameraProvider =
                    cameraProviderFuture.get()
                bindCameraPreview(cameraProvider)
            } catch (e: ExecutionException) {
                Toast.makeText(this, "Error starting camera " + e.message, Toast.LENGTH_SHORT)
                    .show()
            } catch (e: InterruptedException) {
                Toast.makeText(this, "Error starting camera " + e.message, Toast.LENGTH_SHORT)
                    .show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraPreview(@NonNull cameraProvider: ProcessCameraProvider) {
        previewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        val preview = Preview.Builder()
            .build()
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        preview.setSurfaceProvider(previewView.surfaceProvider)

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .setTargetRotation(previewView.display.rotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build().also {
                it.setAnalyzer(Executors.newSingleThreadExecutor(), QRCodeImageAnalyzer(object : QRCodeFoundListener {
                    override fun onQRCodeFound(qrCode: String?) {
                        val data = Intent()
                        data.putExtra(EXTRA_QR_DATA, qrCode)
                        setResult(RESULT_OK, data)
                        finish()
                    }

                    override fun qrCodeNotFound() {
                        Log.d("NOT_FOUND","Did not find qr code.")
                    }
                }))
            }

        val camera: Camera =
            cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, imageAnalysis, preview)
    }

    companion object {
        const val EXTRA_QR_DATA = "qr_data"
        const val EXTRA_START_IMAGE_PICKER = "start_image_picker"
    }

}