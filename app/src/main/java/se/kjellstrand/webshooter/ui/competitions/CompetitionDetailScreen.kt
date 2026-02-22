package se.kjellstrand.webshooter.ui.competitions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun CompetitionDetail(competition: Datum, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = competition.name,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                DetailRow(
                    label = stringResource(R.string.contact_name, ""),
                    value = competition.contactName
                )
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow(
                    label = stringResource(R.string.date, ""),
                    value = competition.date
                )
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow(
                    label = stringResource(R.string.status, ""),
                    value = competition.statusHuman
                )
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow(
                    label = stringResource(R.string.open_for_team_signup, ""),
                    value = competition.signupsOpeningDate
                )
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow(
                    label = stringResource(R.string.last_signup_date, ""),
                    value = competition.signupsClosingDate
                )
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow(
                    label = stringResource(R.string.late_signup, ""),
                    value = competition.allowSignupsAfterClosingDateHuman
                )
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow(
                    label = stringResource(R.string.team_signup, ""),
                    value = if (competition.allowTeams == 1L) stringResource(R.string.yes) else stringResource(
                        R.string.no
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow(
                    label = stringResource(R.string.competition_type, ""),
                    value = competition.competitionType.name
                )
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow(
                    label = stringResource(R.string.result_calculation, ""),
                    value = competition.resultsTypeHuman
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = stringResource(R.string.description),
                    style = MaterialTheme.typography.bodyMedium

                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = competition.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        WeaponClassBadges(
            weaponClasses = competition.weaponClasses,
            userSignups = competition.userSignups
        )
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
