package cut.the.crap.qreverywhere.shared.di

import cut.the.crap.qreverywhere.shared.presentation.viewmodel.MainViewModel
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Common Koin module for shared dependencies
 */
val commonModule = module {
    single { MainViewModel(get()) }
}

/**
 * Platform-specific modules will be provided by each platform
 */
expect fun platformModule(): Module