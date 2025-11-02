package cut.the.crap.qreverywhere.shared.di

import cut.the.crap.qreverywhere.shared.domain.repository.QrRepository
import cut.the.crap.qreverywhere.shared.domain.usecase.QrCodeGenerator
import cut.the.crap.qreverywhere.shared.domain.usecase.QrCodeScanner
import cut.the.crap.qreverywhere.shared.platform.IosQrCodeGenerator
import cut.the.crap.qreverywhere.shared.platform.IosQrCodeScanner
import cut.the.crap.qreverywhere.shared.data.IosQrRepository
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * iOS-specific Koin module
 */
actual fun platformModule(): Module = module {
    single<QrRepository> { IosQrRepository() }
    single<QrCodeGenerator> { IosQrCodeGenerator() }
    single<QrCodeScanner> { IosQrCodeScanner() }

    single {
        // Initialize Napier for iOS
        Napier.base(DebugAntilog())
    }
}