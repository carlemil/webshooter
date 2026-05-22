package se.kjellstrand.webshooter.ui.landingscreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import org.jetbrains.compose.resources.stringResource
import io.ktor.http.encodeURLParameter
import org.koin.compose.koinInject
import se.kjellstrand.webshooter.ui.platform.UrlLauncher
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.ui.screens.charts.resulttrends.ChartsScreen
import se.kjellstrand.webshooter.ui.screens.charts.resulttrends.ResultsTrendsViewModelImpl
import se.kjellstrand.webshooter.ui.screens.charts.seriespoints.SeriesPointsScreen
import se.kjellstrand.webshooter.ui.screens.charts.seriespoints.SeriesPointsViewModelImpl
import se.kjellstrand.webshooter.ui.screens.club.ClubScreen
import se.kjellstrand.webshooter.ui.screens.charts.clubstats.ClubStatsScreen
import se.kjellstrand.webshooter.ui.screens.charts.clubstats.ClubStatsViewModelImpl
import se.kjellstrand.webshooter.ui.screens.competitions.CompetitionsScreen
import se.kjellstrand.webshooter.ui.screens.competitions.CompetitionsViewModelImpl
import se.kjellstrand.webshooter.ui.screens.markera.MarkeraScreen
import se.kjellstrand.webshooter.ui.screens.myresults.MyEntriesScreen
import se.kjellstrand.webshooter.ui.navigation.Screen
import se.kjellstrand.webshooter.ui.screens.licenses.LicensesScreen
import se.kjellstrand.webshooter.ui.screens.settings.SettingsScreen
import se.kjellstrand.webshooter.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebShooterScreen(
    onLogout: () -> Unit,
    onNavigateToResults: (competitionId: Long, resultsType: String, name: String, date: String) -> Unit,
    onNavigateToSignup: (competitionId: Long) -> Unit,
    onNavigateToSignupsList: (competitionId: Long) -> Unit,
    onNavigateToPatrols: (competitionId: Long, competitionTypeId: Int) -> Unit,
    onNavigateToTeams: (competitionId: Long) -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val competitionsItems = listOf(
        NavigationItem(stringResource(Res.string.web_shooter_competitions), Screen.CompetitionsList.route),
        NavigationItem(stringResource(Res.string.my_results), Screen.MyEntries.route)
    )
    val statsItems = listOf(
        NavigationItem(stringResource(Res.string.web_shooter_charts), Screen.Charts.route),
        NavigationItem(stringResource(Res.string.web_shooter_series_points), Screen.SeriesPoints.route),
        NavigationItem(stringResource(Res.string.web_shooter_club_stats), Screen.ClubStats.route)
    )
    val clubItems = listOf(
        NavigationItem(stringResource(Res.string.web_shooter_club), Screen.Club.route)
    )
    val settingsItems = listOf(
        NavigationItem(stringResource(Res.string.web_shooter_settings), Screen.Settings.route),
        NavigationItem(stringResource(Res.string.web_shooter_licenses), Screen.Licenses.route)
    )
    val otherItems = listOf(
        NavigationItem(stringResource(Res.string.markera), Screen.Markera.route)
    )
    val navigationItems = competitionsItems + statsItems + clubItems + settingsItems + otherItems

    val drawerNavController = rememberNavController()
    val navBackStackEntry by drawerNavController.currentBackStackEntryAsState()
    val selectedRoute = navBackStackEntry?.destination?.route ?: Screen.CompetitionsList.route

    BackHandler(selectedRoute != Screen.CompetitionsList.route) {
        drawerNavController.popBackStack(Screen.CompetitionsList.route, inclusive = false)
    }

    val competitionsViewModel: CompetitionsViewModelImpl = koinViewModel()

    val urlLauncher: UrlLauncher = koinInject()
    val suggestionLabel = stringResource(Res.string.web_shooter_send_suggestion)
    val suggestionSubject = stringResource(Res.string.send_suggestion_email_subject)
    val suggestionRecipient = stringResource(Res.string.send_suggestion_email_recipient)
    val sendSuggestionEmail: () -> Unit = {
        urlLauncher.openUrl("mailto:$suggestionRecipient?subject=${suggestionSubject.encodeURLParameter()}")
    }

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(Res.string.web_shooter_menu),
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

                SectionHeader(stringResource(Res.string.menu_group_competitions))
                competitionsItems.forEach { MenuItem(it) }

                HorizontalDivider()
                SectionHeader(stringResource(Res.string.menu_group_stats))
                statsItems.forEach { MenuItem(it) }

                HorizontalDivider()
                SectionHeader(stringResource(Res.string.menu_group_club))
                clubItems.forEach { MenuItem(it) }

                HorizontalDivider()
                SectionHeader(stringResource(Res.string.menu_group_settings))
                settingsItems.forEach { MenuItem(it) }

                HorizontalDivider()
                SectionHeader(stringResource(Res.string.menu_group_other))
                otherItems.forEach { MenuItem(it) }
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
            }
        },
        drawerState = drawerState
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            val title = navigationItems.find { it.route == selectedRoute }?.label ?: stringResource(Res.string.app_name)
            ScreenTopBar(
                title = title,
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(Res.string.web_shooter_menu_content_description)
                        )
                    }
                }
            )
            NavHost(
                navController = drawerNavController,
                startDestination = Screen.CompetitionsList.route
            ) {
                composable(Screen.CompetitionsList.route) {
                    CompetitionsScreen(
                        onNavigateToResults = onNavigateToResults,
                        onNavigateToSignup = onNavigateToSignup,
                        onNavigateToSignupsList = onNavigateToSignupsList,
                        onNavigateToPatrols = onNavigateToPatrols,
                        onNavigateToTeams = onNavigateToTeams,
                        competitionsViewModel = competitionsViewModel,
                    )
                }
                composable(Screen.Markera.route) {
                    MarkeraScreen()
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
                    SettingsScreen(onLoggedOut = onLogout)
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
    WebShooterScreen(
        onLogout = {},
        onNavigateToResults = { _, _, _, _ -> },
        onNavigateToSignup = {},
        onNavigateToSignupsList = {},
        onNavigateToPatrols = { _, _ -> },
        onNavigateToTeams = {},
    )
}
