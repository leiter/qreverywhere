package cut.the.crap.qreverywhere.shared.utils

/**
 * iOS implementation of CrashReporter
 *
 * Uses Firebase Crashlytics if available, otherwise logs to Napier
 *
 * To enable Firebase Crashlytics on iOS:
 * 1. Add GoogleService-Info.plist to iosApp/
 * 2. Add Firebase SDK via SPM or CocoaPods
 * 3. Initialize Firebase in AppDelegate
 * 4. Uncomment the Crashlytics implementation below
 */
actual object CrashReporter {

    private var isEnabled = true

    actual fun log(message: String) {
        if (!isEnabled) return

        // Firebase Crashlytics implementation for iOS (uncomment when Firebase is added)
        // Crashlytics.crashlytics().log(message)

        // Fallback to Napier logging
        Logger.d("CrashReporter") { message }
    }

    actual fun recordException(throwable: Throwable) {
        if (!isEnabled) return

        // Firebase Crashlytics implementation for iOS (uncomment when Firebase is added)
        // Note: iOS uses NSError, so you'd convert:
        // val error = NSError.errorWithDomain("QrEveryWhere", 0, mapOf(
        //     "message" to (throwable.message ?: "Unknown error")
        // ))
        // Crashlytics.crashlytics().recordError(error)

        // Fallback to Napier logging
        Logger.e("CrashReporter", throwable) { "Non-fatal exception recorded" }
    }

    actual fun setCustomKey(key: String, value: String) {
        if (!isEnabled) return

        // Firebase Crashlytics implementation for iOS (uncomment when Firebase is added)
        // Crashlytics.crashlytics().setCustomValue(value, forKey: key)

        // Fallback to Napier logging
        Logger.d("CrashReporter") { "Custom key: $key = $value" }
    }

    actual fun setUserId(userId: String?) {
        if (!isEnabled) return

        // Firebase Crashlytics implementation for iOS (uncomment when Firebase is added)
        // Crashlytics.crashlytics().setUserID(userId ?: "")

        // Fallback to Napier logging
        Logger.d("CrashReporter") { "User ID set: ${userId ?: "(cleared)"}" }
    }

    actual fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        isEnabled = enabled

        // Firebase Crashlytics implementation for iOS (uncomment when Firebase is added)
        // Crashlytics.crashlytics().setCrashlyticsCollectionEnabled(enabled)

        Logger.d("CrashReporter") { "Crash collection ${if (enabled) "enabled" else "disabled"}" }
    }
}
