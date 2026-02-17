package cut.the.crap.qreverywhere.shared.utils

import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Android implementation of CrashReporter using Firebase Crashlytics
 *
 * Setup requirements:
 * 1. Add google-services.json to androidApp/
 * 2. Firebase dependencies are in build.gradle.kts
 * 3. Initialize Firebase in Application class (auto-initialized by default)
 */
actual object CrashReporter {

    private var isEnabled = true

    private val crashlytics: FirebaseCrashlytics?
        get() = try {
            FirebaseCrashlytics.getInstance()
        } catch (e: Exception) {
            // Firebase not initialized
            null
        }

    actual fun log(message: String) {
        if (!isEnabled) return

        crashlytics?.log(message) ?: Logger.d("CrashReporter") { message }
    }

    actual fun recordException(throwable: Throwable) {
        if (!isEnabled) return

        crashlytics?.recordException(throwable)
            ?: Logger.e("CrashReporter", throwable) { "Non-fatal exception recorded" }
    }

    actual fun setCustomKey(key: String, value: String) {
        if (!isEnabled) return

        crashlytics?.setCustomKey(key, value)
            ?: Logger.d("CrashReporter") { "Custom key: $key = $value" }
    }

    actual fun setUserId(userId: String?) {
        if (!isEnabled) return

        crashlytics?.setUserId(userId ?: "")
            ?: Logger.d("CrashReporter") { "User ID set: ${userId ?: "(cleared)"}" }
    }

    actual fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        isEnabled = enabled

        crashlytics?.setCrashlyticsCollectionEnabled(enabled)
        Logger.d("CrashReporter") { "Crash collection ${if (enabled) "enabled" else "disabled"}" }
    }
}
