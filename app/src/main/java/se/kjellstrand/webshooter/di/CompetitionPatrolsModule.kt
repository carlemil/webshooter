package se.kjellstrand.webshooter.di

import kotlinx.serialization.json.Json
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import se.kjellstrand.webshooter.data.competitionpatrols.CompetitionPatrolsRepository
import se.kjellstrand.webshooter.data.competitionpatrols.local.PatrolsDao
import se.kjellstrand.webshooter.data.competitionpatrols.remote.CompetitionPatrolsRemoteDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class CompetitionPatrolsModule {

    @Provides
    @Singleton
    fun providesCompetitionPatrolsRemoteDataSource(retrofit: Retrofit): CompetitionPatrolsRemoteDataSource {
        return retrofit.create(CompetitionPatrolsRemoteDataSource::class.java)
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
