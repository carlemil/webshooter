package se.kjellstrand.webshooter.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.ParametersHolder
import org.koin.dsl.module
import se.kjellstrand.webshooter.data.competitions.remote.ResultsType
import se.kjellstrand.webshooter.ui.navigation.SessionViewModel
import se.kjellstrand.webshooter.ui.screens.charts.clubstats.ClubStatsViewModelImpl
import se.kjellstrand.webshooter.ui.screens.charts.resulttrends.ResultsTrendsViewModelImpl
import se.kjellstrand.webshooter.ui.screens.charts.seriespoints.SeriesPointsViewModelImpl
import se.kjellstrand.webshooter.ui.screens.club.ClubViewModelImpl
import se.kjellstrand.webshooter.ui.screens.competitions.CompetitionsViewModelImpl
import se.kjellstrand.webshooter.ui.screens.login.LoginViewModelImpl
import se.kjellstrand.webshooter.ui.screens.myresults.MyResultsViewModelImpl
import se.kjellstrand.webshooter.ui.screens.patrols.PatrolsViewModelImpl
import se.kjellstrand.webshooter.ui.screens.results.ResultsViewModelImpl
import se.kjellstrand.webshooter.ui.screens.settings.SettingsViewModelImpl
import se.kjellstrand.webshooter.ui.screens.shooterresult.ShooterResultViewModelImpl
import se.kjellstrand.webshooter.ui.screens.signup.SignupViewModelImpl
import se.kjellstrand.webshooter.ui.screens.signups.SignupsViewModelImpl
import se.kjellstrand.webshooter.ui.screens.splash.SplashViewModel
import se.kjellstrand.webshooter.ui.screens.teams.TeamsViewModelImpl

/** Typed positional read from a Koin [ParametersHolder] (positional get
 *  by-type isn't a member of ParametersHolder in 4.0.4). */
private inline fun <reified T : Any> ParametersHolder.at(index: Int): T =
    elementAt(index, T::class)

/**
 * Koin module for every screen's ViewModel. Constructor parameters are
 * resolved from [sharedModule] (repositories, AuthTokenManager, SessionManager,
 * SecurePrefs) plus the platform module (Json, HttpClient, etc.). VMs that
 * take a `SavedStateHandle` get it auto-injected by Koin's compose-viewmodel
 * integration when acquired via `koinViewModel()` from a Compose Navigation
 * back stack entry.
 */
val viewModelsModule = module {
    // No-argument VMs.
    viewModel { SessionViewModel(get()) }
    viewModel { SplashViewModel(get(), get()) }
    viewModel { CompetitionsViewModelImpl(get()) }
    viewModel { ClubViewModelImpl(get()) }
    viewModel { ClubStatsViewModelImpl(get()) }
    viewModel { ResultsTrendsViewModelImpl(get(), get(), get()) }
    viewModel { SeriesPointsViewModelImpl(get(), get(), get()) }
    viewModel { SettingsViewModelImpl(get(), get(), get()) }
    viewModel { MyResultsViewModelImpl(get(), get(), get()) }
    viewModel {
        LoginViewModelImpl(
            loginRepository = get(),
            cookiesRepository = get(),
            authTokenManager = get(),
            securePrefs = get(),
            competitionsRepository = get(),
            applicationScope = get(qualifier = ApplicationCoroutineScopeQualifier),
            crashReporter = get(),
        )
    }

    // VMs that take nav-arg-derived values as explicit constructor params.
    // Each call site supplies them in order via `koinViewModel { parametersOf(...) }`.
    // Positional get<T>(index) is used because Koin's destructuring conflicts
    // with stdlib componentN() extensions on this Kotlin version.
    viewModel { params: ParametersHolder ->
        SignupViewModelImpl(
            competitionId = params.at<Long>(0),
            signupRepository = get(),
            settingsRepository = get(),
        )
    }
    viewModel { params: ParametersHolder ->
        SignupsViewModelImpl(
            competitionId = params.at<Long>(0),
            repository = get(),
            settingsRepository = get(),
        )
    }
    viewModel { params: ParametersHolder ->
        PatrolsViewModelImpl(
            competitionId = params.at<Long>(0),
            competitionTypeId = params.at<Int>(1),
            repository = get(),
            settingsRepository = get(),
        )
    }
    viewModel { params: ParametersHolder ->
        TeamsViewModelImpl(
            competitionId = params.at<Long>(0),
            repository = get(),
            settingsRepository = get(),
        )
    }
    viewModel { params: ParametersHolder ->
        ResultsViewModelImpl(
            competitionId = params.at<Long>(0),
            competitionDate = params.at<String>(1),
            resultsType = params.at<ResultsType>(2),
            competitionName = params.at<String>(3),
            resultsRepository = get(),
            settingsRepository = get(),
        )
    }
    viewModel { params: ParametersHolder ->
        ShooterResultViewModelImpl(
            competitionId = params.at<Long>(0),
            shooterId = params.at<Long>(1),
            resultsType = params.at<ResultsType>(2),
            resultsRepository = get(),
        )
    }
}
