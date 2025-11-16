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
 * Desktop implementation of NativeCameraViewData
 * Would hold webcam capture session and related desktop camera components
 */
actual class NativeCameraViewData

/**
 * Desktop implementation of native camera view
 * Would use SwingPanel or similar to integrate webcam capture
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
    // TODO: Implement using one of:
    // - OpenCV for Java/JVM
    // - Webcam-capture library
    // - JavaCV
    // - Native platform APIs via JNI
    // Wrapped in SwingPanel for Compose Desktop integration

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Desktop Camera Implementation\n\nRequired components:\n• Webcam-capture library\n• Or OpenCV for JVM\n• SwingPanel wrapper\n• ZXing for QR detection",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}