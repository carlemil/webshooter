package se.kjellstrand.webshooter.di

import kotlinx.serialization.json.Json
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import se.kjellstrand.webshooter.data.charts.ChartsRepository
import se.kjellstrand.webshooter.data.competitions.local.CompetitionsDao
import se.kjellstrand.webshooter.data.results.local.ResultsDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ChartsModule {

    @Provides
    @Singleton
    fun providesChartsRepository(
        competitionsDao: CompetitionsDao,
        resultsDao: ResultsDao,
        json: Json
    ): ChartsRepository {
        return ChartsRepository(competitionsDao, resultsDao, json)
    }
}
