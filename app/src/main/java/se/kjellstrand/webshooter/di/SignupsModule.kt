package se.kjellstrand.webshooter.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import se.kjellstrand.webshooter.data.signups.SignupsRepository
import se.kjellstrand.webshooter.data.signups.remote.SignupsRemoteDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SignupsModule {

    @Provides
    @Singleton
    fun providesSignupsRemoteDataSource(retrofit: Retrofit): SignupsRemoteDataSource {
        return retrofit.create(SignupsRemoteDataSource::class.java)
    }

    @Provides
    @Singleton
    fun providesSignupsRepository(
        remoteDataSource: SignupsRemoteDataSource
    ): SignupsRepository {
        return SignupsRepository(remoteDataSource)
    }
}
