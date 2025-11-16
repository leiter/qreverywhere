package cut.the.crap.qreverywhere.shared.camera

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

/**
 * iOS implementation of NativeCameraViewData
 * Would hold AVCaptureSession and related iOS camera components
 */
actual class NativeCameraViewData

/**
 * iOS implementation of native camera view
 * Would use UIViewRepresentable to wrap AVCaptureVideoPreviewLayer
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
    // TODO: Implement using:
    // - UIViewRepresentable wrapper for AVCaptureVideoPreviewLayer
    // - AVCaptureSession for camera management
    // - AVCaptureMetadataOutput for QR detection
    // - Or Vision framework for QR processing

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "iOS Camera Implementation\n\nRequired components:\n• AVCaptureSession\n• AVCaptureVideoPreviewLayer\n• AVCaptureMetadataOutput\n• UIViewRepresentable wrapper",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}