package cut.the.crap.qreverywhere.shared.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class CameraPermissionManager(private val context: Context) {

    actual suspend fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    actual suspend fun requestCameraPermission(): CameraPermissionState {
        val activity = context as? Activity ?: return CameraPermissionState.DENIED

        if (hasCameraPermission()) {
            return CameraPermissionState.GRANTED
        }

        return suspendCancellableCoroutine { continuation ->

            // Check if we should show request permission rationale
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.CAMERA
            )) {
                // Permission was denied before, but not permanently
                continuation.resume(CameraPermissionState.DENIED)
            } else {
                // Request permission
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST_CODE
                )

                // Note: In a real implementation, you'd need to handle the permission result
                // through onRequestPermissionsResult callback
                // For now, we'll check the permission state again
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED

                continuation.resume(
                    if (granted) CameraPermissionState.GRANTED
                    else CameraPermissionState.DENIED
                )
            }
        }
    }

    actual suspend fun getPermissionState(): CameraPermissionState {
        val hasPermission = hasCameraPermission()
        if (hasPermission) return CameraPermissionState.GRANTED

        val activity = context as? Activity ?: return CameraPermissionState.NOT_REQUESTED

        return if (ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.CAMERA
        )) {
            CameraPermissionState.DENIED
        } else {
            // Either not requested yet or permanently denied
            CameraPermissionState.NOT_REQUESTED
        }
    }

    actual suspend fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }
}

@Composable
actual fun rememberCameraPermissionManager(): CameraPermissionManager {
    val context = LocalContext.current
    return remember { CameraPermissionManager(context) }
}