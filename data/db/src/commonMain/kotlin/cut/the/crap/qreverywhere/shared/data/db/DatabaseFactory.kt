package cut.the.crap.qreverywhere.shared.data.db

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * Creates and configures the Room database for each platform
 */
expect fun getDatabaseBuilder(): RoomDatabase.Builder<QrDatabase>

/**
 * Creates the configured database instance
 */
fun createDatabase(): QrDatabase {
    return getDatabaseBuilder()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
