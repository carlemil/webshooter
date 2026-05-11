package se.kjellstrand.webshooter.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module
import se.kjellstrand.webshooter.data.SessionManager
import se.kjellstrand.webshooter.data.charts.ChartsRepository
import se.kjellstrand.webshooter.data.club.ClubRepository
import se.kjellstrand.webshooter.data.club.local.ClubDao
import se.kjellstrand.webshooter.data.club.remote.ClubRemoteDataSource
import se.kjellstrand.webshooter.data.club.remote.ClubRemoteDataSourceKtor
import se.kjellstrand.webshooter.data.clubstats.ClubStatsRepository
import se.kjellstrand.webshooter.data.competitionpatrols.CompetitionPatrolsRepository
import se.kjellstrand.webshooter.data.competitionpatrols.local.PatrolsDao
import se.kjellstrand.webshooter.data.competitionpatrols.remote.CompetitionPatrolsRemoteDataSource
import se.kjellstrand.webshooter.data.competitionpatrols.remote.CompetitionPatrolsRemoteDataSourceKtor
import se.kjellstrand.webshooter.data.competitions.CompetitionsRepository
import se.kjellstrand.webshooter.data.competitions.local.CompetitionsDao
import se.kjellstrand.webshooter.data.competitions.remote.CompetitionsRemoteDataSource
import se.kjellstrand.webshooter.data.competitions.remote.CompetitionsRemoteDataSourceKtor
import se.kjellstrand.webshooter.data.competitionsignups.CompetitionSignupsRepository
import se.kjellstrand.webshooter.data.competitionsignups.local.CompetitionSignupsDao
import se.kjellstrand.webshooter.data.competitionsignups.remote.CompetitionSignupsRemoteDataSource
import se.kjellstrand.webshooter.data.competitionsignups.remote.CompetitionSignupsRemoteDataSourceKtor
import se.kjellstrand.webshooter.data.competitionteams.CompetitionTeamsRepository
import se.kjellstrand.webshooter.data.competitionteams.local.TeamsDao
import se.kjellstrand.webshooter.data.competitionteams.remote.CompetitionTeamsRemoteDataSource
import se.kjellstrand.webshooter.data.competitionteams.remote.CompetitionTeamsRemoteDataSourceKtor
import se.kjellstrand.webshooter.data.cookies.CookiesRepository
import se.kjellstrand.webshooter.data.cookies.remote.CookiesRemoteDataSource
import se.kjellstrand.webshooter.data.cookies.remote.CookiesRemoteDataSourceKtor
import se.kjellstrand.webshooter.data.db.AppDatabase
import se.kjellstrand.webshooter.data.login.LoginRepository
import se.kjellstrand.webshooter.data.login.remote.LoginRemoteDataSource
import se.kjellstrand.webshooter.data.login.remote.LoginRemoteDataSourceKtor
import se.kjellstrand.webshooter.data.mysignups.SignupsRepository
import se.kjellstrand.webshooter.data.mysignups.local.SignupsDao
import se.kjellstrand.webshooter.data.mysignups.remote.SignupsRemoteDataSource
import se.kjellstrand.webshooter.data.mysignups.remote.SignupsRemoteDataSourceKtor
import se.kjellstrand.webshooter.data.results.ResultsRepository
import se.kjellstrand.webshooter.data.results.local.ResultsDao
import se.kjellstrand.webshooter.data.results.remote.ResultsRemoteDataSource
import se.kjellstrand.webshooter.data.results.remote.ResultsRemoteDataSourceKtor
import se.kjellstrand.webshooter.data.seriespoints.SeriesPointsRepository
import se.kjellstrand.webshooter.data.settings.SettingsRepository
import se.kjellstrand.webshooter.data.settings.local.UserProfileDao
import se.kjellstrand.webshooter.data.settings.remote.SettingsRemoteDataSource
import se.kjellstrand.webshooter.data.settings.remote.SettingsRemoteDataSourceKtor
import se.kjellstrand.webshooter.data.signup.SignupRepository
import se.kjellstrand.webshooter.data.signup.remote.SignupRemoteDataSource
import se.kjellstrand.webshooter.data.signup.remote.SignupRemoteDataSourceKtor

/**
 * Koin qualifier for the long-lived application-scoped [CoroutineScope]
 * (SupervisorJob + Dispatchers.Default). Use with
 * `get(qualifier = ApplicationCoroutineScopeQualifier)`. Named distinctly
 * from `:app`'s `@ApplicationScope` Hilt qualifier annotation, which
 * shares the package.
 */
