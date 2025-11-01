package cut.the.crap.qreverywhere.qrcodescan

import android.Manifest.permission.CAMERA
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.common.util.concurrent.ListenableFuture
import cut.the.crap.qreverywhere.MainActivityViewModel
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.databinding.FragmentHomeBinding
import cut.the.crap.qreverywhere.qrdelegates.PickQrCodeDelegate
import cut.the.crap.qreverywhere.qrdelegates.PickQrCodeDelegateImpl
import cut.the.crap.qreverywhere.utils.ui.FROM_SCAN_QR
import cut.the.crap.qreverywhere.utils.data.IntentGenerator.OpenAppSettings
import cut.the.crap.qreverywhere.utils.ui.ORIGIN_FLAG
import cut.the.crap.qreverywhere.utils.ui.hasPermission
import cut.the.crap.qreverywhere.utils.ui.showLongToast
import cut.the.crap.qreverywhere.utils.ui.gone
import cut.the.crap.qreverywhere.utils.ui.viewBinding
import cut.the.crap.qreverywhere.utils.ui.visible
import cut.the.crap.qrrepository.Acquire
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import timber.log.Timber
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors

class HomeFragment : Fragment(R.layout.fragment_home),

    PickQrCodeDelegate by PickQrCodeDelegateImpl(), OnQrCodeRecognition {

    private val activityViewModel by activityViewModel<MainActivityViewModel>()

    private val viewBinding by viewBinding { FragmentHomeBinding.bind(requireView()) }

    private val cameraPermissionLauncher: androidx.activity.result.ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                viewBinding.previewView.visible()
                viewBinding.centerButton.gone()
                viewBinding.qrScanFab.show()
                startCamera()
            } else {
                viewBinding.previewView.gone()
                viewBinding.centerButton.visible()
                viewBinding.qrScanFab.hide()
            }
        }

    private val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> by lazy {
        ProcessCameraProvider.getInstance(requireActivity())
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        attachPickQrCodeDelegate(this, activityViewModel)
    }

    private fun startCamera() {
        cameraProviderFuture.addListener({
            try {
                val cameraProvider =
                    cameraProviderFuture.get()
                bindCameraPreview(cameraProvider)
            } catch (e: ExecutionException) {
                requireContext().showLongToast(R.string.error_starting_camera, e)
            } catch (e: InterruptedException) {
                requireContext().showLongToast(R.string.error_starting_camera, e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun bindCameraPreview(cameraProvider: ProcessCameraProvider) {
        viewBinding.previewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        val preview = Preview.Builder().build()
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        preview.setSurfaceProvider(viewBinding.previewView.surfaceProvider)

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .setTargetRotation(resources.configuration.orientation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build().also {
                it.setAnalyzer(
                    Executors.newSingleThreadExecutor(),
                    QRCodeImageAnalyzer(object : QRCodeFoundListener {
                        override fun onQRCodeFound(qrCode: com.google.zxing.Result) {
                            handleQrCode(qrCode, Acquire.SCANNED)
                        }

                        override fun qrCodeNotFound() {
                            Timber.d("Did not find qr code.")
                        }
                    })
                )
            }

        cameraProvider.bindToLifecycle(
            viewLifecycleOwner,
            cameraSelector,
            imageAnalysis,
            preview
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewBinding) {
            qrScanFab.setOnClickListener { readQrcodeFromFile() }
            requestCameraPermission.setOnClickListener {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(), CAMERA)) {
                    startActivity(OpenAppSettings.getIntent())
                } else {
                    cameraPermissionLauncher.launch(CAMERA)
                }
            }
            requestFileScan.setOnClickListener { readQrcodeFromFile() }
        }
        handleCameraPermission()
    }

    private fun handleCameraPermission() {
        if (!requireContext().hasPermission(CAMERA)) {
            viewBinding.previewView.gone()
            viewBinding.centerButton.visible()
        } else if (requireContext().hasPermission(CAMERA)) {
            viewBinding.previewView.visible()
            viewBinding.centerButton.gone()
            viewBinding.qrScanFab.show()
            startCamera()
        }
    }

    override fun handleQrCode(qrCode: com.google.zxing.Result, @Acquire.Type type: Int) {
        activityViewModel.saveQrItemFromFile(qrCode.text, type)
        findNavController().navigate(
            R.id.actionOpenDetailViewFromQrScanFragment,
            bundleOf(ORIGIN_FLAG to FROM_SCAN_QR)
        )
    }

}

