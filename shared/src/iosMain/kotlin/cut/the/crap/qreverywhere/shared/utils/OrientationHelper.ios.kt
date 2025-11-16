package cut.the.crap.qreverywhere.shared.utils

import androidx.compose.runtime.Composable

/**
 * iOS implementation to get device orientation
 */
@Composable
actual fun getDeviceOrientation(): DeviceOrientation {
    // TODO: Implement using UIDevice.current.orientation
    // For now, return portrait as default
    return DeviceOrientation.PORTRAIT
}