package cut.the.crap.qreverywhere.shared.di

import cut.the.crap.qreverywhere.feature.create.CreateViewModel
import cut.the.crap.qreverywhere.feature.detail.DetailViewModel
import cut.the.crap.qreverywhere.feature.history.HistoryViewModel
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Common Koin module for shared dependencies
 */
val commonModule = module {
    // Feature ViewModels - using single to share state across screens
    single { HistoryViewModel(qrRepository = get()) }
    single { CreateViewModel(qrRepository = get(), qrCodeGenerator = get(), userPreferences = get()) }
    single { DetailViewModel(qrRepository = get(), saveImageUseCase = get(), qrCodeGenerator = get(), userPreferences = get()) }
}

/**
 * Platform-specific modules will be provided by each platform
 */
expect fun platformModule(): Module
