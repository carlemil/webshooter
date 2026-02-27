package se.kjellstrand.webshooter.ui.signups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.data.signups.remote.SignupEntry

@Composable
fun MyEntriesScreen(
    viewModel: SignupsViewModel = hiltViewModel<SignupsViewModelImpl>()
) {
    val uiState by viewModel.uiState.collectAsState()

    when {
        uiState.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        uiState.groupedEntries.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.signups_no_entries))
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                uiState.groupedEntries.forEach { (year, entries) ->
                    item(key = "header_$year") {
                        Text(
                            text = year,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    val byCompetition = entries.groupBy { it.competition.id }
                        .values.toList()
                    items(byCompetition.size, key = { byCompetition[it].first().competition.id }) { index ->
                        CompetitionSignupsItem(byCompetition[index])
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun CompetitionSignupsItem(entries: List<SignupEntry>) {
    val competition = entries.first().competition
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = competition.name,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "${competition.date}  •  ${competition.statusHuman}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        entries.forEach { entry ->
            Spacer(modifier = Modifier.height(6.dp))
            SignupRow(entry)
        }
    }
}

@Composable
private fun SignupRow(entry: SignupEntry) {
    Column(modifier = Modifier.padding(start = 12.dp)) {
        Text(
            text = stringResource(R.string.signups_weapon_class, entry.weaponclass.classname),
            style = MaterialTheme.typography.bodySmall
        )
        val startTime = entry.patrol?.startTimeHuman?.takeIf { it.isNotBlank() }
            ?: entry.startTimeHuman.takeIf { it.isNotBlank() && it != "01:00" }
        if (startTime != null) {
            Text(
                text = stringResource(R.string.signups_start_time, startTime),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (entry.lane > 0) {
            Text(
                text = stringResource(R.string.signups_lane, entry.lane),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (entry.team.isNotEmpty()) {
            Text(
                text = stringResource(R.string.signups_team, entry.team.first().name),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        entry.resultsPlacements?.let { rp ->
            val medal = rp.stdMedal?.let { " · ${stringResource(R.string.signups_medal, it)}" } ?: ""
            Text(
                text = stringResource(R.string.signups_placement, rp.placement) + medal,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = stringResource(R.string.signups_registration_fee, entry.registrationFee),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (!entry.note.isNullOrBlank()) {
            Text(
                text = stringResource(R.string.signups_note, entry.note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
