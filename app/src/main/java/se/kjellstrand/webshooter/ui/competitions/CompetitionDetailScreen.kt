package se.kjellstrand.webshooter.ui.competitions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import se.kjellstrand.webshooter.data.competitions.remote.Datum
import se.kjellstrand.webshooter.ui.SharedComposables.WeaponGroupBadges

@Composable
fun CompetitionDetailScreen(
    competition: Datum?
) {
    if (competition != null) {
        // Display the competition details
        CompetitionDetail(competition)
    } else {
        // Show a loading indicator or error message
        Text("Competition not found.")
    }
}

@Composable
fun CompetitionDetail(competition: Datum) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val displayCutoutPadding = WindowInsets.displayCutout.asPaddingValues().calculateTopPadding()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                PaddingValues(
                    top = statusBarPadding + displayCutoutPadding + 16.dp,
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
            weaponGroups = competition.weapongroups,
            userSignups = competition.usersignups
        )
        // Add more details as needed
    }
}

@Preview(showBackground = true)
@Composable
fun CompetitionDetailScreenPreview() {
    CompetitionDetailScreen(
        competition = MockCompetitionsUiState().uiState.competitions!!.data[0]
    )
}