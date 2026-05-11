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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.ui.screens.charts.resulttrends.ChartsScreen
import se.kjellstrand.webshooter.ui.screens.charts.resulttrends.ResultsTrendsViewModelImpl
import se.kjellstrand.webshooter.ui.screens.charts.seriespoints.SeriesPointsScreen
import se.kjellstrand.webshooter.ui.screens.charts.seriespoints.SeriesPointsViewModelImpl
import se.kjellstrand.webshooter.ui.screens.club.ClubScreen
import se.kjellstrand.webshooter.ui.screens.charts.clubstats.ClubStatsScreen
import se.kjellstrand.webshooter.ui.screens.charts.clubstats.ClubStatsViewModelImpl
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

    val competitionsItems = listOf(
        NavigationItem(stringResource(R.string.web_shooter_competitions), Screen.CompetitionsList.route),
        NavigationItem(stringResource(R.string.my_results), Screen.MyEntries.route)
    )
    val statsItems = listOf(
        NavigationItem(stringResource(R.string.web_shooter_charts), Screen.Charts.route),
        NavigationItem(stringResource(R.string.web_shooter_series_points), Screen.SeriesPoints.route),
        NavigationItem(stringResource(R.string.web_shooter_club_stats), Screen.ClubStats.route)
    )
    val clubItems = listOf(
        NavigationItem(stringResource(R.string.web_shooter_club), Screen.Club.route)
    )
    val settingsItems = listOf(
        NavigationItem(stringResource(R.string.web_shooter_settings), Screen.Settings.route),
        NavigationItem(stringResource(R.string.web_shooter_licenses), Screen.Licenses.route)
    )
    val navigationItems = competitionsItems + statsItems + clubItems + settingsItems

    val drawerNavController = rememberNavController()
    val navBackStackEntry by drawerNavController.currentBackStackEntryAsState()
    val selectedRoute = navBackStackEntry?.destination?.route ?: Screen.CompetitionsList.route

    BackHandler(selectedRoute != Screen.CompetitionsList.route) {
        drawerNavController.popBackStack(Screen.CompetitionsList.route, inclusive = false)
    }

    val competitionsViewModel: CompetitionsViewModelImpl = koinViewModel()

    val context = LocalContext.current
    val suggestionLabel = stringResource(R.string.web_shooter_send_suggestion)
    val suggestionSubject = stringResource(R.string.send_suggestion_email_subject)
    val suggestionRecipient = stringResource(R.string.send_suggestion_email_recipient)
    val sendSuggestionEmail: () -> Unit = {
        val intent = Intent(Intent.ACTION_SENDTO, "mailto:$suggestionRecipient".toUri()).apply {
            putExtra(Intent.EXTRA_SUBJECT, suggestionSubject)
        }
        context.startActivity(intent)
    }

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

                val onItemClick: (String) -> Unit = { route ->
                    drawerNavController.navigate(route) {
                        popUpTo(Screen.CompetitionsList.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                    scope.launch { drawerState.close() }
                }

                @Composable
                fun SectionHeader(text: String) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
                    )
                }

                @Composable
                fun MenuItem(item: NavigationItem) {
                    NavigationDrawerItem(
                        label = { Text(item.label) },
                        selected = item.route == selectedRoute,
                        onClick = { onItemClick(item.route) },
                        shape = RectangleShape,
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }

                SectionHeader(stringResource(R.string.menu_group_competitions))
                competitionsItems.forEach { MenuItem(it) }

                HorizontalDivider()
                SectionHeader(stringResource(R.string.menu_group_stats))
                statsItems.forEach { MenuItem(it) }

                HorizontalDivider()
                SectionHeader(stringResource(R.string.menu_group_club))
                clubItems.forEach { MenuItem(it) }

                HorizontalDivider()
                SectionHeader(stringResource(R.string.menu_group_settings))
                settingsItems.forEach { MenuItem(it) }
                NavigationDrawerItem(
                    label = { Text(suggestionLabel) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        sendSuggestionEmail()
                    },
                    shape = RectangleShape,
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
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
                    ChartsScreen(koinViewModel<ResultsTrendsViewModelImpl>())
                }
                composable(Screen.SeriesPoints.route) {
                    SeriesPointsScreen(koinViewModel<SeriesPointsViewModelImpl>())
                }
                composable(Screen.ClubStats.route) {
                    ClubStatsScreen(koinViewModel<ClubStatsViewModelImpl>())
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
