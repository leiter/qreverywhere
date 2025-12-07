package cut.the.crap.qreverywhere.di

import cut.the.crap.qreverywhere.shared.domain.usecase.UserPreferences
import cut.the.crap.qreverywhere.shared.platform.AndroidUserPreferences
import cut.the.crap.qreverywhere.utils.data.EncryptedPrefs
import cut.the.crap.qreverywhere.shared.di.getSharedModule
import cut.the.crap.qreverywhere.shared.di.platformModule
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    // Provide the shared prefs filename
    single { "appState" }

    // Provide EncryptedPrefs
    single { EncryptedPrefs(androidContext(), get<String>()) }

    // Provide UserPreferences using EncryptedPrefs
    single<UserPreferences> {
        val encryptedPrefs = get<EncryptedPrefs>()
        AndroidUserPreferences(
            getForegroundColorFn = { encryptedPrefs.foregroundColor },
            getBackgroundColorFn = { encryptedPrefs.backgroundColor },
            setForegroundColorFn = { color -> encryptedPrefs.foregroundColor = color },
            setBackgroundColorFn = { color -> encryptedPrefs.backgroundColor = color }
        )
    }
}

// Combined modules including shared KMP module
// Note: The shared MainViewModel is now provided by the shared module
fun getAllModules() = listOf(
    getSharedModule(),
    platformModule(),
    appModule
)
