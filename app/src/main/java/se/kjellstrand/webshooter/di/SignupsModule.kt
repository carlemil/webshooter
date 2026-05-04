package se.kjellstrand.webshooter.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import se.kjellstrand.webshooter.data.mysignups.SignupsRepository
import se.kjellstrand.webshooter.data.mysignups.local.SignupsDao
import se.kjellstrand.webshooter.data.mysignups.remote.SignupsRemoteDataSource
import se.kjellstrand.webshooter.data.mysignups.remote.SignupsRemoteDataSourceKtor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SignupsModule {

    @Provides
    @Singleton
    fun providesSignupsRemoteDataSource(httpClient: HttpClient): SignupsRemoteDataSource {
        return SignupsRemoteDataSourceKtor(httpClient)
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
