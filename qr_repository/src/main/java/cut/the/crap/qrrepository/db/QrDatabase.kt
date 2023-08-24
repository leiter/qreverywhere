package cut.the.crap.qrrepository.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [QrCodeDbItem::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class QrDatabase : RoomDatabase() {
    abstract fun getDao(): QrCodeDao
}