package cut.the.crap.qreverywhere.shared.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Unit tests for ErrorHandler utility
 */
class ErrorHandlerTest {

    // ==================== getDisplayMessage Tests ====================

    @Test
    fun `getDisplayMessage returns message for IllegalArgumentException`() {
        val exception = IllegalArgumentException("Invalid input")
        val message = ErrorHandler.getDisplayMessage(exception)
        assertEquals("Invalid input provided.", message)
    }

    @Test
    fun `getDisplayMessage returns message for IllegalStateException`() {
        val exception = IllegalStateException("Bad state")
        val message = ErrorHandler.getDisplayMessage(exception)
        assertEquals("Operation not allowed in current state.", message)
    }

    @Test
    fun `getDisplayMessage returns generic message for unknown exception`() {
        val exception = RuntimeException("Some error")
        val message = ErrorHandler.getDisplayMessage(exception)
        assertEquals("Some error", message)
    }

    @Test
    fun `getDisplayMessage returns fallback for exception without message`() {
        val exception = RuntimeException()
        val message = ErrorHandler.getDisplayMessage(exception)
        assertEquals("An unexpected error occurred.", message)
    }

    @Test
    fun `getDisplayMessage returns fallback for blank message`() {
        val exception = RuntimeException("   ")
        val message = ErrorHandler.getDisplayMessage(exception)
        assertEquals("An unexpected error occurred.", message)
    }

    @Test
    fun `getDisplayMessage handles network-like exceptions by name`() {
        class NetworkException(message: String) : Exception(message)
        val exception = NetworkException("Failed")
        val message = ErrorHandler.getDisplayMessage(exception)
        assertEquals("Network error. Please check your connection.", message)
    }

    @Test
    fun `getDisplayMessage handles network error by message content`() {
        val exception = RuntimeException("connection refused")
        val message = ErrorHandler.getDisplayMessage(exception)
        assertEquals("Network error. Please check your connection.", message)
    }

    @Test
    fun `getDisplayMessage handles timeout error by message content`() {
        val exception = RuntimeException("Request timeout")
        val message = ErrorHandler.getDisplayMessage(exception)
        assertEquals("Network error. Please check your connection.", message)
    }

    @Test
    fun `getDisplayMessage handles permission error by message content`() {
        val exception = RuntimeException("Permission denied")
        val message = ErrorHandler.getDisplayMessage(exception)
        assertEquals("Permission denied. Please check app permissions.", message)
    }

    @Test
    fun `getDisplayMessage handles storage error by message content`() {
        val exception = RuntimeException("disk space low - no space available")
        val message = ErrorHandler.getDisplayMessage(exception)
        assertEquals("Storage error. Please check available space.", message)
    }

    // ==================== String Message Tests ====================

    @Test
    fun `getDisplayMessage string returns provided message`() {
        val message = ErrorHandler.getDisplayMessage("Custom error")
        assertEquals("Custom error", message)
    }

    @Test
    fun `getDisplayMessage string returns fallback for null`() {
        val message = ErrorHandler.getDisplayMessage(null as String?)
        assertEquals("An unexpected error occurred.", message)
    }

    @Test
    fun `getDisplayMessage string returns fallback for blank`() {
        val message = ErrorHandler.getDisplayMessage("  ")
        assertEquals("An unexpected error occurred.", message)
    }

    // ==================== categorizeError Tests ====================

    @Test
    fun `categorizeError returns NETWORK for network errors`() {
        val exception = RuntimeException("connection timeout")
        assertEquals(ErrorCategory.NETWORK, ErrorHandler.categorizeError(exception))
    }

    @Test
    fun `categorizeError returns VALIDATION for IllegalArgumentException`() {
        val exception = IllegalArgumentException("bad input")
        assertEquals(ErrorCategory.VALIDATION, ErrorHandler.categorizeError(exception))
    }

    @Test
    fun `categorizeError returns STATE for IllegalStateException`() {
        val exception = IllegalStateException("invalid state")
        assertEquals(ErrorCategory.STATE, ErrorHandler.categorizeError(exception))
    }

    @Test
    fun `categorizeError returns PERMISSION for permission errors`() {
        val exception = RuntimeException("permission denied")
        assertEquals(ErrorCategory.PERMISSION, ErrorHandler.categorizeError(exception))
    }

    @Test
    fun `categorizeError returns STORAGE for storage errors`() {
        val exception = RuntimeException("disk full cannot write")
        assertEquals(ErrorCategory.STORAGE, ErrorHandler.categorizeError(exception))
    }

    @Test
    fun `categorizeError returns UNKNOWN for unrecognized errors`() {
        val exception = RuntimeException("something happened")
        assertEquals(ErrorCategory.UNKNOWN, ErrorHandler.categorizeError(exception))
    }

    // ==================== isRecoverable Tests ====================

    @Test
    fun `isRecoverable returns true for network errors`() {
        val exception = RuntimeException("network unreachable")
        assertTrue(ErrorHandler.isRecoverable(exception))
    }

    @Test
    fun `isRecoverable returns true for IllegalArgumentException`() {
        val exception = IllegalArgumentException("bad input")
        assertTrue(ErrorHandler.isRecoverable(exception))
    }

    @Test
    fun `isRecoverable returns true for permission errors`() {
        val exception = RuntimeException("permission denied")
        assertTrue(ErrorHandler.isRecoverable(exception))
    }

    @Test
    fun `isRecoverable returns true for storage errors`() {
        val exception = RuntimeException("no space left on disk")
        assertTrue(ErrorHandler.isRecoverable(exception))
    }

    @Test
    fun `isRecoverable returns false for unknown errors`() {
        val exception = RuntimeException("fatal error")
        assertFalse(ErrorHandler.isRecoverable(exception))
    }

    @Test
    fun `isRecoverable returns false for NullPointerException`() {
        val exception = NullPointerException("null")
        assertFalse(ErrorHandler.isRecoverable(exception))
    }
}
