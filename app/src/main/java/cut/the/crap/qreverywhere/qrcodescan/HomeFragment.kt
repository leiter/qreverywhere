package cut.the.crap.qreverywhere.qrcodescan

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.common.util.concurrent.ListenableFuture
import cut.the.crap.qreverywhere.MainActivityViewModel
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.databinding.FragmentHomeBinding
import cut.the.crap.qreverywhere.qrcodedetailview.DetailViewFragment
import cut.the.crap.qreverywhere.qrdelegates.PickQrCodeDelegate
import cut.the.crap.qreverywhere.qrdelegates.PickQrCodeDelegateImpl
import cut.the.crap.qreverywhere.stuff.*
import cut.the.crap.qreverywhere.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home),
    PickQrCodeDelegate by PickQrCodeDelegateImpl(), ActOnQrCode {

    private val activityViewModel by activityViewModels<MainActivityViewModel>()

    private val viewBinding by viewBinding {
        FragmentHomeBinding.bind(requireView())
    }

    private val cameraPermissionLauncher: androidx.activity.result.ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                viewBinding.previewView.visible()
                startCamera()
            } else {
                viewBinding.previewView.gone()
                requireContext().showShortToast(R.string.permission_denied_text)
            }
        }

    private val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> by lazy {
        ProcessCameraProvider.getInstance(requireActivity())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        attachPickQrCodeDelegate(this)
//        val callback: OnBackPressedCallback = object : OnBackPressedCallback(
//            true
//        ) {
//            override fun handleOnBackPressed() {
//                findNavController().navigateUp()//navigate(R.id.action_detailViewFragment_to_qrHistoryFragment)
//            }
//        }
//        requireActivity().onBackPressedDispatcher.addCallback(
//            this,
//            callback
//        )
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

    private fun bindCameraPreview(@NonNull cameraProvider: ProcessCameraProvider) {
        viewBinding.previewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        val preview = Preview.Builder()
            .build()
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
                        override fun onQRCodeFound(qrCode: String?) {
                            qrCode?.let { handleQrCode(qrCode, Acquire.SCANNED) }
                        }

                        override fun qrCodeNotFound() {
                            Log.d("NOT_FOUND", "Did not find qr code.")
                        }
                    })
                )
            }

        val camera: Camera =
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
            qrScanFab.setOnClickListener {
                readQrcodeFromFile()
            }
            handleCameraPermission()
        }
        activityViewModel.startDetailViewQrCodeItem.observe(viewLifecycleOwner) {
            it?.let {
//                findNavController().navigate(R.id.actionOpenDetailViewFromHistory, bundleOf(
//                    DetailViewFragment.ORIGIN_FLAG to DetailViewFragment.FROM_SCAN_QR
//                ))
            }
        }
    }

    private fun handleCameraPermission() {
        if (requireContext().hasPermission(android.Manifest.permission.CAMERA)) {
            viewBinding.previewView.visible()
            startCamera()
        } else {
            viewBinding.previewView.gone()
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    override fun handleQrCode(qrCode: String, @Acquire.Type type: Int) {
        activityViewModel.saveQrItemFromFile(qrCode, resources, type)

        findNavController().navigate(
            R.id.actionOpenDetailViewFromQrScanFragment, bundleOf(
                DetailViewFragment.ORIGIN_FLAG to DetailViewFragment.FROM_SCAN_QR
            )
        )


//        if(determineType(qrCode) != QrCode.UNKNOWN_CONTENT){
//            createOpenIntent(qrCode, requireContext())?.let { intent ->
//                startActivity(intent)
//            } ?: run {
//                //todo inform and display content (callback(text))
//            }
//        }

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_about -> {
                findNavController().navigate(R.id.actionOpenSettingsFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}