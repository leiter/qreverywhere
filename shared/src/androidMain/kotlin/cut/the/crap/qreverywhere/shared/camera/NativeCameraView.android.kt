package cut.the.crap.qreverywhere.shared.camera

import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Android implementation of NativeCameraViewData
 */
actual class NativeCameraViewData(
    val previewView: PreviewView,
    val cameraProvider: ProcessCameraProvider?,
    val cameraExecutor: ExecutorService
)

/**
 * Android implementation of native camera view using CameraX
 */
@Composable
actual fun NativeCameraView(
    modifier: Modifier,
    cameraConfig: CameraConfig,
    lifecycle: PlatformLifecycle,
    onInitialized: (NativeCameraViewData) -> Unit,
    onQrCodeDetected: (QrCodeResult) -> Unit,
    onError: (String) -> Unit
) {
    val context = rememberPlatformContext()
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val qrCodeDetector = remember { QrCodeDetector() }
    var viewData by remember { mutableStateOf<NativeCameraViewData?>(null) }

    // Setup lifecycle observers
    DisposableEffect(lifecycle) {
        lifecycle.onDestroy {
            cameraProvider?.unbindAll()
            cameraExecutor.shutdown()
            qrCodeDetector.release()
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context.androidContext)
        val mainExecutor = getPlatformMainExecutor(context)

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
            } catch (e: Exception) {
                onError("Failed to initialize camera: ${e.message}")
            }
        }, mainExecutor.executor)

        onDispose {
            cameraProvider?.unbindAll()
            cameraExecutor.shutdown()
            qrCodeDetector.release()
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                implementationMode = PreviewView.ImplementationMode.PERFORMANCE
            }.also { previewView ->
                viewData = NativeCameraViewData(previewView, cameraProvider, cameraExecutor)
                onInitialized(viewData!!)
            }
        },
        update = { previewView ->
            cameraProvider?.let { provider ->
                try {
                    // Get lifecycle owner from the Android-specific lifecycle
                    val lifecycleOwner = (lifecycle as? PlatformLifecycle)?.let {
                        // Access the underlying Android LifecycleOwner
                        // This is a simplified approach - in production, you'd need proper casting
                        context.androidContext as? LifecycleOwner
                    } ?: return@let

                    setupCameraForNativeView(
                        provider = provider,
                        previewView = previewView,
                        lifecycleOwner = lifecycleOwner,
                        config = cameraConfig,
                        cameraExecutor = cameraExecutor,
                        qrCodeDetector = qrCodeDetector,
                        onQrCodeDetected = onQrCodeDetected,
                        onError = onError
                    )
                } catch (e: Exception) {
                    onError("Camera setup failed: ${e.message}")
                }
            }
        }
    )
}

private fun setupCameraForNativeView(
    provider: ProcessCameraProvider,
    previewView: PreviewView,
    lifecycleOwner: LifecycleOwner,
    config: CameraConfig,
    cameraExecutor: ExecutorService,
    qrCodeDetector: QrCodeDetector,
    onQrCodeDetected: (QrCodeResult) -> Unit,
    onError: (String) -> Unit
) {
    provider.unbindAll()

    val cameraSelector = when (config.cameraFacing) {
        CameraFacing.FRONT -> CameraSelector.DEFAULT_FRONT_CAMERA
        CameraFacing.BACK -> CameraSelector.DEFAULT_BACK_CAMERA
    }

    val preview = Preview.Builder()
        .build()
        .also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

    val imageAnalysis = if (config.enableQrDetection) {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { analysis ->
                analysis.setAnalyzer(cameraExecutor, QrCodeAnalyzer(
                    qrCodeDetector = qrCodeDetector,
                    onQrCodeDetected = onQrCodeDetected,
                    onError = onError
                ))
            }
    } else null

    try {
        val camera = if (imageAnalysis != null) {
            provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
        } else {
            provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview
            )
        }

        if (camera.cameraInfo.hasFlashUnit()) {
            camera.cameraControl.enableTorch(config.enableTorch)
        }
    } catch (e: Exception) {
        onError("Failed to bind camera use cases: ${e.message}")
    }
}