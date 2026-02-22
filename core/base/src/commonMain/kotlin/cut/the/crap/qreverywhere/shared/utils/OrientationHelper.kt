package cut.the.crap.qreverywhere.shared.utils

import androidx.compose.runtime.Composable

/**
 * Device orientation
 */
enum class DeviceOrientation {
    PORTRAIT, LANDSCAPE
}

/**
 * Platform-specific helper to get device orientation
 */
@Composable
expect fun getDeviceOrientation(): DeviceOrientation