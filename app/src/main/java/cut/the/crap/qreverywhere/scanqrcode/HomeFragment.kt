package cut.the.crap.qreverywhere.scanqrcode

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.common.util.concurrent.ListenableFuture
import cut.the.crap.qreverywhere.MainActivityViewModel
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.databinding.FragmentHomeBinding
import cut.the.crap.qreverywhere.qrdelegates.ActOnQrCode
import cut.the.crap.qreverywhere.qrdelegates.PickQrCodeDelegate
import cut.the.crap.qreverywhere.qrdelegates.PickQrCodeDelegateImpl
import cut.the.crap.qreverywhere.stuff.createIntent
import cut.the.crap.qreverywhere.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home),
//    CameraReadDelegate by CameraReadDelegateImpl(),
        PickQrCodeDelegate by PickQrCodeDelegateImpl(), ActOnQrCode
{
    private val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> by lazy {
        ProcessCameraProvider.getInstance(requireActivity())
    }
    private lateinit var previewView: PreviewView


    private val viewBinding by viewBinding {
        FragmentHomeBinding.bind(requireView())
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
//        attachCameraReadDelegate(this)
        attachPickQrCodeDelegate(this)
    }

    private fun startCamera() {
        cameraProviderFuture.addListener({
            try {
                val cameraProvider =
                    cameraProviderFuture.get()
                bindCameraPreview(cameraProvider)
            } catch (e: ExecutionException) {
                Toast.makeText(requireContext(), "Error starting camera " + e.message, Toast.LENGTH_SHORT)
                    .show()
            } catch (e: InterruptedException) {
                Toast.makeText(requireContext(), "Error starting camera " + e.message, Toast.LENGTH_SHORT)
                    .show()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
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
            .setTargetRotation(resources.configuration.orientation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build().also {
                it.setAnalyzer(Executors.newSingleThreadExecutor(), QRCodeImageAnalyzer(object : QRCodeFoundListener {
                    override fun onQRCodeFound(qrCode: String?) {
                        qrCode?.let { handleQrCode(qrCode) }

                    }

                    override fun qrCodeNotFound() {
                        Log.d("NOT_FOUND","Did not find qr code.")
                    }
                }))
            }

        val camera: Camera =
            cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, imageAnalysis, preview)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        previewView = viewBinding.qrDecoderView

        with(viewBinding) {
                qrScanFab.setText(R.string.qrScanFabTextFromFile)
                qrScanFab.setIconResource(R.drawable.ic_file_open)
                qrScanFab.setOnClickListener {
                    readQrcodeFromFile()
                }
                startCamera()
        }

    }

    override fun handleQrCode(qrCode: String) {
        createIntent(qrCode, requireContext())?.let { intent ->
            startActivity(intent)
        } ?: run {
            //todo inform and display content   (callback(text))
        }
        Log.e("QRCODE", qrCode ?: "NOT FOUND")
    }

}