package se.kjellstrand.webshooter.di

import kotlinx.serialization.json.Json
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import se.kjellstrand.webshooter.data.competitionsignups.CompetitionSignupsRepository
import se.kjellstrand.webshooter.data.competitionsignups.local.CompetitionSignupsDao
import se.kjellstrand.webshooter.data.competitionsignups.remote.CompetitionSignupsRemoteDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class CompetitionSignupsModule {

    @Provides
    @Singleton
    fun providesCompetitionSignupsRemoteDataSource(retrofit: Retrofit): CompetitionSignupsRemoteDataSource {
        return retrofit.create(CompetitionSignupsRemoteDataSource::class.java)
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
