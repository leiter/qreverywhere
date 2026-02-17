package cut.the.crap.qreverywhere.shared.di

import cut.the.crap.qreverywhere.shared.data.RoomQrRepository
import cut.the.crap.qreverywhere.shared.data.db.QrDatabase
import cut.the.crap.qreverywhere.shared.data.db.createDatabase
import cut.the.crap.qreverywhere.shared.data.db.initializeDatabase
import cut.the.crap.qreverywhere.shared.domain.repository.QrRepository
import cut.the.crap.qreverywhere.shared.domain.usecase.CachingQrCodeGenerator
import cut.the.crap.qreverywhere.shared.domain.usecase.QrCodeGenerator
import cut.the.crap.qreverywhere.shared.domain.usecase.QrCodeScanner
import cut.the.crap.qreverywhere.shared.domain.usecase.SaveImageToFileUseCase
import cut.the.crap.qreverywhere.shared.domain.usecase.UserPreferences
import cut.the.crap.qreverywhere.shared.platform.AndroidQrCodeGenerator
import cut.the.crap.qreverywhere.shared.platform.AndroidQrCodeScanner
import cut.the.crap.qreverywhere.shared.platform.AndroidSaveImageToFileUseCase
import cut.the.crap.qreverywhere.shared.platform.AndroidUserPreferences
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android-specific Koin module
 * Uses the shared KMP Room database instead of the legacy qr_repository module
 */
actual fun platformModule(): Module = module {
    // Initialize database context
    single {
        initializeDatabase(androidContext())
        createDatabase()
    }

    // Repository implementation using shared Room database
    single<QrRepository> { RoomQrRepository(get<QrDatabase>().qrCodeDao()) }

    // Platform-specific QR code operations with caching
    single<QrCodeGenerator> { CachingQrCodeGenerator(AndroidQrCodeGenerator()) }
    single<QrCodeScanner> { AndroidQrCodeScanner() }

    // Save image use case
    single<SaveImageToFileUseCase> { AndroidSaveImageToFileUseCase(androidContext()) }

    single {
        // Initialize Napier for Android
        Napier.base(DebugAntilog())
    }
}