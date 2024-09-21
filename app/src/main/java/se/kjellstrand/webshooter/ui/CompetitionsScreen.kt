package se.kjellstrand.webshooter.ui

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import se.kjellstrand.webshooter.data.competitions.remote.Datum

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = Screen.CompetitionsList.route) {
        composable(Screen.CompetitionsList.route) {
            CompetitionsScreen(navController)
        }
        composable(
            route = Screen.CompetitionDetail.route,
            arguments = listOf(navArgument("competitionId") { type = NavType.LongType })
        ) { backStackEntry ->
            // Get the parent back stack entry for the competitions list
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.CompetitionsList.route)
            }
            val competitionsViewModel: CompetitionsViewModel = hiltViewModel(parentEntry)
            CompetitionDetailScreen(
                competitionId = backStackEntry.arguments?.getLong("competitionId"),
                competitionsViewModel = competitionsViewModel
            )
        }
    }
}

@Composable
fun CompetitionsScreen(
    navController: NavController, competitionsViewModel: CompetitionsViewModel = hiltViewModel()
) {
    val competitionsState by competitionsViewModel.uiState.collectAsState()

    competitionsState.competitions?.let { competitions ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)
        ) {
            items(competitions.data) { competition ->
                CompetitionItem(competition = competition, onItemClick = {
                    // Navigate to the detail screen, passing the competition ID
                    navController.navigate(Screen.CompetitionDetail.createRoute(competition.id))
                })
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    } ?: run {
        // Show loading indicator
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
        .clickable { onItemClick() } // Make the item clickable
        .padding(8.dp)) {
        Text(
            text = competition.name,
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Date: ${competition.date}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text =  competition.status,
            style = MaterialTheme.typography.bodySmall
        )
    }
}