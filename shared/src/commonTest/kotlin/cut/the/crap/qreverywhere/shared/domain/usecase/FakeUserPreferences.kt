package cut.the.crap.qreverywhere.shared.domain.usecase

/**
 * Fake implementation of UserPreferences for testing
 */
class FakeUserPreferences : UserPreferences {
    private var foregroundColor: Int = 0xFF000000.toInt()
    private var backgroundColor: Int = 0xFFFFFFFF.toInt()

    override fun getForegroundColor(): Int = foregroundColor

    override fun getBackgroundColor(): Int = backgroundColor

    override fun setForegroundColor(color: Int) {
        foregroundColor = color
    }

    override fun setBackgroundColor(color: Int) {
        backgroundColor = color
    }

    fun reset() {
        foregroundColor = 0xFF000000.toInt()
        backgroundColor = 0xFFFFFFFF.toInt()
    }
}
