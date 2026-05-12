package se.kjellstrand.webshooter.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import se.kjellstrand.webshooter.data.SessionManager
import se.kjellstrand.webshooter.data.competitions.remote.ResultsType
import se.kjellstrand.webshooter.ui.screens.competitions.CompetitionsScreen
import se.kjellstrand.webshooter.ui.screens.splash.SplashScreen
import se.kjellstrand.webshooter.ui.screens.competitions.CompetitionsViewModelImpl
import se.kjellstrand.webshooter.ui.landingscreen.WebShooterScreen
import se.kjellstrand.webshooter.ui.screens.login.LoginScreen
import se.kjellstrand.webshooter.ui.screens.results.CompetitionResultsScreen
import se.kjellstrand.webshooter.ui.screens.results.ResultsViewModelImpl
import se.kjellstrand.webshooter.ui.screens.shooterresult.ShooterResultScreen
import se.kjellstrand.webshooter.ui.screens.shooterresult.ShooterResultViewModelImpl
import se.kjellstrand.webshooter.ui.screens.patrols.CompetitionPatrolsScreen
import se.kjellstrand.webshooter.ui.screens.patrols.PatrolsViewModelImpl
import se.kjellstrand.webshooter.ui.screens.signups.CompetitionSignupsScreen
import se.kjellstrand.webshooter.ui.screens.signups.SignupsViewModelImpl
import se.kjellstrand.webshooter.ui.screens.signup.SignupScreen
import se.kjellstrand.webshooter.ui.screens.signup.SignupViewModelImpl
import se.kjellstrand.webshooter.ui.screens.teams.CompetitionTeamsScreen
import se.kjellstrand.webshooter.ui.screens.teams.TeamsViewModelImpl

