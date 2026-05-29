package se.kjellstrand.webshooter.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import se.kjellstrand.webshooter.ui.mock.ChartsViewModelMock
import se.kjellstrand.webshooter.ui.mock.ClubViewModelMock
import se.kjellstrand.webshooter.ui.mock.CompetitionsViewModelMock
import se.kjellstrand.webshooter.ui.mock.LoginViewModelMock
import se.kjellstrand.webshooter.ui.mock.MockCompetitions
import se.kjellstrand.webshooter.ui.mock.MockResults
import se.kjellstrand.webshooter.ui.mock.MyResultsViewModelMock
import se.kjellstrand.webshooter.ui.mock.PatrolsViewModelMock
import se.kjellstrand.webshooter.ui.mock.ResultsViewModelMock
import se.kjellstrand.webshooter.ui.mock.SettingsViewModelMock
import se.kjellstrand.webshooter.ui.mock.ShooterResultViewModelMock
import se.kjellstrand.webshooter.ui.mock.SignupViewModelMock
import se.kjellstrand.webshooter.ui.mock.SignupsViewModelMock
import se.kjellstrand.webshooter.ui.mock.TeamsViewModelMock
import se.kjellstrand.webshooter.ui.screens.charts.resulttrends.ChartsScreen
import se.kjellstrand.webshooter.ui.screens.club.ClubScreen
import se.kjellstrand.webshooter.ui.screens.competitions.CompetitionsScreen
import se.kjellstrand.webshooter.ui.screens.licenses.LicensesScreen
import se.kjellstrand.webshooter.ui.screens.login.LoginScreen
import se.kjellstrand.webshooter.ui.screens.myresults.MyEntriesScreen
import se.kjellstrand.webshooter.ui.screens.patrols.CompetitionPatrolsScreen
import se.kjellstrand.webshooter.ui.screens.results.CompetitionResultsScreen
import se.kjellstrand.webshooter.ui.screens.results.ResultsUiState
import se.kjellstrand.webshooter.ui.screens.settings.SettingsScreen
import se.kjellstrand.webshooter.ui.screens.shooterresult.ShooterResultScreen
import se.kjellstrand.webshooter.ui.screens.shooterresult.ShooterResultUiState
import se.kjellstrand.webshooter.ui.screens.signup.SignupScreen
import se.kjellstrand.webshooter.ui.screens.signups.CompetitionSignupsScreen
import se.kjellstrand.webshooter.ui.screens.teams.CompetitionTeamsScreen

/**
 * iOS-only debug entry point. Lets us render any single screen using its
 * existing mock VM (from :shared/commonMain/ui/mock/) by setting the `SCREEN`
 * env var when launching the app via `xcrun simctl launch booted bundle.id
 * SIMCTL_CHILD_SCREEN=<name>`. Lets a developer (or this CI) walk every
 * screen on Simulator without needing valid auth or hitting the backend.
 *
 * The wider app entry point (`MainViewController`) honours this env var when
 * present; otherwise it boots the real `AppNavHost`.
 *
 * ClubStats and SeriesPoints are intentionally absent: their screens take
 * concrete `*Impl` ViewModels and no mocks exist for them in `ui/mock/`.
 * Verify those interactively against the real backend.
 */
@Composable
fun DebugGallery(screenName: String) {
    when (screenName) {
        "login" -> LoginScreen(
            onLoginSuccess = {},
            loginViewModel = LoginViewModelMock(),
        )
        "competitions" -> CompetitionsScreen(
            onNavigateToResults = { _, _, _, _ -> },
            onNavigateToSignup = {},
            onNavigateToSignupsList = {},
            onNavigateToPatrols = { _, _ -> },
            onNavigateToTeams = {},
            competitionsViewModel = CompetitionsViewModelMock(),
        )
        "myentries" -> MyEntriesScreen(viewModel = MyResultsViewModelMock())
        "charts" -> ChartsScreen(viewModel = ChartsViewModelMock())
        "club" -> ClubScreen(viewModel = ClubViewModelMock())
        "settings" -> SettingsScreen(viewModel = SettingsViewModelMock())
        "licenses" -> LicensesScreen()
        // NOTE: populating groupedResults via `ResultsViewModelImpl.groupResults`
        // on these mocks triggers a LazyColumn duplicate-key crash on iOS
        // (kotlin.IllegalArgumentException: "Key 'group-C3-16343' was already
        // used"). The same combination also crashes on Android at runtime —
        // the existing @Preview just doesn't trigger LazyList layout. This is
        // a real latent bug in the screen's key derivation. Tracked TODO;
        // leaving these screens with empty groupedResults so the gallery stays
        // crash-free for verification purposes.
        "results" -> {
            val mockResults = MockResults().results
            CompetitionResultsScreen(
                resultsViewModel = ResultsViewModelMock(
                    ResultsUiState(
                        isLoading = false,
                        competitionName = "Vintercup 2026",
                        results = mockResults,
                    )
                ),
                onBack = {},
                onNavigateToShooter = {},
                showMessage = {},
            )
        }
        "shooter" -> {
            val mockResults = MockResults().results
            ShooterResultScreen(
                onBack = {},
                viewModel = ShooterResultViewModelMock(
                    ShooterResultUiState(
                        isLoading = false,
                        shooterName = "Erik Svensson",
                        results = mockResults,
                    )
                ),
            )
        }
        "signupslist" -> CompetitionSignupsScreen(
            onBack = {},
            viewModel = SignupsViewModelMock(),
        )
        "patrols" -> CompetitionPatrolsScreen(
            onBack = {},
            viewModel = PatrolsViewModelMock(),
        )
        "teams" -> CompetitionTeamsScreen(
            onBack = {},
            viewModel = TeamsViewModelMock(),
        )
        "signup" -> SignupScreen(
            competition = MockCompetitions().competitions.data.first(),
            viewModel = SignupViewModelMock(),
            onBack = {},
        )
        else -> Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Text(
                text = "Unknown SCREEN env: '$screenName'.\nKnown: login, competitions, myentries, charts, club, settings, licenses, results, shooter, signupslist, patrols, teams, signup.",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
