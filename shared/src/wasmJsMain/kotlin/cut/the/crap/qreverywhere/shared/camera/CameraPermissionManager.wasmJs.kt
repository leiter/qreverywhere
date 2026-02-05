package cut.the.crap.qreverywhere.shared.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Web camera permission manager
 * Browser handles permissions via getUserMedia prompts
 */
actual class CameraPermissionManager {
    actual suspend fun hasCameraPermission(): Boolean {
        // For web, we'll assume permission can be requested when needed
        // Browser will prompt for permission when getUserMedia is called
        return true
    }

    actual suspend fun requestCameraPermission(): CameraPermissionState {
        // Browser handles this via getUserMedia
        return CameraPermissionState.GRANTED
    }

    actual suspend fun getPermissionState(): CameraPermissionState {
        return CameraPermissionState.GRANTED
    }

    actual suspend fun openAppSettings() {
        // Not applicable for web
    }
}

@Composable
actual fun rememberCameraPermissionManager(): CameraPermissionManager {
    return remember { CameraPermissionManager() }
}
