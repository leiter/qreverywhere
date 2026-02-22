package cut.the.crap.qreverywhere.shared.di

import cut.the.crap.qreverywhere.shared.data.RoomQrRepository
import cut.the.crap.qreverywhere.shared.data.db.QrDatabase
import cut.the.crap.qreverywhere.shared.data.db.createDatabase
import cut.the.crap.qreverywhere.shared.domain.repository.QrRepository
import cut.the.crap.qreverywhere.shared.domain.usecase.CachingQrCodeGenerator
import cut.the.crap.qreverywhere.shared.domain.usecase.QrCodeGenerator
import cut.the.crap.qreverywhere.shared.domain.usecase.QrCodeScanner
import cut.the.crap.qreverywhere.shared.domain.usecase.SaveImageToFileUseCase
import cut.the.crap.qreverywhere.shared.domain.usecase.UserPreferences
import cut.the.crap.qreverywhere.shared.platform.DesktopQrCodeGenerator
import cut.the.crap.qreverywhere.shared.platform.DesktopQrCodeScanner
import cut.the.crap.qreverywhere.shared.platform.DesktopSaveImageToFileUseCase
import cut.the.crap.qreverywhere.shared.platform.DesktopUserPreferences
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Desktop-specific Koin module
 */
actual fun platformModule(): Module = module {
    // Room database
    single<QrDatabase> { createDatabase() }
    single<QrRepository> { RoomQrRepository(get<QrDatabase>().qrCodeDao()) }

    // Platform-specific implementations with caching
    single<QrCodeGenerator> { CachingQrCodeGenerator(DesktopQrCodeGenerator()) }
    single<QrCodeScanner> { DesktopQrCodeScanner() }
    single<SaveImageToFileUseCase> { DesktopSaveImageToFileUseCase() }
    single<UserPreferences> { DesktopUserPreferences() }

    single {
        // Initialize Napier for Desktop
        Napier.base(DebugAntilog())
    }
}