package cut.the.crap.qreverywhere.shared.di

import cut.the.crap.qreverywhere.shared.data.RoomQrRepository
import cut.the.crap.qreverywhere.shared.data.db.QrDatabase
import cut.the.crap.qreverywhere.shared.data.db.createDatabase
import cut.the.crap.qreverywhere.shared.domain.repository.QrRepository
import cut.the.crap.qreverywhere.shared.domain.usecase.QrCodeGenerator
import cut.the.crap.qreverywhere.shared.domain.usecase.QrCodeScanner
import cut.the.crap.qreverywhere.shared.domain.usecase.SaveImageToFileUseCase
import cut.the.crap.qreverywhere.shared.domain.usecase.UserPreferences
import cut.the.crap.qreverywhere.shared.platform.IosQrCodeGenerator
import cut.the.crap.qreverywhere.shared.platform.IosQrCodeScanner
import cut.the.crap.qreverywhere.shared.data.IosSaveImageToFileUseCase
import cut.the.crap.qreverywhere.shared.data.IosUserPreferences
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

private var isKoinInitialized = false

/**
 * Initialize Koin for iOS
 */
fun initKoinIos() {
    if (isKoinInitialized) return

    try {
        startKoin {
            modules(commonModule, platformModule())
        }
        // Initialize Napier logging
        Napier.base(DebugAntilog())
    } catch (e: Exception) {
        // Koin already started, ignore
    }
    isKoinInitialized = true
}

/**
 * iOS-specific Koin module
 */
actual fun platformModule(): Module = module {
    // Room database
    single<QrDatabase> { createDatabase() }
    single<QrRepository> { RoomQrRepository(get<QrDatabase>().qrCodeDao()) }

    // Platform-specific implementations
    single<QrCodeGenerator> { IosQrCodeGenerator() }
    single<QrCodeScanner> { IosQrCodeScanner() }
    single<SaveImageToFileUseCase> { IosSaveImageToFileUseCase() }
    single<UserPreferences> { IosUserPreferences() }
}