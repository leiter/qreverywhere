package cut.the.crap.qreverywhere.di

import cut.the.crap.qreverywhere.MainActivityViewModel
import cut.the.crap.qreverywhere.utils.data.AcquireDateFormatter
import cut.the.crap.qreverywhere.utils.data.EncryptedPrefs
import cut.the.crap.qreverywhere.shared.di.getSharedModule
import cut.the.crap.qreverywhere.shared.di.platformModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Provide the shared prefs filename
    single { "appState" }

    // Provide EncryptedPrefs
    single { EncryptedPrefs(androidContext(), get<String>()) }

    // Provide AcquireDateFormatter
    single { AcquireDateFormatter(androidContext()) }
}

val viewModelModule = module {
    // ViewModels - temporarily keep the Android-specific one while we transition
    viewModel { MainActivityViewModel(get(), get()) }
}

// Combined modules including shared KMP module
fun getAllModules() = listOf(
    getSharedModule(),
    platformModule(),
    appModule,
    viewModelModule
)
