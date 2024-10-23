package se.kjellstrand.webshooter.ui.competitions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import se.kjellstrand.webshooter.data.competitions.remote.Datum
import se.kjellstrand.webshooter.ui.common.WeaponGroupBadges
import se.kjellstrand.webshooter.ui.mock.CompetitionsViewModelMock
import se.kjellstrand.webshooter.ui.navigation.Screen

@Composable
fun CompetitionsScreen(
    navController: NavController, competitionsViewModel: CompetitionsViewModel
) {
    val competitionsState by competitionsViewModel.uiState.collectAsState()
    competitionsState.competitions?.let { competitions ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(
                top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp
            )
        ) {
            items(competitions.data) { competition ->
                CompetitionItem(competition = competition,
                    onDetailsClick = {
                        navController.navigate(Screen.CompetitionDetail.createRoute(competition.id))
                    },
                    onResultsClick = {
                        navController.navigate(Screen.CompetitionResults.createRoute(competition.id))
                    })
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
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
    competition: Datum,
    onDetailsClick: () -> Unit,
    onResultsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Main content on the left
        Column(
            modifier = Modifier.weight(1f)
        ) {
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
                text = "Status: ${competition.statusHuman}",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Tävlingstyp: ${competition.competitionType.name}",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(6.dp))
            WeaponGroupBadges(
                weaponGroups = competition.weaponGroups,
                userSignups = competition.userSignups
            )
        }

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Button(onClick = { onDetailsClick() }) {
                Text("Detaljer")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(enabled = competition.status == "completed",
                onClick = { onResultsClick() }) {
                Text("Resultat")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CompetitionsScreenPreview() {
    val navController = rememberNavController()
    CompetitionsScreen(
        navController = navController, competitionsViewModel = CompetitionsViewModelMock()
    )
}