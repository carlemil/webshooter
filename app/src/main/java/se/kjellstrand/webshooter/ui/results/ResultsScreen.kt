package se.kjellstrand.webshooter.ui.results

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.data.results.remote.Result
import se.kjellstrand.webshooter.ui.mock.ResultsViewModelMock

@Composable
fun CompetitionResultsScreen(
    resultsViewModel: ResultsViewModel
) {
    val resultsUiState by resultsViewModel.uiState.collectAsState()
    var isFilterBottomSheetOpen  by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { isFilterBottomSheetOpen = true }) {
                Icon(imageVector = Icons.Default.FilterList, contentDescription = "Open Filters")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            ResultsList(resultsUiState)
        }
    }

    if (isFilterBottomSheetOpen) {
        FilterBottomSheet(
            allWeaponGroups = resultsUiState.allWeaponGroups,
            filterState = FilterState(selectedWeaponGroups = resultsUiState.selectedWeaponGroups),
            onFilterChange = { newFilterState ->
                resultsViewModel.setSelectedWeaponGroups(newFilterState.selectedWeaponGroups)
            },
            onDismissRequest = { isFilterBottomSheetOpen = false }
        )
    }
}

@Composable
fun ResultsList(
    resultsUiState: ResultsUiState
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 16.dp,
                start = 0.dp,
                end = 0.dp,
                bottom = 16.dp
            )
        ) {
            val allGroupsSelected = resultsUiState.selectedWeaponGroups.toSet().containsAll(resultsUiState.allWeaponGroups.toSet())
            
            val isLoading = if (allGroupsSelected) {
                resultsUiState.groupedResults.isEmpty()
            } else {
                resultsUiState.filterResults.isEmpty()
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (allGroupsSelected) {
                // GROUPED VIEW with separators and running index
                var totalItemCount = 0
                resultsUiState.groupedResults.forEach { group ->
                    item(key = "separator-${group.header}") {
                        WeaponGroupSeparator(group.header)
                    }
                    items(group.items, key = { it.id }) { result ->
                        ResultItem(result = result, index = totalItemCount)
                        Spacer(modifier = Modifier.height(2.dp))
                        totalItemCount++
                    }
                }
            } else {
                // FILTERED VIEW (flat list) using itemsIndexed
                itemsIndexed(resultsUiState.filterResults, key = { _, it -> it.id }) { index, result ->
                    ResultItem(result = result, index = index)
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
    }
}

@Composable
fun WeaponGroupSeparator(groupName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Divider
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        )
        // Group Name Text
        Text(
            text = groupName,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        // Right Divider
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        )
    }
}

@Composable
fun ResultItem(
    result: Result,
    index: Int
) {
    val backgroundColor = if (index % 2 != 0) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Placement (Left side)
        Text(
            text = result.placement.toString(),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .weight(1.5f)
                .padding(start = 16.dp)
        )

        // 2. Name & Club (Left center)
        Column(
            modifier = Modifier.weight(10f)
        ) {
            Text(
                text = "${result.signup.user.name} ${result.signup.user.lastname}",
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = result.signup.club?.name ?: stringResource(R.string.unknown_club),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 3. Weapon Class / Group (Center right)
        Text(
            text = result.weaponClass.classname,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(2f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // 4. Score (Right side) - Now split into 3 columns
        Row(
            modifier = Modifier
                .weight(5f)
                .padding(end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hits
            Text(
                text = result.hits.toString(),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            // Figure Hits
            Text(
                text = result.figureHits.toString(),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            // Points
            Text(
                text = result.points.toString(),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
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
                Text(stringResource(R.string.select_weapon_groups), style = MaterialTheme.typography.titleMedium)
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
                        Text(stringResource(R.string.clear))
                    }
                    Button(onClick = onDismissRequest) {
                        Text(stringResource(R.string.done_button))
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterOptionsContent(
    weaponGroups: List<String>,
    filterState: FilterState,
    onFilterChange: (FilterState) -> Unit
) {
    FlowRow(
    ) {
        weaponGroups.forEach { weaponGroup ->
            val isSelected = filterState.selectedWeaponGroups.contains(weaponGroup)
            FilterChip(
                selected = isSelected,
                onClick = {
                    val newSelectedGroups = if (isSelected) {
                        filterState.selectedWeaponGroups - weaponGroup
                    } else {
                        filterState.selectedWeaponGroups + weaponGroup
                    }
                    onFilterChange(filterState.copy(selectedWeaponGroups = newSelectedGroups))
                },
                label = { Text(weaponGroup) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ResultsScreenPreview() {
    CompetitionResultsScreen(
        resultsViewModel = ResultsViewModelMock()
    )
}