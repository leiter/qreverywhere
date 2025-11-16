package cut.the.crap.qreverywhere.shared.camera

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executor

/**
 * Android implementation of PlatformContext
 */
actual class PlatformContext(val androidContext: Context)

/**
 * Android implementation: Get Android Context
 */
@Composable
actual fun rememberPlatformContext(): PlatformContext {
    val context = LocalContext.current
    return androidx.compose.runtime.remember { PlatformContext(context) }
}

/**
 * Android implementation of PlatformLifecycle
 */
actual class PlatformLifecycle(private val lifecycleOwner: LifecycleOwner) {
    actual fun onStart(callback: () -> Unit) {
        lifecycleOwner.lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_START) {
                    callback()
                }
            }
        )
    }

    actual fun onStop(callback: () -> Unit) {
        lifecycleOwner.lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_STOP) {
                    callback()
                }
            }
        )
    }

    actual fun onDestroy(callback: () -> Unit) {
        lifecycleOwner.lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_DESTROY) {
                    callback()
                }
            }
        )
    }
}

/**
 * Android implementation: Get LifecycleOwner
 */
@Composable
actual fun rememberPlatformLifecycle(): PlatformLifecycle {
    val lifecycleOwner = LocalLifecycleOwner.current
    return androidx.compose.runtime.remember { PlatformLifecycle(lifecycleOwner) }
}

/**
 * Android implementation of PlatformExecutor
 */
actual class PlatformExecutor(val executor: Executor)

/**
 * Android implementation: Get main executor
 */
actual fun getPlatformMainExecutor(context: PlatformContext): PlatformExecutor {
    val executor = ContextCompat.getMainExecutor(context.androidContext)
    return PlatformExecutor(executor)
}