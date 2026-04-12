package se.kjellstrand.webshooter.di

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import se.kjellstrand.webshooter.data.charts.ChartsRepository
import se.kjellstrand.webshooter.data.competitions.local.CompetitionsDao
import se.kjellstrand.webshooter.data.results.ResultsRepository
import se.kjellstrand.webshooter.data.results.local.ResultsDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ChartsModule {

    @Provides
    @Singleton
    fun providesChartsRepository(
        resultsRepository: ResultsRepository,
        competitionsDao: CompetitionsDao,
        resultsDao: ResultsDao,
        gson: Gson
    ): ChartsRepository {
        return ChartsRepository(resultsRepository, competitionsDao, resultsDao, gson)
    }
}
