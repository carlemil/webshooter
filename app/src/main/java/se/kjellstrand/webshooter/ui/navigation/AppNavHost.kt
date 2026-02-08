package se.kjellstrand.webshooter.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
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
import se.kjellstrand.webshooter.ui.competitions.CompetitionsViewModelImpl
import se.kjellstrand.webshooter.ui.landingscreen.WebShooterScreen
import se.kjellstrand.webshooter.ui.login.LoginScreen
import se.kjellstrand.webshooter.ui.results.CompetitionResultsScreen
import se.kjellstrand.webshooter.ui.results.ResultsViewModelImpl

@Composable
fun AppNavHost(navController: NavHostController) {
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.isStatusBarVisible = false
        systemUiController.isNavigationBarVisible = false
    }

    NavHost(navController, startDestination = Screen.LoginScreen.route) {
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
            route = Screen.CompetitionDetail.route,
            arguments = listOf(navArgument("competitionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.LandingScreen.route)
            }
            val competitionId = backStackEntry.arguments?.getLong("competitionId") ?: -1
            val competitionsViewModel: CompetitionsViewModelImpl = hiltViewModel(parentEntry)
            CompetitionDetailScreen(competitionsViewModel, competitionId)
        }
        composable(
            route = Screen.CompetitionResults.route,
            arguments = listOf(navArgument("competitionId") { type = NavType.IntType })
        ) {
            val resultsViewModel: ResultsViewModelImpl = hiltViewModel()
            CompetitionResultsScreen(resultsViewModel)
        }
    }
}
