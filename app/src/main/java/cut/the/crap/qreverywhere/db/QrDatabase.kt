package cut.the.crap.qreverywhere.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [QrCodeItem::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class QrDatabase : RoomDatabase() {
    abstract fun getRunDao(): QrCodeDao
}