package cut.the.crap.qreverywhere.shared.utils

import androidx.compose.runtime.Composable
import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceOrientation

/**
 * iOS implementation to get device orientation
 */
@Composable
actual fun getDeviceOrientation(): DeviceOrientation {
    val orientation = UIDevice.currentDevice.orientation
    return when (orientation) {
        UIDeviceOrientation.UIDeviceOrientationLandscapeLeft,
        UIDeviceOrientation.UIDeviceOrientationLandscapeRight -> DeviceOrientation.LANDSCAPE
        else -> DeviceOrientation.PORTRAIT
    }
}