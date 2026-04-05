package se.kjellstrand.webshooter.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.flow.collectLatest
import se.kjellstrand.webshooter.data.SessionManager
import se.kjellstrand.webshooter.ui.screens.competitions.CompetitionsScreen
import se.kjellstrand.webshooter.ui.screens.splash.SplashScreen
import se.kjellstrand.webshooter.ui.screens.competitions.CompetitionsViewModelImpl
import se.kjellstrand.webshooter.ui.landingscreen.WebShooterScreen
import se.kjellstrand.webshooter.ui.screens.login.LoginScreen
import se.kjellstrand.webshooter.ui.screens.results.CompetitionResultsScreen
import se.kjellstrand.webshooter.ui.screens.results.ResultsViewModelImpl
import se.kjellstrand.webshooter.ui.screens.shooterresult.ShooterResultScreen
import se.kjellstrand.webshooter.ui.screens.patrols.CompetitionPatrolsScreen
import se.kjellstrand.webshooter.ui.screens.patrols.PatrolsViewModelImpl
import se.kjellstrand.webshooter.ui.screens.signups.CompetitionSignupsScreen
import se.kjellstrand.webshooter.ui.screens.signups.SignupsViewModelImpl
import se.kjellstrand.webshooter.ui.screens.signup.SignupScreen
import se.kjellstrand.webshooter.ui.screens.signup.SignupViewModel
import se.kjellstrand.webshooter.ui.screens.teams.CompetitionTeamsScreen
import se.kjellstrand.webshooter.ui.screens.teams.TeamsViewModelImpl

@Composable
fun AppNavHost(navController: NavHostController) {
    val sessionViewModel: SessionViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        sessionViewModel.sessionManager.events.collectLatest { event ->
            when (event) {
                is SessionManager.SessionEvent.Expired -> {
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }

    NavHost(navController, startDestination = Screen.SplashScreen.route) {
        composable(Screen.SplashScreen.route) {
            SplashScreen(navController)
        }
        composable(Screen.LoginScreen.route) {
            LoginScreen(navController)
        }
        composable(Screen.LandingScreen.route) {
            WebShooterScreen(navController)
        }
        composable(Screen.CompetitionsList.route) {
            val competitionsViewModel: CompetitionsViewModelImpl = hiltViewModel()
            CompetitionsScreen(navController, competitionsViewModel)
        }
        composable(
            route = Screen.CompetitionResults.route,
            arguments = listOf(
                navArgument("competitionId") { type = NavType.LongType },
                navArgument("resultsType") { type = NavType.StringType },
                navArgument("competitionName") { type = NavType.StringType; defaultValue = "" },
                navArgument("competitionDate") { type = NavType.StringType; defaultValue = "" }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = Screen.CompetitionResults.deepLink }
            )
        ) {
            val resultsViewModel: ResultsViewModelImpl = hiltViewModel()
            CompetitionResultsScreen(resultsViewModel, navController)
        }
        composable(
            route = Screen.ShooterResult.route,
            arguments = listOf(
                navArgument("competitionId") { type = NavType.LongType },
                navArgument("shooterId") { type = NavType.LongType },
                navArgument("resultsType") { type = NavType.StringType }
            )
        ) {
            ShooterResultScreen(navController)
        }
        composable(
            route = Screen.CompetitionSignupsList.route,
            arguments = listOf(navArgument("competitionId") { type = NavType.LongType })
        ) {
            val signupsListViewModel: SignupsViewModelImpl = hiltViewModel()
            CompetitionSignupsScreen(navController, signupsListViewModel)
        }
        composable(
            route = Screen.CompetitionPatrols.route,
            arguments = listOf(
                navArgument("competitionId") { type = NavType.LongType },
                navArgument("competitionTypeId") { type = NavType.IntType }
            )
        ) {
            val patrolsViewModel: PatrolsViewModelImpl = hiltViewModel()
            CompetitionPatrolsScreen(navController, patrolsViewModel)
        }
        composable(
            route = Screen.CompetitionTeams.route,
            arguments = listOf(navArgument("competitionId") { type = NavType.LongType })
        ) {
            val teamsViewModel: TeamsViewModelImpl = hiltViewModel()
            CompetitionTeamsScreen(navController, teamsViewModel)
        }
        composable(
            route = Screen.CompetitionSignup.route,
            arguments = listOf(navArgument("competitionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val context = LocalContext.current
            val competitionId = try {
                NavigationArguments.requireLong(backStackEntry.arguments, "competitionId")
            } catch (e: IllegalArgumentException) {
                Log.e("AppNavHost", "CompetitionSignup: ${e.message}")
                LaunchedEffect(Unit) {
                    Toast.makeText(context, "Failed to open signup: missing competition", Toast.LENGTH_LONG).show()
                    navController.safePopBackStack()
                }
                return@composable
            }
            val parentEntry = remember(backStackEntry) {
                try {
                    navController.getBackStackEntry(Screen.LandingScreen.route)
                } catch (e: IllegalArgumentException) {
                    Log.e("AppNavHost", "CompetitionSignup: LandingScreen not in back stack", e)
                    null
                }
            }
            if (parentEntry == null) {
                LaunchedEffect(Unit) {
                    Toast.makeText(context, "Failed to open signup: navigation error", Toast.LENGTH_LONG).show()
                    navController.safePopBackStack()
                }
                return@composable
            }
            val competitionsViewModel: CompetitionsViewModelImpl = hiltViewModel(parentEntry)
            val signupViewModel: SignupViewModel = hiltViewModel()
            val competitionsState by competitionsViewModel.uiState.collectAsState()
            val signupState by signupViewModel.uiState.collectAsState()
            val competition = remember(competitionId) {
                competitionsState.competitions?.data?.find { it.id == competitionId }
            }
            LaunchedEffect(signupState.isSuccess) {
                if (signupState.isSuccess) {
                    navController.safePopBackStack()
                    competitionsViewModel.reload()
                }
            }
            if (competition != null) {
                SignupScreen(competition, signupViewModel, navController)
            }
        }
    }
}