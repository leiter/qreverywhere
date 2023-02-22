package cut.the.crap.qreverywhere.di

import android.content.Context
import cut.the.crap.qrrepository.QrHistoryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRepository(@ApplicationContext application: Context): QrHistoryRepository {
        return QrHistoryRepository(application)
    }

}