val ApplicationCoroutineScopeQualifier = named("ApplicationCoroutineScope")

/**
 * Platform-agnostic Koin module: every singleton whose construction does not
 * need anything from the host platform. Combined with the platform module
 * (Android or iOS) at [initKoin] time.
 */
val sharedModule = module {

    single {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            explicitNulls = false
            // Match the old Gson behavior: emit fields even when they equal their
            // declared default. The OAuth endpoint requires client_id and
            // grant_type, both of which use defaults in LoginRequest.
            encodeDefaults = true
        }
    }

    single { SessionManager() }

    single<CoroutineScope>(ApplicationCoroutineScopeQualifier) {
        // Dispatchers.IO is JVM-only in kotlinx-coroutines 1.8.x; Default is
        // fine here since Ktor and Room do their own dispatching for actual
        // blocking work.
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    // DAOs are derived from the platform-provided AppDatabase singleton.
    single { get<AppDatabase>().competitionsDao() }
    single { get<AppDatabase>().resultsDao() }
    single { get<AppDatabase>().patrolsDao() }
    single { get<AppDatabase>().teamsDao() }
    single { get<AppDatabase>().competitionSignupsDao() }
    single { get<AppDatabase>().signupsDao() }
    single { get<AppDatabase>().clubDao() }
    single { get<AppDatabase>().userProfileDao() }

    // Remote data sources — bound to their Ktor implementations.
    single<LoginRemoteDataSource> { LoginRemoteDataSourceKtor(get()) }
    single<CompetitionsRemoteDataSource> { CompetitionsRemoteDataSourceKtor(get()) }
    single<ResultsRemoteDataSource> { ResultsRemoteDataSourceKtor(get()) }
    single<ClubRemoteDataSource> { ClubRemoteDataSourceKtor(get()) }
    single<SettingsRemoteDataSource> { SettingsRemoteDataSourceKtor(get()) }
    single<SignupRemoteDataSource> { SignupRemoteDataSourceKtor(get()) }
    single<SignupsRemoteDataSource> { SignupsRemoteDataSourceKtor(get()) }
    single<CompetitionPatrolsRemoteDataSource> { CompetitionPatrolsRemoteDataSourceKtor(get()) }
    single<CompetitionTeamsRemoteDataSource> { CompetitionTeamsRemoteDataSourceKtor(get()) }
    single<CompetitionSignupsRemoteDataSource> { CompetitionSignupsRemoteDataSourceKtor(get()) }
    single<CookiesRemoteDataSource> { CookiesRemoteDataSourceKtor(get()) }

    // Repositories.
    single { LoginRepository(get<LoginRemoteDataSource>(), get<WebshooterConfig>().clientSecret) }
    single { ResultsRepository(get<ResultsRemoteDataSource>(), get<ResultsDao>(), get()) }
    single {
        CompetitionsRepository(
            get<CompetitionsRemoteDataSource>(),
            get<CompetitionsDao>(),
            get(),
            get<ResultsRepository>(),
            get<ResultsDao>(),
        )
    }
    single { ClubRepository(get<ClubRemoteDataSource>(), get<ClubDao>(), get()) }
    single { SettingsRepository(get<SettingsRemoteDataSource>(), get<UserProfileDao>(), get()) }
    single { SignupRepository(get<SignupRemoteDataSource>()) }
    single { SignupsRepository(get<SignupsRemoteDataSource>(), get<SignupsDao>(), get()) }
    single { CompetitionPatrolsRepository(get<CompetitionPatrolsRemoteDataSource>(), get<PatrolsDao>(), get()) }
    single { CompetitionTeamsRepository(get<CompetitionTeamsRemoteDataSource>(), get<TeamsDao>(), get()) }
    single { CompetitionSignupsRepository(get<CompetitionSignupsRemoteDataSource>(), get<CompetitionSignupsDao>(), get()) }
    single { CookiesRepository(get<CookiesRemoteDataSource>()) }
    single { ClubStatsRepository(get<ClubRepository>(), get<ResultsDao>()) }
    single { ChartsRepository(get<CompetitionsDao>(), get<ResultsDao>(), get()) }
    single { SeriesPointsRepository(get<ResultsDao>(), get()) }
}
