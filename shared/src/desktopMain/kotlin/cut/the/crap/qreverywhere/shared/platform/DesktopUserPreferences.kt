package cut.the.crap.qreverywhere.shared.platform

import cut.the.crap.qreverywhere.shared.domain.usecase.ThemePreference
import cut.the.crap.qreverywhere.shared.domain.usecase.UserPreferences
import java.util.prefs.Preferences

/**
 * Desktop implementation of UserPreferences using Java Preferences API
 */
class DesktopUserPreferences : UserPreferences {

    private val preferences: Preferences = Preferences.userNodeForPackage(DesktopUserPreferences::class.java)

    override fun getForegroundColor(): Int {
        return preferences.getInt(KEY_FOREGROUND_COLOR, DEFAULT_FOREGROUND_COLOR)
    }

    override fun getBackgroundColor(): Int {
        return preferences.getInt(KEY_BACKGROUND_COLOR, DEFAULT_BACKGROUND_COLOR)
    }

    override fun setForegroundColor(color: Int) {
        preferences.putInt(KEY_FOREGROUND_COLOR, color)
    }

    override fun setBackgroundColor(color: Int) {
        preferences.putInt(KEY_BACKGROUND_COLOR, color)
    }

    override fun getThemePreference(): ThemePreference {
        val value = preferences.get(KEY_THEME_PREFERENCE, ThemePreference.SYSTEM.name)
        return try {
            ThemePreference.valueOf(value)
        } catch (e: Exception) {
            ThemePreference.SYSTEM
        }
    }

    override fun setThemePreference(theme: ThemePreference) {
        preferences.put(KEY_THEME_PREFERENCE, theme.name)
    }

    companion object {
        private const val KEY_FOREGROUND_COLOR = "qr_foreground_color"
        private const val KEY_BACKGROUND_COLOR = "qr_background_color"
        private const val KEY_THEME_PREFERENCE = "app_theme_preference"
        private const val DEFAULT_FOREGROUND_COLOR = 0xFF000000.toInt() // Black
        private const val DEFAULT_BACKGROUND_COLOR = 0xFFFFFFFF.toInt() // White
    }
}
