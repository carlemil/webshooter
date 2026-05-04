package se.kjellstrand.webshooter.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import se.kjellstrand.webshooter.data.competitionteams.CompetitionTeamsRepository
import se.kjellstrand.webshooter.data.competitionteams.local.TeamsDao
import se.kjellstrand.webshooter.data.competitionteams.remote.CompetitionTeamsRemoteDataSource
import se.kjellstrand.webshooter.data.competitionteams.remote.CompetitionTeamsRemoteDataSourceKtor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class CompetitionTeamsModule {

    @Provides
    @Singleton
    fun providesCompetitionTeamsRemoteDataSource(httpClient: HttpClient): CompetitionTeamsRemoteDataSource {
        return CompetitionTeamsRemoteDataSourceKtor(httpClient)
    }

    @Provides
    @Singleton
    fun providesCompetitionTeamsRepository(
        remoteDataSource: CompetitionTeamsRemoteDataSource,
        dao: TeamsDao,
        json: Json
    ): CompetitionTeamsRepository {
        return CompetitionTeamsRepository(remoteDataSource, dao, json)
    }
}
