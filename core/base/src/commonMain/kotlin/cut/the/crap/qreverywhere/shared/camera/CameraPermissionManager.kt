package cut.the.crap.qreverywhere.shared.camera

import androidx.compose.runtime.Composable

/**
 * Camera permission states
 */
enum class CameraPermissionState {
    GRANTED,
    DENIED,
    PERMANENTLY_DENIED,
    NOT_REQUESTED
}

/**
 * Platform-specific camera permission manager
 * Handles camera permission requests and checks across different platforms
 */
expect class CameraPermissionManager {
    /**
     * Check if camera permission is granted
     */
    suspend fun hasCameraPermission(): Boolean

    /**
     * Request camera permission
     */
    suspend fun requestCameraPermission(): CameraPermissionState

    /**
     * Get current permission state
     */
    suspend fun getPermissionState(): CameraPermissionState

    /**
     * Open app settings for permission management
     */
    suspend fun openAppSettings()
}

/**
 * Composable to handle camera permission UI
 */
@Composable
expect fun rememberCameraPermissionManager(): CameraPermissionManager