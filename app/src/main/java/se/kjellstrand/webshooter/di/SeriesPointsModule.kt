package se.kjellstrand.webshooter.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import se.kjellstrand.webshooter.data.results.local.ResultsDao
import se.kjellstrand.webshooter.data.seriespoints.SeriesPointsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SeriesPointsModule {

    @Provides
    @Singleton
    fun providesSeriesPointsRepository(
        resultsDao: ResultsDao,
        json: Json
    ): SeriesPointsRepository {
        return SeriesPointsRepository(resultsDao, json)
    }
}
