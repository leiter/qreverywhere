package cut.the.crap.qreverywhere.shared.camera

import androidx.compose.runtime.Composable

/**
 * Platform-specific context for accessing platform resources
 */
expect class PlatformContext

/**
 * Remember platform context in Compose
 */
@Composable
expect fun rememberPlatformContext(): PlatformContext

/**
 * Platform-specific lifecycle management
 */
expect class PlatformLifecycle {
    fun onStart(callback: () -> Unit)
    fun onStop(callback: () -> Unit)
    fun onDestroy(callback: () -> Unit)
}

/**
 * Remember platform lifecycle in Compose
 */
@Composable
expect fun rememberPlatformLifecycle(): PlatformLifecycle

/**
 * Platform-specific executor for running tasks
 */
expect class PlatformExecutor

/**
 * Get the main/UI thread executor for the platform
 */
expect fun getPlatformMainExecutor(context: PlatformContext): PlatformExecutor