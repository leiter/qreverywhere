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
    // TODO: Implement using desktop camera libraries
    // Options:
    // - OpenCV for camera capture
    // - Webcam-capture library
    // - JavaCV
    // - Native platform APIs (Windows Media Foundation, macOS AVFoundation, Linux V4L2)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Desktop Camera\nImplementation Required\n\nOptions:\n• OpenCV\n• Webcam-capture library\n• JavaCV\n• Platform-specific APIs",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}