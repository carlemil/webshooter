package se.kjellstrand.webshooter.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import se.kjellstrand.webshooter.data.competitions.remote.Datum

@Composable
fun CompetitionsScreen(
    competitionsViewModel: CompetitionsViewModel = hiltViewModel()
) {

    // Assume that the ViewModel has a 'competitions' state holding the Competitions object
    val competitionsState by competitionsViewModel.competitions.collectAsState()

    // Display the list if data is available, otherwise show a loading indicator
    val competitions = competitionsState.competitions

    LazyColumn(
        modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)
    ) {
        if (competitions != null) {
            items(competitions.data) { competition ->
                CompetitionItem(competition = competition)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
fun CompetitionItem(competition: Datum) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = competition.name, style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Date: ${competition.date}", style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Status: ${competition.status}", style = MaterialTheme.typography.bodySmall
        )
    }
}