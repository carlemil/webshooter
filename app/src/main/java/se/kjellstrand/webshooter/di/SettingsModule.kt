package se.kjellstrand.webshooter.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import se.kjellstrand.webshooter.data.settings.SettingsRepository
import se.kjellstrand.webshooter.data.settings.local.UserProfileDao
import se.kjellstrand.webshooter.data.settings.remote.SettingsRemoteDataSource
import se.kjellstrand.webshooter.data.settings.remote.SettingsRemoteDataSourceKtor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SettingsModule {

    @Provides
    @Singleton
    fun providesSettingsRemoteDataSource(httpClient: HttpClient): SettingsRemoteDataSource {
        return SettingsRemoteDataSourceKtor(httpClient)
    }

    @Provides
    @Singleton
    fun providesSettingsRepository(
        remoteDataSource: SettingsRemoteDataSource,
        dao: UserProfileDao,
        json: Json
    ): SettingsRepository {
        return SettingsRepository(remoteDataSource, dao, json)
    }
}
