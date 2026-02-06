package cut.the.crap.qreverywhere.shared.utils

/**
 * Centralized error handling utility for user-friendly error messages
 * Provides consistent error message formatting across the application
 */
object ErrorHandler {

    /**
     * Get a user-friendly display message for a throwable
     * @param throwable The error to convert to a message
     * @return A user-friendly error message
     */
    fun getDisplayMessage(throwable: Throwable): String {
        return when {
            // Network-related errors
            throwable.isNetworkError() -> "Network error. Please check your connection."

            // Invalid input errors
            throwable is IllegalArgumentException -> "Invalid input provided."
            throwable is IllegalStateException -> "Operation not allowed in current state."

            // Null pointer (should be rare in Kotlin but handle gracefully)
            throwable is NullPointerException -> "An unexpected error occurred. Please try again."

            // Security/Permission errors
            throwable.isSecurityError() -> "Permission denied. Please check app permissions."

            // Storage/IO errors
            throwable.isStorageError() -> "Storage error. Please check available space."

            // Use throwable message if available, otherwise generic message
            else -> throwable.message?.takeIf { it.isNotBlank() }
                ?: "An unexpected error occurred."
        }
    }

    /**
     * Get a user-friendly display message from a string error
     * Useful when only error message string is available
     */
    fun getDisplayMessage(errorMessage: String?): String {
        return errorMessage?.takeIf { it.isNotBlank() }
            ?: "An unexpected error occurred."
    }

    /**
     * Categorize error types for analytics/logging purposes
     */
    fun categorizeError(throwable: Throwable): ErrorCategory {
        return when {
            throwable.isNetworkError() -> ErrorCategory.NETWORK
            throwable is IllegalArgumentException -> ErrorCategory.VALIDATION
            throwable is IllegalStateException -> ErrorCategory.STATE
            throwable.isSecurityError() -> ErrorCategory.PERMISSION
            throwable.isStorageError() -> ErrorCategory.STORAGE
            else -> ErrorCategory.UNKNOWN
        }
    }

    /**
     * Check if error is recoverable (user can retry)
     */
    fun isRecoverable(throwable: Throwable): Boolean {
        return when {
            throwable.isNetworkError() -> true
            throwable.isStorageError() -> true
            throwable is IllegalArgumentException -> true // User can fix input
            throwable.isSecurityError() -> true // User can grant permission
            else -> false
        }
    }

    // Extension functions for error type detection (platform-agnostic)
    private fun Throwable.isNetworkError(): Boolean {
        val name = this::class.simpleName ?: ""
        val message = this.message?.lowercase() ?: ""
        return name.contains("Network", ignoreCase = true) ||
               name.contains("Socket", ignoreCase = true) ||
               name.contains("Connection", ignoreCase = true) ||
               name.contains("Timeout", ignoreCase = true) ||
               name.contains("UnknownHost", ignoreCase = true) ||
               message.contains("network") ||
               message.contains("connection") ||
               message.contains("timeout") ||
               message.contains("unreachable")
    }

    private fun Throwable.isSecurityError(): Boolean {
        val name = this::class.simpleName ?: ""
        val message = this.message?.lowercase() ?: ""
        return name.contains("Security", ignoreCase = true) ||
               name.contains("Permission", ignoreCase = true) ||
               message.contains("permission") ||
               message.contains("denied") ||
               message.contains("unauthorized")
    }

    private fun Throwable.isStorageError(): Boolean {
        val name = this::class.simpleName ?: ""
        val message = this.message?.lowercase() ?: ""
        // Check class name for IO-related exceptions
        val isIOException = name.contains("IOException", ignoreCase = true) ||
               name.contains("FileNotFoundException", ignoreCase = true) ||
               name.contains("StorageException", ignoreCase = true)
        // Check message for storage-related keywords with word boundaries
        val hasStorageMessage = message.contains("storage") ||
               message.contains("disk full") ||
               message.contains("disk space") ||
               message.contains("no space") ||
               message.contains("out of space") ||
               message.contains("cannot write") ||
               message.contains("write failed") ||
               message.contains("read failed")
        return isIOException || hasStorageMessage
    }
}

/**
 * Error categories for analytics and logging
 */
enum class ErrorCategory {
    NETWORK,
    VALIDATION,
    STATE,
    PERMISSION,
    STORAGE,
    UNKNOWN
}
