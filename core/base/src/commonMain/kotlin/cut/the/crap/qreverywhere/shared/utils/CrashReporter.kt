package cut.the.crap.qreverywhere.shared.utils

/**
 * Cross-platform crash reporting interface
 *
 * Platform implementations can integrate with:
 * - Firebase Crashlytics (Android/iOS)
 * - Sentry
 * - Custom logging solutions
 *
 * Usage:
 * ```kotlin
 * CrashReporter.log("User performed action X")
 * CrashReporter.recordException(exception)
 * CrashReporter.setUserId("user123")
 * ```
 */
expect object CrashReporter {
    /**
     * Log a message to crash reporter for context
     * These messages are included in crash reports but don't trigger reports on their own
     */
    fun log(message: String)

    /**
     * Record a non-fatal exception
     * Use this for handled exceptions that should still be tracked
     */
    fun recordException(throwable: Throwable)

    /**
     * Set custom key-value pair for crash context
     * Useful for debugging crashes
     */
    fun setCustomKey(key: String, value: String)

    /**
     * Set the user identifier for crash reports
     * Should be a non-PII identifier (not email/phone)
     */
    fun setUserId(userId: String?)

    /**
     * Enable or disable crash collection
     * Use for GDPR compliance or user opt-out
     */
    fun setCrashlyticsCollectionEnabled(enabled: Boolean)
}

/**
 * Extension function to safely record exceptions with context
 */
fun CrashReporter.recordWithContext(throwable: Throwable, context: String) {
    log("Context: $context")
    recordException(throwable)
}
