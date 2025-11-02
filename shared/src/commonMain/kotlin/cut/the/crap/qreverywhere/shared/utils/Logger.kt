package cut.the.crap.qreverywhere.shared.utils

import io.github.aakira.napier.Napier

/**
 * Common logging interface using Napier
 * Works across all platforms
 */
object Logger {
    fun d(tag: String, message: () -> String) {
        Napier.d(message(), tag = tag)
    }

    fun e(tag: String, throwable: Throwable? = null, message: () -> String) {
        Napier.e(message(), throwable, tag)
    }

    fun w(tag: String, message: () -> String) {
        Napier.w(message(), tag = tag)
    }

    fun i(tag: String, message: () -> String) {
        Napier.i(message(), tag = tag)
    }

    fun v(tag: String, message: () -> String) {
        Napier.v(message(), tag = tag)
    }
}