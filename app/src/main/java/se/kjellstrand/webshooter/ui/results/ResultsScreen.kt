package se.kjellstrand.webshooter.ui.results

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.data.competitions.remote.ResultsType
import se.kjellstrand.webshooter.data.results.remote.Result
import se.kjellstrand.webshooter.ui.common.WeaponClassBadge
import se.kjellstrand.webshooter.ui.common.WeaponClassBadgeSize
import se.kjellstrand.webshooter.ui.mock.ResultsViewModelMock
import se.kjellstrand.webshooter.ui.navigation.Screen

@Composable
fun CompetitionResultsScreen(
    resultsViewModel: ResultsViewModel,
    navController: NavController
) {
    val resultsUiState by resultsViewModel.uiState.collectAsState()
    var isFilterBottomSheetOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        resultsViewModel.resultsEvent.collect { event ->
            when (event) {
                is ResultsEvent.Empty -> {
                    Toast.makeText(
                        context,
                        context.getString(R.string.no_results_found),
                        Toast.LENGTH_LONG
                    ).show()
                    navController.popBackStack()
                }
            }
        }
    }

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
            ResultsList(
                resultsUiState,
                resultsViewModel.competitionId,
                navController,
                resultsUiState.resultsType
            )
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
    resultsUiState: ResultsUiState,
    competitionId: Int,
    navController: NavController,
    resultsType: ResultsType = ResultsType.FIELD
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
            val noneSelected = resultsUiState.selectedWeaponGroups.isEmpty()
                    && resultsUiState.allWeaponGroups.isNotEmpty()

            val allGroupsSelected =
                resultsUiState.selectedWeaponGroups.toSet()
                    .containsAll(resultsUiState.allWeaponGroups.toSet())

            val isLoading = if (noneSelected || allGroupsSelected) {
                resultsUiState.groupedResults.isEmpty()
            } else {
                resultsUiState.filterResults.isEmpty()
            }

            if (noneSelected) {
                // empty — nothing to show
            } else if (isLoading) {
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
                resultsUiState.groupedResults.forEach { group ->
                    item(key = "separator-${group.header}") {
                        WeaponGroupSeparator(group.header)
                    }
                    item {
                        ResultsListHeader(isGrouped = true, resultsType = resultsType)
                    }
                    itemsIndexed(group.items, key = { _, item -> item.id }) { index, result ->
                        ResultItem(
                            result = result,
                            index = index,
                            isGrouped = true,
                            resultsType = resultsType,
                            onItemClick = {
                                navController.navigate(
                                    Screen.ShooterResult.createRoute(
                                        competitionId,
                                        result.signup.user.userID.toInt(),
                                        resultsType.name
                                    )
                                )
                            })
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            } else {
                // FILTERED VIEW (flat list) using itemsIndexed
                item {
                    ResultsListHeader(isGrouped = false, resultsType = resultsType)
                }
                itemsIndexed(
                    resultsUiState.filterResults,
                    key = { _, it -> it.id }) { index, result ->
                    ResultItem(
                        result = result,
                        index = index,
                        isGrouped = false,
                        resultsType = resultsType,
                        onItemClick = {
                            navController.navigate(
                                Screen.ShooterResult.createRoute(
                                    competitionId,
                                    result.signup.user.userID.toInt(),
                                    resultsType.name
                                )
                            )
                        })
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
    }
}

@Composable
fun ResultsListHeader(isGrouped: Boolean, resultsType: ResultsType = ResultsType.FIELD) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderText(
            R.string.placement, modifier = Modifier.weight(2f)
        )
        HeaderText(R.string.name, modifier = Modifier.weight(10f))

        Row(
            modifier = Modifier.weight(if (isGrouped) 8f else 9f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isGrouped) {
                HeaderText(
                    R.string.weapon_class_short,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp)
                )
            }
            HeaderText(R.string.medal_short, modifier = Modifier.weight(1f))
            when (resultsType) {
                ResultsType.FIELD -> {
                    HeaderText(R.string.hits_short, modifier = Modifier.weight(1f))
                    HeaderText(R.string.figures_short, modifier = Modifier.weight(1f))
                    HeaderText(R.string.points_short, modifier = Modifier.weight(1f))
                }

                ResultsType.PRECISION,
                ResultsType.MILITARY -> {
                    HeaderText(R.string.points_short, modifier = Modifier.weight(1f))
                    HeaderText(R.string.x, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun HeaderText(
    stringRes: Int,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center,
    style: TextStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
) {
    Text(
        text = stringResource(stringRes),
        style = style,
        textAlign = textAlign,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Clip,
        modifier = modifier
    )
}

@Composable
fun WeaponGroupSeparator(groupName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        )
        WeaponClassBadge(
            modifier = Modifier.padding(vertical = 4.dp),
            weaponGroupName = groupName,
            isHighlighted = false,
            size = WeaponClassBadgeSize.Large
        )
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
    index: Int,
    isGrouped: Boolean,
    resultsType: ResultsType = ResultsType.FIELD,
    onItemClick: () -> Unit
) {
    val backgroundColor = if (index % 2 == 0) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(onClick = onItemClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ItemText(
            text = result.placement.toString(),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.weight(2f)
        )

        Column(
            modifier = Modifier.weight(10f)
        ) {
            ItemText(
                text = "${result.signup.user.name} ${result.signup.user.lastname}",
                overflow = TextOverflow.Ellipsis
            )
            ItemText(
                text = result.signup.club?.name ?: stringResource(R.string.unknown_club),
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            modifier = Modifier.weight(if (isGrouped) 8f else 9f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isGrouped) {
                ItemText(
                    text = result.weaponClass.classname, modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp)
                )
            }
            ItemText(text = result.stdMedal?.value ?: "-", modifier = Modifier.weight(1f))
            when (resultsType) {
                ResultsType.FIELD -> {
                    ItemText(text = result.hits.toString(), modifier = Modifier.weight(1f))
                    ItemText(text = result.figureHits.toString(), modifier = Modifier.weight(1f))
                    ItemText(text = result.points.toString(), modifier = Modifier.weight(1f))
                }

                ResultsType.PRECISION,
                ResultsType.MILITARY -> {
                    ItemText(text = result.points.toString(), modifier = Modifier.weight(1f))
                    ItemText(text = result.hits.toString(), modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ItemText(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center,
    style: TextStyle = MaterialTheme.typography.bodySmall, // TODO Bold for shooter .copy(fontWeight = FontWeight.Bold),
    overflow: TextOverflow = TextOverflow.Clip
) {
    Text(
        text = text,
        style = style,
        textAlign = textAlign,
        maxLines = 1,
        softWrap = false,
        overflow = overflow,
        modifier = modifier
    )
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
                Text(
                    stringResource(R.string.select_weapon_groups),
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
                        Text(stringResource(R.string.filter_all))
                    }
                    TextButton(onClick = {
                        onFilterChange(filterState.copy(selectedWeaponGroups = emptySet()))
                    }) {
                        Text(stringResource(R.string.filter_none))
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
    FlowRow {
        weaponGroups.forEach { weaponGroup ->
            val isSelected = filterState.selectedWeaponGroups.contains(weaponGroup)
            WeaponClassBadge(
                weaponGroupName = weaponGroup,
                isHighlighted = isSelected,
                size = WeaponClassBadgeSize.Large,
                modifier = Modifier.clickable {
                    val newSelectedGroups = if (isSelected) {
                        filterState.selectedWeaponGroups - weaponGroup
                    } else {
                        filterState.selectedWeaponGroups + weaponGroup
                    }
                    onFilterChange(filterState.copy(selectedWeaponGroups = newSelectedGroups))
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ResultsScreenPreview() {
    CompetitionResultsScreen(
        resultsViewModel = ResultsViewModelMock(),
        navController = NavController(LocalContext.current)
    )
}
