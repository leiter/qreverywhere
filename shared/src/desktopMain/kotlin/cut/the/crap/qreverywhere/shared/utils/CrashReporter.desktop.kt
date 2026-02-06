package cut.the.crap.qreverywhere.shared.utils

import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Desktop implementation of CrashReporter
 *
 * Logs to Napier and optionally writes to a crash log file
 * For production, consider integrating Sentry or a similar service
 */
actual object CrashReporter {

    private var isEnabled = true
    private var userId: String? = null
    private val customKeys = mutableMapOf<String, String>()

    // Crash log file location
    private val crashLogFile: File by lazy {
        val userHome = System.getProperty("user.home")
        val appDir = File(userHome, ".qreverywhere")
        appDir.mkdirs()
        File(appDir, "crash_reports.log")
    }

    actual fun log(message: String) {
        if (!isEnabled) return
        Logger.d("CrashReporter") { message }
        appendToLogFile("LOG", message)
    }

    actual fun recordException(throwable: Throwable) {
        if (!isEnabled) return
        Logger.e("CrashReporter", throwable) { "Non-fatal exception recorded" }

        // Write exception details to log file
        val stackTrace = StringWriter().also {
            throwable.printStackTrace(PrintWriter(it))
        }.toString()

        appendToLogFile("EXCEPTION", """
            |Message: ${throwable.message}
            |User: $userId
            |Custom Keys: $customKeys
            |Stack Trace:
            |$stackTrace
        """.trimMargin())
    }

    actual fun setCustomKey(key: String, value: String) {
        if (!isEnabled) return
        customKeys[key] = value
        Logger.d("CrashReporter") { "Custom key: $key = $value" }
    }

    actual fun setUserId(userId: String?) {
        if (!isEnabled) return
        this.userId = userId
        Logger.d("CrashReporter") { "User ID set: ${userId ?: "(cleared)"}" }
    }

    actual fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        isEnabled = enabled
        Logger.d("CrashReporter") { "Crash collection ${if (enabled) "enabled" else "disabled"}" }
    }

    private fun appendToLogFile(level: String, message: String) {
        try {
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            crashLogFile.appendText("[$timestamp] [$level] $message\n\n")
        } catch (e: Exception) {
            // Silently fail if we can't write to the log file
        }
    }
}
