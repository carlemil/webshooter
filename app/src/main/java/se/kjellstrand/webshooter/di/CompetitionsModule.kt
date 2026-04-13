package se.kjellstrand.webshooter.di

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import se.kjellstrand.webshooter.data.competitions.CompetitionsRepository
import se.kjellstrand.webshooter.data.competitions.local.CompetitionsDao
import se.kjellstrand.webshooter.data.competitions.remote.CompetitionsRemoteDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class CompetitionsModule {

    @Provides
    @Singleton
    fun providesCompetitionsRemoteDataSource(retrofit: Retrofit) : CompetitionsRemoteDataSource {
        return retrofit.create(CompetitionsRemoteDataSource::class.java)
    }

    @Provides
    @Singleton
    fun providesCompetitionsRepository(
        competitionsRemoteDataSource: CompetitionsRemoteDataSource,
        dao: CompetitionsDao,
        gson: Gson
    ) : CompetitionsRepository {
        return CompetitionsRepository(competitionsRemoteDataSource, dao, gson)
    }
}
