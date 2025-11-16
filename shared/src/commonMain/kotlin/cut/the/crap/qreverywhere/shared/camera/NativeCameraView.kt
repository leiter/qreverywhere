package cut.the.crap.qreverywhere.shared.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Native camera view data that holds platform-specific camera components
 */
expect class NativeCameraViewData

/**
 * Platform-specific native camera view integration for Compose
 * This handles the integration of platform-specific camera views into Compose UI
 */
@Composable
expect fun NativeCameraView(
    modifier: Modifier = Modifier,
    cameraConfig: CameraConfig = CameraConfig(),
    lifecycle: PlatformLifecycle,
    onInitialized: (NativeCameraViewData) -> Unit = {},
    onQrCodeDetected: (QrCodeResult) -> Unit = {},
    onError: (String) -> Unit = {}
)