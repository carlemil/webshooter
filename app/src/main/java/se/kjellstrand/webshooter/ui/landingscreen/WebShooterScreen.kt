package se.kjellstrand.webshooter.ui.landingscreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import se.kjellstrand.webshooter.ui.common.ScreenTopBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.ui.screens.charts.ChartsScreen
import se.kjellstrand.webshooter.ui.screens.charts.ChartsViewModelImpl
import se.kjellstrand.webshooter.ui.screens.seriespoints.SeriesPointsScreen
import se.kjellstrand.webshooter.ui.screens.seriespoints.SeriesPointsViewModelImpl
import se.kjellstrand.webshooter.ui.screens.club.ClubScreen
import se.kjellstrand.webshooter.ui.screens.clubstats.ClubStatsScreen
import se.kjellstrand.webshooter.ui.screens.clubstats.ClubStatsViewModelImpl
import se.kjellstrand.webshooter.ui.screens.competitions.CompetitionsScreen
import se.kjellstrand.webshooter.ui.screens.competitions.CompetitionsViewModelImpl
import se.kjellstrand.webshooter.ui.screens.myresults.MyEntriesScreen
import se.kjellstrand.webshooter.ui.navigation.Screen
import se.kjellstrand.webshooter.ui.screens.licenses.LicensesScreen
import se.kjellstrand.webshooter.ui.screens.settings.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebShooterScreen(navController: NavController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navigationItems = listOf(
        NavigationItem(stringResource(R.string.web_shooter_competitions), Screen.CompetitionsList.route),
        NavigationItem(stringResource(R.string.my_results), Screen.MyEntries.route),
        NavigationItem(stringResource(R.string.web_shooter_charts), Screen.Charts.route),
        NavigationItem(stringResource(R.string.web_shooter_series_points), Screen.SeriesPoints.route),
        NavigationItem(stringResource(R.string.web_shooter_club_stats), Screen.ClubStats.route),
        NavigationItem(stringResource(R.string.web_shooter_club), Screen.Club.route),
        NavigationItem(stringResource(R.string.web_shooter_settings), Screen.Settings.route),
        NavigationItem(stringResource(R.string.web_shooter_licenses), Screen.Licenses.route)
    )

    val drawerNavController = rememberNavController()
    val navBackStackEntry by drawerNavController.currentBackStackEntryAsState()
    val selectedRoute = navBackStackEntry?.destination?.route ?: Screen.CompetitionsList.route

    BackHandler(selectedRoute != Screen.CompetitionsList.route) {
        drawerNavController.popBackStack(Screen.CompetitionsList.route, inclusive = false)
    }

    val competitionsViewModel: CompetitionsViewModelImpl = hiltViewModel()

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.web_shooter_menu),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )
                HorizontalDivider()
                navigationItems.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.label) },
                        selected = item.route == selectedRoute,
                        onClick = {
                            drawerNavController.navigate(item.route) {
                                popUpTo(Screen.CompetitionsList.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                            scope.launch {
                                drawerState.close()
                            }
                        },
                        shape = RectangleShape,
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        },
        drawerState = drawerState
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            val title = navigationItems.find { it.route == selectedRoute }?.label ?: stringResource(R.string.app_name)
            ScreenTopBar(
                title = title,
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(R.string.web_shooter_menu_content_description)
                        )
                    }
                }
            )
            NavHost(
                navController = drawerNavController,
                startDestination = Screen.CompetitionsList.route
            ) {
                composable(Screen.CompetitionsList.route) {
                    CompetitionsScreen(navController, competitionsViewModel)
                }
                composable(Screen.MyEntries.route) {
                    MyEntriesScreen()
                }
                composable(Screen.Charts.route) {
                    ChartsScreen(hiltViewModel<ChartsViewModelImpl>())
                }
                composable(Screen.SeriesPoints.route) {
                    SeriesPointsScreen(hiltViewModel<SeriesPointsViewModelImpl>())
                }
                composable(Screen.ClubStats.route) {
                    ClubStatsScreen(hiltViewModel<ClubStatsViewModelImpl>())
                }
                composable(Screen.Club.route) {
                    ClubScreen()
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        onLoggedOut = {
                            navController.navigate(Screen.LoginScreen.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Screen.Licenses.route) {
                    LicensesScreen()
                }
            }
        }
    }
}

data class NavigationItem(val label: String, val route: String)

@Preview(showBackground = true)
@Composable
fun WebShooterScreenPreview() {
    val navController = rememberNavController()

    WebShooterScreen(
        navController = navController
    )
}
