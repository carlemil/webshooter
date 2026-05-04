package se.kjellstrand.webshooter.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import se.kjellstrand.webshooter.data.competitionsignups.CompetitionSignupsRepository
import se.kjellstrand.webshooter.data.competitionsignups.local.CompetitionSignupsDao
import se.kjellstrand.webshooter.data.competitionsignups.remote.CompetitionSignupsRemoteDataSource
import se.kjellstrand.webshooter.data.competitionsignups.remote.CompetitionSignupsRemoteDataSourceKtor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class CompetitionSignupsModule {

    @Provides
    @Singleton
    fun providesCompetitionSignupsRemoteDataSource(httpClient: HttpClient): CompetitionSignupsRemoteDataSource {
        return CompetitionSignupsRemoteDataSourceKtor(httpClient)
    }

    @Provides
    @Singleton
    fun providesCompetitionSignupsRepository(
        remoteDataSource: CompetitionSignupsRemoteDataSource,
        dao: CompetitionSignupsDao,
        json: Json
    ): CompetitionSignupsRepository {
        return CompetitionSignupsRepository(remoteDataSource, dao, json)
    }
}
