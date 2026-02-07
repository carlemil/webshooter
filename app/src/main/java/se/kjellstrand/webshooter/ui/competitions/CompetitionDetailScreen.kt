package se.kjellstrand.webshooter.ui.competitions

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.data.competitions.remote.Datum
import se.kjellstrand.webshooter.ui.common.WeaponClassBadges
import se.kjellstrand.webshooter.ui.mock.CompetitionsViewModelMock

@Composable
fun CompetitionDetailScreen(
    competitionsViewModel: CompetitionsViewModel,
    competitionId: Long
) {
    val competitionsState by competitionsViewModel.uiState.collectAsState()
    val competition = competitionsState.competitions?.data?.find { it.id == competitionId }
    if (competition != null) {
        CompetitionDetail(competition)
    } else {
        Text(stringResource(R.string.competition_not_found))
    }
}

@Composable
fun CompetitionDetail(competition: Datum) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
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
            text = stringResource(R.string.contact_name, competition.contactName),
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.date, competition.date),
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.status, competition.statusHuman),
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.open_for_team_signup, competition.signupsOpeningDate),
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.last_signup_date, competition.signupsClosingDate),
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(
                R.string.late_signup,
                competition.allowSignupsAfterClosingDateHuman
            ),
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(
                R.string.team_signup,
                if (competition.allowTeams == 1L) "Ja" else "Nej"
            ),
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.competition_type, competition.competitionType.name),
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.result_calculation, competition.resultsTypeHuman),
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.description),
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = competition.description,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
        )
        Spacer(modifier = Modifier.height(6.dp))
        WeaponClassBadges(
            weaponClasses = competition.weaponClasses,
            userSignups = competition.userSignups
        )
        // Add more details as needed
    }
}

@Preview(showBackground = true)
@Composable
fun CompetitionDetailScreenPreview() {

    CompetitionDetailScreen(
        competitionsViewModel = CompetitionsViewModelMock(),
        1
    )
}