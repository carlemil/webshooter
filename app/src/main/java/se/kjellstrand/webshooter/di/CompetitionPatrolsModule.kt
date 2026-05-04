package se.kjellstrand.webshooter.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import se.kjellstrand.webshooter.data.competitionpatrols.CompetitionPatrolsRepository
import se.kjellstrand.webshooter.data.competitionpatrols.local.PatrolsDao
import se.kjellstrand.webshooter.data.competitionpatrols.remote.CompetitionPatrolsRemoteDataSource
import se.kjellstrand.webshooter.data.competitionpatrols.remote.CompetitionPatrolsRemoteDataSourceKtor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class CompetitionPatrolsModule {

    @Provides
    @Singleton
    fun providesCompetitionPatrolsRemoteDataSource(httpClient: HttpClient): CompetitionPatrolsRemoteDataSource {
        return CompetitionPatrolsRemoteDataSourceKtor(httpClient)
    }

    @Provides
    @Singleton
    fun providesCompetitionPatrolsRepository(
        remoteDataSource: CompetitionPatrolsRemoteDataSource,
        dao: PatrolsDao,
        json: Json
    ): CompetitionPatrolsRepository {
        return CompetitionPatrolsRepository(remoteDataSource, dao, json)
    }
}
