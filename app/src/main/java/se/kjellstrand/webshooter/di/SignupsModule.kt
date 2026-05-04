package se.kjellstrand.webshooter.di

import kotlinx.serialization.json.Json
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import se.kjellstrand.webshooter.data.mysignups.SignupsRepository
import se.kjellstrand.webshooter.data.mysignups.local.SignupsDao
import se.kjellstrand.webshooter.data.mysignups.remote.SignupsRemoteDataSource
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
        remoteDataSource: SignupsRemoteDataSource,
        dao: SignupsDao,
        json: Json
    ): SignupsRepository {
        return SignupsRepository(remoteDataSource, dao, json)
    }
}
