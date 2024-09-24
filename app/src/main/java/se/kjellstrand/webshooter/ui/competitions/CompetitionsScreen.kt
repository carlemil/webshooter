package se.kjellstrand.webshooter.ui.competitions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import se.kjellstrand.webshooter.data.competitions.remote.Datum
import se.kjellstrand.webshooter.ui.Screen
import se.kjellstrand.webshooter.ui.SharedComposables.Common.Companion.getStatusBarAndHeight
import se.kjellstrand.webshooter.ui.SharedComposables.WeaponGroupBadges

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = Screen.CompetitionsList.route) {
        composable(Screen.CompetitionsList.route) {
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

@Composable
fun CompetitionsScreen(
    navController: NavController, competitionsState: CompetitionsUiState
) {
    competitionsState.competitions?.let { competitions ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(
                top = getStatusBarAndHeight() + 16.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp
            )
        ) {
            items(competitions.data) { competition ->
                CompetitionItem(competition = competition, onItemClick = {
                    navController.navigate(Screen.CompetitionDetail.createRoute(competition.id))
                })
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    } ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun CompetitionItem(
    competition: Datum, onItemClick: () -> Unit
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable { onItemClick() }
        .padding(8.dp)) {
        Text(
            text = competition.name,
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Datum: ${competition.date}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Öppnas för anmälan: ${competition.signupsOpeningDate}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Sista anmälningsdag: ${competition.signupsClosingDate}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Efteranmälan: ${competition.allowSignupsAfterClosingDateHuman}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Lagtävling: ${if (competition.allowTeams == 1L) "Ja" else "Nej"}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Tävlingstyp: ${competition.competitiontype.name}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Resultatberäkning: ${competition.resultsTypeHuman}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(6.dp))
        WeaponGroupBadges(
            weaponGroups = competition.weapongroups,
            userSignups = competition.usersignups
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CompetitionsScreenPreview() {
    val navController = rememberNavController()

    CompetitionsScreen(
        navController = navController,
        competitionsState = MockCompetitionsUiState().uiState
    )
}