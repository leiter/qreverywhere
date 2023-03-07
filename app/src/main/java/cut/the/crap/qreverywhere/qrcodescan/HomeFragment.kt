package cut.the.crap.qreverywhere.qrcodescan

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
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
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.common.util.concurrent.ListenableFuture
import cut.the.crap.qreverywhere.MainActivityViewModel
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.databinding.FragmentHomeBinding
import cut.the.crap.qreverywhere.qrdelegates.PickQrCodeDelegate
import cut.the.crap.qreverywhere.qrdelegates.PickQrCodeDelegateImpl
import cut.the.crap.qreverywhere.utils.FROM_SCAN_QR
import cut.the.crap.qreverywhere.utils.IntentGenerator.OpenAppSettings
import cut.the.crap.qreverywhere.utils.ORIGIN_FLAG
import cut.the.crap.qreverywhere.utils.gone
import cut.the.crap.qreverywhere.utils.hasPermission
import cut.the.crap.qreverywhere.utils.viewBinding
import cut.the.crap.qreverywhere.utils.visible
import cut.the.crap.qrrepository.Acquire
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home),

    PickQrCodeDelegate by PickQrCodeDelegateImpl(), OnQrCodeRecognition {

    private val activityViewModel by activityViewModels<MainActivityViewModel>()

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
                Toast.makeText(
                    requireContext(),
                    "Error starting camera " + e.message,
                    Toast.LENGTH_SHORT
                )
                    .show()
            } catch (e: InterruptedException) {
                Toast.makeText(
                    requireContext(),
                    "Error starting camera " + e.message,
                    Toast.LENGTH_SHORT
                )
                    .show()
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
            qrScanFab.setText(R.string.qrScanFabTextFromFile)
            qrScanFab.setIconResource(R.drawable.ic_file_open)
            qrScanFab.setOnClickListener { readQrcodeFromFile() }
            requestCameraPermission.setOnClickListener {
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), android.Manifest.permission.CAMERA)) {
                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                } else {
                    startActivity(OpenAppSettings.getIntent())
                }
            }
            requestFileScan.setOnClickListener {
                if (requireContext().hasPermission(permissionByApiVersion())) {
                    readQrcodeFromFile()
                } else {
                    startActivity(OpenAppSettings.getIntent())
                }
            }
        }
        handleCameraPermission()
    }

    private fun handleCameraPermission() {
        if (!requireContext().hasPermission(android.Manifest.permission.CAMERA)
            && !ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), android.Manifest.permission.CAMERA)) {
            viewBinding.previewView.gone()
            viewBinding.centerButton.visible()
        } else if (requireContext().hasPermission(android.Manifest.permission.CAMERA)) {
            activityViewModel.setCameraPermission(true)
            viewBinding.previewView.visible()
            viewBinding.centerButton.gone()
            viewBinding.qrScanFab.show()
            startCamera()
        } else if (!requireContext().hasPermission(android.Manifest.permission.CAMERA)) {
            viewBinding.previewView.gone()
            viewBinding.centerButton.visible()
        }
//        else {
//            activityViewModel.setCameraPermission(false)
//            viewBinding.previewView.gone()
//            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), android.Manifest.permission.CAMERA)) {
//                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
//            } else {
//                startActivity(OpenAppSettings.getIntent())
//            }
//        }
    }

    override fun handleQrCode(qrCode: com.google.zxing.Result, @Acquire.Type type: Int) {
        activityViewModel.saveQrItemFromFile(qrCode.text, resources, type)
        findNavController().navigate(
            R.id.actionOpenDetailViewFromQrScanFragment,
            bundleOf(ORIGIN_FLAG to FROM_SCAN_QR)
        )
    }

}