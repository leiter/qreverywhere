package cut.the.crap.qreverywhere.di

import android.app.Application
import androidx.room.Room
import cut.the.crap.qreverywhere.db.QrCodeDao
import cut.the.crap.qreverywhere.db.QrDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val DATABASE_NAME = "my_data"

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideAppDb(app: Application): QrDatabase {
        return Room.databaseBuilder(app, QrDatabase::class.java, DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideRunDao(db: QrDatabase): QrCodeDao {
        return db.getRunDao()
    }


}
