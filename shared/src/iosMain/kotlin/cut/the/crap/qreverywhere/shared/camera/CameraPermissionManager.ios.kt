package cut.the.crap.qreverywhere.shared.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

actual class CameraPermissionManager {
    actual suspend fun hasCameraPermission(): Boolean {
        // TODO: Implement using AVFoundation AVCaptureDevice authorization status
        return false
    }

    actual suspend fun requestCameraPermission(): CameraPermissionState {
        // TODO: Implement using AVCaptureDevice.requestAccess(for: .video)
        return CameraPermissionState.NOT_REQUESTED
    }

    actual suspend fun getPermissionState(): CameraPermissionState {
        // TODO: Check AVAuthorizationStatus
        return CameraPermissionState.NOT_REQUESTED
    }

    actual suspend fun openAppSettings() {
        // TODO: Open iOS Settings app
        // UIApplication.shared.open(URL(string: UIApplication.openSettingsURLString))
    }
}

@Composable
actual fun rememberCameraPermissionManager(): CameraPermissionManager {
    return remember { CameraPermissionManager() }
}