package cut.the.crap.qreverywhere.shared.domain.usecase

/**
 * Fake implementation of UserPreferences for testing
 */
class FakeUserPreferences : UserPreferences {
    private var foregroundColor: Int = 0xFF000000.toInt()
    private var backgroundColor: Int = 0xFFFFFFFF.toInt()
    private var themePreference: ThemePreference = ThemePreference.SYSTEM

    override fun getForegroundColor(): Int = foregroundColor

    override fun getBackgroundColor(): Int = backgroundColor

    override fun setForegroundColor(color: Int) {
        foregroundColor = color
    }

    override fun setBackgroundColor(color: Int) {
        backgroundColor = color
    }

    override fun getThemePreference(): ThemePreference = themePreference

    override fun setThemePreference(theme: ThemePreference) {
        themePreference = theme
    }

    fun reset() {
        foregroundColor = 0xFF000000.toInt()
        backgroundColor = 0xFFFFFFFF.toInt()
        themePreference = ThemePreference.SYSTEM
    }
}
