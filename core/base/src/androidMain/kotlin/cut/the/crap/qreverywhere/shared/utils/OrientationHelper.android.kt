package cut.the.crap.qreverywhere.shared.utils

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

/**
 * Android implementation to get device orientation
 */
@Composable
actual fun getDeviceOrientation(): DeviceOrientation {
    val configuration = LocalConfiguration.current
    return when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> DeviceOrientation.LANDSCAPE
        else -> DeviceOrientation.PORTRAIT
    }
}