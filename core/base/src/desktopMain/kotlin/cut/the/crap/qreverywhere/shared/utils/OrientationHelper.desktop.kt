package cut.the.crap.qreverywhere.shared.utils

import androidx.compose.runtime.Composable

/**
 * Desktop implementation to get device orientation
 */
@Composable
actual fun getDeviceOrientation(): DeviceOrientation {
    // Desktop doesn't have orientation in the same way as mobile
    // Could potentially check window dimensions
    return DeviceOrientation.LANDSCAPE
}