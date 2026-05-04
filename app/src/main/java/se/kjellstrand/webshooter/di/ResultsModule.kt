package se.kjellstrand.webshooter.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import se.kjellstrand.webshooter.data.results.ResultsRepository
import se.kjellstrand.webshooter.data.results.local.ResultsDao
import se.kjellstrand.webshooter.data.results.remote.ResultsRemoteDataSource
import se.kjellstrand.webshooter.data.results.remote.ResultsRemoteDataSourceKtor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ResultsModule {

    @Provides
    @Singleton
    fun providesResultsRemoteDataSource(httpClient: HttpClient): ResultsRemoteDataSource {
        return ResultsRemoteDataSourceKtor(httpClient)
    }

    @Provides
    @Singleton
    fun providesResultsRepository(
        resultsRemoteDataSource: ResultsRemoteDataSource,
        dao: ResultsDao,
        json: Json
    ) : ResultsRepository {
        return ResultsRepository(resultsRemoteDataSource, dao, json)
    }
}
