package se.kjellstrand.webshooter.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import se.kjellstrand.webshooter.data.club.local.ClubDao
import se.kjellstrand.webshooter.data.competitionpatrols.local.PatrolsDao
import se.kjellstrand.webshooter.data.competitions.local.CompetitionsDao
import se.kjellstrand.webshooter.data.competitionsignups.local.CompetitionSignupsDao
import se.kjellstrand.webshooter.data.db.AppDatabase
import se.kjellstrand.webshooter.data.results.local.ResultsDao
import se.kjellstrand.webshooter.data.settings.local.UserProfileDao
import se.kjellstrand.webshooter.data.mysignups.local.SignupsDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "webshooter.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideCompetitionsDao(db: AppDatabase): CompetitionsDao = db.competitionsDao()
    @Provides fun provideResultsDao(db: AppDatabase): ResultsDao = db.resultsDao()
    @Provides fun providePatrolsDao(db: AppDatabase): PatrolsDao = db.patrolsDao()
    @Provides fun provideCompetitionSignupsDao(db: AppDatabase): CompetitionSignupsDao = db.competitionSignupsDao()
    @Provides fun provideSignupsDao(db: AppDatabase): SignupsDao = db.signupsDao()
    @Provides fun provideClubDao(db: AppDatabase): ClubDao = db.clubDao()
    @Provides fun provideUserProfileDao(db: AppDatabase): UserProfileDao = db.userProfileDao()
}
