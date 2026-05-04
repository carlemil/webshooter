package se.kjellstrand.webshooter.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import se.kjellstrand.webshooter.data.competitions.CompetitionsRepository
import se.kjellstrand.webshooter.data.competitions.local.CompetitionsDao
import se.kjellstrand.webshooter.data.competitions.remote.CompetitionsRemoteDataSource
import se.kjellstrand.webshooter.data.competitions.remote.CompetitionsRemoteDataSourceKtor
import se.kjellstrand.webshooter.data.results.ResultsRepository
import se.kjellstrand.webshooter.data.results.local.ResultsDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class CompetitionsModule {

    @Provides
    @Singleton
    fun providesCompetitionsRemoteDataSource(httpClient: HttpClient): CompetitionsRemoteDataSource {
        return CompetitionsRemoteDataSourceKtor(httpClient)
    }

    @Provides
    @Singleton
    fun providesCompetitionsRepository(
        competitionsRemoteDataSource: CompetitionsRemoteDataSource,
        dao: CompetitionsDao,
        json: Json,
        resultsRepository: ResultsRepository,
        resultsDao: ResultsDao
    ) : CompetitionsRepository {
        return CompetitionsRepository(competitionsRemoteDataSource, dao, json, resultsRepository, resultsDao)
    }
}
