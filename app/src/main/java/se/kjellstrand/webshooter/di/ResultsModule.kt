package se.kjellstrand.webshooter.di

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import se.kjellstrand.webshooter.data.results.ResultsRepository
import se.kjellstrand.webshooter.data.results.local.ResultsDao
import se.kjellstrand.webshooter.data.results.remote.ResultsRemoteDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ResultsModule {

    @Provides
    @Singleton
    fun providesResultsRemoteDataSource(retrofit: Retrofit) : ResultsRemoteDataSource {
        return retrofit.create(ResultsRemoteDataSource::class.java)
    }

    @Provides
    @Singleton
    fun providesResultsRepository(
        resultsRemoteDataSource: ResultsRemoteDataSource,
        dao: ResultsDao,
        gson: Gson
    ) : ResultsRepository {
        return ResultsRepository(resultsRemoteDataSource, dao, gson)
    }
}
