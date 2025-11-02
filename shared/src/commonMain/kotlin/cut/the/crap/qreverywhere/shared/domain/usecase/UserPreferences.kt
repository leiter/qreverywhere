package cut.the.crap.qreverywhere.shared.domain.usecase

/**
 * Platform-specific interface for user preferences
 */
interface UserPreferences {
    /**
     * Get the foreground color for QR codes
     */
    fun getForegroundColor(): Int

    /**
     * Get the background color for QR codes
     */
    fun getBackgroundColor(): Int

    /**
     * Set the foreground color for QR codes
     */
    fun setForegroundColor(color: Int)

    /**
     * Set the background color for QR codes
     */
    fun setBackgroundColor(color: Int)
}