package se.kjellstrand.webshooter.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import se.kjellstrand.webshooter.ui.screens.competitions.CompetitionsScreen
import se.kjellstrand.webshooter.ui.screens.splash.SplashScreen
import se.kjellstrand.webshooter.ui.screens.competitions.CompetitionsViewModelImpl
import se.kjellstrand.webshooter.ui.landingscreen.WebShooterScreen
import se.kjellstrand.webshooter.ui.screens.login.LoginScreen
import se.kjellstrand.webshooter.ui.screens.results.CompetitionResultsScreen
import se.kjellstrand.webshooter.ui.screens.results.ResultsViewModelImpl
import se.kjellstrand.webshooter.ui.screens.shooterresult.ShooterResultScreen
import se.kjellstrand.webshooter.ui.screens.patrols.CompetitionPatrolsScreen
import se.kjellstrand.webshooter.ui.screens.patrols.CompetitionPatrolsViewModelImpl
import se.kjellstrand.webshooter.ui.screens.signups.CompetitionSignupsScreen
import se.kjellstrand.webshooter.ui.screens.signups.CompetitionSignupsViewModelImpl
import se.kjellstrand.webshooter.ui.screens.signup.SignupScreen
import se.kjellstrand.webshooter.ui.screens.signup.SignupViewModel

@Composable
fun AppNavHost(navController: NavHostController) {
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
                navArgument("competitionId") { type = NavType.IntType },
                navArgument("resultsType") { type = NavType.StringType },
                navArgument("competitionName") { type = NavType.StringType; defaultValue = "" },
                navArgument("competitionDate") { type = NavType.StringType; defaultValue = "" }
            )
        ) {
            val resultsViewModel: ResultsViewModelImpl = hiltViewModel()
            CompetitionResultsScreen(resultsViewModel, navController)
        }
        composable(
            route = Screen.ShooterResult.route,
            arguments = listOf(
                navArgument("competitionId") { type = NavType.IntType },
                navArgument("shooterId") { type = NavType.IntType },
                navArgument("resultsType") { type = NavType.StringType }
            )
        ) {
            ShooterResultScreen(navController)
        }
        composable(
            route = Screen.CompetitionSignupsList.route,
            arguments = listOf(navArgument("competitionId") { type = NavType.LongType })
        ) {
            val signupsListViewModel: CompetitionSignupsViewModelImpl = hiltViewModel()
            CompetitionSignupsScreen(navController, signupsListViewModel)
        }
        composable(
            route = Screen.CompetitionPatrols.route,
            arguments = listOf(
                navArgument("competitionId") { type = NavType.LongType },
                navArgument("competitionTypeId") { type = NavType.IntType }
            )
        ) {
            val patrolsViewModel: CompetitionPatrolsViewModelImpl = hiltViewModel()
            CompetitionPatrolsScreen(navController, patrolsViewModel)
        }
        composable(
            route = Screen.CompetitionSignup.route,
            arguments = listOf(navArgument("competitionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.LandingScreen.route)
            }
            val competitionId = backStackEntry.arguments?.getLong("competitionId") ?: -1L
            val competitionsViewModel: CompetitionsViewModelImpl = hiltViewModel(parentEntry)
            val signupViewModel: SignupViewModel = hiltViewModel()
            val competitionsState by competitionsViewModel.uiState.collectAsState()
            val signupState by signupViewModel.uiState.collectAsState()
            val competition = remember(competitionId) {
                competitionsState.competitions?.data?.find { it.id == competitionId }
            }
            LaunchedEffect(signupState.isSuccess) {
                if (signupState.isSuccess) {
                    navController.popBackStack()
                    competitionsViewModel.reload()
                }
            }
            if (competition != null) {
                SignupScreen(competition, signupViewModel, navController)
            }
        }
    }
}