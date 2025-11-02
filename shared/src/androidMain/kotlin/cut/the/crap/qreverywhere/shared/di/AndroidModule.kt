package cut.the.crap.qreverywhere.shared.di

import cut.the.crap.qreverywhere.shared.data.AndroidQrRepository
import cut.the.crap.qreverywhere.shared.domain.repository.QrRepository
import cut.the.crap.qreverywhere.shared.domain.usecase.QrCodeGenerator
import cut.the.crap.qreverywhere.shared.domain.usecase.QrCodeScanner
import cut.the.crap.qreverywhere.shared.platform.AndroidQrCodeGenerator
import cut.the.crap.qreverywhere.shared.platform.AndroidQrCodeScanner
import cut.the.crap.qrrepository.QrHistoryRepository
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android-specific Koin module
 */
actual fun platformModule(): Module = module {
    // Room repository (Android-specific)
    single { QrHistoryRepository(androidContext()) }

    // Repository implementation
    single<QrRepository> { AndroidQrRepository(get()) }

    // Platform-specific QR code operations
    single<QrCodeGenerator> { AndroidQrCodeGenerator() }
    single<QrCodeScanner> { AndroidQrCodeScanner() }

    single {
        // Initialize Napier for Android
        Napier.base(DebugAntilog())
    }
}