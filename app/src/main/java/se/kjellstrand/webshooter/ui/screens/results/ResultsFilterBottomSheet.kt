package se.kjellstrand.webshooter.ui.screens.results

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.ui.common.WeaponClassBadge
import se.kjellstrand.webshooter.ui.common.WeaponClassBadgeSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupingAndFilterBottomSheet(
    allWeaponGroups: List<String>,
    filterState: FilterState,
    onFilterChange: (FilterState) -> Unit,
    onDismissRequest: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = bottomSheetState,
        content = {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Gruppering", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                GroupingMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFilterChange(filterState.copy(groupingMode = mode)) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = filterState.groupingMode == mode,
                            onClick = { onFilterChange(filterState.copy(groupingMode = mode)) }
                        )
                        Text(
                            text = when (mode) {
                                GroupingMode.WEAPON_CLASS -> "Vapenklass"
                                GroupingMode.CLUB -> "Klubb"
                                GroupingMode.MEDL -> "Medl"
                                GroupingMode.NONE -> "Ingen"
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    stringResource(R.string.results_select_weapon_groups),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                FilterOptionsContent(
                    weaponGroups = allWeaponGroups,
                    filterState = filterState,
                    onFilterChange = onFilterChange
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = {
                        onFilterChange(filterState.copy(selectedWeaponGroups = allWeaponGroups.toSet()))
                    }) {
                        Text(stringResource(R.string.results_filter_all))
                    }
                    TextButton(onClick = {
                        onFilterChange(filterState.copy(selectedWeaponGroups = emptySet()))
                    }) {
                        Text(stringResource(R.string.results_filter_none))
                    }
                    Button(onClick = onDismissRequest) {
                        Text(stringResource(R.string.results_done_button))
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterOptionsContent(
    weaponGroups: List<String>,
    filterState: FilterState,
    onFilterChange: (FilterState) -> Unit
) {
    FlowRow {
        weaponGroups.forEach { weaponGroup ->
            val isSelected = filterState.selectedWeaponGroups.contains(weaponGroup)
            WeaponClassBadge(
                weaponGroupName = weaponGroup,
                isHighlighted = isSelected,
                size = WeaponClassBadgeSize.Large,
                modifier = Modifier
                    .clickable {
                        val newSelectedGroups = if (isSelected) {
                            filterState.selectedWeaponGroups - weaponGroup
                        } else {
                            filterState.selectedWeaponGroups + weaponGroup
                        }
                        onFilterChange(filterState.copy(selectedWeaponGroups = newSelectedGroups))
                    }
                    .padding(2.dp)
            )
        }
    }
}
