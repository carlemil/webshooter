package se.kjellstrand.webshooter.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import se.kjellstrand.webshooter.data.competitions.remote.Datum

@Composable
fun CompetitionDetailScreen(
    competitionId: Long?,
    competitionsViewModel: CompetitionsViewModel = hiltViewModel()
) {
    // Handle the case where competitionId might be null
    if (competitionId == null) {
        Text("Competition not found, competitionId null.")
        return
    }

    // Fetch the competition from the ViewModel or repository
    val competition = competitionsViewModel.getCompetitionById(competitionId)

    if (competition != null) {
        // Display the competition details
        CompetitionDetail(competition)
    } else {
        // Show a loading indicator or error message
        Text("Competition not found, competitionId: $competitionId")
    }
}

@Composable
fun CompetitionDetail(competition: Datum) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = competition.name,
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Contact Name: ${competition.contactName}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Date: ${competition.date}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Description:",
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = competition.description,
            style = MaterialTheme.typography.bodySmall
        )
        // Add more details as needed
    }
}