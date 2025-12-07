package cut.the.crap.qreverywhere.shared.camera

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSURL
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusDenied
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHAuthorizationStatusNotDetermined
import platform.Photos.PHAuthorizationStatusRestricted
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import kotlin.coroutines.resume

/**
 * Photo library permission states
 */
enum class PhotoLibraryPermissionState {
    GRANTED,
    LIMITED,  // iOS 14+ - user granted access to selected photos only
    DENIED,
    RESTRICTED,
    NOT_REQUESTED
}

/**
 * iOS Photo Library Permission Manager
 * Handles permission requests for accessing the photo library
 */
@OptIn(ExperimentalForeignApi::class)
class PhotoLibraryPermissionManager {

    /**
     * Check if photo library permission is granted (full or limited access)
     */
    fun hasPhotoLibraryPermission(): Boolean {
        val status = PHPhotoLibrary.authorizationStatus()
        return status == PHAuthorizationStatusAuthorized || status == PHAuthorizationStatusLimited
    }

    /**
     * Get current permission state without requesting
     */
    fun getPermissionState(): PhotoLibraryPermissionState {
        return when (PHPhotoLibrary.authorizationStatus()) {
            PHAuthorizationStatusAuthorized -> PhotoLibraryPermissionState.GRANTED
            PHAuthorizationStatusLimited -> PhotoLibraryPermissionState.LIMITED
            PHAuthorizationStatusDenied -> PhotoLibraryPermissionState.DENIED
            PHAuthorizationStatusRestricted -> PhotoLibraryPermissionState.RESTRICTED
            PHAuthorizationStatusNotDetermined -> PhotoLibraryPermissionState.NOT_REQUESTED
            else -> PhotoLibraryPermissionState.NOT_REQUESTED
        }
    }

    /**
     * Request photo library permission
     * Returns the resulting permission state
     */
    suspend fun requestPermission(): PhotoLibraryPermissionState {
        val currentStatus = PHPhotoLibrary.authorizationStatus()

        return when (currentStatus) {
            PHAuthorizationStatusAuthorized -> PhotoLibraryPermissionState.GRANTED
            PHAuthorizationStatusLimited -> PhotoLibraryPermissionState.LIMITED
            PHAuthorizationStatusDenied -> PhotoLibraryPermissionState.DENIED
            PHAuthorizationStatusRestricted -> PhotoLibraryPermissionState.RESTRICTED
            PHAuthorizationStatusNotDetermined -> {
                requestPermissionAsync()
            }
            else -> PhotoLibraryPermissionState.NOT_REQUESTED
        }
    }

    /**
     * Request permission asynchronously using suspendCancellableCoroutine
     */
    private suspend fun requestPermissionAsync(): PhotoLibraryPermissionState =
        suspendCancellableCoroutine { continuation ->
            PHPhotoLibrary.requestAuthorization { status ->
                val state = when (status) {
                    PHAuthorizationStatusAuthorized -> PhotoLibraryPermissionState.GRANTED
                    PHAuthorizationStatusLimited -> PhotoLibraryPermissionState.LIMITED
                    PHAuthorizationStatusDenied -> PhotoLibraryPermissionState.DENIED
                    PHAuthorizationStatusRestricted -> PhotoLibraryPermissionState.RESTRICTED
                    else -> PhotoLibraryPermissionState.DENIED
                }
                if (continuation.isActive) {
                    continuation.resume(state)
                }
            }
        }

    /**
     * Open iOS Settings app to manage photo library permission
     */
    fun openSettings() {
        val settingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
        if (settingsUrl != null && UIApplication.sharedApplication.canOpenURL(settingsUrl)) {
            UIApplication.sharedApplication.openURL(
                settingsUrl,
                options = emptyMap<Any?, Any>(),
                completionHandler = null
            )
        }
    }
}
