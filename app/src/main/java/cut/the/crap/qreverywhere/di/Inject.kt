package cut.the.crap.qreverywhere.di

import cut.the.crap.qreverywhere.repository.QrRepository
import cut.the.crap.qreverywhere.repository.QrRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(ViewModelComponent::class)
interface RepositoryBindings {

    @Binds
    fun provideQrRepository(qrRepositoryImpl: QrRepositoryImpl) : QrRepository


}