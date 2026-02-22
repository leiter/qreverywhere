package cut.the.crap.qreverywhere.shared.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

actual class CameraPermissionManager {
    actual suspend fun hasCameraPermission(): Boolean {
        // Desktop usually doesn't require explicit permission requests
        // Camera access is typically handled by the OS at runtime
        return true
    }

    actual suspend fun requestCameraPermission(): CameraPermissionState {
        // Desktop platforms typically don't have a permission system like mobile
        return CameraPermissionState.GRANTED
    }

    actual suspend fun getPermissionState(): CameraPermissionState {
        // Desktop platforms typically grant permissions automatically
        return CameraPermissionState.GRANTED
    }

    actual suspend fun openAppSettings() {
        // Desktop platforms don't have app-specific settings
        // Could potentially open system camera settings
    }
}

@Composable
actual fun rememberCameraPermissionManager(): CameraPermissionManager {
    return remember { CameraPermissionManager() }
}