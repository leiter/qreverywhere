package cut.the.crap.qreverywhere.shared.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Android-specific database factory
 */
private lateinit var appContext: Context

fun initializeDatabase(context: Context) {
    appContext = context.applicationContext
}

actual fun getDatabaseBuilder(): RoomDatabase.Builder<QrDatabase> {
    val dbFile = appContext.getDatabasePath(QrDatabase.DATABASE_NAME)
    return Room.databaseBuilder<QrDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
