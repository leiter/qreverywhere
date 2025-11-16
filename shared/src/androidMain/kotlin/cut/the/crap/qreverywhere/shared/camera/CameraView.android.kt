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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
actual fun CameraView(
    modifier: Modifier,
    config: CameraConfig,
    onQrCodeDetected: (QrCodeResult) -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val qrCodeDetector = remember { QrCodeDetector() }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    // Camera permission check
    val permissionManager = rememberCameraPermissionManager()
    var hasPermission by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        hasPermission = permissionManager.hasCameraPermission()
        if (!hasPermission) {
            onError("Camera permission not granted")
        }
    }

    if (hasPermission) {
        DisposableEffect(lifecycleOwner) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    cameraProvider = cameraProviderFuture.get()
                } catch (e: Exception) {
                    onError("Failed to initialize camera: ${e.message}")
                }
            }, ContextCompat.getMainExecutor(context))

            onDispose {
                // Properly clean up camera resources
                try {
                    cameraProvider?.unbindAll()
                    previewView?.releasePointerCapture()
                    cameraExecutor.shutdown()
                    qrCodeDetector.release()
                } catch (e: Exception) {
                    // Ignore cleanup exceptions
                }
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
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }.also { view ->
                    previewView = view
                }
            },
            update = { view ->
                cameraProvider?.let { provider ->
                    try {
                        setupCamera(
                            provider = provider,
                            previewView = view,
                            lifecycleOwner = lifecycleOwner,
                            config = config,
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
}

private fun setupCamera(
    provider: ProcessCameraProvider,
    previewView: PreviewView,
    lifecycleOwner: LifecycleOwner,
    config: CameraConfig,
    cameraExecutor: ExecutorService,
    qrCodeDetector: QrCodeDetector,
    onQrCodeDetected: (QrCodeResult) -> Unit,
    onError: (String) -> Unit
) {
    try {
        // Unbind all use cases before rebinding
        provider.unbindAll()

        // Select camera
        val cameraSelector = when (config.cameraFacing) {
            CameraFacing.FRONT -> CameraSelector.DEFAULT_FRONT_CAMERA
            CameraFacing.BACK -> CameraSelector.DEFAULT_BACK_CAMERA
        }

        // Check if camera is available
        if (!provider.hasCamera(cameraSelector)) {
            onError("No camera available")
            return
        }

        // Setup preview
        val preview = Preview.Builder()
            .build()

        // Setup image analysis for QR code detection
        val imageAnalysis = if (config.enableQrDetection) {
            ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetRotation(previewView.display?.rotation ?: 0)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor, QrCodeAnalyzer(
                        qrCodeDetector = qrCodeDetector,
                        onQrCodeDetected = onQrCodeDetected,
                        onError = onError
                    ))
                }
        } else null

        // Bind use cases to camera
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

        // Set surface provider after binding
        preview.setSurfaceProvider(previewView.surfaceProvider)

        // Configure torch if needed and available
        if (config.enableTorch && camera.cameraInfo.hasFlashUnit()) {
            camera.cameraControl.enableTorch(true)
        }
    } catch (e: Exception) {
        onError("Failed to setup camera: ${e.message}")
        e.printStackTrace()
    }
}