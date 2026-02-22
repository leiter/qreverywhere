package cut.the.crap.qreverywhere.shared.data.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

/**
 * Room Database for QR code history - cross-platform compatible
 * Uses KSP to generate platform-specific implementations
 */
@Database(
    entities = [QrCodeDbEntity::class],
    version = 1,
    exportSchema = true
)
@ConstructedBy(QrDatabaseConstructor::class)
abstract class QrDatabase : RoomDatabase() {
    abstract fun qrCodeDao(): QrCodeDao

    companion object {
        const val DATABASE_NAME = "qr_history.db"
    }
}

/**
 * Room database constructor for KMP
 * KSP generates the actual implementation for each platform
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object QrDatabaseConstructor : RoomDatabaseConstructor<QrDatabase> {
    override fun initialize(): QrDatabase
}
