package cut.the.crap.qreverywhere.shared.utils

/**
 * Android implementation of CrashReporter
 *
 * Uses Firebase Crashlytics if available, otherwise logs to Napier
 *
 * To enable Firebase Crashlytics:
 * 1. Add google-services.json to androidApp/
 * 2. Add Firebase dependencies to build.gradle.kts
 * 3. Uncomment the Crashlytics implementation below
 */
actual object CrashReporter {

    private var isEnabled = true

    actual fun log(message: String) {
        if (!isEnabled) return

        // Firebase Crashlytics implementation (uncomment when Firebase is added)
        // try {
        //     FirebaseCrashlytics.getInstance().log(message)
        // } catch (e: Exception) {
        //     // Firebase not initialized, fallback to Napier
        //     Logger.d("CrashReporter") { message }
        // }

        // Fallback to Napier logging
        Logger.d("CrashReporter") { message }
    }

    actual fun recordException(throwable: Throwable) {
        if (!isEnabled) return

        // Firebase Crashlytics implementation (uncomment when Firebase is added)
        // try {
        //     FirebaseCrashlytics.getInstance().recordException(throwable)
        // } catch (e: Exception) {
        //     Logger.e("CrashReporter", throwable) { "Non-fatal exception" }
        // }

        // Fallback to Napier logging
        Logger.e("CrashReporter", throwable) { "Non-fatal exception recorded" }
    }

    actual fun setCustomKey(key: String, value: String) {
        if (!isEnabled) return

        // Firebase Crashlytics implementation (uncomment when Firebase is added)
        // try {
        //     FirebaseCrashlytics.getInstance().setCustomKey(key, value)
        // } catch (e: Exception) {
        //     Logger.d("CrashReporter") { "Custom key: $key = $value" }
        // }

        // Fallback to Napier logging
        Logger.d("CrashReporter") { "Custom key: $key = $value" }
    }

    actual fun setUserId(userId: String?) {
        if (!isEnabled) return

        // Firebase Crashlytics implementation (uncomment when Firebase is added)
        // try {
        //     FirebaseCrashlytics.getInstance().setUserId(userId ?: "")
        // } catch (e: Exception) {
        //     Logger.d("CrashReporter") { "User ID: $userId" }
        // }

        // Fallback to Napier logging
        Logger.d("CrashReporter") { "User ID set: ${userId ?: "(cleared)"}" }
    }

    actual fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        isEnabled = enabled

        // Firebase Crashlytics implementation (uncomment when Firebase is added)
        // try {
        //     FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(enabled)
        // } catch (e: Exception) {
        //     Logger.d("CrashReporter") { "Crash collection: $enabled" }
        // }

        Logger.d("CrashReporter") { "Crash collection ${if (enabled) "enabled" else "disabled"}" }
    }
}
