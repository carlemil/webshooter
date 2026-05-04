package se.kjellstrand.webshooter.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import se.kjellstrand.webshooter.data.club.ClubRepository
import se.kjellstrand.webshooter.data.club.local.ClubDao
import se.kjellstrand.webshooter.data.club.remote.ClubRemoteDataSource
import se.kjellstrand.webshooter.data.club.remote.ClubRemoteDataSourceKtor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ClubModule {

    @Provides
    @Singleton
    fun providesClubRemoteDataSource(httpClient: HttpClient): ClubRemoteDataSource {
        return ClubRemoteDataSourceKtor(httpClient)
    }

    @Provides
    @Singleton
    fun providesClubRepository(
        remoteDataSource: ClubRemoteDataSource,
        dao: ClubDao,
        json: Json
    ): ClubRepository {
        return ClubRepository(remoteDataSource, dao, json)
    }
}
