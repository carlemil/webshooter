package se.kjellstrand.webshooter.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import se.kjellstrand.webshooter.ui.competitions.CompetitionDetailScreen
import se.kjellstrand.webshooter.ui.competitions.CompetitionsScreen
import se.kjellstrand.webshooter.ui.competitions.CompetitionsViewModel
import se.kjellstrand.webshooter.ui.landingscreen.LandingScreen
import se.kjellstrand.webshooter.ui.login.LoginScreen
import se.kjellstrand.webshooter.ui.results.CompetitionResultsScreen
import se.kjellstrand.webshooter.ui.results.ResultsViewModel

@Composable
fun AppNavHost(navController: NavHostController) {
    val systemUiController = rememberSystemUiController()
    // Hide the status bar and navigation bar
    SideEffect {
        systemUiController.isStatusBarVisible = false
        systemUiController.isNavigationBarVisible = false
    }

    NavHost(navController, startDestination = Screen.CompetitionsList.route) {
        composable(Screen.LoginScreen.route) {
            LoginScreen(navController)
        }
        composable(Screen.LandingScreen.route) {
            LandingScreen(navController)
        }
        composable(Screen.CompetitionsList.route) {
            // Should we move these two out of the composable? looks like it is the reasonable thing to do
            val competitionsViewModel: CompetitionsViewModel = hiltViewModel()
            val competitionsState by competitionsViewModel.uiState.collectAsState()

            CompetitionsScreen(navController, competitionsState)
        }
        composable(
            route = Screen.CompetitionDetail.route,
            arguments = listOf(navArgument("competitionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.CompetitionsList.route)
            }
            val competitionId = backStackEntry.arguments?.getLong("competitionId") ?: -1
            val competitionsViewModel: CompetitionsViewModel = hiltViewModel(parentEntry)
            val competitionsState by competitionsViewModel.uiState.collectAsState()
            CompetitionDetailScreen(navController, competitionsState, competitionId)
        }
        composable(
            route = Screen.CompetitionResults.route,
            arguments = listOf(navArgument("competitionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.CompetitionResults.route)
            }
            val resultsViewModel: ResultsViewModel = hiltViewModel(parentEntry)
            val resultsUiState by resultsViewModel.uiState.collectAsState()
            CompetitionResultsScreen(resultsUiState)
        }
    }
}