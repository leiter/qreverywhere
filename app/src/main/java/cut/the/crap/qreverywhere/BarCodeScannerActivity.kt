package cut.the.crap.qreverywhere

import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import cut.the.crap.qreverywhere.databinding.ActivityBarCodeScannerBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BarCodeScannerActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var viewBindings: ActivityBarCodeScannerBinding

    private lateinit var cameraExecutorService: ExecutorService

    @androidx.camera.core.ExperimentalGetImage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBindings = ActivityBarCodeScannerBinding.inflate(layoutInflater)
        setContentView(viewBindings.root)

        cameraExecutorService = Executors.newSingleThreadExecutor()
        startCamera()
    }

    override fun onDestroy() {
        super.onDestroy()

        cameraExecutorService.shutdown()
    }

    @androidx.camera.core.ExperimentalGetImage
    private fun startCamera(){

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(
            {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder()
                    .build()
                    .also {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            it.setSurfaceProvider(viewBindings.viewFinder.surfaceProvider)
                        }
                    }

                val barcodeAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(
                            cameraExecutorService,
                            BarCodeAnalyzer{ barcodes ->
                                if( barcodes.isNotEmpty() ) {

                                    val intent = Intent()
                                    intent.putExtra(
                                        RESULT_BARCODE_RAW_VALUE,
                                        barcodes.first().rawValue
                                    )

                                    setResult(RESULT_OK, intent)

                                    cameraExecutorService.shutdown()

                                    finish()
                                }
                            }
                        )
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()

                    cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        barcodeAnalyzer
                    )

                }
                catch (e: Exception){}
            },

            ContextCompat.getMainExecutor(this)
        )
    }

    companion object{
        const val RESULT_BARCODE_RAW_VALUE = "barcode_raw_value"
    }
}


