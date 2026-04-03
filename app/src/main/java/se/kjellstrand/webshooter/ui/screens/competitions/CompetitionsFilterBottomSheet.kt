package se.kjellstrand.webshooter.ui.screens.competitions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.data.common.CompetitionType

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CompetitionsFilterBottomSheet(
    allCompetitionTypes: List<CompetitionType>,
    allStatuses: Map<String, String>,
    selectedCompetitionTypeIds: Set<Int>,
    selectedStatuses: Set<String>,
    onCompetitionTypesChange: (Set<Int>) -> Unit,
    onStatusesChange: (Set<String>) -> Unit,
    onDismissRequest: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = bottomSheetState
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.competitions_filter_status),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                allStatuses.forEach { status ->
                    val isSelected = selectedStatuses.contains(status.key)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val updated = if (isSelected) selectedStatuses - status.key
                            else selectedStatuses + status.key
                            onStatusesChange(updated)
                        },
                        label = { Text(status.value) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(R.string.competitions_filter_competition_type),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                allCompetitionTypes.forEach { competitionType ->
                    val isSelected = selectedCompetitionTypeIds.contains(competitionType.id)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val updated =
                                if (isSelected) selectedCompetitionTypeIds - competitionType.id
                                else selectedCompetitionTypeIds + competitionType.id
                            onCompetitionTypesChange(updated)
                        },
                        label = { Text(competitionType.name) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = onDismissRequest) {
                    Text(stringResource(R.string.results_done_button))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
