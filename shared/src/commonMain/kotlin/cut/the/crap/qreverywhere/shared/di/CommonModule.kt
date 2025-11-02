package cut.the.crap.qreverywhere.shared.di

import cut.the.crap.qreverywhere.shared.presentation.viewmodel.MainViewModel
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Common Koin module for shared dependencies
 */
val commonModule = module {
    // Shared ViewModel with all dependencies
    // Using factory for proper lifecycle scoping
    factory {
        MainViewModel(
            qrRepository = get(),
            qrCodeGenerator = get(),
            saveImageUseCase = get(),
            userPreferences = get()
        )
    }
}

/**
 * Platform-specific modules will be provided by each platform
 */
expect fun platformModule(): Module