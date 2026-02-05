package cut.the.crap.qreverywhere.shared.platform

import cut.the.crap.qreverywhere.shared.domain.usecase.UserPreferences

/**
 * Web implementation of UserPreferences using localStorage
 */
class WebUserPreferences : UserPreferences {

    override fun getForegroundColor(): Int {
        return getFromLocalStorage(KEY_FOREGROUND_COLOR)?.toIntOrNull() ?: DEFAULT_FOREGROUND_COLOR
    }

    override fun getBackgroundColor(): Int {
        return getFromLocalStorage(KEY_BACKGROUND_COLOR)?.toIntOrNull() ?: DEFAULT_BACKGROUND_COLOR
    }

    override fun setForegroundColor(color: Int) {
        setToLocalStorage(KEY_FOREGROUND_COLOR, color.toString())
    }

    override fun setBackgroundColor(color: Int) {
        setToLocalStorage(KEY_BACKGROUND_COLOR, color.toString())
    }

    companion object {
        private const val KEY_FOREGROUND_COLOR = "qr_foreground_color"
        private const val KEY_BACKGROUND_COLOR = "qr_background_color"
        private const val DEFAULT_FOREGROUND_COLOR = 0xFF000000.toInt() // Black
        private const val DEFAULT_BACKGROUND_COLOR = 0xFFFFFFFF.toInt() // White
    }
}

// localStorage interop for Wasm
@JsFun("(key) => localStorage.getItem(key)")
private external fun localStorageGetItem(key: String): String?

@JsFun("(key, value) => localStorage.setItem(key, value)")
private external fun localStorageSetItem(key: String, value: String)

private fun getFromLocalStorage(key: String): String? {
    return try {
        localStorageGetItem(key)
    } catch (e: Exception) {
        null
    }
}

private fun setToLocalStorage(key: String, value: String) {
    try {
        localStorageSetItem(key, value)
    } catch (e: Exception) {
        // Ignore storage errors
    }
}
