package cut.the.crap.qreverywhere.shared.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Desktop implementation of PlatformContext
 * Would hold JFrame/Window reference or similar desktop context
 */
actual class PlatformContext

@Composable
actual fun rememberPlatformContext(): PlatformContext {
    return remember { PlatformContext() }
}

/**
 * Desktop implementation of PlatformLifecycle
 * Would integrate with window lifecycle events
 */
actual class PlatformLifecycle {
    actual fun onStart(callback: () -> Unit) {
        // TODO: Implement with window focus events
        callback()
    }

    actual fun onStop(callback: () -> Unit) {
        // TODO: Implement with window minimize/unfocus events
    }

    actual fun onDestroy(callback: () -> Unit) {
        // TODO: Implement with window close events
    }
}

@Composable
actual fun rememberPlatformLifecycle(): PlatformLifecycle {
    return remember { PlatformLifecycle() }
}

/**
 * Desktop implementation of PlatformExecutor
 * Would use Swing EDT or similar
 */
actual class PlatformExecutor

actual fun getPlatformMainExecutor(context: PlatformContext): PlatformExecutor {
    // TODO: Return SwingUtilities.invokeLater wrapper or similar
    return PlatformExecutor()
}