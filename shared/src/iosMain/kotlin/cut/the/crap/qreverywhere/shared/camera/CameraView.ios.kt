package cut.the.crap.qreverywhere.shared.camera

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

@Composable
actual fun CameraView(
    modifier: Modifier,
    config: CameraConfig,
    onQrCodeDetected: (QrCodeResult) -> Unit,
    onError: (String) -> Unit
) {
    // TODO: Implement using AVFoundation
    // - AVCaptureSession for camera
    // - AVCaptureVideoDataOutput for video frames
    // - AVCaptureMetadataOutput for QR detection
    // - UIViewRepresentable wrapper for SwiftUI/Compose integration

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "iOS Camera\nImplementation Required\n\nUse AVFoundation with:\n• AVCaptureSession\n• AVCaptureMetadataOutput\n• Vision framework for QR detection",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}