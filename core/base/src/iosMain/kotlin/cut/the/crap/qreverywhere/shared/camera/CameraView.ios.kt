package cut.the.crap.qreverywhere.shared.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitViewController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import platform.AVFoundation.AVCaptureDevice
import qreverywhere.shared.generated.resources.Res
import qreverywhere.shared.generated.resources.camera_not_available
import qreverywhere.shared.generated.resources.camera_simulator_hint
import qreverywhere.shared.generated.resources.camera_starting
import platform.AVFoundation.AVMediaTypeVideo

/**
 * iOS Camera View implementation using AVCaptureSession
 * Provides real-time QR code scanning using the device camera
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CameraView(
    modifier: Modifier,
    config: CameraConfig,
    onQrCodeDetected: (QrCodeResult) -> Unit,
    onError: (String) -> Unit
) {
    // Check if camera is available (not available on simulator)
    val hasCamera = remember {
        AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo) != null
    }

    if (!hasCamera) {
        // Show placeholder for simulator or devices without camera
        SimulatorCameraPlaceholder(
            modifier = modifier,
            onQrCodeDetected = onQrCodeDetected,
            onError = onError
        )
        return
    }

    val scope = rememberCoroutineScope()
    var cameraController by remember { mutableStateOf<IosCameraViewController?>(null) }
    var isReady by remember { mutableStateOf(false) }

    // Create the camera controller
    val controller = remember {
        IosCameraViewController(
            onQrCodeDetected = { text ->
                onQrCodeDetected(QrCodeResult(text = text))
            },
            onError = onError
        )
    }

    // Update torch when config changes
    LaunchedEffect(config.enableTorch) {
        cameraController?.setTorchEnabled(config.enableTorch)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Native iOS Camera View
        UIKitViewController(
            factory = {
                cameraController = controller
                isReady = true
                controller
            },
            modifier = Modifier.fillMaxSize(),
            update = { viewController ->
                // Update camera settings if needed
                (viewController as? IosCameraViewController)?.setTorchEnabled(config.enableTorch)
            }
        )

        // Show loading indicator while camera initializes
        if (!isReady) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(Res.string.camera_starting),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    // Cleanup when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            cameraController = null
        }
    }
}

/**
 * Placeholder view for simulator or devices without camera
 * The FAB in ScanScreen provides the "Scan from file" functionality
 */
@Composable
private fun SimulatorCameraPlaceholder(
    modifier: Modifier,
    onQrCodeDetected: (QrCodeResult) -> Unit,
    onError: (String) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "📷",
                style = MaterialTheme.typography.displayLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(Res.string.camera_not_available),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.camera_simulator_hint),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
