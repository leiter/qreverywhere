package cut.the.crap.qreverywhere.shared.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * iOS implementation of PlatformContext
 * Would hold UIViewController or similar iOS context
 */
actual class PlatformContext

@Composable
actual fun rememberPlatformContext(): PlatformContext {
    return remember { PlatformContext() }
}

/**
 * iOS implementation of PlatformLifecycle
 * Would integrate with UIViewController lifecycle
 */
actual class PlatformLifecycle {
    actual fun onStart(callback: () -> Unit) {
        // TODO: Implement with UIViewController viewWillAppear
    }

    actual fun onStop(callback: () -> Unit) {
        // TODO: Implement with UIViewController viewWillDisappear
    }

    actual fun onDestroy(callback: () -> Unit) {
        // TODO: Implement with UIViewController deinit
    }
}

@Composable
actual fun rememberPlatformLifecycle(): PlatformLifecycle {
    return remember { PlatformLifecycle() }
}

/**
 * iOS implementation of PlatformExecutor
 * Would use DispatchQueue.main
 */
actual class PlatformExecutor

actual fun getPlatformMainExecutor(context: PlatformContext): PlatformExecutor {
    // TODO: Return DispatchQueue.main wrapper
    return PlatformExecutor()
}