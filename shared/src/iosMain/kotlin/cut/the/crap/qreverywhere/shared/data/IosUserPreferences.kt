package cut.the.crap.qreverywhere.shared.data

import cut.the.crap.qreverywhere.shared.domain.usecase.ThemePreference
import cut.the.crap.qreverywhere.shared.domain.usecase.UserPreferences
import platform.Foundation.NSUserDefaults

/**
 * iOS implementation of UserPreferences using NSUserDefaults
 */
class IosUserPreferences : UserPreferences {

    private val defaults = NSUserDefaults.standardUserDefaults

    companion object {
        private const val KEY_FOREGROUND_COLOR = "qr_foreground_color"
        private const val KEY_BACKGROUND_COLOR = "qr_background_color"
        private const val KEY_THEME_PREFERENCE = "app_theme_preference"

        // Default colors (black on white)
        private const val DEFAULT_FOREGROUND = 0xFF000000.toInt() // Black
        private const val DEFAULT_BACKGROUND = 0xFFFFFFFF.toInt() // White
    }

    override fun getForegroundColor(): Int {
        return if (defaults.objectForKey(KEY_FOREGROUND_COLOR) != null) {
            defaults.integerForKey(KEY_FOREGROUND_COLOR).toInt()
        } else {
            DEFAULT_FOREGROUND
        }
    }

    override fun getBackgroundColor(): Int {
        return if (defaults.objectForKey(KEY_BACKGROUND_COLOR) != null) {
            defaults.integerForKey(KEY_BACKGROUND_COLOR).toInt()
        } else {
            DEFAULT_BACKGROUND
        }
    }

    override fun setForegroundColor(color: Int) {
        defaults.setInteger(color.toLong(), forKey = KEY_FOREGROUND_COLOR)
        defaults.synchronize()
    }

    override fun setBackgroundColor(color: Int) {
        defaults.setInteger(color.toLong(), forKey = KEY_BACKGROUND_COLOR)
        defaults.synchronize()
    }

    override fun getThemePreference(): ThemePreference {
        val value = defaults.stringForKey(KEY_THEME_PREFERENCE) ?: return ThemePreference.SYSTEM
        return try {
            ThemePreference.valueOf(value)
        } catch (e: Exception) {
            ThemePreference.SYSTEM
        }
    }

    override fun setThemePreference(theme: ThemePreference) {
        defaults.setObject(theme.name, forKey = KEY_THEME_PREFERENCE)
        defaults.synchronize()
    }
}
