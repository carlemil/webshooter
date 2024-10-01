package se.kjellstrand.webshooter.ui.competitions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import se.kjellstrand.webshooter.data.competitions.remote.Datum
import se.kjellstrand.webshooter.ui.common.WeaponGroupBadges
import se.kjellstrand.webshooter.ui.mock.CompetitionsViewModelMock
import se.kjellstrand.webshooter.ui.mock.MockCompetitions
import se.kjellstrand.webshooter.ui.navigation.Screen

@Composable
fun CompetitionDetailScreen(
    navController: NavController,
    competitionsViewModel: CompetitionsViewModel,
    competitionId: Long
) {
    val competitionsState by competitionsViewModel.uiState.collectAsState()
    val competition = competitionsState.competitions?.data?.find { it.id == competitionId }
    if (competition != null) {
        // Display the competition details
        CompetitionDetail(competition, onItemClick = {
            navController.navigate(Screen.CompetitionResults.createRoute(competition.id))
        })
    } else {
        // Show a loading indicator or error message
        Text("Competition not found.")
    }
}

@Composable
fun CompetitionDetail(competition: Datum, onItemClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .padding(
                PaddingValues(
                    top = 16.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp
                )
            )
    ) {
        Text(
            text = competition.name,
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Contact Name: ${competition.contactName}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(2.dp))
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
            text = "Tävlingstyp: ${competition.competitionType.name}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Resultatberäkning: ${competition.resultsTypeHuman}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Description:",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = competition.description,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
        )
        Spacer(modifier = Modifier.height(6.dp))
        WeaponGroupBadges(
            weaponGroups = competition.weaponGroups,
            userSignups = competition.userSignups
        )
        // Add more details as needed
    }
}

@Preview(showBackground = true)
@Composable
fun CompetitionDetailScreenPreview() {
    val navController = rememberNavController()

    CompetitionDetailScreen(
        navController,
        competitionsViewModel = CompetitionsViewModelMock(),
        1
    )
}