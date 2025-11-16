package cut.the.crap.qreverywhere.shared.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * Multiplatform Camera View that properly abstracts platform-specific implementations
 * This is the recommended approach for Compose Multiplatform camera integration
 */
@Composable
fun CameraViewMultiplatform(
    modifier: Modifier = Modifier,
    config: CameraConfig = CameraConfig(),
    onQrCodeDetected: (QrCodeResult) -> Unit = {},
    onError: (String) -> Unit = {}
) {
    val permissionManager = rememberCameraPermissionManager()
    val lifecycle = rememberPlatformLifecycle()
    var hasPermission by remember { mutableStateOf(false) }
    var cameraViewData by remember { mutableStateOf<NativeCameraViewData?>(null) }

    // Check permission
    LaunchedEffect(Unit) {
        hasPermission = permissionManager.hasCameraPermission()
        if (!hasPermission) {
            val state = permissionManager.requestCameraPermission()
            hasPermission = state == CameraPermissionState.GRANTED
        }
    }

    if (hasPermission) {
        NativeCameraView(
            modifier = modifier,
            cameraConfig = config,
            lifecycle = lifecycle,
            onInitialized = { viewData ->
                cameraViewData = viewData
            },
            onQrCodeDetected = onQrCodeDetected,
            onError = onError
        )
    } else {
        // Permission not granted - handled by parent composable
        onError("Camera permission not granted")
    }
}