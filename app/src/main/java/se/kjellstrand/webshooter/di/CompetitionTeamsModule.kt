package se.kjellstrand.webshooter.di

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import se.kjellstrand.webshooter.data.competitionteams.CompetitionTeamsRepository
import se.kjellstrand.webshooter.data.competitionteams.local.TeamsDao
import se.kjellstrand.webshooter.data.competitionteams.remote.CompetitionTeamsRemoteDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class CompetitionTeamsModule {

    @Provides
    @Singleton
    fun providesCompetitionTeamsRemoteDataSource(retrofit: Retrofit): CompetitionTeamsRemoteDataSource {
        return retrofit.create(CompetitionTeamsRemoteDataSource::class.java)
    }

    @Provides
    @Singleton
    fun providesCompetitionTeamsRepository(
        remoteDataSource: CompetitionTeamsRemoteDataSource,
        dao: TeamsDao,
        gson: Gson
    ): CompetitionTeamsRepository {
        return CompetitionTeamsRepository(remoteDataSource, dao, gson)
    }
}
