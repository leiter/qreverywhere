package cut.the.crap.qreverywhere.shared.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Web platform context - minimal implementation
 */
actual class PlatformContext

@Composable
actual fun rememberPlatformContext(): PlatformContext {
    return remember { PlatformContext() }
}

/**
 * Web platform lifecycle using Page Visibility API
 */
actual class PlatformLifecycle {
    private var onStartCallback: (() -> Unit)? = null
    private var onStopCallback: (() -> Unit)? = null
    private var onDestroyCallback: (() -> Unit)? = null

    actual fun onStart(callback: () -> Unit) {
        onStartCallback = callback
    }

    actual fun onStop(callback: () -> Unit) {
        onStopCallback = callback
    }

    actual fun onDestroy(callback: () -> Unit) {
        onDestroyCallback = callback
    }
}

@Composable
actual fun rememberPlatformLifecycle(): PlatformLifecycle {
    return remember { PlatformLifecycle() }
}

/**
 * Web platform executor - not needed for coroutines
 */
actual class PlatformExecutor

actual fun getPlatformMainExecutor(context: PlatformContext): PlatformExecutor {
    return PlatformExecutor()
}
