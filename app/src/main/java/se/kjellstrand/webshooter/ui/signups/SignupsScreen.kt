package se.kjellstrand.webshooter.ui.signups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
                contentPadding = PaddingValues(
                    top = 8.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                uiState.groupedEntries.forEach { (year, entries) ->
                    item(key = "header_$year") {
                        Text(
                            text = year,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    val byCompetition = entries.groupBy { it.competition.id }
                        .values.toList()
                    items(
                        byCompetition.size,
                        key = { byCompetition[it].first().competition.id }) { index ->
                        CompetitionSignupsItem(byCompetition[index])
                        HorizontalDivider(thickness = 2.dp)
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
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = competition.name, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "${competition.date}  •  ${competition.statusHuman}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Header row
        GridRow {
            GridCell(stringResource(R.string.signups_col_class), 1.5f, fontWeight = FontWeight.Bold)
            GridCell(
                stringResource(R.string.signups_col_start),
                1.5f,
                fontWeight = FontWeight.Bold
            )
            GridCell(
                stringResource(R.string.signups_col_lane),
                1f,
                fontWeight = FontWeight.Bold
            )
            GridCell(
                stringResource(R.string.signups_col_team),
                2f,
                fontWeight = FontWeight.Bold
            )
            GridCell(
                stringResource(R.string.placement),
                1.0f,
                fontWeight = FontWeight.Bold
            )
            GridCell(stringResource(R.string.signups_col_fee), 1.5f, fontWeight = FontWeight.Bold)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

        // Value rows
        entries.sortedBy { it.startTimeHuman }.forEach { entry ->
            val startTime = entry.patrol?.startTimeHuman?.takeIf(String::isNotBlank)
                ?: entry.startTimeHuman.takeIf { it.isNotBlank() && it != "01:00" }
            val placement = entry.resultsPlacements?.let { rp ->
                "${rp.placement}" + (rp.stdMedal?.let { " $it" } ?: "")
            }

            GridRow {
                GridCell(entry.weaponclass.classname, 1.5f)
                GridCell(startTime ?: "-", 1.5f)
                GridCell(if (entry.lane > 0) entry.lane.toString() else "-", 1f)
                GridCell(entry.team.firstOrNull()?.name ?: "-", 2f)
                GridCell(placement ?: "-", 1.0f)
                GridCell(if (entry.registrationFee == 0L) "-" else entry.registrationFee.toString(), 1.5f)
            }
        }
    }
}

@Composable
private fun GridRow(content: @Composable RowScope.() -> Unit) {
    Row(modifier = Modifier.fillMaxWidth()) {
        content()
    }
}

@Composable
private fun RowScope.GridCell(
    text: String,
    weight: Float,
    fontWeight: FontWeight? = null
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = fontWeight,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        softWrap = false,
        modifier = Modifier.weight(weight)
    )
}