@Composable
fun AppNavHost(navController: NavHostController) {
    val sessionViewModel: SessionViewModel = koinViewModel()
    val toastContext = LocalContext.current
    val showMessage: (String) -> Unit = { message ->
        Toast.makeText(toastContext, message, Toast.LENGTH_LONG).show()
    }

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

    val navigateToResults: (Long, String, String, String) -> Unit =
        { competitionId, resultsType, name, date ->
            navController.safeNavigate(
                Screen.CompetitionResults.createRoute(competitionId, resultsType, name, date)
            )
        }
    val navigateToSignup: (Long) -> Unit = { competitionId ->
        navController.safeNavigate(Screen.CompetitionSignup.createRoute(competitionId))
    }
    val navigateToSignupsList: (Long) -> Unit = { competitionId ->
        navController.safeNavigate(Screen.CompetitionSignupsList.createRoute(competitionId))
    }
    val navigateToPatrols: (Long, Int) -> Unit = { competitionId, competitionTypeId ->
        navController.safeNavigate(
            Screen.CompetitionPatrols.createRoute(competitionId, competitionTypeId)
        )
    }
    val navigateToTeams: (Long) -> Unit = { competitionId ->
        navController.safeNavigate(Screen.CompetitionTeams.createRoute(competitionId))
    }

    NavHost(navController, startDestination = Screen.SplashScreen.route) {
        composable(Screen.SplashScreen.route) {
            SplashScreen(
                onNavigateToLanding = {
                    navController.navigate(Screen.LandingScreen.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
        composable(Screen.LoginScreen.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.LandingScreen.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
        composable(Screen.LandingScreen.route) {
            WebShooterScreen(
                onLogout = {
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToResults = navigateToResults,
                onNavigateToSignup = navigateToSignup,
                onNavigateToSignupsList = navigateToSignupsList,
                onNavigateToPatrols = navigateToPatrols,
                onNavigateToTeams = navigateToTeams,
            )
        }
        composable(Screen.CompetitionsList.route) {
            val competitionsViewModel: CompetitionsViewModelImpl = koinViewModel()
            CompetitionsScreen(
                onNavigateToResults = navigateToResults,
                onNavigateToSignup = navigateToSignup,
                onNavigateToSignupsList = navigateToSignupsList,
                onNavigateToPatrols = navigateToPatrols,
                onNavigateToTeams = navigateToTeams,
                competitionsViewModel = competitionsViewModel,
            )
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
        ) { backStackEntry ->
            val competitionId = NavigationArguments.requireLong(backStackEntry.arguments, "competitionId")
            val resultsType = runCatching {
                ResultsType.valueOf(NavigationArguments.requireString(backStackEntry.arguments, "resultsType"))
            }.getOrDefault(ResultsType.FIELD)
            val competitionName = backStackEntry.arguments?.getString("competitionName") ?: ""
            val competitionDate = backStackEntry.arguments?.getString("competitionDate") ?: ""
            val resultsViewModel: ResultsViewModelImpl = koinViewModel {
                parametersOf(competitionId, competitionDate, resultsType, competitionName)
            }
            CompetitionResultsScreen(
                resultsViewModel = resultsViewModel,
                onBack = { navController.safePopBackStack() },
                onNavigateToShooter = { shooterId ->
                    navController.safeNavigate(
                        Screen.ShooterResult.createRoute(competitionId, shooterId, resultsType.name)
                    )
                },
                showMessage = showMessage,
            )
        }
        composable(
            route = Screen.ShooterResult.route,
            arguments = listOf(
                navArgument("competitionId") { type = NavType.LongType },
                navArgument("shooterId") { type = NavType.LongType },
                navArgument("resultsType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val competitionId = NavigationArguments.requireLong(backStackEntry.arguments, "competitionId")
            val shooterId = NavigationArguments.requireLong(backStackEntry.arguments, "shooterId")
            val resultsType = runCatching {
                ResultsType.valueOf(NavigationArguments.requireString(backStackEntry.arguments, "resultsType"))
            }.getOrDefault(ResultsType.FIELD)
            val shooterResultViewModel: ShooterResultViewModelImpl = koinViewModel {
                parametersOf(competitionId, shooterId, resultsType)
            }
            ShooterResultScreen(onBack = { navController.safePopBackStack() }, viewModel = shooterResultViewModel)
        }
        composable(
            route = Screen.CompetitionSignupsList.route,
            arguments = listOf(navArgument("competitionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val competitionId = NavigationArguments.requireLong(backStackEntry.arguments, "competitionId")
            val signupsListViewModel: SignupsViewModelImpl = koinViewModel {
                parametersOf(competitionId)
            }
            CompetitionSignupsScreen(onBack = { navController.safePopBackStack() }, viewModel = signupsListViewModel)
        }
        composable(
            route = Screen.CompetitionPatrols.route,
            arguments = listOf(
                navArgument("competitionId") { type = NavType.LongType },
                navArgument("competitionTypeId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val competitionId = NavigationArguments.requireLong(backStackEntry.arguments, "competitionId")
            val competitionTypeId = NavigationArguments.requireInt(backStackEntry.arguments, "competitionTypeId")
            val patrolsViewModel: PatrolsViewModelImpl = koinViewModel {
                parametersOf(competitionId, competitionTypeId)
            }
            CompetitionPatrolsScreen(onBack = { navController.safePopBackStack() }, viewModel = patrolsViewModel)
        }
        composable(
            route = Screen.CompetitionTeams.route,
            arguments = listOf(navArgument("competitionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val competitionId = NavigationArguments.requireLong(backStackEntry.arguments, "competitionId")
            val teamsViewModel: TeamsViewModelImpl = koinViewModel {
                parametersOf(competitionId)
            }
            CompetitionTeamsScreen(onBack = { navController.safePopBackStack() }, viewModel = teamsViewModel)
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
            val competitionsViewModel: CompetitionsViewModelImpl =
                koinViewModel(viewModelStoreOwner = parentEntry)
            val signupViewModel: SignupViewModelImpl = koinViewModel {
                parametersOf(competitionId)
            }
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
                SignupScreen(competition, signupViewModel, onBack = { navController.safePopBackStack() })
            }
        }
    }
}
