package cut.the.crap.qreverywhere.shared.di

import cut.the.crap.qreverywhere.shared.data.WebQrRepository
import cut.the.crap.qreverywhere.shared.domain.repository.QrRepository
import cut.the.crap.qreverywhere.shared.domain.usecase.QrCodeGenerator
import cut.the.crap.qreverywhere.shared.domain.usecase.SaveImageToFileUseCase
import cut.the.crap.qreverywhere.shared.domain.usecase.UserPreferences
import cut.the.crap.qreverywhere.shared.platform.WebQrCodeGenerator
import cut.the.crap.qreverywhere.shared.platform.WebSaveImageToFileUseCase
import cut.the.crap.qreverywhere.shared.platform.WebUserPreferences
import io.github.aakira.napier.Napier
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Web-specific Koin module
 */
actual fun platformModule(): Module = module {
    // localStorage-based repository (no Room for web)
    single<QrRepository> { WebQrRepository() }

    // Platform-specific implementations
    single<QrCodeGenerator> { WebQrCodeGenerator() }
    single<SaveImageToFileUseCase> { WebSaveImageToFileUseCase() }
    single<UserPreferences> { WebUserPreferences() }

    single {
        // Initialize Napier for Web (console logging)
        // Note: Napier may need web-specific antilog
    }
}
