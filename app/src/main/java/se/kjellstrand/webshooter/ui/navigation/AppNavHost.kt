package se.kjellstrand.webshooter.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import se.kjellstrand.webshooter.ui.competitions.CompetitionDetailScreen
import se.kjellstrand.webshooter.ui.competitions.CompetitionsScreen
import se.kjellstrand.webshooter.ui.competitions.CompetitionsViewModel
import se.kjellstrand.webshooter.ui.landingscreen.LandingScreen
import se.kjellstrand.webshooter.ui.login.LoginScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = Screen.LandingScreen.route) {
        composable(Screen.LoginScreen.route) {
            LoginScreen(navController)
        }
        composable(Screen.LandingScreen.route) {
            LandingScreen(navController)
        }
        composable(Screen.CompetitionsList.route) {

            // java.lang.IllegalArgumentException: No destination with route competitions_list is on the NavController's back stack.
            // The current destination is Destination(0x775862cd) route=competition_detail/{competitionId}

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
            CompetitionDetailScreen(competitionsState, competitionId)
        }
    }
}