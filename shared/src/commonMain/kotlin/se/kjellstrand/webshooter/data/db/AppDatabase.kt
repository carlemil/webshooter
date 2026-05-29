package se.kjellstrand.webshooter.data.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import se.kjellstrand.webshooter.data.club.local.ClubDao
import se.kjellstrand.webshooter.data.club.local.ClubEntity
import se.kjellstrand.webshooter.data.competitionpatrols.local.PatrolEntity
import se.kjellstrand.webshooter.data.competitionpatrols.local.PatrolsDao
import se.kjellstrand.webshooter.data.competitionteams.local.TeamEntity
import se.kjellstrand.webshooter.data.competitionteams.local.TeamsDao
import se.kjellstrand.webshooter.data.competitions.local.CompetitionEntity
import se.kjellstrand.webshooter.data.competitions.local.CompetitionsDao
import se.kjellstrand.webshooter.data.competitionsignups.local.CompetitionSignupEntity
import se.kjellstrand.webshooter.data.competitionsignups.local.CompetitionSignupsDao
import se.kjellstrand.webshooter.data.results.local.ResultEntity
import se.kjellstrand.webshooter.data.results.local.ResultsDao
import se.kjellstrand.webshooter.data.settings.local.UserProfileDao
import se.kjellstrand.webshooter.data.settings.local.UserProfileEntity
import se.kjellstrand.webshooter.data.mysignups.local.SignupEntryEntity
import se.kjellstrand.webshooter.data.mysignups.local.SignupsDao

@Database(
    entities = [
        CompetitionEntity::class,
        ResultEntity::class,
        PatrolEntity::class,
        TeamEntity::class,
        CompetitionSignupEntity::class,
        SignupEntryEntity::class,
        ClubEntity::class,
        UserProfileEntity::class
    ],
    version = DB_VERSION,
    exportSchema = true
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun competitionsDao(): CompetitionsDao
    abstract fun resultsDao(): ResultsDao
    abstract fun patrolsDao(): PatrolsDao
    abstract fun teamsDao(): TeamsDao
    abstract fun competitionSignupsDao(): CompetitionSignupsDao
    abstract fun signupsDao(): SignupsDao
    abstract fun clubDao(): ClubDao
    abstract fun userProfileDao(): UserProfileDao
}

// KSP generates the `actual` for non-Android targets at build time; Android
// still uses reflection via the legacy `Room.databaseBuilder(context, klass)`
// API, so no manual actual is needed there either.
@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

