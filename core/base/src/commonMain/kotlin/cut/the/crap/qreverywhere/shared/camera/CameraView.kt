package cut.the.crap.qreverywhere.shared.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlin.time.Clock

/**
 * Camera configuration for the view
 */
data class CameraConfig(
    val enableTorch: Boolean = false,
    val enableAutoFocus: Boolean = true,
    val enableQrDetection: Boolean = true,
    val cameraFacing: CameraFacing = CameraFacing.BACK
)

/**
 * Camera facing direction
 */
enum class CameraFacing {
    FRONT, BACK
}

/**
 * QR code detection result
 */
data class QrCodeResult(
    val text: String,
    val format: String? = null,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds()
)

/**
 * Platform-specific camera view composable
 * Displays camera preview with optional QR code scanning
 */
@Composable
expect fun CameraView(
    modifier: Modifier = Modifier,
    config: CameraConfig = CameraConfig(),
    onQrCodeDetected: (QrCodeResult) -> Unit = {},
    onError: (String) -> Unit = {}
)