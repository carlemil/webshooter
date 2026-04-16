package se.kjellstrand.webshooter.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import se.kjellstrand.webshooter.data.club.ClubRepository
import se.kjellstrand.webshooter.data.clubstats.ClubStatsRepository
import se.kjellstrand.webshooter.data.results.local.ResultsDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ClubStatsModule {

    @Provides
    @Singleton
    fun providesClubStatsRepository(
        clubRepository: ClubRepository,
        resultsDao: ResultsDao
    ): ClubStatsRepository {
        return ClubStatsRepository(clubRepository, resultsDao)
    }
}
