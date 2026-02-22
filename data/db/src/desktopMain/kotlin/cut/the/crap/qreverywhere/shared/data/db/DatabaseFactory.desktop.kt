package cut.the.crap.qreverywhere.shared.data.db

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

/**
 * Desktop (JVM) specific database factory
 */
actual fun getDatabaseBuilder(): RoomDatabase.Builder<QrDatabase> {
    val dbFile = File(System.getProperty("user.home"), ".qreverywhere/${QrDatabase.DATABASE_NAME}")
    dbFile.parentFile?.mkdirs()
    return Room.databaseBuilder<QrDatabase>(
        name = dbFile.absolutePath
    )
}
