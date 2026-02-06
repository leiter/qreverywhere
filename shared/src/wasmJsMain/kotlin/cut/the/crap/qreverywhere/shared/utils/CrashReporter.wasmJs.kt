package cut.the.crap.qreverywhere.shared.utils

/**
 * Web (Wasm) implementation of CrashReporter
 *
 * Uses browser console for logging
 * For production, consider integrating Sentry or a similar browser error tracking service
 */
actual object CrashReporter {

    private var isEnabled = true

    actual fun log(message: String) {
        if (!isEnabled) return
        console.log("[CrashReporter] $message")
    }

    actual fun recordException(throwable: Throwable) {
        if (!isEnabled) return
        console.error("[CrashReporter] Non-fatal exception: ${throwable.message}")
        throwable.printStackTrace()
    }

    actual fun setCustomKey(key: String, value: String) {
        if (!isEnabled) return
        console.log("[CrashReporter] Custom key: $key = $value")
    }

    actual fun setUserId(userId: String?) {
        if (!isEnabled) return
        console.log("[CrashReporter] User ID: ${userId ?: "(cleared)"}")
    }

    actual fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        isEnabled = enabled
        console.log("[CrashReporter] Crash collection ${if (enabled) "enabled" else "disabled"}")
    }
}

// Browser console access
private external object console {
    fun log(message: String)
    fun error(message: String)
    fun warn(message: String)
}
