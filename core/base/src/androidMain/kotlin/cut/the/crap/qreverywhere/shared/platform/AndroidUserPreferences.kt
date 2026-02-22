package cut.the.crap.qreverywhere.shared.platform

import cut.the.crap.qreverywhere.shared.domain.usecase.ThemePreference
import cut.the.crap.qreverywhere.shared.domain.usecase.UserPreferences

/**
 * Android implementation of UserPreferences
 * This will be provided by the app module through DI
 */
class AndroidUserPreferences(
    private val getForegroundColorFn: () -> Int,
    private val getBackgroundColorFn: () -> Int,
    private val setForegroundColorFn: (Int) -> Unit,
    private val setBackgroundColorFn: (Int) -> Unit,
    private val getThemePreferenceFn: () -> ThemePreference = { ThemePreference.SYSTEM },
    private val setThemePreferenceFn: (ThemePreference) -> Unit = {}
) : UserPreferences {

    override fun getForegroundColor(): Int = getForegroundColorFn()

    override fun getBackgroundColor(): Int = getBackgroundColorFn()

    override fun setForegroundColor(color: Int) = setForegroundColorFn(color)

    override fun setBackgroundColor(color: Int) = setBackgroundColorFn(color)

    override fun getThemePreference(): ThemePreference = getThemePreferenceFn()

    override fun setThemePreference(theme: ThemePreference) = setThemePreferenceFn(theme)
}