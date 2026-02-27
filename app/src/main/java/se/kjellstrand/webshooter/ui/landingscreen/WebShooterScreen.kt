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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.data.common.CompetitionStatus
import se.kjellstrand.webshooter.ui.club.ClubScreen
import se.kjellstrand.webshooter.ui.competitions.CompetitionsScreen
import se.kjellstrand.webshooter.ui.competitions.CompetitionsViewModelImpl
import se.kjellstrand.webshooter.ui.navigation.Screen
import se.kjellstrand.webshooter.ui.settings.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebShooterScreen(navController: NavController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navigationItems = listOf(
        NavigationItem(stringResource(R.string.web_shooter_competitions), Screen.CompetitionsList.route),
        NavigationItem(stringResource(R.string.my_entries), Screen.MyEntries.route),
        NavigationItem(stringResource(R.string.web_shooter_club), Screen.Club.route),
        NavigationItem(stringResource(R.string.web_shooter_settings), Screen.Settings.route)
    )

    var selectedRoute by remember { mutableStateOf(Screen.CompetitionsList.route) }
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
                            selectedRoute = item.route
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
            when (selectedRoute) {
                Screen.CompetitionsList.route -> {
                    CompetitionsScreen(navController, competitionsViewModel, CompetitionStatus.ALL)
                }
                Screen.MyEntries.route -> {
                    CompetitionsScreen(navController, competitionsViewModel, CompetitionStatus.MY_ENTRIES)
                }
                Screen.Club.route -> ClubScreen()
                Screen.Settings.route -> SettingsScreen(
                    onLoggedOut = {
                        navController.navigate(Screen.LoginScreen.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
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
