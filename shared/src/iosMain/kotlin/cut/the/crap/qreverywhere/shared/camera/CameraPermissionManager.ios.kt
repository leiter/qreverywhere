package cut.the.crap.qreverywhere.shared.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import kotlin.coroutines.resume

/**
 * iOS implementation of CameraPermissionManager using AVFoundation
 */
actual class CameraPermissionManager {

    /**
     * Check if camera permission is currently granted
     */
    actual suspend fun hasCameraPermission(): Boolean {
        val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
        return status == AVAuthorizationStatusAuthorized
    }

    /**
     * Request camera permission from the user
     * Returns the resulting permission state after the request
     */
    actual suspend fun requestCameraPermission(): CameraPermissionState {
        val currentStatus = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)

        return when (currentStatus) {
            AVAuthorizationStatusAuthorized -> CameraPermissionState.GRANTED
            AVAuthorizationStatusDenied -> CameraPermissionState.PERMANENTLY_DENIED
            AVAuthorizationStatusRestricted -> CameraPermissionState.PERMANENTLY_DENIED
            AVAuthorizationStatusNotDetermined -> {
                // Request permission
                val granted = requestPermissionAsync()
                if (granted) {
                    CameraPermissionState.GRANTED
                } else {
                    CameraPermissionState.DENIED
                }
            }
            else -> CameraPermissionState.NOT_REQUESTED
        }
    }

    /**
     * Get the current permission state without requesting
     */
    actual suspend fun getPermissionState(): CameraPermissionState {
        val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)

        return when (status) {
            AVAuthorizationStatusAuthorized -> CameraPermissionState.GRANTED
            AVAuthorizationStatusDenied -> CameraPermissionState.PERMANENTLY_DENIED
            AVAuthorizationStatusRestricted -> CameraPermissionState.PERMANENTLY_DENIED
            AVAuthorizationStatusNotDetermined -> CameraPermissionState.NOT_REQUESTED
            else -> CameraPermissionState.NOT_REQUESTED
        }
    }

    /**
     * Open the iOS Settings app to the app's settings page
     * where the user can manually enable camera permission
     */
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun openAppSettings() {
        val settingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
        if (settingsUrl != null && UIApplication.sharedApplication.canOpenURL(settingsUrl)) {
            UIApplication.sharedApplication.openURL(
                settingsUrl,
                options = emptyMap<Any?, Any>(),
                completionHandler = null
            )
        }
    }

    /**
     * Helper function to request camera permission asynchronously
     */
    private suspend fun requestPermissionAsync(): Boolean = suspendCancellableCoroutine { continuation ->
        AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
            if (continuation.isActive) {
                continuation.resume(granted)
            }
        }
    }
}

/**
 * Composable to remember and provide a CameraPermissionManager instance
 */
@Composable
actual fun rememberCameraPermissionManager(): CameraPermissionManager {
    return remember { CameraPermissionManager() }
}